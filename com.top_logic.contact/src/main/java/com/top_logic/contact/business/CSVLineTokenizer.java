/*
 * SPDX-FileCopyrightText: 2004 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.contact.business;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.StringServices;

/**
 * Better use {@link com.top_logic.basic.io.CSVTokenizer}.
 */
public class CSVLineTokenizer {
    
    private BufferedReader input;
    
    private String tokenStartQuote;
    
    
    private String tokenEndQuote;
    
    private String separator;
    
    private String newLine;
    
    private int tokenCount;
    
    private boolean requiresQuotes;
    
    private boolean allowMultiline;
    
    private boolean ignoreMissingCells = false;
    
    private int currentLineNumber = 0;

    public CSVLineTokenizer(
            BufferedReader anInput, 
            String aTokenStartQuote, 
            String aTokenEndQuote, 
            String aSeparator,
            String aNewLine,
            boolean aRequiresQuotes,
            boolean anAllowMultiline,
            boolean ignoreMissingCells
        ) throws IOException {
        
        super();
        this.input = anInput;
        this.tokenStartQuote = aTokenStartQuote;
        this.tokenEndQuote = aTokenEndQuote;
        this.separator = aSeparator;
        this.newLine = aNewLine;
        this.requiresQuotes = aRequiresQuotes;
        this.allowMultiline = anAllowMultiline; 
        
        // set number of tokens
        String theFirstLine = getNextLine();
        String theInitSep = this.tokenEndQuote + this.separator + this.tokenStartQuote;
        int theCount = 1;
        int theCurrentIndex = 0;
        int theIndex;
        do {
            theIndex = theFirstLine.indexOf(theInitSep, theCurrentIndex);
            if (theIndex > 0) {
                theCount ++;
                theCurrentIndex = theIndex + theInitSep.length();
            }
        } while (theIndex > 0);
        this.tokenCount = theCount;
        this.ignoreMissingCells = ignoreMissingCells;
    }
    
    public CSVLineTokenizer(
            BufferedReader anInput, 
            String aTokenStartQuote, 
            String aTokenEndQuote, 
            String aSeparator,
            String aNewLine,
            boolean aRequiresQuotes,
            boolean anAllowMultiline) throws IOException {
        this(anInput, aTokenStartQuote, aTokenEndQuote, aSeparator, aNewLine, aRequiresQuotes, anAllowMultiline, false);
    }
    
    public int getTokenCount() {
        return this.tokenCount;
    }
    
