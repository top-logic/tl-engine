/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.admin;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.Accessor;
import com.top_logic.layout.form.model.AbstractFieldModel;
import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.layout.form.model.FieldModelListener;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.common.ReactTextControl;
import com.top_logic.layout.react.control.form.ReactCheckboxControl;
import com.top_logic.layout.react.control.table.ReactCellControlProvider;
import com.top_logic.layout.react.control.table.ReactTableControl;
import com.top_logic.layout.table.model.ColumnConfiguration;
import com.top_logic.layout.table.model.ObjectTableModel;
import com.top_logic.layout.table.model.TableConfiguration;
import com.top_logic.layout.table.model.TableConfigurationFactory;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.security.SecurityScope;
import com.top_logic.layout.view.security.SecurityScopeService;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.wrap.BoundedRole;
import com.top_logic.util.Resources;

/**
 * Permission matrix for the access-control administration: one row per
 * {@link SecurityScope scope} and command group, one column per {@link BoundedRole role}, with a
 * checkbox in each cell granting/revoking that role's command group on that scope.
 *
 * <p>
 * App-specific admin widget (referenced by {@code class=}, not a reusable {@code @TagName} element).
 * Rows and role columns come from the {@link SecurityScopeService} catalog and the persisted role
 * assignments rather than from a model query, and the cells edit those assignments directly, so this
 * is a distinct element rather than a configuration of the generic data table.
 * </p>
 */
public class SecurityMatrixElement implements UIElement {

	/** Column id for the leading "scope" label column. */
	private static final String SCOPE_COLUMN = "scope";

	/** Column id for the leading "command group" label column. */
	private static final String GROUP_COLUMN = "group";

	/** Accessor used for all columns; cell content is produced by the cell control provider instead. */
	private static final Accessor<Object> NULL_ACCESSOR = new Accessor<>() {
		@Override
		public Object getValue(Object object, String property) {
			return null;
		}

		@Override
		public void setValue(Object object, String property, Object value) {
			// Read-only via accessor; edits happen through the checkbox cell controls.
		}
	};

	/**
	 * Configuration for {@link SecurityMatrixElement}.
	 */
	public interface Config extends UIElement.Config {

		@Override
		@ClassDefault(SecurityMatrixElement.class)
		Class<? extends UIElement> getImplementationClass();
	}

	/**
	 * Creates a new {@link SecurityMatrixElement} from configuration.
	 */
	@CalledByReflection
	public SecurityMatrixElement(InstantiationContext context, Config config) {
		// No configuration needed.
	}

	@Override
	public IReactControl createControl(ViewContext context) {
		List<Row> rows = new ArrayList<>();
		for (SecurityScope scope : SecurityScopeService.getInstance().getAllScopes()) {
			for (BoundCommandGroup group : scope.getCommandGroups()) {
				rows.add(new Row(scope, group));
			}
		}

		List<BoundedRole> roles = BoundedRole.getAll();
		Map<String, BoundedRole> roleByColumn = new LinkedHashMap<>();
		for (BoundedRole role : roles) {
			roleByColumn.put("role_" + role.getName(), role);
		}

		TableConfiguration tableConfig = TableConfigurationFactory.table();
		declareColumn(tableConfig, SCOPE_COLUMN, Resources.getInstance().getString(I18NConstants.MATRIX_SCOPE_COLUMN));
		declareColumn(tableConfig, GROUP_COLUMN, Resources.getInstance().getString(I18NConstants.MATRIX_GROUP_COLUMN));
		for (Map.Entry<String, BoundedRole> entry : roleByColumn.entrySet()) {
			declareColumn(tableConfig, entry.getKey(), entry.getValue().getName());
		}

		List<String> columnNames = new ArrayList<>();
		columnNames.add(SCOPE_COLUMN);
		columnNames.add(GROUP_COLUMN);
		columnNames.addAll(roleByColumn.keySet());

		ObjectTableModel tableModel = new ObjectTableModel(columnNames, tableConfig, new ArrayList<>(rows));

		ReactCellControlProvider cellProvider = (ctx, rowObject, columnName, cellValue) -> {
			Row row = (Row) rowObject;
			if (SCOPE_COLUMN.equals(columnName)) {
				return new ReactTextControl(ctx, row.scopeLabel());
			}
			if (GROUP_COLUMN.equals(columnName)) {
				return new ReactTextControl(ctx, row._group.getID());
			}
			BoundedRole role = roleByColumn.get(columnName);
			return roleCheckbox(ctx, row, role);
		};

		return new ReactTableControl(context, tableModel, cellProvider);
	}

	private static ReactControl roleCheckbox(com.top_logic.layout.react.ReactContext ctx, Row row, BoundedRole role) {
		AbstractFieldModel field = new AbstractFieldModel(Boolean.valueOf(row._scope.isGranted(row._group, role)));
		field.addListener(new FieldModelListener() {
			@Override
			public void onValueChanged(FieldModel source, Object oldValue, Object newValue) {
				row._scope.setGranted(row._group, role, Boolean.TRUE.equals(newValue));
			}

			@Override
			public void onEditabilityChanged(FieldModel source, boolean editable) {
				// Editability is fixed.
			}

			@Override
			public void onValidationChanged(FieldModel source) {
				// No validation.
			}
		});
		return new ReactCheckboxControl(ctx, field);
	}

	private static void declareColumn(TableConfiguration tableConfig, String name, String label) {
		ColumnConfiguration column = tableConfig.declareColumn(name);
		column.setColumnLabel(label);
		column.setAccessor(NULL_ACCESSOR);
	}

	/**
	 * A matrix row: one command group of one security scope.
	 */
	private static final class Row {

		final SecurityScope _scope;

		final BoundCommandGroup _group;

		Row(SecurityScope scope, BoundCommandGroup group) {
			_scope = scope;
			_group = group;
		}

		String scopeLabel() {
			ResKey label = _scope.getLabel();
			if (label != null) {
				return Resources.getInstance().getString(label);
			}
			return String.valueOf(_scope.getSecurityId());
		}
	}
}
