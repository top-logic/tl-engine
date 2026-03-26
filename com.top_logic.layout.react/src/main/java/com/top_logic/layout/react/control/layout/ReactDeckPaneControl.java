/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.layout;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactCommand;
import com.top_logic.layout.react.control.ReactControl;

import de.haumacher.msgbuf.json.JsonWriter;

/**
 * A {@link ReactControl} that shows one child at a time from a list, driven by a server-side
 * active index.
 *
 * <p>
 * Previously visited children are cached so that re-selecting them preserves their state. This
 * follows the same lazy-creation pattern as {@code ReactTabBarControl}.
 * </p>
 *
 * <p>
 * The React component {@code TLDeckPane} receives the following state:
 * </p>
 * <ul>
 * <li>{@code activeIndex} - the index of the currently active child</li>
 * <li>{@code activeChild} - the active child's control descriptor (or {@code null})</li>
 * <li>{@code childCount} - total number of children</li>
 * </ul>
 */
public class ReactDeckPaneControl extends ReactControl {

	private static final String REACT_MODULE = "TLDeckPane";

	private static final String ACTIVE_INDEX = "activeIndex";

	private static final String ACTIVE_CHILD = "activeChild";

	private static final String CHILD_COUNT = "childCount";

	private static final String INDEX_ARG = "index";

	/**
	 * The child definitions. Each entry provides a factory for lazy creation.
	 */
	public interface ChildFactory {

		/**
		 * Creates the child control.
		 */
		ReactControl create();
	}

	private final List<ChildFactory> _childFactories = new ArrayList<>();

	private final LinkedHashMap<Integer, ReactControl> _childCache = new LinkedHashMap<>();

	private int _activeIndex;

	/**
	 * Creates a new {@link ReactDeckPaneControl}.
	 *
	 * @param childFactories
	 *        The factories for lazily creating child controls. Must not be empty.
	 * @param initialActiveIndex
	 *        The initially active child index.
	 */
	public ReactDeckPaneControl(ReactContext context, List<ChildFactory> childFactories, int initialActiveIndex) {
		super(context, null, REACT_MODULE);
		_childFactories.addAll(childFactories);
		_activeIndex = initialActiveIndex;

		putState(ACTIVE_INDEX, Integer.valueOf(_activeIndex));
		putState(CHILD_COUNT, Integer.valueOf(_childFactories.size()));
		// activeChild is null until writeAsChild creates it.
	}

	/**
	 * Creates a new {@link ReactDeckPaneControl} with the first child active.
	 */
	public ReactDeckPaneControl(ReactContext context, List<ChildFactory> childFactories) {
		this(context, childFactories, 0);
	}

	/**
	 * Selects the child at the given index.
	 *
	 * @param index
	 *        The index of the child to select.
	 */
	public void selectChild(int index) {
		if (index == _activeIndex) {
			return;
		}
		if (index < 0 || index >= _childFactories.size()) {
			throw new IllegalArgumentException("Index out of bounds: " + index);
		}
		_activeIndex = index;

		if (!isSSEAttached()) {
			putStateSilent(ACTIVE_INDEX, Integer.valueOf(_activeIndex));
			return;
		}

		ReactControl content = getOrCreateChild(index);

		Map<String, Object> patch = new HashMap<>();
		patch.put(ACTIVE_INDEX, Integer.valueOf(index));
		patch.put(ACTIVE_CHILD, content);
		patchReactState(patch);
	}

	/**
	 * The currently active child index.
	 */
	public int getActiveIndex() {
		return _activeIndex;
	}

	@Override
	protected void writeAsChild(JsonWriter writer)
			throws IOException {
		if (getState(ACTIVE_CHILD) == null) {
			ReactControl activeChild = getOrCreateChild(_activeIndex);
			putStateSilent(ACTIVE_CHILD, activeChild);
		}
		super.writeAsChild(writer);
	}

	@Override
	protected void cleanupChildren() {
		for (ReactControl cached : _childCache.values()) {
			cached.cleanupTree();
		}
		_childCache.clear();
	}

	private ReactControl getOrCreateChild(int index) {
		ReactControl cached = _childCache.get(Integer.valueOf(index));
		if (cached != null) {
			return cached;
		}

		ReactControl child = _childFactories.get(index).create();
		_childCache.put(Integer.valueOf(index), child);
		return child;
	}

	// -- Commands --

	/**
	 * Handles child selection from the client.
	 */
	@ReactCommand("selectChild")
	void handleSelectChild(Map<String, Object> arguments) {
		Object indexObj = arguments.get(INDEX_ARG);
		if (indexObj instanceof Number) {
			selectChild(((Number) indexObj).intValue());
		}
	}
}
