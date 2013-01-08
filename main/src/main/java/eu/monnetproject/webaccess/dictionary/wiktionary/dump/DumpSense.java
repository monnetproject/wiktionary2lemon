package eu.monnetproject.webaccess.dictionary.wiktionary.dump;

import eu.monnetproject.webaccess.dictionary.wiktionary.util.Strings;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author John McCrae
 */
public class DumpSense {
    List<String> refs = new ArrayList<String>();
    String gloss;
    String example;
    List<String> contexts = new ArrayList<String>();

    @Override
    public String toString() {
        return "<"+gloss+"}" + Strings.join("\"", refs) + ">";
    }


}
