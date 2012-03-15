package com.examples.pdf.concurrent;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.jmupdf.JmuPdf;
import com.jmupdf.enums.ImageType;
import com.jmupdf.exceptions.DocException;
import com.jmupdf.exceptions.DocSecurityException;
import com.jmupdf.exceptions.PageException;
import com.jmupdf.page.Page;
import com.jmupdf.pdf.PdfDocument;

public class JMuPDFShowCase
{
    static long imageMin = Long.MAX_VALUE;

    static long imageMax = Long.MIN_VALUE;

    private static int count = 0;

    public static synchronized int incrementImageCount()
    {
        count++;
        return count;
    }

    private static class DocumentTrace
    {
        private int expected = 0;

        private int used = 0;

        private int instanciatedPages = 0;

        public synchronized void incrementExpect()
        {
            expected++;
        }

        public synchronized void incrementUsed()
        {
            used++;
            if (used == expected)
            {
                System.out.println("FREE Document : " + pdfDocument + ", instanciatedPages=" + instanciatedPages
                        + ", used=" + used + ", expected=" + expected);
                for (List<Page> pageList : pageMap.values())
                {
                    for (Page page : pageList)
                    {
                        page.dispose();
                    }
                    pageList.clear();
                }
                pageMap.clear();
                pdfDocument.dispose();
                pdfDocument = null;
            }
        }

        private PdfDocument pdfDocument;

        public void setPdfDocument(PdfDocument pdfDocument)
        {
            this.pdfDocument = pdfDocument;
        }

        private Map<Integer, List<Page>> pageMap = new HashMap<Integer, List<Page>>();

        public synchronized Page getPage(int pageNumber) throws PageException
        {
            List<Page> pageList = pageMap.get(pageNumber);
            if (pageList == null)
            {
                pageList = new ArrayList<Page>();
                pageMap.put(pageNumber, pageList);
            }
            if (pageList.isEmpty())
            {
                instanciatedPages++;
                return pdfDocument.getPage(pageNumber); // XXX ---
            }

            Page page = pageList.get(0);
            pageList.remove(0);
            return page;
        }

        public synchronized void releasePage(Page page)
        {
            int pageNumber = page.getPageNumber();
            List<Page> pageList = pageMap.get(pageNumber);
            if (pageList == null)
            {
                pageList = new ArrayList<Page>();
                pageMap.put(pageNumber, pageList);
            }
            pageList.add(page);
        }
    }

    private static Map<Integer, DocumentTrace> documentTraceMap = new java.util.concurrent.ConcurrentHashMap<Integer, DocumentTrace>();

    private static ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(4);

    private static void processFile(final String file, final int rdCount) throws DocException, DocSecurityException
    {
        long startup = System.currentTimeMillis();
        PdfDocument pdfDocument = new PdfDocument(file, 20);

        DocumentTrace documentTrace = new DocumentTrace();
        documentTrace.setPdfDocument(pdfDocument);
        documentTraceMap.put(rdCount, documentTrace);

        System.out
                .println("Open #" + rdCount + " pdfDocument " + file + " : " + (System.currentTimeMillis() - startup));

        System.out.println("pageCount : " + pdfDocument.getPageCount());

        final ImageType color = ImageType.IMAGE_TYPE_RGB;
        final float gamma = 0.0f;
        final int rotate = 0;

        final int min = 100;
        final int max = 1100;
        final int step = 1;

        documentTrace.incrementExpect();

        for (int desc = min; desc <= max; desc += step)
        {
            for (int pageNumber = 1; pageNumber <= pdfDocument.getPageCount(); pageNumber++)
            {
                final String imagePath = "d:/tmp/images/jmupdf_" + pageNumber + "_" + desc + "_" + Math.random() + ".png";
                System.out.println("Page : " + pageNumber + " => file " + imagePath);
                final float zoom = ((float) desc) / 595f;
                final int fPageNumber = pageNumber;

                documentTrace.incrementExpect();

                executor.execute(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        try
                        {
                            long st = System.currentTimeMillis();

                            DocumentTrace documentTrace = documentTraceMap.get(rdCount);
                            Page page;
                            try
                            {
                                page = documentTrace.getPage(fPageNumber);
                            }
                            catch (PageException e)
                            {
                                System.err.println("PageException " + e);
                                return;
                            }
                            long st2 = System.currentTimeMillis();
                            System.out.println("getPage " + fPageNumber + " : " + (st2 - st));

                            page.saveAsPng(imagePath, rotate, zoom, color, gamma);
                            long st3 = System.currentTimeMillis();
                            imageMin = Math.min(imageMin, (st3 - st2));
                            imageMax = Math.max(imageMax, (st3 - st2));

                            System.out.println("Doc#" + rdCount + " : " + incrementImageCount() + " : savePng "
                                    + fPageNumber + " zoom " + zoom + " : " + (st3 - st2) + " - min=" + imageMin
                                    + ", max=" + imageMax);

                            new File(imagePath).delete();

                            documentTrace.releasePage(page);

                            documentTrace.incrementUsed();

                        }
                        catch (RuntimeException e)
                        {
                            System.err.println("RuntimeException" + e);
                            e.printStackTrace();
                        }
                    }
                });
            }
        }
        documentTrace.incrementUsed();

    }

    public static void main(String arg[]) throws DocException, DocSecurityException, IOException
    {
        System.out.println("Jmupdf version : " + JmuPdf.getLibVersion());

        BufferedReader br = new BufferedReader(new FileReader("d:\\tmp\\filelist.txt"));

        int documentCount = 0;

        long grandStartup = System.currentTimeMillis();

        while (true)
        {
            final String file = br.readLine();

            if (file == null)
                break;

            documentCount++;

            final int rdCount = documentCount;

            new Thread()
            {
                public void run()
                {
                    try
                    {
                        System.out.println("*** Processing file : " + file + " #" + rdCount);
                        processFile(file, rdCount);
                    }
                    catch (DocException e)
                    {
                        System.err.println("DocException " + e.getMessage());
                        e.printStackTrace();
                    }
                    catch (DocSecurityException e)
                    {
                        System.err.println("DocSecurityException " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }.start();

            if (rdCount > 60)
                break;
        }

        try
        {
            Thread.sleep(6 * 1000);
        }
        catch (InterruptedException e1)
        {
            e1.printStackTrace();
        }
        executor.shutdown();
        try
        {
            executor.awaitTermination(120, TimeUnit.MINUTES);
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
        System.out.println("Finished !");
        System.out.println("Total : " + (System.currentTimeMillis() - grandStartup));

    }
}
