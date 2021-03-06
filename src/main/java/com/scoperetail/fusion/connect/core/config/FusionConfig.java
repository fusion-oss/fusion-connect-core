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

import com.scoperetail.fusion.adapter.dedupe.cassandra.config.FusionCassandraDedupeConfig;
import com.scoperetail.fusion.adapter.dedupe.jpa.config.FusionJpaDedupeConfig;
import com.scoperetail.fusion.connect.core.common.constant.SourceType;
import lombok.*;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import javax.annotation.PostConstruct;
import java.util.*;

@Configuration
@ConfigurationProperties(prefix = "fusion")
@Getter
@Setter
@ToString
@Import({FusionJpaDedupeConfig.class, FusionCassandraDedupeConfig.class})
public class FusionConfig {
  private String resourceURL;
  private String resourceDirectory;
  private String cacheDataUrl;
  private List<Source> sources;
  private List<Event> events;
  private Map<String, String> sourceTypes;
  private final List<AMQBroker> amqBrokers = new ArrayList<>(1);
  private final Map<String, Map<String, Object>> cache = new HashMap<>(1);

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

  void setCacheData(final Map<String, Map<String, Object>> cacheData) {
    this.cache.putAll(cacheData);
  }

  public Map<String, Object> getCacheDataByTenantId(final String key) {
    return cache.get(key);
  }
}
