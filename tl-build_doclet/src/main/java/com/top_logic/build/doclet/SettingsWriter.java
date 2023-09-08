/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.build.doclet;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

/**
 * Helper class to write settings file.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class SettingsWriter {

	private Boolean _showSrcLink;

	private String _srcBasePath;

	SettingsWriter setShowSourceLinks(Boolean showSrcLink) {
		_showSrcLink = showSrcLink;
		return this;
	}

	SettingsWriter setSourceBasePath(String srcBasePath) {
		_srcBasePath = srcBasePath;
		return this;
	}

	void writeToJSON(File jsonFile) throws IOException {
		boolean hasContent = false;
		try (Writer w = new OutputStreamWriter(new FileOutputStream(jsonFile), "utf-8")) {
			w.write("{");
			if (_showSrcLink != null) {
				if (hasContent) {
					w.write(",");
				}
				writeJsonProperty(w, "showSrcLink");
				writeJsonValue(w, _showSrcLink);
				hasContent = true;
			}
			if (_srcBasePath != null) {
				if (hasContent) {
					w.write(",");
				}
				writeJsonProperty(w, "srcBasePath");
				writeJsonValue(w, _srcBasePath);
				hasContent = true;
			}
			w.write("}");
		}
	}

	private static void writeJsonValue(Writer w, boolean value) throws IOException {
		w.write(Boolean.toString(value));
	}

	private static void writeJsonValue(Writer w, String value) throws IOException {
		w.write("\"");
		w.write(value);
		w.write("\"");
	}

	private static void writeJsonProperty(Writer w, String name) throws IOException {
		w.write("\"" + name + "\":");
	}

}

