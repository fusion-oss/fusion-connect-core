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

import static com.scoperetail.fusion.connect.core.application.service.transform.template.engine.FreemarkerTemplateEngine.FTL_EXTENSION;
import static com.scoperetail.fusion.connect.core.common.constant.CharacterConstant.DOLLAR_SIGN;
import static com.scoperetail.fusion.connect.core.common.constant.CharacterConstant.FORWARD_SLASH;
import static com.scoperetail.fusion.connect.core.common.constant.ExchangePropertyConstants.EVENT;
import static com.scoperetail.fusion.connect.core.common.constant.ExchangePropertyConstants.EVENT_DATA_MAP;
import static com.scoperetail.fusion.connect.core.common.constant.ExchangePropertyConstants.EVENT_FORMAT;
import static com.scoperetail.fusion.connect.core.common.constant.ExchangePropertyConstants.IS_VALID_MESSAGE;
import static com.scoperetail.fusion.connect.core.common.constant.Format.JSON;
import static com.scoperetail.fusion.connect.core.common.constant.Format.XML;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Map.Entry;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.xml.sax.SAXException;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.scoperetail.fusion.connect.core.application.service.transform.impl.DomainToFtlTemplateTransformer;
import com.scoperetail.fusion.connect.core.common.helper.DocumentBuilderHelper;
import com.scoperetail.fusion.connect.core.config.Event;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ComputeHeader {
  @Autowired private DocumentBuilderHelper documentBuilderHelper;
  @Autowired private DomainToFtlTemplateTransformer domainToFtlTemplateTransformer;

  public void process(final Message message, final Exchange exchange) throws Exception {
    final boolean isValidMessage = exchange.getProperty(IS_VALID_MESSAGE, Boolean.class);
    if (isValidMessage) {
      final Event eventConfig = exchange.getProperty(EVENT, Event.class);
      final String format = exchange.getProperty(EVENT_FORMAT, String.class);
      final Map<String, Object> eventData = exchange.getProperty(EVENT_DATA_MAP, Map.class);
      log.debug(
          "Computing event headers started for eventType: {} format: {}",
          eventConfig.getEventType(),
          format);
      final String payload = message.getBody(String.class);
      final Object document = getDocument(format, payload);
      for (final Entry<String, Object> entry : eventConfig.getHeaders().entrySet()) {
        final String headerKey = entry.getKey();
        final Object headerValue = entry.getValue();
        Object computedValue = null;
        if (headerValue.toString().endsWith(FTL_EXTENSION)) {
          computedValue =
              domainToFtlTemplateTransformer.transform(eventData, headerValue.toString());
        } else if (headerValue.toString().startsWith(DOLLAR_SIGN)) {
          computedValue = ((DocumentContext) document).read(headerValue.toString());
        } else if (headerValue.toString().startsWith(FORWARD_SLASH)) {
          final XPath xPath = XPathFactory.newInstance().newXPath();
          computedValue = xPath.compile(headerValue.toString()).evaluate(document);
        } else {
          computedValue = headerValue;
        }
        log.debug(
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