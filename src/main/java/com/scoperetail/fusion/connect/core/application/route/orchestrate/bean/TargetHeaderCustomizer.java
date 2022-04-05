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

import static com.scoperetail.fusion.connect.core.common.constant.CharacterConstant.COMMA;
import static com.scoperetail.fusion.connect.core.common.constant.CharacterConstant.EQUAL_TO;
import static com.scoperetail.fusion.connect.core.common.constant.ExchangePropertyConstants.ADD_CUSTOM_TARGET_HEADERS;
import static com.scoperetail.fusion.connect.core.common.constant.ExchangePropertyConstants.CUSTOM_MESSAGE_HEADER;
import static com.scoperetail.fusion.connect.core.common.constant.ExchangePropertyConstants.EVENT_DATA_MAP;
import static com.scoperetail.fusion.connect.core.common.constant.ExchangePropertyConstants.HEADER_CUSTOMIZER_TEMPLATE_URI;
import static com.scoperetail.fusion.connect.core.common.constant.ExchangePropertyConstants.METHOD_TYPE;
import static com.scoperetail.fusion.connect.core.common.constant.ExchangePropertyConstants.TARGET_HEADER_BLACK_LIST;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.camel.Exchange;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import com.scoperetail.fusion.connect.core.application.service.transform.impl.DomainToFtlTemplateTransformer;
import com.scoperetail.fusion.connect.core.config.FusionConfig;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TargetHeaderCustomizer {
  private static final String NEWLINE_EXPRESSION = "\\R";
  @Autowired private FusionConfig fusionConfig;
  @Autowired private DomainToFtlTemplateTransformer domainToFtlTemplateTransformer;

  public void customizeTargetHeaders(final Exchange exchange) throws Exception {
    blacklistTargetHeaders(exchange);
    addCustomHeaders(exchange);
    customizeTargetHeader(exchange);
  }

  private void blacklistTargetHeaders(final Exchange exchange) {
    final String targetHeadersBlacklist =
        exchange.getProperty(TARGET_HEADER_BLACK_LIST, String.class);
    if (StringUtils.isNotBlank(targetHeadersBlacklist)) {
      Arrays.stream(targetHeadersBlacklist.split(COMMA))
          .forEach(
              s -> {
                exchange.getIn().removeHeader(s);
                exchange.getIn().removeHeaders(s);
              });
      log.debug("Blacklisted target headers: {}", targetHeadersBlacklist);
    }
  }

  private void addCustomHeaders(final Exchange exchange) {
    final boolean canAddCustomTargetHeaders =
        Boolean.parseBoolean(exchange.getProperty(ADD_CUSTOM_TARGET_HEADERS, String.class));
    if (canAddCustomTargetHeaders) {
      final Map<String, Object> params = exchange.getProperty(EVENT_DATA_MAP, Map.class);
      final Object headerData = params.get(CUSTOM_MESSAGE_HEADER);
      if (Objects.nonNull(headerData) && headerData instanceof Map) {
        exchange.getIn().getHeaders().putAll((Map<String, Object>) headerData);
      }
    }
    final String methodType = exchange.getProperty(METHOD_TYPE, String.class);
    if (StringUtils.isNotBlank(methodType)) {
      exchange.getIn().setHeader(Exchange.HTTP_METHOD, methodType);
    }
  }

  private void customizeTargetHeader(final Exchange exchange) throws Exception {
    final String headerCustomizerTemplateUri =
        exchange.getProperty(HEADER_CUSTOMIZER_TEMPLATE_URI, String.class);
    if (StringUtils.isNotBlank(headerCustomizerTemplateUri)) {
      final String customHeadersStr =
          domainToFtlTemplateTransformer.transform(
              exchange.getProperty(EVENT_DATA_MAP, Map.class), headerCustomizerTemplateUri);
      if (StringUtils.isNotBlank(customHeadersStr)) {
        final Map<String, Object> customHeaderByNameMap =
            Arrays.stream(customHeadersStr.split(NEWLINE_EXPRESSION))
                .map(s -> s.split(EQUAL_TO))
                .collect(Collectors.toMap(s -> s[0], s -> s[1]));
        exchange.getIn().getHeaders().putAll(customHeaderByNameMap);
      }
    }
  }
}
