/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.storage.azure;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Iterator;

import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.models.BlobItem;
import com.azure.storage.blob.models.ListBlobsOptions;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.io.binary.AbstractBinaryData;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.knowledge.service.migration.processors.DocumentRepositoryConfig;
import com.top_logic.knowledge.service.migration.processors.FileRepositoryContentReader;
import com.top_logic.knowledge.service.migration.processors.MigrateDocumentContentProcessor;
import com.top_logic.knowledge.service.migration.processors.RepositoryContentReader;

/**
 * {@link RepositoryContentReader} for a document repository of former versions that was stored in
 * a container of Azure Blob Storage.
 *
 * <p>
 * The repository is located in the configured container below the directory
 * <code>&lt;directory-name&gt;/&lt;path&gt;/</code>, deleted documents below
 * <code>&lt;directory-name&gt;/&lt;attic&gt;/</code>. Below these directories, the blobs follow
 * the same naming as the files of a repository in the file system (see
 * {@link FileRepositoryContentReader}): the versions of a document are blobs named
 * <code>_&lt;version&gt;_n_&lt;author&gt;</code> in the directory <code>_f&lt;name&gt;</code>,
 * located in the (escaped) directories of the document path.
 * </p>
 *
 * <p>
 * To migrate the documents of such a repository, the application configures the reader as input
 * of the {@link MigrateDocumentContentProcessor} in its configuration, with the settings of the
 * former repository:
 * </p>
 *
 * <pre>
 * &lt;config config:interface="com.top_logic.knowledge.service.migration.processors.DocumentRepositoryConfig"&gt;
 *    &lt;reader class="com.top_logic.storage.azure.AzureRepositoryContentReader"
 *       connection-string="%AZURE_BLOB_CONNECTION_SECRET%"
 *       container-name="..."
 *       directory-name="..."
 *       path="repository"
 *       attic="attic"
 *    /&gt;
 * &lt;/config&gt;
 * </pre>
 *
 * @see DocumentRepositoryConfig
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class AzureRepositoryContentReader extends AbstractConfiguredInstance<AzureRepositoryContentReader.Config<?>>
		implements RepositoryContentReader {

	/** Separator of path elements and directories. */
	public static final String PATH_SEPARATOR = FileRepositoryContentReader.PATH_SEPARATOR;

	/** Content type of the content read from the repository. */
	private static final String CONTENT_TYPE = "application/octet-stream";

	/**
	 * Configuration options of {@link AzureRepositoryContentReader}.
	 */
	public interface Config<I extends AzureRepositoryContentReader>
			extends PolymorphicConfiguration<I>, AzureStorageAccountConfig {

		/** Configuration name of {@link #getContainerName()}. */
		String CONTAINER_NAME = "container-name";

		/** Configuration name of {@link #getDirectoryName()}. */
		String DIRECTORY_NAME = "directory-name";

		/** Configuration name of {@link #getPath()}. */
		String PATH = "path";

		/** Configuration name of {@link #getAttic()}. */
		String ATTIC = "attic";

		/** Default value of {@link #getPath()}. */
		String DEFAULT_PATH = "repository";

		/**
		 * The name of the container holding the repository.
		 */
		@Name(CONTAINER_NAME)
		@Mandatory
		String getContainerName();

		/**
		 * @see #getContainerName()
		 */
		void setContainerName(String value);

		/**
		 * The directory in the container holding the repository and the attic.
		 */
		@Name(DIRECTORY_NAME)
		@Mandatory
		String getDirectoryName();

		/**
		 * @see #getDirectoryName()
		 */
		void setDirectoryName(String value);

		/**
		 * The directory of the repository, relative to the directory given by
		 * {@link #getDirectoryName()}.
		 */
		@Name(PATH)
		@StringDefault(DEFAULT_PATH)
		String getPath();

		/**
		 * @see #getPath()
		 */
		void setPath(String value);

		/**
		 * The directory holding documents deleted from the repository, relative to the directory
		 * given by {@link #getDirectoryName()}.
		 *
		 * <p>
		 * If not given, deleted documents are not looked up.
		 * </p>
		 */
		@Name(ATTIC)
		@Nullable
		String getAttic();

		/**
		 * @see #getAttic()
		 */
		void setAttic(String value);

	}

	private final BlobContainerClient _container;

	private final String _root;

	private final String _attic;

	/**
	 * Creates a {@link AzureRepositoryContentReader} from configuration.
	 *
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public AzureRepositoryContentReader(InstantiationContext context, Config<?> config) {
		super(context, config);
		AzureStorageAccount account =
			AzureStorageAccount.create(context, config, "Azure document repository reader");
		_container = account == null ? null : account.getClient().getBlobContainerClient(config.getContainerName());
		String base = directory(config.getDirectoryName());
		_root = base + directory(config.getPath());
		String attic = config.getAttic();
		_attic = StringServices.isEmpty(attic) ? null : base + directory(attic);
	}

	/**
	 * The name prefix of the blobs in the given directory: the directory name without leading and
	 * with a trailing separator, empty for an empty directory name.
	 */
	private static String directory(String name) {
		String result = StringServices.nonNull(name);
		while (result.startsWith(PATH_SEPARATOR)) {
			result = result.substring(1);
		}
		if (result.isEmpty() || result.endsWith(PATH_SEPARATOR)) {
			return result;
		}
		return result + PATH_SEPARATOR;
	}

	/**
	 * The name prefix of the blobs in the repository directory.
	 */
	public String getRoot() {
		return _root;
	}

	/**
	 * The name prefix of the blobs in the attic directory, <code>null</code> if no attic is
	 * configured.
	 */
	public String getAttic() {
		return _attic;
	}

	@Override
	public BinaryData read(String path, int version) throws IOException {
		if (version <= 0) {
			return null;
		}
		String[] elements = elements(path);
		if (elements.length == 0) {
			return null;
		}
		String versionName = FileRepositoryContentReader.ESCAPE + version
			+ FileRepositoryContentReader.NORMAL_VERSION_INFIX;

		BlobItem item = find(entryDirectory(elements) + versionName);
		if (item == null && _attic != null) {
			item = find(_attic + String.join(PATH_SEPARATOR, elements) + PATH_SEPARATOR + versionName);
		}
		if (item == null) {
			return null;
		}
		Long size = item.getProperties().getContentLength();
		return new BlobContent(_container, item.getName(), elements[elements.length - 1],
			size == null ? 0 : size.longValue());
	}

	/**
	 * The name prefix of the blobs holding the versions of the document with the given path
	 * elements in the repository.
	 */
	public String entryDirectory(String[] elements) {
		StringBuilder result = new StringBuilder(_root);
		for (int n = 0; n < elements.length - 1; n++) {
			result.append(escape(elements[n]));
			result.append(PATH_SEPARATOR);
		}
		result.append(FileRepositoryContentReader.ENTRY_PREFIX);
		result.append(elements[elements.length - 1]);
		result.append(PATH_SEPARATOR);
		return result.toString();
	}

	/**
	 * The first blob whose name starts with the given prefix and has no further directory, or
	 * <code>null</code> if there is none.
	 */
	private BlobItem find(String prefix) throws IOException {
		try {
			Iterator<BlobItem> items = _container
				.listBlobsByHierarchy(PATH_SEPARATOR, new ListBlobsOptions().setPrefix(prefix), null).iterator();
			while (items.hasNext()) {
				BlobItem item = items.next();
				if (!Boolean.TRUE.equals(item.isPrefix())) {
					return item;
				}
			}
			return null;
		} catch (RuntimeException ex) {
			throw new IOException("Listing blobs with prefix '" + prefix + "' failed: " + ex.getMessage(), ex);
		}
	}

	private static String[] elements(String path) {
		return Arrays.stream(path.split(PATH_SEPARATOR))
			.filter(element -> !element.isEmpty())
			.toArray(String[]::new);
	}

	private static String escape(String element) {
		String escape = FileRepositoryContentReader.ESCAPE;
		return element.startsWith(escape) ? escape + element : element;
	}

	/**
	 * Content of a blob, read on demand.
	 */
	private static final class BlobContent extends AbstractBinaryData {

		private final BlobContainerClient _container;

		private final String _blobName;

		private final String _name;

		private final long _size;

		BlobContent(BlobContainerClient container, String blobName, String name, long size) {
			_container = container;
			_blobName = blobName;
			_name = name;
			_size = size;
		}

		@Override
		public String getName() {
			return _name;
		}

		@Override
		public long getSize() {
			return _size;
		}

		@Override
		public String getContentType() {
			return CONTENT_TYPE;
		}

		@Override
		public InputStream getStream() throws IOException {
			try {
				return _container.getBlobClient(_blobName).openInputStream();
			} catch (RuntimeException ex) {
				throw new IOException("Reading blob '" + _blobName + "' failed: " + ex.getMessage(), ex);
			}
		}

	}

}
