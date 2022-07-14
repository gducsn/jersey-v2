package com.util;

import java.util.Random;

import com.model.People;

public class RandomUtil {
	
	public static int randomAge() {
		int max = 99;
		int min = 1;
		int range = (max - min) + 1;
		int rnd = (int) (Math.random() * range) + min;
		return rnd;
	}
	
	public static String randomStr() {
		
		 // create a string of uppercase and lowercase characters and numbers
	    String upperAlphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
	    String lowerAlphabet = "abcdefghijklmnopqrstuvwxyz";
	    String numbers = "0123456789";

	    String alphaNumeric = upperAlphabet + lowerAlphabet + numbers;
	    StringBuilder sb = new StringBuilder();
	    Random random = new Random();
	    for(int i = 0; i <11; i++) {
	      int index = random.nextInt(alphaNumeric.length());
	      char randomChar = alphaNumeric.charAt(index);
	      sb.append(randomChar);
	    }

	    String randomString = sb.toString();
	    return randomString;

	}
	
	public static People peopleRandom(int age) {
		People p = new People(RandomUtil.randomStr() + "gdu",RandomUtil.randomStr(),age);
		return p;
	}

}
