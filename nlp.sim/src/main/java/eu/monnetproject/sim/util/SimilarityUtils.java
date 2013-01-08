package eu.monnetproject.sim.util;

import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import eu.monnetproject.util.Logger;

import eu.monnetproject.label.LabelExtractor;
import eu.monnetproject.lang.Language;
import eu.monnetproject.ontology.AnnotationProperty;
import eu.monnetproject.ontology.AnnotationValue;
import eu.monnetproject.ontology.Entity;
import eu.monnetproject.ontology.Individual;
import eu.monnetproject.ontology.LiteralOrIndividual;
import eu.monnetproject.ontology.LiteralValue;
import eu.monnetproject.ontology.Ontology;
import eu.monnetproject.ontology.Property;
import eu.monnetproject.tokenizer.Token;
import eu.monnetproject.translator.Translation;
import eu.monnetproject.translator.Translator;
import eu.monnetproject.util.Logging;

public class SimilarityUtils {

    private static Logger log = Logging.getLogger(SimilarityUtils.class);
    private static volatile HashMap<URI,Map<Language,Collection<String>>> labelCache = new HashMap<URI,Map<Language,Collection<String>>>(5000);
    private static volatile Map<URI,Map<String,Set<LiteralOrIndividual>>> propertyPathValues = new HashMap<URI,Map<String,Set<LiteralOrIndividual>>>(5000);
    private static volatile Map<String,Collection<Collection<Entity>>> propertyCache = new HashMap<String,Collection<Collection<Entity>>>(5000);
    private static Collection<Language> languages = new HashSet<Language>();
    
	public static Collection<URI> determineLabelProperties(Entity srcEntity,
			Entity tgtEntity) {
		
		//log.info("Trying to determine label properties.");

		Set<URI> uris = new HashSet<URI>();

		for(AnnotationProperty prop : srcEntity.getOntology().getAnnotationProperties()) {
			if (prop.getURI().toString().toLowerCase().matches(".*label.*"))
				uris.add(prop.getURI());
		}

		for(AnnotationProperty prop : tgtEntity.getOntology().getAnnotationProperties()) {
			if (prop.getURI().toString().toLowerCase().matches(".*label.*"))
				uris.add(prop.getURI());
		}
		
		//log.info(uris.size()+" URIs: "+uris.iterator().next().toString());

		return (Collection<URI>)uris;
		
	}
	
	public static Collection<Language> determineLanguages(Entity srcEntity,
			Entity tgtEntity) {
		
		if (languages.size() >= 1)
			return languages;
		
		log.info("Trying to determine languages.");

		Set<Language> srcLangs = getLanguagesInOntology(srcEntity.getOntology());
		Set<Language> tgtLangs = getLanguagesInOntology(tgtEntity.getOntology());
				
		languages = srcLangs;
		
		languages.retainAll(tgtLangs);
		
		if (languages.size() < 1) {
			log.warning("Ontologies don't share any languages. Using languages of source ontology.");
			languages = srcLangs;
		}
		
		if (languages.size() < 1) {
			log.warning("Source ontology has no languages. Using languages of target ontology.");
			languages = tgtLangs;
		}

		return languages;
		
	}

	private static Set<Language> getLanguagesInOntology(Ontology ontology) {
		Set<Language> langs = new HashSet<Language>();

		for(Entity ent : ontology.getEntities()) {
			
			Map<AnnotationProperty,Collection<AnnotationValue>> map = ent.getAnnotations();
			
			for (AnnotationProperty prop : map.keySet()) {
				
				for (AnnotationValue anno : map.get(prop)) {
					
					if (anno instanceof LiteralValue) {
						langs.add(((LiteralValue)anno).getLanguage());
					}
					
				}
				
			}

		}
		
		return langs;
	}

	public static Collection<Language> getLanguages(String languages) {

		Collection<Language> rv = new HashSet<Language>();

		if (languages.equals(""))
			return Collections.emptySet();
		
		for (String lang : languages.split(",")) {
			rv.add(Language.get(lang));
		}
		
		return rv;
		
	}

	public static Map<Language, Collection<String>> getLabelsIncludingPuns(
			Entity entity, LabelExtractor lex) {
		
		Map<Language,Collection<String>> labels = labelCache.get(entity.getURI());
		
		if (labels != null && labels.size() >= 1) {
			return labels;
		}
		
		labels = new HashMap<Language,Collection<String>>();
		
		Collection<Entity> entities = entity.getOntology().getEntities(entity.getURI());
		
		for (Entity pun : entities) {
		  
			Map<Language,Collection<String>> punLabels = lex.getLabels(pun);

			for (Language lang : punLabels.keySet()) {
				
				try {
				
					lang.getLanguageOnly();
					
				} catch (Exception e) {
					continue;
				}
				
				if (!labels.containsKey(lang.getLanguageOnly()))
					labels.put(lang.getLanguageOnly(), new HashSet<String>());
				labels.get(lang.getLanguageOnly()).addAll(punLabels.get(lang));
			}
			
			
		}
		
		labelCache.put(entity.getURI(), labels);

		return labels;

	}

