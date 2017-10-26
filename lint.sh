#!/usr/bin/env bash

yarn install
./node_modules/.bin/raml-cop raml-util/ramls/mod-users/*.raml
