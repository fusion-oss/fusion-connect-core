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

import static com.scoperetail.fusion.connect.core.common.constant.CharacterConstant.DOLLAR_SIGN;
import static com.scoperetail.fusion.connect.core.common.constant.CharacterConstant.FORWARD_SLASH;
import static com.scoperetail.fusion.connect.core.common.constant.ExchangePropertyConstants.EVENT;
import static com.scoperetail.fusion.connect.core.common.constant.ExchangePropertyConstants.EVENT_FORMAT;
import static com.scoperetail.fusion.connect.core.common.constant.ExchangePropertyConstants.IS_VALID_MESSAGE;
import static com.scoperetail.fusion.connect.core.common.constant.Format.JSON;
import static com.scoperetail.fusion.connect.core.common.constant.Format.XML;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.xml.sax.SAXException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.scoperetail.fusion.connect.core.application.service.transform.impl.DomainToFtlTemplateTransformer;
import com.scoperetail.fusion.connect.core.common.helper.DocumentBuilderHelper;
import com.scoperetail.fusion.connect.core.common.util.JsonUtils;
import com.scoperetail.fusion.connect.core.config.Event;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ComputeHeader {
  private static final String FTL_EXTENSION = ".ftl";

  @Autowired private DomainToFtlTemplateTransformer domainToFtlTemplateTransformer;
  @Autowired private DocumentBuilderHelper documentBuilderHelper;

  public void process(final Message message, final Exchange exchange) throws Exception {
    final boolean isValidMessage = exchange.getProperty(IS_VALID_MESSAGE, Boolean.class);
    if (isValidMessage) {
      final Event eventConfig = exchange.getProperty(EVENT, Event.class);
      final String format = exchange.getProperty(EVENT_FORMAT, String.class);
      log.debug(
          "Computing event headers started for eventType: {} format: {}",
          eventConfig.getEventType(),
          format);
      final String payload = message.getBody(String.class);
      final Object document = getDocument(format, payload);
      final Map<String, Object> eventObject = new HashMap<>();
      for (final Entry<String, Object> entry : eventConfig.getHeaders().entrySet()) {
        final String headerKey = entry.getKey();
        final Object headerValue = entry.getValue();
        Object computedValue = null;
        if (headerValue.toString().startsWith(DOLLAR_SIGN)) {
          computedValue = ((DocumentContext) document).read(headerValue.toString());
        } else if (headerValue.toString().startsWith(FORWARD_SLASH)) {
          final XPath xPath = XPathFactory.newInstance().newXPath();
          computedValue = xPath.compile(headerValue.toString()).evaluate(document);
        } else if (headerValue.toString().endsWith(FTL_EXTENSION)) {
          computedValue =
              computeValueUsingFtl(
                  eventConfig.getEventType(),
                  format,
                  payload,
                  message.getHeaders(),
                  eventObject,
                  headerValue.toString());
        } else {
          computedValue = headerValue;
        }
        log.trace(
            "headerKey: {} headerValue: {} computedValue: {}",
            headerKey,
            headerValue,
            computedValue);
        exchange.setProperty(headerKey, computedValue);
      }
      log.debug(
          "Computing event headers completed for eventType: {} format: {}",
          eventConfig.getEventType(),
          format);
    }
  }

  private Object computeValueUsingFtl(
      final String eventType,
      final String format,
      final String payload,
      final Map<String, Object> messageHeaders,
      final Map<String, Object> eventObject,
      final String templatePath)
      throws Exception {
    if (eventObject.isEmpty()) {
      eventObject.putAll(messageHeaders);
      if (JSON.name().equalsIgnoreCase(format)) {
        eventObject.putAll(
            JsonUtils.unmarshal(Optional.ofNullable(payload), Map.class.getCanonicalName()));
      } else if (XML.name().equalsIgnoreCase(format)) {
        final XmlMapper xmlMapper = new XmlMapper();
        eventObject.putAll(xmlMapper.readValue(payload, Map.class));
      }
    }
    return domainToFtlTemplateTransformer.transform(eventType, eventObject, templatePath);
  }

  private Object getDocument(final String format, final String payload)
      throws SAXException, IOException {
    Object document = null;
    if (JSON.name().equalsIgnoreCase(format)) {
      document = JsonPath.parse(payload);
    } else if (XML.name().equalsIgnoreCase(format)) {
      final InputStream is = new ByteArrayInputStream(payload.getBytes());
      document = documentBuilderHelper.getDocumentBuilder().parse(is);
    }
    return document;
  }
}
