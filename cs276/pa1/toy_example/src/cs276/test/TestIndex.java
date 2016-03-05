package cs276.test;

import java.util.ArrayList;
import java.util.List;

import cs276.assignments.Index;
import cs276.assignments.Quicksort;
import cs276.util.Pair;

public class TestIndex {

	 public static void main(String[] args) {
		 
		 Pair<Integer, Integer> p1 = new Pair<Integer,Integer>(345,1283);
		 Pair<Integer, Integer> p2 = new Pair<Integer,Integer>(1,13);
		 Pair<Integer, Integer> p3 = new Pair<Integer,Integer>(32,1);
		 Pair<Integer, Integer> p4 = new Pair<Integer,Integer>(1,15);
		 Pair<Integer, Integer> p5 = new Pair<Integer,Integer>(1,29);
		 Pair<Integer, Integer> p6 = new Pair<Integer,Integer>(84,13);

		 List<Pair<Integer,Integer>> test = new ArrayList<Pair<Integer,Integer>>();
		 test.add(p1); test.add(p2); test.add(p3); test.add(p4); test.add(p5); test.add(p6);
 
		 //Index.radixSort(test);
		 new Quicksort().sort(test);
		
		 List<Pair<Integer,Integer>> expected = new ArrayList<Pair<Integer,Integer>>();
		 expected.add(p2); expected.add(p4); expected.add(p5); expected.add(p3); expected.add(p6); expected.add(p1);
		 
		 boolean isFailed = false;
		 for (int i=0; i<expected.size(); i++) {
			if (expected.get(i).getFirst() != test.get(i).getFirst() 
			    || expected.get(i).getSecond() != test.get(i).getSecond()) {
				isFailed = true;
			}
			System.out.println(test.get(i).getFirst() + " " + test.get(i).getSecond());
		 }
		 
		 if (isFailed) {
			 System.out.println("Failed");
		 } else {
			 System.out.println("Passed");
		 }
	 }
}
