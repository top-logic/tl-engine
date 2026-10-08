/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.dnd;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.dnd.AcceptedKinds;
import com.top_logic.layout.react.control.dnd.DropEvent;
import com.top_logic.layout.react.control.dnd.DropTarget;
import com.top_logic.layout.react.control.dnd.DropVerdict;
import com.top_logic.layout.view.I18NConstants;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.command.DisabledIf;
import com.top_logic.layout.view.command.ViewAction;
import com.top_logic.layout.view.command.ViewActionChain;
import com.top_logic.layout.view.command.ViewExecutabilityRule;
import com.top_logic.tool.execution.ExecutableState;

/**
 * The {@link DropTarget} of an element's declared drops: it accepts what the drops declare, and
 * applies a drop by running the action chain of the drop that matches it.
 *
 * <p>
 * The {@link #acceptedKinds() accepted kinds} of the binding are those of its enabled drops
 * together, and reach the client, which thereby offers only a drop that can apply. The drop that
 * arrives is matched by its {@link DropEvent#kind() kind} again - per declared drop this time, to
 * find the one that applies: a drop accepting {@link AcceptedKinds#ANY any} drag matches every
 * kind and a drag without one, a drop listing kinds matches only a drag of one of them.
 * </p>
 *
 * <p>
 * The binding knows nothing about what the control displays: a drop on a single item names that
 * item as the {@link DropEvent#target() target} of the event, and the {@link DropScope} of each
 * declared drop says whether it is made on such an item or on the control as a whole.
 * </p>
 *
 * <p>
 * A drop is restricted in three stages, each asked only after the previous one accepted: its
 * {@link Drop#executability() control-wide state} decides whether the drop is offered at all - a
 * disabled drop contributes no kind and applies nothing; its {@link Drop#targetRule() target rule}
 * decides over the item an {@link DropScope#ITEM item} drop is made on; its
 * {@link Drop#refuseIf() refusal function} decides over the target and the dragged objects together.
 * The first refusal is what the user is shown while dragging, see {@link #check(DropEvent)}.
 * </p>
 *
 * @see DropConfig
 * @see DeclaredDrop
 */
public class DropBinding implements DropTarget {

	/**
	 * One declared drop.
	 *
	 * @param accepted
	 *        The kinds of the drags the drop accepts.
	 * @param scope
	 *        Whether the control as a whole or a single item is the target.
	 * @param targetChannel
	 *        The channel the target item is written to before the actions run, or {@code null} if
	 *        the drop declares none. A {@link DropScope#CONTROL} drop writes {@code null}.
	 * @param actions
	 *        The action chain applying the drop, with the dropped objects as its input.
	 * @param executability
	 *        The control-wide state of the drop, asked anew on every use: while it is not
	 *        executable, the drop is neither announced nor applied.
	 * @param targetRule
	 *        The rule deciding over the item a {@link DropScope#ITEM} drop is made on, the item
	 *        being its input. Not asked for a {@link DropScope#CONTROL} drop.
	 * @param refuseIf
	 *        Computes the reason a drop is refused from the target item ({@code null} for a drop on
	 *        the control as a whole) and the list of dropped objects; the result is interpreted as by
	 *        {@link DisabledIf#stateFor(Object)}. {@code null} refuses nothing.
	 */
	public record Drop(AcceptedKinds accepted, DropScope scope, ViewChannel targetChannel,
			List<ViewAction> actions, Supplier<ExecutableState> executability, ViewExecutabilityRule targetRule,
			BiFunction<Object, List<?>, Object> refuseIf) {

		/**
		 * Creates an unrestricted {@link Drop}: always enabled, accepting every target.
		 *
		 * @see #Drop(AcceptedKinds, DropScope, ViewChannel, List, Supplier, ViewExecutabilityRule,
		 *      BiFunction)
		 */
		public Drop(AcceptedKinds accepted, DropScope scope, ViewChannel targetChannel,
				List<ViewAction> actions) {
			this(accepted, scope, targetChannel, actions, () -> ExecutableState.EXECUTABLE,
				ViewExecutabilityRule.ALWAYS_EXECUTABLE, null);
		}

		/**
		 * Whether the {@link #executability() control-wide state} currently lets the drop be
		 * offered.
		 */
		public boolean isEnabled() {
			return executability.get().isExecutable();
		}

		/**
		 * Asks the {@link #targetRule() target rule} (for a {@link DropScope#ITEM} drop) and the
		 * {@link #refuseIf() refusal function} about a drop of the given objects on the given
		 * target; the control-wide state is not asked here.
		 *
		 * @return The reason of the first refusal, {@code null} if the drop is accepted.
		 */
		ResKey refusal(Object target, List<?> objects) {
			if (scope == DropScope.ITEM) {
				ResKey refusal = reasonOf(targetRule.isExecutable(target));
				if (refusal != null) {
					return refusal;
				}
			}
			if (refuseIf != null) {
				return reasonOf(DisabledIf.stateFor(refuseIf.apply(target, objects)));
			}
			return null;
		}
	}

	private final ReactContext _context;

	private final List<Drop> _drops;

	/**
	 * Creates a {@link DropBinding}.
	 *
	 * @param context
	 *        The context the action chains run in.
	 * @param drops
	 *        The declared drops, in declaration order - which is the order a drop is matched
	 *        against them in.
	 */
	public DropBinding(ReactContext context, List<Drop> drops) {
		_context = context;
		_drops = drops;
	}

