/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.log.entry;

import static java.util.stream.Collectors.*;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.stream.Stream;

import com.top_logic.basic.util.ResKey;
import com.top_logic.event.infoservice.InfoService;

/**
 * Collects log files in a given directory, reads them and returns them as a {@link LogFile}
 * {@link List}.
 * <p>
 * This class is stateless and therefore thread safe.
 * </p>
 * 
 * @author <a href=mailto:jst@top-logic.com>Jan Stolzenburg</a>
 */
public class LogFileCollector {

	/** The {@link LogFileCollector} instance. */
	public static final LogFileCollector INSTANCE = new LogFileCollector();

	/** The default file ending of log files. */
	public static final String DEFAULT_FILE_ENDING = ".log";

	/** The default encoding of log files. */
	public static final Charset DEFAULT_ENCODING = StandardCharsets.UTF_8;

	/**
	 * The maximum folder depth at which log files are searched.
	 * <p>
	 * When the folder structure is deeper than that, it is probably a loop caused by a symbolic
	 * link to an enclosing folder. To prevent an infinite recursion, the depth is therefore limited
	 * to this value.
	 * </p>
	 */
	public static final int DEFAULT_MAX_FOLDER_DEPTH = 10;

	/** The default {@link FileVisitOption} settings when searching log files: Follow links. */
	public static final FileVisitOption[] DEFAULT_FILE_VISIT_OPTIONS =
		new FileVisitOption[] { FileVisitOption.FOLLOW_LINKS };

	/** Searches all log files in the given directory, recursively. */
	public List<LogFile> collect(Path logDirectory) {
		try (Stream<Path> matches = findFiles(logDirectory)) {
			return matches.map(this::readFile).collect(toList());
		} catch (IOException ex) {
			throw new UncheckedIOException(ex);
		}
	}

	private Stream<Path> findFiles(Path logDirectory) throws IOException {
		return Files.find(logDirectory, getMaxFolderDepth(), this::isLogFile, getFileVisitOptions());
	}

	private boolean isLogFile(Path path, BasicFileAttributes attributes) {
		return attributes.isRegularFile() && path.getFileName().toString().endsWith(DEFAULT_FILE_ENDING);
	}

	private LogFile readFile(Path file) {
		String fileName = file.getFileName().toString();
		try {
			String content = Files.readString(file, getEncoding());
			return new LogFile(fileName, content);
		} catch (IOException exception) {
			ResKey uiMessage = I18NConstants.FAILED_TO_READ_FILE__NAME;
			String logMessage = "Failed to read a log file: " + fileName;
			InfoService.logError(uiMessage, logMessage, exception, LogParser.class);
			return new LogFile(fileName, "");
		}
	}

	/**
	 * The file name ending of log files.
	 * <p>
	 * Only files whose name ends with this are collected.
	 * </p>
	 */
	protected String getFileEnding() {
		return DEFAULT_FILE_ENDING;
	}

	/**
	 * The encoding of the log files.
	 * <p>
	 * Is used to read the file content.
	 * </p>
	 */
	protected Charset getEncoding() {
		return DEFAULT_ENCODING;
	}

	/**
	 * The maximum folder depth at which log files are searched.
	 * <p>
	 * This should be a small value to prevent infinite recursion caused by symbolic links to
	 * folders.
	 * </p>
	 */
	protected int getMaxFolderDepth() {
		return DEFAULT_MAX_FOLDER_DEPTH;
	}

	/** The {@link FileVisitOption} settings when searching log files. */
	protected FileVisitOption[] getFileVisitOptions() {
		return DEFAULT_FILE_VISIT_OPTIONS;
	}

}