	public static Map<Language, Collection<String>> getLabelsExcludingPuns(
			Entity entity, LabelExtractor lex) {
		
		Map<Language,Collection<String>> labels = labelCache.get(entity.getURI());
		
		if (labels != null) {
			return labels;
		}
		
		labels = lex.getLabels(entity);
		
		Map<Language,Collection<String>> newLabels = new HashMap<Language,Collection<String>>();

		for (Language lang : labels.keySet()) {
			
			try {
				
				lang.getLanguageOnly();
				
			} catch (Exception e) {
				continue;
			}
			
			if (!newLabels.containsKey(lang.getLanguageOnly()))
				newLabels.put(lang.getLanguageOnly(), new HashSet<String>());
			labels.get(lang.getLanguageOnly()).addAll(newLabels.get(lang.getLanguageOnly()));
		}

		
		labelCache.put(entity.getURI(), labels);

		return labels;

	}

	public static boolean getIncludePuns(String includePuns) {
		if (includePuns.equals("true"))
			return true;
		return false;
	}

	public static Collection<String> getTranslatedLabels(Entity srcEntity,
			Language tgtLanguage, Translator translator, LabelExtractor lex) {

		Map<Language,Collection<String>> map = getLabelsIncludingPuns(srcEntity,lex);
		
		Map<Language,Collection<String>> newMap = new HashMap<Language,Collection<String>>();
		Collection<String> newLabels = new HashSet<String>();

		for (Language srcLanguage : map.keySet()) {
			if (srcLanguage.toString().matches(".*null.*")) {
				continue;
			}
			
			newMap.put(srcLanguage, new HashSet<String>());
			newMap.get(srcLanguage).addAll(map.get(srcLanguage));
			
			for (String label : map.get(srcLanguage)) {
				Collection<Translation> translations = translator.translate(label, srcLanguage, tgtLanguage);
				for (Translation translation : translations) {
					log.info("Translation of "+label+" is "+translation.getLabel());
					if (translation.getLabel() != null)
						newLabels.add(translation.getLabel());
					else
						log.warning("Null translation");
				}
			}
		}
		
		newMap.put(tgtLanguage, newLabels);
		labelCache.put(srcEntity.getURI(), newMap);
		
		return newLabels;
	}
	
	public static String sortTokens(List<Token> tokens) {
		
		List<String> strings = new LinkedList<String>();
		
		for (Token token: tokens) {			
			strings.add(token.getValue());			
		}
		
		Collections.sort(strings, String.CASE_INSENSITIVE_ORDER);
		
		String returnString = "";
		
		for (String string : strings) {
			returnString += string+" ";
		}
		
		return returnString.trim();
		
	}
	
	public static String removePunctuation(String string) {
		
		return string.replaceAll("(,|\\.|;|\"|\'|:|!|\\?|-|\\(|\\)|\\[|\\]|\\{|\\})", "");
		
	}
	
	public static Collection<Collection<Entity>> getPresentations(Entity ent, boolean transitive) {

		return getValueAggregation(ent, transitive, URI.create("http://www.monnetproject.eu/ontologies/xbrl/xbrl.owl#presentation"));
		
	}

	public static Collection<Collection<Entity>> getCalculations(Entity ent, boolean transitive) {

		return getValueAggregation(ent, transitive, URI.create("http://www.monnetproject.eu/ontologies/xbrl/xbrl.owl#calculation"));
		
	}

	private static Collection<Collection<Entity>> getValueAggregation(Entity ent, boolean transitive, URI propURI) {

		String propCacheKey = ent.getURI().toString()+transitive+propURI.toString();
		
		if (propertyCache.containsKey(propCacheKey))
			return propertyCache.get(propCacheKey);
		
		Collection<Collection<Entity>> aggregations = new LinkedList<Collection<Entity>>();

		Collection<LiteralOrIndividual> propVals = getPropertyPathValues(ent, 
				Arrays.asList((Property)ent.getOntology().getFactory().makeObjectProperty(propURI)));

		for (LiteralOrIndividual litOrInd1 : propVals) {

			Individual ind1 = null;

			if (litOrInd1 instanceof Individual)
				ind1 = (Individual)litOrInd1;

			if (ind1 != null) {

				Collection<LiteralOrIndividual> roleRefs1 = ind1.getPropertyValues(ent.getOntology().getFactory().makeObjectProperty(URI.create("http://www.monnetproject.eu/ontologies/xbrl/xbrl.owl#roleRef")));

				for (LiteralOrIndividual roleRef : roleRefs1) {

					Individual roleRefInd = null;

					if (roleRef instanceof Individual)
						roleRefInd = (Individual)roleRef;

					Collection<Entity> items = getItems(ind1,roleRefInd, transitive, propURI);
					if (items.size() > 0) {
						aggregations.add(items);
					}

				}
			}
		}
		
		propertyCache.put(propCacheKey, aggregations);

		return aggregations;

	}

