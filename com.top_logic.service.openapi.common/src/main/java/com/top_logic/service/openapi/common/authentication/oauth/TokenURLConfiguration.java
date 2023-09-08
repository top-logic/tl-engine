/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.common.authentication.oauth;

import java.net.URL;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.URLFormat;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.func.misc.IsFalse;
import com.top_logic.basic.func.misc.IsTrue;
import com.top_logic.element.layout.meta.HideActiveIf;
import com.top_logic.element.layout.meta.HideActiveIfNot;
import com.top_logic.layout.form.values.edit.annotation.DynamicMandatory;
import com.top_logic.layout.form.values.edit.annotation.DynamicMode;

/**
 * Configuration of an token URL. This is made either by configuring the token URL direct or by
 * giving the Issuer of an Open-ID server.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface TokenURLConfiguration extends ConfigurationItem {

	/** Configuration name for {@link #getTokenURL()}. */
	String TOKEN_URL = "token-url";

	/** Configuration name for {@link #getUseOpenIdServer()}. */
	String USE_OPEN_ID_SERVER = "use-open-id-server";

	/** Configuration name for {@link #getOpenIDIssuer()}. */
	String OPEN_IS_ISSUER = "open-id-issuer";

	/**
	 * URL of the server to get or validate token.
	 */
	@Format(URLFormat.class)
	@Name(TOKEN_URL)
	@DynamicMode(fun = HideActiveIf.class, args = @Ref(USE_OPEN_ID_SERVER))
	@DynamicMandatory(fun = IsFalse.class, args = @Ref(USE_OPEN_ID_SERVER))
	URL getTokenURL();

	/**
	 * Setter for {@link #getTokenURL()}.
	 */
	void setTokenURL(URL value);

	/**
	 * {@link URL} of the Open-ID provider.
	 * 
	 * <p>
	 * When {@link #getUseOpenIdServer()} is set, this is the {@link URL} of the Open-ID provider's
	 * configuration information, which is used to get the valid token {@link URL}, i.e the issuer
	 * {@link URL} under which the self-description of the server can be found.
	 * </p>
	 */
	@Format(URLFormat.class)
	@Name(OPEN_IS_ISSUER)
	@DynamicMode(fun = HideActiveIfNot.class, args = @Ref(USE_OPEN_ID_SERVER))
	@DynamicMandatory(fun = IsTrue.class, args = @Ref(USE_OPEN_ID_SERVER))
	URL getOpenIDIssuer();

	/**
	 * Setter for {@link #getOpenIDIssuer()}.
	 */
	void setOpenIDIssuer(URL value);

	/**
	 * Whether an Open-ID provider is used to determine the token {@link URL}.
	 */
	@BooleanDefault(true)
	@Name(USE_OPEN_ID_SERVER)
	boolean getUseOpenIdServer();

	/**
	 * Setter for {@link #getUseOpenIdServer()}.
	 */
	void setUseOpenIdServer(boolean value);

}

