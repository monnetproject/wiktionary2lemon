package eu.monnetproject.webaccess.dictionary.wiktionary.dump;

import eu.monnetproject.lang.Language;
import eu.monnetproject.lang.LanguageCodeFormatException;
import eu.monnetproject.sim.StringSimilarityMeasure;
import eu.monnetproject.util.Logging;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import eu.monnetproject.util.Logger;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author John McCrae
 */
public class ReadDump {

    private static final Logger log = Logging.getLogger(ReadDump.class);
    private static String langCode = "en|es|de|fr|nl|ja";
    private static final HashSet<Language> supportedLangs = new HashSet<Language>();

    static {
        supportedLangs.add(Language.ENGLISH);
        supportedLangs.add(Language.SPANISH);
        supportedLangs.add(Language.GERMAN);
        supportedLangs.add(Language.FRENCH);
        supportedLangs.add(Language.DUTCH);
        supportedLangs.add(Language.JAPANESE);
    }
    private static boolean inPronounciation = false;
    // static Map<String, List<DumpEntry>> entries = new HashMap<String, List<DumpEntry>>();
    static DumpEntry current = null;
    static DumpEntry pageCurrent = null;
    static String currentKey = null;
    static boolean dead = false;

    public static void main(String[] args) throws Exception {
        BufferedReader in;
        boolean rdfMode = false;
        if (args.length == 0) {
            in = new BufferedReader(new InputStreamReader(System.in));
        } else if (args.length == 1) {
            in = new BufferedReader(new FileReader(args[0]));
        } else if (args.length == 2) {
            in = new BufferedReader(new FileReader(args[0]));
            rdfMode = true;
        } else {
            throw new IllegalArgumentException();
        }
        //dumpTo(in, new PrintStream[]{System.out}, rdfMode, null);

    }
    public static int LINES = 82362274;
    private static int lineCount = 0;
    private static int outIdx = 0;
    private static int nextCount;
    public static final String wiktionaryURL = "http://monnetproject.deri.ie/lemonsource/wiktionary";
    
    public void dumpToNonStatic(BufferedReader in,  boolean rdfMode, StringSimilarityMeasure measure) throws Exception {
        dumpTo(in, rdfMode, measure);
    }

    private static PrintWriter getOrElse(Language lang, Map<Language,PrintWriter> out, String name) throws FileNotFoundException {
        if(out.containsKey(lang)) {
            return out.get(lang);
        } else {
            final PrintWriter newOut = new PrintWriter(name + "__" + lang+".ttl");
            out.put(lang, newOut);
            printHeader(true, newOut);
            return newOut;
        }
    }
    
