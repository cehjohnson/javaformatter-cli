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
package com.github.jrialland.javaformatter.compass;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Callable;

import com.darrinholst.sass_java.Compiler;
import com.github.jrialland.javaformatter.Transpiler;

/**
 * Reads a file named compass.rb with compass configuration, and apply it on the
 * project.
 * 
 * Sample configuration file :
 * 
 * <code>
 * preferred_syntax = :sass
 * http_path = '/'
 * css_dir = 'assets/stylesheets'
 * sass_dir = 'assets/sass'
 * images_dir = 'assets/images'
 * javascripts_dir = 'assets/javascripts'
 * relative_assets = true
 * line_comments = true
 * output_style = :compressed
 * </code>
 * 
 * @author Julien.Rialland
 * 
 */
public class Compass implements Transpiler {

   private Callable<Compiler> callable;

   private File configFile;

   public Compass() {
      callable = new Callable<Compiler>() {
         @Override
         public Compiler call() throws Exception {
            final Compiler compiler = new Compiler();
            callable = new Callable<Compiler>() {
               @Override
               public Compiler call() throws Exception {
                  return compiler;
               }
            };
            return compiler;
         }
      };
   }

   protected Compiler getCompiler() {
      try {
         return callable.call();
      } catch (Exception e) {
         throw new RuntimeException(e);
      }
   }

   @Override
   public boolean accept(Path path) {
      if (Files.isRegularFile(path)
            && path.getFileName().toString().equals("compass.rb")) {
         setConfigFile(path.toFile());
         return true;
      }
      return false;
   }

   public void setConfigFile(File configFile) {
      this.configFile = configFile;
   }

   @Override
   public void transpile(Path file) {
      Compiler compiler = getCompiler();
      compiler.setConfigLocation(configFile);
      compiler.compile();
   }

   @Override
   public String getName() {
      return "Sass";
   }

   @Override
   public String getType() {
      return "sass";
   }

   @Override
   public String getShortdesc() {
      return "Runs compass (http://compass-style.org/) on compass.rb";
   }
}
