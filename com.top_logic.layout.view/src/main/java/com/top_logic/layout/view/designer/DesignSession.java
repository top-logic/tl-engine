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
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.util.error.TopLogicException;

/**
 * The state of a View Designer window: the views opened in it and the view it shows.
 *
 * <p>
 * A view is opened from its {@link DesignSource}: a {@code .view.xml} file
 * ({@link FileDesignSource}) or a view stored in an attribute of a model object
 * ({@link AttributeDesignSource}). The designer shows one view at a time, see
 * {@link DesignTreeBuilder}. The tree of a view is built when the view is opened first and kept for
 * the whole session, so that unsaved edits, dirty markers and everything a display remembers about
 * the nodes of a view survive navigating to another view and back.
 * </p>
 *
 * <p>
 * The {@link #getPath() path} is the trail of views from the {@link #getRoot() root} the designer
 * was opened on to the {@link #getCurrent() view shown}: each view refers to the next one. Opening
 * a view referenced from the view shown appends it, opening a view on the path goes back to it. A
 * reference always names a file, so a session opened on a stored view continues with files.
 * </p>
 *
 * <p>
 * Displays follow the session through {@link #addListener(Runnable)}; a channel holding the path
 * as {@link DesignSource}s - e.g. for a breadcrumb - is kept in sync by {@link #bindPath(ViewChannel)}.
 * </p>
 */
public class DesignSession {

	private final DesignSource _root;

	/**
	 * The trees of the opened views, by source.
	 */
	private final Map<DesignSource, DesignTreeNode> _trees = new LinkedHashMap<>();

	private final List<Runnable> _listeners = new ArrayList<>();

	private List<DesignSource> _path;

	/**
	 * Creates a {@link DesignSession} showing the given root file.
	 *
	 * @param rootFile
	 *        The full path of the application's root view file (e.g.
	 *        {@code /WEB-INF/views/app.view.xml}).
	 */
	public DesignSession(String rootFile) {
		this(new FileDesignSource(rootFile));
	}

	/**
	 * Creates a {@link DesignSession} showing the given view.
	 *
	 * @param root
	 *        The view the designer is opened on, the first entry of every {@link #getPath() path}.
	 */
	public DesignSession(DesignSource root) {
		_root = Objects.requireNonNull(root, "root");
		_path = List.of(root);
	}

	/**
	 * The view the designer was opened on, the first entry of every {@link #getPath() path}.
	 */
	public DesignSource getRoot() {
		return _root;
	}

	/**
	 * The views from the {@link #getRoot() root} to the {@link #getCurrent() view shown}.
	 */
	public List<DesignSource> getPath() {
		return _path;
	}

	/**
	 * The view shown, the last entry of the {@link #getPath() path}.
	 */
	public DesignSource getCurrent() {
		return _path.get(_path.size() - 1);
	}

	/**
	 * The tree of the {@link #getCurrent() view shown}.
	 *
	 * @throws TopLogicException
	 *         If the view cannot be loaded.
	 */
	public DesignTreeNode getCurrentTree() {
		return tree(getCurrent());
	}

	/**
	 * The tree of the given view, built when it is requested first.
	 *
	 * @param source
	 *        Where the view is stored.
	 * @return The root of the tree, the same node on every call until {@link #revert()}.
	 * @throws TopLogicException
	 *         If the view cannot be loaded.
	 */
	public DesignTreeNode tree(DesignSource source) {
		DesignTreeNode tree = _trees.get(source);
		if (tree == null) {
			try {
				tree = new DesignTreeBuilder().build(source);
			} catch (ConfigurationException ex) {
				throw new TopLogicException(
					I18NConstants.ERROR_OPEN_VIEW_FAILED__PATH_DETAILS.fill(source.getLabel(), ex.getMessage()),
					ex);
			}
			_trees.put(source, tree);
		}
		return tree;
	}

	/**
	 * The trees of all views opened in this session, the ones not shown included.
	 */
	public Collection<DesignTreeNode> getOpenedTrees() {
		return Collections.unmodifiableCollection(_trees.values());
	}

	/**
	 * The opened views that were modified since they were saved last, by source.
	 */
	public Map<DesignSource, ViewElement.Config> getDirtyViews() {
		Map<DesignSource, ViewElement.Config> result = new LinkedHashMap<>();
		for (Map.Entry<DesignSource, DesignTreeNode> entry : _trees.entrySet()) {
			DesignTreeNode tree = entry.getValue();
			// A modification marks the nodes up to the root of the tree of its view.
			if (tree.isDirty() && DesignTreeNodes.configOf(tree) instanceof ViewElement.Config view) {
				result.put(entry.getKey(), view);
			}
		}
		return result;
	}

