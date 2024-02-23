/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.migration.data;

import com.top_logic.basic.TLID;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.equal.EqualityByValue;
import com.top_logic.model.TLModelPart;
import com.top_logic.model.TLObject;

/**
 * Identifier of a {@link TLModelPart} in the migration.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface BranchIdType extends EqualityByValue {

	/**
	 * Branch in which the model part lives.
	 */
	long getBranch();

	/**
	 * Setter for {@link #getBranch()}.
	 */
	void setBranch(long branch);

	/**
	 * Internal ID of the {@link TLModelPart}.
	 * 
	 * @see TLObject#tIdLocal()
	 */
	TLID getID();

	/**
	 * Setter for {@link #getID()}.
	 */
	void setID(TLID id);

	/**
	 * Table in which the {@link TLModelPart} is stored.
	 * 
	 * @see TLObject#tTable()
	 */
	String getTable();

	/**
	 * Setter for {@link #getTable()}.
	 */
	void setTable(String name);

	/**
	 * Visitor pattern for {@link BranchIdType}.
	 */
	<R, A> R visit(BranchIdTypeVisitor<R, A> v, A arg);

	/**
	 * Creates a new instance of the given {@link BranchIdType}.
	 */
	static <T extends BranchIdType> T newInstance(Class<T> configType, long branch, TLID id, String table) {
		T branchIdType = TypedConfiguration.newConfigItem(configType);
		branchIdType.setBranch(branch);
		branchIdType.setID(id);
		branchIdType.setTable(table);
		return branchIdType;
	}

}

