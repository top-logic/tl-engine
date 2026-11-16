/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.oauth.server;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;

import org.apache.hc.client5.http.DnsResolver;
import org.apache.hc.client5.http.SystemDefaultDnsResolver;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.util.Timeout;

import com.nimbusds.oauth2.sdk.OAuth2Error;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.DateUtil;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.basic.config.annotation.defaults.LongDefault;
import com.top_logic.basic.config.format.MillisFormat;
import com.top_logic.basic.util.ResKey;

/**
 * {@link ClientMetadataFetcher} retrieving the document by an HTTP <code>GET</code>.
 *
 * <p>
 * The URL comes from an unauthenticated request, so fetching it lets anyone make the application
 * send a request. This fetcher therefore:
 * </p>
 * <ul>
 * <li>connects only to addresses of the public internet, unless
 * {@link Config#getAllowPrivateNetwork()} is set: the host is resolved once, refused if one of its
 * addresses is a loopback, link-local, private or other non-public address, and the connection
 * goes to exactly the checked addresses,</li>
 * <li>follows no redirect and uses no proxy,</li>
 * <li>gives up after {@link Config#getTimeout()},</li>
 * <li>reads at most {@link Config#getMaxSize()} bytes.</li>
 * </ul>
 *
 * @see PublicAddressDnsResolver
 */
