/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dsa.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import com.top_logic.basic.ArrayUtil;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.dob.DataObject;
import com.top_logic.dob.simple.FileDataObject;
import com.top_logic.dsa.DataAccessProxy;
import com.top_logic.dsa.DataSourceAdaptor;
import com.top_logic.dsa.DatabaseAccessException;
import com.top_logic.dsa.evt.DataChangeEvent;
import com.top_logic.dsa.ex.NotSupportedException;
import com.top_logic.dsa.file.DOImporter.ConverterConfig;
import com.top_logic.dsa.impl.AbstractDataSourceAdaptor;

/**
 * Base class for {@link DataSourceAdaptor}s based on {@link File}s.
 */
public abstract class AbstractFilesystemDataSourceAdaptor extends AbstractDataSourceAdaptor {

	/** Name of the standard {@link ConverterConfig}. */
	public static final String STANDARD_IMPORT = "DOStandardImporter";

	/**
	 * Configuration options for {@link AbstractFilesystemDataSourceAdaptor}.
	 */
	public interface Config<I extends AbstractFilesystemDataSourceAdaptor> extends PolymorphicConfiguration<I> {

		/** Name of the configuration {@link #getConverterMapping()}. */
		String CONVERTER_MAPPING = "converterMapping";

		/** Mapping for the DOConverter. */
		@StringDefault(STANDARD_IMPORT)
		@Name(CONVERTER_MAPPING)
		String getConverterMapping();

		/**
		 * Setter for {@link #getConverterMapping()}.
		 * 
		 * @param value
		 *        New value of {@link #getConverterMapping()}.
		 */
		void setConverterMapping(String value);
	}

	/** Separator to be used with files */
	protected static final char EXTERNAL_SEPARATORCHAR = '/';

	private final String converterMapping;

	/** Randomize used to create unique names */
	private Random rand;

	/**
	 * Creates a {@link AbstractFilesystemDataSourceAdaptor} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public AbstractFilesystemDataSourceAdaptor(InstantiationContext context, Config<?> config) {
		this.converterMapping = config.getConverterMapping();
	}

	/**
	 * Creates a {@link AbstractFilesystemDataSourceAdaptor}.
	 */
	public AbstractFilesystemDataSourceAdaptor() {
		this.converterMapping = STANDARD_IMPORT;
	}

	/**
	 * Does the element already exist?
	 *
	 * @param path
	 *        the name of the element
	 * @return true if the element exists
	 */
	@Override
	public boolean exists(String path)
			throws DatabaseAccessException {
		if (Logger.isDebugEnabled(this)) {
			Logger.debug("exists(" + path + ")", this);
		}

		return (this.getFile(path).exists());
	}

	/**
	 * Is the element an entry or a container?
	 *
	 * @param path
	 *        the name of the element
	 * @return true if it is a container
	 */
	@Override
	public boolean isContainer(String path)
			throws DatabaseAccessException {
		// Logger.debug ("isContainer(" + name + ")" , this);
		return getFile(path).isDirectory();
	}

	/**
	 * Is the element an entry or a container?
	 *
	 * @param path
	 *        the name of the element
	 * @return true if it is an entry
	 */
	@Override
	public boolean isEntry(String path)
			throws DatabaseAccessException {
		// Logger.debug ("isEntry(" + name + ")" , this);
		return getFile(path).isFile();
	}

	/**
	 * Get the name of the element (entry/container)
	 *
	 * @param path
	 *        the name of the element
	 * @return the name of the entry/container (not fully qualified)
	 */
	@Override
	public String getName(String path)
			throws DatabaseAccessException {
		// Logger.debug ("getName(" + name + ")" , this);
		if (path.length() > 0) {
			return getFile(path).getName();
		} else
			return path; // root may return '.' otherwise ...
	}

	/**
	 * Rename the element (entry/container)
	 *
	 * @param oldPath
	 *        the fully qualified name of the element
	 * @param newName
	 *        the new element name of the element
	 * @return true, if renaming succeeded
	 */
	@Override
	public String rename(String oldPath, String newName)
			throws DatabaseAccessException {
		File currFile = getFile(oldPath);
		File newFile = new File(currFile.getParentFile(), newName);
		if (!currFile.renameTo(newFile)) {
			throw new DatabaseAccessException("Failed to rename '" +
				oldPath + " ' to '" + newName + "'");
		} else {
			return newName;
		}
	}

