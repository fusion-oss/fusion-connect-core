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

import static com.scoperetail.fusion.connect.core.common.constant.CharacterConstant.SQUARE_BRACKET;
import static com.scoperetail.fusion.connect.core.common.constant.CharacterConstant.UNDERSCORE;
import static com.scoperetail.fusion.connect.core.common.constant.ExchangeHeaderConstants.TENANT_ID;
import static com.scoperetail.fusion.connect.core.common.constant.ExchangePropertyConstants.CUSTOM_MESSAGE_HEADER;
import static com.scoperetail.fusion.connect.core.common.constant.ExchangePropertyConstants.EVENT_DATA_MAP;
import static com.scoperetail.fusion.connect.core.common.constant.ExchangePropertyConstants.EVENT_FORMAT;
import static com.scoperetail.fusion.connect.core.common.constant.ExchangePropertyConstants.EVENT_TYPE;
import static com.scoperetail.fusion.connect.core.common.constant.ExchangePropertyConstants.IS_VALID_MESSAGE;
import static com.scoperetail.fusion.connect.core.common.constant.ExchangePropertyConstants.TENANT_IDENTIFIER_HEADER;
import static com.scoperetail.fusion.connect.core.common.constant.Format.JSON;
import static com.scoperetail.fusion.connect.core.common.constant.Format.XML;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.camel.Exchange;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.xml.sax.SAXException;
import com.scoperetail.fusion.connect.core.common.util.JsonUtils;
import com.scoperetail.fusion.connect.core.common.util.XmlUtil;
import com.scoperetail.fusion.connect.core.config.FusionConfig;

public class EventDataToMapConverter {
  private static final String CUSTOM_PAYLOAD_KEY = "payload";
  private static final String CUSTOM_HEADER_KEY = "headers";
  private static final String MESSAGE_HEADER = "HEADER";
  private static final String MESSAGE_BODY = "BODY";
  private static final String CUSTOM_MESSAGE_BODY = "CUSTOM_BODY";
  private static final String ENV = "ENV";
  @Autowired private FusionConfig fusionConfig;

  public void updateEventDataWithPayload(final Exchange exchange)
      throws IOException, ParserConfigurationException, SAXException {
    final boolean isValidMessage = exchange.getProperty(IS_VALID_MESSAGE, Boolean.class);
    if (isValidMessage) {
      final String eventFormat = exchange.getProperty(EVENT_FORMAT, String.class);
      final String payload = exchange.getIn().getBody(String.class);
      final Map<String, Object> params = exchange.getProperty(EVENT_DATA_MAP, Map.class);
      Object messageBody = payload;
      if (JSON.name().equalsIgnoreCase(eventFormat)) {
        String canonicalName = Map.class.getCanonicalName();
        if (payload.trim().startsWith(SQUARE_BRACKET)) {
          canonicalName = List.class.getCanonicalName();
        }
        messageBody = JsonUtils.unmarshal(Optional.ofNullable(payload), canonicalName);
      } else if (XML.name().equalsIgnoreCase(eventFormat)) {
        messageBody = XmlUtil.convertToMap(payload);
      }
      params.put(MESSAGE_BODY, messageBody);
      params.put(ENV, StringUtils.isBlank(fusionConfig.getEnv()) ? "stage" : fusionConfig.getEnv());
    }
  }

  public void enrichEvent(final Exchange exchange) {
    final boolean isValidMessage = exchange.getProperty(IS_VALID_MESSAGE, Boolean.class);
    if (isValidMessage) {
      final String eventType = exchange.getProperty(EVENT_TYPE, String.class);
      final Map<String, Object> params = exchange.getProperty(EVENT_DATA_MAP, Map.class);
      String tenantIdentifierHeader = exchange.getProperty(TENANT_IDENTIFIER_HEADER, String.class);
      tenantIdentifierHeader =
          StringUtils.isNotBlank(tenantIdentifierHeader) ? tenantIdentifierHeader : TENANT_ID;
      final String tenantId = exchange.getIn().getHeader(tenantIdentifierHeader, String.class);
      if (StringUtils.isNotBlank(tenantId)) {
        final Map<String, Object> customData =
            fusionConfig.getCacheDataByTenantId(tenantId + UNDERSCORE + eventType);
        if (MapUtils.isNotEmpty(customData)) {
          params.put(CUSTOM_MESSAGE_HEADER, customData.get(CUSTOM_HEADER_KEY));
          params.put(CUSTOM_MESSAGE_BODY, customData.get(CUSTOM_PAYLOAD_KEY));
        }
      }
    }
  }

  public void initializeEventDataWithHeaders(final Exchange exchange) {
    final Map<String, Object> params = new HashMap<>();
    params.put(MESSAGE_HEADER, exchange.getMessage().getHeaders());
    exchange.setProperty(EVENT_DATA_MAP, params);
  }
}