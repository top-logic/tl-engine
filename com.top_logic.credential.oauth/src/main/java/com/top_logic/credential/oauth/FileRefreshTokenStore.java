/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.credential.oauth;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.LinkedHashMap;
import java.util.Map;

import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.exception.I18NRuntimeException;
import com.top_logic.basic.json.JSON;
import com.top_logic.basic.util.ResKeyUtil;
import com.top_logic.basic.util.ResourcesModule;

/**
 * {@link RefreshTokenStore} keeping the refresh token of a client in a file readable only by its
 * owner.
 *
 * <pre>
 * &lt;refresh-token-store class="com.top_logic.credential.oauth.FileRefreshTokenStore"/&gt;
 * </pre>
 *
 * <p>
 * Without a configured path, the file is
 * <code>~/.config/toplogic/oauth/&lt;server&gt;__&lt;client&gt;.json</code> in the home directory
 * of the user running the application, where server and client are the identifier of the
 * authorization server and the client identifier, with every character other than letters,
 * digits, dot and hyphen replaced by an underscore. All applications of the user against the same
 * client share the file, so that one login serves all of them.
 * </p>
 *
 * <p>
 * The file is a JSON object holding the token together with the server and client it belongs to.
 * A file whose server or client does not match is ignored and overwritten by the next token. On
 * file systems with POSIX permissions, the file is readable and writable only by its owner (0600),
 * and directories created for it are accessible only by their owner (0700). The file is replaced
 * atomically, so that a concurrent reader never sees a partially written token.
 * </p>
 */
