package eu.monnetproject.webaccess.dictionary.wiktionary.dump;

import eu.monnetproject.lang.Language;
import eu.monnetproject.webaccess.dictionary.wiktionary.util.Strings;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.net.*;
import java.util.HashMap;
import java.util.Map;
import java.io.PrintWriter;
import java.util.logging.Logger;

/**
 *
 * @author John McCrae
 */
public class DumpEntry {
    private final static Logger log = Logger.getLogger(DumpEntry.class.getName());
    public String key;
    public String type;
    public Language lang;
    public List<String> inflString = new ArrayList<String>();
    public List<String> phonetics = new ArrayList<String>();
    public List<List<DumpTranslation>> translations = new ArrayList<List<DumpTranslation>>();
    public List<String> transGlosses = new ArrayList<String>();
    public Map<Integer, Integer> transSenses = new HashMap<Integer, Integer>();
    public Map<DumpSense, DumpSense> synSenses = new HashMap<DumpSense, DumpSense>(),
            hypSenses = new HashMap<DumpSense, DumpSense>(),
            antSenses = new HashMap<DumpSense, DumpSense>();
    public List<DumpSense> definitions = null;
    public List<DumpSense> synonyms = new ArrayList<DumpSense>();
    public List<DumpSense> hyponyms = new ArrayList<DumpSense>();
    public List<DumpSense> antonyms = new ArrayList<DumpSense>();
    public int id = -1;

    @Override
    public String toString() {
        return "INSERT into wiktionary (Id,title,types,lang,inflStrings,phonetics,translations,transGlosses,synonyms,hyponyms,antonyms) VALUES ("
                + id + ","
                + sqlLiteral(key) + ","
                + sqlLiteral(type) + ","
                + sqlLiteral(lang.toString()) + ","
                + sqlLiteral(Strings.join("{", inflString)) + ","
                + sqlLiteral(Strings.join("{", phonetics)) + ","
                + sqlLiteral(Strings.join("{", translations)) + ","
                + sqlLiteral(Strings.join("{", transGlosses)) + ","
                + sqlLiteral(Strings.join("{", synonyms)) + ","
                + sqlLiteral(Strings.join("{", hyponyms)) + ","
                + sqlLiteral(Strings.join("{", antonyms)) + ");";

        /*return "Key:" + key + "\nTypes:"+types+"\nLangs:"+lang+"\nInflections:"+inflString+ "\nPhonetics:"+
        phonetics + "\nTranslations:" + translations + "\nTranslationGlosses:"+transGlosses+"\n" +
        (synonyms != null ? synonyms+"\n" : "") +
        (hyponyms != null ? hyponyms+"\n" : "") +
        (antonyms != null ? antonyms+"\n" : "");*/
    }

    private String enc(String str, String lang) {
        try {
            final String encShort = URLEncoder.encode(str, "UTF-8");
            if (encShort.matches("[A-Z_a-z\u00C0-\u00D6\u00D8-\u00F6\u00F8-\u02FF\u0370-\u037D\u037F-\u1FFF\u200C-\u200D\u2070-\u218F\u2C00-\u2FEF\u3001-\uD7FF\uF900-\uFDCF\uFDF0-\uFFFD][A-Z_a-z\\-0-9\u00b7\u0300-\u036f\u203f-\u2040\u00C0-\u00D6\u00D8-\u00F6\u00F8-\u02FF\u0370-\u037D\u037F-\u1FFF\u200C-\u200D\u2070-\u218F\u2C00-\u2FEF\u3001-\uD7FF\uF900-\uFDCF\uFDF0-\uFFFD]*")) {
                return lang + ":" + encShort;
            } else {
                return "<" + ReadDump.wiktionaryURL + "__" + lang + "/" + encShort + ">";
            }
        } catch (java.io.UnsupportedEncodingException x) {
            x.printStackTrace();
            return str;
        }
    }
    private final static int DEFINITION = 0;
    private final static int TRANSLATION = 1;
    private final static int SYNONYM = 2;
    private final static int ANTONYM = 3;
    private final static int HYPONYM = 4;
    private final static int DEFAULT = 5;

    // Only for definitions
    private String getSenseID(DumpSense s) {
        return getSenseID(s, definitions.indexOf(s), DEFINITION);
    }

