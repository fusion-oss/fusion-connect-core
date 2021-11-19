package com.scoperetail.fusion.connect.core.application.route.orchestrate.bean;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
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
        exchange.getMessage().setHeaders(headers);
      } catch (Exception e) {
        log.error(
            "Unable to call Plugin class: {} due to exception: {}",
            pluginClassName,
            e.getMessage());
      }
    }
  }
}
