/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.importer.excel;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.top_logic.base.office.POIUtil;
import com.top_logic.base.office.excel.ProxyExcelContext;
import com.top_logic.basic.StringServices;

/**
 * Handle headers by column name (e.g. "A", "B", ...) or special names. 
 * 
 * @author <a href="mailto:tgi@top-logic.com">tgi</a>
 */
public class MixedModeExcelContext extends ProxyExcelContext {

    private static String	EXCEL_ADDR_PREFIX	= "#[";
	private static String	HEADER_PREFIX			= "%[";
	private static String	ADDR_SUFFIX				= "]";

	Pattern					_colAdressPattern	= Pattern.compile("#\\[([A-Z]*)\\]");
	Pattern					_colHeaderPattern	= Pattern.compile("%\\[(.*)\\]");
	Map<String, Integer>	_columnsByHeader;
	Map<String, String>		_typedExcelAdresses;
	Map<String, String>		_typedHeaders;
    Map<String, Integer>    _columnMap;

	public MixedModeExcelContext(Object aSource) {
		super(aSource);
		prepareColNamesAsHeaders();

		_columnsByHeader = new HashMap<>();
		_typedExcelAdresses = new HashMap<>();
		_typedHeaders = new HashMap<>();
        _columnMap = new HashMap<>();

		final Map<String, Integer> cmap = getInner().getColumnMap();
		for (String untyped : cmap.keySet()) {
			_typedExcelAdresses.put(untyped, buildTypedExcelAdress(untyped));
            this._columnMap.put(untyped, POIUtil.getColumn(untyped));
		}
	}

	@Override
	public MixedModeExcelContext column(int aIndex) {
		super.column(aIndex);
		return this;
	}
	@Override
	public MixedModeExcelContext column(String aKey) {
		return column(getColumnIndex(aKey));
	}
	@Override
	public Boolean getBoolean(String aColumn) {
		return getBoolean(getColumnIndex(aColumn));
	}

	public int getColumnIndex(String column) {
		Matcher matcher = _colAdressPattern.matcher(column);
		String val = null;
		if (matcher.matches()) {
			val = matcher.group(1);
			if (!StringServices.isEmpty(val)) {
				Map<String, Integer> cm = getColumnMap();
				if (cm != null) {
					final Integer index = cm.get(val);
					return index == null ? -1 : index;
				}
			}
		}
		matcher = _colHeaderPattern.matcher(column);
		val = null;
		if (matcher.matches()) {
			val = matcher.group(1);
		}
		if (StringServices.isEmpty(val)) {// if it is not in shape %[...] assume it is a header.
			val = column;
		}
		Integer index = _columnsByHeader.get(val);
		if (index != null) {
			return index.intValue();
		}
		return -1;

	}
	@Override
	public Map<String, Integer> getColumnMap() {
        return this._columnMap;
	}
	@Override
	public String getColumnNr(int aCol) {
		int col;
		for (Entry<String, Integer> theEntry : _columnsByHeader.entrySet()) {
			col = theEntry.getValue();
			if (aCol == col) {
				final String untypedAddr = theEntry.getKey();
				if (!StringServices.isEmpty(untypedAddr)) {
					return getTypedHeaderAdress(untypedAddr);
				}
			}
		}

		final String untypedExcelAddr = super.getColumnNr(aCol);
		if (!StringServices.isEmpty(untypedExcelAddr)) {
			return getTypedExcelAdress(untypedExcelAddr);
		}
		return null;
	}
	String getTypedHeaderAdress(String untypedHeader) {
		return _typedHeaders.get(untypedHeader);
	}

	String buildTypedExcelAdress(String untypedExcelAdress) {
		return new StringBuilder(EXCEL_ADDR_PREFIX).append(untypedExcelAdress).append(ADDR_SUFFIX).toString();
	}
	String buildTypedHeader(String untypedHeader) {
		return new StringBuilder(HEADER_PREFIX).append(untypedHeader).append(ADDR_SUFFIX).toString();
	}
	String getTypedExcelAdress(String untypedExcelAdress) {
		return _typedExcelAdresses.get(untypedExcelAdress);
	}
	@Override
	public Double getDouble(String aColumn) {
		return getDouble(getColumnIndex(aColumn));
	}
	@Override
	public Integer getInt(String aColumn) {
		return getInt(getColumnIndex(aColumn));
	}
	@Override
	public Long getLong(String aColumn) {
		return getLong(getColumnIndex(aColumn));
	}
	@Override
	public String getString(String aColumn) {
		return getString(getColumnIndex(aColumn));
	}
	@Override
	public String getURL(String aColumn) {
		return getURL(getColumnIndex(aColumn));
	}
	@Override
	public boolean hasColumn(String aColumn) {
		return getColumnIndex(aColumn) > -1;
	}
	@Override
	public boolean isInvalid(String aColumn) {
		return isInvalid(getColumnIndex(aColumn));
	}
	@Override
	public void prepareHeaderRows() {// don't use super here to be independet of super implementation
		this.prepareHeaderRows(Short.MAX_VALUE);
	}
	@Override
	public void prepareHeaderRows(int aMaxColumn) {
		int theMax = Math.min(aMaxColumn, this.getLastCellNum());

		for (int thePos = 0; thePos < theMax; thePos++) {
			String theString = this.getString(thePos);

			if (!StringServices.isEmpty(theString)) {
				final String untypedHeader = theString.trim().replace('\n', ' ');
				_columnsByHeader.put(untypedHeader, thePos);
				_columnMap.put(untypedHeader, thePos);
				_typedHeaders.put(untypedHeader, buildTypedHeader(untypedHeader));
			}
		}
	}

}
