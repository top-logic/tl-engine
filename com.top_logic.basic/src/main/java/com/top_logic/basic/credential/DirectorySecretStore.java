/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.credential;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermissions;
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
 *
 * <p>
 * Secrets are written to the directory while the application may write it, e.g. the directory
 * <code>~/.top-logic/secrets</code> of a developer or a directory of the installation on a
 * server. A written secret is stored in the file named like the secret. Nothing can be written to
 * a directory the application may not write, e.g. a secret volume mounted read-only into a
 * container. A written file is readable for the owner only, where the file system supports POSIX
 * permissions, and replaces the previous file atomically.
 * </p>
 *
 * @see #getWritableStore()
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
		 * A path starting with <code>~/</code> lies in the home directory of the user running the
		 * application, e.g. <code>~/.top-logic/secrets</code>. Like any configuration value, the
		 * path may contain aliases, e.g. <code>%SECRETS_DIR%</code>.
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

	/**
	 * Prefix of a {@link Config#getDirectory() directory} in the home directory of the user.
	 */
	private static final String HOME = "~";

	/**
	 * Name of the POSIX {@link java.nio.file.attribute.FileAttributeView}.
	 */
	private static final String POSIX_VIEW = "posix";

	/**
	 * Permissions of a written secret file: read and write for the owner only.
	 */
	private static final String OWNER_ONLY = "rw-------";

	/**
	 * Suffix of the temporary file a secret is written to before it replaces the secret file.
	 */
	private static final String TEMP_SUFFIX = ".tmp";

	private final Path _directory;

	private final ConcurrentMap<String, SecretFile> _files = new ConcurrentHashMap<>();

	private final WritableSecretStore _writable = new Writer();

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
		_directory = resolve(config.getDirectory());
	}

	private static Path resolve(String directory) {
		if (directory.equals(HOME) || directory.startsWith(HOME + "/")) {
			return Paths.get(System.getProperty("user.home"), directory.substring(HOME.length()))
				.toAbsolutePath().normalize();
		}
		return Paths.get(directory).toAbsolutePath().normalize();
	}

	@Override
	protected String lookupKey(String key) {
		return file(key).lookup();
	}

	@Override
	protected ResKey missing(String key) {
		return I18NConstants.ERROR_SECRET_FILE_UNREADABLE__PATH.fill(file(key).getPath().toString());
	}

	/**
	 * A view of this store writing the secret files, if the application may currently write the
	 * {@link Config#getDirectory() directory}.
	 *
	 * @return The writable view, <code>null</code> if the directory does not exist or is not
	 *         writable.
	 */
	@Override
	public WritableSecretStore getWritableStore() {
		return isDirectoryWritable() ? _writable : null;
	}

	private boolean isDirectoryWritable() {
		return Files.isDirectory(_directory) && Files.isWritable(_directory);
	}

	private SecretFile file(String key) {
		return _files.computeIfAbsent(key, this::createFile);
	}

	private SecretFile createFile(String key) {
		Path path = path(key);
		if (path == null) {
			throw new I18NRuntimeException(I18NConstants.ERROR_SECRET_NAME_INVALID__NAME.fill(key));
		}
		return new SecretFile(path);
	}

	/**
	 * The file holding the secret with the given key.
	 *
	 * @return The path of the file, <code>null</code> if the key is no valid file name in the
	 *         {@link Config#getDirectory() directory}.
	 */
	private Path path(String key) {
		if (key.isEmpty()) {
			return null;
		}
		Path path = _directory.resolve(key).normalize();
		if (!_directory.equals(path.getParent())) {
			return null;
		}
		return path;
	}

	private boolean canStoreKey(String key) {
		Path path = path(key);
		if (path == null) {
			return false;
		}
		if (Files.exists(path)) {
			return Files.isWritable(path);
		}
		return isDirectoryWritable();
	}

	private void storeKey(String key, String secret) {
		if (secret == null || secret.isBlank()) {
			throw new I18NRuntimeException(I18NConstants.ERROR_SECRET_EMPTY__NAME.fill(key));
		}
		Path path = file(key).getPath();
		if (!canStoreKey(key)) {
			throw new I18NRuntimeException(I18NConstants.ERROR_SECRET_FILE_NOT_WRITABLE__PATH.fill(path.toString()));
		}
		try {
			Path temp = Files.createTempFile(_directory, "." + key + ".", TEMP_SUFFIX, ownerOnly());
			try {
				Files.write(temp, secret.getBytes(StandardCharsets.UTF_8));
				try {
					Files.move(temp, path, StandardCopyOption.ATOMIC_MOVE);
				} catch (AtomicMoveNotSupportedException ex) {
					Files.move(temp, path, StandardCopyOption.REPLACE_EXISTING);
				}
			} finally {
				Files.deleteIfExists(temp);
			}
		} catch (IOException ex) {
			throw new I18NRuntimeException(I18NConstants.ERROR_SECRET_FILE_NOT_WRITABLE__PATH.fill(path.toString()),
				ex);
		} finally {
			_files.remove(key);
		}
	}

	private FileAttribute<?>[] ownerOnly() {
		if (_directory.getFileSystem().supportedFileAttributeViews().contains(POSIX_VIEW)) {
			return new FileAttribute<?>[] {
				PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString(OWNER_ONLY)) };
		}
		return new FileAttribute<?>[0];
	}

	private boolean removeKey(String key) {
		Path path = file(key).getPath();
		try {
			return Files.deleteIfExists(path);
		} catch (IOException ex) {
			throw new I18NRuntimeException(
				I18NConstants.ERROR_SECRET_FILE_NOT_REMOVABLE__PATH.fill(path.toString()), ex);
		} finally {
			_files.remove(key);
		}
	}

	/**
	 * {@link WritableSecretStore} view of the {@link DirectorySecretStore}, writing the file named
	 * like the secret (with the prefix of the store).
	 */
	private final class Writer implements WritableSecretStore {

		@Override
		public String lookup(String name) {
			return DirectorySecretStore.this.lookup(name);
		}

		@Override
		public String getSecret(String name) {
			return DirectorySecretStore.this.getSecret(name);
		}

		/**
		 * Whether the name is a valid file name and the file holding the secret is writable: an
		 * existing file must be writable itself, a missing file needs a writable directory.
		 */
		@Override
		public boolean canStore(String name) {
			return canStoreKey(key(name));
		}

		@Override
		public void store(String name, String secret) {
			storeKey(key(name), secret);
		}

		@Override
		public boolean remove(String name) {
			return removeKey(key(name));
		}

	}

}
