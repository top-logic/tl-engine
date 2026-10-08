/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.dnd;

import java.util.List;
import java.util.function.Predicate;

import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.dnd.DragSourceControl;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.command.LiveExecutability;
import com.top_logic.layout.view.command.ViewExecutabilityRule;
import com.top_logic.layout.view.command.ViewExecutabilityRules;

/**
 * Makes the items of a control draggable as a {@link DragConfig} declares.
 *
 * <p>
 * Rules for single items decide per item, with the item as their input; the control-wide
 * {@link DragConfig#getExecutability() executability} is followed while the control is displayed,
 * and takes the drag away from all items while it refuses.
 * </p>
 */
public final class DragSourceBinding {

	/**
	 * The control whose items are dragged, as far as the binding switches dragging on and off.
	 */
	public interface Source {

		/**
		 * Whether the items are currently draggable.
		 *
		 * @see DragSourceControl#isDragEnabled()
		 */
		boolean isDragEnabled();

		/**
		 * Makes the items draggable as drags of the given kind.
		 *
		 * @param dragKind
		 *        The {@link DragSourceControl#dragKind() kind} of a drag, {@code null} for a drag
		 *        without a kind.
		 * @param draggable
		 *        Which items may be dragged, {@code null} for all of them.
		 */
		void setDragSource(String dragKind, Predicate<Object> draggable);

		/**
		 * Switches dragging on or off, keeping what {@link #setDragSource(String, Predicate)} set.
		 */
		void setDragEnabled(boolean enabled);

		/**
		 * Asks the predicate given to {@link #setDragSource(String, Predicate)} again for the
		 * displayed items, after its answer may have changed.
		 */
		void refreshDragSource();

	}

	private DragSourceBinding() {
		// Static utility.
	}

	/**
	 * Makes the items of the given control draggable.
	 *
	 * @param context
	 *        The context the rules are built in.
	 * @param control
	 *        The control displaying the items; the control-wide rules are followed while it is
	 *        displayed.
	 * @param source
	 *        Switches dragging of the control's items on and off.
	 * @param drag
	 *        The declared drag, whose control-wide {@link DragConfig#getExecutability()
	 *        executability} is followed.
	 * @param dragKind
	 *        The {@link DragConfig#getKind() kind} of a drag, {@code null} or empty for a drag
	 *        without a kind.
	 * @param itemExecutability
	 *        The rules deciding which single item may be dragged, each item being their input. Empty
	 *        lets every item be dragged.
	 */
	public static void install(ViewContext context, ReactControl control, Source source, DragConfig drag,
			String dragKind, List<PolymorphicConfiguration<? extends ViewExecutabilityRule>> itemExecutability) {
		ViewExecutabilityRule itemRule = ViewExecutabilityRules.build(itemExecutability, context);
		Predicate<Object> draggable = itemRule == ViewExecutabilityRule.ALWAYS_EXECUTABLE ? null
			: item -> itemRule.isExecutable(item).isExecutable();
		source.setDragSource(dragKind == null || dragKind.isEmpty() ? null : dragKind, draggable);
		if (drag.getExecutability().isEmpty()) {
			return;
		}

		LiveExecutability[] live = new LiveExecutability[1];
		Runnable update = () -> {
			boolean enabled = live[0].getState().isExecutable();
			if (enabled == source.isDragEnabled()) {
				source.refreshDragSource();
			} else {
				source.setDragEnabled(enabled);
			}
		};
		live[0] = LiveExecutability.create(drag, context, update);
		source.setDragEnabled(live[0].getState().isExecutable());
		live[0].followWhileDisplayed(context, control, update);
	}

}
