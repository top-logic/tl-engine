/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.monitoring.log;

import static com.top_logic.basic.shared.collection.factory.CollectionFactoryShared.*;
import static com.top_logic.monitoring.log.LogLine.*;
import static java.util.stream.Collectors.*;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.event.infoservice.InfoService;
import com.top_logic.util.TLContextManager;

/**
 * Parses a log file into {@link LogLine} objects.
 * <p>
 * Example line:
 * <code>2023-08-25T13:59:11,136 INFO  [TL-Bootstrap]: com.top_logic.basic.module.BasicRuntimeModule - Service 'com.top_logic.knowledge.wrap.WebFolderFactory' successfully started.</code>
 * </p>
 * <p>
 * There is no enum for the entry parts, as applications may use different log patterns with other
 * parts.
 * </p>
 * <p>
 * The parser stores a cache of {@link LogLine} part strings to do a soft form of "interning"
 * ({@link String#intern()}). Instead of filling the JVM string cache, it uses its own. The cache is
 * non-static. When the {@link LogParser} object is garbage collected, its cache is garbage
 * collected, too.
 * </p>
 * <p>
 * This class is thread-safe and can be used for parsing multiple files in parallel.
 * </p>
 * 
 * @author <a href=mailto:jst@top-logic.com>Jan Stolzenburg</a>
 */
public class LogParser {

	/** The pattern for creating the {@link DateTimeFormatter}. */
	protected static final String TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss,SSS";

	/**
	 * The pattern for identifying a time stamp and the start of a new log statement.
	 * <p>
	 * Example: <code>2023-08-25T13:59:11,138</code>
	 * </p>
	 * 
	 * @see #ENTRY_START_PATTERN
	 */
	protected static final String TIME_PATTERN = "\\d{4}-\\d{2}-\\d{2}T\\d{2}\\:\\d{2}\\:\\d{2},\\d{3}";

	/**
	 * The pattern identifying the start of a new log statement by expecting a time stamp.
	 * <p>
	 * When this pattern does not match at the beginning of a line, that line is added to the
	 * previous log statement's details.
	 * </p>
	 * 
	 * @see #TIME_PATTERN
	 */
	protected static final Pattern ENTRY_START_PATTERN = Pattern.compile("^" + TIME_PATTERN);

	/**
	 * The pattern for parsing the severity.
	 * <p>
	 * The beginning of the severity is determined by the end of the time stamp. This pattern is
	 * used to determine the end of the severity.
	 * </p>
	 */
	protected static final Pattern SEVERITY_PATTERN = Pattern.compile("\\w+");

	/**
	 * The pattern for parsing the thread.
	 * <p>
	 * The beginning of the thread is determined by the end of the severity. This pattern is used to
	 * determine the end of the thread.
	 * </p>
	 */
	protected static final Pattern THREAD_PATTERN = Pattern.compile("(?<=\\[)[^\\]]+");

	/**
	 * The pattern for parsing the category.
	 * <p>
	 * The beginning of the category is determined by the end of the thread. This pattern is used to
	 * determine the end of the category.
	 * </p>
	 */
	protected static final Pattern CATEGORY_PATTERN = Pattern.compile("(?<=\\]\\: )\\S+");

	/**
	 * Marks the beginning of the message.
	 * <p>
	 * Everything after the end of the category and before the end of this marker is ignored.
	 * </p>
	 */
	protected static final String MESSAGE_START_MARKER = " - ";

	private final ZoneId _timeZoneId = TLContextManager.getSubSession().getCurrentTimeZone().toZoneId();

	/** The format for parsing the time stamp. */
	private final DateTimeFormatter _timeFormat = DateTimeFormatter
		.ofPattern(TIME_FORMAT, ResourcesModule.getLogLocale())
		.withZone(_timeZoneId);

	private final Map<String, String> _messages = new ConcurrentHashMap<>();

	private final Map<String, String> _categories = new ConcurrentHashMap<>();

	private final Map<String, String> _threads = new ConcurrentHashMap<>();

	private final Map<String, String> _details = new ConcurrentHashMap<>();

	/**
	 * Parses the {@link LogFile}.
	 * 
	 * @param logFile
	 *        Must not be null.
	 * @return Never null. The {@link List} might be immutable.
	 */
	public List<LogLine> parseLog(LogFile logFile) {
		return separateEntries(logFile)
			.stream()
			.map(logLine -> createParsedLogLine(logFile.getFileCategory(), logFile.getFileName(), logLine))
			.collect(toList());
	}

	/**
	 * Separated the {@link LogFile} into "unparsed" entries.
	 * <p>
	 * The parts of the entries are split into separate strings, but not parsed into proper objects,
	 * yet. Therefore "unparsed" entries.
	 * </p>
	 */
	protected List<Map<String, Object>> separateEntries(LogFile logFile) {
		try {
			return separateEntriesUnsafe(logFile.getContent());
		} catch (RuntimeException exception) {
			ResKey uiMessage = I18NConstants.FAILED_TO_PARSE_FILE__NAME;
			String logMessage = "Failed to split a log file into entries: " + logFile.getFileName();
			InfoService.logError(uiMessage, logMessage, exception, LogParser.class);
			return List.of();
		}
	}

