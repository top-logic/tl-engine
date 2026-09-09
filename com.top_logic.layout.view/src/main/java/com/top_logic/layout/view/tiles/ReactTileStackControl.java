/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.tiles;

import java.util.List;
import java.util.Map;

import com.top_logic.basic.Logger;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.layout.react.control.ErrorSink;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.reveal.ChildRevealer;
import com.top_logic.layout.view.DefaultViewContext;
import com.top_logic.layout.view.ReloadableControl;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.ViewElement;
import com.top_logic.layout.view.ViewLoader;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.channel.DirtyChannel;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.channel.ViewChannel.ChannelListener;
import com.top_logic.layout.view.navigation.RevealPath;
import com.top_logic.layout.view.navigation.RevealRegistry;

/**
 * Server-side control of {@link TileStackElement}.
 *
 * <p>
 * Subscribes to the path channel; whenever its value changes, rebuilds the top frame and sends it
 * to the client as the {@code frame} child in the React state. Empty path → renders the
 * {@code initial} view; non-empty path → renders the view referenced by the top frame, with the
 * frame's {@link TileFrame#getParams() params} pre-registered as channels in the frame's child
 * context.
 * </p>
 *
 * <p>
 * Each frame is wrapped in a {@link ReloadableControl} so that view-file edits in the designer
 * propagate to the active frame.
 * </p>
 */
public class ReactTileStackControl extends ReactControl implements ChildRevealer {

	private static final String REACT_MODULE = "TLTileStack";

	private static final String FRAME = "frame";

	/**
	 * Prefix of the key addressing a pushed frame, followed by the frame's position in the path.
	 *
	 * @see #frameKey(int)
	 */
	public static final String FRAME_KEY_PREFIX = "frame";

	private final ViewContext _parentContext;

	private final TileStackElement _element;

	private final RevealPath _here;

	private final ViewChannel _pathChannel;

	private final TileStackScope _scope;

	private final String _initialViewPath;

	private final ChannelListener _pathListener;

	private ReactControl _currentChild;

	/**
	 * Creates a new {@link ReactTileStackControl}.
	 *
	 * @param parent
	 *        The {@link ViewContext} in which the {@code <tile-stack>} is embedded. Used to
	 *        inherit ambient services (error sink, dirty channel) into each frame's child context.
	 * @param element
	 *        The configured element this control displays, addressed when one of its frames is to
	 *        be brought into view.
	 * @param pathChannel
	 *        The channel holding the {@code List<TileFrame>} path.
	 * @param scope
	 *        The {@link TileStackScope} installed into each frame so that nested commands can push.
	 * @param initialViewRef
	 *        View path (relative to {@code /WEB-INF/views/}) shown when the path is empty.
	 */
	public ReactTileStackControl(ViewContext parent, TileStackElement element, ViewChannel pathChannel,
			TileStackScope scope, String initialViewRef) {
		super(parent, null, REACT_MODULE);
		_parentContext = parent;
		_element = element;
		_here = RevealPath.of(parent);
		_pathChannel = pathChannel;
		_scope = scope;
		_initialViewPath = ViewLoader.VIEW_BASE_PATH + initialViewRef;

		_pathListener = (sender, oldValue, newValue) -> rebuildChild();
		_pathChannel.addListener(_pathListener);
		addCleanupAction(() -> _pathChannel.removeListener(_pathListener));

		RevealRegistry registry = parent.getRevealRegistry();
		if (registry != null) {
			addCleanupAction(registry.registerContainer(element, _here, this));
		}

		_currentChild = buildChild();
		putState(FRAME, _currentChild);
	}

	/**
	 * The key addressing the frame at the given position of the path.
	 *
	 * @param index
	 *        Position of the frame in the path, counted from the frame pushed first.
	 * @return The key, as {@link #revealChild(String)} takes it.
	 */
	public static String frameKey(int index) {
		return FRAME_KEY_PREFIX + index;
	}

	/**
	 * Returns to the initial view or to a frame the path already holds, by dropping everything
	 * drilled down beyond it.
	 *
	 * @param key
	 *        {@link TileStackElement.Config#INITIAL} for the view the stack starts with, otherwise
	 *        a {@link #frameKey(int) frame key}.
	 */
	@Override
	public void revealChild(String key) {
		_scope.popTo(framePosition(key) + 1);
	}

	/**
	 * The position the given key addresses in the path, {@code -1} for the initial view.
	 */
	private static int framePosition(String key) {
		if (TileStackElement.Config.INITIAL.equals(key)) {
			return -1;
		}
		if (key.startsWith(FRAME_KEY_PREFIX)) {
			try {
				return Integer.parseInt(key.substring(FRAME_KEY_PREFIX.length()));
			} catch (NumberFormatException ex) {
				throw new IllegalArgumentException("A stack of drilled-down views has no frame '" + key + "'.", ex);
			}
		}
		throw new IllegalArgumentException("A stack of drilled-down views has no child '" + key + "'.");
	}

	private void rebuildChild() {
		ReactControl built;
		try {
			built = buildChild();
		} catch (RuntimeException ex) {
			Logger.error("Failed to build tile frame.", ex, ReactTileStackControl.class);
			return;
		}
		ReactControl old = _currentChild;
		_currentChild = built;
		putState(FRAME, built);
		if (old != null) {
			old.cleanupTree();
		}
	}

	private ReactControl buildChild() {
		List<TileFrame> path = _scope.getPath();
		String viewPath;
		Map<String, Object> params;
		String key;
		if (path.isEmpty()) {
			viewPath = _initialViewPath;
			params = Map.of();
			key = TileStackElement.Config.INITIAL;
		} else {
			TileFrame top = path.get(path.size() - 1);
			viewPath = ViewLoader.VIEW_BASE_PATH + top.getViewRef();
			params = top.getParams();
			key = frameKey(path.size() - 1);
		}

		ViewElement frameView;
		try {
			frameView = ViewLoader.getOrLoadView(viewPath);
		} catch (ConfigurationException ex) {
			throw new RuntimeException("Failed to load tile frame view: " + viewPath, ex);
		}

		ViewContext frameContext = new DefaultViewContext(getReactContext());
		ErrorSink parentErrorSink = _parentContext.getErrorSink();
		if (parentErrorSink != null) {
			frameContext = frameContext.withErrorSink(parentErrorSink);
		}
		DirtyChannel parentDirty = _parentContext.getDirtyChannel();
		if (parentDirty != null) {
			frameContext.setDirtyChannel(parentDirty);
		}
		frameContext = frameContext.withScope(TileStackScope.class, _scope)
			.withScope(RevealPath.class, _here.append(_element, key));

		for (Map.Entry<String, Object> entry : params.entrySet()) {
			DefaultViewChannel paramChannel = new DefaultViewChannel(entry.getKey());
			paramChannel.set(entry.getValue());
			frameContext.registerChannel(entry.getKey(), paramChannel);
		}

		return new ReloadableControl(viewPath, frameContext,
			(ReactControl) frameView.createControl(frameContext));
	}

}
