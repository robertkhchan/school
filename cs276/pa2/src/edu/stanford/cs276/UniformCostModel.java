package edu.stanford.cs276;

public class UniformCostModel implements EditCostModel {
	
	private static final long serialVersionUID = 686877863003751069L;
	
	@Override
	public double editProbability(String original, String R) {
		if (original.equals(R)) {
			return 0.90;
		} else {
			int distance = computeMinimumEditDistance(original,R);			
			return Math.pow(0.01, distance);
		}
	}
	
	/**
	 * Code reference from "Edit Distance in Java"
	 * @see <a href="http://www.programcreek.com/2013/12/edit-distance-in-java/">here</a>
	 * 
	 * @param word1
	 * @param word2
	 * @return
	 */
	private int computeMinimumEditDistance(String word1, String word2) {
		int len1 = word1.length();
		int len2 = word2.length();
	 
		// len1+1, len2+1, because finally return dp[len1][len2]
		int[][] dp = new int[len1 + 1][len2 + 1];
	 
		for (int i = 0; i <= len1; i++) {
			dp[i][0] = i;
		}
	 
		for (int j = 0; j <= len2; j++) {
			dp[0][j] = j;
		}
	 
		//iterate though, and check last char
		for (int i = 0; i < len1; i++) {
			char c1 = word1.charAt(i);
			for (int j = 0; j < len2; j++) {
				char c2 = word2.charAt(j);
	 
				//if last two chars equal
				if (c1 == c2) {
					//update dp value for +1 length
					dp[i + 1][j + 1] = dp[i][j];
				} else {
					int replace = dp[i][j] + 1;
					int insert = dp[i][j + 1] + 1;
					int delete = dp[i + 1][j] + 1;
	 
					int min = replace > insert ? insert : replace;
					min = delete > min ? min : delete;
					dp[i + 1][j + 1] = min;
				}
			}
		}
	 
		return dp[len1][len2];
	}

}
