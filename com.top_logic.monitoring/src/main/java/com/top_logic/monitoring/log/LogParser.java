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
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.config.format.RegExpValueProvider;
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
public class LogParser extends AbstractConfiguredInstance<LogParser.Config> {

	/** {@link ConfigurationItem} for the {@link LogParser}. */
	public interface Config extends PolymorphicConfiguration<LogParser> {

		/** The default for {@link #getTimeFormat()}. */
		String DEFAULT_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss,SSS";

		/** The default for {@link #getTimePattern()}. */
		String DEFAULT_TIME_PATTERN = "\\d{4}-\\d{2}-\\d{2}T\\d{2}\\:\\d{2}\\:\\d{2},\\d{3}";

		/** The default for {@link #getEntryStartPattern()}. */
		String DEFAULT_ENTRY_START_PATTERN = "^" + DEFAULT_TIME_PATTERN;

		/** The default for {@link #getSeverityPattern()}. */
		String DEFAULT_SEVERITY_PATTERN = "\\w+";

		/** The default for {@link #getThreadPattern()}. */
		String DEFAULT_THREAD_PATTERN = "(?<=\\[)[^\\]]+";

		/** The default for {@link #getCategoryPattern()}. */
		String DEFAULT_CATEGORY_PATTERN = "(?<=\\]\\: )\\S+";

		/** The default for {@link #getMessageStartMarker()}. */
		String DEFAULT_MESSAGE_START_MARKER = " - ";

		/** The pattern for creating the {@link DateTimeFormatter}. */
		@StringDefault(DEFAULT_TIME_FORMAT)
		String getTimeFormat();

		/**
		 * The pattern for identifying a time stamp and the start of a new log statement.
		 * <p>
		 * Example: <code>2023-08-25T13:59:11,138</code>
		 * </p>
		 * 
		 * @see #DEFAULT_TIME_FORMAT
		 */
		@FormattedDefault(DEFAULT_TIME_PATTERN)
		@Format(RegExpValueProvider.class)
		Pattern getTimePattern();

		/**
		 * The pattern identifying the start of a new log statement by expecting a time stamp.
		 * <p>
		 * When this pattern does not match at the beginning of a line, that line is added to the
		 * previous log statement's details.
		 * </p>
		 */
		@FormattedDefault(DEFAULT_ENTRY_START_PATTERN)
		@Format(RegExpValueProvider.class)
		Pattern getEntryStartPattern();

		/**
		 * The pattern for parsing the severity.
		 * <p>
		 * The beginning of the severity is determined by the end of the time stamp. This pattern is
		 * used to determine the end of the severity.
		 * </p>
		 */
		@FormattedDefault(DEFAULT_SEVERITY_PATTERN)
		@Format(RegExpValueProvider.class)
		Pattern getSeverityPattern();

		/**
		 * The pattern for parsing the thread.
		 * <p>
		 * The beginning of the thread is determined by the end of the severity. This pattern is
		 * used to determine the end of the thread.
		 * </p>
		 */
		@FormattedDefault(DEFAULT_THREAD_PATTERN)
		@Format(RegExpValueProvider.class)
		Pattern getThreadPattern();

		/**
		 * The pattern for parsing the category.
		 * <p>
		 * The beginning of the category is determined by the end of the thread. This pattern is
		 * used to determine the end of the category.
		 * </p>
		 */
		@FormattedDefault(DEFAULT_CATEGORY_PATTERN)
		@Format(RegExpValueProvider.class)
		Pattern getCategoryPattern();

		/**
		 * Marks the beginning of the message.
		 * <p>
		 * Everything after the end of the category and before the end of this marker is ignored.
		 * </p>
		 */
		@StringDefault(DEFAULT_MESSAGE_START_MARKER)
		String getMessageStartMarker();

	}

	private final ZoneId _timeZoneId = TLContextManager.getSubSession().getCurrentTimeZone().toZoneId();

	/** The format for parsing the time stamp. */
	private final DateTimeFormatter _timeFormat;

	private final Pattern _timePattern;

	private final Pattern _entryStartPattern;

	private final Pattern _severityPattern;

	private final Pattern _threadPattern;

	private final Pattern _categoryPattern;

	private final String _messageStartMarker;

	private final Map<String, String> _messages = new ConcurrentHashMap<>();

	private final Map<String, String> _categories = new ConcurrentHashMap<>();

	private final Map<String, String> _threads = new ConcurrentHashMap<>();

	private final Map<String, String> _details = new ConcurrentHashMap<>();

	/** {@link TypedConfiguration} constructor for {@link LogParser}. */
	public LogParser(InstantiationContext context, Config config) {
		super(context, config);
		_timeFormat = DateTimeFormatter
			.ofPattern(config.getTimeFormat(), ResourcesModule.getLogLocale())
			.withZone(_timeZoneId);
		_timePattern = config.getTimePattern();
		_entryStartPattern = config.getEntryStartPattern();
		_severityPattern = config.getSeverityPattern();
		_threadPattern = config.getThreadPattern();
		_categoryPattern = config.getCategoryPattern();
		_messageStartMarker = config.getMessageStartMarker();
	}

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
			.map(logLine -> createLogLine(logFile.getFileCategory(), logFile.getFileName(), logLine))
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

