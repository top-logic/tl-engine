/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tools.cleanup;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.tooling.ModuleLayoutConstants;
import com.top_logic.layout.tools.FileHandler;
import com.top_logic.layout.tools.FileUtil;
import com.top_logic.tools.resources.ResourceFile;

/**
 * Tool scanning <code>Icons</code> definitions and extract theme-settings.properties definitions.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ExternalizeIconReferences implements FileHandler {

	static final String CONSTANTS_FILE = "Icons.java";

	static final String TAB = "\t";

	static final String S = "[ \t]*";

	static final String WS = "\\s*";

	static final Pattern ICON_PATTERN =
		Pattern.compile(
			"ThemeImage" + WS + "([\\w]+)" + WS + "=" + WS
				+ "ThemeImage" + WS + "\\." + WS + "icon" + WS + "\\(" + WS + "\"" + "([^\\s\\\"]+)" + "\"" + WS
				+ "\\)");

	static final String NL = nl();

	/**
	 * Main entry point of the tool.
	 */
	public static void main(String[] args) throws Exception {
		ExternalizeIconReferences tool = new ExternalizeIconReferences();
		FileUtil.handleFiles(args, tool);
	}

	private static String nl() {
		StringWriter buffer = new StringWriter();
		PrintWriter out = new PrintWriter(buffer);
		try {
			out.println();
		} finally {
			out.close();
		}
		return buffer.toString();
	}

	@Override
	public void handleFile(String fileName) throws Exception {
		File srcDir = new File(fileName);
		try (PackageHandler handler = new PackageHandler(srcDir)) {
			process(handler, srcDir);
		}
	}

	private void process(PackageHandler handler, File file) throws Exception {
		if (file.isDirectory()) {
//			System.out.println("Inspecting: " + file);
			handler.processDir(file);

			for (File child : FileUtilities.listFiles(file)) {
				if (child.isDirectory()) {
					process(handler, child);
				}
			}
		} else {
			System.err.println("ERROR: Not a directory: " + file);
		}
	}

	static class PackageHandler implements AutoCloseable {

		Map<String, String> _vars = new HashMap<>();

		private File _srcDir;

		public PackageHandler(File srcDir) {
			_srcDir = srcDir;
		}

		@Override
		public void close() throws IOException {
			if (_vars.isEmpty()) {
				return;
			}
			File settingsDir = new File(_srcDir, "META-INF/themes/default");
			File settingsFile = new File(settingsDir, "/theme-settings.properties");

			com.top_logic.tools.resources.ResourceFile resourceFile = new ResourceFile(settingsFile);

			for (Entry<String, String> entry : _vars.entrySet()) {
				resourceFile.setProperty(entry.getKey(), entry.getValue());
			}

			settingsDir.mkdirs();
			resourceFile.save();
		}

		public void processDir(File dir) throws Exception {
			File definition = definitionFile(dir);
			if (!definition.exists()) {
				return;
			}
			SourceFile source = SourceFile.read(definition);

			process(source);

			if (source.isChanged()) {
				source.save();
				System.out.println("Updating: " + source.getFile());
			}
		}

		private void process(SourceFile source) throws IOException {
			String packageName = getPackage(source.getFile());

			String content = source.getContent();
			StringBuffer buffer = new StringBuffer();
			Matcher matcher = ICON_PATTERN.matcher(content);
			boolean hasMatch = false;
			while (matcher.find()) {
				hasMatch = true;
				String constName = matcher.group(1);
				String iconDef = matcher.group(2);

				_vars.put(packageName + "." + "Icons" + "." + constName, iconDef);

				matcher.appendReplacement(buffer, "ThemeImage " + constName);
			}
			matcher.appendTail(buffer);

			if (hasMatch) {
				source.setContent(buffer.toString());
			}
		}
	}

	static String getPackage(File file) throws IOException {
		File path = file.getCanonicalFile().getParentFile();

		String result = "";
		while (!path.getName().equals(ModuleLayoutConstants.SRC_MAIN_DIR)) {
			result = path.getName() + (result.isEmpty() ? "" : "." + result);
			path = path.getParentFile();

			assert path != null;
		}

		return result;
	}

	static File definitionFile(File dir) {
		return new File(dir, "Icons.java");
	}

	static class SourceFile {
	
		private final File _file;
	
		private String _content;
	
		private boolean _changed;
	
		public SourceFile(File file, String content) {
			_file = file;
			_content = content;
		}
	
		public void setContent(String content) {
			_content = content;
			_changed = true;
		}
	
		public File getFile() {
			return _file;
		}
	
		public boolean isChanged() {
			return _changed;
		}
	
		public String getContent() {
			return _content;
		}
	
		public static SourceFile read(File file) throws IOException {
			String content = FileUtilities.readFileToString(file);
			return new ExternalizeIconReferences.SourceFile(file, content);
		}
	
		public void save() throws IOException {
			FileUtilities.writeStringToFile(_content, _file);
		}
	
		public void saveAs(File file) throws IOException {
			FileUtilities.writeStringToFile(_content, file);
		}
	
	}

	static <T> Map<T, T> invert(Map<T, T> map) {
		Map<T, T> result = new HashMap<>();
		for (Entry<T, T> entry : map.entrySet()) {
			T clash = result.put(entry.getValue(), entry.getKey());
			assert clash == null;
		}
		return result;
	}

}
