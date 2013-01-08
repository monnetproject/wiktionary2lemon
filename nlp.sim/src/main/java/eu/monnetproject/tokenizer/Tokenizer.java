package eu.monnetproject.tokenizer;

import eu.monnetproject.lang.Script;
import java.util.List;

/**
 * Interface to a tokenizer
 * @author John McCrae
 */
public interface Tokenizer {
    /**
     * Tokenize a single string
     * @param input The string to tokenize
     * @return A list of tokens
     */
	List<Token> tokenize(String input);
	
	/**
	 * Get the script the tokenizer supports
	 */
	Script getScript();
}
