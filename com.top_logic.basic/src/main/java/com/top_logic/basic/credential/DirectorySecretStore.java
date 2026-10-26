/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.credential;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.exception.I18NRuntimeException;
import com.top_logic.basic.util.ResKey;

/**
 * {@link SecretStore} reading each secret from the file named like the secret in a directory,
 * e.g. the Kubernetes or Docker secrets mounted under <code>/run/secrets</code>.
 *
 * <pre>
 * &lt;store class="com.top_logic.basic.credential.DirectorySecretStore" directory="/run/secrets"/&gt;
 * </pre>
 *
 * <p>
 * The secret <code>db_password</code> is then read from the file
 * <code>/run/secrets/db_password</code>. The file is read like the file of a
 * {@link FileSecretSource}: as UTF-8 text without surrounding white space, cached and read again
 * when the file has been replaced. A missing file means that the store does not hold the secret.
 * </p>
 */
@Label("Secret directory")
public class DirectorySecretStore extends AbstractSecretStore<DirectorySecretStore.Config> {

	/**
	 * Configuration options for {@link DirectorySecretStore}.
	 */
	@TagName(Config.TAG_NAME)
	public interface Config extends AbstractSecretStore.Config<DirectorySecretStore> {

		/**
		 * Tag name of this configuration in a list of {@link SecretStore}s.
		 */
		String TAG_NAME = "directory-store";

		/**
		 * @see #getDirectory()
		 */
		String DIRECTORY = "directory";

		/**
		 * Absolute path of the directory holding one file per secret.
		 *
		 * <p>
		 * Like any configuration value, the path may contain aliases, e.g.
		 * <code>%SECRETS_DIR%</code>.
		 * </p>
		 */
		@Name(DIRECTORY)
		@Mandatory
		String getDirectory();

		/**
		 * @see #getDirectory()
		 */
		void setDirectory(String value);

	}

	private final Path _directory;

	private final ConcurrentMap<String, SecretFile> _files = new ConcurrentHashMap<>();

	/**
	 * Creates a {@link DirectorySecretStore} from configuration.
	 *
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	public DirectorySecretStore(InstantiationContext context, Config config) {
		super(context, config);
		_directory = Paths.get(config.getDirectory()).toAbsolutePath().normalize();
	}

	@Override
	protected String lookupKey(String key) {
		return file(key).lookup();
	}

	@Override
	protected ResKey missing(String key) {
		return I18NConstants.ERROR_SECRET_FILE_UNREADABLE__PATH.fill(file(key).getPath().toString());
	}

	private SecretFile file(String key) {
		return _files.computeIfAbsent(key, this::createFile);
	}

	private SecretFile createFile(String key) {
		Path path = _directory.resolve(key).normalize();
		if (key.isEmpty() || !_directory.equals(path.getParent())) {
			throw new I18NRuntimeException(I18NConstants.ERROR_SECRET_NAME_INVALID__NAME.fill(key));
		}
		return new SecretFile(path);
	}

}
