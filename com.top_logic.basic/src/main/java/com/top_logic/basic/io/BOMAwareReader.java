/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PushbackInputStream;
import java.io.Reader;


/**
 * This Reader checks for a BOM to identify the used encoding. 
 * The bytes of the BOM can be skipped during read.
 * If no BOM is found, a given default-encoding or UTF-8 is used.
 * (see different Constructors)
 * 
 * @author    <a href="mailto:cca@top-logic.com>cca</a>
 */
public class BOMAwareReader extends Reader {

    private static final String UTF_8     = "UTF-8";
    private static final String UTF_16_LE = "UTF-16LE";
    private static final String UTF_16_BE = "UTF-16BE";
    private static final String UTF_32_LE = "UTF-32LE";
    private static final String UTF_32_BE = "UTF-32BE";

    private static final String DEFAULT_ENCODING = UTF_8;
    
    private static final int BOM_SIZE = 4;

    private PushbackInputStream pis;
    private InputStreamReader   isr;
    private String              enc;
    private boolean             bomUsed;
    private boolean             skipBOM;

    /**
     * Use this Constructor to set the fallback-encoding (in case no BOM is found) to the 
     * given encoding.
     */
    public BOMAwareReader(InputStream in, String defaultEnc) {
         this(in, defaultEnc, true);
    }
    
    /**
     * Use this Constructor to set the fallback-encoding to UTF-8 and skipBOM to true
     */
    public BOMAwareReader(InputStream in) {
        this(in, DEFAULT_ENCODING, true);
    }
    
    /**
     * Use this Constructor to use the default encoding-fallback (UTF-8) but customize
     * the handling of the BOM (true = skip, false = read)
     */
    public BOMAwareReader(InputStream in, boolean skipBOM) {
        this(in, DEFAULT_ENCODING, skipBOM);
    }

    /**
     * Use this Constructor to customize the handling of the BOM. If skipBOM == true, the 
     * BOM bytes are skipped during reading.
     * The fallback-encoding if no BOM is found is set to the given encoding.
     */
    public BOMAwareReader(InputStream in, String defaultEnc, boolean skipBOM) {
        pis = new PushbackInputStream(in, BOM_SIZE);
        enc = defaultEnc == null ? DEFAULT_ENCODING : defaultEnc;
        bomUsed = false;
        this.skipBOM = skipBOM;
    }
    
    /**
     * Returns the Encoding. This is initially the given encoding or UTF-8.
     * Call init() or read() first to make sure the Encoding is set correctly if
     * a BOM was found. hasBOM() can be used to make sure if there was a BOM. In
     * case there was a BOM the Encoding should be correct, otherwise it is just
     * a fallback default.
     */
    public String getEncoding() {
         return enc;
    }
    
    /**
     * Call init() or read() first! hasBOM returns true if a BOM was found during init/read.  
     */
    public boolean hasBOM() {
        return bomUsed;
    }
    
    @Override
	public void close() throws IOException {
        pis.close();
        if (isr != null) {
            isr.close();
        }
    }

    @Override
	public int read(char[] cbuf, int off, int len) throws IOException {
        init();
        return isr.read(cbuf, off, len);
    }
    
    public void init() throws IOException {
        if (isr != null) {
            return;
        }

        byte bom[] = new byte[BOM_SIZE];
        int read = 0;
        int unread = 0;
        read = pis.read(bom, 0, BOM_SIZE);

        if ( (bom[0] == (byte)0x00) && (bom[1] == (byte)0x00) &&
                (bom[2] == (byte)0xFE) && (bom[3] == (byte)0xFF) ) {
            // found 4 byte BOM for UTF-32BE -> set encoding and don't unread bytes, we only read 4
            enc = UTF_32_BE;
            unread = 0;
            bomUsed = true;
        } 
        else if ( (bom[0] == (byte)0xFF) && (bom[1] == (byte)0xFE) &&
                (bom[2] == (byte)0x00) && (bom[3] == (byte)0x00) ) {
            // found 4 byte BOM for UTF-32LE -> set encoding and don't unread bytes, we only read 4
            enc = UTF_32_LE;
            unread = 0;
            bomUsed = true;
        } 
        else if (  (bom[0] == (byte)0xEF) && (bom[1] == (byte)0xBB) && (bom[2] == (byte)0xBF) ) {
            // found 3 byte BOM for UTF-8 -> set encoding and unread all read bytes but the 3 from the BOM
            enc = UTF_8;
            unread = read - 3;
            bomUsed = true;
        } 
        else if ( (bom[0] == (byte)0xFE) && (bom[1] == (byte)0xFF) ) {
            // found 2 byte BOM for UTF-16BE -> set encoding and unread all read bytes but the 2 from the BOM
            enc = UTF_16_BE;
            unread = read - 2;
            bomUsed = true;
        } 
        else if ( (bom[0] == (byte)0xFF) && (bom[1] == (byte)0xFE) ) {
            // found 2 byte BOM for UTF-16LE -> set encoding and unread all read bytes but the 2 from the BOM
            enc = UTF_16_LE;
            unread = read - 2;
            bomUsed = true;
        } 
        else {
            // No BOM found -> unread all read bytes.
            unread = read;
        }    
        
        // if we want to skip the BOM and we've read more bytes than the BOM needed, we have to unread these
        if (skipBOM) { 
            if (unread > 0) {
                pis.unread(bom, (read - unread), unread);
            }
        }
        // else unread all read bytes
        else {
            pis.unread(bom, 0, read);
        }

        // set the actual InputStreamReader to the found encoding and the properly back-pushed PushbackInputStream
        isr = new InputStreamReader(pis, enc);
    }
    
    /**
     * Read a String BOM-Aware from a File. If a BOM is found, this is used to 
     * identify the encoding. The Bytes from the BOM are skipped for reading.
     */
    public static String readFromFile(File aFile) throws IOException {
        FileInputStream fis = new FileInputStream(aFile);
        return readFromStream(fis);
    }
    
    /**
     * Read a String BOM-Aware from a File. If a BOM is found, this is used to 
     * identify the encoding. The Bytes from the BOM are skipped for reading.
     */
    public static String readFromStream(InputStream is) throws IOException {
        BOMAwareReader bar = new BOMAwareReader(is);
        BufferedReader reader = new BufferedReader(bar);
        StringBuffer buffer = new StringBuffer();
        
        for (int c = reader.read(); c > -1; c = reader.read()) {
            buffer.append((char)c);
        }
        
        reader.close();
        bar.close();
        
        return buffer.toString();
    }
    
}
