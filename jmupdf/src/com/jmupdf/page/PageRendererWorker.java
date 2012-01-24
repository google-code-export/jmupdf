/*
 * 
 * See copyright file
 *  
 */
package com.jmupdf.page;


/**
 * PageRendererWorker class
 * 
 * Create a single thread worker for rendering pages. </br>
 * This is used by PageRenderer class and only beneficial if </br>
 * reusing instance while rendering pages. It is meant to keep </br>
 * the creation of threads minimal.
 * 
 * @author Pedro J Rivera
 * 
 */
public class PageRendererWorker implements Runnable {
	private boolean isWorkerActive;
	private PageRenderer render;
	private Thread th;
	
	/**
	 * Create new instance
	 */
	public PageRendererWorker() {
		isWorkerActive = true;
		th = new Thread(this);
		th.start();
	}
	
	/**
	 * Render page in worker thread
	 * 
	 * @param render
	 */
	public void renderPage(PageRenderer render) {
		synchronized (this) {
			this.render = render;
			notify();
		}
	}

	/**
	 * Shutdown worker thread
	 * 
	 */
	public void shutdown() {
		synchronized (this) {
			this.isWorkerActive = false;
			notify();
		}		
	}
	
	/**
	 * Is worker thread still active
	 * 
	 * @return
	 */
	public boolean isWorkerActive() {
		return isWorkerActive;
	}
	
	/**
	 * Run job
	 */
	public void run() {
		synchronized (this) {
			while (isWorkerActive) {
				if (render != null && 
					!render.isPageRendered() && 
					!render.isPageRendering()) {
					render.render(true);
				}
				try {
					wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}			
		}
	}

}
