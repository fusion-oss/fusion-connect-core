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

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.scoperetail.fusion.connect.core.application.route.orchestrate.bean.BuildAction;
import com.scoperetail.fusion.connect.core.application.route.orchestrate.bean.BuildConfigSpec;
import com.scoperetail.fusion.connect.core.application.route.orchestrate.bean.ComputeHeader;
import com.scoperetail.fusion.connect.core.application.route.orchestrate.bean.CustomHeader;
import com.scoperetail.fusion.connect.core.application.route.orchestrate.bean.DelimiterConfig;
import com.scoperetail.fusion.connect.core.application.route.orchestrate.bean.EventFinder;
import com.scoperetail.fusion.connect.core.application.route.orchestrate.bean.FilterAction;
import com.scoperetail.fusion.connect.core.application.route.orchestrate.bean.HeaderValidator;
import com.scoperetail.fusion.connect.core.application.route.orchestrate.bean.SourceHeaderRemover;
import com.scoperetail.fusion.connect.core.common.constant.SourceType;
import com.scoperetail.fusion.connect.core.config.FusionConfig;
import com.scoperetail.fusion.connect.core.config.Source;

@Component
@Slf4j
public class OrchestratorRoute {
  @Autowired private CamelContext camelContext;
  @Autowired private FusionConfig config;

  @PostConstruct
  public void init() throws Exception {
    final List<Source> sources = config.getSources();
    for (final Source source : sources) {
      final String[] sourceUriParts = source.getUri().split(":");
      final String sourceComponent = sourceUriParts.length > 0 ? sourceUriParts[0] : null;
      camelContext.addRoutes(
          new DynamicRouteBuilder(camelContext, source, config.getSourceType(sourceComponent)));
    }
  }

  private static final class DynamicRouteBuilder extends RouteBuilder {
    private final Source source;
    private final SourceType sourceType;

    public DynamicRouteBuilder(
        final CamelContext camelContext, final Source source, final SourceType sourceType) {
      super(camelContext);
      this.source = source;
      this.sourceType = sourceType;
    }

    @Override
    public void configure() {
      from(source.getUri())
          .process(
              new Processor() {
                @Override
                public void process(final Exchange exchange) throws Exception {
                  String payload = exchange.getIn().getBody(String.class);
                  exchange.getMessage().setBody(payload);
                  log.info("payload: [{}]", payload);
                }
              })
          .setProperty("source", constant(source))
          .setProperty("sourceType", constant(sourceType))
          .setProperty("isValidMessage", constant(true))
          .setProperty("errorTemplateUri", constant(source.getErrorTemplateUri()))
          .setProperty("onValidationFailureUri", constant(source.getOnValidationFailureUri()))
          .bean(EventFinder.class)
          .bean(HeaderValidator.class)
          .bean(ComputeHeader.class)
          .bean(BuildConfigSpec.class)
          .bean(CustomHeader.class)
          .filter()
          .method(FilterAction.class, "filter")
          .bean(BuildAction.class)
          .loop(exchangeProperty("actionCount"))
          .toD("${exchangeProperty.action_" + "${exchangeProperty.CamelLoopIndex}" + "}")
          .end()
          .choice()
          .when(exchangeProperty("isValidMessage"))
          .bean(DelimiterConfig.class)
          .bean(SourceHeaderRemover.class)
          .recipientList(
              simple("${exchangeProperty.targetUri}"),
              simple("${exchangeProperty.targetDelimiter}").toString())
          .endChoice()
          .otherwise()
          .to("direct:failure")
          .end();
    }
  }
}