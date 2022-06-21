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

public class ResourceNameConstants {
  private ResourceNameConstants() {}

  public static final String ERROR_HEADER_TEMPLATE_NAME = "error_header.ftl";
  public static final String ERROR_PAYLOAD_TEMPLATE_NAME = "error_payload.ftl";
  public static final String CONFIG_LOOKUP_KEY_TEMPLATE_NAME = "config_lookup_key.ftl";
  public static final String TARGET_URI_TEMPLATE_NAME = "target_uri.ftl";
  public static final String IDEMPOTENCY_KEY_TEMPLATE_NAME = "idempotency_key.ftl";
  public static final String MANDATORY_HEADER_SCHEMA_NAME = "mandatoryHeaders.jsd";
  public static final String PAYLOAD_SCHEMA_NAME = "payload";
  public static final String TRANSFORMER_TEMPLATE_NAME = "transformer.ftl";
  public static final String HEADER_CUSTOMIZER_TEMPLATE_NAME = "headerCustomizer.ftl";

  public static final String SPLIT_CUSTOMIZER_TEMPLATE_NAME = "SplitMessageheaderCustomizer.ftl";
}
