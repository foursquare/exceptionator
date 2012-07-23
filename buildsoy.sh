#!/bin/sh
java -jar SoyToJsSrcCompiler.jar --outputPathFormat src/main/resources/content/{INPUT_FILE_NAME}.js src/main/soy/*.soy

