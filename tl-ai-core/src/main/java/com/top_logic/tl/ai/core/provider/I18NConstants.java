/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.provider;

import com.top_logic.basic.i18n.I18NConstantsBase;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;

/**
 * I18N constants for the provider package.
 */
public class I18NConstants extends I18NConstantsBase {

	/**
	 * Label of the command that loads the available models from the provider endpoint.
	 *
	 * @en Load models
	 * @de Modelle laden
	 */
	public static ResKey LOAD_MODELS;

	/**
	 * Tooltip of the button that clears the model allowlist.
	 *
	 * @en Clear models
	 * @de Modelle leeren
	 */
	public static ResKey CLEAR_MODELS;

	/**
	 * Info message reporting how many model IDs were appended to the models field.
	 *
	 * @en {0} model(s) added.
	 * @de {0} Modell(e) hinzugefügt.
	 */
	public static ResKey1 LOADED_MODELS__COUNT;

	/**
	 * Info message shown when the provider endpoint returns no models.
	 *
	 * @en The provider endpoint returned no models. Enter the model names manually.
	 * @de Der Anbieter-Endpunkt hat keine Modelle zurückgegeben. Bitte die Modellnamen manuell
	 *     eingeben.
	 */
	public static ResKey NO_MODELS_FROM_ENDPOINT;

	/**
	 * Error message shown when model discovery against the provider endpoint fails.
	 *
	 * @en Model discovery failed. Enter the model names manually.
	 * @de Die Modellabfrage ist fehlgeschlagen. Bitte die Modellnamen manuell eingeben.
	 */
	public static ResKey MODEL_DISCOVERY_FAILED;

	/**
	 * Error reported when an LLM provider is configured with both explicit credentials and an API
	 * key.
	 *
	 * @en The LLM provider "{0}" configures both credentials and an API key. Configure only one of
	 *     them.
	 * @de Der LLM-Anbieter "{0}" konfiguriert sowohl Zugangsdaten als auch einen API-Schlüssel.
	 *     Nur eines von beiden darf angegeben werden.
	 */
	public static ResKey1 ERROR_CREDENTIALS_AND_API_KEY__PROVIDER;

	static {
		initConstants(I18NConstants.class);
	}
}
