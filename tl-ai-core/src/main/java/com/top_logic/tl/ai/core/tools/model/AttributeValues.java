/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import com.top_logic.basic.sql.DBType;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.basic.util.ResKey;
import com.top_logic.model.TLClassifier;
import com.top_logic.model.TLEnumeration;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLPrimitive;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;
import com.top_logic.model.access.StorageMapping;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.tl.ai.core.security.ModelSecurity;
import com.top_logic.tl.ai.core.security.ReadFilter;
import com.top_logic.util.Resources;

/**
 * Turns the value a model wrote into a tool call into the value an attribute stores, and a stored
 * value back into what the model reads.
 *
 * <p>
 * <b>The server converts, the model does not.</b> A model writes JSON - a number, a string, a list,
 * an identifier - and has no business knowing that a date attribute stores a {@link Date} at midnight
 * or that an enumeration stores a classifier object. Every conversion is decided by the attribute's
 * own type:
 * </p>
 *
 * <ul>
 * <li>A <b>primitive</b> is parsed for its column type and then handed to the type's own
 * {@link StorageMapping} - the path the engine's XML data import takes, so a custom datatype (a
 * colour, a unit) is converted the way the application defined it rather than the way this class
 * guesses.</li>
 * <li>An <b>enumeration</b> takes the classifier's name.</li>
 * <li>A <b>reference</b> takes the identifier of an object, resolved through {@link TidResolver} and
 * therefore read-checked: a reference to an object the principal may not read is refused like one to
 * an object that does not exist.</li>
 * <li>A <b>multiple</b> attribute takes a list of such values and replaces the whole collection.</li>
 * </ul>
 *
 * <p>
 * A value that does not fit is refused with a sentence that names the attribute, its type and the
 * form it takes - never coerced into something the caller did not write. Every message is an English
 * literal: it is read by the model, see the Coding Rules in {@code CLAUDE.md}.
 * </p>
 *
 * @see ObjectEditTool
 */
final class AttributeValues {

	private AttributeValues() {
		// Static utility.
	}

	/**
	 * The value to store in the given attribute for what the model wrote.
	 *
	 * @param part
	 *        The attribute, already known to be writable.
	 * @param value
	 *        What the model wrote: <code>null</code>, a string, a number, a boolean, a list, a map with
	 *        an {@code _tid}, or a {@link TLObject} a caller already holds.
	 * @param security
	 *        The rights every referenced object is resolved under.
	 * @return The value for {@link TLObject#tUpdate(TLStructuredTypePart, Object)}; <code>null</code>
	 *         clears a single-valued attribute, an empty collection a multiple one.
	 * @throws IllegalArgumentException
	 *         If the value does not fit the attribute; the message is for the model.
	 */
	static Object convert(TLStructuredTypePart part, Object value, ModelSecurity security) {
		if (part.isMultiple()) {
			if (value == null) {
				return emptyCollection(part);
			}
			if (!(value instanceof Collection<?> items)) {
				throw new IllegalArgumentException(
					"'" + part.getName() + "' holds several values; pass a list"
						+ " (an empty list clears it), not " + describe(value) + ".");
			}
			Collection<Object> result = emptyCollection(part);
			for (Object item : items) {
				if (item == null) {
					throw new IllegalArgumentException(
						"The list for '" + part.getName() + "' contains null; leave the entry out.");
				}
				result.add(single(part, item, security));
			}
			return result;
		}
		if (value instanceof Collection<?> items) {
			if (items.size() != 1) {
				throw new IllegalArgumentException("'" + part.getName() + "' holds one value, not a list"
					+ " of " + items.size() + ".");
			}
			value = items.iterator().next();
		}
		return value == null ? null : single(part, value, security);
	}

	private static Collection<Object> emptyCollection(TLStructuredTypePart part) {
		return part.isOrdered() || part.isBag() ? new ArrayList<>() : new LinkedHashSet<>();
	}

	private static Object single(TLStructuredTypePart part, Object value, ModelSecurity security) {
		TLType type = part.getType();
		if (type instanceof TLPrimitive primitive) {
			return primitive(part, primitive, value);
		}
		if (type instanceof TLEnumeration enumeration) {
			return classifier(part, enumeration, value);
		}
		return reference(part, type, value, security);
	}

	// -- primitives --

	private static Object primitive(TLStructuredTypePart part, TLPrimitive type, Object value) {
		StorageMapping<?> mapping = type.getStorageMapping();
		Class<?> applicationType = mapping.getApplicationType();
		if (applicationType != null && ResKey.class.isAssignableFrom(applicationType)) {
			return translations(part, value);
		}
		DBType dbType = type.getDBType();
		Object stored = storage(part, type, dbType, value);
		Object business;
		try {
			business = mapping.getBusinessObject(stored);
		} catch (RuntimeException ex) {
			throw new IllegalArgumentException("'" + part.getName() + "' of type '" + type
				+ "' does not accept " + describe(value) + ": " + ex.getMessage(), ex);
		}
		if (business != null && !mapping.isCompatible(business)) {
			throw new IllegalArgumentException("'" + part.getName() + "' of type '" + type
				+ "' does not accept " + describe(value) + ".");
		}
		return business;
	}

