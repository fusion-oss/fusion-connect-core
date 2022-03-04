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

import static com.scoperetail.fusion.connect.core.common.constant.ExchangePropertyConstants.EVENT_DATA_MAP;
import static com.scoperetail.fusion.connect.core.common.constant.ExchangePropertyConstants.EVENT_FORMAT;
import static com.scoperetail.fusion.connect.core.common.constant.ExchangePropertyConstants.IS_VALID_MESSAGE;
import static com.scoperetail.fusion.connect.core.common.constant.Format.JSON;
import static com.scoperetail.fusion.connect.core.common.constant.Format.XML;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.camel.Exchange;
import org.xml.sax.SAXException;
import com.scoperetail.fusion.connect.core.common.util.JsonUtils;
import com.scoperetail.fusion.connect.core.common.util.XmlUtil;

public class EventDataToMapConverter {
  private static final String MESSAGE_HEADER = "HEADER";
  private static final String MESSAGE_BODY = "BODY";

  public void updateEventDataWithPayload(final Exchange exchange)
      throws IOException, ParserConfigurationException, SAXException {
    final boolean isValidMessage = exchange.getProperty(IS_VALID_MESSAGE, Boolean.class);
    if (isValidMessage) {
      final String eventFormat = exchange.getProperty(EVENT_FORMAT, String.class);
      final String payload = exchange.getIn().getBody(String.class);
      final Map<String, Object> params = exchange.getProperty(EVENT_DATA_MAP, Map.class);
      if (JSON.name().equalsIgnoreCase(eventFormat)) {
        if (payload.trim().startsWith("[")) {
          params.put(
              MESSAGE_BODY,
              JsonUtils.unmarshal(Optional.ofNullable(payload), List.class.getCanonicalName()));
        } else {
          params.put(
              MESSAGE_BODY,
              JsonUtils.unmarshal(Optional.ofNullable(payload), Map.class.getCanonicalName()));
        }
      } else if (XML.name().equalsIgnoreCase(eventFormat)) {
        params.put(MESSAGE_BODY, XmlUtil.convertToMap(payload));
      }
    }
  }

  public void initializeEventDataWithHeaders(final Exchange exchange) {
    final Map<String, Object> params = new HashMap<>();
    params.put(MESSAGE_HEADER, exchange.getMessage().getHeaders());
    exchange.setProperty(EVENT_DATA_MAP, params);
  }
}
