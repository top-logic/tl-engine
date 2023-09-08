/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.ref;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.top_logic.basic.NamedConstant;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.time.CalendarUtil;
import com.top_logic.basic.time.TimeZones;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.layout.scripting.runtime.GlobalVariableStore;
import com.top_logic.layout.scripting.runtime.action.ApplicationAssertions;

/**
 * Flexible parser for primitive values (numbers, dates, strings/labels, variable references).
 * 
 * @see "The corresponding test case."
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class FuzzyValueFormat {

	static final String SEP = ";";

	/**
	 * Finds the option with the given label.
	 * 
	 * @param ref
	 *        The configuration from which the value is resolved.
	 * @param options
	 *        The options to choose from.
	 * @param labelProvider
	 *        The {@link LabelProvider} that is used to identify values (by label).
	 * @param valueLabel
	 *        The label to search.
	 * 
	 * @return The option with the matching label.
	 */
	public static Object resolveLabeledValue(ActionContext actionContext, ConfigurationItem ref, List<?> options,
			LabelProvider labelProvider, String valueLabel) {

		return getParser().parse(actionContext, ref, options, labelProvider, valueLabel);
	}

	private static Parser getParser() {
		return new Parser();
	}

	/**
	 * Produces the string representation of the given object.
	 */
	public static String toLabel(Object value) {
		if (value == null) {
			return "";
		}
		else if (value instanceof Number) {
			if (value instanceof Float || value instanceof Double) {
				return newDecimalFormat("0.0#########").format(value);
			} else {
				return newDecimalFormat("0").format(value);
			}
		}
		else if (value instanceof String) {
			return '"' + value.toString() + '"';
		}
		else if (value instanceof Date) {
			return CalendarUtil.newSimpleDateFormat("dd.MM.yyyy HH:mm", Locale.GERMANY).format(value);
		}
		else if (value instanceof Boolean) {
			return ((Boolean) value).booleanValue() ? "wahr" : "falsch";
		}
		else if (value instanceof Collection<?>) {
			StringBuilder buffer = new StringBuilder();
			for (Object element : (Collection<?>) value) {
				if (buffer.length() > 0) {
					buffer.append(SEP);
					buffer.append(" ");
				}
				buffer.append(toLabel(element));
			}
			return buffer.toString();
		}

		return null;
	}

	static DecimalFormat newDecimalFormat(String pattern) {
		return new DecimalFormat(pattern, DecimalFormatSymbols.getInstance(Locale.GERMANY));
	}

	static class Parser {

		private static final String SPACE = "\\s*";

		private static final String SOME_SPACE = "\\s+";

		private static final String TWO_DIGITS = "[0-9]{1,2}";

		private static final String INCREMENT = "[\\+\\-][1-9][0-9]*";

		private static final String DOT = "\\.";

		static final Object SEPARATOR = new NamedConstant("separator");

		private static final Object NONE = new NamedConstant("Nothing found");

		private static final String OR = "|";

		static abstract class Interpreter {
		
			public abstract Object parse(ActionContext actionContext, ConfigurationItem ref, Matcher matcher, int group);
		
		}
	
		static abstract class TokenInterpreter extends Interpreter {
		
			@Override
			public Object parse(ActionContext actionContext, ConfigurationItem ref, Matcher matcher, int group) {
				return parseToken(actionContext, ref, matcher.group(group));
			}
		
			protected abstract Object parseToken(ActionContext actionContext, ConfigurationItem ref, String token);
		
		}
	
		private static final Interpreter LITERALTEXT = new TokenInterpreter() {
			@Override
			protected Object parseToken(ActionContext actionContext, ConfigurationItem ref, String token) {
				return token;
			}
		};
	
		private static final Interpreter NUMBER = new Interpreter() {
			@Override
			public Object parse(ActionContext actionContext, ConfigurationItem ref, Matcher matcher, int group) {
				if (matcher.group(group + 1) != null) {
					try {
						return newDecimalFormat("0,0").parse(matcher.group(group)).doubleValue();
					} catch (ParseException ex) {
						throw ApplicationAssertions.fail(ref, "Wrong number format.", ex);
					}
				} else {
					return Integer.parseInt(matcher.group(group));
				}
			}
		};
	
		private static final Interpreter DATE = new Interpreter() {

			@Override
			public Object parse(ActionContext actionContext, ConfigurationItem ref, Matcher matcher, int group) {
				String daySpec = matcher.group(group + 1);
				String monthSpec = matcher.group(group + 2);
				String yearSpec = matcher.group(group + 3);
				String hourSpec = matcher.group(group + 4);
				String minuteSpec = matcher.group(group + 5);
				String yearInc = matcher.group(group + 6);
				String monthInc = matcher.group(group + 7);
				String dayInc = matcher.group(group + 8);
				String hourInc = matcher.group(group + 9);
				String minuteInc = matcher.group(group + 10);
				String secondInc = matcher.group(group + 11);
				String milliInc = matcher.group(group + 12);

				GregorianCalendar calendar = new GregorianCalendar(TimeZones.systemTimeZone());
				calendar.clear();
				if (yearSpec != null) {
					calendar.set(Calendar.YEAR, Integer.parseInt(yearSpec));
					calendar.set(Calendar.MONTH, Integer.parseInt(monthSpec) - (1 - Calendar.JANUARY));
					calendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(daySpec));
					if (hourSpec != null) {
						calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(hourSpec));
						calendar.set(Calendar.MINUTE, Integer.parseInt(minuteSpec));
					}
				}

				inc(calendar, Calendar.YEAR, yearInc);
				inc(calendar, Calendar.MONTH, monthInc);
				inc(calendar, Calendar.DAY_OF_MONTH, dayInc);
				inc(calendar, Calendar.HOUR_OF_DAY, hourInc);
				inc(calendar, Calendar.MINUTE, minuteInc);
				inc(calendar, Calendar.SECOND, secondInc);
				inc(calendar, Calendar.MILLISECOND, milliInc);

				return calendar.getTime();
			}

			protected void inc(GregorianCalendar calendar, int field, String inc) {
				if (inc != null) {
					calendar.add(field, parseInc(inc));
				}
			}

			protected int parseInc(String inc) {
				if (inc.startsWith("+")) {
					return Integer.parseInt(inc.substring(1));
				} else {
					return Integer.parseInt(inc);
				}
			}
			
		};
	
		private static final Interpreter TRUE = new Interpreter() {
			@Override
			public Object parse(ActionContext actionContext, ConfigurationItem ref, Matcher matcher, int group) {
				return true;
			}
		};
	
		private static final Interpreter FALSE = new Interpreter() {
			@Override
			public Object parse(ActionContext actionContext, ConfigurationItem ref, Matcher matcher, int group) {
				return false;
			}
		};
	
		private static final Interpreter VARIABLE = new TokenInterpreter() {
			@Override
			protected Object parseToken(ActionContext actionContext, ConfigurationItem ref, String token) {
				String variableName = token;
				GlobalVariableStore store = actionContext.getGlobalVariableStore();
				if (store.has(variableName)) {
					return store.get(variableName);
				} else {
					throw ApplicationAssertions.fail(ref, "Global variabel '" + variableName
						+ "' is not defined. Existing global variables: " + store.getMappings().keySet());
				}
			}
		};
	
		private static final Interpreter SEPARATOR_VALUE = new Interpreter() {
			@Override
			public Object parse(ActionContext actionContext, ConfigurationItem ref, Matcher matcher, int group) {
				return SEPARATOR;
			}
		};
	
		private final List<Interpreter> _interpreters = new ArrayList<>();
	
		private final Pattern _pattern;

		private int _group;
	
		public Parser() {
			_pattern = Pattern.compile(pattern());
		}
	
		public Object parse(ActionContext actionContext, ConfigurationItem ref, List<?> options,
				LabelProvider labelProvider, String source) {
			return parseCollectionSpec(actionContext, ref, options, labelProvider, source);
		}
	
		private Object parseCollectionSpec(ActionContext actionContext, ConfigurationItem ref,
				List<?> options, LabelProvider labelProvider, String valueLabel) {
			boolean hasValue = false;
			Object value = null;
			List<Object> collection = null;
			if (!valueLabel.isEmpty()) {
				Matcher tokens = matcher(valueLabel);
				while (tokens.lookingAt()) {
					Object element = nextValue(actionContext, ref, valueLabel, tokens);
					if (element != SEPARATOR) {
						element = resolveLabeledValue(ref, options, labelProvider, element);

						if (hasValue) {
							if (collection == null) {
								collection = new ArrayList<>();
								collection.add(value);
							}
							collection.add(element);
						} else {
							value = element;
							hasValue = true;
						}
					}
				}
	
				checkAllMachted(ref, valueLabel, tokens);
			}
			if (collection != null) {
				return collection;
			} else {
				return value;
			}
		}
	
		private Matcher matcher(String valueLabel) {
			return _pattern.matcher(valueLabel);
		}
	
		private static void checkAllMachted(ConfigurationItem ref, String valueLabel, Matcher tokens) {
			int position = tokens.regionStart();
			if (position < valueLabel.length()) {
				throw ApplicationAssertions.fail(ref, "Parse error: " + valueLabel.substring(0, position)
					+ " <!> " + valueLabel.substring(position));
			}
		}
	
		private Object nextValue(ActionContext actionContext, ConfigurationItem ref, String valueLabel, Matcher tokens) {
			Object value = extractValue(actionContext, ref, tokens);
			tokens.region(tokens.end(), valueLabel.length());
			return value;
		}
	
		private Object extractValue(ActionContext actionContext, ConfigurationItem ref, Matcher matcher) {
			for (int n = 0, cnt = _interpreters.size(); n < cnt; n++) {
				int group = n + 1;
				if (matcher.group(group) != null) {
					Interpreter interpreter = _interpreters.get(n);
					if (interpreter != null) {
						return interpreter.parse(actionContext, ref, matcher, group);
					}
				}
			}
	
			throw new UnreachableAssertion("No such token: " + matcher.group());
		}
	
		private String pattern() {
			String end = SPACE + "(?=" + SEP + "|$)";
			return
				quotedStringPattern() + end + OR +
				datePattern() + end + OR +
				numberPattern() + end + OR +
				variablePattern() + end + OR +
				booleanPattern() + end + OR +
				separatorPattern() + OR +
				unquotedTextPattern();
		}
	
		private String quotedStringPattern() {
			return SPACE + "\"" + group(_group++, "[^\"]*", LITERALTEXT) + "\"";
		}
	
		private String datePattern() {
			return SPACE +
				group(_group++,
					expr(
						expr(absoluteDatePattern() + optional(SOME_SPACE + timePattern())) + OR +
						expr("Jetzt" + OR + "now")) +
					optional(incremetPattern() + 
						expr(
							expr("Jahr" + optional("e")) + OR + 
							expr("year" + optional("s")) + OR + 
							"a")) +
					optional(incremetPattern() + 
						expr(
							expr("Monat" + "e") + OR + 
							expr("month" + optional("s")) + OR +
							"m")) +
					optional(incremetPattern() + 
						expr(
							expr("Tag" + optional("e")) + OR + 
							expr("day" + optional("s")) + OR + 
							"d")) +
					optional(incremetPattern() + 
						expr(
							expr("Stunde" + optional("n")) + OR + 
							expr("hour" + optional("s")) + OR + 
							"h")) +
					optional(incremetPattern() + 
						expr(
							expr("Minute" + optional("n")) + OR + 
							expr("minute" + optional("s")) + OR + 
							"min")) +
					optional(incremetPattern() + 
						expr(
							expr("Secunde" + optional("n")) + OR + 
							expr("second" + optional("s")) + OR + 
							"s")) +
					optional(incremetPattern() + 
						expr(
							expr("Millisekunde" + optional("n")) + OR + 
							expr("milli" + optional("s")) + OR +
							"ms"))
					, DATE);
		}

		private String incremetPattern() {
			return SOME_SPACE + group(_group++, INCREMENT) + SPACE;
		}

		private String timePattern() {
			return group(_group++, TWO_DIGITS) + ":" + group(_group++, TWO_DIGITS);
		}

		private String absoluteDatePattern() {
			return group(_group++, TWO_DIGITS) + DOT + group(_group++, TWO_DIGITS) + DOT + group(_group++, "[0-9]{2,4}");
		}
	
		private String group(int group, String expr) {
			return group(group, expr, null);
		}

		private String optional(String expr) {
			return expr(expr) + "?";
		}

		private String expr(String expr) {
			return "(?:" + expr + ")";
		}

		private String numberPattern() {
			return SPACE
				+ group(_group++, expr(expr("-?" + "[1-9][0-9]*") + "|" + "0")
					+ expr(
					group(_group++, "," + "[0-9]" + "+", null) + OR +
						""), NUMBER);
		}
	
		private String variablePattern() {
			return SPACE + "\\$" + group(_group++, "[^" + SEP + "\\s]+", VARIABLE);
		}
	
		private String booleanPattern() {
			return SPACE + group(
				_group++
				, group(_group++, "ja", TRUE) + OR +
					group(_group++, "wahr", TRUE) + OR +
					group(_group++, "yes", TRUE) + OR +
					group(_group++, "true", TRUE) + OR +
					group(_group++, "nein", FALSE) + OR +
					group(_group++, "falsch", FALSE) + OR +
					group(_group++, "false", FALSE) + OR +
					group(_group++, "no", FALSE), null);
		}
	
		private String separatorPattern() {
			return SPACE + group(_group++, SEP, SEPARATOR_VALUE) + SPACE;
		}
	
		private String unquotedTextPattern() {
			return SPACE + group(_group++, ("[^" + SEP + "]") + "+", LITERALTEXT) + SPACE;
		}
	
		private String group(int group, String string, Interpreter interpreter) {
			while (_interpreters.size() <= group) {
				_interpreters.add(null);
			}
			_interpreters.set(group, interpreter);
			return "(" + string + ")";
		}
	
		private static Object resolveLabeledValue(ConfigurationItem ref, List<?> options, LabelProvider labelProvider,
				Object valueSpec) {
	
			if (valueSpec instanceof String && options != null) {
				Object value = NONE;
				for (Object option : options) {
					String optionLabel = labelProvider.getLabel(option);
					if (StringServices.equals(optionLabel, valueSpec)) {
						if (value != NONE) {
							throw ApplicationAssertions.fail(ref, "Ambiguous options for label '" + value + "'.");
						}
						value = option;
					}
				}
				if (value == NONE) {
					throw ApplicationAssertions.fail(ref, "Unable to find an option with given label ('" + valueSpec
						+ "'). Options: " + toString(options, labelProvider));
				}
				return value;
			} else {
				return valueSpec;
			}
		}

		private static String toString(List<?> options, LabelProvider labelProvider) {
			String optionsString;
			Iterator<?> optionsIter = options.iterator();
			if (optionsIter.hasNext()) {
				StringBuilder optionsBuffer = new StringBuilder();
				optionsBuffer.append(labelProvider.getLabel(optionsIter.next()));
				while (optionsIter.hasNext()) {
					optionsBuffer.append(", ");
					optionsBuffer.append(labelProvider.getLabel(optionsIter.next()));
				}
				optionsString = optionsBuffer.toString();
			} else {
				optionsString = "No options";
			}
			return optionsString;
		}
	}

}
