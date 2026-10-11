/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.views;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.top_logic.basic.FileManager;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.io.Content;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.io.StreamUtilities;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.layout.view.ViewLoader;

/**
 * The view files of the running application, and which of them it can write.
 *
 * <p>
 * A view is assembled from every module's copy of its path, the lowest module's first (see
 * {@link ViewLoader#loadConfig(String)}). The copy a change writes is the application's own: the
 * file the workspace holds for that path, as the engine's view designer writes it. A view that only
 * a module jar holds cannot be changed here.
 * </p>
 */
public final class ViewFiles {

	private static final String SUFFIX = ".view.xml";

	private ViewFiles() {
		// Static utility.
	}

	/**
	 * The resource path of a view the caller named.
	 *
	 * @param named
	 *        A path relative to {@link ViewLoader#VIEW_BASE_PATH}, or that prefix included.
	 * @return The path {@link ViewLoader} takes.
	 * @throws IllegalArgumentException
	 *         When the path is empty, leaves the views directory, or is not a view file.
	 */
	public static String resource(String named) {
		String path = named == null ? "" : named.strip().replace('\\', '/');
		if (path.isEmpty()) {
			throw new IllegalArgumentException("Name the view, for example my-app/orders.view.xml.");
		}
		if (path.contains("..") || path.contains("//")) {
			throw new IllegalArgumentException("A view path cannot contain '..' or an empty segment.");
		}
		if (!path.endsWith(SUFFIX)) {
			throw new IllegalArgumentException("A view file ends in " + SUFFIX + ".");
		}
		return ViewLoader.fullPath(path);
	}

	/** The path as it is written in a {@code <view-ref>}: relative to the views directory. */
	public static String relative(String resource) {
		return ViewLoader.viewRef(resource);
	}

	/**
	 * Every view of the application, relative to the views directory, sorted.
	 */
	public static List<String> list() {
		List<String> found = new ArrayList<>();
		collect(FileManager.getInstance(), ViewLoader.VIEW_BASE_PATH, found);
		Collections.sort(found);
		return found;
	}

	private static void collect(FileManager files, String dir, List<String> found) {
		for (String resource : files.getResourcePaths(dir)) {
			String path = resource.startsWith("/") ? resource : "/" + resource;
			if (files.isDirectory(path)) {
				collect(files, path.endsWith("/") ? path : path + "/", found);
			} else if (path.endsWith(SUFFIX)) {
				found.add(relative(path));
			}
		}
	}

	/** Whether any module has a copy of the view. */
	private static boolean exists(String resource) {
		return !copies(resource).isEmpty();
	}

	/**
	 * The file a change of the view writes: the workspace's copy, or where a new view is created.
	 *
	 * @return The file, or <code>null</code> when the view exists only in a module jar.
	 */
	public static File file(String resource) {
		File file = FileManager.getInstance().getIDEFileOrNull(resource);
		if (file != null) {
			return file;
		}
		return exists(resource) ? null : FileManager.getInstance().getIDEFile(resource);
	}

	/**
	 * The text a change of the view starts from: the workspace copy; for a view only a jar holds,
	 * its most derived copy, to read.
	 *
	 * @return The text, or <code>null</code> when there is no such view.
	 */
	public static String read(String resource) {
		File file = FileManager.getInstance().getIDEFileOrNull(resource);
		if (file != null) {
			return readFile(file);
		}
		List<BinaryData> copies = copies(resource);
		return copies.isEmpty() ? null : text(copies.get(0));
	}

	/**
	 * The sources the engine assembles the view from, with the workspace copy replaced by a text.
	 *
	 * @param text
	 *        The candidate text of the workspace copy.
	 * @return The sources, base first, as {@link ViewLoader#parseConfig(List)} takes them.
	 */
	public static List<Content> sourcesWith(String resource, String text) {
		List<BinaryData> copies = copies(resource);
		File file = FileManager.getInstance().getIDEFileOrNull(resource);
		String own = file == null ? null : readFile(file);
		List<Content> sources = new ArrayList<>(copies);
		int replaced = -1;
		for (int n = 0; n < copies.size() && own != null; n++) {
			if (own.equals(text(copies.get(n)))) {
				replaced = n;
				break;
			}
		}
		Content candidate = CharacterContents.newContent(text, resource);
		if (replaced < 0) {
			sources.add(0, candidate);
		} else {
			sources.set(replaced, candidate);
		}
		// The file manager answers the most derived copy first; the loader wants the base first.
		Collections.reverse(sources);
		return sources;
	}

	/**
	 * Writes the workspace copy of a view.
	 *
	 * @param text
	 *        The new text, <code>null</code> to delete the file.
	 * @throws IllegalArgumentException
	 *         When the view exists only in a module jar.
	 */
	public static void write(String resource, String text) {
		File file = writableFile(resource);
		try {
			if (text == null) {
				Files.deleteIfExists(file.toPath());
			} else {
				Files.createDirectories(file.getParentFile().toPath());
				Files.writeString(file.toPath(), text, StandardCharsets.UTF_8);
			}
		} catch (IOException ex) {
			throw new UncheckedIOException("Could not write " + relative(resource) + ": " + ex.getMessage(), ex);
		}
	}

	/**
	 * The file a change of the view writes, see {@link #file(String)}.
	 *
	 * @throws IllegalArgumentException
	 *         When the view exists only in a module jar.
	 */
	public static File writableFile(String resource) {
		File file = file(resource);
		if (file == null) {
			throw new IllegalArgumentException(relative(resource)
				+ " comes from a module jar, not from this application's workspace, so it cannot be changed here.");
		}
		return file;
	}

	private static List<BinaryData> copies(String resource) {
		try {
			return FileManager.getInstance().getDataOverlays(resource);
		} catch (IOException ex) {
			throw new UncheckedIOException(ex);
		}
	}

	private static String readFile(File file) {
		try {
			return FileUtilities.readFileToString(file, StandardCharsets.UTF_8);
		} catch (IOException ex) {
			throw new UncheckedIOException(ex);
		}
	}

	private static String text(BinaryData data) {
		try {
			return StreamUtilities.readAllFromStream(data, StringServices.UTF8);
		} catch (IOException ex) {
			throw new UncheckedIOException(ex);
		}
	}
}
