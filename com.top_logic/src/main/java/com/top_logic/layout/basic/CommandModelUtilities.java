/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic;

import com.top_logic.basic.util.ResKey;
import com.top_logic.tool.execution.ExecutableState;

/**
 * The class {@link CommandModelUtilities} is a utility class for {@link ButtonUIModel}s.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class CommandModelUtilities {

	/**
	 * This method copies the properties of <code>aSourceModel</code> to
	 * <code>aTargetModel</code>. <br/>
	 * The copied properties are:
	 * <ul>
	 * <li>the disabled state</li>
	 * <li>the visibility</li>
	 * <li>the disabled image</li>
	 * <li>the enabled image</li>
	 * <li>the label</li>
	 * <li>the key for the disabled reason</li>
	 * <li>the cssClasses</li>
	 * <li>the tooltip and tooltipCaption</li>
	 * </ul>
	 * 
	 * <b>The additional properties are not copied</b>
	 * 
	 * @param aSourceModel
	 *        the model to get the values from.
	 * @param aTargetModel
	 *        the model to store the values into.
	 * @throws NullPointerException
	 *         if one of the models is <code>null</code>
	 */
	public static void copyValues(ButtonUIModel aSourceModel, ButtonUIModel aTargetModel) {
		boolean executable = aSourceModel.isExecutable();
		if (executable) {
			aTargetModel.setExecutable();
		} else {
			aTargetModel.setNotExecutable(aSourceModel.getNotExecutableReasonKey());
		}
		aTargetModel.setVisible(aSourceModel.isVisible());
		aTargetModel.setNotExecutableImage(aSourceModel.getNotExecutableImage());
		aTargetModel.setImage(aSourceModel.getImage());
		aTargetModel.setLabel(aSourceModel.getLabel());
		aTargetModel.setCssClasses(aSourceModel.getCssClasses());
		aTargetModel.setTooltip(aSourceModel.getTooltip());
		aTargetModel.setTooltipCaption(aSourceModel.getTooltipCaption());
	}

	/**
	 * Update the given {@link ButtonUIModel} with information information about the visibility and
	 * executablility in the given {@link ExecutableState}.
	 * 
	 * @param executability
	 *        The {@link ExecutableState} given the visibility and executability information.
	 * @param command
	 *        The {@link ButtonUIModel} to update.
	 */
	public static void applyExecutability(ExecutableState executability, ButtonUIModel command) {
		switch (executability.visibility()) {
			case VISIBLE:
				command.setExecutable();
				break;
			case DISABLED:
				command.setVisible(true);
				command.setNotExecutable(executability.getI18NReasonKey());
				break;
			case HIDDEN:
				command.setVisible(false);
				break;
			default:
				throw new AssertionError("No such visibility: " + executability.visibility());
		}
	}

	/**
	 * Sets the executability state of the given {@link ButtonUIModel} and the corresponding reason
	 * key.
	 * 
	 * @param model
	 *        The model to update.
	 * @param executable
	 *        Whether the model should be executable.
	 * @param notExecutableReasonKey
	 *        The {@link ButtonUIModel#getNotExecutableReasonKey() reason key} why the model is not
	 *        executable. This parameter is not used when model is set executable.
	 * 
	 * @see #setExecutable(ButtonUIModel)
	 * @see #setNonExecutable(ButtonUIModel, ResKey)
	 */
	public static void setExecutability(ButtonUIModel model, boolean executable, ResKey notExecutableReasonKey) {
		if (executable) {
			setExecutable(model);
		} else {
			setNonExecutable(model, notExecutableReasonKey);
		}
	}

	/**
	 * Set the given model not executable and sets the given reason key.
	 * 
	 * @param model
	 *        The model to set not executable.
	 * @param notExecutableReasonKey
	 *        The {@link ButtonUIModel#getNotExecutableReasonKey() reason} why the model is not
	 *        executable.
	 * 
	 * @see #setExecutability(ButtonUIModel, boolean, ResKey)
	 * @see #setExecutable(ButtonUIModel)
	 */
	public static void setNonExecutable(ButtonUIModel model, ResKey notExecutableReasonKey) {
		model.setNotExecutable(notExecutableReasonKey);
	}

	/**
	 * Sets the given {@link ButtonUIModel} executable and removes the reason why the model is not
	 * executable.
	 * 
	 * @param model
	 *        The {@link ButtonUIModel} to set executable.
	 * 
	 * @see #setExecutability(ButtonUIModel, boolean, ResKey)
	 * @see #setNonExecutable(ButtonUIModel, ResKey)
	 */
	public static void setExecutable(ButtonUIModel model) {
		/* set executability before reason key, because setting reason key model triggers update
		 * which needs the reason key. */
		model.setExecutable();
	}

}