	private List<Map<String, Object>> separateEntriesUnsafe(String log) {
		List<Map<String, Object>> entries = list();
		Map<String, Object> currentEntry = map();
		for (String line : toLines(log)) {
			Matcher matcher = ENTRY_START_PATTERN.matcher(line);
			if (matcher.find()) {
				currentEntry = parseEntry(line, matcher);
				entries.add(currentEntry);
			} else {
				@SuppressWarnings("unchecked")
				List<String> details = (List<String>) currentEntry.computeIfAbsent(PROPERTY_DETAILS, ignored -> new ArrayList<>());
				/* Don't strip the beginning of the details: It would remove the indentation of stack traces. */
				details.add(line.stripTrailing());
			}
		}
		if ((!entries.isEmpty()) && entries.get(0).isEmpty()) {
			/* The first entry represents the text before the first log statement. It should always
			 * be empty. */
			entries.remove(0);
		}
		return entries;
	}

	private List<String> toLines(String text) {
		return text.lines().collect(toList());
	}

	private Map<String, Object> parseEntry(String line, Matcher timeMatcher) {
		Map<String, Object> entry = map();
		entry.put(PROPERTY_TIME, timeMatcher.group().strip());

		int timeEnd = timeMatcher.end();
		int severityEnd = parseProperty(entry, PROPERTY_SEVERITY, SEVERITY_PATTERN, timeEnd, line);
		if (severityEnd == -1) {
			entry.put(PROPERTY_MESSAGE, line.substring(timeEnd));
			return entry;
		}

		int threadEnd = parseProperty(entry, PROPERTY_THREAD, THREAD_PATTERN, severityEnd, line);
		if (threadEnd == -1) {
			entry.put(PROPERTY_MESSAGE, line.substring(severityEnd));
			return entry;
		}

		int categoryEnd = parseProperty(entry, PROPERTY_CATEGORY, CATEGORY_PATTERN, threadEnd, line);
		if (categoryEnd == -1) {
			entry.put(PROPERTY_MESSAGE, line.substring(threadEnd));
			return entry;
		}

		int messageStart = line.indexOf(MESSAGE_START_MARKER, categoryEnd) + MESSAGE_START_MARKER.length();
		entry.put(PROPERTY_MESSAGE, line.substring(messageStart));
		return entry;
	}

	private int parseProperty(Map<String, Object> entry, String property, Pattern pattern, int start, String line) {
		Matcher matcher = pattern.matcher(line);
		matcher.region(start, line.length());
		matcher.useTransparentBounds(true);
		if (!matcher.find()) {
			return -1;
		}
		entry.put(property, matcher.group().strip());
		return matcher.end();
	}

	private LogLine createParsedLogLine(String fileCategory, String fileName, Map<String, Object> entry) {
		String message = internMessage((String) entry.get(PROPERTY_MESSAGE));
		Date time = parseTime((String) entry.get(PROPERTY_TIME));
		LogLineSeverity severity = LogLineSeverity.getOrCreate((String) entry.get(PROPERTY_SEVERITY));
		String category = internCategory((String) entry.get(PROPERTY_CATEGORY));
		String thread = internThread((String) entry.get(PROPERTY_THREAD));
		@SuppressWarnings("unchecked")
		String details = internDetails(joinDetails((List<String>) entry.get(PROPERTY_DETAILS)));
		return new LogLine(fileCategory, fileName, message, time, severity, category, thread, details);
	}

	private Date parseTime(String time) {
		/* The advantage of the DateTimeFormatter compared with the SimpleDateFormat is its
		 * thread-safety. That makes it trivial to parse multiple files in parallel. Additionally,
		 * the DateTimeFormatter parses faster than SimpleDateFormat. It is actually even faster to
		 * parse with the DateTimeFormatter and create a Date from the Instant, than to parse the
		 * Date directly with SimpleDateFormat. DateTimeFormatter is apparently much more
		 * performance optimized than SimpleDateFormat. */
		return Date.from(_timeFormat.parse(time, Instant::from));
	}

	private String joinDetails(List<String> originals) {
		if (originals == null) {
			return "";
		}
		return StringServices.join(originals, System.lineSeparator());
	}

	private String internMessage(String original) {
		return _messages.computeIfAbsent(original, Function.identity());
	}

	private String internCategory(String original) {
		return _categories.computeIfAbsent(original, Function.identity());
	}

	private String internThread(String original) {
		return _threads.computeIfAbsent(original, Function.identity());
	}

	private String internDetails(String original) {
		return _details.computeIfAbsent(original, Function.identity());
	}

}
