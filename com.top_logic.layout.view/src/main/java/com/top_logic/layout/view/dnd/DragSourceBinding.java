/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.dnd;

import java.util.List;
import java.util.Objects;
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
		 * The type tag the items are currently dragged under, {@code null} while they are not
		 * draggable.
		 *
		 * @see DragSourceControl#dragType()
		 */
		String dragType();

		/**
		 * Makes the items draggable under the given type tag.
		 *
		 * @param dragType
		 *        The {@link #dragType() type tag}, or {@code null} to make the items undraggable.
		 * @param draggable
		 *        Which items may be dragged, {@code null} for all of them.
		 */
		void setDragSource(String dragType, Predicate<Object> draggable);

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
	 * @param dragType
	 *        The type tag the items are dragged under.
	 * @param itemExecutability
	 *        The rules deciding which single item may be dragged, each item being their input. Empty
	 *        lets every item be dragged.
	 */
	public static void install(ViewContext context, ReactControl control, Source source, DragConfig drag,
			String dragType, List<PolymorphicConfiguration<? extends ViewExecutabilityRule>> itemExecutability) {
		ViewExecutabilityRule itemRule = ViewExecutabilityRules.build(itemExecutability, context);
		Predicate<Object> draggable = itemRule == ViewExecutabilityRule.ALWAYS_EXECUTABLE ? null
			: item -> itemRule.isExecutable(item).isExecutable();
		if (drag.getExecutability().isEmpty()) {
			source.setDragSource(dragType, draggable);
			return;
		}

		LiveExecutability[] live = new LiveExecutability[1];
		Runnable update = () -> {
			String type = live[0].getState().isExecutable() ? dragType : null;
			if (Objects.equals(type, source.dragType())) {
				source.refreshDragSource();
			} else {
				source.setDragSource(type, draggable);
			}
		};
		live[0] = LiveExecutability.create(drag, context, update);
		source.setDragSource(live[0].getState().isExecutable() ? dragType : null, draggable);
		live[0].followWhileDisplayed(context, control, update);
	}

}
