package com.scoperetail.fusion.connect.core.common.constant;

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

import lombok.Getter;

public enum ErrorStatus {
  EVENT_NOT_FOUND("EVENT_NOT_FOUND"),
  DUPLICATE_EVENT("DUPLICATE_EVENT"),
  PARSE_ERROR("PARSE_ERROR"),
  MISSING_MANDATORY_VALUE("MISSING_MANDATORY_VALUE"),
  INVALID_VALUE("INVALID_VALUE");
  @Getter private final String errorStatus;

  private ErrorStatus(final String errorStatus) {
    this.errorStatus = errorStatus;
  }

  public static final String STATUS = "status";
  public static final String ERRORS = "errors";
}