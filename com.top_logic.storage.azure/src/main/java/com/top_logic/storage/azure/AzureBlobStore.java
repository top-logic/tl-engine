/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.storage.azure;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.stream.Stream;

import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.models.BlobErrorCode;
import com.azure.storage.blob.models.BlobHttpHeaders;
import com.azure.storage.blob.models.BlobItem;
import com.azure.storage.blob.models.BlobItemProperties;
import com.azure.storage.blob.models.BlobRange;
import com.azure.storage.blob.models.BlobRequestConditions;
import com.azure.storage.blob.models.BlobStorageException;
import com.azure.storage.blob.models.DeleteSnapshotsOptionType;
import com.azure.storage.blob.models.ListBlobsOptions;
import com.azure.storage.blob.options.BlobInputStreamOptions;
import com.azure.storage.blob.options.BlockBlobCommitBlockListOptions;
import com.azure.storage.blob.options.BlockBlobSimpleUploadOptions;
import com.azure.storage.blob.sas.BlobSasPermission;
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues;
import com.azure.storage.blob.specialized.BlockBlobClient;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.config.annotation.defaults.LongDefault;
import com.top_logic.basic.config.constraint.annotation.Bound;
import com.top_logic.basic.config.constraint.annotation.Comparision;
import com.top_logic.basic.config.constraint.annotation.ComparisonDependency;
import com.top_logic.basic.config.constraint.annotation.Constraint;
import com.top_logic.basic.config.constraint.impl.HasURLFormat;
import com.top_logic.basic.config.constraint.impl.NonNegative;
import com.top_logic.basic.config.format.MemorySizeFormat;
import com.top_logic.basic.config.format.MillisFormat;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.basic.io.LimitedInputStream;
import com.top_logic.basic.io.binary.ContentDisposition;
import com.top_logic.basic.io.blob.AbstractBlobStore;
import com.top_logic.basic.io.blob.BlobInfo;
import com.top_logic.basic.io.blob.BlobStore;
import com.top_logic.basic.io.blob.NoSuchBlobException;
import com.top_logic.layout.form.values.edit.annotation.DynamicMode;
import com.top_logic.tool.boundsec.CommandHandler.ConfirmConfig.VisibleIf;

