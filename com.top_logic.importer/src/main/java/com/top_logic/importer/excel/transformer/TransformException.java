/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.importer.excel.transformer;

import com.top_logic.basic.tools.NameBuilder;
import com.top_logic.basic.util.ResKey;
import com.top_logic.util.Resources;

/**
 * Exception thrown if a {@link Transformer} has a problem.
 * 
 * @author     <a href="mailto:fma@top-logic.com">fma</a>
 */
@SuppressWarnings("serial")
public class TransformException extends RuntimeException{

	private final Object[] params;
	private final ResKey key;
	private int    row;
	private String col;

	public TransformException(ResKey key, int row, String col, Object... params) {
		this.key = key;
		this.params = params;
		this.row = row;
		this.col = col;
	}
	
	@Override
	public String toString() {
	    return new NameBuilder(this)
	            .add("row", this.row)
	            .add("col", this.col)
	            .add("params", this.params)
	            .build();
	}

	@Override
	public String getMessage() {
		return Resources.getLogInstance().getMessage(this.getKey(), this.getAllParams());
	}

	@Override
	public String getLocalizedMessage() {
	    return Resources.getInstance().getMessage(this.getKey(), this.getAllParams());
	}

	public Object[] getParams() {
		return params;
	}

	public Object[] getAllParams() {
		if (params == null) {
			return new Object[] {this.row, this.col};
		}
		
		Object[] theParams = new Object[this.params.length + 2];
		theParams[0] = this.row;
		theParams[1] = this.col;
		for (int i=0; i<this.params.length; i++) {
			theParams[i+2] = this.params[i];
		}
		
		return theParams;
	}

	public ResKey getKey() {
		return key;
	}
	
	public int getRow() {
		return row;
	}
	
	public String col() {
		return col;
	}

}
