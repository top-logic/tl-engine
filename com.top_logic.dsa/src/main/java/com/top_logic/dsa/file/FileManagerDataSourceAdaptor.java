/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dsa.file;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.top_logic.basic.FileManager;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.basic.mime.MimeTypesModule;
import com.top_logic.dob.DataObject;
import com.top_logic.dsa.DataSourceAdaptor;
import com.top_logic.dsa.DatabaseAccessException;
import com.top_logic.dsa.evt.DataChangeEvent;
import com.top_logic.dsa.evt.DataChangeListener;
import com.top_logic.dsa.ex.DataChangeException;
import com.top_logic.dsa.ex.NotSupportedException;

/**
 * {@link AbstractFilesystemDataSourceAdaptor} that uses path resolution of {@link FileManager}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class FileManagerDataSourceAdaptor implements DataSourceAdaptor {

	/**
	 * Configuration options for {@link FileManagerDataSourceAdaptor}.
	 */
	public interface Config<I extends FileManagerDataSourceAdaptor> extends PolymorphicConfiguration<I> {

		/**
		 * The base path to prepend to all resolved paths.
		 */
		@Nullable
		String getBase();

	}

	private String _base;

	private FileManager _fm;

	private String _protocol;

	/**
	 * Creates a {@link FileManagerDataSourceAdaptor}.
	 */
	public FileManagerDataSourceAdaptor(InstantiationContext context, Config<?> config) {
		_base = config.getBase();
		_fm = FileManager.getInstance();
	}

	@Override
	public void close() throws DatabaseAccessException {
		// Ignore.
	}

	@Override
	public String getMimeType(String path) throws DatabaseAccessException {
		return MimeTypesModule.getInstance().getMimeType(path);
	}

	@Override
	public boolean isStructured() {
		return false;
	}

	@Override
	public boolean isRepository() {
		return false;
	}

	@Override
	public boolean isPrivate() {
		return false;
	}

	@Override
	public boolean exists(String path) throws DatabaseAccessException {
		return _fm.exists(resource(path));
	}

	@Override
	public boolean isContainer(String path) throws DatabaseAccessException {
		return _fm.isDirectory(resource(path));
	}

	@Override
	public boolean isEntry(String path) throws DatabaseAccessException {
		return !_fm.isDirectory(resource(path));
	}

	@Override
	public String getDisplayName(String path) throws DatabaseAccessException {
		return getName(path);
	}

	@Override
	public String getName(String path) throws DatabaseAccessException {
		int endIndex;
		int length = path.length();
		if (!path.isEmpty() && path.charAt(length - 1) == '/') {
			endIndex = length - 1;
		} else {
			endIndex = length;
		}

		int sepIndex = path.lastIndexOf('/', endIndex - 1);

		int startIndex;
		if (sepIndex < 0) {
			startIndex = 0;
		} else {
			startIndex = sepIndex + 1;
		}

		return path.substring(startIndex, endIndex);
	}

	@Override
	public String rename(String oldPath, String newName) throws DatabaseAccessException {
		throw new UnsupportedOperationException();
	}

	@Override
	public String move(String oldPath, String newPath) throws DatabaseAccessException {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getParent(String path) throws DatabaseAccessException {
		int stopIndex;
		int length = path.length();
		if (!path.isEmpty() && path.charAt(length - 1) == '/') {
			stopIndex = length - 1;
		} else {
			stopIndex = length;
		}

		int startIndex = path.lastIndexOf('/', stopIndex);
		if (startIndex < 0) {
			return null;
		}

		return path.substring(0, startIndex);
	}

	@Override
	public String getChild(String containerPath, String elementName) throws DatabaseAccessException {
		if (containerPath.isEmpty() || containerPath.charAt(containerPath.length() - 1) != '/') {
			containerPath += '/';
		}
		return containerPath + elementName;
	}

	@Override
	public InputStream getEntry(String path) throws DatabaseAccessException {
		try {
			return _fm.getStream(resource(path));
		} catch (IOException ex) {
			throw new DatabaseAccessException(ex);
		}
	}

	@Override
	public DataObject getObjectEntry(String path) throws DatabaseAccessException {
		throw new UnsupportedOperationException();
	}

	@Override
	public InputStream getEntry(String path, String version) throws DatabaseAccessException {
		throw new UnsupportedOperationException();
	}

	@Override
	public DataObject getObjectEntry(String path, String version) throws DatabaseAccessException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void putEntry(String path, InputStream data) throws DatabaseAccessException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void putEntry(String path, DataObject data) throws DatabaseAccessException {
		throw new UnsupportedOperationException();
	}

	@Override
	public OutputStream putEntry(String containerPath, String elementName) throws DatabaseAccessException {
		throw new UnsupportedOperationException();
	}

	@Override
	public OutputStream getEntryOutputStream(String path) throws DatabaseAccessException {
		try {
			String resource = resource(path);
			_fm.createData(resource, BinaryDataFactory.createBinaryData(new byte[0]));
			File file = _fm.getIDEFile(resource);
			return new FileOutputStream(file);
		} catch (IOException ex) {
			throw new DatabaseAccessException(ex);
		}
	}

	@Override
	public OutputStream getEntryAppendStream(String path) throws DatabaseAccessException {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getURL(String path) throws NotSupportedException {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getForwardURL(String path) throws NotSupportedException {
		throw new UnsupportedOperationException();
	}

	@Override
	public String createEntry(String containerPath, String elementName, InputStream data)
			throws DatabaseAccessException {
		throw new UnsupportedOperationException();
	}

	@Override
	public String createNewEntryName(String containerPath, String prefix, String suffix)
			throws DatabaseAccessException {
		throw new UnsupportedOperationException();
	}

	@Override
	public DataObject createObjectEntry(String containerPath, String elementName) throws DatabaseAccessException {
		throw new UnsupportedOperationException();
	}

	@Override
	public OutputStream createEntry(String containerPath, String elementName) throws DatabaseAccessException {
		throw new UnsupportedOperationException();
	}

	@Override
	public String createContainer(String containerPath, String elementName) throws DatabaseAccessException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void delete(String path, boolean force) throws DatabaseAccessException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void deleteRecursively(String path) throws DatabaseAccessException {
		throw new UnsupportedOperationException();
	}

	@Override
	public String[] getEntryNames(String path) throws DatabaseAccessException {
		return _fm.getResourcePaths(resource(path) + "/").toArray(new String[0]);
	}

	@Override
	public String[] getVersions(String path) throws DatabaseAccessException {
		throw new UnsupportedOperationException();
	}

	@Override
	public DataObject getProperties(String path) throws DatabaseAccessException {
		throw new UnsupportedOperationException();
	}

	@Override
	public DataObject getProperties(String path, String version) throws DatabaseAccessException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setProperties(String path, DataObject props) throws DatabaseAccessException {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean lock(String path) throws DatabaseAccessException {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean unlock(String path) throws DatabaseAccessException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setProtocol(String protocol) {
		_protocol = protocol;
	}

	@Override
	public String getProtocol() {
		return _protocol;
	}

	@Override
	public boolean addDataChangeListener(DataChangeListener aListener) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean removeDataChangeListener(DataChangeListener aListener) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void fireDataChanged(DataChangeEvent anEvent) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean fireCheckAllow(DataChangeEvent anEvent) throws DataChangeException {
		throw new UnsupportedOperationException();
	}

	private String resource(String path) {
		if (_base == null) {
			return path;
		}
		return _base + '/' + path;
	}

}
