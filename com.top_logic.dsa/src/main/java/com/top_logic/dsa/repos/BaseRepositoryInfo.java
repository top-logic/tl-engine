/*
 * SPDX-FileCopyrightText: 2000 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dsa.repos;

import java.io.File;

import com.top_logic.basic.Logger;

/** This class wrappes an Item (entry/container) in the FileRepository.
 *
 * It is used internally by the 
 * {@link AbstractRepository} to cache
 * information needed for its operation. Most accessors work on
 * demand to waste as little time as possible.
 *
 * The Implementation mainly matches the needs for containers,
 * additional infos on entries are handled via 
 * {@link BaseEntryInfo}
 * 
 * @author    <a href="mailto:kha@top-logic.com">Klaus Halfmann</a>
 */
public class BaseRepositoryInfo implements RepositoryInfo {

    /** Directory where the revisions are stored or a normal Directory */
	protected ContainerObject revDir;

    /** Name as visible from user Space */
    protected String          name;

    /** Path as visible from user Space, using '/' as delimiter */
    protected String          path;

    /** Path just by the FileSytem, using File.separatorChar as delimiter */
    protected String          systemPath;

    /** Parent path as visible from user Space, using '/' as delimiter */
    protected String          parentPath;

    /** Modification Date of revDir (needed for refetch) */
    protected long            lastModified;

    /** Indicates that the top-level-revision is deleted */
    protected boolean         deleted;
    
