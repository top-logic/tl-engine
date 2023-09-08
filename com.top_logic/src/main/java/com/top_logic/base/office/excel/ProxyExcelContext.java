/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.office.excel;

import java.io.IOException;
import java.util.Map;

/**
 * Proxy excel context for handling different types of excel files.
 * 
 * @author     <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class ProxyExcelContext extends ExcelContext {

	/** The real excel context to be used. */
	private final ExcelContext _innerContext;

    /** 
     * Creates a {@link ProxyExcelContext}.
     * 
     * @param    aSource    The source file for {@link ExcelContext#getInstance(Object)}.
     */
    public ProxyExcelContext(final Object aSource) {
        this(ExcelContext.getInstance(aSource));
    }

    /** 
     * Creates a {@link ProxyExcelContext}.
     * 
     * @param    aContext    The excel context to be used by this proxy.
     */
    public ProxyExcelContext(final ExcelContext aContext) {
		_innerContext = aContext;
    }

	/**
	 * Returns the wrapped {@link ExcelContext}.
	 */
	protected ExcelContext getInner() {
		return _innerContext;
	}

    @Override
    public void close() throws IOException {
		getInner().close();
    }

    @Override
    public <T> T value() {
		return getInner().value();
    }

    @Override
    public <T> ExcelContext value(T aValue) {
		getInner().value(aValue);
        return this;
    }

    @Override
    public int getLastCellNum() {
		return getInner().getLastCellNum();
    }

    @Override
    public boolean hasMoreRows() {
		return getInner().hasMoreRows();
    }

    @Override
    public String getURL(int aColumn) {
		return getInner().getURL(aColumn);
    }

    @Override
    public boolean isInvalid(int aColumn) {
		return getInner().isInvalid(aColumn);
    }

    @Override
    public int row() {
		return getInner().row();
    }

    @Override
    public ExcelContext row(int aIndex) {
		getInner().row(aIndex);
        return this;
    }

    @Override
    public int column() {
		return getInner().column();
    }

    @Override
    public ExcelContext column(int aIndex) {
		getInner().column(aIndex);
        return this;
    }

    @Override
    public ExcelContext up() {
		getInner().up();
        return this;
    }

    @Override
    public ExcelContext down() {
		getInner().down();
        return this;
    }

    @Override
    public ExcelContext left() {
		getInner().left();
        return this;
    }

    @Override
    public ExcelContext right() {
		getInner().right();
        return this;
    }
    
    @Override
    public Map<String, Integer> getColumnMap() {
		return getInner().getColumnMap();
    }

    @Override
    public void prepareColNamesAsHeaders() {
		getInner().prepareColNamesAsHeaders();
    }

    @Override
    public void prepareHeaderRows(int aMaxColumn) {
		getInner().prepareHeaderRows(aMaxColumn);
    }
}
