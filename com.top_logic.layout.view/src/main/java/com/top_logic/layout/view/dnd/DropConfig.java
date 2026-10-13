/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.dnd;

import java.util.List;

import com.top_logic.basic.config.CommaSeparatedStrings;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.EntryTag;
import com.top_logic.basic.config.annotation.Format;
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

/**
 * Configuration of one {@code <drop>} of an element: what it accepts, when it applies, and what it
 * does.
 *
 * <p>
 * What a drop is made on - the element as a whole, a single item of it, or a place in the order of
 * its items - is decided by the element, and so are the objects the drop refers to there: the item
 * dropped onto as its target, the object the dropped objects are inserted under as their parent (in
 * a tree), the item the dropped objects are inserted before. A drop is restricted in three stages, each asked only after the previous one accepted:
 * the {@link #getExecutability() executability} rules decide for the element as a whole over the
 * value of the {@link #getInput() input} channel, and are followed live - while they refuse, the
 * drop is not offered at all; the {@link #getTargetExecutability() target executability} rules
 * decide over the item a drop is made on; the {@link #getRefuseIf() refusal function} decides over
 * the dragged objects and the target together. While the user drags, the first refusal is shown at
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

	/** Configuration name for {@link #getParentChannel()}. */
	String PARENT_CHANNEL = "parent-channel";

	/** Configuration name for {@link #getBeforeChannel()}. */
	String BEFORE_CHANNEL = "before-channel";

	/** Configuration name for {@link #getTargetExecutability()}. */
	String TARGET_EXECUTABILITY = "target-executability";

	/** Configuration name for {@link #getRefuseIf()}. */
	String REFUSE_IF = "refuse-if";

	/** Configuration name for {@link #getActions()}. */
	String ACTIONS = "actions";

	/**
	 * The {@link DragConfig#getKind() kinds} of the drags this drop accepts.
	 *
	 * <p>
	 * Empty (default), the drop accepts every drag, of any kind or none. Otherwise it accepts
	 * exactly the drags of a listed kind; a drag without a kind is then refused. The client is told
	 * the accepted kinds, so a drag the drop cannot take is not offered to it in the first place.
	 * Which objects the drop takes beyond that - for instance only those of a certain model type -
	 * is decided by the {@link #getRefuseIf() refusal function}.
	 * </p>
	 */
	@Name(ACCEPT)
	@Format(CommaSeparatedStrings.class)
	List<String> getAccept();

	/**
	 * A {@link ViewChannel} the target item is written to before the {@link #getActions() actions}
	 * run, so they can read what was dropped on.
	 *
	 * <p>
	 * Unset (default) leaves the target unpublished. A drop on the element as a whole has no target
	 * item, and writes {@code null} where a channel is declared anyway. An insertion has no target
	 * item either, but the item it inserts before, see {@link #getBeforeChannel()}; declaring a
	 * target channel there is a configuration error.
	 * </p>
	 */
	@Name(TARGET_CHANNEL)
	@Format(ChannelRefFormat.class)
	@Nullable
	ChannelRef getTargetChannel();

	/**
	 * A {@link ViewChannel} the object the dropped objects are inserted under is written to before
	 * the {@link #getActions() actions} run, so they can add the objects to its children.
	 *
	 * <p>
	 * Only an insertion into a tree has such a parent: the object whose children the dropped
	 * objects become, which is the object the tree is built from for an insertion among the
	 * top-level nodes - or {@code null} where that object is displayed as a node itself. Declaring
	 * this channel for any other drop, including an insertion into a flat list, is a configuration
	 * error. Unset (default) leaves the parent unpublished.
	 * </p>
	 */
	@Name(PARENT_CHANNEL)
	@Format(ChannelRefFormat.class)
	@Nullable
	ChannelRef getParentChannel();

	/**
	 * A {@link ViewChannel} the item the dropped objects are inserted before is written to before
	 * the {@link #getActions() actions} run, so they can place the objects there.
	 *
	 * <p>
	 * Only an insertion - a drop at a place in the order of the items - has such an item; it is
	 * {@code null} for an insertion at the end. Declaring this channel for any other drop is a
	 * configuration error. Unset (default) leaves the item unpublished.
	 * </p>
	 */
	@Name(BEFORE_CHANNEL)
	@Format(ChannelRefFormat.class)
	@Nullable
	ChannelRef getBeforeChannel();

	/**
	 * Rules deciding on which items the drop may be made, each target item being the input they
	 * decide over.
	 *
	 * <p>
	 * Only a drop onto a single item has a target to decide over; declaring rules here for any
	 * other drop is a configuration error. Empty (default) accepts every item.
	 * </p>
	 */
	@Name(TARGET_EXECUTABILITY)
	@EntryTag(RULE)
	List<PolymorphicConfiguration<? extends ViewExecutabilityRule>> getTargetExecutability();

	/**
	 * TL-Script function computing why a drop must not be made, from the dragged objects and the
	 * objects the drop refers to at its place.
	 *
	 * <p>
	 * The first argument is the list of dragged objects. The objects the drop refers to follow it,
	 * and depend on what the drop is made on: a drop on the element as a whole or onto a single item
	 * gets the target, {@code objects -> target -> reason}, which is the item dropped onto, or
	 * {@code null} for a drop on the element as a whole; an insertion into a list gets the item the
	 * objects are inserted before, {@code objects -> before -> reason}, which is {@code null} for an
	 * insertion at the end; an insertion into a tree gets the object the objects are inserted under
	 * before it, {@code objects -> parent -> before -> reason}. A function deciding over the dragged
	 * objects alone is written {@code objects -> reason} for every drop: surplus arguments of a
	 * function with fewer parameters are ignored.
	 * </p>
	 *
	 * <p>
	 * No value or <code>false</code> accepts the drop, <code>true</code> refuses it with a generic reason, a resource key or a text
	 * refuses it with that reason - the same interpretation as the {@link DisabledIf.Config
	 * disabled-if} rule. Unset (default) refuses nothing.
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
