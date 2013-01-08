package eu.monnetproject.webaccess.dictionary.wiktionary.dump;

import eu.monnetproject.sim.StringSimilarityMeasure;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author John McCrae
 */
public class SenseMerger {

    private static PrintStream stats,notMergedOut;
    private int merged = 0, notMerged = 0;

    static {
        try {
            stats = new PrintStream("sense-merges2.csv");
            notMergedOut = new PrintStream("sense-notMergedOut.csv");
        } catch (FileNotFoundException ex) {
            stats = null;
        }
    }
    private final StringSimilarityMeasure measure;
    private final double threshold = -1.0;

    public SenseMerger(StringSimilarityMeasure measure) {
        this.measure = measure;
    }

    public void calcMerge(DumpEntry entry) {
        if (entry.definitions != null) {
            entry.transSenses = makeMap(entry.definitions, entry.transGlosses);
            entry.synSenses = makeMap2(entry.definitions, entry.synonyms, "syns");
            entry.antSenses = makeMap2(entry.definitions, entry.antonyms, "ants");
            entry.hypSenses = makeMap2(entry.definitions, entry.hyponyms, "hyps");
        }
        notMergedOut.println("\"notMerged\",\"\"," + notMerged + ",-1.0");
    }

    private Map<Integer, Integer> makeMap(List<DumpSense> glosses, List<String> transGlosses) {
        Map<Integer, Integer> rval = new HashMap<Integer, Integer>();
        for (int i = 0; i < transGlosses.size(); i++) {
            String transGloss = transGlosses.get(i);
            int bestIdx = -1;
            double bestScore = threshold;
            for (int j = 0; j < glosses.size(); j++) {
                String gloss = glosses.get(j).gloss;
                double score;
                if (gloss.contains(transGloss)) {
                    score = 100.0; // certain
                } else {
                    score = measure.getScore(gloss, transGloss);
                }
                if (score > bestScore) {
                    bestIdx = j;
                    bestScore = score;
                }
            }
            if (bestIdx >= 0 && !rval.values().contains(bestIdx)) {
                rval.put(i, bestIdx);
                stats.println("\"trans\",\"" + glosses.get(bestIdx).gloss + "\",\"" + transGloss + "\"," + bestScore);
            } else {
                notMerged++;
            }
        }
        return rval;
    }

    private Map<DumpSense, DumpSense> makeMap2(List<DumpSense> glosses, List<DumpSense> transGlosses, String name) {
        Map<DumpSense, DumpSense> rval = new HashMap<DumpSense, DumpSense>();
        for (int i = 0; i < transGlosses.size(); i++) {
            String transGloss = transGlosses.get(i).gloss;
            DumpSense bestSense = null;
            double bestScore = threshold;
            for (int j = 0; j < glosses.size(); j++) {
                String gloss = glosses.get(j).gloss;
                double score;
                if (gloss.contains(transGloss)) {
                    score = 100.0; // certain
                } else {
                    score = measure.getScore(gloss, transGloss);
                }
                if (score > bestScore) {
                    bestSense = glosses.get(j);
                    bestScore = score;

                }
            }
            if (bestSense != null && !rval.values().contains(bestSense)) {
                rval.put(transGlosses.get(i), bestSense);
                merged++;
                stats.println("\"" + name + "\",\"" + bestSense.gloss + "\",\"" + transGloss + "\"," + bestScore);
            } else {
                notMerged++;
            }
        }
        return rval;
    }
}
