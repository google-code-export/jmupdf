package com.examples.pdf.concurrent;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Sample1_a {

	public static void main(String[] args) {
		long s;
		
		s = System.currentTimeMillis();
		
		String doc = "c:\\tmp\\test-";

		ExecutorService jobPool;
		jobPool = Executors.newFixedThreadPool(3);
		
		for (int i=0; i<=6; i++) {
			jobPool.execute( new Sample1_b(doc, i) );
		}
		
		jobPool.shutdown();
		
		try {
			while (jobPool.awaitTermination(2, TimeUnit.SECONDS) == false) {}
		} catch (InterruptedException e) {}
		
		System.out.println("Total time = " + (System.currentTimeMillis() - s));
		
     }

}
