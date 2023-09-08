/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.storage.azure.blob;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlob;
import com.microsoft.azure.storage.blob.CloudBlobDirectory;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import com.microsoft.azure.storage.blob.ListBlobItem;
import com.microsoft.azure.storage.core.PathUtility;

import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.tools.NameBuilder;
import com.top_logic.dsa.repos.ContainerObject;
import com.top_logic.dsa.repos.LeafObject;
import com.top_logic.dsa.repos.RepositoryException;

/**
 * Cloud based container. 
 * 
 * @author    <a href="mailto:Michael Gänsler@top-logic.com">Michael Gänsler</a>
 */
public class CloudContainerObject implements ContainerObject {

	private static final String FOLDER_FILE = "__folder.txt";

	private final CloudBlobDirectory _repository;

	private final CloudBlobDirectory _dir;

	private final String _name;

	private final String _path;

	private CloudBlockBlob _file;

	/**
	 * Creates a {@link CloudContainerObject}.
	 * 
	 * @param aParent
	 *        Parent for the new container.
	 * @param aName
	 *        Name of the new container.
	 * @throws URISyntaxException
	 *         If the resource URI is invalid.
	 * @throws StorageException
	 *         If a storage service error occurred.
	 * @throws URISyntaxException
	 *         If the resource URI is invalid.
	 */
	public CloudContainerObject(CloudContainerObject aParent, String aName) throws URISyntaxException, StorageException {
		this(aParent.getDirectory(), aName);
	}

	/**
	 * Creates a {@link CloudContainerObject}.
	 * 
	 * @param aParent
	 *        Parent for the new container.
	 * @param aName
	 *        Name of the new container.
	 * @throws StorageException
	 *         If a storage service error occurred.
	 * @throws URISyntaxException
	 *         If the resource URI is invalid.
	 */
	public CloudContainerObject(CloudBlobDirectory aParent, String aName) throws URISyntaxException, StorageException {
		this(aParent, aName, ((aParent == null) || aName.isEmpty()) ? aParent : aParent.getDirectoryReference(aName));
	}

	/**
	 * Creates a {@link CloudContainerObject}.
	 * 
	 * @param aDir
	 *        Cloud blob directory to be represented.
	 * @throws StorageException
	 *         If a storage service error occurred.
	 * @throws URISyntaxException
	 *         If the resource URI is invalid.
	 */
	public CloudContainerObject(CloudBlobDirectory aRepository, CloudBlobDirectory aDir) throws URISyntaxException, StorageException {
		this(aRepository, CloudContainerObject.getName(aDir), aDir);
	}

	/**
	 * Creates a {@link CloudContainerObject}.
	 * 
	 * @param aName
	 *        Name of the container object.
	 * @throws StorageException
	 *         If a storage service error occurred.
	 * @throws URISyntaxException
	 *         If the resource URI is invalid.
	 */
	public CloudContainerObject(String aName) throws URISyntaxException, StorageException {
		this(null, aName, null);
	}

	private CloudContainerObject(CloudBlobDirectory aRepository, String aName, CloudBlobDirectory aFile) throws URISyntaxException, StorageException {
		_repository = aRepository;

		if (aName.endsWith("/")) {
			aName = aName.substring(0, aName.length() - 1);
		}

		int thePos = aName.lastIndexOf('/');

		_name = (thePos < 0) ? aName : aName.substring(thePos + 1);
		_dir  = aFile;

		if (aFile != null) {
			_path = CloudContainerObject.getPath(aRepository, aFile).replace("%20", " ");
			_file = this.initFolderFile(aFile);
		}
		else {
			_path = aName;
			_file = null;
		}
	}

	@Override
	public String toString() {
		return new NameBuilder(this)
			.add("name", _name)
			.add("path", _path)
			.add("dir", (_dir != null) ? _dir.getUri().toString() : null)
			.build();
	}

	@Override
	public String getName() {
		return _name;
	}

	@Override
	public String getPath() {
		return _path;
	}

	@Override
	public ContainerObject getParentContainer() throws RepositoryException {
		try {
			String theParent = this.getParent();

			return (theParent != null) ? new CloudContainerObject(_repository, theParent) : new CloudContainerObject(_repository, "");
		}
		catch (URISyntaxException | StorageException ex) {
			throw new RepositoryException(ex);
		}
	}

	@Override
	public long lastModified() throws RepositoryException {
		try {
			return _file.exists() ? _file.getProperties().getLastModified().getTime() : 0l;
		}
		catch (StorageException ex) {
			throw new RepositoryException(ex);
		}
	}

	@Override
	public String getParent() {
		String thePath = this.getPath();
		int    thePos  = thePath.lastIndexOf('/');

		return (thePos > -1) ? thePath.substring(0, thePos) : null;
	}

