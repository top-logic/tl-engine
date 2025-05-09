/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.changelog.model.trans;

import com.top_logic.element.changelog.model.TlChangelogFactory;
import com.top_logic.element.changelog.model.WarningMessage;
import com.top_logic.model.impl.TransientTLObjectImpl;

/**
 * Transient {@link WarningMessage}.
 */
public class TransientWarningMessage extends TransientTLObjectImpl implements WarningMessage {
	/**
	 * Creates a {@link TransientWarningMessage}.
	 *
	 */
	public TransientWarningMessage() {
		super(TlChangelogFactory.getWarningMessageType(), null);
	}
}
