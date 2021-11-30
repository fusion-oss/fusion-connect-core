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

import static com.scoperetail.fusion.connect.core.common.constant.SourceType.ASYNC;
import static com.scoperetail.fusion.connect.core.common.constant.SourceType.SYNC;
import static org.apache.camel.support.builder.PredicateBuilder.and;
import static org.apache.camel.support.builder.PredicateBuilder.not;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.builder.ValueBuilder;
import org.apache.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class FailureRoute extends RouteBuilder {

  @Override
  public void configure() throws Exception {
    final ValueBuilder sourceType = exchangeProperty("sourceType");
    from("direct:failure")
        .log("${exchangeProperty.sourceType}")
        .choice()
        .when(
            and(
                sourceType.isEqualTo(ASYNC),
                not(simple("${exchangeProperty.onValidationFailureUri} == null"))))
        .toD("${exchangeProperty.onValidationFailureUri}")
        .when(sourceType.isEqualTo(SYNC))
        .setHeader("CamelHttpResponseCode", constant(HttpStatus.SC_BAD_REQUEST))
        .otherwise()
        .log(LoggingLevel.INFO, "Validation URI is not provided")
        .log(LoggingLevel.ERROR, "${exchangeProperty.reason}");
  }
}
