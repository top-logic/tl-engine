/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.storage.azure.blob;

import java.net.URISyntaxException;
import java.security.InvalidKeyException;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlobDirectory;

import com.top_logic.basic.Logger;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.dsa.repos.AbstractRepository;
import com.top_logic.dsa.repos.BaseEntryInfo;
import com.top_logic.dsa.repos.RepositoryException;
import com.top_logic.dsa.repos.RepositoryInfo;

/**
 * Cloud based implementation of a top-logic repository.
 * 
 * @author    <a href="mailto:Michael Gänsler@top-logic.com">Michael Gänsler</a>
 */
public class CloudBasedRepository<C extends CloudContainerObject, L extends CloudLeafObject> extends AbstractRepository<C, L> {

	/**
	 * Configuration of {@link CloudBasedRepository}.
	 * 
	 * @author <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
	 */
	public interface Config<I extends CloudBasedRepository<?, ?>> extends AbstractRepository.Config<I> {

		/** @see #getConnectString() */
		String CONNECT_STRING = "connect-string";

		/** @see #getContainerName() */
		String CONTAINER_NAME = "container-name";

		/** @see #getBaseDirectoryName() */
		String DIRECTORY_NAME = "directory-name";

		/** Name of the container to be opened. */
		@Mandatory
		@Name(CONTAINER_NAME)
		String getContainerName();

		/** @see #getContainerName() */
		void setContainerName(String aName);

		/** String to be used for connecting to the blob storage. */
		@Mandatory
		@Name(CONNECT_STRING)
		String getConnectString();

		/** @see #getConnectString() */
		void setConnectString(String aName);

		/** Name of the base directory this repository is located in. */
		@Mandatory
		@Name(DIRECTORY_NAME)
		String getBaseDirectoryName();

		/** @see #getBaseDirectoryName() */
		void setBaseDirectoryName(String aName);
	}

	private final CloudBlobContainer _container;

	private final CloudBlobDirectory _baseDir;

	/**
	 * Creates a new {@link CloudBasedRepository} from the given configuration.
	 * 
	 * @param aContext
	 *        {@link InstantiationContext} to instantiate sub configurations.
	 * @param aConfig
	 *        Configuration for this {@link CloudBasedRepository}.
	 * @throws ConfigurationException
	 *         When configuration is invalid.
	 */
	public CloudBasedRepository(InstantiationContext aContext, Config<?> aConfig) throws ConfigurationException {
		String theName = aConfig.getPath();

		_container = this.connect(aConfig);

		try {
			_baseDir = _container.getDirectoryReference(aConfig.getBaseDirectoryName());

			this.init(this.getRepositoryContainer(aConfig),
					  this.getWorkareaContainer(aConfig),
					  this.getAtticContainer(aConfig));
		} 
		catch (RepositoryException | URISyntaxException ex) {
			throw new ConfigurationException(I18NConstants.ERROR_INIT_BASE_DIRECTORY, Config.PATH, theName, ex);
		}
	}

	@Override
	protected C createContainerReference(C aParent, String aName) throws RepositoryException {
		try {
			return this.toC(new CloudContainerObject(aParent, aName));
		} 
		catch (URISyntaxException | StorageException ex) {
			throw new RepositoryException(ex);
		}
	}

	@Override
	protected C createContainerReference(String aName) throws RepositoryException {
		try {
			return this.toC(new CloudContainerObject(aName));
		} 
		catch (URISyntaxException | StorageException ex) {
			throw new RepositoryException(ex);
		}
	}

	@Override
	protected RepositoryInfo createRepositoryInfo(String aPath, C aContainer) throws RepositoryException {
		throw new UnsupportedOperationException("Cannot create entry info for '" + aPath + "' and '" + aContainer + "'!");
	}

	@Override
	protected String normalizePath(String aPath, String aFileName) {
		return aPath + '/' + aFileName;
	}

	@Override
	protected RepositoryInfo createContainerInfo(C relPath, C absPath, String aFile) throws RepositoryException {
		return new CloudBlobRepositoryInfo(relPath, absPath, aFile);
	}

	@Override
	protected RepositoryInfo createContainerInfo(C relPath, C absPath) throws RepositoryException {
		return new CloudBlobRepositoryInfo(relPath, absPath);
	}

	@Override
	protected BaseEntryInfo createEntryInfo(C relPath, C absPath) throws RepositoryException {
		return new CloudBlobEntryInfo(relPath, absPath);
	}

