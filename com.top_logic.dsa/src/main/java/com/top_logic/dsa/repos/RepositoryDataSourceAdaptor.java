/*
 * SPDX-FileCopyrightText: 2002 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dsa.repos;

import java.beans.IntrospectionException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Random;

import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.dob.DataObject;
import com.top_logic.dob.bean.BeanDataObject;
import com.top_logic.dsa.DataSourceAdaptor;
import com.top_logic.dsa.DatabaseAccessException;
import com.top_logic.dsa.evt.DataChangeEvent;
import com.top_logic.dsa.ex.DuplicateEntryException;
import com.top_logic.dsa.ex.NotSupportedException;
import com.top_logic.dsa.file.FilesystemDataSourceAdaptor;
import com.top_logic.dsa.impl.AbstractDataSourceAdaptor;

/**
 * Gives access to the {@link Repository} interface.
 *
 * @author    <a href="mailto:kbu@top-logic.com">Karsten Buch</a>
 */
public class RepositoryDataSourceAdaptor extends AbstractDataSourceAdaptor {

	/**
	 * Configuration of {@link RepositoryDataSourceAdaptor}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends PolymorphicConfiguration<RepositoryDataSourceAdaptor> {

		/** Configuration name for {@link #getRepository()} */
		String REPOSITORY = "repository";

		/** Configuration name for {@link #getWorkareaURL()} */
		String WORKAREAURL = "workarea-url";

		/** Configuration name for {@link #getForwardURL()} */
		String FORWARDURL = "forward-url";

		/** Filename of Repository */
		@Mandatory
		@Name(REPOSITORY)
		PolymorphicConfiguration<Repository> getRepository();

		/** Path into work area used for ULRs. */
		@Name(WORKAREAURL)
		String getWorkareaURL();

