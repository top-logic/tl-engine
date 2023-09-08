/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table;

import com.top_logic.basic.col.Mapping;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable.Property;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.scripting.recorder.ref.GenericModelOwner;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.ModelResolver;

/**
 * {@link GenericModelOwner} for {@link TableData}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class GenericTableDataOwner extends GenericModelOwner<TableData> implements TableDataOwner {

	/**
	 * Algorithm to find the {@link TableData} as attached object to a {@link FormMember}.
	 * 
	 * <p>
	 * To initialise the reference call {@link #annotate(FormMember, TableData)}.
	 * </p>
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static class FormMemberAlgorithm implements Mapping<Object, TableData> {

		private static final Property<TableData> TABLE_DATA = TypedAnnotatable.property(TableData.class, "table data");

		/** Singleton {@link FormMemberAlgorithm} instance. */
		public static final FormMemberAlgorithm INSTANCE = new FormMemberAlgorithm();

		private FormMemberAlgorithm() {
			// singleton instance
		}

		@Override
		public TableData map(Object input) {
			return ((FormMember) input).get(TABLE_DATA);
		}

		/**
		 * Annotates the given {@link TableData} to the {@link FormMember} to be found later.
		 */
		public void annotate(FormMember reference, TableData data) {
			reference.set(TABLE_DATA, data);
		}

		/**
		 * Resets the annotated {@link TableData} from the {@link FormMember}.
		 */
		public void resetAnnotation(FormMember reference) {
			reference.reset(TABLE_DATA);
		}

	}

	/**
	 * Creates a new {@link GenericTableDataOwner}.
	 */
	public GenericTableDataOwner(Object reference, Mapping<Object, TableData> algorithm) {
		super(reference, algorithm);
	}

	@Override
	public ModelName getModelName() {
		return ModelResolver.buildModelNameIfAvailable(this).get();
	}

	@Override
	public TableData getTableData() {
		return getModel();
	}

}

