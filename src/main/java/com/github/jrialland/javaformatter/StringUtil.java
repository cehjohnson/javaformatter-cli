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
package com.github.jrialland.javaformatter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class StringUtil {

   public static String toJavaComment(String txt) {
      try {
         BufferedReader br = new BufferedReader(new StringReader(txt));
         StringWriter sw = new StringWriter();
         String line = null;
         boolean first = true;
         while ((line = br.readLine()) != null) {
            if (first) {
               sw.append("/* ");
               first = false;
            } else {
               sw.append(" * ");
            }
            sw.append(line.replaceAll("\r|\n", ""));
            sw.append("\n");
         }
         sw.append(" */\n");
         String comment = sw.toString();

         Date date = new Date();

         comment = comment.replaceAll("\\$\\{year\\}", new SimpleDateFormat(
               "yyyy").format(date));
         comment = comment.replaceAll("\\$\\{month\\}", new SimpleDateFormat(
               "MM").format(date));
         comment = comment.replaceAll("\\$\\{day\\}",
               new SimpleDateFormat("dd").format(date));
         for (Entry<String, String> entry : System.getenv().entrySet()) {
            comment = comment.replaceAll("\\$\\{env\\." + entry.getKey()
                  + "\\}", entry.getValue());
         }
         return comment;
      } catch (IOException e) {
         throw new RuntimeException(e);
      }
   }

   public static String insertHeader(String header, String javaSource) {
      Matcher m = Pattern
            .compile(
                  "package|import|public|protected|private|class|interface|@enum|enum")
            .matcher(javaSource);
      if (m.find()) {
         int pos = m.start();
         if (pos == 0 || javaSource.charAt(pos - 1) == '\n') {
            return header + javaSource.substring(m.start());
         }
      }
      return javaSource;
   }
}
