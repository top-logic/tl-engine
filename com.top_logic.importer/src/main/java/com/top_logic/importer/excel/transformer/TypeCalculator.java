/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.importer.excel.transformer;

import com.top_logic.base.office.excel.ExcelContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.importer.excel.ExcelListImportParser;

/**
 * @author     <a href="mailto:fma@top-logic.com">fma</a>
 */
public interface TypeCalculator {

    public interface Config extends PolymorphicConfiguration<Transformer<?>> {
	}

	public String getType(ExcelContext aContext, ExcelListImportParser<?> uploadHandler);
}
