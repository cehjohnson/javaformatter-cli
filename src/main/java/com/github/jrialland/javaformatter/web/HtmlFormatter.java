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
package com.github.jrialland.javaformatter.web;

import java.nio.file.Files;
import java.nio.file.Path;

public class HtmlFormatter extends AbstractJsBeautifyFormatter {

   public HtmlFormatter() {
      super(
            "bower_components/js-beautify/js/lib/beautify-html.js",
            "html_beautify",
            "{'indent_inner_html': false,'indent_size': 2,'indent_char': ' ','wrap_line_length': 78,'brace_style': 'expand','unformatted': ['a', 'sub', 'sup', 'b', 'i', 'u'],'preserve_newlines': true,'max_preserve_newlines': 5,'indent_handlebars': false,'extra_liners': ['/html']}");
   }

   @Override
   public String getName() {
      return "Html beautifier";
   }

   @Override
   public String getType() {
      return "html";
   }

   @Override
   public boolean mayApplyOn(Path file) {
      return Files.isRegularFile(file)
            && file.toString().toLowerCase().endsWith(".html");
   }

   @Override
   public String getShortDesc() {
      return "Beautifies .html files";
   }
}
