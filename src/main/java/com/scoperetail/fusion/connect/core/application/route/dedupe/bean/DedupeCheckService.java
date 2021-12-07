package com.scoperetail.fusion.connect.core.application.route.dedupe.bean;

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

import com.scoperetail.fusion.connect.core.application.port.in.command.DuplicateCheckUseCase;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.commons.lang3.StringUtils;

@AllArgsConstructor
@Slf4j
public class DedupeCheckService {

  private DuplicateCheckUseCase duplicateCheckUseCase;

  public void isDuplicate(final Exchange exchange) throws Exception {
    boolean isDuplicate = false;
    final String idempotencyKey = exchange.getProperty("idempotencyKey", String.class);
    if (StringUtils.isNotBlank(idempotencyKey)) {
      isDuplicate = duplicateCheckUseCase.isDuplicate(idempotencyKey);
    } else {
      log.info("Dedupe Route configured incorrectly, try adding idempotencyKey");
    }
    isDuplicate =
        isDuplicate ? !exchange.getProperty("continueOnDuplicate", Boolean.class) : isDuplicate;
    if (isDuplicate) {
      exchange.setProperty("isValidMessage", false);
      exchange.setProperty("reason", "Duplicate message found");
    }
  }
}
