/*
 * SPDX-FileCopyrightText: 2003 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.ocr;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.tooling.ModuleLayoutConstants;

/**
 * Wrapper for the 
 *  <a href="http://www.cvisiontech.com/cvistapdf.html">PDFCompressor</a> by 
 *  <a href="http://www.cvisiontech.com/">CVision</a>.
 * 
 * This is modeled after (and actually wrapps) the command line tool PDFCompress.
 * 
 * Not all Options of PDFCompress 1.3 are supported, just the needed ones
 * 
 * @author    <a href="mailto:kha@top-logic.com">kha</a>
 */
public class CVistaPDFCompress {

    /** The path to the executable we actually use */
    protected String    exe;

    /** Ignore output of tool (otherwise it will be copied to System.out). */
    protected boolean   ignoreOutput;

    /** Indicates recursive descending into the directory. */
    protected boolean   recurse;

    /** Convert multiple pages in a subdirectory into one Document */
    protected boolean   mergeDir;

    /** Override files with same name in any case. */
    protected boolean   clobber;

    /** Do not loose any picture information. */
    protected boolean   lossless;

    /** Clean the picture(s) before conversion. */
    protected boolean   clean = true;

    /** Quite mode, no not geenerated any output, on by default here. */
    protected boolean   quiet = true;

    /** Genearte Text output, too. */
    protected boolean   text;

    /** Turns on aggressive symbolic matching. 
     * 
     * This mode will help minimize the compressed file size, 
     * but increases compression time.
     */
    protected boolean   matching;
    
    /** Turn segmentation ON or OFF. 
     * 
     * OFF mode (the default) is faster,
     * but ON mode generates smaller compressed files.
     */
    protected boolean   segment = true;

    /** Turns despeckling on. 
     * 
     * More ambitious than regular cleaning,  this removes larger stray 
     * specks that are judged to be artifacts from scanning or photocopying. 
     * This computerized judgment call is not infallible, however, 
     * so the resultant document should be checked to ensure that 
     * no portion of the actual image was lost in this process.
     */       
    protected boolean   despeckling;

    /** Check for document skew (alternate document orientations).  
     */
    protected boolean   descew;

    /** Will compress all image streams within the PDF file on 
     *   a stream-by-stream basis
     */
    protected boolean   stream;

    /** Causes the output files to be "web-optimized".
     */
    protected boolean   linearize;
    
    
    /** Attempts to smooth out jagged edges in the image. 
     * 
     * On a scale from 1 to 7 indicating the type of smoothing you wish to apply.
     * 0 means no smootihing (default).
     */
    protected int       smooth;
    
    /** Qualitiy Level needed to actually start OCR.  
     * 
     * 1 (default) try all pages, maximum is 10.
     */
    protected int       level = 1;

    /** Timeout for OCR in Seconds.  
     * 0 indicates no timeout.
     */
    protected int       timeout;

    /** X-Resolution of compressed OutputFile
     * 
     * for 0 the compression engine will attempt to automatically determine 
     * the resolution from the input file...
     */
    protected int       xres;

    /** Y- Resolution of compressed OutputFile
     * 
     * for 0 it will be the same as xres
     */
    protected int       yres;

    /** Allows you to gain access to password-protected files.
     */
    protected String    userpw;

    /** Language to use (See installed .lng-files).
     */
    protected String    language;

    /** List of options matching the currently set variables.  
     */
    protected List<String> params;

    /**
     * Construct a new PDFCompress for the given path.
     * 
     * @param pathToExe path to PDFCompressor.exe (or PDFMaestro.exe)
     */
    public CVistaPDFCompress(String pathToExe) {
        this.exe = pathToExe;
    }
    
    /**
     * Protected CTor for subclasses.
     * 
     * Nothing will be intialized here.
     */
    public CVistaPDFCompress() {
        // protected subclasses.
    }

    /** Accumulate the compression options for the Tool. 
     *
     * Do not modify the returned parameters ! 
     */
    protected List<String> getParams() {
        
        if (params == null) {
            ArrayList<String> opts = new ArrayList<>(16);
            opts.add(exe);
            if (quiet)      opts.add("-q");
            if (lossless)   opts.add("-d");
            if (matching)   opts.add("-m");
            if (despeckling)opts.add("-v");
            if (stream)     opts.add("-stream");
            if (linearize)  opts.add("-linearize");
            
            if (!clean) {   // Clean is default
                opts.add("-c");      
                opts.add("OFF");
            }            
            if (smooth != 0)    { opts.add("-h");      opts.add(Integer.toString(smooth)); }
            if (userpw != null) { opts.add("-userpw"); opts.add(userpw); }
            
            if (level  != 0) {  
                opts.add("-o"); opts.add(Integer.toString(level));
                // only allowed when -o is set
                if (timeout != 0)     { opts.add("-ot");   opts.add(Integer.toString(timeout));     }
                if (descew)           { opts.add("-dsrimage");                       }
                    else              { opts.add("-dsoff");                          }
                if (language != null) { opts.add("-lang"); opts.add(language);       }
                if (text)             { opts.add("-txt");                            } 
                
                
            }
            if (xres   != 0) {
                opts.add("-pdfres");  opts.add(Integer.toString(xres));
                if (yres != 0)
                    opts.add(Integer.toString(xres));
            }
            opts.trimToSize();
            params = opts;
        }
        return params; // only return a copy !
    }