    /**
	 * Construct a FileEntryInfo from a Directory.
	 *
	 * Escape processing of directory names must be done by caller (the file repository).
	 *
	 * @param relDir
	 *        Directory with relative path (not escaped)
	 * @param absDir
	 *        Directory with absolute path actually used by repository (escaped)
	 *
	 * @throws RepositoryException
	 *         in case aRevDir is not a directory or does not exist.
	 */
	public BaseRepositoryInfo(ContainerObject relDir, ContainerObject absDir) throws RepositoryException {
        name        = relDir.getName();
        parentPath  = normalizePath(relDir.getParent());
        systemPath  = relDir.getPath();
        path        = normalizePath(systemPath); 
        if (absDir.exists()) { // a simple directory 
            if (!absDir.isDirectory())
                throw new RepositoryException("'" + path + "' in '" + absDir.getPath() + "' must be a Directory ");
            revDir      = absDir;
        } else {
            // Try a "File" (revision folder) with that name 
			String fName = AbstractRepository.FILEPREFIX + name;
			ContainerObject theFile = absDir.getParentContainer().createContainer(fName);
            if (theFile.exists())  {
                if (!theFile.isDirectory())
                    throw new RepositoryException(path + " must be a Directory");
                revDir = theFile;
            }
            else {
                // Try a deleted Folder with that name 
				String delname = AbstractRepository.DELDIRPREFIX + name;
				ContainerObject delDir = absDir.getParentContainer().createContainer(delname);
                if (delDir.exists()) {
                    if (!delDir.isDirectory())
                        throw new RepositoryException(path + " must be a Directory");
                    revDir = delDir;
                }
                else
                    throw new NoEntryException("No Entry for '" + path + "' in '"
                                                + absDir + "' found");
            }
        }
        this.lastModified = revDir.lastModified();       
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
	public BaseRepositoryInfo(ContainerObject relParent, ContainerObject absParent, String fName) throws RepositoryException {
		parentPath = "";
        
        if (relParent != null) {
            String pPath = relParent.getPath();
            parentPath   = normalizePath(pPath);
            path         = parentPath + '/' + fName; 
			systemPath   = this.createSystemPath(pPath, fName);
        }
        else
            path       = systemPath = fName; 
            
        name  = fName;
        
		String dirname = AbstractRepository.escapeName(fName);
		ContainerObject theDir = absParent.createContainer(dirname);
        if (theDir.exists()) {
            revDir = theDir;
            return;
            // throw new RepositoryException("'" + fName + "' in '" + parentPath + "' is a Directory");
        }

		theDir = absParent.createContainer(AbstractRepository.DELDIRPREFIX + dirname);
        if (theDir.exists())  {
            revDir = theDir;
            return;
            // throw new RepositoryException("'" + fName + "' in '" + parentPath + "' is a (deleted) Directory");
        }

        // Create a "File" (revision folder) with that name 
		fName = AbstractRepository.FILEPREFIX + fName;
		revDir = absParent.createContainer(fName);
        this.lastModified = revDir.lastModified();       
    }

    /**
	 * Equals is based on the path. It works on other FileEntryInfos and Strings. <br/>
	 * This could be used the caching mechanisms used by the FileBasedRepository2. <br/>
	 * Note: This method is <em>not</em> <i>symmetric</i> as specified in
	 * {@link java.lang.Object#equals(java.lang.Object)} when used on a String. (Design decision by
	 * KHA.)
	 *
	 * @param other
	 *        the object to compare to
	 *
	 * @return true if equals else false
	 */
    @Override
	public boolean equals(Object other) {
    	if (other == this) {
    		return true;
    	}
        if (other instanceof BaseRepositoryInfo) { 
            return path.equals (((BaseRepositoryInfo) other).path);
        }
		// TODO #19482: FindBugs(EQ_CHECK_FOR_OPERAND_NOT_COMPATIBLE_WITH_THIS)
        if (other instanceof String) {
            return path.equals (other);
        }
        return false;    
    }
    
    /**
     * The hashCode is based on the path.
     *
     * @return the hash code
     */
    @Override
	public int hashCode() {
        return path.hashCode();
    }

    /** 
     * A string description of this instance.
     *
     * @return some String usefull for debugging.
     */
    @Override
	public String toString() {
        return this.getClass().getName() + " for " + path;
    }

    /**
     * Return (leaf) name of the entry (file / directory).
     *
     * @return (leaf) name of the entry (file / directory)
     */
	@Override
	public String   getName() {
        return name;
	}
    
	/**
     * Return Complete path name of the entry (file / directory).
     *
     * @return Complete path name of the entry (file / directory)
     */
	@Override
	public String   getPath()  {
        return path; 
    }

	/**
	 * Return Complete path name of the entry as used by File
	 *
	 * @return Complete path name of the entry as used by File
	 */
	public String   getSystemPath()  {
	    return systemPath; 
	}

	/**
     * Return parent path of the entry (file / directory).
     * 
     * @return parent path of the entry (file / directory)
     */
	@Override
	public String   getParentPath()  {
	    return parentPath;
    }

	/**
     * Get the size of the latest revision.
     * <br/>
     * <code>0L</code> is returned, this is ok for containers,
     * overwrite this method for EntryInfos.
     *
     * @return the size of the latest revision
     */
	@Override
	public long     getSize() throws RepositoryException {
        return 0L;
	}
    
    /**
     * Similar to {@link java.io.File#isFile()}.
     * <br/>
     * Note: Directories/Files here are <em>not</em> equivalent
     * to Directories/Files in the file system.
     * 
     * @return true if a file is refered
     */
    @Override
	public boolean  getIsEntry() {
        //The existance of revDir should be ensured already.
		return revDir.getName().startsWith(AbstractRepository.FILEPREFIX);
    }

    
	/**
     * Similar to  {@link java.io.File#isDirectory()}.
     * <br/>
     * Note: Directories/Files here are <em>not</em> equivalent
     * to Directories/Files in the file system.
     * 
     * @return true if a directory is refered
     */
	public boolean  isDirectory()  {
		return !revDir.getName().startsWith(AbstractRepository.FILEPREFIX);
	}
    
    /**
     * Similar to  {@link java.io.File#isDirectory()}.
     * <br />
     * Note: Directories/Files here are <em>not</em> equivalent
     * to Directories/Files in the file system.
     * 
     * @return true if a container (directory) is refered
     */
    @Override
	public boolean getIsContainer() {
		return !revDir.getName().startsWith(AbstractRepository.FILEPREFIX);
    }

    @Override
	public long getLastModified() throws RepositoryException {
        return revDir.lastModified();
    }

    @Override
	public boolean isDeleted() throws RepositoryException {
		return revDir.getName().startsWith(AbstractRepository.DELDIRPREFIX);
    }
    
    /** Accessor to revDir, please use with care */
	public ContainerObject getRevDir() {
        return revDir;
    }

	@Override
	public boolean checkModified() {
		try {
			return this.lastModified != revDir.lastModified();
		}
		catch (RepositoryException ex) {
			return true;
		}
    }
 
	@Override
	public void updateModified() {
        try {
			this.lastModified = revDir.lastModified();
		}
        catch (RepositoryException ex) {
			Logger.error("Failed to update last modified", ex, BaseRepositoryInfo.class);
		}
    }
    
    /**
     * Helper function to convert \path\file into /path/file.
     */
    protected String normalizePath(String aPath) {
        int ind;
		if (File.separatorChar == '/' || aPath == null || (ind = aPath.indexOf(File.separatorChar)) < 0) {
			return aPath;
		}
        int          len    = aPath.length();
        StringBuffer result = new StringBuffer(len);
        char         chars[]= aPath.toCharArray();
        result.append(chars, 0, ind);
        result.append('/');
        for (int i = ind+1; i < len; i++)  {
            char c = chars[i];
            if (c == File.separatorChar)
                result.append('/');
            else    
                result.append(c);
        }
        return result.toString();
    }

	/**
	 * Create the system path for the path and file name.
	 * 
	 * @param aPath
	 *        Path of the system path.
	 * @param aName
	 *        Name of the represented object.
	 * @return The requested path.
	 */
	protected String createSystemPath(String aPath, String aName) {
		return aPath + File.separatorChar + aName;
	}
}
