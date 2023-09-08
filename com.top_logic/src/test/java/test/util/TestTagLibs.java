/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.util;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.HashSet;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.top_logic.basic.tooling.ModuleLayoutConstants;
import com.top_logic.basic.xml.DOMUtil;

/**
 * This tool checks the entries in tag-lib files (.tld)
 *
 * @author    <a href="mailto:tsa@top-logic.com">Theo Sattler</a>
 */
public class TestTagLibs {
    
    /** the extension of the file to check */
    public static final String FILE_EXTENSION = ".tld"; 

    /** The base directory to check. */
    String baseDirName = "webapps/top-logic/WEB-INF/tlds";
    
    /** number of faulty files found. */
    int faultyFileCount;
    /** number of checkt files. */
    int fileCount;
    /** number of faulty tags found. */
    int faultyTagCount;
    /** number of checkt tags. */
    int tagCount;
    
    /** the destination for the output. */
    PrintStream out = System.out;


    /** Map of directories to ignore */
    Set ignoreSet;
    

    /** 
     * Constructor. 
     */
    public TestTagLibs ()  {
        ignoreSet = new HashSet();
    }
    /**
     * Update the copyright entry of all java source files in a subtree (baseDirName).
     */
    public void startTreeUpdate() {
        fileCount = 0;
        faultyFileCount = 0;
        tagCount = 0;
        faultyTagCount = 0;
        this.out.println();
        this.out.println("Tag-check started.");
        this.out.println("Base directory = " + baseDirName);
        this.out.println();

        File theComBaseDir = new File (baseDirName);
        handleDirectory(theComBaseDir);
        
        this.out.println("Tag-check done."); 
        this.out.println("    Total    Files: " + fileCount);
        this.out.println("    Faulty   Files: " + faultyFileCount);
        this.out.println("    Total    Tags:  " + tagCount);
        this.out.println("    Faulty   Tags:  " + faultyTagCount);
        this.out.println("    % faulty files: " + ((double)faultyFileCount/fileCount*100));
        this.out.println("    % faulty tags:  " + ((double)faultyTagCount/tagCount*100));
    }
    
    /**
     * Tests the comment of all files in a directory and 
     * calls the test for all sub directories.
     * 
     * @param aDir  the directory to start testing. 
     */
    protected void handleDirectory (File aDir) {
        int count;
        File[] theJavaFiles = aDir.listFiles(FILE_DIR_FILTER);
        count = theJavaFiles.length;
        for (int i=0; i<count; i++) {
            File theFile = theJavaFiles[i];
            if (theFile.isFile())
                handleFile(theJavaFiles[i]);
            else { // theFile.isDirectory()
                if (!ignoreSet.contains(theFile.getName()))
                    handleDirectory(theFile);
            }
        }
    }

    /**
     * Handles a single java file.
     */
    protected void handleFile(File aFile) {
        try {
            fileCount++;
            
            InputStream theFileInputStream = new FileInputStream(aFile);
			DocumentBuilder theBuilder = DOMUtil.newDocumentBuilder();
            Document theFileDocument = theBuilder.parse (theFileInputStream);

            NodeList theTags = theFileDocument.getElementsByTagName("tag");
            
            int theTagsLength = theTags.getLength();
            tagCount +=theTagsLength;
            
            boolean fileIsFaulty = false;
            
            this.out.println(aFile.getName() + ":");
            
            for (int i=0; i<theTagsLength; i++) {
                Node theCurrentTag = theTags.item(i);
                NodeList theTagChilds = theCurrentTag.getChildNodes();
                String theTagName = "";
                String theTagFileName = "";
                boolean tagIsFaulty = false;
                for (int j=0; j<theTagChilds.getLength(); j++) {
                    Node theChild = theTagChilds.item(j);
                    if (theChild.getNodeName().equals("tagclass")) {
                        theTagFileName = theChild.getFirstChild().toString();
                        if (!checkFile(theTagFileName)) {
                            faultyTagCount++;
                            fileIsFaulty = true;
                            tagIsFaulty = true;
                        }
                    }
                    if (theChild.getNodeName().equals("name")) {
                        theTagName = theChild.getFirstChild().toString();
                    }
                }
                this.out.print("  ");
                if (tagIsFaulty) {
                    this.out.print("Faulty: ");
                } else {
                    this.out.print("OK: ");
                }
                this.out.print(theTagName);
                this.out.print(" (");
                this.out.print(theTagFileName);
                this.out.print(')');
                this.out.println();
            }
            
            if (fileIsFaulty) {
                faultyFileCount++;
            }
        } catch (Exception e) { 
           this.out.println("ERROR: could not handle file "+aFile.getPath());
        }
            
    }
    
    protected boolean checkFile(String aFileName) {
		String thePath = ModuleLayoutConstants.SRC_MAIN_DIR + "/" + aFileName.replaceAll("\\.", "/") + ".java";
        File theFile = new File(thePath);
        return theFile.exists();
    }

    /**
     * Filter for extracting java src files.
     */
    public static final FileFilter FILE_DIR_FILTER = new FileFilter() {
        @Override
		public boolean accept(File aFile) {
            String name = aFile.getName();
            return name.endsWith(FILE_EXTENSION) || 
                (aFile.isDirectory() && !"CVS".equals(name));
        }
    };

    /**
     * Start all necessary updates.
     */
    public void startCheck() {
        startTreeUpdate();
    }

    public static void main(String[] args) {
        TestTagLibs tester = new TestTagLibs();
        tester.startCheck();
    }
}
