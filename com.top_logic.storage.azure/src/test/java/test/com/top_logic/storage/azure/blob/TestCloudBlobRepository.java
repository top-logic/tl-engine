/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.storage.azure.blob;

import java.net.URISyntaxException;
import java.security.InvalidKeyException;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.DeactivatedTest;
import test.com.top_logic.dsa.DSATestSetup;
import test.com.top_logic.dsa.repos.AbstractTestRepositoryDataSourceAdaptor;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlob;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlobDirectory;
import com.microsoft.azure.storage.blob.ListBlobItem;

import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.storage.azure.blob.CloudBasedRepository;
import com.top_logic.storage.azure.blob.CloudBasedRepository.Config;
import com.top_logic.storage.azure.blob.I18NConstants;

/**
 * Tests for the working of the cloud based repository system.
 *
 */
@DeactivatedTest("Azure cloud account no longer available.")
public class TestCloudBlobRepository extends AbstractTestRepositoryDataSourceAdaptor {

	/**
	 * Creates a {@link TestCloudBlobRepository}.
	 */
	public TestCloudBlobRepository(String name) {
		super(name);
	}

	@Override
	public void doCleanup() {
		try {
			CloudBasedRepository.Config<?> theConfig = (Config<?>) this.getImplementationConfiguration();
			CloudBlobContainer theContainer = this.connect(theConfig);

			for (ListBlobItem theItem : theContainer.listBlobs()) {
				if (theItem instanceof CloudBlob) {
					Logger.info("Deleting blob '" + theItem.getStorageUri().getPrimaryUri().toString() + "'...", TestCloudBlobRepository.class);

					((CloudBlob) theItem).deleteIfExists();
				}
				else if (theItem instanceof CloudBlobDirectory) {
					if (this.getName(theItem).startsWith("test-")) {
						this.deleteDirectory((CloudBlobDirectory) theItem);
					}
				}
			}
		}
		catch (ConfigurationException | StorageException | URISyntaxException ex) {
			Logger.error("Failed to cleanup cloud space", ex, TestCloudBlobRepository.class);
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	protected PolymorphicConfiguration<?> getImplementationConfiguration() {
		CloudBasedRepository.Config<?> config = TypedConfiguration.newConfigItem(CloudBasedRepository.Config.class);

		config.setConnectString(System.getProperty("test.azure.connection"));
		config.setImplementationClass((Class) CloudBasedRepository.class);
		config.setContainerName("testcases");
		config.setBaseDirectoryName("test-" + StringServices.randomUUID().replace("-", ""));
		config.setPath("repository");
		config.setWorkarea("workarea");
		config.setAttic("attic");

		return config;
	}

	private String getName(ListBlobItem anItem) {
		String theURI = anItem.getStorageUri().getPrimaryUri().toString();
		if (theURI.endsWith("/")) {
			theURI = theURI.substring(0, theURI.length() - 1);
		}

		int thePos = theURI.lastIndexOf('/');

		return (thePos > 0) ? theURI.substring(thePos + 1) : theURI;
	}

	private void deleteDirectory(CloudBlobDirectory aDir) throws StorageException, URISyntaxException {
		for (ListBlobItem theItem : aDir.listBlobs()) {
			if (theItem instanceof CloudBlob) {
				Logger.info("Deleting blob '" + theItem.getStorageUri().getPrimaryUri().toString() + "'...", TestCloudBlobRepository.class);

				((CloudBlob) theItem).deleteIfExists();
			} 
			else if (theItem instanceof CloudBlobDirectory) {
				this.deleteDirectory((CloudBlobDirectory) theItem);
			}
		}
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

	/**
	 * the suite of tests to execute.
	 */
	public static Test suite() {
		TestSuite suite = new TestSuite();

		suite.addTest(new TestSuite(TestCloudBlobRepository.class));
		suite.addTest(new TestCloudBlobRepository("doCleanup"));

		return DSATestSetup.createDSATestSetup(suite);
	}

	/**
	 * Main function for direct testing.
	 */
	public static void main(String[] args) {
		Logger.configureStdout("ERROR");
		junit.textui.TestRunner.run(suite());
	}
}
