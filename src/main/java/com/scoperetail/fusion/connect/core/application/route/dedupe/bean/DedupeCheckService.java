package com.scoperetail.fusion.connect.core.application.route.dedupe.bean;

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

import static com.scoperetail.fusion.connect.core.common.constant.ErrorStatus.DUPLICATE_EVENT;
import static com.scoperetail.fusion.connect.core.common.constant.ErrorStatus.STATUS;
import static com.scoperetail.fusion.connect.core.common.constant.ExchangePropertyConstants.CONTINUE_ON_DUPLICATE;
import static com.scoperetail.fusion.connect.core.common.constant.ExchangePropertyConstants.IDEMPOTENCY_KEY;
import static com.scoperetail.fusion.connect.core.common.constant.ExchangePropertyConstants.IS_VALID_MESSAGE;
import org.apache.camel.Exchange;
import org.apache.commons.lang3.StringUtils;
import com.scoperetail.fusion.connect.core.application.port.in.command.DuplicateCheckUseCase;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@AllArgsConstructor
@Slf4j
public class DedupeCheckService {

  private DuplicateCheckUseCase duplicateCheckUseCase;

  public void isDuplicate(final Exchange exchange) throws Exception {
    boolean isDuplicate = false;
    final String idempotencyKey = exchange.getProperty(IDEMPOTENCY_KEY, String.class);
    if (StringUtils.isNotBlank(idempotencyKey)) {
      isDuplicate = duplicateCheckUseCase.isDuplicate(idempotencyKey);
    } else {
      log.info("Dedupe Route configured incorrectly, try adding idempotencyKey");
    }
   Boolean isExist= exchange.getProperty(CONTINUE_ON_DUPLICATE, Boolean.class);
    if(isExist != null && isExist){
      isDuplicate =
              isDuplicate ? exchange.getProperty(CONTINUE_ON_DUPLICATE, Boolean.class) : isDuplicate;
    }else {
      isDuplicate =
              isDuplicate ? Boolean.FALSE : isDuplicate;
    }
    if (isDuplicate) {
      exchange.setProperty(IS_VALID_MESSAGE, false);
      exchange.setProperty(STATUS, DUPLICATE_EVENT.getErrorStatus());
    }
  }
}
