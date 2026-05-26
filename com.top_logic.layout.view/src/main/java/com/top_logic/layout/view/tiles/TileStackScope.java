/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.tiles;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.view.channel.ViewChannel;

/**
 * Ambient handle to the surrounding {@link TileStackElement &lt;tile-stack&gt;}.
 *
 * <p>
 * Installed by the stack into its frame's child {@link com.top_logic.layout.view.ViewContext} via
 * {@link com.top_logic.layout.view.ViewContext#withTileStackScope(TileStackScope)}. Commands
 * inside a frame (such as {@link NavigatePushCommand &lt;navigate-push&gt;}) look it up via
 * {@link com.top_logic.layout.view.ViewContext#getTileStackScope()} and call
 * {@link #push(String, ResKey, Map)} / {@link #pop()} / {@link #popTo(int)} on it.
 * </p>
 *
 * <p>
 * The scope is a thin facade over the stack's path {@link ViewChannel}: all mutators read the
 * channel's current value, compute the new list and write it back. The channel is the sole source
 * of truth.
 * </p>
 */
public class TileStackScope {

	private final ViewChannel _pathChannel;

	/**
	 * Creates a {@link TileStackScope} backed by the given path channel.
	 *
	 * @param pathChannel
	 *        The channel holding the {@code List<TileFrame>} path. Must not be {@code null}.
	 */
	public TileStackScope(ViewChannel pathChannel) {
		_pathChannel = pathChannel;
	}

	/**
	 * Current path (immutable snapshot).
	 */
	public List<TileFrame> getPath() {
		return readPath();
	}

	/**
	 * Appends a frame to the path.
	 *
	 * @param viewRef
	 *        Path of the view file to mount.
	 * @param label
	 *        Breadcrumb label, may be {@code null}.
	 * @param params
	 *        Bound parameter values captured at push time.
	 */
	public void push(String viewRef, ResKey label, Map<String, Object> params) {
		List<TileFrame> next = new ArrayList<>(readPath());
		next.add(new TileFrame(viewRef, label, params));
		_pathChannel.set(Collections.unmodifiableList(next));
	}

	/**
	 * Drops the topmost frame, if any.
	 */
	public void pop() {
		List<TileFrame> current = readPath();
		if (current.isEmpty()) {
			return;
		}
		_pathChannel.set(Collections.unmodifiableList(new ArrayList<>(current.subList(0, current.size() - 1))));
	}

	/**
	 * Truncates the path so that exactly the frames at positions {@code 0..size-1} remain. After
	 * the call, the new top frame is at index {@code size-1}; for {@code size == 0}, the stack
	 * shows its {@link TileStackElement.Config#getInitial() initial} view again.
	 *
	 * @param size
	 *        Number of frames to keep. Negative values clamp to 0, values larger than the current
	 *        size are no-ops.
	 */
	public void popTo(int size) {
		List<TileFrame> current = readPath();
		int target = Math.max(0, Math.min(size, current.size()));
		if (target == current.size()) {
			return;
		}
		_pathChannel.set(Collections.unmodifiableList(new ArrayList<>(current.subList(0, target))));
	}

	@SuppressWarnings("unchecked")
	private List<TileFrame> readPath() {
		Object value = _pathChannel.get();
		if (value instanceof List<?> list) {
			return (List<TileFrame>) list;
		}
		return Collections.emptyList();
	}
}
