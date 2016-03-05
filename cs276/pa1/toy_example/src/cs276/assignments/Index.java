package cs276.assignments;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadPendingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import cs276.util.Pair;

public class Index {

	// Term id -> (position in index file, doc frequency) dictionary
	private static Map<Integer, Pair<Long, Integer>> postingDict 
		= new TreeMap<Integer, Pair<Long, Integer>>();
	// Doc name -> doc id dictionary
	private static Map<String, Integer> docDict
		= new TreeMap<String, Integer>();
	// Term -> term id dictionary
	private static Map<String, Integer> termDict
		= new TreeMap<String, Integer>();
	// Block queue
	private static LinkedList<File> blockQueue
		= new LinkedList<File>();

	// Total file counter
	private static int totalFileCount = 0;
	// Document counter
	private static int docIdCounter = 0;
	// Term counter
	private static int wordIdCounter = 0;
	// Index
	private static BaseIndex index = null;

	
	/* 
	 * Write a posting list to the file 
	 * You should record the file position of this posting list
	 * so that you can read it back during retrieval
	 * 
	 * */
	private static void writePosting(FileChannel fc, PostingList posting)
			throws IOException {		
		postingDict.put(posting.getTermId(), new Pair<Long,Integer>(fc.position(),posting.getList().size()));
		index.writePosting(fc, posting);
		
	}

