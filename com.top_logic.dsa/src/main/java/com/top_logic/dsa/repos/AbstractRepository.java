/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dsa.repos;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.col.HashMapWeak;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.dsa.repos.file.FileRepository;


/**
 * Implementation of the repository based on simple files and directories.
 * <p>
 * This implementation allows all file names allowed by the host-OS minus some length needed for
 * bookkeeping. The original names are encoded using an escape character (We use '_' for all
 * examples but this constant could be changed to <em>any</em> value). Directories / Entries are
 * separated by '/' Repositories can be moved to other file systems. Repositories inherit their
 * behavior with case-sensitive file names from the underlying file system.
 * </p>
 * <p>
 * Names <em>starting</em> with the escape char are encoded by doubling it, e.g.
 * <code>_a_File_</code> -&gt; <code>__a_File_</code> Normal Directories are encoded with a
 * <code>__</code> in case they start with a <code>_</code>. Deleted directories are encoded with
 * <code>_dDirName</code>. Files are stored in directories starting with _fFileName. Inside that
 * directory the Files are encoded as follows: <br/>
 * <code>_version_n_author</code> For <b>n</b>ormal revisions and <br/>
 * <code>_version_d_author</code> For <b>d</b>eleted revisions and <br/>
 * Revision 0 is special and is used to record locking, the top_level revision number and the update
 * state: <br/>
 * <code>_0_revision</code> contains the revision in binary integer form followed by the user name
 * of the current revision followed by a boolean indicating that the current version is deleted
 * followed by a boolean indicating that the current version is locked followed by the name of the
 * current locker followed by a boolean indicating that the entry is currently updated. <br/>
 * An entry is <code>inUpdate</code> when some user locked it AND updated the entry. Updating an
 * entry "inUpdate" will override the previous entry. The inUpdate state is reset when unlocking the
 * entry. Example:
 * 
 * <pre>
 * Contents of _f__Some_File
 *    _0_revision                   # containing 4, "Härbört, true, false 
 *    _1_n_Müller                   # First Version by "Müller"
 *    _2_n_Ângstrøm                 # Second one by "Ângstrøm"
 *    _3_n_Müller                   # Third one by "Müller" again
 *    _4_n_Some User                # Fouth one by "Some User"
 *    _5_d_Härbört                  # Deleted in Revision 5
 * </pre>
 * </p>
 * <p>
 * When an Attic is used the behavior with deleted files and folders changes. In case files are
 * deleted they are moved to a corresponding folder in the attic. In case folders are deleted
 * nothing will happen (assuming that files inside the folder where moved to the attic). Filename
 * Conflicts in the attic are solved by appending numbers to the files.
 * </p>
 * <p>
 * This repository <em>should</em> work correctly when used concurrently on a shared file system.
 * (shared) Attic was not tested, but should work. Work areas (shared or distributed) should not be
 * used in this case: Results are unpredictable.
 * </p>
 * 
 * @author    <a href="mailto:fma@top-logic.com">fma</a>
 */
public abstract class AbstractRepository<C extends ContainerObject, L extends LeafObject> implements Repository {

	/**
	 * Configuration of {@link FileRepository}.
	 * 
	 * @author <a href="mailto:mga@top-logic.com">Michael G�nsler</a>
	 */
	public interface Config<I extends AbstractRepository<?, ?>> extends PolymorphicConfiguration<I> {

		/** Configuration name for {@link #getPath()}. */
		String PATH = "path";

		/** Configuration name for {@link #getWorkarea()}. */
		String WORKAREA = "workarea";

		/** Configuration name for {@link #getAttic()}. */
		String ATTIC = "attic";

		/** Filename of the repository directory. */
		@Mandatory
		@Name(PATH)
		String getPath();

		/**
		 * @see #getPath()
		 */
		void setPath(String value);

		/** Filename of a mirrored top-level view on the repository (aka. work area). */
		@Name(WORKAREA)
		String getWorkarea();

		/**
		 * @see #getWorkarea()
		 */
		void setWorkarea(String value);

		/** Filename of attic. */
		@Name(ATTIC)
		String getAttic();

		/**
		 * @see #getAttic()
		 */
		void setAttic(String value);
	}

	/** Character used to escape the information needed by us. */
    protected static final char ESCAPE              = '_';

	/** Character following a Name denoting a deleted directory */
    protected static final char ESCAPE_DELDIR       = 'd';

	/** Character following a Name (File) denoting a directory containing revisions. */
    protected static final char ESCAPE_FILES        = 'f';

	/** Character used to escape the information as String. */
	public static final String ESCAPESTR = String.valueOf(ESCAPE);

	/** Character in revision file encoding a deleted revisions. */
	public static final char ESCAPE_DELFILEC = 'd';

	/** String in revision file encoding a normal revisions ('_n_') */
    protected static final String ESCAPE_NORMFILE   = ESCAPESTR + 'n' + ESCAPE;

	/** String in revision file encoding a deleted revisions ('_d_') */
    protected static final String ESCAPE_DELFILE    = ESCAPESTR + ESCAPE_DELFILEC + ESCAPE;

	/** String prefixing a deleted directory. */
	public static final String DELDIRPREFIX = ESCAPESTR + ESCAPE_DELDIR;

	/** Length of String prefixing a deleted directory. */
    protected static final int    DELDIRPREFIXLEN   = DELDIRPREFIX.length();

    /** String prefixing a directory with revisions. */
	public static final String FILEPREFIX = ESCAPESTR + ESCAPE_FILES;

    /** Length String prefixing a directory with revisions. */
    protected static final int    FILEPREFIXLEN     = FILEPREFIX.length();

