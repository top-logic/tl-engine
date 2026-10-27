/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.tiles;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.view.ChildGroup;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelBindingConfig;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.ChannelRefFormat;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.navigation.RevealPath;

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
 * The frame at the end of the path is the one displayed; when the path is empty, that is the
 * {@link Config#getInitial() initial} view. The frames it covers stay as the user left them, so
 * navigating back to one displays it again instead of building it anew. Each frame gets its own
 * isolated channel namespace and a {@link TileStackScope} reachable for descendants so that nested
 * commands can push further frames without explicit configuration. Under the name
 * {@link Config#getBindPathTo() bind-path-to}, a frame additionally sees the path it sits on, and
 * each of the {@link Config#getBindings() bindings} makes a channel of the enclosing view visible in
 * every frame.
 * </p>
 *
 * <p>
 * The frames that can be pushed are declared as {@link FrameRouteConfig &lt;frame&gt;} entries: a
 * push names the declaration by its id, and the declarations nested in one another say which frame
 * may follow which. The drill-down path is part of the address of the page, for every frame whose
 * declaration has a route: the URL names the frames of the path one route each, and opening that
 * URL again restores exactly those frames.
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
 *     &lt;frame id="product" route="product/:product" view="products/detail.view.xml"&gt;
 *       &lt;param name="product"
 *         expr="objectId($product)"
 *         reverse="$value.objectResolve(`my:Product`)"/&gt;
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

		/** Configuration name for {@link #getBindings()}. */
		String BINDINGS = "bindings";

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
		 * it sits - a {@code <derived-channel name="depth" inputs="navPath" expr="$navPath.size()"/>}
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
		 * Channels of the enclosing view that every mounted frame sees.
		 *
		 * <p>
		 * Each binding works as in a {@code <view-ref>}: the channel of the enclosing view named by
		 * {@code to} is registered in every frame under the name {@code channel}, and a frame view
		 * declaring a channel of that name reads and writes the enclosing view's channel. Unlike a
		 * frame parameter, which is a snapshot taken at push time, a bound channel is live: a value a
		 * frame writes is seen by the frames it covers, which stay alive meanwhile - e.g. a list the
		 * initial frame displays and a pushed detail frame changes.
		 * </p>
		 */
		@Name(BINDINGS)
		List<ChannelBindingConfig> getBindings();

		/**
		 * The declarations of the frames that may be pushed onto the initial view.
		 *
		 * <p>
		 * Each declaration names the frames that may follow its own frame in turn, so that the
		 * declarations form the graph of the paths of the stack, see {@link FrameRouteConfig}. A path
		 * of frames with routes is written into the URL, and opening that URL again restores the path
		 * frame by frame.
		 * </p>
		 */
		@Name(FRAMES)
		@DefaultContainer
		@Key(FrameRouteConfig.ID)
		List<FrameRouteConfig> getFrames();
	}

	private final ChannelRef _pathRef;

	private final String _initialViewRef;

	private final String _bindPathTo;

	private final List<ChannelBindingConfig> _bindings;

	private final FrameRoute _frameRoot;

	private final String _cssClass;

	/**
	 * Creates a new {@link TileStackElement} from configuration.
	 */
	@CalledByReflection
	public TileStackElement(InstantiationContext context, Config config) {
		_pathRef = config.getPath();
		_initialViewRef = config.getInitial();
		_bindPathTo = config.getBindPathTo();
		_bindings = config.getBindings();
		_frameRoot = FrameRoute.createGraph(context, config.getInitial(), config.getFrames());
		_cssClass = config.getCssClass();
	}

	@Override
	public List<ChildGroup> getChildGroups() {
		return List.of(ChildGroup.view(Config.INITIAL, _initialViewRef));
	}

	@Override
	public IReactControl createControl(ViewContext context) {
		ViewChannel pathChannel = context.resolveChannel(_pathRef);
		TileStackScope scope = new TileStackScope(pathChannel, _frameRoot, RevealPath.of(context), this);
		Map<String, ViewChannel> sharedChannels = new LinkedHashMap<>();
		if (_bindPathTo != null) {
			sharedChannels.put(_bindPathTo, pathChannel);
		}
		for (ChannelBindingConfig binding : _bindings) {
			sharedChannels.put(binding.getChannel(), context.resolveChannel(binding.getTo()));
		}
		ReactTileStackControl result =
			new ReactTileStackControl(context, this, pathChannel, scope, _initialViewRef, sharedChannels);
		result.setCssClass(_cssClass);
		return result;
	}
}
