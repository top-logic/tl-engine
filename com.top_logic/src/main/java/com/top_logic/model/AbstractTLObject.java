/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model;

import com.top_logic.dob.identifier.ObjectKey;

/**
 * Base class for implementing {@link TLObject}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractTLObject implements TLObject {

	@Override
	public boolean equals(Object other) {
		if (other == this) {
			return (true);
		}

		if (!(other instanceof TLObject)) {
			return false;
		}

		ObjectKey selfId = tId();
		ObjectKey otherId = ((TLObject) other).tId();

		if (selfId == null || otherId == null) {
			// Since other != this, this are not the same objects.
			return false;
		}

		return selfId.equals(otherId);
	}

	@Override
	public int hashCode() {
		ObjectKey selfId = tId();
		if (selfId == null) {
			return super.hashCode();
		}

		return selfId.hashCode();
	}

}