    public static void dumpTo(BufferedReader in, boolean rdfMode, StringSimilarityMeasure measure) throws Exception {
        final HashMap<Language,PrintWriter> lexiconOuts = new HashMap<Language, PrintWriter>();
        final HashMap<Language,PrintWriter> mappingOuts = new HashMap<Language, PrintWriter>();
        final HashMap<Language,PrintWriter> allOuts = new HashMap<Language, PrintWriter>();
        //printHeader(rdfMode, out);
        SenseMerger merger = null;
        if (measure != null) {
            merger = new SenseMerger(measure);
        }
        int idCount = 0;
        //nextCount = LINES / outs.length + outs.length;
        String s;
        while ((s = in.readLine()) != null) {
            lineCount++;
            if (s.matches(".*</page>.*")) {
                if (current != null && current.lang != null) {
                    if (current.id < 0) {
                        current.id = idCount++;
                    }
                    if (merger != null) {
                        merger.calcMerge(current);
                    }
                    
                    printEntry(rdfMode, getOrElse(current.lang, lexiconOuts, "wiktionary"),
                            getOrElse(current.lang, mappingOuts, "mapping"),allOuts);
                }
                pageCurrent = null;
                current = null;
                dead = false;
            }
            if (dead) {
                continue;
            }

            Matcher m = Pattern.compile(".*<title>(.*)</title>.*").matcher(s);
            if (m.matches()) {
                if (m.group(1).matches(".*:.*")) {
                    dead = true;
                    continue;
                }
                currentKey = m.group(1);
                //entries.put(key, new ArrayList<DumpEntry>());
            }

            if (s.matches("==[^=]+==.*") || s.matches(".*[^=]==[^=]+==.*")) {
                if (current != null && current.lang != null) {
                    if (current.id < 0) {
                        current.id = idCount++;
                    }
                    if (merger != null) {
                        merger.calcMerge(current);
                    }
                    printEntry(rdfMode, getOrElse(current.lang, lexiconOuts, "wiktionary"),
                            getOrElse(current.lang, mappingOuts, "mapping"),allOuts);
                }

                current = new DumpEntry();
                current.key = currentKey;
                if (pageCurrent == null) {
                    pageCurrent = current;
                }
            }

            if (current == null) {
                continue;
            }
            m = Pattern.compile(".*\\{\\{(" + langCode + ")-([^\\}\\|]+)(\\|[^\\}]*)?\\}\\}.*").matcher(s);
            if (m.matches()) {
                Language lang = Language.get(m.group(1));
                if (current.lang != null && !current.lang.equals(lang)) {
                    log.warning("Multiple inconsistent lang tags for " + current.key);
                } else {
                    current.lang = lang;
                }
                if (DumpEntry.acceptType(m.group(2))) {
                    if (current != null && current.type != null) {
                        if (current.id < 0) {
                            current.id = idCount++;
                        }
                        if (merger != null) {
                            merger.calcMerge(current);
                        }
                        printEntry(rdfMode, getOrElse(current.lang, lexiconOuts, "wiktionary"),
                            getOrElse(current.lang, mappingOuts, "mapping"),allOuts);
                        current = new DumpEntry();
                        current.key = currentKey;
                        current.phonetics = pageCurrent.phonetics;
                        current.lang = lang;
                    }
                }
                current.type = m.group(2);
                if (m.group(3) == null || m.group(3).length() == 0) {
                    current.inflString.add("");
                } else {
                    current.inflString.add(m.group(3).substring(1));
                }
            }

            m = Pattern.compile(".*\\{\\{infl\\|(" + langCode + ")\\|([^\\|]+)\\|?([^\\}]*)\\}\\}.*").matcher(s);
            if (m.matches()) {
                Language lang = Language.get(m.group(1));
                if (current.lang != null && !current.lang.equals(lang)) {
                    log.warning("Multiple inconsistent lang tags for " + current.key);
                } else {
                    current.lang = lang;
                }
//                current.types.add(m.group(2));
                if (m.group(3) == null || m.group(3).length() == 0) {
                    current.inflString.add("");
                } else {
                    current.inflString.add(m.group(3).substring(1));
                }
            }

            if (s.contains("===Pronunciation===")) {
                inPronounciation = true;
            } else if (s.matches("===.*")) {
                inPronounciation = false;
            }

            if (inPronounciation) {
                m = Pattern.compile(".*\\{\\{IPA\\|/([^\\|\\}]+)/(\\||\\}).*").matcher(s);
                if (m.matches()) {
                    current.phonetics.add(m.group(1));
                }
            }

            m = Pattern.compile(".*\\{\\{trans-top\\|?([^\\}]*)\\}\\}.*").matcher(s);
            if (m.matches()) {
                current.transGlosses.add(m.group(1));
                readTranslations(in);
            }

            m = Pattern.compile(".*\\{\\{trans-see\\|([^\\|]+)\\|([^\\}]+)\\}\\}.*").matcher(s);
            if (m.matches()) {
                current.transGlosses.add(m.group(1));
                DumpTranslation trans = new DumpTranslation();
                trans.mapKey = m.group(2);
                current.translations.add(Collections.singletonList(trans));
            }

            if (s.matches("====Synonyms====")) {
                current.synonyms.addAll(readSet(in));
            }
            if (s.matches("====Hyponyms====")) {
                current.hyponyms.addAll(readSet(in));
            }
            if (s.matches("====Antonyms====")) {
                current.antonyms.addAll(readSet(in));
            }
            if (s.matches("#.*") && current != null && current.definitions == null) {
                current.definitions = readDefinitions(s, in);
            }
        }
        for(PrintWriter out : lexiconOuts.values()) {
            out.close();
        }
        for(PrintWriter out : mappingOuts.values()) {
            out.close();
        }
        for(PrintWriter out : allOuts.values()) {
            out.close();
        }
        log.info("Done");
    }