    private String getSenseID(DumpSense s, int idx, int _type) {
        if (_type == DEFINITION) {
            return enc(key + "__" + (type == null ? "" : type + "__") + lang.toString() + "#sense_" + idx,lang.toString());
        } else if (_type == TRANSLATION) {
            if (transSenses.containsKey(idx)) {
                return getSenseID(definitions.get(transSenses.get(idx)), transSenses.get(idx), DEFINITION);
            } else {
                return enc(key + "__" + (type == null ? "" : type + "__") + lang.toString() + "#sense_t" + idx,lang.toString());
            }
        } else if (_type == SYNONYM) {
            if (synSenses.containsKey(s)) {
                return getSenseID(synSenses.get(s));
            } else {
                return enc(key + "__" + (type == null ? "" : type + "__") + lang.toString() + "#sense_s" + idx,lang.toString());
            }
        } else if (_type == ANTONYM) {
            if (antSenses.containsKey(s)) {
                return getSenseID(antSenses.get(s));
            } else {
                return enc(key + "__" + (type == null ? "" : type + "__") + lang.toString() + "#sense_a" + idx,lang.toString());
            }
        } else if (_type == HYPONYM) {
            if (hypSenses.containsKey(s)) {
                return getSenseID(hypSenses.get(s));
            } else {
                return enc(key + "__" + (type == null ? "" : type + "__") + lang.toString() + "#sense_h" + idx,lang.toString());
            }
        } else if (_type == DEFAULT) {
            if(s == null) {
                return enc(key + "__" + lang.toString() + "#sense_def",lang.toString());
            } else {
                return enc(s.refs.get(0)+"__"+ lang.toString()+"#sense_def",lang.toString());
            }
            
        } else {
            throw new IllegalArgumentException();
        }
    }
    
    private String getDefSenseID(DumpTranslation dt) {
        return getDefSenseID(dt.translation, dt.lang);
    }
    
    private String getDefSenseID(String ref, Language lang) {
        return enc(ref + "__" + lang + "#sense_def",lang.toString());
    }

    public String toRDF(PrintWriter lexicon, PrintWriter mapping) {
        return toRDF(type,lexicon,mapping);
    }

