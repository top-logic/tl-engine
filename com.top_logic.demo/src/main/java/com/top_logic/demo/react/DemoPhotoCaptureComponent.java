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
import com.top_logic.basic.io.binary.SimpleBinaryDataValue;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.download.ReactDownloadControl;
import com.top_logic.layout.react.control.photo.ReactPhotoCaptureControl;
import com.top_logic.layout.react.control.photo.ReactPhotoViewerControl;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * Demo {@link LayoutComponent} that showcases the photo capture and photo viewer React controls.
 *
 * <p>
 * Provides a {@link ReactPhotoCaptureControl} that captures a photo from the user's camera. The
 * captured photo is fed into a {@link ReactPhotoViewerControl} rendered below the capture control,
 * allowing the user to view the photo directly in the browser.
 * </p>
 */
public class DemoPhotoCaptureComponent extends LayoutComponent {

	/**
	 * Configuration for {@link DemoPhotoCaptureComponent}.
	 */
	public interface Config extends LayoutComponent.Config {
		// No additional configuration needed.
	}

	private ReactPhotoCaptureControl _captureControl;

	private ReactPhotoViewerControl _viewerControl;

	private ReactDownloadControl _downloadControl;

	/**
	 * Creates a new {@link DemoPhotoCaptureComponent}.
	 */
	public DemoPhotoCaptureComponent(InstantiationContext context, Config config) throws ConfigurationException {
		super(context, config);
	}

	@Override
	public void writeBody(ServletContext servletContext, HttpServletRequest request, HttpServletResponse response,
			TagWriter out) throws IOException, ServletException {
		DisplayContext displayContext = DefaultDisplayContext.getDisplayContext(request);

		if (_captureControl == null) {
			ReactContext ctx = ReactContext.fromDisplayContext(displayContext);
			// Three controls, one model, zero callbacks.
			SimpleBinaryDataValue photoModel = new SimpleBinaryDataValue();
			_captureControl = new ReactPhotoCaptureControl(ctx, photoModel);
			_viewerControl = new ReactPhotoViewerControl(ctx, photoModel);
			_downloadControl = new ReactDownloadControl(ctx, photoModel);
			_downloadControl.setClearable(true);
		}

		out.beginTag(HTMLConstants.H2);
		out.writeText("Photo Capture Demo");
		out.endTag(HTMLConstants.H2);

		out.beginTag(HTMLConstants.PARAGRAPH);
		out.writeText("Click the camera button to open your camera. "
			+ "Click 'Capture' to take a photo and upload it. "
			+ "Use the download button to save the photo or view it below.");
		out.endTag(HTMLConstants.PARAGRAPH);

		out.beginBeginTag(HTMLConstants.DIV);
		out.writeAttribute(HTMLConstants.STYLE_ATTR, "margin-bottom: 1em;");
		out.endBeginTag();
		_downloadControl.write(displayContext, out);
		_captureControl.write(displayContext, out);
		out.endTag(HTMLConstants.DIV);

		out.beginTag(HTMLConstants.H3);
		out.writeText("Preview");
		out.endTag(HTMLConstants.H3);

		_viewerControl.write(displayContext, out);
	}

}
