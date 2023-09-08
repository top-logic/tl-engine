/*
 * SPDX-FileCopyrightText: 2004 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.io;

// InputStream, Reader, BufferedReader, InputStreamReader, IOException
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

/**
 * This class searches for a String in given Streams.
 *
 * TODO KHA    The wish for WildcardSearches will soon arrive.
 *
 * @author <a href="mailto:kha@top-logic.com"">Klaus Halfmann</a>
 */
public abstract class StreamFinder { // This class has static methods only 

    /**
     * Helper function for isInReader, when first character was found.
     */
    static private boolean isRestInReader(
        Reader aReader, String aString, int length)  
            throws IOException {
        for (int i=1; i < length; i++) {
            if (aString.charAt(i) != aReader.read()) {
                return false;
            }
        }

        return true;    
    }

    /**
     * Return true when the given String is found as part of the Stream.
     *<p>
     *  a  null  String is not part of any stream. 
     *  An Empty String is     part of any stream.
     *  You should use a Reader which really knows about Character conversion.
     *</p>
     */
    static public boolean isInReader(Reader aReader, String aString) 
                                            throws IOException    {
        if (aString == null) {
            return false;
        }
        int length = aString.length();    
        if (length == 0) {
            return true;    // Mhh, empty String is always contained
        }

        // need a buffered reader for mark()            
        BufferedReader theReader =
            (aReader instanceof BufferedReader) ?
                (BufferedReader) aReader : new BufferedReader(aReader);
        
        int firstChar = aString.charAt(0);
        int currChar  = theReader.read();
        while (currChar >= 0) 
        {
            if (currChar == firstChar) {
                theReader.mark(length);
                if (length < 2 || isRestInReader(theReader,aString, length)) {
                    return true;
                }
                // else 
                theReader.reset();
            }    
            currChar  = theReader.read();  
        }   
        return false; 
    }

    /**
     * Return true when the one of the given String is found as part of the Stream.
     *<p>
     *  a  null  String is not part of any stream. 
     *  An Empty String is     part of any stream.
     *  You should use a Reader which really knows about Character conversion.
     *</p>
     * @param strings array of Strings, any String after first null.String will be ignored.
     */
    static public boolean isInReader(Reader aReader, String strings[]) 
                                            throws IOException    {
        
        int l = strings.length;
        if (l == 1) { // shortcut for degenerated single string
            return isInReader(aReader,strings[0]);
        }

        int firstChars[] = new int[l];
        int length[]     = new int[l];

        for (int i=0; i < l; i++) {
            String aString = strings[i];
            if (aString == null) { // ignore all following    
                l = i;
                break;
            }
            int sl = length[i] = aString.length();
            if (sl == 0) {
                return true;    // Mhh, empty String is always contained
            }
            firstChars[i] = aString.charAt(0);
        }
        
        // need a buffered reader for mark()            
        BufferedReader theReader =
            (aReader instanceof BufferedReader) ?
                (BufferedReader) aReader : new BufferedReader(aReader);
        
        int currChar  = theReader.read();
        while (currChar >= 0) {
            for (int i=0; i < l; i++) {
                if (currChar == firstChars[i]) {
                    int sl = length[i];
                    theReader.mark(sl);
                    if (sl < 2 || isRestInReader(theReader,strings[i], sl)) {
                        return true;
                    }
                    else {
                        theReader.reset();
                    }
                }
            }    
            currChar  = theReader.read();  
        }   
        return false; 
    }

    /**
     * Helper function for isInReaderLowerCase, when first character was found.
     */
    static private boolean isRestInReaderLowerCase (
        Reader aReader, String aString, int length)  
            throws IOException {
        for (int i=1; i < length; i++) {
            if (aString.charAt(i) != Character.toLowerCase((char) aReader.read())) {
                return false;
            }
        }

        return true;    
    }

