package com.scoperetail.fusion.connect.core.common.util.matcher.impl;

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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import com.scoperetail.fusion.connect.core.common.helper.DocumentBuilderHelper;
import com.scoperetail.fusion.connect.core.common.util.matcher.EventMatcher;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class XmlEventMatcher implements EventMatcher {
  @Autowired private DocumentBuilderHelper documentBuilderHelper;

  @Override
  public boolean match(final String eventType, final String expression, final String payload) {
    boolean isMatched = false;
    try {
      final InputStream is = new ByteArrayInputStream(payload.getBytes());
      final Document document = documentBuilderHelper.getDocumentBuilder().parse(is);
      final XPath xPath = XPathFactory.newInstance().newXPath();
      final Object object = xPath.compile(expression).evaluate(document);
      isMatched = eventType.equalsIgnoreCase(object.toString());
    } catch (final Exception e) {
      log.error(
          "Unable to match eventType: {} expression: {} due to exception: {}",
          eventType,
          expression,
          e.getMessage());
    }
    return isMatched;
  }

  @Override
  public boolean contains(
      final List<String> configurations, final String expression, final String payload) {
    boolean isMatched = false;
    try {
      final InputStream is = new ByteArrayInputStream(payload.getBytes());
      final Document document = documentBuilderHelper.getDocumentBuilder().parse(is);
      final XPath xPath = XPathFactory.newInstance().newXPath();
      final Object object = xPath.compile(expression).evaluate(document);
      isMatched = configurations.contains(object.toString());
    } catch (final Exception e) {
      log.error(
          "Unable to match configured list: {} expression: {} due to exception: {}",
          configurations,
          expression,
          e.getMessage());
    }
    return isMatched;
  }
}