    private static void printEntry(boolean rdfMode, PrintWriter lexicon, PrintWriter mapping, Map<Language,PrintWriter> allOuts) throws FileNotFoundException {
        if (rdfMode) {
            
            new File("wiktionary__" + current.lang.toString()).mkdir();
            if(!allOuts.containsKey(current.lang)) {
                final PrintWriter newOut = new PrintWriter("wiktionary__all__" + current.lang.toString()+".ttl");
                allOuts.put(current.lang, newOut);
                printHeader(rdfMode,newOut);
            }
            final PrintWriter out2 = new PrintWriter("wiktionary__" + current.lang.toString() + "/" + current.key.replaceAll(File.separator,"SLASH") + ".ttl");
            final PrintWriter out3 = allOuts.get(current.lang);
            printHeader(rdfMode,out2);
            out2.println(current.toRDF(lexicon, mapping));
            out3.println(current.toRDF(lexicon, mapping));
            out2.flush();
            out2.close();
//            if (lineCount > nextCount) {
//                outIdx++;
//                nextCount += LINES / outs.length;
//                out = outs[outIdx];
//                printHeader(rdfMode, out);
//            }
        } else {
            //out.println(current.toString());
        }
    }

    private static void printHeader(boolean rdfMode, PrintStream out) {
        if (rdfMode) {
            out.println("@prefix en: <" + wiktionaryURL + "__en/> .");
            out.println("@prefix es: <" + wiktionaryURL + "__es/> .");
            out.println("@prefix de: <" + wiktionaryURL + "__de/> .");
            out.println("@prefix ja: <" + wiktionaryURL + "__ja/> .");
            out.println("@prefix nl: <" + wiktionaryURL + "__nl/> .");
            out.println("@prefix fr: <" + wiktionaryURL + "__fr/> .");
            out.println("@prefix lemon: <http://www.monnet-project.eu/lemon#> .");
            out.println("@prefix lexinfo: <http://www.lexinfo.net/ontology/2.0/lexinfo#> .");
            out.println("@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .");
            out.println("");
            for (Language lang : supportedLangs) {
                out.println("<"+wiktionaryURL+"__" + lang.toString() + "> a lemon:Lexicon ;\n lemon:language \"" + lang.toString() + "\" .\n");
            }
        }
    }
    
    private static void printHeader(boolean rdfMode, PrintWriter out) {
        if (rdfMode) {
            out.println("@prefix en: <" + wiktionaryURL + "__en/> .");
            out.println("@prefix es: <" + wiktionaryURL + "__es/> .");
            out.println("@prefix de: <" + wiktionaryURL + "__de/> .");
            out.println("@prefix ja: <" + wiktionaryURL + "__ja/> .");
            out.println("@prefix nl: <" + wiktionaryURL + "__nl/> .");
            out.println("@prefix fr: <" + wiktionaryURL + "__fr/> .");
            out.println("@prefix lemon: <http://www.monnet-project.eu/lemon#> .");
            out.println("@prefix lexinfo: <http://www.lexinfo.net/ontology/2.0/lexinfo#> .");
            out.println("@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .");
            out.println("");
        }
    }
    private static Map<String, Language> langsByName = new HashMap<String, Language>();

    static {
        Class c = Language.class;
        for (Field f : c.getDeclaredFields()) {
            if (f.getType().equals(Language.class)) {
                try {
                    Language l = (Language) f.get(null);
                    langsByName.put(l.getName().toLowerCase(), l);
                } catch (Exception x) {
                }
            }
        }
    }

    protected static void setCurrent(String title) {
        current = new DumpEntry();
        current.key = title;
    }

