package com.scoperetail.fusion.connect.core.application.route.orchestrate.bean;

import static com.scoperetail.fusion.connect.core.common.constant.CharacterConstant.COMMA;
import static com.scoperetail.fusion.connect.core.common.constant.ExchangeHeaderConstants.TENANT_ID;
import static com.scoperetail.fusion.connect.core.common.constant.ExchangePropertyConstants.ADD_CUSTOM_TARGET_HEADERS;
import static com.scoperetail.fusion.connect.core.common.constant.ExchangePropertyConstants.METHOD_TYPE;
import static com.scoperetail.fusion.connect.core.common.constant.ExchangePropertyConstants.TARGET_HEADER_BLACK_LIST;
import java.util.Arrays;
import java.util.Map;
import org.apache.camel.Exchange;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import com.scoperetail.fusion.connect.core.config.FusionConfig;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TargetHeaderCustomizer {
  @Autowired private FusionConfig fusionConfig;

  public void customizeTargetHeaders(final Exchange exchange) {
    blacklistTargetHeaders(exchange);
    addCustomHeaders(exchange);
  }

  private void blacklistTargetHeaders(final Exchange exchange) {
    final String targetHeadersBlacklist =
        exchange.getProperty(TARGET_HEADER_BLACK_LIST, String.class);
    if (StringUtils.isNotBlank(targetHeadersBlacklist)) {
      Arrays.stream(targetHeadersBlacklist.split(COMMA))
          .forEach(
              s -> {
                exchange.getIn().removeHeader(s);
                exchange.getIn().removeHeaders(s);
              });
      log.debug("Blacklisted target headers: {}", targetHeadersBlacklist);
    }
  }

  private void addCustomHeaders(final Exchange exchange) {
    final boolean canAddCustomTargetHeaders =
        Boolean.parseBoolean(exchange.getProperty(ADD_CUSTOM_TARGET_HEADERS, String.class));
    final String tenantId = exchange.getIn().getHeader(TENANT_ID, String.class);
    if (canAddCustomTargetHeaders && StringUtils.isNotBlank(tenantId)) {
      final Map<String, Object> headerData = fusionConfig.getCacheDataByTenantId(tenantId);
      if (MapUtils.isNotEmpty(headerData)) {
        exchange.getIn().getHeaders().putAll(headerData);
      }
    }
    final String methodType = exchange.getProperty(METHOD_TYPE, String.class);
    if (StringUtils.isNotBlank(methodType)) {
      exchange.getIn().setHeader(Exchange.HTTP_METHOD, methodType);
    }
  }
}
