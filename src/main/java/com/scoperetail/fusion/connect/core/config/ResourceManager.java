package com.scoperetail.fusion.connect.core.config;

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

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.lingala.zip4j.ZipFile;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@Component
@AllArgsConstructor
@Slf4j
public class ResourceManager implements ApplicationListener<ContextRefreshedEvent> {

  private FusionConfig fusionConfig;
  private ResourceLoader resourceLoader;

  @Override
  public void onApplicationEvent(final ContextRefreshedEvent event) {

    final String resourceDirectory = fusionConfig.getResourceDirectory();

    if (StringUtils.isNotBlank(resourceDirectory)) {
      try {
        createSystemDir(resourceDirectory);
        final Resource resource = resourceLoader.getResource(fusionConfig.getResourceURL());
        final Path resourceDirectoryPath = Path.of(resourceDirectory, resource.getFilename());
        Files.copy(
            resource.getInputStream(), resourceDirectoryPath, StandardCopyOption.REPLACE_EXISTING);
        final String path = resourceDirectoryPath.toAbsolutePath().toString();
        try (final ZipFile zipFile = new ZipFile(path)) {
          zipFile.extractAll(resourceDirectory);
          log.info("Successfully mapped external resource directory: {}", path);
        }
      } catch (final Exception e) {
        log.error("Exception occurred while mapping external resource directory: {}", e);
        throw new RuntimeException(e);
      }
    } else {
      log.info(
          "External resource directory not specified, falling back to local resource directory");
    }
  }

  private void createSystemDir(final String resourceDirectory) throws IOException {
    final File systemDirectory = new File(resourceDirectory);
    if (systemDirectory.exists()) {
      FileUtils.cleanDirectory(systemDirectory);
      FileUtils.forceDelete(systemDirectory);
    }
    FileUtils.forceMkdir(systemDirectory);
  }
}
