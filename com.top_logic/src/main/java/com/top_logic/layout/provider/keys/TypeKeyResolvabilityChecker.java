/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.provider.keys;

import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.KeyStorage;
import com.top_logic.basic.keystorages.KeyStorageChecker;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLType;
import com.top_logic.model.util.TLModelUtil;

/**
 * {@link KeyStorageChecker} checking that all {@link TLClass}s used in a key exist.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class TypeKeyResolvabilityChecker implements KeyStorageChecker {

	private static final String META_ELEMENT_PREFIX = TLTypeKeyProvider.META_ELEMENT_PREFIX;

	private static final String META_ELEMENT_PREFIX_HISTORIC = TLTypeKeyProvider.META_ELEMENT_PREFIX_HISTORIC;

	/** The instance of the {@link TypeKeyResolvabilityChecker}. */
	public static final TypeKeyResolvabilityChecker INSTANCE = new TypeKeyResolvabilityChecker();

	@Override
	public Exception check(String key, String value) {
		if (!(key.startsWith(META_ELEMENT_PREFIX) || key.startsWith(META_ELEMENT_PREFIX_HISTORIC))) {
			return null;
		}
		if (KeyStorage.isDeactivated(value)) {
			return null;
		}
		int typeNameStart = key.indexOf(":") + 1;
		String typeName = key.substring(typeNameStart);
		return tryResolve(typeName);
	}

	private Exception tryResolve(String typeName) {
		try {
			TLType result = TLModelUtil.findType(typeName);
			assert result != null;
			return null;
		} catch (Exception ex) {
			return createError(typeName, ex);
		}
	}

	private Exception createError(String typeName, Exception ex) {
		String message = "Type '" + typeName + "' cannot be resolved.";
		return new ConfigurationError(message + " Cause: " + ex.getMessage(), ex);
	}

}
