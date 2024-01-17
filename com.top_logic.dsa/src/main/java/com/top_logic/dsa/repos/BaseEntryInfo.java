/*
 * SPDX-FileCopyrightText: 2000 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dsa.repos;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.thread.ThreadContext;

/** This class wrappes the entries used by the FileRepository.
 *
 * It is used internally by the 
 * {@link AbstractRepository} to cache
 * information needed for its operation. Most accessors work on
 * demand to waste as little time as possible.
 *
 * @author    <a href="mailto:kha@top-logic.com">Klaus Halfmann</a>
 */
public class BaseEntryInfo extends BaseRepositoryInfo implements EntryInfo {
    
    /** use to set the inUpdateFlag */
    public static final boolean IN_UPDATE = true;
    
    /** Number of Versions found in the Directory.
     *
     *  0 indicated that this information was not yet retrieved.
     */
    protected int             numVersions;
    
    /** Author extracted from _0_version file on demand */
    protected String          author;
    
    /** Indicates that the entry is locked */
    protected boolean         locked;

    /** Name of user who locked the repository */
    protected String          locker;

    /** Indicates that the entry was updated by a locking user. */
    protected boolean         inUpdate;

    /** Special file containing info about Locks etc. */
	protected LeafObject verFile;
    
    
    /** Array of VersionInfos created on Demand with numVersions size */
    protected BaseVersionInfo versions[];
    
    /** Construct a FileEntryInfo from a Directory.
     *
     * Escape processing of directorynames must be done by caller
     * (the file repository).
     *
     * @param relDir   Directory with relative path (not escaped)
     * @param absDir   Directory with aboslute path actually used by repository (escaped)
     *
     * @throws RepositoryException in case aRevDir is not a directory
     *                             or does not exist.
     */
	public BaseEntryInfo(ContainerObject relDir, ContainerObject absDir) throws RepositoryException {
        super(relDir, absDir);

		verFile = revDir.createLeafObject(AbstractRepository.VERSIONFILE);
		this.lastModified = this.getModifiedFromFile();
    }

    /** Construct a FileEntryInfo for a new File, that may not yet exist.
     *
     * Escape processing of directory names must be done by caller
     * (the file repository).
     *
     * @param relParent relative pathname to parent null indicates top-level.
     * @param absParent absolute pathname to parent
     * @param fName     name of File (may not yet exist)
     *
     * @throws RepositoryException in case of unexpected Errors
     */
	public BaseEntryInfo(ContainerObject relParent, ContainerObject absParent, String fName) throws RepositoryException {
        super(relParent, absParent, fName);

		verFile = revDir.createLeafObject(AbstractRepository.VERSIONFILE);
		this.lastModified = this.getModifiedFromFile();
    }

    /**
     * Equals is based on the path.
     * It works on other FileEntryInfos and Strings.
     * <br/>
     *  This could be used the caching mechanisms used by the FileRepository.
     * <br/>
     * Note: This method is <em>not</em> <i>symmetric</i> as specified
     * in {@link java.lang.Object#equals(java.lang.Object)} when used on
     * a String. (Design decision by KHA.)
     *
     * @param other the object to compare to
     *
     * @return true if equals else false
     */
    @Override
	public boolean equals(Object other) {
		// Ignoring SpotBugs(EQ_OVERRIDING_EQUALS_NOT_SYMMETRIC)
    	if (other == this) {
    		return true;
    	}
        if (other instanceof BaseEntryInfo) 
            return path.equals (((BaseEntryInfo) other).path);
		// TODO #19482: FindBugs(EQ_CHECK_FOR_OPERAND_NOT_COMPATIBLE_WITH_THIS)
        if (other instanceof String)     
            return path.equals (other);
        return false;    
    }

	/**
     * Get the size of the latest revision.
     * <br/>
     * For a directory <code>0L</code> is returned.
     *
     * @return the size of the latest revision
     */
	@Override
	public long     getSize() throws RepositoryException {
        return getLastVersionInfo().getSize();
    }

	@Override
	public void readInfo() throws RepositoryException {
		DataInputStream theStream = null;

        try {
			theStream = new DataInputStream(verFile.getInputStream());

			this.readInfo(theStream);
        }
        catch (IOException iox) {
            throw new RepositoryException(iox);
        }
        finally  {
			FileUtilities.close(theStream);
        }
    }
    
	@Override
	public void writeInfo() throws RepositoryException {
	    DataOutputStream dout = null;
	    try {
			LeafObject   theFile   = revDir.createLeafObject(AbstractRepository.VERSIONFILE);
			OutputStream theStream = theFile.getOutputStream();

			try {
				theStream.write(this.getInfoString());
			}
			finally {
				FileUtilities.close(theStream);
			}
//			dout = new DataOutputStream(theFile.getOutputStream());
//	        dout.writeInt(numVersions);
//	        dout.writeUTF(author);
//	        dout.writeBoolean(deleted);
//	        dout.writeBoolean(locked);
//            if (locked) {
//        	    dout.writeUTF(locker);
//                dout.writeBoolean(inUpdate);
//            }
	    }
	    catch (IOException iox) {
	        throw new RepositoryException(iox);
	    } finally {
            FileUtilities.close(dout);
	    }
        
	}

	protected void readInfo(DataInputStream aStream) throws IOException {
		numVersions  = aStream.readInt();
		author       = aStream.readUTF();
		deleted      = aStream.readBoolean();
		locked       = aStream.readBoolean();

		if (locked) {
		    locker       = aStream.readUTF();
		    if (aStream.available() > 0) { // new format
		        inUpdate = aStream.readBoolean();
		    }
		}
		else {
		    locker   = null;   
		    inUpdate = false; 
		}
	}

