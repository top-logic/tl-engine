/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.log.entry;

import static com.top_logic.basic.shared.string.StringServicesShared.*;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents a log file and its content.
 * 
 * @author <a href=mailto:jst@top-logic.com>Jan Stolzenburg</a>
 */
public class LogFile {

	/** The value when no log file category can be derived. */
	public static final String NO_CATEGORY = "";

	/** The default {@link Pattern} for extracting the {@link #getFileCategory() log file category}. */
	public static final Pattern DEFAULT_CATEGORY_PATTERN = Pattern.compile("^(.*?)(?:\\.[0-9]+)?\\.log$");

	private final String _fileName;

	private final String _fileCategory;

	private final String _content;

	/**
	 * Creates a {@link LogFile} with the given name and content.
	 * 
	 * @param fileName
	 *        Must not be null.
	 * @param content
	 *        If null, the empty {@link String} is used.
	 */
	public LogFile(String fileName, String content) {
		super();
		_fileName = Objects.requireNonNull(fileName);
		_fileCategory = extractCategory(fileName);
		_content = nonNull(content);
	}

	private String extractCategory(String fileName) {
		Matcher matcher = getCategoryPattern().matcher(fileName);
		return matcher.find() ? matcher.group(1) : NO_CATEGORY;
	}

	/**
	 * The {@link Pattern} for extracting the {@link #getFileCategory() log file category}.
	 * <p>
	 * The first group in the match is used as the category. If there is no match,
	 * {@link #NO_CATEGORY} is used.
	 * </p>
	 */
	protected Pattern getCategoryPattern() {
		return DEFAULT_CATEGORY_PATTERN;
	}

	/**
	 * The local name of the file.
	 * <p>
	 * The directory structure is not part of the name.
	 * </p>
	 */
	public String getFileName() {
		return _fileName;
	}

	/**
	 * The category of the file is the file name without file ending and counter.
	 * <p>
	 * Example: "tl-demo.scheduler.12.log" => "tl-demo.scheduler"
	 * </p>
	 */
	public String getFileCategory() {
		return _fileCategory;
	}

	/** The content of the file as a {@link String}. */
	public String getContent() {
		return _content;
	}

}
