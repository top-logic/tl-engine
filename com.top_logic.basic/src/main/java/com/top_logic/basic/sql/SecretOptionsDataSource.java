/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.sql;

import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import javax.sql.DataSource;

import com.top_logic.basic.Logger;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.credential.SecretSource;
import com.top_logic.basic.exception.I18NRuntimeException;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResourcesModule;

/**
 * {@link DataSource} whose driver options contain secrets, e.g. the database password, that are
 * read from their {@link SecretSource}s each time a connection is opened.
 *
 * <p>
 * The underlying {@link DataSource} is created by a {@link Factory} from the plain options
 * together with the current values of the secrets. A secret that its source does not hold, e.g.
 * an unset named secret, leaves its option out, so that the driver behaves as without the option,
 * and is logged as warning each time the underlying {@link DataSource} is created; a secret that
 * is present but cannot be read is an error. When a connection is requested and a secret has
 * changed since, a new underlying {@link DataSource} is created with the current values before the
 * connection is opened. This way a rotated password takes effect for new connections without a
 * restart, connections opened before keep working.
 * </p>
 *
 * <p>
 * The values of the secrets are not kept: the change of a secret is detected by a digest of the
 * values the current underlying {@link DataSource} was created with.
 * </p>
 */
public class SecretOptionsDataSource extends DataSourceProxy {

	/**
	 * Creates the underlying {@link DataSource} from the driver options.
	 */
	@FunctionalInterface
	public interface Factory {

		/**
		 * Creates a {@link DataSource}.
		 *
		 * @param options
		 *        All driver options, the secret options with their current values.
		 * @return The {@link DataSource} to open connections from.
		 */
		DataSource createDataSource(Properties options) throws SQLException, ConfigurationException;

	}

	private static final String DIGEST_ALGORITHM = "SHA-256";

	/** Separator of option name and value and of options in the digest input. */
	private static final char SEPARATOR = '\0';

	private final Properties _options;

	private final Map<String, SecretSource> _secrets;

	private final Factory _factory;

	private volatile Implementation _impl;

	/** Login timeout set through {@link #setLoginTimeout(int)}, <code>null</code> if never set. */
	private Integer _loginTimeout;

	/** Log writer set through {@link #setLogWriter(PrintWriter)}, <code>null</code> if never set. */
	private PrintWriter _logWriter;

	/**
	 * The underlying {@link DataSource} with the digest of the secrets it was created with.
	 */
	private static final class Implementation {

		final DataSource _dataSource;

		final byte[] _secretsDigest;

		Implementation(DataSource dataSource, byte[] secretsDigest) {
			_dataSource = dataSource;
			_secretsDigest = secretsDigest;
		}

	}

	/**
	 * Creates a {@link SecretOptionsDataSource}.
	 *
	 * <p>
	 * The underlying {@link DataSource} is created immediately with the current values of the
	 * secrets.
	 * </p>
	 *
	 * @param options
	 *        The driver options with a plain value.
	 * @param secrets
	 *        The sources of the driver options holding a secret, indexed by option name.
	 * @param factory
	 *        Creates the underlying {@link DataSource} from all options.
	 *
	 * @throws I18NRuntimeException
	 *         If a secret is present but cannot be read.
	 */
	public SecretOptionsDataSource(Properties options, Map<String, SecretSource> secrets, Factory factory)
			throws SQLException, ConfigurationException {
		_options = options;
		_secrets = new LinkedHashMap<>(secrets);
		_factory = factory;
		Map<String, String> values = readSecrets();
		_impl = new Implementation(createDataSource(values), digest(values));
	}

	@Override
	protected DataSource impl() {
		return _impl._dataSource;
	}

	@Override
	public Connection getConnection() throws SQLException {
		return current().getConnection();
	}

	@Override
	public Connection getConnection(String user, String password) throws SQLException {
		return current().getConnection(user, password);
	}

