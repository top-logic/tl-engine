/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.importer.excel.transformer;

import com.top_logic.base.office.excel.ExcelContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.importer.excel.AbstractExcelFileImportParser;
import com.top_logic.importer.logger.ImportLogger;

/**
 * Make a simple transformation of an excel cell value into an expected object type.
 * 
 * @author     <a href="mailto:fma@top-logic.com">fma</a>
 */
public interface Transformer<T> {

    public interface Config extends PolymorphicConfiguration<Transformer<?>> {

        @BooleanDefault(false)
		boolean isMandatory();
	}

	/** 
	 * Transform the value named in the column from the given context.
	 * 
	 * @param    aContext         The context to get the value from.
	 * @param    columnName       Name of the requested column.
	 * @param    uploadHandler    Handler performing the upload.
	 * @param    aLogger          Logging option.
	 * @return   The requested object.
	 */
	public T transform(ExcelContext aContext, String columnName, AbstractExcelFileImportParser<?> uploadHandler,ImportLogger aLogger );
}
