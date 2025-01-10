/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.common.authentication.oauth;

import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.element.layout.meta.HideActiveIfNot;
import com.top_logic.layout.form.values.edit.annotation.DynamicMode;
import com.top_logic.service.openapi.common.authentication.ServerAuthentication;

/**
 * {@link TokenBasedAuthentication} to authenticate by using client credentials or users access
 * token.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@TagName("client-credentials-authentication")
@Label("OpenID authentication")
@DisplayOrder({
	ServerCredentials.IN_USER_CONTEXT,
	ServerCredentials.USERNAME_FIELD,
})
public interface ServerCredentials extends TokenBasedAuthentication, ServerAuthentication {

	/**
	 * Configuration name for {@link #isInUserContext()}.
	 */
	String IN_USER_CONTEXT = "in-user-context";

	/**
	 * Configuration name for {@link #getUsernameField()}.
	 */
	String USERNAME_FIELD = "username-field";

	/**
	 * Whether the protected operation must be executed in user context.
	 * 
	 * <p>
	 * If this property is set, then it is expected that the sent access token is the personal token
	 * of a person which has an account in the application. The operation is then processed as if
	 * the user has executed it after a login.
	 * </p>
	 */
	@Name(IN_USER_CONTEXT)
	boolean isInUserContext();

	/**
	 * Setter for {@link #isInUserContext()}.
	 */
	void setInUserContext(boolean value);

	/**
	 * Name of the field in the token introspection response which contains the user.
	 * 
	 * <p>
	 * When no field is configured the default field "username" is used to find the user name.
	 * </p>
	 */
	@Name(USERNAME_FIELD)
	@DynamicMode(fun = HideActiveIfNot.class, args = @Ref(IN_USER_CONTEXT))
	String getUsernameField();

	/**
	 * Setter for {@link #getUsernameField()}.
	 */
	void setUsernameField(String value);

}

