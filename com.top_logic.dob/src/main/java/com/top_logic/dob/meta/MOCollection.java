
/*
 * SPDX-FileCopyrightText: 2000 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dob.meta;

import java.util.regex.Pattern;

import com.top_logic.dob.MetaObject;

/**
 * This is a template for classes....
 *
 * @author  Marco Perra
 */
public interface MOCollection extends MetaObject {

	/**
	 * Raw type of {@link MOCollection}s that define any collections.
	 */
    public static final String COLLECTION = "COLLECTION";

    /**
     * Raw type of {@link MOCollection}s that define collections with intrinsic
     * order.
     */
    public static final String LIST = "LIST";
    
	/**
	 * Raw type of {@link MOCollection}s that define collections with unique
	 * entries.
	 */
    public static final String SET = "SET";

	/**
	 * Pattern that matches the name of an {@link MOCollection}.
	 * 
	 * @see #getName()
	 */
	Pattern MO_COLLECTION_NAME_PATTERN = Pattern.compile("(" + COLLECTION + "|" + LIST + "|" + SET + ")<(.+)>");
    
    /**
     * Return the types of the element that this collection holds.
     */
    public MetaObject getElementType();

    /**
     * One of {@link #COLLECTION}, {@link #LIST}, or {@link #SET}.
     */
	String getRawType();
	
	/**
	 * The name of the {@link MOCollection} suffices the
	 * {@link #MO_COLLECTION_NAME_PATTERN name pattern}. It has the form
	 * "<i>rawType</i>&lt;<i>entryType</i>&gt;" where <i>rawType</i> is
	 * {@link #COLLECTION}, {@link #LIST}, or {@link #SET} (the
	 * {@link #getRawType() raw type}) and <i>entryType</i> is the name of the
	 * {@link #getElementType() element type}.
	 * 
	 * @see com.top_logic.dob.MOPart#getName()
	 */
	@Override
	String getName();

}
