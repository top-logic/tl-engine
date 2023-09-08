/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.gui.layout.webfolder;

import java.util.Map;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.knowledge.service.KnowledgeBaseException;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.Clipboard;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.basic.Command;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.CommandHandlerFactory;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * {@link CommandHandler} to {@link Clipboard#clear() clear} the {@link Clipboard}.
 * 
 * @author jco
 */
public class ClearClipboardHandler extends AbstractCommandHandler implements Command {

	/** The command provided by this instance. */
	public static final String COMMAND = "clearClipboard";

    /** I18N prefix for messages from this handler. */
	public static final ResPrefix CLIPBOARD_I18N = I18NConstants.CLIPBOARD_RESOURCES;

	/**
	 * Default CTor for the {@link CommandHandlerFactory}.
	 */
    public ClearClipboardHandler(InstantiationContext context, Config config) {
		super(context, config);
    }

	/**
	 * Wrap {@link Clipboard#clear()} in a {@link Transaction}.
	 */
	@Override
	public HandlerResult executeCommand(DisplayContext context) {
		HandlerResult theResult = HandlerResult.DEFAULT_RESULT;
		try {
			Clipboard theBoard = Clipboard.getInstance();
			Transaction tx = PersistencyLayer.getKnowledgeBase().beginTransaction();
			theBoard.clear();
			tx.commit();
		} catch (KnowledgeBaseException ex) {
			theResult = new HandlerResult();
			theResult.addErrorMessage(CLIPBOARD_I18N.key("clear.error.fail"));
		}
		return theResult;
	}

	/**
	 * Call {@link #executeCommand(DisplayContext)} and update the environment.
	 */
	@Override
	public HandlerResult handleCommand(DisplayContext aContext, LayoutComponent aComponent, Object model, Map<String, Object> ignored) {
		HandlerResult theResult = executeCommand(aContext);
		aComponent.invalidate();
		return theResult;
    }

}

