/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.io;

/** 
 * A Special Tokenizer optimized to parse lines of CSV-files.
 * 
 * Invalid escape sequences will be ignored and returned including the
 * original '\' character.
 * @author klaus.halfmann@t-online.de
 */
public class CSVTokenizer extends SimpleCSVTokenizer {
    
    /** standard escape character used inside quotations. */
    protected static final char      ESC = '\\';

    /** Optional quotation character for texts, default 0 */
    protected char      quot;
 
    /** Parse a line sepepated by ';' without quoted text. */
    public CSVTokenizer(String aLine, char aQuot) {
        super(aLine);
        quot = aQuot;
    }

    /** Prepare a Tokenzer with given seperator and quotation. 
     * You must call <code>reset</code> before you can use it
     */
    public CSVTokenizer(char aSep, char aQuot) {
        super(aSep);
        quot = aQuot;
    }

    /** Parse a line sepepated by given seperator and quotation. */
    public CSVTokenizer(String aLine, char aSep, char aQuot) {
        super(aLine, aSep);
        quot = aQuot;
    }
    
    /** 
     * Skip from p to next occurence of seperator and set pos to it 
     *
     * @param p current position which is not a seperator (usually a quote) 
     */
    final void skipToSeperator(int p, int len) {
        p++;
        // search for the trailing seperator            
        while (p < len) { 
            char c = line.charAt(p);
            if (c == sep) 
                break;
            p++;
        }
        pos = p;

    }
    
    
    /** 
     * When items are escaped we need some other aproach to return them. 
     * 
     * @param tokenStart index in line where token started (excluding quote)
     * @param start      index in line where '\' was found,
     * 
     */
    public String nextEscapedToken(int tokenStart, int start, int len) {
        StringBuffer retval = new StringBuffer(len - tokenStart);
        // append String found so far.
        if (start > tokenStart)
            retval.append(line.substring(tokenStart, start));
        int p = start;
        while (p < len) {
            char c = line.charAt(p++);
            if (c == quot)
                break;
            if (c != ESC)
                retval.append(c);
            else {
                if (p >= len) // unexpected end of String
                   break;
                c = line.charAt(p++);
                switch (c)
                {
                case 0 :
                     continue;
                case 'b':
                    retval.append('\b'); continue;
                case 't':
                    retval.append('\t'); continue;
                case 'n':
                    retval.append('\n'); continue;
                case 'f':
                    retval.append('\f'); continue;
                case 'r':
                    retval.append('\r'); continue;
                case '"':
                    retval.append('"');  continue;
                case '\'':
                    retval.append('\''); continue;
                case '\\':
                    retval.append('\\'); continue;
                case 'u':   // E.G. \u0123
                    int p4 = p+4;
                    if (p4 >= len) // unexpected end of String
                        return retval.toString();
                    String hex = line.substring(p, p4);
                    p = p4;
                    retval.append((char) Integer.parseInt(hex, 16));
                    continue;
                default: // Invalid Escape, ignore it
                    retval.append('\\');
                    retval.append(c);
                 }
            }
        }
        return retval.toString();
    }
      
    /** return next Token, null when no more tokens are found */
    @Override
	public String nextToken() {
         int     len = line.length();
         int     p = pos + 1;
         char    c = 0;
         if (p < len) {
             c = line.charAt(p);
             if (c != quot) // does not start with a quote
             {
                 while (p < len) {
                     c = line.charAt(p);
                     if (c == sep) 
                         break;
                     p++;
                 }
             } else { // starts with a quote
                pos = p++;    // remember the pos where quote started  
                while (p < len) {
                    c = line.charAt(p);
                    if (c == ESC) {
                        // must use other aproach now
                        String result = nextEscapedToken( pos + 1, p , len);
                        skipToSeperator(p, len);
                        return result;
                    }
                    if (c == quot) 
                        break;
                    p++;
                }
             }
         }
         if (p > pos && p <= len) {
             String result = line.substring(pos + 1,p);
             if (c == quot)    // ends with a quote
                 skipToSeperator(p, len);
             else
                pos = p;    // next pos at the seperator.
             return result;
         }
         return null;   
     } 

     /** Testing , will only compile with JDK 1.4 */ 
}
