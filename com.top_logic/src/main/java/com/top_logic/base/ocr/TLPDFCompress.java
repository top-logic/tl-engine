/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.ocr;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.top_logic.basic.Configuration;
import com.top_logic.basic.Logger;
import com.top_logic.basic.Reloadable;
import com.top_logic.basic.ReloadableManager;
import com.top_logic.basic.StringServices;

/**
 * TopLogic specific extensions to the PDFCompressor.
 * 
 * @author    <a href="mailto:kha@top-logic.com">Klaus Halfmann</a>
 */
public class TLPDFCompress extends CVistaPDFCompress implements Reloadable {
    
    /** Can be used a singleton to synchronize against */
    protected static TLPDFCompress instance;

    /** 
     * Map iso languages to the PDFCompressor languages.
     */
    protected Map<String, String> languageMap;

    /**
     * Construct a new TLPDFCompress from (XML-)Properties.
     */
    public TLPDFCompress() {
    	this.init();
    }
    
    /**
     * Setup from XML-Properties.
     */
    protected void init() {
		Properties conf = Configuration.getConfiguration(TLPDFCompress.class).getProperties();
        exe     =  (String) conf.remove("exe");
        params  = StringServices.toList((String) conf.remove("params"), ' ');
        
        if (exe == null) {
            Logger.warn("No exe configured for TLPDFCompress in int(), will not be able to run", TLPDFCompress.class);
            return;
        }
        if (params == null) {
            Logger.warn("No params configured for TLPDFCompress in int(), will not be able to run", TLPDFCompress.class);
            return;
        }
		params.add(0, exe);

        setIgnoreOutput(true);
        languageMap = new HashMap<String, String>((Map) conf);
        
        ReloadableManager.getInstance().addReloadable(this);
	}

	/** Check if the needed executable is actually installed. */
    public boolean isInstalled() {
        return exe != null && new File(exe).exists();      
    }

    /** Overriden to use ISO language codes.
     * 
     * @param inFile     a File (PDF, tiff, bmp ...) to scan/compress.
     * @param outFile    a Directory or file to use for output.
     * @param aLanguage  an <a href="http://www.ics.uci.edu/pub/ietf/http/related/iso639.txt">ISO Language code</a> (not as found with PDF-Compress)
     *                  
     * @return the exitValue() of the process. 
     */
    @Override
	public synchronized int run(File inFile, File outFile, String aLanguage) 
            throws IOException, InterruptedException {
        return super.run(inFile, outFile, languageMap.get(aLanguage));
    }

    /** Return a collection of supported Languages (ISO Codes).
     */
    public Collection<String> supportedLanguages() {
        return languageMap.keySet();
    }

    public static synchronized TLPDFCompress getInstance() {
        if (instance == null) {
            instance = new TLPDFCompress();
        }
        return instance;
    }

	/**
	 * @see com.top_logic.basic.Reloadable#getDescription()
	 */
	@Override
	public String getDescription() {
		return "OCR for PDF documents containing images";
	}

	/**
	 * @see com.top_logic.basic.Reloadable#getName()
	 */
	@Override
	public String getName() {
		return "TLPDFCompress";
	}

	/**
	 * @see com.top_logic.basic.Reloadable#reload()
	 */
	@Override
	public boolean reload() {
		this.init();
		
		return true;
	}

	/**
	 * @see com.top_logic.basic.Reloadable#usesXMLProperties()
	 */
	@Override
	public boolean usesXMLProperties() {
		return true;
	}


}
