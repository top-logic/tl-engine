/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.tiles;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.List;

import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.TreeProperty;
import com.top_logic.layout.form.values.edit.annotation.TitleProperty;
import com.top_logic.layout.view.ViewRef;
import com.top_logic.layout.view.ViewRefFormat;
import com.top_logic.layout.view.routing.WithFinalTitle;

/**
 * Declaration of a frame of a {@link TileStackElement &lt;tile-stack&gt;}: the view it displays, the
 * route in the URL it occupies, and the frames that may follow it.
 *
 * <p>
 * A frame is pushed by naming its declaration: a {@link NavigatePushCommand &lt;navigate-push&gt;}
 * and a display target name it by its {@link #getId() id}, and the view, the parameters and the
 * label of the frame come from here.
 * </p>
 *
 * <p>
 * A drill-down path is part of the address of the page: each frame on the path contributes the route
 * of its declaration, so that the URL names the path the user drilled down and opening it again
 * restores exactly those frames. A declaration without a route is displayed like any other but has
 * no address, and neither has anything drilled into from it.
 * </p>
 *
 * <p>
 * The route is a pattern of static segments and parameter placeholders such as
 * {@code person/:person}: the static segments name what the frame shows, the placeholders carry its
 * {@link #getParams() parameters}. The declaration also carries the {@link #getLabel() label} of the
 * frame, which every frame of that view is named by - the one restored from a URL as well as the one
 * a {@link NavigatePushCommand &lt;navigate-push&gt;} pushes without a label of its own.
 * With {@link #FINAL_TITLE} set, that label stays the title of the page while the frame is displayed,
 * whatever the frame view displays inside - tabs dividing what is shown of the person, for instance.
 * </p>
 *
 * <p>
 * The declarations form a graph of the paths the stack can be addressed with. The declarations
 * directly inside the {@link TileStackElement &lt;tile-stack&gt;} are the frames that may follow its
 * initial view; the {@link #getSuccessors() successors} of a declaration - the
 * {@link FrameRouteConfig &lt;frame&gt;} declarations nested in it and the ones its
 * {@link FrameRefConfig &lt;frame-ref&gt;} entries name by {@link #getId() id} - are the frames that
 * may follow its frame. A declaration without successors is the end of every path it is on. A push
 * names a successor of the declaration its frame was built for, and the address names exactly the
 * paths of this graph: reading it, the first route is looked for among the declarations directly
 * inside the stack, each further one among the successors of the declaration before, and a route
 * found nowhere there ends the path; writing it, a frame that is no successor of the frame below it
 * has no address, nor has anything drilled into from it.
 * </p>
 *
 * <p>
 * Example:
 * </p>
 *
 * <pre>
 * &lt;tile-stack initial="accounts/overview.view.xml" path="navPath"&gt;
 *   &lt;frame id="person" route="person/:person" view="accounts/person-detail.view.xml" final-title="true"&gt;
 *     &lt;param name="person"
 *       expr="objectId($person)"
 *       reverse="$value.objectResolve(`tl.accounts:Person`)"/&gt;
 *     &lt;label class="com.top_logic.layout.view.tiles.ScriptedTileLabel"
 *       expr="$person.get(`tl.accounts:Person#name`)" inputs="person"/&gt;
 *     &lt;frame id="order" route="order/:order" view="accounts/order-detail.view.xml"&gt;
 *       ...
 *     &lt;/frame&gt;
 *   &lt;/frame&gt;
 * &lt;/tile-stack&gt;
 * </pre>
 *
 * <p>
 * Here an order may be drilled into from a person: <code>person/&lt;id&gt;/order/&lt;id&gt;</code>
 * is an address of the stack, <code>order/&lt;id&gt;</code> alone is none.
 * </p>
 */
@TagName(FrameRouteConfig.TAG_NAME)
@TitleProperty(name = FrameRouteConfig.ID)
public interface FrameRouteConfig extends WithFinalTitle, FrameSuccessorConfig {

