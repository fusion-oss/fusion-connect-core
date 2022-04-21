package com.scoperetail.fusion.connect.core.application.route.orchestrate.bean;

/*-
 * *****
 * fusion-connect-core
 * -----
 * Copyright (C) 2018 - 2022 Scope Retail Systems Inc.
 * -----
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * =====
 */

import static com.scoperetail.fusion.connect.core.common.constant.CharacterConstant.COMMA;
import static com.scoperetail.fusion.connect.core.common.constant.ErrorStatus.ERRORS;
import static com.scoperetail.fusion.connect.core.common.constant.ErrorStatus.INVALID_VALUE;
import static com.scoperetail.fusion.connect.core.common.constant.ErrorStatus.MISSING_MANDATORY_VALUE;
import static com.scoperetail.fusion.connect.core.common.constant.ErrorStatus.PARSE_ERROR;
import static com.scoperetail.fusion.connect.core.common.constant.ErrorStatus.STATUS;
import static com.scoperetail.fusion.connect.core.common.constant.ExchangePropertyConstants.EVENT;
import static com.scoperetail.fusion.connect.core.common.constant.ExchangePropertyConstants.IS_VALID_MESSAGE;
import static com.scoperetail.fusion.connect.core.common.constant.ExchangePropertyConstants.MANDATORY_HEADERS_VALIDATOR_URI;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import com.scoperetail.fusion.connect.core.common.util.JsonUtils;
import com.scoperetail.fusion.connect.core.config.Event;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HeaderValidator {
  private static final String MANDATORY_HEADERS = "mandatoryHeaders";

  private enum HeaderValidation {
    CHECK_MISSING_VALUE_FIELDS,
    CHECK_EMPTY_VALUE_FIELDS,
    CHECK_INVALID_VALUE_FIELDS;
  }

  public void validateHeaders(final Message message) throws Exception {
    final Exchange exchange = message.getExchange();
    final boolean isValidMessage = exchange.getProperty(IS_VALID_MESSAGE, Boolean.class);
    if (isValidMessage) {
      final Event event = exchange.getProperty(EVENT, Event.class);
      final Optional<Object> optMandatoryHeaders = getMandatoryHeaders(event);
      if (optMandatoryHeaders.isPresent()) {
        final String mandatoryHeadersStr = String.valueOf(optMandatoryHeaders.get());
        if (StringUtils.isNotBlank(mandatoryHeadersStr)) {
          log.info("Validating mandatory headers: {}", mandatoryHeadersStr);
          validateMandatoryHeaders(message, exchange, event, mandatoryHeadersStr);
        }
      }
    }
  }

  private void validateMandatoryHeaders(
      final Message message,
      final Exchange exchange,
      final Event event,
      final String mandatoryHeadersStr)
      throws FileNotFoundException {
    final Map<String, String> mandatoryHeadersTypeByNameMap =
        getMandatoryHeaders(mandatoryHeadersStr);
    final Map<String, Object> exchangeHeadersMap = message.getHeaders();
    boolean isValidMessage = true;
    log.info("Message headers: {}", exchangeHeadersMap.toString());
    for (final HeaderValidation headerValidation : HeaderValidation.values()) {
      switch (headerValidation) {
        case CHECK_MISSING_VALUE_FIELDS:
          isValidMessage =
              setMissingValueFields(
                  exchange,
                  exchangeHeadersMap.keySet(),
                  new HashSet<>(mandatoryHeadersTypeByNameMap.keySet()));
          log.info(
              HeaderValidation.CHECK_MISSING_VALUE_FIELDS.name() + "result is {}", isValidMessage);
          break;
        case CHECK_EMPTY_VALUE_FIELDS:
          isValidMessage =
              setEmptyValueFields(
                  exchange, exchangeHeadersMap, mandatoryHeadersTypeByNameMap.keySet());
          log.info(
              HeaderValidation.CHECK_EMPTY_VALUE_FIELDS.name() + "result is {}", isValidMessage);
          break;
        case CHECK_INVALID_VALUE_FIELDS:
          isValidMessage =
              setInvalidValueFields(
                  exchange, event, mandatoryHeadersTypeByNameMap, exchangeHeadersMap);
          log.info(
              HeaderValidation.CHECK_INVALID_VALUE_FIELDS.name() + "result is {}", isValidMessage);
          break;
      }
      if (!isValidMessage) {
        exchange.setProperty(IS_VALID_MESSAGE, isValidMessage);
        break;
      }
    }
  }

  private boolean setInvalidValueFields(
      final Exchange exchange,
      final Event event,
      final Map<String, String> mandatoryHeadersTypeByNameMap,
      final Map<String, Object> exchangeHeadersMap)
      throws FileNotFoundException {

    Set<String> invalidValueFields =
        validateHeaderDataType(exchangeHeadersMap, mandatoryHeadersTypeByNameMap);
    boolean isValidMessage = true;
    if (CollectionUtils.isEmpty(invalidValueFields)) {
      final String mandatoryHeadersValidatorUri =
          exchange.getProperty(MANDATORY_HEADERS_VALIDATOR_URI, String.class);
      if (StringUtils.isNotBlank(mandatoryHeadersValidatorUri)) {
        invalidValueFields =
            validateHeaderSchema(
                mandatoryHeadersValidatorUri, exchangeHeadersMap, mandatoryHeadersTypeByNameMap);
      }
    }
    if (CollectionUtils.isNotEmpty(invalidValueFields)) {
      isValidMessage = false;
      exchange.setProperty(STATUS, INVALID_VALUE.getErrorStatus());
      exchange.setProperty(ERRORS, invalidValueFields);
    }

    return isValidMessage;
  }

  private Set<String> validateHeaderDataType(
      final Map<String, Object> exchangeHeadersMap,
      final Map<String, String> mandatoryHeadersTypeByNameMap) {
    final Set<String> invalidValueFields = new HashSet<>(1);
    mandatoryHeadersTypeByNameMap.forEach(
        (headerName, headerDataType) -> {
          Object object = exchangeHeadersMap.get(headerName);
          try {
            object = updateExchangeHeaderValue(object, headerDataType);
            exchangeHeadersMap.put(headerName, object);
          } catch (final Exception e) {
            log.error("Exception occured while updating data type for exchange header: {}", e);
          }
          try {
            final Class<?> clazz = Class.forName("java.lang." + headerDataType);
            if (!clazz.isInstance(object)) {
              invalidValueFields.add(headerName);
            }
          } catch (final ClassNotFoundException e) {
            //This is configuration error
            log.error(
                "Invalid data type is configured for the header. headerName: {}, dataType:{}",
                headerName,
                headerDataType);
            invalidValueFields.add(headerName);
          }
        });
    return invalidValueFields;
  }

  private Object updateExchangeHeaderValue(
      Object exchangeHeaderValue, final String headerDataType) {
    switch (headerDataType) {
      case "Integer":
        exchangeHeaderValue = Integer.valueOf(exchangeHeaderValue.toString());
        break;
      case "Long":
        exchangeHeaderValue = Long.valueOf(exchangeHeaderValue.toString());
        break;
      case "String": //By default exchange headers are string.
      default: //Do nothing.
    }
    return exchangeHeaderValue;
  }

  private Set<String> validateHeaderSchema(
      final String mandatoryHeadersValidatorUri,
      final Map<String, Object> messageHeadersByNameMap,
      final Map<String, String> mandatoryHeadersByNameMap)
      throws FileNotFoundException {
    final JsonNode jsonNode =
        JsonUtils.mapper.valueToTree(
            getMandatoryHeadersMap(messageHeadersByNameMap, mandatoryHeadersByNameMap.keySet()));
    final JsonSchema schema =
        JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7)
            .getSchema(new FileInputStream(mandatoryHeadersValidatorUri));
    final Set<ValidationMessage> validationResult = schema.validate(jsonNode);
    return getInvalidValueFields(validationResult);
  }

  private Set<String> getInvalidValueFields(final Set<ValidationMessage> validationResult) {
    final Set<String> invalidValueFields = new HashSet<>(validationResult.size());
    validationResult.forEach(
        result -> {
          final String[] split = result.getPath().split("\\.");
          if (split.length > 0) {
            invalidValueFields.add(split[split.length - 1]);
          }
        });
    return invalidValueFields;
  }

  private Map<String, String> getMandatoryHeaders(final String mandatoryHeadersStr) {
    return Arrays.stream(mandatoryHeadersStr.trim().split(COMMA))
        .map(String::trim)
        .collect(Collectors.toSet())
        .stream()
        .map(s -> s.split(":"))
        .collect(Collectors.toMap(a -> a[0], a -> a[1]));
  }

  private boolean setEmptyValueFields(
      final Exchange exchange,
      final Map<String, Object> messageHeaders,
      final Set<String> mandatoryHeaderKeys) {
    final Set<String> headersWithMissingValues = new HashSet<>(1);
    mandatoryHeaderKeys.forEach(
        key -> {
          final Object value = messageHeaders.get(key);
          if (Objects.isNull(value)
              || ((value instanceof String) && StringUtils.isBlank((String) value))) {
            headersWithMissingValues.add(key);
          }
        });
    boolean isValidMessage = true;
    if (CollectionUtils.isNotEmpty(headersWithMissingValues)) {
      isValidMessage = false;
      exchange.setProperty(STATUS, MISSING_MANDATORY_VALUE.getErrorStatus());
      exchange.setProperty(ERRORS, headersWithMissingValues);
    }
    return isValidMessage;
  }

  private Optional<Object> getMandatoryHeaders(final Event eventConfig) {
    final Map<String, Object> eventHeaders = eventConfig.getHeaders();
    return Optional.ofNullable(eventHeaders.get(MANDATORY_HEADERS));
  }

  private boolean setMissingValueFields(
      final Exchange exchange,
      final Set<String> messageHeaderKeys,
      final Set<String> mandatoryHeaderKeys) {
    mandatoryHeaderKeys.removeAll(messageHeaderKeys);
    boolean isValidMessage = true;
    if (CollectionUtils.isNotEmpty(mandatoryHeaderKeys)) {
      isValidMessage = false;
      exchange.setProperty(STATUS, PARSE_ERROR.getErrorStatus());
      exchange.setProperty(ERRORS, mandatoryHeaderKeys);
    }
    return isValidMessage;
  }

  private Map<String, Object> getMandatoryHeadersMap(
      final Map<String, Object> messageHeadersByNameMap, final Set<String> mandatoryHeaderNames) {
    final Map<String, Object> mandatoryHeadersMap = new HashMap<>();
    mandatoryHeaderNames.forEach(
        headerName -> mandatoryHeadersMap.put(headerName, messageHeadersByNameMap.get(headerName)));
    return mandatoryHeadersMap;
  }
}