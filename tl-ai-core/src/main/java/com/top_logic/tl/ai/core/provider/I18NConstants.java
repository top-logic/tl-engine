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

	static {
		initConstants(I18NConstants.class);
	}
}
