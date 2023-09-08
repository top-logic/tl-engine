/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.importer.excel.transformer;

import java.util.List;

import com.top_logic.base.office.excel.ExcelContext;
import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.contact.orgunit.OrgUnitBase;
import com.top_logic.importer.excel.AbstractExcelFileImportParser;
import com.top_logic.importer.logger.ImportLogger;

/**
 * Return the {@link OrgUnitBase}s from an excel cell.
 * 
 * @author     <a href="mailto:fma@top-logic.com">fma</a>
 */
public class OrgUnitsTransformer extends AbstractOrgUnitTransformer<List<OrgUnitBase>> {

    public OrgUnitsTransformer(InstantiationContext aContext, Config aConfig) {
        super(aContext, aConfig);
    }

	@Override
	public List<OrgUnitBase> transform(ExcelContext aContext, String columnName, AbstractExcelFileImportParser<?> handler, ImportLogger logger) {
		List<OrgUnitBase> theUnits = this.getOrgUnits(aContext, columnName, logger);

		if (this.mandatory && CollectionUtil.isEmpty(theUnits)) {
	    	throw new TransformException(I18NConstants.VALUE_EMPTY, aContext.row() + 1, columnName, "");
		}
		else {
		    return theUnits;
		}
	}
}