    /** String prefixing the special '0' Revision. */
    protected static final String ZEROPREFIX        = ESCAPESTR + '0' + ESCAPE;

	/** Filename of the file contains the top-level revision. */
	public static final String VERSIONFILE = ZEROPREFIX + "version";

	/** Message written into marker files. */
    protected static final String DEL_MSG           = "Marker for deleted files";

	/** Optional Directory containing a copy of the top-level revisions. */
	private C workArea;

	/** Optional Directory where deleted revisions will be moved to. */
	private C attic;

	/** Directory contains all the revisions. */
	private C repository;

    // a simple, one entry EntryInfo cache ...
    
    /** Cache for the file/folder info last used, 
        indexed by normalized path name with File.separator . */
	protected HashMapWeak<String, RepositoryInfo> cache;

	protected String getName(C aContainer) {
		return aContainer.getName();
	}

	protected String getParent(C aContainer) {
		return aContainer.getPath();
	}

	protected C getParentContainer(C aFile) throws RepositoryException {
		return this.toC(aFile.getParentContainer());
	}

	protected RepositoryInfo createRepositoryInfo(C relPath, C absPath, boolean aDir) throws RepositoryException {
		if (aDir) {
			return this.createContainerInfo(relPath, absPath);
		}
		else {
			return this.createEntryInfo(relPath, absPath);
		}
	}

	protected RepositoryInfo createRepositoryInfo(C relPath, C absPath, String aFile, boolean aDir) throws RepositoryException {
		if (aDir) {
			return this.createContainerInfo(relPath, absPath, aFile);
		} else {
			return this.createEntryInfo(relPath, absPath, aFile);
		}
	}

	protected RepositoryInfo createContainerInfo(C relPath, C absPath, String aFile) throws RepositoryException {
		return new BaseRepositoryInfo(relPath, absPath, aFile);
	}

	protected RepositoryInfo createContainerInfo(C relPath, C absPath) throws RepositoryException {
		return new BaseRepositoryInfo(relPath, absPath);
	}

	protected EntryInfo createEntryInfo(C relPath, C absPath) throws RepositoryException {
		return new BaseEntryInfo(relPath, absPath);
	}

	protected EntryInfo createEntryInfo(C relPath, C absPath, String aFile) throws RepositoryException {
		return new BaseEntryInfo(relPath, absPath, aFile);
	}

	protected C getRevisionContainer(RepositoryInfo anInfo) throws RepositoryException {
		return this.toC(((BaseRepositoryInfo) anInfo).getRevDir());
	}

	protected void copyLeaf(L aSource, L aDest) throws RepositoryException {
		try {
			aSource.copy(aDest);
		} catch (IOException ex) {
			throw new RepositoryException(ex);
		}
	}

	protected List<String> getChildren(C aContainer) throws RepositoryException {
		return CollectionUtil.toList(aContainer.list());
	}

	protected L createLeafReference(C aContainer, String aName) throws RepositoryException {
		return this.toL(aContainer.createLeafObject(aName));
	}

	protected C createTempContainer(C aParent, String aName) throws RepositoryException {
		try {
			return this.toC(aParent.createTempFile(aName, ""));
		} catch (IOException ex) {
			throw new RepositoryException("Unable to move file to attic '" + aName + "' (parent: '" + aParent + "')",
				ex);
		}
	}

	protected boolean deleteLeaf(L aLeaf) {
		return aLeaf.delete();
	}

	protected boolean mkContainer(C aFile) throws RepositoryException {
		return aFile.mkdir();
	}

	protected boolean mkContainers(C aFile) throws RepositoryException {
		return aFile.mkdirs();
	}

	protected boolean deleteContainer(C aContainer) throws RepositoryException {
		return aContainer.delete();
	}

	protected boolean existsContainer(C aFile) throws RepositoryException {
		return aFile.exists();
	}

	protected boolean isDirectory(C aFile) {
		return aFile.isDirectory();
	}

	protected boolean isDirectory(RepositoryInfo anInfo) throws RepositoryException {
		return anInfo.getIsContainer();
//		C theInfo = this.getRevisionContainer(anInfo);
//
//		return !this.isDirectory(theInfo.getName());
	}

	protected boolean isDirectory(String aName) {
		int    thePos  = aName.lastIndexOf('/');
		String theName = (thePos > -1) ? aName.substring(thePos + 1) : aName;

		return !theName.startsWith(AbstractRepository.FILEPREFIX);
	}
	protected void copyLeaf(InputStream aStream, L aLeaf) throws RepositoryException {
		try {
			aLeaf.copy(aStream);
		} catch (IOException ex) {
			throw new RepositoryException(ex);
		}
	}

	protected boolean renameContainer(C anOrig, C aNew) {
		return anOrig.renameTo(aNew);
	}

	protected L getLeaf(VersionInfo anInfo) {
		return this.toL(((BaseVersionInfo) anInfo).getFile());
	}

	protected InputStream get(C aContainer, int aRev, String aUser) throws RepositoryException {
		LeafObject target = aContainer.createLeafObject(ESCAPESTR + aRev + ESCAPE_NORMFILE + aUser);

		try {
			return target.getInputStream();
		} catch (FileNotFoundException fnf) {
			throw new NoEntryException(aContainer.getAbsolutePath(), fnf);
		}
	}

	protected OutputStream create(C aContainer, int aRev, String aUser) throws RepositoryException {
		LeafObject target = aContainer.createLeafObject(ESCAPESTR + aRev + ESCAPE_NORMFILE + aUser);

		try {
			return target.getOutputStream();
		} catch (FileNotFoundException fnf) {
			throw new NoEntryException(aContainer.getAbsolutePath(), fnf);
		}
	}

