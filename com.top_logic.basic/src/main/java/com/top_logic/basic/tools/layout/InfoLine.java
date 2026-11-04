/*
 * SPDX-FileCopyrightText: 2004 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.tools.layout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.top_logic.basic.StringServices;

/**
 * Contains information for a single entry of a level in a layout.
 * 
 * @author    <a href="mailto:tsa@top-logic.com">Theo Sattler</a>
 */
public class InfoLine extends HashMap {

    /**
     * Key of the level of the entry.
     */
    public static final String LEVEL = "level";
    /**
     * Key of the name of the entry.
     */
    public static final String NAME  = "name";
    /**
     * Key of the resource key of the entry.
     */
    public static final String KEY   = "key";
    /**
     * Key of the image of the entry.
     */
    public static final String IMAGE = "image"; 
    /**
     * Key of the layout file of the entry.
     */
    public static final String FILE  = "file"; 
    /**
     * Key of the types of the entry, separated by <code>&amp;</code>.
     */
    public static final String TYPE  = "type"; 
    /**
     * Key of the template of the entry.
     */
    public static final String TEMPLATE  = "template"; 
    /**
     * Key of the master of the entry.
     */
    public static final String MASTER  = "master"; 
    /**
     * Key of the flag whether the default checker is used.
     */
    public static final String USE_DEFAULT_CHECKER  = "defaultChecker"; 
    /**
     * Key of the domain of the entry.
     */
    public static final String DOMAIN  = "domain"; 

	/**
	 * Key of the project the entry is generated into.
	 */
	public static final String TARGET_PROJECT = "targetProject";

	/**
	 * Key of the path the entry is generated into.
	 */
	public static final String TARGET_PATH = "path";
    
	private final List<InfoLine> _children = new ArrayList<>();
	private InfoLine _nextLine;       

	public InfoLine(String aLevel, String aName, String aFile, String aKey, String anImage, String aType, String aSize,
			String aTemplate, String aMaster, String aUseDefaultChecker, String aDomain, String targetProject,
			String targetPath) {
		super();
        this.put(LEVEL,               StringServices.nonEmpty(aLevel));
        this.put(NAME,                StringServices.nonEmpty(aName));
        this.put(FILE,                StringServices.nonEmpty(aFile));
        this.put(KEY,                 StringServices.nonEmpty(aKey));
        this.put(IMAGE,               StringServices.nonEmpty(anImage));
        this.put(TYPE,                StringServices.nonEmpty(aType));
        this.put(TEMPLATE,            StringServices.nonEmpty(aTemplate));
        this.put(MASTER,              StringServices.nonEmpty(aMaster));
        this.put(USE_DEFAULT_CHECKER, StringServices.nonEmpty(aUseDefaultChecker));
        this.put(DOMAIN,              StringServices.nonEmpty(aDomain));
		this.put(TARGET_PROJECT, StringServices.nonEmpty(targetProject));
		this.put(TARGET_PATH, StringServices.nonEmpty(targetPath));
    }
    
    public InfoLine (Map someValues) {
        super(someValues);
    }
    
    /**
     * The value of the given key in the given line.
     *
     * @param infoLine
     *        The line to look up, may be <code>null</code>.
     * @param aKey
     *        The key of the value.
     * @return The value, or <code>null</code> if no line is given.
     */
    public static String value(InfoLine infoLine, String aKey) {
    	if (infoLine == null) {
    		return null;
    	}
		return (String) infoLine.get(aKey);
	}

    @Override
	public String toString() {
        return "InfoLine: " + super.toString();
    }
    
    /**
     * Adds an entry on the next level.
     */
    public void addChild(InfoLine line) {
		_children.add(line);
	}

	/**
	 * Whether there are entries on the next level.
	 */
	public boolean hasChildren() {
		return ! _children.isEmpty();
	}
	
	/**
	 * The entries on the next level.
	 */
	public List<InfoLine> getChildren() {
		return _children;
	}

	/**
	 * Setter for {@link #getNextSibbling()}.
	 */
	public void setNextSibbling(InfoLine nextLine) {
		_nextLine = nextLine;
	}

	/**
	 * The next entry on the same level.
	 */
	public InfoLine getNextSibbling() {
		return _nextLine;
	}

	/**
	 * The types of the given line.
	 *
	 * @see #TYPE
	 */
	public static Set<String> getTypes(InfoLine aLine) {
		if (aLine == null) {
			return Collections.emptySet();
		}
		String typeSpec = value(aLine, TYPE);
		if (StringServices.isEmpty(typeSpec)) {
			return Collections.emptySet();
		} else {
			return StringServices.toSet(typeSpec, '&');
		}
	}
}
