/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.search;

import com.top_logic.basic.annotation.FrameworkInternal;
import com.top_logic.basic.db.sql.SQLLimit;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.service.Revision;

/**
 * Additional configuration of a {@link RevisionQuery}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class RevisionQueryArguments extends QueryArguments<RevisionQueryArguments> {
	
	/** Argument to set as start row to get the first row in the result. */
	public static final int FIRST_ROW = SQLLimit.FIRST_ROW;

	private static final int NO_SPECIAL_DATA_REVISION = -1;

	private long requestedRevision = Revision.CURRENT_REV;

	private long dataRevision = NO_SPECIAL_DATA_REVISION;
	private int startRow = 0; 
	
	RevisionQueryArguments() {
	}

	/**
	 * The {@link KnowledgeItem#getHistoryContext() revision} that the result objects must
	 *         have.
	 */
	public long getRequestedRevision() {
		return requestedRevision;
	}
	
	/**
	 * Sets value of {@link #getRequestedRevision()}.
	 * 
	 * @return A reference to this {@link RevisionQueryArguments} object.
	 * 
	 * @see #getRequestedRevision()
	 */
	public RevisionQueryArguments setRequestedRevision(long requestedRevision) {
		this.requestedRevision = requestedRevision;
		return this;
	}
	
	/**
	 * Sets value of {@link #getDataRevision()}.
	 * 
	 * @return A reference to this {@link RevisionQueryArguments} object.
	 * 
	 * @see #getDataRevision()
	 */
	@FrameworkInternal
	public RevisionQueryArguments setDataRevision(long dataRevision) {
		this.dataRevision = dataRevision;
		return this;
	}

	/**
	 * The revision of the data the result objects must have.
	 */
	@FrameworkInternal
	public long getDataRevision() {
		if (isDataRevisionSet()) {
			return dataRevision;
		}
		return getRequestedRevision();
	}

	/**
	 * Whether an explicit {@link #getDataRevision() data revision} was set.
	 */
	@FrameworkInternal
	public boolean isDataRevisionSet() {
		return dataRevision != NO_SPECIAL_DATA_REVISION;
	}

	public int getStartRow() {
		return startRow;
	}
	
	public RevisionQueryArguments setStartRow(int startRow) {
		this.startRow = startRow;
		return this;
	}

	@Override
	protected RevisionQueryArguments chain() {
		return this;
	}
	
}
