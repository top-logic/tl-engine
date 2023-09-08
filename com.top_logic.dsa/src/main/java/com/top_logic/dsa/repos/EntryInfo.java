/*
 * SPDX-FileCopyrightText: 2000 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dsa.repos;



/** Information about an entry in a Repository.
 *
 * You think of this class as a Readonly-Wrapper for File,
 * which may or may not be the actual implementation.
 * <p>
 * Classes implementing this interface are handled to UI-code
 * via the {@link com.top_logic.dob.bean.BeanDataObject}, 
 * therefore the names of the properties are found here, too
 * </p>
 *
 * @author    <a href="mailto:kha@top-logic.com">Klaus Halfmann</a>
 */
public interface EntryInfo extends RepositoryInfo {

    /**
     * Is the entry locked?
     * Locks by the current user are included.
     *
     * @return true if locked, else false
     */
	public boolean  isLocked() throws RepositoryException;

	/**
     * Get the name of the locker.
     *
     * @return name of locker, or null when file is not locked
     */
	public String   getLocker() throws RepositoryException;

	/**
     * Get the author of the top-level revision.
     *
     * @return name of author for top-level revision
     */
	public String   getAuthor() throws RepositoryException;

    /**
     * Get the size of the latest revision.
     * 
     * @return the size of the latest revision
     */
    @Override
	public long     getSize() throws RepositoryException;

	/**
     * Get the number of versions.
     *
     * @return the number of versions
     */
	public int      getNumVersions () throws RepositoryException;

    /**
     * Get the number of versions visible for a given user.
     *
     * @return the number of versions
     */
    public int      getNumVersions (String aUser) throws RepositoryException;

	/**
     * Get the {@link VersionInfo}
     * corresponding to the top-level revision.
     *
     * @return VersionInfo for top-level revision
     */
	public VersionInfo getLastVersionInfo () throws RepositoryException;

    /**
     * Get the last modification date.
     * 
     * @return    The last modification date.
     */
    @Override
	public long getLastModified() throws RepositoryException;

	/**
     * Get the {@link VersionInfo}
     * corresponding to the specified revision.
     *
     * @param revisions the revision
     *
     * @return VersionInfo for the specified revision
     */
	public VersionInfo getVersionInfo (int revisions)
        throws RepositoryException;
		
    /**
     * Redeclared to make BeanDataObject happy.
     */
    @Override
	public String getName();

    /**
     * Return true when there is some private/locked version.
     *
     * @return true when entry has an updated revision
     */
    public boolean isInUpdate() throws RepositoryException;

	/**
	 * Helper function to extract _0_version information.
	 * 
	 * Is used to reload the Info (on File System changes), too.
	 *
	 * @throws RepositoryException
	 *         normally as Result of an IOException.
	 */
	public void readInfo() throws RepositoryException;

	/**
	 * Return the number of revisions.
	 *
	 * @return the number of revisions
	 */
	public int getVersions() throws RepositoryException;

	/**
	 * This function is called from the Repository to lock a revision.
	 *
	 * @param forUser
	 *        the user doing the locking
	 *
	 * @return the current version number.
	 */
	public int lock(String forUser) throws RepositoryException;

	/**
	 * This function is called from the repository to unlock a revision.
	 *
	 * @return the current version number.
	 */
	public int unlock() throws RepositoryException;

	/**
	 * This function is called from the repository to unlock a revision in delete or unlock.
	 */
	public void locallyUnlock();

	/**
	 * Helper function to write back _0_version information
	 *
	 * @throws RepositoryException
	 *         normally as Result of an IOException.
	 */
	public void writeInfo() throws RepositoryException;

	/**
	 * Is called from the Repository when a new revision is created.
	 *
	 * It will clear the locked and deleted Flags and write back the values.
	 *
	 * @param forUser
	 *        the use doing the creation
	 *
	 * @return the new version number created.
	 */
	public int newRevision(String forUser) throws RepositoryException;
}
