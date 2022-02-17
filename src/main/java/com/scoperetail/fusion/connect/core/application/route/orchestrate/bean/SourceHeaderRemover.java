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

import java.util.Arrays;
import org.apache.camel.Exchange;
import org.apache.commons.lang3.StringUtils;
import com.scoperetail.fusion.connect.core.common.constant.ExchangePropertyConstants;
import com.scoperetail.fusion.connect.core.config.Source;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SourceHeaderRemover {

  public void removeSourceHeaders(final Exchange exchange) {
    final Source source = (Source) exchange.getProperty(ExchangePropertyConstants.SOURCE);
    final String removeTargetHeadersStr = source.getRemoveTargetHeaders();
    if (StringUtils.isNotBlank(removeTargetHeadersStr)) {
      Arrays.stream(removeTargetHeadersStr.split(","))
          .forEach(
              s -> {
                exchange.getIn().removeHeader(s);
                exchange.getIn().removeHeaders(s);
              });
      log.debug("Removed target headers: {}", removeTargetHeadersStr);
    }
  }
}