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
import com.top_logic.layout.react.control.audio.ReactAudioPlayerControl;
import com.top_logic.layout.react.control.upload.ReactFileUploadControl;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * Demo {@link LayoutComponent} that showcases the file upload and audio player React controls.
 *
 * <p>
 * Provides a {@link ReactFileUploadControl} that allows the user to select and upload an audio
 * file. The uploaded file is fed into a {@link ReactAudioPlayerControl} rendered below the upload
 * control, allowing the user to play back the uploaded audio directly in the browser.
 * </p>
 */
public class DemoFileUploadComponent extends LayoutComponent {

	/**
	 * Configuration for {@link DemoFileUploadComponent}.
	 */
	public interface Config extends LayoutComponent.Config {
		// No additional configuration needed.
	}

	private ReactFileUploadControl _uploadControl;

	private ReactAudioPlayerControl _playerControl;

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
			_playerControl = new ReactAudioPlayerControl(null);

			_uploadControl = new ReactFileUploadControl(data -> {
				_playerControl.setAudioData(data);
			});
			_uploadControl.setAccept("audio/*");
		}

		out.beginTag(HTMLConstants.H2);
		out.writeText("File Upload & Audio Player Demo");
		out.endTag(HTMLConstants.H2);

		out.beginTag(HTMLConstants.PARAGRAPH);
		out.writeText("Upload an audio file using the upload control below. "
			+ "The audio player will then allow you to play back the uploaded file.");
		out.endTag(HTMLConstants.PARAGRAPH);

		out.beginBeginTag(HTMLConstants.DIV);
		out.writeAttribute(HTMLConstants.STYLE_ATTR, "margin-bottom: 1em;");
		out.endBeginTag();
		_uploadControl.write(displayContext, out);
		out.endTag(HTMLConstants.DIV);

		out.beginTag(HTMLConstants.H3);
		out.writeText("Audio Playback");
		out.endTag(HTMLConstants.H3);

		_playerControl.write(displayContext, out);
	}

}
