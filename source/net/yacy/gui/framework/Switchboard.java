/**
 *  Switchboard
 *  Copyright 2010 by Michael Peter Christen; mc@yacy.net, Frankfurt a. M., Germany
 *  First released 05.08.2010 at http://yacy.net
 *  
 *  $LastChangedDate: 2010-06-16 17:11:21 +0200 (Mi, 16 Jun 2010) $
 *  $LastChangedRevision: 6922 $
 *  $LastChangedBy: orbiter $
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *  
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *  
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program in the file lgpl21.txt
 *  If not, see <http://www.gnu.org/licenses/>.
 */

package net.yacy.gui.framework;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import javax.swing.text.JTextComponent;

import org.apache.log4j.Logger;



/**
 * a static class that holds application-wide parameters
 * very useful because global settings need not be passed
 * to all classes and methods
 * 
 * @author m.christen
 *
 */
public class Switchboard {

    /**
     * the shallrun variable is used
     * by all application parts to see if they must terminate
     */
    private static boolean shallrun = true;
    
    /**
     * a global properties object
     */
    private static Properties properties = new Properties();
    
    public static Logger log = Logger.getLogger(Switchboard.class);

    
    public static void startInfoUpdater() {
        new InfoUpdater(2000).start();
    }
    
    public static void addShutdownHook(Thread mainThread) {
        // registering shutdown hook
        final Runtime run = Runtime.getRuntime();
        run.addShutdownHook(new shutdownHookThread(mainThread));
    }
    
    public static JTextComponent InfoBox = null;
    private static String InfoBoxMessage = "";
    private static long InfoBoxMessageUntil = 0;
    
    public static void info(String infoString, long infoTime) {
        InfoBoxMessage = infoString;
        InfoBoxMessageUntil = System.currentTimeMillis() + infoTime;
    }
    
    public static class InfoUpdater extends Thread {
        long steptime;
        public InfoUpdater(long steptime) {
            this.steptime = steptime;
        }
        public void run() {
            while (shallrun) {
                if (InfoBox != null) {
                    if (System.currentTimeMillis() < InfoBoxMessageUntil) {
                        InfoBox.setText(InfoBoxMessage);
                    }
                }
                try {Thread.sleep(steptime);} catch (InterruptedException e) {}
            }
        }
    }
    
    /**
    * This class is a helper class whose instance is started, when the java virtual
    * machine shuts down. Signals the plasmaSwitchboard to shut down.
    */
    public static class shutdownHookThread extends Thread {
        private final Thread mainThread;

        public shutdownHookThread(final Thread mainThread) {
            super();
            this.mainThread = mainThread;
        }

        public void run() {
            try {
                if (shallrun()) {
                    log.info("Shutdown via shutdown hook.");

                    // send a shutdown signal to the switchboard
                    log.info("Signaling shutdown to the switchboard.");
                    shutdown();
                    
                    // waiting for the main thread to finish execution
                    log.info("Waiting for main thread to finish.");
                    if (this.mainThread != null && this.mainThread.isAlive()) {
                        this.mainThread.join();
                    }
                }
            } catch (final Exception e) {
                log.info("Unexpected error. " + e.getClass().getName(),e);
            }
        }
    }
    
    /**
     * test if the application shall run
     * @return true if the application shall run
     */
    public static boolean shallrun() {
        return shallrun;
    }
    
    /**
     * set a termination signal.
     * this is not reversible.
     */
    public static void shutdown() {
        shallrun = false;
    }
    
    /**
     * initialize the properties with the content of a file
     * @param propFile
     */
    public static void load(File propFile) {
        try {
            properties.load(new FileInputStream(propFile));
        } catch (FileNotFoundException e1) {
            log.info("error: file dispatcher.properties does not exist. Exit");
            System.exit(-1);
        } catch (IOException e1) {
            log.info("error: file dispatcher.properties cannot be readed. Exit");
            System.exit(-1);
        }
    }
    
    /**
     * access to the properties object
     * @param key
     * @return the property value or null if the property does not exist
     */
    public static String get(String key) {
        return properties.getProperty(key);
    }
    
    /**
     * access to the properties object
     * @param key
     * @param dflt
     * @return
     */
    public static String get(String key, String dflt) {
        return properties.getProperty(key, dflt);
    }
    
    /**
     * convenience access to integer values in properties
     * @param key
     * @param dflt
     * @return
     */
    public static int getInt(String key, int dflt) {
        if (!properties.containsKey(key)) return dflt;
        return Integer.parseInt(properties.getProperty(key));
    }
    
    /**
     * convenience access to boolean values in properties
     * @param key
     * @param dflt
     * @return
     */
    public static boolean getBool(String key, boolean dflt) {
        if (!properties.containsKey(key)) return dflt;
        String s = properties.getProperty(key);
        return s.equals("true") || s.equals("1");
    }
    
    public static File getFile(String key) {
        String s = properties.getProperty(key);
        if (s == null) return null;
        s.replace("/", File.separator);
        return new File(s);
    }
    
    /**
     * set a property
     * @param key
     * @param value
     */
    public static void set(String key, String value) {
        properties.setProperty(key, value);
    }

    /**
     * convenience method to set a integer property
     * @param key
     * @param value
     */
    public static void set(String key, int value) {
        properties.setProperty(key, Integer.toString(value));
    }
    
    /**
     * convenience method to set a boolean property
     * @param key
     * @param value
     */
    public static void set(String key, boolean value) {
        properties.setProperty(key, (value) ? "true" : "false");
    }
}