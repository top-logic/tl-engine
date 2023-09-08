/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.io;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * The CSVReader reads and parses CSV files according to the RFC standard, using per default
 * ',' as value separator, '"' as quoting character and line break as line separator.
 * See RFC 4180: http://tools.ietf.org/html/rfc4180.
 *
 * In addition, the separators / quoting can be customized or disabled.
 * Combined with a {@link StringReader}, content from strings can be parsed also.
 *
 * @author <a href="mailto:Christian.Braun@top-logic.com">Christian Braun</a>
 */
public class CSVReader {

    /** Special character indicating that no character has been read from the reader yet. */
    private static final int NOT_INITIALIZED = Integer.MIN_VALUE;

    /** Special character indicating end of file. */
    private static final int EOF = -1;

    private static final char LF = '\n';
    private static final char CR = '\r';


    /** The default value separator. */
    public static final char DEFAULT_VALUE_SEPARATOR = ',';

    /** The default quote character. */
    public static final char DEFAULT_QUOTE_CHARACTER = '"';

    /** Special character indicating that "\n", "\r", "\r\n" or "\n\r" shall be used as line separator. */
    public static final char DEFAULT_LINE_SEPARATOR = Character.MAX_VALUE - 1;

    /** Quote character indicating quoting is disabled. */
    public static final char CHAR_DISABLED = Character.MAX_VALUE;



    /** The reader to read the content from */
    private Reader reader;

    private char valueSeparator;
    private char lineSeparator;
    private char quoteCharacter;

    private boolean valueSeparatorEnabled;
    private boolean lineSeparatorEnabled;
    private boolean quotingEnabled;
    
    /** Indicates whether OS specific line breaks shall be used as line separator. */
    private boolean defaultLineSeparator;

    private long charsRead;
    private long tokensRead;
    private long linesRead;



    /** Holds the next character from the reader. */
    private int nextChar = NOT_INITIALIZED;



    /**
     * Creates a new instance of this class.
     * @see #CSVReader(Reader, char, char, char)
     */
    public CSVReader(Reader reader) {
        this(reader, DEFAULT_VALUE_SEPARATOR, DEFAULT_QUOTE_CHARACTER, DEFAULT_LINE_SEPARATOR);
    }

    /**
     * Creates a new instance of this class.
     * @see #CSVReader(Reader, char, char, char)
     */
    public CSVReader(Reader reader, char valueSeparator) {
        this(reader, valueSeparator, DEFAULT_QUOTE_CHARACTER, DEFAULT_LINE_SEPARATOR);
    }

    /**
     * Creates a new instance of this class.
     * @see #CSVReader(Reader, char, char, char)
     */
    public CSVReader(Reader reader, char valueSeparator, char quoteCharacter) {
        this(reader, valueSeparator, quoteCharacter, DEFAULT_LINE_SEPARATOR);
    }

    /**
     * Creates a new instance of this class. Call this with an {@link StringReader} to parse
     * the content from a string. Each char parameter may be {@link #CHAR_DISABLED} to
     * disable the specific control function.
     *
     * @param reader
     *        the reader to read the content from
     * @param valueSeparator
     *        the char to use as value separator
     * @param quoteCharacter
     *        the char to use for quoting values
     * @param lineSeparator
     *        the char to use as line separator; may be {@link #DEFAULT_LINE_SEPARATOR}
     */
    public CSVReader(Reader reader, char valueSeparator, char quoteCharacter, char lineSeparator) {
        this.reader = reader;
        this.valueSeparator = valueSeparator;
        this.lineSeparator = lineSeparator;
        this.quoteCharacter = quoteCharacter;
        this.valueSeparatorEnabled = valueSeparator != CHAR_DISABLED;
        this.lineSeparatorEnabled = lineSeparator != CHAR_DISABLED;
        this.quotingEnabled = quoteCharacter != CHAR_DISABLED;
        this.defaultLineSeparator = lineSeparator == DEFAULT_LINE_SEPARATOR;
        if (reader == null) {
            throw new IllegalArgumentException("Reader must not be null!");
        }
        if ((valueSeparator == lineSeparator && valueSeparator != CHAR_DISABLED) ||
            (valueSeparator == quoteCharacter && valueSeparator != CHAR_DISABLED) ||
            (lineSeparator == quoteCharacter && quoteCharacter != CHAR_DISABLED)) {
            throw new IllegalArgumentException("valueSeparator, lineSeparator and quoteCharacter must all be different or disabled.");
        }
    }



    /**
     * Closes the underlying reader.
     */
    public void close() throws IOException {
        reader.close();
    }