	/**
	 * The column value for what the model wrote, following the engine's own XML import, which reads a
	 * primitive by its {@link DBType} before its {@link StorageMapping} sees it.
	 */
	private static Object storage(TLStructuredTypePart part, TLPrimitive type, DBType dbType, Object value) {
		if (dbType == null) {
			return value;
		}
		switch (dbType) {
			case BOOLEAN:
				return bool(part, value);
			case BYTE:
				return Byte.valueOf((byte) integral(part, value, Byte.MIN_VALUE, Byte.MAX_VALUE));
			case SHORT:
				return Short.valueOf((short) integral(part, value, Short.MIN_VALUE, Short.MAX_VALUE));
			case INT:
				return Integer.valueOf((int) integral(part, value, Integer.MIN_VALUE, Integer.MAX_VALUE));
			case LONG:
				return Long.valueOf(integral(part, value, Long.MIN_VALUE, Long.MAX_VALUE));
			case FLOAT:
				return Float.valueOf((float) decimal(part, value));
			case DOUBLE:
			case DECIMAL:
				return Double.valueOf(decimal(part, value));
			case DATE:
				return dayStart(part, value);
			case DATETIME:
				return instant(part, value);
			case TIME:
				return timeOfDay(part, value);
			case STRING:
			case CLOB:
			case CHAR:
				return text(part, value);
			case BLOB:
				throw new IllegalArgumentException("'" + part.getName() + "' holds a file of type '" + type
					+ "'. Files cannot be written with this tool.");
			case ID:
				throw new IllegalArgumentException("'" + part.getName() + "' holds an internal identifier"
					+ " and cannot be written with this tool.");
		}
		return value;
	}

	private static Boolean bool(TLStructuredTypePart part, Object value) {
		if (value instanceof Boolean flag) {
			return flag;
		}
		if (value instanceof String text) {
			String trimmed = text.trim();
			if ("true".equalsIgnoreCase(trimmed)) {
				return Boolean.TRUE;
			}
			if ("false".equalsIgnoreCase(trimmed)) {
				return Boolean.FALSE;
			}
		}
		throw new IllegalArgumentException(
			"'" + part.getName() + "' is a yes/no value; pass true or false, not " + describe(value) + ".");
	}

	private static long integral(TLStructuredTypePart part, Object value, long min, long max) {
		BigDecimal number = number(part, value, "a whole number");
		BigDecimal whole;
		try {
			whole = new BigDecimal(number.toBigIntegerExact());
		} catch (ArithmeticException ex) {
			throw new IllegalArgumentException(
				"'" + part.getName() + "' takes a whole number, not " + describe(value) + ".", ex);
		}
		if (whole.compareTo(BigDecimal.valueOf(min)) < 0 || whole.compareTo(BigDecimal.valueOf(max)) > 0) {
			throw new IllegalArgumentException("'" + part.getName() + "' takes a whole number between "
				+ min + " and " + max + ", not " + describe(value) + ".");
		}
		return whole.longValueExact();
	}

	private static double decimal(TLStructuredTypePart part, Object value) {
		return number(part, value, "a number").doubleValue();
	}

	private static BigDecimal number(TLStructuredTypePart part, Object value, String expected) {
		if (value instanceof Number number) {
			try {
				return new BigDecimal(number.toString());
			} catch (NumberFormatException ex) {
				throw new IllegalArgumentException(
					"'" + part.getName() + "' takes " + expected + ", not " + describe(value) + ".", ex);
			}
		}
		if (value instanceof String text) {
			try {
				return new BigDecimal(text.trim());
			} catch (NumberFormatException ex) {
				throw new IllegalArgumentException("'" + part.getName() + "' takes " + expected
					+ ". Pass a JSON number such as 450 or 12.5, without unit or thousands separator, not "
					+ describe(value) + ".", ex);
			}
		}
		throw new IllegalArgumentException(
			"'" + part.getName() + "' takes " + expected + ", not " + describe(value) + ".");
	}

	private static String text(TLStructuredTypePart part, Object value) {
		if (value instanceof String text) {
			return text;
		}
		if (value instanceof Number || value instanceof Boolean) {
			return value.toString();
		}
		throw new IllegalArgumentException(
			"'" + part.getName() + "' takes a text, not " + describe(value) + ".");
	}