	public static void main(String[] args) throws IOException {
		/* Parse command line */
		if (args.length != 3) {
			System.err
					.println("Usage: java Index [Basic|VB|Gamma] data_dir output_dir");
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

		/* Get root directory */
		String root = args[1];
		File rootdir = new File(root);
		if (!rootdir.exists() || !rootdir.isDirectory()) {
			System.err.println("Invalid data directory: " + root);
			return;
		}

		/* Get output directory */
		String output = args[2];
		File outdir = new File(output);
		if (outdir.exists() && !outdir.isDirectory()) {
			System.err.println("Invalid output directory: " + output);
			return;
		}

		if (!outdir.exists()) {
			if (!outdir.mkdirs()) {
				System.err.println("Create output directory failure");
				return;
			}
		}

		/* BSBI indexing algorithm */
		File[] dirlist = rootdir.listFiles();

		/* For each block */
		for (File block : dirlist) {
			File blockFile = new File(output, block.getName());
			blockQueue.add(blockFile);

			File blockDir = new File(root, block.getName());
			File[] filelist = blockDir.listFiles();
			
			/*
			 * Your code here
			 */
			Map<Integer, List<Integer>> postings = new TreeMap<Integer, List<Integer>>();
			
			/* For each file */
			for (File file : filelist) {
				++totalFileCount;
				String fileName = block.getName() + "/" + file.getName();
				docDict.put(fileName, docIdCounter++);
				
				BufferedReader reader = new BufferedReader(new FileReader(file));
				String line;
				while ((line = reader.readLine()) != null) {
					String[] tokens = line.trim().split("\\s+");
					for (String token : tokens) {
						/*
						 * Your code here
						 */
						if (!termDict.keySet().contains(token)) {
							termDict.put(token, wordIdCounter);
							postings.put(wordIdCounter, new ArrayList<Integer>());
							wordIdCounter++;
						}
						postings.get(termDict.get(token)).add(docDict.get(fileName));
					}
				}
				reader.close();
			}

			/* Sort and output */
			if (!blockFile.createNewFile()) {
				System.err.println("Create new block failure.");
				return;
			}
			
			RandomAccessFile bfc = new RandomAccessFile(blockFile, "rw");
			
			/*
			 * Your code here
			 */
			for (Map.Entry<Integer, List<Integer>> entry : postings.entrySet()) {
				writePosting(bfc.getChannel(), new PostingList(entry.getKey(), entry.getValue()));
			}
			
			bfc.close();
		}

		/* Required: output total number of files. */
		System.out.println(totalFileCount);

		/* Merge blocks */
		while (true) {
			if (blockQueue.size() <= 1)
				break;

			File b1 = blockQueue.removeFirst();
			File b2 = blockQueue.removeFirst();
			
			File combfile = new File(output, b1.getName() + "+" + b2.getName());
			if (!combfile.createNewFile()) {
				System.err.println("Create new block failure.");
				return;
			}

			RandomAccessFile bf1 = new RandomAccessFile(b1, "r");
			RandomAccessFile bf2 = new RandomAccessFile(b2, "r");
			RandomAccessFile mf = new RandomAccessFile(combfile, "rw");
			 
			/*
			 * Your code here
			 */			
			PostingList bf1Posting = index.readPosting(bf1.getChannel());
			PostingList bf2Posting = index.readPosting(bf2.getChannel());
			while (bf1Posting != null || bf2Posting != null) {
				if (bf1Posting != null && bf2Posting != null) {
					if (bf1Posting.getTermId() == bf2Posting.getTermId()) {
						List<Integer> combinedList = combinePostingList(bf1Posting.getList(), bf2Posting.getList());
						PostingList mfPosting = new PostingList(bf1Posting.getTermId(), combinedList);
						writePosting(mf.getChannel(), mfPosting);
						bf1Posting = index.readPosting(bf1.getChannel());
						bf2Posting = index.readPosting(bf2.getChannel());
					} else {
						if (bf1Posting.getTermId() < bf2Posting.getTermId()) {
							writePosting(mf.getChannel(), bf1Posting);
							bf1Posting = index.readPosting(bf1.getChannel());
						} else {
							writePosting(mf.getChannel(), bf2Posting);
							bf1Posting = index.readPosting(bf2.getChannel());							
						}
					}
				} else {
					if (bf1Posting != null) {
						writePosting(mf.getChannel(), bf1Posting);
						bf1Posting = index.readPosting(bf1.getChannel());						
					} else {
						writePosting(mf.getChannel(), bf2Posting);
						bf1Posting = index.readPosting(bf2.getChannel());						
					}
				}
			}
			
			bf1.close();
			bf2.close();
			mf.close();
			b1.delete();
			b2.delete();
			blockQueue.add(combfile);
		}

		/* Dump constructed index back into file system */
		File indexFile = blockQueue.removeFirst();
		indexFile.renameTo(new File(output, "corpus.index"));

		BufferedWriter termWriter = new BufferedWriter(new FileWriter(new File(
				output, "term.dict")));
		for (String term : termDict.keySet()) {
			termWriter.write(term + "\t" + termDict.get(term) + "\n");
		}
		termWriter.close();

		BufferedWriter docWriter = new BufferedWriter(new FileWriter(new File(
				output, "doc.dict")));
		for (String doc : docDict.keySet()) {
			docWriter.write(doc + "\t" + docDict.get(doc) + "\n");
		}
		docWriter.close();

		BufferedWriter postWriter = new BufferedWriter(new FileWriter(new File(
				output, "posting.dict")));
		for (Integer termId : postingDict.keySet()) {
			postWriter.write(termId + "\t" + postingDict.get(termId).getFirst()
					+ "\t" + postingDict.get(termId).getSecond() + "\n");
		}
		postWriter.close();
	}
	
	private static List<Integer> combinePostingList(List<Integer> list, List<Integer> list2) {
		List<Integer> result = new ArrayList<Integer>();
		Iterator<Integer> iter = list.iterator();
		Iterator<Integer> iter2 = list2.iterator();

		int posting = iter.next();
		int posting2 = iter2.next();
		while (true) {
			if (posting == posting2) {
				result.add(posting);
				if (iter.hasNext() && iter2.hasNext()) {
					posting = iter.next();
					posting2 = iter2.next();
				} else {
					while (iter.hasNext()) {
						result.add(iter.next());
					}
					while (iter2.hasNext()) {
						result.add(iter2.next());
					}
					break;
				}
			} else if (posting < posting2) {
				result.add(posting);
				if (iter.hasNext()) {
					posting = iter.next();
				} else {
					result.add(posting2);
					break;
				}
			} else {
				result.add(posting2);
				if (iter2.hasNext()) {
					posting2 = iter2.next();
				} else {
					result.add(posting);
					break;
				}
			}
		}
		
		return result;
	}

	public static void radixSort(List<Pair<Integer,Integer>> a)
    {		
		List<Pair<Integer,Integer>> b = new ArrayList<Pair<Integer,Integer>>(a);
		
        int i = 0;
        int m = a.get(0).getFirst();
        int exp = 1;
        int n = a.size(); 
        
        for (i = 1; i < n; i++)
            if (a.get(i).getFirst() > m)
                m = a.get(i).getFirst();
        
        while (m / exp > 0)
        {
            int[] bucket = new int[10];
 
            for (i = 0; i < n; i++)
                bucket[(a.get(i).getFirst() / exp) % 10]++;
            for (i = 1; i < 10; i++)
                bucket[i] += bucket[i - 1];
            for (i = n - 1; i >= 0; i--)
            	b.set(--bucket[(a.get(i).getFirst() / exp) % 10], a.get(i));
            for (i = 0; i < n; i++)
            	a.set(i, b.get(i));
            
            exp *= 10;        
        }
    }   

}
