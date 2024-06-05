/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.generate;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.top_logic.basic.Environment;

/**
 * Generator that constructs a new file.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class FileGenerator {

	/**
	 * System property / environment variable name to customize the copyright holder in generated
	 * files.
	 */
	public static final String COPYRIGHT_HOLDER_PROPERTY = "tl_copyright_holder";

	/**
	 * System property / environment variable name to set copyright header in generated files.
	 * 
	 * <p>
	 * The string <code>${year}</code> is replaced by the current year.
	 * </p>
	 */
	public static final String COPYRIGHT_HEADER_PROPERTY = "tl_copyright_header";

	private PrintWriter out;
	
	private StringBuilder indent = new StringBuilder(64);

	private final FileType fileType;

	/** name value pairs eventually extracted form a previous file */
	private Map<String, String> tags = Collections.emptyMap();

	private final StringBuilder _nextLine = new StringBuilder();

	public abstract String fileName();
	
	
	public FileGenerator(FileType fileType) {
		this.fileType = fileType;
	}
	
	public final void generate(File sourceFile) throws IOException {
		Pattern vcsTagPattern = Pattern.compile("\\$(\\w+)\\:([^\\$]*)\\$");
		
		Map<String, String> before = tags;
		this.tags = new HashMap<>();
		if (sourceFile.exists()) {
			// Read old contents to get values of current CVS tags.
			try (BufferedReader reader =
				new BufferedReader(new InputStreamReader(new FileInputStream(sourceFile), getEncoding()))) {

				for (String line = reader.readLine(); line != null; line = reader.readLine()) {
					Matcher matcher = vcsTagPattern.matcher(line);
					for (int start = 0; matcher.find(start); start = matcher.end()) {
						String tagName = matcher.group(1);
						String tagValue = matcher.group(2);

						this.tags.put(tagName, tagValue);
					}
				}
			}
		}
		
		try (Writer fileOut = new OutputStreamWriter(new FileOutputStream(sourceFile), getEncoding())) {
			generate(fileOut);
		}
		
		this.tags = before;
	}

	/**
	 * The encoding to use, when generating the source file.
	 */
	protected Charset getEncoding() {
		return Charset.defaultCharset();
	}

	/**
	 * Starts generation writing to the given {@link Writer}.
	 */
	public void generate(Writer writer) {
		this.out = new PrintWriter(writer);
		try {
			writeFile();
		} finally {
			this.out.flush();
			this.out = null;
		}
	}
	
	protected String getExistingTag(String name) {
		return tags.get(name);
	}
	
	protected void writeFile() {
		writeFileHeader();
		writeContents();
	}
	
	protected abstract void writeContents();

	protected void writeFileHeader() {
		fileType.commentStart(out);

		String copyrightHeader =
			Environment.getSystemPropertyOrEnvironmentVariable(COPYRIGHT_HEADER_PROPERTY, null);
		if (copyrightHeader != null) {
			int year = new GregorianCalendar().get(Calendar.YEAR);
			copyrightHeader = copyrightHeader.replace("${year}", Integer.toString(year));
			{
				Matcher matcher = Pattern.compile("\\R").matcher(copyrightHeader);
				int startIDX = 0;
				while (matcher.find()) {
					fileType.commentLine(out, copyrightHeader.substring(startIDX, matcher.start()));
					startIDX = matcher.end();
				}
				fileType.commentLine(out, copyrightHeader.substring(startIDX, copyrightHeader.length()));
			}
		} else {
			String businessOperationSystems = "Business Operation Systems GmbH";
			String copyrightHolder =
				Environment.getSystemPropertyOrEnvironmentVariable(COPYRIGHT_HOLDER_PROPERTY, businessOperationSystems);
			int year = new GregorianCalendar().get(Calendar.YEAR);
			if (businessOperationSystems.equals(copyrightHolder)) {
				fileType.commentLine(out,
					"SPDX-FileCopyrightText: " + year + " (c) Business Operation Systems GmbH <info@top-logic.com>");
				fileType.commentLine(out, "");
				fileType.commentLine(out, "SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0");
			} else {
				fileType.commentLine(out, "Copyright (c) " + year + " " + copyrightHolder + ". All Rights Reserved.");
			}
		}

		fileType.commentStop(out);
	}
	

	protected void line(String line) {
		printIndent();
		out.println(line);
	}


	private void printIndent() {
		out.print(indent);
	}

	protected void unindent() {
		indent.setLength(indent.length() - 1);
	}

	protected void indent() {
		indent.append('\t');
	}
	
	protected void nl() {
		out.println();
	}
	
    /**
     * Start a multi-lined comment.
     */
	public void commentStart() {
		out.println();
		printIndent();
		fileType.commentStart(out);
	}
	
	/**
	 * Write a comment between {@link #commentStart()} and {@link #commentStop()}.
	 */
	public void commentLine(String comment) {
		printIndent();
		fileType.commentLine(out, comment);
	}
	
	/**
	 * End a multi-lined comment.
	 */
	public void commentStop() {
		printIndent();
		fileType.commentStop(out);
	}
	
	/**
	 * Write a single one-line comment.
	 */
	public void comment(String comment) {
		printIndent();
		fileType.comment(out, comment);
	}
	
	/**
	 * Access to the current line buffer.
	 */
	public final StringBuilder lineBuffer() {
		return _nextLine;
	}

	/**
	 * Appends the given contents to the current line buffer.
	 */
	public Appendable append(char c) {
		return _nextLine.append(c);
	}

	/**
	 * Appends the given contents to the current line buffer.
	 */
	public Appendable append(CharSequence csq) {
		return _nextLine.append(csq);
	}

	/**
	 * Appends the given contents to the current line buffer.
	 */
	public Appendable append(int value) {
		return _nextLine.append(value);
	}

	/**
	 * Appends the given contents to the current line buffer.
	 */
	public Appendable append(boolean value) {
		return _nextLine.append(value);
	}

	/**
	 * Appends the given contents to the current line buffer.
	 */
	public Appendable append(CharSequence csq, int start, int end) {
		return _nextLine.append(csq, start, end);
	}

	/**
	 * Flushes the buffered line.
	 * 
	 * @see #append(CharSequence)
	 */
	public void flushLine() {
		line(_nextLine.toString());
		_nextLine.setLength(0);
	}

	/**
	 * Creates a marker in the current line buffer.
	 * 
	 * <p>
	 * With a call to {@link #revert(int)}, the current line buffer can be reset to the marked
	 * position.
	 * </p>
	 */
	public int mark() {
		return _nextLine.length();
	}

	/**
	 * Reverts the current line to the position created with {@link #mark()}.
	 */
	public void revert(int pos) {
		_nextLine.setLength(pos);
	}

	/**
	 * Returns the generated line contents starting with the given position as string and
	 * {@link #revert(int) reverts} to that position.
	 */
	public String take(int pos) {
		String result = _nextLine.substring(pos);
		revert(pos);
		return result;
	}

}
