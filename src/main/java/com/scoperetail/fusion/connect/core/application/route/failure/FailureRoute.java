package com.scoperetail.fusion.connect.core.application.route.failure;

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

import com.scoperetail.fusion.connect.core.application.service.transform.impl.DomainToFtlTemplateTransformer;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.builder.ValueBuilder;
import org.apache.camel.support.builder.PredicateBuilder;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static com.scoperetail.fusion.connect.core.common.constant.SourceType.ASYNC;
import static com.scoperetail.fusion.connect.core.common.constant.SourceType.SYNC;
import static org.apache.http.HttpStatus.SC_BAD_REQUEST;

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
                final String template = exchange.getProperty("errorTemplateUri", String.class);
                if (StringUtils.isNotBlank(template)) {
                  final Map<String, Object> paramsMap = new HashMap<>();
                  paramsMap.put("reason", exchange.getProperty("reason", String.class));
                  paramsMap.put(
                      "exception",
                      exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class));
                  paramsMap.put(
                      "failedMessagePayload", exchange.getMessage().getBody(String.class));
                  paramsMap.put(
                      "missingHeaders", exchange.getProperty("missingHeaders", Collection.class));
                  exchange
                      .getMessage()
                      .setBody(
                          domainToFtlTemplateTransformer.transform(
                              exchange.getProperty("event.type", String.class),
                              paramsMap,
                              template));
                }
              }
            })
        .choice()
        .when(
            PredicateBuilder.and(
                constant(sourceType.isEqualTo(ASYNC)),
                simple("${exchangeProperty.onValidationFailureUri} != null")))
        .toD("${exchangeProperty.onValidationFailureUri}")
        .when(sourceType.isEqualTo(SYNC))
        .setHeader("CamelHttpResponseCode", constant(SC_BAD_REQUEST));
  }
}
