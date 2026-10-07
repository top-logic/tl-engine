/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.component;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.HashMap;
import java.util.Map;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.layout.form.component.I18NConstants;
import com.top_logic.layout.form.component.TransactionHandler;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.CommandHandler;

/**
 * Configuration plug-in to configure a command commit message.
 */
@Abstract
public interface WithCommitMessage extends ConfigurationItem {

	/**
	 * @see #getCommitMessage()
	 */
	String COMMIT_MESSAGE = "commitMessage";

	/**
	 * The message to annotate to the performed change.
	 * 
	 * <p>
	 * If not set, a default commit message is derived from the command label and the target model.
	 * </p>
	 * 
	 * <p>
	 * A message may contain the placeholder '{0}' that is replaced with the label of the target
	 * model.
	 * </p>
	 */
	@Name(COMMIT_MESSAGE)
	ResKey1 getCommitMessage();

	/** @see com.top_logic.basic.reflect.DefaultMethodInvoker */
	Lookup LOOKUP = MethodHandles.lookup();

	/**
	 * Enhances the given command arguments with the given custom commit message.
	 */
	default Map<String, Object> addCommitMessage(Map<String, Object> arguments, Object model) {
		ResKey1 message = getCommitMessage();
		if (message == null) {
			return arguments;
		}
		ResKey commitMessage = message.fill(MetaLabelProvider.INSTANCE.getLabel(model));
		Map<String, Object> enhancedArguments = new HashMap<>(arguments);
		enhancedArguments.put(TransactionHandler.CUSTOM_COMMIT_MESSAGE, commitMessage);
		return enhancedArguments;
	}

	/**
	 * Resolves a commit message to use for the given command.
	 * 
	 * @see #buildCommitMessage(ResKey, Object)
	 */
	default ResKey buildCommandMessage(LayoutComponent component, CommandHandler handler, Object model) {
		return buildCommitMessage(handler.getResourceKey(component), model);
	}

	/**
	 * Resolves the commit message for an operation performed on the given model.
	 * 
	 * <p>
	 * The {@link #getCommitMessage() configured message} is filled with the label of the given model.
	 * Without a configured message, the message names the operation and the model.
	 * </p>
	 * 
	 * @param operationLabel
	 *        The label of the performed operation, {@code null} for an operation without a label.
	 * @param model
	 *        The model the operation is performed on, {@code null} for an operation without a
	 *        target model.
	 * @return The message to annotate to the change.
	 */
	default ResKey buildCommitMessage(ResKey operationLabel, Object model) {
		ResKey1 customMessage = getCommitMessage();
		if (customMessage != null) {
			return customMessage.fill(MetaLabelProvider.INSTANCE.getLabel(model));
		}
		return defaultCommitMessage(operationLabel, model);
	}

	/**
	 * The message annotated to an operation performed on the given model, when no message is
	 * configured.
	 * 
	 * @param operationLabel
	 *        The label of the performed operation, {@code null} for an operation without a label.
	 * @param model
	 *        The model the operation is performed on, {@code null} for an operation without a
	 *        target model.
	 * @return A message naming the operation and the model, as far as they are given.
	 */
	static ResKey defaultCommitMessage(ResKey operationLabel, Object model) {
		if (operationLabel == null) {
			if (model == null) {
				return I18NConstants.PERFORMED_CHANGES;
			} else {
				return I18NConstants.UPDATED__MODEL.fill(MetaLabelProvider.INSTANCE.getLabel(model));
			}
		} else {
			if (model == null) {
				return I18NConstants.PERFORMED__OPERATION.fill(operationLabel);
			} else {
				return I18NConstants.PERFORMED__OPERATION_MODEL.fill(operationLabel,
					MetaLabelProvider.INSTANCE.getLabel(model));
			}
		}
	}

}
