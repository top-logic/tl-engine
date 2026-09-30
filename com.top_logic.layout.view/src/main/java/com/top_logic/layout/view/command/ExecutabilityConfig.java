/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.command;

import java.util.List;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.EntryTag;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.ChannelRefFormat;
import com.top_logic.model.util.TLModelPartRef;

/**
 * Configuration of an operation guarded by {@link ViewExecutabilityRule}s: the input the rules
 * decide over, the rules themselves, and the types whose changes let them decide anew.
 *
 * <p>
 * A {@link ViewCommand} is such an operation. While its UI is displayed, the rules are followed
 * live by a {@link LiveExecutability}.
 * </p>
 */
@Abstract
public interface ExecutabilityConfig extends ConfigurationItem {

	/** Configuration name for {@link #getInput()}. */
	String INPUT = "input";

	/** Configuration name for {@link #getExecutability()}. */
	String EXECUTABILITY = "executability";

	/** Configuration name for {@link #getObservedTypes()}. */
	String OBSERVED_TYPES = "observed-types";

	/** Entry tag of a rule in {@link #getExecutability()}. */
	String RULE = "rule";

	/**
	 * Reference to a channel whose value is the input of the operation: the value it works on, and
	 * the value its {@link #getExecutability() executability} rules decide over.
	 */
	@Name(INPUT)
	@Nullable
	@Format(ChannelRefFormat.class)
	ChannelRef getInput();

	/**
	 * Rules that determine when the operation is executable.
	 */
	@Name(EXECUTABILITY)
	@EntryTag(RULE)
	List<PolymorphicConfiguration<? extends ViewExecutabilityRule>> getExecutability();

	/**
	 * Types whose object changes (create / update / delete) trigger a re-evaluation of the
	 * {@link #getExecutability() executability}, in addition to the {@link #getInput() input}
	 * object, which is always observed.
	 *
	 * <p>
	 * Configure this only for a rule that navigates beyond the input object, e.g. one deciding by an
	 * attribute of the input's container: a change of that other object is invisible to the input's
	 * own observation. Empty (default) observes just the input object.
	 * </p>
	 */
	@Name(OBSERVED_TYPES)
	@Format(TLModelPartRef.CommaSeparatedTLModelPartRefs.class)
	List<TLModelPartRef> getObservedTypes();

}