	/**
	 * A day, as the start of that day in the server's zone.
	 *
	 * <p>
	 * That is how a day survives the trip through a {@code DATE} column, which keeps the date the JVM
	 * sees and drops the time: any other zone moves a day near midnight into its neighbour on some
	 * server.
	 * </p>
	 */
	private static Date dayStart(TLStructuredTypePart part, Object value) {
		LocalDate day = day(part, value);
		return Date.from(day.atStartOfDay(ZoneId.systemDefault()).toInstant());
	}

	private static LocalDate day(TLStructuredTypePart part, Object value) {
		if (value instanceof String text) {
			String trimmed = text.trim();
			try {
				return LocalDate.parse(trimmed.length() > 10 ? trimmed.substring(0, 10) : trimmed);
			} catch (DateTimeParseException ex) {
				throw new IllegalArgumentException(dateHint(part, value), ex);
			}
		}
		throw new IllegalArgumentException(dateHint(part, value));
	}

	private static String dateHint(TLStructuredTypePart part, Object value) {
		return "'" + part.getName() + "' takes a date; pass it as an ISO date such as \"2027-05-14\", not "
			+ describe(value) + ".";
	}

	/**
	 * A point in time. Without an offset it is read in the user's time zone, because that is the
	 * zone the user said it in.
	 */
	private static Date instant(TLStructuredTypePart part, Object value) {
		if (!(value instanceof String text)) {
			throw new IllegalArgumentException(dateTimeHint(part, value));
		}
		String trimmed = text.trim();
		try {
			return Date.from(OffsetDateTime.parse(trimmed).toInstant());
		} catch (DateTimeParseException withoutOffset) {
			try {
				return Date.from(Instant.parse(trimmed));
			} catch (DateTimeParseException notAnInstant) {
				try {
					LocalDateTime local = trimmed.length() == 10
						? LocalDate.parse(trimmed).atStartOfDay()
						: LocalDateTime.parse(trimmed);
					return Date.from(local.atZone(userZone()).toInstant());
				} catch (DateTimeParseException ex) {
					throw new IllegalArgumentException(dateTimeHint(part, value), ex);
				}
			}
		}
	}

	private static String dateTimeHint(TLStructuredTypePart part, Object value) {
		return "'" + part.getName() + "' takes a date and time; pass it in ISO form such as"
			+ " \"2027-05-14T09:30\" (read in the user's time zone) or \"2027-05-14T09:30:00+02:00\", not "
			+ describe(value) + ".";
	}

	private static Date timeOfDay(TLStructuredTypePart part, Object value) {
		if (value instanceof String text) {
			try {
				LocalTime time = LocalTime.parse(text.trim());
				return Date.from(time.atDate(LocalDate.EPOCH).atZone(userZone()).toInstant());
			} catch (DateTimeParseException ex) {
				throw new IllegalArgumentException(timeHint(part, value), ex);
			}
		}
		throw new IllegalArgumentException(timeHint(part, value));
	}

	private static String timeHint(TLStructuredTypePart part, Object value) {
		return "'" + part.getName() + "' takes a time of day such as \"09:30\", not " + describe(value) + ".";
	}

	private static ZoneId userZone() {
		java.util.TimeZone zone = ThreadContext.getTimeZone();
		return zone == null ? ZoneId.systemDefault() : zone.toZoneId();
	}

	/**
	 * An internationalised text: a map from language to text, or one text for the user's language.
	 */
	private static ResKey translations(TLStructuredTypePart part, Object value) {
		ResKey.Builder builder = ResKey.builder();
		if (value instanceof String text) {
			builder.add(userLocale(), text);
			return builder.build();
		}
		if (value instanceof Map<?, ?> byLanguage && !byLanguage.isEmpty()) {
			for (Map.Entry<?, ?> entry : byLanguage.entrySet()) {
				if (!(entry.getValue() instanceof String text)) {
					throw new IllegalArgumentException(translationHint(part, value));
				}
				builder.add(Locale.forLanguageTag(String.valueOf(entry.getKey())), text);
			}
			return builder.build();
		}
		throw new IllegalArgumentException(translationHint(part, value));
	}

	private static String translationHint(TLStructuredTypePart part, Object value) {
		return "'" + part.getName() + "' is a translated text; pass one text per language such as"
			+ " {\"en\": \"Summer party\", \"de\": \"Sommerfest\"}, or a single text for the user's"
			+ " language, not " + describe(value) + ".";
	}

	private static Locale userLocale() {
		Locale locale = ThreadContext.getLocale();
		return locale == null ? Locale.ENGLISH : locale;
	}

	// -- enumerations --

