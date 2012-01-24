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
package com.examples.swing.util;

import javax.swing.*;
import java.lang.reflect.Method;

/**
 * Bare Bones Browser Launch for Java<br> Utility class to open a web page from
 * a Swing application in the user's default browser.<br> Supports: Mac OS X,
 * GNU/Linux, Unix, Windows XP<br> Example Usage:<code><br> &nbsp; &nbsp; String
 * url = "http://www.google.com/";<br> &nbsp; &nbsp; BareBonesBrowserLaunch.openURL(url);<br></code>
 * Latest Version: <a href="http://www.centerkey.com/java/browser/">http://www.centerkey.com/java/browser</a><br>
 * Author: Dem Pilafian<br> Public Domain Software -- Free to Use as You Like
 *
 * @version 1.5, December 10, 2005
 */
public class BareBonesBrowserLaunch {
    private static final String errMsg = "Error attempting to launch web browser";
    public static final String FILE_PREFIX = "file://";
    private static String os;

    static{
        os = System.getProperty("os.name").toLowerCase();
    }

    /**
     * Opens the specified web page in a web browser
     *
     * @param url An absolute URL of a web page (ex: "http://www.google.com/")
     */
    public static void openURL(String url) {
        try {
            if (isMac()) {
                Class fileMgr = Class.forName("com.apple.eio.FileManager");
                Method openURL = fileMgr.getDeclaredMethod("openURL",
                        new Class[]{
                                String.class});
                openURL.invoke(null, url);
            } else if (isWindows())
                Runtime.getRuntime()
                        .exec("rundll32 url.dll,FileProtocolHandler " + url);
            else if(isUnix()) {
                String[] browsers = {
                        "firefox", "opera", "konqueror", "epiphany", "mozilla",
                        "netscape"};
                String browser = null;
                for (int count = 0; count < browsers.length && browser == null;
                     count++)
                    if (Runtime.getRuntime().exec(
                            new String[]{"which", browsers[count]}).waitFor() ==
                            0)
                        browser = browsers[count];
                if (browser == null)
                    throw new Exception("Could not find web browser");
                else
                    Runtime.getRuntime().exec(new String[]{browser, url});
            }
            else{
                JOptionPane.showMessageDialog(null, errMsg );
            }
        }
        catch (Exception e) {
            JOptionPane.showMessageDialog(null, errMsg + ":\n" +
                    e.getLocalizedMessage());
        }
    }


    /**
     * Opens the specified file path using the OS's preferred application binding
     *
     * @param filePath to open on host OS.
     */
    public static void openFile(String filePath) {
        openURL(FILE_PREFIX + filePath);
    }

    public static boolean isWindows() {
        return (os.indexOf("win") >= 0);

    }

    public static boolean isMac() {
        return (os.indexOf("mac") >= 0);
    }

    public static boolean isUnix() {
        return (os.indexOf("nix") >= 0 || os.indexOf("nux") >= 0);
    }


}

