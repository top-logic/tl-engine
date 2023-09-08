/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.wysiwyg.ui;

import java.util.Map;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.WindowScope;
import com.top_logic.layout.basic.ControlCommand;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Handler to create a top-logic object link.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class CreateTLObjectLink extends ControlCommand {

	/**
	 * Singleton {@link CreateTLObjectLink} instance.
	 */
	public static final CreateTLObjectLink INSTANCE = new CreateTLObjectLink();

	private static final String COMMAND_ID = "createTLObjectLink";

	/**
	 * Creates a {@link CreateTLObjectLink} command with the given id.
	 */
	public CreateTLObjectLink() {
		super(COMMAND_ID);
	}

	@Override
	public ResKey getI18NKey() {
		return I18NConstants.CREATE_TL_OBJECT_LINK;
	}

	@Override
	protected HandlerResult execute(DisplayContext commandContext, Control control, Map<String, Object> arguments) {
		StructuredTextControl editorControl = (StructuredTextControl) control;
		WindowScope windowScope = editorControl.getFrameScope().getWindowScope();
		SelectObjectDialog objectDialog = new SelectObjectDialog(editorControl);
		objectDialog.open(windowScope);

		return HandlerResult.DEFAULT_RESULT;
	}

}
