/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.dnd;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.dnd.AcceptedKinds;
import com.top_logic.layout.react.control.dnd.DropEvent;
import com.top_logic.layout.react.control.dnd.DropLocation;
import com.top_logic.layout.react.control.dnd.DropMode;
import com.top_logic.layout.react.control.dnd.DropRequest;
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
 * applies a drop by running the action chain of the drop that accepts it.
 *
 * <p>
 * The {@link #acceptedKinds() accepted kinds} of the binding are those of its enabled drops
 * together, and reach the client, which thereby offers only a drop that can apply; so do the
 * {@link #dropModes() modes} of the enabled drops, which tell the client how to split an item into
 * zones. A drop that arrives is matched against the declared drops in their declaration order:
 * the first drop that accepts its {@link DropRequest#kind() kind} - a drop accepting
 * {@link AcceptedKinds#ANY any} drag matches every kind and a drag without one, a drop listing
 * kinds matches only a drag of one of them -, finds a {@link DropRequest#location(DropMode)
 * location} for its {@link DropMode mode} at the place the drop was made, and is not restricted
 * there applies it.
 * </p>
 *
 * <p>
 * The binding knows nothing about what the control displays: the control resolves the place a drop
 * is made at into a location per mode, and the location of a {@link DropMode#ONTO} drop names the
 * item the drop is made on.
 * </p>
 *
 * <p>
 * A drop is restricted in three stages, each asked only after the previous one accepted: its
 * {@link Drop#executability() control-wide state} decides whether the drop is offered at all - a
 * disabled drop contributes no kind and no mode, and applies nothing; its
 * {@link Drop#targetRule() target rule} decides over the item an {@link DropMode#ONTO} drop is made
 * on; its {@link Drop#refuseIf() refusal function} decides over the target and the dragged objects
 * together. A drop that refuses passes the drop on to the next declared one; where none accepts,
 * the first refusal is what the user is shown, see {@link #check(DropRequest)}.
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
	 * @param mode
	 *        How the drop relates to the items of the control: made on the control as a whole, or
	 *        onto a single item.
	 * @param targetChannel
	 *        The channel the target item is written to before the actions run, or {@code null} if
	 *        the drop declares none. A drop other than {@link DropMode#ONTO} writes {@code null}.
	 * @param actions
	 *        The action chain applying the drop, with the dropped objects as its input.
	 * @param executability
	 *        The control-wide state of the drop, asked anew on every use: while it is not
	 *        executable, the drop is neither announced nor applied.
	 * @param targetRule
	 *        The rule deciding over the item a {@link DropMode#ONTO} drop is made on, the item being
	 *        its input. Not asked for a drop of another mode.
	 * @param refuseIf
	 *        Computes the reason a drop is refused from the target item ({@code null} for a drop
	 *        other than {@link DropMode#ONTO}) and the list of dropped objects; the result is
	 *        interpreted as by {@link DisabledIf#stateFor(Object)}. {@code null} refuses nothing.
	 */
	public record Drop(AcceptedKinds accepted, DropMode mode, ViewChannel targetChannel,
			List<ViewAction> actions, Supplier<ExecutableState> executability, ViewExecutabilityRule targetRule,
			BiFunction<Object, List<?>, Object> refuseIf) {

		/**
		 * Creates an unrestricted {@link Drop}: always enabled, accepting every target.
		 *
		 * @see #Drop(AcceptedKinds, DropMode, ViewChannel, List, Supplier, ViewExecutabilityRule,
		 *      BiFunction)
		 */
		public Drop(AcceptedKinds accepted, DropMode mode, ViewChannel targetChannel,
				List<ViewAction> actions) {
			this(accepted, mode, targetChannel, actions, () -> ExecutableState.EXECUTABLE,
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
		 * Asks the {@link #targetRule() target rule} (for a {@link DropMode#ONTO} drop) and the
		 * {@link #refuseIf() refusal function} about a drop of the given objects on the given
		 * target; the control-wide state is not asked here.
		 *
		 * @return The reason of the first refusal, {@code null} if the drop is accepted.
		 */
		ResKey refusal(Object target, List<?> objects) {
			if (mode == DropMode.ONTO) {
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

	/**
	 * The outcome of matching a drop against the declared drops.
	 *
	 * @param drop
	 *        The declared drop that accepts the drop, {@code null} if none does.
	 * @param location
	 *        The location {@code drop} accepts the drop at, {@code null} if none does.
	 * @param refusal
	 *        The first refusal met on the way, {@code null} if there was none.
	 */
	private record Match(Drop drop, DropLocation location, ResKey refusal) {
		// Pure data.
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
	 * The modes of the declared drops that are currently {@link Drop#isEnabled() enabled}, in
	 * declaration order.
	 */
	@Override
	public Set<DropMode> dropModes() {
		Set<DropMode> result = new LinkedHashSet<>();
		for (Drop drop : _drops) {
			if (drop.isEnabled()) {
				result.add(drop.mode());
			}
		}
		return result;
	}

	/**
	 * Decides which declared drop applies the given drop, and where.
	 *
	 * <p>
	 * The declared drops are tried in declaration order. A drop not accepting the drag's kind, or
	 * finding no location for its mode in the request, is skipped silently; one that is
	 * {@link Drop#isEnabled() disabled}, or whose {@link Drop#targetRule() target rule} or
	 * {@link Drop#refuseIf() refusal function} refuses the drop at its location, is skipped with its
	 * reason. The first drop that accepts wins, and the verdict accepts the drop at its location.
	 * Where none accepts, the verdict is the first refusal met, or a refusal as not accepted where
	 * there was none. Nothing is modified, in particular no target channel is written.
	 * </p>
	 */
	@Override
	public DropVerdict check(DropRequest request) {
		Match match = match(request);
		if (match.drop() != null) {
			return DropVerdict.accepted(match.location());
		}
		return DropVerdict.refused(match.refusal() != null ? match.refusal()
			: com.top_logic.layout.react.I18NConstants.ERROR_DROP_NOT_ACCEPTED);
	}

	/**
	 * Applies the drop through the declared drop that accepts it at its location.
	 *
	 * <p>
	 * The drop is matched as {@link #check(DropRequest)} matches a {@link DropRequest#of(DropEvent)
	 * request of exactly the event's location} - which picks the same declared drop the check of the
	 * place it was made at picked: a drop declared before it either has no location of the event's
	 * mode, or refused that same location. A drop nothing accepts does nothing.
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
	 * nothing accepts.
	 * </p>
	 */
	@Override
	public void onDrop(DropEvent event, Runnable onApplied) {
		Match match = match(DropRequest.of(event));
		if (match.drop() != null) {
			apply(match.drop(), event.objects(), targetOf(match.location()), onApplied);
		}
	}

	/**
	 * Matches the given drop against the declared drops in declaration order, see
	 * {@link #check(DropRequest)}.
	 */
	private Match match(DropRequest request) {
		ResKey firstRefusal = null;
		for (Drop drop : _drops) {
			if (!drop.accepted().accepts(request.kind())) {
				continue;
			}
			DropLocation location = request.location(drop.mode());
			if (location == null) {
				continue;
			}
			ResKey refusal;
			if (!drop.isEnabled()) {
				refusal = reasonOf(drop.executability().get());
				if (refusal == null) {
					// The drop turned enabled between the two lookups; it is refused all the same.
					refusal = I18NConstants.ERROR_DROP_REFUSED;
				}
			} else {
				refusal = drop.refusal(targetOf(location), request.objects());
			}
			if (refusal == null) {
				return new Match(drop, location, null);
			}
			if (firstRefusal == null) {
				firstRefusal = refusal;
			}
		}
		return new Match(null, null, firstRefusal);
	}

	/**
	 * The target item a drop at the given location applies to: the item dropped onto for a
	 * {@link DropLocation.Onto} location, {@code null} for any other.
	 */
	private static Object targetOf(DropLocation location) {
		return location instanceof DropLocation.Onto onto ? onto.target() : null;
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
