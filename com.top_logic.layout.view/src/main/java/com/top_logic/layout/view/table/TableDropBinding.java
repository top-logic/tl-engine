/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.table;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.dnd.DragSourceControl;
import com.top_logic.layout.react.control.dnd.DropEvent;
import com.top_logic.layout.react.control.dnd.DropTarget;
import com.top_logic.layout.react.control.dnd.DropVerdict;
import com.top_logic.layout.view.I18NConstants;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.command.DisabledIf;
import com.top_logic.layout.view.command.ViewAction;
import com.top_logic.layout.view.command.ViewActionChain;
import com.top_logic.layout.view.command.ViewExecutabilityRule;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLType;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.tool.execution.ExecutableState;

/**
 * The {@link DropTarget} of a declared table: it accepts what the table's drops declare, and applies
 * a drop by running the action chain of the drop that matches it.
 *
 * <p>
 * Acceptance is decided over the model here, where the model is known, and reaches the client as a
 * plain set of type tags: a declared type contributes its own qualified name and those of all its
 * subtypes, so the client's comparison of tags accepts a subtype exactly as this side does. The
 * client thereby offers only a drop that can apply, and the drop that arrives is matched against the
 * same tags again - per declared drop this time, to find the one that applies.
 * </p>
 *
 * <p>
 * A drop is restricted in three stages, each asked only after the previous one accepted: its
 * {@link Drop#executability() table-wide state} decides whether the drop is offered at all - a
 * disabled drop contributes no tag and applies nothing; its {@link Drop#targetRule() target rule}
 * decides over the row a {@link DropTargetMode#ROW row} drop is made on; its
 * {@link Drop#refuseIf() refusal function} decides over the target and the dragged objects together.
 * The first refusal is what the user is shown while dragging, see {@link #check(DropEvent)}.
 * </p>
 *
 * @see DropTargetMode
 */
public class TableDropBinding implements DropTarget {

