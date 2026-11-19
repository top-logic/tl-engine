/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.form;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.basic.DummyDisplayContext;
import com.top_logic.layout.react.control.ErrorSink;

/**
 * {@link ErrorSink} of a test window, recording the text of every message shown in it, prefixed by
 * its severity ({@code info: }, {@code warning: }, {@code error: }).
 */
public class RecordingErrorSink implements ErrorSink {

	private final List<String> _shown = new ArrayList<>();

	/**
	 * The messages shown so far, in the order they were shown.
	 */
	public List<String> shown() {
		return _shown;
	}

	@Override
	public void showError(HTMLFragment content) {
		_shown.add("error: " + text(content));
	}

	@Override
	public void showWarning(HTMLFragment content) {
		_shown.add("warning: " + text(content));
	}

	@Override
	public void showInfo(HTMLFragment content) {
		_shown.add("info: " + text(content));
	}

	/**
	 * The text the given content reads, with the markup around it stripped.
	 */
	public static String text(HTMLFragment content) {
		TagWriter out = new TagWriter();
		try {
			content.write(new DummyDisplayContext(), out);
		} catch (IOException ex) {
			throw new AssertionError(ex);
		}
		return out.toString().replaceAll("<[^>]*>", " ").replaceAll("\\s+", " ").trim();
	}

}
