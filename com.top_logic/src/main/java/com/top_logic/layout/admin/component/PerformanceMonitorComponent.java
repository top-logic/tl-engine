/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.admin.component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.admin.component.PerformanceDataEntryAccessor.NumberKind;
import com.top_logic.layout.admin.component.PerformanceMonitor.PerformanceDataEntry;
import com.top_logic.layout.admin.component.PerformanceMonitor.PerformanceDataEntryIntervals;
import com.top_logic.layout.form.component.FormComponent;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.layout.form.model.TableField;
import com.top_logic.layout.table.SortConfigFactory;
import com.top_logic.layout.table.model.ColumnConfiguration;
import com.top_logic.layout.table.model.FormTableModel;
import com.top_logic.layout.table.model.ObjectTableModel;
import com.top_logic.layout.table.model.TableConfiguration;
import com.top_logic.tool.execution.I18NConstants;

/**
 * Show {@link PerformanceDataEntry}
 * 
 * @author <a href="mailto:kbu@top-logic.com">kbu</a>
 */
public class PerformanceMonitorComponent extends FormComponent {

	public interface Config extends FormComponent.Config {
		@Name(XML_ATT_NUMBER_KIND)
		@StringDefault("max")
		String getShow();
	}

	/** The table field name. */
	public static final String PERFORMANCE_TABLE = "performanceTable";

	private static final String[] PERFORMANCE_DATA_ENTRY_COLUMN_NAMES = new String[] {
		PerformanceDataEntryAccessor.TRIGGER_NAME, PerformanceDataEntryAccessor.CONTEXT_NAME };

	private static final String XML_ATT_NUMBER_KIND = "show";

	private PerformanceDataEntryAccessor accessor;

	/**
	 * Create a new PerformanceMonitorComponent ...
	 */
	public PerformanceMonitorComponent(InstantiationContext context, Config atts) throws ConfigurationException {
		super(context, atts);
		String theKind = atts.getShow();
		NumberKind numKind = NumberKind.getKind(theKind);
		this.accessor = new PerformanceDataEntryAccessor(numKind);
	}

	/** 
	 * @see com.top_logic.layout.form.component.FormComponent#createFormContext()
	 */
	@Override
	public FormContext createFormContext() {
		FormContext theContext = new FormContext(this);

		ObjectTableModel innerTableModel = this.createTableModel();
		if (innerTableModel != null) {
			FormGroup theGroup = new FormGroup("theRowGroup", this.getResPrefix());
			theContext.addMember(theGroup);
			FormTableModel formTableModel = new FormTableModel(innerTableModel, theGroup);
			TableField tableField = FormFactory.newTableField(PERFORMANCE_TABLE, formTableModel, true);
			tableField.setSelectable(false);
			theContext.addMember(tableField);
		}

		return theContext;
	}

	@Override
	protected void becomingVisible() {
		Object model =
			new ArrayList<>(PerformanceMonitor.getInstance()
				.getIntervalPerformanceData(
					PerformanceMonitor.getInstance().getPerformanceDataGroupedBy(false, false, true, true)));
		setModel(model);
		super.becomingVisible();
	}

	@Override
	protected void becomingInvisible() {
		super.becomingInvisible();

		setModel(null);
	}

	@Override
	public ResKey hideReason(Object potentialModel) {
		if (!PerformanceMonitor.isEnabled()) {
			return I18NConstants.ERROR_FUNCTIONALITY_DISABLED;
		}
		return super.hideReason(potentialModel);
	}

	private ObjectTableModel createTableModel() {
		Object theModel = this.getModel();
		if (theModel instanceof Collection) {
			@SuppressWarnings("unchecked")
			List<PerformanceDataEntryIntervals> elements = (List<PerformanceDataEntryIntervals>) this.getModel();

			TableConfiguration config = TableConfiguration.table();
			config.getDefaultColumn().setAccessor(this.accessor);
			adaptTableConfiguration(PERFORMANCE_TABLE, config);
			if (this.getModel() instanceof PerformanceDataEntryAccessor) {
				config
					.setDefaultSortOrder(
						Collections.singleton(SortConfigFactory.ascending(PerformanceDataEntryAccessor.TRIGGER_NAME)));
			} else {
				config.setDefaultSortOrder(
					Collections
						.singleton(SortConfigFactory.sortConfig(PerformanceDataEntryAccessor.CONTEXT_NAME, true)));
			}

			String[] theCols = new String[PERFORMANCE_DATA_ENTRY_COLUMN_NAMES.length + 15];
			for (int i = 0; i < PERFORMANCE_DATA_ENTRY_COLUMN_NAMES.length; i++) {
				theCols[i] = PERFORMANCE_DATA_ENTRY_COLUMN_NAMES[i];
			}
			int start = PERFORMANCE_DATA_ENTRY_COLUMN_NAMES.length;
			for (int i = 0; i < 15; i++) {
				String columnName = "" + (i - 15 + 1);
				theCols[start + i] = columnName;
				ColumnConfiguration column = config.declareColumn(columnName);
				column.setColumnLabel(columnName);
			}

			return new ObjectTableModel(theCols, config, elements);
		}

		return null;
	}

}