	private static TLClassifier classifier(TLStructuredTypePart part, TLEnumeration enumeration, Object value) {
		if (value instanceof TLClassifier classifier && classifier.getOwner() == enumeration) {
			return classifier;
		}
		String name = value instanceof String text ? text.trim() : null;
		if (name != null) {
			// The qualified form a model may copy out of a script, "module:Enum#NAME".
			int hash = name.lastIndexOf('#');
			if (hash >= 0) {
				name = name.substring(hash + 1);
			}
			TLClassifier exact = enumeration.getClassifier(name);
			if (exact != null) {
				return exact;
			}
			for (TLClassifier candidate : enumeration.getClassifiers()) {
				if (candidate.getName().equalsIgnoreCase(name)) {
					return candidate;
				}
			}
		}
		throw new IllegalArgumentException("'" + part.getName() + "' takes one of "
			+ enumeration.getClassifiers().stream().map(TLClassifier::getName).collect(Collectors.joining(", "))
			+ " (the name, not the label), not " + describe(value) + ".");
	}

	// -- references --

	private static TLObject reference(TLStructuredTypePart part, TLType type, Object value,
			ModelSecurity security) {
		TLObject target;
		if (value instanceof TLObject object) {
			// A caller that already holds the object read it under its own rights; still asked again,
			// since what may be written into a reference is what the principal may read.
			if (!security.canRead(object)) {
				throw new IllegalArgumentException(TidDiagnostics.explain(ReadFilter.idOf(object),
					TidDiagnostics.Types.NONE));
			}
			target = object;
		} else {
			String id = identifier(value);
			if (id == null) {
				throw new IllegalArgumentException("'" + part.getName() + "' refers to objects of type '"
					+ type + "'; pass the identifier of one, 'TypeName:id' as a tool answer printed it, not "
					+ describe(value) + ".");
			}
			target = TidResolver.resolve(id, security);
		}
		if (!TLModelUtil.isCompatibleInstance(type, target)) {
			throw new IllegalArgumentException("'" + part.getName() + "' refers to objects of type '" + type
				+ "', and '" + ReadFilter.idOf(target) + "' is of type '" + target.tType() + "'.");
		}
		return target;
	}

	private static String identifier(Object value) {
		if (value instanceof String text) {
			return text;
		}
		if (value instanceof Map<?, ?> map) {
			// The shape getInstance writes for a referenced object.
			Object tid = map.get(ReadFilter.TID);
			return tid instanceof String text ? text : null;
		}
		return null;
	}

	// -- reading back --

	/**
	 * What the model reads for a stored value: JSON-compatible data, with every object the principal
	 * may not read left out.
	 *
	 * <p>
	 * A date is written in the ISO form this class reads, a classifier by its name, and an
	 * internationalised text as one text per language - so the confirmation of a change says it in the
	 * same terms the call did, and a mistake in it is visible.
	 * </p>
	 */
	static Object echo(TLStructuredTypePart part, Object value, ReadFilter filter) {
		if (value == null) {
			return null;
		}
		if (value instanceof Collection<?> items) {
			List<Object> result = new ArrayList<>(items.size());
			for (Object item : items) {
				if (item instanceof TLObject object && !(item instanceof TLClassifier)
					&& !filter.canRead(object)) {
					continue;
				}
				result.add(echo(part, item, filter));
			}
			return result;
		}
		if (value instanceof TLClassifier classifier) {
			return classifier.getName();
		}
		if (value instanceof Date date) {
			return dateText(part, date);
		}
		if (value instanceof ResKey key) {
			return Resources.getInstance(userLocale()).getString(key, null);
		}
		if (value instanceof TLObject) {
			Object disclosed = filter.disclose(value);
			if (disclosed instanceof Map<?, ?> identity) {
				// The label and the identifier, the pair every other text answer uses.
				Map<String, Object> result = new LinkedHashMap<>();
				result.put("label", identity.get(ReadFilter.DISPLAY));
				result.put("id", identity.get(ReadFilter.TID));
				return result;
			}
			return disclosed;
		}
		return filter.disclose(value);
	}

	private static String dateText(TLStructuredTypePart part, Date date) {
		DBType dbType = part.getType() instanceof TLPrimitive primitive ? primitive.getDBType() : null;
		if (dbType == DBType.DATE) {
			return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().toString();
		}
		if (dbType == DBType.TIME) {
			return date.toInstant().atZone(userZone()).toLocalTime().toString();
		}
		return date.toInstant().atZone(userZone()).toOffsetDateTime().toString();
	}

	/**
	 * What the model wrote, in a few words, for a refusal: the kind of value and, if it is short, the
	 * value itself.
	 */
	static String describe(Object value) {
		if (value == null) {
			return "null";
		}
		if (value instanceof String text) {
			return text.length() > 60 ? "a text of " + text.length() + " characters" : "\"" + text + "\"";
		}
		if (value instanceof Number || value instanceof Boolean) {
			return value.toString();
		}
		if (value instanceof Collection<?> items) {
			return "a list of " + items.size();
		}
		if (value instanceof Map<?, ?>) {
			return "an object";
		}
		return "a " + value.getClass().getSimpleName();
	}
}
