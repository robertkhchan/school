package edu.stanford.cs276;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import edu.stanford.cs276.util.Dictionary;
import edu.stanford.cs276.util.Pair;


public class LanguageModel implements Serializable {

	private static final long serialVersionUID = 7728724539060419979L;

	private static LanguageModel lm_;
	/* Feel free to add more members here.
	 * You need to implement more methods here as needed.
	 * 
	 * Your code here ...
	 */
	private static final double lambda = 0.1; 
	Dictionary unigram = new Dictionary();
	Map<Pair<String,String>,Integer> bigram = new HashMap<Pair<String,String>,Integer>();
	
	// Do not call constructor directly since this is a Singleton
	private LanguageModel(String corpusFilePath) throws Exception {
		constructDictionaries(corpusFilePath);
	}
	
	public double P(String phrase) {
		String[] words = phrase.split(" ");
		double result = 1.0;
		if (words.length > 0) {
			result *= P_mle(words[0]);
			for (int i = 1; i < words.length; i++) {
				result *= P_int(words[i],words[i-1]);
			}
		}
		return result;
	}
	
	public double P_int(String w2, String w1) {
		return lambda * P_mle(w2) + (1-lambda) * P_mle(w2,w1);
	}
	
	public double P_mle(String w2, String w1) {
		Integer countW1W2 = bigram.get(new Pair<String, String>(w1,w2));
		if (countW1W2 != null) {
			return (double)countW1W2 / (double)unigram.count(w1);
		} else {
			return 0;
		}
	}
	
	public double P_mle(String w1) {
		return (double)unigram.count(w1) / (double)unigram.termCount();
	}
	
	
	
	public void constructDictionaries(String corpusFilePath)
			throws Exception {

		System.out.println("Constructing dictionaries...");
		File dir = new File(corpusFilePath);
		for (File file : dir.listFiles()) {
			if (".".equals(file.getName()) || "..".equals(file.getName())) {
				continue; // Ignore the self and parent aliases.
			}
			System.out.printf("Reading data file %s ...\n", file.getName());
			BufferedReader input = new BufferedReader(new FileReader(file));
			String line = null;
			while ((line = input.readLine()) != null) {
				/*
				 * Your code here
				 */
				String[] tokens = line.split(" ");
				String previousToken = "";
				for (String token : tokens) {
					unigram.add(token);
					if (!previousToken.equals("")) {
						Pair<String,String> pair = new Pair<String, String>(previousToken, token);
						if (bigram.containsKey(pair)) {
							bigram.put(pair, bigram.get(pair) + 1);
						} else {
							bigram.put(pair, 1);
						}
					}
					previousToken = token;
				}
			}
			input.close();
		}
		System.out.println("Done.");
	}
	
	// Loads the object (and all associated data) from disk
	public static LanguageModel load() throws Exception {
		try {
			if (lm_==null){
				FileInputStream fiA = new FileInputStream(Config.languageModelFile);
				ObjectInputStream oisA = new ObjectInputStream(fiA);
				lm_ = (LanguageModel) oisA.readObject();
				oisA.close();
			}
		} catch (Exception e){
			throw new Exception("Unable to load language model.  You may have not run build corrector");
		}
		return lm_;
	}
	
	// Saves the object (and all associated data) to disk
	public void save() throws Exception{
		FileOutputStream saveFile = new FileOutputStream(Config.languageModelFile);
		ObjectOutputStream save = new ObjectOutputStream(saveFile);
		save.writeObject(this);
		save.close();
	}
	
	// Creates a new lm object from a corpus
	public static LanguageModel create(String corpusFilePath) throws Exception {
		if(lm_ == null ){
			lm_ = new LanguageModel(corpusFilePath);
		}
		return lm_;
	}
}
