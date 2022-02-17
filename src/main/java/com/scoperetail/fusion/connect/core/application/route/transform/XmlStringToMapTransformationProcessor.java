package com.scoperetail.fusion.connect.core.application.route.transform;

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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

@Component
public class XmlStringToMapTransformationProcessor implements Processor {

  @Override
  public void process(final Exchange exchange) throws Exception {
    final String xmlPayload = removeWhitespaces(exchange.getIn().getBody(String.class));
    exchange.getIn().setBody(convertToMap(xmlPayload));
  }

  private String removeWhitespaces(String xmlPayload) {
    xmlPayload = xmlPayload.replaceAll("(\\r\\n|\\n|\\r)", "");
    xmlPayload = xmlPayload.replaceAll(">\\s+<", "><");
    return xmlPayload;
  }

  private Map<String, Object> convertToMap(final String xml) throws Exception {
    final InputStream is = new ByteArrayInputStream(xml.getBytes());
    final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    dbf.setNamespaceAware(true);
    dbf.setIgnoringElementContentWhitespace(true);
    dbf.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
    dbf.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
    final DocumentBuilder db = dbf.newDocumentBuilder();
    final Document document = db.parse(is);
    return (Map<String, Object>) createMap(document.getDocumentElement());
  }

  public Object createMap(final Node node) {
    final Map<String, Object> map = new HashMap<>();
    final NodeList nodeList = node.getChildNodes();
    for (int i = 0; i < nodeList.getLength(); i++) {
      final Node currentNode = nodeList.item(i);
      final String name = currentNode.getNodeName();
      Object value = null;
      if (currentNode.getNodeType() == Node.ELEMENT_NODE) {
        value = createMap(currentNode);
      } else if (currentNode.getNodeType() == Node.TEXT_NODE) {
        return currentNode.getTextContent();
      }
      if (map.containsKey(name)) {
        final Object os = map.get(name);
        if (os instanceof List) {
          ((List<Object>) os).add(value);
        } else {
          final List<Object> objs = new LinkedList<Object>();
          objs.add(os);
          objs.add(value);
          map.put(name, objs);
        }
      } else {
        map.put(name, value);
      }
    }
    return map;
  }
}
