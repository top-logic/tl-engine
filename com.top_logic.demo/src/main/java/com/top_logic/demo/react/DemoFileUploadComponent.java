/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.react;

import java.io.IOException;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.form.control.DataItemControl;
import com.top_logic.layout.form.model.DataField;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.react.control.download.ReactDownloadControl;
import com.top_logic.layout.react.control.upload.ReactFileUploadControl;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * Demo {@link LayoutComponent} that showcases the file upload React control.
 *
 * <p>
 * Provides a {@link ReactFileUploadControl} that allows the user to select and upload a file. The
 * uploaded file is stored in a {@link DataField} which is rendered below the upload control,
 * allowing the user to download the uploaded file.
 * </p>
 */
public class DemoFileUploadComponent extends LayoutComponent {

	/**
	 * Configuration for {@link DemoFileUploadComponent}.
	 */
	public interface Config extends LayoutComponent.Config {
		// No additional configuration needed.
	}

	private ReactDownloadControl _downloadControl;

	private ReactFileUploadControl _uploadControl;

	private DataField _fileField;

	private DataItemControl _fileFieldControl;

	/**
	 * Creates a new {@link DemoFileUploadComponent}.
	 */
	public DemoFileUploadComponent(InstantiationContext context, Config config) throws ConfigurationException {
		super(context, config);
	}

	@Override
	public void writeBody(ServletContext servletContext, HttpServletRequest request, HttpServletResponse response,
			TagWriter out) throws IOException, ServletException {
		DisplayContext displayContext = DefaultDisplayContext.getDisplayContext(request);

		if (_uploadControl == null) {
			FormContext formContext = new FormContext(this);
			_fileField = FormFactory.newDataField("file");
			formContext.addMember(_fileField);

			_fileFieldControl = new DataItemControl(_fileField);

			_downloadControl = new ReactDownloadControl(null, null);

			_uploadControl = new ReactFileUploadControl(data -> {
				_fileField.setValue(data);
				_downloadControl.setData(data, data.getName());
			});
		}

		out.beginTag(HTMLConstants.H2);
		out.writeText("File Upload Demo");
		out.endTag(HTMLConstants.H2);

		out.beginTag(HTMLConstants.PARAGRAPH);
		out.writeText("Click 'Choose File' or drag and drop a file onto the upload area. "
			+ "After uploading, use the download button to retrieve the file.");
		out.endTag(HTMLConstants.PARAGRAPH);

		out.beginBeginTag(HTMLConstants.DIV);
		out.writeAttribute(HTMLConstants.STYLE_ATTR, "margin-bottom: 1em;");
		out.endBeginTag();
		_downloadControl.write(displayContext, out);
		_uploadControl.write(displayContext, out);
		out.endTag(HTMLConstants.DIV);

		out.beginTag(HTMLConstants.H3);
		out.writeText("Uploaded File");
		out.endTag(HTMLConstants.H3);

		_fileFieldControl.write(displayContext, out);
	}

}
