/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.export;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.top_logic.base.office.excel.ExcelValue;
import com.top_logic.basic.CollectionUtil;

/**
 * The container combines {@link ExcelCellFormatter}s (e.g. {@link ColorCellFormatter}
 * or {@link BorderCellFormatter}).
 * 
 * @author    <a href=mailto:tdi@top-logic.com>tdi</a>
 */
public class ContainerCellFormatter implements ExcelCellFormatter {

	private List cellFormatter;
	
	public ContainerCellFormatter(ExcelCellFormatter cellFormatter1, ExcelCellFormatter cellFormatter2) {
		this(Arrays.asList(new ExcelCellFormatter[] {cellFormatter1, cellFormatter2}));
	}
	
	public ContainerCellFormatter(ExcelCellFormatter cellFormatter1, ExcelCellFormatter cellFormatter2, ExcelCellFormatter cellFormatter3) {
		this(Arrays.asList(new ExcelCellFormatter[] {cellFormatter1, cellFormatter2, cellFormatter3}));
	}
	
	public ContainerCellFormatter(List cellFormatter) {
		this.cellFormatter = cellFormatter != null ? cellFormatter : Collections.EMPTY_LIST;
	}
	
	@Override
	public void formatCell(ExcelValue excelValue) {
		for (Iterator iterator = getCellFormatter().iterator(); iterator.hasNext();) {
			ExcelCellFormatter excelCellFormatter = (ExcelCellFormatter) iterator.next();
			excelCellFormatter.formatCell(excelValue);
		}
	}

	/**
	 * Returns the cellFormatter.
	 */
	public List getCellFormatter() {
		return this.cellFormatter;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj == null) return false;
		
		if (!(obj instanceof ContainerCellFormatter)) return false;
		
		ContainerCellFormatter other = (ContainerCellFormatter) obj;
		
		return CollectionUtil.containsSame(getCellFormatter(), other.getCellFormatter());
	}
	
	@Override
	public int hashCode() {
		int hash = 0;
		for (Iterator iterator = getCellFormatter().iterator(); iterator.hasNext();) {
			ExcelCellFormatter excelCellFormatter = (ExcelCellFormatter) iterator.next();
			hash += excelCellFormatter.hashCode();
		}
		
		return hash;
	}
	
}
