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

import static com.scoperetail.fusion.connect.core.common.constant.CharacterConstant.UNDERSCORE;
import static com.scoperetail.fusion.connect.core.common.constant.ExchangePropertyConstants.ACTION;
import static com.scoperetail.fusion.connect.core.common.constant.ExchangePropertyConstants.ACTIONS;
import static com.scoperetail.fusion.connect.core.common.constant.ExchangePropertyConstants.ACTION_COUNT;
import static com.scoperetail.fusion.connect.core.common.constant.ExchangePropertyConstants.IS_VALID_MESSAGE;
import org.apache.camel.Exchange;
import org.apache.commons.lang3.StringUtils;

public class BuildAction {
  public void build(final Exchange exchange) {
    final boolean isValidMessage = exchange.getProperty(IS_VALID_MESSAGE, Boolean.class);
    int actionCount = 0;
    if (isValidMessage) {
      final String actions = exchange.getProperty(ACTIONS, String.class);
      if (StringUtils.isNotBlank(actions)) {
        final String[] split = actions.split(",");
        actionCount = split.length;
        int index = 0;
        for (final String s : split) {
          exchange.setProperty(ACTION + UNDERSCORE + index++, s);
        }
      }
      exchange.setProperty(ACTION_COUNT, actionCount);
    } else {
      exchange.setProperty(ACTION_COUNT, actionCount);
    }
  }
}
