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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public final class PathUtil {

   public static interface FileOp {
      public String doWithContent(Path path, String content) throws Exception;
   }

   public static void doWithTmpFile(Path path, FileOp operation) {
      byte[] data = null;
      Path tmpFile = Paths.get(path.toAbsolutePath().toString() + "~");
      // backup
      try {
         data = Files.readAllBytes(path);
         Files.move(path, tmpFile, StandardCopyOption.REPLACE_EXISTING);
      } catch (IOException e) {
         throw new RuntimeException(e);
      }

      try {
         String newContent = operation.doWithContent(path, new String(data));
         Files.copy(new ByteArrayInputStream(newContent.getBytes()), path,
               StandardCopyOption.REPLACE_EXISTING);
      } catch (Exception e) {
         // revert
         try {
            Files.move(tmpFile, path, StandardCopyOption.REPLACE_EXISTING);
         } catch (IOException e2) {
            throw new RuntimeException(e2);
         }
      }

   }
}
