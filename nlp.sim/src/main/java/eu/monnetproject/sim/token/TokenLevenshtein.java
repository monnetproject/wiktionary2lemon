package eu.monnetproject.sim.token;

import eu.monnetproject.sim.TokenSimilarityMeasure;
import eu.monnetproject.tokenizer.Token;
import java.util.List;


/**
 * Token list measure using Levenshtein algorithm
 * 
 * @author Tobias Wunner
 *
 */
public class TokenLevenshtein implements TokenSimilarityMeasure {

	private int minimum(int a, int b, int c) {
		return Math.min(Math.min(a, b), c);
	}

	public int levenhsteinTokenDistance(List<Token> tokens1, List<Token> tokens2) {
		int n1 = tokens1.size();
		int n2 = tokens2.size();
		int[][] distance = new int[n1 + 1][n2 + 1];
		for (int i = 0; i <= n1; i++)
			distance[i][0] = i;
		for (int j = 0; j <= n2; j++)
			distance[0][j] = j;
		for (int i = 1; i <= n1; i++)
			for (int j = 1; j <= n2; j++) {
				int a = distance[i - 1][j] + 1;
				int b = distance[i][j - 1] + 1;
				int c = distance[i - 1][j - 1] + ((tokens1.get(i - 1).equals(tokens2.get(j - 1))) ? 0 : 1);
				distance[i][j] = minimum(a,b,c);
			}
		return distance[n1][n2];
	}
	
	@Override
	public double getScore(List<Token> tokens1, List<Token> tokens2) {
		double MAX_DIST = Math.max(tokens1.size(),tokens2.size());
		double dist = levenhsteinTokenDistance(tokens1, tokens2)/MAX_DIST;
		return 1-dist;
	}

	@Override
	public String getName() {
		return "TokenLevenshteinMeasure";
	}

}
