package com.scoperetail.fusion.connect.core.config;

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

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.PostConstruct;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import com.scoperetail.fusion.adapter.dedupe.cassandra.config.FusionApacheCassandraDedupeConfig;
import com.scoperetail.fusion.adapter.dedupe.cassandra.config.FusionAstraCassandraDedupeConfig;
import com.scoperetail.fusion.adapter.dedupe.jpa.config.FusionJpaDedupeConfig;
import com.scoperetail.fusion.connect.core.common.constant.SourceType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Configuration
@ConfigurationProperties(prefix = "fusion")
@Getter
@Setter
@ToString
@Import({
  FusionJpaDedupeConfig.class,
  FusionApacheCassandraDedupeConfig.class,
  FusionAstraCassandraDedupeConfig.class
})
public class FusionConfig {
  private String resourceURL;
  private String resourceDirectory;
  private String cacheDataUrl;
  private List<Source> sources;
  private List<Event> events;
  private Map<String, String> sourceTypes;

  @Getter(AccessLevel.NONE)
  @Setter(AccessLevel.NONE)
  private final Map<EventSourceAndFormat, Set<Event>> eventsBySourceAndFormatMap = new HashMap<>();

  @PostConstruct
  private void init() {
    initEventsBySourceAndFormatMap();
  }

  private void initEventsBySourceAndFormatMap() {
    for (final Event event : events) {
      final Map<String, String> spec = event.getSpec();
      final String sourcekey = spec.get("source");
      final String formatKey = spec.get("format").toUpperCase();
      final EventSourceAndFormat eventSourceAndFormatKey =
          new EventSourceAndFormat(sourcekey, formatKey);
      Set<Event> eventsBySourceAndFormat = eventsBySourceAndFormatMap.get(eventSourceAndFormatKey);
      if (eventsBySourceAndFormat == null) {
        eventsBySourceAndFormat = new HashSet<>();
        eventsBySourceAndFormatMap.put(eventSourceAndFormatKey, eventsBySourceAndFormat);
      }
      eventsBySourceAndFormat.add(event);
    }
  }

  public Set<Event> getEvents(final String sourceName, final String format) {
    return eventsBySourceAndFormatMap.getOrDefault(
        new EventSourceAndFormat(sourceName, format), Collections.emptySet());
  }

  public SourceType getSourceType(final String source) {
    return SourceType.valueOf(sourceTypes.get(source).toUpperCase());
  }

  @Data
  @EqualsAndHashCode
  @AllArgsConstructor
  class EventSourceAndFormat {
    private String source;
    private String format;
  }
}
