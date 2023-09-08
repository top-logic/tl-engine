/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.common.webfolder.ui.clipboard;

import com.top_logic.knowledge.service.KnowledgeBaseFactory;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.Clipboard;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.knowledge.wrap.WrapperHistoryUtils;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.component.ComponentUtil;
import com.top_logic.layout.form.model.CommandField;
import com.top_logic.layout.form.model.ConstantExecutabilityModel;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.execution.ExecutableState;
import com.top_logic.util.Resources;

/**
 * Add or remove an object to the personal clipboard.
 * 
 * @author <a href="mailto:fsc@top-logic.com">Friedemann Schneider</a>
 */
public class ModifyClipboardExecutable extends CommandField {

	/* package protected */static enum State {
		IN_CLIPBOARD,
		NOT_IN_CLIPBOARD,
		CLIPBOARD_DISABLED
	}

	/** The object to be added to clipboard. */
	private final Wrapper model;

	/** current state of the executable */
	private State state;

	/**
	 * Creates a {@link ModifyClipboardExecutable}.
	 * 
	 * @param aModel
	 *        The object to be added to clipboard, must not be <code>null</code>.
	 * @throws IllegalArgumentException
	 *         If given object is <code>null</code> or invalid.
	 */
	public ModifyClipboardExecutable(String name, Wrapper aModel, State initialState, ExecutableState executability) {
		super(name, new ConstantExecutabilityModel(executability));
		if (aModel == null) {
			throw new IllegalArgumentException("Given wrapper is null.");
		}

		this.model = aModel;
		this.state = initialState;
	}


	@Override
	public HandlerResult executeCommand(DisplayContext aContext) {
		if (!ComponentUtil.isValid(model)) {
			return ComponentUtil.errorObjectDeleted(aContext);
		}

		HandlerResult theResult = new HandlerResult();

		// do nothing if an error occurred while creation of the field
		if (this.state == State.CLIPBOARD_DISABLED) {
			return theResult;
		}

		Clipboard clipboard = Clipboard.getInstance();
		Resources theRes = Resources.getInstance();
		{
			boolean inClipboard = clipboard.contains(this.model);

			// check if internal state is consistent with the clipboard state there might be
			// differences because the wrapper might be added or removed from clipboard somewhere
			// else and we have no good possibility to be informed about that
			if (this.state == State.IN_CLIPBOARD) {
				if (inClipboard) {
					this.removeFromClipboardCommit(this.model, clipboard);
				}
				setImage(Icons.ADD_CLIPBOARD);
				setTooltip(theRes.getString(I18NConstants.ADD_TOOLTIP));
				this.state = State.NOT_IN_CLIPBOARD;
			} else if (this.state == State.NOT_IN_CLIPBOARD) {
				if (!inClipboard) {
					this.addToClipboardCommit(this.model, clipboard);
				}
				setImage(Icons.REMOVE_CLIPBOARD);
				setTooltip(theRes.getString(I18NConstants.REMOVE_TOOLTIP));
				this.state = State.IN_CLIPBOARD;
			}
		}
		return theResult;

	}

	private void addToClipboardCommit(Wrapper aModel, Clipboard clipboard) {
		try (Transaction theTX = KnowledgeBaseFactory.getInstance().getDefaultKnowledgeBase().beginTransaction()) {
			clipboard.add(aModel);
			theTX.commit();
		}
	}

	private void removeFromClipboardCommit(Wrapper aModel, Clipboard clipboard) {
		try (Transaction theTX = KnowledgeBaseFactory.getInstance().getDefaultKnowledgeBase().beginTransaction()) {
			clipboard.remove(aModel);
			theTX.commit();
		}
	}

	/**
	 * Return the {@link State}
	 */
	/* package protected */State getState() {
		return this.state;
	}

	/**
	 * Return the default {@link ExecutableState} for a {@link Wrapper}.
	 * 
	 * It will be {@link Command} for current versions of a wrapper, and hidden for
	 * <code>null</code>, invalid or historic versions.
	 */
	public static ExecutableState getDefaultExecutability(Wrapper aModel) {

		if (aModel == null || !aModel.tValid()) {
			return ExecutableState.NOT_EXEC_HIDDEN;
		}

		// historic wrappers cannot be added to the clipboard
		if (WrapperHistoryUtils.getRevision(aModel).isCurrent()) {
			return ExecutableState.EXECUTABLE;
		} else {
			return ExecutableState.NOT_EXEC_HIDDEN;
		}
	}

	/**
	 * Create a standard {@link CommandField} that contains a {@link ModifyClipboardExecutable}
	 * combined with {@link #getDefaultExecutability(Wrapper)}.
	 * 
	 * @see #createField(Wrapper, String, ExecutableState)
	 */
	public static CommandField createField(Wrapper aWrapper, String aName) {
		return createField(aWrapper, aName, getDefaultExecutability(aWrapper));
	}

	/**
	 * Create a standard {@link CommandField} that contains a {@link ModifyClipboardExecutable}. The
	 * {@link ExecutableState} is directly passed into the {@link Command}.
	 */
	public static CommandField createField(Wrapper aWrapper, String aName, ExecutableState executability) {

		Resources theRes = Resources.getInstance();

		ThemeImage theImage;
		String theTool;
		State theState;
		{
			// Init the field status which depends if the wrapper is in clipboard or not
			if (Clipboard.getInstance().contains(aWrapper)) {
				theImage = Icons.REMOVE_CLIPBOARD;
				theTool = theRes.getString(I18NConstants.REMOVE_TOOLTIP);
				theState = State.IN_CLIPBOARD;
			} else {
				theImage = Icons.ADD_CLIPBOARD;
				theTool = theRes.getString(I18NConstants.ADD_TOOLTIP);
				theState = State.NOT_IN_CLIPBOARD;
			}
		}

		CommandField theField = new ModifyClipboardExecutable(aName, aWrapper, theState, executability);
		theField.setImage(theImage);
		theField.setTooltip(theTool);
		theField.setNotExecutableImage(Icons.ADD_CLIPBOARD_DISABLED);
		theField.setNotExecutableReasonKey(I18NConstants.DISABLED);

		return theField;
	}
}