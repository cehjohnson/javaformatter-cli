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
package com.github.jrialland.javaformatter.xml;

import java.io.StringReader;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import com.github.jrialland.javaformatter.SourceFormatter;

public class XmlFormatter implements SourceFormatter {

   private static final List<String> extensions = Arrays.asList(".xml",
         ".xhtml", ".svg");

   @Override
   public String apply(String xmlFile) {
      try {
         Transformer transformer = TransformerFactory.newInstance()
               .newTransformer();
         transformer.setOutputProperty(OutputKeys.INDENT, "yes");
         StringWriter sw = new StringWriter();
         StreamResult r = new StreamResult(sw);
         StreamSource s = new StreamSource(new StringReader(xmlFile));
         transformer.transform(s, r);
         return sw.toString();
      } catch (Exception e) {
         throw new RuntimeException(e);
      }
   }

   @Override
   public String getName() {
      return "Xml formatter";
   }

   @Override
   public String getType() {
      return "xml";
   }

   @Override
   public boolean mayApplyOn(Path file) {
      if (Files.isRegularFile(file)) {
         for (String ext : extensions) {
            if (file.toString().endsWith(ext)) {
               return true;
            }
         }
      }
      return false;
   }

   @Override
   public String getShortDesc() {
      return "Formats xml files (.xml, *.xhtml, *.svg)";
   }
}
