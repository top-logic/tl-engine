/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.resource;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import com.top_logic.basic.FileManager;
import com.top_logic.basic.Log;
import com.top_logic.basic.LogProtocol;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.io.FileCompiler;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.layout.servlet.CacheControlFilter;

/**
 * Default {@link ResourceResolver}.
 *
 * <p>
 * A context-relative resource is emitted with a content version: the query parameter
 * {@link CacheControlFilter#VERSION_PARAMETER} carrying the first {@value #VERSION_LENGTH}
 * hexadecimal digits of the SHA-256 hash of the resource content, read through the
 * {@link FileManager}. The URL therefore changes whenever the content changes, which allows the
 * {@link CacheControlFilter} to let the browser cache it as immutable. If the declared resource
 * already has a query string, the parameter is appended to it.
 * </p>
 *
 * <p>
 * The version of a resource is computed once per resolver instance, on first use, so all URLs
 * emitted for a resource through the same instance are identical. A resource whose content cannot
 * be read is reported once and emitted without version.
 * </p>
 *
 * <p>
 * A {@code webjar:} reference is resolved to its served path, which already contains the webjar
 * version. An absolute URL (with a scheme or starting with {@code //}) is used verbatim.
 * </p>
 */
public class DefaultResourceResolver implements ResourceResolver {

	/**
	 * Number of hexadecimal digits of the content hash used as version.
	 */
	public static final int VERSION_LENGTH = 12;

	private static final Log LOG = new LogProtocol(DefaultResourceResolver.class);

	private static final String WEBJAR_PREFIX = "webjar:";

	private static final String HASH_ALGORITHM = "SHA-256";

	/**
	 * Pattern matching an absolute URL: a URI scheme or a protocol-relative <code>//</code> prefix.
	 */
	private static final Pattern ABSOLUTE_URL = Pattern.compile("^([a-zA-Z][a-zA-Z0-9+.-]*:|//)");

	/**
	 * Marker in {@link #_versions} for a resource without version.
	 */
	private static final String NO_VERSION = "";

	/**
	 * Content version by context-relative file path (without query string).
	 */
	private final Map<String, String> _versions = new ConcurrentHashMap<>();

	@Override
	public List<String> resolve(ResourceConfig resource) {
		if (resource instanceof StyleSheetConfig css) {
			return Collections.singletonList(resolvePath(css.getResource()));
		}
		if (resource instanceof ModuleScriptConfig script) {
			return Collections.singletonList(resolvePath(script.getResource()));
		}
		if (resource instanceof ScriptConfig script) {
			return Collections.singletonList(resolvePath(script.getResource()));
		}
		return Collections.emptyList();
	}

	private String resolvePath(String resource) {
		if (resource.startsWith(WEBJAR_PREFIX)) {
			return FileCompiler.resolveResourcePath(LOG, resource);
		}
		if (ABSOLUTE_URL.matcher(resource).lookingAt()) {
			return resource;
		}
		return addVersion(resource);
	}

	private String addVersion(String resource) {
		int queryStart = resource.indexOf('?');
		String path = queryStart < 0 ? resource : resource.substring(0, queryStart);
		String version = _versions.computeIfAbsent(path, DefaultResourceResolver::computeVersion);
		if (version.isEmpty()) {
			return resource;
		}
		char separator = queryStart < 0 ? '?' : '&';
		return resource + separator + CacheControlFilter.VERSION_PARAMETER + '=' + version;
	}

	private static String computeVersion(String path) {
		BinaryData data = FileManager.getInstance().getDataOrNull(path);
		if (data == null) {
			LOG.info("Client resource '" + path + "' not found, emitted without content version.", Log.WARN);
			return NO_VERSION;
		}
		try (InputStream in = data.getStream()) {
			MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);
			byte[] buffer = new byte[8192];
			int length;
			while ((length = in.read(buffer)) >= 0) {
				digest.update(buffer, 0, length);
			}
			return StringServices.toHexString(digest.digest()).substring(0, VERSION_LENGTH);
		} catch (IOException | NoSuchAlgorithmException ex) {
			LOG.info("Client resource '" + path + "' cannot be read, emitted without content version: "
				+ ex.getMessage(), Log.WARN);
			return NO_VERSION;
		}
	}

}