    public List getNextEntry() throws InvalidLineException, IOException {
        String theCurrentLine = this.getNextLine();
        String theRemainingContent = theCurrentLine;
        String theTokenStartQuote = this.getStartQuoteToFind();
        String theTokenEndQuote = this.getEndQuoteToFind();
        // check if there are more lines at all
        if (theCurrentLine == null) {
            return null;
        }
        // line must start with a valid token (may be an empty token)
        if (!theCurrentLine.startsWith(theTokenStartQuote) && !theCurrentLine.startsWith(this.separator)) {
            throw new InvalidLineException(theCurrentLine, this.currentLineNumber, "Invalid Token Start Quote.");
        }
        
        List theResult = new ArrayList();
        while (theRemainingContent != null) {
            // handle empty last token
            if (StringServices.isEmpty(theRemainingContent)) {
                theResult.add("");
                theRemainingContent = null;
                continue;
            }
            // handle empty current token
            if (theRemainingContent.startsWith(this.separator)) {
                theResult.add("");
                theRemainingContent = theRemainingContent.substring(1);
                continue;
            }
             // current token must start with token start quote
            if (!theRemainingContent.startsWith(theTokenStartQuote)) {
                throw new InvalidLineException(theCurrentLine, this.currentLineNumber, "Invalid Token Start Quote.");
            }
            // check if needss end quote
            boolean forceEndQuote = false;
            if (theRemainingContent.startsWith(this.tokenStartQuote)) {
                forceEndQuote = true;
            }
            // find end quote
            int theEndQuoteIndex = -1;
            while (theEndQuoteIndex < 0) {
                // find inner token separator
                String theCurrentEndQuote = forceEndQuote ? this.tokenEndQuote : theTokenEndQuote;
                theEndQuoteIndex = theRemainingContent.indexOf(theCurrentEndQuote + this.separator);
                if (theEndQuoteIndex < 0) {
                    // check for valid end of line (the last entry must end with an end quote even if requireQuotes is true
                    if (theRemainingContent.endsWith(this.tokenEndQuote)) {
                        theEndQuoteIndex = theRemainingContent.length() - this.tokenEndQuote.length();
                    } else {
                        if (allowMultiline) {
                            String theNextLine = this.getNextLine();
                            if (theNextLine == null) {
                                throw new InvalidLineException(theCurrentLine, this.currentLineNumber, "Invalid Token End Quote.");
                            }
                            theRemainingContent = theRemainingContent + this.newLine + theNextLine;
                        } else {
                            theEndQuoteIndex = theRemainingContent.length();
                        }
                    }
                } 
            }
            // get Content
            int cutIndexOffset;
            if (forceEndQuote) {
                cutIndexOffset = this.tokenEndQuote.length();
            } else {
                cutIndexOffset = theTokenEndQuote.length();
            }
           
            String theTokenContent = theRemainingContent.substring(
                    theTokenStartQuote.length(), theEndQuoteIndex + cutIndexOffset);
            if (!requiresQuotes || forceEndQuote) {
                if (theTokenContent.startsWith(this.tokenStartQuote)
                        && theTokenContent.endsWith(this.tokenEndQuote)) {
                    theTokenContent = theTokenContent.substring(
                            this.tokenStartQuote.length(), 
                            theTokenContent.length()
                                    - this.tokenEndQuote.length());
                }
            }
            theResult.add(theTokenContent);
            // cut remaining content
            theRemainingContent = theRemainingContent.substring(theEndQuoteIndex + cutIndexOffset);
            if (theRemainingContent.startsWith(this.separator)) {
                theRemainingContent = theRemainingContent.substring(this.separator.length());
            } else {
                if (StringServices.isEmpty(theRemainingContent)) {
                    // to terminate the line
                    theRemainingContent = null;
                } else {
                    throw new InvalidLineException(theCurrentLine, this.currentLineNumber, "Invalid Token End Quote: "+theRemainingContent+".");
                }
            }
        }
        int theDiff = this.tokenCount - theResult.size();
        if (theDiff != 0) {
            if (theDiff > 0 && ignoreMissingCells) {
                while (theDiff > 0) {
                    theResult.add(StringServices.EMPTY_STRING);
                    theDiff--;
                }
            }
            else {
                throw new InvalidLineException(theCurrentLine, this.currentLineNumber, "Invalid Token Count. Expected "+this.tokenCount+" but was "+theResult.size()+".");            
            }
        }
        return theResult;
    }
    
    private String getStartQuoteToFind() {
        return requiresQuotes ? this.tokenStartQuote : "";
    }
    
    private String getEndQuoteToFind() {
        return requiresQuotes ? this.tokenEndQuote : "";
    }
     
    /**
     * Read the next line and increment the line number.
     * 
     * @return the next line from the input reader, 
     *         null if no more lines are available.
     */
    private String getNextLine() throws IOException {
        String theResult = this.input.readLine();
        if (theResult != null) {
            this.currentLineNumber++;
        }
        return theResult;
    }
    
    public class InvalidLineException extends Exception {
        int lineNumber;
        String content;
        String reason;

        public InvalidLineException(String content, int number, String reason) {
            super();
            // TODO Auto-generated constructor stub
            this.content = content;
            this.lineNumber = number;
            this.reason = reason;
        }
        @Override
		public String toString() {
            return "Invalid entry at line "+this.lineNumber+" Reason: "+this.reason+" Content: "+this.content;
        }
    }
    

}
