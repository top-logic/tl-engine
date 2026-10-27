/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.factory.git;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Owner and name of a repository on a git hosting service, e.g. {@code my-org} and {@code my-app}.
 *
 * @see #parse(String, String)
 */
public final class RepositoryName {

	/**
	 * Separator of owner and name in a {@link #getFullName() full name}.
	 */
	public static final char SEPARATOR = '/';

	/**
	 * Suffix of the path of a clone URL.
	 */
	public static final String GIT_SUFFIX = ".git";

	/**
	 * A full name without scheme and host: {@code owner/name}, optionally followed by
	 * {@link #GIT_SUFFIX}.
	 */
	private static final Pattern FULL_NAME = Pattern.compile("([^/:@\\s]+)/([^/:@\\s]+)");

	/**
	 * The SCP-like address of SSH: {@code user@host:owner/name.git}.
	 */
	private static final Pattern SCP_ADDRESS = Pattern.compile("[^@/\\s]+@([^:/\\s]+):/?(.*)");

	private static final String HTTP = "http";

	private static final String HTTPS = "https";

	private static final String SSH = "ssh";

	private final String _owner;

	private final String _name;

	/**
	 * Creates a {@link RepositoryName}.
	 *
	 * @param owner
	 *        See {@link #getOwner()}.
	 * @param name
	 *        See {@link #getName()}.
	 */
	public RepositoryName(String owner, String name) {
		_owner = Objects.requireNonNull(owner);
		_name = Objects.requireNonNull(name);
	}

	/**
	 * The user or organization owning the repository.
	 */
	public String getOwner() {
		return _owner;
	}

	/**
	 * The name of the repository within its {@link #getOwner() owner}.
	 */
	public String getName() {
		return _name;
	}

	/**
	 * Owner and name separated by {@link #SEPARATOR}.
	 */
	public String getFullName() {
		return _owner + SEPARATOR + _name;
	}

	/**
	 * The repository of the given {@link #getFullName() full name}: {@code owner/name}, optionally
	 * followed by {@link #GIT_SUFFIX}.
	 *
	 * @return The repository, {@code null} if the given value is no full name.
	 */
	public static RepositoryName fromFullName(String fullName) {
		if (fullName == null) {
			return null;
		}
		String input = fullName.trim();
		return FULL_NAME.matcher(input).matches() ? fromSegments(input) : null;
	}

	/**
	 * Whether the given repository has the same owner and name as this one, compared without
	 * regard to case, as the hosting services do.
	 */
	public boolean matches(RepositoryName other) {
		return other != null && _owner.equalsIgnoreCase(other._owner) && _name.equalsIgnoreCase(other._name);
	}

	/**
	 * Reads the repository a user names, as he enters it.
	 *
	 * <p>
	 * Accepted are
	 * </p>
	 * <ul>
	 * <li>the full name {@code owner/name},</li>
	 * <li>an HTTP(S) URL below the given base URL whose path starts with owner and name: the clone
	 * URL ({@code https://git.example.com/owner/name.git}) or the URL of a page of the repository
	 * ({@code https://git.example.com/owner/name/src/branch/main}); scheme and port are not
	 * compared,</li>
	 * <li>an SSH clone URL of the host of the base URL ({@code ssh://git@git.example.com:2222/owner/name.git}
	 * or {@code git@git.example.com:owner/name.git}).</li>
	 * </ul>
	 *
	 * <p>
	 * A trailing {@link #GIT_SUFFIX} of the name is removed; surrounding white space is ignored.
	 * </p>
	 *
	 * @param reference
	 *        What the user entered.
	 * @param baseUrl
	 *        The URL of the hosting service, e.g. {@code https://git.example.com/}; a path is the
	 *        path below which the service is served.
	 * @return The repository, {@code null} if the reference names no repository of the hosting
	 *         service of the given base URL.
	 */
	public static RepositoryName parse(String reference, String baseUrl) {
		if (reference == null) {
			return null;
		}
		String input = reference.trim();
		if (input.isEmpty()) {
			return null;
		}

		Matcher fullName = FULL_NAME.matcher(input);
		if (fullName.matches()) {
			return fromSegments(input);
		}

		URI base = toUri(baseUrl);
		if (base == null || base.getHost() == null) {
			return null;
		}

		Matcher scp = SCP_ADDRESS.matcher(input);
		if (!input.contains("://") && scp.matches()) {
			if (!sameHost(scp.group(1), base)) {
				return null;
			}
			return fromSegments(scp.group(2));
		}

		URI uri = toUri(input);
		if (uri == null || uri.getScheme() == null || uri.getHost() == null || !sameHost(uri.getHost(), base)) {
			return null;
		}
		String path = uri.getPath();
		if (path == null) {
			return null;
		}
		String scheme = uri.getScheme().toLowerCase(Locale.ROOT);
		switch (scheme) {
			case HTTP:
			case HTTPS: {
				String basePath = trimSlashes(base.getPath() == null ? "" : base.getPath());
				String relative = trimSlashes(path);
				if (!basePath.isEmpty()) {
					if (!relative.startsWith(basePath + SEPARATOR)) {
						return null;
					}
					relative = relative.substring(basePath.length() + 1);
				}
				return fromSegments(relative);
			}
			case SSH:
				return fromSegments(path);
			default:
				return null;
		}
	}

	/**
	 * The repository named by the first two segments of the given path.
	 */
	private static RepositoryName fromSegments(String path) {
		String[] segments = trimSlashes(path).split("/");
		if (segments.length < 2) {
			return null;
		}
		String owner = segments[0];
		String name = segments[1];
		if (segments.length == 2 && name.endsWith(GIT_SUFFIX)) {
			name = name.substring(0, name.length() - GIT_SUFFIX.length());
		}
		if (owner.isEmpty() || name.isEmpty()) {
			return null;
		}
		return new RepositoryName(owner, name);
	}

	private static boolean sameHost(String host, URI base) {
		return host.equalsIgnoreCase(base.getHost());
	}

	private static URI toUri(String value) {
		if (value == null) {
			return null;
		}
		try {
			return new URI(value.trim());
		} catch (URISyntaxException ex) {
			return null;
		}
	}

	private static String trimSlashes(String path) {
		int start = 0;
		int end = path.length();
		while (start < end && path.charAt(start) == SEPARATOR) {
			start++;
		}
		while (end > start && path.charAt(end - 1) == SEPARATOR) {
			end--;
		}
		return path.substring(start, end);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof RepositoryName)) {
			return false;
		}
		RepositoryName other = (RepositoryName) obj;
		return _owner.equals(other._owner) && _name.equals(other._name);
	}

	@Override
	public int hashCode() {
		return Objects.hash(_owner, _name);
	}

	@Override
	public String toString() {
		return getFullName();
	}

}
