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
import com.top_logic.layout.react.control.audio.ReactAudioRecorderControl;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * Demo {@link LayoutComponent} that showcases the audio recorder and audio player React controls.
 *
 * <p>
 * Provides a {@link ReactAudioRecorderControl} that records audio from the user's microphone. The
 * recorded audio is fed into a {@link ReactAudioPlayerControl} rendered below the recorder,
 * allowing the user to play back the recording directly in the browser.
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

	private ReactAudioPlayerControl _playerControl;

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
			_playerControl = new ReactAudioPlayerControl(null);

			_recorderControl = new ReactAudioRecorderControl(data -> {
				_playerControl.setAudioData(data);
			});
		}

		out.beginTag(HTMLConstants.H2);
		out.writeText("Audio Recorder Demo");
		out.endTag(HTMLConstants.H2);

		out.beginTag(HTMLConstants.PARAGRAPH);
		out.writeText("Click 'Record' to start recording audio from your microphone. "
			+ "Click 'Stop' to finish and upload the recording. "
			+ "Use the player below to play back the recorded audio.");
		out.endTag(HTMLConstants.PARAGRAPH);

		out.beginBeginTag(HTMLConstants.DIV);
		out.writeAttribute(HTMLConstants.STYLE_ATTR, "margin-bottom: 1em;");
		out.endBeginTag();
		_recorderControl.write(displayContext, out);
		out.endTag(HTMLConstants.DIV);

		out.beginTag(HTMLConstants.H3);
		out.writeText("Playback");
		out.endTag(HTMLConstants.H3);

		_playerControl.write(displayContext, out);
	}

}
