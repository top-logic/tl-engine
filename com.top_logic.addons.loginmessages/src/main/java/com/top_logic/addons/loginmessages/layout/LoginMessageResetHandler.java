/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.addons.loginmessages.layout;

import java.util.Date;
import java.util.Map;

import com.top_logic.addons.loginmessages.model.intf.LoginMessage;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.util.ResKey;
import com.top_logic.element.layout.grid.GridComponent;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.DisplayContext;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.ConfirmCommandHandler;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * A {@link CommandHandler} for confirmable reset of {@link Person}'s confirmations of a {@link LoginMessage}.
 *
 * @author <a href=mailto:Dmitry.Ivanizki@top-logic.com>Dmitry Ivanizki</a>
 */
public class LoginMessageResetHandler extends ConfirmCommandHandler {

	/**
	 * Creates a new {@link LoginMessageResetHandler}.
	 */
	public LoginMessageResetHandler(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	protected HandlerResult internalHandleCommand(DisplayContext context, LayoutComponent component, Object model,
			Map<String, Object> someArguments) {
		final LoginMessage loginMessage = (LoginMessage) ((GridComponent) component).getSelected();
		try (Transaction transaction = loginMessage.tKnowledgeBase().beginTransaction()) {
			loginMessage.setConfirmExpirationDate(new Date());
			transaction.commit();
		}
		return HandlerResult.DEFAULT_RESULT;
	}

	@Override
	protected ResKey getConfirmMessage(DisplayContext context, LayoutComponent component, Object model,
			Map<String, Object> someArguments) {
		return I18NConstants.RESET_INFO_MESSAGE;
	}

}
