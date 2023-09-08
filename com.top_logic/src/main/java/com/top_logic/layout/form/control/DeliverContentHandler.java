/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.control;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import com.top_logic.basic.io.binary.BinaryDataSource;
import com.top_logic.layout.ContentHandler;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.URLParser;

/**
 * Deliver the content of the held {@link BinaryDataSource} as stream to the browser.
 * 
 * @since 5.7.5
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class DeliverContentHandler implements ContentHandler {

	private BinaryDataSource _data;

	/**
	 * Sets the {@link BinaryDataSource} to deliver.
	 * 
	 * @param data
	 *        May be <code>null</code>. In such case nothing is delivered.
	 */
	public void setData(BinaryDataSource data) {
		_data = data;
	}

	@Override
	public void handleContent(DisplayContext context, String id, URLParser url) throws IOException {
		if (_data == null) {
			return;
		}
		deliverContent(context, _data);
	}

	/**
	 * Deliver content of the {@link BinaryDataSource}.
	 * 
	 * @param context
	 *        Http context for this response.
	 * @param data
	 *        Content to deliver.
	 * @throws IOException
	 *         If an input or output exception occured.
	 */
	public static void deliverContent(DisplayContext context, BinaryDataSource data) throws IOException {
		HttpServletResponse response = context.asResponse();

		response.setContentType(data.getContentType());
		response.setContentLength((int) data.getSize());
		response.setHeader("Cache-Control", "max-age=180");

		data.deliverTo(response.getOutputStream());
	}
}
