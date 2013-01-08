package eu.monnetproject.sim;

/**
 * Measure calculating the similarity between two types
 * 
 * @author Tobias Wunner
 *
 */
public interface SimilarityMeasure<T>  {
	
    public double getScore(T x1, T x2);
    public String getName();

}
