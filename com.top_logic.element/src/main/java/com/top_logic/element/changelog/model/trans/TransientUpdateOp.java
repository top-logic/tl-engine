/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.changelog.model.trans;

import com.top_logic.element.changelog.model.TlChangelogFactory;
import com.top_logic.element.changelog.model.UpdateOp;
import com.top_logic.model.impl.TransientTLObjectImpl;

/**
 * Transient {@link UpdateOp}.
 */
public class TransientUpdateOp extends TransientTLObjectImpl implements UpdateOp {
	/**
	 * Creates a {@link TransientUpdateOp}.
	 *
	 */
	public TransientUpdateOp() {
		super(TlChangelogFactory.getUpdateOpType(), null);
	}
}
