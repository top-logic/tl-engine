/*
 * SPDX-FileCopyrightText: 2000 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dsa.repos;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;

/**
 * Interface for defining the different operations executable on a repository.
 *<p>
 *  Directories / Entries are separated by
 * {@link java.io.File#pathSeparatorChar}.
 *</p>
 *
 *            2001.02.15    fma    changed new methods after discussion with kbu
 *            2001-12-10    kha    Redesign after respecification.
 *
 * @author     Klaus Halfmann / Michael Gänsler
 */
public interface Repository {

    /**
     * Give numer of the current top-level revision, starting at 1.
     * 
     * In case the Entry is <code>inUpdate</code> for the given user
     * it will one more than in other cases.
     *
     * @param  aUser                  User Requesting the info.
     * @param  aPathIncludingFileName path to the file including the file name
     *
     * @return a number starting at 1 for the first revision.
     *
     * @throws RepositoryException  If there is a problem with the repository 
     */
    public int getCurrentRevisionNum(String aUser, String aPathIncludingFileName) throws 
                            RepositoryException;

    /**
     * Give information for top-level revision.
     *
     * @param   aPath path to a file or folder.
     * @return  The information for the top-level revision
     *
     * @throws  RepositoryException If there is a problem with the repository 
     */
    public RepositoryInfo getInformation (String aPath) throws RepositoryException;

    /**
     * Lock the document described by aPath and file name.
     *
     * The lock will fail in case some other user has already locked the file.
     *
     * @param    user                   the User locking the entry
     * @param    aPathIncludingFileName path to the file including file name
     *
     * @return   true if the object was succesfully locked.
     *
     * @throws   RepositoryException   If there is a problem with the repository
     */
    public boolean lock (String user, String aPathIncludingFileName) throws 
                            RepositoryException;

    /**
     * Resets a lock on the given file if the lock was held by the current user.
     *
     * You can only unlock a File in case the same user has called
     * lock() before. In case the Entry was <code>inUpdate</code> the
     * new verison will be released.
     *
     * @param user                   the User locking the entry
     * @param aPathIncludingFileName path to the file including the file name
     *
     * @return true if unlock succeeded, else false
     *
     * @throws RepositoryException If there is a problem with the repository 
     */
    public boolean unlock (String user, String aPathIncludingFileName) throws
                            RepositoryException;

    /**
     * Adds a new document to the repository.
     * The new revision number is returned.
     *
     * The path must not end with an delimiter.
     * In case this is a new entry it will be locked. 
     * In case the entry alreay exists ist must be locked to allow an update.
     * Until the entry is <code>unlock()</code>ed any further create 
     * will only update the existing entry (The entry is <code>inUpdate</code>)
     *
     * @param user                      the User creating the entry
     * @param aPathIncludingFileName    path to the file including the file name
     * @param aFile                     the data to store,
     *                                  will be exhausted and close()d 
     *
     * @return   The revisionnumber of the newly created file.
     *
     * @throws   RepositoryException  If there is a problem with the repository 
     */
    public int create (String user, String aPathIncludingFileName, InputStream aFile)
        throws RepositoryException;

    /**
     * Adds a new document to the repository.
     * The new revision number is returned.
     *
     * The path must not end with an delimiter.
     * In case this is a new entry it will be locked. 
     * In case the entry alreay exists ist must be locked to allow an update.
     * Until the entry is <code>unlock()</code>ed any further create 
     * will only update the existing entry (The entry is <code>inUpdate</code>)
     *
     * @param user         the User creating the entry
     * @param aPath        the path to the file excluding the file name
     * @param aFilename    the name of the file
     * @param aFile        the data to store 
     *
     * @return   The revisionnumber of the newly created file.
     *
     * @throws   RepositoryException  If there is a problem with the repository 
     */
    public int create (String user, String aPath, String aFilename, File aFile)
        throws RepositoryException;

    /**
     * Adds a new document to the repository.
     * The new revision number is returned.
     *
     * In case this is a new entry it will be locked. 
     * In case the entry alreay exists ist must be locked to allow an update.
     * Until the entry is <code>unlock()</code>ed any further create 
     * will only update the existing entry (The entry is <code>inUpdate</code>)
     *
     * @param user          the User creating the entry
     * @param aPath         the path to the file excluding the file name
     * @param aFilename     the name of the file
     * @param aFile         the data to store, will be exhausted and close()d 
     *
     * @return   The revisionnumber of the newly created file.
     *
     * @throws   RepositoryException  If there is a problem with the repository 
     */
    public int create (String user, String aPath, String aFilename,
                       InputStream aFile) throws RepositoryException;

    /**
     * Adds a new document to the repository.
     * 
     * In case this is a new entry it will be locked. 
     * In case the entry alreay exists ist must be locked to allow an update.
     * Until the entry is <code>unlock()</code>ed any further create 
     * will only update the existing entry (The entry is <code>inUpdate</code>)
     *
     * @param user                 the User creating the entry
     * @param aPathWithoutFilename the path to the file excluding the file name
     *                             use null to indicate a top-level file
     * @param aFilename            the name of the file
     *
     * @return   an OputputStream you can use to write to the new revision.
     *           Please close this stream as soon as possible.
     *
     * @throws   RepositoryException  If there is a problem with the repository 
     */
    public OutputStream create (String user, String aPathWithoutFilename,
                                             String aFilename)
                                             throws RepositoryException;
    
