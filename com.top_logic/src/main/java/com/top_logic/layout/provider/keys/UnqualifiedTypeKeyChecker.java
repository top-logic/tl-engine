/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.provider.keys;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.keystorages.KeyStorageChecker;
import com.top_logic.model.TLClass;

/**
 * {@link KeyStorageChecker} checking that all {@link TLClass} names are qualified.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class UnqualifiedTypeKeyChecker implements KeyStorageChecker {

	private static final String META_ELEMENT_PREFIX = TLTypeKeyProvider.META_ELEMENT_PREFIX;

	private static final String META_ELEMENT_PREFIX_HISTORIC = TLTypeKeyProvider.META_ELEMENT_PREFIX_HISTORIC;

	/** The instance of the {@link UnqualifiedTypeKeyChecker}. */
	public static final UnqualifiedTypeKeyChecker INSTANCE = new UnqualifiedTypeKeyChecker();

	@Override
	public Exception check(String key, String value) {
		if (isUnqualified(key, META_ELEMENT_PREFIX) || isUnqualified(key, META_ELEMENT_PREFIX_HISTORIC)) {
			return createError(key);
		}
		return null;
	}

	private Exception createError(String key) {
		String message = "Unqualified type name: '" + key + "'. Use the qualified name instead."
			+ " Unqualified names can cause clashes, which can break the application.";
		return new ConfigurationException(message);
	}

	private boolean isUnqualified(String key, String mePrefix) {
		return key.startsWith(mePrefix) && (key.indexOf(":", mePrefix.length()) == -1);
	}

}