	@Override
	public synchronized void setLoginTimeout(int seconds) throws SQLException {
		impl().setLoginTimeout(seconds);
		_loginTimeout = Integer.valueOf(seconds);
	}

	@Override
	public synchronized void setLogWriter(PrintWriter out) throws SQLException {
		impl().setLogWriter(out);
		_logWriter = out;
	}

	/**
	 * The underlying {@link DataSource} created with the current values of the secrets.
	 */
	private DataSource current() throws SQLException {
		Map<String, String> values;
		try {
			values = readSecrets();
		} catch (I18NRuntimeException ex) {
			throw new SQLException(ex.getMessage(), ex);
		}
		byte[] digest = digest(values);

		Implementation impl = _impl;
		if (!MessageDigest.isEqual(impl._secretsDigest, digest)) {
			synchronized (this) {
				impl = _impl;
				if (!MessageDigest.isEqual(impl._secretsDigest, digest)) {
					DataSource dataSource;
					try {
						dataSource = createDataSource(values);
					} catch (ConfigurationException ex) {
						throw new SQLException("Creating the data source with changed secrets failed.", ex);
					}
					if (_loginTimeout != null) {
						dataSource.setLoginTimeout(_loginTimeout.intValue());
					}
					if (_logWriter != null) {
						dataSource.setLogWriter(_logWriter);
					}
					impl = new Implementation(dataSource, digest);
					_impl = impl;
				}
			}
		}
		return impl._dataSource;
	}

	private DataSource createDataSource(Map<String, String> secretValues) throws SQLException, ConfigurationException {
		logAbsentSecrets(secretValues);
		Properties options = new Properties();
		options.putAll(_options);
		options.putAll(secretValues);
		return _factory.createDataSource(options);
	}

	/**
	 * Logs a warning for each secret option that is left out because its source does not hold the
	 * secret, naming the option and the secret, never a value.
	 */
	private void logAbsentSecrets(Map<String, String> secretValues) {
		for (Entry<String, SecretSource> entry : _secrets.entrySet()) {
			String option = entry.getKey();
			if (!secretValues.containsKey(option)) {
				Logger.warn("Driver option '" + option + "' is left out: its secret is not set ("
					+ describe(entry.getValue().reference()) + ").", SecretOptionsDataSource.class);
			}
		}
	}

	/**
	 * The text of the given description of a secret in the language of the log.
	 */
	private static String describe(ResKey reference) {
		if (reference == null) {
			return "no description";
		}
		ResourcesModule.Module module = ResourcesModule.Module.INSTANCE;
		if (!module.isActive()) {
			return reference.toString();
		}
		return module.getImplementationInstance().getBundle(ResourcesModule.getLogLocale()).getString(reference);
	}

	/**
	 * The current values of the secrets, indexed by option name.
	 *
	 * <p>
	 * A secret its source does not hold (see {@link SecretSource#lookup()}) has no entry, so that
	 * the option is left out of the driver options.
	 * </p>
	 */
	private Map<String, String> readSecrets() {
		Map<String, String> result = new LinkedHashMap<>();
		for (Entry<String, SecretSource> entry : _secrets.entrySet()) {
			String value = entry.getValue().lookup();
			if (value != null) {
				result.put(entry.getKey(), value);
			}
		}
		return result;
	}

	private static byte[] digest(Map<String, String> secretValues) {
		MessageDigest digest;
		try {
			digest = MessageDigest.getInstance(DIGEST_ALGORITHM);
		} catch (NoSuchAlgorithmException ex) {
			throw new IllegalStateException("Digest algorithm not available: " + DIGEST_ALGORITHM, ex);
		}
		for (Entry<String, String> entry : secretValues.entrySet()) {
			digest.update(entry.getKey().getBytes(StandardCharsets.UTF_8));
			digest.update((byte) SEPARATOR);
			digest.update(entry.getValue().getBytes(StandardCharsets.UTF_8));
			digest.update((byte) SEPARATOR);
		}
		return digest.digest();
	}

}
