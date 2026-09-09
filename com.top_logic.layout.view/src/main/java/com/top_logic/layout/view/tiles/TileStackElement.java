/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.tiles;

import java.util.List;
import java.util.stream.Collectors;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.ChannelRefFormat;
import com.top_logic.layout.view.channel.ViewChannel;

/**
 * UIElement that renders a stack of drilled-down views.
 *
 * <p>
 * The stack maintains a path of {@link TileFrame}s in the channel referenced by
 * {@link Config#getPath()}. The path is the single source of truth: both push (from a
 * {@link NavigatePushCommand &lt;navigate-push&gt;} via the installed {@link TileStackScope}) and
 * pop (e.g. from a {@link TileBreadcrumbElement &lt;tile-breadcrumb&gt;} writing a shorter prefix
 * to the channel) work by mutating the channel value.
 * </p>
 *
 * <p>
 * Only the top frame is rendered. When the path is empty, the
 * {@link Config#getInitial() initial} view is shown. Each mounted frame gets its own isolated
 * channel namespace and a {@link TileStackScope} reachable for descendants so that nested
 * commands can push further frames without explicit configuration. Under the name
 * {@link Config#getBindPathTo() bind-path-to}, a frame additionally sees the path it sits on.
 * </p>
 *
 * <p>
 * The drill-down path is part of the address of the page, for every frame view the stack declares a
 * {@link FrameRouteConfig &lt;frame&gt;} route for: the URL names the frames of the path one route
 * each, and opening that URL again restores exactly those frames.
 * </p>
 *
 * <p>
 * Example:
 * </p>
 *
 * <pre>
 * &lt;view&gt;
 *   &lt;channels&gt;&lt;channel name="navPath"/&gt;&lt;/channels&gt;
 *   &lt;tile-stack path="navPath" initial="products/overview.view.xml"&gt;
 *     &lt;frame route="product/:product" view="products/detail.view.xml"&gt;
 *       &lt;param name="product"
 *         expr="p -&gt; objectId($p)"
 *         reverse="id -&gt; objectResolve(`my:Product`, $id)"/&gt;
 *     &lt;/frame&gt;
 *   &lt;/tile-stack&gt;
 * &lt;/view&gt;
 * </pre>
 */
@InApp
public class TileStackElement implements UIElement {

	/**
	 * Configuration for {@link TileStackElement}.
	 */
	@TagName("tile-stack")
	public interface Config extends UIElement.Config {

		@Override
		@ClassDefault(TileStackElement.class)
		Class<? extends UIElement> getImplementationClass();

		/** Configuration name for {@link #getPath()}. */
		String PATH = "path";

		/** Configuration name for {@link #getInitial()}. */
		String INITIAL = "initial";

		/** Configuration name for {@link #getBindPathTo()}. */
		String BIND_PATH_TO = "bind-path-to";

		/** Configuration name for {@link #getFrames()}. */
		String FRAMES = "frames";

		/**
		 * Reference to the channel holding the {@code List<TileFrame>} path.
		 *
		 * <p>
		 * The channel must be declared in an enclosing view. The breadcrumb and any URL persistence
		 * use the same channel as their source of truth.
		 * </p>
		 */
		@Name(PATH)
		@Mandatory
		@Format(ChannelRefFormat.class)
		ChannelRef getPath();

		/**
		 * Path of the view file shown when the stack is empty (the home frame), relative to
		 * {@code /WEB-INF/views/}.
		 */
		@Name(INITIAL)
		@Mandatory
		String getInitial();

		/**
		 * Name under which every mounted frame sees the path channel of this stack.
		 *
		 * <p>
		 * The frame gets the stack's own channel, not a copy: a frame view declaring a
		 * {@code <channel name="..."/>} of that name reads the live path. It thereby knows how deep
		 * it sits - a {@code <derived-channel name="depth" inputs="navPath" expr="p -&gt; $p.size()"/>}
		 * turns the path into a number a command's
		 * {@link com.top_logic.layout.view.command.VisibleIf &lt;visible-if&gt;} rule can test, so
		 * a "Back" button hides itself on the initial frame.
		 * </p>
		 *
		 * <p>
		 * Without a name, a frame sees only the parameters it was pushed with.
		 * </p>
		 */
		@Name(BIND_PATH_TO)
		@Nullable
		String getBindPathTo();

		/**
		 * The routes in the URL that the frames of this stack occupy.
		 *
		 * <p>
		 * One entry per frame view whose frames are part of the address of the page. A path of such
		 * frames is written into the URL, and opening that URL again restores the path frame by
		 * frame; a frame view without an entry is displayed like any other but has no address.
		 * </p>
		 */
		@Name(FRAMES)
		@DefaultContainer
		List<FrameRouteConfig> getFrames();
	}

	private final ChannelRef _pathRef;

	private final String _initialViewRef;

	private final String _bindPathTo;

	private final List<FrameRoute> _frameRoutes;

	/**
	 * Creates a new {@link TileStackElement} from configuration.
	 */
	@CalledByReflection
	public TileStackElement(InstantiationContext context, Config config) {
		_pathRef = config.getPath();
		_initialViewRef = config.getInitial();
		_bindPathTo = config.getBindPathTo();
		_frameRoutes = config.getFrames().stream()
			.map(frame -> FrameRoute.create(context, frame))
			.collect(Collectors.toList());
	}

	@Override
	public IReactControl createControl(ViewContext context) {
		ViewChannel pathChannel = context.resolveChannel(_pathRef);
		TileStackScope scope = new TileStackScope(pathChannel, _frameRoutes);
		return new ReactTileStackControl(context, pathChannel, scope, _initialViewRef, _bindPathTo);
	}
}
