/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.accesstoken;

import com.top_logic.basic.i18n.I18NConstantsBase;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;

/**
 * Messages to the person creating an access token.
 */
public class I18NConstants extends I18NConstantsBase {

	/**
	 * A token was to be created without a name.
	 *
	 * @en Give the token a name, for example the client you are going to use it in.
	 * @de Geben Sie dem Token einen Namen, zum Beispiel den des Clients, in dem Sie es verwenden.
	 */
	public static ResKey ERROR_TOKEN_NAME_MISSING;

	/**
	 * A token was to be created with a validity of zero or fewer days.
	 *
	 * @en A token is valid for at least one day. Leave the field empty for a token that does not
	 *     expire.
	 * @de Ein Token ist mindestens einen Tag gültig. Lassen Sie das Feld leer für ein Token, das
	 *     nicht abläuft.
	 */
	public static ResKey ERROR_TOKEN_VALIDITY_NOT_POSITIVE;

	/**
	 * A token was to be created without a permission.
	 *
	 * @en Choose at least one permission: a token without a permission admits to nothing.
	 * @de Wählen Sie mindestens eine Berechtigung: Ein Token ohne Berechtigung lässt zu nichts zu.
	 */
	public static ResKey ERROR_NO_PERMISSION;

	/**
	 * A token was to be created with a permission no module of the application offers.
	 *
	 * @en The permission "{0}" is not offered by this application.
	 * @de Die Berechtigung "{0}" wird von dieser Anwendung nicht angeboten.
	 */
	public static ResKey1 ERROR_UNKNOWN_PERMISSION__NAME;

	static {
		initConstants(I18NConstants.class);
	}
}
