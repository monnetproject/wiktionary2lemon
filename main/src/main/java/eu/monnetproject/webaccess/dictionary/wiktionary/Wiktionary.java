package eu.monnetproject.webaccess.dictionary.wiktionary;

import eu.monnetproject.lang.Language;
import eu.monnetproject.mrd.LexicalRelation;
import eu.monnetproject.mrd.MRDEntry;
import eu.monnetproject.mrd.MachineReadableDictionary;
import eu.monnetproject.pos.*;
import eu.monnetproject.util.Logging;
import eu.monnetproject.webaccess.dictionary.wiktionary.dump.ReadDump;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;
import java.util.Collection;
import eu.monnetproject.util.Logger;
import org.openrdf.model.ValueFactory;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;

/**
 *
 * @author John McCrae
 */
public class Wiktionary implements MachineReadableDictionary {

    private final Logger log = Logging.getLogger(this);
    
    /** OSGi only */
    public Wiktionary() { }
    
    /**
     * Create a wiktionary instance
     * @param Repository repository The repository containing wiktionary
     */
    
    private Repository repository;

    public void bindRepository(Repository repository) {
        this.repository = repository;
    }

    public void activate() {
        RepositoryConnection conn = null;
        try {
            conn = repository.getConnection();
            ValueFactory factory = conn.getValueFactory();
            if(!conn.hasStatement(factory.createURI(ReadDump.wiktionaryURL+"wiktionary__lexicon__en"),
                    factory.createURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),
                    factory.createURI("http://www.monnet-project.eu/lemon#Lexicon"),false)) {
                    log.severe("Wiktionary dump not on disk or in repository");
                    throw new RuntimeException();
            }
        } catch (RepositoryException ex) {
            throw new RuntimeException(ex);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (RepositoryException ex) {
                    log.severe(ex.getMessage());
                }
            }
        }
    }
    
    
    public Collection<MRDEntry> getEntries(String string, Language lng) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Collection<MRDEntry> getEntries(String string, Language lng, POS pos) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Collection<LexicalRelation> getSupportedRelations() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
