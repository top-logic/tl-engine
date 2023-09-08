/*
 * SPDX-FileCopyrightText: 2000 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dsa.repos;

/** Information about an entry or container in a Repository.
 *
 * You think of this class as a Readonly-Wrapper for File,
 * which may or may not be the actual implementation.
 * <p>
 * Classes implementing this interface are handled to UI-code
 * via the {@link com.top_logic.dob.bean.BeanDataObject}, 
 * therefore the names of the properties are found here, too
 * </p>
 *
 * @author    <a href="mailto:tsa@top-logic.com">Klaus Halfmann</a>
 */
public interface RepositoryInfo {
    
    /**
     * Get (leaf) name of the entry (file / directory).
     *
     * @return the name of the entry
     */
	public String   getName();

	/**
     * Get complete path name of the entry (file / directory).
     *
     * @return complete path name of the entry
     */
	public String   getPath();

	/**
     * Get parent path of the entry (file / directory).
     *
     * @return the parent path of the entry
     */
	public String   getParentPath();

	/** Same as  {@link java.io.File#length() } for Top-Level Revision. */
	public long     getSize() throws RepositoryException;

    /** Similar to {@link java.io.File#isFile() } for Top-Level Revision. */
    public boolean getIsEntry();
    
    /**  Similar to {@link java.io.File#isDirectory() } for Top-Level Revision. */
    public boolean getIsContainer();

    /**
     * Get the the modification Date for the current version,
     * corresponding to the top-level revision.
     *
     * @return Date of last modification
     */
    public long getLastModified () throws RepositoryException;

    /**
     * Is the entry deleted?
     * 
     * @return true when the top-level revision is marked as deleted
     */
    public boolean  isDeleted() throws RepositoryException;

	/**
	 * Update the flag in case intended Modifications where made.
	 */
	public void updateModified();

	/**
	 * Check if the represented repository entry has been changed since last modified.
	 * 
	 * @return <code>true</code> when version information differs from {@link #getLastModified()}.
	 */
	public boolean checkModified();
}
