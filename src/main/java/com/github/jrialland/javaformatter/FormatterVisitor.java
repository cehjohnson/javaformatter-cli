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
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FormatterVisitor {

   private static final Logger LOGGER = LoggerFactory
         .getLogger(FormatterVisitor.class);


   public void visitWithFormatters(Path dir,
         final List<SourceFormatter> formatters) {
      try {

         Files.walkFileTree(dir, new FileVisitor<Path>() {
            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc)
                  throws IOException {
               return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult preVisitDirectory(Path dir,
                  BasicFileAttributes attrs) throws IOException {
               return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file,
                  BasicFileAttributes attrs) throws IOException {
               applyAllFormattersOnFile(file, formatters);
               return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc)
                  throws IOException {
               throw new RuntimeException("while applying formatters", exc);
            }
         });
      } catch (IOException e) {
         throw new RuntimeException(e);
      }
   }

   public void applyAllFormattersOnFile(Path file,
         List<SourceFormatter> formatters) throws IOException {
      Path tmpFile = Paths.get(file.toAbsolutePath().toString() + "~");
      for (SourceFormatter formatter : formatters) {
         if (formatter.mayApplyOn(file)) {

            // get file data
            byte[] data = Files.readAllBytes(file);

            // backup
            Files.move(file, tmpFile, StandardCopyOption.REPLACE_EXISTING);

            try {
               String strData = new String(data);
               LOGGER.debug("Applying formatter '{}' on {} ...", formatter.getType(), file.toString());
               String modified = formatter.apply(strData);
               LOGGER.debug("... done.");
	       if (strData.equals(modified)) {
		   LOGGER.warn("No formatting was done on {}. Check that the file compiles if you're not happy", file.toString());
	       }
               Files.copy(new ByteArrayInputStream(modified.getBytes()), file,
                     StandardCopyOption.REPLACE_EXISTING);

               // rm backup
               Files.delete(tmpFile);

            } catch (Exception e) {
               LOGGER.error("Formatter error", e);

               // replace file with the backup in case of error
               Files.move(tmpFile, file, StandardCopyOption.REPLACE_EXISTING);
            }
         }
      }
   }

}
