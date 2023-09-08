/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.wrap;

import java.util.Arrays;
import java.util.Collection;
import java.util.Random;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.module.ModuleException;
import com.top_logic.dsa.DataAccessProxy;
import com.top_logic.dsa.DatabaseAccessException;

/**
 * COS Specific variant of the WebFolderFactory.
 * 
 * @author    <a href=mailto:kha@top-logic.com>kha</a>
 */
public class TreeWebfolderFactory extends WebFolderFactory {
    
	private static final String FOLDER_NAME_PREFIX = "topl";

	/** Max depth of folder tree until real WebFolder creation happens. */
    protected static final int MAX_DEPTH = 4;
    
    /** Max number of sub folders per folder. */ 
	protected static final int MAX_WIDTH = 256;
    
    /** Max text length of formatted numbers. */
    protected static final int MAX_LEN = ("" + (MAX_WIDTH - 1)).length();
    
	private final Random _random = new Random();

	/**
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        Configuration for {@link TreeWebfolderFactory}.
	 * @throws ModuleException
	 *         When "repository://" is invalid (not existent or not a folder).
	 */
	public TreeWebfolderFactory(InstantiationContext context, Config config) throws ModuleException {
		super(context, config);
	}

    /**
	 * Collection with allowed folder types.
	 * 
	 * @see com.top_logic.knowledge.wrap.WebFolderFactory#createAllowedFolderTypes(Config)
	 */
    @Override
	protected Collection<String> createAllowedFolderTypes(Config config) {
    	if(config.getFolderTypes().isEmpty()) {
			return Arrays.asList(STANDARD_FOLDER);
    	} else {
        	return config.getFolderTypes();	
    	}
    }

    /**
     * Creates a folder tree of MAX_DEPTH depth and MAX_WIDTH width
     * to avoid too many folders in one directory.
     * 
     * @see com.top_logic.knowledge.wrap.WebFolderFactory#createUniqueFolderDSN()
     */
    @Override
	protected String createUniqueFolderDSN() throws DatabaseAccessException {

        DataAccessProxy theRoot = getDocFolderRoot();
		if (!theRoot.exists() && theRoot.isContainer()) {
			throw new DatabaseAccessException("Invalid Root Folder " + theRoot);
        }

        // Set up initial folder creation
        int[]     theNames   = new int    [MAX_DEPTH];
        boolean[] theCreates = new boolean[MAX_DEPTH];
        
        // Current max
        // Add 1
		this.calcFolderCreation(theRoot, 0, theNames, theCreates);
        
        DataAccessProxy theFolder = theRoot;
        boolean theCreate = false;
        for (int i=0; i<MAX_DEPTH; i++) {
            // if a folder has to be created all sub folder will have to be created as well ;-)
            theCreate |= theCreates[i];
			String theName = folderName(theNames[i], i);
            
            if (theCreate) {
                theFolder = theFolder.createContainerProxy(theName);
            }
            else {
                theFolder = theFolder.getChildProxy       (theName);
            }
        }

        return theFolder.getPath();
    }

	private String folderName(int name, int depth) {
		return FOLDER_NAME_PREFIX + stringFromInt(name, depth);
	}

	private String stringFromInt(int i, int depth) {
		return Integer.toString(i, radix(depth));
	}

	private int intFromString(String s, int depth) {
		return Integer.parseInt(s, radix(depth));
	}

	private int radix(int depth) {
		/* At top level currently all folders have increasing number. It is possible that more than
		 * 256 are contained, therefore it must currently interpreted as decimal number. */
		return depth == 0 ? 10 : 16;
	}

