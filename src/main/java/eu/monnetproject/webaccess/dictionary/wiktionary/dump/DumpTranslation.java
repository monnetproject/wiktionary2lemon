package eu.monnetproject.webaccess.dictionary.wiktionary.dump;

import eu.monnetproject.lang.Language;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author John McCrae
 */
public class DumpTranslation {
    Language lang;
    String translation;
    String annotation;

    String mapKey = null;

    @Override
    public String toString() {
        if(mapKey != null)
            return "see [["+mapKey+"]]";
        else
            return "\"" + translation + "\"@" + lang + (annotation == null ? "" :  "[" + annotation + "]");
    }


}
