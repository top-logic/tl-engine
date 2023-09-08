/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

/**
 * Default {@link Values} implementation based on a {@link ValidityChainImpl}.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ValuesImpl extends ValidityChainImpl<Values> implements Values {

	/** @see #getData() */
	private final Object[] _data;

	/**
	 * Creates a new {@link ValuesImpl} object.
	 * 
	 * @param minValidity
	 *        Value of {@link #minValidity()}.
	 * @param maxValidity
	 *        Value of {@link #maxValidity()}.
	 * @param data
	 *        Value of {@link #getData()}.
	 */
	public ValuesImpl(long minValidity, long maxValidity, Object[] data) {
		super(minValidity, maxValidity);
		_data = data;
	}

	@Override
	public Object[] getData() {
		return _data;
	}

	@Override
	public boolean isAlive() {
		return true;
	}

	/**
	 * Throws an {@link DeletedObjectAccess} that the values are not alive.
	 */
	protected DeletedObjectAccess deletedObjectAccess() {
		StringBuilder error = new StringBuilder();
		error.append("Values are deleted in from revision ");
		error.append(minValidity());
		error.append(" to ");
		error.append(maxValidity());
		throw new DeletedObjectAccess(error.toString());
	}

	@Override
	public String toString() {
		StringBuilder tmp = new StringBuilder();
		tmp.append("Values[");
		ValidityChainImpl.appendValidityRange(tmp, this);
		if (formerValidity() != null) {
			tmp.append(",older:");
			tmp.append(formerValidity());
		}
		tmp.append("]");
		return tmp.toString();
	}

}

