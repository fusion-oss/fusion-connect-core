package com.scoperetail.fusion.connect.core.application.route.orchestrate.bean;

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

import static com.scoperetail.fusion.connect.core.common.constant.CamelComponentConstants.FILE_COMPONENT;
import static com.scoperetail.fusion.connect.core.common.constant.ExchangePropertyConstants.EVENT;
import static com.scoperetail.fusion.connect.core.common.constant.ExchangePropertyConstants.EVENT_DATA_MAP;
import static com.scoperetail.fusion.connect.core.common.constant.ExchangePropertyConstants.IS_VALID_MESSAGE;
import static com.scoperetail.fusion.connect.core.common.constant.ExchangePropertyConstants.SOURCE;
import static com.scoperetail.fusion.connect.core.common.constant.ExchangePropertyConstants.TARGET_URI;
import static com.scoperetail.fusion.connect.core.common.constant.ResourceNameConstants.TARGET_URI_TEMPLATE_NAME;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import org.apache.camel.Exchange;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import com.scoperetail.fusion.connect.core.application.service.transform.impl.DomainToFtlTemplateTransformer;
import com.scoperetail.fusion.connect.core.config.Event;
import com.scoperetail.fusion.connect.core.config.FusionConfig;
import com.scoperetail.fusion.connect.core.config.Source;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TargetURICustomizer {
  private static final String TARGET_NAME = "<<TARGET_NAME>>";
  @Autowired private DomainToFtlTemplateTransformer domainToFtlTemplateTransformer;
  @Autowired private FusionConfig fusionConfig;

  @Value("${ENV:}")
  private String ENV;

  public void customizeTargetUri(final Exchange exchange) throws Exception {
    final boolean isValidMessage = exchange.getProperty(IS_VALID_MESSAGE, Boolean.class);
    String targetUri = exchange.getProperty(TARGET_URI, String.class);
    if (isValidMessage && StringUtils.isNotBlank(targetUri) && targetUri.contains(TARGET_NAME)) {
      final Event event = exchange.getProperty(EVENT, Event.class);
      final Source source = exchange.getProperty(SOURCE, Source.class);
      Path templatePath =
          Path.of(
              fusionConfig.getResourceDirectory(),
              source.getName(),
              event.getEventType(),
              ENV,
              TARGET_URI_TEMPLATE_NAME);
      if (!Files.exists(templatePath)) {
        templatePath =
            Path.of(
                fusionConfig.getResourceDirectory(),
                source.getName(),
                event.getEventType(),
                TARGET_URI_TEMPLATE_NAME);
      }
      if (Files.exists(templatePath)) {
        final String targetName =
            domainToFtlTemplateTransformer.transform(
                exchange.getProperty(EVENT_DATA_MAP, Map.class),
                FILE_COMPONENT + templatePath.toAbsolutePath().toString());
        targetUri = targetUri.replace(TARGET_NAME, targetName);
        log.debug("Customized target URI: {}", targetUri);
        exchange.setProperty(TARGET_URI, targetUri);
      }
    }
  }
}