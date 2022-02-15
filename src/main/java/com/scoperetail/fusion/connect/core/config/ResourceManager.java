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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.zeroturnaround.zip.ZipUtil;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ResourceManager implements ApplicationListener<ContextRefreshedEvent> {
  private final FusionConfig fusionConfig;
  private final ResourceLoader resourceLoader;
  private final String resourceDirectory;

  public ResourceManager(
      final FusionConfig fusionConfig,
      final ResourceLoader resourceLoader,
      @Value("${RESOURCE_DIRECTORY}") final String resourceDirectory) {
    this.fusionConfig = fusionConfig;
    this.resourceLoader = resourceLoader;
    this.resourceDirectory = resourceDirectory;
  }

  @Override
  public void onApplicationEvent(final ContextRefreshedEvent event) {

    final String resourceURL = fusionConfig.getResourceURL();
    if (StringUtils.isNotBlank(resourceURL)) {
      try {
        final File resourceDir = getResourceDir();
        final Resource resource = resourceLoader.getResource(fusionConfig.getResourceURL());
        final InputStream inputStream = resource.getInputStream();
        ZipUtil.unpack(inputStream, resourceDir);
      } catch (final Exception e) {
        log.error("Exception occurred while mapping external resource directory: {}", e);
        throw new RuntimeException(e);
      }
    } else {
      log.info(
          "External resource directory not specified, falling back to local resource directory");
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
}
