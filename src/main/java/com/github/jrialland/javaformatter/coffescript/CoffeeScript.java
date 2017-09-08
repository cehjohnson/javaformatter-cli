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
package com.github.jrialland.javaformatter.coffescript;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import com.github.jrialland.javaformatter.Transpiler;

public class CoffeeScript implements Transpiler {

   private ScriptEngine scriptEngine;

   public CoffeeScript() {
      scriptEngine = new ScriptEngineManager().getEngineByName("js");
      InputStream coffeCompiler = CoffeeScript.class
            .getClassLoader()
            .getResourceAsStream(
                  "META-INF/resources/webjars/coffee-script/1.11.0/coffee-script.min.js");
      try {
         scriptEngine.eval(new InputStreamReader(coffeCompiler));
      } catch (Exception e) {
         throw new RuntimeException(e);
      }
   }

   @Override
   public boolean accept(Path path) {
      return Files.isRegularFile(path) && path.toString().endsWith(".coffee");
   }

   @Override
   public String getName() {
      return "CoffeeScript compiler";
   }

   @Override
   public String getType() {
      return "coffeescript";
   }

   @Override
   public void transpile(Path file) {
      try {
         byte[] data = Files.readAllBytes(file);
         Path output = Paths.get(file.toAbsolutePath().toString()
               .replaceFirst("\\.coffee$", ".coffee.js"));
         if (!Files.isRegularFile(output)) {
            Files.createFile(output);
         }
         Bindings bindings = scriptEngine
               .getBindings(ScriptContext.ENGINE_SCOPE);
         bindings.put("coffescript_src", new String(data));
         Object coffeSrc = scriptEngine.eval(
               "CoffeeScript.compile(coffescript_src, {bare: true})", bindings);
         Files.write(output, coffeSrc.toString().getBytes(),
               StandardOpenOption.WRITE);
      } catch (Exception e) {
         throw new RuntimeException(e);
      }
   }

   @Override
   public String getShortdesc() {
      return "Runs coffescript compiler on *.coffee files";
   }

   public static void main(String[] args) {
      new CoffeeScript()
            .transpile(Paths
                  .get("/home/jrialland/dev/gitprojects/androidtest/test/testcoffee.coffee"));
   }
}
