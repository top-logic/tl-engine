/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.gui.layout.webfolder;

import java.util.Map;

import com.top_logic.base.services.simpleajax.AbstractSystemAjaxCommand;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.KnowledgeBaseException;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.Clipboard;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.knowledge.wrap.WrapperHistoryUtils;
import com.top_logic.layout.DisplayContext;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.tool.execution.ExecutableState;

/**
 * Add elements to the clipboard.
 * 
 * @author <a href="mailto:mga@top-logic.com">Michael G&auml;nsler</a>
 */
public class AddToClipboardHandler extends AbstractSystemAjaxCommand {

    /** The command provided by this instance. */
    public static final String COMMAND = "addToClipboard";

	/**
	 * Default CTor for register.
	 */
    public AddToClipboardHandler(InstantiationContext context, Config config) {
		super(context, config);
    }


    @Override
	public HandlerResult handleCommand(DisplayContext aContext, LayoutComponent aComponent, Object model, Map<String, Object> someArguments) {
		if (model == null) {
			return newErrorResult(I18NConstants.ADD_ERROR_NO_MODEL);
    	}

		Wrapper object = (Wrapper) model;
		{
			KnowledgeBase kb = object.getKnowledgeBase();
			Clipboard clipboard = Clipboard.getInstance(kb);
			Transaction tx = kb.beginTransaction();
			try {
				boolean success = clipboard.add(object);
				if (!success) {
					return newErrorResult(I18NConstants.CLIPBOARD_CONTAINS_MODEL);
				}
				tx.commit();
			} catch (KnowledgeBaseException ex) {
				StringBuilder commitFailed = new StringBuilder();
				commitFailed.append("Commit of adding model '");
				commitFailed.append(object);
				commitFailed.append("' to clipboard '");
				commitFailed.append(clipboard);
				commitFailed.append("' failed.");
				Logger.error(commitFailed.toString(), ex, AddToClipboardHandler.class);
				return newErrorResult(I18NConstants.ADD_TO_CLIPBOARD_FAILED);
			} finally {
				tx.rollback();
			}
    	}
		aComponent.invalidate();
		HandlerResult result = new HandlerResult();
		result.addProcessed(object);
		return result;
    }

	private static HandlerResult newErrorResult(ResKey errorMessage) {
		HandlerResult result = new HandlerResult();
		result.addErrorMessage(errorMessage);
		return result;
	}

	/**
	 * {@link ExecutabilityRule} for {@link AddToClipboardHandler}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
    public static class AddToClipboardRule implements ExecutabilityRule {

		/**
		 * Disabled {@link ExecutableState} used when the model is already contained in clipboard.
		 */
		protected static final ExecutableState EXEC_STATE_DISABLED =
			ExecutableState.createDisabledState(I18NConstants.ALREADY_IN_CLIPBOARD);

		/**
		 * Disabled {@link ExecutableState} used when the component has no model.
		 */
		protected static final ExecutableState EXEC_STATE_NO_WRAPPER =
			ExecutableState.createDisabledState(I18NConstants.NO_WRAPPER);

		/**
		 * Hidden {@link ExecutableState} used when no clipboard can be found.
		 */
		protected static final ExecutableState EXEC_STATE_NO_CLIP =
			new ExecutableState(ExecutableState.CommandVisibility.HIDDEN, I18NConstants.NO_CLIPBOARD);

		/**
		 * Disabled {@link ExecutableState} used when no model to put into clipboard is an historic
		 * object.
		 */
		protected static final ExecutableState EXEC_STATE_NO_IS_HISTORIC =
			ExecutableState.createDisabledState(I18NConstants.NOT_IN_HISTORIC_STATE);

		/**
		 * Singleton {@link AddToClipboardRule} instance.
		 */
		public static final AddToClipboardRule INSTANCE = new AddToClipboardRule();

		private AddToClipboardRule() {
			// Singleton constructor.
		}

    	@Override
		public ExecutableState isExecutable(LayoutComponent component,
    			Object model, Map<String, Object> someArguments) {

    		boolean isContained = false;

			try {
				// resolve model from component
				if (!(model instanceof Wrapper)) {
					return EXEC_STATE_NO_WRAPPER;
				}
				Wrapper wrapper = (Wrapper) model;
				Clipboard clipboard = Clipboard.getInstance(wrapper.getKnowledgeBase());
				if (clipboard == null) {
					return EXEC_STATE_NO_CLIP;
				}
				isContained = clipboard.contains(wrapper);
				if (isContained) {
					return EXEC_STATE_DISABLED;
				}

				boolean isCurrent = WrapperHistoryUtils.getRevision(wrapper).isCurrent();
				if (!isCurrent) {
					return EXEC_STATE_NO_IS_HISTORIC;
				}
			} catch (Exception e) {
				// Ignore here - suppose it is not contained
			}

    		return isContained ? EXEC_STATE_DISABLED : ExecutableState.EXECUTABLE;
    	}
    }
}
