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
import eu.monnetproject.sim.TokenSimilarityMeasure;
import eu.monnetproject.sim.util.Functions;
import eu.monnetproject.sim.util.SimilarityUtils;
import eu.monnetproject.tokenizer.Token;
import eu.monnetproject.tokenizer.Tokenizer;
import eu.monnetproject.translator.Translator;
import eu.monnetproject.util.Logging;

/**
 * Bag-of-words cosine similarity.
 * Intralingual aggregation: average
 * Interlingual aggregation: average
 * 
 * @author Dennis Spohr
 *
 */
@Component(provide=EntitySimilarityMeasure.class)
public class AverageAverageBagOfWordsCosine implements EntitySimilarityMeasure {
	
    private Logger log = Logging.getLogger(this);
	private final String name = this.getClass().getName();
	private LabelExtractorFactory lef;
	private LabelExtractor lex;
	private Collection<Language> languages = Collections.emptySet();
	private TokenSimilarityMeasure measure;
	private Tokenizer tokenizer;
	private boolean includePuns = false;
	private Translator translator = null;
	
	public AverageAverageBagOfWordsCosine() {
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

	@Reference(service=TokenSimilarityMeasure.class,type='+')
	public void addMeasure(TokenSimilarityMeasure measure, Map props) {
		if (props.get("measure") != null && props.get("measure").equals("BagOfWordsCosine")) {
			this.measure = measure;
			log.info("Binding measure to "+measure);
		}
	}

	public void removeMeasure(TokenSimilarityMeasure measure) {
		log.info("Removing measure "+measure);
		this.measure = null;
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

    @Reference(type='?')
    public void bindTranslator(Translator t) {
    	log.info("Binding translator "+t);
    	this.translator  = t;
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
			this.lex = this.lef.getExtractor(SimilarityUtils.determineLabelProperties(srcEntity, tgtEntity), true, true);
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
			
				List<Token> srcTokens = tokenizer.tokenize(srcLabel);
			
				for (String tgtLabel : tgtLabels) {
				
					List<Token> tgtTokens = tokenizer.tokenize(tgtLabel);

					scores[index++] = measure.getScore(srcTokens, tgtTokens);
				
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