	protected String getSystemPath(RepositoryInfo anInfo) {
		return ((BaseRepositoryInfo) anInfo).getSystemPath();
	}

	protected int prepareCreate(String aUser, RepositoryInfo anInfo) throws RepositoryException {
		boolean inUpdate;
		C theRevDir = this.getRevisionContainer(anInfo);
		EntryInfo theFileInfo = (EntryInfo) anInfo;

		if (!theRevDir.exists()) { // new Entry
			theRevDir.mkdir();
			inUpdate = false;
		} else { // Existing File
			theFileInfo.readInfo(); // changed on distributed FS ?

			if (!theFileInfo.isLocked() || !aUser.equals(theFileInfo.getLocker())) {
				throw new RepositoryException("cannot create('" + anInfo.getPath() + "'), entry is not locked");
			}

			inUpdate = theFileInfo.isInUpdate();
		}

		return inUpdate ? theFileInfo.getVersions() : theFileInfo.newRevision(aUser);
	}

	protected void createDeletedLeaf(L aLeaf) throws RepositoryException {
		Writer theWriter = null;

		try {
			theWriter = aLeaf.getWriter();

			theWriter.write(DEL_MSG);
		} catch (IOException ex) {
			throw new RepositoryException(ex);
		} finally {
			FileUtilities.close(theWriter);
		}
	}

	protected void updateEntryInfo4Delete(String user, EntryInfo anInfo, int revision) {
		BaseEntryInfo finfo = (BaseEntryInfo) anInfo;

		finfo.numVersions = revision; // set new Version number
		finfo.author = user; // and new author
		finfo.versions = null; // invalidate old Versions Info
		finfo.deleted = true; // Mark current Version as deleted
	}

	/**
	 * Convert the given container object to the requested type.
	 * 
	 * @param aContainer
	 *        Container to be converted.
	 * @return The given container in the matching type.
	 */
	@SuppressWarnings("unchecked")
	protected C toC(ContainerObject aContainer) {
		return (C) aContainer;
	}

	/**
	 * Convert the given leaf object to the requested type.
	 * 
	 * @param aLeaf
	 *        Leaf to be converted.
	 * @return The given leaf in the matching type.
	 */
	@SuppressWarnings("unchecked")
	protected L toL(LeafObject aLeaf) {
		return (L) aLeaf;
	}

	protected abstract RepositoryInfo createRepositoryInfo(String aPath, C aContainer) throws RepositoryException;

	protected abstract C createContainerReference(C aParent, String aName) throws RepositoryException;

	protected abstract C createContainerReference(String aName) throws RepositoryException;

	@Override
	public int getCurrentRevisionNum(String aUser, String aPathIncludingFileName) throws RepositoryException {
		EntryInfo theInfo   = this.getEntryInfo(aPathIncludingFileName);
		int       theResult = theInfo.getVersions();

		if (theInfo.isInUpdate() && !aUser.equals(theInfo.getLocker())) {
			theResult--; // Other users than the updating users wont see that.
		}

		return theResult;
	}

	@Override
	public InputStream get(String aUser, String aPathIncludingFileName) throws RepositoryException {
		EntryInfo theInfo = this.getEntryInfo(aPathIncludingFileName);
	
		if (theInfo.isDeleted()) {
			throw new NoEntryException("'" + aPathIncludingFileName + " is deleted");
		}
		else {
			int revision = theInfo.getVersions();
			String lastUser = theInfo.getAuthor();
	
			if (theInfo.isInUpdate() && !aUser.equals(lastUser)) {
				revision--;
				if (revision <= 0)
					throw new NoEntryException("'" + aPathIncludingFileName + " initial Revsion not yet released");
			}
	
			return this.get(this.getRevisionContainer(theInfo), revision, lastUser);
		}
	}

	@Override
	public InputStream get(String aPathIncludingFileName, int aVersion) throws RepositoryException {
		EntryInfo theInfo = this.getEntryInfo(aPathIncludingFileName);
	
		if (aVersion <= 0 || aVersion > theInfo.getVersions()) {
			throw new NoEntryException("'" + aPathIncludingFileName + " Revsion " + aVersion + " is out of Range (1," + theInfo.getVersions() + ")");
		}
		else {
			VersionInfo vinfo = theInfo.getVersionInfo(aVersion);
	
			if (vinfo.isDeleted()) {
				throw new NoEntryException("'" + aPathIncludingFileName + " Revsion " + aVersion + " is deleted");
			}
	
			return this.get(this.getRevisionContainer(theInfo), vinfo.getVersion(), vinfo.getAuthor());
		}
	}

	@Override
	public RepositoryInfo getInformation(String aPathIncludingFileName) throws RepositoryException {
        try {
			return this.getRepositoryInfo(aPathIncludingFileName);
        }
        catch (NoEntryException nex) {
            return null;   
        }
    }

    @Override
	public boolean lock(String user, String aPathIncludingFileName) throws RepositoryException {
		EntryInfo fInfo = getEntryInfo(aPathIncludingFileName, true);

		if (fInfo.isLocked()) {
            return user.equals(fInfo.getLocker());
            // is current user locked, fine
        }

        fInfo.lock(user);
        fInfo.updateModified();
        return true;
    }

