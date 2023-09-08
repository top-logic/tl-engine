/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.compare;

import java.util.Date;
import java.util.List;
import java.util.Set;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.layout.DisplayValue;
import com.top_logic.layout.basic.ResourceText;
import com.top_logic.layout.component.model.SelectionListener;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.TableField;
import com.top_logic.layout.provider.DateTimeLabelProvider;
import com.top_logic.layout.table.ConfigKey;
import com.top_logic.layout.table.TableModel;
import com.top_logic.layout.table.TableViewModel;
import com.top_logic.layout.table.model.CachedObjectTableModel;
import com.top_logic.layout.table.model.TableConfiguration;
import com.top_logic.layout.table.model.TableConfigurationFactory;
import com.top_logic.layout.table.model.TableConfigurationProvider;
import com.top_logic.layout.table.model.TableUtil;
import com.top_logic.layout.toolbar.ToolBar;
import com.top_logic.layout.toolbar.ToolBarChangeListener;
import com.top_logic.layout.toolbar.ToolBarOwner;
import com.top_logic.mig.html.SelectionModel;

/**
 * {@link AbstractRevisionSelectComponent} that allows selecting a {@link Revision} from a list of
 * {@link Revision}s.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class RevisionSelectComponent extends AbstractRevisionSelectComponent {

	/** Name of the table that holds the revisions. */
	protected static final String TABLE_FIELD_NAME = "revisionTable";

	/**
	 * Creates a new {@link RevisionSelectComponent}.
	 */
	public RevisionSelectComponent(InstantiationContext context, Config config) throws ConfigurationException {
		super(context, config);
	}

	@Override
	protected Revision getSelectedRevision() {
		if (!hasFormContext()) {
			return null;
		}
		TableField field = getTableField();
		TableViewModel viewModel = field.getViewModel();
		int selectedRow = TableUtil.getSingleSelectedRow(field);
		if (selectedRow == -1) {
			return null;
		}
		return (Revision) viewModel.getRowObject(selectedRow);
	}

	@Override
	protected void setSelectedRevision(Revision rev) {
		TableField field = getTableField();

		TableModel tableModel = field.getTableModel();
		TableViewModel viewModel = field.getViewModel();
		int rowIndex = tableModel.getRowOfObject(rev);
		if (rowIndex < 0) {
			viewModel.adjustFiltersForRow(rev);
			rowIndex = tableModel.getRowOfObject(rev);
		}
		int viewRowIndex = viewModel.getViewModelRow(rowIndex);
		TableUtil.selectRow(field, viewRowIndex);
		updateTitle(rev);
	}

	private TableField getTableField() {
		TableField field = (TableField) getFormContext().getField(TABLE_FIELD_NAME);
		return field;
	}

	@Override
	public FormContext createFormContext() {
		FormContext form = new FormContext(this);
		TableField tableField = FormFactory.newTableField(TABLE_FIELD_NAME, ConfigKey.part(this, TABLE_FIELD_NAME));
		tableField.setSelectable(true);
		List<?> rows = (List<?>) getBuilder().getModel(getModel(), this);
		String[] columnNames = null;
		tableField.setTableModel(new CachedObjectTableModel(columnNames, createTableConfiguration(), rows));
		addSelectionListener(tableField);
		// Update title when toolbar changes.
		addToolbarListener(tableField);
		if (!rows.isEmpty()) {
			// Set after adding listeners to ensure the revision is updated.
			TableUtil.selectRow(tableField, 0);
		}
		form.addMember(tableField);
		return form;
	}

	private void addSelectionListener(final TableField tableField) {
		tableField.getSelectionModel().addSelectionListener(new SelectionListener() {

			@Override
			public void notifySelectionChanged(SelectionModel model, Set<?> formerlySelectedObjects,
					Set<?> selectedObjects) {
				Revision selectedRevision = RevisionSelectComponent.this.getSelectedRevision();
				RevisionSelectComponent.this.handleRevisionChanged(selectedRevision);
				RevisionSelectComponent.this.updateTitle(tableField, selectedRevision);
			}
		});
	}

	private void addToolbarListener(TableField tableField) {
		final TableViewModel viewModel = tableField.getViewModel();
		tableField.addListener(TOOLBAR_PROPERTY, new ToolBarChangeListener() {

			@Override
			public void notifyToolbarChange(ToolBarOwner sender, ToolBar oldValue, ToolBar newValue) {
				if (newValue != null) {
					int selectedRow = TableUtil.getSingleSelectedRow(tableField);
					Revision selectedRevision;
					if (selectedRow >= 0) {
						selectedRevision = (Revision) viewModel.getRowObject(selectedRow);
					} else {
						selectedRevision = null;
					}
					newValue.setTitle(createTitle(selectedRevision));
				}
			}

		});
	}

	private TableConfiguration createTableConfiguration() {
		TableConfigurationProvider configuredTable = lookupTableConfigurationBuilder(TABLE_FIELD_NAME);
		TableConfigurationProvider programmaticProvider = getTableConfigurationProvider();
		TableConfigurationProvider finalProvider =
			TableConfigurationFactory.combine(programmaticProvider, configuredTable);
		return TableConfigurationFactory.build(finalProvider);
	}

	/**
	 * The programmatic {@link TableConfigurationProvider} for the {@link #TABLE_FIELD_NAME table}.
	 */
	protected TableConfigurationProvider getTableConfigurationProvider() {
		return TableConfigurationFactory.emptyProvider();
	}

	/**
	 * Updates the title of the {@link #TABLE_FIELD_NAME}.
	 * 
	 * <p>
	 * The field must already contained in {@link #getFormContext()}
	 * </p>
	 * 
	 * @param selectedRevision
	 *        The revision to create title for. May be <code>null</code>.
	 * 
	 * @see #updateTitle(TableField, Revision)
	 */
	protected final void updateTitle(Revision selectedRevision) {
		TableField table = getTableField();
		updateTitle(table, selectedRevision);
	}

	/**
	 * Updates the title of the given {@link TableField}.
	 * 
	 * @param table
	 *        The table to update title.
	 * @param selectedRevision
	 *        The revision to create title for. May be <code>null</code>.
	 * 
	 * @see #updateTitle(Revision)
	 */
	protected final void updateTitle(TableField table, Revision selectedRevision) {
		table.getToolBar().setTitle(createTitle(selectedRevision));
	}

	/**
	 * Creates the title for the table.
	 */
	protected DisplayValue createTitle(Revision selectedRevision) {
		return createTitle(selectedRevision, getTitleKey(TABLE_FIELD_NAME));
	}

	private DisplayValue createTitle(Revision selectedRevision, ResKey tableTitle) {
		DisplayValue fallback = new ResourceText(tableTitle);
		if (selectedRevision == null) {
			return fallback;
		}
		ResKey key;
		if (selectedRevision.isCurrent()) {
			key = tableTitle.suffix(".currentRevision");
		} else {
			Date revisionDate = new Date(selectedRevision.getDate());
			String dateAsString = DateTimeLabelProvider.INSTANCE.getLabel(revisionDate);
			key = ResKey.message(tableTitle.suffix(".stableRevision"), dateAsString);
		}
		return new ResourceText(key, fallback);
	}

}
