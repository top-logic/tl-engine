/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout;

import static com.top_logic.layout.processor.LayoutModelConstants.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.io.PathUpdate;

/**
 * Update describing a change to the existing layout.
 * 
 * <p>
 * Can be created by a {@link PathUpdate} by retaining all layout relevant informations.
 * </p>
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
public class LayoutUpdate {

	private boolean _canIncrementalUpdate = true;

	private Collection<String> _layoutKeysToUpdate = new HashSet<>();

	/**
	 * Create an update for the layout from the given general filesystem update.
	 */
	public LayoutUpdate(PathUpdate update) {
		if (_canIncrementalUpdate) {
			visitFileChanges(update);
		}

		if (_canIncrementalUpdate) {
			visitFileDeletions(update);
		}

		if (_canIncrementalUpdate) {
			visitFileCreations(update);
		}
	}

	private void visitFileCreations(PathUpdate update) {
		for (Path path : update.getCreations()) {
			Path layoutDirectory = layoutDirectory(path);
			if (layoutDirectory == null) {
				continue;
			}

			if (Files.isDirectory(path)) {
				try {
					Set<Path> children = Files.walk(path).filter(Files::isRegularFile).collect(Collectors.toSet());

					for (Path child : children) {
						visitFile(layoutDirectory, child);
					}
				} catch (IOException exception) {
					Logger.error("Problem while reading " + path, exception, this);
				}
			} else {
				visitFile(layoutDirectory, path);
			}

			if (!_canIncrementalUpdate) {
				return;
			}
		}
	}

	private void visitFileDeletions(PathUpdate update) {
		for (Path path : update.getDeletions()) {
			Path layoutDirectory = layoutDirectory(path);
			if (layoutDirectory == null) {
				continue;
			}

			/* A deleted path can no longer be inspected, so a directory reaches visitFile(Path, Path)
			 * and is recognized there by its missing file name extension. */
			visitFile(layoutDirectory, path);

			if (!_canIncrementalUpdate) {
				return;
			}
		}
	}

	private void visitFileChanges(PathUpdate update) {
		for (Path path : update.getChanges()) {
			Path layoutDirectory = layoutDirectory(path);
			if (layoutDirectory == null) {
				continue;
			}

			if (!Files.isDirectory(path)) {
				visitFile(layoutDirectory, path);
			}

			if (!_canIncrementalUpdate) {
				return;
			}
		}
	}

	/**
	 * The layout directory containing the given path, or <code>null</code> if the path is not part of
	 * a layout directory.
	 */
	private static Path layoutDirectory(Path path) {
		Path layoutDirectory = LayoutUtils.getLayoutDirectory(path);

		if (layoutDirectory == null || !path.startsWith(layoutDirectory)) {
			return null;
		}

		return layoutDirectory;
	}

	/**
	 * Records what the given path in the given layout directory invalidates.
	 */
	private void visitFile(Path layoutDirectory, Path path) {
		Path filename = path.getFileName();

		if (filename == null) {
			return;
		}

		String name = filename.toString();

		if (LayoutUtils.isLayoutOverlay(name)) {
			invalidate(getLayoutKeyFromOverlay(layoutDirectory, path));
		} else if (LayoutUtils.isLayout(name)) {
			invalidate(getLayoutKeyFromLayout(layoutDirectory, path));
		} else if (!LayoutUtils.isTemplate(name) && canDefineLayouts(name)) {
			/* Some other resource a layout can be composed of, e.g. the target of a template call, or
			 * a whole directory of layouts. Which layouts it contributes to is not known here, so all
			 * of them have to be re-loaded. A template, in contrast, is loaded on its own, see
			 * DynamicComponentService. */
			_canIncrementalUpdate = false;
		}
	}

	/**
	 * Whether a resource with the given name can take part in the definition of layouts.
	 *
	 * <p>
	 * Layouts are composed of XML resources exclusively. A name without a file name extension is a
	 * directory, which may contain such resources.
	 * </p>
	 *
	 * <p>
	 * Any other file is invisible to the layout loader and must invalidate nothing - in particular a
	 * temporary or backup copy, whose name may well carry a layout suffix in the middle, as in
	 * <code>someView.layout.xml12345.tmp</code>. Such a name is neither the layout
	 * <code>someView.layout.xml</code> nor a reason to re-load every layout.
	 * </p>
	 */
	private static boolean canDefineLayouts(String name) {
		int extensionStart = name.lastIndexOf('.');

		if (extensionStart <= 0) {
			// A directory, or a hidden file without extension.
			return true;
		}

		return name.endsWith(FileUtilities.XML_FILE_ENDING);
	}

	private void invalidate(String layoutKey) {
		_layoutKeysToUpdate.add(layoutKey);
	}

	private String getLayoutKeyFromLayout(Path base, Path other) {
		return FileUtilities.getRelativizedPath(base, other);
	}

	private String getLayoutKeyFromOverlay(Path base, Path other) {
		String layoutKey = getLayoutKeyFromLayout(base, other);

		return StringServices.changeSuffix(layoutKey, LAYOUT_XML_OVERLAY_FILE_SUFFIX, LAYOUT_XML_FILE_SUFFIX);
	}

	/**
	 * Flag indicating the kind of an update. Returns true if the update could be applied
	 * incremental, otherwise false, the whole layout have to be reloaded to apply all updates.
	 */
	public boolean canIncrementalUpdate() {
		return _canIncrementalUpdate;
	}

	/**
	 * Returns all keys for layout parts that should be reloaded.
	 */
	public Collection<String> getInvalidLayoutKeys() {
		return _layoutKeysToUpdate;
	}

	/**
	 * True if the update contains changes to the layout.
	 */
	public boolean hasChanges() {
		if (_canIncrementalUpdate) {
			return !_layoutKeysToUpdate.isEmpty();
		}

		return true;
	}

}
