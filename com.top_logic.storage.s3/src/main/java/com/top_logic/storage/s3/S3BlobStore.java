/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.storage.s3;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.checksums.RequestChecksumCalculation;
import software.amazon.awssdk.core.checksums.ResponseChecksumValidation;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.CompletedPart;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.ListMultipartUploadsRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.MultipartUpload;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.NoSuchUploadException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.model.S3Object;
import software.amazon.awssdk.services.s3.model.ServerSideEncryption;
import software.amazon.awssdk.services.s3.model.UploadPartResponse;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.utils.SdkAutoCloseable;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Encrypted;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.basic.config.annotation.defaults.LongDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.config.constraint.annotation.Bound;
import com.top_logic.basic.config.constraint.annotation.Comparision;
import com.top_logic.basic.config.constraint.annotation.ComparisonDependency;
import com.top_logic.basic.config.constraint.annotation.Constraint;
import com.top_logic.basic.config.constraint.impl.HasURLFormat;
import com.top_logic.basic.config.constraint.impl.MandatoryIfGiven;
import com.top_logic.basic.config.constraint.impl.NonNegative;
import com.top_logic.basic.config.format.MemorySizeFormat;
import com.top_logic.basic.config.format.MillisFormat;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.basic.io.binary.ContentDisposition;
import com.top_logic.basic.io.LimitedInputStream;
import com.top_logic.basic.io.blob.AbstractBlobStore;
import com.top_logic.basic.io.blob.BlobInfo;
import com.top_logic.basic.io.blob.BlobStore;
import com.top_logic.basic.io.blob.NoSuchBlobException;

