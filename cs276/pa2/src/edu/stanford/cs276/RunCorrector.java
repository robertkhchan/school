package edu.stanford.cs276;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Set;

public class RunCorrector {

	public static LanguageModel languageModel;
	public static NoisyChannelModel nsm;
	
	private static final double u = 0.5;
	

	public static void main(String[] args) throws Exception {
		
		long startTime = System.currentTimeMillis();
		
		// Parse input arguments
		String uniformOrEmpirical = null;
		String queryFilePath = null;
		String goldFilePath = null;
		String extra = null;
		BufferedReader goldFileReader = null;
		if (args.length == 2) {
			// Run without extra and comparing to gold
			uniformOrEmpirical = args[0];
			queryFilePath = args[1];
		}
		else if (args.length == 3) {
			uniformOrEmpirical = args[0];
			queryFilePath = args[1];
			if (args[2].equals("extra")) {
				extra = args[2];
			} else {
				goldFilePath = args[2];
			}
		} 
		else if (args.length == 4) {
			uniformOrEmpirical = args[0];
			queryFilePath = args[1];
			extra = args[2];
			goldFilePath = args[3];
		}
		else {
			System.err.println(
					"Invalid arguments.  Argument count must be 2, 3 or 4" +
					"./runcorrector <uniform | empirical> <query file> \n" + 
					"./runcorrector <uniform | empirical> <query file> <gold file> \n" +
					"./runcorrector <uniform | empirical> <query file> <extra> \n" +
					"./runcorrector <uniform | empirical> <query file> <extra> <gold file> \n" +
					"SAMPLE: ./runcorrector empirical data/queries.txt \n" +
					"SAMPLE: ./runcorrector empirical data/queries.txt data/gold.txt \n" +
					"SAMPLE: ./runcorrector empirical data/queries.txt extra \n" +
					"SAMPLE: ./runcorrector empirical data/queries.txt extra data/gold.txt \n");
			return;
		}
		
		if (goldFilePath != null ){
			goldFileReader = new BufferedReader(new FileReader(new File(goldFilePath)));
		}
				
		// Load models from disk
		CandidateGenerator candidateGenerator = CandidateGenerator.get();
		BufferedReader queriesFileReader = new BufferedReader(new FileReader(new File(queryFilePath)));
		if (!queryFilePath.contains("google")) {
			languageModel = LanguageModel.load(); 
			nsm = NoisyChannelModel.load();
			nsm.setProbabilityType(uniformOrEmpirical);
			if ("extra".equals(extra)) {
				candidateGenerator.setDictionary("data/US.dic");
			} else {
				candidateGenerator.setDictionary(languageModel.unigram);
			}
		}
		
		int totalCount = 0;
		int yourCorrectCount = 0;
		String query = null;
		
		/*
		 * Each line in the file represents one query.  We loop over each query and find
		 * the most likely correction
		 */
		
		while ((query = queriesFileReader.readLine()) != null) {
			
			/*
			 * Your code here
			 */
			String correctedQuery = query;
			if (!queryFilePath.contains("google")) {
			
				//System.out.println(String.format("Query: %s", query));
				double highestScore = 0;
				Set<String> candidates = candidateGenerator.getCandidates(query);
				for (String candidate : candidates) {
					double score = calculateScore(query, candidate);
					//System.out.println(String.format("\t Candidate: %s, Score: %5.10f", candidate, score));
					if (score > highestScore) {
						highestScore = score;
						correctedQuery = candidate;
					}
				}
			}
			
			
			
			
			if ("extra".equals(extra)) {
				/*
				 * If you are going to implement something regarding to running the corrector, 
				 * you can add code here. Feel free to move this code block to wherever 
				 * you think is appropriate. But make sure if you add "extra" parameter, 
				 * it will run code for your extra credit and it will run you basic 
				 * implementations without the "extra" parameter.
				 */	
			}
			

			// If a gold file was provided, compare our correction to the gold correction
			// and output the running accuracy
			if (goldFileReader != null) {
				String goldQuery = goldFileReader.readLine();
				if (goldQuery.equals(correctedQuery)) {
					yourCorrectCount++;
				}
				totalCount++;
			}
			System.out.println(correctedQuery);
		}
		queriesFileReader.close();
		long endTime   = System.currentTimeMillis();
		long totalTime = endTime - startTime;
		// System.out.println("RUNNING TIME: "+totalTime/1000+" seconds ");

		//System.out.println((double)yourCorrectCount / (double) totalCount);
	}


	private static double calculateScore(String query, String candidateQuery) {
		double errorModelValue = nsm.getScore(query, candidateQuery);
		double languageModelValue = languageModel.P(candidateQuery);
		double score = errorModelValue * Math.pow(languageModelValue, u);
		//System.out.println(String.format("\t score=%5.15f, errorModel=%5.15f, languageModel=%5.15f, u=%1.1f", score, errorModelValue, languageModelValue, u));
		return score;
	}
}