//    private static PreparedStatement getEntryStat;
//
//    private ResultSet getIds(String wordForm, Language lang) {
//        try {
//            if (getEntryStat == null) {
//                getEntryStat = connection.prepareStatement("SELECT Id,types FROM wiktionary WHERE title=? AND lang=?");
//            }
//            getEntryStat.setString(1, wordForm);
//            if (lang.getIso639_1() != null) {
//                getEntryStat.setString(2, lang.getIso639_1());
//            } else if (lang.getIso639_2() != null) {
//                getEntryStat.setString(2, lang.getIso639_2());
//            } else if (lang.getIso639_3() != null) {
//                getEntryStat.setString(2, lang.getIso639_3());
//            }
//            return getEntryStat.executeQuery();
//        } catch (SQLException x) {
//            x.printStackTrace();
//            return null;
//        }
//    }
//
//    public Collection<MRDEntry> getEntries(String wordForm, Language lang) {
//        try {
//            ResultSet rs = getIds(wordForm, lang);
//            List<MRDEntry> rval = new ArrayList<MRDEntry>();
//            while (rs.next()) {
//                String[] pos = rs.getString("types").split("\\{");
//                for (int i = 0; i < pos.length; i++) {
//                    rval.add(new WiktionaryEntry(rs.getInt("Id"), i, wordForm, lang));
//                }
//            }
//            return rval;
//        } catch (SQLException x) {
//            x.printStackTrace();
//            return null;
//        }
//    }
//
//    public Collection<MRDEntry> getEntries(String wordForm, Language lang, POS partOfSpeech) {
//        try {
//            ResultSet rs = getIds(wordForm, lang);
//            List<MRDEntry> rval = new ArrayList<MRDEntry>();
//            while (rs.next()) {
//                String[] pos = rs.getString("types").split("\\{");
//                for (int i = 0; i < pos.length; i++) {
//                    if (posMap.containsKey(pos[i]) && posMap.get(pos[i]) == partOfSpeech) {
//                        rval.add(new WiktionaryEntry(rs.getInt("Id"), i, wordForm, lang));
//                    }
//                }
//            }
//            return rval;
//        } catch (SQLException x) {
//            x.printStackTrace();
//            return null;
//        }
//    }
//    private static HashMap<String, POS> posMap = new HashMap<String, POS>();
//
//    static {
//        posMap.put("adj", new POSImpl("ADJECTIVE"));
//        posMap.put("adv", new POSImpl("ADVERB"));
//        posMap.put("conj", new POSImpl("CONJUNCTION"));
//        posMap.put("det", new POSImpl("DET"));
//        posMap.put("noun", new POSImpl("NOUN"));
//        posMap.put("", new POSImpl("POSTPOSITION"));
//        posMap.put("pron", new POSImpl("PRONOUN"));
//        posMap.put("proper", new POSImpl("PROPERNOUN"));
//        posMap.put("verb", new POSImpl("VERB"));
//        posMap.put("abbr", new POSImpl("NOUN"));
//        posMap.put("cont", new POSImpl("NOUN"));
//    }
//    static PreparedStatement getPOS;
//    static PreparedStatement getSenses;
//    static PreparedStatement getSynSet;
//
//    private static String clean(String s) {
//        try {
//            return new String(s.getBytes("latin1"), "UTF-8");
//        } catch (UnsupportedEncodingException x) {
//            x.printStackTrace();
//        } catch (NullPointerException x) {
//        }
//        return null;
//    }
//    static List<LexicalRelation> supportedRelations = new LinkedList<LexicalRelation>();
//
//    public Collection<LexicalRelation> getSupportedRelations() {
//        if (supportedRelations.isEmpty()) {
//            supportedRelations.add(LexicalRelation.ANOTNYM);
//            supportedRelations.add(LexicalRelation.HYPONYM);
//            supportedRelations.add(LexicalRelation.SYNONYM);
//            supportedRelations.add(TranslationImpl.getInstance(Language.ENGLISH, Language.FRENCH));
//            supportedRelations.add(TranslationImpl.getInstance(Language.ENGLISH, Language.GERMAN));
//            supportedRelations.add(TranslationImpl.getInstance(Language.ENGLISH, Language.SPANISH));
//            supportedRelations.add(TranslationImpl.getInstance(Language.ENGLISH, Language.DUTCH));
//            supportedRelations.add(TranslationImpl.getInstance(Language.ENGLISH, Language.JAPANESE));
//        }
//        return supportedRelations;
//    }
//
//    private class WiktionaryEntry implements MRDEntry {
//
//        int id;
//        int index;
//        String title;
//        Language lang;
//
//        public WiktionaryEntry(int id, int index, String title, Language lang) {
//            this.id = id;
//            this.title = title;
//            this.index = index;
//            this.lang = lang;
//        }
//
//        public Language getLanguage() {
//            return lang;
//        }
//
//        public POS getPartOfSpeech() {
//            try {
//                if (getPOS == null) {
//                    getPOS = connection.prepareStatement("SELECT types FROM wiktionary WHERE Id=?");
//                }
//                getPOS.setInt(1, id);
//                ResultSet rs = getPOS.executeQuery();
//                if (!rs.next()) {
//                    throw new IllegalStateException("WiktionaryEntry in illegal state");
//                }
//                String[] types = rs.getString("types").split("\\{");
//                return posMap.get(types[index]);
//            } catch (SQLException x) {
//                x.printStackTrace();
//                return null;
//            }
//        }
//        HashMap<String, WiktionarySense> senses;
//
//        public Collection<Sense> getSenses() {
//            if (senses != null) {
//                return (Collection) senses.values();
//            }
//            senses = new HashMap<String, WiktionarySense>();
//            try {
//                if (getSenses == null) {
//                    getSenses = connection.prepareStatement("SELECT transGlosses,synonyms,hyponyms,antonyms FROM wiktionary WHERE Id=?");
//                }
//                getSenses.setInt(1, id);
//                ResultSet rs = getSenses.executeQuery();
//                if (!rs.next()) {
//                    throw new IllegalStateException("WiktionaryEntry in illegal state");
//                }
//                String[] transSenses = rs.getString("transGlosses").split("\\{");
//                for (int i = 0; i < transSenses.length; i++) {
//                    senses.put(transSenses[i], new WiktionarySense(id, transSenses[i], this));
//                    senses.get(transSenses[i]).setTransId(id);
//                }
//
//                String[] sensStrs = rs.getString("synonyms").split("\\{");
//                for (int i = 0; i < sensStrs.length; i++) {
//                    if (sensStrs[i].length() == 0) {
//                        continue;
//                    }
//                    Matcher m = Pattern.compile("<(.*)\\}.*").matcher(sensStrs[i]);
//                    if (!m.matches()) {
//                        throw new IllegalStateException("WiktionaryEntry in illegal state");
//                    }
//                    String gloss = m.group(1);
//                    WiktionarySense ws = senses.get(gloss);
//                    if (ws == null) {
//                        senses.put(gloss, ws = new WiktionarySense(id, gloss, this));
//                    }
//                    ws.setSynId(i);
//                }
//                sensStrs = rs.getString("hyponyms").split("\\{");
//                for (int i = 0; i < sensStrs.length; i++) {
//                    if (sensStrs[i].length() == 0) {
//                        continue;
//                    }
//                    Matcher m = Pattern.compile("<(.*)\\}.*").matcher(sensStrs[i]);
//                    if (!m.matches()) {
//                        throw new IllegalStateException("WiktionaryEntry in illegal state");
//                    }
//                    String gloss = m.group(1);
//                    WiktionarySense ws = senses.get(gloss);
//                    if (ws == null) {
//                        senses.put(gloss, ws = new WiktionarySense(id, gloss, this));
//                    }
//                    ws.setHypId(i);
//                }
//                sensStrs = rs.getString("antonyms").split("\\{");
//                for (int i = 0; i < sensStrs.length; i++) {
//                    if (sensStrs[i].length() == 0) {
//                        continue;
//                    }
//                    Matcher m = Pattern.compile("<(.*)\\}.*").matcher(sensStrs[i]);
//                    if (!m.matches()) {
//                        throw new IllegalStateException("WiktionaryEntry in illegal state");
//                    }
//                    String gloss = m.group(1);
//                    WiktionarySense ws = senses.get(gloss);
//                    if (ws == null) {
//                        senses.put(gloss, ws = new WiktionarySense(id, gloss, this));
//                    }
//                    ws.setAntId(i);
//                }
//                return (Collection) senses.values();
//            } catch (SQLException x) {
//                x.printStackTrace();
//                return null;
//            }
//        }
//
//        public Collection<Synset> getSynSets() {
//            try {
//                if (getSynSet == null) {
//                    getSynSet = connection.prepareStatement("SELECT synonyms FROM wiktionary WHERE Id=?");
//                }
//                getSynSet.setInt(1, id);
//                ResultSet rs = getSynSet.executeQuery();
//                if (!rs.next()) {
//                    throw new IllegalStateException("WiktionaryEntry in illegal state");
//                }
//                getSenses();
//                String[] synSets = rs.getString("synonyms").split("\\{");
//                List<Synset> rval = new ArrayList<Synset>();
//                for (int i = 0; i < synSets.length; i++) {
//                    Matcher matcher = Pattern.compile("<(.*)\\}(.*)>").matcher(synSets[i]);
//                    rval.add(new WiktionarySynSet(senses.get(matcher.group(1)), matcher.group(2).split("\"")));
//                }
//                return rval;
//            } catch (SQLException x) {
//                x.printStackTrace();
//                return null;
//            }
//
//        }
//
//        public Collection<String> getWordForms() {
//            return Collections.singletonList(title);
//        }
//    }
//    PreparedStatement getHyp, getSyn, getAnt, getTrans;
//
//    private class DummyWiktionaryEntry implements MRDEntry {
//
//        Language lang;
//        POS pos;
//        String title;
//
//        public DummyWiktionaryEntry(Language lang, POS pos, String title) {
//            this.lang = lang;
//            this.pos = pos;
//            this.title = title;
//        }
//
//        public Language getLanguage() {
//            return lang;
//        }
//
//        public POS getPartOfSpeech() {
//            return pos;
//        }
//
//        public Collection<Sense> getSenses() {
//            return Collections.singletonList((Sense) new WiktionarySense(-1, "dummy", this));
//        }
//
//        public Collection<Synset> getSynSets() {
//            return Collections.EMPTY_LIST;
//        }
//
//        public Collection<String> getWordForms() {
//            return Collections.singletonList(title);
//        }
//    }
//
//    private class WiktionarySense implements Sense {
//
//        int transId = -1, synId = -1, hypId = -1, antId = -1, Id;
//        final String gloss;
//        final MRDEntry entry;
//
//        public WiktionarySense(int Id, String gloss, MRDEntry entry) {
//            this.Id = Id;
//            this.gloss = gloss;
//            this.entry = entry;
//        }
//
//        public void setAntId(int antId) {
//            this.antId = antId;
//        }
//
//        public void setHypId(int hypId) {
//            this.hypId = hypId;
//        }
//
//        public void setSynId(int synId) {
//            this.synId = synId;
//        }
//
//        public void setTransId(int transId) {
//            this.transId = transId;
//        }
//
//        public String getDefinition(Language language) {
//            if (language == Language.ENGLISH) {
//                return gloss;
//            } else {
//                return null;
//            }
//        }
//
//        public Collection<Language> getDefinitionLanguages() {
//            return Collections.singletonList(Language.ENGLISH);
//        }
//
//        public Collection<LexicalRelationInstance> getRelations(LexicalRelation relation) {
//            try {
//                if (relation.getName().equals("hypernym") && hypId >= 0) {
//                    if (getHyp == null) {
//                        getHyp = connection.prepareStatement("SELECT hyponyms FROM wiktionary WHERE Id=?");
//                    }
//                    return getLRIs("hyponyms", hypId, getHyp, relation);
//                } else if (relation.getName().equals("synonym") && synId >= 0) {
//                    if (getSyn == null) {
//                        getSyn = connection.prepareStatement("SELECT synonyms FROM wiktionary WHERE Id=?");
//                    }
//                    return getLRIs("synonyms", synId, getSyn, relation);
//                } else if (relation.getName().equals("antonym") && antId >= 0) {
//                    if (getAnt == null) {
//                        getAnt = connection.prepareStatement("SELECT antonyms FROM wiktionary WHERE Id=?");
//                    }
//                    return getLRIs("antonyms", antId, getAnt, relation);
//                } else if (relation.getName().matches("[a-zA-Z]{2,3}->[a-zA-z]{2,3}")) {
//                    if (getTrans == null) {
//                        getTrans = connection.prepareStatement("SELECT translations FROM wiktionary WHERE Id=?");
//                    }
//                    Matcher matcher = Pattern.compile("([a-zA-Z]{2,3})->([a-zA-z]{2,3})").matcher(relation.getName());
//                    if (!matcher.matches()) {
//                        throw new RuntimeException("Unreachable");
//                    }
//                    if (!entry.getLanguage().equals(Language.get(matcher.group(1)))) {
//                        return null; // Source language not applicable
//                    }
//                    Language trgt = Language.get(matcher.group(2));
//                    getTrans.setInt(1, Id);
//                    ResultSet rs = getTrans.executeQuery();
//                    if (!rs.next()) {
//                        throw new IllegalStateException("WiktionaryEntry in illegal state");
//                    }
//                    matcher = Pattern.compile("\"([^\"]+)\"@" + trgt.toString() + "(\\[|,)([^\\]]*\\])?").matcher(rs.getString("translations"));
//                    Collection<LexicalRelationInstance> rval = new ArrayList<LexicalRelationInstance>();
//                    while (matcher.find()) {
//                        String objStr = matcher.group(1);
//                        Collection<MRDEntry> entries = getEntries(objStr, trgt);
//                        if (entries.isEmpty()) {
//                            DummyWiktionaryEntry dwe = new DummyWiktionaryEntry(trgt, entry.getPartOfSpeech(), objStr);
//                            Sense s = dwe.getSenses().iterator().next();
//                            rval.add(new WiktionaryLRI(this, relation, s));
//                        }
//
//                        for (MRDEntry mrde : entries) {
//                            Collection<Sense> senses = mrde.getSenses();
//                            for (Sense sense : senses) {
//                                rval.add(new WiktionaryLRI(this, relation, sense));
//                            }
//                        }
//                    }
//                    return rval;
//                } else {
//                    return Collections.EMPTY_LIST;
//                }
//            } catch (SQLException x) {
//                x.printStackTrace();
//                return null;
//            }
//        }
//
//        private Collection<LexicalRelationInstance> getLRIs(String column, int propId, PreparedStatement stat, LexicalRelation lr) throws SQLException {
//            stat.setInt(1, Id);
//            ResultSet rs = stat.executeQuery();
//            if (!rs.next()) {
//                throw new IllegalStateException("WiktionaryEntry in illegal state");
//            }
//            String s = rs.getString(column).split("\\{")[propId];
//            Matcher matcher = Pattern.compile("<(.*)\\}(.*)>").matcher(s);
//            if (!matcher.matches()) {
//                throw new IllegalStateException("WiktionaryEntry in illegal state");
//            }
//            String[] ss = matcher.group(2).split("\"");
//            Collection<LexicalRelationInstance> rval = new ArrayList<LexicalRelationInstance>();
//            for (int i = 0; i < ss.length; i++) {
//                Collection<MRDEntry> entries = getEntries(ss[i], entry.getLanguage());
//                for (MRDEntry mrde : entries) {
//                    Collection<Sense> senses = mrde.getSenses();
//                    for (Sense sense : senses) {
//                        rval.add(new WiktionaryLRI(this, lr, sense));
//                    }
//                }
//            }
//            return rval;
//        }
//
//        public MRDEntry getEntry() {
//            return entry;
//        }
//    }
//
//    private class WiktionaryLRI implements LexicalRelationInstance {
//
//        Sense object, subject;
//        LexicalRelation relation;
//
//        public WiktionaryLRI(Sense subject, LexicalRelation relation, Sense object) {
//            this.object = object;
//            this.subject = subject;
//            this.relation = relation;
//        }
//
//        public Sense getObject() {
//            return object;
//        }
//
//        public LexicalRelation getRelation() {
//            return relation;
//        }
//
//        public Sense getSubject() {
//            return subject;
//        }
//    }
//
//    private class WiktionarySynSet implements Synset {
//
//        WiktionarySense sense;
//        String[] elements;
//
//        public WiktionarySynSet(WiktionarySense sense, String[] elements) {
//            this.sense = sense;
//            this.elements = elements;
//        }
//        List<MRDEntry> elementList;
//
//        public Collection<MRDEntry> getElements() {
//            if (elementList != null) {
//                return elementList;
//            }
//            for (String elem : elements) {
//                elementList.addAll(getEntries(elem, sense.entry.getLanguage()));
//            }
//            return elementList;
//        }
//
//        public Collection<Sense> getElementSenses() {
//            return Collections.singleton((Sense) sense);
//        }
//
//        public Sense getSense() {
//            return sense;
//        }
//    }
//}
//
//class POSImpl implements POS {
//
//    private final String value;
//
//    public POSImpl(String value) {
//        this.value = value;
//        WiktionaryPOSSet.instance.add(this);
//    }
//
//    /**
//     * Get the string value of this part-of-speech annotation
//     */
//    public String getValue() {
//        return value;
//    }
//
//    /**
//     * Get the containing POS tag set
//     */
//    public POSSet getPOSSet() {
//        return WiktionaryPOSSet.instance;
//    }
//
//    public int hashCode() {
//        return value.hashCode();
//    }
//
//    public boolean equals(Object o) {
//        if (o instanceof POS) {
//            return value.equals(((POS) o).getValue());
//        } else {
//            return false;
//        }
//    }
//}
//
//class WiktionaryPOSSet extends HashSet<POS> implements POSSet {
//
//    private WiktionaryPOSSet() {
//    }
//    public static final WiktionaryPOSSet instance = new WiktionaryPOSSet();
//
//    public String getPOSSetID() {
//        return "Wiktionary";
//    }
//}
//
//class TranslationImpl implements TranslationRelation {
//
//    private final Language src, trg;
//
//    private TranslationImpl(Language src, Language trg) {
//        this.src = src;
//        this.trg = trg;
//    }
//    private static final HashMap<String, TranslationImpl> map = new HashMap<String, TranslationImpl>();
//
//    public static TranslationRelation getInstance(Language src, Language trg) {
//        String key = src.toString() + "__" + trg.toString();
//        if (map.containsKey(key)) {
//            return map.get(key);
//        } else {
//            map.put(key, new TranslationImpl(src, trg));
//            return map.get(key);
//        }
//    }
//
//    /**
//     * Get the name of this relation
//     */
//    public String getName() {
//        return "translation";
//    }
//
//    /**
//     * Get the source language
//     * @return The source language
//     */
//    public Language getSourceLang() {
//        return src;
//    }
//
//    /**
//     * Get the target language
//     * @return The target language
//     */
//    public Language getTargetLang() {
//        return trg;
//    }

}
