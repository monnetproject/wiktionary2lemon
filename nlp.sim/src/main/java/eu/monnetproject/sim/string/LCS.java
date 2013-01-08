package eu.monnetproject.sim.string;

import eu.monnetproject.sim.StringSimilarityMeasure;

/**
 * Longest common subsequence measure
 * 
 * @author Tobias Wunner
 *
 */
public class LCS implements StringSimilarityMeasure {

	public int lcs(String s1, String s2) {
		if (s1 == null || s2 == null || s1.length() == 0 || s2.length() == 0) {
			return 0;
		}

		int maxLen = 0;
		int fl = s1.length();
		int sl = s2.length();
		int[][] table = new int[fl][sl];

		for (int i = 0; i < fl; i++) {
			for (int j = 0; j < sl; j++) {
				if (s1.charAt(i) == s2.charAt(j)) {
					if (i == 0 || j == 0) {
						table[i][j] = 1;
					}
					else {
						table[i][j] = table[i - 1][j - 1] + 1;
					}
					if (table[i][j] > maxLen) {
						maxLen = table[i][j];
					}
				}
			}
		}
		return maxLen;
	}

	@Override
	public double getScore(String s1, String s2) {
		double MAX_DIST = (s1.length()+s2.length())/2.0 ;
		double score = lcs(s1, s2);
		return score/MAX_DIST;
	}

	@Override
	public String getName() {
		return "LongestCommonSubsequenceMeasure";
	}

}