    @Override
	public boolean unlock(String user, String aPathIncludingFileName) throws RepositoryException {
		EntryInfo fInfo = getEntryInfo(aPathIncludingFileName, true);

        if (!fInfo.isLocked()) {
            return true;    // nothing to unlock, thats ok.
        }

        String theLocker = fInfo.getLocker();

		if (!user.equals(theLocker) && !ThreadContext.isSuperUser()) { // superuser may unlock any file
            return false; 
        }

        if (workArea != null      // must care aboutWorkarea now 
         && fInfo.isInUpdate()) { // actually wasInUpdate()
			String revName = ESCAPESTR + fInfo.getVersions() + ESCAPE_NORMFILE + theLocker;

			L workFile = this.createLeafReference(workArea, this.getSystemPath(fInfo));
			L revFile  = this.createLeafReference(this.getRevisionContainer(fInfo), revName);

			this.copyLeaf(revFile, workFile);
		}

        fInfo.unlock();

        return true;
    }

    @Override
	public int create(String user, String aPath, String aFilename, File aFile) throws RepositoryException {
		try {
			final FileInputStream theStream = new FileInputStream(aFile);

			try {
				return this.create(user, aPath, aFilename, theStream);
			}
			finally {
				theStream.close();
			}
		} 
		catch (IOException ex) {
			throw new NoEntryException(aFile.getPath(), ex);
		}
	}

	@Override
	public int create(String user, String aPathIncludingFileName, InputStream aFile) throws RepositoryException {
		return this.create(user, this.getEntryInfo(aPathIncludingFileName), aFile);
    }

	@Override
	public int create(String user, String aPath, String aFilename, InputStream aFile) throws RepositoryException {
		return this.create(user, this.getEntryInfo(aPath, aFilename), aFile);
    }

    @Override
	public OutputStream create(String user, String aPath) throws RepositoryException {
		return this.create(user, this.getEntryInfo(aPath));
    }

    @Override
	public OutputStream create(String user, String aPath, String aFilename) throws RepositoryException {
		return this.create(user, this.getEntryInfo(aPath, aFilename));
    }

    @Override
	public int delete(String user, String aPathIncludingFileName, boolean force) throws RepositoryException {
		int       theRevision = 0;
		EntryInfo theInfo     = this.getEntryInfo(aPathIncludingFileName);

		// Care for remote File System changes
		theInfo.readInfo();

		if (!theInfo.getIsEntry()) {
			throw new RepositoryException("cannot delete(" + aPathIncludingFileName + ") is not an entry");
		}

		if (force) {
			theInfo.locallyUnlock();
		} else {
			if (theInfo.isLocked()) {
				throw new RepositoryException("cannot delete(" + aPathIncludingFileName + ") is locked");
			}
		}

		theRevision = (attic == null) ? deleteInPlace(user, theInfo) : deleteToAttic(theInfo);

		invalidateRepositoryInfo(theInfo);

		if (workArea != null) { // Delete in work area, too
			L theLeaf = this.createLeafReference(workArea, this.getSystemPath(theInfo));

			if (!this.deleteLeaf(theLeaf)) {
				Logger.error("Failed to delete workarea file '" + theLeaf + "'", AbstractRepository.class);
			}
		}

		return theRevision;
	}

	/**
	 * Creates a directory under the path.
	 *
	 * @param aParentPath
	 *        the pathname to the directory
	 * @param aDirectoryName
	 *        the name of the directory to be created.
	 *
	 * @return true if created/resurrected, else false
	 *
	 * @throws RepositoryException
	 *         If there is a problem with the repository
	 */
    @Override
	public boolean mkdir(String aParentPath, String aDirectoryName) throws RepositoryException {
        C    parent  = createParentDir(aParentPath);

		// Do not allow creating a directory for a file with the same name...
        String  fileName= FILEPREFIX + aDirectoryName;
        C       theFile = this.createContainerReference(parent, fileName);

		if (this.existsContainer(theFile)) {
            throw new RepositoryException("Cannot mkdir('" + aParentPath + "','" + aDirectoryName + "'), file already exists");
        }

        String  escapeD = escapeName(aDirectoryName);
        C       newDir  = this.createContainerReference(parent, escapeD);

        String  delName = DELDIRPREFIX + escapeD;
        C       delDir  = this.createContainerReference(parent, delName);

        boolean ressurect = this.existsContainer(delDir);
        boolean result;
		if (ressurect) { // Resurrect old directory
            result = this.renameContainer(delDir, newDir);
        }
        else {
            result = this.mkContainer(newDir);
        }

        if (result && workArea != null) {
            String workName = aParentPath + '/' + aDirectoryName;
            C      workDir  = this.createContainerReference(workArea, workName);

            if (!this.mkContainer(workDir)) {
                Logger.error("Failed to create workarea dir '" + workDir + "'", AbstractRepository.class);
            }
            // Since it is not allowed to delete folder containing anything
            // but deleted files and subfolders the following is not needed
            // (for now ;-)
            // if (ressurect)  {
            //    export(workName, workDir); // Recreate deleted directory
            // }
        }
        return result;
    }

