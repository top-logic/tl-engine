/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.control;

import java.util.Map;

import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable.Property;
import com.top_logic.layout.basic.AbstractControl;
import com.top_logic.layout.basic.AbstractControlBase;
import com.top_logic.layout.basic.AttachedPropertyListener;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.form.FormHandler;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.scripting.recorder.ScriptingRecorder;
import com.top_logic.layout.scripting.recorder.ref.AbstractModelNamingScheme;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.ModelNamingScheme;
import com.top_logic.layout.scripting.recorder.ref.ModelResolver;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.layout.table.TableData;

/**
 * {@link TableData} proxy implementing {@link FormHandler}.
 * 
 * <p>
 * Used as {@link FormContext#setOwningModel(FormHandler) owning model} of a {@link FormContext} for
 * table filter forms to enable script recording.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class FilterFormOwner implements FormHandler {

	static final Property<Map<String, FilterFormOwner>> FILTER_FORM_OWNERS =
		TypedAnnotatable.propertyMap("filterFormOwner");

	/**
	 * {@link ModelNamingScheme} for {@link FilterFormOwner}.
	 */
	public static class Naming extends AbstractModelNamingScheme<FilterFormOwner, Naming.Name> {

		/**
		 * {@link ModelName} for {@link FilterFormOwner}.
		 */
		public interface Name extends ModelName {

			/**
			 * The context table.
			 */
			ModelName getTableName();

			/**
			 * @see FilterFormOwner#getTableData()
			 */
			void setTableName(ModelName modelName);

			/**
			 * The name/id of the form context associated with the table of {@link #getTableName()}
			 */
			String getContextName();

			/**
			 * @see #getContextName()
			 */
			void setContextName(String contexName);
		}

		@Override
		public Class<Name> getNameClass() {
			return Name.class;
		}

		@Override
		public Class<FilterFormOwner> getModelClass() {
			return FilterFormOwner.class;
		}

		@Override
		protected void initName(Name name, FilterFormOwner model) {
			name.setTableName(model.getTableData().getModelName());
			name.setContextName(model.getFormId());
		}

		@Override
		public FilterFormOwner locateModel(ActionContext context, Name name) {
			TableData tableData = (TableData) ModelResolver.locateModel(context, name.getTableName());
			Map<String, FilterFormOwner> filterFormOwners = tableData.get(FILTER_FORM_OWNERS);
			return filterFormOwners.get(name.getContextName());
		}
	}

	private final TableData _tableData;
	private final FormContext _filterForm;
	private final String _formId;

	/**
	 * Creates a {@link FilterFormOwner}
	 */
	public FilterFormOwner(TableData theTableData, FormContext filterForm, String formId) {
		_tableData = theTableData;
		_filterForm = filterForm;
		_formId = formId;
	}

	/**
	 * The wrapped {@link TableData}.
	 */
	public TableData getTableData() {
		return _tableData;
	}

	@Override
	public ModelName getModelName() {
		return ModelResolver.buildModelName(this);
	}

	@Override
	public boolean hasFormContext() {
		return true;
	}

	@Override
	public FormContext getFormContext() {
		return _filterForm;
	}

	/**
	 * explicit given id of {@link #getFormContext()}
	 */
	public String getFormId() {
		return _formId;
	}

	@Override
	public Command getDiscardClosure() {
		return null;
	}

	@Override
	public Command getApplyClosure() {
		return null;
	}

	/**
	 * Adds this {@link FilterFormOwner} to the filter form owner registry of the underlying table
	 * data, so that it can be found in scripted tests.
	 */
	public void registerToTableData() {
		Map<String, FilterFormOwner> filterFormOwnerMap = getFilterFormOwnerMap(getTableData());
		filterFormOwnerMap.put(getFormId(), this);
	}

	/**
	 * Register a new {@link FilterFormOwner} for the {@link FormContext} to the {@link TableData}.
	 * <p>
	 * It will be unregistered when the given control is detached.
	 * </p>
	 */
	public static void register(FormContext formContext, TableData tableData, final AbstractControlBase control) {
		final FilterFormOwner filterFormOwner = new FilterFormOwner(tableData, formContext, formContext.getName());
		formContext.setOwningModel(filterFormOwner);
		if (!ScriptingRecorder.isEnabled()) {
			return;
		}
		filterFormOwner.registerToTableData();
		control.addListener(AbstractControl.ATTACHED_PROPERTY, new AttachedPropertyListener() {

			@Override
			public void handleAttachEvent(AbstractControlBase sender, Boolean oldValue, Boolean newValue) {
				if (!newValue) {
					filterFormOwner.deregisterFromTableData();
					sender.removeListener(AbstractControl.ATTACHED_PROPERTY, this);
				}
			}

		});
	}

	/**
	 * @see #registerToTableData()
	 */
	public void deregisterFromTableData() {
		Map<String, FilterFormOwner> filterFormOwnerMap = getFilterFormOwnerMap(getTableData());
		filterFormOwnerMap.remove(getFormId());
	}

	private Map<String, FilterFormOwner> getFilterFormOwnerMap(TableData tableData) {
		return tableData.mkMap(FILTER_FORM_OWNERS);
	}
}