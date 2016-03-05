package edu.stanford.cs276;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;

public class EmpiricalCostModel implements EditCostModel{
	
	private static final long serialVersionUID = 1L;

	int uni[]   = new int[255];
	int bi[][]  = new int[255][255];
	int ins[][] = new int[255][255];
	int del[][] = new int[255][255];
	int tra[][] = new int[255][255];
	int sub[][] = new int[255][255];
	
	// Constructor for testing purposes
	EmpiricalCostModel() {
		
	}
	
	public EmpiricalCostModel(String editsFile) throws IOException {
		BufferedReader input = new BufferedReader(new FileReader(editsFile));
		System.out.println("Constructing edit distance map...");
		String line = null;
		while ((line = input.readLine()) != null) {
			Scanner lineSc = new Scanner(line);
			lineSc.useDelimiter("\t");
			String noisy = lineSc.next();
			String clean = lineSc.next();
			
			// Determine type of error and record probability
			/*
			 * Your code here
			 */
			recordEdit(noisy, clean);
			
			lineSc.close();
		}

		input.close();
		System.out.println("Done.");
	}

	void recordEdit(String noisy, String clean) {
		/*
		 * Your code here
		 */
		noisy = ' ' + noisy + ' ';
		clean = ' ' + clean + ' ';
		
		for (int i=1, j=1; i<noisy.length()-1 && j < clean.length()-1; i++,j++) {
			
			// Unigram
			uni[noisy.charAt(i)]++;
			
			// Bigram
			bi[noisy.charAt(i)][noisy.charAt(i+1)]++;
			
			if (noisy.charAt(i) != clean.charAt(j)) {
				if (noisy.charAt(i+1)==clean.charAt(j)) {
					if(noisy.charAt(i)==clean.charAt(j+1)) {
						// Transposition
						tra[noisy.charAt(i)][noisy.charAt(i+1)]++;
					} else {
						// Insertion
						ins[noisy.charAt(i-1)][noisy.charAt(i)]++;
						i++;
					}
				} else if (noisy.charAt(i)==clean.charAt(j+1)) {
					// Deletion
					del[clean.charAt(j-1)][clean.charAt(j)]++;
					j++;
				} else if (noisy.charAt(i-1)==clean.charAt(j-1) && noisy.charAt(i+1)==clean.charAt(j+1)) {
					// Substituion
					sub[noisy.charAt(i)][clean.charAt(j)]++;
				}
			}
			
		}
	}
	
	// You need to update this to calculate the proper empirical cost
	@Override
	public double editProbability(String original, String R) {
		double probability = 0.0;
		
		original = ' ' + original + ' ';
		R = ' ' + R + ' ';
		
		for (int i=1, j=1; i<original.length()-1 && j < R.length()-1; i++,j++) {
			
			if (original.charAt(i) != R.charAt(j)) {
				if (original.charAt(i+1)==R.charAt(j)) {
					if(original.charAt(i)==R.charAt(j+1)) {
						// Transposition
						probability += Math.log((double) (tra[original.charAt(i)][original.charAt(i+1)]+1)) - Math.log((double) (bi[original.charAt(i)][original.charAt(i+1)]+44));
					} else {
						// Insertion
						probability += Math.log((double) (ins[original.charAt(i-1)][original.charAt(i)]+1)) - Math.log((double) (uni[original.charAt(i-1)]+44));
						i++;
					}
				} else if (original.charAt(i)==R.charAt(j+1)) {
					// Deletion
					probability += Math.log((double) (del[R.charAt(j-1)][R.charAt(j)]+1)) - Math.log((double) (bi[R.charAt(j-1)][R.charAt(j)]+44)); 
					j++;
				} else if (original.charAt(i-1)==R.charAt(j-1) && original.charAt(i+1)==R.charAt(j+1)) {
					// Substituion
					probability += Math.log((double) (sub[original.charAt(i)][R.charAt(j)]+1)) - Math.log((double) (uni[R.charAt(j)]+44)); 
				}
			}
			
		}	
		
		return Math.exp(probability);
	}
}
