/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.designer;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.top_logic.basic.Logger;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.listener.Registration;
import com.top_logic.layout.view.I18NConstants;
import com.top_logic.layout.view.ViewElement;
import com.top_logic.layout.view.ViewLoader;
import com.top_logic.layout.view.ViewRef;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.util.error.TopLogicException;

/**
 * The state of a View Designer window: the view files opened in it and the file it shows.
 *
 * <p>
 * The designer shows one view file at a time, see {@link DesignTreeBuilder}. The tree of a file is
 * built when the file is opened first and kept for the whole session, so that unsaved edits, dirty
 * markers and everything a display remembers about the nodes of a file survive navigating to
 * another file and back.
 * </p>
 *
 * <p>
 * The {@link #getPath() path} is the trail of files from the {@link #getRootFile() root file} of
 * the application to the {@link #getCurrentFile() file shown}: each file refers to the next one.
 * Opening a file referenced from the file shown appends it, opening a file on the path goes back
 * to it.
 * </p>
 *
 * <p>
 * Displays follow the session through {@link #addListener(Runnable)}; a channel holding the path
 * as {@link ViewRef}s - e.g. for a breadcrumb - is kept in sync by {@link #bindPath(ViewChannel)}.
 * </p>
 */
public class DesignSession {

	private final String _rootFile;

	/**
	 * The trees of the opened files, by full file path.
	 */
	private final Map<String, DesignTreeNode> _trees = new LinkedHashMap<>();

	private final List<Runnable> _listeners = new ArrayList<>();

	private List<String> _path;

	/**
	 * Creates a {@link DesignSession} showing the given root file.
	 *
	 * @param rootFile
	 *        The full path of the application's root view file (e.g.
	 *        {@code /WEB-INF/views/app.view.xml}).
	 */
	public DesignSession(String rootFile) {
		_rootFile = Objects.requireNonNull(rootFile, "rootFile");
		_path = List.of(rootFile);
	}

	/**
	 * The full path of the application's root view file, the first entry of every
	 * {@link #getPath() path}.
	 */
	public String getRootFile() {
		return _rootFile;
	}

	/**
	 * The full paths of the files from the {@link #getRootFile() root file} to the
	 * {@link #getCurrentFile() file shown}.
	 */
	public List<String> getPath() {
		return _path;
	}

	/**
	 * The full path of the view file shown, the last entry of the {@link #getPath() path}.
	 */
	public String getCurrentFile() {
		return _path.get(_path.size() - 1);
	}

	/**
	 * The tree of the {@link #getCurrentFile() file shown}.
	 *
	 * @throws TopLogicException
	 *         If the file cannot be loaded.
	 */
	public DesignTreeNode getCurrentTree() {
		return tree(getCurrentFile());
	}

	/**
	 * The tree of the given view file, built when it is requested first.
	 *
	 * @param file
	 *        The full path of the view file.
	 * @return The root of the tree, the same node on every call until {@link #revert()}.
	 * @throws TopLogicException
	 *         If the file cannot be loaded.
	 */
	public DesignTreeNode tree(String file) {
		DesignTreeNode tree = _trees.get(file);
		if (tree == null) {
			try {
				tree = new DesignTreeBuilder().build(file);
			} catch (ConfigurationException ex) {
				throw new TopLogicException(
					I18NConstants.ERROR_OPEN_VIEW_FAILED__PATH_DETAILS.fill(ViewLoader.viewRef(file), ex.getMessage()),
					ex);
			}
			_trees.put(file, tree);
		}
		return tree;
	}

	/**
	 * The trees of all files opened in this session, the ones not shown included.
	 */
	public Collection<DesignTreeNode> getOpenedTrees() {
		return Collections.unmodifiableCollection(_trees.values());
	}

	/**
	 * The views of the opened files that were modified since they were saved last, by full file
	 * path.
	 */
	public Map<String, ViewElement.Config> getDirtyViews() {
		Map<String, ViewElement.Config> result = new LinkedHashMap<>();
		for (Map.Entry<String, DesignTreeNode> entry : _trees.entrySet()) {
			DesignTreeNode tree = entry.getValue();
			// A modification marks the nodes up to the root of the tree of its file.
			if (tree.isDirty() && DesignTreeNodes.configOf(tree) instanceof ViewElement.Config view) {
				result.put(entry.getKey(), view);
			}
		}
		return result;
	}

	/**
	 * Clears the dirty markers of the trees of the given files, after they were saved.
	 *
	 * @param files
	 *        The full paths of the saved files.
	 */
	public void markSaved(Collection<String> files) {
		for (String file : files) {
			DesignTreeNode tree = _trees.get(file);
			if (tree != null) {
				clearDirty(tree);
			}
		}
	}

	private static void clearDirty(DesignTreeNode node) {
		node.clearDirty();
		for (DesignTreeNode child : node.getChildren()) {
			clearDirty(child);
		}
	}

	/**
	 * Shows the given file, referenced from the {@link #getCurrentFile() file shown}.
	 *
	 * <p>
	 * The file is appended to the {@link #getPath() path}. A file already on the path is shown by
	 * going back to it instead, so that a reference cycle does not grow the path.
	 * </p>
	 *
	 * @param file
	 *        The full path of the view file.
	 * @throws TopLogicException
	 *         If the file cannot be loaded. The session then stays as it is.
	 */
	public void open(String file) {
		int index = _path.indexOf(file);
		List<String> path;
		if (index >= 0) {
			path = _path.subList(0, index + 1);
		} else {
			path = new ArrayList<>(_path);
			path.add(file);
		}
		openPath(path);
	}

