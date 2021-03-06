package com.scoperetail.fusion.connect.core.application.route.split;

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

import static com.scoperetail.fusion.connect.core.common.constant.ExchangePropertyConstants.IS_VALID_MESSAGE;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;
import com.scoperetail.fusion.connect.core.application.route.orchestrate.bean.TargetHeaderCustomizer;

@Component
public class SplitterRoute extends RouteBuilder {
  @Override
  public void configure() throws Exception {

    from("direct:split")
        .log("Splitter START")
        .choice()
        .when(exchangeProperty(IS_VALID_MESSAGE))
        .choice()
        .when()
        .simple("${exchangeProperty.splitFormat} == 'json'")
        .to("direct:jsonSplitter")
        .when()
        .simple("${exchangeProperty.splitFormat} == 'xml'")
        .to("direct:xmlSplitter")
        .otherwise()
        .to("direct:tokenSplitter")
        .end()
        .log("Split Completed Successfully")
        .stop()
        .end();

    from("direct:jsonSplitter")
        .log("JSON Splitter started")
        .split(jsonpath("${exchangeProperty.splitCondition}"))
        .streaming()
        .marshal()
        .json(true)
        .bean(TargetHeaderCustomizer.class)
        .log("Split message : ${body} ")
        .toD("${exchangeProperty.targetUri}");

    from("direct:xmlSplitter")
        .log("XML Splitter started: ${body} ")
        .split()
        .tokenizeXML("${exchangeProperty.splitCondition}")
        .streaming()
        .bean(TargetHeaderCustomizer.class)
        .log("Split message : ${body} ")
        .toD("${exchangeProperty.targetUri}");

    from("direct:tokenSplitter")
        .log("Token Splitter started")
        .split(body().tokenize("${exchangeProperty.splitCondition}"))
        .streaming()
        .bean(TargetHeaderCustomizer.class)
        .log("Split message : ${body} ")
        .toD("${exchangeProperty.targetUri}");
  }
}
