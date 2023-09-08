/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

import com.top_logic.basic.ArrayUtil;
import com.top_logic.knowledge.service.Revision;

/**
 * Immutable empty implementation of {@link Values}.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public final class EmptyValues implements Values {

	/** Singleton {@link EmptyValues} instance. */
	public static final EmptyValues INSTANCE = new EmptyValues();

	private EmptyValues() {
		// singleton instance
	}

	@Override
	public void updateMaxValidity(long validity) {
		throw new UnsupportedOperationException("This Values object is always valid.");
	}

	@Override
	public void publishLocalValidity(long newMinValidity) {
		throw new UnsupportedOperationException("This Values object is always valid.");
	}

	@Override
	public long minValidity() {
		return 0;
	}

	@Override
	public long maxValidity() {
		return Revision.CURRENT_REV;
	}

	@Override
	public Values formerValidity() {
		return null;
	}

	@Override
	public void setFormerValidity(Values formerValidity) {
		throw new UnsupportedOperationException(
			"This Values object is always valid. For this reason no old values must be set.");
	}

	@Override
	public void cleanup() {
		// no old data here
	}

	@Override
	public Object[] getData() {
		return ArrayUtil.EMPTY_ARRAY;
	}

	@Override
	public boolean isAlive() {
		return true;
	}

}

