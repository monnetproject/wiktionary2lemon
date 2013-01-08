package eu.monnetproject.sim.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Some math functions for calculating mean, max, cosine etc.
 * 
 * @author Dennis Spohr
 *
 */
public class Functions {

	public static double mean(double[] scores) {
		if (scores.length == 0)
			return 0;
		double sum = 0;
	    for (int i=0; i<scores.length; i++) {
	        sum += scores[i];
	    }
	    return sum / scores.length;
	}
	
	public static double max(double[] scores) {
		if (scores.length == 0)
			return 0;
		double max = scores[0];
	    for (int i=1; i<scores.length; i++) {
	        max = Math.max(max, scores[i]);
	    }
	    return max;
	}
	
	public static double min(double[] scores) {
		if (scores.length == 0)
			return 0;
		double min = scores[0];
	    for (int i=1; i<scores.length; i++) {
	        min = Math.min(min, scores[i]);
	    }
	    return min;
	}
	
    public static double cosine(int[] a, int[] b) {
    
    	double score = 0;
    	double aMagn = 0;
    	double bMagn = 0;
            
    	for(int i = 0; i<a.length; i++) {
                    
    		score += a[i] * b[i];
    		aMagn += a[i] * a[i];
    		bMagn += b[i] * b[i];
                    
    	}
            
    	if (aMagn == 0 || bMagn == 0)
    		return 0;
            
    	score = score / (Math.sqrt(aMagn) * Math.sqrt(bMagn));

    	return score;
    
    }

    public static double bestFirst(double[][] scores) {

    	List<Integer> mapped = new ArrayList<Integer>();
    	double resultScore = 0;


    	double[][] resultScores = scores;

    	while (scores.length > 0) {

    		double best = 0;
    		int bestI = 0;
    		int bestJ = 0;

    		for (int i = 0; i < scores.length; i++) {

    			for (int j = 0; j < scores[i].length; j++) {

    				if (mapped.contains(j))
    					continue;

    				if (scores[i][j] > best) {
    					bestI = i;
    					bestJ = j;
    					best = scores[i][j];
    				}

    			}

    		}

    		resultScore += best;

    		if (scores.length == 1 || scores[0].length == 1)
    			break;

    		double[][] tempScores = new double[scores.length-1][scores[0].length-1];

    		for (int i = 0; i < scores.length; i++) {

    			int index = i;

    			if (i == bestI)
    				continue;
    			else if (i > bestI)
    				index = i - 1;

    			if (bestJ == 0)
    				System.arraycopy(scores[index], 1, tempScores[index], 0,
    						scores[index].length - 1);
    			else
    				System.arraycopy(scores[index], 0, tempScores[index], 0, bestJ);

    			if (bestJ < scores[index].length - 1)
    				System.arraycopy(scores[index], bestJ + 1, tempScores[index],
    						bestJ, (scores[index].length - 1 - bestJ));

    		}

    		scores = tempScores;

    	}

    	return resultScore / (Math.max(resultScores.length, resultScores[0].length));

    }
	
}