	/**
	 * The kinds accepted by any of the declared drops that are currently {@link Drop#isEnabled()
	 * enabled}.
	 */
	@Override
	public AcceptedKinds acceptedKinds() {
		AcceptedKinds result = AcceptedKinds.NONE;
		for (Drop drop : _drops) {
			if (drop.isEnabled()) {
				result = result.union(drop.accepted());
			}
		}
		return result;
	}

	/**
	 * Whether one of the currently {@link Drop#isEnabled() enabled} drops targets a single item.
	 */
	@Override
	public boolean dropOnRows() {
		for (Drop drop : _drops) {
			if (drop.scope() == DropScope.ITEM && drop.isEnabled()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Decides over the drop {@link #onDrop(DropEvent)} would apply.
	 *
	 * <p>
	 * The drop is matched exactly as {@link #onDrop(DropEvent)} matches it, among the enabled drops,
	 * and its {@link Drop#targetRule() target rule} and its {@link Drop#refuseIf() refusal function}
	 * are asked in this order; the first refusal is the verdict. Where no enabled drop matches, a
	 * matching disabled one gives the reason of its {@link Drop#executability() control-wide state};
	 * a drop nothing matches at all is refused as not accepted. Nothing is modified, in particular
	 * no target channel is written.
	 * </p>
	 */
	@Override
	public DropVerdict check(DropEvent event) {
		Drop drop = select(event, true);
		ResKey refusal;
		if (drop != null) {
			refusal = drop.refusal(targetOf(drop, event), event.objects());
		} else {
			Drop disabled = select(event, false);
			refusal = disabled == null ? com.top_logic.layout.react.I18NConstants.ERROR_DROP_NOT_ACCEPTED
				: reasonOf(disabled.executability().get());
			if (refusal == null) {
				// The drop turned enabled between the two lookups; it is refused all the same.
				refusal = I18NConstants.ERROR_DROP_REFUSED;
			}
		}
		return refusal == null ? DropVerdict.ACCEPTED : DropVerdict.refused(refusal);
	}

	/**
	 * Applies the drop through the first enabled declared drop that matches it.
	 *
	 * <p>
	 * A drop made on an item is offered to the {@link DropScope#ITEM item} drops first, and falls
	 * back to a {@link DropScope#CONTROL control} drop when none of them accepts the drag - a control
	 * whose items are targets for drags of one kind still accepts drags of another kind as a whole, wherever
	 * the pointer happened to be. A drop nothing accepts does nothing, and neither does one whose
	 * matching declared drop is {@link Drop#isEnabled() disabled}.
	 * </p>
	 */
	@Override
	public void onDrop(DropEvent event) {
		onDrop(event, null);
	}

	/**
	 * Applies the drop as {@link #onDrop(DropEvent)} does, and runs the given follow-up once the
	 * action chain of the matching drop has run to its end.
	 *
	 * <p>
	 * The follow-up runs as a last step of the chain: after an action that waits for the user, it
	 * runs once the chain resumes, and an aborted chain does not run it - neither does a drop
	 * nothing matches.
	 * </p>
	 */
	@Override
	public void onDrop(DropEvent event, Runnable onApplied) {
		Drop drop = select(event, true);
		if (drop != null) {
			apply(drop, event.objects(), targetOf(drop, event), onApplied);
		}
	}

	/**
	 * The declared drop that applies the given drop: the first item drop accepting its
	 * {@link DropEvent#kind() kind} for a drop made on an item, otherwise the first such control
	 * drop; {@code null} if none matches.
	 *
	 * @param enabledOnly
	 *        Whether only the currently {@link Drop#isEnabled() enabled} drops are considered.
	 */
	private Drop select(DropEvent event, boolean enabledOnly) {
		String kind = event.kind();
		if (event.target() != null) {
			Drop itemDrop = matching(DropScope.ITEM, kind, enabledOnly);
			if (itemDrop != null) {
				return itemDrop;
			}
		}
		return matching(DropScope.CONTROL, kind, enabledOnly);
	}

	private Drop matching(DropScope scope, String kind, boolean enabledOnly) {
		for (Drop drop : _drops) {
			if (drop.scope() == scope && drop.accepted().accepts(kind) && (!enabledOnly || drop.isEnabled())) {
				return drop;
			}
		}
		return null;
	}

	/**
	 * The target the given declared drop applies a drop to: the item dropped on for an item drop,
	 * {@code null} for a drop on the control as a whole.
	 */
	private static Object targetOf(Drop drop, DropEvent event) {
		return drop.scope() == DropScope.ITEM ? event.target() : null;
	}

	/**
	 * The reason a drop is refused by the given state, {@code null} if the state is executable.
	 *
	 * <p>
	 * A state disabled for a reason of its own gives that reason; a hidden state, or one disabled
	 * without a specific reason, gives the generic {@link I18NConstants#ERROR_DROP_REFUSED}.
	 * </p>
	 */
	static ResKey reasonOf(ExecutableState state) {
		if (state.isExecutable()) {
			return null;
		}
		ResKey reason = state.getI18NReasonKey();
		if (!state.isDisabled() || reason == null || reason == ResKey.NONE
			|| reason == ExecutableState.NOT_EXEC_DISABLED_REASON) {
			return I18NConstants.ERROR_DROP_REFUSED;
		}
		return reason;
	}

	private void apply(Drop drop, List<?> objects, Object target, Runnable onApplied) {
		ViewChannel targetChannel = drop.targetChannel();
		if (targetChannel != null) {
			targetChannel.set(target);
		}
		List<ViewAction> actions = drop.actions();
		if (onApplied != null) {
			actions = new ArrayList<>(actions);
			actions.add((context, input) -> {
				onApplied.run();
				return input;
			});
		}
		ViewActionChain.run(_context, actions, objects, null);
	}

}
