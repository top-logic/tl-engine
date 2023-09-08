/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.addons.loginmessages.layout;

import java.util.List;

import com.top_logic.addons.loginmessages.layout.LoginMessageNamingScheme.LoginMessageName;
import com.top_logic.addons.loginmessages.model.LoginMessagesUtil;
import com.top_logic.addons.loginmessages.model.intf.LoginMessage;
import com.top_logic.basic.util.Utils;
import com.top_logic.layout.scripting.recorder.ref.AbstractModelNamingScheme;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.ModelNamingScheme;
import com.top_logic.layout.scripting.runtime.ActionContext;

/**
 * A {@link ModelNamingScheme} for {@link LoginMessage}s.
 *
 * @author <a href="mailto:Dmitry.Ivanizki@top-logic.com">Dmitry Ivanizki</a>
 */
public class LoginMessageNamingScheme extends AbstractModelNamingScheme<LoginMessage, LoginMessageName> {

	/**
	 * A {@link ModelName} for a {@link LoginMessage}.
	 *
	 * @author <a href=mailto:Dmitry.Ivanizki@top-logic.com>Dmitry Ivanizki</a>
	 */
	public interface LoginMessageName extends ModelName {

		/** Name getter. */
		public String getName();

		/** Name setter. */
		public void setName(String name);

	}

	@Override
	public Class<LoginMessageName> getNameClass() {
		return LoginMessageName.class;
	}

	@Override
	public Class<LoginMessage> getModelClass() {
		return LoginMessage.class;
	}

	@Override
	public LoginMessage locateModel(ActionContext context, LoginMessageName name) {
		String loginMessageName = name.getName();
		List<LoginMessage> loginMessages = LoginMessagesUtil.getLoginMessages();
		for (LoginMessage loginMessage : loginMessages) {
			if (Utils.equals(loginMessageName, loginMessage.getName())) {
				return loginMessage;
			}
		}
		throw new RuntimeException("Could not find loginMessage: '" + name
			+ "'; Candidates: " + loginMessages);
	}

	@Override
	protected void initName(LoginMessageName name, LoginMessage model) {
		name.setName(model.getName());
	}
}