    private String toRDF(String type, PrintWriter lexicon, PrintWriter mapping) {
        if (type != null && (type.matches("decl-.*") || type.matches(".*\\}\\}.*")
                || type.matches(".*of") || type.matches(".*form.*")
                || type.matches("conj-.*") || type.matches("kanji.*")
                || type.matches("i") || type.matches("readings")
                || type.matches(".*participle.*") || type.matches("see-also"))) {
            return "";
        }
        //String name = enc(key+"__"+type+"__"+lang.toString());;
        StringBuffer rdf = new StringBuffer();

        lexicon.append("<http://monnetproject.deri.ie/lemonsource/wiktionary__").append(lang.toString()).append("> lemon:entry ").append(enc(key + "__" + (type == null ? "" : type + "__") + lang.toString(),lang.toString())).append(" .\n\n");

        rdf.append(enc(key + "__" + (type == null ? "" : type + "__") + lang.toString(),lang.toString())).append(" rdf:type lemon:LexicalEntry ;\n lemon:canonicalForm ").append(enc(key+"__"+type+"__"+lang.toString()+"#canForm",lang.toString()));
        if (type != null) {
            mapping.println(enc(key + "__" + lang.toString(),lang.toString()) + " rdfs:seeAlso " + enc(key + "__" + (type == null ? "" : type + "__") + lang.toString(),lang.toString()) + " . ");
            appendType(rdf, type);
        }
        int i = 0;
        if (definitions != null) {
            for (DumpSense ds : definitions) {
                rdf.append(";\n lemon:sense ").append(getSenseID(ds, i, DEFINITION));
                i++;
            }
        }
        i = 0;
        for (List<DumpTranslation> translationSet : translations) {
            rdf.append(";\n lemon:sense ").append(getSenseID(null, i, TRANSLATION));
            i++;
        }
        i = 0;
        for (DumpSense ds : synonyms) {
            rdf.append(";\n lemon:sense ").append(getSenseID(ds, i, SYNONYM));
            i++;
        }
        i = 0;
        for (DumpSense ds : hyponyms) {
            rdf.append(";\n lemon:sense ").append(getSenseID(ds, i, HYPONYM));
            i++;
        }
        i = 0;
        for (DumpSense ds : antonyms) {
            rdf.append(";\n lemon:sense ").append(getSenseID(ds, i, ANTONYM));
            i++;
        }
        String defSense = getSenseID(null, -1, DEFAULT);
        rdf.append(";\n lemon:sense ").append(defSense);
        rdf.append(" .\n\n");

        // form section
        rdf.append(enc(key+"__"+(type == null ? "" : type + "__")+lang.toString()+"#canForm",lang.toString())).append(" lemon:writtenRep \"").append(escapeText(key)).append("\"@").append(lang.toString());
        for (String phonetic : phonetics) {
            rdf.append(" ;\n lexinfo:pronunciation \"").append(escapeText(phonetic)).append("\"@").append(lang).append("-fonipa");
        }
        rdf.append(" .\n\n ");
        
        i = 0;
        if (definitions != null) {
            for (DumpSense ds : definitions) {
                if (ds.gloss != null) {
                    rdf.append(getSenseID(ds, i, DEFINITION)).append(" lemon:definition [ lemon:value \"").
                            append(escapeText(ds.gloss)).append("\"@en ] .\n");
                }
                try {
                    rdf.append(getSenseID(ds,i,DEFINITION)).
                            append(" lemon:reference <http://en.wiktionary.org/wiki/").append(URLEncoder.encode(key,"UTF-8")).append("> .\n");
                } catch (UnsupportedEncodingException ex) {
                }
                for(String context : ds.contexts) {
                    rdf.append(getSenseID(ds,i,DEFINITION)).append(" lemon:definition [ lemon:value \"").append(escapeText(context)).append("\"@en ] .\n");
                }
                i++;
            }
        }

        i = 0;
        for (List<DumpTranslation> translationSet : translations) {
            for (DumpTranslation dt : translationSet) {
                if (dt.mapKey == null) {
                    rdf.append(getSenseID(null, i, TRANSLATION)).append(" lexinfo:translation ").append(getDefSenseID(dt)).append(" .\n");
                }
            }
            rdf.append(getSenseID(null, i, TRANSLATION)).append(" lemon:definition [ lemon:value \"").append(escapeText(transGlosses.get(i))).append("\"@en ] .\n");
            i++;
        }
        rdf.append("\n");
        i = 0;
        for (DumpSense ds : synonyms) {
            for (String ref : ds.refs) {
                rdf.append(getSenseID(ds, i, SYNONYM)).append(" lexinfo:synonym ").append(getDefSenseID(ref, lang)).append(" .\n");
            }
            if (ds.gloss != null) {
                rdf.append(getSenseID(ds, i, SYNONYM)).append(" lemon:definition [ lemon:value \"").append(escapeText(ds.gloss)).append("\"@en  ] .\n");
            }
            i++;
        }
        i = 0;
        for (DumpSense ds : hyponyms) {
            for (String ref : ds.refs) {
                rdf.append(getSenseID(ds, i, HYPONYM)).append(" lexinfo:hyponym ").append(getDefSenseID(ref, lang)).append(" .\n");
            }
            if (ds.gloss != null) {
                rdf.append(getSenseID(ds, i, HYPONYM)).append(" lemon:definition [ lemon:value \"").append(escapeText(ds.gloss)).append("\"@en  ] .\n");
            }
            i++;
        }
        i = 0;
        for (DumpSense ds : antonyms) {
            for (String ref : ds.refs) {
                rdf.append(getSenseID(ds, i, ANTONYM)).append(" lexinfo:antonym ").append(getDefSenseID(ref, lang)).append(" .\n");
            }
            if (ds.gloss != null) {
                rdf.append(getSenseID(ds, i, ANTONYM)).append(" lemon:definition [ lemon:value \"").append(escapeText(ds.gloss)).append("\"@en  ] .\n");
            }
            i++;
        }

        return rdf.toString();
    }

    public static boolean acceptType(String type) {
        StringBuffer buf = new StringBuffer();
        appendType(buf, type);
        return buf.length() > 0;
    }

