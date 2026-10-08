/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.dnd;

import java.util.List;
import java.util.function.Function;

import com.top_logic.layout.react.control.dnd.DropLocation;
import com.top_logic.layout.view.channel.ChannelRef;

/**
 * One object a drop refers to at its {@link DropLocation}, such as the item dropped onto or the
 * item the dropped objects are inserted before.
 *
 * <p>
 * The references of a drop are fixed by its {@link DropSignature}. Each reference is handed to the
 * {@link DropConfig#getRefuseIf() refusal function} as a leading argument, and is written to the
 * channel the {@code <drop>} declares for it under {@link #channelProperty()} before the action
 * chain runs.
 * </p>
 *
 * @param channelProperty
 *        The name of the {@link DropConfig} property declaring the channel this reference is
 *        written to.
 * @param channel
 *        Reads the channel declared under {@code channelProperty} from a drop configuration,
 *        {@code null} if none is declared.
 * @param value
 *        The object referred to at a location, {@code null} where the location refers to none.
 */
public record DropReference(String channelProperty, Function<DropConfig, ChannelRef> channel,
		Function<DropLocation, Object> value) {

	/**
	 * The item a drop is made onto: the {@link DropLocation.Onto#target() target} of an
	 * {@link DropLocation.Onto} location, {@code null} at any other location.
	 */
	public static final DropReference TARGET = new DropReference(DropConfig.TARGET_CHANNEL,
		DropConfig::getTargetChannel,
		location -> location instanceof DropLocation.Onto onto ? onto.target() : null);

	/**
	 * The object the dropped objects are inserted under: the {@link DropLocation.Insert#parent()
	 * parent} of an {@link DropLocation.Insert} location, {@code null} at any other location.
	 */
	public static final DropReference PARENT = new DropReference(DropConfig.PARENT_CHANNEL,
		DropConfig::getParentChannel,
		location -> location instanceof DropLocation.Insert insert ? insert.parent() : null);

	/**
	 * The item the dropped objects are inserted before: the {@link DropLocation.Insert#before()
	 * before} item of an {@link DropLocation.Insert} location, {@code null} for an insertion at the
	 * end and at any other location.
	 */
	public static final DropReference BEFORE = new DropReference(DropConfig.BEFORE_CHANNEL,
		DropConfig::getBeforeChannel,
		location -> location instanceof DropLocation.Insert insert ? insert.before() : null);

	/**
	 * All references a {@link DropConfig} can declare a channel for.
	 *
	 * <p>
	 * A {@code <drop>} declaring the channel of a reference its {@link DropSignature} does not have
	 * is a configuration error, see
	 * {@link DeclaredDrop#compile(com.top_logic.basic.config.InstantiationContext, DropConfig, DropSignature)}.
	 * </p>
	 */
	public static final List<DropReference> ALL = List.of(TARGET, PARENT, BEFORE);

}
