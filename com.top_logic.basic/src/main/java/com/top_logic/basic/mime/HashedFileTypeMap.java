/*
 * SPDX-FileCopyrightText: 2002 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
 package com.top_logic.basic.mime;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import jakarta.activation.FileTypeMap;

import com.top_logic.basic.io.binary.BinaryData;

/** A filetypemap based on a Hashmap.
 * 
 * This class is used by {@link com.top_logic.basic.mime.MimetypesParser}
 * but may well be used on its own.
 * 
 * @author 	<a href="mailto:kha@top-logic.com"">Klaus Halfmann</a>
 */
public class HashedFileTypeMap extends FileTypeMap {

	/** A map index by extension without '.' */
	Map<String, String> _map;

	HashedFileTypeMap(Map<String, String> map) {
		this._map = map;
	}

	/** Default CTor, uses some reasonalble default size */
	public HashedFileTypeMap() {
		_map = new HashMap<>(256);
	}
	
	/** CTor with gicen intial size. */
	public HashedFileTypeMap(int size) {
		_map = new HashMap<>(size);
	}

	/**
	 * @see jakarta.activation.FileTypeMap#getContentType(File)
	 */
	@Override
	public String getContentType(File aFile) {
		return getContentType(aFile.getName());
	}

    @Override
	public String getContentType(String aName) {
        String theResult = null;

        int i  = aName.lastIndexOf('.');
        if (i >= 0) {
            String ext    = aName.substring(i+1).toLowerCase();
			theResult = _map.get(ext);
        }

        if (theResult == null) {
            return BinaryData.CONTENT_TYPE_OCTET_STREAM;
        } else {
        	return theResult;
        }
    }

	/** Add a new Mapping here.
	 * 
	 * @param ext 	the Extension to map (must be lowercase).
	 * @param type the Mimetype that will be returned.
	 */
	public void addType(String ext, String type) {
		_map.put(ext, type);
	}
}