    /**
     * Reads the next line of data from the CSV stream. This method takes into account that
     * line and value separators could be quoted.
     *
     * @return a list of strings representing the values of the next line or
     *         <code>null</code>, if the end of the stream has been reached and no
     *         further line is available.
     * @throws IOException
     *         if an I/O error occurs
     */
    public List<String> readLine() throws IOException {
        int i; char ch;
        boolean isQuoted = false;
        List<String> tokens = new ArrayList<> ();
        StringBuilder sb = new StringBuilder();

        while (true) {
            i = getNextChar();

            // end of content
            if (i == EOF) {
                // skip empty last line or further calls of this method after
                // end of stream has been reached
                if (tokens.isEmpty() && sb.length() == 0) {
                    return null;
                }
                tokens.add(sb.toString());
                tokensRead++;
                linesRead++;
                return tokens;
            }
            ch = (char)i;
            charsRead++;

            // quote character
            if (isQuoteCharacter(ch)) {
                if (isQuoted) {
                    i = previewNextChar();
                    if (i == quoteCharacter) { //escaped quote character
                        sb.append((char)getNextChar());
                        charsRead++;
                    }
                    else if (i == EOF || isValueSeparator((char)i) || isLineSeparator((char)i)) {
                        // quoting can only be ended at end of a value
                        isQuoted = false;
                    }
                    else {
                        // else treat it as normal character
                        sb.append(ch);
                    }
                }
                else {
                    // quoting can only be started at begin of a value
                    if (sb.length() == 0) {
                        isQuoted = true;
                    }
                    else { // else treat it as normal character
                        sb.append(ch);
                    }
                }
            }

            // value separator
            else if (isValueSeparator(ch)) {
                if (isQuoted) {
                    sb.append(ch);
                }
                else {
                    tokens.add(sb.toString());
                    sb.setLength(0); // clear string buffer
                    tokensRead++;
                }
            }

            // line separator
            else if (isLineSeparator(ch)) {
                if (defaultLineSeparator) {
                    // This is required because Windows uses "CR LF" as line separator instead
                    // of only one character, so read the next char also, if they are different.
                    i = previewNextChar();
                    if (ch == LF && i == CR || ch == CR && i == LF) {
                        getNextChar(); //read next char also
                        ch = LF; // normalize to '\n'
                        charsRead++;
                    }
                }
                if (isQuoted) {
                    sb.append(ch);
                }
                else {
                    tokens.add(sb.toString());
                    tokensRead++;
                    linesRead++;
                    return tokens;
                }
            }

            // normal character
            else {
                sb.append(ch);
            }

        }

    }

    /**
     * Reads and parses the whole CSV stream into a list of lines.
     *
     * @return a list of lines, where each line is a list of values; may be empty but never
     *         <code>null</code>
     * @throws IOException
     *         if an I/O error occurs
     */
    public List<List<String>> readAllLines() throws IOException {
        List<List<String>> theContent = new ArrayList<>();
        List<String>       theLine    = readLine();
        while (theLine != null) {
            theContent.add(theLine);
            theLine = readLine();
        }
        return theContent;
    }



    /**
     * Checks whether the given char is a line separator.
     */
    private boolean isLineSeparator(char aChar) {
        return lineSeparatorEnabled && (defaultLineSeparator ? (aChar == LF || aChar == CR) : aChar == lineSeparator);
    }

    /**
     * Checks whether the given char is a value separator.
     */
    private boolean isValueSeparator(char aChar) {
        return valueSeparatorEnabled && aChar == valueSeparator;
    }

    /**
     * Checks whether the given char is a quoting character.
     */
    private boolean isQuoteCharacter(char aChar) {
        return quotingEnabled && aChar == quoteCharacter;
    }



    /**
     * Gets the next char as int from the reader. Reads another char from the reader
     * afterwards, so that multiple calls of this method return always the next character
     * from the reader.
     *
     * @return the next char as int from 0 to 65535 or -1, if the end of the stream has been
     *         reached
     * @throws IOException
     *         if an I/O error occurs
     */
    private int getNextChar() throws IOException {
        int ch = previewNextChar();
        nextChar = reader.read();
        return ch;
    }

    /**
     * Returns the character which the next call of the {@link #getNextChar()} method will
     * return. In opposition to the {@link #getNextChar()} method, there will not be read a
     * new character from the reader, so that multiple calls of this method will return
     * always the same character.
     *
     * @return the next char as int from 0 to 65535 or -1, if the end of the stream has been
     *         reached
     * @throws IOException
     *         if an I/O error occurs
     */
    private int previewNextChar() throws IOException {
        if (nextChar == NOT_INITIALIZED) {
            nextChar = reader.read();
        }
        return nextChar;
    }



    /**
     * Returns the number of chars read from the stream until now.
     */
    public long getCharsRead() {
        return this.charsRead;
    }

    /**
     * Returns the number of tokens (values) read from the stream until now.
     */
    public long getTokensRead() {
        return this.tokensRead;
    }

    /**
     * Returns the number of lines read from the stream until now.
     */
    public long getLinesRead() {
        return this.linesRead;
    }

}