	/**
	 * One declared drop of the table.
	 *
	 * @param acceptedTags
	 *        The qualified names of the accepted types and of their subtypes.
	 * @param mode
	 *        Whether the table or a single row is the target.
	 * @param targetChannel
	 *        The channel the target row is written to before the actions run, or {@code null} if
	 *        the drop declares none. A {@link DropTargetMode#TABLE} drop writes {@code null}.
	 * @param actions
	 *        The action chain applying the drop, with the dropped objects as its input.
	 * @param executability
	 *        The table-wide state of the drop, asked anew on every use: while it is not
	 *        executable, the drop is neither announced nor applied.
	 * @param targetRule
	 *        The rule deciding over the row a {@link DropTargetMode#ROW} drop is made on, the row
	 *        being its input. Not asked for a {@link DropTargetMode#TABLE} drop.
	 * @param refuseIf
	 *        Computes the reason a drop is refused from the target row ({@code null} for a drop on
	 *        the table) and the list of dropped objects; the result is interpreted as by
	 *        {@link DisabledIf#stateFor(Object)}. {@code null} refuses nothing.
	 */
	public record Drop(Set<String> acceptedTags, DropTargetMode mode, ViewChannel targetChannel,
			List<ViewAction> actions, Supplier<ExecutableState> executability, ViewExecutabilityRule targetRule,
			BiFunction<Object, List<?>, Object> refuseIf) {

		/**
		 * Creates an unrestricted {@link Drop}: always enabled, accepting every target.
		 *
		 * @see #Drop(Set, DropTargetMode, ViewChannel, List, Supplier, ViewExecutabilityRule,
		 *      BiFunction)
		 */
		public Drop(Set<String> acceptedTags, DropTargetMode mode, ViewChannel targetChannel,
				List<ViewAction> actions) {
			this(acceptedTags, mode, targetChannel, actions, () -> ExecutableState.EXECUTABLE,
				ViewExecutabilityRule.ALWAYS_EXECUTABLE, null);
		}

		/**
		 * Whether the {@link #executability() table-wide state} currently lets the drop be offered.
		 */
		public boolean isEnabled() {
			return executability.get().isExecutable();
		}

		/**
		 * Asks the {@link #targetRule() target rule} (for a {@link DropTargetMode#ROW} drop) and
		 * the {@link #refuseIf() refusal function} about a drop of the given objects on the given
		 * target; the table-wide state is not asked here.
		 *
		 * @return The reason of the first refusal, {@code null} if the drop is accepted.
		 */
		ResKey refusal(Object target, List<?> objects) {
			if (mode == DropTargetMode.ROW) {
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
	 * Creates a {@link TableDropBinding}.
	 *
	 * @param context
	 *        The context the action chains run in.
	 * @param drops
	 *        The declared drops, in declaration order - which is the order a drop is matched
	 *        against them in.
	 */
	public TableDropBinding(ReactContext context, List<Drop> drops) {
		_context = context;
		_drops = drops;
	}

	/**
	 * The qualified names of the accepted types and of their subtypes, over all declared drops that
	 * are currently {@link Drop#isEnabled() enabled}.
	 */
	@Override
	public Collection<String> acceptedTypes() {
		Set<String> tags = new LinkedHashSet<>();
		for (Drop drop : _drops) {
			if (drop.isEnabled()) {
				tags.addAll(drop.acceptedTags());
			}
		}
		return tags;
	}

	/**
	 * Whether one of the currently {@link Drop#isEnabled() enabled} drops targets rows.
	 */
	@Override
	public boolean dropOnRows() {
		for (Drop drop : _drops) {
			if (drop.mode() == DropTargetMode.ROW && drop.isEnabled()) {
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
	 * matching disabled one gives the reason of its {@link Drop#executability() table-wide state};
	 * a drop nothing matches at all is refused as not accepted. Nothing is modified, in particular
	 * no target channel is written.
	 * </p>
	 */
	@Override
	public DropVerdict check(DropEvent event) {
		String tag = draggedType(event);
		if (tag == null) {
			return DropVerdict.refused(com.top_logic.layout.react.I18NConstants.ERROR_DROP_NOT_ACCEPTED);
		}
		Drop drop = select(event, tag, true);
		ResKey refusal;
		if (drop != null) {
			refusal = drop.refusal(targetOf(drop, event), event.objects());
		} else {
			Drop disabled = select(event, tag, false);
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
	 * A drop made on a row is offered to the {@link DropTargetMode#ROW row} drops first, and falls
	 * back to a {@link DropTargetMode#TABLE table} drop when none of them accepts the drag - a table
	 * whose rows are targets for one kind of object still accepts another kind as a whole, wherever
	 * the pointer happened to be. A drop nothing accepts does nothing, and neither does one whose
	 * matching declared drop is {@link Drop#isEnabled() disabled}.
	 * </p>
	 */
	@Override
	public void onDrop(DropEvent event) {
		String tag = draggedType(event);
		if (tag == null) {
			return;
		}
		Drop drop = select(event, tag, true);
		if (drop != null) {
			apply(drop, event.objects(), targetOf(drop, event));
		}
	}

	/**
	 * The declared drop that applies a drop of the given tag: a matching row drop for a drop made on
	 * a row, otherwise a matching table drop; {@code null} if none matches.
	 *
	 * @param enabledOnly
	 *        Whether only the currently {@link Drop#isEnabled() enabled} drops are considered.
	 */
	private Drop select(DropEvent event, String tag, boolean enabledOnly) {
		if (event.target() != null) {
			Drop rowDrop = matching(DropTargetMode.ROW, tag, enabledOnly);
			if (rowDrop != null) {
				return rowDrop;
			}
		}
		return matching(DropTargetMode.TABLE, tag, enabledOnly);
	}

	private Drop matching(DropTargetMode mode, String tag, boolean enabledOnly) {
		for (Drop drop : _drops) {
			if (drop.mode() == mode && drop.acceptedTags().contains(tag) && (!enabledOnly || drop.isEnabled())) {
				return drop;
			}
		}
		return null;
	}

	/**
	 * The target the given declared drop applies a drop to: the row dropped on for a row drop,
	 * {@code null} for a drop on the table.
	 */
	private static Object targetOf(Drop drop, DropEvent event) {
		return drop.mode() == DropTargetMode.ROW ? event.target() : null;
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

	/**
	 * The type tag of what was dropped, or {@code null} if the drop says nothing about it.
	 *
	 * <p>
	 * The control the objects were dragged out of is the authority on what it drags, so its declared
	 * tag is what the drops are matched against - the very tag the client offered the drop by. A drop
	 * replayed from a recorded script names no such control; there the objects say what they are, and
	 * they have to agree on it, so that a drag of several rows is never applied in part.
	 * </p>
	 */
	private static String draggedType(DropEvent event) {
		if (event.source() instanceof DragSourceControl source && source.dragType() != null) {
			return source.dragType();
		}
		String tag = null;
		for (Object object : event.objects()) {
			if (!(object instanceof TLObject model)) {
				return null;
			}
			String objectTag = TLModelUtil.qualifiedName(model.tType());
			if (tag == null) {
				tag = objectTag;
			} else if (!tag.equals(objectTag)) {
				return null;
			}
		}
		return tag;
	}

	private void apply(Drop drop, List<?> objects, Object target) {
		ViewChannel targetChannel = drop.targetChannel();
		if (targetChannel != null) {
			targetChannel.set(target);
		}
		ViewActionChain.run(_context, drop.actions(), objects, null);
	}

	/**
	 * The tags a declared type is accepted under: its own qualified name and those of its subtypes,
	 * so that the client accepts a subtype by comparing tags.
	 *
	 * @param types
	 *        The declared accepted types.
	 */
	public static Set<String> tagsOf(Collection<? extends TLType> types) {
		Set<String> tags = new LinkedHashSet<>();
		for (TLType type : types) {
			tags.add(TLModelUtil.qualifiedName(type));
			if (type instanceof TLClass generalization) {
				for (TLClass specialization : TLModelUtil.getTransitiveSpecializations(generalization)) {
					tags.add(TLModelUtil.qualifiedName(specialization));
				}
			}
		}
		return tags;
	}

}
