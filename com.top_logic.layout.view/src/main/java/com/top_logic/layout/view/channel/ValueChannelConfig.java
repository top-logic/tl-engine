/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.channel;

import java.util.List;

import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.ui.ScriptContextVariables;
import com.top_logic.model.util.TLModelPartRef;

/**
 * Configuration for a mutable value channel.
 *
 * <p>
 * A value channel holds a single mutable value that can be read and written by UI elements.
 * Without an {@link #getInit() initialization function}, it starts empty. With one, it is filled
 * from that function over its {@link #getInputs() inputs} when it is created, and filled again
 * whenever an input takes a new value or an object an input holds is changed; in between it holds
 * what was last written into it.
 * </p>
 *
 * <p>
 * A {@link #isPersonalize() personalized} channel remembers the value last written into it per
 * user and starts with that value in a later session.
 * </p>
 *
 * <pre>
 * &lt;channel name="budget"
 *   inputs="project"
 *   init="$project.get(`tl.demo.projectManagement:ProjectScope#budget`)"
 * /&gt;
 * </pre>
 */
@TagName("channel")
public interface ValueChannelConfig extends ChannelConfig, Inputs {

	@Override
	@ClassDefault(ValueChannelFactory.class)
	Class<? extends ChannelFactory> getImplementationClass();

	/** Configuration name for {@link #getInit()}. */
	String INIT = "init";

	/** Configuration name for {@link #getObservedTypes()}. */
	String OBSERVED_TYPES = "observed-types";

	/** Configuration name for {@link #isPersonalize()}. */
	String PERSONALIZE = "personalize";

	/**
	 * Configuration name for {@link #getPersonalizationKey()}, the same as the one of
	 * {@link UIElement.Config#getPersonalizationKey()}.
	 */
	String PERSONALIZATION_KEY = UIElement.Config.PERSONALIZATION_KEY;

	/**
	 * TL-Script function computing the value the channel is filled with.
	 *
	 * <p>
	 * The function sees the values of the {@link #getInputs() inputs} as variables named after their
	 * channels, like the expression of a derived channel, see {@link InputScript}. It is applied when the channel is created, whenever
	 * an input takes a new value, and whenever an object an input holds (or an object of one of the
	 * {@link #getObservedTypes() observed types}) is changed in a way that changes its result. The
	 * channel stays writable: between two fills it holds the value an element or action last wrote.
	 * </p>
	 *
	 * <p>
	 * Without inputs, the expression is evaluated once, when the channel is created, which gives
	 * the channel an initial value - unless {@link #getObservedTypes() observed types} are
	 * configured, whose changes evaluate it again.
	 * </p>
	 *
	 * <p>
	 * Empty (default) for a channel that starts empty and is only written by elements and actions.
	 * </p>
	 */
	@Name(INIT)
	@Label("Initialization")
	@ScriptContextVariables(InputScript.Variables.class)
	Expr getInit();

	/**
	 * Types whose object changes (create / update / delete) evaluate the {@link #getInit()
	 * initialization function} again, in addition to the objects the {@link #getInputs() inputs}
	 * hold, which are always observed.
	 *
	 * <p>
	 * Configure this only for a function that navigates beyond the input objects. Requires an
	 * {@link #getInit() initialization function}.
	 * </p>
	 */
	@Name(OBSERVED_TYPES)
	@Format(TLModelPartRef.CommaSeparatedTLModelPartRefs.class)
	List<TLModelPartRef> getObservedTypes();

	/**
	 * Whether the value last written into the channel is remembered per user.
	 *
	 * <p>
	 * When the channel is created in a later session of the same user, it starts with the
	 * remembered value instead of the value of the {@link #getInit() initialization function}.
	 * Writing an empty value forgets the remembered one, so that the channel starts with the value
	 * of the initialization function again. Values are remembered as long as they can be named
	 * stably: texts, numbers, truth values, dates, model elements (types, attributes,
	 * classifiers), persistent objects, and lists, sets and maps of those. A remembered value that
	 * no longer exists (a deleted object, a removed classifier) is forgotten, and the channel
	 * starts with the value of the initialization function. Any other value is not remembered.
	 * </p>
	 */
	@Name(PERSONALIZE)
	@Label("Remember value per user")
	boolean isPersonalize();

	/**
	 * The key the value of a {@link #isPersonalize() personalized} channel is remembered under.
	 *
	 * <p>
	 * Channels with the same key share the remembered value, also across views. Empty (default)
	 * for the key derived from the position of the view and the channel name. Only used for a
	 * {@link #isPersonalize() personalized} channel.
	 * </p>
	 *
	 * @see ViewContext#getPersonalizationKey()
	 */
	@Name(PERSONALIZATION_KEY)
	String getPersonalizationKey();
}