	protected byte[] getInfoString() throws IOException {
		ByteArrayOutputStream theStream = new ByteArrayOutputStream();

		try {
			DataOutputStream dout = new DataOutputStream(theStream);

			dout.writeInt(numVersions);
			dout.writeUTF(author);
			dout.writeBoolean(deleted);
			dout.writeBoolean(locked);
			if (locked) {
				dout.writeUTF(locker);
				dout.writeBoolean(inUpdate);
			}

			theStream.flush();

			return theStream.toByteArray();
		} finally {
			FileUtilities.close(theStream);
		}
	}

	@Override
	public boolean isDeleted() throws RepositoryException {
		if (getIsEntry()) {
    		if (numVersions == 0) 
	    	    readInfo();
	        return deleted;
		}
	    else    
			return revDir.getName().startsWith(AbstractRepository.DELDIRPREFIX);
	}

	@Override
	public boolean isLocked() throws RepositoryException {
		if (getIsEntry()) {
			if (numVersions == 0) 
	    	    readInfo();
	        return locked;
		}
	    else    
	        return false;
	}

    @Override
	public boolean isInUpdate() throws RepositoryException {
        if (getIsEntry()) {
            if (numVersions == 0) 
                readInfo();
            return inUpdate;
        }
        else    
            return false;
    }

	@Override
	public String getAuthor () throws RepositoryException {
	    if (numVersions == 0) 
            readInfo();
	    
        return author;
    }														

	@Override
	public String getLocker () throws RepositoryException {
	    if (numVersions == 0) 
	        readInfo();
	    
	    return locker;
	}														

    @Override
	public int getNumVersions (String aUser) throws RepositoryException  {
        if (numVersions == 0)
            readInfo();
        
        int result = numVersions;
        
        if (inUpdate) {
            if (aUser == null || !aUser.equals(author))
                result --;
        }
        
        return result;
    }

    @Override
	public int getNumVersions () throws RepositoryException  {
        if (numVersions == 0)
            readInfo();
        
        int result = numVersions;
        
        if (inUpdate) {
            // All users except for the author will not see the current version
            ThreadContext tc = ThreadContext.getThreadContext();
            if (tc == null)
                result--;
            else {
                String currUser = tc.getCurrentUserName();
                if ( currUser == null
                 || !currUser.equals(author))
                    result --;
            }
        }
        
        return result;
    }

	@Override
	public int getVersions () throws RepositoryException  {
        if (numVersions == 0)
            readInfo();
	    
        return numVersions;
	}

	@Override
	public VersionInfo getLastVersionInfo () throws RepositoryException  {
        return getVersionInfo(getVersions());
	}

    @Override
	public long getLastModified() {
        try {
			return this.getLastVersionInfo().getLastModified().getTime();
        }
        catch (RepositoryException ex) {
			return 0l;
        }
    }

	/** Used by getVersionInfo to create an FileVersionInfo when needed. 
     *
     * @param num must be the reuslt of a previous getNumVersions() call.
     *
     * @return the FileVersionInfos
     */
	protected BaseVersionInfo[] createVersionInfos(int num) throws RepositoryException {
        BaseVersionInfo result[] = new BaseVersionInfo[num];

        try {
			for (LeafObject theVersion : revDir.listChildren()) {
				BaseVersionInfo fvi = new BaseVersionInfo(this, theVersion);
				// This results in parsing of the Filename, but this
				// must be done anyway :-(
				int revision = fvi.getVersion();

				if (revision > 0) {
					result[revision - 1] = fvi;
				}
	        }
        }
        catch (Exception ex) {
        	throw new RepositoryException(ex);
        }

        return result;
	}

	/**
     * Return VersionInfo for given revision number.
     * 
     * @param revision the revision number
     * 
     * @return VersionInfo for given revision number
     */
	@Override
	public VersionInfo getVersionInfo(int revision) throws RepositoryException {
        int num = getVersions();
        if (versions == null)
            versions = createVersionInfos(num);
        
        return versions[revision - 1];
	}
    
	@Override
	public int newRevision(String forUser) throws RepositoryException {
        
        assert !inUpdate : "Must not make new Revision in Update";
        
        numVersions++;          // set new Version number
        author      = forUser;  // and new author
        deleted     = false;    // reset deletion flag
        locked      = true;     // ew Revision is locked by default
        locker      = forUser;  // set the locking user
        versions    = null;     // invalidate old Versions Info
        inUpdate    = true;     // mark as "sticky" for further updates
        writeInfo();            // Write back the whole thing
        
        return numVersions;
    }

	@Override
	public int lock(String forUser) throws RepositoryException {
        assert !inUpdate : "Must not Lock when inUpdate";
        
        locked      = true;     // set lock flag
        locker      = forUser;
        writeInfo();            // Write back the whole thing
        
        return numVersions;
    }

	@Override
	public int unlock() throws RepositoryException {
        locallyUnlock();
        writeInfo();             // Write back the whole thing
        
		lastModified = this.getModifiedFromFile(); // update modification date
        return numVersions;
    }

	@Override
	public void locallyUnlock() {
		locked       = false;    // set lock flag
        locker       = null;
		inUpdate = false; // Implicitly release locked version
	}
    
    /**
     * Check if underlying File / Directory was eventually changed. 
     */
    @Override
	public boolean checkModified() {
		return this.lastModified != this.getModifiedFromFile();
    }
 
    /**
     * Update the flag in case intended Modifications where made. 
     */
    @Override
	public void updateModified() {
		this.lastModified = this.getModifiedFromFile();
    }

	protected long getModifiedFromFile() {
		return verFile.lastModified();
	}
}
