/*
 * SPDX-FileCopyrightText: 2002 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.data.base.description;

/**
 * The description provides meta information about the data.
 * 
 * So the description defines not only name and type of the data 
 * to be retrieved but also which operators can be used on data values
 * of the type specified by this description.
 * 
 * @author    <a href="mailto:tri@top-logic.com">tri</a>
 */
public interface Description {

	/**
	 * Return the type of object you can expect from the value holder.
     *
     * This method is needed to know, how the returned data from the value
     * holder can be operated within.
     * 
	 * @return    The type of the described Data, must not be <code>null</code>.
	 */
	public Class getType();


	/**
	 * The name returned by this method identifies the description internally.
     * 
     * Because this value will be used for identifying the object, this
     * method must not return <code>null</code>.
     * 
     * @return    The Name (ID) of this description (not <code>null</code>).
	 */
	public String getName();

	/**
	 * To be used to show this description in GUIs.
     * 
     * @return    The display name of this description, must not be
     *            <code>null</code>.
	 */
	public String getDisplayName();
	
	/**
	 * the number of entries in the described values
	 */
	public int getNumberOfValueEntries();

    /**
     * Returns the entries.
     */
    public String[] getEntries();
}
