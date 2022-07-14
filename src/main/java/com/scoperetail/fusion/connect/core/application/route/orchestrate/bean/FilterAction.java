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

import static com.scoperetail.fusion.connect.core.common.constant.ExchangePropertyConstants.EVENT;
import static com.scoperetail.fusion.connect.core.common.constant.ExchangePropertyConstants.EVENT_FORMAT;
import static com.scoperetail.fusion.connect.core.common.constant.ExchangePropertyConstants.IS_VALID_MESSAGE;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.apache.camel.Exchange;
import org.springframework.beans.factory.annotation.Autowired;
import com.scoperetail.fusion.connect.core.common.constant.Format;
import com.scoperetail.fusion.connect.core.common.util.matcher.EventMatcher;
import com.scoperetail.fusion.connect.core.common.util.matcher.impl.JsonEventMatcher;
import com.scoperetail.fusion.connect.core.common.util.matcher.impl.XmlEventMatcher;
import com.scoperetail.fusion.connect.core.config.Event;
import com.scoperetail.fusion.connect.core.config.FilterCriteria;
import com.scoperetail.fusion.connect.core.config.FilterCriteria.Type;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FilterAction {

  @Autowired private JsonEventMatcher jsonEventMatcher;
  @Autowired private XmlEventMatcher xmlEventMatcher;

  public boolean filter(final Exchange exchange) {
    boolean isMatched = true;
    final boolean isValidMessage = exchange.getProperty(IS_VALID_MESSAGE, Boolean.class);
    if (isValidMessage) {
      final Event event = exchange.getProperty(EVENT, Event.class);
      final String format = exchange.getProperty(EVENT_FORMAT, String.class).toUpperCase();
      final EventMatcher eventMatcher =
          format.equals(Format.JSON.name()) ? jsonEventMatcher : xmlEventMatcher;
      if (Objects.nonNull(event.getFilters())) {
        final Map<String, Object> exchangeHeaders = exchange.getMessage().getHeaders();
        final String payload = exchange.getIn().getBody(String.class);
        for (final FilterCriteria filterCriteria : event.getFilters()) {
          isMatched = isFilterMatched(filterCriteria, exchangeHeaders, payload, eventMatcher);
          if (!isMatched) {
            log.info(
                "Stopping the flow as filter criteria :: {} failed", filterCriteria.getValues());
            break;
          }
          log.info("Filter passed, criteria :: {}", filterCriteria.getValues());
        }
      }
    }
    return isMatched;
  }

  private boolean isFilterMatched(
      final FilterCriteria filterCriteria,
      final Map<String, Object> exchangeHeaders,
      final String payload,
      final EventMatcher eventMatcher) {
    boolean isMatched = true;
    final List<String> filterValues = filterCriteria.getValues();
    final Type filterType =
        filterCriteria.getType() == null ? Type.PAYLOAD_MATCHER : filterCriteria.getType();
    if (filterType == Type.HEADER_MATCHER) {
      final String headerValue = (String) exchangeHeaders.get(filterCriteria.getExpression());
      isMatched = filterValues.contains(headerValue);
    } else if (filterType == Type.PAYLOAD_MATCHER) {
      isMatched = eventMatcher.contains(filterValues, filterCriteria.getExpression(), payload);
    }
    return isMatched;
  }
}
