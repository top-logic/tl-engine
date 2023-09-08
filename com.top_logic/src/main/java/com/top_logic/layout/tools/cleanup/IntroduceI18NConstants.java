/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tools.cleanup;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.top_logic.basic.generate.CodeUtil;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.time.CalendarUtil;
import com.top_logic.basic.tooling.ModuleLayoutConstants;
import com.top_logic.layout.tools.FileHandler;
import com.top_logic.layout.tools.FileUtil;

/**
 * Tool scanning source files and introducing I18NConstants when trivially possible.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class IntroduceI18NConstants implements FileHandler {

	static final String TAB = "\t";

	static final String S = "[ \t]*";

	static final String WS = "\\s*";

	static final Pattern KEY_PATTERN =
		Pattern.compile("ResKey" + WS + "\\." + WS + "legacy" + WS + "\\(" + WS + "\"" + "([^\\s\\\"]+)" + "\"" + WS
		+ "\\)");

	static final Pattern PREFIX_PATTERN =
		Pattern.compile("ResPrefix" + WS + "\\." + WS + "legacyString" + WS + "\\(" + WS + "\"" + "([^\\s\\\"]+)"
		+ "\"" + WS
		+ "\\)");

	static final String NL = nl();

	/**
	 * Main entry point of the tool.
	 */
	public static void main(String[] args) throws Exception {
		IntroduceI18NConstants tool = new IntroduceI18NConstants();
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
		process(new File(fileName));
	}

	private void process(File file) throws Exception {
		if (file.getName().startsWith("I18NConstants")) {
			return;
		}

		if (file.isDirectory()) {
//			System.out.println("Inspecting: " + file);
			new PackageHandler().processDir(file);

			for (File child : FileUtilities.listFiles(file)) {
				if (child.isDirectory()) {
					process(child);
				}
			}
		} else {
			System.err.println("ERROR: Not a directory: " + file);
		}
	}

	static class PackageHandler {

		private SourceFile _i18nSource;

		Map<String, String> _constByKey;

		Map<String, String> _constByPrefix;

		private Set<String> _keys = new HashSet<>();

		private Set<String> _prefixes = new HashSet<>();

		public void processDir(File dir) throws Exception {
			File i18n = i18nFile(dir);
			_i18nSource = loadI18N(i18n);

			List<SourceFile> sources = new ArrayList<>();
			for (File child : FileUtilities.listFiles(dir)) {
				if (child.isFile()) {
					if (child.getName().endsWith(".java")) {
						SourceFile source = SourceFile.read(child);

						if (analyze(source)) {
							if (child.getName().equals("I18NConstants.java")) {
								System.err.println("ERROR: Invalid I18NConstants file with legacy key definition: "
									+ child);
								continue;
							}

							sources.add(source);
						}
					}
				}
			}

			createConstants();
			defineConstants();

			for (SourceFile source : sources) {
				replaceKeysWithConstants(source);
				source.save();
				System.out.println("Updating: " + source.getFile());
			}

			if (_i18nSource.isChanged()) {
				_i18nSource.save();
				System.out.println("Updating: " + _i18nSource.getFile());
			}
		}

		private void createConstants() {
			_constByPrefix = createConstants(_prefixes);
			_constByKey = createConstants(_keys);
		}

		private void defineConstants() throws AssertionError {
			defineKeys(_i18nSource, _constByKey);
			definePrefixes(_i18nSource, _constByPrefix);
		}

		private void replaceKeysWithConstants(SourceFile source) {
			rewrite(source, KEY_PATTERN, replacer(_constByKey));
			rewrite(source, PREFIX_PATTERN, replacer(_constByPrefix));
		}

		private ReplaceHandler replacer(final Map<String, String> constByKey) {
			ReplaceHandler replaceWithConstant = new ReplaceHandler() {
				@Override
				public void replaceMatch(StringBuilder out, Matcher matcher) {
					String constName = constByKey.get(matcher.group(1));

					out.append("I18NConstants.");
					out.append(constName);
				}
			};
			return replaceWithConstant;
		}

		private Map<String, String> createConstants(Set<String> keys) {
			Map<String, String> keyByConst = new HashMap<>();
			Set<String> invalidNames = new HashSet<>();
			List<String> todo = new ArrayList<>(keys);
			while (!todo.isEmpty()) {
				String key = todo.remove(todo.size() - 1);

				String lastName = "";
				String name;
				int level = 1;
				while (true) {
					name = keyConstName(level, key);

					if (name.isEmpty() || !Character.isJavaIdentifierStart(name.charAt(0))) {
						level++;
						lastName = name;
						continue;
					}

					if (invalidNames.contains(name) && !name.equals(lastName)) {
						level++;
						lastName = name;
						continue;
					}

					String clash = keyByConst.remove(name);
					if (clash != null) {
						invalidNames.add(name);
						todo.add(clash);
						level++;
						lastName = name;
						continue;
					}

					break;
				}

				keyByConst.put(name, key);
			}

			return invert(keyByConst);
		}

		private boolean analyze(SourceFile source) {
			boolean result = false;
			result |= collectKeys(source);
			result |= collectPrefixes(source);
			return result;
		}

		private boolean collectKeys(SourceFile source) {
			return collectConstants(source, KEY_PATTERN, _keys);
		}

		private boolean collectPrefixes(SourceFile source) {
			return collectConstants(source, PREFIX_PATTERN, _prefixes);
		}

		private boolean collectConstants(SourceFile source, Pattern pattern, final Set<String> keys) {
			return analyzefile(source, pattern, new Analyzer() {
				@Override
				public void match(Matcher matcher) {
					String key = matcher.group(1);
					keys.add(key);
				}
			});
		}
	}


	interface ReplaceHandler {

		void replaceMatch(StringBuilder out, Matcher matcher);

	}

	interface Analyzer {

		void match(Matcher matcher);

	}

	interface InsertHandler {

		void insert(StringBuilder out);

	}

	static boolean analyzefile(SourceFile source, Pattern pattern, Analyzer handler) {
		String content = source.getContent();
		Matcher matcher = pattern.matcher(content);
		int start = 0;
		while (matcher.find(start)) {
			handler.match(matcher);
			start = matcher.end();
		}

		return start > 0;
	}

	static void rewrite(SourceFile source, Pattern pattern, ReplaceHandler handler) {
		String content = source.getContent();
		StringBuilder out = new StringBuilder();
		Matcher matcher = pattern.matcher(content);
		int start = 0;
		while (matcher.find(start)) {
			out.append(content.substring(start, matcher.start()));

			handler.replaceMatch(out, matcher);

			start = matcher.end();
		}

		if (start > 0) {
			out.append(content.substring(start));

			source.setContent(out.toString());
		}
	}

	static String keyConstName(int level, String key) {
		int partStop = key.length();
		if (key.endsWith(".")) {
			partStop--;
		}

		int sepIndex = partStop;
		for (int n = 0; n < level; n++) {
			if (sepIndex < 0) {
				// Some constant is a suffix of another.
				sepIndex = -1;
				break;
			}

			sepIndex = key.lastIndexOf('.', sepIndex - 1);
			if (sepIndex < 0) {
				sepIndex = -1;
			}
		}

		int partStart = sepIndex + 1;
		String constName = CodeUtil.toAllUpperCase(key.substring(partStart, partStop));
		return constName;
	}

	static void defineKeys(SourceFile i18nSource, Map<String, String> constByKey) throws AssertionError {
		if (constByKey.isEmpty()) {
			return;
		}

		Pattern pattern = Pattern.compile("^" + S + "static" + WS + "\\{", Pattern.MULTILINE);
		final Map<String, String> keyByConst = invert(constByKey);
		insertBefore(i18nSource, pattern, new InsertHandler() {
			@Override
			public void insert(StringBuilder out) {
				ArrayList<String> consts = new ArrayList<>(keyByConst.keySet());
				Collections.sort(consts);
				for (String constName : consts) {
					out.append(TAB);
					out.append("public static ResKey ");
					out.append(constName);
					out.append(" = legacyKey(\"");
					out.append(keyByConst.get(constName));
					out.append("\");");
					out.append(NL);
					out.append(NL);
				}
			}
		});
	}

	static void definePrefixes(SourceFile i18nSource, Map<String, String> constByKey) throws AssertionError {
		if (constByKey.isEmpty()) {
			return;
		}
		final Map<String, String> keyByConst = invert(constByKey);
		Pattern pattern =
			Pattern.compile("\\b" + "extends" + WS + "I18NConstantsBase" + WS + "\\{" + S + "$", Pattern.MULTILINE);
		insertAfter(i18nSource, pattern, new InsertHandler() {
			@Override
			public void insert(StringBuilder out) {
				ArrayList<String> consts = new ArrayList<>(keyByConst.keySet());
				Collections.sort(consts);
				for (String constName : consts) {
					out.append(NL);
					out.append(NL);
					out.append(TAB);
					out.append("public static ResPrefix ");
					out.append(constName);
					out.append(" = legacyPrefix(\"");
					out.append(keyByConst.get(constName));
					out.append("\");");
				}
			}
		});
	}

	static void insertBefore(SourceFile source, Pattern pattern, InsertHandler handler) {
		insert(source, pattern, true, handler);
	}

	static void insertAfter(SourceFile source, Pattern pattern, InsertHandler handler) {
		insert(source, pattern, false, handler);
	}

	static void insert(SourceFile source, Pattern pattern, boolean before, InsertHandler handler) {
		String content = source.getContent();

		Matcher matcher = pattern.matcher(content);

		int insertPos;
		if (!matcher.find()) {
			System.err.println("ERROR: Cannot determine insert position in file: " + source.getFile());

			insertPos = content.length();
		} else {
			insertPos = before ? matcher.start() : matcher.end();
		}

		StringBuilder out = new StringBuilder();
		out.append(content.substring(0, insertPos));

		handler.insert(out);

		out.append(content.substring(insertPos));

		source.setContent(out.toString());
	}

	static SourceFile loadI18N(File i18n) throws IOException {
		SourceFile i18nSource;
		if (i18n.exists()) {
			i18nSource = SourceFile.read(i18n);
		} else {
			String pkg = getPackage(i18n);
			int year = CalendarUtil.createCalendar().get(Calendar.YEAR);
			i18nSource = new SourceFile(i18n,
				"/*" + NL +
				" * SPDX-FileCopyrightText: " + year + " (c) Business Operation Systems GmbH <info@top-logic.com>" + NL +
				" *" + NL +
				" * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0" + NL +
				" */" + NL +
					"package " + pkg + ";" + NL +
				"" + NL +
				"import com.top_logic.basic.util.ResKey;" + NL +
				"import com.top_logic.layout.I18NConstantsBase;" + NL +
				"import com.top_logic.layout.ResPrefix;" + NL +
				"" + NL +
				"/**" + NL +
				" * Internationalization constants for this package." + NL +
				" *" + NL +
				" * @see ResPrefix" + NL +
				" * @see ResKey" + NL +
				" *" + NL +
				" */" + NL +
				"@SuppressWarnings(\"javadoc\")" + NL +
				"public class I18NConstants extends I18NConstantsBase {" + NL +
				"" + NL +
				"	static {" + NL +
				"		initConstants(I18NConstants.class);" + NL +
				"	}" + NL +
					"}" + NL);
		}
		return i18nSource;
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

	static File i18nFile(File dir) {
		return new File(dir, "I18NConstants.java");
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
			return new IntroduceI18NConstants.SourceFile(file, content);
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
