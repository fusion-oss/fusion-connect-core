package com.scoperetail.fusion.connect.core.application.route.validate;

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

import static org.apache.camel.LoggingLevel.DEBUG;
import static org.apache.commons.lang3.StringUtils.LF;
import java.util.Set;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.ValidationException;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jsonvalidator.JsonValidationException;
import org.springframework.stereotype.Component;
import com.networknt.schema.ValidationMessage;

@Component
public class ValidatorRoute extends RouteBuilder {

  @Override
  public void configure() throws Exception {
    from("direct:validate")
        .doTry()
        .log(
            DEBUG,
            "Validating message with the schema provided at path: ${exchangeProperty.validatorUri}")
        .toD("${exchangeProperty.validatorUri}")
        .log(DEBUG, "Message Validated successfully")
        .doCatch(ValidationException.class)
        .process(
            new Processor() {
              @Override
              public void process(final Exchange exchange) throws Exception {
                final Throwable throwable =
                    exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Throwable.class);
                final StringBuilder messageBuilder = new StringBuilder();
                if (throwable.getClass() == JsonValidationException.class) {
                  messageBuilder.append(LF);
                  final JsonValidationException jsonValidationException =
                      (JsonValidationException) throwable;
                  messageBuilder.append(
                      String.format(
                          "Message validation failed with %d errors",
                          jsonValidationException.getNumberOfErrors()));
                  final Set<ValidationMessage> errors = jsonValidationException.getErrors();
                  for (final ValidationMessage error : errors) {
                    messageBuilder.append(error.getMessage());
                    messageBuilder.append(LF);
                  }
                }
                exchange.setProperty("isValidMessage", false);
                exchange.setProperty("reason", messageBuilder.toString());
              }
            });
  }
}
