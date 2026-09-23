/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.io.binary.scan;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.basic.config.annotation.defaults.LongDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.util.ResKey;

/**
 * {@link UploadContentChecker} scanning uploaded content with a {@code ClamAV} daemon (<code>clamd</code>).
 *
 * <p>
 * The content is streamed to <code>clamd</code> over a socket using its <code>INSTREAM</code>
 * command, so the file never has to be shared with the daemon through the file system. When the
 * daemon reports a signature match, the upload is rejected. What happens when the daemon cannot be
 * reached is controlled by {@link Config#getUnavailablePolicy()}.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ClamAvScanner implements UploadContentChecker, ConfiguredInstance<ClamAvScanner.Config> {

	/**
	 * The <code>clamd</code> command starting a stream scan. The <code>z</code> prefix selects the
	 * null-terminated command/reply dialect.
	 */
	private static final byte[] COMMAND_INSTREAM = "zINSTREAM\0".getBytes(StandardCharsets.US_ASCII);

	/** Prefix of every <code>clamd</code> stream reply. */
	private static final String REPLY_PREFIX = "stream: ";

	/** Suffix of a <code>clamd</code> reply for content that is clean. */
	private static final String REPLY_OK = "OK";

	/** Suffix of a <code>clamd</code> reply reporting a signature match. */
	private static final String REPLY_FOUND = "FOUND";

	/**
	 * Behavior when the {@code ClamAV} daemon cannot be reached (connection refused, timeout, protocol
	 * error, or content exceeding the scan size limit).
	 */
	public enum UnavailablePolicy {

		/** Reject the upload (fail closed). */
		REJECT,

		/** Accept the upload after logging a warning (fail open). */
		ACCEPT;

	}

	/**
	 * Configuration of the {@link ClamAvScanner}.
	 */
	public interface Config extends PolymorphicConfiguration<ClamAvScanner> {

		/**
		 * The host name of the {@code ClamAV} daemon.
		 */
		@Name("host")
		@StringDefault("localhost")
		String getHost();

		/**
		 * The TCP port the {@code ClamAV} daemon listens on.
		 */
		@Name("port")
		@IntDefault(3310)
		int getPort();

		/**
		 * Timeout in milliseconds for establishing the connection to the daemon.
		 */
		@Name("connect-timeout")
		@LongDefault(5000)
		long getConnectTimeout();

		/**
		 * Timeout in milliseconds for reading the daemon's reply.
		 */
		@Name("read-timeout")
		@LongDefault(30000)
		long getReadTimeout();

		/**
		 * The number of content bytes sent to the daemon per <code>INSTREAM</code> chunk.
		 */
		@Name("chunk-size")
		@IntDefault(8192)
		int getChunkSize();

		/**
		 * The maximum content size in bytes that is scanned, or <code>0</code> for no limit.
		 *
		 * <p>
		 * Content larger than this limit is not sent to the daemon; it is treated like an
		 * unreachable daemon (see {@link #getUnavailablePolicy()}). The limit should not exceed the
		 * daemon's own <code>StreamMaxLength</code> setting.
		 * </p>
		 */
		@Name("max-scan-size")
		@LongDefault(0)
		long getMaxScanSize();

		/**
		 * What to do with an upload that could not be scanned because the daemon was unavailable.
		 */
		@Name("unavailable-policy")
		@FormattedDefault("REJECT")
		UnavailablePolicy getUnavailablePolicy();

	}

	private final Config _config;

	/**
	 * Creates a {@link ClamAvScanner} from configuration.
	 *
	 * @param context
	 *        For error reporting during instantiation.
	 * @param config
	 *        The configuration for the new instance.
	 */
	@CalledByReflection
	public ClamAvScanner(InstantiationContext context, Config config) {
		_config = config;
	}

	@Override
	public Config getConfig() {
		return _config;
	}

	@Override
	public ResKey check(BinaryData data) {
		long maxScanSize = _config.getMaxScanSize();
		if (maxScanSize > 0 && data.getSize() > maxScanSize) {
			return unavailable(data, "content size " + data.getSize() + " exceeds the scan limit " + maxScanSize);
		}

		String reply;
		try {
			reply = scan(data);
		} catch (IOException ex) {
			Logger.error("Virus scan of upload '" + data.getName() + "' failed to contact the ClamAV daemon at "
				+ _config.getHost() + ":" + _config.getPort() + ".", ex, ClamAvScanner.class);
			return unavailable(data, ex.getMessage());
		}

		String result = reply.trim();
		if (result.endsWith(REPLY_OK)) {
			return null;
		}
		if (result.endsWith(REPLY_FOUND)) {
			return I18NConstants.ERROR_UPLOAD_INFECTED__NAME_SIGNATURE.fill(data.getName(), signature(result));
		}

		// Any other reply (e.g. "... ERROR") means the content could not be scanned.
		Logger.error("Virus scan of upload '" + data.getName() + "' returned an error: " + result, ClamAvScanner.class);
		return unavailable(data, result);
	}

	/**
	 * Streams the content to <code>clamd</code> via <code>INSTREAM</code> and returns its reply.
	 */
	private String scan(BinaryData data) throws IOException {
		try (Socket socket = new Socket()) {
			socket.connect(new InetSocketAddress(_config.getHost(), _config.getPort()), (int) _config.getConnectTimeout());
			socket.setSoTimeout((int) _config.getReadTimeout());

			OutputStream out = socket.getOutputStream();
			out.write(COMMAND_INSTREAM);

			byte[] buffer = new byte[_config.getChunkSize()];
			try (InputStream content = data.getStream()) {
				int read;
				while ((read = content.read(buffer)) != -1) {
					if (read == 0) {
						continue;
					}
					out.write(intToNetworkBytes(read));
					out.write(buffer, 0, read);
				}
			}
			// A zero-length chunk terminates the stream.
			out.write(intToNetworkBytes(0));
			out.flush();

			return readReply(socket.getInputStream());
		}
	}

	/**
	 * Reads a single null-terminated <code>clamd</code> reply.
	 */
	private static String readReply(InputStream in) throws IOException {
		ByteArrayOutputStream reply = new ByteArrayOutputStream();
		int b;
		while ((b = in.read()) != -1) {
			if (b == 0) {
				break;
			}
			reply.write(b);
		}
		return reply.toString(StandardCharsets.US_ASCII);
	}

	/**
	 * Extracts the reported signature name from a {@link #REPLY_FOUND} reply of the form
	 * "{@code stream: <signature> FOUND}".
	 */
	private static String signature(String result) {
		String signature = result;
		if (signature.startsWith(REPLY_PREFIX)) {
			signature = signature.substring(REPLY_PREFIX.length());
		}
		if (signature.endsWith(REPLY_FOUND)) {
			signature = signature.substring(0, signature.length() - REPLY_FOUND.length()).trim();
		}
		return signature;
	}

	private ResKey unavailable(BinaryData data, String detail) {
		switch (_config.getUnavailablePolicy()) {
			case ACCEPT:
				Logger.warn("Accepting upload '" + data.getName() + "' without virus scan: " + detail,
					ClamAvScanner.class);
				return null;
			case REJECT:
			default:
				return I18NConstants.ERROR_UPLOAD_SCAN_UNAVAILABLE__NAME.fill(data.getName());
		}
	}

	private static byte[] intToNetworkBytes(int value) {
		return new byte[] {
			(byte) (value >>> 24),
			(byte) (value >>> 16),
			(byte) (value >>> 8),
			(byte) value
		};
	}

}
