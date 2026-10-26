/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.provider;

import com.top_logic.basic.i18n.I18NConstantsBase;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.basic.util.ResKey2;
import com.top_logic.basic.util.ResKey3;

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
	 * Error of a run that needs an AI model while no provider is configured at all.
	 *
	 * @en No AI provider is configured.
	 * @de Es ist kein KI-Anbieter eingerichtet.
	 */
	public static ResKey ERROR_NO_PROVIDER_CONFIGURED;

	/**
	 * Error of a run that needs an AI model while no configured provider has its credentials.
	 *
	 * @en No AI provider is configured. Provide the API key of a provider as one of the secrets {0}.
	 * @de Es ist kein KI-Anbieter eingerichtet. Hinterlegen Sie den API-Schlüssel eines Anbieters als
	 *     eines der Secrets {0}.
	 */
	public static ResKey1 ERROR_NO_PROVIDER_AVAILABLE__SECRETS;

	/**
	 * A list of secrets in a message, the given list extended by the given secret.
	 *
	 * @en {0}, {1}
	 * @de {0}, {1}
	 */
	public static ResKey2 SECRET_LIST__LIST_SECRET;

	/**
	 * Error of a run on a model whose provider does not have its credentials.
	 *
	 * @en The AI model {0} cannot be used: the credentials of its provider {1} are not available.
	 *     Provide the secret {2}.
	 * @de Das KI-Modell {0} kann nicht verwendet werden: Die Zugangsdaten seines Anbieters {1} sind
	 *     nicht verfügbar. Hinterlegen Sie das Secret {2}.
	 */
	public static ResKey3 ERROR_PROVIDER_UNAVAILABLE__MODEL_PROVIDER_SECRET;

	/**
	 * Error of a run on a model whose provider does not have its credentials, when the provider
	 * cannot tell where they come from.
	 *
	 * @en The AI model {0} cannot be used: the credentials of its provider {1} are not available.
	 * @de Das KI-Modell {0} kann nicht verwendet werden: Die Zugangsdaten seines Anbieters {1} sind
	 *     nicht verfügbar.
	 */
	public static ResKey2 ERROR_PROVIDER_UNAVAILABLE__MODEL_PROVIDER;

	/**
	 * Error of the model discovery of a provider that does not have its credentials.
	 *
	 * @en The models cannot be loaded: the credentials of the provider {0} are not available.
	 *     Provide the secret {1}.
	 * @de Die Modelle können nicht geladen werden: Die Zugangsdaten des Anbieters {0} sind nicht
	 *     verfügbar. Hinterlegen Sie das Secret {1}.
	 */
	public static ResKey2 ERROR_DISCOVERY_UNAVAILABLE__PROVIDER_SECRET;

	/**
	 * Error of the model discovery of a provider that does not have its credentials, when the
	 * provider cannot tell where they come from.
	 *
	 * @en The models cannot be loaded: the credentials of the provider {0} are not available.
	 * @de Die Modelle können nicht geladen werden: Die Zugangsdaten des Anbieters {0} sind nicht
	 *     verfügbar.
	 */
	public static ResKey1 ERROR_DISCOVERY_UNAVAILABLE__PROVIDER;

	static {
		initConstants(I18NConstants.class);
	}
}