	/**
	 * Move some element or container to some other location.
	 *
	 * @param oldPath
	 *        the fully qualified name of the element
	 * @param newPath
	 *        the path where the new Object will be found
	 * @return path to the new object, not necessary equal to the given old path
	 */
	@Override
	public String move(String oldPath, String newPath)
			throws DatabaseAccessException {
		File currFile = getFile(oldPath);
		File newFile = getFile(newPath);
		if (!currFile.renameTo(newFile)) {
			throw new DatabaseAccessException("Failed to move '" +
				oldPath + " ' to '" + newPath + "'");
		} else {
			return newPath;
		}
	}

	/**
	 * Get the name of the parent container of the given element (entry/container).
	 *
	 * Since the names always must be relative pathes cotainige only '/' as seprator we can use a
	 * simple String operation.
	 *
	 * @param path
	 *        the name of the element
	 * @return the name of the parent container (fully qualified) , null for the top-level container
	 */
	@Override
	public String getParent(String path)
			throws DatabaseAccessException {

		if (path.length() == 0) {
			return null;
		}

		int index = path.lastIndexOf(EXTERNAL_SEPARATORCHAR);
		if (index > 0) {
			return path.substring(0, index);
		} else
			return "";
	}

	/**
	 * Create the complete path to a child for a given parent.
	 *
	 * @param containerPath
	 *        complete path to the parent
	 * @param elementName
	 *        name of a (potential) child.
	 * @return the complete path to the child.
	 */
	@Override
	public String getChild(String containerPath, String elementName)
			throws DatabaseAccessException {

		if (containerPath.length() > 0) {
			return containerPath + EXTERNAL_SEPARATORCHAR + elementName;
		} else
			return elementName;
	}

	/**
	 * Get an entry from a database.
	 *
	 * @param path
	 *        the name of the entry
	 * @return an {@link InputStream} for reading the entry's data
	 *
	 * @throws DatabaseAccessException
	 *         in case this is not an Entry of it does not exist
	 */
	@Override
	public InputStream getEntry(String path)
			throws DatabaseAccessException {

		// Logger.debug ("getEntry(" + name + ")" , this);
		File theFile = getFile(path);
		if (!theFile.isFile()) {
			throw new DatabaseAccessException("FilesystemDataSourceAdaptor.getEntry(" + path + "): not an Entry: "
				+ theFile);
		}

		FileInputStream fis = null;
		try {
			fis = new FileInputStream(theFile);
		} catch (FileNotFoundException e) {
			throw new DatabaseAccessException(e);
		}
		return fis;
	}

	/**
	 * Import the XMLFormat generated by the DOXMLWriter.
	 * 
	 * This works fine to simulate other Datasources like an SAP-DSA.
	 * 
	 * @see com.top_logic.dsa.DataSourceAdaptor#getObjectEntry(java.lang.String)
	 * 
	 * @return null when an empty XML-Structure was found.
	 */
	@Override
	public DataObject getObjectEntry(String path) throws DatabaseAccessException {
		DOImporter theImporter = new DOImporter(this.converterMapping);
		theImporter.doImport(getFile(path));
		Collection theDOs = theImporter.getDoColl();
		Iterator theIter = theDOs.iterator();
		if (theIter.hasNext()) {
			DataObject theResultObject = (DataObject) theIter.next();
			return theResultObject;
		}

		return null;
	}

	/**
	 * Put an entry in a database.
	 *
	 * <p>
	 * Note: The given {@link InputStream} is closed after this operation.
	 * </p>
	 *
	 * @param path
	 *        The name of the element
	 * @param aSource
	 *        The physical data given as an {@link OutputStream}
	 */
	@Override
	public void putEntry(String path, InputStream aSource)
			throws DatabaseAccessException {

		File theFile = getFile(path);

		if (!theFile.exists()) {
			throw new DatabaseAccessException("putEntry: does not exist - " + path);
		}
		if (!theFile.isFile()) {
			throw new DatabaseAccessException("putEntry: not an Entry ! - " + path);
		}

		DataChangeEvent theEvent = new DataChangeEvent(this,
			DataChangeEvent.ENTRY_MODIFIED,
			this.getSourceName(path));

		this.fireCheckAllow(theEvent);
		try {
			FileUtilities.copyFile(aSource, theFile);
		} catch (IOException iox) {
			Logger.warn("Unable to _putEntry(" + path + ") ", iox, this);
			throw new DatabaseAccessException(iox);
		}
		this.fireDataChanged(theEvent);
	}

