package utils;

import java.util.Random;

public class Test {
	
	public static void main(String[] args) {
		
		while(true) {
			int duration = 1+ new Random().nextInt(1000);
			int subD1 = 1 + new Random().nextInt(duration);
			int subD2 = duration - subD1;
			
			if(duration != subD1 + subD2) {
				System.err.println("error");
				System.exit(-1);
			}
			
			
		}
	}

}