	/**
	 * Deletes (marks as deleted) the directory if it is empty.
	 *
	 * @param aPath
	 *        The path to the directory to be deleted.
	 * @return <code>true</code> if the directory was (marked as) deleted, false otherwise.
	 * @throws RepositoryException
	 *         If there is a problem with the repository.
	 */
	@Override
	public boolean rmdir(String aPath) throws RepositoryException {
		RepositoryInfo finfo = this.getRepositoryInfo(aPath);

		if (finfo.getIsEntry()) {
			throw new RepositoryException("cannot rmdir('" + aPath + "') is a file");
        }
        C       theDir = this.getRevisionContainer(finfo);    
        boolean result = this.deleteContainer(theDir);   // try to delete the (empty) directory

        if (attic != null) {
            if (result) {
                invalidateRepositoryInfo(finfo);
            }
        }
        // Check if all Files _inside_ the folder are marked as deleted.
		else if (!result && allFilesDeleted(finfo, theDir)) {
        
            // "delete" dir by renaming it ...
            String delName = DELDIRPREFIX + this.getName(theDir);
			C      delDir  = this.createContainerReference(this.getParentContainer(theDir), delName);

			result = renameContainer(theDir, delDir);
        
            if (!result) {
				Logger.warn("rmdir('" + aPath + "') renaming to '" + delName + "' failed", AbstractRepository.class);
            }
            else {
                invalidateRepositoryInfo(finfo);
            }
        }
        if (result && workArea != null) {
			C workDir = this.createContainerReference(workArea, aPath);

			if (!this.deleteContainer(workDir)) {
				Logger.warn("Failed to remove directory '" + workDir + "' in Workarea", AbstractRepository.class);
            }
        }
        return  result;
    }
														
	@Override
	public boolean exists(String aPath) throws RepositoryException {
        try  {
			this.getRepositoryInfo(aPath);

            return true;
        } 
        catch (NoEntryException nex) {
            return false;
        }
    }																						

	@Override
	public boolean existsEntry(String aPath) throws RepositoryException {
        try  {
			this.getRepositoryInfo(aPath);

            return true;
        } 
        catch (NoEntryException nex) {
            return false;
        }
	}

	/**
	 * Returns a Collection of EntryInfos for the given Directory.
	 *
	 * @param aPathOfDirectory
	 *        the path to the directory
	 *
	 * @return a Collection with the FileRepositoryInfos of all the top-Level entries or
	 *         Directories.
	 *
	 * @throws RepositoryException
	 *         If there is a problem with the repository
	 */
    @Override
	public final Collection<? extends RepositoryInfo> getEntries(String aPathOfDirectory) throws RepositoryException {
        return internalGetEntries(aPathOfDirectory);
    }
    
    /**
	 * Checks that the given name does not contain '/', '\', or ':' characters, and is 
	 * neither be ".", nor "..".
	 * 
     * @see com.top_logic.dsa.repos.Repository#checkName(java.lang.String)
     */
	@Override
	public void checkName(String name) {
		int length = name.length();
		if (length == 0) {
			throw new IllegalArgumentException("Name must not be empty.");
		}
	
		for (int n = 0; n < length; n++) {
			char ch = name.charAt(n);
			switch (ch) {
				case '\\':
				case '/':
				case ':':
				case '*':
				case '?':
				case '"':
				case '<':
				case '>':
				case '|':
					throw new IllegalArgumentException(
						"Name contains invalid character '" + ch + "'");
			}
		}
	
		if (name.charAt(0) == '.') {
			if (length == 1 || (length == 2 && name.charAt(1) == '.')) {
				throw new IllegalArgumentException(
						"Name must not be '.', or '..'");
			}
		}
	}

	/**
	 * Helper function to move a Document to the Attic on deletion
	 *
	 * @param anInfo
	 *        information about the file.
	 * @return always 0 to indicate that there are no more revisions their.
	 * @throws RepositoryException
	 *         If there is a problem with the repository
	 */
	public int deleteToAttic(EntryInfo anInfo) throws RepositoryException {
		C inAttic = this.createContainerReference(this.attic, anInfo.getPath());
		C parent = this.getParentContainer(inAttic);

		if (!this.existsContainer(parent) && !this.mkContainers(parent)) { // ensure path is there
			throw new RepositoryException("Unable to mkdirs '" + parent + "'");
		}

		C theRevDir = this.getRevisionContainer(anInfo);

		if (!this.renameContainer(theRevDir, inAttic)) { // try to rename once
			inAttic = this.createTempContainer(parent, anInfo.getName());

			// Since this file exists now we must remove it before we can copy the folder
			this.deleteContainer(inAttic);

			if (!this.renameContainer(theRevDir, inAttic)) { // try to rename to tmp-file ...
				throw new RepositoryException("Unable to move file to attic '" + inAttic + "'");
			}
		}

		invalidateRepositoryInfo(anInfo); // Forget about this file ...

		return 0; // no more revisions here
	}

	/**
	 * Helper function to mark a document as deleted in the Repository.
	 *
	 * @param user
	 *        user requesting the deletion
	 * @param anInfo
	 *        information about the file.
	 *
	 * @return the revision number used for the deleted file.
	 *
	 * @throws RepositoryException
	 *         If there is a problem with the repository
	 */
	public int deleteInPlace(String user, EntryInfo anInfo) throws RepositoryException {
		int revision = 1 + anInfo.getVersions(); // create new, deleted Version

	    // Update fInfo with new Values.
		this.updateEntryInfo4Delete(user, anInfo, revision);
		anInfo.writeInfo(); // Write back the whole thing

	    // Write placeholder for deleted Revision e.g. _99_d_<User>                    
	    String  delName  = ESCAPESTR + revision + ESCAPE_DELFILE + user; // _x_d_<User>

		this.createDeletedLeaf(this.createLeafReference(this.getRevisionContainer(anInfo), delName));

	    return revision;
	}

	// Additional public functions for the repository

