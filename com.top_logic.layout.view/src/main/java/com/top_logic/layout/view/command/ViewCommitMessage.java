/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.command;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.layout.component.WithCommitMessage;
import com.top_logic.layout.provider.MetaLabelProvider;

/**
 * The commit message of a change a {@link ViewAction} performs.
 *
 * <p>
 * The message is the one the action is configured with. Without one, it is a default message
 * naming the {@link ViewCommand#getLabel() label} of the {@link ViewCommand} the action runs in and
 * the model the change is performed on, or a default message of the action's own.
 * </p>
 *
 * <p>
 * The command is the innermost {@link ViewCommand} whose configuration contains the action's
 * configuration, also through nested actions such as {@link IfAction} or
 * {@link WithTransactionAction}. An action created outside of a command has no command label.
 * </p>
 *
 * @see WithCommitMessage#buildCommitMessage(ResKey, Object)
 */
public final class ViewCommitMessage {

	private final WithCommitMessage _config;

	private final ResKey1 _defaultMessage;

	private ViewCommand _command;

	/**
	 * Creates a {@link ViewCommitMessage} defaulting to the message naming the command and the
	 * model.
	 *
	 * @param context
	 *        The context the action is being instantiated in; the {@link ViewCommitMessage} must be
	 *        created from the constructor of the action.
	 * @param config
	 *        The configuration of the action.
	 */
	public ViewCommitMessage(InstantiationContext context, WithCommitMessage config) {
		this(context, config, null);
	}

	/**
	 * Creates a {@link ViewCommitMessage} with a default message of the action's own.
	 *
	 * @param context
	 *        The context the action is being instantiated in; the {@link ViewCommitMessage} must be
	 *        created from the constructor of the action.
	 * @param config
	 *        The configuration of the action.
	 * @param defaultMessage
	 *        The message used when the action is configured without one, filled with the label of
	 *        the model. {@code null} for the message naming the command and the model.
	 */
	public ViewCommitMessage(InstantiationContext context, WithCommitMessage config, ResKey1 defaultMessage) {
		_config = config;
		_defaultMessage = defaultMessage;
		context.resolveReference(InstantiationContext.OUTER, ViewCommand.class, command -> _command = command);
	}

	/**
	 * The commit message of a change performed on the given model.
	 *
	 * @param model
	 *        The object the change is performed on, {@code null} for a change without a target
	 *        model.
	 */
	public ResKey create(Object model) {
		if (_defaultMessage != null && _config.getCommitMessage() == null) {
			return _defaultMessage.fill(MetaLabelProvider.INSTANCE.getLabel(model));
		}
		return _config.buildCommitMessage(operationLabel(), model);
	}

	/**
	 * The label of the command the action runs in, {@code null} when there is none.
	 */
	private ResKey operationLabel() {
		return _command == null ? null : _command.getLabel();
	}

}