	/**
	 * Get the Number from a folder name.
	 * 
	 * @param aString
	 *        the folder name
	 * @param depth
	 *        The depth of the folder in the tree
	 * @return the number, -1 if parsing fails
	 */
	private int getNumberFromString(String aString, int depth) {
        int theNumber = -1;
        try  {
			int theIndex = aString.lastIndexOf(FOLDER_NAME_PREFIX);
            if (theIndex < 0) {
                // in case there are non topl* files (e.g. .cvsignore)
                return -1;
            }
			String theNum = aString.substring(theIndex + FOLDER_NAME_PREFIX.length(), aString.length());
			theNumber = intFromString(theNum, depth);
        }
        catch (Exception ex) {
            // Ignore, return -1 below
        }
        
        return theNumber;
    }

    /**
     * Calculate the folder creation info
     * 
     * @param aDocFolderRoot    the current folder
     * @param aDepth            the current depth
     * @param aNames            the folder number list
     * @param aCreates          the folder creation info
     */
    private void calcFolderCreation(DataAccessProxy aDocFolderRoot, int aDepth, int[] aNames, boolean[] aCreates) throws DatabaseAccessException {
        if (aDepth == MAX_DEPTH - 1) {
			calcLeafFolderCreation(aDocFolderRoot, aDepth, aNames, aCreates);
		} else {
			calcNonLeafFolderCreation(aDocFolderRoot, aDepth, aNames, aCreates);
		}

	}

	private void calcNonLeafFolderCreation(DataAccessProxy aDocFolderRoot, int aDepth, int[] aNames, boolean[] aCreates)
			throws DatabaseAccessException {
		String[] theNames = aDocFolderRoot.getEntryNames();
		int theMaxNumber = getMaxChildNumber(theNames, aDepth);

		if (theMaxNumber < 0) {
			// No child -> have to create -> stop here
			aNames[aDepth] = 0;
			aCreates[aDepth] = true;
			return;
		}

		aNames[aDepth] = theMaxNumber;
		DataAccessProxy child = aDocFolderRoot.getChildProxy(folderName(theMaxNumber, aDepth));
		this.calcFolderCreation(child, aDepth + 1, aNames, aCreates);

		// Check number overflow
		if (aDepth > 0) { // Allow overflow on top level
			int theNumber = aNames[aDepth];
			if (theNumber >= MAX_WIDTH) {
				overflow(aDepth, aNames, aCreates);
			}
		}
	}

	private void overflow(int aDepth, int[] aNames, boolean[] aCreates) {
		int theSuperNumber = aNames[aDepth - 1];
		aNames[aDepth] = 0;
		aNames[aDepth - 1] = theSuperNumber + 1;
		aCreates[aDepth - 1] = true;
	}

	private int getMaxChildNumber(String[] childNames, int depth) {
		// Find the max child
		int theMaxNumber = -1;
		if (childNames != null && childNames.length != 0) {
			for (int i = 0; i < childNames.length; i++) {
				int theNum = this.getNumberFromString(childNames[i], depth);
				if (theMaxNumber < theNum) {
					theMaxNumber = theNum;
				}
			}

		}
		return theMaxNumber;
	}

	private void calcLeafFolderCreation(DataAccessProxy aDocFolderRoot, int aDepth, int[] aNames, boolean[] aCreates)
			throws DatabaseAccessException {
		String[] existingNames = aDocFolderRoot.getEntryNames();
		if (existingNames != null && existingNames.length >= MAX_WIDTH) {
			aNames[aDepth] = MAX_WIDTH;
			overflow(aDepth, aNames, aCreates);
		} else {
			/* It is extremely fantastic to fetch a random integer equal to one of the 256 existing
			 * more than 100 times */
			assert MAX_WIDTH <= 256;
			int retry = 99;
			while (retry-- > 0) {
				int newName = _random.nextInt();
				DataAccessProxy potentialChild = aDocFolderRoot.getChildProxy(folderName(newName, aDepth));
				if (!potentialChild.exists()) {
					aNames[aDepth] = newName;
					break;
				}
			}
			if (retry == 0) {
				aNames[aDepth] = MAX_WIDTH;
			}

		}
		aCreates[aDepth] = true;
	}
}
