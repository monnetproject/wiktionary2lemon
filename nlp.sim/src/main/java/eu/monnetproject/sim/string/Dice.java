package eu.monnetproject.sim.string;

import eu.monnetproject.sim.StringSimilarityMeasure;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;


/**
 * Dice measure
 * 
 * @author Tobias Wunner
 *
 */
public class Dice implements StringSimilarityMeasure {

	private double dice(String s1, String s2, int n)
	{
		double totcombigrams = 0;
		Set<String> nx = new HashSet<String>();
		Set<String> ny = new HashSet<String>();
		Set<String> intersection = null;

		for (int i=0; i < s1.length()-n; i++) {
			String tmp = s1.substring(i, i+n);
			//System.out.println(tmp);
			nx.add(tmp);
		}
		for (int j=0; j < s2.length()-n; j++) {
			String tmp = s2.substring(j, j+n);
			ny.add(tmp);
		}

		intersection = new TreeSet<String>(nx);
		intersection.retainAll(ny);
		totcombigrams = intersection.size();

		return 1 - ((2*totcombigrams) / (nx.size()+ny.size()));
	}   

	
	@Override
	public double getScore(String s1, String s2) {
		return 1-dice(s1, s2, 2);
	}

	@Override
	public String getName() {
		return "DiceCoefficientMeasure";
	}

}
