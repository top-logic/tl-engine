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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.io.PathUpdate;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataFactory;

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

	private Collection<String> _layoutKeysToDelete = new HashSet<>();

	private Map<String, Set<BinaryData>> _overlaysToAddByLayoutKey = new HashMap<>();

	private Map<String, Set<BinaryData>> _overlaysToDeleteByLayoutKey = new HashMap<>();

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
			Path layoutDirectory = LayoutUtils.getLayoutDirectory(path);
			if (layoutDirectory == null) {
				continue;
			}

			if (path.startsWith(layoutDirectory)) {
				if (Files.isDirectory(path)) {
					try {
						Set<Path> children = Files.walk(path).filter(Files::isRegularFile).collect(Collectors.toSet());

						for (Path child : children) {
							visisitFileCreation(layoutDirectory, child);
						}
					} catch (IOException exception) {
						Logger.error("Problen while reading " + path, exception, this);
					}
				} else {
					visisitFileCreation(layoutDirectory, path);
				}
			}
		}
	}

	private void visisitFileCreation(Path layoutDirectory, Path path) {
		Path filename = path.getFileName();

		if (filename != null) {
			String name = filename.toString();

			if (LayoutUtils.isLayoutOverlay(name)) {
				visitOverlayCreation(layoutDirectory, path);
			} else if (LayoutUtils.isLayout(name)) {
				_layoutKeysToUpdate.add(getLayoutKeyFromLayout(layoutDirectory, path));
			} else if (!LayoutUtils.isTemplate(name)) {
				_canIncrementalUpdate = false;
				return;
			}
		}
	}

	private void visitOverlayCreation(Path layoutDirectory, Path path) {
		String layoutKey = getLayoutKeyFromOverlay(layoutDirectory, path);
		_overlaysToAddByLayoutKey.computeIfAbsent(layoutKey, k -> new HashSet<>())
			.add(BinaryDataFactory.createBinaryDataWithName(path, layoutKey));
		_layoutKeysToUpdate.add(layoutKey);
	}

	private void visitFileDeletions(PathUpdate update) {
		for (Path path : update.getDeletions()) {
			Path layoutDirectory = LayoutUtils.getLayoutDirectory(path);
			if (layoutDirectory == null) {
				continue;
			}

			if (path.startsWith(layoutDirectory)) {
				if (!Files.isDirectory(path)) {
					Path filename = path.getFileName();

					if (filename != null) {
						String name = filename.toString();

						if (LayoutUtils.isLayoutOverlay(name)) {
							visitOverlayDeletion(path, layoutDirectory);
						} else if (LayoutUtils.isLayout(name)) {
							_layoutKeysToDelete.add(getLayoutKeyFromLayout(layoutDirectory, path));
						} else if (!LayoutUtils.isTemplate(name)) {
							_canIncrementalUpdate = false;
							return;
						}
					}
				}
			}
		}
	}

	private void visitOverlayDeletion(Path path, Path layoutDirectory) {
		String layoutKey = getLayoutKeyFromOverlay(layoutDirectory, path);
		_overlaysToDeleteByLayoutKey.computeIfAbsent(layoutKey, k -> new HashSet<>())
			.add(BinaryDataFactory.createBinaryDataWithName(path, layoutKey));
		_layoutKeysToUpdate.add(layoutKey);
	}

	private void visitFileChanges(PathUpdate update) {
		for (Path path : update.getChanges()) {
			Path layoutDirectory = LayoutUtils.getLayoutDirectory(path);
			if (layoutDirectory == null) {
				continue;
			}

			if (path.startsWith(layoutDirectory)) {
				if (!Files.isDirectory(path)) {
					Path filename = path.getFileName();

					if (filename != null) {
						String name = filename.toString();

						if (LayoutUtils.isLayoutOverlay(name)) {
							_layoutKeysToUpdate.add(getLayoutKeyFromOverlay(layoutDirectory, path));
						} else if (LayoutUtils.isLayout(name)) {
							_layoutKeysToUpdate.add(getLayoutKeyFromLayout(layoutDirectory, path));
						} else if (!LayoutUtils.isTemplate(name)) {
							_canIncrementalUpdate = false;
							return;
						}
					}
				}
			}
		}
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
	public Collection<String> getLayoutKeysToUpdate() {
		return _layoutKeysToUpdate;
	}

	/**
	 * Returns all keys for layout parts that can be removed.
	 */
	public Collection<String> getLayoutKeysToDelete() {
		return _layoutKeysToDelete;
	}

	/**
	 * Returns all new overlays for layout parts that should be applied.
	 */
	public Map<String, Set<BinaryData>> getOverlaysToAdd() {
		return _overlaysToAddByLayoutKey;
	}

	/**
	 * Returns all overlays for layout parts that can be removed.
	 */
	public Map<String, Set<BinaryData>> getOverlaysToDelete() {
		return _overlaysToDeleteByLayoutKey;
	}

	/**
	 * True if the update contains changes to the layout.
	 */
	public boolean hasChanges() {
		if (_canIncrementalUpdate) {
			boolean hasLayoutKeyChanges = !_layoutKeysToUpdate.isEmpty() || !_layoutKeysToDelete.isEmpty();
			boolean hasOverlayChanges = !_overlaysToAddByLayoutKey.isEmpty() || !_overlaysToDeleteByLayoutKey.isEmpty();

			return hasLayoutKeyChanges || hasOverlayChanges;
		}

		return true;
	}

}