/**
 * {@link BlobStore} keeping each blob as block blob in a container of Azure Blob Storage.
 *
 * <p>
 * The blob of a content is named by the configured prefix followed by the key of the blob. The
 * container must exist. Content up to the single upload threshold is buffered in memory and
 * uploaded in a single request; larger content and content of unknown size exceeding the threshold
 * is streamed in blocks, buffering one block at a time, and committed when it is complete. Blocks
 * of a failed upload are never committed; the storage discards them automatically after 7 days. A
 * blob is never overwritten: storing fails if a blob with the chosen name exists.
 * </p>
 *
 * <p>
 * Optionally, browsers download content directly from the storage through short-lived URLs with a
 * shared access signature instead of receiving it through the application server, see
 * {@link Config#getDirectDownload()}.
 * </p>
 *
 * @implNote The store communicates through the HTTP client of the JDK, see
 *           {@link AzureStorageAccount}. Blocks are staged and committed explicitly
 *           ({@link BlockBlobClient#stageBlock(String, BinaryData)},
 *           {@link BlockBlobClient#commitBlockListWithResponse(BlockBlobCommitBlockListOptions, Duration, Context)}),
 *           so that the size of the content is checked before the blob becomes visible. Writes
 *           are conditional on the absence of the blob (<code>If-None-Match: *</code>).
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class AzureBlobStore extends AbstractBlobStore<AzureBlobStore.Config<?>> {

	/**
	 * Minimum size of a block of an upload in blocks.
	 */
	public static final long MIN_BLOCK_SIZE = 64L * 1024;

	/**
	 * Maximum size of a single upload request: the size of a byte array that buffers the content,
	 * which is less than the limit of the storage.
	 */
	public static final long MAX_REQUEST_SIZE = Integer.MAX_VALUE - 8;

	/**
	 * Maximum number of blocks of a block blob accepted by the storage.
	 */
	public static final int MAX_BLOCKS = 50000;

	/**
	 * Default value of {@link Config#getBlockSize()}.
	 */
	public static final long DEFAULT_BLOCK_SIZE = 8L * 1024 * 1024;

	/**
	 * Default value of {@link Config#getSingleUploadThreshold()}.
	 */
	public static final long DEFAULT_SINGLE_UPLOAD_THRESHOLD = 16L * 1024 * 1024;

	/**
	 * Default value of {@link Config#getDirectDownloadMinSize()}.
	 */
	public static final long DEFAULT_DIRECT_DOWNLOAD_MIN_SIZE = 1024L * 1024;

	/**
	 * Default value of {@link Config#getDirectDownloadLifetime()} in milliseconds.
	 */
	public static final long DEFAULT_DIRECT_DOWNLOAD_LIFETIME = 60L * 1000;

	/**
	 * Maximum lifetime of a direct download URL (7 days) in milliseconds.
	 */
	public static final long MAX_DIRECT_DOWNLOAD_LIFETIME = 7L * 24 * 60 * 60 * 1000;

	/**
	 * Minimum lifetime of a direct download URL in milliseconds.
	 */
	public static final long MIN_DIRECT_DOWNLOAD_LIFETIME = 1000;

	/**
	 * Value of the <code>Cache-Control</code> header that a direct download is delivered with.
	 *
	 * <p>
	 * The content is user specific and its URL is only valid for a short time, so neither the
	 * browser nor a proxy keeps it.
	 * </p>
	 */
	public static final String DIRECT_DOWNLOAD_CACHE_CONTROL = "private, no-store";

	/**
	 * Content type sent for content without a content type.
	 */
	private static final String DEFAULT_CONTENT_TYPE = "application/octet-stream";

	/**
	 * Value of the <code>If-None-Match</code> condition that matches any existing blob.
	 */
	private static final String ANY_ETAG = "*";

	/**
	 * Separator of path segments in blob names.
	 */
	private static final String DELIMITER = "/";

	/**
	 * Format of the block IDs before their base64 encoding; all block IDs of a blob must have the
	 * same length.
	 */
	private static final String BLOCK_ID_FORMAT = "block-%06d";

	/**
	 * HTTP status of a range request whose range starts beyond the end of the content.
	 */
	private static final int STATUS_RANGE_NOT_SATISFIABLE = 416;

	/**
	 * HTTP status of a request for a missing blob.
	 */
	private static final int STATUS_NOT_FOUND = 404;

	/**
	 * Configuration of a {@link AzureBlobStore}.
	 */
	@DisplayOrder({
		Config.CONTAINER,
		Config.PREFIX,
		Config.SINGLE_UPLOAD_THRESHOLD,
		Config.BLOCK_SIZE,
		Config.DIRECT_DOWNLOAD,
		Config.DIRECT_DOWNLOAD_MIN_SIZE,
		Config.DIRECT_DOWNLOAD_LIFETIME,
		Config.PUBLIC_ENDPOINT,
	})
	public interface Config<I extends AzureBlobStore> extends BlobStore.Config<I>, AzureStorageAccountConfig {

		/**
		 * Configuration name of {@link #getContainer()}.
		 */
		String CONTAINER = "container";

		/**
		 * Configuration name of {@link #getPrefix()}.
		 */
		String PREFIX = "prefix";

		/**
		 * Configuration name of {@link #getSingleUploadThreshold()}.
		 */
		String SINGLE_UPLOAD_THRESHOLD = "single-upload-threshold";

		/**
		 * Configuration name of {@link #getBlockSize()}.
		 */
		String BLOCK_SIZE = "block-size";

		/**
		 * Configuration name of {@link #getDirectDownload()}.
		 */
		String DIRECT_DOWNLOAD = "direct-download";

		/**
		 * Configuration name of {@link #getDirectDownloadMinSize()}.
		 */
		String DIRECT_DOWNLOAD_MIN_SIZE = "direct-download-min-size";

		/**
		 * Configuration name of {@link #getDirectDownloadLifetime()}.
		 */
		String DIRECT_DOWNLOAD_LIFETIME = "direct-download-lifetime";

		/**
		 * Configuration name of {@link #getPublicEndpoint()}.
		 */
		String PUBLIC_ENDPOINT = "public-endpoint";

		/**
		 * The name of the container holding the blobs.
		 *
		 * <p>
		 * The container must exist; it is not created by the store, so that a misspelled name is
		 * detected instead of silently storing the content in a new container.
		 * </p>
		 */
		@Name(CONTAINER)
		@Mandatory
		String getContainer();

		/**
		 * @see #getContainer()
		 */
		void setContainer(String value);

		/**
		 * Prefix of the names of the blobs inside the container, e.g. <code>blobs/</code>.
		 *
		 * <p>
		 * The name of a blob is this prefix directly followed by the key of the blob, so a prefix
		 * that is meant as directory must end with a slash. Several stores may share a container
		 * with different prefixes. Empty to store the blobs at the top level of the container.
		 * Blobs under the prefix that are not blobs of the store, and blobs in deeper directories
		 * below the prefix, are ignored.
		 * </p>
		 */
		@Name(PREFIX)
		String getPrefix();

		/**
		 * @see #getPrefix()
		 */
		void setPrefix(String value);

		/**
		 * The maximum size of content uploaded in a single request.
		 *
		 * <p>
		 * Content up to this size is buffered in memory and uploaded in one request. Larger
		 * content is uploaded in blocks. The size is given in bytes, optionally with a unit, e.g.
		 * <code>16MB</code>, and must not exceed 2147483639 bytes (2 GB minus 8 bytes). The
		 * default is 16 MB.
		 * </p>
		 *
		 * <p>
		 * The threshold should be at least the {@link #getBlockSize()}: a smaller threshold
		 * uploads content between both sizes as a single block, which takes two requests instead
		 * of one.
		 * </p>
		 *
		 * @implNote The upper bound is {@link AzureBlobStore#MAX_REQUEST_SIZE}.
		 */
		@Name(SINGLE_UPLOAD_THRESHOLD)
		@Format(MemorySizeFormat.class)
		@LongDefault(DEFAULT_SINGLE_UPLOAD_THRESHOLD)
		@Constraint(NonNegative.class)
		@Bound(comparison = Comparision.SMALLER_OR_EQUAL, value = MAX_REQUEST_SIZE)
		@ComparisonDependency(comparison = Comparision.GREATER_OR_EQUAL, other = @Ref(BLOCK_SIZE), symmetric = false,
			asWarning = true)
		long getSingleUploadThreshold();

		/**
		 * @see #getSingleUploadThreshold()
		 */
		void setSingleUploadThreshold(long value);

		/**
		 * The size of the blocks of an upload in blocks.
		 *
		 * <p>
		 * One block is buffered in memory per running upload. The size is given in bytes,
		 * optionally with a unit, e.g. <code>8MB</code>. It must be at least 64 kB and must not
		 * exceed 2147483639 bytes (2 GB minus 8 bytes). Since a blob consists of at most 50,000
		 * blocks, the block size limits the size of a blob: the default of 8 MB allows blobs of up
		 * to 400 GB.
		 * </p>
		 *
		 * @implNote The bounds are {@link AzureBlobStore#MIN_BLOCK_SIZE} and
		 *           {@link AzureBlobStore#MAX_REQUEST_SIZE}.
		 */
		@Name(BLOCK_SIZE)
		@Format(MemorySizeFormat.class)
		@LongDefault(DEFAULT_BLOCK_SIZE)
		@Bound(comparison = Comparision.GREATER_OR_EQUAL, value = MIN_BLOCK_SIZE)
		@Bound(comparison = Comparision.SMALLER_OR_EQUAL, value = MAX_REQUEST_SIZE)
		long getBlockSize();

		/**
		 * @see #getBlockSize()
		 */
		void setBlockSize(long value);

		/**
		 * Whether browsers download content directly from the storage.
		 *
		 * <p>
		 * When enabled, a download in the React UI is answered with a redirect to a URL of the
		 * blob with a shared access signature instead of streaming the content through the
		 * application server. The browser must be able to reach the storage, see
		 * {@link #getPublicEndpoint()}. The storage then also serves range requests, e.g. for
		 * seeking in audio or PDF content. The integrity check of the content against its stored
		 * hash is not applied to direct downloads. Signing the URL requires the account key; with
		 * a connection string that contains no account key, the content is streamed.
		 * </p>
		 */
		@Name(DIRECT_DOWNLOAD)
		boolean getDirectDownload();

		/**
		 * @see #getDirectDownload()
		 */
		void setDirectDownload(boolean value);

		/**
		 * The minimum size of content downloaded directly from the storage.
		 *
		 * <p>
		 * Smaller content is streamed through the application server, since the redirect would
		 * cost more than it saves. The size is given in bytes, optionally with a unit, e.g.
		 * <code>1MB</code>; the default is 1 MB. Only relevant if {@link #getDirectDownload()} is
		 * enabled.
		 * </p>
		 */
		@Name(DIRECT_DOWNLOAD_MIN_SIZE)
		@DynamicMode(fun = VisibleIf.class, args = @Ref(DIRECT_DOWNLOAD))
		@Format(MemorySizeFormat.class)
		@LongDefault(DEFAULT_DIRECT_DOWNLOAD_MIN_SIZE)
		@Constraint(NonNegative.class)
		long getDirectDownloadMinSize();

		/**
		 * @see #getDirectDownloadMinSize()
		 */
		void setDirectDownloadMinSize(long value);

		/**
		 * The time a URL for a direct download stays valid, e.g. <code>60s</code>.
		 *
		 * <p>
		 * The storage checks the validity when the transfer starts, so a slow download of large
		 * content completes after the URL has expired. Anyone holding the URL can fetch the
		 * content within this time. At least one second, at most 7 days; the default is one
		 * minute. Only relevant if {@link #getDirectDownload()} is enabled.
		 * </p>
		 *
		 * @implNote The bounds are {@link AzureBlobStore#MIN_DIRECT_DOWNLOAD_LIFETIME} and
		 *           {@link AzureBlobStore#MAX_DIRECT_DOWNLOAD_LIFETIME}.
		 */
		@Name(DIRECT_DOWNLOAD_LIFETIME)
		@DynamicMode(fun = VisibleIf.class, args = @Ref(DIRECT_DOWNLOAD))
		@Format(MillisFormat.class)
		@LongDefault(DEFAULT_DIRECT_DOWNLOAD_LIFETIME)
		@Bound(comparison = Comparision.GREATER_OR_EQUAL, value = MIN_DIRECT_DOWNLOAD_LIFETIME)
		@Bound(comparison = Comparision.SMALLER_OR_EQUAL, value = MAX_DIRECT_DOWNLOAD_LIFETIME)
		long getDirectDownloadLifetime();

		/**
		 * @see #getDirectDownloadLifetime()
		 */
		void setDirectDownloadLifetime(long value);

		/**
		 * The URL under which browsers reach the blob service of the storage account, e.g.
		 * <code>https://files.example.com</code>.
		 *
		 * <p>
		 * URLs for direct downloads are issued for this address instead of the endpoint of the
		 * account. For the storage emulator Azurite, the address includes the account name as
		 * path, like the endpoint does. Empty if browsers reach the storage under the endpoint of
		 * the account. The value is an absolute URL with protocol and host. Only relevant if
		 * {@link #getDirectDownload()} is enabled.
		 * </p>
		 */
		@Name(PUBLIC_ENDPOINT)
		@DynamicMode(fun = VisibleIf.class, args = @Ref(DIRECT_DOWNLOAD))
		@Constraint(HasURLFormat.class)
		String getPublicEndpoint();

		/**
		 * @see #getPublicEndpoint()
		 */
		void setPublicEndpoint(String value);

		/**
		 * Implementation class of the store.
		 */
		@Override
		@ClassDefault(AzureBlobStore.class)
		Class<? extends I> getImplementationClass();

	}

	private final String _containerName;

	private final String _prefix;

	private final AzureStorageAccount _account;

	private final BlobContainerClient _container;

	private final int _singleUploadThreshold;

	private final int _blockSize;

	private final URI _publicEndpoint;

	private final Duration _directDownloadLifetime;

	/**
	 * Creates a {@link AzureBlobStore} from configuration.
	 *
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public AzureBlobStore(InstantiationContext context, Config<?> config) {
		super(context, config);
		String owner = "Azure blob store '" + config.getName() + "'";
		_containerName = config.getContainer();
		_prefix = StringServices.nonNull(config.getPrefix());
		_blockSize =
			(int) checkBounds(context, Config.BLOCK_SIZE, config.getBlockSize(), MIN_BLOCK_SIZE, MAX_REQUEST_SIZE);
		_singleUploadThreshold = (int) checkBounds(context, Config.SINGLE_UPLOAD_THRESHOLD,
			config.getSingleUploadThreshold(), 0, MAX_REQUEST_SIZE);
		_publicEndpoint =
			AzureStorageAccount.parseUrl(context, owner, Config.PUBLIC_ENDPOINT, config.getPublicEndpoint());
		_directDownloadLifetime = Duration.ofMillis(checkBounds(context, Config.DIRECT_DOWNLOAD_LIFETIME,
			config.getDirectDownloadLifetime(), MIN_DIRECT_DOWNLOAD_LIFETIME, MAX_DIRECT_DOWNLOAD_LIFETIME));
		_account = AzureStorageAccount.create(context, config, owner);
		_container = _account == null ? null : _account.getClient().getBlobContainerClient(_containerName);
	}

	private static long checkBounds(InstantiationContext context, String property, long value, long min, long max) {
		if (value < min || value > max) {
			context.error("Value of '" + property + "' out of range [" + min + ", " + max + "]: " + value);
			return Math.max(min, Math.min(max, value));
		}
		return value;
	}

	/**
	 * The access to the storage account.
	 */
	public AzureStorageAccount getAccount() {
		return _account;
	}

	/**
	 * The client of the container holding the blobs.
	 */
	protected BlobContainerClient getContainerClient() {
		return _container;
	}

	/**
	 * The name of the container holding the blobs.
	 */
	public String getContainer() {
		return _containerName;
	}

	/**
	 * The prefix of the names of the blobs inside the container.
	 */
	public String getPrefix() {
		return _prefix;
	}

	/**
	 * The URL of the blob service as reached by browsers.
	 *
	 * <p>
	 * The configured public endpoint, or the URL of the account if none is configured.
	 * </p>
	 */
	public URI getPublicEndpoint() {
		return _publicEndpoint != null ? _publicEndpoint : URI.create(_account.getAccountUrl());
	}

	/**
	 * The time a URL for a direct download stays valid.
	 */
	public Duration getDirectDownloadLifetime() {
		return _directDownloadLifetime;
	}

	/**
	 * The name of the blob holding the content with the given key.
	 *
	 * @param key
	 *        The key of the blob.
	 * @throws IllegalArgumentException
	 *         If the key is not a valid key of this store.
	 */
	public String getBlobName(String key) {
		checkKey(key);
		return _prefix + key;
	}

	/**
	 * The key of the content stored in the blob with the given name, or <code>null</code> if the
	 * blob is not a blob of this store.
	 *
	 * @param blobName
	 *        The name of a blob in the container.
	 */
	public String getKey(String blobName) {
		if (blobName == null || !blobName.startsWith(_prefix)) {
			return null;
		}
		String key = blobName.substring(_prefix.length());
		if (!isValidKey(key)) {
			return null;
		}
		return key;
	}

	/**
	 * The ID of the block with the given index in an upload in blocks.
	 *
	 * @param index
	 *        The index of the block, starting with <code>0</code>.
	 */
	public static String blockId(int index) {
		return Base64.getEncoder()
			.encodeToString(String.format(BLOCK_ID_FORMAT, Integer.valueOf(index)).getBytes(StandardCharsets.US_ASCII));
	}

	private BlockBlobClient blob(String key) {
		return _container.getBlobClient(getBlobName(key)).getBlockBlobClient();
	}

	@Override
	public String put(InputStream content, long size, String contentType) throws IOException {
		String key = newKey();
		BlockBlobClient blob = blob(key);
		BlobHttpHeaders headers =
			new BlobHttpHeaders().setContentType(contentType == null ? DEFAULT_CONTENT_TYPE : contentType);

		// For a known size, read at most one byte more than announced to detect longer content
		// without reading it to its end.
		InputStream in = size >= 0 && size < Long.MAX_VALUE ? new LimitedInputStream(content, size + 1) : content;
		byte[] head = null;
		if (size < 0 || size <= _singleUploadThreshold) {
			head = in.readNBytes(_singleUploadThreshold + 1);
			if (head.length <= _singleUploadThreshold) {
				checkSize(size, head.length);
				uploadSingle(blob, head, headers);
				return key;
			}
		}
		uploadBlocks(blob, head, in, size, headers);
		return key;
	}

	private void uploadSingle(BlockBlobClient blob, byte[] content, BlobHttpHeaders headers) throws IOException {
		BlockBlobSimpleUploadOptions options = new BlockBlobSimpleUploadOptions(BinaryData.fromBytes(content))
			.setHeaders(headers)
			.setRequestConditions(createOnly());
		try {
			blob.uploadWithResponse(options, null, Context.NONE);
		} catch (RuntimeException ex) {
			throw ioException("Storing blob '" + blob.getBlobName() + "' failed", ex);
		}
	}

	/**
	 * Uploads the content in blocks and commits them as blob.
	 *
	 * @param head
	 *        The content already read from the stream, or <code>null</code>.
	 */
	private void uploadBlocks(BlockBlobClient blob, byte[] head, InputStream in, long size, BlobHttpHeaders headers)
			throws IOException {
		List<String> blockIds = new ArrayList<>();
		byte[] buffer = new byte[_blockSize];
		int headPosition = 0;
		long total = 0;
		try {
			while (true) {
				int length = 0;
				if (head != null && headPosition < head.length) {
					length = Math.min(_blockSize, head.length - headPosition);
					System.arraycopy(head, headPosition, buffer, 0, length);
					headPosition += length;
				}
				if (length < _blockSize) {
					length += in.readNBytes(buffer, length, _blockSize - length);
				}
				if (length == 0) {
					break;
				}
				total += length;
				if (size >= 0 && total > size) {
					checkSize(size, total);
				}
				if (blockIds.size() >= MAX_BLOCKS) {
					throw new IOException("Content of blob '" + blob.getBlobName() + "' exceeds the maximum of "
						+ MAX_BLOCKS + " blocks of " + _blockSize + " bytes.");
				}
				String blockId = blockId(blockIds.size());
				byte[] block = length == _blockSize ? buffer : Arrays.copyOf(buffer, length);
				// The buffer is not copied, the call returns after the block has been transferred.
				blob.stageBlock(blockId, BinaryData.fromBytes(block));
				blockIds.add(blockId);
				if (length < _blockSize) {
					break;
				}
			}
			checkSize(size, total);

			BlockBlobCommitBlockListOptions options = new BlockBlobCommitBlockListOptions(blockIds)
				.setHeaders(headers)
				.setRequestConditions(createOnly());
			blob.commitBlockListWithResponse(options, null, Context.NONE);
		} catch (RuntimeException ex) {
			throw ioException("Uploading blob '" + blob.getBlobName() + "' failed", ex);
		}
	}

	/**
	 * Request conditions that let a write fail, if the blob exists.
	 */
	private static BlobRequestConditions createOnly() {
		return new BlobRequestConditions().setIfNoneMatch(ANY_ETAG);
	}

	@Override
	public InputStream get(String key) throws IOException {
		BlockBlobClient blob = blob(key);
		try {
			return blob.openInputStream();
		} catch (RuntimeException ex) {
			throw readException(key, ex);
		}
	}

	@Override
	public InputStream get(String key, long offset, long length) throws IOException {
		checkRange(offset, length);
		BlockBlobClient blob = blob(key);
		try {
			if (length == 0) {
				// An empty range cannot be expressed as range; only the existence is checked.
				blob.getProperties();
				return InputStream.nullInputStream();
			}
			BlobRange range = offset + (length - 1) < offset ? new BlobRange(offset) : new BlobRange(offset, length);
			return blob.openInputStream(new BlobInputStreamOptions().setRange(range));
		} catch (BlobStorageException ex) {
			if (ex.getStatusCode() == STATUS_RANGE_NOT_SATISFIABLE) {
				// The range starts at or beyond the end of the content.
				return InputStream.nullInputStream();
			}
			throw readException(key, ex);
		} catch (RuntimeException ex) {
			throw readException(key, ex);
		}
	}

	private IOException readException(String key, RuntimeException ex) {
		if (ex instanceof BlobStorageException) {
			BlobStorageException storageException = (BlobStorageException) ex;
			if (storageException.getStatusCode() == STATUS_NOT_FOUND
				&& !BlobErrorCode.CONTAINER_NOT_FOUND.equals(storageException.getErrorCode())) {
				return new NoSuchBlobException(getName(), key, ex);
			}
		}
		return ioException("Reading blob '" + key + "' failed", ex);
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * Creates a URL with a shared access signature for reading the blob, if direct downloads are
	 * enabled, the content has at least the configured minimum size, and the account key is
	 * known. The signature forces the response headers <code>Content-Type</code>,
	 * <code>Content-Disposition</code> (<code>inline</code> with the given file name) and
	 * <code>Cache-Control</code> ({@link #DIRECT_DOWNLOAD_CACHE_CONTROL}), independent of the
	 * properties of the stored blob.
	 * </p>
	 */
	@Override
	public URI createDownloadUrl(String key, long size, String contentType, String fileName) {
		Config<?> config = getConfig();
		if (!config.getDirectDownload() || size < config.getDirectDownloadMinSize()
			|| _account.getCredential() == null) {
			return null;
		}
		BlockBlobClient blob = blob(key);
		BlobServiceSasSignatureValues signature = new BlobServiceSasSignatureValues(
			OffsetDateTime.now().plus(_directDownloadLifetime), new BlobSasPermission().setReadPermission(true))
				.setContentType(contentType == null ? DEFAULT_CONTENT_TYPE : contentType)
				.setContentDisposition(ContentDisposition.headerValue(ContentDisposition.INLINE, fileName))
				.setCacheControl(DIRECT_DOWNLOAD_CACHE_CONTROL);
		try {
			String sas = blob.generateSas(signature);
			return new URI(publicUrl(blob.getBlobUrl()) + "?" + sas);
		} catch (RuntimeException | URISyntaxException ex) {
			Logger.warn("Cannot create a direct download URL for blob '" + key + "' (Azure blob store '" + getName()
				+ "'), the content is streamed.", ex, AzureBlobStore.class);
			return null;
		}
	}

	/**
	 * The given URL of a blob, as reached under the {@link #getPublicEndpoint() public endpoint}.
	 */
	private String publicUrl(String blobUrl) {
		String accountUrl = _account.getAccountUrl();
		if (_publicEndpoint == null || !blobUrl.startsWith(accountUrl)) {
			return blobUrl;
		}
		String publicEndpoint = _publicEndpoint.toString();
		if (publicEndpoint.endsWith(DELIMITER)) {
			publicEndpoint = publicEndpoint.substring(0, publicEndpoint.length() - 1);
		}
		return publicEndpoint + blobUrl.substring(accountUrl.length());
	}

	@Override
	public void delete(String key) throws IOException {
		BlockBlobClient blob = blob(key);
		try {
			blob.deleteIfExistsWithResponse(DeleteSnapshotsOptionType.INCLUDE, null, null, Context.NONE);
		} catch (RuntimeException ex) {
			throw ioException("Deleting blob '" + key + "' failed", ex);
		}
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * The listing is fetched page by page while the stream is consumed. A failure while fetching a
	 * page is reported as {@link BlobStorageException}.
	 * </p>
	 */
	@Override
	public Stream<BlobInfo> list() throws IOException {
		ListBlobsOptions options = new ListBlobsOptions();
		if (!_prefix.isEmpty()) {
			options.setPrefix(_prefix);
		}
		try {
			return _container.listBlobsByHierarchy(DELIMITER, options, null).stream()
				.map(this::info)
				.filter(info -> info != null);
		} catch (RuntimeException ex) {
			throw ioException("Listing container '" + _containerName + "' failed", ex);
		}
	}

	private BlobInfo info(BlobItem item) {
		if (Boolean.TRUE.equals(item.isPrefix())) {
			return null;
		}
		String key = getKey(item.getName());
		if (key == null) {
			return null;
		}
		BlobItemProperties properties = item.getProperties();
		Long size = properties.getContentLength();
		OffsetDateTime lastModified = properties.getLastModified();
		return new BlobInfo(key, size == null ? 0 : size.longValue(),
			lastModified == null ? null : lastModified.toInstant());
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * Blocks of uploads that were never committed are discarded by the storage itself after 7
	 * days, so the store has no temporary artifacts to remove.
	 * </p>
	 */
	@Override
	public void cleanup(Instant olderThan) throws IOException {
		// Uncommitted blocks expire in the storage.
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * The HTTP client of the JDK releases its connections when it is no longer referenced, so
	 * there is nothing to close.
	 * </p>
	 */
	@Override
	public void close() throws IOException {
		// No resources held.
	}

	private IOException ioException(String message, RuntimeException ex) {
		return new IOException(message + " (Azure blob store '" + getName() + "'): " + ex.getMessage(), ex);
	}

}