	/**
	 * Export (copy) a part of the repository to the given target directory.
	 * <p>
	 * The complete contents will be copied recursively.
	 * </p>
	 * 
	 * @param aPathOfDirectory
	 *        Directory in repository where to start, null or "" for the complete repository.
	 * @param aTarget
	 *        Target Directory where copy will start.
	 *
	 * @throws RepositoryException
	 *         when aPathOfDirectory or target are not directories or if there is a problem with the
	 *         repository
	 */
	public void export(String aPathOfDirectory, C aTarget) throws RepositoryException {
		RepositoryInfo parentInfo = this.getRepositoryInfo(aPathOfDirectory);

		if (!this.isDirectory(parentInfo)) {
			throw new RepositoryException("'" + aPathOfDirectory + "' is not a directory");
	    }
		else if (parentInfo.isDeleted()) {
			throw new RepositoryException("'" + aPathOfDirectory + "' already deleted" + " (use mkdir to recreate)");
	    }
		else if (!this.isDirectory(aTarget)) {
			throw new RepositoryException("Target for export ('" + aTarget + "') is not a directory");
	    }

		for (String theName : this.getChildren(this.getRevisionContainer(parentInfo))) {
			int nlen = theName.length();

			if (nlen > 2 && theName.charAt(0) == ESCAPE) { // "_f" , "_d"
				if (theName.charAt(1) == ESCAPE_FILES) {
					String theFullName = aPathOfDirectory + '/' + theName.substring(2, nlen);

					if (this.getRepositoryInfo(theFullName).isDeleted()) {
	                    continue;
	                }
//	                int     revision = finfo.getNumVersions();
//	                String  lastUser = finfo.getAuthor();
//	                File    fromFile = new File(finfo.revDir,ESCAPESTR + revision + ESCAPE_NORMFILE + lastUser);
//	                File    toFile   = new File(target, name);
	            }
				else {
	                continue;//Do not export deleted Directories or unknown files.
				}
	        }
			else { // create and descend into sub directory
				C subdir = this.createContainerReference(aTarget, theName);
				this.mkContainer(subdir);
				export(aPathOfDirectory + '/' + theName, subdir);
	        }
	    }
	}

	/**
	 * Recursively (Re)create the work area for a given path.
	 * 
	 * The work area will not be cleared, no checks will be done if the work area is in any good
	 * shape. It will not work when the Repository was created without a work area.
	 */
	public void createWorkarea(String path) throws RepositoryException, IOException {
		for (RepositoryInfo theInfo : internalGetEntries(path)) {
			if (theInfo.isDeleted()) {
				continue;
			}
			else if (theInfo.getIsEntry()) {
				this.copyLeaf(theInfo, this.createLeafReference(workArea, theInfo.getPath()));
			} 
			else if (theInfo.getIsContainer()) {
				this.mkContainer(this.createContainerReference(workArea, theInfo.getPath()));

				// Recursive call to copy sub structure
				this.createWorkarea(theInfo.getPath());
			}
		}
	}

	private void copyLeaf(RepositoryInfo aSource, L aDest) throws RepositoryException {
		EntryInfo theEntryInfo = (EntryInfo) aSource;

		this.copyLeaf(this.getLeaf(theEntryInfo.getLastVersionInfo()), aDest);
	}

	// internal, protected methods used for implementation of Repository

	/**
	 * Initialize the base attributes of the repository.
	 *
	 * @param aRepository
	 *        Root directory for the repository to use.
	 * @param aWorkArea
	 *        This directory will mirror all changes at the top-level of the repository, may be
	 *        <code>null</code> indicating no work area.
	 * @param anAttic
	 *        Deleted object will be moved here (when given).
	 * @throws RepositoryException
	 *         When initializing the repository fails for a reason.
	 */
	protected void init(C aRepository, C aWorkArea, C anAttic) throws RepositoryException {
		this.repository = aRepository;
		this.workArea   = aWorkArea;
		this.attic      = anAttic;
		this.cache      = new HashMapWeak<>();
	}

	/**
	 * The repository root container.
	 */
	public C getRoot() {
		return repository;
	}

    /**
	 * Helper function for the two variants of create. An OutputStream to write to is returned.
	 * 
	 * @param aUser
	 *        the User creating the entry
	 * @param anInfo
	 *        Describes the File to create.
	 * 
	 * @return an {@link OutputStream} to write to the new revision. Must be closed as soon as
	 *         possible.
	 * 
	 * @throws RepositoryException
	 *         If there is a problem with the repository
	 */
	protected OutputStream create(String aUser, RepositoryInfo anInfo) throws RepositoryException {
		return this.create(this.getRevisionContainer(anInfo), this.prepareCreate(aUser, anInfo), aUser);
	}

	/**
	 * Get cached info for a given container or leaf.
	 *
	 * @param aPath
	 *        may be a container or leaf.
	 *
	 * @return the info
	 */
	protected RepositoryInfo getRepositoryInfo(String aPath) throws RepositoryException {
		C relPath;

		if (aPath != null) {
			relPath = this.createContainerReference(aPath);
			aPath   = this.getParent(relPath); // normalize the Path
		} 
		else {
			relPath = this.createContainerReference("");
			aPath   = "";
		}
	
		synchronized (this.cache) {
			RepositoryInfo theInfo = this.cache.get(aPath);
	
			if (theInfo == null || theInfo.checkModified()) {
				C absPath; // must create absolute (escaped) path here
	
				if (aPath != null) {
					C escapedPath = this.escapeDirNames(aPath, relPath);
					absPath = this.createContainerReference(this.repository, escapedPath.getPath());
				} 
				else {
					absPath = this.repository;
				}

				theInfo = this.createContainerInfo(relPath, absPath);

				if (theInfo.getIsEntry()) {
					theInfo = this.createEntryInfo(relPath, absPath);
				}

				this.cache.put(aPath, theInfo);
			}
	
			return theInfo;
		}
	}

