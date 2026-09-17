/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.navigation;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.top_logic.basic.Logger;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.layout.view.ChildGroup;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewElement;
import com.top_logic.layout.view.ViewLoader;

/**
 * Where the view files reachable from a root view are displayed within it.
 *
 * <p>
 * Answers, for a view file, the {@link MountPath}s it is displayed at: the containers to ask on the
 * way from the root view down to it. A view referenced from several places has several mount paths,
 * a view no reference reaches has none.
 * </p>
 *
 * <p>
 * Built from the loaded configuration alone, by walking the {@link UIElement#getChildGroups()
 * content groups} of the root view's elements. Containers that create their content only when it is
 * first displayed (a sidebar item, a tab) are therefore covered, whereas content that exists only
 * once a user produced it (a frame pushed onto a tile stack) is not: a tile stack contributes its
 * initial view.
 * </p>
 */
public final class ViewMounts {

	private static final ConcurrentHashMap<String, CachedMounts> CACHE = new ConcurrentHashMap<>();

	private static final ViewResolver LOADER = viewRef -> ViewLoader.getOrLoadView(ViewLoader.fullPath(viewRef));

	private final String _rootView;

	private final Map<String, List<MountPath>> _mounts;

	private final Map<String, Set<String>> _channels;

	private ViewMounts(String rootView, Map<String, List<MountPath>> mounts, Map<String, Set<String>> channels) {
		_rootView = rootView;
		_mounts = mounts;
		_channels = channels;
	}

	/**
	 * The mounts of the application's view files, as reachable from the given root view.
	 *
	 * <p>
	 * The result is cached until one of the visited view files is written.
	 * </p>
	 *
	 * @param rootView
	 *        Path of the root view file, either relative to {@link ViewLoader#VIEW_BASE_PATH} or
	 *        full.
	 * @return The scan of everything reachable from that view.
	 */
	public static ViewMounts forRootView(String rootView) {
		String viewRef = ViewLoader.viewRef(rootView);

		CachedMounts cached = CACHE.get(viewRef);
		if (cached != null && cached._signature == signature(cached._mounts.getMountedViews())) {
			return cached._mounts;
		}

		ViewMounts mounts = scan(viewRef, LOADER);
		CACHE.put(viewRef, new CachedMounts(mounts, signature(mounts.getMountedViews())));
		return mounts;
	}

	/**
	 * Scans the views reachable from the given root view, resolving referenced views through the
	 * given resolver.
	 *
	 * @param rootView
	 *        Path of the root view file, either relative to {@link ViewLoader#VIEW_BASE_PATH} or
	 *        full.
	 * @param resolver
	 *        Access to the referenced view files.
	 * @return The scan, computed afresh.
	 */
	public static ViewMounts scan(String rootView, ViewResolver resolver) {
		return new Scan(resolver).run(ViewLoader.viewRef(rootView));
	}

	/**
	 * The root view the scan started from, relative to {@link ViewLoader#VIEW_BASE_PATH}.
	 */
	public String getRootView() {
		return _rootView;
	}

	/**
	 * Every view file reached from the {@link #getRootView() root view}, including the root view
	 * itself and the files a reference names but that could not be loaded.
	 */
	public Set<String> getMountedViews() {
		return _mounts.keySet();
	}

	/**
	 * Whether the given view file is displayed anywhere within the {@link #getRootView() root view}.
	 *
	 * @param viewRef
	 *        Path of the view file, either relative to {@link ViewLoader#VIEW_BASE_PATH} or full.
	 */
	public boolean isMounted(String viewRef) {
		return _mounts.containsKey(ViewLoader.viewRef(viewRef));
	}

	/**
	 * The places the given view file is displayed at.
	 *
	 * @param viewRef
	 *        Path of the view file, either relative to {@link ViewLoader#VIEW_BASE_PATH} or full.
	 * @return The mount paths in the order the scan found them, empty for a view the root view does
	 *         not reach.
	 */
	public List<MountPath> getMounts(String viewRef) {
		return _mounts.getOrDefault(ViewLoader.viewRef(viewRef), List.of());
	}

