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
import static com.scoperetail.fusion.connect.core.common.constant.ExchangePropertyConstants.ERROR_HEADER_TEMPLATE_URI;
import static com.scoperetail.fusion.connect.core.common.constant.ExchangePropertyConstants.ERROR_PAYLOAD_TEMPLATE_URI;
import static com.scoperetail.fusion.connect.core.common.constant.ExchangePropertyConstants.EVENT;
import static com.scoperetail.fusion.connect.core.common.constant.ExchangePropertyConstants.EVENT_FORMAT;
import static com.scoperetail.fusion.connect.core.common.constant.ExchangePropertyConstants.IDEMPOTENCY_KEY;
import static com.scoperetail.fusion.connect.core.common.constant.ExchangePropertyConstants.IS_VALID_MESSAGE;
import static com.scoperetail.fusion.connect.core.common.constant.ExchangePropertyConstants.MANDATORY_HEADERS_VALIDATOR_URI;
import static com.scoperetail.fusion.connect.core.common.constant.ExchangePropertyConstants.SOURCE;
import static com.scoperetail.fusion.connect.core.common.constant.ExchangePropertyConstants.TRANSFORMER_TEMPLATE_URI;
import static com.scoperetail.fusion.connect.core.common.constant.ExchangePropertyConstants.VALIDATOR_URI;
import static com.scoperetail.fusion.connect.core.common.constant.Format.JSON;
import static com.scoperetail.fusion.connect.core.common.constant.Format.XML;
import static com.scoperetail.fusion.connect.core.common.constant.ResourceNameConstants.CONFIG_LOOKUP_KEY_TEMPLATE_NAME;
import static com.scoperetail.fusion.connect.core.common.constant.ResourceNameConstants.ERROR_HEADER_TEMPLATE_NAME;
import static com.scoperetail.fusion.connect.core.common.constant.ResourceNameConstants.ERROR_PAYLOAD_TEMPLATE_NAME;
import static com.scoperetail.fusion.connect.core.common.constant.ResourceNameConstants.TRANSFORMER_TEMPLATE_NAME;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javax.annotation.PostConstruct;
import org.apache.camel.Exchange;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.scoperetail.fusion.connect.core.application.service.transform.impl.DomainToFtlTemplateTransformer;
import com.scoperetail.fusion.connect.core.common.constant.ResourceNameConstants;
import com.scoperetail.fusion.connect.core.common.util.JsonUtils;
import com.scoperetail.fusion.connect.core.config.Event;
import com.scoperetail.fusion.connect.core.config.ResourceManager;
import com.scoperetail.fusion.connect.core.config.Source;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConventionOverConfiguration {

  private static final String ZIP_DIR = "sourceDir";
  private String resourceDirBasePath;
  @Autowired private DomainToFtlTemplateTransformer domainToFtlTemplateTransformer;
  @Autowired private ResourceManager resourceManager;

  @PostConstruct
  private void init() {
    resourceDirBasePath =
        Paths.get(resourceManager.getResourceDirectoryBasePath(), ZIP_DIR)
            .toAbsolutePath()
            .toString();
  }

  public static final String SET_MANDATORY_HEADER_VALIDATOR_URI = "setMandatoryHeaderValidatorURI";
  public static final String SET_ERROR_TEMPLATE_URI = "setErrorTemplateURI";
  public static final String SET_TEMPLATE_URI = "setTemplateURI";

  public void setErrorTemplateURI(final Exchange exchange) {
    final boolean isValidMessage = exchange.getProperty(IS_VALID_MESSAGE, Boolean.class);
    if (isValidMessage) {
      final Source source = (Source) exchange.getProperty(SOURCE);
      final String sourceName = source.getName();
      exchange.setProperty(
          ERROR_HEADER_TEMPLATE_URI,
          getTemplateUri(
              sourceName, source.getErrorHeaderTemplateUri(), ERROR_HEADER_TEMPLATE_NAME));

      exchange.setProperty(
          ERROR_PAYLOAD_TEMPLATE_URI,
          getTemplateUri(
              sourceName, source.getErrorPayloadTemplateUri(), ERROR_PAYLOAD_TEMPLATE_NAME));
    }
  }

  public void setMandatoryHeaderValidatorURI(final Exchange exchange) {
    final boolean isValidMessage = exchange.getProperty(IS_VALID_MESSAGE, Boolean.class);
    if (isValidMessage) {
      final Event event = exchange.getProperty(EVENT, Event.class);
      final Map<String, Object> eventHeaders = event.getHeaders();
      Optional<Object> optMandatoryHeadersValidatorUri =
          Optional.ofNullable(eventHeaders.get(MANDATORY_HEADERS_VALIDATOR_URI));
      if (!optMandatoryHeadersValidatorUri.isPresent()) {
        final Source source = (Source) exchange.getProperty(SOURCE);
        final Path path =
            getPath(
                resourceDirBasePath,
                source.getName(),
                event.getEventType(),
                ResourceNameConstants.MANDATORY_HEADER_SCHEMA_NAME);
        optMandatoryHeadersValidatorUri =
            Optional.ofNullable(Files.exists(path) ? path.toString() : null);
      }
      if (optMandatoryHeadersValidatorUri.isPresent()) {
        log.debug("Mandatory validator URI is:{}", optMandatoryHeadersValidatorUri.get());
        exchange.setProperty(
            MANDATORY_HEADERS_VALIDATOR_URI, String.valueOf(optMandatoryHeadersValidatorUri.get()));
      }
    }
  }

  public void setTemplateURI(final Exchange exchange) throws Exception {
    final boolean isValidMessage = exchange.getProperty(IS_VALID_MESSAGE, Boolean.class);
    if (isValidMessage) {
      setConfigLookupKey(exchange);
      setIdempotencyKey(exchange);
      setValidatorUri(exchange);
      setTransformerTemplateUri(exchange);
    }
  }

  private void setConfigLookupKey(final Exchange exchange) throws Exception {
    String configLookupKey = exchange.getProperty(CONFIG_LOOK_UP_KEY, String.class);
    configLookupKey =
        StringUtils.isBlank(configLookupKey) ? getConfigLookupKey(exchange) : configLookupKey;
    if (StringUtils.isNotBlank(configLookupKey)) {
      exchange.setProperty(CONFIG_LOOK_UP_KEY, configLookupKey);
    }
  }

  private void setIdempotencyKey(final Exchange exchange) throws Exception {
    String idempotencyKey = exchange.getProperty(IDEMPOTENCY_KEY, String.class);
    if (StringUtils.isBlank(idempotencyKey)) {
      final Source source = (Source) exchange.getProperty(SOURCE);
      final Event event = exchange.getProperty(EVENT, Event.class);
      final Path idempotencyKeyTemplatePath =
          Paths.get(
              resourceDirBasePath,
              source.getName(),
              event.getEventType(),
              ResourceNameConstants.IDEMPOTENCY_KEY_TEMPLATE_NAME);
      if (Files.exists(idempotencyKeyTemplatePath)) {
        idempotencyKey =
            computeValueUsingFtl(
                event.getEventType(),
                exchange.getProperty(EVENT_FORMAT, String.class),
                exchange.getMessage().getBody(String.class),
                exchange.getMessage().getHeaders(),
                FILE_COMPONENT + idempotencyKeyTemplatePath.toString());
        exchange.setProperty(IDEMPOTENCY_KEY, idempotencyKey);
      }
    }
  }

  private void setValidatorUri(final Exchange exchange) {
    final String validatorUri = (String) exchange.getProperty(VALIDATOR_URI);
    if (StringUtils.isBlank(validatorUri)) {
      final Source source = (Source) exchange.getProperty(SOURCE);
      final Event event = exchange.getProperty(EVENT, Event.class);
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
              resourceDirBasePath,
              source.getName(),
              event.getEventType(),
              ResourceNameConstants.PAYLOAD_SCHEMA_NAME + payloadSchemaFileExt);
      if (Files.exists(payloadSchemaPath)) {
        exchange.setProperty(
            VALIDATOR_URI,
            camelSchemaValidator + FILE_COMPONENT + payloadSchemaPath.toString() + jsdOptions);
      }
    }
  }

  private void setTransformerTemplateUri(final Exchange exchange) {
    String transformerTemplateUri = exchange.getProperty(TRANSFORMER_TEMPLATE_URI, String.class);
    if (StringUtils.isBlank(transformerTemplateUri)) {
      final Source source = (Source) exchange.getProperty(SOURCE);
      final Event event = exchange.getProperty(EVENT, Event.class);
      final Path transformerTemplatePath =
          Paths.get(
              resourceDirBasePath,
              source.getName(),
              event.getEventType(),
              TRANSFORMER_TEMPLATE_NAME);
      if (Files.exists(transformerTemplatePath)) {
        transformerTemplateUri = FILE_COMPONENT + transformerTemplatePath.toString();
        exchange.setProperty(TRANSFORMER_TEMPLATE_URI, transformerTemplateUri);
      }
    }
  }

  private String getTemplateUri(
      final String sourceName, String templateUri, final String templateName) {
    if (StringUtils.isBlank(templateUri)) {
      final Path errorHeaderTemplatePath = getPath(resourceDirBasePath, sourceName, templateName);
      templateUri =
          Files.exists(errorHeaderTemplatePath)
              ? FILE_COMPONENT + errorHeaderTemplatePath.toString()
              : null;
    }
    return templateUri;
  }

  private Path getPath(final String first, final String... more) {
    return Paths.get(first, more);
  }

  private String getConfigLookupKey(final Exchange exchange) throws Exception {
    final Event event = exchange.getProperty(EVENT, Event.class);
    final Source source = (Source) exchange.getProperty(SOURCE);
    final Path configLookupKeyTemplatePath =
        Paths.get(
            resourceDirBasePath,
            source.getName(),
            event.getEventType(),
            CONFIG_LOOKUP_KEY_TEMPLATE_NAME);
    String configLookupKey = null;
    if (Files.exists(configLookupKeyTemplatePath)) {
      configLookupKey =
          computeValueUsingFtl(
              event.getEventType(),
              exchange.getProperty(EVENT_FORMAT, String.class),
              exchange.getMessage().getBody(String.class),
              exchange.getMessage().getHeaders(),
              FILE_COMPONENT + configLookupKeyTemplatePath.toString());
    }
    return configLookupKey;
  }

  private String computeValueUsingFtl(
      final String eventType,
      final String format,
      final String payload,
      final Map<String, Object> messageHeaders,
      final String templatePath)
      throws Exception {
    final Map<String, Object> eventObject = new HashMap<>();
    eventObject.putAll(messageHeaders);
    if (JSON.name().equalsIgnoreCase(format)) {
      eventObject.putAll(
          JsonUtils.unmarshal(Optional.ofNullable(payload), Map.class.getCanonicalName()));
    } else if (XML.name().equalsIgnoreCase(format)) {
      final XmlMapper xmlMapper = new XmlMapper();
      eventObject.putAll(xmlMapper.readValue(payload, Map.class));
    }
    return domainToFtlTemplateTransformer.transform(eventType, eventObject, templatePath);
  }
}
