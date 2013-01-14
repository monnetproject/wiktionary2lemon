#!/bin/bash

for x in "data/wiktionary0.ttl" "data/wiktionary1.ttl" "data/wiktionary2.ttl" "data/wiktionary3.ttl" "data/wiktionary4.ttl" "data/wiktionary5.ttl" "data/wiktionary6.ttl" "data/wiktionary7.ttl" "data/wiktionary8.ttl" "data/wiktionary9.ttl"
do
  echo "start: $x"
  GRAPH="http://monnet/wiktionary"
  echo "ttlp_mt (file_to_string_output ('$x'), '', '$GRAPH'); checkpoint;"
  isql-vt 1111 dba dba exec="ttlp_mt (file_to_string_output ('$x'), '', '$GRAPH'); checkpoint;"
done

isql-vt 1111 dba dba exec="ttlp_mt (file_to_string_output ('data/wiktionary5.ttl'), '', 'http://monnet/wiktionary'); checkpoint;"

