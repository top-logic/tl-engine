/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.io;

import java.util.ArrayList;
import java.util.List;

/** 
 * A Special Tokenizer optimized to parse lines of CSV-files.
 *  
 * @author klaus.halfmann@t-online.de
 */
public class SimpleCSVTokenizer {
    
     /** The String to fetch the fields from. */
    protected String    line;     

    /** The current Position of the last sep. inside the line */
    protected int       pos;  

    /** Character seperating the fields, default ';' */
    protected char      sep;
    
    
    /** Parse a line sepepated by ';' without quoted text. */
    public SimpleCSVTokenizer(String aLine) {
        line = aLine;
        pos  = -1;
        sep  = ';';
    }

    /** Prepare the Tokenizer with given seperator.
     *  You must call <code>reset</code> before you can use it 
     */
    public SimpleCSVTokenizer(char aSep) {
        sep  = aSep;
    }

    /** Parse a line sepepated by given seperator without quoted text. */
    public SimpleCSVTokenizer(String aLine, char aSep) {
        line = aLine;
        pos  = -1;
        sep  = aSep;
    }

    
    /** return next Token, null when no more tokens are found */
    public String nextToken() {
        int     len = line.length();
        int     p = pos + 1;
        while (p < len) {
            char c = line.charAt(p);
            if (c == sep) 
                break;
            p++;
        }
        if (p > pos && p <= len) {
            String result = line.substring(pos + 1,p);
            pos = p;    // next pos after the seperator.
            return result;
        }
        return null;   
    } 
    
    /** Reset the Tokenizer to start with another line */
    public void reset(String aLine) {
        line = aLine;
        reset();
    }
    
    /** Reset the Tokenizer to start with same line again */
    public void reset() {
        pos  = -1;
    }

    /**
	 * Splits the complete line into its tokens. Note: Resets the tokenizer before.
	 *
	 * @return a list holding all tokens.
	 */
	public List<String> tokenize() {
        reset();
		List<String> theList = new ArrayList<>();
        String theToken = nextToken();
        while(theToken != null) {
            theList.add(theToken);
            theToken = nextToken();
        }
        return theList;
    }

    /**
	 * Splits the given line string into its tokens. Note: Resets the tokenizer before with the
	 * given line.
	 *
	 * @return a list holding all tokens of the given input line.
	 */
	public List<String> tokenize(String aLine) {
        reset(aLine);
        return tokenize();
    }

    /** Testing , will only compile with JDK 1.4 */    
    /*
    public static void main(String args[]) {
    }
    */
}
