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
 * The references of a drop are fixed by its {@link DropSignature}. Each reference is an argument of
 * the {@link DropConfig#getRefuseIf() refusal function} and of the action chain of the drop, named
 * {@link #defaultName()} or by the name the {@code <drop>} gives it under {@link #nameProperty()},
 * see {@link DropArguments}. It is written to the channel the {@code <drop>} declares for it under
 * {@link #channelProperty()} before the action chain runs.
 * </p>
 *
 * @param defaultName
 *        The name of the argument holding the reference when the drop gives it no other.
 * @param nameProperty
 *        The name of the {@link DropConfig} property naming the argument.
 * @param name
 *        Reads the name declared under {@code nameProperty} from a drop configuration,
 *        {@code null} if none is declared.
 * @param channelProperty
 *        The name of the {@link DropConfig} property declaring the channel this reference is
 *        written to.
 * @param channel
 *        Reads the channel declared under {@code channelProperty} from a drop configuration,
 *        {@code null} if none is declared.
 * @param value
 *        The object referred to at a location, {@code null} where the location refers to none.
 */
public record DropReference(String defaultName, String nameProperty, Function<DropConfig, String> name,
		String channelProperty, Function<DropConfig, ChannelRef> channel, Function<DropLocation, Object> value) {

	/**
	 * The item a drop is made onto: the {@link DropLocation.Onto#target() target} of an
	 * {@link DropLocation.Onto} location, {@code null} at any other location.
	 */
	public static final DropReference TARGET = new DropReference(DropArguments.TARGET, DropConfig.TARGET_AS,
		DropConfig::getTargetAs, DropConfig.TARGET_CHANNEL,
		DropConfig::getTargetChannel,
		location -> location instanceof DropLocation.Onto onto ? onto.target() : null);

	/**
	 * The object the dropped objects are inserted under: the {@link DropLocation.Insert#parent()
	 * parent} of an {@link DropLocation.Insert} location, {@code null} at any other location.
	 */
	public static final DropReference PARENT = new DropReference(DropArguments.PARENT, DropConfig.PARENT_AS,
		DropConfig::getParentAs, DropConfig.PARENT_CHANNEL,
		DropConfig::getParentChannel,
		location -> location instanceof DropLocation.Insert insert ? insert.parent() : null);

	/**
	 * The item the dropped objects are inserted before: the {@link DropLocation.Insert#before()
	 * before} item of an {@link DropLocation.Insert} location, {@code null} for an insertion at the
	 * end and at any other location.
	 */
	public static final DropReference BEFORE = new DropReference(DropArguments.BEFORE, DropConfig.BEFORE_AS,
		DropConfig::getBeforeAs, DropConfig.BEFORE_CHANNEL,
		DropConfig::getBeforeChannel,
		location -> location instanceof DropLocation.Insert insert ? insert.before() : null);

	/**
	 * All references a {@link DropConfig} can declare a channel or a name for.
	 *
	 * <p>
	 * A {@code <drop>} declaring the channel or the name of a reference its {@link DropSignature}
	 * does not have is a configuration error, see
	 * {@link DeclaredDrop#compile(com.top_logic.basic.config.InstantiationContext, DropConfig, DropSignature)}.
	 * </p>
	 */
	public static final List<DropReference> ALL = List.of(TARGET, PARENT, BEFORE);

}