	/**
	 * Get cached info for a File which may be new.
	 *
	 * Will normalize the path using File and lookup the name in the cache. Will create the info
	 * when needed.
	 *
	 * @param aPath
	 *        must be a Folder.
	 * @param aFileName
	 *        name of the file (may not yet exist).
	 *
	 * @return the info
	 */
	protected RepositoryInfo getRepositoryInfoFromPath(String aPath, String aFileName, boolean aDir) throws RepositoryException {
		String thePath;
		C      theParent = null;
		C      absParent = this.repository;
	
		if (aPath != null && aPath.length() > 0) {
			theParent = this.createContainerReference(aPath);
			aPath     = this.getParent(escapeDirNames(aPath, theParent));
			thePath   = normalizePath(aPath, aFileName); // normalized Path
			absParent = this.createContainerReference(this.repository, aPath);
		} 
		else {
			thePath = aFileName; // path is just the filename
		}
	
		synchronized (this.cache) {
			RepositoryInfo theInfo = this.cache.get(thePath);
	
			if (theInfo == null || theInfo.checkModified()) {
				theInfo = this.createRepositoryInfo(theParent, absParent, aFileName, aDir);
	
				this.cache.put(thePath, theInfo);
			}
	
			return theInfo;
		}
	}

	/**
	 * Create a normalized file name for the repository with given path.
	 * 
	 * @param aPath
	 *        Path the file should live in.
	 * @param aFileName
	 *        Name of the file.
	 * @return Requested name.
	 */
	protected abstract String normalizePath(String aPath, String aFileName);

	/**
	 * Invalidate cached info for a File or Folder.
	 *
	 * Will <em>not</em> normalize the path since it is used internally only.
	 *
	 * @param info
	 *        may be a File <em>or</em> Folder.
	 */
	protected void invalidateRepositoryInfo(RepositoryInfo info) {
		cache.remove(this.getSystemPath(info));
	}
	
	/**
	 * Get cached info for a container of leaf.
	 *
	 * Will <em>not</em> normalize the path since it is used internally only.
	 *
	 * @param aPath
	 *        may be a File <em>or</em> Folder, must contain '\' not '/'
	 *
	 * @return the info
	 */
	protected RepositoryInfo getNormalizedRepositoryInfo(String aPath) throws RepositoryException {
		synchronized (this.cache) {
			RepositoryInfo cachedInfo = cache.get(aPath);
	
			if (cachedInfo == null) {
				cachedInfo = this.createRepositoryInfo(aPath);
				cache.put(aPath, cachedInfo);
			}
			return cachedInfo;
		}
	}

	/**
	 * @param user
	 *        the User creating the entry
	 * @param finfo
	 *        identifies the file.
	 * @param content
	 *        the data to store, will be exhausted. must not be
	 *        <code>null</code>
	 * 
	 * @return The revision number of the newly created file.
	 * 
	 * @throws RepositoryException
	 *         If there is a problem with the repository
	 */
	protected int create(String user, RepositoryInfo finfo, InputStream content) throws RepositoryException {
		int    result  = prepareCreate(user, finfo);
		String newName = ESCAPESTR + result + ESCAPE_NORMFILE + user;
		L      newFile = this.createLeafReference(this.getRevisionContainer(finfo), newName);

		this.copyLeaf(content, newFile);

		finfo.updateModified(); // Update after close()
		return result;
	}

	/**
	 * Recursively escape all directory names in the given path.
	 *
	 * Creation of new Objects is avoided if possible.
	 *
	 * @param anObject
	 *        the file whose name is to be escaped
	 *
	 * @return a File with an escaped version of the name
	 * @throws RepositoryException
	 *         When accessing the container fails.
	 */
	protected C escapeDirNamesRecursivly(C anObject) throws RepositoryException {
        if (anObject != null && !anObject.equals(this.repository)) {
			C      theParent      = this.getParentContainer(anObject);
            C      theGrandParent = this.escapeDirNamesRecursivly(theParent);
			String theName        = this.getName(anObject);

            if (theName.startsWith(ESCAPESTR)) {
                return this.createContainerReference(theGrandParent, ESCAPESTR + theName);
            }
			else if (theParent != null && !theParent.equals(theGrandParent)) {
                return this.createContainerReference(theGrandParent, theName);
            }
        }

        return anObject;
    }

	/**
	 * Recursively escape all directory names in the given path.
	 *
	 * Creation of new Objects is avoided if possible. <br />
	 * The data redundancy caused by path is due to speed optimization.
	 *
	 * @param aPath
	 *        must be the same as aFile.getPath().
	 * @param aFile
	 *        the file whose path is to be escaped
	 *
	 * @return a File with an escaped version of the name
	 * @throws RepositoryException
	 *         When accessing the container fails.
	 */
	protected final C escapeDirNames(String aPath, C aFile) throws RepositoryException {
		if (aPath.indexOf(ESCAPE) >= 0) {
			// Avoid recursion in case no ESCAPE char is around at all
            return this.escapeDirNamesRecursivly(aFile);
        }
        else {
			return aFile;
        }
    }    

    /**
	 * Create the parent directory even for null names, and escape it.
	 * 
	 * @param aParentPath
	 *        the path of the parent
	 * @return the File corresponding to the escaped version of the path
	 * @throws RepositoryException
	 *         When accessing the container fails.
	 * 
	 */
	protected C createParentDir(String aParentPath) throws RepositoryException {
		if (StringServices.isEmpty(aParentPath)) {
			return this.repository; // no need to escape in this case
        }
        else {
			return this.escapeDirNames(aParentPath, this.createContainerReference(this.repository, aParentPath));
        }
    }