	/** Separates the log into its lines and parses them into a {@link List} of {@link Map} objects.*/
	protected List<Map<String, Object>> separateEntriesUnsafe(String log) {
		List<Map<String, Object>> entries = list();
		Map<String, Object> currentEntry = map();
		for (String line : toLines(log)) {
			Matcher matcher = getEntryStartPattern().matcher(line);
			if (matcher.find()) {
				currentEntry = parseEntry(line);
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

	/** Parses the line into its parts and returns them in a {@link Map}. */
	protected Map<String, Object> parseEntry(String line) {
		Map<String, Object> entry = map();

		int timeStart = 0;
		int timeEnd = parseProperty(entry, PROPERTY_TIME, getTimePattern(), timeStart, line);
		if (timeEnd == -1) {
			entry.put(PROPERTY_MESSAGE, line.substring(timeStart));
			return entry;
		}

		int severityEnd = parseProperty(entry, PROPERTY_SEVERITY, getSeverityPattern(), timeEnd, line);
		if (severityEnd == -1) {
			entry.put(PROPERTY_MESSAGE, line.substring(timeEnd));
			return entry;
		}

		int threadEnd = parseProperty(entry, PROPERTY_THREAD, getThreadPattern(), severityEnd, line);
		if (threadEnd == -1) {
			entry.put(PROPERTY_MESSAGE, line.substring(severityEnd));
			return entry;
		}

		int categoryEnd = parseProperty(entry, PROPERTY_CATEGORY, getCategoryPattern(), threadEnd, line);
		if (categoryEnd == -1) {
			entry.put(PROPERTY_MESSAGE, line.substring(threadEnd));
			return entry;
		}

		int messageStart = line.indexOf(getMessageStartMarker(), categoryEnd) + getMessageStartMarker().length();
		entry.put(PROPERTY_MESSAGE, line.substring(messageStart));
		return entry;
	}

	/** Extracts the property and puts it into the map. */
	protected int parseProperty(Map<String, Object> entry, String property, Pattern pattern, int start, String line) {
		Matcher matcher = pattern.matcher(line);
		matcher.region(start, line.length());
		matcher.useTransparentBounds(true);
		if (!matcher.find()) {
			return -1;
		}
		entry.put(property, matcher.group().strip());
		return matcher.end();
	}

	/** Creates a {@link LogLine} from the map of its parts. */
	protected LogLine createLogLine(String fileCategory, String fileName, Map<String, Object> entry) {
		String message = internMessage((String) entry.get(PROPERTY_MESSAGE));
		Date time = parseTime((String) entry.get(PROPERTY_TIME));
		LogLineSeverity severity = LogLineSeverity.getOrCreate((String) entry.get(PROPERTY_SEVERITY));
		String category = internCategory((String) entry.get(PROPERTY_CATEGORY));
		String thread = internThread((String) entry.get(PROPERTY_THREAD));
		@SuppressWarnings("unchecked")
		String details = internDetails(joinDetails((List<String>) entry.get(PROPERTY_DETAILS)));
		return createLogLine(fileCategory, fileName, message, time, severity, category, thread, details);
	}

	/** Creates a {@link LogLine} from its parts. */
	protected LogLine createLogLine(String fileCategory, String fileName, String message, Date time,
			LogLineSeverity severity, String category, String thread, String details) {
		return new LogLine(fileCategory, fileName, message, time, severity, category, thread, details);
	}

	/** Parses the {@link String} into a {@link Date}. */
	protected Date parseTime(String time) {
		/* The advantage of the DateTimeFormatter compared with the SimpleDateFormat is its
		 * thread-safety. That makes it trivial to parse multiple files in parallel. Additionally,
		 * the DateTimeFormatter parses faster than SimpleDateFormat. It is actually even faster to
		 * parse with the DateTimeFormatter and create a Date from the Instant, than to parse the
		 * Date directly with SimpleDateFormat. DateTimeFormatter is apparently much more
		 * performance optimized than SimpleDateFormat. */
		return Date.from(getTimeFormat().parse(time, Instant::from));
	}

	/**
	 * Joins the strings.
	 * 
	 * @param originals
	 *        <code>null</code> is returned as the empty {@link String}.
	 */
	protected String joinDetails(List<String> originals) {
		if (originals == null) {
			return "";
		}
		return StringServices.join(originals, System.lineSeparator());
	}

	/** Interns the {@link String} by using a single cached instance for every equal text. */
	protected String internMessage(String original) {
		return _messages.computeIfAbsent(original, Function.identity());
	}

	/** Interns the {@link String} by using a single cached instance for every equal text. */
	protected String internCategory(String original) {
		return _categories.computeIfAbsent(original, Function.identity());
	}

	/** Interns the {@link String} by using a single cached instance for every equal text. */
	protected String internThread(String original) {
		return _threads.computeIfAbsent(original, Function.identity());
	}

	/** Interns the {@link String} by using a single cached instance for every equal text. */
	protected String internDetails(String original) {
		return _details.computeIfAbsent(original, Function.identity());
	}

	/** @see Config#getTimeFormat() */
	public DateTimeFormatter getTimeFormat() {
		return _timeFormat;
	}

	/** @see Config#getTimePattern() */
	public Pattern getTimePattern() {
		return _timePattern;
	}

	/** @see Config#getEntryStartPattern() */
	public Pattern getEntryStartPattern() {
		return _entryStartPattern;
	}

	/** @see Config#getSeverityPattern() */
	public Pattern getSeverityPattern() {
		return _severityPattern;
	}

	/** @see Config#getThreadPattern() */
	public Pattern getThreadPattern() {
		return _threadPattern;
	}

	/** @see Config#getCategoryPattern() */
	public Pattern getCategoryPattern() {
		return _categoryPattern;
	}

	/** @see Config#getMessageStartMarker() */
	public String getMessageStartMarker() {
		return _messageStartMarker;
	}

}