public class HttpClientMetadataFetcher extends AbstractConfiguredInstance<HttpClientMetadataFetcher.Config<?>>
		implements ClientMetadataFetcher {

	/** The media type of a client ID metadata document. */
	public static final String JSON_MEDIA_TYPE = "application/json";

	/**
	 * Configuration options for {@link HttpClientMetadataFetcher}.
	 */
	public interface Config<I extends HttpClientMetadataFetcher> extends PolymorphicConfiguration<I> {

		/**
		 * Configuration name of {@link #getTimeout()}.
		 */
		String TIMEOUT = "timeout";

		/**
		 * Configuration name of {@link #getMaxSize()}.
		 */
		String MAX_SIZE = "max-size";

		/**
		 * Configuration name of {@link #getAllowPrivateNetwork()}.
		 */
		String ALLOW_PRIVATE_NETWORK = "allow-private-network";

		/**
		 * How long connecting and reading the document may take in all.
		 *
		 * <p>
		 * Connecting and each read are limited to this time; a document that has not arrived
		 * completely when it has passed is abandoned.
		 * </p>
		 */
		@Name(TIMEOUT)
		@Format(MillisFormat.class)
		@LongDefault(5 * DateUtil.SECOND_MILLIS)
		long getTimeout();

		/**
		 * The largest document in bytes that is accepted.
		 */
		@Name(MAX_SIZE)
		@IntDefault(5 * 1024)
		int getMaxSize();

		/**
		 * Whether a document may be fetched from a host in a private network, the loopback
		 * interface included.
		 *
		 * <p>
		 * Off by default: a client ID is chosen by whoever sends an authorization request, and with
		 * this option on, it can make the application send requests into its own network.
		 * </p>
		 */
		@Name(ALLOW_PRIVATE_NETWORK)
		@BooleanDefault(false)
		boolean getAllowPrivateNetwork();

	}

	private final DnsResolver _resolver;

	/**
	 * Creates a {@link HttpClientMetadataFetcher} from configuration.
	 */
	@CalledByReflection
	public HttpClientMetadataFetcher(InstantiationContext context, Config<?> config) {
		super(context, config);
		_resolver = config.getAllowPrivateNetwork() ? SystemDefaultDnsResolver.INSTANCE
			: new PublicAddressDnsResolver(SystemDefaultDnsResolver.INSTANCE);
	}

	/**
	 * The outcome of one request, read within the time limit.
	 *
	 * @param status
	 *        The HTTP status.
	 * @param content
	 *        The body, at most one byte more than the size limit; <code>null</code> if the time
	 *        limit passed while reading it.
	 */
	private record Fetched(int status, byte[] content) {
		// Pure value.
	}

	@Override
	public String fetch(URI documentUrl) throws OAuthException {
		Config<?> config = getConfig();
		Timeout timeout = Timeout.ofMilliseconds(config.getTimeout());
		int maxSize = config.getMaxSize();
		long deadline = System.currentTimeMillis() + config.getTimeout();

		HttpGet request = new HttpGet(documentUrl);
		request.setHeader(HttpHeaders.ACCEPT, JSON_MEDIA_TYPE);
		Fetched fetched;
		try (CloseableHttpClient client = createClient(timeout)) {
			fetched = client.execute(request, response -> {
				HttpEntity entity = response.getEntity();
				if (response.getCode() != HttpStatus.SC_OK || entity == null) {
					return new Fetched(response.getCode(), new byte[0]);
				}
				try (InputStream in = entity.getContent()) {
					return new Fetched(HttpStatus.SC_OK, read(in, maxSize + 1, deadline));
				}
			});
		} catch (PublicAddressDnsResolver.NonPublicAddressException ex) {
			throw failure(I18NConstants.ERROR_CLIENT_METADATA_PRIVATE_HOST__URL.fill(documentUrl.toString()), ex);
		} catch (IOException ex) {
			throw failure(I18NConstants.ERROR_CLIENT_METADATA_UNREACHABLE__URL_PROBLEM.fill(documentUrl.toString(),
				String.valueOf(ex.getMessage())), ex);
		}

		if (fetched.status() != HttpStatus.SC_OK) {
			throw failure(I18NConstants.ERROR_CLIENT_METADATA_STATUS__URL_STATUS.fill(documentUrl.toString(),
				Integer.valueOf(fetched.status())));
		}
		if (fetched.content() == null) {
			throw failure(I18NConstants.ERROR_CLIENT_METADATA_TIMEOUT__URL.fill(documentUrl.toString()));
		}
		if (fetched.content().length > maxSize) {
			throw failure(I18NConstants.ERROR_CLIENT_METADATA_TOO_LARGE__URL_MAX.fill(documentUrl.toString(),
				Integer.valueOf(maxSize)));
		}
		return new String(fetched.content(), StandardCharsets.UTF_8);
	}

	private CloseableHttpClient createClient(Timeout timeout) {
		return HttpClients.custom()
			.setConnectionManager(PoolingHttpClientConnectionManagerBuilder.create()
				.setDnsResolver(_resolver)
				.setDefaultConnectionConfig(ConnectionConfig.custom()
					.setConnectTimeout(timeout)
					.setSocketTimeout(timeout)
					.build())
				.build())
			.setDefaultRequestConfig(RequestConfig.custom()
				.setConnectionRequestTimeout(timeout)
				.setResponseTimeout(timeout)
				.setRedirectsEnabled(false)
				.build())
			.disableRedirectHandling()
			.disableAutomaticRetries()
			.disableCookieManagement()
			.build();
	}

	/**
	 * Reads at most {@code limit} bytes from {@code in}.
	 *
	 * @return The bytes, or <code>null</code> if the deadline passed before the end was reached.
	 */
	private static byte[] read(InputStream in, int limit, long deadline) throws IOException {
		ByteArrayOutputStream result = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		while (result.size() < limit) {
			if (System.currentTimeMillis() > deadline) {
				return null;
			}
			int read = in.read(buffer, 0, Math.min(buffer.length, limit - result.size()));
			if (read < 0) {
				break;
			}
			result.write(buffer, 0, read);
		}
		return result.toByteArray();
	}

	private static OAuthException failure(ResKey reason) {
		return new OAuthException(OAuth2Error.INVALID_CLIENT, reason);
	}

	private static OAuthException failure(ResKey reason, Throwable cause) {
		return new OAuthException(OAuth2Error.INVALID_CLIENT, reason, cause);
	}

}