    /**
     * String converted to lowercase is searched in the Stream as lowercase.
     *<p>
     *  a  null  String is not part of any stream. 
     *  An Empty String is     part of any stream.
     *  You should use a Reader which really knows about Character conversion.
     *</p>
     */
    static public boolean isInReaderLowerCase(Reader aReader, String aString) 
                                            throws IOException    {
        if (aString == null) {
            return false;
        }
        int length = aString.length();    
        if (length == 0) {
            return true;    // Mhh, empty String is always contained
        }

        aString = aString.toLowerCase();
        // need a buffered reader for mark()            
        BufferedReader theReader =
            (aReader instanceof BufferedReader) ?
                (BufferedReader) aReader : new BufferedReader(aReader);
        
        int firstChar = aString.charAt(0);
        int currChar  = theReader.read();
        while (currChar >= 0) 
        {
            if (Character.toLowerCase((char) currChar) == firstChar) {
                theReader.mark(length);
                if (length < 2 || isRestInReaderLowerCase(theReader,aString, length)) {
                    return true;
                }
                else {
                    theReader.reset();
                }
            }    
            currChar  = theReader.read();  
        }   
        return false; 
    }

    /**
     * Strings converted to lowercase are searched in the Stream as lowercase.
     *<p>
     *  a  null  String is not part of any stream. 
     *  An Empty String is     part of any stream.
     *  You should use a Reader which really knows about Character conversion.
     *</p>
     * @param strings array of Strings, any String after first null.String will be ignored.
     */
    static public boolean isInReaderLowerCase(Reader aReader, String[] strings) 
                                            throws IOException    {
        int l = strings.length;
        if (l == 1) { // shortcut for degenerated single string
            return isInReaderLowerCase(aReader,strings[0]);
        }

        int     firstChars[] = new int[l];
        int     length[]     = new int[l];
        String  lStrings[]   = new String[l];
        
        for (int i=0; i < l; i++) {
            String aString = strings[i];
            if (aString == null) { // ignore all following

                l = i;
                break;
            }
            int sl = length[i] = aString.length();
            if (sl == 0) {
                return true;    // Mhh, empty String is always contained
            }
            aString = aString.toLowerCase();
            firstChars[i]   = aString.charAt(0);
            lStrings[i]     = aString;
        }

        BufferedReader theReader =
            (aReader instanceof BufferedReader) ?
                (BufferedReader) aReader : new BufferedReader(aReader);
        
        int currChar  = theReader.read();
        while (currChar >= 0) 
        {
            currChar = Character.toLowerCase((char) currChar);   
            for (int i=0; i < l; i++)  {
                if (currChar == firstChars[i]) {
                    int sl = length[i];
                    theReader.mark(sl);
                    if (sl < 2 || isRestInReaderLowerCase(theReader,lStrings[i], sl)) {
                        return true;
                    }
                    else {
                        theReader.reset();
                    }
                }
            }    
            currChar  = theReader.read();  
        }   
        return false; 
    }

    /**
     * Return true when the given String is found a part of the Stream.
     *<p>
     *  a  null  String is not part of any stream. 
     *  An Empty String is     part of any stream.
     *  Better use a Reader which really knows about Character conversion.
     *</p>
     */
    static public boolean isInStream(InputStream aStream, String aString) 
                                                throws IOException    {
        if (aString == null) {
            return false;
        }
        if (aString.length() == 0) {
            return true;    // Mhh, empty String is always contained
        }
        
        return isInReader(new InputStreamReader(aStream), aString);
    }

    /**
     * String converted to lowercase is searched in the Stream as lowercase.
     *<p>
     *  a  null  String is not part of any stream. 
     *  An Empty String is     part of any stream.
     *  Better use a Reader which really knows about Character conversion.
     *</p>
     */
    static public boolean isInStreamLowerCase(InputStream aStream, String aString) 
                                                throws IOException    {
        if (aString == null) {
            return false;
        }
        if (aString.length() == 0) {
            return true;    // Mhh, empty String is always contained
        }
        
        return isInReaderLowerCase(new InputStreamReader(aStream), aString);
    }
}
