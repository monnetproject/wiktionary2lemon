#!/bin/bash

for x in "data/wordnet-antonym.rdf" "data/wordnet-participleof.rdf" "data/wordnet-attribute.rdf" "data/wordnet-partmeronym.rdf" "data/wordnet-causes.rdf" "data/wordnet-pertainsto.rdf" "data/wordnet-classifiedby.rdf" "data/wordnet-sameverbgroupas.rdf" "data/wordnet-derivationallyrelated.rdf" "data/wordnet-seealso.rdf" "data/wordnet-entailment.rdf" "data/wordnet-similarity.rdf" "data/wordnet-frame.rdf" "data/wordnet-substancemeronym.rdf" "data/wordnet-glossary.rdf" "data/wordnet-synset.rdf" "data/wordnet-hyponym.rdf" "data/wordnet-wordsensesandwords.rdf" "data/wordnet-membermeronym.rdf"
do
  echo "start: $x"
  GRAPH="http://monnet/wordnet"
  echo "data/ttlp_mt (file_to_string_output ('$x'), '', '$GRAPH'); checkpoint;"
  isql-vt 1111 dba dba exec="ttlp_mt (file_to_string_output ('$x'), '', '$GRAPH'); checkpoint;"
done


