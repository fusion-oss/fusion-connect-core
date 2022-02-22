package com.scoperetail.fusion.connect.core.application.route.orchestrate.bean;

import static com.scoperetail.fusion.connect.core.common.constant.CamelComponentConstants.FILE_COMPONENT;
import static com.scoperetail.fusion.connect.core.common.constant.ExchangePropertyConstants.EVENT;
import static com.scoperetail.fusion.connect.core.common.constant.ExchangePropertyConstants.EVENT_FORMAT;
import static com.scoperetail.fusion.connect.core.common.constant.ExchangePropertyConstants.IS_VALID_MESSAGE;
import static com.scoperetail.fusion.connect.core.common.constant.ExchangePropertyConstants.SOURCE;
import static com.scoperetail.fusion.connect.core.common.constant.ExchangePropertyConstants.TARGET_URI;
import static com.scoperetail.fusion.connect.core.common.constant.ResourceNameConstants.TARGET_URI_TEMPLATE_NAME;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import org.apache.camel.Exchange;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import com.scoperetail.fusion.connect.core.common.helper.FTLHelper;
import com.scoperetail.fusion.connect.core.config.Event;
import com.scoperetail.fusion.connect.core.config.FusionConfig;
import com.scoperetail.fusion.connect.core.config.Source;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TargetURICustomizer {
  private static final String TARGET_NAME = "<<TARGET_NAME>>";
  @Autowired private FTLHelper ftlHelper;
  @Autowired private FusionConfig fusionConfig;

  public void customizeTargetUri(final Exchange exchange) throws Exception {
    final boolean isValidMessage = exchange.getProperty(IS_VALID_MESSAGE, Boolean.class);
    String targetUri = exchange.getProperty(TARGET_URI, String.class);
    if (isValidMessage && StringUtils.isNotBlank(targetUri) && targetUri.contains(TARGET_NAME)) {
      final Event event = exchange.getProperty(EVENT, Event.class);
      final Source source = exchange.getProperty(SOURCE, Source.class);
      final Path templatePath =
          Path.of(
              fusionConfig.getResourceDirectory(),
              source.getName(),
              event.getEventType(),
              TARGET_URI_TEMPLATE_NAME);
      if (Files.exists(templatePath)) {
        final String format = exchange.getProperty(EVENT_FORMAT, String.class);
        final String payload = exchange.getMessage().getBody(String.class);
        final Map<String, Object> headers = exchange.getMessage().getHeaders();
        final String targetName =
            ftlHelper.computeValue(
                event.getEventType(),
                format,
                payload,
                headers,
                new HashMap<>(),
                FILE_COMPONENT + templatePath.toAbsolutePath().toString());
        targetUri = targetUri.replace(TARGET_NAME, targetName);
        log.debug("Customized target URI: {}", targetUri);
        exchange.setProperty(TARGET_URI, targetUri);
      }
    }
  }
}
