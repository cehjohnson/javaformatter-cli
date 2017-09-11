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

import java.net.URL;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.jrialland.javaformatter.java.JavaFormatter;

public class FormatterCli {

   private static final Logger LOGGER = LoggerFactory
         .getLogger(FormatterCli.class);
   private static final File FORMATTER_PROFILE_FILE = new File(
         System.getProperty("user.home"), "formatter-profile.xml");

   protected static void showHelp(Options opts,
         List<SourceFormatter> sourceFormatters) {
      HelpFormatter helpFormatter = new HelpFormatter();
      helpFormatter.printHelp(FormatterCli.class.getSimpleName(), opts);
      System.out.println();

      System.out.println("Available source formatters : ");
      for (SourceFormatter fmt : sourceFormatters) {
         System.out.println("\t* " + fmt.getName() + " (" + fmt.getShortDesc()
               + ")");
      }
   }

   public static void main(String[] args) throws Exception {

      Options opts = new Options();

      Option conf = Option.builder("c").longOpt("conf").required(false)
            .numberOfArgs(1).desc("Eclipse configuration file to use")
            .argName("eclipseConf").build();
      opts.addOption(conf);

      Option level = Option.builder("l").longOpt("level").required(false)
            .numberOfArgs(1).argName("javaVersion").desc("source level")
            .build();
      opts.addOption(level);

      Option header = Option.builder("H").longOpt("header").required(false)
            .numberOfArgs(1).argName("txtFile").desc("source file header")
            .build();
      opts.addOption(header);

      Option encoding = Option.builder("e").longOpt("encoding").required(false)
            .numberOfArgs(1).argName("charset").desc("source encoding").build();
      opts.addOption(encoding);

      Option lsep = Option.builder("s").longOpt("linesep").required(false)
            .hasArg(true).argName("crlf_value").build();
      opts.addOption(lsep);

      Option help = Option.builder("h").longOpt("help").hasArg(false)
            .desc("Shows this help").build();
      opts.addOption(help);

      CommandLine cmd = new DefaultParser().parse(opts, args);

      JavaFormatter javaFormatter;
      List<SourceFormatter> formatters = new ArrayList<SourceFormatter>();

      // Look for formatter profile file in home directory
      if (cmd.hasOption("conf")) {
         URL confUrl = Paths.get(cmd.getOptionValue("conf")).toUri().toURL();
         javaFormatter = new JavaFormatter(confUrl);
         if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Using command line configuration with url {}", confUrl);
         }
      } else if (FORMATTER_PROFILE_FILE.exists()) {
         if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Using home directory configuration at {}",
                  FORMATTER_PROFILE_FILE);
         }
         javaFormatter = new JavaFormatter(FORMATTER_PROFILE_FILE.toURL());
      } else {
         if (LOGGER.isInfoEnabled()) {
            LOGGER.info(
                  "No command line configuration parameter found, nor home directory configuration of {}. Using Eclipse default formatting",
                  FORMATTER_PROFILE_FILE);
         }
         javaFormatter = new JavaFormatter();
      }
      formatters.add(javaFormatter);

      if (cmd.hasOption("help")) {
         showHelp(opts, formatters);
         return;
      }

      if (cmd.hasOption("level")) {
         javaFormatter.setSource(cmd.getOptionValue("level"));
      }

      if (cmd.hasOption("encoding")) {
         javaFormatter.setEncoding(cmd.getOptionValue("encoding"));
      }

      if (cmd.hasOption("linesep")) {
         String linesep = cmd.getOptionValue("linesep");
         if (!Arrays.asList("lf", "cr", "crlf").contains(linesep)) {
            throw new IllegalArgumentException(
                  "linesep : must be one of ['lf', 'cr', 'crlf']");
         }
         linesep = linesep.toLowerCase().replaceAll("cr", "\r")
               .replaceAll("lf", "\n");
         javaFormatter.setLineSep(linesep);
      }

      if (cmd.hasOption("header")) {
         javaFormatter.setHeader(Paths.get(cmd.getOptionValue("header"))
               .toUri().toURL());
      }

      if (args.length == 0) {
         System.out.println("Missing file or directory parameter.");
         showHelp(opts, formatters);
         System.exit(255);
      }

      Path path = Paths.get(args[args.length - 1]);

      if (LOGGER.isDebugEnabled()) {
         LOGGER.debug("Registered formatters : ");
         for (SourceFormatter fmt : formatters) {
            LOGGER.debug("\t- " + fmt.getName());
         }
      }

      // apply formatters
      if (Files.isRegularFile(path)) {
         new FormatterVisitor().applyAllFormattersOnFile(path, formatters);
      } else if (Files.isDirectory(path)) {
         new FormatterVisitor().visitWithFormatters(path, formatters);
      } else {
         throw new IllegalArgumentException("unsupported path : " + path);
      }

   }
}
