/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

import java.sql.SQLException;

import com.top_logic.basic.TLID;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.meta.MOReference;
import com.top_logic.dob.meta.MOReference.ReferencePart;
import com.top_logic.dob.meta.ObjectContext;
import com.top_logic.dob.sql.DBAttribute;

/**
 * The class {@link ItemResultAdapter} delegates all {@link ItemResult} methods
 * to an implementation.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class ItemResultAdapter extends QueryResultAdapter implements ItemResult {

	/**
	 * Returns the {@link ItemResult} which is used to dispatch methods to.
	 * Never <code>null</code>.
	 */
	@Override
	protected abstract ItemResult getImplementation();

	@Override
	public BinaryData getBlobValue(DBAttribute nameAttr, DBAttribute contentTypeAttr, DBAttribute sizeAttr,
			DBAttribute blobAttr) throws SQLException {
		return getImplementation().getBlobValue(nameAttr, contentTypeAttr, sizeAttr, blobAttr);
	}

	@Override
	public byte getByteValue(DBAttribute attribute) throws SQLException {
		return getImplementation().getByteValue(attribute);
	}

	@Override
	public TLID getIDValue(DBAttribute attribute) throws SQLException {
		return getImplementation().getIDValue(attribute);
	}

	@Override
	public String getClobStringValue(DBAttribute attribute) throws SQLException {
		return getImplementation().getClobStringValue(attribute);
	}

	@Override
	public double getDoubleValue(DBAttribute attribute) throws SQLException {
		return getImplementation().getDoubleValue(attribute);
	}

	@Override
	public float getFloatValue(DBAttribute attribute) throws SQLException {
		return getImplementation().getFloatValue(attribute);
	}

	@Override
	public int getIntValue(DBAttribute attribute) throws SQLException {
		return getImplementation().getIntValue(attribute);
	}

	@Override
	public long getLongValue(DBAttribute attribute) throws SQLException {
		return getImplementation().getLongValue(attribute);
	}

	@Override
	public String getStringValue(DBAttribute attribute) throws SQLException {
		return getImplementation().getStringValue(attribute);
	}

	@Override
	public String getStringValue(MOReference attribute, ReferencePart part) throws SQLException {
		return getImplementation().getStringValue(attribute, part);
	}

	@Override
	public Object getValue(MOAttribute attribute, ObjectContext context) throws SQLException {
		return getImplementation().getValue(attribute, null);
	}

}
