package eu.monnetproject.webaccess.dictionary.wiktionary;

import eu.monnetproject.sim.StringSimilarityMeasure;
import eu.monnetproject.sim.string.Levenshtein;
import eu.monnetproject.webaccess.dictionary.wiktionary.dump.ReadDump;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

/**
 *
 * @author John McCrae
 */
public class WiktionaryGenerator {

    private static String DUMP_DATE = "20120101";
    //private final static Logger log = Logging.getLogger(WiktionaryGenerator.class);

    public static void main(String[] args) throws Exception {
        final StringSimilarityMeasure measure = new Levenshtein();
        if(args.length == 2) {
            DUMP_DATE = args[0];
            ReadDump.LINES = Integer.parseInt(args[1]);
        }
        final File f = new File("dumps/enwiktionary-" + DUMP_DATE + "-pages-articles.xml");
        if (f.exists()) {
            ReadDump.dumpTo(new BufferedReader(new FileReader(f)),  true, measure);
        } else {
            System.err.println("No dump: \nwget http://download.wikimedia.org/enwiktionary/20110724/enwiktionary-" + DUMP_DATE + "-pages-articles.xml.bz2");
            System.err.println("mkdir dumps");
            System.err.println("bunzip2 enwiktionary-" + DUMP_DATE + "-pages-articles.xml.bz2");
            System.err.println("mv enwiktionary-" + DUMP_DATE + "-pages-articles.xml dumps/");
        }
    }
}
