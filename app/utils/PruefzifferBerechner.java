package utils;

import java.util.ArrayList;
import java.math.*;

public class PruefzifferBerechner {

	private static int[][] matrix = {
		{0, 9, 4, 6, 8, 2, 7, 1, 3, 5},
		{9, 4, 6, 8, 2, 7, 1, 3, 5, 0},
		{4, 6, 8, 2, 7, 1, 3, 5, 0, 9},
		{6, 8, 2, 7, 1, 3, 5, 0, 9, 4},
		{8, 2, 7, 1, 3, 5, 0, 9, 4, 6},
		{2, 7, 1, 3, 5, 0, 9, 4, 6, 8},
		{7, 1, 3, 5, 0, 9, 4, 6, 8, 2},
		{1, 3, 5, 0, 9, 4, 6, 8, 2, 7},
		{3, 5, 0, 9, 4, 6, 8, 2, 7, 1},
		{5, 0, 9, 4, 6, 8, 2, 7, 1, 3}
	};
	
	public ArrayList<Integer> divide(BigInteger num){
		
		ArrayList<Integer> list = new ArrayList<Integer>(); 
		
		String stringNum = "" + num;
		int anzStellen = stringNum.length();
		BigInteger zehn = new BigInteger("10");
		
		for(int i = anzStellen-1; i >= 0; i--){
			BigInteger divisor = zehn.pow(i);
			BigInteger tmp = num.divide(divisor);
			BigInteger res = tmp.mod(new BigInteger("" + 10));
			list.add(new Integer(res.intValue()));
		}
		return list;
	}
	
	public int computeNumber(BigInteger num){
		
		ArrayList<Integer> list = divide(num);
		
		int uebertrag = 0;
		for(Integer i : list){
			uebertrag = matrix[uebertrag][i];
		}
		
		return (10-uebertrag)%10;
		
	}
}
