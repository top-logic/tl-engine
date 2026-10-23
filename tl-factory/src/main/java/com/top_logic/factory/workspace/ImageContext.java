/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.factory.workspace;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Pattern;

/**
 * The files an image is {@link WorkspaceProvider#buildImage(String, ImageContext, ExecOutput)
 * built} from: a {@link #DOCKERFILE} and the files it copies into the image.
 *
 * <p>
 * The context identifies its content by a {@link #contentHash() hash}, which a provider uses as tag
 * of the image: an unchanged context reuses the image built before, a changed one leads to a new
 * image.
 * </p>
 */
public final class ImageContext {

	/**
	 * Path of the file in the context describing how to build the image.
	 */
	public static final String DOCKERFILE = "Dockerfile";

	/**
	 * Number of hexadecimal digits of the {@link #contentHash()}.
	 */
	public static final int HASH_LENGTH = 16;

	private static final String HASH_ALGORITHM = "SHA-256";

	private static final char[] HEX_DIGITS = "0123456789abcdef".toCharArray();

	/**
	 * Syntax of a path in the context: relative, separated by {@code /}, without empty, {@code .}
	 * or {@code ..} segments.
	 */
	private static final Pattern PATH_PATTERN =
		Pattern.compile("(?!\\.{1,2}(?:/|$))[A-Za-z0-9_.-]+(?:/(?!\\.{1,2}(?:/|$))[A-Za-z0-9_.-]+)*");

	private final SortedMap<String, byte[]> _files = new TreeMap<>();

	/**
	 * Adds a file to the context.
	 *
	 * @param path
	 *        The path of the file relative to the root of the context, segments separated by
	 *        {@code /}.
	 * @param content
	 *        The content of the file.
	 * @return This instance for chaining.
	 */
	public ImageContext add(String path, byte[] content) {
		if (!PATH_PATTERN.matcher(path).matches()) {
			throw new IllegalArgumentException("Invalid path in image context: '" + path + "'");
		}
		_files.put(path, content.clone());
		return this;
	}

	/**
	 * Adds a text file to the context, encoded as UTF-8.
	 *
	 * @see #add(String, byte[])
	 */
	public ImageContext add(String path, String content) {
		return add(path, content.getBytes(StandardCharsets.UTF_8));
	}

	/**
	 * The files of the context by their paths, in the order of the paths.
	 */
	public Map<String, byte[]> getFiles() {
		return Collections.unmodifiableMap(_files);
	}

	/**
	 * Hash over the paths and contents of all files, {@link #HASH_LENGTH} hexadecimal digits.
	 *
	 * <p>
	 * The hash does not depend on the order in which files are added.
	 * </p>
	 */
	public String contentHash() {
		MessageDigest digest;
		try {
			digest = MessageDigest.getInstance(HASH_ALGORITHM);
		} catch (NoSuchAlgorithmException ex) {
			throw new IllegalStateException("Hash algorithm not available: " + HASH_ALGORITHM, ex);
		}
		for (Map.Entry<String, byte[]> file : _files.entrySet()) {
			byte[] path = file.getKey().getBytes(StandardCharsets.UTF_8);
			byte[] content = file.getValue();
			updateLength(digest, path.length);
			digest.update(path);
			updateLength(digest, content.length);
			digest.update(content);
		}
		byte[] hash = digest.digest();
		StringBuilder result = new StringBuilder(HASH_LENGTH);
		for (int n = 0; result.length() < HASH_LENGTH; n++) {
			result.append(HEX_DIGITS[(hash[n] >> 4) & 0xF]);
			result.append(HEX_DIGITS[hash[n] & 0xF]);
		}
		return result.toString();
	}

	private static void updateLength(MessageDigest digest, int length) {
		digest.update((byte) (length >>> 24));
		digest.update((byte) (length >>> 16));
		digest.update((byte) (length >>> 8));
		digest.update((byte) length);
	}

	@Override
	public String toString() {
		return "Image context " + _files.keySet();
	}

}
