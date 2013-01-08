package eu.monnetproject.sim.entity;

import java.util.Collection;
import java.util.Properties;
import eu.monnetproject.util.Logger;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;

import eu.monnetproject.ontology.Entity;
import eu.monnetproject.sim.EntitySimilarityMeasure;
import eu.monnetproject.sim.util.Functions;
import eu.monnetproject.sim.util.SimilarityUtils;
import eu.monnetproject.util.Logging;

/**
 * Similarity measure based on difference between average number of elementary summation items in the calculations of the entities.
 * 
 * @author Dennis Spohr
 *
 */
@Component(provide=EntitySimilarityMeasure.class)
public class AverageElementarySummationItems implements EntitySimilarityMeasure {
	
    private Logger log = Logging.getLogger(this);
	private final String name = this.getClass().getName();
	
	public AverageElementarySummationItems() {
	}
	
	@Activate
	public void start() {
		log.info("Activating "+this.name);
	}
	
    public void configure(Properties properties) {
    }
    
	@Override
	public double getScore(Entity srcEntity, Entity tgtEntity) {
		
		Collection<Collection<Entity>> srcCalculations = SimilarityUtils.getCalculations(srcEntity,true);
		Collection<Collection<Entity>> tgtCalculations = SimilarityUtils.getCalculations(tgtEntity,true);
		
		if (srcCalculations.size() < 1 && tgtCalculations.size() < 1) {
			return 1.;
		}

		if (srcCalculations.size() < 1 || tgtCalculations.size() < 1)
			return 0.;
		
		double[] srcItems = new double[srcCalculations.size()];
		double[] tgtItems = new double[tgtCalculations.size()];
		
		int index = 0;
		
		for (Collection<Entity> srcCalculation : srcCalculations) {
			srcItems[index++] = srcCalculation.size();
		}

		index = 0;
		
		for (Collection<Entity> tgtCalculation : tgtCalculations) {
			tgtItems[index++] = tgtCalculation.size();
		}
		
		double srcAvg = Functions.mean(srcItems);
		
		double tgtAvg = Functions.mean(tgtItems);
		
		return 1 - (Math.abs(srcAvg - tgtAvg) / Math.max(srcAvg,tgtAvg));

	}

	@Override
	public String getName() {
		return this.name;
	}

}
