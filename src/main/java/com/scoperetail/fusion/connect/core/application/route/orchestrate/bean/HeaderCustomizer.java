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

import com.scoperetail.fusion.connect.core.application.service.transform.impl.DomainToFtlTemplateTransformer;
import com.scoperetail.fusion.connect.core.common.util.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.scoperetail.fusion.connect.core.common.constant.CharacterConstant.EQUAL_TO;
import static com.scoperetail.fusion.connect.core.common.constant.ExchangePropertyConstants.SPLIT_CUSTOMIZER_URI;

@Slf4j
public class HeaderCustomizer {
  public static final String CUST_HEADER = "splittedMessage";
  @Autowired private DomainToFtlTemplateTransformer domainToFtlTemplateTransformer;

  public void splitMethod(Exchange exchange) throws Exception {
//logs
    final String payload =
        exchange.getIn().getBody(String.class).trim().length() == 0
            ? "{}"
            : exchange.getIn().getBody(String.class);
    String canonicalName = Map.class.getCanonicalName(); // todo: no need of new var
    Object messageBody = JsonUtils.unmarshal(Optional.ofNullable(payload), canonicalName);
    Map<String, Object> stringObjectMap = new HashMap<>();
    stringObjectMap.put("HEADER", exchange.getIn().getHeaders()); // remove hardcoding
    stringObjectMap.put("BODY", messageBody);
    final String headerCustomizerTemplateUri =
        exchange.getProperty(SPLIT_CUSTOMIZER_URI, String.class);
    if (StringUtils.isNotBlank(headerCustomizerTemplateUri)) {
      final String customHeadersStr =
          domainToFtlTemplateTransformer.transform(stringObjectMap, headerCustomizerTemplateUri);
      if (StringUtils.isNotBlank(customHeadersStr)) {
        final Map<String, Object> customHeaderByNameMap =
            Arrays.stream(customHeadersStr.split("\\R"))
                .map(s -> s.split(EQUAL_TO))
                .collect(Collectors.toMap(s -> s[0], s -> s[1]));
        exchange.getIn().getHeaders().putAll(customHeaderByNameMap);
        log.info("After Header Customizer in customizer:" + exchange.getIn().getHeaders());
      }
    }
  }
}
