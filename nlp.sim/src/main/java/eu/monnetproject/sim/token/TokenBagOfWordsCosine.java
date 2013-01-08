package eu.monnetproject.sim.token;

import eu.monnetproject.sim.TokenSimilarityMeasure;
import eu.monnetproject.sim.util.Functions;

import java.util.List;
import java.util.Vector;

import aQute.bnd.annotation.component.Component;
import eu.monnetproject.tokenizer.Token;


/**
 * Token list measure calculating bag-of-words cosine similarity
 * 
 * @author Dennis Spohr
 *
 */
@Component(provide=TokenSimilarityMeasure.class,properties="measure=BagOfWordsCosine")
public class TokenBagOfWordsCosine implements TokenSimilarityMeasure {

	@Override
	public double getScore(List<Token> srcTokens, List<Token> tgtTokens) {
        
        List<String> bag = new Vector<String>();
        
        int[] aVec = new int[srcTokens.size()+tgtTokens.size()];
        int[] bVec = new int[aVec.length];
        
        for (Token token : srcTokens) {
        	if (bag.contains(token.getValue().toLowerCase()))
        		aVec[bag.indexOf(token.getValue().toLowerCase())]++;
        	else {
        		bag.add(token.getValue().toLowerCase());
        		aVec[bag.indexOf(token.getValue().toLowerCase())] = 1;
        	}
        }

        for (Token token : tgtTokens) {
        	if (bag.contains(token.getValue().toLowerCase()))
        		bVec[bag.indexOf(token.getValue().toLowerCase())]++;
        	else {
        		bag.add(token.getValue().toLowerCase());
        		bVec[bag.indexOf(token.getValue().toLowerCase())] = 1;
        	}
        }
        
        return Functions.cosine(aVec, bVec);
        
	}

	@Override
	public String getName() {
		return "TokenBagOfWordsCosine";
	}
	
}
