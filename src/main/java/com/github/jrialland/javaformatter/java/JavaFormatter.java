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
package com.github.jrialland.javaformatter.java;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.digester3.Digester;
import org.apache.commons.digester3.RuleSetBase;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.formatter.CodeFormatter;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.TextEdit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import com.github.jrialland.javaformatter.SourceFormatter;
import com.github.jrialland.javaformatter.StringUtil;

public class JavaFormatter implements SourceFormatter {

   private static final Logger LOGGER = LoggerFactory
         .getLogger(JavaFormatter.class);

   private String source = "1.8";

   // The default separator should be that of the OS
   private String lineSep = System.getProperty("line.separator");

   private String encoding = "utf-8";

   private Map<String, String> options;

   private CodeFormatter codeFormatter;

   private String headerComment;

   public JavaFormatter(URL configUrl) throws IOException, SAXException {
      options = readConfigurationFormXml(configUrl);
      codeFormatter = ToolFactory.createCodeFormatter(options);
   }

   public void setHeader(URL headerUrl) {
      try {
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         InputStream is = headerUrl.openStream();
         byte[] buf = new byte[4096];
         int c = 0;
         while ((c = is.read(buf)) > -1) {
            baos.write(buf, 0, c);
         }
         is.close();
         headerComment = StringUtil.toJavaComment(baos.toString("utf-8"));
      } catch (Exception e) {
         throw new RuntimeException(e);
      }
   }

   public void setSource(String source) {
      this.source = source;
   }

   public String getSource() {
      return source;
   }

   public void setLineSep(String lineSep) {
      this.lineSep = lineSep;
   }

   public void setEncoding(String encoding) {
      this.encoding = encoding;
   }

   public String getEncoding() {
      return encoding;
   }

   public JavaFormatter() {
      super();
      options = new TreeMap<String, String>();
      options.put(JavaCore.COMPILER_SOURCE, source);
      options.put(JavaCore.COMPILER_COMPLIANCE, source);
      options.put(JavaCore.CORE_ENCODING, encoding);
      codeFormatter = ToolFactory.createCodeFormatter(options);
   }

   protected Map<String, String> readConfigurationFormXml(URL configUrl)
         throws IOException, SAXException {
      Digester digester = new Digester();
      digester.addRuleSet(new RuleSetBase() {
         @Override
         public void addRuleInstances(Digester digester) {
            digester.addObjectCreate("profiles", Profiles.class);
            digester.addObjectCreate("profiles/profile", Profile.class);
            digester.addObjectCreate("profiles/profile/setting", Setting.class);
            digester.addSetNext("profiles/profile", "addProfile");
            digester.addSetNext("profiles/profile/setting", "addSetting");
            digester.addSetProperties("profiles/profile", "kind", "kind");
            digester.addSetProperties("profiles/profile/setting", "id", "id");
            digester.addSetProperties("profiles/profile/setting", "value",
                  "value");
         }
      });
      Object result = digester.parse(configUrl.openStream());
      if (result != null) {
         if (result instanceof Profiles) {
            List<Map<String, String>> list = ((Profiles) result).getProfiles();
            if (list.isEmpty()) {
               return new TreeMap<String, String>();
            } else {
               return list.iterator().next();
            }
         } else {
            throw new IllegalArgumentException("could not read "
                  + configUrl.toExternalForm());
         }
      } else {
         throw new IllegalArgumentException("could not read "
               + configUrl.toExternalForm());
      }
   }

   public String format(String javaCode) {
      TextEdit textEdit = codeFormatter.format(CodeFormatter.K_COMPILATION_UNIT
            + CodeFormatter.F_INCLUDE_COMMENTS, javaCode, 0, javaCode.length(),
            0, lineSep);
      if (textEdit == null) {
         return addHeader(headerComment, javaCode);
      }
      IDocument doc = new Document(javaCode);
      try {
         textEdit.apply(doc);
      } catch (BadLocationException e) {
         throw new RuntimeException(e);
      }
      return addHeader(headerComment, doc.get());
   }

   private String addHeader(String header, String source) {
      if (header == null) {
         return source;
      } else {
         return StringUtil.insertHeader(header, source);
      }
   }

   public void formatFile(Path javaFile) throws IOException {
      LOGGER.info("format " + javaFile.toString());
      Path tmpFile = Paths.get(javaFile.toString() + "~");
      // backup
      Files.copy(javaFile, tmpFile, StandardCopyOption.REPLACE_EXISTING);
      try {
         byte[] data = Files.readAllBytes(javaFile);
         String formatted = format(new String(data, encoding));
         Files.copy(new ByteArrayInputStream(formatted.getBytes()), javaFile,
               StandardCopyOption.REPLACE_EXISTING);
         Files.delete(tmpFile);
      } catch (Exception e) {
         LOGGER.error("while formatting file", e);
         Files.copy(tmpFile, javaFile, StandardCopyOption.REPLACE_EXISTING);
      }
   }

   @Override
   public String apply(String fileContent) {
      return format(fileContent);
   }

   @Override
   public String getName() {
      return "Eclipse java formatter";
   }

   @Override
   public String getType() {
      return "java";
   }

   @Override
   public boolean mayApplyOn(Path file) {
      return Files.isRegularFile(file) && file.toString().endsWith(".java");
   }

   @Override
   public String getShortDesc() {
      return "Formats java source files";
   }
}
