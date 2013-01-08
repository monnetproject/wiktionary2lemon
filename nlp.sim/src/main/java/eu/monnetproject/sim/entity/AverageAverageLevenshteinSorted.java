package eu.monnetproject.sim.entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import eu.monnetproject.util.Logger;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;

import eu.monnetproject.label.LabelExtractor;
import eu.monnetproject.label.LabelExtractorFactory;
import eu.monnetproject.lang.Language;
import eu.monnetproject.ontology.Entity;
import eu.monnetproject.sim.EntitySimilarityMeasure;
import eu.monnetproject.sim.StringSimilarityMeasure;
import eu.monnetproject.sim.util.Functions;
import eu.monnetproject.sim.util.SimilarityUtils;
import eu.monnetproject.tokenizer.Tokenizer;
import eu.monnetproject.translator.Translator;
import eu.monnetproject.util.Logging;

/**
 * Levenshtein similarity after sorting the labels. For cases like "Assets, current" vs. "Current assets"
 * Intralingual aggregation: average
 * Interlingual aggregation: average
 * 
 * @author Dennis Spohr
 *
 */
@Component(provide=EntitySimilarityMeasure.class,properties={"measure=Levenshtein"})
public class AverageAverageLevenshteinSorted implements EntitySimilarityMeasure {
	
    private Logger log = Logging.getLogger(this);
	private final String name = this.getClass().getName();
	private LabelExtractorFactory lef;
	private LabelExtractor lex;
	private Collection<Language> languages = Collections.emptySet();
	private StringSimilarityMeasure measure;
	private boolean includePuns = false;
	private Translator translator;
	private Tokenizer tokenizer;
	
	public AverageAverageLevenshteinSorted() {
	}
	
	@Activate
	public void start() {
		log.info("Activating "+this.name);
	}
	
	@Reference
	public void bindLabelExtractorFactory(LabelExtractorFactory lef) {
		log.info("Binding label extractor factory to "+lef);
		this.lef = lef;
	}

	public void unbindLabelExtractorFactory(LabelExtractorFactory lef) {
		log.info("Removing label extractor factory "+lef);
		this.lef = null;
	}

	@Reference(service=StringSimilarityMeasure.class,type='+')
	public void addMeasure(StringSimilarityMeasure measure, Map props) {
		if (props.get("measure") != null && props.get("measure").equals("Levenshtein")) {
			this.measure = measure;
			log.info("Binding measure to "+measure);
		}
	}

	public void removeMeasure(StringSimilarityMeasure measure) {
		log.info("Removing measure "+measure);
		this.measure = null;
	}

    @Reference(type='?')
    public void bindTranslator(Translator t) {
    	log.info("Binding translator "+t);
    	this.translator  = t;
    }
    
	@Reference(type='+')
	public void addTokenizer(Tokenizer tok) {
		log.info("Binding tokenizer to "+tok);
		this.tokenizer = tok;
	}

	public void removeTokenizer(Tokenizer tok) {
		log.info("Removing tokenizer "+tok);
		this.tokenizer = null;
	}

	public void configure(Properties properties) {
		this.languages = SimilarityUtils.getLanguages(properties.getProperty("languages", ""));    	
		for (Language lang : this.languages) {
			log.info("Requested language: "+lang);
		}
		this.includePuns = SimilarityUtils.getIncludePuns(properties.getProperty("include_puns", "false"));
    }
    
	@Override
	public double getScore(Entity srcEntity, Entity tgtEntity) {

		if (this.lex == null) {
			this.lex = this.lef.getExtractor(SimilarityUtils.determineLabelProperties(srcEntity, tgtEntity), true, false);
		}

		if (this.languages.size() < 1) {
			log.warning("No languages specified in config file.");

			this.languages = SimilarityUtils.determineLanguages(srcEntity, tgtEntity);
					
			String langs = "";
			for (Language lang : languages) {
				langs += lang.getName()+", ";
			}
			
			try {
				log.warning("Using "+langs.substring(0, langs.lastIndexOf(","))+".");
			} catch (Exception e) {
				log.severe("No languages in source and target ontology.");
			}

		}
		
		Map<Language, Collection<String>> srcMap = null;
		Map<Language, Collection<String>> tgtMap = null;

		if (includePuns) {
			srcMap = SimilarityUtils.getLabelsIncludingPuns(srcEntity,lex);
			tgtMap = SimilarityUtils.getLabelsIncludingPuns(tgtEntity,lex);
		} else {
			srcMap = SimilarityUtils.getLabelsExcludingPuns(srcEntity,lex);
			tgtMap = SimilarityUtils.getLabelsExcludingPuns(tgtEntity,lex);
		}
		
		List<Double> intralingualScores = new ArrayList<Double>();
		
		for (Language language : this.languages) {
			
			Collection<String> srcLabels = srcMap.get(language);
			Collection<String> tgtLabels = tgtMap.get(language);
			
			if (srcLabels == null) {
				if (translator == null) {
					log.warning("Can't match in "+language+" because "+srcEntity.getURI()+" has no labels in "+language+" and no translator is available.");
					continue;
				}
				srcLabels = SimilarityUtils.getTranslatedLabels(srcEntity,language,translator,lex);
			}
				
			if (tgtLabels == null) {
				if (translator == null) {
					log.warning("Can't match in "+language+" because "+tgtEntity.getURI()+" has no labels in "+language+" and no translator is available.");
					continue;
				}
				tgtLabels = SimilarityUtils.getTranslatedLabels(tgtEntity,language,translator,lex);
			}
			
			double[] scores = new double[srcLabels.size()*tgtLabels.size()];
			
			int index = 0;
			
			for (String srcLabel : srcLabels) {
				
				srcLabel = SimilarityUtils.sortTokens(tokenizer.tokenize(srcLabel));
				
				for (String tgtLabel : tgtLabels) {
					
					tgtLabel = SimilarityUtils.sortTokens(tokenizer.tokenize(tgtLabel));
					
					scores[index++] = measure.getScore(srcLabel, tgtLabel);
					
				}
								
			}
			
			intralingualScores.add(Functions.mean(scores));
			
		}

		if (intralingualScores.size() < 1)
			return 0.;
		
		double[] intralingualScoresArray = new double[intralingualScores.size()];
		
		for (int i = 0; i < intralingualScores.size(); i++) {
			intralingualScoresArray[i] = intralingualScores.get(i);
		}
		
		return Functions.mean(intralingualScoresArray);
		
	}

	@Override
	public String getName() {
		return this.name;
	}

}
