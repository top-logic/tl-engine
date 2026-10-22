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
}
