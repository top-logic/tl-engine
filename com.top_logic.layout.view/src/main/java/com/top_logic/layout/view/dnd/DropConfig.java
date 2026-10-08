/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.dnd;

import java.util.List;

import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.EntryTag;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.layout.form.values.edit.AllInAppImplementations;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.ChannelRefFormat;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.command.DisabledIf;
import com.top_logic.layout.view.command.ExecutabilityConfig;
import com.top_logic.layout.view.command.ViewAction;
import com.top_logic.layout.view.command.ViewExecutabilityRule;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.util.TLModelPartRef;

/**
 * Configuration of one {@code <drop>} of an element: what it accepts, when it applies, and what it
 * does.
 *
 * <p>
 * What a drop is made on - the element as a whole, or a single item of it - is decided by the
 * element. A drop is restricted in three stages, each asked only after the previous one accepted:
 * the {@link #getExecutability() executability} rules decide for the element as a whole over the
 * value of the {@link #getInput() input} channel, and are followed live - while they refuse, the
 * drop is not offered at all; the {@link #getTargetExecutability() target executability} rules
 * decide over the item a drop is made on; the {@link #getRefuseIf() refusal function} decides over
 * the target and the dragged objects together. While the user drags, the first refusal is shown at
 * the target under the pointer, with its reason.
 * </p>
 *
 * @see DropBinding
 */
@TagName(DropConfig.TAG_NAME)
public interface DropConfig extends ExecutabilityConfig {

	/** The tag a {@link DropConfig} is written with in a list of drops. */
	String TAG_NAME = "drop";

	/** Configuration name for {@link #getAccept()}. */
	String ACCEPT = "accept";

	/** Configuration name for {@link #getTargetChannel()}. */
	String TARGET_CHANNEL = "target-channel";

	/** Configuration name for {@link #getTargetExecutability()}. */
	String TARGET_EXECUTABILITY = "target-executability";

	/** Configuration name for {@link #getRefuseIf()}. */
	String REFUSE_IF = "refuse-if";

	/** Configuration name for {@link #getActions()}. */
	String ACTIONS = "actions";

	/**
	 * The types of the objects this drop accepts.
	 *
	 * <p>
	 * A subtype of an accepted type is accepted as well. Acceptance is decided over the model on
	 * the server; the client is told the resulting set of type names, so a drag it cannot be applied
	 * to is not offered in the first place.
	 * </p>
	 */
	@Name(ACCEPT)
	@Mandatory
	@Format(TLModelPartRef.CommaSeparatedTLModelPartRefs.class)
	List<TLModelPartRef> getAccept();

	/**
	 * A {@link ViewChannel} the target item is written to before the {@link #getActions() actions}
	 * run, so they can read what was dropped on.
	 *
	 * <p>
	 * Unset (default) leaves the target unpublished, which is what a drop on the element as a whole
	 * needs - it has no target item, and writes {@code null} where a channel is declared anyway.
	 * </p>
	 */
	@Name(TARGET_CHANNEL)
	@Format(ChannelRefFormat.class)
	@Nullable
	ChannelRef getTargetChannel();

	/**
	 * Rules deciding on which items the drop may be made, each target item being the input they
	 * decide over.
	 *
	 * <p>
	 * Only a drop on a single item has a target to decide over; declaring rules here for a drop on
	 * the element as a whole is a configuration error. Empty (default) accepts every item.
	 * </p>
	 */
	@Name(TARGET_EXECUTABILITY)
	@EntryTag(RULE)
	List<PolymorphicConfiguration<? extends ViewExecutabilityRule>> getTargetExecutability();

	/**
	 * TL-Script function computing why a drop must not be made, from the target and the dragged
	 * objects: {@code target -> objects -> reason}.
	 *
	 * <p>
	 * The target is the item dropped on, or {@code null} for a drop on the element as a whole; the
	 * objects are the list of dragged objects. No value or <code>false</code> accepts the drop,
	 * <code>true</code> refuses it with a generic reason, a resource key or a text refuses it with
	 * that reason - the same interpretation as the {@link DisabledIf.Config disabled-if} rule. Unset
	 * (default) refuses nothing.
	 * </p>
	 */
	@Name(REFUSE_IF)
	Expr getRefuseIf();

	/**
	 * The chain of actions applying the drop, receiving the dropped objects as the input of its
	 * first action.
	 */
	@Name(ACTIONS)
	@DefaultContainer
	@Options(fun = AllInAppImplementations.class)
	List<PolymorphicConfiguration<? extends ViewAction>> getActions();

}
