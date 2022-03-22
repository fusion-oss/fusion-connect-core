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

public final class ExchangePropertyConstants {
  private ExchangePropertyConstants() {}

  public static final String IS_VALID_MESSAGE = "isValidMessage";
  public static final String EXCEPTION = "exception";
  public static final String PAYLOAD = "payload";
  public static final String TARGET_DELIMITER = "targetDelimiter";
  public static final String EVENT = "event";
  public static final String EVENT_TYPE = "event.type";
  public static final String EVENT_FORMAT = "event.format";
  public static final String SOURCE = "source";
  public static final String SOURCE_TYPE = "sourceType";
  public static final String SOURCE_ERROR_PAYLOAD_TEMPLATE_URI = "errorPayloadTemplateUri";
  public static final String SOURCE_ERROR_HEADER_TEMPLATE_URI = "errorHeaderTemplateUri";
  public static final String ERROR_TARGET_URI = "errorTargetUri";
  public static final String IDEMPOTENCY_KEY = "idempotencyKey";
  public static final String CONTINUE_ON_DUPLICATE = "continueOnDuplicate";
  public static final String ACTION = "action";
  public static final String ACTIONS = "actions";
  public static final String CONFIG_LOOK_UP_KEY = "configLookupKey";
  public static final String PLUGIN = "plugin";
  public static final String TRANSFORMER_TEMPLATE_URI = "transformerTemplateUri";
  public static final String ACTION_COUNT = "actionCount";
  public static final String VALIDATOR_URI = "validatorUri";
  public static final String MANDATORY_HEADERS_VALIDATOR_URI = "mandatoryHeadersValidatorUri";
  public static final String TARGET_HEADER_BLACK_LIST = "targetHeaderBlacklist";
  public static final String CACHE_DATA_URL = "cacheDataUrl";
  public static final String METHOD_TYPE = "methodType";
  public static final String TARGET_URI = "targetUri";
  public static final String TARGET_URI_PARAMS = "targetUriParams";
  public static final String ADD_CUSTOM_TARGET_HEADERS = "addCustomTargetHeaders";
  public static final String EVENT_DATA_MAP = "eventDataMap";
  public static final String TENANT_IDENTIFIER_HEADER = "tenantIdentifierHeader";
  public static final String CUSTOM_MESSAGE_HEADER = "CUSTOM_HEADER";
}
