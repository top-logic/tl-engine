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
	 */
	default ResKey buildCommandMessage(ResKey commandLabel, Object model) {
		ResKey message;
		ResKey1 customMessage = getCommitMessage();
		if (customMessage == null) {
			if (model == null) {
				message = I18NConstants.PERFORMED__OPERATION.fill(commandLabel);
			} else {
				message = I18NConstants.PERFORMED__OPERATION_MODEL.fill(commandLabel,
					MetaLabelProvider.INSTANCE.getLabel(model));
			}
		} else {
			message = customMessage.fill(MetaLabelProvider.INSTANCE.getLabel(model));
		}
		return message;
	}

}
