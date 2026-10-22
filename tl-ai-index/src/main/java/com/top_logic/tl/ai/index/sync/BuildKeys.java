/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.index.sync;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.col.CloseableIterator;
import com.top_logic.element.meta.MetaElementUtil;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.tl.ai.core.security.ReadFilter;
import com.top_logic.tl.ai.index.config.IndexedTypes;

/**
 * The list of objects a full build of a generation works through, kept in a file of the generation.
 *
 * <p>
 * The build writes the keys of all indexed objects once, at the revision it starts from, and then
 * works through them in batches; the byte offset it has reached is part of each commit. So the
 * worker never holds more than one batch of objects, and a build interrupted by a stop goes on where
 * it stopped instead of starting over.
 * </p>
 */
final class BuildKeys {

	/** The file name in the generation's directory. */
	static final String FILE = "build-keys.txt";

	/**
	 * A batch of keys read from the file.
	 *
	 * @param ids
	 *        The object identifiers.
	 * @param next
	 *        The offset to read the next batch from.
	 * @param end
	 *        Whether the file is read to its end.
	 */
	record Batch(List<String> ids, long next, boolean end) {
		// A plain carrier.
	}

	private BuildKeys() {
		// Static utility.
	}

	/**
	 * Writes the identifiers of all objects of the indexed classes.
	 *
	 * @return How many were written.
	 */
	static long write(Path file, IndexedTypes types) throws IOException {
		long count = 0;
		try (BufferedWriter out = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
			for (TLClass type : types.concreteClasses()) {
				try (CloseableIterator<TLObject> objects = MetaElementUtil.iterateDirectInstances(type, TLObject.class)) {
					while (objects.hasNext()) {
						String id = ReadFilter.idOf(objects.next());
						if (id != null) {
							out.write(id);
							out.write('\n');
							count++;
						}
					}
				}
			}
		}
		return count;
	}

	/**
	 * Reads at most {@code max} identifiers from the given offset.
	 */
	static Batch read(Path file, long offset, int max) throws IOException {
		List<String> ids = new ArrayList<>(max);
		try (RandomAccessFile in = new RandomAccessFile(file.toFile(), "r")) {
			in.seek(offset);
			while (ids.size() < max) {
				String line = readLine(in);
				if (line == null) {
					return new Batch(ids, in.getFilePointer(), true);
				}
				if (!line.isEmpty()) {
					ids.add(line);
				}
			}
			return new Batch(ids, in.getFilePointer(), in.getFilePointer() >= in.length());
		}
	}

	/**
	 * The next line in UTF-8, as the file is written; {@link RandomAccessFile#readLine()} reads each
	 * byte as a character.
	 */
	private static String readLine(RandomAccessFile in) throws IOException {
		ByteArrayOutputStream line = new ByteArrayOutputStream();
		int b = in.read();
		if (b < 0) {
			return null;
		}
		while (b >= 0 && b != '\n') {
			line.write(b);
			b = in.read();
		}
		return line.toString(StandardCharsets.UTF_8);
	}

}
