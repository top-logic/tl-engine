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
import com.top_logic.layout.react.control.audio.ReactAudioRecorderControl;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * Demo {@link LayoutComponent} that showcases the audio recorder React control.
 *
 * <p>
 * Provides a {@link ReactAudioRecorderControl} that records audio from the user's microphone. The
 * recorded audio is stored in a {@link DataField} which is rendered below the recorder, allowing
 * the user to download the recorded audio file.
 * </p>
 */
public class DemoAudioRecorderComponent extends LayoutComponent {

	/**
	 * Configuration for {@link DemoAudioRecorderComponent}.
	 */
	public interface Config extends LayoutComponent.Config {
		// No additional configuration needed.
	}

	private ReactAudioRecorderControl _recorderControl;

	private DataField _audioField;

	private DataItemControl _audioFieldControl;

	/**
	 * Creates a new {@link DemoAudioRecorderComponent}.
	 */
	public DemoAudioRecorderComponent(InstantiationContext context, Config config) throws ConfigurationException {
		super(context, config);
	}

	@Override
	public void writeBody(ServletContext servletContext, HttpServletRequest request, HttpServletResponse response,
			TagWriter out) throws IOException, ServletException {
		DisplayContext displayContext = DefaultDisplayContext.getDisplayContext(request);

		if (_recorderControl == null) {
			FormContext formContext = new FormContext(this);
			_audioField = FormFactory.newDataField("audio");
			formContext.addMember(_audioField);

			_audioFieldControl = new DataItemControl(_audioField);

			_recorderControl = new ReactAudioRecorderControl(data -> {
				_audioField.setValue(data);
			});
		}

		out.beginTag(HTMLConstants.H2);
		out.writeText("Audio Recorder Demo");
		out.endTag(HTMLConstants.H2);

		out.beginTag(HTMLConstants.PARAGRAPH);
		out.writeText("Click 'Record' to start recording audio from your microphone. "
			+ "Click 'Stop' to finish and upload the recording. "
			+ "The recorded audio will appear in the file field below.");
		out.endTag(HTMLConstants.PARAGRAPH);

		out.beginBeginTag(HTMLConstants.DIV);
		out.writeAttribute(HTMLConstants.STYLE_ATTR, "margin-bottom: 1em;");
		out.endBeginTag();
		_recorderControl.write(displayContext, out);
		out.endTag(HTMLConstants.DIV);

		out.beginTag(HTMLConstants.H3);
		out.writeText("Recorded Audio");
		out.endTag(HTMLConstants.H3);

		_audioFieldControl.write(displayContext, out);
	}

}
