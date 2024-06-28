/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.component.edit;

import java.util.Map;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.layout.DisplayContext;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.mig.html.layout.FindFirstMatchingComponent;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.OpenModalDialogCommandHandler;
import com.top_logic.util.error.TopLogicException;

/**
 * Handler for opening a dialog in edit mode.
 *
 * This handler isn't registered in the CommandHandlerFactory, because it isn't state less.
 * 
 * @author <a href="mailto:dbu@top-logic.com">Daniel Busche</a>
 */
public class OpenDialogInEdit extends OpenModalDialogCommandHandler {

	/** Config interface for {@link OpenModalDialogCommandHandler}. */
	public interface Config extends OpenModalDialogCommandHandler.Config {

		/**
		 * @see #getEditor()
		 */
		String EDITOR_NAME_PROPERTY = "editor-name";

		/**
		 * Name of the editor in the dialog.
		 * 
		 * <p>
		 * If not given, the first editor of the dialog is taken.
		 * </p>
		 */
		@Name(EDITOR_NAME_PROPERTY)
		ComponentName getEditor();
	}

	/**
	 * Creates a {@link OpenDialogInEdit}.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	public OpenDialogInEdit(InstantiationContext context, Config config) {
		super(context, config);
	}

	private Config config() {
		return (Config) getConfig();
	}

	@Override
	protected void beforeOpening(DisplayContext aContext, LayoutComponent aComponent, Map aSomeArguments,
			LayoutComponent aDialog) {
		findEditor(aDialog).setEditMode();
		super.beforeOpening(aContext, aComponent, aSomeArguments, aDialog);
	}

	private EditMode findEditor(LayoutComponent aDialog) {
		ComponentName editorName = config().getEditor();
		if (editorName != null) {
			return findEditorByName(aDialog, editorName);
		} else {
			return findFirstEditor(aDialog);
		}
	}

	private EditMode findFirstEditor(LayoutComponent aDialog) {
		FindFirstMatchingComponent visitor = new FindFirstMatchingComponent(EditMode.class::isInstance);
		aDialog.acceptVisitorRecursively(visitor);
		LayoutComponent editor = visitor.result();
		if (editor == null) {
			throw new TopLogicException(I18NConstants.EXCEPTION_NO_EDITOR_FOUND);
		}
		return (EditMode) editor;
	}

	private EditMode findEditorByName(LayoutComponent aDialog, ComponentName editorName) {
		LayoutComponent editor = aDialog.getComponentByName(editorName);
		if (editor == null) {
			throw new TopLogicException(I18NConstants.EXCEPTION_NO_COMPONENT_FOUND);
		}
		if (!(editor instanceof EditMode)) {
			throw new TopLogicException(I18NConstants.EXCEPTION_NOT_AN_EDITOR);
		}
		return (EditMode) editor;
	}
}