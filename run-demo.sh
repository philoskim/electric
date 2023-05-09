#!/bin/bash

rm -rf resources/public/js
clj -A:dev -X user/main

