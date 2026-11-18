/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.accesstoken;

import com.top_logic.element.meta.kbbased.AttributedWrapper;
import com.top_logic.knowledge.objects.KnowledgeObject;

/**
 * The implementation of {@link AccessToken}, bound to the type <code>tl.accounts:AccessToken</code>
 * in the model.
 */
public class AccessTokenImpl extends AttributedWrapper implements AccessToken {

	/**
	 * Creates a {@link AccessTokenImpl} for the given persistent object.
	 */
	public AccessTokenImpl(KnowledgeObject ko) {
		super(ko);
	}

}