	/** 
	 * Check if all Files/Folders in a Folder are "deleted".
	 *
	 * @param aContainer must already be escaped correctly.
	 *
	 * @return  true if all Files/Folders have been deleted, else false
	 */
	protected boolean allFilesDeleted(RepositoryInfo anInfo, C aContainer) throws RepositoryException {
		for (String theName : this.getChildren(aContainer)) {
	        int nlen = theName.length();     
	
	        if (theName.startsWith(ESCAPESTR) && nlen > 2) {
	            char c = theName.charAt(1); // character after '_'
	
	            if (c == ESCAPE_FILES)  {
	                theName = theName.substring(FILEPREFIXLEN);
	
					RepositoryInfo theInfo = this.getRepositoryInfo(anInfo, theName);

					((EntryInfo) theInfo).readInfo();

					if (!theInfo.isDeleted()) {
	                    return false;    
	                }
	            } 
	            else if (c == ESCAPE_DELDIR) {
	                continue;   // This _is_ deleted no need for further lookup.
	            }
	            else if (c == ESCAPE) {
	                return false;   // This escaped dir-name    -> not deleted
	            }
				else {
					// Hmmm something starting with '_' but neither f nor d
					Logger.error("Unexpected entry in '" + anInfo + "': '" + theName + "'", AbstractRepository.class);
	                return false;
	            }
	        }
			else {
				// a normal directory, is not deleted, well
				return false;
			}
	    }
	    return true; // all entries ok.
	}

	/**
	 * Return the information about the requested entry.
	 * 
	 * @param aPath
	 *        Full path of the requested entry.
	 * @param withRead
	 *        <code>true</code> for {@link EntryInfo#readInfo()}.
	 * @return the requested information.
	 * @throws RepositoryException
	 *         When reading the information fails.
	 */
	protected EntryInfo getEntryInfo(String aPath, boolean withRead) throws RepositoryException {
		EntryInfo theInfo = this.getEntryInfo(aPath);

		if (withRead) {
			theInfo.readInfo();
		}

		return theInfo;
	}

	private RepositoryInfo getRepositoryInfo(RepositoryInfo anInfo, String aName) throws RepositoryException {
		return this.getNormalizedRepositoryInfo(this.getSystemPath(anInfo) + File.separatorChar + aName);
	}

	private RepositoryInfo createRepositoryInfo(String aPath) throws RepositoryException {
		return this.createRepositoryInfo(aPath, this.createContainerReference(repository, aPath));
	}

	private Collection<RepositoryInfo> internalGetEntries(String pathOfDirectory) throws RepositoryException {
		RepositoryInfo parentInfo = this.getRepositoryInfo(pathOfDirectory);
	
		if (!this.isDirectory(parentInfo)) {
			throw new RepositoryException("'" + pathOfDirectory + "' is not a directory (info is: '" + parentInfo + "')");
		}
		else if (parentInfo.isDeleted()) {
			throw new RepositoryException("'" + pathOfDirectory + "' already deleted (use mkdir to recreate)");
		}
	
		ArrayList<RepositoryInfo> theResult = new ArrayList<>();
	
		for (String theName : this.getChildren(this.getRevisionContainer(parentInfo))) {
			int nlen = theName.length();
			boolean isDir = true;
	
			if (nlen > 2 && theName.charAt(0) == ESCAPE) { // "_f" , "_d"
				char c = theName.charAt(1);

				isDir = !(c == ESCAPE_FILES);

				if (c == ESCAPE_FILES || c == ESCAPE_DELDIR) {
					theName = theName.substring(2, nlen);
				} 
				else if (c == ESCAPE) {
					// something like "__file" designating an escaped "_file"
					theName = theName.substring(1, nlen); // discard initial ESCAPE ('_')
				}
				else {
					Logger.error("Unexpected filename in '" + pathOfDirectory + "' : '" + theName + "'", AbstractRepository.class);
				}
			}
	
			theResult.add(this.getRepositoryInfoFromPath(pathOfDirectory, theName, isDir));
		}
	
		return theResult;
	}

	private EntryInfo getEntryInfo(String aPath) throws RepositoryException {
		RepositoryInfo theResult = this.getRepositoryInfo(aPath);

		if (theResult instanceof EntryInfo) {
			return (EntryInfo) theResult;
        }
        else {
			throw new RepositoryException("Path '" + aPath + "' does not lead to an Entry.");
        }
    }

	/**
	 * Return the entry information for the given parameters.
	 * 
	 * @param aPath
	 *        Path of the file.
	 * @param aFileName
	 *        Name of the requested file.
	 * @return The requested entry information.
	 * @throws RepositoryException
	 *         When given parameters didn't point to an entry.
	 */
	protected synchronized EntryInfo getEntryInfo(String aPath, String aFileName) throws RepositoryException {
		RepositoryInfo theResult = this.getRepositoryInfoFromPath(aPath, aFileName, false);

		if (theResult instanceof EntryInfo) {
			return (EntryInfo) theResult;
        }
        else {
            throw new RepositoryException("Path does not lead to an Entry.");
        }
    }

	/**
	 * Escape a name starting with ESCAPESTR.
	 *
	 * @param aName
	 *        the name to be escaped.
	 * @return the escaped name.
	 */
	public static final String escapeName(String aName) {
	    if (aName.startsWith(ESCAPESTR)) {
	        aName = ESCAPESTR + aName;
	    }
	    return aName;
	}

}