	/**
	 * Shows the last file of the given path.
	 *
	 * @param path
	 *        The full paths of the files from the {@link #getRootFile() root file} to the file to
	 *        show. A path not starting with the root file is ignored.
	 * @throws TopLogicException
	 *         If the last file cannot be loaded. The session then stays as it is.
	 */
	public void openPath(List<String> path) {
		if (path.isEmpty() || !_rootFile.equals(path.get(0)) || path.equals(_path)) {
			return;
		}
		tree(path.get(path.size() - 1));
		_path = List.copyOf(path);
		fireChanged();
	}

	/**
	 * Shows the given file, reached from the {@link #getRootFile() root file} on the shortest path
	 * of references.
	 *
	 * @param file
	 *        The full path of the view file.
	 * @throws TopLogicException
	 *         If the file cannot be loaded. The session then stays as it is.
	 * @see #referencePath(String)
	 */
	public void select(String file) {
		openPath(referencePath(file));
	}

	/**
	 * The shortest trail of references from the {@link #getRootFile() root file} to the given file.
	 *
	 * <p>
	 * The trees of the files are searched breadth-first along their {@link ViewLinkDesignTreeNode
	 * links}, building the trees of the files visited on the way. A file not reachable from the root
	 * file is answered as the root file followed by that file.
	 * </p>
	 *
	 * @param file
	 *        The full path of the view file.
	 * @return The full paths of the files from the root file to the given one.
	 */
	public List<String> referencePath(String file) {
		Map<String, String> referrer = new HashMap<>();
		referrer.put(_rootFile, null);
		Deque<String> queue = new ArrayDeque<>();
		queue.add(_rootFile);
		while (!queue.isEmpty()) {
			String current = queue.removeFirst();
			if (current.equals(file)) {
				return pathTo(current, referrer);
			}
			DesignTreeNode tree;
			try {
				tree = tree(current);
			} catch (TopLogicException ex) {
				Logger.warn("Skipping the references of a view that cannot be loaded: " + current, ex,
					DesignSession.class);
				continue;
			}
			for (String target : linkTargets(tree)) {
				if (!referrer.containsKey(target)) {
					referrer.put(target, current);
					queue.add(target);
				}
			}
		}
		return List.of(_rootFile, file);
	}

	private static List<String> pathTo(String file, Map<String, String> referrer) {
		List<String> path = new ArrayList<>();
		for (String current = file; current != null; current = referrer.get(current)) {
			path.add(current);
		}
		Collections.reverse(path);
		return path;
	}

	/**
	 * The full paths of the files the given tree links to, in tree order.
	 */
	private static List<String> linkTargets(DesignTreeNode tree) {
		List<String> result = new ArrayList<>();
		addLinkTargets(tree, result);
		return result;
	}

	private static void addLinkTargets(DesignTreeNode node, List<String> result) {
		if (node instanceof ViewLinkDesignTreeNode link) {
			result.add(link.getTargetFile());
		}
		for (DesignTreeNode child : node.getChildren()) {
			addLinkTargets(child, result);
		}
	}

	/**
	 * Discards the trees of all opened files with their unsaved modifications, and shows the
	 * {@link #getRootFile() root file} read anew.
	 *
	 * <p>
	 * The trees are built on the shared configurations of {@link ViewLoader#getOrLoadConfig(String)},
	 * which their edits modify in place. Therefore the cached configuration of every opened file is
	 * {@link ViewLoader#discardCachedConfig(String) discarded}, so that its tree is built from the
	 * file on disk when it is opened again.
	 * </p>
	 */
	public void revert() {
		for (Map.Entry<String, DesignTreeNode> entry : _trees.entrySet()) {
			entry.getValue().cleanup();
			ViewLoader.discardCachedConfig(entry.getKey());
		}
		_trees.clear();
		_path = List.of(_rootFile);
		fireChanged();
	}

	/**
	 * Registers a listener notified whenever the {@link #getCurrentTree() tree shown} or the
	 * {@link #getPath() path} changed.
	 *
	 * @return The registration that drops the listener again.
	 */
	public Registration addListener(Runnable listener) {
		_listeners.add(listener);
		return Registration.onDispose(() -> _listeners.remove(listener));
	}

	private void fireChanged() {
		for (Runnable listener : new ArrayList<>(_listeners)) {
			listener.run();
		}
	}

	/**
	 * Keeps the given channel holding the {@link #getPath() path} as list of {@link ViewRef}s.
	 *
	 * <p>
	 * The binding works both ways: a path written to the channel - e.g. a prefix of it by a
	 * breadcrumb going back - is {@link #openPath(List) opened}.
	 * </p>
	 *
	 * @param channel
	 *        The channel to bind.
	 * @return The registration that ends the binding.
	 */
	public Registration bindPath(ViewChannel channel) {
		channel.set(viewRefs(_path));
		Registration sessionRegistration = addListener(() -> channel.set(viewRefs(_path)));
		Registration channelRegistration = channel.addListener((sender, oldValue, newValue) -> {
			if (newValue instanceof List<?> refs) {
				openPath(files(refs));
			}
		});
		return Registration.onDispose(() -> {
			channelRegistration.dispose();
			sessionRegistration.dispose();
		});
	}

	private static List<ViewRef> viewRefs(List<String> files) {
		List<ViewRef> result = new ArrayList<>(files.size());
		for (String file : files) {
			result.add(new ViewRef(ViewLoader.viewRef(file)));
		}
		return Collections.unmodifiableList(result);
	}

	private static List<String> files(List<?> refs) {
		List<String> result = new ArrayList<>(refs.size());
		for (Object ref : refs) {
			result.add(ref instanceof ViewRef viewRef ? viewRef.fullPath() : ViewLoader.fullPath(ref.toString()));
		}
		return result;
	}
}
