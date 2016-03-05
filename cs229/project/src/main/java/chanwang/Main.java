package chanwang;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.stream.Stream;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.CoreMap;

public class Main {

  public static void main(String[] args) throws IOException {
	  
	boolean isTraining = !("test".equalsIgnoreCase(args[0]));

    Properties props = new Properties();
    props.put("annotators", "tokenize, ssplit, pos, lemma, ner, parse, dcoref, sentiment");
    props.put("tokenize.whitespace", "true");
    StanfordCoreNLP pipeline = new StanfordCoreNLP(props); 
    
    double[] scores = new double[2];
    LineNumberReader  lnr = new LineNumberReader(new FileReader(new File(args[1])));
	lnr.skip(Long.MAX_VALUE);
	int[][] predictions = new int[(int) lnr.getLineNumber()][2];
	lnr.close();
    int[] index = new int[1];
    index[0] = 0;
	    
	Path path = FileSystems.getDefault().getPath("", args[1]);	
	try (Stream<String> lines = Files.lines(path)) {	    
		lines.skip(1).forEach(content -> {			
			String[] tokens = content.split("\t");
	
 		    Annotation annotation = pipeline.process(tokens[2]);
 		    
    		int mainSentiment = 0;
 		    int longest = 0;
 		    for (CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class)) {
 		        Tree tree = sentence
 		                .get(SentimentCoreAnnotations.AnnotatedTree.class);
 		        int sentiment = RNNCoreAnnotations.getPredictedClass(tree);
 		        String partText = sentence.toString();
 		        if (partText.length() > longest) {
 		            mainSentiment = sentiment;
 		            longest = partText.length();
 		        }
 		    }
 		    
 		    if (isTraining) {
	 		    if (mainSentiment == Integer.parseInt(tokens[3]))
	 		    	scores[0]++;
	 		    else
	 		    	scores[1]++;
 		    } else {
 		    	predictions[index[0]][0] = Integer.parseInt(tokens[0]);
 		    	predictions[index[0]][1] = mainSentiment;
 		    	index[0]++;
 		    }
 		    
		});
	}
	
	
	if (isTraining) {
		double accuracy = (scores[0]/(scores[0]+scores[1]))*100;
		System.out.println("Correct=" + scores[0] + ", Incorrect=" + scores[1] + ", Accuracy=" + String.valueOf(accuracy));
	} else {		
		BufferedWriter writer = new BufferedWriter(new FileWriter(args[2]));
		for (int[] prediction : predictions) {
			String content = prediction[0] + "," + prediction[1]+"\n";
			writer.write(content);
		}
		writer.close();
	}
			

    
  }

}
