package com.scoperetail.fusion.connect.core.application.route.dedupe;

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

import static org.apache.camel.support.builder.PredicateBuilder.and;
import static org.apache.camel.support.builder.PredicateBuilder.not;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;
import com.scoperetail.fusion.connect.core.application.route.dedupe.bean.DedupeCheckService;

@Component
public class DeDupeRoute extends RouteBuilder {
  @Override
  public void configure() throws Exception {
    from("direct:dedupe")
        .log("Checking for duplicate message")
        .bean(DedupeCheckService.class)
        .log("Is message duplicate: ${exchangeProperty.isDuplicate}")
        .choice()
        .when(
            and(
                simple("${exchangeProperty.isDuplicate}"),
                not(simple("${exchangeProperty.continueOnDuplicate}"))))
        .log(
            "Stopping message flow as duplicat message detected and continueOnDuplicate property is set to false")
        .stop()
        .end();
  }
}
