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
import static com.scoperetail.fusion.connect.core.common.constant.ExchangePropertyConstants.CONFIG_LOOK_UP_KEY;
import static com.scoperetail.fusion.connect.core.common.constant.ExchangePropertyConstants.EVENT;
import static com.scoperetail.fusion.connect.core.common.constant.ExchangePropertyConstants.IS_VALID_MESSAGE;
import java.util.HashMap;
import java.util.Map;
import org.apache.camel.Exchange;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import com.scoperetail.fusion.connect.core.config.Event;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BuildConfigSpec {

  public void build(final Exchange exchange) {
    final boolean isValidMessage = exchange.getProperty(IS_VALID_MESSAGE, Boolean.class);
    if (isValidMessage) {
      final String configLookupKey = exchange.getProperty(CONFIG_LOOK_UP_KEY, String.class);
      final Event event = exchange.getProperty(EVENT, Event.class);
      log.debug("Building config spec started for event: {}", event.getEventType());
      final Map<String, Map<String, Object>> configSpec = event.getConfigSpec();
      final Map<String, Object> configSpecByNameMap = new HashMap<>(configSpec.get("default"));
      if (StringUtils.isNotBlank(configLookupKey) && !"default".equals(configLookupKey)) {
        final String key = event.getEventType() + UNDERSCORE + configLookupKey;
        log.debug("Overriding default config specifications using the config look up key: {}", key);
        final Map<String, Object> specificConfigSpec = configSpec.get(key);
        if (MapUtils.isNotEmpty(specificConfigSpec)) {
          configSpecByNameMap.putAll(specificConfigSpec);
        }
      }
      configSpecByNameMap.forEach(exchange::setProperty);
      log.debug("Building config spec completed for event: {}", event.getEventType());
    }
  }
}
