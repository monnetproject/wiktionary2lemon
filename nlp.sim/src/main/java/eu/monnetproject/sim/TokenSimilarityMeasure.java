package eu.monnetproject.sim;

import eu.monnetproject.tokenizer.Token;
import java.util.List;


/**
 * Measure calculating the similarity between two lists of tokens
 * 
 * @author Tobias Wunner
 *
 */
public interface TokenSimilarityMeasure extends SimilarityMeasure<List<Token>> {

}
