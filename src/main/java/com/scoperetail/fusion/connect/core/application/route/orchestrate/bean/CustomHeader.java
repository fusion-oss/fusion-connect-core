package com.scoperetail.fusion.connect.core.application.route.orchestrate.bean;

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

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import java.lang.reflect.Method;
import java.util.Map;

@Slf4j
public class CustomHeader {

  @Autowired ApplicationContext context;

  public void process(final Exchange exchange) {

    final String pluginClassName = exchange.getProperty("plugin", String.class);
    if (StringUtils.isNotBlank(pluginClassName)) {
      try {
        final Class<?> filterClass = Class.forName(pluginClassName);
        final Method method = filterClass.getDeclaredMethod("getHeaders", String.class);
        final Object object = filterClass.getDeclaredConstructor().newInstance();
        final Map<String, Object> headers =
            (Map<String, Object>) method.invoke(object, exchange.getIn().getBody(String.class));
        if (MapUtils.isNotEmpty(headers)) {
          exchange.getMessage().setHeaders(headers);
        }
      } catch (Exception e) {
        log.error(
            "Unable to call Plugin class: {} due to exception: {}",
            pluginClassName,
            e.getMessage());
      }
    }
  }
}