/**
 * {@link BlobStore} keeping each blob as an object in a bucket of AWS S3 or an S3-compatible
 * object storage (e.g. <code>MinIO</code>, Ceph RGW, Garage, <code>SeaweedFS</code>).
 *
 * <p>
 * The object of a blob is named by the configured prefix followed by the key of the blob. The
 * bucket must exist. Content up to the multipart threshold is buffered in memory and uploaded in a
 * single request; larger content and content of unknown size exceeding the threshold is streamed
 * in a multipart upload, buffering one part at a time. A failed multipart upload is aborted;
 * leftovers of interrupted uploads are removed by the cleanup of the store.
 * </p>
 *
 * <p>
 * Without configured access keys, the credentials are taken from the default credentials provider
 * chain of the AWS SDK: environment variables, the <code>~/.aws/credentials</code> file, or the
 * IAM role of the instance or container.
 * </p>
 *
 * <p>
 * Optionally, browsers download content directly from the storage through short-lived presigned
 * URLs instead of receiving it through the application server, see
 * {@link Config#getDirectDownload()}.
 * </p>
 *
 * @implNote The store communicates through the lightweight HTTP client based on
 *           {@link java.net.HttpURLConnection} ({@link UrlConnectionHttpClient}), so that neither
 *           the Apache nor the Netty HTTP client of the AWS SDK is required. Request checksums are
 *           only computed and response checksums only validated where the S3 API requires them
 *           ({@link RequestChecksumCalculation#WHEN_REQUIRED}), since many S3-compatible servers
 *           do not support the trailing checksums the SDK sends otherwise. The client is created by
 *           {@link #createClient()}, which subclasses may override; the resolved connection
 *           settings are available through {@link #getEndpoint()}, {@link #getRegion()} and
 *           {@link #getCredentialsProvider()}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class S3BlobStore extends AbstractBlobStore<S3BlobStore.Config<?>> {

	/**
	 * Minimum size of a part of a multipart upload accepted by S3 (except for the last part).
	 */
	public static final long MIN_PART_SIZE = 5L * 1024 * 1024;

	/**
	 * Maximum size of a single upload request: the S3 limit, further restricted by the size of a
	 * byte array that buffers the content.
	 */
	public static final long MAX_REQUEST_SIZE = Integer.MAX_VALUE - 8;

	/**
	 * Maximum number of parts of a multipart upload accepted by S3.
	 */
	public static final int MAX_PARTS = 10000;

	/**
	 * Default value of {@link Config#getPartSize()}.
	 */
	public static final long DEFAULT_PART_SIZE = 8L * 1024 * 1024;

	/**
	 * Default value of {@link Config#getMultipartThreshold()}.
	 */
	public static final long DEFAULT_MULTIPART_THRESHOLD = 16L * 1024 * 1024;

	/**
	 * Default value of {@link Config#getDirectDownloadMinSize()}.
	 */
	public static final long DEFAULT_DIRECT_DOWNLOAD_MIN_SIZE = 1024L * 1024;

	/**
	 * Default value of {@link Config#getDirectDownloadLifetime()} in milliseconds.
	 */
	public static final long DEFAULT_DIRECT_DOWNLOAD_LIFETIME = 60L * 1000;

	/**
	 * Maximum lifetime of a presigned URL accepted by S3 (7 days) in milliseconds.
	 */
	public static final long MAX_DIRECT_DOWNLOAD_LIFETIME = 7L * 24 * 60 * 60 * 1000;

	/**
	 * Minimum lifetime of a presigned URL in milliseconds.
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
	 * Default value of {@link Config#getRegion()}.
	 */
	public static final String DEFAULT_REGION = "us-east-1";

	/**
	 * Content type sent for content without a content type.
	 */
	private static final String DEFAULT_CONTENT_TYPE = "application/octet-stream";

	/**
	 * Separator of path segments in object names.
	 */
	private static final String DELIMITER = "/";

	/**
	 * HTTP status of a range request whose range starts beyond the end of the content.
	 */
	private static final int STATUS_RANGE_NOT_SATISFIABLE = 416;

	/**
	 * HTTP status of a request for a missing object.
	 */
	private static final int STATUS_NOT_FOUND = 404;

	/**
	 * Configuration of a {@link S3BlobStore}.
	 */
	@DisplayOrder({
		Config.ENDPOINT,
		Config.REGION,
		Config.PATH_STYLE_ACCESS,
		Config.BUCKET,
		Config.PREFIX,
		Config.ACCESS_KEY,
		Config.SECRET_KEY,
		Config.SERVER_SIDE_ENCRYPTION,
		Config.KMS_KEY_ID,
		Config.MULTIPART_THRESHOLD,
		Config.PART_SIZE,
		Config.DIRECT_DOWNLOAD,
		Config.DIRECT_DOWNLOAD_MIN_SIZE,
		Config.DIRECT_DOWNLOAD_LIFETIME,
		Config.PUBLIC_ENDPOINT,
	})
	public interface Config<I extends S3BlobStore> extends BlobStore.Config<I> {

		/**
		 * Configuration name of {@link #getEndpoint()}.
		 */
		String ENDPOINT = "endpoint";

		/**
		 * Configuration name of {@link #getRegion()}.
		 */
		String REGION = "region";

		/**
		 * Configuration name of {@link #getBucket()}.
		 */
		String BUCKET = "bucket";

		/**
		 * Configuration name of {@link #getPrefix()}.
		 */
		String PREFIX = "prefix";

		/**
		 * Configuration name of {@link #getPathStyleAccess()}.
		 */
		String PATH_STYLE_ACCESS = "path-style-access";

		/**
		 * Configuration name of {@link #getAccessKey()}.
		 */
		String ACCESS_KEY = "access-key";

		/**
		 * Configuration name of {@link #getSecretKey()}.
		 */
		String SECRET_KEY = "secret-key";

		/**
		 * Configuration name of {@link #getServerSideEncryption()}.
		 */
		String SERVER_SIDE_ENCRYPTION = "server-side-encryption";

		/**
		 * Configuration name of {@link #getKmsKeyId()}.
		 */
		String KMS_KEY_ID = "kms-key-id";

		/**
		 * Configuration name of {@link #getMultipartThreshold()}.
		 */
		String MULTIPART_THRESHOLD = "multipart-threshold";

		/**
		 * Configuration name of {@link #getPartSize()}.
		 */
		String PART_SIZE = "part-size";

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
		 * The URL of the S3 service, e.g. <code>https://minio.example.com:9000</code>.
		 *
		 * <p>
		 * Empty for AWS S3, where the endpoint is derived from the region. Required for
		 * S3-compatible servers. The value is an absolute URL with protocol and host.
		 * </p>
		 */
		@Name(ENDPOINT)
		@Constraint(HasURLFormat.class)
		String getEndpoint();

		/**
		 * @see #getEndpoint()
		 */
		void setEndpoint(String value);

		/**
		 * The region of the bucket, e.g. <code>eu-central-1</code>.
		 *
		 * <p>
		 * S3-compatible servers usually accept any region; the request signature is computed for
		 * the region given here.
		 * </p>
		 */
		@Name(REGION)
		@StringDefault(DEFAULT_REGION)
		String getRegion();

		/**
		 * @see #getRegion()
		 */
		void setRegion(String value);

		/**
		 * The name of the bucket holding the blobs.
		 *
		 * <p>
		 * The bucket must exist; it is not created by the store.
		 * </p>
		 */
		@Name(BUCKET)
		@Mandatory
		String getBucket();

		/**
		 * @see #getBucket()
		 */
		void setBucket(String value);

		/**
		 * Prefix of the object names of the blobs inside the bucket, e.g. <code>blobs/</code>.
		 *
		 * <p>
		 * The object name of a blob is this prefix directly followed by the key of the blob, so a
		 * prefix that is meant as directory must end with a slash. Several stores may share a
		 * bucket with different prefixes. Empty to store the blobs at the top level of the bucket.
		 * Objects under the prefix that are not blobs of the store, and objects in deeper
		 * directories below the prefix, are ignored.
		 * </p>
		 */
		@Name(PREFIX)
		String getPrefix();

		/**
		 * @see #getPrefix()
		 */
		void setPrefix(String value);

		/**
		 * Whether the bucket is addressed as part of the URL path
		 * (<code>https://host/bucket/key</code>) instead of the host name
		 * (<code>https://bucket.host/key</code>).
		 *
		 * <p>
		 * Most self-hosted S3-compatible servers (e.g. <code>MinIO</code>) require path-style access.
		 * </p>
		 */
		@Name(PATH_STYLE_ACCESS)
		boolean getPathStyleAccess();

		/**
		 * @see #getPathStyleAccess()
		 */
		void setPathStyleAccess(boolean value);

		/**
		 * The access key ID for authenticating at the storage.
		 *
		 * <p>
		 * Access key and secret key are either both given or both empty. When both are empty, the
		 * credentials are taken from the default credentials provider chain of the AWS SDK
		 * (environment variables, <code>~/.aws/credentials</code>, IAM role of the instance or
		 * container).
		 * </p>
		 */
		@Name(ACCESS_KEY)
		@Constraint(value = MandatoryIfGiven.class, args = @Ref(SECRET_KEY))
		String getAccessKey();

		/**
		 * @see #getAccessKey()
		 */
		void setAccessKey(String value);

		/**
		 * The secret key belonging to the {@link #getAccessKey()}.
		 *
		 * <p>
		 * Required, if an access key is given. The value is given encrypted in the configuration,
		 * or as plain text with the prefix <code>unencrypted:</code>.
		 * </p>
		 */
		@Name(SECRET_KEY)
		@Encrypted
		@Constraint(value = MandatoryIfGiven.class, args = @Ref(ACCESS_KEY))
		String getSecretKey();

		/**
		 * @see #getSecretKey()
		 */
		void setSecretKey(String value);

		/**
		 * The server-side encryption the storage applies to stored blobs.
		 */
		@Name(SERVER_SIDE_ENCRYPTION)
		@FormattedDefault("sse-s3")
		ServerSideEncryptionMode getServerSideEncryption();

		/**
		 * @see #getServerSideEncryption()
		 */
		void setServerSideEncryption(ServerSideEncryptionMode value);

		/**
		 * The ID or ARN of the key used for the encryption mode SSE-KMS.
		 *
		 * <p>
		 * Empty to use the default key of the key management service. Only relevant if the
		 * {@link #getServerSideEncryption()} is SSE-KMS, ignored for other encryption modes.
		 * </p>
		 */
		@Name(KMS_KEY_ID)
		@Label("KMS key ID")
		String getKmsKeyId();

		/**
		 * @see #getKmsKeyId()
		 */
		void setKmsKeyId(String value);

		/**
		 * The maximum size of content uploaded in a single request.
		 *
		 * <p>
		 * Content up to this size is buffered in memory and uploaded in one request. Larger
		 * content is uploaded in parts. The size is given in bytes, optionally with a unit, e.g.
		 * <code>16MB</code>, and must not exceed 2147483639 bytes (2 GB minus 8 bytes). The
		 * default is 16 MB.
		 * </p>
		 *
		 * <p>
		 * The threshold should be at least the {@link #getPartSize()}: a smaller threshold uploads
		 * content between both sizes as multipart upload of a single part, which takes three
		 * requests instead of one.
		 * </p>
		 *
		 * @implNote The upper bound is {@link S3BlobStore#MAX_REQUEST_SIZE}.
		 */
		@Name(MULTIPART_THRESHOLD)
		@Format(MemorySizeFormat.class)
		@LongDefault(DEFAULT_MULTIPART_THRESHOLD)
		@Constraint(NonNegative.class)
		@Bound(comparison = Comparision.SMALLER_OR_EQUAL, value = MAX_REQUEST_SIZE)
		@ComparisonDependency(comparison = Comparision.GREATER_OR_EQUAL, other = @Ref(PART_SIZE), symmetric = false,
			asWarning = true)
		long getMultipartThreshold();

		/**
		 * @see #getMultipartThreshold()
		 */
		void setMultipartThreshold(long value);

		/**
		 * The size of the parts of a multipart upload.
		 *
		 * <p>
		 * One part is buffered in memory per running upload. The size is given in bytes,
		 * optionally with a unit, e.g. <code>8MB</code>. It must be at least 5 MB and must not
		 * exceed 2147483639 bytes (2 GB minus 8 bytes). Since S3 accepts at most 10,000 parts per
		 * upload, the part size limits the size of a blob: the default of 8 MB allows blobs of up
		 * to 80 GB.
		 * </p>
		 *
		 * @implNote The bounds are {@link S3BlobStore#MIN_PART_SIZE} and
		 *           {@link S3BlobStore#MAX_REQUEST_SIZE}.
		 */
		@Name(PART_SIZE)
		@Format(MemorySizeFormat.class)
		@LongDefault(DEFAULT_PART_SIZE)
		@Bound(comparison = Comparision.GREATER_OR_EQUAL, value = MIN_PART_SIZE)
		@Bound(comparison = Comparision.SMALLER_OR_EQUAL, value = MAX_REQUEST_SIZE)
		long getPartSize();

		/**
		 * @see #getPartSize()
		 */
		void setPartSize(long value);

		/**
		 * Whether browsers download content directly from the storage.
		 *
		 * <p>
		 * When enabled, a download in the React UI is answered with a redirect to a presigned URL
		 * of the object instead of streaming the content through the application server. The
		 * browser must be able to reach the storage, see {@link #getPublicEndpoint()}. The storage
		 * then also serves range requests, e.g. for seeking in audio or PDF content. The
		 * integrity check of the content against its stored hash is not applied to direct
		 * downloads.
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
		 * @implNote The bounds are {@link S3BlobStore#MIN_DIRECT_DOWNLOAD_LIFETIME} and
		 *           {@link S3BlobStore#MAX_DIRECT_DOWNLOAD_LIFETIME}.
		 */
		@Name(DIRECT_DOWNLOAD_LIFETIME)
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
		 * The URL under which browsers reach the S3 service, e.g.
		 * <code>https://storage.example.com</code>.
		 *
		 * <p>
		 * URLs for direct downloads are issued for this address. Empty if browsers reach the
		 * storage under the address given in {@link #getEndpoint()}. The value is an absolute URL
		 * with protocol and host. Only relevant if {@link #getDirectDownload()} is enabled.
		 * </p>
		 */
		@Name(PUBLIC_ENDPOINT)
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
		@ClassDefault(S3BlobStore.class)
		Class<? extends I> getImplementationClass();

	}

	private final String _bucket;

	private final String _prefix;

	private final URI _endpoint;

	private final Region _region;

	private final AwsCredentialsProvider _credentialsProvider;

	private final int _multipartThreshold;

	private final int _partSize;

	private final S3Client _client;

	private final URI _publicEndpoint;

	private final Duration _directDownloadLifetime;

	/**
	 * The presigner for direct download URLs, created on first use.
	 */
	private S3Presigner _presigner;

	/**
	 * Creates a {@link S3BlobStore} from configuration.
	 *
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public S3BlobStore(InstantiationContext context, Config<?> config) {
		super(context, config);
		_bucket = config.getBucket();
		_prefix = StringServices.nonNull(config.getPrefix());
		_endpoint = parseEndpoint(context, config.getEndpoint());
		_region = Region.of(StringServices.isEmpty(config.getRegion()) ? DEFAULT_REGION : config.getRegion());
		_credentialsProvider = createCredentialsProvider(context, config);
		_partSize = (int) checkBounds(context, Config.PART_SIZE, config.getPartSize(), MIN_PART_SIZE, MAX_REQUEST_SIZE);
		_multipartThreshold =
			(int) checkBounds(context, Config.MULTIPART_THRESHOLD, config.getMultipartThreshold(), 0, MAX_REQUEST_SIZE);
		_publicEndpoint = parseEndpoint(context, config.getPublicEndpoint());
		_directDownloadLifetime = Duration.ofMillis(checkBounds(context, Config.DIRECT_DOWNLOAD_LIFETIME,
			config.getDirectDownloadLifetime(), MIN_DIRECT_DOWNLOAD_LIFETIME, MAX_DIRECT_DOWNLOAD_LIFETIME));
		_client = createClient();
	}

	private static URI parseEndpoint(InstantiationContext context, String endpoint) {
		if (StringServices.isEmpty(endpoint)) {
			return null;
		}
		try {
			URI result = new URI(endpoint);
			if (result.getScheme() == null || result.getHost() == null) {
				context.error("Invalid S3 endpoint, expected an absolute URL: '" + endpoint + "'.");
				return null;
			}
			return result;
		} catch (URISyntaxException ex) {
			context.error("Invalid S3 endpoint '" + endpoint + "': " + ex.getMessage(), ex);
			return null;
		}
	}

	private static AwsCredentialsProvider createCredentialsProvider(InstantiationContext context, Config<?> config) {
		String accessKey = config.getAccessKey();
		String secretKey = config.getSecretKey();
		boolean hasAccessKey = !StringServices.isEmpty(accessKey);
		boolean hasSecretKey = !StringServices.isEmpty(secretKey);
		if (hasAccessKey && hasSecretKey) {
			return StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey));
		}
		if (hasAccessKey || hasSecretKey) {
			context.error("S3 blob store '" + config.getName() + "': Access key and secret key must either both be"
				+ " given or both be empty.");
		}
		return DefaultCredentialsProvider.builder().build();
	}

	private static long checkBounds(InstantiationContext context, String property, long value, long min, long max) {
		if (value < min || value > max) {
			context.error("Value of '" + property + "' out of range [" + min + ", " + max + "]: " + value);
			return Math.max(min, Math.min(max, value));
		}
		return value;
	}

	/**
	 * Creates the client for accessing the storage.
	 *
	 * <p>
	 * Called once from the constructor, after the connection settings have been resolved.
	 * </p>
	 */
	protected S3Client createClient() {
		return configureClient(S3Client.builder()).build();
	}

	/**
	 * Applies the connection settings of this store to the given builder.
	 */
	protected S3ClientBuilder configureClient(S3ClientBuilder builder) {
		builder
			.httpClientBuilder(UrlConnectionHttpClient.builder())
			.region(_region)
			.credentialsProvider(_credentialsProvider)
			.forcePathStyle(Boolean.valueOf(getConfig().getPathStyleAccess()))
			.requestChecksumCalculation(RequestChecksumCalculation.WHEN_REQUIRED)
			.responseChecksumValidation(ResponseChecksumValidation.WHEN_REQUIRED);
		if (_endpoint != null) {
			builder.endpointOverride(_endpoint);
		}
		return builder;
	}

	/**
	 * Creates the presigner for the URLs of direct downloads.
	 *
	 * <p>
	 * Called on the first direct download. The presigner signs for the
	 * {@link #getPublicEndpoint() public endpoint} of the storage.
	 * </p>
	 */
	protected S3Presigner createPresigner() {
		return configurePresigner(S3Presigner.builder()).build();
	}

	/**
	 * Applies the connection settings of this store to the given presigner builder.
	 */
	protected S3Presigner.Builder configurePresigner(S3Presigner.Builder builder) {
		builder
			.region(_region)
			.credentialsProvider(_credentialsProvider)
			.serviceConfiguration(S3Configuration.builder()
				.pathStyleAccessEnabled(Boolean.valueOf(getConfig().getPathStyleAccess()))
				.build());
		URI endpoint = getPublicEndpoint();
		if (endpoint != null) {
			builder.endpointOverride(endpoint);
		}
		return builder;
	}

	private synchronized S3Presigner getPresigner() {
		if (_presigner == null) {
			_presigner = createPresigner();
		}
		return _presigner;
	}

	/**
	 * The URL of the S3 service as reached by browsers, or <code>null</code> for AWS S3, where the
	 * endpoint is derived from the region.
	 *
	 * <p>
	 * The configured public endpoint, or {@link #getEndpoint()} if none is configured.
	 * </p>
	 */
	public URI getPublicEndpoint() {
		return _publicEndpoint != null ? _publicEndpoint : _endpoint;
	}

	/**
	 * The time a URL for a direct download stays valid.
	 */
	public Duration getDirectDownloadLifetime() {
		return _directDownloadLifetime;
	}

	/**
	 * The URL of the S3 service, or <code>null</code> for AWS S3, where the endpoint is derived
	 * from the region.
	 */
	public URI getEndpoint() {
		return _endpoint;
	}

	/**
	 * The region of the bucket.
	 */
	public Region getRegion() {
		return _region;
	}

	/**
	 * The provider of the credentials for authenticating at the storage.
	 */
	public AwsCredentialsProvider getCredentialsProvider() {
		return _credentialsProvider;
	}

	/**
	 * The client accessing the storage.
	 */
	protected S3Client getClient() {
		return _client;
	}

	/**
	 * The name of the bucket holding the blobs.
	 */
	public String getBucket() {
		return _bucket;
	}

	/**
	 * The prefix of the object names of the blobs inside the bucket.
	 */
	public String getPrefix() {
		return _prefix;
	}

	/**
	 * The server-side encryption requested for stored blobs, or <code>null</code> if none is
	 * requested.
	 */
	public ServerSideEncryption getServerSideEncryption() {
		return getConfig().getServerSideEncryption().sdkValue();
	}

	/**
	 * The ID of the key for the server-side encryption, or <code>null</code> if the default key is
	 * used or the encryption mode uses no key management service.
	 */
	public String getKmsKeyId() {
		if (getConfig().getServerSideEncryption() != ServerSideEncryptionMode.SSE_KMS) {
			return null;
		}
		return StringServices.nonEmpty(getConfig().getKmsKeyId());
	}

	/**
	 * The name of the object holding the content of the blob with the given key.
	 *
	 * @param key
	 *        The key of the blob.
	 * @throws IllegalArgumentException
	 *         If the key is not a valid key of this store.
	 */
	public String getObjectName(String key) {
		checkKey(key);
		return _prefix + key;
	}

	/**
	 * The key of the blob stored in the object with the given name, or <code>null</code> if the
	 * object is not a blob of this store.
	 *
	 * @param objectName
	 *        The name of an object in the bucket.
	 */
	public String getKey(String objectName) {
		if (objectName == null || !objectName.startsWith(_prefix)) {
			return null;
		}
		String key = objectName.substring(_prefix.length());
		if (!isValidKey(key)) {
			return null;
		}
		return key;
	}

	/**
	 * The value of the HTTP <code>Range</code> header requesting the given range of content.
	 *
	 * @param offset
	 *        The position of the first byte. Must not be negative.
	 * @param length
	 *        The maximum number of bytes. Must be positive.
	 */
	public static String rangeHeader(long offset, long length) {
		long last = offset + (length - 1);
		if (last < offset) {
			// Overflow: The range extends to the end of the content.
			return "bytes=" + offset + "-";
		}
		return "bytes=" + offset + "-" + last;
	}

	@Override
	public String put(InputStream content, long size, String contentType) throws IOException {
		String key = newKey();
		String objectName = getObjectName(key);
		String type = contentType == null ? DEFAULT_CONTENT_TYPE : contentType;

		// For a known size, read at most one byte more than announced to detect longer content
		// without reading it to its end.
		InputStream in = size >= 0 && size < Long.MAX_VALUE ? new LimitedInputStream(content, size + 1) : content;
		if (size < 0 || size <= _multipartThreshold) {
			byte[] head = in.readNBytes(_multipartThreshold + 1);
			if (head.length <= _multipartThreshold) {
				checkSize(size, head.length);
				putObject(objectName, head, head.length, type);
				return key;
			}
			in = new SequenceInputStream(new ByteArrayInputStream(head), in);
		}
		multipartUpload(objectName, in, size, type);
		return key;
	}

	private void putObject(String objectName, byte[] buffer, int length, String contentType) throws IOException {
		PutObjectRequest.Builder request = PutObjectRequest.builder()
			.bucket(_bucket)
			.key(objectName)
			.contentType(contentType)
			.contentLength(Long.valueOf(length))
			.serverSideEncryption(getServerSideEncryption())
			.ssekmsKeyId(getKmsKeyId());
		try {
			_client.putObject(request.build(), body(buffer, length, contentType));
		} catch (SdkException ex) {
			throw ioException("Storing blob '" + objectName + "' failed", ex);
		}
	}

	private void multipartUpload(String objectName, InputStream in, long size, String contentType)
			throws IOException {
		CreateMultipartUploadRequest.Builder request = CreateMultipartUploadRequest.builder()
			.bucket(_bucket)
			.key(objectName)
			.contentType(contentType)
			.serverSideEncryption(getServerSideEncryption())
			.ssekmsKeyId(getKmsKeyId());

		String uploadId;
		try {
			uploadId = _client.createMultipartUpload(request.build()).uploadId();
		} catch (SdkException ex) {
			throw ioException("Starting the upload of blob '" + objectName + "' failed", ex);
		}

		boolean success = false;
		try {
			List<CompletedPart> parts = new ArrayList<>();
			byte[] buffer = new byte[_partSize];
			long total = 0;
			while (true) {
				int length = in.readNBytes(buffer, 0, _partSize);
				if (length == 0) {
					break;
				}
				total += length;
				if (size >= 0 && total > size) {
					checkSize(size, total);
				}
				int partNumber = parts.size() + 1;
				if (partNumber > MAX_PARTS) {
					throw new IOException("Content of blob '" + objectName + "' exceeds the maximum of " + MAX_PARTS
						+ " parts of " + _partSize + " bytes.");
				}
				UploadPartResponse response = _client.uploadPart(
					part -> part.bucket(_bucket).key(objectName).uploadId(uploadId).partNumber(partNumber)
						.contentLength(Long.valueOf(length)),
					body(buffer, length, contentType));
				parts.add(CompletedPart.builder().partNumber(partNumber).eTag(response.eTag()).build());
				if (length < _partSize) {
					break;
				}
			}
			checkSize(size, total);

			_client.completeMultipartUpload(complete -> complete.bucket(_bucket).key(objectName).uploadId(uploadId)
				.multipartUpload(upload -> upload.parts(parts)));
			success = true;
		} catch (SdkException ex) {
			throw ioException("Uploading blob '" + objectName + "' failed", ex);
		} finally {
			if (!success) {
				abortUpload(objectName, uploadId);
			}
		}
	}

	/**
	 * A request body delivering the first bytes of the given buffer.
	 *
	 * <p>
	 * The buffer is not copied; the body can be read again when the SDK retries the request.
	 * </p>
	 */
	private static RequestBody body(byte[] buffer, int length, String contentType) {
		return RequestBody.fromContentProvider(() -> new ByteArrayInputStream(buffer, 0, length), length, contentType);
	}

	private void abortUpload(String objectName, String uploadId) {
		try {
			_client.abortMultipartUpload(abort -> abort.bucket(_bucket).key(objectName).uploadId(uploadId));
		} catch (NoSuchUploadException ex) {
			// Already gone.
		} catch (SdkException ex) {
			Logger.warn("Cannot abort upload '" + uploadId + "' of blob '" + objectName + "' in bucket '" + _bucket
				+ "', it is removed by the cleanup of the store.", ex, S3BlobStore.class);
		}
	}

	@Override
	public InputStream get(String key) throws IOException {
		String objectName = getObjectName(key);
		try {
			return _client.getObject(request -> request.bucket(_bucket).key(objectName));
		} catch (SdkException ex) {
			throw readException(key, ex);
		}
	}

	@Override
	public InputStream get(String key, long offset, long length) throws IOException {
		checkRange(offset, length);
		String objectName = getObjectName(key);
		try {
			if (length == 0) {
				// An empty range cannot be expressed as range header; only the existence is checked.
				_client.headObject(request -> request.bucket(_bucket).key(objectName));
				return InputStream.nullInputStream();
			}
			return _client.getObject(request -> request.bucket(_bucket).key(objectName)
				.range(rangeHeader(offset, length)));
		} catch (S3Exception ex) {
			if (ex.statusCode() == STATUS_RANGE_NOT_SATISFIABLE) {
				// The range starts at or beyond the end of the content.
				return InputStream.nullInputStream();
			}
			throw readException(key, ex);
		} catch (SdkException ex) {
			throw readException(key, ex);
		}
	}

	private IOException readException(String key, SdkException ex) {
		if (ex instanceof NoSuchKeyException
			|| (ex instanceof S3Exception && ((S3Exception) ex).statusCode() == STATUS_NOT_FOUND)) {
			return new NoSuchBlobException(getName(), key, ex);
		}
		return ioException("Reading blob '" + key + "' failed", ex);
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * Creates a presigned URL for reading the object, if direct downloads are enabled and the
	 * content has at least the configured minimum size. The URL forces the response headers
	 * <code>Content-Type</code>, <code>Content-Disposition</code> (<code>inline</code> with the
	 * given file name) and <code>Cache-Control</code> ({@link #DIRECT_DOWNLOAD_CACHE_CONTROL}),
	 * independent of the metadata of the stored object.
	 * </p>
	 */
	@Override
	public URI createDownloadUrl(String key, long size, String contentType, String fileName) {
		Config<?> config = getConfig();
		if (!config.getDirectDownload() || size < config.getDirectDownloadMinSize()) {
			return null;
		}
		GetObjectRequest request = GetObjectRequest.builder()
			.bucket(_bucket)
			.key(getObjectName(key))
			.responseContentType(contentType == null ? DEFAULT_CONTENT_TYPE : contentType)
			.responseContentDisposition(ContentDisposition.headerValue(ContentDisposition.INLINE, fileName))
			.responseCacheControl(DIRECT_DOWNLOAD_CACHE_CONTROL)
			.build();
		try {
			return getPresigner()
				.presignGetObject(presign -> presign.signatureDuration(_directDownloadLifetime).getObjectRequest(request))
				.url().toURI();
		} catch (SdkException | URISyntaxException ex) {
			Logger.warn("Cannot create a direct download URL for blob '" + key + "' (S3 blob store '" + getName()
				+ "'), the content is streamed.", ex, S3BlobStore.class);
			return null;
		}
	}

	@Override
	public void delete(String key) throws IOException {
		String objectName = getObjectName(key);
		try {
			_client.deleteObject(request -> request.bucket(_bucket).key(objectName));
		} catch (SdkException ex) {
			throw ioException("Deleting blob '" + key + "' failed", ex);
		}
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * The listing is fetched page by page while the stream is consumed. A failure while fetching a
	 * page is reported as {@link SdkException}.
	 * </p>
	 */
	@Override
	public Stream<BlobInfo> list() throws IOException {
		ListObjectsV2Request.Builder request = ListObjectsV2Request.builder()
			.bucket(_bucket)
			.delimiter(DELIMITER);
		if (!_prefix.isEmpty()) {
			request.prefix(_prefix);
		}
		try {
			return _client.listObjectsV2Paginator(request.build()).contents().stream()
				.map(this::info)
				.filter(info -> info != null);
		} catch (SdkException ex) {
			throw ioException("Listing bucket '" + _bucket + "' failed", ex);
		}
	}

	private BlobInfo info(S3Object object) {
		String key = getKey(object.key());
		if (key == null) {
			return null;
		}
		return new BlobInfo(key, object.size().longValue(), object.lastModified());
	}

	/**
	 * Aborts the multipart uploads of blobs of this store that were started before the given time.
	 *
	 * <p>
	 * Some S3-compatible servers (e.g. <code>MinIO</code>) list multipart uploads only for an exact object name,
	 * so no stale upload is found there; these servers remove stale uploads themselves (<code>MinIO</code>:
	 * setting <code>api stale_uploads_expiry</code>, 24 hours by default). For AWS S3, a lifecycle
	 * rule aborting incomplete multipart uploads is an alternative.
	 * </p>
	 */
	@Override
	public void cleanup(Instant olderThan) throws IOException {
		ListMultipartUploadsRequest.Builder request = ListMultipartUploadsRequest.builder().bucket(_bucket);
		if (!_prefix.isEmpty()) {
			request.prefix(_prefix);
		}
		try {
			for (MultipartUpload upload : _client.listMultipartUploadsPaginator(request.build()).uploads()) {
				if (getKey(upload.key()) == null) {
					continue;
				}
				if (upload.initiated() != null && upload.initiated().isBefore(olderThan)) {
					abortUpload(upload.key(), upload.uploadId());
				}
			}
		} catch (SdkException ex) {
			throw ioException("Listing the uploads in bucket '" + _bucket + "' failed", ex);
		}
	}

	@Override
	public void close() throws IOException {
		try {
			synchronized (this) {
				if (_presigner != null) {
					_presigner.close();
					_presigner = null;
				}
			}
			_client.close();
		} finally {
			if (_credentialsProvider instanceof SdkAutoCloseable) {
				((SdkAutoCloseable) _credentialsProvider).close();
			}
		}
	}

	private IOException ioException(String message, SdkException ex) {
		return new IOException(message + " (S3 blob store '" + getName() + "'): " + ex.getMessage(), ex);
	}

}
