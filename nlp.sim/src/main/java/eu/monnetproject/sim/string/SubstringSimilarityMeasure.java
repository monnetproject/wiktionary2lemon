package eu.monnetproject.sim.string;

import aQute.bnd.annotation.component.Component;
import eu.monnetproject.sim.StringSimilarityMeasure;

@Component(provide=StringSimilarityMeasure.class,properties="measure=Substring")
public class SubstringSimilarityMeasure implements StringSimilarityMeasure {

	private final String name = "Substring Similarity";
	
	/**
	 * The following method has been taken from the Ontosim package.
	 */
	@Override
	public double getScore(String label1, String label2) {
		if (label1 == null || label2 == null) {
			//throw new IllegalArgumentException("Strings must not be null");
			return 1.;
		}
	        
		int l1 = label1.length(); // length of s
		int l2 = label2.length(); // length of t
	                
		if ((l1 == 0) && ( l2 == 0 )) return 0.;
		if ((l1 == 0) || ( l2 == 0 )) return 1.;

		//int max = Math.min( l1, l2 ); // the maximal length of a subs
		int best = 0; // the best subs length so far
	            
		int i = 0; // iterates through label1
		int j = 0; // iterates through label2

		for( i=0; (i < l1) && (l1-i > best); i++ ){
			j = 0;
			while( l2-j > best ){
				int k = i;
				for( ; (j < l2 )
						&& (label1.charAt(k) != label2.charAt(j) ); j++) {};
						if ( j != l2 ) {// we have found a starting point
							for( j++, k++; (j < l2) && (k < l1) && (label1.charAt(k) == label2.charAt(j)); j++, k++);
							best = Math.max( best, k-i );
						}
			}
		}
		return ((double)2*best / (double)(l1+l2));
	}

	@Override
	public String getName() {

		return this.name;
	}

}
