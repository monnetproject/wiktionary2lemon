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
 * Similarity measure based on difference between minimum number of elementary parent children in the presentations of the entities.
 * 
 * @author Dennis Spohr
 *
 */
@Component(provide=EntitySimilarityMeasure.class)
public class MinimumElementaryParentChildren implements EntitySimilarityMeasure {
	
    private Logger log = Logging.getLogger(this);
	private final String name = this.getClass().getName();
	
	public MinimumElementaryParentChildren() {
	}
	
	@Activate
	public void start() {
		log.info("Activating "+this.name);
	}
	
    public void configure(Properties properties) {
    }
    
	@Override
	public double getScore(Entity srcEntity, Entity tgtEntity) {
		
		Collection<Collection<Entity>> srcPresentations = SimilarityUtils.getPresentations(srcEntity,true);
		Collection<Collection<Entity>> tgtPresentations = SimilarityUtils.getPresentations(tgtEntity,true);
		
		if (srcPresentations.size() < 1 && tgtPresentations.size() < 1) {
			return 1.;
		}

		if (srcPresentations.size() < 1 || tgtPresentations.size() < 1)
			return 0.;
		
		double[] srcItems = new double[srcPresentations.size()];
		double[] tgtItems = new double[tgtPresentations.size()];
		
		int index = 0;
		
		for (Collection<Entity> srcPresentation : srcPresentations) {
			srcItems[index++] = srcPresentation.size();
		}

		index = 0;
		
		for (Collection<Entity> tgtPresentation : tgtPresentations) {
			tgtItems[index++] = tgtPresentation.size();
		}
		
		double srcMin = Functions.min(srcItems);
		
		double tgtMin = Functions.min(tgtItems);
		
		return 1 - (Math.abs(srcMin - tgtMin) / Math.max(srcMin,tgtMin));

	}

	@Override
	public String getName() {
		return this.name;
	}

}
