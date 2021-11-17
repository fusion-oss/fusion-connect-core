package com.scoperetail.fusion.connect.core.application.route.orchestrate;

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

import java.util.List;
import javax.annotation.PostConstruct;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.scoperetail.fusion.connect.core.application.route.orchestrate.bean.BuildAction;
import com.scoperetail.fusion.connect.core.application.route.orchestrate.bean.BuildConfigSpec;
import com.scoperetail.fusion.connect.core.application.route.orchestrate.bean.ComputeHeader;
import com.scoperetail.fusion.connect.core.application.route.orchestrate.bean.EventFinder;
import com.scoperetail.fusion.connect.core.application.route.orchestrate.bean.FilterAction;
import com.scoperetail.fusion.connect.core.config.FusionConfig;
import com.scoperetail.fusion.connect.core.config.Source;

@Component
public class OrchestratorRoute {
  @Autowired private CamelContext camelContext;
  @Autowired private FusionConfig config;

  @PostConstruct
  public void init() throws Exception {
    final List<Source> sources = config.getSources();
    for (final Source source : sources) {
      camelContext.addRoutes(new DynamicRouteBuilder(camelContext, source));
    }
  }

  private static final class DynamicRouteBuilder extends RouteBuilder {
    private final Source source;

    public DynamicRouteBuilder(final CamelContext camelContext, final Source source) {
      super(camelContext);
      this.source = source;
    }

    @Override
    public void configure() {
      from(source.getUri())
          .process(
              new Processor() {
                @Override
                public void process(final Exchange exchange) throws Exception {
                  exchange.getMessage().setBody(exchange.getIn().getBody(String.class));
                }
              })
          .setProperty("source", constant(source))
          .bean(EventFinder.class)
          .choice()
          .when(simple("${exchangeProperty.event} == null"))
          .log("Stopping the route as event not found")
          .toD("${exchangeProperty.source.bo}")
          .stop()
          .end()
          .bean(ComputeHeader.class)
          .bean(BuildConfigSpec.class)
          .filter()
          .method(FilterAction.class, "filter")
          .choice()
          .when(simple("${exchangeProperty.actionExecution} == 'sequence'"))
          .log("Executing actions sequentially")
          .bean(BuildAction.class)
          .loop(exchangeProperty("actionCount"))
          .toD("${exchangeProperty.action_" + "${exchangeProperty.CamelLoopIndex}" + "}")
          .end()
          .recipientList(simple("${exchangeProperty.targetUri}"))
          .end();
    }
  }
}
