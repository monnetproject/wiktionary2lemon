package eu.monnetproject.util;

/**
 * Represents a pair of values. This class allows types to be preserved and the
 * pair of values to be used in hashes and tree sets.
 *
 * @author John McCrae
 */
public interface Pair<E,F> {
	/**
     * Get the first value in the pair
     */	
	E getFirst();
	
	/**
	 * Get the second value in the pair
	 */
	F getSecond();
}
