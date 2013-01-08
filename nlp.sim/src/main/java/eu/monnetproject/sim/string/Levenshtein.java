package eu.monnetproject.sim.string;

import aQute.bnd.annotation.component.Component;
import eu.monnetproject.sim.StringSimilarityMeasure;

/**
 * Levenshtein measure
 * 
 * @author Tobias Wunner
 *
 */
@Component(provide=StringSimilarityMeasure.class,properties={"measure=Levenshtein"})
public class Levenshtein implements StringSimilarityMeasure {

/*	private int minimum(int a, int b, int c) {
		return Math.min(Math.min(a, b), c);
	}*/

	/* This algorithm has been taken from the alignment API. */
	private double levenshteinDistance (String s, String t) {

		if (s == null || t == null) {
            return 1.;
        }
                
        int n = s.length(); // length of s
        int m = t.length(); // length of t
                
        if (n == 0) return 1.;
        else if (m == 0) return 1.;

        int p[] = new int[n+1]; //'previous' cost array, horizontally
        int d[] = new int[n+1]; // cost array, horizontally
        int _d[]; //placeholder to assist in swapping p and d

        // indexes into strings s and t
        int i; // iterates through s
        int j; // iterates through t

        char t_j; // jth character of t

        int cost; // cost

        for (i = 0; i<=n; i++) p[i] = i;
        
        for (j = 1; j<=m; j++) {
            t_j = t.charAt(j-1);
            d[0] = j;
            
            for (i=1; i<=n; i++) {
                cost = s.charAt(i-1)==t_j ? 0 : 1;
                // minimum of cell to the left+1, to the top+1,
                // diagonally left and up +cost                         
                d[i] = Math.min(Math.min(d[i-1]+1, p[i]+1),  p[i-1]+cost);  
            }
            
            // copy current distance counts to 'previous row' distance counts
            _d = p;
            p = d;
            d = _d;
        } 

        int min = Math.min( n, m );
        int diff = Math.max( n, m ) - min;
        return (double)p[n] / (double)(min + diff);
    }

	
/*	private int levenhsteinDistance(String str1, String str2) {
		int[][] distance = new int[str1.length() + 1][str2.length() + 1];
		for (int i = 0; i <= str1.length(); i++)
			distance[i][0] = i;
		for (int j = 0; j <= str2.length(); j++)
			distance[0][j] = j;
		for (int i = 1; i <= str1.length(); i++)
			for (int j = 1; j <= str2.length(); j++)
				distance[i][j] = minimum(
						distance[i - 1][j] + 1,
						distance[i][j - 1] + 1,
						distance[i - 1][j - 1]
						                + ((str1.charAt(i - 1) == str2.charAt(j - 1)) ? 0
						                		: 1));
		return distance[str1.length()][str2.length()];
	}
*/	
	@Override
	public double getScore(String s1, String s2) {
		//double MAX_DIST = Math.max(s1.length(),s2.length());
		double dist = levenshteinDistance(s1, s2); ///MAX_DIST;
		return 1-dist;
	}

	@Override
	public String getName() {
		return "LevenshteinMeasure";
	}

}
