package eu.monnetproject.stl.t;

import eu.monnetproject.lang.Script;
import eu.monnetproject.framework.services.Services;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import eu.monnetproject.tokenizer.Tokenizer;
import eu.monnetproject.tokenizer.TokenizerFactory;

public class TermAnalyzer {

	private final String CHARACTER_PATTERN = "(.*)";
	private final int MAXIMUM_NUMBER_OF_TERMS_IN_SET = 3;
	private HashSet<List<String>> orderedTermLists;

	Tokenizer tokenzier = Services.get(TokenizerFactory.class).getTokenizer(Script.LATIN);
	Set<String> termbase = new HashSet<String>();

	public TermAnalyzer() {
		//initOrderedTermLists(null);
	}

	public TermAnalyzer(Set<String> termbase) {
		super();
		this.termbase = termbase;
		//initOrderedTermLists(null);
	}

	public TermAnalyzer(Tokenizer tokenzier,Set<String> termbase) {
		super();
		this.tokenzier = tokenzier;
		this.termbase = termbase;
		//initOrderedTermLists(null);
	}

	private void initOrderedTermLists(String mainTermAsFilter) {
		// filter out term which are not in the main term
		Set<String> terms = termbase;
		if (mainTermAsFilter!=null) {
			terms = new HashSet<String>();
			for(String term:termbase) {
				//System.out.println("t->"+term+"  "+mainTermAsFilter);
				if (mainTermAsFilter.matches(".*\\b"+term+"\\b.*"))
					terms.add(term);
			}
		}
		// compute permuatations of all powersets
		this.orderedTermLists = new HashSet<List<String>>();
		for(Set<String> set:powerSet(terms)) {
			Permutations<String> permutations = new Permutations<String>(new ArrayList<String>(set));
			while(permutations.hasNext())
				orderedTermLists.add(permutations.next());
		}
		//		System.out.println(orderedTermLists.size());
	}

	public void addTerm(String term) {
		this.termbase.add(term);
	}

	public String termlist2Pattern(ArrayList<String> termlist) {
		String pattern = "";
		for(String term:termlist)
			if (pattern.isEmpty())
				pattern=term;
			else
				pattern=pattern+CHARACTER_PATTERN+term;
		return "^"+CHARACTER_PATTERN+pattern+CHARACTER_PATTERN+"$";
	}

	public <T> Set<Set<T>> powerSet(Set<T> originalSet) {
		Set<Set<T>> sets = new HashSet<Set<T>>();
		if (originalSet.isEmpty()) {
			sets.add(new HashSet<T>());
			return sets;
		}
		List<T> list = new ArrayList<T>(originalSet);
		T head = list.get(0);
		Set<T> rest = new HashSet<T>(list.subList(1, list.size())); 
		for (Set<T> set : powerSet(rest)) {
			Set<T> newSet = new HashSet<T>();
			newSet.add(head);
			newSet.addAll(set);
			if (newSet.size()<(MAXIMUM_NUMBER_OF_TERMS_IN_SET+1))
				sets.add(newSet);
			if (set.size()<(MAXIMUM_NUMBER_OF_TERMS_IN_SET+1))
				sets.add(set);
		}           
		return sets;
	}

	public List<String> analyzeTerm(String term) {

		//lower case
		term=term.toLowerCase();

		// init ordered term lists
		initOrderedTermLists(term);
		Iterator<List<String>> it = orderedTermLists.iterator();
		List<String> bestMatch = new ArrayList<String>();

		// iterate over powerset and compute minimum distance
		int min = tokenzier.tokenize(term).size();
		while(it.hasNext()) {

			// iterate over all combinations
			List<String> matchedTerms = it.next();

			// number of groups
			int ngroups = matchedTerms.size()+2;

			// compute matchintg pattern from term list
			String termPattern = termlist2Pattern(new ArrayList<String>(matchedTerms));
			String P = termPattern;
			Matcher m = Pattern.compile(P).matcher(term);
			boolean matched = false;
			int numberOfUnmatchedTokensTotal = 0;
			while(m.find()) {
				matched = true;
				for (int i = 1; i < ngroups; i++) {
					int numberOfUnMatchedTokens = tokenzier.tokenize(m.group(i).trim()).size();
					numberOfUnmatchedTokensTotal = numberOfUnmatchedTokensTotal + numberOfUnMatchedTokens;
				}
			}
			if (matched) {
				if (min>numberOfUnmatchedTokensTotal) {
					min = numberOfUnmatchedTokensTotal;
					bestMatch=matchedTerms;
				}
			}
		}

		return bestMatch;
	}

	public static void main(String[] args) {

		TermAnalyzer termAnalyzer = new TermAnalyzer();
		termAnalyzer.addTerm("minimum");
		termAnalyzer.addTerm("lease payments");
		termAnalyzer.addTerm("finance lease payments");
		//termAnalyzer.addTerm("receivable");
		termAnalyzer.addTerm("end of period");
		List<String> bestMatch = termAnalyzer.analyzeTerm("minimum finance lease payments receivable end of period");
		System.out.println(bestMatch);

	}

}
