package eu.monnetproject.sim.entity;

import java.util.Collection;
import java.util.Map;
import java.util.Properties;
import eu.monnetproject.util.Logger;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;

import eu.monnetproject.label.LabelExtractorFactory;
import eu.monnetproject.ontology.Entity;
import eu.monnetproject.sim.EntitySimilarityMeasure;
import eu.monnetproject.sim.StringSimilarityMeasure;
import eu.monnetproject.sim.util.Functions;
import eu.monnetproject.sim.util.SimilarityUtils;
import eu.monnetproject.translator.Translator;
import eu.monnetproject.util.Logging;

/**
 * Similarity measure based on best first string difference between elementary summation items in the calculations of the entities.
 * 
 * @author Dennis Spohr
 *
 */
@Component(provide=EntitySimilarityMeasure.class)
public class BestFirstStringDifferenceElementarySummationItems implements EntitySimilarityMeasure {
	
    private Logger log = Logging.getLogger(this);
	private final String name = this.getClass().getName();
	private EntitySimilarityMeasure subMeasure = null;
	private LabelExtractorFactory lef;
	private StringSimilarityMeasure stringMeasure;
	private Translator translator;

	
	public BestFirstStringDifferenceElementarySummationItems() {
	}
	
	/*
	 * subMeasure isn't bound via @Reference since this results in a cycle for EntitySimilarityMeasure. Therefore, 
	 * all references required by subMeasure are bound here and passed on when this measure is activated.
	 */
	@Activate
	public void start() {
		log.info("Activating "+this.name);
		this.subMeasure = new AverageAverageLevenshtein(lef,translator,stringMeasure);
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
			this.stringMeasure = measure;
			log.info("Binding measure to "+measure);
		}
	}

	public void removeMeasure(StringSimilarityMeasure measure) {
		log.info("Removing measure "+measure);
		this.stringMeasure = null;
	}

    @Reference(type='?')
    public void bindTranslator(Translator t) {
    	log.info("Binding translator "+t);
    	this.translator  = t;
    }
    
	public void configure(Properties properties) {
		subMeasure.configure(properties);
    }
    
	@Override
	public double getScore(Entity srcEntity, Entity tgtEntity) {
		
		if (subMeasure == null) {
			log.severe("Couldn't bind sub-measure for calculating String similarity. Returning score 0.");
			return 0.;
		}
		
		Collection<Collection<Entity>> srcCalculations = SimilarityUtils.getCalculations(srcEntity,true);
		Collection<Collection<Entity>> tgtCalculations = SimilarityUtils.getCalculations(tgtEntity,true);
		
		if (srcCalculations.size() < 1 && tgtCalculations.size() < 1) {
			return 1.;
		}

		if (srcCalculations.size() < 1 || tgtCalculations.size() < 1)
			return 0.;
		
		int index = 0;
		
		double[] calculationSims = new double[srcCalculations.size()+tgtCalculations.size()];
		
		for (Collection<Entity> srcCalculation : srcCalculations) {

			double calculationSim = 0;
            Collection<Entity> bestCalculation = null;
            
			for (Collection<Entity> tgtCalculation : tgtCalculations) {
				
				double[][] similarityMatrix = new double[srcCalculation.size()][tgtCalculation.size()];
			
				int i = 0;
				
				for (Entity srcItem : srcCalculation) {
					
					int j = 0;

					for (Entity tgtItem : tgtCalculation) {

						similarityMatrix[i][j] = subMeasure.getScore(srcItem, tgtItem);

                        j++;

					}
					
					i++;
					
				}
				
				double score;

                score = Functions.bestFirst(similarityMatrix);

                if (score > calculationSim) {
                        calculationSim = score;
                        bestCalculation = tgtCalculation;
                }
			}
			
			if (bestCalculation != null) {
                calculationSims[index] = calculationSim;
                index++;
			}
		}
		
		return Functions.max(calculationSims);

	}

	@Override
	public String getName() {
		return this.name;
	}

}