    public static void appendType(StringBuffer rdf, String type) {
        if (type.equals("noun")) {
            rdf.append(";\n lexinfo:partOfSpeech lexinfo:noun");
        } else if (type.equals("noun-inv")) {
            rdf.append(";\n lexinfo:partOfSpeech lexinfo:noun");
        } else if (type.equals("noun-unc")) {
            rdf.append(";\n lexinfo:partOfSpeech lexinfo:noun");
        } else if (type.equals("noun-m")) {
            rdf.append(";\n lexinfo:partOfSpeech lexinfo:noun ;\n lexinfo:gender lexinfo:masculine");
        } else if (type.equals("noun-f")) {
            rdf.append(";\n lexinfo:partOfSpeech lexinfo:noun ;\n lexinfo:gender lexinfo:feminine");
        } else if (type.equals("noun-n")) {
            rdf.append(";\n lexinfo:partOfSpeech lexinfo:noun ;\n lexinfo:gender lexinfo:neuter");
        } else if (type.equals("noun-mf")) {
            rdf.append(";\n lexinfo:partOfSpeech lexinfo:noun  ;\n lexinfo:gender lexinfo:masculine, lexinfo:feminine");
        } else if (type.equals("adj")) {
            rdf.append(";\n lexinfo:partOfSpeech lexinfo:adjective");
        } else if (type.equals("adj-al")) {
            rdf.append(";\n lexinfo:partOfSpeech lexinfo:adjective");
        } else if (type.equals("adj-mf")) {
            rdf.append(";\n lexinfo:partOfSpeech lexinfo:adjective");
        } else if (type.equals("adj-more")) {
            rdf.append(";\n lexinfo:partOfSpeech lexinfo:adjective");
        } else if (type.equals("adj-inflected")) {
            rdf.append(";\n lexinfo:partOfSpeech lexinfo:adjective");
        } else if (type.equals("adjective")) {
            rdf.append(";\n lexinfo:partOfSpeech lexinfo:adjective");
        } else if (type.equals("adv")) {
            rdf.append(";\n lexinfo:partOfSpeech lexinfo:adverb");
        } else if (type.equals("adverb")) {
            rdf.append(";\n lexinfo:partOfSpeech lexinfo:adverb");
        } else if (type.equals("proper noun")) {
            rdf.append(";\n lexinfo:partOfSpeech lexinfo:properNoun");
        } else if (type.equals("proper-noun")) {
            rdf.append(";\n lexinfo:partOfSpeech lexinfo:properNoun");
        } else if (type.equals("verb")) {
            rdf.append(";\n lexinfo:partOfSpeech lexinfo:verb");
        } else if (type.equals("verb-strong")) {
            rdf.append(";\n lexinfo:partOfSpeech lexinfo:verb");
        } else if (type.equals("verb-weak")) {
            rdf.append(";\n lexinfo:partOfSpeech lexinfo:verb");
        } else if (type.equals("verb-irregular")) {
            rdf.append(";\n lexinfo:partOfSpeech lexinfo:verb");
        } else if (type.equals("intj")) {
            rdf.append(";\n lexinfo:partOfSpeech lexinfo:interjection");
        } else if (type.equals("verb-form")) {
            rdf.append(";\n lexinfo:partOfSpeech lexinfo:verb");
        } else if (type.equals("intj")) {
            rdf.append(";\n lexinfo:partOfSpeech lexinfo:interjection");
        } else if (type.equals("interjection")) {
            rdf.append(";\n lexinfo:partOfSpeech lexinfo:interjection");
        } else if (type.equals("interj")) {
            rdf.append(";\n lexinfo:partOfSpeech lexinfo:interjection");
        } else if (type.equals("letter")) {
            rdf.append(";\n lexinfo:partOfSpeech lexinfo:letter");
        } else if (type.equals("number")) {
            rdf.append(";\n lexinfo:partOfSpeech lexinfo:numeral");
        } else if (type.equals("numeral")) {
            rdf.append(";\n lexinfo:partOfSpeech lexinfo:numeral");
        } else if (type.equals("symbol")) {
            rdf.append(";\n lexinfo:termType lexinfo:symbol");
        } else if (type.equals("prefix")) {
            rdf.append(";\n lexinfo:termElement lexinfo:prefix");
        } else if (type.equals("suffix")) {
            rdf.append(";\n lexinfo:termElement lexinfo:suffix");
        } else if (type.equals("abbreviation")) {
            rdf.append(";\n lexinfo:termType lexinfo:abbreviation");
        } else if (type.equals("preposition")) {
            rdf.append(";\n lexinfo:termType lexinfo:preposition");
        } else if (type.equals("prep")) {
            rdf.append(";\n lexinfo:termType lexinfo:preposition");
        } else if (type.equals("conj")) {
            rdf.append(";\n lexinfo:partOfSpeech lexinfo:conjunction");
        } else if (type.equals("conj-er")) {
            rdf.append(";\n lexinfo:partOfSpeech lexinfo:conjunction");
        } else if (type.equals("conjunction")) {
            rdf.append(";\n lexinfo:partOfSpeech lexinfo:conjunction");
        } else if (type.equals("pos")) {
            rdf.append(";\n lexinfo:partOfSpeech lexinfo:postposition");
        } else if (type.equals("cardinal number")) {
            rdf.append(";\n lexinfo:partOfSpeech lexinfo:cardinalNumeral");
        } else if (type.equals("def")) {
            rdf.append(";\n lexinfo:partOfSpeech lexinfo:noun");
        } else if (type.equals("indefinite pronoun")) {
            rdf.append(";\n lexinfo:partOfSpeech lexinfo:indefinitePronoun");
        } else if (type.equals("initialism")) {
            rdf.append(";\n lexinfo:termType lexinfo:initialism");
        } else if (type.equals("phrase")) {
            rdf.append(";\n rdf:type lemon:Phrase");
        } else if (type.equals("pron")) {
            rdf.append(";\n lexinfo:partOfSpeech lexinfo:pronoun");
        } else if (type.equals("pronoun")) {
            rdf.append(";\n lexinfo:partOfSpeech lexinfo:pronoun");
        } else if (type.equals("determiner")) {
            rdf.append(";\n lexinfo:partOfSpeech lexinfo:determiner");
        } else if (type.equals("article")) {
            rdf.append(";\n lexinfo:partOfSpeech lexinfo:article");
        } else if (type.equals("personal pronoun")) {
            rdf.append(";\n lexinfo:partOfSpeech lexinfo:personalPronoun");
        } else if (type.equals("det")) {
            rdf.append(";\n lexinfo:partOfSpeech lexinfo:determiner");
        } else if (type.equals("demonstrative determiner")) {
            rdf.append(";\n lexinfo:partOfSpeech lexinfo:demonstrativeDeterminer");
        } else if (type.equals("relative pronoun")) {
            rdf.append(";\n lexinfo:partOfSpeech lexinfo:relativePronoun");
        } else if (type.equals("postposition")) {
            rdf.append(";\n lexinfo:partOfSpeech lexinfo:postposition");
        } else if (type.equals("noun2")) {
            rdf.append(";\n lexinfo:partOfSpeech lexinfo:noun");
        } else if (type.equals("particle")) {
            rdf.append(";\n lexinfo:partOfSpeech lexinfo:particle");
        } else if (type.equals("cont")) {
            rdf.append(";\n lexinfo:termType lexinfo:contraction");
        } else if (type.equals("contraction")) {
            rdf.append(";\n lexinfo:termType lexinfo:contraction");
        } else if (type.equals("acronym")) {
            rdf.append(";\n lexinfo:termType lexinfo:acronym");
        } else if (type.equals("pnoun")) {
            rdf.append(";\n lexinfo:partOfSpeech lexinfo:properNoun");
        } else if (type.equals("possessive determiner")) {
            rdf.append(";\n lexinfo:partOfSpeech lexinfo:possessiveDeterminer");
        } else {
            log.warning("Unknown type:" + type);
        }
    }

    public static String escapeText(String str) {
        // Definitely a hack!
        return str.replaceAll("\\\\(?![\\\\\\\"])", "\\\\\\\\").replaceAll("\"", "\\\\\"").replaceAll("(?<!\\\\)\\\\\\\\\\\\(?!\\\\)","\\\\\\\\");
    }

    public static String sqlLiteral(String str) {
        String rval = str.replaceAll("(['_%/])", "\\\\$1");
        if (str.equals(rval)) {
            return "'" + rval + "'";
        } else {
            return "'" + rval + "'";
        }
    }
}