	/** Access to the default methods of this interface for the configuration proxy. */
	Lookup LOOKUP = MethodHandles.lookup();

	/** Configuration tag of a {@link FrameRouteConfig}. */
	String TAG_NAME = "frame";

	/** Configuration name for {@link #getView()}. */
	String VIEW = "view";

	/** Configuration name for {@link #getRoute()}. */
	String ROUTE = "route";

	/** Configuration name for {@link #getContents()}. */
	String CONTENTS = "contents";

	/** Configuration name for {@link #getLabel()}. */
	String LABEL = "label";

	/**
	 * Path of the view the frame displays, relative to {@code /WEB-INF/views/}.
	 */
	@Name(VIEW)
	@Mandatory
	@Format(ViewRefFormat.class)
	ViewRef getView();

	/**
	 * Pattern of the URL segments the frame occupies.
	 *
	 * <p>
	 * A pattern of static segments and parameter placeholders, {@code person/:person} for instance.
	 * Each placeholder names a parameter of the frame, and the static segments in front of it give
	 * the address a readable prefix that says what the frame shows.
	 * </p>
	 *
	 * <p>
	 * Without a route, the frame has no address: the URL describes the path up to the frame below
	 * it.
	 * </p>
	 */
	@Name(ROUTE)
	@Nullable
	String getRoute();

	/**
	 * Name of this declaration, by which a {@link NavigatePushCommand &lt;navigate-push&gt;} pushes
	 * its frame, a display target drills down to it, and a {@link FrameRefConfig &lt;frame-ref&gt;}
	 * names it as a successor of another declaration.
	 *
	 * <p>
	 * Unique within the stack: a second declaration of the same id is a configuration error.
	 * </p>
	 */
	@Override
	@Name(ID)
	@Mandatory
	String getId();

	/**
	 * The entries of the declaration: its {@link FrameParamConfig &lt;param&gt;} entries and its
	 * successors, the nested {@link FrameRouteConfig &lt;frame&gt;} declarations and
	 * {@link FrameRefConfig &lt;frame-ref&gt;} references.
	 *
	 * @see #getParams()
	 * @see #getSuccessors()
	 */
	@Name(CONTENTS)
	@DefaultContainer
	@Key(ID)
	@TreeProperty
	List<FrameContentConfig> getContents();

	/**
	 * The parameters of the frame among the {@link #getContents() contents}: conversions between
	 * their values and the texts the URL carries, and the parameters taken from the frames below on
	 * the path.
	 *
	 * <p>
	 * One entry per parameter of the {@link #getRoute() route} that is not a text already. A
	 * parameter without an entry is carried as text. An entry naming no placeholder of the route
	 * declares a {@link FrameParamConfig path parameter}.
	 * </p>
	 */
	default List<FrameParamConfig> getParams() {
		return getContents().stream()
			.filter(FrameParamConfig.class::isInstance)
			.map(FrameParamConfig.class::cast)
			.toList();
	}

	/**
	 * The frames that may follow the frame of this declaration, among the {@link #getContents()
	 * contents}: nested {@link FrameRouteConfig &lt;frame&gt;} declarations and
	 * {@link FrameRefConfig &lt;frame-ref&gt;} references to declarations elsewhere in the stack.
	 *
	 * <p>
	 * A push from the frame names one of them, and the address of a path names only the frames that
	 * follow one another this way.
	 * </p>
	 */
	default List<FrameSuccessorConfig> getSuccessors() {
		return getContents().stream()
			.filter(FrameSuccessorConfig.class::isInstance)
			.map(FrameSuccessorConfig.class::cast)
			.toList();
	}

	/**
	 * Provider for the label naming a frame of this view.
	 *
	 * <p>
	 * Evaluated over the parameters of the frame, which the provider reads as channels of their
	 * names: {@link ScriptedTileLabel &lt;scripted&gt;} with
	 * {@code inputs="person" expr="$person.get(`tl.accounts:Person#name`)"} names a frame by the
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
