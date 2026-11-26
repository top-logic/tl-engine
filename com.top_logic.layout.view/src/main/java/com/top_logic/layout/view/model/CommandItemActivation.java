/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.model;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.layout.react.control.layout.ItemAction;
import com.top_logic.layout.react.control.layout.ItemActivation;
import com.top_logic.layout.react.control.layout.ReactLayoutControl;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.command.ViewCommandModel;
import com.top_logic.layout.view.element.I18NConstants;
import com.top_logic.tool.execution.ExecutableState;
import com.top_logic.util.Resources;

/**
 * {@link ItemActivation} running a configured command with the object of the activated item as its
 * main argument - the activation command of an object list, say.
 *
 * <p>
 * The command's executability rules decide over each object: an object the rules hide the command
 * for offers no action, so its item cannot be activated; one they disable it for offers a disabled
 * action, explained by the reason the rules give. The name of an item's action is the command's
 * label for the object, or "Open" followed by the label of the object where the command has none.
 * </p>
 *
 * <p>
 * The command's model follows its inputs while the container is attached; a change of what the
 * rules decide is reported to the container, which publishes the actions of its items again.
 * </p>
 */
public class CommandItemActivation implements ItemActivation {

	private final ViewCommandModel _model;

	/**
	 * Makes the items of the given container activatable by the given command.
	 *
	 * @param context
	 *        The context the container is displayed in, providing the model scope the command
	 *        follows its inputs in.
	 * @param container
	 *        The container whose items are activated.
	 * @param model
	 *        The command run with the object of an activated item.
	 * @param button
	 *        Whether the items of a selectable container show an open button, see
	 *        {@link ReactLayoutControl#setActivation(ItemActivation, boolean)}.
	 */
	public static void bind(ViewContext context, ReactLayoutControl container, ViewCommandModel model,
			boolean button) {
		container.setActivation(new CommandItemActivation(model), button);

		Runnable update = container::updateActivation;
		model.addStateChangeListener(update);
		container.addAttachListener(() -> model.attach(context.getModelScope()));
		container.addDetachListener(model::detach);
		container.addCleanupAction(() -> model.removeStateChangeListener(update));
	}

	/**
	 * Creates a {@link CommandItemActivation}.
	 *
	 * @param model
	 *        The command run with the object of an activated item.
	 */
	public CommandItemActivation(ViewCommandModel model) {
		_model = model;
	}

	@Override
	public ItemAction action(Object item) {
		ExecutableState state = _model.executability(item);
		if (state.isHidden()) {
			return null;
		}
		String tooltip = state.isExecutable() ? _model.tooltip(item) : reason(state, item);
		return new ItemAction(label(item), !state.isExecutable(), tooltip);
	}

	/**
	 * The name of the action of the given object: the command's label for it, or "Open" followed by
	 * the label of the object.
	 */
	private String label(Object item) {
		String label = _model.label(item);
		if (label != null && !label.isEmpty()) {
			return label;
		}
		String itemLabel = MetaLabelProvider.INSTANCE.getLabel(item);
		ResKey key = itemLabel == null || itemLabel.isEmpty()
			? I18NConstants.TABLE_ACTIVATE_ROW
			: I18NConstants.TILE_ACTIVATE__TITLE.fill(itemLabel);
		return Resources.getInstance().getString(key);
	}

	/**
	 * Why the command is disabled for the given object, its own tooltip where the rules give no
	 * reason.
	 */
	private String reason(ExecutableState state, Object item) {
		ResKey reason = state.getI18NReasonKey();
		if (reason == null || reason == ResKey.NONE) {
			return _model.tooltip(item);
		}
		return Resources.getInstance().getString(reason);
	}

	@Override
	public void activate(Object item) {
		_model.activate(item);
	}

}