public class FileRefreshTokenStore extends AbstractConfiguredInstance<FileRefreshTokenStore.Config>
		implements RefreshTokenStore {

	/**
	 * Configuration options for {@link FileRefreshTokenStore}.
	 */
	@TagName(Config.TAG_NAME)
	public interface Config extends PolymorphicConfiguration<FileRefreshTokenStore> {

		/**
		 * Tag name of this configuration in a list of {@link RefreshTokenStore}s.
		 */
		String TAG_NAME = "file-refresh-token-store";

		/**
		 * @see #getPath()
		 */
		String PATH = "path";

		/**
		 * Path of the file holding the refresh token.
		 *
		 * <p>
		 * If empty, the file is located in the directory <code>.config/toplogic/oauth</code> of
		 * the user's home directory and named after the authorization server and the client.
		 * </p>
		 */
		@Name(PATH)
		String getPath();

		/**
		 * @see #getPath()
		 */
		void setPath(String value);

	}

	/**
	 * Field of the stored JSON object with the identifier of the authorization server.
	 */
	private static final String SERVER = "server";

	/**
	 * Field of the stored JSON object with the client identifier.
	 */
	private static final String CLIENT_ID = "client_id";

	/**
	 * Field of the stored JSON object with the refresh token.
	 */
	private static final String REFRESH_TOKEN = "refresh_token";

	private static final String TEMP_SUFFIX = ".tmp";

	private static final String FILE_SUFFIX = ".json";

	private static final String NAME_SEPARATOR = "__";

	private static final String POSIX = "posix";

	private static final String OWNER_ONLY_FILE = "rw-------";

	private static final String OWNER_ONLY_DIRECTORY = "rwx------";

	private final String _path;

	/**
	 * Creates a {@link FileRefreshTokenStore} from configuration.
	 *
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	public FileRefreshTokenStore(InstantiationContext context, Config config) {
		super(context, config);
		_path = StringServices.nonEmpty(config.getPath());
	}

	/**
	 * The file holding the refresh token of the given client.
	 *
	 * @param server
	 *        The identifier of the authorization server.
	 * @param clientId
	 *        The identifier of the client at the server.
	 * @return The configured path, or the default path in the user's home directory.
	 */
	public Path getFile(String server, String clientId) {
		if (_path != null) {
			return Paths.get(_path);
		}
		return defaultFile(server, clientId);
	}

	/**
	 * The file holding the refresh token of the given client if no path is configured.
	 *
	 * @param server
	 *        The identifier of the authorization server.
	 * @param clientId
	 *        The identifier of the client at the server.
	 * @return The file in the directory <code>.config/toplogic/oauth</code> of the user's home
	 *         directory.
	 */
	public static Path defaultFile(String server, String clientId) {
		String name = sanitize(stripScheme(server)) + NAME_SEPARATOR + sanitize(clientId) + FILE_SUFFIX;
		return Paths.get(System.getProperty("user.home"), ".config", "toplogic", "oauth", name);
	}

	private static String stripScheme(String url) {
		int index = url.indexOf("://");
		return index < 0 ? url : url.substring(index + 3);
	}

	private static String sanitize(String value) {
		return value.replaceAll("[^A-Za-z0-9.\\-]", "_");
	}

	@Override
	public String load(String server, String clientId) {
		Path file = getFile(server, clientId);
		String content;
		try {
			content = Files.readString(file, StandardCharsets.UTF_8);
		} catch (NoSuchFileException ex) {
			return null;
		} catch (IOException ex) {
			throw unreadable(file, ex);
		}
		Map<?, ?> json = parse(file, content);
		if (!server.equals(json.get(SERVER)) || !clientId.equals(json.get(CLIENT_ID))) {
			Logger.info(
				ResKeyUtil.getTranslation(I18NConstants.REFRESH_TOKEN_FOREIGN__PATH.fill(file.toString()),
					ResourcesModule.getLogLocale()),
				FileRefreshTokenStore.class);
			return null;
		}
		Object token = json.get(REFRESH_TOKEN);
		if (!(token instanceof String) || ((String) token).isEmpty()) {
			throw unreadable(file, null);
		}
		return (String) token;
	}

	/**
	 * Parses the content of the token file.
	 *
	 * <p>
	 * A parse error is reported without its cause, since the message of the cause quotes the input,
	 * which contains the token.
	 * </p>
	 */
	private static Map<?, ?> parse(Path file, String content) {
		Object json;
		try {
			json = JSON.fromString(content);
		} catch (JSON.ParseException | RuntimeException ex) {
			throw unreadable(file, null);
		}
		if (!(json instanceof Map)) {
			throw unreadable(file, null);
		}
		return (Map<?, ?>) json;
	}

	private static I18NRuntimeException unreadable(Path file, IOException cause) {
		return new I18NRuntimeException(I18NConstants.ERROR_REFRESH_TOKEN_UNREADABLE__PATH.fill(file.toString()),
			cause);
	}

	@Override
	public void store(String server, String clientId, String refreshToken) {
		Map<String, Object> json = new LinkedHashMap<>();
		json.put(SERVER, server);
		json.put(CLIENT_ID, clientId);
		json.put(REFRESH_TOKEN, refreshToken);
		Path file = getFile(server, clientId).toAbsolutePath();
		try {
			write(file, JSON.toString(json));
		} catch (IOException ex) {
			throw notWritten(file, ex);
		}
	}

	private static void write(Path file, String content) throws IOException {
		Path dir = file.getParent();
		boolean posix = isPosix(file.getFileSystem());
		Files.createDirectories(dir, permissions(posix, OWNER_ONLY_DIRECTORY));
		Path temp = Files.createTempFile(dir, file.getFileName().toString(), TEMP_SUFFIX,
			permissions(posix, OWNER_ONLY_FILE));
		try {
			Files.writeString(temp, content, StandardCharsets.UTF_8);
			try {
				Files.move(temp, file, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
			} catch (AtomicMoveNotSupportedException ex) {
				Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING);
			}
		} finally {
			Files.deleteIfExists(temp);
		}
	}

	private static boolean isPosix(FileSystem fileSystem) {
		return fileSystem.supportedFileAttributeViews().contains(POSIX);
	}

	private static FileAttribute<?>[] permissions(boolean posix, String permissions) {
		if (!posix) {
			return new FileAttribute<?>[0];
		}
		return new FileAttribute<?>[] {
			PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString(permissions)) };
	}

	@Override
	public void clear(String server, String clientId) {
		Path file = getFile(server, clientId);
		try {
			Files.deleteIfExists(file);
		} catch (IOException ex) {
			throw notWritten(file, ex);
		}
	}

	private static I18NRuntimeException notWritten(Path file, IOException cause) {
		return new I18NRuntimeException(I18NConstants.ERROR_REFRESH_TOKEN_NOT_WRITTEN__PATH.fill(file.toString()),
			cause);
	}

}
