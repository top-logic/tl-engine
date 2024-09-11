/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.gui.layout.upload;

import java.util.List;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.util.ResKey;

/**
 * Default implementation of {@link com.top_logic.knowledge.gui.layout.upload.FileNameStrategy}.
 * 
 * @author    <a href="mailto:skr@top-logic.com">Sylwester Kras</a>
 */
public class SimpleFileNameStrategy implements FileNameStrategy {

	/** one and only instance */
    public static SimpleFileNameStrategy INSTANCE =  new SimpleFileNameStrategy();
    
    /** Characters that are not allowed in a filename. */
    public static String INVALID_CHARACTERS = "?*\\/:\"<>|";

	private List<String> blackList;

	private List<String> whiteList;
    private boolean ignoreCase;
    private boolean trim;

    /** CTor. */
    private SimpleFileNameStrategy() {
    	this(null, null);
    }
    
    /**
	 * Creates a {@link SimpleFileNameStrategy} with the given black and white list.
	 */
	public SimpleFileNameStrategy(List<String> aBlackList, List<String> aWhiteList) {
    	this(aBlackList, aWhiteList, true, true);
    }
    
    /**
	 * Creates a {@link SimpleFileNameStrategy} with the given black and white list.
	 */
	public SimpleFileNameStrategy(List<String> aBlackList, List<String> aWhiteList, boolean ignoreCase, boolean trim) {
    	super();
		this.blackList = aBlackList;
		this.whiteList = aWhiteList;
    	this.trim       = trim;
    	this.ignoreCase = ignoreCase;
    }

    @Override
	public ResKey checkFileName(String fileName) {
    	String newFileName = fileName;
    	
    	if (this.trim && !StringServices.isEmpty(newFileName)) {
    		newFileName = newFileName.trim();
    	}
    	
        // check black list
		List<String> theBlackList = getListWithNotAllowedNames();
		if (theBlackList != null) {
			if (contains(theBlackList, newFileName)) {
				return I18NConstants.ERROR_WRONG_FILENAME__DISALLOWED.fill(
					StringServices.join(theBlackList, ", "));
			}
		}

        // check white list
		List<String> theWhiteList = getListWithAllowedNames();
		if (theWhiteList != null) {
			if (!contains(theWhiteList, newFileName)) {
				return I18NConstants.ERROR_WRONG_FILENAME__FILENAMES.fill(
					StringServices.join(theWhiteList, ", "));
			}
		}
        
        // check for invalid characters
		boolean containsInvalid = StringServices.containsChar(newFileName, INVALID_CHARACTERS);
		if (containsInvalid) {
			return I18NConstants.ERROR_FILENAME_WITH_INVALID_CHAR.fill(INVALID_CHARACTERS);
		}

		return null;
    }

	private boolean contains(List<String> nameList, String name) {
		if (ignoreCase) {
			for (String element : nameList) {
				if (name.equalsIgnoreCase(element)) {
					return true;
				}
			}
			return false;
		} else {
			return nameList.contains(name);
		}
	}

	/**
	 * Here you must return all the names which are not allowed as new entries. This method is
	 * called by a JSP for clientSide check.
	 * 
	 * the case is handled by the caller of this method. there the comparison works:
	 * 
	 * - without case matching to allow an upload of files with differences in the case of
	 * filenames.
	 * 
	 * - with case matching not to allow an upload of files with the same filename, without caring
	 * about the case, like DOS extension called M$-Windows does.
	 * 
	 * (this interacts with getListWithAllowedNames)
	 * 
	 * @return a List with all the not allowed names. <code>null</code> or an empty list signals
	 *         there are not not-allowed names.
	 */
	public List<String> getListWithNotAllowedNames() {
        return this.blackList;
    }

	/**
	 * Returned names are the names, which are allowed. No other name will be allowed. (this
	 * interacts with getListWithNotAllowedNames)
	 * 
	 * @return the list with all allowed names. In case <code>null</code>, all names will be
	 *         allowed.
	 */
	public List<String> getListWithAllowedNames() {
        return this.whiteList;
    }
    
    /** singleton 
     *
     * @deprecated use {@link #INSTANCE} directly.
     */
    @Deprecated
	public static synchronized FileNameStrategy getInstance() {
        return INSTANCE;
    }
    

}
