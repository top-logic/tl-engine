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
import com.top_logic.layout.react.routing.RouteManager;
import com.top_logic.layout.view.DefaultViewContext;
import com.top_logic.layout.view.ReloadableControl;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.ViewElement;
import com.top_logic.layout.view.ViewLoader;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.channel.DirtyChannel;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.channel.ViewChannel.ChannelListener;

/**
 * Server-side control of {@link TileStackElement}.
 *
 * <p>
 * Subscribes to the path channel; whenever its value changes, rebuilds the top frame and sends it
 * to the client as the {@code frame} child in the React state. Empty path → renders the
 * {@code initial} view; non-empty path → renders the view referenced by the top frame, with the
 * frame's {@link TileFrame#getParams() params} pre-registered as channels in the frame's child
 * context - and, where the stack names one, the path channel itself.
 * </p>
 *
 * <p>
 * Each frame is wrapped in a {@link ReloadableControl} so that view-file edits in the designer
 * propagate to the active frame. Where the stack declares
 * {@link TileStackElement.Config#getFrames() frame routes}, that control also carries the
 * {@link TileFrameRouteParticipant} which writes the path into the URL and restores it from one.
 * </p>
 */
public class ReactTileStackControl extends ReactControl {

	private static final String REACT_MODULE = "TLTileStack";

	private static final String FRAME = "frame";

	private final ViewContext _parentContext;

	private final ViewChannel _pathChannel;

	private final TileStackScope _scope;

	private final String _initialViewPath;

	private final String _bindPathTo;

	private final ChannelListener _pathListener;

	private ReactControl _currentChild;

	/**
	 * Creates a new {@link ReactTileStackControl}.
	 *
	 * @param parent
	 *        The {@link ViewContext} in which the {@code <tile-stack>} is embedded. Used to
	 *        inherit ambient services (error sink, dirty channel) into each frame's child context.
	 * @param pathChannel
	 *        The channel holding the {@code List<TileFrame>} path.
	 * @param scope
	 *        The {@link TileStackScope} installed into each frame so that nested commands can push.
	 * @param initialViewRef
	 *        View path (relative to {@code /WEB-INF/views/}) shown when the path is empty.
	 * @param bindPathTo
	 *        Channel name under which each frame sees {@code pathChannel}, or {@code null} to keep
	 *        the path out of the frames' channel namespace.
	 */
	public ReactTileStackControl(ViewContext parent, ViewChannel pathChannel, TileStackScope scope,
			String initialViewRef, String bindPathTo) {
		super(parent, null, REACT_MODULE);
		_parentContext = parent;
		_pathChannel = pathChannel;
		_scope = scope;
		_initialViewPath = ViewLoader.VIEW_BASE_PATH + initialViewRef;
		_bindPathTo = bindPathTo;

		_pathListener = (sender, oldValue, newValue) -> exchangeFrame();
		_pathChannel.addListener(_pathListener);
		addCleanupAction(() -> _pathChannel.removeListener(_pathListener));

		_currentChild = buildChild();
		putState(FRAME, _currentChild);
	}

	/**
	 * Exchanges the displayed frame for the one the path now names.
	 *
	 * <p>
	 * Applied as a navigation where the URL follows the display: drilling down and coming back out
	 * are steps the user takes and returns from with the back button, and they are one step each
	 * however many participants the exchanged frames bring and take with them.
	 * </p>
	 */
	private void exchangeFrame() {
		RouteManager routeManager = getReactContext().getRouteManager();
		if (routeManager != null) {
			routeManager.navigate(this::rebuildChild);
		} else {
			rebuildChild();
		}
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

		// The frame that is gone goes first, then the one that takes its place: what the leaving frame
		// contributed to its surroundings - the participant naming the path it displayed, above all -
		// belongs to that frame, and left in place beside the arriving one it is read as belonging to
		// it. Attached here rather than left to be attached when it is rendered, because a URL being
		// taken up is resolved by the participants the display registers, and the display it registers
		// them from is built now.
		if (isAttached()) {
			built.attach();
		}
	}

	private ReactControl buildChild() {
		List<TileFrame> path = _scope.getPath();
		String viewPath;
		Map<String, Object> params;
		if (path.isEmpty()) {
			viewPath = _initialViewPath;
			params = Map.of();
		} else {
			TileFrame top = path.get(path.size() - 1);
			viewPath = ViewLoader.VIEW_BASE_PATH + top.getViewRef();
			params = top.getParams();
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
		frameContext = frameContext.withScope(TileStackScope.class, _scope);

		if (_bindPathTo != null) {
			// The stack's own channel, so the frame sees the path it sits on change.
			frameContext.registerChannel(_bindPathTo, _pathChannel);
		}
		for (Map.Entry<String, Object> entry : params.entrySet()) {
			DefaultViewChannel paramChannel = new DefaultViewChannel(entry.getKey());
			paramChannel.set(entry.getValue());
			frameContext.registerChannel(entry.getKey(), paramChannel);
		}

		ReloadableControl frame = new ReloadableControl(viewPath, frameContext,
			(ReactControl) frameView.createControl(frameContext));
		installRouteParticipant(frame);
		return frame;
	}

	/**
	 * Anchors the {@link TileFrameRouteParticipant} of the given frame at its control.
	 *
	 * <p>
	 * Anchored rather than implemented by the frame's control, because the participant describes the
	 * path of the stack, not the frame view: it enters the URL where the stack sits in the display
	 * and leaves it with the frame that carries it. Registration follows the frame's attach and
	 * detach, so that a stack inside a tab contributes to the address exactly while its tab is shown.
	 * </p>
	 */
	private void installRouteParticipant(ReactControl frame) {
		if (_scope.frameRoutes().isEmpty()) {
			// The stack declares no addressable frame: its path is display state only.
			return;
		}
		RouteManager routeManager = getReactContext().getRouteManager();
		if (routeManager == null) {
			return;
		}

		TileFrameRouteParticipant participant = new TileFrameRouteParticipant(_scope, routeManager);
		frame.addRouteParticipant(participant);
		frame.addAttachListener(() -> routeManager.register(participant));
		frame.addDetachListener(() -> routeManager.unregister(participant));
		frame.addCleanupAction(participant::dispose);
	}

}