	/**
	 * Put a new entry in a container.
	 *
	 * @param elementName
	 *        the name/id for the new entry
	 *
	 * @return an {@link OutputStream} to the new entry
	 */
	@Override
	public OutputStream putEntry(String containerPath, String elementName)
			throws DatabaseAccessException {
		String childPath = getChild(containerPath, elementName);
		File theFile = this.getFile(childPath);

		if (!theFile.exists()) {
			throw new DatabaseAccessException("putEntry(" + theFile +
				") does not exist");
		}

		return this.getEntryOutputStream(childPath,
			theFile,
			DataChangeEvent.ENTRY_MODIFIED);
	}

	/**
	 * Get an {@link OutputStream} for putting an entry in a database.
	 *
	 * @param aName
	 *        the name of the element
	 * @return an {@link OutputStream} to write data into the entry
	 */
	@Override
	public OutputStream getEntryOutputStream(String aName)
			throws DatabaseAccessException {

		return getEntryOutputStream(aName, null, DataChangeEvent.ENTRY_MODIFIED);
	}

	/**
	 * Get an {@link OutputStream} for appendding to an existing entry.
	 *
	 * Overwritten to implement the appending on a {@link FileOutputStream}.
	 * 
	 * @param path
	 *        the name of the element
	 * @return an {@link OutputStream} to append data to the entry
	 * @see com.top_logic.dsa.DataSourceAdaptor#getEntryAppendStream(java.lang.String)
	 */
	@Override
	public OutputStream getEntryAppendStream(String path)
			throws DatabaseAccessException {
		// delegates to the intenal method
		return getEntryOutputStream(path, null, DataChangeEvent.ENTRY_MODIFIED, true);
	}

	/**
	 * Create a new entry in a container.
	 *
	 * data will be closed after this operation.
	 *
	 * @param elementName
	 *        the name/id for the new entry
	 * @param data
	 *        the physical data given as an {@link OutputStream}
	 */
	@Override
	public String createEntry(String containerPath, String elementName, InputStream data)
			throws DatabaseAccessException {
		String childPath = getChild(containerPath, elementName);
		File theFile = this.getFile(childPath);

		if (theFile.exists()) {
			throw new DatabaseAccessException("createEntry(" +
				getProtocol() + DataAccessProxy.SEPARATOR + childPath + ") already exists");
		}

		DataChangeEvent theEvent = new DataChangeEvent(this,
			DataChangeEvent.ENTRY_CREATED,
			this.getSourceName(childPath));
		this.fireCheckAllow(theEvent);
		try {
			FileUtilities.copyFile(data, theFile);
		} catch (IOException iox) {
			throw new DatabaseAccessException(iox);
		}

		this.fireDataChanged(theEvent);

		return childPath;
	}

	/**
	 * Create a new entry in a container.
	 *
	 * @param elementName
	 *        the name/id for the new entry
	 *
	 * @return an {@link OutputStream} to the new entry
	 */
	@Override
	public OutputStream createEntry(String containerPath, String elementName)
			throws DatabaseAccessException {

		String childPath;
		if (containerPath.length() > 0) {
			childPath = containerPath + EXTERNAL_SEPARATORCHAR + elementName;
		} else {
			childPath = elementName;
		}

		File theFile = this.getFile(childPath);

		if (theFile.exists()) {
			throw new DatabaseAccessException("createEntry(" +
				getProtocol() + DataAccessProxy.SEPARATOR + childPath + ") already exists");
		}

		return this.getEntryOutputStream(childPath, theFile, DataChangeEvent.ENTRY_CREATED);
	}

	/**
	 * Internal function.
	 *
	 * @param aName
	 *        the name of the element.
	 * @param aFile
	 *        The file to be used, created when <code>null</code>.
	 * @param aMode
	 *        type of event ({@link DataChangeEvent#ENTRY_CREATED} /
	 *        {@link DataChangeEvent#ENTRY_DELETED} / {@link DataChangeEvent#ENTRY_MODIFIED}) to
	 *        fire
	 * @return An {@link OutputStream} to write data into the entry.
	 */
	public OutputStream getEntryOutputStream(String aName,
			File aFile,
			int aMode)
			throws NotSupportedException, DatabaseAccessException {
		// changed by asc, uses the new version of this method
		// which supports appending
		return getEntryOutputStream(aName, aFile, aMode, false);
	}