    protected static void readTranslations(BufferedReader in) throws Exception {
        String s;
        List<DumpTranslation> transes = new ArrayList<DumpTranslation>();
        current.translations.add(transes);
        while ((s = in.readLine()) != null) {
            lineCount++;
            if (s.matches(".*\\{\\{trans-bottom.*")) {
                return;
            }
            if (s.matches("</page>")) {
                log.warning("Unexpected </page>");
                current = null;
                return;
            }
            boolean firstMatches = false;
            Matcher m = Pattern.compile(".*\\{\\{t.?\\|(...?)\\|([^\\|]+)\\|?([^\\}]*)\\}\\}.*").matcher(s);
            while (m.find()) {
                firstMatches = true;
                DumpTranslation trans = new DumpTranslation();
                try {
                    trans.lang = Language.get(m.group(1));
                    if (!supportedLangs.contains(trans.lang)) {
                        continue;
                    }
                    if (trans.lang == null) {
                        trans.lang = Language.getInstance("Unknown", "Unknown", null, m.group(1), m.group(1));
                    }
                    trans.translation = m.group(2);
                    if (m.group(3) != null && m.group(3).length() > 0) {
                        trans.annotation = m.group(3);
                    }
                    transes.add(trans);
                } catch (LanguageCodeFormatException x) {
                    log.config(x.getMessage());
                }
            }
            if (!firstMatches) {
                m = Pattern.compile(".*\\*\\s*(\\[\\[)?([^\\]]+)(\\]\\])?\\s*:(.*)").matcher(s);
                if (m.matches()) {
                    String langStr;
                    if (m.group(2).matches("\\{\\{ttbc\\|.*\\}\\}")) {
                        langStr = m.group(2).substring(7, m.group(2).length() - 2);
                    } else {
                        langStr = m.group(2);
                    }
                    Language lang = langsByName.get(langStr.toLowerCase());
                    if (lang == null) {
                        continue;
                    }

                    if (supportedLangs.contains(lang)) {

                        m = Pattern.compile("\\[\\[([^\\]\\|]+)\\]\\]").matcher(m.group(4));
                        while (m.find()) {
                            DumpTranslation trans = new DumpTranslation();
                            trans.lang = lang;
                            trans.translation = m.group(1);
                            transes.add(trans);
                        }
                    }
                }
            }
        }
    }

    protected static List<DumpSense> readSet(BufferedReader in) throws Exception {
        String s;
        List<DumpSense> rval = new ArrayList<DumpSense>();
        while ((s = in.readLine()) != null) {
            lineCount++;
            if (!s.matches("\\s*\\*.*")) {
                return rval;
            }
            Matcher m = Pattern.compile("[^\\]]*\\{\\{.+\\|([^\\}]+)\\}\\}.*").matcher(s);
            if (m.matches()) {
                DumpSense dumpSense = new DumpSense();
                dumpSense.gloss = m.group(1);
                m = Pattern.compile("\\[\\[([^\\]\\|]+)\\]\\]").matcher(s);
                while (m.find()) {
                    dumpSense.refs.add(m.group(1));
                }
                rval.add(dumpSense);
            }
        }
        return rval;
    }

    protected static List<DumpSense> readDefinitions(String first, BufferedReader in) throws Exception {
        String s;
        List<DumpSense> rval = new ArrayList<DumpSense>();
        DumpSense current = null;
        doReadDefinition(first, current, rval);
        while ((s = in.readLine()) != null) {
            lineCount++;
            if (!s.matches("\\s*#.*")) {
                return rval;
            }
            doReadDefinition(s, current, rval);
        }
        return rval;
    }
    private final static String[] notAContext = {
        "by extension",
        "chiefly",
        "_",
        "almost always",
        "in the plural",
        "transitive",
        "intransitive",
        "emerging usage in",
        "with .*",
        "dö",
        "qualifier",
        "male or female",
        "used .*",
        "context",
        "of .*",
        ".*=.*",
        ".*verb.*",
        "except .*",
        "sometimes .*",
        ".*conjugation.*",
        ".*inflection.*",
        "\\d",
        "\\w\\w?\\w?",
        "",
        ".* form .*",
        "third-person singular present",
        ".*\\[\\[.*",
        ".*plural.*"
    };

    private static void doReadDefinition(String s, DumpSense current, List<DumpSense> rval) {
        Matcher m = Pattern.compile("[^\\]]*# (\\{\\{[^\\}]+\\}\\})?(.*)").matcher(s);
        Matcher m2 = Pattern.compile("[^\\]]*#: (.*)").matcher(s);
        if (m.matches()) {
            current = new DumpSense();
            String gloss = m.group(2).replaceAll("[\\[\\]\\{\\}]", "");
            if (m.group(1) != null) {
                CTXT_LOOP:
                for (String context : m.group(1).substring(2, m.group(1).length() - 2).split("\\|")) {
                    for (String notContext : notAContext) {
                        if (context.matches(notContext)) {
                            continue CTXT_LOOP;
                        }
                    }
                    current.contexts.add(context);
                }
            }
            current.gloss = gloss;
            rval.add(current);
        } else if (m2.matches() && current != null) {
            String example = m.group(1).replaceAll("[\\[\\]\\{\\}]", "");
            current.example = example;
        }
    }
}
