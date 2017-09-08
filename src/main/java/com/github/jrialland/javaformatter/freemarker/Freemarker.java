/* Copyright (c) 2016, Julien Rialland
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * 
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 */
package com.github.jrialland.javaformatter.freemarker;

import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.jrialland.javaformatter.Transpiler;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.Version;

public class Freemarker implements Transpiler {

  private static final Logger LOGGER = LoggerFactory.getLogger(Freemarker.class);

  private static final Logger getLog() {
    return LOGGER;
  }

  @Override
  public boolean accept(Path path) {
    return Files.isRegularFile(path) && path.toString().endsWith(".ftl");
  }

  @Override
  public String getName() {
    return "Freemarker template";
  }

  @Override
  public String getType() {
    return "ftl";
  }

  @Override
  public void transpile(Path file) {
    try {
      Configuration configuration = new Configuration(new Version(2, 3, 23));
      configuration.setDirectoryForTemplateLoading(file.toFile().getParentFile());
      configuration.setOutputEncoding("utf-8");
      configuration.setAutoFlush(true);
      Template template = configuration.getTemplate(file.getFileName().toString());
      Map<String, Object> model = new HashMap<String, Object>();
      for (Entry<Object, Object> entry : System.getProperties().entrySet()) {
        String key = entry.getKey().toString();
        String value = entry.getValue().toString();
        insertValue(model, key, value);
      }

      for (Entry<String, String> entry : System.getenv().entrySet()) {
        String key = "env." + entry.getKey();
        String value = entry.getValue();
        insertValue(model, key, value);
      }

      Path outFile = Paths.get(file.toAbsolutePath().toString().replaceFirst("\\.ftl$", ""));
      FileWriter fw = new FileWriter(outFile.toFile());
      getLog().debug(String.format("%s",model));
      template.process(model, fw);
      fw.flush();
      fw.close();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private static void insertValue(Map<String, Object> root, String key, String value) {
    String[] parts = key.split("\\.");
    if (parts.length == 1) {
      root.put(key, value);
      return;
    }

    Map<String, Object> current = root;
    for (int i = 0; i < parts.length - 1; i++) {
      String part = parts[i];
      Object objChild = current.get(part);
      if (objChild instanceof String) {
        part = part + "_";
      }
      @SuppressWarnings("unchecked")
      Map<String, Object> child = (Map<String, Object>) current.get(part);
      if (child == null) {
        child = new HashMap<String, Object>();
        current.put(part, child);
      }
      current = child;
    }
    current.put(parts[parts.length - 1], value);
  }

  @Override
  public String getShortdesc() {
    return "Runs freemarker template, applies on *.ftl files";
  }

}
