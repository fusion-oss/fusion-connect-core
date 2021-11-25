package com.scoperetail.fusion.connect.core.common.util;

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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import freemarker.template.SimpleHash;
import freemarker.template.SimpleScalar;
import freemarker.template.SimpleSequence;
import freemarker.template.TemplateModelException;

import java.util.Map;

public class TestUtil {

  private static int GTIN_SIZE = 14;

  public static String getGtinFromUpc(String upc) {
    if (upc == null) {
      return null;
    }
    return padUpcToGtin14(upc + checkDigit(upc));
  }

  public static char checkDigit(String upc) {
    int sum = 0;
    for (int i = 0, position = upc.length(); i < upc.length(); i++, position--) {
      int n = upc.charAt(i) - '0';
      sum += n + (n + n) * (position & 1);
    }
    return (char) ('0' + ((10 - (sum % 10)) % 10));
  }

  public static String padUpcToGtin14(String upc) {
    if (upc.length() >= GTIN_SIZE) {
      return upc;
    }
    String s = "00000000000000".substring(0, (GTIN_SIZE - upc.length())) + upc;
    return s;
  }

  public static String hashToString(SimpleHash hash) throws TemplateModelException {
    // Get Map from hash
    Map<String, String> map = hash.toMap();
    ObjectMapper mapper = new ObjectMapper();
    try {
      // MAP -> JSON
      String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(map);
      return json;
    } catch (JsonProcessingException e) {
      // Return error
      e.printStackTrace();
      return e.toString();
    }
  }

  public static String sequenceToString(SimpleSequence seq) throws TemplateModelException {
    String json = "[";
    for (int i = 0; i < seq.size(); i++) {
      Object current = seq.get(i);

      if (current.getClass() == SimpleHash.class) {
        SimpleHash currentHash = (SimpleHash) seq.get(i);
        json += TestUtil.hashToString(currentHash);
      }

      if (current.getClass() == SimpleScalar.class) json += current.toString();

      if (i < seq.size() - 1) json += ",";
    }
    json += "]";
    return json;
  }
}
