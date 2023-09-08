/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tools.resources;

import java.io.IOException;
import java.io.InputStream;

/**
 * read in a "logical line" from input stream, skip all comment and blank lines and filter out
 * those leading whitespace characters ( , and ) from the beginning of a "natural line". Method
 * returns the char length of the "logical line" and stores the line in "lineBuf".
 * 
 * This class is mostly a copy of the private class: <code>Properties.LineReader</code>
 */
public class LineReader {
    public LineReader(InputStream inStream) {
        this.inStream = inStream;
    }

    byte[] inBuf = new byte[8192];

    char[] lineBuf = new char[1024];

    int inLimit = 0;

    int inOff = 0;

    InputStream inStream;

    int readLine() throws IOException {
        int len = 0;
        char c = 0;

        boolean skipWhiteSpace = true;
        boolean isCommentLine = false;
        boolean isNewLine = true;
        boolean appendedLineBegin = false;
        boolean precedingBackslash = false;
        boolean skipLF = false;

        while (true) {
            if (inOff >= inLimit) {
                inLimit = inStream.read(inBuf);
                inOff = 0;
                if (inLimit <= 0) {
                    if (len == 0 || isCommentLine) {
                        return -1;
                    }
                    return len;
                }
            }
            // The line below is equivalent to calling a
            // ISO8859-1 decoder.
            c = (char) (0xff & inBuf[inOff++]);
            if (skipLF) {
                skipLF = false;
                if (c == '\n') {
                    continue;
                }
            }
            if (skipWhiteSpace) {
                if (c == ' ' || c == '\t' || c == '\f') {
                    continue;
                }
                if (!appendedLineBegin && (c == '\r' || c == '\n')) {
                    continue;
                }
                skipWhiteSpace = false;
                appendedLineBegin = false;
            }
            if (isNewLine) {
                isNewLine = false;
                if (c == '#' || c == '!') {
                    isCommentLine = true;
                    continue;
                }
            }

            if (c != '\n' && c != '\r') {
                lineBuf[len++] = c;
                if (len == lineBuf.length) {
                    int newLength = lineBuf.length * 2;
                    if (newLength < 0) {
                        newLength = Integer.MAX_VALUE;
                    }
                    char[] buf = new char[newLength];
                    System.arraycopy(lineBuf, 0, buf, 0, lineBuf.length);
                    lineBuf = buf;
                }
                // flip the preceding backslash flag
                if (c == '\\') {
                    precedingBackslash = !precedingBackslash;
                } else {
                    precedingBackslash = false;
                }
            } else {
                // reached EOL
                if (isCommentLine || len == 0) {
                    isCommentLine = false;
                    isNewLine = true;
                    skipWhiteSpace = true;
                    len = 0;
                    continue;
                }
                if (inOff >= inLimit) {
                    inLimit = inStream.read(inBuf);
                    inOff = 0;
                    if (inLimit <= 0) {
                        return len;
                    }
                }
                if (precedingBackslash) {
                    len -= 1;
                    // skip the leading whitespace characters in following line
                    skipWhiteSpace = true;
                    appendedLineBegin = true;
                    precedingBackslash = false;
                    if (c == '\r') {
                        skipLF = true;
                    }
                } else {
                    return len;
                }
            }
        }
    }
}