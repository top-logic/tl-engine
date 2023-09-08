/*
 * SPDX-FileCopyrightText: 2000 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dsa.repos;

import java.util.Date;

/** This class wrappes the entries used by the FileRepository.
 *
 * @author    <a href="mailto:kha@top-logic.com">Klaus Halfmann</a>
 */
public class BaseVersionInfo implements VersionInfo {

    /** Entry Info where we are contained in */
    protected BaseEntryInfo fileInfo;

    /** File representing the actual Revision. */
	protected LeafObject file;

    /** The RevisionNumber extracted from the Filename */
    protected int           version;

    /** The Deletion Flag extracted from the Filename */
    protected boolean       deleted;

    /** The Author extracted from the Filename */
    protected String        author;

    /** Constructor, should only be use by FileEntryInfo 
     *
     * @param parent    Entry Info where we are contained in
     * @param versFile  File representing the actual Revision.
     */
	protected BaseVersionInfo(BaseEntryInfo parent, LeafObject versFile) {
        fileInfo = parent;
        file     = versFile;
    }

    /**
     * Return (leaf) name of the entry (file / directory).
     *
     * @return (leaf) name of the entry (file / directory)
     */
    public EntryInfo getEntry() {
        return fileInfo;
    }

    /**
     * Return File we actually wrap, please use with care.
     */
	public LeafObject getFile() {
        return file;
    }

    /**
     * Get the the modification Date for the version,
     *
     * @return Date of last modification
     */
	@Override
	public Date getLastModified()  {
	    return new Date(file.lastModified());
    }

	/**
     * Same as {@link java.io.File#length()}.
     *
     * @return the file size
     */
	@Override
	public long getSize() {
	    return file.length();
	}
    
    /** Helper function to parse the file name into its parts.
     *
     * A file name is of format _number_flag_Author.
     * Errors while parsing are silently ignored.
     */
    protected void parseName()  {
        String name = file.getName(); 
        int len = name.length();
        if (len < 5) // "_1_x_".length()
            return;
		int pos = name.indexOf(AbstractRepository.ESCAPESTR, 1);
        if (pos < 0)
            return;
        version = Integer.parseInt(name.substring(1, pos));
        pos += 1;
        if (pos >= len) // No flag ?
            return;
        char flag = name.charAt(pos);
		deleted = flag == AbstractRepository.ESCAPE_DELFILEC;
        pos += 2;
        if (pos >= len) // No Author ?
            return;
        author = name.substring(pos);
    }

	@Override
	public int getVersion() {
    	if (version == 0)
            parseName();
        return version;    
    }

	/**
     * Return true when entry marks a deleted entry.
     *
     * @return true when entry marks a deleted entry
     */
	@Override
	public boolean  isDeleted() {
    	if (version == 0)
    	    parseName();
    	return deleted;
	}
    
	/**
     * Return name of author.
     *
     * @return name of author
     */
	@Override
	public String   getAuthor ()  {
	    if (version == 0)
	        parseName();
	    return author;
    }
    
    /**
     * Return name of base file.
     *
     * @return name of base file.
     */
    @Override
	public String getName() {
        return fileInfo.getName();
    }

    														
}
