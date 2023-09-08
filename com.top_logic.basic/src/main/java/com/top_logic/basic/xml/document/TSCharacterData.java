/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.xml.document;

import org.w3c.dom.CharacterData;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;

/**
 * Immutable {@link CharacterData} implementation.
 * 
 * @author    <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class TSCharacterData extends NonElementNode implements CharacterData {

	private String data;

	protected TSCharacterData(CharacterData sourceNode, Node parent) {
		super(sourceNode, parent);
		this.data = sourceNode.getData();
	}

	@Override
	public String getData() throws DOMException {
		return this.data;
	}
	
	@Override
	public int getLength() {
		return this.data.length();
	}
	
	@Override
	public String substringData(int offset, int count) throws DOMException {
        int length = data.length();
        int tailIndex = Math.min(offset + count, length);

        return data.substring(offset, tailIndex);
	}
	
	@Override
	public void setData(String aData) throws DOMException {
		throw noModificationAllowedErr();
	}

	@Override
	public void appendData(String aArg) throws DOMException {
		throw noModificationAllowedErr();
	}

	@Override
	public void insertData(int aOffset, String aArg) throws DOMException {
		throw noModificationAllowedErr();
	}

	@Override
	public void deleteData(int aOffset, int aCount) throws DOMException {
		throw noModificationAllowedErr();
	}

	@Override
	public void replaceData(int aOffset, int aCount, String aArg) throws DOMException {
		throw noModificationAllowedErr();
	}



}