	/**
	 * Clears the dirty markers of the trees of the given views, after they were saved.
	 *
	 * @param sources
	 *        The saved views.
	 */
	public void markSaved(Collection<? extends DesignSource> sources) {
		for (DesignSource source : sources) {
			DesignTreeNode tree = _trees.get(source);
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
	 * Shows the given view, referenced from the {@link #getCurrent() view shown}.
	 *
	 * <p>
	 * The view is appended to the {@link #getPath() path}. A view already on the path is shown by
	 * going back to it instead, so that a reference cycle does not grow the path.
	 * </p>
	 *
	 * @param source
	 *        Where the view is stored.
	 * @throws TopLogicException
	 *         If the view cannot be loaded. The session then stays as it is.
	 */
	public void open(DesignSource source) {
		int index = _path.indexOf(source);
		List<DesignSource> path;
		if (index >= 0) {
			path = _path.subList(0, index + 1);
		} else {
			path = new ArrayList<>(_path);
			path.add(source);
		}
		openPath(path);
	}

	/**
	 * Shows the last view of the given path.
	 *
	 * @param path
	 *        The views from the {@link #getRoot() root} to the view to show. A path not starting
	 *        with the root is ignored.
	 * @throws TopLogicException
	 *         If the last view cannot be loaded. The session then stays as it is.
	 */
	public void openPath(List<? extends DesignSource> path) {
		if (path.isEmpty() || !_root.equals(path.get(0)) || path.equals(_path)) {
			return;
		}
		tree(path.get(path.size() - 1));
		_path = List.copyOf(path);
		fireChanged();
	}

	/**
	 * Shows the given view, reached from the {@link #getRoot() root} on the shortest path of
	 * references.
	 *
	 * @param source
	 *        Where the view is stored.
	 * @throws TopLogicException
	 *         If the view cannot be loaded. The session then stays as it is.
	 * @see #referencePath(DesignSource)
	 */
	public void select(DesignSource source) {
		openPath(referencePath(source));
	}

	/**
	 * The shortest trail of references from the {@link #getRoot() root} to the given view.
	 *
	 * <p>
	 * The trees of the views are searched breadth-first along their {@link ViewLinkDesignTreeNode
	 * links}, building the trees of the views visited on the way. A view not reachable from the
	 * root is answered as the root followed by that view.
	 * </p>
	 *
	 * @param source
	 *        Where the view is stored.
	 * @return The views from the root to the given one.
	 */
	public List<DesignSource> referencePath(DesignSource source) {
		Map<DesignSource, DesignSource> referrer = new HashMap<>();
		referrer.put(_root, null);
		Deque<DesignSource> queue = new ArrayDeque<>();
		queue.add(_root);
		while (!queue.isEmpty()) {
			DesignSource current = queue.removeFirst();
			if (current.equals(source)) {
				return pathTo(current, referrer);
			}
			DesignTreeNode tree;
			try {
				tree = tree(current);
			} catch (TopLogicException ex) {
				Logger.warn("Skipping the references of a view that cannot be loaded: " + current.getId(), ex,
					DesignSession.class);
				continue;
			}
			for (DesignSource target : linkTargets(tree)) {
				if (!referrer.containsKey(target)) {
					referrer.put(target, current);
					queue.add(target);
				}
			}
		}
		return List.of(_root, source);
	}

	private static List<DesignSource> pathTo(DesignSource source, Map<DesignSource, DesignSource> referrer) {
		List<DesignSource> path = new ArrayList<>();
		for (DesignSource current = source; current != null; current = referrer.get(current)) {
			path.add(current);
		}
		Collections.reverse(path);
		return path;
	}

	/**
	 * The views the given tree links to, in tree order.
	 */
	private static List<DesignSource> linkTargets(DesignTreeNode tree) {
		List<DesignSource> result = new ArrayList<>();
		addLinkTargets(tree, result);
		return result;
	}

	private static void addLinkTargets(DesignTreeNode node, List<DesignSource> result) {
		if (node instanceof ViewLinkDesignTreeNode link) {
			result.add(link.getTargetSource());
		}
		for (DesignTreeNode child : node.getChildren()) {
			addLinkTargets(child, result);
		}
	}

	/**
	 * Discards the trees of all opened views with their unsaved modifications, and shows the
	 * {@link #getRoot() root} read anew.
	 *
	 * <p>
	 * A tree edits the configuration its source loaded in place - for a file the shared
	 * configuration of {@link ViewLoader#getOrLoadConfig(String)}. Therefore every opened source
	 * {@link DesignSource#discard() discards} what it loaded, so that its tree is built from the
	 * stored view when it is opened again.
	 * </p>
	 */
	public void revert() {
		for (Map.Entry<DesignSource, DesignTreeNode> entry : _trees.entrySet()) {
			entry.getValue().cleanup();
			entry.getKey().discard();
		}
		_trees.clear();
		_path = List.of(_root);
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
	 * Keeps the given channel holding the {@link #getPath() path} as list of {@link DesignSource}s,
	 * each displayed by its {@link DesignSource#getLabel() label}.
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
		channel.set(_path);
		Registration sessionRegistration = addListener(() -> channel.set(_path));
		Registration channelRegistration = channel.addListener((sender, oldValue, newValue) -> {
			if (newValue instanceof List<?> entries) {
				openPath(sources(entries));
			}
		});
		return Registration.onDispose(() -> {
			channelRegistration.dispose();
			sessionRegistration.dispose();
		});
	}

	private static List<DesignSource> sources(List<?> entries) {
		List<DesignSource> result = new ArrayList<>(entries.size());
		for (Object entry : entries) {
			if (entry instanceof DesignSource source) {
				result.add(source);
			} else {
				// Not a path of this session.
				return List.of();
			}
		}
		return result;
	}
}
