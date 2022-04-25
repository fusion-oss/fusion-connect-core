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

import static com.scoperetail.fusion.connect.core.common.constant.CamelComponentConstants.FILE_COMPONENT;
import static com.scoperetail.fusion.connect.core.common.constant.CamelComponentConstants.JSON_VALIDATOR;
import static com.scoperetail.fusion.connect.core.common.constant.CamelComponentConstants.XML_VALIDATOR;
import static com.scoperetail.fusion.connect.core.common.constant.ExchangePropertyConstants.CONFIG_LOOK_UP_KEY;
import static com.scoperetail.fusion.connect.core.common.constant.ExchangePropertyConstants.EVENT;
import static com.scoperetail.fusion.connect.core.common.constant.ExchangePropertyConstants.EVENT_DATA_MAP;
import static com.scoperetail.fusion.connect.core.common.constant.ExchangePropertyConstants.EVENT_FORMAT;
import static com.scoperetail.fusion.connect.core.common.constant.ExchangePropertyConstants.HEADER_CUSTOMIZER_TEMPLATE_URI;
import static com.scoperetail.fusion.connect.core.common.constant.ExchangePropertyConstants.IDEMPOTENCY_KEY;
import static com.scoperetail.fusion.connect.core.common.constant.ExchangePropertyConstants.IS_VALID_MESSAGE;
import static com.scoperetail.fusion.connect.core.common.constant.ExchangePropertyConstants.MANDATORY_HEADERS_VALIDATOR_URI;
import static com.scoperetail.fusion.connect.core.common.constant.ExchangePropertyConstants.SOURCE;
import static com.scoperetail.fusion.connect.core.common.constant.ExchangePropertyConstants.SOURCE_ERROR_HEADER_TEMPLATE_URI;
import static com.scoperetail.fusion.connect.core.common.constant.ExchangePropertyConstants.SOURCE_ERROR_PAYLOAD_TEMPLATE_URI;
import static com.scoperetail.fusion.connect.core.common.constant.ExchangePropertyConstants.TRANSFORMER_TEMPLATE_URI;
import static com.scoperetail.fusion.connect.core.common.constant.ExchangePropertyConstants.VALIDATOR_URI;
import static com.scoperetail.fusion.connect.core.common.constant.Format.JSON;
import static com.scoperetail.fusion.connect.core.common.constant.Format.XML;
import static com.scoperetail.fusion.connect.core.common.constant.ResourceNameConstants.CONFIG_LOOKUP_KEY_TEMPLATE_NAME;
import static com.scoperetail.fusion.connect.core.common.constant.ResourceNameConstants.ERROR_HEADER_TEMPLATE_NAME;
import static com.scoperetail.fusion.connect.core.common.constant.ResourceNameConstants.ERROR_PAYLOAD_TEMPLATE_NAME;
import static com.scoperetail.fusion.connect.core.common.constant.ResourceNameConstants.HEADER_CUSTOMIZER_TEMPLATE_NAME;
import static com.scoperetail.fusion.connect.core.common.constant.ResourceNameConstants.TRANSFORMER_TEMPLATE_NAME;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Optional;
import org.apache.camel.Exchange;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import com.scoperetail.fusion.connect.core.application.service.transform.impl.DomainToFtlTemplateTransformer;
import com.scoperetail.fusion.connect.core.common.constant.ResourceNameConstants;
import com.scoperetail.fusion.connect.core.config.Event;
import com.scoperetail.fusion.connect.core.config.FusionConfig;
import com.scoperetail.fusion.connect.core.config.Source;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConventionOverConfiguration {
  @Autowired private FusionConfig fusionConfig;
  @Autowired private DomainToFtlTemplateTransformer domainToFtlTemplateTransformer;

  public void setSourceErrorHeaderTemplateURI(final Exchange exchange) {
    final Source source = (Source) exchange.getProperty(SOURCE);
    String errorHeaderTemplateUri = source.getErrorHeaderTemplateUri();
    if (StringUtils.isBlank(errorHeaderTemplateUri)) {
      final Path errorHeaderTemplatePath =
          Paths.get(
              fusionConfig.getResourceDirectory(), source.getName(), ERROR_HEADER_TEMPLATE_NAME);
      final String errorHeaderTemplateFilePath =
          errorHeaderTemplatePath.toAbsolutePath().toString();
      log.debug(
          "Error header template is not configured. Searching at path: {}",
          errorHeaderTemplateFilePath);
      errorHeaderTemplateUri =
          Files.exists(errorHeaderTemplatePath)
              ? FILE_COMPONENT + errorHeaderTemplateFilePath
              : null;
    }
    log.debug(
        "Source error header template URI is set to: {} for Source: {}",
        errorHeaderTemplateUri,
        source.getName());
    exchange.setProperty(SOURCE_ERROR_HEADER_TEMPLATE_URI, errorHeaderTemplateUri);
  }

  public void setSourceErrorPayloadTemplateURI(final Exchange exchange) {
    final Source source = (Source) exchange.getProperty(SOURCE);
    String errorPayloadTemplateUri = source.getErrorPayloadTemplateUri();
    if (StringUtils.isBlank(errorPayloadTemplateUri)) {
      final Path errorPayloadTemplatePath =
          Paths.get(
              fusionConfig.getResourceDirectory(), source.getName(), ERROR_PAYLOAD_TEMPLATE_NAME);
      final String errorPayloadTemplateFilePath =
          errorPayloadTemplatePath.toAbsolutePath().toString();
      log.debug(
          "Error payload template is not configured. Searching at path: {}",
          errorPayloadTemplateFilePath);
      errorPayloadTemplateUri =
          Files.exists(errorPayloadTemplatePath)
              ? FILE_COMPONENT + errorPayloadTemplateFilePath
              : null;
    }
    log.debug(
        "Source error payload template URI is set to: {} for Source: {}",
        errorPayloadTemplateUri,
        source.getName());
    exchange.setProperty(SOURCE_ERROR_PAYLOAD_TEMPLATE_URI, errorPayloadTemplateUri);
  }

  public void setEventLevelMandatoryHeaderValidatorURI(final Exchange exchange) {
    final boolean isValidMessage = exchange.getProperty(IS_VALID_MESSAGE, Boolean.class);
    if (isValidMessage) {
      final Event event = exchange.getProperty(EVENT, Event.class);
      final Map<String, Object> eventHeaders = event.getHeaders();
      Optional<Object> optMandatoryHeadersValidatorUri =
          Optional.ofNullable(eventHeaders.get(MANDATORY_HEADERS_VALIDATOR_URI));
      final Source source = (Source) exchange.getProperty(SOURCE);
      if (!optMandatoryHeadersValidatorUri.isPresent()) {
        final Path mandatoryHeadersValidatorUriPath =
            Paths.get(
                fusionConfig.getResourceDirectory(),
                source.getName(),
                event.getEventType(),
                ResourceNameConstants.MANDATORY_HEADER_SCHEMA_NAME);
        optMandatoryHeadersValidatorUri =
            Optional.ofNullable(
                Files.exists(mandatoryHeadersValidatorUriPath)
                    ? mandatoryHeadersValidatorUriPath.toAbsolutePath().toString()
                    : null);
      }
      if (optMandatoryHeadersValidatorUri.isPresent()) {
        exchange.setProperty(
            MANDATORY_HEADERS_VALIDATOR_URI, String.valueOf(optMandatoryHeadersValidatorUri.get()));
        log.debug(
            "Event level mandatory validator URI is set to:{} for source:{} event:{}",
            optMandatoryHeadersValidatorUri.get(),
            source.getName(),
            event.getEventType());
      }
    }
  }

  public void setEventLevelConfigLookupKey(final Exchange exchange) throws Exception {
    final boolean isValidMessage = exchange.getProperty(IS_VALID_MESSAGE, Boolean.class);
    if (isValidMessage) {
      final Event event = exchange.getProperty(EVENT, Event.class);
      final Source source = (Source) exchange.getProperty(SOURCE);
      String configLookupKey = exchange.getProperty(CONFIG_LOOK_UP_KEY, String.class);
      if (StringUtils.isBlank(configLookupKey)) {
        final Path configLookupKeyTemplatePath =
            Paths.get(
                fusionConfig.getResourceDirectory(),
                source.getName(),
                event.getEventType(),
                CONFIG_LOOKUP_KEY_TEMPLATE_NAME);
        if (Files.exists(configLookupKeyTemplatePath)) {
          configLookupKey =
              domainToFtlTemplateTransformer.transform(
                  exchange.getProperty(EVENT_DATA_MAP, Map.class),
                  FILE_COMPONENT + configLookupKeyTemplatePath.toAbsolutePath().toString());
        }
      }
      if (StringUtils.isNotBlank(configLookupKey)) {
        exchange.setProperty(CONFIG_LOOK_UP_KEY, configLookupKey);
        log.debug(
            "Event level config look up key is set to:{} for source:{} event:{}",
            configLookupKey,
            source.getName(),
            event.getEventType());
      }
    }
  }

  public void setEventLevelIdempotencyKey(final Exchange exchange) throws Exception {
    final boolean isValidMessage = exchange.getProperty(IS_VALID_MESSAGE, Boolean.class);
    if (isValidMessage) {
      final Source source = (Source) exchange.getProperty(SOURCE);
      final Event event = exchange.getProperty(EVENT, Event.class);
      String idempotencyKey = exchange.getProperty(IDEMPOTENCY_KEY, String.class);
      if (StringUtils.isBlank(idempotencyKey)) {
        final Path idempotencyKeyTemplatePath =
            Paths.get(
                fusionConfig.getResourceDirectory(),
                source.getName(),
                event.getEventType(),
                ResourceNameConstants.IDEMPOTENCY_KEY_TEMPLATE_NAME);
        if (Files.exists(idempotencyKeyTemplatePath)) {
          idempotencyKey =
              domainToFtlTemplateTransformer.transform(
                  exchange.getProperty(EVENT_DATA_MAP, Map.class),
                  FILE_COMPONENT + idempotencyKeyTemplatePath.toAbsolutePath().toString());
          exchange.setProperty(IDEMPOTENCY_KEY, idempotencyKey);
        }
      }
      log.info(
          "Event level idempotency key is set to:{} for source:{} event:{}",
          idempotencyKey,
          source.getName(),
          event.getEventType());
    }
  }

  public void setEventLevelSchemaValidatorUri(final Exchange exchange) {
    final boolean isValidMessage = exchange.getProperty(IS_VALID_MESSAGE, Boolean.class);
    if (isValidMessage) {
      String validatorUri = (String) exchange.getProperty(VALIDATOR_URI);
      final Source source = (Source) exchange.getProperty(SOURCE);
      final Event event = exchange.getProperty(EVENT, Event.class);
      if (StringUtils.isBlank(validatorUri)) {
        final String eventFormat = exchange.getProperty(EVENT_FORMAT, String.class);
        String payloadSchemaFileExt = null;
        String jsdOptions = "";
        String camelSchemaValidator = null;
        if (JSON.name().equalsIgnoreCase(eventFormat)) {
          payloadSchemaFileExt = ".jsd";
          camelSchemaValidator = JSON_VALIDATOR;
          jsdOptions = "?schemaLoader=#bean:customJsonSchemaLoader";
        } else if (XML.name().equalsIgnoreCase(eventFormat)) {
          payloadSchemaFileExt = ".xsd";
          camelSchemaValidator = XML_VALIDATOR;
        }
        final Path payloadSchemaPath =
            Paths.get(
                fusionConfig.getResourceDirectory(),
                source.getName(),
                event.getEventType(),
                ResourceNameConstants.PAYLOAD_SCHEMA_NAME + payloadSchemaFileExt);
        if (Files.exists(payloadSchemaPath)) {
          validatorUri =
              camelSchemaValidator
                  + FILE_COMPONENT
                  + payloadSchemaPath.toAbsolutePath().toString()
                  + jsdOptions;
          exchange.setProperty(VALIDATOR_URI, validatorUri);
        }
      }
      log.debug(
          "Event level validator URI is set to:{} for source:{} event:{}",
          validatorUri,
          source.getName(),
          event.getEventType());
    }
  }

  public void setEventLevelTransformerTemplateUri(final Exchange exchange) {
    final boolean isValidMessage = exchange.getProperty(IS_VALID_MESSAGE, Boolean.class);
    if (isValidMessage) {
      String transformerTemplateUri = exchange.getProperty(TRANSFORMER_TEMPLATE_URI, String.class);
      final Source source = (Source) exchange.getProperty(SOURCE);
      final Event event = exchange.getProperty(EVENT, Event.class);
      if (StringUtils.isBlank(transformerTemplateUri)) {
        final Path transformerTemplatePath =
            Paths.get(
                fusionConfig.getResourceDirectory(),
                source.getName(),
                event.getEventType(),
                TRANSFORMER_TEMPLATE_NAME);
        if (Files.exists(transformerTemplatePath)) {
          transformerTemplateUri =
              FILE_COMPONENT + transformerTemplatePath.toAbsolutePath().toString();
          exchange.setProperty(TRANSFORMER_TEMPLATE_URI, transformerTemplateUri);
        }
      }
      log.debug(
          "Event level transformer template URI is set to:{} for source:{} event:{}",
          transformerTemplateUri,
          source.getName(),
          event.getEventType());
    }
  }

  public void setEventLevelHeaderCustomizerTemplateUri(final Exchange exchange) {
    final boolean isValidMessage = exchange.getProperty(IS_VALID_MESSAGE, Boolean.class);
    if (isValidMessage) {
      String headerCustomizerTemplateUri =
          exchange.getProperty(HEADER_CUSTOMIZER_TEMPLATE_URI, String.class);
      final Source source = (Source) exchange.getProperty(SOURCE);
      final Event event = exchange.getProperty(EVENT, Event.class);
      if (StringUtils.isBlank(headerCustomizerTemplateUri)) {
        final Path headerCustomizerTemplatePath =
            Paths.get(
                fusionConfig.getResourceDirectory(),
                source.getName(),
                event.getEventType(),
                HEADER_CUSTOMIZER_TEMPLATE_NAME);
        if (Files.exists(headerCustomizerTemplatePath)) {
          headerCustomizerTemplateUri =
              FILE_COMPONENT + headerCustomizerTemplatePath.toAbsolutePath().toString();
          exchange.setProperty(HEADER_CUSTOMIZER_TEMPLATE_URI, headerCustomizerTemplateUri);
        }
      }
      log.debug(
          "Event level header customizer template URI is set to:{} for source:{} event:{}",
          headerCustomizerTemplateUri,
          source.getName(),
          event.getEventType());
    }
  }
}