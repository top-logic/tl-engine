/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Properties;

import com.top_logic.basic.Logger;

/**
 * Provider of some functions in handling properties.
 * 
 * @author     <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class PropertiesHelper {

    /** 
     * Sort the properties in file "source" by their keys.
     * 
     * @param    aSource    The source file to be parsed, must not be <code>null</code>.
     * @param    aDest      The destination file to write the sorted properties to, must not b <code>null</code>.
     */
    public static void sortProperties(File aSource, File aDest) {
        if (aSource.exists()) {
            Properties      theProps  = new Properties();
            FileInputStream theStream = null;

            try {
                theStream = new FileInputStream(aSource);

                theProps.load(theStream);
            }
            catch (Exception ex) {
                Logger.warn("Given source file '" + aSource + "' cannot be read!", ex, PropertiesHelper.class);
            }
            finally {
                if (theStream != null) {
                    try {
                        theStream.close();
                    }
                    catch (IOException e) { }
                }
            }

            ArrayList  theKeys = new ArrayList(theProps.keySet());
            FileWriter theFile = null;

            Collections.sort(theKeys);

            try {
                theFile = new FileWriter(aDest);

                for (Iterator theIt = theKeys.iterator(); theIt.hasNext();) {
                    String theKey   = (String) theIt.next();
                    String theValue = theProps.getProperty(theKey);

                    theFile.write(theKey + " = " + theValue + '\n');
                }
            }
            catch (Exception ex) {
                Logger.warn("Given destination file '" + aDest + "' cannot be written!", ex, PropertiesHelper.class);
            }
            finally {
                if (theFile != null) {
                    try {
                        theFile.close();
                    }
                    catch (IOException e) { }
                }
            }
        }
        else {
            Logger.warn("Given source file '" + aSource + "' doesn't exist!", PropertiesHelper.class);
        }
    }
    
    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Please call this with: " + PropertiesHelper.class.getName() + " <source> <dest>");
        }
        else {
            File theSource = new File(args[0]);
            File theDest   = new File(args[1]);

            if (!theSource.exists()) {
                System.out.println("Source file '" + theSource + "' doesn't exist!");
            }

            PropertiesHelper.sortProperties(theSource, theDest);
        }
    }
}
