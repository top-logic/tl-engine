/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.monitoring.log;

import static java.util.stream.Collectors.*;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.NonNullable;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.basic.config.constraint.annotation.Constraint;
import com.top_logic.basic.config.constraint.impl.NonNegative;
import com.top_logic.basic.config.format.CharsetFormat;
import com.top_logic.basic.config.format.RegExpValueProvider;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.util.ResKey;
import com.top_logic.event.infoservice.InfoService;

/**
 * Collects log files in a given directory, reads them and returns them as a {@link LogFile}
 * {@link List}.
 * <p>
 * This class is immutable and therefore thread safe. (As the {@link ConfigurationItem} must not be
 * changed anyway.)
 * </p>
 * 
 * @author <a href=mailto:jst@top-logic.com>Jan Stolzenburg</a>
 */
public class LogFileCollector extends AbstractConfiguredInstance<LogFileCollector.Config> {

	/**
	 * This {@link Charset} is used when reading a log file fails due to a
	 * {@link CharacterCodingException}.
	 */
	private static final Charset FALLBACK_LOG_FILE_CHARSET = StandardCharsets.UTF_8;

	/**
	 * The Unicode "Replacement Character" for undecodable bytes.
	 * <p>
	 * <a href="https://en.wikipedia.org/wiki/Specials_(Unicode_block)#Replacement_character">
	 * Wikipedia: Replacement character</a>
	 * </p>
	 */
	private static final String REPLACEMENT_CHARACTER = "\uFFFD";

	/** {@link ConfigurationItem} for the {@link LogFileCollector}. */
	public interface Config extends PolymorphicConfiguration<LogFileCollector> {

		/** The default for {@link #getFilePattern()}. */
		String DEFAULT_FILE_PATTERN = ".*\\.log";

		/** The default for {@link #getEncoding()}. */
		Charset DEFAULT_ENCODING = StandardCharsets.UTF_8;

		/** The default for {@link #getMaxFolderDepth()}. */
		int DEFAULT_MAX_FOLDER_DEPTH = 10;

		/**
		 * The pattern for detecting log files.
		 * <p>
		 * Only files whose name matches are collected. Only the local file name is matched, not the
		 * path.
		 * </p>
		 */
		@NonNullable
		@FormattedDefault(DEFAULT_FILE_PATTERN)
		@Format(RegExpValueProvider.class)
		Pattern getFilePattern();

		/**
		 * The encoding of the log files.
		 * <p>
		 * Is used to read the file content.
		 * </p>
		 */
		@FormattedDefault(StringServices.UTF8)
		@Format(CharsetFormat.class)
		Charset getEncoding();

		/**
		 * The maximum folder depth at which log files are searched.
		 * <p>
		 * When the folder structure is deeper than that, it is probably a loop caused by a symbolic
		 * link to an enclosing folder. To prevent an infinite recursion, the depth is therefore
		 * limited to this value.
		 * </p>
		 * <p>
		 * For "unlimited" use a large number like {@link Integer#MAX_VALUE}.
		 * </p>
		 */
		@Constraint(NonNegative.class)
		@IntDefault(DEFAULT_MAX_FOLDER_DEPTH)
		int getMaxFolderDepth();

	}

	/** {@link TypedConfiguration} constructor for {@link LogFileCollector}. */
	public LogFileCollector(InstantiationContext context, Config config) {
		super(context, config);
	}

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

	/** Whether the file is a log file. */
	protected boolean isLogFile(Path path, BasicFileAttributes attributes) {
		return attributes.isRegularFile() && matchesFilePattern(path.getFileName().toString());
	}

	/** Matches the given name against the {@link #getFilePattern()}. */
	protected final boolean matchesFilePattern(String fileName) {
		return getFilePattern().matcher(fileName).matches();
	}

	private LogFile readFile(Path file) {
		String fileName = file.getFileName().toString();
		try {
			String content = readFileToString(file);
			return createLogFile(fileName, content);
		} catch (IOException exception) {
			logErrorFileReadingFailed(fileName, exception);
			return new LogFile(fileName, "");
		}
	}

	private void logErrorFileReadingFailed(String fileName, IOException exception) {
		ResKey uiMessage = I18NConstants.FAILED_TO_READ_FILE__NAME.fill(fileName);
		String logMessage = "Failed to read a log file: " + fileName;
		InfoService.logError(uiMessage, logMessage, exception, LogParser.class);
	}

	private String readFileToString(Path file) throws IOException {
		try {
			return Files.readString(file, getEncoding());
		} catch (CharacterCodingException exception) {
			String fileName = file.getFileName().toString();
			logErrorCodingException(fileName, exception);
			return readFileWithBrokenCharacters(file);
		}
	}

	private void logErrorCodingException(String fileName, CharacterCodingException exception) {
		ResKey uiMessage = I18NConstants.FILE_IN_WRONG_ENCODING__NAME_ENCODING.fill(fileName, getEncoding());
		String logMessage = "Failed to read a log file, as it is not in encoding '" + getEncoding()
			+ "'. The actual encoding is unknown. File: " + fileName;
		InfoService.logError(uiMessage, logMessage, exception, LogParser.class);
	}

	/**
	 * Tries to read the given file, which could not be read in a first attempt.
	 * <p>
	 * The first attempt used {@link #getEncoding()} but failed with an
	 * {@link CharacterCodingException}.
	 * </p>
	 */
	protected String readFileWithBrokenCharacters(Path path) throws IOException {
		CharsetDecoder charsetDecoder = createErrorTolerantDecoder(FALLBACK_LOG_FILE_CHARSET);
		try (InputStream inputStream = new FileInputStream(path.toFile());
				Reader reader = new InputStreamReader(inputStream, charsetDecoder);
				BufferedReader bufferedReader = new BufferedReader(reader)) {
			return readToString(bufferedReader);
		}
	}

	private CharsetDecoder createErrorTolerantDecoder(Charset charset) {
		return charset.newDecoder()
			.onMalformedInput(CodingErrorAction.REPLACE)
			.onUnmappableCharacter(CodingErrorAction.REPLACE)
			.replaceWith(REPLACEMENT_CHARACTER);
	}

	private String readToString(BufferedReader reader) throws IOException {
		StringBuilder stringBuilder = new StringBuilder();
		FileUtilities.readAllLinesFromReader(reader, stringBuilder);
		return stringBuilder.toString();
	}

	/**
	 * Creates a {@link LogFile} object for the file and its content.
	 * 
	 * @param content
	 *        <code>null</code> is converted to the empty string.
	 */
	protected LogFile createLogFile(String fileName, String content) {
		return new LogFile(fileName, content);
	}

	/** @see Config#getFilePattern() */
	protected Pattern getFilePattern() {
		return getConfig().getFilePattern();
	}

	/** @see Config#getEncoding() */
	protected Charset getEncoding() {
		return getConfig().getEncoding();
	}

	/** @see Config#getMaxFolderDepth() */
	protected int getMaxFolderDepth() {
		return getConfig().getMaxFolderDepth();
	}

	/** The {@link FileVisitOption} settings when searching log files. */
	protected FileVisitOption[] getFileVisitOptions() {
		return DEFAULT_FILE_VISIT_OPTIONS;
	}

}
