package cs276.test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import cs276.assignments.BaseIndex;
import cs276.assignments.BasicIndex;
import cs276.assignments.PostingList;

public class TestBasicIndex {

	 public static void main(String[] args) {
		 
		 Map<Integer, List<Integer>> postings = new TreeMap<Integer, List<Integer>>();
		 
		 postings.put(4, new ArrayList<Integer>());
		 postings.get(4).add(1);
		 postings.get(4).add(12);
		 postings.get(4).add(334);
		 
		 postings.put(12, new ArrayList<Integer>());
		 postings.get(12).add(77);
		 postings.get(12).add(335);
		 
		try {
			File testFile = new File("C:\\Temp", "TestBasicIndexOutput.txt");
			RandomAccessFile bfc = new RandomAccessFile(testFile, "rw");
			BaseIndex bIndex = new BasicIndex(); 
			
			
			for (Map.Entry<Integer, List<Integer>> entry : postings.entrySet()) {
				bIndex.writePosting(bfc.getChannel(), new PostingList(entry.getKey(),entry.getValue()));
			}
			
			
			PostingList postingList = bIndex.readPosting(bfc.getChannel());
			
			bfc.close();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.out.println(e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println(e.getMessage());
		}
	 }
}
