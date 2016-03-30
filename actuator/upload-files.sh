#!/usr/bin/env bash

file_name="$1"
curl -i  -F file=@"$file_name" http://localhost:8080/histogram/uploads

