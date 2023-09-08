/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tools;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.tooling.ModuleLayoutConstants;
import com.top_logic.layout.processor.LayoutModelConstants;

/**
 * Base class for tools updating layout XML definitions.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class LayoutRewrite extends XMLRewriter {

	private List<File> failedFiles = new ArrayList<>();

	private boolean _includeSrc;

	@Override
	protected int longOption(String option, String[] args, int i) {
		if (option.equals("includeSrc")) {
			_includeSrc = true;
		} else {
			return super.longOption(option, args, i);
		}
		return i;
	}

	@Override
	public void handleFile(String fileName) throws IOException, SAXException {
		processProjects(fileName);
		if (failedFiles.size() > 0) {
			for (File file : failedFiles) {
				System.err.println("Manual check required for: " + file);
			}
			failedFiles.clear();
		}
	}

	private void processProjects(String workspaceFolder) throws SAXException, IOException, FileNotFoundException {
		File file = new File(workspaceFolder);
		if (file.isDirectory()) {
			for (File project : FileUtilities.listFiles(file)) {
				if (project.getName().startsWith(".")) {
					continue;
				}

				scanIfExists(
					new File(project, ModuleLayoutConstants.LAYOUT_LOC),
					false);

				if (_includeSrc) {
					// Test case fixtures.
					scanIfExists(new File(project, ModuleLayoutConstants.SRC_TEST_DIR + "/" + "test"), true);
				}
			}
		}
	}

	private void scanIfExists(File dir, boolean componentOnly) throws SAXException, IOException {
		if (dir.exists() && dir.isDirectory()) {
			scan(dir, componentOnly);
		}
	}

	private void scan(File dir, boolean componentOnly) throws SAXException, IOException {
		for (File file : FileUtilities.listFiles(dir)) {
			if (file.getName().startsWith(".")) {
				continue;
			}
			if (file.isDirectory()) {
				scan(file, componentOnly);
			} else {
				if (!file.getName().endsWith(LayoutModelConstants.XML_SUFFIX)) {
					continue;
				}

				update(file, componentOnly);
			}
		}
	}

	private void update(File file, boolean componentOnly) throws IOException, FileNotFoundException {
		process(file, componentOnly);
	}

	private void process(File file, boolean componentOnly) throws IOException, FileNotFoundException {
		Document layout;
		try {
			layout = parse(file);
		} catch (SAXException ex) {
			System.err.println("ERROR: File '" + file.getPath() + "' cannot be parsed: " + ex.getMessage());
			return;
		}
		if (componentOnly) {
			if (!layout.getDocumentElement().getLocalName().equals("component")) {
				// skip.
				return;
			}
		}
		boolean rewriteSuccessful = process(layout);
		dump(layout, file);
		if (!rewriteSuccessful) {
			failedFiles.add(file);
		}
	}

	/**
	 * Actually transforms the given layout XML.
	 * 
	 * @return Whether the upgrade was performed sucessfully.
	 */
	protected abstract boolean process(Document layout);

}
