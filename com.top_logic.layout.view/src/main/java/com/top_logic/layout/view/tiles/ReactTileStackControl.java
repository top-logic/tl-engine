/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.tiles;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.Logger;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.layout.react.control.ErrorSink;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.ScriptingControl;
import com.top_logic.layout.react.reveal.ChildRevealer;
import com.top_logic.layout.react.routing.RouteManager;
import com.top_logic.layout.view.DefaultViewContext;
import com.top_logic.layout.view.ReloadableControl;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.ViewElement;
import com.top_logic.layout.view.ViewLoader;
import com.top_logic.layout.view.channel.ChannelNotificationScope;
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
 * Subscribes to the path channel and holds one frame per position: the {@code initial} view at
 * index 0, followed by the frames of the path, each rendering the view its {@link TileFrame}
 * references with the frame's {@link TileFrame#getParams() params} pre-registered as channels in
 * its child context - and, where the stack names one, the path channel itself. The frame at the end
 * of the path is the active one; the client displays it and keeps the frames below it, so that
 * returning to one - by a {@link TileBreadcrumbElement breadcrumb} click or a
 * {@link NavigatePopCommand pop} - shows it as the user left it rather than building it anew.
 * </p>
 *
 * <p>
 * A path change keeps the frames of the longest common prefix and disposes the ones the new path
 * drops, so a path that a URL reconstructs keeps the frames it names ({@link TileFrame} compares by
 * view, label and params).
 * </p>
 *
 * <p>
 * Each frame is wrapped in a {@link ReloadableControl} so that view-file edits in the designer
 * propagate to it. Where the stack declares {@link TileStackElement.Config#getFrames() frame
 * routes}, the stack carries a {@link TileFrameRouteParticipant} which writes the path into the URL
 * and restores it from one. Only the active frame is {@link #visibleChildren() visible}: a covered
 * frame is rendered, but what it contains neither names anything in the URL nor takes up a route.
 * </p>
 */
public class ReactTileStackControl extends ReactControl implements ChildRevealer {

	private static final String REACT_MODULE = "TLTileStack";

	private static final String FRAMES = "frames";

	private static final String ACTIVE_INDEX = "activeIndex";

	/** Addresses a frame by its position on the stack, the initial view being position 0. */
	private static final String FRAME_SLOT = "frame";

	/**
	 * Prefix of the key addressing a pushed frame, followed by the frame's position in the path.
	 *
	 * @see #frameKey(int)
	 */
	public static final String FRAME_KEY_PREFIX = "frame";

	private final ViewContext _parentContext;

	/** The element this control displays, the container a frame is addressed through. */
	private final TileStackElement _element;

	/** The position of this stack in the display, which every frame extends by its own key. */
	private final RevealPath _here;

	private final ViewChannel _pathChannel;

	private final TileStackScope _scope;

	private final String _initialViewPath;

	private final String _bindPathTo;

	private final ChannelListener _pathListener;

	/**
	 * The path the {@link #_frameControls} beyond the initial view were built for.
	 */
	private final List<TileFrame> _frames = new ArrayList<>();

	/**
	 * The frame controls: index 0 renders the initial view, index {@code i + 1} the
	 * {@link #_frames} entry {@code i}.
	 */
	private final List<ReactControl> _frameControls = new ArrayList<>();

	/**
	 * Creates a new {@link ReactTileStackControl}.
	 *
	 * @param parent
	 *        The {@link ViewContext} in which the {@code <tile-stack>} is embedded. Used to
	 *        inherit ambient services (error sink, dirty channel) into each frame's child context.
	 * @param element
	 *        The element this control displays, the container a frame is addressed through.
	 * @param pathChannel
	 *        The channel holding the {@code List<TileFrame>} path.
	 * @param scope
	 *        The {@link TileStackScope} installed into each frame so that nested commands can push.
	 * @param initialViewRef
	 *        View path (relative to {@code /WEB-INF/views/}) of the frame shown when the path is
	 *        empty.
	 * @param bindPathTo
	 *        Channel name under which each frame sees {@code pathChannel}, or {@code null} to keep
	 *        the path out of the frames' channel namespace.
	 */
	public ReactTileStackControl(ViewContext parent, TileStackElement element, ViewChannel pathChannel,
			TileStackScope scope, String initialViewRef, String bindPathTo) {
		super(parent, null, REACT_MODULE);
		_parentContext = parent;
		_element = element;
		_here = RevealPath.of(parent);
		_pathChannel = pathChannel;
		_scope = scope;
		_initialViewPath = ViewLoader.VIEW_BASE_PATH + initialViewRef;
		_bindPathTo = bindPathTo;

		_pathListener = (sender, oldValue, newValue) -> exchangeFrames();
		_pathChannel.addListener(_pathListener);
		addCleanupAction(() -> _pathChannel.removeListener(_pathListener));

		RevealRegistry registry = parent.getRevealRegistry();
		if (registry != null) {
			addCleanupAction(registry.registerContainer(element, _here, this));
		}

		installRouteParticipant();

		_frameControls.add(buildFrame(_initialViewPath, Map.of(), TileStackElement.Config.INITIAL));
		publishFrames();
		updateFrames();
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

	/**
	 * Brings the frames in line with a path that has changed.
	 *
	 * <p>
	 * Applied as a navigation where the URL follows the display: drilling down and coming back out
	 * are steps the user takes and returns from with the back button, and they are one step each
	 * however many participants the frames built and dropped bring and take with them.
	 * </p>
	 */
	private void exchangeFrames() {
		RouteManager routeManager = getReactContext().getRouteManager();
		if (routeManager != null) {
			routeManager.navigate(this::updateFrames);
		} else {
			updateFrames();
		}
	}

	/**
	 * Brings the frames in line with the current path and publishes them.
	 */
	private void updateFrames() {
		List<TileFrame> path = _scope.getPath();
		int keep = commonPrefixSize(path);
		if (keep == _frames.size() && keep == path.size()) {
			// The path already describes the frames held; nothing to build, drop or publish.
			return;
		}

		List<ReactControl> dropped = new ArrayList<>(_frames.size() - keep);
		while (_frames.size() > keep) {
			_frames.remove(_frames.size() - 1);
			dropped.add(_frameControls.remove(_frameControls.size() - 1));
		}
		for (int i = keep; i < path.size(); i++) {
			TileFrame frame = path.get(i);
			ReactControl control;
			try {
				control = buildFrame(ViewLoader.VIEW_BASE_PATH + frame.getViewRef(), frame.getParams(), frameKey(i));
			} catch (RuntimeException ex) {
				Logger.error("Failed to build tile frame.", ex, ReactTileStackControl.class);
				break;
			}
			_frames.add(frame);
			_frameControls.add(control);

			// Attached now rather than when it is rendered, because a URL being taken up is resolved by
			// the participants the display registers, and the display it registers them from is built
			// now: the frame a route restores must be able to take up the next route itself.
			if (isAttached()) {
				control.attach();
			}
		}

		publishFrames();

		// A dropped frame may hold listeners of the very channel whose notification is running, so
		// its disposal waits until that notification has unwound.
		ChannelNotificationScope.current()
			.afterNotification(() -> dropped.forEach(ReactControl::cleanupTree));
	}

	/**
	 * Sends the frames held and the position of the displayed one to the client.
	 */
	private void publishFrames() {
		Object tx = beginUpdate();
		putState(FRAMES, new ArrayList<>(_frameControls));
		putState(ACTIVE_INDEX, Integer.valueOf(_frames.size()));
		commitUpdate(tx);
	}

	/**
	 * The number of leading frames the given path shares with the frames currently held, and whose
	 * controls therefore stay.
	 */
	private int commonPrefixSize(List<TileFrame> path) {
		int max = Math.min(_frames.size(), path.size());
		int common = 0;
		while (common < max && _frames.get(common).equals(path.get(common))) {
			common++;
		}
		return common;
	}

	/**
	 * Creates the control of one frame: the view at the given path, in a child context carrying the
	 * given params as channels.
	 */
	private ReactControl buildFrame(String viewPath, Map<String, Object> params, String key) {
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

		if (_bindPathTo != null) {
			// The stack's own channel, so the frame sees the path it sits on change.
			frameContext.registerChannel(_bindPathTo, _pathChannel);
		}
		for (Map.Entry<String, Object> entry : params.entrySet()) {
			DefaultViewChannel paramChannel = new DefaultViewChannel(entry.getKey());
			paramChannel.set(entry.getValue());
			frameContext.registerChannel(entry.getKey(), paramChannel);
		}

		return new ReloadableControl(viewPath, frameContext,
			(ReactControl) frameView.createControl(frameContext));
	}

	/**
	 * Anchors the {@link TileFrameRouteParticipant} of this stack at its control.
	 *
	 * <p>
	 * The participant describes the path of the stack, not a frame view: it enters the URL where the
	 * stack sits in the display, before whatever the active frame contributes. Registration follows
	 * the stack's attach and detach, so that a stack inside a tab contributes to the address exactly
	 * while its tab is shown.
	 * </p>
	 */
	private void installRouteParticipant() {
		if (_scope.frameRoutes().isEmpty()) {
			// The stack declares no addressable frame: its path is display state only.
			return;
		}
		RouteManager routeManager = getReactContext().getRouteManager();
		if (routeManager == null) {
			return;
		}

		TileFrameRouteParticipant participant = new TileFrameRouteParticipant(_scope, routeManager);
		addRouteParticipant(participant);
		addAttachListener(() -> routeManager.register(participant));
		addDetachListener(() -> routeManager.unregister(participant));
		addCleanupAction(participant::dispose);
	}

	/**
	 * The active frame alone: a covered frame is rendered so that it keeps its state, but the user
	 * does not see it, so nothing in it names a segment of the URL or takes up one.
	 */
	@Override
	public List<ReactControl> visibleChildren() {
		if (_frameControls.isEmpty()) {
			return List.of();
		}
		return List.of(_frameControls.get(_frameControls.size() - 1));
	}

	/**
	 * Addresses a frame by its position on the stack (e.g. {@code frame[0]} for the initial view),
	 * so that content addresses encode the frame they belong to.
	 */
	@Override
	public String scriptingChildSlot(ReactControl child) {
		int position = _frameControls.indexOf(child);
		if (position < 0) {
			return null;
		}
		return ScriptingControl.slotSegment(FRAME_SLOT, Integer.toString(position));
	}

}
