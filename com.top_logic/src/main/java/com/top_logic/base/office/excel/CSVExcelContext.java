/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.office.excel;

import java.io.IOException;
import java.util.List;

import com.top_logic.basic.Logger;
import com.top_logic.basic.io.CSVReader;

/**
 * Instances of this class provide convenient access to Excel sheet using {@link CSVReader}.
 * 
 * @author     <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class CSVExcelContext extends ExcelContext {

	private CSVReader reader;

    private List<String> line;

    private int readerRow;

	/**
     * Create a new {@link CSVExcelContext} providing convenient access to the
     * given CSV file.
     * 
     * @param aReader
     *            the {@link CSVReader} to create the accessor for.
     */
	protected CSVExcelContext(final CSVReader aReader) {
		this.reader    = aReader;
        this.readerRow = -1;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T value() {
        int theColumn = this.column();

        return (this.line.size() > theColumn) ? (T) this.line.get(theColumn) : null;
	}

	@Override
	public <T> CSVExcelContext value(final T value) {
		throw new UnsupportedOperationException("Cannot set value in CSV file (streaming reader only).");
	}

	@Override
	public ExcelContext up() {
		throw new UnsupportedOperationException("Cannot step up in CSV file (streaming reader only).");
	}

	@Override
    public ExcelContext row(int anIndex) {
        if (this.readerRow > anIndex) {
            throw new UnsupportedOperationException("Row " + anIndex + " has already been read.");
        }

        try {
            while (this.readerRow < anIndex) { 
                this.readerRow++;
                this.line = this.reader.readLine();
            }
        }
        catch (IOException ex) {
            this.line = null;
            Logger.warn("Cannot read more data from " + this.reader, ex, this);
        }

        return super.row(anIndex);
    }

	@Override
	public void prepareHeaderRows(int aMaxColumn) {
		if (this.line == null) {
			this.row(0);
		}

		super.prepareHeaderRows(aMaxColumn);
	}

	@Override
	protected void prepareColNamesAsHeaders(int aMaxColumn) {
		if (this.line == null) {
			this.row(0);
		}

		super.prepareColNamesAsHeaders(aMaxColumn);
	}

	@Override
	public int getLastCellNum() {
		return (this.line != null) ? this.line.size() : -1;
    }

    @Override
    public boolean hasMoreRows() {
    	return this.line != null;
    }

    @Override
    public void close() throws IOException {
    	this.reader.close();
    }
}