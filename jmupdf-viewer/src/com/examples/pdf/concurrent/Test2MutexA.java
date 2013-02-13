package com.examples.pdf.concurrent;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.jmupdf.exceptions.DocException;
import com.jmupdf.exceptions.DocSecurityException;

public class Test2MutexA {

	public static void main(String[] args) throws DocException, DocSecurityException {

		// say hi...
		log("start...");
		
		// Let's process up to 12 concurrent pages
		ExecutorService jobPool;
		jobPool = Executors.newFixedThreadPool(12);

		// Loop thru pages and queue up for rendering
		for (int i = 0; i < 1; i++) {
			jobPool.execute( new Test2MutexB("f:\\tmp\\test1.pdf") );
			jobPool.execute( new Test2MutexB("f:\\tmp\\test2.pdf") );
			jobPool.execute( new Test2MutexB("f:\\tmp\\test3.pdf") );
			jobPool.execute( new Test2MutexB("f:\\tmp\\test4.pdf") );
			//jobPool.execute( new Test2MutexB("d:\\tmp\\test5.pdf") );
			//jobPool.execute( new Test2MutexB("d:\\tmp\\test6.pdf") );
		}
		
		jobPool.shutdown();

		try {
			while (jobPool.awaitTermination(2, TimeUnit.SECONDS) == false) {}
		} catch (InterruptedException e) {}
		
		// say bye...
		log("end!");
		
     }

    protected static void log(String text) {
    	System.out.println(text);
    }
    
}