	@Override
	public String getAbsolutePath() {
		CloudBlobDirectory theDir = this.getDirectory();

		return (theDir != null) ? this.getDirectory().getUri().toString() : null;
	}

	@Override
	public ContainerObject createTempFile(String aPrefix, String aSuffix) throws IOException {
		throw new UnsupportedOperationException("Creating a temporary directory for '" + aPrefix + "', '" + aSuffix + "' is not supported!");
	}

	@Override
	public LeafObject createLeafObject(String aName) throws RepositoryException {
		try {
			return new CloudLeafObject(this.getDirectory(), aName);
		} 
		catch (URISyntaxException | StorageException ex) {
			throw new RepositoryException(ex);
		}
	}

	@Override
	public List<LeafObject> listChildren() throws RepositoryException {
		List<LeafObject>   theChildren = new ArrayList<>();
		CloudBlobDirectory theDir      = this.getDirectory();

		if (theDir != null) {
			try {
				for (ListBlobItem theChild : theDir.listBlobs()) {
					if (theChild instanceof CloudBlob) {
						CloudBlob theBlob = (CloudBlob) theChild;
						String    theName = theBlob.getName();

						if (!theName.endsWith(FOLDER_FILE)) {
							theChildren.add(new CloudLeafObject(theBlob));
						}
					}
				}
			} 
			catch (URISyntaxException | StorageException ex) {
				throw new RepositoryException(ex);
			}
		}

		return theChildren;
	}

	@Override
	public List<String> list() throws RepositoryException {
		List<String>       theChildren = new ArrayList<>();
		CloudBlobDirectory theDir      = this.getDirectory();

		if (theDir != null) {
			try {
				for (ListBlobItem theChild : theDir.listBlobs()) {
					if (theChild instanceof CloudBlob) {
						String theName = ((CloudBlob) theChild).getName();

						if (!theName.endsWith(FOLDER_FILE)) {
							theChildren.add(StringServices.replace(theName, "%20", " "));
						}
					}
					else if (theChild instanceof CloudBlobDirectory) {
						theChildren.add(StringServices.replace(CloudContainerObject.getName((CloudBlobDirectory) theChild), "%20", " "));
					}
				}
			} 
			catch (URISyntaxException | StorageException ex) {
				throw new RepositoryException(ex);
			}
		}

		return theChildren;
	}

	@Override
	public boolean delete() throws RepositoryException {
		try {
			if (this._file.exists() && this.list().isEmpty()) {
				this._file.delete();
				return true;
			}
			else {
				return false;
			}
		} 
		catch (StorageException ex) {
			throw new RepositoryException(ex);
		}
	}

	@Override
	public boolean mkdir() throws RepositoryException {
		try {
			if (!_file.exists()) {
				_file.uploadText("FOLDER: " + this.getName());
				return true;
			}
			else {
				return false;
			}
		}
		catch (StorageException | IOException ex) {
			throw new RepositoryException(ex);
		}
	}

	@Override
	public boolean mkdirs() throws RepositoryException {
		return this.mkdir();
	}

	@Override
	public boolean exists() throws RepositoryException {
		try {
			return _file.exists();
		} 
		catch (StorageException ex) {
			throw new RepositoryException(ex);
		}
	}

	@Override
	public boolean isDirectory() {
		return true;
	}

	@Override
	public boolean renameTo(ContainerObject aNew) {
		try {
			for (LeafObject theChild : this.listChildren()) {
				LeafObject theNewLeaf = aNew.createLeafObject(theChild.getName());

				theChild.copy(theNewLeaf);
				theChild.delete();
			}

			return this.delete();
		} 
		catch (IOException | RepositoryException ex) {
			Logger.error("Failed to rename " + this, ex, CloudContainerObject.class);

			return false;
		}
	}

	@Override
	public ContainerObject createContainer(String aName) throws RepositoryException {
		try {
			return new CloudContainerObject(this, aName);
		}
		catch (URISyntaxException | StorageException ex) {
			throw new RepositoryException(ex);
		}
	}

	private CloudBlockBlob initFolderFile(CloudBlobDirectory aFile) throws URISyntaxException, StorageException {
		return aFile.getBlockBlobReference(FOLDER_FILE);
	}

	private CloudBlobDirectory getDirectory() {
		return _dir;
	}

	private static String getPath(CloudBlobDirectory aRepository, CloudBlobDirectory aDir) {
		String thePath = aDir.getUri().toString();
		String theRep  = aRepository.getUri().toString();
		int    theRLen = theRep.length();

		if (thePath.equals(theRep)) {
			return "";
		}
		else { 
			return thePath.endsWith("/") ? thePath.substring(theRLen, thePath.length() - 1) : thePath.substring(theRLen);
		}
	}

	private static String getName(CloudBlobDirectory aDir) {
		return PathUtility.getDirectoryNameFromURI(aDir.getUri(), false);
	}
}

