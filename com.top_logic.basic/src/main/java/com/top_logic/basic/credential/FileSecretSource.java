/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.credential;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.Objects;

import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.exception.I18NRuntimeException;

/**
 * {@link SecretSource} reading the secret from a file, e.g. a Kubernetes or Docker secret mounted
 * under <code>/run/secrets</code>.
 *
 * <pre>
 * &lt;api-key class="com.top_logic.basic.credential.FileSecretSource" path="/run/secrets/my-api-key"/&gt;
 * </pre>
 *
 * <p>
 * The file is read as UTF-8 text. Leading and trailing white space, including the line break that
 * tools like <code>echo</code> or an editor append, is not part of the secret. API keys, tokens and
 * generated passwords never start or end with white space, while a stray line break in a secret
 * file is a frequent cause of failing authentication.
 * </p>
 *
 * <p>
 * The content is cached and read again when the file has been replaced. The file is identified by
 * its attributes (file key, last-modified time and size), read through symbolic links. Kubernetes
 * updates a mounted secret by writing the new content to a fresh directory and swapping the
 * <code>..data</code> symbolic link to it: the configured path then resolves to a different file
 * with a different file key (inode) and last-modified time, so the change is detected with a single
 * <code>stat</code> per use instead of reading the content each time.
 * </p>
 */
public class FileSecretSource extends AbstractConfiguredInstance<FileSecretSource.Config> implements SecretSource {

	/**
	 * Configuration options for {@link FileSecretSource}.
	 */
	@TagName(Config.TAG_NAME)
	public interface Config extends PolymorphicConfiguration<FileSecretSource> {

		/**
		 * Tag name of this configuration in a list of {@link SecretSource}s.
		 */
		String TAG_NAME = "file-secret";

		/**
		 * @see #getPath()
		 */
		String PATH = "path";

		/**
		 * Absolute path of the file containing the secret.
		 *
		 * <p>
		 * Like any configuration value, the path may contain aliases, e.g.
		 * <code>%SECRETS_DIR%/my-api-key</code>. An alias takes its value from an environment
		 * variable in its definition in the application configuration:
		 * <code>&lt;entry name="%SECRETS_DIR%" value="${env:SECRETS_DIR}"/&gt;</code>.
		 * </p>
		 */
		@Name(PATH)
		@Mandatory
		String getPath();

		/**
		 * @see #getPath()
		 */
		void setPath(String value);

	}

	private final Path _path;

	private volatile Snapshot _snapshot;

	/**
	 * Creates a {@link FileSecretSource} from configuration.
	 *
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	public FileSecretSource(InstantiationContext context, Config config) {
		super(context, config);
		_path = Paths.get(config.getPath());
	}

	@Override
	public String getSecret() {
		try {
			BasicFileAttributes attributes = Files.readAttributes(_path, BasicFileAttributes.class);
			Snapshot snapshot = _snapshot;
			if (snapshot == null || !snapshot.matches(attributes)) {
				String secret = new String(Files.readAllBytes(_path), StandardCharsets.UTF_8).strip();
				snapshot = new Snapshot(attributes, secret);
				_snapshot = snapshot;
			}
			if (snapshot.getSecret().isEmpty()) {
				throw new I18NRuntimeException(I18NConstants.ERROR_SECRET_FILE_EMPTY__PATH.fill(_path.toString()));
			}
			return snapshot.getSecret();
		} catch (IOException ex) {
			_snapshot = null;
			throw new I18NRuntimeException(I18NConstants.ERROR_SECRET_FILE_UNREADABLE__PATH.fill(_path.toString()),
				ex);
		}
	}

	/**
	 * The content of the secret file together with the attributes of the file it was read from.
	 */
	private static final class Snapshot {

		private final Object _fileKey;

		private final FileTime _lastModified;

		private final long _size;

		private final String _secret;

		Snapshot(BasicFileAttributes attributes, String secret) {
			_fileKey = attributes.fileKey();
			_lastModified = attributes.lastModifiedTime();
			_size = attributes.size();
			_secret = secret;
		}

		/**
		 * Whether the file with the given attributes is the one this snapshot was read from.
		 */
		boolean matches(BasicFileAttributes attributes) {
			return Objects.equals(_fileKey, attributes.fileKey())
				&& _lastModified.equals(attributes.lastModifiedTime())
				&& _size == attributes.size();
		}

		String getSecret() {
			return _secret;
		}

	}

}
