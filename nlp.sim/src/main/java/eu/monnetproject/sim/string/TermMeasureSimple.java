package eu.monnetproject.sim.string;

import eu.monnetproject.sim.StringSimilarityMeasure;
import eu.monnetproject.sim.token.TokenLevenshtein;

import java.util.ArrayList;
import java.util.List;

import eu.monnetproject.stl.t.TermAnalyzer;
import eu.monnetproject.tokenizer.Token;

/**
 * Termbase measure using a termbase and Levenshtein Token Measure
 * 
 * @author Tobias Wunner
 *
 */
public class TermMeasureSimple implements StringSimilarityMeasure {

	private TermAnalyzer termAnalyzer;
	private TokenLevenshtein tokenLev = new TokenLevenshtein();

	public void actvate(TermAnalyzer termAnalyzer) {
		this.termAnalyzer = termAnalyzer;
	}
	
	private List<Token> termList2TokenList(List<String> termList) {
		List<Token> tokenList = new ArrayList<Token>();
		for(String termToken:termList) {
			tokenList.add(new Token(termToken));
		}
		return tokenList;
	}
	
	@Override
	public double getScore(String s1, String s2) {
		List<Token> tokens1 = termList2TokenList(termAnalyzer.analyzeTerm(s1));
		List<Token> tokens2 = termList2TokenList(termAnalyzer.analyzeTerm(s1));
		return tokenLev.getScore(tokens1, tokens2);
	}

	@Override
	public String getName() {
		return "TermBaseMeasureSimple";
	}

}
