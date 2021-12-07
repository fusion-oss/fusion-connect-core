package com.scoperetail.fusion.connect.core.application.route.transform;

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

import java.util.Map;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.scoperetail.fusion.connect.core.application.service.transform.impl.DomainToFtlTemplateTransformer;

@Component
public class TransformerRoute extends RouteBuilder {

  @Autowired private DomainToFtlTemplateTransformer domainToFtlTemplateTransformer;

  @Override
  public void configure() throws Exception {
    from("direct:transform")
        .choice()
        .when(exchangeProperty("isValidMessage"))
        .to("direct:marshaller");

    from("direct:marshaller")
        .choice()
        .when()
        .simple("${exchangeProperty.event.format} == 'json'")
        .unmarshal()
        .json(Map.class)
        .to("direct:transformer")
        .when()
        .simple("${exchangeProperty.event.format} == 'xml'")
        .unmarshal()
        .jacksonxml(Map.class)
        .to("direct:transformer");

    from("direct:transformer")
        .process(
            new Processor() {
              @Override
              public void process(final Exchange exchange) throws Exception {
                exchange
                    .getMessage()
                    .setBody(
                        domainToFtlTemplateTransformer.transform(
                            exchange.getProperty("event.type", String.class),
                            (Map<String, Object>) exchange.getMessage().getBody(),
                            exchange.getProperty("transformerTemplateUri", String.class)));
              }
            })
        .log("After transformation:" + "${body}")
        .log("Transformation Completed Successfully");
  }
}