    /**
     * Adds a new document to the repository.
     * 
     * In case this is a new entry it will be locked. 
     * In case the entry alreay exists ist must be locked to allow an update.
     * Until the entry is <code>unlock()</code>ed any further create 
     * will only update the existing entry (The entry is <code>inUpdate</code>)
     *
     * @param user      the User creating the entry
     * @param aPath     the path to the file including the file name
     *
     * @return   an OputputStream you can use to write to the new revision.
     *           Please close this stream as soon as possible.
     *
     * @throws   RepositoryException  If there is a problem with the repository 
     */
    public OutputStream create (String user, String aPath)
                                             throws RepositoryException;

    /**
     * Returns a stream with the top-level version of the given
     * repository entry.
     *<p>
     *  Please close() the stream as soon as possible since the underlying
     *  Implementation may not be able to reuse the entry until then.
     *</p>
     *
     * @param aPathIncludingFileName path to the file including the file name
     *
     * @throws RepositoryException If there is a problem with the repository 
     *
     * @return   an InputStream with the actual version of the document 
     *           (please close() after usage).
     */
    public InputStream get (String aUser, String aPathIncludingFileName) throws
                        RepositoryException;                            

    /**
     * Returns a stream with the given version of the given reporitory entry
     *<p>
     *  Please close() the stream as soon as possible since the underlying
     *  Implementation my not be able to reuse the entry until then.
     *</p>
     *
     * @param aPathIncludingFileName path to the file including the file name
     *                               use null to indicate a top-level file
     * @param aVersion               version of the repository entry to retrive
     *
     * @throws NoEntryException   If there is no document in the Repository.
     *
     * @return    an InputStream with the requested version of the document
     *           (please close() it after usage).
     */
    public InputStream get (String aPathIncludingFileName, int aVersion) throws
                            RepositoryException;

    /**
     * Marks a document as deleted in the Repository.
     * 
     * The object must not be locked to allow a delete.
     *
     * @param user                   the User deleting the entry
     * @param aPathIncludingFileName path to the file including the file name.
     *                               use null to indicate a top-level file
     * @param force 
     *        Whether locks are automatically broken independent of the locking user. 
     *        See {@link #unlock(String, String)}.
     * @return the revision that was used for deletion.
     *
     * @throws RepositoryException If there is a problem with the repository 
     */
    public int delete (String user, String aPathIncludingFileName, boolean force) throws
                            RepositoryException;

    /**
     * Creates a directory below the given Container.
     *
     * @param    aParentPath    the pathname to the directory
     *                          use null to indicate a top-level directory
     * @param    aDirectoryName the name of the directory to be created.
     *
     * @return   true when cerating succeeded
     *
     * @throws   RepositoryException If there is a problem with the repository 
     */
    public boolean mkdir (String aParentPath, String aDirectoryName) throws
                            RepositoryException;
    
	/**
	 * Deletes (marks as deleted) the directory if it is empty.
	 *
	 * @param    aPath     The path to the directory to be deleted
	 *
	 * @return   true if the directory was (marked as) deleted, false otherwise
	 *
	 * @throws   RepositoryException  If there is a problem with the repository 
	 */
	public boolean rmdir(String aPath) throws RepositoryException;
                                                        
    /**
     * Checks if the given name exists in the repository
     *
     * @param    aPath    a path of a file or a directory
     *
     * @throws RepositoryException If there is a problem with the repository 
     */
	public boolean exists(String aPath) throws RepositoryException;

	/**
	 * Checks if the given name exists in the repository
	 *
	 * @param aPath
	 *        a path of a file or a directory
	 *
	 * @throws RepositoryException
	 *         If there is a problem with the repository
	 */
	public boolean existsEntry(String aPath) throws RepositoryException;

    /**
     * Returns a Collection of EntryInfos for the given Directory.
     *
     * @param    aPathOfDirectory    the path to the directory
     *
     * @return   a Collection with the VersionInfo of all the top-Level
     *           entries or Directories.
     *
     * @throws   RepositoryException If there is a problem with the repository 
     */
    public Collection<? extends RepositoryInfo> getEntries (String aPathOfDirectory) throws
                            RepositoryException;

	/**
	 * Throws {@link IllegalArgumentException}, if the given file or directory name is invalid for
	 * this {@link Repository}.
	 * 
	 * <p>
	 * A repository implementation must not accept the '/' character as name part, since
	 * {@link RepositoryDataSourceAdaptor} depends upon it for its URI creation.
	 * </p>
	 * 
	 * @param name
	 *        The name to check
	 * @throws IllegalArgumentException
	 *         If the given name is invalid.
	 */
	public void checkName(String name) throws IllegalArgumentException;
}