    /** Start running the conversion process.
     * 
     * @param inFile  a File (PDF, tiff, bmp ...) to scan/compress.
     * @param outFile a Directory or file to use for output.
     * 
     * @return the exitValue() of the process. 
     */
    public synchronized int run(File inFile, File outFile) 
            throws IOException, InterruptedException {
        List<String> theParams = getParams();
        ArrayList<String> cmdarray  = new ArrayList<>(theParams.size() + 4);
        cmdarray.addAll(theParams);
        if (inFile.isDirectory()) {
            cmdarray.add("-dir");
            cmdarray.add(inFile.getPath());
        } else {
            cmdarray.add("-in");
            cmdarray.add(inFile.getPath());
        }
        cmdarray.add("-out");
        cmdarray.add(outFile.getPath());
        
        return letItRun(cmdarray);
    }

    /** Start the conversion process using a specific language.
     * 
     * @param inFile     a File (PDF, tiff, bmp ...) to scan/compress.
     * @param outFile    a Directory or file to use for output.
     * @param aLanguage  Language to use (See installed .lng-files).
     *                  
     * @return the exitValue() of the process. 
     */
    public synchronized int run(File inFile, File outFile, String aLanguage) 
            throws IOException, InterruptedException {
       List<String>      theParams = getParams();
       ArrayList<String> cmdarray  = new ArrayList<>(theParams.size() + 6);
       cmdarray.addAll(theParams);
       cmdarray.add("-lang");
       cmdarray.add(aLanguage);
       cmdarray.add("-in");
       cmdarray.add(inFile.getPath());
       cmdarray.add("-out");
       cmdarray.add(outFile.getPath());
           
        return letItRun(cmdarray);
    }

    /** 
     * Extract the paramters, Care about the Output of the process and wait for its exit.
     * 
     * @return the exitValue() of the process. 
     */ 
    protected int letItRun(List<String> cmdList) throws IOException, InterruptedException {
        
        Runtime     rt       = Runtime.getRuntime();
        String[]    cmdarray = cmdList.toArray(new String[cmdList.size()]);
        Process     proc     = rt.exec(cmdarray);
        InputStream is       = proc.getInputStream();
        int         tmp;
        while ((tmp = is.read()) > 0) {
            if (!ignoreOutput)
                System.out.write(tmp);
        }
        
        proc.waitFor();
        return proc.exitValue();
    }

    /** Start running the conversion process.
     * 
     * @param inFile  a Directory of files to scan/compress.
     * @param ext     The type of file/extenison to use (pdf tif jpg bmp)
     * @param outFile a Directory or file to use for output.
     * 
     * @return the exitValue() of the process. 
     */
    public synchronized int run(File inFile, String ext, File outFile) 
            throws InterruptedException, IOException {

        List<String>      theParams = getParams();
        ArrayList<String> cmdarray  = new ArrayList<>(theParams.size() + 7);
        cmdarray.addAll(theParams);
        cmdarray.add("-in");
        cmdarray.add("-dir");
        cmdarray.add(inFile.getPath());
        cmdarray.add("-ext");
        cmdarray.add(ext);
        cmdarray.add("-out");
        cmdarray.add(outFile.getPath());

        return letItRun(cmdarray);
    }

    /**
     * Clean the picture(s) before conversion. 
     * 
     * Default is true.
     */
    public void setClean(boolean aClean) {
        clean  = aClean;
        params = null;
    }

    /**
     * Override files with same name in any case.
     */
    public void setClobber(boolean aClobber) {
        clobber = aClobber;
        params  = null;
   }

    /**
     * Check for document skew (alternate document orientations).  
     */
    public void setDescew(boolean aDescew) {
        descew = aDescew;
        params = null;
    }

