/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.element;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.form.CompositionTableControl;
import com.top_logic.layout.view.form.FormControl;
import com.top_logic.layout.view.form.FormModel;

/**
 * Declarative {@link UIElement} that creates a {@link CompositionTableControl} for an inline
 * composition table within a form.
 *
 * <p>
 * Must be nested inside a {@link FormElement}. Parses a {@code <composition-table>} configuration
 * element with an {@code attribute} reference and an optional {@code <columns>} child containing
 * {@code <column>} elements.
 * </p>
 *
 * <p>
 * Example configuration:
 * </p>
 *
 * <pre>
 * &lt;composition-table attribute="items"&gt;
 *     &lt;columns&gt;
 *         &lt;column attribute="name"/&gt;
 *         &lt;column attribute="quantity"/&gt;
 *         &lt;column attribute="unitPrice" readonly="true"/&gt;
 *     &lt;/columns&gt;
 * &lt;/composition-table&gt;
 * </pre>
 */
public class CompositionTableElement implements UIElement {

	/**
	 * Configuration for {@link CompositionTableElement}.
	 */
	@TagName("composition-table")
	public interface Config extends UIElement.Config {

		@Override
		@ClassDefault(CompositionTableElement.class)
		Class<? extends UIElement> getImplementationClass();

		/** Configuration name for {@link #getAttribute()}. */
		String ATTRIBUTE = "attribute";

		/** Configuration name for {@link #getColumns()}. */
		String COLUMNS = "columns";

		/**
		 * The name of the composition reference attribute on the parent object.
		 */
		@Name(ATTRIBUTE)
		@Mandatory
		String getAttribute();

		/**
		 * The column definitions for this table.
		 */
		@Name(COLUMNS)
		ColumnsConfig getColumns();
	}

	/**
	 * Container for the list of column configurations.
	 */
	public interface ColumnsConfig extends ConfigurationItem {

		/**
		 * The column definitions.
		 */
		@DefaultContainer
		List<ColumnConfig> getColumns();
	}

	/**
	 * Configuration for a single column in the composition table.
	 */
	@TagName("column")
	public interface ColumnConfig extends ConfigurationItem {

		/** Configuration name for {@link #getAttribute()}. */
		String ATTRIBUTE = "attribute";

		/** Configuration name for {@link #getReadonly()}. */
		String READONLY = "readonly";

		/**
		 * The name of the model attribute to display in this column.
		 */
		@Name(ATTRIBUTE)
		@Mandatory
		String getAttribute();

		/**
		 * Whether this column is always read-only regardless of form edit mode.
		 */
		@Name(READONLY)
		boolean getReadonly();
	}

	private final Config _config;

	/**
	 * Creates a new {@link CompositionTableElement} from configuration.
	 */
	@CalledByReflection
	public CompositionTableElement(InstantiationContext context, Config config) {
		_config = config;
	}

	@Override
	public IReactControl createControl(ViewContext context) {
		FormModel formModel = context.getFormModel();
		if (formModel == null) {
			throw new IllegalStateException(
				"CompositionTableElement must be nested inside a FormElement. No FormModel found in ViewContext.");
		}

		// FormElement always sets a FormControl as the FormModel.
		FormControl formControl = (FormControl) formModel;

		// Convert config to CompositionTableControl.ColumnConfig list.
		List<CompositionTableControl.ColumnConfig> columnConfigs = new ArrayList<>();
		if (_config.getColumns() != null) {
			for (ColumnConfig col : _config.getColumns().getColumns()) {
				columnConfigs.add(new CompositionTableControl.ColumnConfig(
					col.getAttribute(), col.getReadonly()));
			}
		}

		CompositionTableControl control = new CompositionTableControl(
			context, formControl, _config.getAttribute(), columnConfigs);
		control.initTable();
		return control;
	}
}
