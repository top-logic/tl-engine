/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.admin;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.form.model.AbstractFieldModel;
import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.layout.form.model.FieldModelListener;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.react.control.table.TableViewControl;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.security.SecurityScope;
import com.top_logic.layout.view.security.SecurityScopeService;
import com.top_logic.table.CellContent;
import com.top_logic.table.Column;
import com.top_logic.table.TableId;
import com.top_logic.table.filter.TextColumnFilter;
import com.top_logic.table.impl.DefaultColumn;
import com.top_logic.table.impl.DefaultTableView;
import com.top_logic.table.impl.ListRowSource;
import com.top_logic.table.impl.PersonalConfigViewStateStore;
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
 * is a distinct element rather than a configuration of the generic data table. It renders through the
 * green-field {@link TableViewControl} (like the {@code <table>} element), so the two leading label
 * columns are sortable and text-filterable, the role checkbox cells are editable, and column
 * width / order are personalized per user.
 * </p>
 */
public class SecurityMatrixElement implements UIElement {

	/** Column id for the leading "scope" label column. */
	private static final String SCOPE_COLUMN = "scope";

	/** Column id for the leading "command group" label column. */
	private static final String GROUP_COLUMN = "group";

	/** Stable personalization key for the matrix. */
	private static final TableId TABLE_ID = new TableId("admin.security-matrix");

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

		List<Column<Row, ?>> columns = new ArrayList<>();

		// Leading label columns: sortable and text-filterable.
		columns.add(DefaultColumn.<Row, String> builder(SCOPE_COLUMN, Row::scopeLabel)
			.label(I18NConstants.MATRIX_SCOPE_COLUMN)
			.renderer(CellContent::text)
			.sort(() -> Comparator.<String> naturalOrder())
			.filter(new TextColumnFilter<>(text -> text))
			.width(220)
			.build());
		columns.add(DefaultColumn.<Row, String> builder(GROUP_COLUMN, row -> row._group.getID())
			.label(I18NConstants.MATRIX_GROUP_COLUMN)
			.renderer(CellContent::text)
			.sort(() -> Comparator.<String> naturalOrder())
			.filter(new TextColumnFilter<>(text -> text))
			.width(180)
			.build());

		// One checkbox column per role; each cell is an editable boolean field writing the grant.
		for (BoundedRole role : BoundedRole.getAll()) {
			Map<Row, FieldModel> fields = new LinkedHashMap<>();
			for (Row row : rows) {
				fields.put(row, grantField(row, role));
			}
			columns.add(DefaultColumn.<Row, FieldModel> builder("role_" + role.getName(), fields::get)
				.label(ResKey.text(role.getName()))
				.renderer(field -> field == null ? CellContent.EMPTY : new CellContent.Editable(field))
				.width(90)
				.build());
		}

		ListRowSource<Row> source = new ListRowSource<>(rows, columns);
		DefaultTableView<Row> view =
			DefaultTableView.create(columns, source, PersonalConfigViewStateStore.INSTANCE, TABLE_ID);

		return new TableViewControl<>(context, view, false);
	}

	/**
	 * The editable boolean field for one (scope/command-group, role) grant; its value listener
	 * writes the grant change straight through to the {@link SecurityScope}.
	 */
	private static FieldModel grantField(Row row, BoundedRole role) {
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
		return field;
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
