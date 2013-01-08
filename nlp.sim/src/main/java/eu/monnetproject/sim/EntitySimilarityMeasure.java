package eu.monnetproject.sim;

import java.util.Properties;

import eu.monnetproject.ontology.Entity;

/**
 * Measure calculating the similarity between two ontology entities
 * 
 * @author Dennis Spohr
 *
 */
public interface EntitySimilarityMeasure extends SimilarityMeasure<Entity> {
	
	public void configure(Properties properties);
	
}
