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
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.ChannelRefFormat;
import com.top_logic.layout.view.channel.ViewChannel;

/**
 * UIElement that renders an outward navigation trail for a {@link TileStackElement tile stack}.
 *
 * <p>
 * Reads the same path channel as the stack. The trail consists of the home entry followed by one
 * crumb per frame on the path; the last crumb represents the currently displayed view.
 * </p>
 *
 * <p>
 * The breadcrumb writes a truncated path back to the channel when the user clicks a crumb - it
 * does not need access to a {@link TileStackScope}, so it can be placed freely (sibling, header,
 * sidebar, dedicated view linked via {@code <bind>}).
 * </p>
 */
public class TileBreadcrumbElement implements UIElement {

	/**
	 * Configuration for {@link TileBreadcrumbElement}.
	 */
	@TagName("tile-breadcrumb")
	public interface Config extends UIElement.Config {

		@Override
		@ClassDefault(TileBreadcrumbElement.class)
		Class<? extends UIElement> getImplementationClass();

		/** Configuration name for {@link #getPath()}. */
		String PATH = "path";

		/** Configuration name for {@link #getHomeLabel()}. */
		String HOME_LABEL = "home-label";

		/**
		 * Reference to the channel holding the {@code List<TileFrame>} path.
		 */
		@Name(PATH)
		@Mandatory
		@Format(ChannelRefFormat.class)
		ChannelRef getPath();

		/**
		 * Label of the home crumb (rendered as the first entry, always navigates to an empty
		 * path).
		 */
		@Name(HOME_LABEL)
		@Nullable
		ResKey getHomeLabel();
	}

	private final ChannelRef _pathRef;

	private final ResKey _homeLabel;

	/**
	 * Creates a new {@link TileBreadcrumbElement} from configuration.
	 */
	@CalledByReflection
	public TileBreadcrumbElement(InstantiationContext context, Config config) {
		_pathRef = config.getPath();
		_homeLabel = config.getHomeLabel();
	}

	@Override
	public IReactControl createControl(ViewContext context) {
		ViewChannel pathChannel = context.resolveChannel(_pathRef);
		return new ReactTileBreadcrumbControl(context, pathChannel, _homeLabel);
	}
}
