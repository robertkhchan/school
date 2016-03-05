package edu.stanford.cs276;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import edu.stanford.cs276.util.Dictionary;

public class CandidateGenerator implements Serializable {


	private static final long serialVersionUID = 5463066132240763774L;
	
	private static CandidateGenerator cg_;
	
	private final static String space = " ";
	
	private Dictionary dictionary;
	
	// Don't use the constructor since this is a Singleton instance
	private CandidateGenerator() {
	}
	
	public static CandidateGenerator get() throws Exception{
		if (cg_ == null ){
			cg_ = new CandidateGenerator();			
		}
		return cg_;
	}
	
	
	public static final Character[] alphabet = {
					'a','b','c','d','e','f','g','h','i','j','k','l','m','n',
					'o','p','q','r','s','t','u','v','w','x','y','z',
					'0','1','2','3','4','5','6','7','8','9',
					' ',','};
	
	public void setDictionary(Dictionary dictionary) {
		System.out.println("Using dictionary from language model.");
		this.dictionary = dictionary;
	}
	
	public void setDictionary(String dictionaryFilePath) throws IOException {
		System.out.println("Using local dictionary.");
		this.dictionary = new Dictionary();
		
		BufferedReader fileReader = new BufferedReader(new FileReader(new File(dictionaryFilePath)));
		String word = null;
		while ((word = fileReader.readLine()) != null) {
			dictionary.add(word.trim());
		}
		fileReader.close();
	}
			

	boolean isInDictionary(String token) {
		return dictionary.count(token) > 0;
	}
		
	// Generate all candidates for the target query
	public Set<String> getCandidates(String query) throws Exception {
		
		// cartesian product of every variation of almost every token in the query
		Set<String> candidates = getCartesianProductCandidates(query);
		
		// handling of merging of neighbor tokens
		for (int i=0; i<query.length(); i++) {
			if (query.charAt(i) == ' ') {
				String newQuery = query.substring(0,i) + query.substring(i+1);
				candidates.addAll(getCartesianProductCandidates(newQuery));
			}
		}

		// handling of single word change candidates
		// Commenting out below step because it does not produce good results
		candidates.addAll(getSingleWordChangeCandidates(query));
			
		
		return candidates;
	}

	Set<String> getSingleWordChangeCandidates(String query) {
		Set<String> newCandidates = new HashSet<String>();
		int startIndex=0;
		int endIndex = query.indexOf(' ');
		while(true) {
			Set<String> newTokens = generateCandidates(query.substring(startIndex, endIndex), 2);
			for (String newToken : newTokens) {
				String newQuery = query.substring(0, startIndex) + newToken + query.substring(endIndex);
				newCandidates.add(newQuery);
			}
			
			if (endIndex == query.length()) {
				break;
			} else {
				startIndex = endIndex+1;
				endIndex = query.indexOf(' ', startIndex);
				if (endIndex == -1) {
					endIndex = query.length();
				}
			}
		}
		return newCandidates;
	}
	
	public Set<String> getCartesianProductCandidates(String query) {
		Set<String> lastSet = null;
		Set<String> currentSet = null;
		String[] words = query.split(space);
		for (int i=0; i<words.length; i++) {
			if (i==0) {
				if (isInDictionary(words[i])) {
					lastSet = new HashSet<String>();
					lastSet.add(words[i]);
				} else {
					lastSet = generateCandidates(words[i], 2);
				}				
			} else {
				if (isInDictionary(words[i])) {
					currentSet = new HashSet<String>();
					currentSet.add(words[i]);
				} else {
					currentSet = generateCandidates(words[i], 2);
				}
				lastSet = CartesianProduct(lastSet, currentSet);
			}
		}
		
		return lastSet;
	}
	
	
	/**
	 * Code referenced from http://raelcunha.com/spell-correct.php
	 *  
	 * @param token
	 * @param distance TODO
	 * @return
	 */
	private Set<String> generateCandidates(String token, int distance) {
		Set<String> result = new HashSet<String>();
		
		--distance;
				
		// insertion
		for(int i=0; i < token.length(); ++i) {
			String newToken = token.substring(0, i) + token.substring(i+1);
			if (isInDictionary(newToken)) {
				result.add(newToken);
			}
		}
		// transposition
		for(int i=0; i < token.length()-1; ++i) {
			String newToken = token.substring(0, i) + token.substring(i+1, i+2) + token.substring(i, i+1) + token.substring(i+2);
			if (isInDictionary(newToken)) {
				result.add(newToken);
			}
		}
		// replacement
		for(int i=0; i < token.length(); ++i) {
			for(char c='a'; c <= 'z'; ++c) {
				String newToken = token.substring(0, i) + String.valueOf(c) + token.substring(i+1);
				if (isInDictionary(newToken)) {
					result.add(newToken);
				}
			}
		}
		// deletion
		for(int i=0; i <= token.length(); ++i) {
			for(char c='a'; c <= 'z'; ++c) {
				String newToken = token.substring(0, i) + String.valueOf(c) + token.substring(i);
				if (isInDictionary(newToken)) {
					result.add(newToken);
				}
			}
		}
		
		// split
		for(int i=0; i <= token.length(); ++i) {
			String firstString = token.substring(0,i);
			String secondString = token.substring(i);
			if (isInDictionary(firstString) && isInDictionary(secondString)) {
				result.add(firstString + space + secondString);
			}
		}
				
		return result;
	}

	
	private Set<String> CartesianProduct(Set<String> stringSet1, Set<String> stringSet2) {
		Set<String> result = new HashSet<String>();
		for (String string1 : stringSet1) {
			for (String string2 : stringSet2) {
				result.add(string1 + space + string2);
			}
		}
		return result;
	}
	
}
