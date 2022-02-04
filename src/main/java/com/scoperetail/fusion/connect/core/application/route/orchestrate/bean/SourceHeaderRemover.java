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

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import org.apache.camel.Exchange;
import com.scoperetail.fusion.connect.core.config.Event;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SourceHeaderRemover {

  private static final String REMOVE_SOURCE_HEADERS = "removeSourceHeaders";

  public void removeSourceHeaders(final Exchange exchange) {
    final boolean isValidMessage = exchange.getProperty("isValidMessage", Boolean.class);
    if (isValidMessage) {
      final Optional<Object> optSourceHeaders = getHeaders(exchange);
      if (optSourceHeaders.isPresent()) {
        final String headersStr = String.valueOf(optSourceHeaders.get());
        Arrays.stream(headersStr.split(","))
            .forEach(
                s -> {
                  exchange.getIn().removeHeader(s);
                  exchange.getIn().removeHeaders(s);
                });
        log.debug("Removed source headers: {}", headersStr);
      }
    }
  }

  private Optional<Object> getHeaders(final Exchange exchange) {
    final Event eventConfig = exchange.getProperty("event", Event.class);
    final Map<String, Object> eventHeaders = eventConfig.getHeaders();
    return Optional.ofNullable(eventHeaders.get(REMOVE_SOURCE_HEADERS));
  }
}
