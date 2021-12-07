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

import static com.scoperetail.fusion.connect.core.common.constant.CharacterConstant.COMMA;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.commons.lang3.StringUtils;
import com.scoperetail.fusion.connect.core.config.Event;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HeaderValidator {
  private static final String MANDATORY_HEADERS = "mandatoryHeaders";

  public void validateHeaders(final Message message) {
    final Exchange exchange = message.getExchange();
    final boolean isValidMessage = exchange.getProperty("isValidMessage", Boolean.class);
    if (isValidMessage) {
      final String eventType = exchange.getProperty("event.type", String.class);
      final String format = exchange.getProperty("event.format", String.class);
      log.debug(
          "Mandatory header validation started for eventType: {} format: {}", eventType, format);
      final Optional<Object> optMandatoryHeaders = getMandatoryHeaders(exchange);
      if (optMandatoryHeaders.isPresent()) {
        final String mandatoryHeadersStr = String.valueOf(optMandatoryHeaders.get());
        if (StringUtils.isNotBlank(mandatoryHeadersStr)) {
          final Set<String> missingHeaders = getMissingHeaders(message, mandatoryHeadersStr);
          final boolean isValidHeader = missingHeaders.isEmpty();
          exchange.setProperty("isValidMessage", isValidHeader);
          exchange.setProperty("missingHeaders", missingHeaders);
          exchange.setProperty("reason", "Missing mandatory headers:" + missingHeaders);
          log.debug(
              "Is mandatory headers provided:{}  Missing headers:{}",
              isValidHeader,
              missingHeaders);
        }
      }
      log.debug(
          "Mandatory header validation completed for eventType: {} format: {}", eventType, format);
    }
  }

  private Optional<Object> getMandatoryHeaders(final Exchange exchange) {
    final Event eventConfig = exchange.getProperty("event", Event.class);
    final Map<String, Object> eventHeaders = eventConfig.getHeaders();
    return Optional.ofNullable(eventHeaders.get(MANDATORY_HEADERS));
  }

  private Set<String> getMissingHeaders(final Message message, final String mandatoryHeadersStr) {
    final Map<String, Object> messageHeaders = message.getHeaders();
    final Set<String> headerKeys = messageHeaders.keySet();
    final Set<String> mandatoryHeaders =
        Arrays.stream(mandatoryHeadersStr.trim().split(COMMA))
            .map(String::trim)
            .collect(Collectors.toSet());
    mandatoryHeaders.removeAll(headerKeys);
    return mandatoryHeaders;
  }
}
