/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config;

import com.top_logic.basic.StringServices;

/**
 * Default {@link Location} implementation.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class LocationImpl implements Location {

	/**
	 * Placeholder for missing {@link Location} information.
	 */
	public static final Location NONE = new LocationImpl(null, -1, -1);

	/**
	 * Creates a {@link Location} with the given information.
	 */
	public static Location location(String file, int line, int column) {
		if (file == null && line == NO_LINE && column == NO_COLUMN) {
			return NONE;
		} else {
			return new LocationImpl(file, line, column);
		}
	}

	private final String _file;
	private final int _line;
	private final int _column;

	private LocationImpl(String file, int line, int column) {
		this._file = file;
		this._line = line;
		this._column = column;
	}

	@Override
	public String getResource() {
		return _file;
	}

	@Override
	public int getLine() {
		return _line;
	}

	@Override
	public int getColumn() {
		return _column;
	}
	
	@Override
	public boolean equals(Object obj) {
    	if (obj == this) {
    		return true;
    	}
		if (obj instanceof Location) {
			return equalsLocation((Location) obj);
		}
		return false;
	}
	
	private boolean equalsLocation(Location other) {
		return StringServices.equals(_file, other.getResource()) && _line == other.getLine()
			&& _column == other.getColumn();
	}

	@Override
	public int hashCode() {
		int result = (_file != null) ? _file.hashCode() : 0;
		result += 78721 * _line;
		result += 71887 * _column;
		return result;
	}
	
	@Override
	public String toString() {
		return LocationImpl.toString(this);
	}

	/**
	 * Creates a canonical {@link Object#toString()} representation for the given {@link Location}.
	 */
	public static String toString(Location location) {
		String resource = location.getResource();
		int line = location.getLine();
		int column = location.getColumn();

		boolean hasFile = resource != null;
		boolean hasLine = line != LocationImpl.NO_LINE;
		boolean hasColumn = column != LocationImpl.NO_COLUMN;
		
		if (!hasFile && !hasLine && !hasColumn) {
			return "unknown location";
		} else {
			StringBuilder buffer = new StringBuilder();
			if (hasFile) {
				buffer.append(resource);
				if (hasLine || hasColumn) {
					buffer.append(" ");
				}
			}
			if (hasLine) {
				buffer.append("line ");
				buffer.append(line);
				if (hasColumn) {
					buffer.append(" ");
				}
			}
			if (hasColumn) {
				buffer.append("column "); 
				buffer.append(column);
			}
			return buffer.toString();
		}
	}

}
