package eu.monnetproject.tokenizer;

import eu.monnetproject.lang.Script;

/**
 * A factory for tokenizers
 * 
 * @author John McCrae
 */
public interface TokenizerFactory {
    Tokenizer getTokenizer(Script script);
}
