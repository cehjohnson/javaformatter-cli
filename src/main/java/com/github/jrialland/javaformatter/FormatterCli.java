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

import com.github.jrialland.javaformatter.coffescript.CoffeeScript;
import com.github.jrialland.javaformatter.compass.Compass;
import com.github.jrialland.javaformatter.freemarker.Freemarker;
import com.github.jrialland.javaformatter.java.JavaFormatter;
import com.github.jrialland.javaformatter.minify.Minifier;
import com.github.jrialland.javaformatter.web.CssFormatter;
import com.github.jrialland.javaformatter.web.HtmlFormatter;
import com.github.jrialland.javaformatter.web.JsFormatter;
import com.github.jrialland.javaformatter.xml.XmlFormatter;

public class FormatterCli {

  private static final Logger LOGGER = LoggerFactory.getLogger(FormatterCli.class);
  private static final String FORMATTER_PROFILE_FILENAME = "formatter-profile.xml";

  protected static void showHelp(Options opts, List<SourceFormatter> sourceFormatters, List<Transpiler> transpilers) {
    HelpFormatter helpFormatter = new HelpFormatter();
    helpFormatter.printHelp(FormatterCli.class.getSimpleName(), opts);
    System.out.println();

    System.out.println("Available source formatters : ");
    for (SourceFormatter fmt : sourceFormatters) {
      System.out.println("\t* " + fmt.getName() + " (" + fmt.getShortDesc() + ")");
    }
    System.out.println();

    System.out.println("Available transpilers : ");
    for (Transpiler transpiler : transpilers) {
      System.out.println("\t* " + transpiler.getName() + " (" + transpiler.getShortdesc() + ")");
    }
    System.out.println();

    return;
  }

  public static void main(String[] args) throws Exception {

    Options opts = new Options();

    Option conf = Option.builder("c").longOpt("conf").required(false).numberOfArgs(1).desc("Eclipse configuration file to use")
        .argName("eclipseConf").build();
    opts.addOption(conf);

    Option level = Option.builder("l").longOpt("level").required(false).numberOfArgs(1).argName("javaVersion")
        .desc("source level").build();
    opts.addOption(level);

    Option header = Option.builder("H").longOpt("header").required(false).numberOfArgs(1).argName("txtFile")
        .desc("source file header").build();
    opts.addOption(header);

    Option encoding = Option.builder("e").longOpt("encoding").required(false).numberOfArgs(1).argName("charset")
        .desc("source encoding").build();
    opts.addOption(encoding);

    Option lsep = Option.builder("s").longOpt("linesep").required(false).hasArg(true).argName("crlf_value").build();
    opts.addOption(lsep);

    Option help = Option.builder("h").longOpt("help").hasArg(false).desc("Shows this help").build();
    opts.addOption(help);

    CommandLine cmd = new DefaultParser().parse(opts, args);

    List<Transpiler> transpilers = Arrays.asList(new Freemarker(), new CoffeeScript(), new Compass(), new Minifier());

    JavaFormatter javaFormatter;
    List<SourceFormatter> formatters = new ArrayList<SourceFormatter>();

    // Look for formatter profile file in home directory
    File profileFile = new File(System.getProperty("java.home"), FORMATTER_PROFILE_FILENAME);
    if (cmd.hasOption("conf")) {
      javaFormatter = new JavaFormatter(Paths.get(cmd.getOptionValue("conf")).toUri().toURL());
    } else if (profileFile.exists()) {
      javaFormatter = new JavaFormatter(profileFile.toURL());
    } else {
      javaFormatter = new JavaFormatter();
    }
    formatters.add(javaFormatter);
    formatters.addAll(Arrays.asList(new HtmlFormatter(), new CssFormatter(), new JsFormatter(), new XmlFormatter()));

    if (cmd.hasOption("help")) {
      showHelp(opts, formatters, transpilers);
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
        throw new IllegalArgumentException("linesep : must be one of ['lf', 'cr', 'crlf']");
      }
      linesep = linesep.toLowerCase().replaceAll("cr", "\r").replaceAll("lf", "\n");
      javaFormatter.setLineSep(linesep);
    }

    if (cmd.hasOption("header")) {
      javaFormatter.setHeader(Paths.get(cmd.getOptionValue("header")).toUri().toURL());
    }

    if (args.length == 0) {
      System.out.println("Missing file or directory parameter.");
      showHelp(opts, formatters, transpilers);
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

    // apply transpilers (the order is ok // each transpiler has to be applied
    // on every files one by one as they may be chained)
    for (Transpiler transpiler : transpilers) {
      if (Files.isRegularFile(path)) {
        new FormatterVisitor().applyTranspilerOnFile(path, transpiler);
      } else if (Files.isDirectory(path)) {
        new FormatterVisitor().visitWithTranspiler(path, transpiler);
      } else {
        throw new IllegalArgumentException("unsupported path : " + path);
      }
    }

  }
}
