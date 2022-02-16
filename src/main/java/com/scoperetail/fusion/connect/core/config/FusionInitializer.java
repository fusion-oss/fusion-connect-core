package com.scoperetail.fusion.connect.core.config;

import static com.scoperetail.fusion.connect.core.application.route.cache.CacheRoute.CACHE_ROUTE;

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

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.zeroturnaround.zip.ZipUtil;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class FusionInitializer implements ApplicationListener<ContextRefreshedEvent> {
  private final FusionConfig fusionConfig;
  private final ResourceLoader resourceLoader;
  private final String resourceDirectory;
  private final ProducerTemplate producerTemplate;

  @Getter(AccessLevel.NONE)
  @Setter(AccessLevel.NONE)
  private final Map<String, Map<String, Object>> cache = new HashMap<>(1);

  public FusionInitializer(
      final FusionConfig fusionConfig,
      final ResourceLoader resourceLoader,
      final CamelContext camelContext,
      @Value("${RESOURCE_DIRECTORY}") final String resourceDirectory) {
    this.fusionConfig = fusionConfig;
    this.resourceLoader = resourceLoader;
    this.producerTemplate = camelContext.createProducerTemplate();
    this.resourceDirectory = resourceDirectory;
  }

  @Override
  public void onApplicationEvent(final ContextRefreshedEvent event) {
    try {
      downloadResources();
      buildCache();
    } catch (final Exception e) {
      throw new RuntimeException(e);
    }
  }

  private void downloadResources() throws Exception {
    final String resourceURL = fusionConfig.getResourceURL();
    if (StringUtils.isNotBlank(resourceURL)) {
      final File resourceDir = getResourceDir();
      final Resource resource = resourceLoader.getResource(fusionConfig.getResourceURL());
      ZipUtil.unpack(resource.getInputStream(), resourceDir);
    } else {
      log.info(
          "External resource directory not specified, falling back to local resource directory");
    }
  }

  private void buildCache() throws InterruptedException, ExecutionException {
    final CompletableFuture<Object> cacheDataResponse =
        producerTemplate.asyncSendBody(CACHE_ROUTE, null);
    final Object cacheData = cacheDataResponse.get();
    if (Objects.nonNull(cacheData) && cacheData instanceof Map) {
      this.cache.putAll((Map<String, Map<String, Object>>) cacheData);
    }
  }

  private File getResourceDir() throws IOException {
    final File file = new File(Paths.get(resourceDirectory).toString());
    if (file.exists()) {
      FileUtils.cleanDirectory(file);
    } else {
      FileUtils.forceMkdir(file);
    }
    return file;
  }

  public String getResourceDirectoryBasePath() {
    return Paths.get(resourceDirectory).toAbsolutePath().toString();
  }

  public Map<String, Object> getCacheDataByTenantId(final String key) {
    return cache.get(key);
  }
}
