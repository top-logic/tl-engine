/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.office.excel;

import java.io.File;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.top_logic.base.office.AbstractOffice;
import com.top_logic.base.office.OfficeException;
import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.col.Mapping;
import com.top_logic.basic.col.Mappings;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.io.binary.BinaryData;

/**
 * Access class for MS-Excel.
 * 
 * This class will provide high level methods for reading and writing 
 * Excel files. High level means that values from the Excel can be read 
 * and written by simple {@link java.util.Map maps}.
 * 
 * The maps are expected to be the following:
 * 
 * <ul>
 *     <li>getValues(): <em>key</em>: The unique cell (as used in Excel). 
 *                      <em>value</em>: The found content of the cell.</li>
 *     <li>setValues(): <em>key</em>: The unique cell (as used in Excel).
 *                      <em>value</em>: The new content of the cell.</li>
 * </ul>
 * 
 * @see #newInstance()
 * 
 * @author     <a href="mailto:mga@top-logic.com">Michael G&auml;nsler</a>
 */
public abstract class ExcelAccess extends AbstractOffice {

	/**
	 * Global {@link ExcelAccess} configuration options.
	 */
	public interface GlobalConfig extends ConfigurationItem {

		/**
		 * The factory to use for creating {@link ExcelAccess} instances.
		 */
		@InstanceFormat
		@Mandatory
		ExcelAccessFactory getFactory();

	}

	/**
	 * A Factory for {@link ExcelAccess} instances.
	 */
	public interface ExcelAccessFactory {

		/**
		 * Creates a new {@link ExcelAccess} instance.
		 * 
		 * @see ExcelAccess#newInstance()
		 */
		ExcelAccess newInstance();

	}

    /** File extension for MS-Excel documents. */
    public static final String XLS_EXT = ".xls";

    /**
     * Protected CTor for the singleton pattern.
     */
    public ExcelAccess() {
        super();
    }

    /**
     * Return the file extension of excel documents (is {@link #XLS_EXT}).
     * 
     * @return    {@link #XLS_EXT} (is ".xls").
     * @see       com.top_logic.base.office.AbstractOffice#getExtension()
     */
    @Override
	public String getExtension() {
        return (ExcelAccess.XLS_EXT);
    }
    
	@Override
	public Mapping<?, ?> getStringMapping() {
		return Mappings.identity();
	}

    /**
     * Set the values found in the given map to the given stream and store the 
     * result in a new file with the given name.
     * 
     * This method will first create a copy of the given stream (having the 
     * given name) and replace all values in the copy.
     * 
     * @param    aSource    The source stream to make the replacements on.
     * @param    aDest      The name of the new file to be created.
     * @param    autoFit    Flag, if columns have to be auto fitted to matching width.
     * @return   <code>true</code>, if everything works fine.
     * @throws   OfficeException    If executing fails for a reason.
     */
    public abstract boolean setValuesDirect(InputStream aSource, File aDest, ExcelValue[] aValueList, boolean autoFit) throws OfficeException;

    /**
     * Return the values found in the given excel file on the defined sheet.
     * 
     * @param    aFile     The file to be inspected.
     * @param    aSheet    The sheet to be inspected.
     * @return   The array of values.
     * @throws   Exception If parsing fails for a reason.
     */
	public abstract Object[][] getValues(BinaryData aFile, String aSheet) throws Exception;

    /**
     * Automatically fit columns of first sheet in given document.
     */
    public abstract void autoFitColumns(File aFile) throws OfficeException;

    /**
	 * Extract the values defined in the given collection from the given excel file.
	 * 
	 * @param aFile
	 *        The excel file to extract the values from, must not be <code>null</code>.
	 * @param someCoords
	 *        A collection of coordinates (in format <code>[sheet name]![coord]</code>) to get the
	 *        values for.
	 * @return The map of found values.
	 */
	public Map<String, Object> getValues(BinaryData aFile, Collection<String> someCoords) {
        if (CollectionUtil.isEmptyOrNull(someCoords)) {
            return Collections.emptyMap();
        }
        else {
            Map<String, Set<String>> theCoords = createCoordMap(someCoords);

            return this.getValues(aFile, theCoords);
        }
    }

	/**
	 * Parse coordinate collection into a table / cell map
	 * 
	 * @param someCoords the coordinates. May be <code>null</code> or empty.
	 * @return the parsed values as a map. {@link Collections#emptyMap()} if someCoords is <code>null</code> or empty.
	 */
	public static Map<String, Set<String>> createCoordMap(Collection<String> someCoords) {
		if (someCoords == null) {
			return Collections.emptyMap();
		}
		
		Map<String, Set<String>> theCoords = new HashMap<>();

		for (String theString : someCoords) {
		    int thePos = theString.indexOf('!');

		    if (thePos > 0) {
		        String      theSheet = theString.substring(0, thePos);
		        String      theCoord = theString.substring(thePos + 1);
		        Set<String> theSet   = theCoords.get(theSheet);

		        if (theSet == null) {
		            theSet = new HashSet<>();

		            theCoords.put(theSheet, theSet);
		        }

		        theSet.add(theCoord);
		    }
		}
		return theCoords;
	}

    /** 
     * Extract the values defined in the given map from the given excel file.
     * 
     * @param    aFile         The excel file to extract the values from, must not be <code>null</code>.
     * @param    someCoords    The coordinates to extract the values from (Sheet name, Set of local coordinates).
     * @return   The map of found values.
     */
	public Map<String, Object> getValues(BinaryData aFile, Map<String, Set<String>> someCoords) {
        return Collections.emptyMap();
    }

    /** 
     * Same as {@link #setValuesDirect(InputStream, File, ExcelValue[], boolean)} (here with 'autoFit' <code>true</code>).
     * 
     * @see #setValuesDirect(InputStream, File, ExcelValue[], boolean)
     */
    public boolean setValuesDirect(InputStream aSource, File aDest, ExcelValue[] aValueList) throws OfficeException {
        return this.setValuesDirect(aSource, aDest, aValueList, true);
    }

	/**
	 * A new {@link ExcelAccess} instance for export.
	 */
	public static ExcelAccess newInstance() {
		return ApplicationConfig.getInstance().getConfig(GlobalConfig.class).getFactory().newInstance();
	}

}
