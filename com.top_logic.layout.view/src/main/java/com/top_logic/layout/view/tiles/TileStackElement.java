/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.tiles;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
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
 * commands can push further frames without explicit configuration.
 * </p>
 *
 * <p>
 * Example:
 * </p>
 *
 * <pre>
 * &lt;view&gt;
 *   &lt;channels&gt;&lt;channel name="navPath"/&gt;&lt;/channels&gt;
 *   &lt;tile-stack path="navPath" initial="products/overview.view.xml"/&gt;
 * &lt;/view&gt;
 * </pre>
 */
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
	}

	private final ChannelRef _pathRef;

	private final String _initialViewRef;

	/**
	 * Creates a new {@link TileStackElement} from configuration.
	 */
	@CalledByReflection
	public TileStackElement(InstantiationContext context, Config config) {
		_pathRef = config.getPath();
		_initialViewRef = config.getInitial();
	}

	@Override
	public IReactControl createControl(ViewContext context) {
		ViewChannel pathChannel = context.resolveChannel(_pathRef);
		TileStackScope scope = new TileStackScope(pathChannel);
		return new ReactTileStackControl(context, pathChannel, scope, _initialViewRef);
	}
}
