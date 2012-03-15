package com.examples.pdf.concurrent;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.jmupdf.exceptions.DocException;
import com.jmupdf.exceptions.DocSecurityException;
import com.jmupdf.pdf.PdfDocument;
import com.jmupdf.xps.XpsDocument;

public class TestMutexA {

	public static void main(String[] args) throws DocException, DocSecurityException {
		
		// Limit each opened doc to a max of 30mb
		int maxMemory = 30;		
		PdfDocument doc1 = new PdfDocument("c:\\tmp\\test2.pdf", maxMemory);
		PdfDocument doc2 = new PdfDocument("c:\\tmp\\test3.pdf", maxMemory);
		PdfDocument doc3 = new PdfDocument("c:\\tmp\\test4.pdf", maxMemory);
		
		// Let's process up to 12 concurrent pages
		ExecutorService jobPool;
		jobPool = Executors.newFixedThreadPool(12);

		// Loop thru pages and queue up for rendering
		boolean more = true;
		int i = 1;
		while (more) {
			if (i <= doc1.getPageCount()) {
				jobPool.execute( new TestMutexB(doc1, i) );
			}
			if (i <= doc2.getPageCount()) {
				jobPool.execute( new TestMutexB(doc2, i) );
			}
			if (i <= doc3.getPageCount()) {
				jobPool.execute( new TestMutexB(doc3, i) );
			}
			if ((i >= doc1.getPageCount()) && (i >= doc2.getPageCount()) && (i >= doc3.getPageCount())) {
				more = false;
			}
			i++;
		}
		
		jobPool.shutdown();

		try {
			while (jobPool.awaitTermination(2, TimeUnit.SECONDS) == false) {}
		} catch (InterruptedException e) {}
		
     }

}
