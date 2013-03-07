Wiktionary to Lemon Converter
=============================

Converter from Wiktionary to _lemon_. 

The converter can be executed as follows

    DUMP_DATE=20120101
    wget http://download.wikimedia.org/enwiktionary/20110724/enwiktionary-$DUMP_DATE-pages-articles.xml.bz2
    bunzip2 enwiktionary-$DUMP_DATE-pages-articles.xml.bz2
    mkdir -p dumps/
    mv enwiktionary-$DUMP_DATE-pages-articles.xml.bz2 dumps/
    LINES=`wc -l enwiktionary-$DUMP_DATE-pages-articles.xml`
    mvn exec:java -Dexec.mainClass=eu.monnetproject.webaccess.dictionary.wiktionary.WiktionaryGenerator \
       -Dexec.args="$DUMP_DATE $LINES"
