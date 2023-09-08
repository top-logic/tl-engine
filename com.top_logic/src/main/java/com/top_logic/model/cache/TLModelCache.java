/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.cache;

/**
 * The cache used by the {@link TLModelCacheService}.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public interface TLModelCache {

	/** A {@link TLModelOperations} instance valid for this sub-session. */
	TLModelOperations getValue();

}