    /**
     * Turns despeckling on, when set to true. 
     * 
     * More ambitious than regular cleaning,  this removes larger stray 
     * specks that are judged to be artifacts from scanning or photocopying. 
     * This computerized judgment call is not infallible, however, 
     * so the resultant document should be checked to ensure that 
     * no portion of the actual image was lost in this process.     
     */
    public void setDespeckling(boolean aDespeckling) {
        despeckling = aDespeckling;
        params = null;
    }

    /**
     *  Language to use (See installed .lng-files).
     * 
     *  Default (null) is english.
     */
    public void setLanguage(String aLanguage) {
        language = aLanguage;
        params = null;
   }
    
    /**
     * Qualitiy Level needed to actually start OCR.  
     * 
     * 1 (default) try all pages, maximum is 10.
     */
    public void setLevel(int aLevel) {
        level = aLevel;
        params = null;
    }

    /**
     * Causes the output files to be "web-optimized".
     */
    public void setLinearize(boolean aLinearize) {
        linearize = aLinearize;
        params = null;
   }

    /**
     * Set true to not loose any picture information. 
     */
    public void setLossless(boolean aLossless) {
        lossless = aLossless;
        params = null;
    }

    /**
     * When true turns on aggressive symbolic matching. 
     * 
     * This mode will help minimize the compressed file size, 
     * but increases compression time.
     */
    public void setMatching(boolean aMatching) {
        matching = aMatching;
        params = null;
    }

    /**
     * Convert multiple pages in a subdirectory into one Document.
     */
    public void setMergeDir(boolean aMergeDir) {
        mergeDir = aMergeDir;
        params = null;
    }

    /**
     * Quite mode, no not geenrated any output, on by default here.
     */
    public void setQuiet(boolean aQuiet) {
        quiet = aQuiet;
        params = null;
    }

    /**
     * Indicates recursive descending into the input directory.
     */
    public void setRecurse(boolean aRecurse) {
        recurse = aRecurse;
        params = null;
    }

    /**
     * Ignore output of tool (otherwise it will be copied to System.out).
     */
    public void setIgnoreOutput(boolean aIgnoreOutput) {
        ignoreOutput = aIgnoreOutput;
    }
    
    /**
     * Turn segmentation ON (true) or OFF (false). 
     * 
     * OFF mode (the default) is faster,
     * but ON mode generates smaller compressed files.
     */
    public void setSegment(boolean aSegment) {
        segment = aSegment;
        params = null;
    }

    /** When true will attempt to smooth out jagged edges in the image. 
     * 
     * On a scale from 1 to 7 indicating the type of smoothing you wish to 
     * apply. 0 means no smootihing (default).
     */
    public void setSmooth(int aSmooth) {
        smooth = aSmooth;
        params = null;
    }

    /** When true this will compress all image streams within the PDF file on 
     *  a stream-by-stream basis.
     */
    public void setStream(boolean aStream) {
        stream = aStream;
        params = null;
    }

    /**
     * Set the Timeout for OCR in Seconds.
     *   
     * 0 indicates no timeout.
     */
    public void setTimeout(int aTimeout) {
        timeout = aTimeout;
        params = null;
    }

    /**
     * Create an additional text file frm the OCR.
     * 
     * Default is true.
     */
    public void setText(boolean createText) {
        text = createText;
        params = null;
    }

    /**
     * Allows you to gain access to password-protected files.
     * 
     * @param aUserpw the passwrod to use for encryption
     */
    public void setUserpw(String aUserpw) {
        userpw = aUserpw;
        params = null;
    }

    /** Set the X-Resolution of compressed OutputFile.
     * 
     * for 0 the compression engine will attempt to automatically determine 
     * the resolution from the input file...
     */
    public void setXres(int aXRes) {
        xres = aXRes;
        params = null;
    }

    /**
     * Set the Y- Resolution of compressed OutputFile.
     * 
     * for 0 it will be the same as xres.
     */
    public void setYres(int aYRes) {
        yres = aYRes;
        params = null;
    }
    
    /** Example of usage */
    public static void main(String args[]) throws IOException, InterruptedException {
        
        CVistaPDFCompress compress = 
            new CVistaPDFCompress("C:\\Programme\\CVision\\PdfCompressor 3.1\\PDFCompress.exe");
        compress.setQuiet(true);
        compress.setLanguage("english");
        compress.setDescew (true);
        compress.setClobber(true);
        compress.setText   (true);
        // compress.setIgnoreOutput(true);
		File in = new File(ModuleLayoutConstants.SRC_TEST_DIR + "/test/com/top_logic/base/ocr/STR_039.pdf");
        File out = new File("tmp");
        if (!out.exists())
            out.mkdir();
        compress.run(in,out);
        // compress.run(in.getParentFile(),"pdf", out);
    }
}
