/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.decorator;

import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable.Property;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.ModelResolver;
import com.top_logic.layout.table.TableData;
import com.top_logic.layout.table.TableDataOwner;

/**
 * {@link TableDataOwner} for the comparison table.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
class CompareTableOwner implements TableDataOwner {

	static final Property<CompareTableOwner> COMPARE_TABLE_OWNER =
		TypedAnnotatable.property(CompareTableOwner.class, "compareTable");

	private TableData _table;

	private FormMember _holder;

	/**
	 * Sets the {@link TableData} and an holder to attach this owner to.
	 */
	void init(TableData table, FormMember holder) {
		_table = table;
		_holder = holder;
		holder.set(COMPARE_TABLE_OWNER, this);
	}

	/**
	 * @param holder
	 *        a {@link FormMember} formerly given by {@link #init(TableData, FormMember)}.
	 */
	static CompareTableOwner getOwner(FormMember holder) {
		return holder.get(COMPARE_TABLE_OWNER);
	}

	FormMember getHolder() {
		return _holder;
	}

	@Override
	public ModelName getModelName() {
		return ModelResolver.buildModelName(this);
	}

	@Override
	public TableData getTableData() {
		return _table;
	}

}
