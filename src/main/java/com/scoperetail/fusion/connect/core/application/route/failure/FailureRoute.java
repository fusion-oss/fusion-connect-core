package com.scoperetail.fusion.connect.core.application.route.failure;

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

import static com.scoperetail.fusion.connect.core.common.constant.ErrorStatus.ERRORS;
import static com.scoperetail.fusion.connect.core.common.constant.ErrorStatus.STATUS;
import static com.scoperetail.fusion.connect.core.common.constant.ExchangePropertyConstants.EVENT_DATA_MAP;
import static com.scoperetail.fusion.connect.core.common.constant.ExchangePropertyConstants.EXCEPTION;
import static com.scoperetail.fusion.connect.core.common.constant.ExchangePropertyConstants.IS_VALID_MESSAGE;
import static com.scoperetail.fusion.connect.core.common.constant.ExchangePropertyConstants.PAYLOAD;
import static com.scoperetail.fusion.connect.core.common.constant.ExchangePropertyConstants.SOURCE_ERROR_HEADER_TEMPLATE_URI;
import static com.scoperetail.fusion.connect.core.common.constant.ExchangePropertyConstants.SOURCE_ERROR_PAYLOAD_TEMPLATE_URI;
import static com.scoperetail.fusion.connect.core.common.constant.SourceType.ASYNC;
import static com.scoperetail.fusion.connect.core.common.constant.SourceType.SYNC;
import static org.apache.http.HttpStatus.SC_BAD_REQUEST;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.builder.ValueBuilder;
import org.apache.camel.support.builder.PredicateBuilder;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.core.type.TypeReference;
import com.scoperetail.fusion.connect.core.application.service.transform.impl.DomainToFtlTemplateTransformer;
import com.scoperetail.fusion.connect.core.common.util.JsonUtils;

@Component
public class FailureRoute extends RouteBuilder {

  @Autowired private DomainToFtlTemplateTransformer domainToFtlTemplateTransformer;

  @Override
  public void configure() throws Exception {
    final ValueBuilder sourceType = exchangeProperty("sourceType");
    from("direct:failure")
        .process(
            new Processor() {
              @Override
              public void process(final Exchange exchange) throws Exception {
                final String errorHeaderTemplate =
                    exchange.getProperty(SOURCE_ERROR_HEADER_TEMPLATE_URI, String.class);
                if (StringUtils.isNotBlank(errorHeaderTemplate)) {
                  setErrorHeaders(exchange, errorHeaderTemplate);
                }
                final String template =
                    exchange.getProperty(SOURCE_ERROR_PAYLOAD_TEMPLATE_URI, String.class);
                if (StringUtils.isNotBlank(template)) {
                  setErrorBody(exchange, template);
                }
              }
            })
        .choice()
        .when(
            PredicateBuilder.and(
                constant(sourceType.isEqualTo(ASYNC)),
                simple("${exchangeProperty.errorTargetUri} != null")))
        .toD("${exchangeProperty.errorTargetUri}")
        .when(sourceType.isEqualTo(SYNC))
        .setHeader("CamelHttpResponseCode", constant(SC_BAD_REQUEST));
  }

  private void setErrorHeaders(final Exchange exchange, final String errorHeaderTemplate)
      throws Exception {
    final String newHeadersJson =
        domainToFtlTemplateTransformer.transform(
            exchange.getProperty(EVENT_DATA_MAP, Map.class), errorHeaderTemplate);
    final Map<String, Object> newHeaders =
        JsonUtils.unmarshal(
            Optional.of(newHeadersJson), Optional.of(new TypeReference<Map<String, Object>>() {}));
    exchange.getIn().getHeaders().putAll(newHeaders);
  }

  private void setErrorBody(final Exchange exchange, final String template) throws Exception {
    final Map<String, Object> paramsMap = new HashMap<>();
    paramsMap.put(STATUS, exchange.getProperty(STATUS, String.class));
    paramsMap.put(ERRORS, exchange.getProperty(ERRORS, Object.class));
    paramsMap.put(PAYLOAD, exchange.getMessage().getBody(String.class));
    paramsMap.put(EXCEPTION, exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class));
    paramsMap.put(IS_VALID_MESSAGE, exchange.getProperty(IS_VALID_MESSAGE, Boolean.class));
    exchange.getMessage().setBody(domainToFtlTemplateTransformer.transform(paramsMap, template));
  }
}