	private static Collection<Entity> getItems(Individual aggregate, Individual roleRef, boolean transitive, URI propURI) {

		Set<Entity> items = new HashSet<Entity>();

		for (LiteralOrIndividual item : getPropertyPathValues(aggregate, 
				Arrays.asList((Property)aggregate.getOntology().getFactory().makeObjectProperty(URI.create("http://www.monnetproject.eu/ontologies/xbrl/xbrl.owl#arc")),
						(Property)aggregate.getOntology().getFactory().makeObjectProperty(URI.create("http://www.w3.org/1999/xlink/to"))))) {

			if (!transitive) {
				items.add((Entity)item);
			} else {

				Set<LiteralOrIndividual> aggregationsOfItem = getPropertyPathValues((Entity)item, 
						Arrays.asList((Property)aggregate.getOntology().getFactory().makeObjectProperty(propURI)));

				boolean added = false;

				if (aggregationsOfItem.size() == 0) {

					for (Entity ent : aggregate.getOntology().getEntities(((Entity)item).getURI())) {
						items.add(ent);
						added = true;
						break;
					}

				}

				for (LiteralOrIndividual aggregationOfItem : aggregationsOfItem) {

					Individual aggrOfItemInd = null;

					if (aggregationOfItem instanceof Individual)
						aggrOfItemInd = (Individual)aggregationOfItem;

					if (aggrOfItemInd.getPropertyValues(aggregate.getOntology().getFactory().makeObjectProperty(URI.create("http://www.monnetproject.eu/ontologies/xbrl/xbrl.owl#roleRef"))).contains(roleRef)) {
						added = true;
						items.addAll(getItems(aggrOfItemInd,roleRef,transitive,propURI));
					}

				}

				if (!added) {
					for (Entity ent : aggregate.getOntology().getEntities(((Entity)item).getURI())) {
						items.add(ent);
						break;
					}
				}
			}
		}

		return items;

	}

	private static Set<LiteralOrIndividual> getPropertyPathValues(Entity ent, List<Property> propertyPath) {
		
		String propertyPathKey = "";
		
		for (Property prop : propertyPath) {
			propertyPathKey += prop.getURI().toString();
		}
		
		if (propertyPathValues.containsKey(ent.getURI()) && propertyPathValues.get(ent.getURI()).containsKey(propertyPathKey)) {
			return propertyPathValues.get(ent.getURI()).get(propertyPathKey);
		}

		Iterator<Property> propIterator = propertyPath.iterator();

		Individual ind = null;
		
		for (Entity entity : ent.getOntology().getEntities(ent.getURI())) {
			if (entity instanceof Individual) {
				ind = (Individual)entity;
				break;
			}
		}

		if (ind == null) {
			Set<LiteralOrIndividual> returnSet = Collections.emptySet();
			return returnSet;
		}

		Set<LiteralOrIndividual> vals = new HashSet<LiteralOrIndividual>();
		vals.add(ind);

		while (propIterator.hasNext()) {

			Property thisProp = propIterator.next();
			Set<LiteralOrIndividual> thisVals = new HashSet<LiteralOrIndividual>();

			for (LiteralOrIndividual val : vals) {
				if (!(((Individual)val).getPropertyValues(thisProp) == null)) {
					thisVals.addAll(((Individual)val).getPropertyValues(thisProp));
				}
			}

			vals = thisVals;

			if (!propIterator.hasNext())
				break;
		}
		
		if (!propertyPathValues.containsKey(ent.getURI()))
			propertyPathValues.put(ent.getURI(), new HashMap<String,Set<LiteralOrIndividual>>());
		
		propertyPathValues.get(ent.getURI()).put(propertyPathKey, vals);
		
		return vals;

	}

	public static double getRatio(Collection<Entity> collection, eu.monnetproject.ontology.Class cls1, eu.monnetproject.ontology.Class cls2) {
		
		int cls1Cnt = 0;
		int cls2Cnt = 0;
		
		for (Entity item : collection) {
			
			eu.monnetproject.ontology.Class cls = null;
			
			for (Entity pun : item.getOntology().getEntities(item.getURI())) {
				if (pun instanceof eu.monnetproject.ontology.Class) {
					cls = (eu.monnetproject.ontology.Class)pun;
					break;
				}
			}
			
			if (cls == null)
				return 0.;
			
			if (cls.getSuperClassOf().size() < 1) {
				log.warning(cls.getURI().toString()+" has no superclasses.");
			}

			if (cls.getSuperClassOf().contains(cls1)) {
				cls1Cnt++;
			} else if (cls.getSuperClassOf().contains(cls2)) {
				cls2Cnt++;
			}
			
		}
		
		if (cls1Cnt == 0 && cls2Cnt == 0)
			return 0.;
		
		if (cls2Cnt == 0)
			return 1.;
		
		double score = new Double(cls1Cnt) / new Double(cls1Cnt + cls2Cnt);
		
		return score;

	}

}
