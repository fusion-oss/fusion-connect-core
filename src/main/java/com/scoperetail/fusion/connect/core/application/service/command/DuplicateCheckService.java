package com.scoperetail.fusion.connect.core.application.service.command;

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

import com.scoperetail.fusion.adapter.dedupe.DedupeOutboundPort;
import com.scoperetail.fusion.connect.core.application.port.in.command.DuplicateCheckUseCase;
import com.scoperetail.fusion.connect.core.application.port.in.command.HashServiceUseCase;
import com.scoperetail.fusion.shared.kernel.common.annotation.UseCase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Objects;

@UseCase
@Slf4j
public class DuplicateCheckService implements DuplicateCheckUseCase {
  private HashServiceUseCase hashServiceUseCase;
  private DedupeOutboundPort dedupeOutboundPort;

  public DuplicateCheckService(
      final HashServiceUseCase hashServiceUseCase,
      @Autowired(required = false) final DedupeOutboundPort dedupeOutboundPort) {
    this.hashServiceUseCase = hashServiceUseCase;
    this.dedupeOutboundPort = dedupeOutboundPort;
  }

  @Override
  public boolean isDuplicate(final String idempotencyKey) throws Exception {
    boolean isDuplicate = false;
    if (Objects.nonNull(dedupeOutboundPort)) {
      final String hashKey = hashServiceUseCase.generateHash(idempotencyKey);
      isDuplicate = !dedupeOutboundPort.isNotDuplicate(hashKey);
    } else {
      log.info(
          "Dedupe Route configured incorrectly, try adding property db.type as Relational/Apache-Cassandra/Astra-Cassandra");
    }
    return isDuplicate;
  }
}
