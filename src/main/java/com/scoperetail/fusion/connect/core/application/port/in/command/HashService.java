package com.scoperetail.fusion.connect.core.application.port.in.command;

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

import java.io.IOException;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import com.fasterxml.jackson.core.type.TypeReference;
import com.scoperetail.fusion.connect.core.common.util.HashUtil;
import com.scoperetail.fusion.connect.core.common.util.JsonUtils;
import com.scoperetail.fusion.shared.kernel.common.annotation.UseCase;
import com.scoperetail.fusion.shared.kernel.events.DomainProperty;
import lombok.AllArgsConstructor;

@UseCase
@AllArgsConstructor
public class HashService implements HashServiceUseCase {
  private static final String EQUALS = "=";
  private static final String SEMICOLON = ";";

  @Override
  public String generateHash(final Set<DomainProperty> properties) {
    final StringBuilder hashKeyBuilder = new StringBuilder();
    properties.forEach(
        property -> {
          hashKeyBuilder.append(property.getName());
          hashKeyBuilder.append(EQUALS);
          hashKeyBuilder.append(property.getValue());
          hashKeyBuilder.append(SEMICOLON);
        });
    return HashUtil.getHash(hashKeyBuilder.toString(), HashUtil.SHA3_512);
  }

  @Override
  public String generateHash(final String idempotencyKey) throws Exception {
    return generateHash(getProperties(idempotencyKey));
  }

  public Set<DomainProperty> getProperties(final String hashKeyJson) throws IOException {
    final Set<DomainProperty> properties =
        JsonUtils.unmarshal(
            Optional.of(hashKeyJson), Optional.of(new TypeReference<TreeSet<DomainProperty>>() {}));
    properties.forEach(
        u -> {
          u.setName(u.getName().trim());
          u.setValue(u.getValue().trim());
        });
    return properties;
  }
}