	/**
	 * Internal function.
	 *
	 * @param path
	 *        the name of the element.
	 * @param aFile
	 *        The file to be used, created when <code>null</code>.
	 * @param aMode
	 *        type of event ({@link DataChangeEvent#ENTRY_CREATED} /
	 *        {@link DataChangeEvent#ENTRY_DELETED} / {@link DataChangeEvent#ENTRY_MODIFIED}) to
	 *        fire
	 * @return An {@link OutputStream} to write data into the entry.
	 */
	public OutputStream getEntryOutputStream(String path,
			File aFile,
			int aMode, boolean isAppendMode)
			throws DatabaseAccessException {
		ChangeEventOutputStream theFOS = null;
		DataChangeEvent theEvent = new DataChangeEvent(this, aMode,
			this.getSourceName(path));
		this.fireCheckAllow(theEvent);

		try {
			if (aFile == null) {
				aFile = this.getFile(path);
			}

			theFOS = new ChangeEventOutputStream(aFile, path, aMode, isAppendMode);
		} catch (FileNotFoundException ex) {
			throw new DatabaseAccessException("getEntryOutputStream: " +
				"FileNotFound - " + path + ": ", ex);
		} catch (IOException ex) {
			throw new DatabaseAccessException("getEntryOutputStream: " +
				"IOException - " + path + ": ", ex);
		}

		return (theFOS);
	}

	/**
	 * Create a new entry name that is guaranteed not to be used by any of the existing children.
	 * 
	 * <code>this</code> must be a container. The prefix and/or suffix may be ignored by the
	 * implementation. The name is unique as of the time of creation. Any subsequent call may or may
	 * not return the same name. The container is not actually modified.
	 * 
	 * @param containerPath
	 *        name of container where entry shall be created
	 * @param prefix
	 *        a prefix used a hint for the new name.
	 * @param suffix
	 *        a suffix used a hint for the new name.
	 * 
	 * @return a String suiteable fore one of the <code>create...</code> Methods
	 */
	@Override
	public String createNewEntryName(String containerPath, String prefix, String suffix)
			throws DatabaseAccessException {

		if (suffix == null) {
			suffix = "";
		}

		String newName = prefix + suffix;
		String childPath = getChild(containerPath, newName);
		File theDir = this.getFile(childPath);

		if (!theDir.exists()) {
			return newName;
		}

		// We will not use File.createTempFile() since this will
		// actually create the File

		if (rand == null) { // Create rand on demand
			rand = new Random();
		}

		for (int i = 0; i < 100; i++) {
			newName = prefix + rand.nextInt(99999) + suffix;
			childPath = getChild(containerPath, newName);
			theDir = this.getFile(childPath);

			if (!theDir.exists()) {
				return newName;
			}
		}
		throw new DatabaseAccessException("Unable to createNewEntryName()");
	}

	/**
	 * Create a container in the given parent container.
	 *
	 * @param containerPath
	 *        the fully qualified name of the parent container
	 * @param elementName
	 *        the name/id for the new container
	 *
	 * @return the fully qualified name of the created container
	 */
	@Override
	public String createContainer(String containerPath, String elementName)
			throws DatabaseAccessException {

		String childPath = getChild(containerPath, elementName);
		File theDir = this.getFile(childPath);

		if (theDir.exists()) {
			throw new DatabaseAccessException(
				"createContainer(" + childPath + ") already in " + getProtocol());
		}

		DataChangeEvent theEvent = new DataChangeEvent(this,
			DataChangeEvent.CONTAINER_CREATED, getSourceName(childPath));

		this.fireCheckAllow(theEvent);
		theDir.mkdirs();
		this.fireDataChanged(theEvent);

		return childPath;
	}

	/**
	 * Get the element names of the current container.
	 *
	 * @param path
	 *        the name of the container.
	 * @return an array of Strings with the fully qualified element names null in case there are no
	 *         entries
	 */
	@Override
	public String[] getEntryNames(String path)
			throws DatabaseAccessException {
		// Logger.debug ("getEntryNames(" + name + ")" , this);

		try {
			return internalGetEntryNames(path);
		} catch (IOException ex) {
			throw new DatabaseAccessException(ex);
		}
	}

