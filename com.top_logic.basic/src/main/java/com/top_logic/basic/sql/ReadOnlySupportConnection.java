/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.sql;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * A {@link ConnectionProxy} for {@link Connection}s that do not handle {@link #isReadOnly()},
 * {@link #setReadOnly(boolean)} correct.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ReadOnlySupportConnection extends ConnectionProxy {

	private final Connection _impl;

	private boolean _readOnly;

	/**
	 * Creates a new {@link ReadOnlySupportConnection}.
	 */
	public ReadOnlySupportConnection(Connection impl) {
		_impl = impl;
	}

	@Override
	protected Connection impl() throws SQLException {
		return _impl;
	}

	@Override
	public void setReadOnly(boolean a1) throws SQLException {
		super.setReadOnly(a1);
		_readOnly = a1;
	}

	@Override
	public boolean isReadOnly() throws SQLException {
		return _readOnly;
	}

	@Override
	public void commit() throws SQLException {
		if (isReadOnly()) {
			throw new SQLException("Connection is set to read only. Can not commit anything.");
		}
		super.commit();
	}

}

