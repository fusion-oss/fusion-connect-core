package com.scoperetail.fusion.connect.core.application.route.cache;

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

import static com.scoperetail.fusion.connect.core.common.constant.ExchangePropertyConstants.CACHE_DATA_URL;
import java.util.Map;
import java.util.Optional;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.scoperetail.fusion.connect.core.common.util.JsonUtils;
import com.scoperetail.fusion.connect.core.config.FusionConfig;

@Component
public class CacheRoute extends RouteBuilder {
  public static final String CACHE_ROUTE = "direct:cache";
  @Autowired private FusionConfig fusionConfig;

  @Override
  public void configure() throws Exception {
    from(CACHE_ROUTE)
        .setHeader(Exchange.HTTP_METHOD, simple("GET"))
        .process(
            new Processor() {
              @Override
              public void process(final Exchange exchange) throws Exception {
                final Optional<String> optCacheDataUrl =
                    Optional.ofNullable(fusionConfig.getCacheDataUrl());
                optCacheDataUrl.ifPresent(
                    cacheDataUrl -> exchange.setProperty(CACHE_DATA_URL, cacheDataUrl));
              }
            })
        .choice()
        .when(exchangeProperty(CACHE_DATA_URL).isNotNull())
        .toD("${exchangeProperty.cacheDataUrl}")
        .process(
            new Processor() {
              @Override
              public void process(final Exchange exchange) throws Exception {
                final String response = exchange.getIn().getBody(String.class);
                exchange
                    .getIn()
                    .setBody(
                        JsonUtils.unmarshal(Optional.of(response), Map.class.getCanonicalName()));
              }
            })
        .endChoice();
  }
}
