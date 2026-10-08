/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.dnd;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.dnd.AcceptedKinds;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.command.LiveExecutability;
import com.top_logic.layout.view.command.ViewAction;
import com.top_logic.layout.view.command.ViewExecutabilityRules;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.tool.execution.ExecutableState;

/**
 * A {@link DropConfig} instantiated for the element declaring it: its action chain instantiated,
 * its refusal function compiled, and the {@link DropScope} the element assigns it.
 *
 * <p>
 * An element compiles its drops once, when it is instantiated, and {@link #bind(ViewContext,
 * ReactControl, Runnable, List) binds} them to every control it creates.
 * </p>
 *
 * @param config
 *        What the drop accepts, when it applies and where it publishes its target.
 * @param scope
 *        Whether the drop is made on the control as a whole or on a single item of it.
 * @param actions
 *        The instantiated action chain applying the drop.
 * @param refuseIf
 *        The compiled {@link DropConfig#getRefuseIf()}, {@code null} without one.
 */
public record DeclaredDrop(DropConfig config, DropScope scope, List<ViewAction> actions, QueryExecutor refuseIf) {

	/**
	 * Instantiates the action chain of the given drop and compiles its refusal function, so that
	 * applying the drop only has to run them.
	 *
	 * @param context
	 *        The context the actions are instantiated in, and where problems are reported.
	 * @param config
	 *        The declared drop.
	 * @param scope
	 *        Whether the element makes the drop on the control as a whole or on a single item.
	 */
	public static DeclaredDrop compile(InstantiationContext context, DropConfig config, DropScope scope) {
		List<ViewAction> actions = config.getActions().stream()
			.<ViewAction> map(actionConfig -> context.getInstance(actionConfig))
			.filter(action -> action != null)
			.toList();
		Expr refuseIf = config.getRefuseIf();
		return new DeclaredDrop(config, scope, actions, refuseIf == null ? null : QueryExecutor.compile(refuseIf));
	}

	/**
	 * The {@link DropBinding} applying the given drops to the given control, resolved for the
	 * session of the given context: the target channels against the view, the rules against the
	 * context.
	 *
	 * <p>
	 * The control-wide {@link DropConfig#getExecutability() executability} of each drop is followed
	 * while the control is displayed, and a change of it runs the given refresh.
	 * </p>
	 *
	 * @param context
	 *        The context of the view the control is displayed in.
	 * @param control
	 *        The control accepting the drops.
	 * @param refresh
	 *        Announces the {@link DropBinding#acceptedKinds() accepted kinds} and the item targeting
	 *        of the binding to the client again.
	 * @param drops
	 *        The compiled drops, in declaration order.
	 */
	public static DropBinding bind(ViewContext context, ReactControl control, Runnable refresh,
			List<DeclaredDrop> drops) {
		List<DropBinding.Drop> resolved = new ArrayList<>(drops.size());
		for (DeclaredDrop drop : drops) {
			resolved.add(drop.resolve(context, control, refresh));
		}
		return new DropBinding(context, resolved);
	}

	/**
	 * The kinds this drop accepts: those {@link DropConfig#getAccept() declared}, every drag where
	 * none is.
	 */
	public AcceptedKinds acceptedKinds() {
		List<String> accept = config.getAccept();
		return accept.isEmpty() ? AcceptedKinds.ANY : AcceptedKinds.of(accept);
	}

	/**
	 * This drop resolved for the session of the given context.
	 *
	 * @see #bind(ViewContext, ReactControl, Runnable, List)
	 */
	private DropBinding.Drop resolve(ViewContext context, ReactControl control, Runnable refresh) {
		ChannelRef targetChannelRef = config.getTargetChannel();

		Supplier<ExecutableState> executability;
		if (config.getExecutability().isEmpty()) {
			executability = () -> ExecutableState.EXECUTABLE;
		} else {
			LiveExecutability live = LiveExecutability.create(config, context, refresh);
			live.followWhileDisplayed(context, control, refresh);
			executability = live::getState;
		}

		QueryExecutor compiledRefuseIf = refuseIf;
		return new DropBinding.Drop(acceptedKinds(), scope,
			targetChannelRef == null ? null : context.resolveChannel(targetChannelRef),
			actions,
			executability,
			ViewExecutabilityRules.build(config.getTargetExecutability(), context),
			compiledRefuseIf == null ? null : (target, objects) -> compiledRefuseIf.execute(target, objects));
	}

}
