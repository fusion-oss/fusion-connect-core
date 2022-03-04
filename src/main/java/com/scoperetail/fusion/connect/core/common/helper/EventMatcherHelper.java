package com.scoperetail.fusion.connect.core.common.helper;

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

import static com.scoperetail.fusion.connect.core.common.constant.Format.JSON;
import static com.scoperetail.fusion.connect.core.common.constant.Format.JSON_ARRAY;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.scoperetail.fusion.connect.core.common.constant.Format;
import com.scoperetail.fusion.connect.core.common.util.matcher.EventMatcher;
import com.scoperetail.fusion.connect.core.common.util.matcher.impl.JsonEventMatcher;
import com.scoperetail.fusion.connect.core.common.util.matcher.impl.XmlEventMatcher;
import com.scoperetail.fusion.connect.core.config.Event;

@Component
public class EventMatcherHelper {

  @Autowired private JsonEventMatcher jsonEventMatcher;
  @Autowired private XmlEventMatcher xmlEventMatcher;

  public Event getEvent(
      final Map<String, Object> headers,
      final String payload,
      final Format payloadFormat,
      final Set<Event> events) {
    final EventMatcher eventMatcher =
        JSON.equals(payloadFormat) || JSON_ARRAY.equals(payloadFormat)
            ? jsonEventMatcher
            : xmlEventMatcher;
    boolean isMatch = false;
    Event matchedEvent = null;
    for (final Event event : events) {
      final String eventTypePath = event.getSpec().get("eventTypePath");
      final Object eventType = headers.get(eventTypePath);
      if (Objects.nonNull(eventType)) {
        isMatch = event.getEventType().equalsIgnoreCase(eventType.toString());
      } else {
        isMatch = eventMatcher.match(event.getEventType(), eventTypePath, payload);
      }
      if (isMatch) {
        matchedEvent = event;
        break;
      }
    }
    return matchedEvent;
  }

  //  public Event getEvent(final String payload, final Format payloadFormat, final Set<Event> events) {
  //    final EventMatcher eventMatcher =
  //        payloadFormat.equals(Format.JSON) ? jsonEventMatcher : xmlEventMatcher;
  //    boolean isMatch = false;
  //    Event matchedEvent = null;
  //    for (final Event event : events) {
  //      final String eventTypePath = event.getSpec().get("eventTypePath");
  //      isMatch = eventMatcher.match(event.getEventType(), eventTypePath, payload);
  //      if (isMatch) {
  //        matchedEvent = event;
  //        break;
  //      }
  //    }
  //    return matchedEvent;
  //  }

  //  public Event getEvent(
  //      final Map<String, Object> headers,
  //      final String payload,
  //      final Format payloadFormat,
  //      final Set<Event> events) {
  //    final EventMatcher eventMatcher =
  //        payloadFormat.equals(Format.JSON) ? jsonEventMatcher : xmlEventMatcher;
  //    boolean isMatch = false;
  //    Event matchedEvent = null;
  //    for (final Event event : events) {
  //      final String eventTypePath = event.getSpec().get("eventTypePath");
  //      if (eventTypePath.startsWith("$") || eventTypePath.startsWith("/")) {
  //        isMatch = eventMatcher.match(event.getEventType(), eventTypePath, payload);
  //      } else {
  //        final Object eventType = headers.get(eventTypePath);
  //        if (Objects.nonNull(eventType)) {
  //          isMatch = event.getEventType().equalsIgnoreCase(eventType.toString());
  //        }
  //      }
  //      if (isMatch) {
  //        matchedEvent = event;
  //        break;
  //      }
  //    }
  //    return matchedEvent;
  //  }

}
