package eu.monnetproject.sim.entity;

import java.net.URI;
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
 * Similarity measure based on difference between maximum number of direct children in the calculations (for monetary items) or presentations (for string items) of the entities.
 * 
 * @author Dennis Spohr
 *
 */
@Component(provide=EntitySimilarityMeasure.class)
public class MaximumDirectChildren implements EntitySimilarityMeasure {
	
    private Logger log = Logging.getLogger(this);
	private final String name = this.getClass().getName();
	
	public MaximumDirectChildren() {
	}
	
	@Activate
	public void start() {
		log.info("Activating "+this.name);
	}
	
    public void configure(Properties properties) {
    }
    
	@Override
	public double getScore(Entity srcEntity, Entity tgtEntity) {

		Collection<Collection<Entity>> srcCalculations = null;
		Collection<Collection<Entity>> tgtCalculations = null;

		if (srcEntity instanceof eu.monnetproject.ontology.Class) {
			if (((eu.monnetproject.ontology.Class)srcEntity).getSuperClassOf().contains(srcEntity.getOntology().getFactory().makeClass(URI.create("http://www.xbrl.org/2003/instance/monetaryItemType")))) {
				srcCalculations = SimilarityUtils.getCalculations(srcEntity,false);
				tgtCalculations = SimilarityUtils.getCalculations(tgtEntity,false);
			} else if (((eu.monnetproject.ontology.Class)srcEntity).getSuperClassOf().contains(srcEntity.getOntology().getFactory().makeClass(URI.create("http://www.xbrl.org/2003/instance/stringItemType")))) {
				srcCalculations = SimilarityUtils.getPresentations(srcEntity,false);
				tgtCalculations = SimilarityUtils.getPresentations(tgtEntity,false);
			}
		}

		
		
		if ((srcCalculations == tgtCalculations) || (srcCalculations.size() < 1 && tgtCalculations.size() < 1)) {
			return 1.;
		}

		if (srcCalculations == null || tgtCalculations == null || srcCalculations.size() < 1 || tgtCalculations.size() < 1)
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
		
		double srcAvg = Functions.max(srcItems);
		
		double tgtAvg = Functions.max(tgtItems);
		
		return 1 - (Math.abs(srcAvg - tgtAvg) / Math.max(srcAvg,tgtAvg));

	}

	@Override
	public String getName() {
		return this.name;
	}

}
