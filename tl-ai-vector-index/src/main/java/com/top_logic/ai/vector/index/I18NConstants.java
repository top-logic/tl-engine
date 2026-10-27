/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.vector.index;

import com.top_logic.basic.i18n.I18NConstantsBase;
import com.top_logic.basic.util.ResKey;

/**
 * Internationalization constants for this package.
 */
public class I18NConstants extends I18NConstantsBase {

	/**
	 * Error reported when the vector index is searched although it has no embedding model, e.g.
	 * because no Mistral API key is available.
	 *
	 * @en The vector index is not available: no embedding model is configured. Ask the administrator
	 *     to configure a Mistral API key for the vector index.
	 * @de Der Vektorindex ist nicht verfügbar: Es ist kein Embedding-Modell konfiguriert. Bitten Sie
	 *     den Administrator, einen Mistral-API-Schlüssel für den Vektorindex zu konfigurieren.
	 */
	public static ResKey ERROR_VECTOR_INDEX_NOT_AVAILABLE;

	static {
		initConstants(I18NConstants.class);
	}

}
