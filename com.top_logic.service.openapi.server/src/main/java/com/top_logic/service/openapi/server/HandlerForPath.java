/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.service.openapi.server.conf.PathItem;

/**
 * {@link HandlerForPath} holds a {@link PathHandler} and allows checking whether a given input is
 * appropriate for the handler.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class HandlerForPath {

	private static final Pattern PARAMETER_PATTERN = Pattern.compile(PathItem.VARIABLE_PATTERN);

	/**
	 * Object in {@link HandlerForPath#_parts} representing a path variable.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	private static class Variable {

		String _varName;

		public Variable(String name) {
			_varName = Objects.requireNonNull(name);
		}

		public String getVariableName() {
			return _varName;
		}

		@Override
		public String toString() {
			return "<" + _varName + ">";
		}
	}

	/**
	 * Split path for {@link #getHandler()}. Contains either {@link String} representing a constant
	 * part in the path or {@link Variable} objects which represent a variable part in the path.
	 */
	private final Object[] _parts;

	private final PathHandler _handler;

	/**
	 * Creates a new {@link HandlerForPath}.
	 */
	public HandlerForPath(PathHandler handler) {
		_handler = handler;
		String pathWithParams = handler.getPath();
		Matcher matcher = PARAMETER_PATTERN.matcher(pathWithParams);
		if (!matcher.find()) {
			// No path parameters
			_parts = new Object[] { pathWithParams };
		} else {
			List<Object> splitPath = new ArrayList<>();
			String variableName;
			int index = 0;
			do {
				variableName = matcher.group(1);
				int start = matcher.start();
				if (start > index) {
					splitPath.add(pathWithParams.substring(index, start));
				}
				splitPath.add(new Variable(variableName));
				index = matcher.end();
			} while (matcher.find());
			if (index < pathWithParams.length()) {
				// Last part is constant
				splitPath.add(pathWithParams.substring(index));
			}
			_parts = splitPath.toArray();
		}

	}

	/**
	 * Checks whether the given input is appropriate for the given {@link PathHandler}.
	 * 
	 * @param input
	 *        The input string to check.
	 * @return Either <code>null</code>, if {@link #getHandler()} can not be used for the given
	 *         input, or a {@link Map} containing the given variables to deliver to the
	 *         {@link PathHandler}. May be empty, when the {@link PathHandler} does not uses path
	 *         variables.
	 */
	public Map<String, String> matches(String input) {
		if (_parts.length == 0) {
			if (input.length() == 0) {
				return Collections.emptyMap();
			}
			// No Match;
			return null;
		}
		Map<String, String> variables = Collections.emptyMap();
		int inputIndex = 0;
		String openVariable = null;
		for (int partIndex = 0; partIndex < _parts.length; partIndex++) {
			Object part = _parts[partIndex];
			if (part instanceof Variable) {
				String variableName = ((Variable) part).getVariableName();
				if (partIndex == _parts.length - 1) {
					// Let the request param care about multiplicity.
					variables = addVariable(variables, variableName, input.substring(inputIndex));
					inputIndex = input.length();
					openVariable = null;
					break;
				}
				if (openVariable != null) {
					// more than one variable in sequence. Fill the first with empty string.
					variables = addVariable(variables, openVariable, "");
				}
				openVariable = variableName;
			} else if (part instanceof String) {
				String constant = (String) part;
				if (openVariable == null) {
					// expected constant part
					if (!input.startsWith(constant, inputIndex)) {
						// No Match;
						return null;
					}
				} else {
					int indexOfConstant = input.indexOf(constant, inputIndex);
					if (indexOfConstant < 0) {
						// No Match;
						return null;
					} else {
						String variableValue = input.substring(inputIndex, indexOfConstant);
						if (variableValue.indexOf('/') >= 0) {
							// There is a part that does not match
							return null;
						}
						variables = addVariable(variables, openVariable, variableValue);
						inputIndex += variableValue.length();
					}
					openVariable = null;
				}
				inputIndex += constant.length();
			} else {
				throw new UnreachableAssertion("Only Strings and Variables are contained.");
			}
		}
		if (inputIndex < input.length()) {
			// Not all consumed
			return null;
		}
		return variables;
	}

	private Map<String, String> addVariable(Map<String, String> variables, String variableName, String value) {
		Map<String, String> result = variables;
		switch (variables.size()) {
			case 0:
				result = Collections.singletonMap(variableName, value);
				break;
			case 1:
				result = new HashMap<>(variables);
				//$FALL-THROUGH$
			default:
				result.put(variableName, value);
		}
		return result;
	}

	/**
	 * The {@link PathHandler} for the given path.
	 */
	public PathHandler getHandler() {
		return _handler;
	}

	@Override
	public String toString() {
		return "Parts: " + Arrays.toString(_parts);
	}

	/**
	 * {@link Comparator} for {@link HandlerForPath}.
	 * 
	 * <p>
	 * A {@link HandlerForPath} is compared by comparing the single parts. Constant parts are
	 * ordered by {@link String#compareTo(String)} and less than variables. All variables are equal.
	 * </p>
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static class CompareByParts implements Comparator<HandlerForPath> {

		/** Singleton {@link HandlerForPath.CompareByParts} instance. */
		public static final HandlerForPath.CompareByParts INSTANCE = new HandlerForPath.CompareByParts();

		/**
		 * Creates a new {@link CompareByParts}.
		 */
		CompareByParts() {
			// singleton instance
		}

		@Override
		public int compare(HandlerForPath o1, HandlerForPath o2) {
			Object[] parts1 = o1._parts;
			Object[] parts2 = o2._parts;
			int len1 = parts1.length;
			int len2 = parts2.length;
			int lim = Math.min(len1, len2);
			for (int k = 0; k < lim; k++) {
				Object part1 = parts1[k];
				Object part2 = parts2[k];
				if (part1 instanceof String) {
					if (part2 instanceof Variable) {
						return -1;
					} else {
						int constantCompare = ((String) part1).compareTo((String) part2);
						if (constantCompare == 0) {
							continue;
						}
						return constantCompare;
					}
				} else {
					if (part2 instanceof Variable) {
						continue;
					} else {
						return 1;
					}
				}
			}
			return len1 - len2;
		}

	}


}