		/** Path into work area used for internal ULRs. */
		@Name(FORWARDURL)
		String getForwardURL();
	}

	/**
	 * Separator character that is used to build up the path part of a {@link DataSourceAdaptor} URI 
	 * from a list of names.
	 * 
	 * @see Repository#checkName(String)
	 */
    private static final char PATH_SEPARATOR = '/';

    /** Randomizer used to create unique names */
    protected Random            rand;
    
    /** The repository contains all the versions of a file */
    private Repository          repository;

	/** Name of property for Path into work area used for ULRS */
	protected String workareaURL = StringServices.EMPTY_STRING;

	/** Name of property for internal Path into work area used for getForwarrdUrl */
	protected String forwardURL = StringServices.EMPTY_STRING;

	/**
	 * Creates a new {@link RepositoryDataSourceAdaptor} from the given configuration.
	 * 
	 * @param context
	 *        {@link InstantiationContext} to instantiate sub configurations.
	 * @param config
	 *        Configuration for this {@link RepositoryDataSourceAdaptor}.
	 * 
	 */
	public RepositoryDataSourceAdaptor(InstantiationContext context, Config config) {
		this.repository  = context.getInstance(config.getRepository());
		this.workareaURL = config.getWorkareaURL();
		this.forwardURL  = config.getForwardURL();

		Logger.info("<init> URL        = " + workareaURL, RepositoryDataSourceAdaptor.class);
	}

	@Override
	public void close() throws DatabaseAccessException {
		this.repository = null;
    }
  
    @Override
	public String getChild (String containerPath, String elementName){
    	repository.checkName(elementName);

		return (containerPath.length() > 0) ? containerPath + PATH_SEPARATOR + elementName : elementName;
    }

    @Override
	public boolean isRepository () {
        return true;
    }

    @Override
	public boolean exists(String path) throws DatabaseAccessException {
        return repository.exists (path);
    }

    @Override
	public boolean isContainer(String path) throws DatabaseAccessException {
        RepositoryInfo info = repository.getInformation(path);
        return (info != null) && info.getIsContainer();
    }

    @Override
	public boolean isEntry(String path) throws DatabaseAccessException {
        RepositoryInfo info = repository.getInformation(path);
        return (info != null) && info.getIsEntry();
    }

    @Override
	public String getName(String path) throws DatabaseAccessException, NotSupportedException {
        int ind = path.lastIndexOf('/');

		return (ind > 0) ? path.substring(ind + 1) : path;
    }

    @Override
	public String getParent(String path) throws DatabaseAccessException, NotSupportedException {
		String theReturnValue;

        int ind = path.lastIndexOf('/');

        if (ind > 0) { // contains a slash, well
            theReturnValue = path.substring(0, ind);
        }
		else if (path.length() > 0) {
			theReturnValue = ""; // root
		}
		else {
			theReturnValue = null;
		}

		return theReturnValue;
    }

    @Override
	public InputStream getEntry(String path) throws DatabaseAccessException, NotSupportedException {
		return repository.get(getUserName(), path);
    }

    @Override
	public InputStream getEntry(String path, String version) throws DatabaseAccessException {
        try  {
			return repository.get(path, Integer.parseInt(version));
        }
        catch (NumberFormatException nfx) {
            throw new DatabaseAccessException(nfx);
        }
    }

    @Override
	public void putEntry(String aName, InputStream data) throws DatabaseAccessException {
		DataChangeEvent theEvent = new DataChangeEvent(this, DataChangeEvent.ENTRY_MODIFIED, this.getSourceName(aName));

		this.fireCheckAllow(theEvent);
		repository.create(this.getUserName(), aName, data);
		this.fireDataChanged(theEvent);
    }

    @Override
	public OutputStream getEntryOutputStream(String aName) throws DatabaseAccessException {
		DataChangeEvent theEvent = new DataChangeEvent(this, DataChangeEvent.ENTRY_MODIFIED, this.getSourceName(aName));
        this.fireCheckAllow (theEvent);

		OutputStream theStream = repository.create(this.getUserName(), aName);
		return new ChangeEventOutputStream(theStream, aName, DataChangeEvent.ENTRY_MODIFIED);
    }

    @Override
	public OutputStream putEntry(String containerPath, String elementName) throws DatabaseAccessException {
		String theName = this.getChild(containerPath, elementName);
		DataChangeEvent theEvent = new DataChangeEvent(this, DataChangeEvent.ENTRY_MODIFIED, this.getSourceName(theName));

        this.fireCheckAllow (theEvent);

		OutputStream theOut = repository.create(this.getUserName(), containerPath, elementName);
		return new ChangeEventOutputStream(theOut, theName, DataChangeEvent.ENTRY_MODIFIED);
    }

    @Override
	public String getURL(String path) throws NotSupportedException {
		if (workareaURL.isEmpty()) {
            throw new NotSupportedException("No workarealURL configured for " + getProtocol());
        }
        return workareaURL + path;
    }

    @Override
	public String getForwardURL(String path) throws NotSupportedException {
		if (this.forwardURL.isEmpty()) {
            throw new NotSupportedException("No forwardURL configured for " + getProtocol());
        }
        return this.forwardURL + path;
    }

    @Override
	public String createEntry(String containerPath, String elementName, InputStream data) throws DatabaseAccessException {
        String theName = this.getChild(containerPath, elementName);
            
		if (repository.existsEntry(theName)) {
            throw new DatabaseAccessException(theName + " already exists, use putEntry() to create new Versions");
        }

		DataChangeEvent theEvent = new DataChangeEvent(this, DataChangeEvent.ENTRY_CREATED, this.getSourceName(theName));

        this.fireCheckAllow (theEvent);
        repository.create (getUserName(), containerPath, elementName, data);
        this.fireDataChanged (theEvent);
        return theName;
    }

    @Override
	public OutputStream createEntry(String containerPath, String elementName) throws DatabaseAccessException {
        String          theName  = getChild(containerPath, elementName);
		DataChangeEvent theEvent = new DataChangeEvent(this, DataChangeEvent.ENTRY_CREATED, this.getSourceName(theName));

        this.fireCheckAllow (theEvent);
		OutputStream theOut = repository.create(this.getUserName(), containerPath, elementName);
		return new ChangeEventOutputStream(theOut, theName, DataChangeEvent.ENTRY_CREATED);
    }    

    @Override
	public String createNewEntryName(String containerPath, String prefix, String suffix) throws DatabaseAccessException {
        if (suffix == null) {
            suffix = "";
        }
        
        String  theName = prefix + suffix;
        String  thePath = getChild(containerPath, theName);
        
		if (!repository.existsEntry(thePath)) {
            return theName;
        }

        // We will not use File.createTempFile() since this will
        // actually create the File 
		if (rand == null) {
            rand = new Random();
        }
        
        for (int i=0; i < 100; i++) {
            theName = prefix + rand.nextInt(99999) + suffix;
            thePath = getChild(containerPath, theName);
			if (!repository.existsEntry(thePath)) {
                return theName;
            }
        }

        throw new DatabaseAccessException("Unable to createNewEntryName('" + containerPath + "','" + prefix + "','" + suffix + "') in " + repository);
	}

    @Override
	public String createContainer(String containerPath, String elementName) throws DatabaseAccessException {
        String          theName  = this.getChild(containerPath, elementName);
        DataChangeEvent theEvent = new DataChangeEvent(this, DataChangeEvent.CONTAINER_CREATED, this.getSourceName(theName));

		this.fireCheckAllow(theEvent);
        String result = null;

		if (repository.mkdir(containerPath, elementName)) {
            result = (containerPath.length () != 0) ? containerPath + '/' + elementName : elementName;
        } 
        else {
			throw new DuplicateEntryException("createContainer('" + containerPath + "','" +   elementName + "') failed for " + repository.toString());
		}

        this.fireDataChanged (theEvent);
        return result;
    }

    @Override
	public void delete(String path, boolean force) throws DatabaseAccessException {
        RepositoryInfo info = repository.getInformation(path);

        if (info == null) {
        	//this is no good!! it would spoil up the whole deletion process of a wrapper just because
			// some repository folder doesn't exist anymore!
            //throw new DatabaseAccessException("cannot delete('" + aName + "') it does not exist");
        	Logger.warn("cannot delete('" + path + "') it does not exist", this);
        	return;
        }

		int mode = info.getIsContainer() ? DataChangeEvent.CONTAINER_DELETED : DataChangeEvent.ENTRY_DELETED;
		DataChangeEvent theEvent = new DataChangeEvent(this, mode, this.getSourceName(path));
        this.fireCheckAllow (theEvent);

        if (info.getIsEntry()) {
            repository.delete (getUserName(), path, force);
        }
        else if (info.getIsContainer()) {
            if (!repository.rmdir (path)) {
                throw new DatabaseAccessException("failed to delete('" + path + "')");
            }
        }
		else {
			throw new DatabaseAccessException("cannot delete('" + path + "') is neither file nor directory?");
		}

        this.fireDataChanged (theEvent);
    }

    @Override
	public void deleteRecursively(String path) throws NotSupportedException {
		throw new NotSupportedException("deleteRecursively is not supported by " + this.getClass().getName());
    }

	@Override
	public String[] getEntryNames(String path) throws DatabaseAccessException {
		Collection<? extends RepositoryInfo> entries = repository.getEntries(path);
		String[] result = new String[entries.size()];
		int i = 0;

		// Care about directories first
		for (RepositoryInfo info : entries) {
			if (!info.getIsContainer()) {
				continue;
			}
			result[i++] = info.getPath();
		}

		// Now care about files
		for (RepositoryInfo info : entries) {
			if (!info.getIsEntry()) {
				continue;
			}
			result[i++] = info.getPath();
		}
		if (i != 0) {
			return result;
		} else {
			return null; // as specified in DAP
		}
	}

    @Override
	public String[] getVersions(String path) throws DatabaseAccessException {
        RepositoryInfo info = repository.getInformation(path);

        if (info == null) {
			// Check if path exists
			throw new DatabaseAccessException("Path '" + path + "' does not exist.");
        }     
		else if (info.getIsContainer()) {
			// Check if path is directory
			throw new DatabaseAccessException("Cannot get versions for container '" + path + "'");
		}

        // PrepositoryInfo is EntryInfo
        EntryInfo eInfo = (EntryInfo) info;
                
        String[] result = null;
        int num = eInfo.getNumVersions();
        result  = new String[num];
        int idx = 0;
        for (int i = num; i > 0; i--, idx++) {
            result[idx] = Integer.toString(i);
        }
        return result;
    }

    @Override
	public DataObject getProperties(String path) throws DatabaseAccessException {
        try {
            RepositoryInfo info = repository.getInformation(path);
            if (info == null) {
            	return null;
            }
            if (info instanceof EntryInfo) {
            	return new BeanDataObject(info, EntryInfo.class);
            } else {
            	return new BeanDataObject(info, RepositoryInfo.class);
            }
        } catch (Exception e) {
            throw new DatabaseAccessException(e);
        }
    }

    @Override
	public DataObject getProperties(String path, String version) throws DatabaseAccessException {
        RepositoryInfo info = repository.getInformation(path);

        if (info == null) {
			// Check if path exists
			throw new DatabaseAccessException("Path '" + path + "' does not exist.");
        }     
		else if (info.getIsContainer()) {
			// Check if path is directory
			return new FilesystemDataSourceAdaptor(path).getProperties(path);
        }
        // RepositoryInfo is EntryInfo
        EntryInfo eInfo = (EntryInfo) info;
        
        // Check if given version exists
        int numVersions = eInfo.getNumVersions();      
        int theVersion = 0;

        try {
            theVersion = Integer.parseInt(version);        
        }
        catch (NumberFormatException nfe) {
            throw new DatabaseAccessException("Invalid version number '" + version + "'");
        }
        
        if (theVersion > numVersions || theVersion <= 0) {
            throw new DatabaseAccessException("Version number '" + theVersion + "' is out of range");
        }

        try {
			VersionInfo vInfo = eInfo.getVersionInfo(theVersion);
			return new BeanDataObject(vInfo, VersionInfo.class);
        } 
        catch (IntrospectionException e) {
            throw new DatabaseAccessException("Cannot get properties from version '" + version + "' due to an introspection error");
        }
    }

    @Override
	public boolean lock(String path) throws DatabaseAccessException {
		return repository.lock(this.getUserName(), path);
    }

    @Override
	public boolean unlock(String path) throws DatabaseAccessException {
		return repository.unlock(this.getUserName(), path);
    }

    private String getUserName() {
        ThreadContext context = ThreadContext.getThreadContext();
        if (context != null) {
            String name = context.getCurrentUserName();
            if (name != null)
                return name;
        }
        return "guest";
    }
    
	@Override
	public String toString() {
		// Note: This value is printed in the application monitor to identify the repository.
		return repository.toString();
	}

	/**
	 * This class performs like an normal {@link FileOutputStream} but its close method fires an
	 * data changed event.
	 * <p>
	 * This is why this stream should be returned by all methods, which allow the modification of an
	 * entry by an output stream.
	 * </p>
	 *
	 * @author <a href="mailto:tri@top-logic.com">Thomas Richter</a>
	 */
    class ChangeEventOutputStream extends java.io.BufferedOutputStream {

        // Attributes

        /** The name of the entry to be written. */
        private String entryName;
        
        /** The kind of change. */
        private int kindOfChange;

        private boolean alreadyNotified;        

        // Constructors

        /**
		 * Constructor
		 *
		 * @param aStream
		 *        the stream to create the output stream for.
		 * @param anEntryName
		 *        the External Name of the entry.
		 * @param kindOfDataChange
		 *        theEvent sent on close()
		 */
		public ChangeEventOutputStream(OutputStream aStream, String anEntryName, int kindOfDataChange) {
			super(aStream);
            this.entryName      = anEntryName;
            this.kindOfChange   = kindOfDataChange;
        }
            
        // Public methods

        @Override
		public void finalize() throws Throwable {
            if (!this.alreadyNotified) {
                Logger.error("Stream for " + entryName + " in " + 
                    RepositoryDataSourceAdaptor.this + " was not closed",
                    RepositoryDataSourceAdaptor.this);
                this.alreadyNotified = true;
                // Notification won work here anymore ...   
            }
            super.finalize();
        }

        /**
         * Close the stream and fires an data changed event, when closing
         * succeeds.
         *
         * @throws    IOException    If closing fails.
         */
		@Override
		public void close () throws IOException {
			if (Logger.isDebugEnabled(this)) {
				Logger.debug("Close stream for " + this.entryName, this);
            }

			super.close();

            if (!this.alreadyNotified) {
                this.alreadyNotified = true;
				fireDataChanged(new DataChangeEvent(this, kindOfChange, this.getSourceName()));
            }
        }

		@SuppressWarnings("synthetic-access")
		private String getSourceName() {
			return RepositoryDataSourceAdaptor.this.getSourceName(this.entryName);
		}
    }
}

