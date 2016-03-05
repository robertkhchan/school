package cs276.assignments;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import cs276.util.Pair;

public class Query {

	// Term id -> position in index file
	private static Map<Integer, Long> posDict = new TreeMap<Integer, Long>();
	// Term id -> document frequency
	private static Map<Integer, Integer> freqDict = new TreeMap<Integer, Integer>();
	// Doc id -> doc name dictionary
	private static Map<Integer, String> docDict = new TreeMap<Integer, String>();
	// Term -> term id dictionary
	private static Map<String, Integer> termDict = new TreeMap<String, Integer>();
	// Index
	private static BaseIndex index = null;

	
	/* 
	 * Write a posting list with a given termID from the file 
	 * You should seek to the file position of this specific
	 * posting list and read it back.
	 * */
	private static PostingList readPosting(FileChannel fc, int termId)
			throws IOException {
		Long position = posDict.get(termId);
		fc.position(position);
		return index.readPosting(fc);
	}

	public static void main(String[] args) throws IOException {
		/* Parse command line */
		if (args.length != 2) {
			System.err.println("Usage: java Query [Basic|VB|Gamma] index_dir");
			return;
		}

		/* Get index */
		String className = "cs276.assignments." + args[0] + "Index";
		try {
			Class<?> indexClass = Class.forName(className);
			index = (BaseIndex) indexClass.newInstance();
		} catch (Exception e) {
			System.err
					.println("Index method must be \"Basic\", \"VB\", or \"Gamma\"");
			throw new RuntimeException(e);
		}

		/* Get index directory */
		String input = args[1];
		File inputdir = new File(input);
		if (!inputdir.exists() || !inputdir.isDirectory()) {
			System.err.println("Invalid index directory: " + input);
			return;
		}

		/* Index file */
		RandomAccessFile indexFile = new RandomAccessFile(new File(input,
				"corpus.index"), "r");

		String line = null;
		/* Term dictionary */
		BufferedReader termReader = new BufferedReader(new FileReader(new File(
				input, "term.dict")));
		while ((line = termReader.readLine()) != null) {
			String[] tokens = line.split("\t");
			termDict.put(tokens[0], Integer.parseInt(tokens[1]));
		}
		termReader.close();

		/* Doc dictionary */
		BufferedReader docReader = new BufferedReader(new FileReader(new File(
				input, "doc.dict")));
		while ((line = docReader.readLine()) != null) {
			String[] tokens = line.split("\t");
			docDict.put(Integer.parseInt(tokens[1]), tokens[0]);
		}
		docReader.close();

		/* Posting dictionary */
		BufferedReader postReader = new BufferedReader(new FileReader(new File(
				input, "posting.dict")));
		while ((line = postReader.readLine()) != null) {
			String[] tokens = line.split("\t");
			posDict.put(Integer.parseInt(tokens[0]), Long.parseLong(tokens[1]));
			freqDict.put(Integer.parseInt(tokens[0]),
					Integer.parseInt(tokens[2]));
		}
		postReader.close();

		/* Processing queries */
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

		/* For each query */
		List<Pair<Integer,Integer>> freqQueryTerms = new ArrayList<Pair<Integer,Integer>>();
		while ((line = br.readLine()) != null) {
			/*
			 * Your code here
			 */
			String[] tokens = line.split(" ");
			for (String token : tokens) {
				if (termDict.containsKey(token)) {
					freqQueryTerms.add(new Pair<Integer, Integer>(termDict.get(token), freqDict.get(termDict.get(token))));
				} else {
					freqQueryTerms.clear();
					break;
				}
			}
			
		}
		br.close();
		
		// Sort by postings length
		Collections.sort(freqQueryTerms, new Comparator<Pair<Integer,Integer>>() {
			@Override
			public int compare(Pair<Integer, Integer> arg0, Pair<Integer, Integer> arg1) {
				if (arg0.getSecond() < arg1.getSecond()) return -1;
				else if (arg0.getSecond().equals(arg1.getSecond())) return 0;
				else return 1;
			}
		});
		
		// Merge posting lists
		List<Integer> mergedList = new ArrayList<Integer>();
		for (Pair<Integer, Integer> queryTerm : freqQueryTerms) {
			PostingList next = readPosting(indexFile.getChannel(), queryTerm.getFirst());
			if (mergedList.isEmpty()) {
				mergedList.addAll(next.getList());
			} else {
				mergedList = mergeList(mergedList, next.getList());
			}
		}
		
		indexFile.close();
		
		if (mergedList.isEmpty()) {
			System.out.println("no results found");
		} else {
			// Sort documents lexicographically
			Set<String> docs = new TreeSet<String>();
			for (Integer docId : mergedList) {
				docs.add(docDict.get(docId));
			}
			for (String doc : docs) {
				System.out.println(doc);
			}
		}
	}

	static List<Integer> mergeList(List<Integer> list1, List<Integer> list2) {
		List<Integer> result = new ArrayList<Integer>();
		Iterator<Integer> iter1 = list1.iterator();
		Iterator<Integer> iter2 = list2.iterator();
		if (iter1.hasNext() && iter2.hasNext()) {
			Integer docId1 = iter1.next();
			Integer docId2 = iter2.next();
			
			while (true) {
				if (docId1.equals(docId2)) {
					result.add(docId1);
					if (iter1.hasNext() && iter2.hasNext()) {
						docId1 = iter1.next();
						docId2 = iter2.next();
					} else {
						break;
					}
				} else if (docId1 < docId2) {
					if (iter1.hasNext()) {
						docId1 = iter1.next();
					} else {
						break;
					}
				} else {
					if (iter2.hasNext()) {
						docId2 = iter2.next();
					} else {
						break;
					}
				}
			}
		}		
		return result;
	}
}
