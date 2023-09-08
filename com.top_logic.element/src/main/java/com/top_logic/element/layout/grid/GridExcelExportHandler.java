/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.grid;

import java.util.List;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.defaults.ImplementationClassDefault;
import com.top_logic.basic.config.annotation.defaults.ItemDefault;
import com.top_logic.basic.config.annotation.defaults.ListDefault;
import com.top_logic.layout.basic.check.CheckScopeProvider;
import com.top_logic.layout.basic.check.SelfCheckProvider;
import com.top_logic.layout.table.export.StreamingExcelExportHandler;
import com.top_logic.tool.execution.AlwaysExecutable;
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.tool.export.ExcelExportHandler;

/**
 * Allows export even if there is no model as not every grid component needs a model.
 * 
 * @deprecated Use {@link StreamingExcelExportHandler}
 */
@Deprecated
public class GridExcelExportHandler extends ExcelExportHandler {

	/**
	 * Well-known command ID for the default grid export handler.
	 */
	public static final String COMMAND_ID = "exportExcelGrid";

	/**
	 * Configuration for a {@link GridExcelExportHandler}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends ExcelExportHandler.Config {

		/**
		 * In contrast to {@link ExcelExportHandler} this handler can be used when no model exists,
		 * as it is not needed to create export values. Moreover it can be used in edit mode.
		 */
		@Override
		@ListDefault(AlwaysExecutable.class)
		List<PolymorphicConfiguration<? extends ExecutabilityRule>> getExecutability();

		@Override
		@ImplementationClassDefault(SelfCheckProvider.class)
		@ItemDefault
		PolymorphicConfiguration<CheckScopeProvider> getCheckScopeProvider();

	}

	/**
	 * Creates a new instance of this class.
	 */
	public GridExcelExportHandler(InstantiationContext context, Config config) {
		super(context, config);
	}

}
