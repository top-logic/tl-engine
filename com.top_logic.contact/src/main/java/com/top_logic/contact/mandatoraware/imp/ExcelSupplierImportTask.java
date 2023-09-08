/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.contact.mandatoraware.imp;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Properties;

import com.top_logic.basic.FileManager;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.element.structured.wrap.Mandator;
import com.top_logic.util.sched.task.impl.TaskImpl;

/**
 * Wrap excel supplier importer in a Task to execute it periodically.
 * 
 * @author    <a href=mailto:kbu@top-logic.com>kbu</a>
 */
public class ExcelSupplierImportTask extends TaskImpl {

	ExcelSupplier20060529Importer importBatch;
	
    protected String fileNamePattern;
    boolean doCreate;
    boolean isDeltaImport;
    boolean isSupplierImport;
    
	public ExcelSupplierImportTask(Properties prop) {
		super(prop);
		
        fileNamePattern  = prop.getProperty("fileNamePattern");
        doCreate         = Boolean.valueOf(prop.getProperty("create",           "true"));
        isDeltaImport    = Boolean.valueOf(prop.getProperty("isDeltaImport",    "false"));
        isSupplierImport = Boolean.valueOf(prop.getProperty("isSupplierImport", "true"));
	}
	
	@Override
	public void run() {
		super.run();
		
		BinaryData importFile = this.getImportFile();
		if (importFile != null) {
			importBatch = new ExcelSupplier20060529Importer(importFile, this.doCreate, this.isDeltaImport, this.isSupplierImport,
				Mandator.getRootMandator());
			importBatch.run();
		}		
	}
	
	/**
	 * get the import file replacing YY MM DD in the name by the current date bits. Returns <code>null</code> if the file does not exist
	 */
	protected BinaryData getImportFile() {
		GregorianCalendar theCal = new GregorianCalendar();
		String theYear = "0" + (theCal.get(Calendar.YEAR) % 100);
		if (theYear.length() > 2) theYear = theYear.substring(1);
		String theName = this.fileNamePattern.replace("YY", theYear);
		String theMonth = "0" + (theCal.get(Calendar.MONTH) + 1);
		if (theMonth.length() > 2) theMonth = theMonth.substring(1);
		theName = theName.replace("MM", theMonth);
		String theDay = "0" + (theCal.get(Calendar.DAY_OF_MONTH));
		if (theDay.length() > 2) theDay = theDay.substring(1);
		theName = theName.replace("DD", theDay);
		
		return FileManager.getInstance().getDataOrNull(theName);
	}

	@Override
	public void signalShutdown() {
		super.signalShutdown();
		importBatch.signalShutdown();
	}
	
	@Override
	public boolean signalStopHook() {
		return importBatch.signalStop();
	}
	
}
