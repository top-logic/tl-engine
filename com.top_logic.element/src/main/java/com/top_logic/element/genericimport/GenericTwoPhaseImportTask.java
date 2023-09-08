/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.genericimport;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;

import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.FileManager;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.element.genericimport.interfaces.GenericCache;
import com.top_logic.util.sched.task.impl.TaskImpl;

/**
 * @author    <a href="mailto:TEH@top-logic.com">TEH</a>
 */
public class GenericTwoPhaseImportTask extends TaskImpl {
    
    private String importPath;
    
    public GenericTwoPhaseImportTask(Properties someProps) {
        super(someProps);
        importPath = someProps.getProperty("importPath", "/WEB-INF/init/");
    }
    
    @Override
	public void run() {
		File theImportDir = FileManager.getInstance().getIDEFile(importPath);
		File theConfigFiles[];
		try {
			theConfigFiles = FileUtilities.listFiles(theImportDir, new FileFilter() {
				/**
				 * List all configuration .xml files.
				 * 
				 * @see java.io.FileFilter#accept(java.io.File)
				 */
				@Override
				public boolean accept(File aPathname) {
					return aPathname.getName().endsWith(".xml");
				}
			});
		} catch (IOException ex) {
			throw new ConfigurationError("Unable to list directory: " + ex.getMessage(), ex);
		}
        
        List<File> theFiles = Arrays.asList(theConfigFiles);
        Collections.sort(theFiles, new Comparator<File>() {
            @Override
			public int compare(File aO1, File aO2) {
                return aO1.getName().compareTo(aO2.getName());
            }
        });
        
        int theSize = theFiles.size();
        GenericDataImportTask theImporters[] = new GenericDataImportTask[theSize];
        GenericCache theGlobalCache = new MetaElementBasedCache(new Properties());
        
        for (int i = 0; i < theSize; ++i) {
            Properties theProps = new Properties();
            
            theProps.setProperty("name"      , "GenericDataImporter");
            theProps.setProperty("daytype"   , "ONCE");
            theProps.setProperty("hour"      , "02");
            theProps.setProperty("minute"    , "22");
            theProps.setProperty("configFile", importPath + theFiles.get(i).getName());
        
            theImporters[i] = new GenericDataImportTask(theProps);
            theImporters[i].init();
            theImporters[i].doImport();
            theGlobalCache.merge(theImporters[i].getImportConfiguration().getCache());
            theImporters[i].getImportConfiguration().getCache().reload();
        }
    }
}