	/**
	 * The channels the given mounted view declares, in declaration order.
	 *
	 * <p>
	 * These are the names a channel binding addressing this view may use.
	 * </p>
	 *
	 * @param viewRef
	 *        Path of the view file, either relative to {@link ViewLoader#VIEW_BASE_PATH} or full.
	 * @return The declared channel names, empty for a view that is not mounted or could not be
	 *         loaded.
	 */
	public Set<String> getChannelNames(String viewRef) {
		return _channels.getOrDefault(ViewLoader.viewRef(viewRef), Set.of());
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "(" + _rootView + ": " + _mounts.values() + ")";
	}

	/**
	 * The change signature of the given view files, deciding whether a cached scan is still valid.
	 */
	private static long signature(Collection<String> viewRefs) {
		long result = 1L;
		for (String viewRef : viewRefs) {
			result = 31 * result + ViewLoader.modificationSignature(ViewLoader.fullPath(viewRef));
		}
		return result;
	}

	/**
	 * The traversal producing a {@link ViewMounts}.
	 */
	private static final class Scan {

		private final ViewResolver _resolver;

		private final Map<String, List<MountPath>> _mounts = new LinkedHashMap<>();

		private final Map<String, Set<String>> _channels = new LinkedHashMap<>();

		/**
		 * The view files being descended into, so that a view reaching itself is mounted but not
		 * descended into twice.
		 */
		private final Deque<String> _descending = new ArrayDeque<>();

		Scan(ViewResolver resolver) {
			_resolver = resolver;
		}

		ViewMounts run(String rootView) {
			addView(rootView, List.of());

			Map<String, List<MountPath>> mounts = new LinkedHashMap<>();
			_mounts.forEach((viewRef, paths) -> mounts.put(viewRef, List.copyOf(paths)));
			return new ViewMounts(rootView, Collections.unmodifiableMap(mounts),
				Collections.unmodifiableMap(_channels));
		}

		private void addView(String viewRef, List<MountStep> path) {
			_mounts.computeIfAbsent(viewRef, key -> new ArrayList<>()).add(new MountPath(viewRef, path));

			if (_descending.contains(viewRef)) {
				// The view reaches itself: its mount is recorded, but descending again would not
				// terminate. What lies below is already covered by the enclosing descent.
				return;
			}

			ViewElement view;
			try {
				view = _resolver.getView(viewRef);
			} catch (ConfigurationException | RuntimeException ex) {
				// A defective view hides only its own content: the places already found stay
				// usable, and the mount of the defective view itself is recorded above.
				Logger.error("Skipping the content of a view that could not be loaded: " + viewRef, ex,
					ViewMounts.class);
				return;
			}
			_channels.putIfAbsent(viewRef, view.getChannelNames());

			_descending.push(viewRef);
			try {
				addElement(view, path);
			} finally {
				_descending.pop();
			}
		}

		private void addElement(UIElement element, List<MountStep> path) {
			for (ChildGroup group : element.getChildGroups()) {
				List<MountStep> groupPath = path;
				if (!ChildGroup.NO_KEY.equals(group.key())) {
					groupPath = new ArrayList<>(path);
					groupPath.add(new MountStep(element, group.key()));
					groupPath = List.copyOf(groupPath);
				}

				if (group instanceof ChildGroup.EmbeddedView embedded) {
					addView(embedded.viewPath(), groupPath);
				} else if (group instanceof ChildGroup.Elements elements) {
					for (UIElement child : elements.children()) {
						addElement(child, groupPath);
					}
				}
			}
		}
	}

	private static final class CachedMounts {

		final ViewMounts _mounts;

		final long _signature;

		CachedMounts(ViewMounts mounts, long signature) {
			_mounts = mounts;
			_signature = signature;
		}
	}
}
