#!/bin/bash
formatter_jar=~/.m2/repository/com/github/jrialland/javaformatter-cli/1.0-SNAPSHOT/javaformatter-cli-1.0-SNAPSHOT.jar
java -jar "$formatter_jar" "${@}"
