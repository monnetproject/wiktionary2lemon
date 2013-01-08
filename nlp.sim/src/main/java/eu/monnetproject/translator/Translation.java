package eu.monnetproject.translator;

import eu.monnetproject.lang.Language;

import java.util.Collection;

/**
 * Represents the result of a translation service
 *
 * @author John McCrae
 */
@Deprecated
public interface Translation {

    /**
     * Get the source of this result.
     * @return The set of sources of this result
     */
    Collection<TranslationSource> getSources();

    /**
     * Get the language that this result is in.
     * @return The language
     */
    Language getLanguage();

    /**
     * Get the source label used to produce this translation
     * @return The source label, or null if no source label was used
     */
    String getSourceLabel();
    /**
     * Get the language of the source label language
     * @return The source label langauge, or null if no source label exists.
     */
    Language getSourceLanguage();

	/**
 	 * Get the translated label
 	 */
    String getLabel();
}
