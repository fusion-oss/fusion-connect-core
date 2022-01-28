package com.scoperetail.fusion.connect.core.application.route.orchestrate.bean;

/*-
 * *****
 * fusion-connect-core
 * -----
 * Copyright (C) 2018 - 2021 Scope Retail Systems Inc.
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

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import com.scoperetail.fusion.connect.core.common.util.JsonUtils;
import com.scoperetail.fusion.connect.core.config.Event;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.commons.lang3.StringUtils;

import java.io.FileInputStream;
import java.util.*;
import java.util.stream.Collectors;

import static com.scoperetail.fusion.connect.core.common.constant.CharacterConstant.COMMA;
import static org.apache.commons.lang3.StringUtils.LF;

@Slf4j
public class HeaderValidator {
  private static final String MANDATORY_HEADERS = "mandatoryHeaders";
  private static final String MANDATORY_HEADERS_VALIDATOR_URI = "mandatoryHeadersValidatorUri";

  public void validateHeaders(final Message message) throws Exception {
    final Exchange exchange = message.getExchange();
    final boolean isValidMessage = exchange.getProperty("isValidMessage", Boolean.class);
    if (isValidMessage) {
      final String eventType = exchange.getProperty("event.type", String.class);
      final String format = exchange.getProperty("event.format", String.class);
      log.debug(
          "Mandatory header validation started for eventType: {} format: {}", eventType, format);
      final Event eventConfig = exchange.getProperty("event", Event.class);
      final Optional<Object> optMandatoryHeaders = getMandatoryHeaders(eventConfig);
      boolean isValidHeader = false;
      if (optMandatoryHeaders.isPresent()) {
        final String mandatoryHeadersStr = String.valueOf(optMandatoryHeaders.get());
        if (StringUtils.isNotBlank(mandatoryHeadersStr)) {
          final Set<String> mandatoryHeaders =
              Arrays.stream(mandatoryHeadersStr.trim().split(COMMA))
                  .map(String::trim)
                  .collect(Collectors.toSet());
          final Map<String, Object> messageHeaders = message.getHeaders();
          final Set<String> missingHeaders =
              getMissingHeaders(messageHeaders, new HashSet<>(mandatoryHeaders));
          isValidHeader = missingHeaders.isEmpty();
          exchange.setProperty("isValidMessage", isValidHeader);
          exchange.setProperty("missingHeaders", missingHeaders);
          exchange.setProperty("reason", "Missing mandatory headers:" + missingHeaders);
          log.debug(
              "Is mandatory headers provided:{}  Missing headers:{}",
              isValidHeader,
              missingHeaders);

          final Optional<Object> mandatoryHeadersValidatorUri = getMandatoryHeadersUri(eventConfig);
          if (isValidHeader && mandatoryHeadersValidatorUri.isPresent()) {
            // Create JsonNode from Map
            final JsonNode jsonNode =
                JsonUtils.mapper.valueToTree(
                    getMandatoryHeadersMap(messageHeaders, mandatoryHeaders));
            // Initialise schema from JSD location
            final JsonSchema schema =
                JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7)
                    .getSchema(
                        new FileInputStream(String.valueOf(mandatoryHeadersValidatorUri.get())));
            // Validate JsonNode against JSD
            final Set<ValidationMessage> validationResult = schema.validate(jsonNode);
            if (!validationResult.isEmpty()) {
              // Log validation errors if any
              isValidHeader = false;
              final String validationErrors = getValidationErrors(validationResult);
              exchange.setProperty("isValidMessage", isValidHeader);
              exchange.setProperty("reason", validationErrors);
            }
          }
        }
      }
      log.debug(
          "Mandatory header validation completed for eventType: {} format: {} Validation result: {}",
          eventType,
          format,
          isValidHeader);
    }
  }

  private Optional<Object> getMandatoryHeaders(final Event eventConfig) {
    final Map<String, Object> eventHeaders = eventConfig.getHeaders();
    return Optional.ofNullable(eventHeaders.get(MANDATORY_HEADERS));
  }

  private Optional<Object> getMandatoryHeadersUri(final Event eventConfig) {
    final Map<String, Object> eventHeaders = eventConfig.getHeaders();
    return Optional.ofNullable(eventHeaders.get(MANDATORY_HEADERS_VALIDATOR_URI));
  }

  private Set<String> getMissingHeaders(
      final Map<String, Object> messageHeaders, final Set<String> mandatoryHeaders) {
    final Set<String> headerKeys = messageHeaders.keySet();
    mandatoryHeaders.removeAll(headerKeys);
    return mandatoryHeaders;
  }

  private Map<String, Object> getMandatoryHeadersMap(
      final Map<String, Object> messageHeaders, final Set<String> mandatoryHeaders) {
    final Map<String, Object> mandatoryHeadersMap = new HashMap<>();
    for (String key : mandatoryHeaders) {
      mandatoryHeadersMap.put(key, messageHeaders.get(key));
    }
    return mandatoryHeadersMap;
  }

  private String getValidationErrors(Set<ValidationMessage> validationResult) {
    final StringBuilder messageBuilder = new StringBuilder();
    messageBuilder.append(LF);
    messageBuilder.append(
        String.format("Message validation failed with %d errors", validationResult.size()));
    messageBuilder.append(LF);
    for (final ValidationMessage error : validationResult) {
      messageBuilder.append(error.getMessage());
      messageBuilder.append(LF);
    }
    return messageBuilder.toString();
  }
}
