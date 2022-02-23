package com.scoperetail.fusion.connect.core.common.helper;

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

import static com.scoperetail.fusion.connect.core.common.constant.Format.JSON;
import static com.scoperetail.fusion.connect.core.common.constant.Format.XML;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.scoperetail.fusion.connect.core.application.service.transform.impl.DomainToFtlTemplateTransformer;
import com.scoperetail.fusion.connect.core.common.util.JsonUtils;
import com.scoperetail.fusion.connect.core.common.util.XmlUtil;

@Component
public class FTLHelper {
  private static final String MESSAGE_HEADER = "HEADER";
  private static final String MESSAGE_BODY = "BODY";
  @Autowired private DomainToFtlTemplateTransformer domainToFtlTemplateTransformer;

  public String computeValue(
      final String eventType,
      final String format,
      final String payload,
      final Map<String, Object> messageHeaderByNameMap,
      final Map<String, Object> templateParamByNameMap,
      final String templatePath)
      throws Exception {
    if (templateParamByNameMap.isEmpty()) {
      templateParamByNameMap.put(MESSAGE_HEADER, messageHeaderByNameMap);
      if (JSON.name().equalsIgnoreCase(format)) {
        templateParamByNameMap.put(
            MESSAGE_BODY,
            JsonUtils.unmarshal(Optional.ofNullable(payload), Map.class.getCanonicalName()));
      } else if (XML.name().equalsIgnoreCase(format)) {
        templateParamByNameMap.put(MESSAGE_BODY, XmlUtil.convertToMap(payload));
      }
    }
    return domainToFtlTemplateTransformer.transform(
        eventType, templateParamByNameMap, templatePath);
  }
}
