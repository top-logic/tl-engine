/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tools.cleanup;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Tool for migrating I18N resource translations from {@code WEB-INF/conf/resources/*_en.properties}
 * files into {@code @en} <code>JavaDoc</code> annotations on the corresponding {@code ResKey} fields in
 * {@code I18NConstants.java} files, see Ticket #29111.
 *
 * <p>
 * Usage:
 * </p>
 *
 * <pre>
 * mvn exec:java@migrate-ticket
 *   -Dtl.migrationClass=com.top_logic.layout.tools.cleanup.MigrateResourcesToJavaDoc
 *   -Dexec.args="/path/to/project/root"
 * </pre>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class MigrateResourcesToJavaDoc {

	private static final Charset ISO_8859_1 = Charset.forName("ISO-8859-1");

	private static final Pattern PACKAGE_PATTERN =
		Pattern.compile("^\\s*package\\s+([a-zA-Z0-9_.]+)\\s*;", Pattern.MULTILINE);

	private static final Pattern FIELD_PATTERN =
		Pattern.compile("public\\s+static\\s+(ResKey\\w*|ResPrefix)\\s+(\\w+)\\s*;");

	private static final Pattern CUSTOM_KEY_PATTERN =
		Pattern.compile("@CustomKey\\(\"([^\"]+)\"\\)");

	private static final Pattern CLASS_BODY_PATTERN =
		Pattern.compile("\\b(class|interface|enum)\\s+\\w+.*\\{", Pattern.DOTALL);

	/**
	 * Represents a migration action: adding @en (and optionally @tooltip) to a field.
	 */
	static class MigrationEntry {
		final String _fieldName;
		String _enText;
		String _tooltipText;
		String _helpText;

		MigrationEntry(String fieldName) {
			_fieldName = fieldName;
		}
	}

	/**
	 * Location of a field in a Java source file.
	 */
	static class FieldLocation {
		final File _javaFile;
		final String _fieldName;

		FieldLocation(File javaFile, String fieldName) {
			_javaFile = javaFile;
			_fieldName = fieldName;
		}
	}

	/**
	 * Main entry point.
	 *
	 * @param args
	 *        Single argument: the project root directory.
	 */
	public static void main(String[] args) throws Exception {
		if (args.length < 1) {
			System.err.println("Usage: MigrateResourcesToJavaDoc <project-root>");
			System.exit(1);
		}

		File projectRoot = new File(args[0]);
		if (!projectRoot.isDirectory()) {
			System.err.println("ERROR: Not a directory: " + projectRoot);
			System.exit(1);
		}

		new MigrateResourcesToJavaDoc().run(projectRoot);
	}

	/**
	 * Runs the migration.
	 */
	public void run(File projectRoot) throws IOException {
		// Step 1: Discover all I18NConstants.java files
		List<File> i18nFiles = new ArrayList<>();
		discoverI18NConstants(projectRoot, i18nFiles);
		System.out.println("Found " + i18nFiles.size() + " I18NConstants.java files.");

		// Step 2: Parse each I18NConstants.java and build a map from resource key to (file, field)
		Map<String, List<FieldLocation>> keyToFields = new HashMap<>();
		for (File javaFile : i18nFiles) {
			parseI18NConstants(javaFile, keyToFields);
		}
		System.out.println("Discovered " + keyToFields.size() + " resource keys from I18NConstants fields.");

		// Step 3: Discover and load all *_en.properties files
		List<File> propertiesFiles = new ArrayList<>();
		discoverResourceFiles(projectRoot, propertiesFiles);
		System.out.println("Found " + propertiesFiles.size() + " English resource files.");

		// Step 4: Match properties entries to I18NConstants fields
		// Map: java file -> (field name -> migration entry)
		Map<File, Map<String, MigrationEntry>> migrations = new LinkedHashMap<>();
		// Map: properties file -> set of keys to remove
		Map<File, Set<String>> removals = new LinkedHashMap<>();
		int matchCount = 0;

		for (File propsFile : propertiesFiles) {
			Properties props = loadProperties(propsFile);
			for (String propKey : props.stringPropertyNames()) {
				String baseKey = propKey;
				String suffix = null;

				if (propKey.endsWith(".tooltip")) {
					baseKey = propKey.substring(0, propKey.length() - ".tooltip".length());
					suffix = "tooltip";
				} else if (propKey.endsWith(".help")) {
					baseKey = propKey.substring(0, propKey.length() - ".help".length());
					suffix = "help";
				}

				List<FieldLocation> fields = keyToFields.get(baseKey);
				if (fields == null) {
					continue;
				}

				String value = props.getProperty(propKey);
				if (value == null || value.isEmpty()) {
					continue;
				}

				// Sanitize value: replace newlines with <br /> for JavaDoc compatibility
				String sanitized = sanitizeForJavaDoc(value);

				for (FieldLocation loc : fields) {
					MigrationEntry entry = migrations
						.computeIfAbsent(loc._javaFile, k -> new LinkedHashMap<>())
						.computeIfAbsent(loc._fieldName, k -> new MigrationEntry(loc._fieldName));

					if (suffix == null) {
						entry._enText = sanitized;
					} else if ("tooltip".equals(suffix)) {
						entry._tooltipText = sanitized;
					} else if ("help".equals(suffix)) {
						entry._helpText = sanitized;
					}

					removals.computeIfAbsent(propsFile, k -> new HashSet<>()).add(propKey);
					matchCount++;
				}
			}
		}

		System.out.println("Matched " + matchCount + " property entries to I18NConstants fields.");

		// Step 5: Apply changes to Java source files
		int javaFilesModified = 0;
		for (Map.Entry<File, Map<String, MigrationEntry>> fileEntry : migrations.entrySet()) {
			File javaFile = fileEntry.getKey();
			List<MigrationEntry> entries = new ArrayList<>(fileEntry.getValue().values());
			// Filter out entries with no @en text
			entries.removeIf(e -> e._enText == null);
			if (!entries.isEmpty() && applyToJavaFile(javaFile, entries)) {
				javaFilesModified++;
			}
		}
		System.out.println("Modified " + javaFilesModified + " Java source files.");

		// Step 6: Remove migrated entries from properties files
		int propsFilesModified = 0;
		for (Map.Entry<File, Set<String>> entry : removals.entrySet()) {
			File propsFile = entry.getKey();
			Set<String> keysToRemove = entry.getValue();
			if (removeFromPropertiesFile(propsFile, keysToRemove)) {
				propsFilesModified++;
			}
		}
		System.out.println("Modified " + propsFilesModified + " properties files.");
	}

	// -- Step 1: Discover I18NConstants.java files --

	private void discoverI18NConstants(File dir, List<File> result) {
		if (dir.getName().equals("target")) {
			return;
		}
		File[] children = dir.listFiles();
		if (children == null) {
			return;
		}
		for (File child : children) {
			if (child.isDirectory()) {
				discoverI18NConstants(child, result);
			} else if (child.getName().equals("I18NConstants.java")) {
				String path = child.getAbsolutePath();
				if (path.contains(File.separator + "src" + File.separator + "main" + File.separator + "java" + File.separator)
					|| path.contains(File.separator + "src" + File.separator + "test" + File.separator + "java" + File.separator)) {
					result.add(child);
				}
			}
		}
	}

	// -- Step 2: Parse I18NConstants.java files --

	private void parseI18NConstants(File javaFile, Map<String, List<FieldLocation>> keyToFields) throws IOException {
		String src = readFile(javaFile);

		Matcher pkgMatcher = PACKAGE_PATTERN.matcher(src);
		if (!pkgMatcher.find()) {
			System.err.println("WARNING: No package declaration in: " + javaFile);
			return;
		}
		String pkg = pkgMatcher.group(1);
		String className = pkg + ".I18NConstants";

		Matcher fieldMatcher = FIELD_PATTERN.matcher(src);
		while (fieldMatcher.find()) {
			String fieldType = fieldMatcher.group(1);
			String fieldName = fieldMatcher.group(2);

			if ("ResPrefix".equals(fieldType)) {
				continue; // Skip ResPrefix fields
			}

			// Check for @CustomKey annotation before this field
			String resourceKey = computeResourceKey(src, fieldMatcher.start(), className, fieldName);

			keyToFields.computeIfAbsent(resourceKey, k -> new ArrayList<>())
				.add(new FieldLocation(javaFile, fieldName));
		}
	}

	private String computeResourceKey(String src, int fieldStart, String className, String fieldName) {
		// Look backwards from the field declaration for @CustomKey annotation
		// Search within the preceding 500 characters (covers annotations and JavaDoc)
		int searchStart = Math.max(0, fieldStart - 500);
		String preceding = src.substring(searchStart, fieldStart);

		// Find the last @CustomKey before this field, but make sure there's no other field
		// declaration between the @CustomKey and this field
		Matcher customKeyMatcher = CUSTOM_KEY_PATTERN.matcher(preceding);
		String customKeyValue = null;
		int customKeyEnd = -1;
		while (customKeyMatcher.find()) {
			customKeyValue = customKeyMatcher.group(1);
			customKeyEnd = customKeyMatcher.end();
		}

		if (customKeyValue != null) {
			// Make sure no other field declaration exists between @CustomKey and this field
			String between = preceding.substring(customKeyEnd);
			if (!FIELD_PATTERN.matcher(between).find()) {
				return customKeyValue;
			}
		}

		// Default key: class.<fully.qualified.name>.<FIELD_NAME>
		return "class." + className + "." + fieldName;
	}

	// -- Step 3: Discover resource files --

	private void discoverResourceFiles(File dir, List<File> result) {
		if (dir.getName().equals("target")) {
			return;
		}
		File[] children = dir.listFiles();
		if (children == null) {
			return;
		}
		for (File child : children) {
			if (child.isDirectory()) {
				discoverResourceFiles(child, result);
			} else if (child.getName().endsWith("_en.properties")) {
				String path = child.getAbsolutePath();
				if (path.contains("WEB-INF" + File.separator + "conf" + File.separator + "resources")) {
					result.add(child);
				}
			}
		}
	}

	// -- Step 4: Load properties --

	private Properties loadProperties(File propsFile) throws IOException {
		Properties props = new Properties();
		try (FileInputStream fis = new FileInputStream(propsFile);
				InputStreamReader reader = new InputStreamReader(fis, ISO_8859_1)) {
			props.load(reader);
		}
		return props;
	}

	// -- Step 5: Apply changes to Java source files --

	private boolean applyToJavaFile(File javaFile, List<MigrationEntry> entries) throws IOException {
		String src = readFile(javaFile);
		String original = src;

		// Build a map from field name to migration entry
		Map<String, MigrationEntry> entryByField = new LinkedHashMap<>();
		for (MigrationEntry entry : entries) {
			entryByField.put(entry._fieldName, entry);
		}

		// Process each field - collect positions first
		Matcher fieldMatcher = FIELD_PATTERN.matcher(src);
		List<int[]> fieldPositions = new ArrayList<>();
		List<String> fieldNames = new ArrayList<>();
		while (fieldMatcher.find()) {
			fieldPositions.add(new int[] { fieldMatcher.start(), fieldMatcher.end() });
			fieldNames.add(fieldMatcher.group(2));
		}

		// Process from last to first to keep offsets stable
		for (int i = fieldPositions.size() - 1; i >= 0; i--) {
			String fieldName = fieldNames.get(i);
			MigrationEntry entry = entryByField.get(fieldName);
			if (entry == null) {
				continue;
			}

			int fieldStart = fieldPositions.get(i)[0];
			src = insertJavaDoc(src, fieldStart, entry);
		}

		if (src.equals(original)) {
			return false;
		}

		writeFile(javaFile, src);
		System.out.println("  Updated: " + javaFile);
		return true;
	}

	/**
	 * Inserts or updates JavaDoc for a field at the given position.
	 */
	private String insertJavaDoc(String src, int fieldStart, MigrationEntry entry) {
		// Find the start of the declaration block (including annotations and JavaDoc)
		// Look backwards for existing JavaDoc
		int searchStart = Math.max(0, fieldStart - 2000);
		String preceding = src.substring(searchStart, fieldStart);

		// Find existing JavaDoc comment that belongs to this field
		int javadocEnd = findJavaDocEnd(preceding);

		if (javadocEnd >= 0) {
			// Has existing JavaDoc - modify it
			int javadocStart = findJavaDocStart(preceding, javadocEnd);
			if (javadocStart >= 0) {
				String existingDoc = preceding.substring(javadocStart, javadocEnd + 2); // +2 for */
				String newDoc = updateJavaDoc(existingDoc, entry);
				return src.substring(0, searchStart + javadocStart)
					+ newDoc
					+ src.substring(searchStart + javadocEnd + 2);
			}
		}

		// No existing JavaDoc - insert new one before the field (or its annotations)
		int insertPos = findInsertPosition(preceding);
		// Detect indent from the field declaration line itself (not a potentially blank line)
		String indent = detectIndent(src, fieldStart);
		String newDoc = createJavaDoc(indent, entry);
		return src.substring(0, searchStart + insertPos)
			+ newDoc
			+ src.substring(searchStart + insertPos);
	}

	/**
	 * Finds the position of the closing "* /" of the last JavaDoc comment in the preceding text,
	 * ensuring no field declaration exists between it and the current field.
	 *
	 * @return index of '*' in "* /", or -1 if no JavaDoc found.
	 */
	private int findJavaDocEnd(String preceding) {
		int lastClose = preceding.lastIndexOf("*/");
		if (lastClose < 0) {
			return -1;
		}

		// Check that this is a JavaDoc comment (starts with /**)
		int openComment = preceding.lastIndexOf("/**", lastClose);
		if (openComment < 0) {
			return -1;
		}

		// Ensure no field declaration or class body opening between this JavaDoc and field
		String between = preceding.substring(lastClose + 2);
		if (FIELD_PATTERN.matcher(between).find()) {
			return -1; // The JavaDoc belongs to a different field
		}
		if (CLASS_BODY_PATTERN.matcher(between).find()) {
			return -1; // The JavaDoc belongs to the class declaration, not to this field
		}

		return lastClose;
	}

	/**
	 * Finds the start of the JavaDoc comment that ends at the given position.
	 */
	private int findJavaDocStart(String preceding, int javadocEnd) {
		return preceding.lastIndexOf("/**", javadocEnd);
	}

	/**
	 * Updates an existing JavaDoc comment by adding/replacing @en and @tooltip tags.
	 */
	private String updateJavaDoc(String existingDoc, MigrationEntry entry) {
		String indent = detectDocIndent(existingDoc);

		// Check if @en already exists
		Pattern enTagPattern = Pattern.compile("^(\\s*\\*\\s*)@en\\s", Pattern.MULTILINE);
		Matcher enMatcher = enTagPattern.matcher(existingDoc);

		String enLine = formatTagLine(indent, "@en", entry._enText);
		String tooltipLine = entry._tooltipText != null ? formatTagLine(indent, "@tooltip", entry._tooltipText) : "";

		if (enMatcher.find()) {
			// Replace existing @en tag (and any continuation lines)
			int enStart = enMatcher.start();
			int enEnd = findTagEnd(existingDoc, enMatcher.end());

			// Check if there's an existing @tooltip after @en
			Pattern tooltipTagPattern = Pattern.compile("^\\s*\\*\\s*@tooltip\\s", Pattern.MULTILINE);
			String afterEn = existingDoc.substring(enEnd);
			Matcher tooltipMatcher = tooltipTagPattern.matcher(afterEn);
			if (tooltipMatcher.find()) {
				int tooltipStart = enEnd + tooltipMatcher.start();
				int tooltipEnd = enEnd + findTagEnd(afterEn, tooltipMatcher.end());
				// Replace both @en and @tooltip
				return existingDoc.substring(0, enStart)
					+ enLine
					+ tooltipLine
					+ existingDoc.substring(tooltipEnd);
			}

			// Just replace @en, add @tooltip if needed
			return existingDoc.substring(0, enStart)
				+ enLine
				+ tooltipLine
				+ existingDoc.substring(enEnd);
		}

		// No existing @en - add before closing */
		int closingIdx = existingDoc.lastIndexOf("*/");
		if (closingIdx < 0) {
			return existingDoc;
		}

		// Go back to start of the */ line to avoid duplicating indentation
		int closingLineStart = existingDoc.lastIndexOf('\n', closingIdx);
		if (closingLineStart >= 0) {
			closingLineStart++;
		} else {
			closingLineStart = 0;
		}

		// Check if */ is on the same line as /** (single-line JavaDoc)
		int openIdx = existingDoc.indexOf("/**");
		if (openIdx >= closingLineStart) {
			// Single-line JavaDoc: replace entire comment with multi-line version.
			// Note: no indent before "/**" because existingDoc starts at "/**" (not the
			// preceding whitespace), and the original indent is preserved by the splice
			// in insertJavaDoc.
			return "/**\n"
				+ enLine
				+ tooltipLine
				+ indent + " */";
		}

		return existingDoc.substring(0, closingLineStart)
			+ enLine
			+ tooltipLine
			+ indent + " */";
	}

	/**
	 * Detects the structural indent of JavaDoc continuation lines. Returns the whitespace that
	 * precedes the " * " pattern, i.e., the structural indentation (typically a tab), not including
	 * the space immediately before '*'. This way {@code indent + " * "} reproduces the correct
	 * continuation line prefix.
	 */
	private String detectDocIndent(String doc) {
		// Match " * " continuation lines and capture whitespace before the " * " pattern.
		Pattern linePattern = Pattern.compile("^([ \t]*) \\* ", Pattern.MULTILINE);
		Matcher m = linePattern.matcher(doc);
		if (m.find()) {
			return m.group(1);
		}
		return "\t";
	}

	/**
	 * Finds the end of a JavaDoc tag block (handles continuation lines that don't start a new tag).
	 */
	private int findTagEnd(String text, int afterMatch) {
		int pos = afterMatch;
		while (pos < text.length()) {
			int nextNewline = text.indexOf('\n', pos);
			if (nextNewline < 0) {
				return text.length();
			}
			// Check if next line is a continuation (not a new tag, not closing */)
			String rest = text.substring(nextNewline + 1);
			if (rest.isEmpty()) {
				return nextNewline + 1;
			}
			String trimmedRest = rest.stripLeading();
			if (trimmedRest.startsWith("* @") || trimmedRest.startsWith("*/")) {
				return nextNewline + 1;
			}
			if (trimmedRest.startsWith("*")) {
				// Check if it's a continuation (indented content after *)
				String afterStar = trimmedRest.substring(1);
				if (afterStar.isEmpty() || afterStar.startsWith(" ")) {
					String content = afterStar.stripLeading();
					if (content.isEmpty() || content.startsWith("@") || content.startsWith("/")) {
						return nextNewline + 1;
					}
					// This is a continuation line
					pos = nextNewline + 1;
					continue;
				}
			}
			return nextNewline + 1;
		}
		return pos;
	}

	/**
	 * Creates a new JavaDoc comment with @en (and optionally @tooltip) tags.
	 */
	private String createJavaDoc(String indent, MigrationEntry entry) {
		StringBuilder sb = new StringBuilder();
		sb.append(indent).append("/**\n");
		sb.append(formatTagLine(indent, "@en", entry._enText));
		if (entry._tooltipText != null) {
			sb.append(formatTagLine(indent, "@tooltip", entry._tooltipText));
		}
		sb.append(indent).append(" */\n");
		return sb.toString();
	}

	/**
	 * Formats a JavaDoc tag line, wrapping long lines at approximately 100 characters.
	 */
	private String formatTagLine(String indent, String tag, String text) {
		String prefix = indent + " * " + tag + " ";
		String continuationIndent = indent + " *     ";

		if (prefix.length() + text.length() <= 100) {
			return prefix + text + "\n";
		}

		// Wrap at ~100 characters
		StringBuilder sb = new StringBuilder();
		sb.append(prefix);
		int lineLen = prefix.length();
		String[] words = text.split("(?<=\\s)");
		boolean firstWord = true;

		for (String word : words) {
			if (!firstWord && lineLen + word.length() > 100) {
				sb.append("\n");
				sb.append(continuationIndent);
				lineLen = continuationIndent.length();
			}
			sb.append(word);
			lineLen += word.length();
			firstWord = false;
		}
		sb.append("\n");
		return sb.toString();
	}

	/**
	 * Finds the position where a new JavaDoc comment should be inserted. This is before any
	 * annotations that belong to the field, at the start of the first annotation (or field) line.
	 */
	private int findInsertPosition(String preceding) {
		// Start at the field declaration line (last line of preceding)
		int contentLineStart = preceding.lastIndexOf('\n') + 1;

		// Walk backwards past annotation lines
		Pattern annotationLine = Pattern.compile("^[ \t]*@\\w+(\\(\"[^\"]*\"\\))?[ \t]*$");
		while (contentLineStart > 0) {
			int prevLineEnd = contentLineStart - 1; // position of \n before current line
			int prevLineStart = preceding.lastIndexOf('\n', prevLineEnd - 1) + 1;
			String prevLine = preceding.substring(prevLineStart, prevLineEnd);

			if (annotationLine.matcher(prevLine).matches()) {
				contentLineStart = prevLineStart;
			} else {
				break;
			}
		}

		return contentLineStart;
	}

	/**
	 * Detects the indentation at the given position in the preceding text.
	 */
	private String detectIndent(String preceding, int pos) {
		int lineStart = preceding.lastIndexOf('\n', pos > 0 ? pos - 1 : 0) + 1;
		StringBuilder indent = new StringBuilder();
		for (int i = lineStart; i < preceding.length(); i++) {
			char c = preceding.charAt(i);
			if (c == ' ' || c == '\t') {
				indent.append(c);
			} else {
				break;
			}
		}
		return indent.toString();
	}

	// -- Step 6: Remove entries from properties files --

	/**
	 * Removes the given keys from a properties file, preserving file structure. Handles backslash
	 * continuation lines.
	 */
	private boolean removeFromPropertiesFile(File propsFile, Set<String> keysToRemove) throws IOException {
		List<String> lines = readLines(propsFile);
		List<String> result = new ArrayList<>();
		boolean modified = false;

		for (int i = 0; i < lines.size(); i++) {
			String line = lines.get(i);

			// Check if this line starts a property that should be removed
			String key = extractPropertyKey(line);
			if (key != null && keysToRemove.contains(key)) {
				modified = true;
				// Skip continuation lines
				while (i < lines.size() && isContinuationLine(lines.get(i))) {
					i++;
				}
				continue;
			}

			result.add(line);
		}

		if (!modified) {
			return false;
		}

		// Remove trailing blank lines
		while (!result.isEmpty() && result.get(result.size() - 1).trim().isEmpty()) {
			result.remove(result.size() - 1);
		}

		writeLines(propsFile, result);
		System.out.println("  Cleaned: " + propsFile);
		return true;
	}

	/**
	 * Extracts the property key from a line, or null if not a property line.
	 */
	private String extractPropertyKey(String line) {
		String trimmed = line.trim();
		if (trimmed.isEmpty() || trimmed.startsWith("#") || trimmed.startsWith("!")) {
			return null;
		}

		StringBuilder key = new StringBuilder();
		boolean escaped = false;
		for (int i = 0; i < trimmed.length(); i++) {
			char c = trimmed.charAt(i);
			if (escaped) {
				key.append(c);
				escaped = false;
			} else if (c == '\\') {
				escaped = true;
			} else if (c == '=' || c == ':' || c == ' ' || c == '\t') {
				break;
			} else {
				key.append(c);
			}
		}
		return key.length() > 0 ? key.toString() : null;
	}

	/**
	 * Checks if a line ends with a backslash continuation.
	 */
	private boolean isContinuationLine(String line) {
		int backslashes = 0;
		for (int i = line.length() - 1; i >= 0 && line.charAt(i) == '\\'; i--) {
			backslashes++;
		}
		return backslashes % 2 != 0;
	}

	/**
	 * Sanitizes a property value for use in a JavaDoc tag. Replaces embedded newlines with
	 * {@code <br />} since JavaDoc tags must stay on a single logical line.
	 */
	private String sanitizeForJavaDoc(String value) {
		if (value.indexOf('\n') < 0 && value.indexOf('\r') < 0) {
			return value;
		}
		return value.replaceAll("\\r?\\n", "<br />");
	}

	// -- File I/O utilities --

	private String readFile(File file) throws IOException {
		StringBuilder sb = new StringBuilder();
		try (BufferedReader reader = new BufferedReader(
				new InputStreamReader(new FileInputStream(file), ISO_8859_1))) {
			char[] buf = new char[8192];
			int read;
			while ((read = reader.read(buf)) >= 0) {
				sb.append(buf, 0, read);
			}
		}
		return sb.toString();
	}

	private void writeFile(File file, String content) throws IOException {
		try (Writer writer = new OutputStreamWriter(new FileOutputStream(file), ISO_8859_1)) {
			writer.write(content);
		}
	}

	private List<String> readLines(File file) throws IOException {
		List<String> lines = new ArrayList<>();
		try (BufferedReader reader = new BufferedReader(
				new InputStreamReader(new FileInputStream(file), ISO_8859_1))) {
			String line;
			while ((line = reader.readLine()) != null) {
				lines.add(line);
			}
		}
		return lines;
	}

	private void writeLines(File file, List<String> lines) throws IOException {
		try (Writer writer = new OutputStreamWriter(new FileOutputStream(file), ISO_8859_1)) {
			for (int i = 0; i < lines.size(); i++) {
				writer.write(lines.get(i));
				writer.write('\n');
			}
		}
	}

}
