/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.gui.layout.upload;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.fileupload2.core.FileItem;

import com.top_logic.basic.io.binary.AbstractBinaryData;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.dsa.util.MimeTypes;
import com.top_logic.knowledge.service.binary.FileItemBinaryData;

/**
 * The class {@link DefaultDataItem} is a simple immutable implementation of
 * {@link BinaryData}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class DefaultDataItem extends AbstractBinaryData {

	/**
	 * Creates a {@link BinaryData} based on the given {@link File}
	 * 
	 * @param file
	 *        The file which provides the informations for the returned {@link BinaryData}.
	 * 
	 * @return a {@link BinaryData} representing the given {@link File}
	 */
	public static BinaryData createDataItem(File file) {
		String name = file.getName();
		BinaryData data = BinaryDataFactory.createBinaryData(file);
		String contentType = MimeTypes.getInstance().getMimeType(name);
		return new DefaultDataItem(name, data, contentType);
	}

	/**
	 * Creates a {@link BinaryData} based on the given {@link FileItem}
	 * 
	 * @param item
	 *        the item which provides the informations for the returned
	 *        {@link BinaryData}.
	 * 
	 * @return a {@link BinaryData} representing the given {@link FileItem}
	 */
	public static BinaryData createDataItem(FileItem<?> item) {
		return new DefaultDataItem(item.getName(), new FileItemBinaryData(item), item.getContentType());
	}

	private final String contentType;

	private final BinaryData data;

	private final String name;

	/**
	 * Creates a {@link BinaryData} based on the given data, name, and contentType
	 * 
	 * @param name
	 *        must not be <code>null</code>. Returned in {@link #getName()}.
	 * @param contentType
	 *        must not be <code>null</code>. Returned in
	 *        {@link #getContentType()}.
	 */
	public DefaultDataItem(String name, BinaryData data, String contentType) {
		super();
		this.contentType = nonNullContentType(contentType);
		this.data = data;
		this.name = name;
	}

	@Override
	public String getContentType() {
		return contentType;
	}

	@Override
	public InputStream getStream() throws IOException {
		return data.getStream();
	}

	@Override
	public long getSize() {
		return data.getSize();
	}

	@Override
	public String getName() {
		return name;
	}
}
