/*
 * Copyright (C) 2010-2011 Pedro J Rivera
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package com.examples.swing;

import javax.swing.SwingUtilities;

import com.examples.swing.controller.MainController;
import com.examples.swing.view.MainView;

public class Main {

    /**
     * Main()
     * 
     * Note: Create the GUI on the event-dispatching thread
     * 
     * @param args the command line arguments
     */
	public static void main(String[] args) {
		try {
	    	SwingUtilities.invokeAndWait(new Runnable() {
				public void run() {
					new MainController(new MainView());
				}
			});
		} catch (Exception e) {
            e.printStackTrace();
            System.err.flush();
            System.exit(10);
		}		
	}

}
