/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.action;

import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.scripting.runtime.action.AbstractApplicationActionOp;
import com.top_logic.layout.scripting.runtime.action.LoginActionOp;

/**
 * Pseudo action that enables a new user to be used in a script.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface LoginAction extends ApplicationAction {

	/**
	 * The ID of the script processor to start.
	 * 
	 * <p>
	 * The started script processor executes those actions that have the given value in their
	 * {@link #getUserID()} field.
	 * </p>
	 */
	@Mandatory
	String getProcessId();

	/**
	 * Name of the {@link Person account} to be used for executing following actions that are marked
	 * with {@link #getProcessId()} in their {@link ApplicationAction#getUserID()} field.
	 * 
	 * <p>
	 * If not set, the account with the name {@link #getProcessId()} is used.
	 * </p>
	 */
	@Nullable
	String getAccount();

	/**
	 * The password for the {@link #getAccount()}.
	 * 
	 * <p>
	 * If not set, the master script session asks for the password (if the {@link #getAccount()} is
	 * different from the user that executes the script).
	 * </p>
	 */
	@Nullable
	String getPassword();

	@Override
	@ClassDefault(LoginActionOp.class)
	Class<? extends AbstractApplicationActionOp<?>> getImplementationClass();

}
