/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.values;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.layout.Control;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.control.TextInputControl;
import com.top_logic.layout.form.template.ControlProvider;

/**
 * {@link ControlProvider} generating a multi-line {@link TextInputControl}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class MultiLineText extends AbstractConfiguredInstance<MultiLineText.Config> implements ControlProvider {

	/**
	 * Configuration options for {@link MultiLineText}.
	 */
	public interface Config extends PolymorphicConfiguration<MultiLineText> {

		/** @see #getRows() */
		String ROWS = "rows";

		/**
		 * @see #getColumns()
		 */
		String COLUMNS = "columns";

		/**
		 * The number of rows to display in the text area.
		 */
		@Name(ROWS)
		@IntDefault(DEFAULT_ROWS)
		int getRows();
		/**
		 * Setter for {@link #getRows()}.
		 */
		void setRows(int rows);

		/**
		 * The number of columns to display in the text area.
		 */
		@Name(COLUMNS)
		@IntDefault(TextInputControl.NO_COLUMNS)
		int getColumns();

		/**
		 * Setter for {@link #getColumns()}.
		 */
		void setColumns(int columns);

	}

	/** Number of rows to display in the text area. */
	public static final int DEFAULT_ROWS = 5;

	/** Singleton {@link MultiLineText} instance. */
	public static final MultiLineText INSTANCE = newInstance(DEFAULT_ROWS, TextInputControl.NO_COLUMNS);

	/**
	 * Creates a {@link MultiLineText} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public MultiLineText(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public Control createControl(Object model, String style) {
		FormField field = (FormField) model;
		TextInputControl control = createControl(field);
		control.setMultiLine(true);
		control.setRows(getConfig().getRows());
		control.setColumns(getConfig().getColumns());
		return control;
	}

	/**
	 * Creation of the result {@link TextInputControl}.
	 * 
	 * <p>
	 * The control is later set to {@link TextInputControl#setMultiLine(boolean) multi line} and
	 * gets the configured number of rows.
	 * </p>
	 */
	protected TextInputControl createControl(FormField field) {
		return new TextInputControl(field);
	}

	/**
	 * Creates a {@link MultiLineText} with the given number of rows.
	 */
	public static MultiLineText newInstance(int rows, int columns) {
		Config config = TypedConfiguration.newConfigItem(MultiLineText.Config.class);
		config.setRows(rows);
		config.setColumns(columns);
		return TypedConfigUtil.createInstance(config);
	}
}