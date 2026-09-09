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
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ViewChannel;

/**
 * Ambient handle to the surrounding {@link TileStackElement &lt;tile-stack&gt;}.
 *
 * <p>
 * Installed by the stack into its frame's child {@link com.top_logic.layout.view.ViewContext} via
 * {@link com.top_logic.layout.view.ViewContext#withScope(Class, Object)}. Commands and actions
 * inside a frame ({@link NavigatePushCommand &lt;navigate-push&gt;},
 * {@link NavigatePopCommand &lt;navigate-pop&gt;} / {@link NavigatePopAction the same as a chain
 * action}, {@link NavigatePopToCommand &lt;navigate-pop-to&gt;} / {@link NavigatePopToAction the
 * same as a chain action}) look it up via {@link #lookup(ReactContext, String)} and call
 * {@link #push(String, ResKey, Map)} / {@link #pop()} / {@link #popTo(int)} on it.
 * </p>
 *
 * <p>
 * The scope is a thin facade over the stack's path {@link ViewChannel}: all mutators read the
 * channel's current value, compute the new list and write it back. The channel is the sole source
 * of truth.
 * </p>
 *
 * <p>
 * The scope also carries the {@link #frameRoutes() routes} the stack declares for its frame views:
 * they name a frame that is pushed without a label of its own, and they are what
 * {@link TileFrameRouteParticipant} reflects the path in the URL with and restores it from.
 * </p>
 */
public class TileStackScope {

	/**
	 * The {@link TileStackScope} enclosing the given context.
	 *
	 * @param context
	 *        The context in which the requesting command or action executes.
	 * @param tag
	 *        Configuration tag of the requesting command or action, quoted in the failure message.
	 * @return The scope of the innermost enclosing {@link TileStackElement &lt;tile-stack&gt;}.
	 * @throws IllegalStateException
	 *         if the context is no {@link ViewContext}, or no tile stack encloses it.
	 */
	public static TileStackScope lookup(ReactContext context, String tag) {
		if (!(context instanceof ViewContext viewContext)) {
			throw new IllegalStateException(
				"<" + tag + "> requires a ViewContext, got " + context.getClass().getName());
		}
		TileStackScope scope = viewContext.getScope(TileStackScope.class);
		if (scope == null) {
			throw new IllegalStateException(
				"<" + tag + "> executed outside of any enclosing <tile-stack>.");
		}
		return scope;
	}

	private final ViewChannel _pathChannel;

	private final List<FrameRoute> _frameRoutes;

	private long _restoredAdoption;

	private List<TileFrame> _restoredPath = List.of();

	/**
	 * Creates a {@link TileStackScope} whose stack declares no frame routes.
	 *
	 * @param pathChannel
	 *        The channel holding the {@code List<TileFrame>} path. Must not be {@code null}.
	 */
	public TileStackScope(ViewChannel pathChannel) {
		this(pathChannel, List.of());
	}

	/**
	 * Creates a {@link TileStackScope} backed by the given path channel.
	 *
	 * @param pathChannel
	 *        The channel holding the {@code List<TileFrame>} path. Must not be {@code null}.
	 * @param frameRoutes
	 *        The routes the stack declares for its frame views, in declaration order.
	 */
	public TileStackScope(ViewChannel pathChannel, List<FrameRoute> frameRoutes) {
		_pathChannel = pathChannel;
		_frameRoutes = List.copyOf(frameRoutes);
	}

	/**
	 * Current path (immutable snapshot).
	 */
	public List<TileFrame> getPath() {
		return readPath();
	}

	/**
	 * The channel holding the path, the stack's single source of truth.
	 *
	 * <p>
	 * Every navigation of the stack is a write to this channel, so watching it is how something
	 * outside the stack learns of one - the {@link TileFrameRouteParticipant} keeping the address bar
	 * on the displayed path, for instance.
	 * </p>
	 */
	public ViewChannel pathChannel() {
		return _pathChannel;
	}

	/**
	 * The routes the stack declares for its frame views, in declaration order.
	 */
	public List<FrameRoute> frameRoutes() {
		return _frameRoutes;
	}

	/**
	 * The route the stack declares for the given frame view, or {@code null} if it declares none.
	 *
	 * @param viewRef
	 *        Path of a frame view, as {@link TileFrame#getViewRef()} holds it.
	 */
	public FrameRoute frameRoute(String viewRef) {
		return FrameRoute.byView(_frameRoutes, viewRef);
	}

	/**
	 * Appends a frame to the path.
	 *
	 * @param viewRef
	 *        Path of the view file to mount.
	 * @param label
	 *        Breadcrumb label, or {@code null} to use the one the stack declares for the view.
	 * @param params
	 *        Bound parameter values captured at push time.
	 */
	public void push(String viewRef, ResKey label, Map<String, Object> params) {
		List<TileFrame> next = new ArrayList<>(readPath());
		next.add(frame(viewRef, label, params));
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

	/**
	 * Puts the frame a URL names at the given position of the path, dropping whatever the path held
	 * from there on.
	 *
	 * <p>
	 * The path is written in one step, so that the frame the URL names replaces the one displayed
	 * instead of the display passing through the shortened path on its way there.
	 * </p>
	 *
	 * @param base
	 *        Number of frames the URL has already established, and therefore the position of the
	 *        frame it names now.
	 * @param viewRef
	 *        Path of the view file to mount.
	 * @param params
	 *        The parameter values the URL carries for the frame.
	 * @param adoptionId
	 *        Identifies the URL adoption this restore belongs to, so that
	 *        {@link #restoreBase(long)} can tell the frames it establishes from those of the
	 *        adoption before.
	 */
	public void restore(int base, String viewRef, Map<String, Object> params, long adoptionId) {
		List<TileFrame> current = readPath();
		List<TileFrame> next = new ArrayList<>(current.subList(0, Math.max(0, Math.min(base, current.size()))));
		next.add(frame(viewRef, null, params));
		List<TileFrame> path = Collections.unmodifiableList(next);

		// Recorded before the write: writing the path mounts the frame, and the participant of that
		// frame asks for its base while the write is still in progress.
		_restoredAdoption = adoptionId;
		_restoredPath = path;

		_pathChannel.set(path);
	}

	/**
	 * The number of frames of the current path that the given URL adoption has established, and
	 * therefore the position at which the frame it names next belongs.
	 *
	 * <p>
	 * Zero for a path the adoption has not touched: the URL describes the path from its first frame,
	 * so what the display holds from an earlier navigation is replaced rather than extended.
	 * </p>
	 *
	 * @param adoptionId
	 *        Identifies the URL adoption in progress, see
	 *        {@link com.top_logic.layout.react.routing.RouteManager#adoptionId()}.
	 */
	public int restoreBase(long adoptionId) {
		List<TileFrame> current = readPath();
		if (_restoredAdoption != adoptionId || !_restoredPath.equals(current)) {
			return 0;
		}
		return current.size();
	}

	/**
	 * The frame for the given view, named by the given label or, without one, by the label the stack
	 * declares for the view.
	 */
	private TileFrame frame(String viewRef, ResKey label, Map<String, Object> params) {
		if (label != null) {
			return new TileFrame(viewRef, label, params);
		}
		FrameRoute route = frameRoute(viewRef);
		return new TileFrame(viewRef, route == null ? null : route.computeLabel(params), params);
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