	@Override
	protected BaseEntryInfo createEntryInfo(C relPath, C absPath, String aFile) throws RepositoryException {
		return new CloudBlobEntryInfo(relPath, absPath, aFile);
	}

	@Override
	protected C escapeDirNamesRecursivly(C anObject) throws RepositoryException {
		return ((anObject != null) && (anObject.getParent() != null)) ? super.escapeDirNamesRecursivly(anObject) : anObject;
	}

	private CloudBlobContainer connect(Config<?> aConfig) throws ConfigurationException {
		String theContainer = aConfig.getContainerName();
		String theConnect = aConfig.getConnectString();

		try {
			// Retrieve storage account from connection-string.
			CloudStorageAccount storageAccount = CloudStorageAccount.parse(theConnect);

			Logger.info("Connect to Azure Blob Storage Container '" + theContainer + "' (URI: '" + storageAccount.getBlobStorageUri() + "')!", CloudBasedRepository.class);

			// Create the blob client.
			CloudBlobClient blobClient = storageAccount.createCloudBlobClient();

			// Get a reference to a container.
			// The container name must be lower case
			CloudBlobContainer container = blobClient.getContainerReference(theContainer);

			// Create the container if it does not exist.
			if (container.createIfNotExists()) {
				Logger.info("Created Blob Storage Container '" + theContainer + "'!", CloudBasedRepository.class);
			}

			return container;
		} 
		catch (URISyntaxException | InvalidKeyException ex) {
			throw new ConfigurationException(I18NConstants.ERROR_CONNECT_STRING, Config.CONNECT_STRING, theConnect, ex);
		} 
		catch (StorageException ex) {
			throw new ConfigurationException(I18NConstants.ERROR_CONNECT_CONTAINER, Config.CONTAINER_NAME, theContainer, ex);
		}
	}

	private C getRepositoryContainer(Config<?> aConfig) throws RepositoryException {
		C theFolder = this.getBaseDirectory(aConfig.getPath());

		if (theFolder == null) {
			throw new RepositoryException("Repository folder '" + aConfig + "' not found!");
		}
		else if (!theFolder.exists()) {
			theFolder.mkdirs();
		}
		else if (!theFolder.isDirectory()) {
			throw new RepositoryException("Repository root ('" + theFolder + "') must be a directory");
		}

		Logger.info("<init> Repository = " + theFolder.getAbsolutePath(), CloudBasedRepository.class);

		return theFolder;
	}

	private C getAtticContainer(Config<?> aConfig) {
		C theFolder = null;
		String theFolderName = aConfig.getAttic();

		if (!theFolderName.isEmpty()) {
			theFolder = this.getBaseDirectory(theFolderName);

			Logger.info("<init> Attic root = " + (theFolder == null ? "null" : theFolder.getAbsolutePath()), CloudBasedRepository.class);
		}

		return theFolder;
	}

	private C getWorkareaContainer(Config<?> aConfig) throws RepositoryException {
		C theFolder = null;
		String theFolderName = aConfig.getWorkarea();

		if (!theFolderName.isEmpty()) {
			theFolder = this.getBaseDirectory(theFolderName);

			if (theFolder != null) {
				String thePath = theFolder.getAbsolutePath();

				if (!theFolder.exists()) {
					theFolder.mkdirs();
				} 
				else if (!theFolder.isDirectory()) {
					throw new RepositoryException("Work area root ('" + thePath + "') must be a Directory");
				}

				Logger.info("<init> work area path = " + thePath, CloudBasedRepository.class);
			}
			else {
				Logger.warn("<init> Failed to initialize work area root ('" + theFolderName + "') no matching folder found", CloudBasedRepository.class);
			}
		}

		return theFolder;
	}

	@SuppressWarnings("unchecked")
	private C getBaseDirectory(String aFileName) {
        try {
			CloudBlobDirectory theDir = _baseDir.getDirectoryReference(aFileName);

			return (C) new CloudContainerObject(theDir, theDir);
		}
		catch (Exception ex) {	
			Logger.error("Failed to getBaseDirectory('" + aFileName + "')!", ex, CloudBasedRepository.class);

			return null;
		}
	}

	@Override
	public String toString() {
		// Note: This value is printed in the application monitor to identify the repository.
		return "azure-blobstore://" + getRoot().getAbsolutePath();
	}
}