	private String[] internalGetEntryNames(String path) throws IOException {
		String[] content;
		List<File> files = filesForPath(path);
		switch (files.size()) {
			case 0:
				content = null;
				break;
			case 1:
				File uniqueFile = files.get(0);
				if (uniqueFile.isDirectory()) {
					String[] entryNames = FileUtilities.list(uniqueFile);
					content = postProcessEntryNames(entryNames, path);
				} else {
					content = null;
				}
				break;
			default:
				Collection<String> names = new HashSet<>();
				boolean exists = false;
				for (File file : files) {
					if (file.exists() && file.isDirectory()) {
						Collections.addAll(names, FileUtilities.list(file));
						exists = true;
					}
				}
				if (exists) {
					String[] entryNames = names.toArray(ArrayUtil.EMPTY_STRING_ARRAY);
					content = postProcessEntryNames(entryNames, path);
				} else {
					content = null;
				}

		}
		return content;
	}

	private String[] postProcessEntryNames(String[] entries, String path) {
		if (path.isEmpty()) {
			return entries;
		}
		if (entries.length == 0) {
			return null;
		}
		for (int i = 0; i < entries.length; i++) {
			entries[i] = path + EXTERNAL_SEPARATORCHAR + entries[i];
			// Logger.debug ("entryName: " + entries [ i ], this);
		}
		return entries;
	}

	/**
	 * Get the database specific properties of an element.
	 *
	 * @param path
	 *        the name of the element
	 * @return the database specific properties
	 */
	@Override
	public DataObject getProperties(String path)
			throws DatabaseAccessException {

		return new FileDataObject(getFile(path));
	}

	/**
	 * Set the database specific properties of an element.
	 *
	 * @param path
	 *        the name of the element
	 */
	@Override
	public void setProperties(String path, DataObject propsDob)
			throws DatabaseAccessException, NotSupportedException {

		throw new NotSupportedException("setProperties() is not supported any< more");
		/* Logger.debug ("setProperties(" + name + ")" , this); File f = getFile (name); try {
		 * Boolean // Set the read only property if (props.getIsWriteable () != null) { if
		 * (!props.getIsWriteable ().booleanValue ()) { f.setReadOnly (); } }
		 * 
		 * // Set the last modified property if (props.getLastModified () != null) { long lastMod =
		 * props.getLastModified ().longValue (); f.setLastModified (lastMod); } } catch (Exception
		 * e) { throw new DatabaseAccessException
		 * ("FilesystemDataSourceAdaptor: Error setting the properties:" + e); } */
	}

	/**
	 * Get a {@link File} for a given path name.
	 *
	 * @param path
	 *        the entry path
	 * @return the {@link File}
	 *
	 */
	protected abstract File getFile(String path);

	/**
	 * Gets all {@link File}s related to a given path name.
	 *
	 * @param path
	 *        the entry path
	 * @return the {@link File}s correponding to the given path name.
	 */
	protected abstract List<File> filesForPath(String path);

	/**
	 * This class performs like an normal Fileoutputstream but its close method fires an datachanged
	 * event. This is why this stream should be returned by all methods, which allow the
	 * modification of an entry by an outputstream.
	 */
	class ChangeEventOutputStream extends FileOutputStream {

		private boolean alreadyNotified;

		private String entryName;

		private int kindOfChange;

		/**
		 * Constructor
		 *
		 * @param file
		 *        the file to create the Outputstream to
		 * @param aEntryName
		 *        the External Name of the entry
		 * @param aKind
		 *        the Kind of Event that will be fired on close.
		 */
		public ChangeEventOutputStream(File file, String aEntryName, int aKind)
				throws IOException {
			this(file, aEntryName, aKind, false);
		}

		/**
		 * Constructor
		 *
		 * @param file
		 *        the file to create the Outputstream to
		 * @param aEntryName
		 *        the External Name of the entry
		 * @param aKind
		 *        the Kind of Event that will be fired on close.
		 * @param isAppendMode
		 *        <b>true</b> if the underlying {@link OutputStream} should be in append mode.
		 */
		public ChangeEventOutputStream(File file, String aEntryName, int aKind, boolean isAppendMode)
				throws IOException {
			super(file, isAppendMode);

			this.entryName = aEntryName;
			this.kindOfChange = DataChangeEvent.ENTRY_CREATED;
			this.kindOfChange = aKind;
		}

		@Override
		public void close() throws IOException {
			super.close();

			if (!this.alreadyNotified) {
				String theSource = getSourceName(this.entryName);

				try {
					this.alreadyNotified = true;

					fireDataChanged(new DataChangeEvent(this, kindOfChange, theSource));
				} catch (Exception ex) {
					// ususally happens in case finalizer tries to close us ...
					Logger.error("close(), Cannot fire event for " +
						theSource, ex, this);
				}
			}
		}

		public void setKindOfDataChange(int flag) {
			this.kindOfChange = flag;
		}
	}

}
