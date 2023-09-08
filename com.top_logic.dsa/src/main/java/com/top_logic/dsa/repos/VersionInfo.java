/*
 * SPDX-FileCopyrightText: 2000 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dsa.repos;

import java.util.Date;

/** Information about a version of an entry in a Repository.
 *
 * Returns informations about a particular version of a Repository entry,
 * represented by {@link EntryInfo}.
 *
 * @author    <a href="mailto:kha@top-logic.com">Klaus Halfmann</a>
 */
public interface VersionInfo {

    /**
     * Get the the modification Date for the version,
     *
     * @return Date of last modification
     */
    public Date getLastModified () throws RepositoryException;

	/**
     * Same as {@link java.io.File#length()}.
     *
     * @return the file size
     */
	public long getSize();

	/**
     * Return true when version is marked a deleted entry.
     *
     * @return true if the version marked as deleted, else false
     */
	public boolean isDeleted();

	/**
     * Get the name of the author.
     *
     * @return the name of the author
     */
	public String getAuthor ();

    /**
     * Get the name of the entry.
     *
     * @return the name of the entry.
     */
    public String getName ();

	/**
	 * Return revision number parsed from file name.
	 *
	 * @return revision number parsed from file name
	 */
	public int getVersion();
}
