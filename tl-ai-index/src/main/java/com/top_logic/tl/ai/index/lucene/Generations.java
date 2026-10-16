/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.index.lucene;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * The generations of one index: {@code <root>/gen-<n>/}, one Lucene directory each.
 *
 * <p>
 * The index is derived data. A change of the schema, the analysis, the chunking, the type
 * configuration or the embedding model builds a new generation beside the one that serves, and the
 * service switches when the new one has caught up with the history. Each generation records the
 * {@link #fingerprint(String...) fingerprint} of the configuration it was built with in its commit
 * data.
 * </p>
 */
public final class Generations {

	private static final Pattern NAME = Pattern.compile("gen-(\\d+)");

	private final Path _root;

	/**
	 * Creates the generations below the given directory.
	 */
	public Generations(Path root) {
		_root = root;
	}

	/**
	 * The numbers of the existing generations, oldest first.
	 */
	public List<Integer> numbers() {
		if (!Files.isDirectory(_root)) {
			return List.of();
		}
		List<Integer> result = new ArrayList<>();
		try (Stream<Path> children = Files.list(_root)) {
			children.forEach(child -> {
				Matcher matcher = NAME.matcher(child.getFileName().toString());
				if (matcher.matches() && Files.isDirectory(child)) {
					result.add(Integer.valueOf(matcher.group(1)));
				}
			});
		} catch (IOException ex) {
			throw new UncheckedIOException(ex);
		}
		result.sort(Comparator.naturalOrder());
		return result;
	}

	/**
	 * The directory of the given generation.
	 */
	public Path path(int number) {
		return _root.resolve("gen-" + number);
	}

	/**
	 * The number a new generation gets.
	 */
	public int next() {
		List<Integer> numbers = numbers();
		return numbers.isEmpty() ? 1 : numbers.get(numbers.size() - 1) + 1;
	}

	/**
	 * Deletes the directory of the given generation with everything in it.
	 */
	public void delete(int number) throws IOException {
		Path path = path(number);
		if (!Files.exists(path)) {
			return;
		}
		try (Stream<Path> files = Files.walk(path)) {
			for (Path file : files.sorted(Comparator.reverseOrder()).toList()) {
				Files.delete(file);
			}
		}
	}

	/**
	 * The fingerprint of a configuration: a hash over the schema version, the Lucene major version and
	 * the given parts.
	 *
	 * @param parts
	 *        Everything else the content of the index depends on, each in a stable text form.
	 */
	public static String fingerprint(String... parts) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			digest.update(("schema=" + IndexSchema.SCHEMA_VERSION + "\nlucene=" + IndexSchema.luceneMajor())
				.getBytes(StandardCharsets.UTF_8));
			for (String part : parts) {
				digest.update((byte) '\n');
				digest.update(String.valueOf(part).getBytes(StandardCharsets.UTF_8));
			}
			return HexFormat.of().formatHex(digest.digest());
		} catch (NoSuchAlgorithmException ex) {
			throw new IllegalStateException(ex);
		}
	}

}
