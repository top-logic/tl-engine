/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.tiles;

import java.util.List;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.layout.form.values.edit.annotation.TitleProperty;

/**
 * The route in the URL that one frame of a {@link TileStackElement &lt;tile-stack&gt;} occupies.
 *
 * <p>
 * A drill-down path is part of the address of the page: each frame on the path contributes the route
 * declared for its view, so that the URL names the path the user drilled down and opening it again
 * restores exactly those frames. A frame view without a declaration is displayed like any other but
 * has no address, and neither has anything drilled into from it.
 * </p>
 *
 * <p>
 * The route is a pattern of static segments and parameter placeholders such as
 * {@code person/:person}: the static segments name what the frame shows, the placeholders carry its
 * {@link #getParams() parameters}. The declaration also carries the {@link #getLabel() label} of the
 * frame, which every frame of that view is named by - the one restored from a URL as well as the one
 * a {@link NavigatePushCommand &lt;navigate-push&gt;} pushes without a label of its own.
 * </p>
 *
 * <p>
 * Example:
 * </p>
 *
 * <pre>
 * &lt;tile-stack initial="accounts/overview.view.xml" path="navPath"&gt;
 *   &lt;frame route="person/:person" view="accounts/person-detail.view.xml"&gt;
 *     &lt;param name="person"
 *       expr="p -&gt; objectId($p)"
 *       reverse="id -&gt; objectResolve(`tl.accounts:Person`, $id)"/&gt;
 *     &lt;label class="com.top_logic.layout.view.tiles.ScriptedTileLabel"
 *       expr="p -&gt; $p.get(`tl.accounts:Person#name`)" inputs="person"/&gt;
 *   &lt;/frame&gt;
 * &lt;/tile-stack&gt;
 * </pre>
 */
@TagName(FrameRouteConfig.TAG_NAME)
@TitleProperty(name = FrameRouteConfig.VIEW)
public interface FrameRouteConfig extends ConfigurationItem {

	/** Configuration tag of a {@link FrameRouteConfig}. */
	String TAG_NAME = "frame";

	/** Configuration name for {@link #getView()}. */
	String VIEW = "view";

	/** Configuration name for {@link #getRoute()}. */
	String ROUTE = "route";

	/** Configuration name for {@link #getParams()}. */
	String PARAMS = "params";

	/** Configuration name for {@link #getLabel()}. */
	String LABEL = "label";

	/**
	 * Path of the frame view this route describes, relative to {@code /WEB-INF/views/}.
	 *
	 * <p>
	 * The same reference a frame of that view is pushed with, so that a frame on the path finds the
	 * route describing it.
	 * </p>
	 */
	@Name(VIEW)
	@Mandatory
	String getView();

	/**
	 * Pattern of the URL segments the frame occupies.
	 *
	 * <p>
	 * A pattern of static segments and parameter placeholders, {@code person/:person} for instance.
	 * Each placeholder names a parameter of the frame, and the static segments in front of it give
	 * the address a readable prefix that says what the frame shows.
	 * </p>
	 */
	@Name(ROUTE)
	@Mandatory
	String getRoute();

	/**
	 * Conversions between the parameter values of the frame and the texts the URL carries.
	 *
	 * <p>
	 * One entry per parameter of the {@link #getRoute() route} that is not a text already. A
	 * parameter without an entry is carried as text.
	 * </p>
	 */
	@Name(PARAMS)
	@DefaultContainer
	List<FrameParamConfig> getParams();

	/**
	 * Provider for the label naming a frame of this view.
	 *
	 * <p>
	 * Evaluated over the parameters of the frame, which the provider reads as channels of their
	 * names: {@link ScriptedTileLabel &lt;scripted&gt;} with
	 * {@code inputs="person" expr="p -&gt; $p.get(`tl.accounts:Person#name`)"} names a frame by the
	 * object it displays, {@link StaticTileLabel &lt;static&gt;} by fixed text.
	 * </p>
	 *
	 * <p>
	 * The declaration names every frame of this view: the one a URL restores, and the one a
	 * {@link NavigatePushCommand &lt;navigate-push&gt;} pushes without a frame label of its own -
	 * which is what lets a frame be named once, at the stack, for every place that pushes it.
	 * </p>
	 */
	@Name(LABEL)
	@Nullable
	PolymorphicConfiguration<? extends TileLabelProvider> getLabel();
}
