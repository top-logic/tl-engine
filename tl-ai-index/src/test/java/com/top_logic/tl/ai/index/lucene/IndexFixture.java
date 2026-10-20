/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.index.lucene;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Stream;

/**
 * Temporary index directories and synthetic vectors for the Lucene tests.
 */
public final class IndexFixture {

	private IndexFixture() {
		// Static utility.
	}

	/**
	 * A new empty directory below the build's temporary directory.
	 */
	public static Path tempDir(String prefix) throws IOException {
		Path base = Path.of(System.getProperty("java.io.tmpdir"));
		return Files.createTempDirectory(base, prefix);
	}

	/**
	 * Deletes the given directory with everything in it.
	 */
	public static void deleteRecursively(Path dir) throws IOException {
		if (dir == null || !Files.exists(dir)) {
			return;
		}
		try (Stream<Path> files = Files.walk(dir)) {
			for (Path file : files.sorted(Comparator.reverseOrder()).toList()) {
				Files.delete(file);
			}
		}
	}

	/**
	 * A random vector with independent normal components of the given deviation.
	 */
	public static float[] gauss(Random random, int dimensions, double sigma) {
		float[] result = new float[dimensions];
		for (int i = 0; i < dimensions; i++) {
			result[i] = (float) (random.nextGaussian() * sigma);
		}
		return result;
	}

	/**
	 * The normalized sum of two vectors.
	 */
	public static float[] plus(float[] a, float[] b) {
		float[] result = new float[a.length];
		for (int i = 0; i < a.length; i++) {
			result[i] = a[i] + b[i];
		}
		return Vectors.normalized(result);
	}

	/**
	 * A chunk with the given filter values.
	 */
	public static ChunkDocument chunk(String object, int number, Collection<String> types,
			Collection<String> scopes, String text, float[] vector) {
		return new ChunkDocument(object, "text", new Locator(number, 0, text.length(), 0), types, scopes,
			Map.of(), 1, text, vector == null ? null : "hash-" + text.hashCode(), vector == null ? null : "synthetic",
			vector);
	}

	/**
	 * A chunk without filter values.
	 */
	public static ChunkDocument chunk(String object, String text, float[] vector) {
		return chunk(object, 0, List.of(), List.of(), text, vector);
	}

}
