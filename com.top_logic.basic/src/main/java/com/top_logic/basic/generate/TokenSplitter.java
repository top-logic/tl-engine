/*
 * Copyright (c) 2023 Business Operation Systems GmbH. All Rights Reserved.
 */
package com.top_logic.basic.generate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Algorithm splitting a word into well-known tokens from a glossary.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TokenSplitter {

	private static final Pattern NUMBER = Pattern.compile("^([0-9]+)");

	private Map<String, List<String>> _glossary;

	/**
	 * Creates a {@link TokenSplitter}.
	 */
	public TokenSplitter(Map<String, List<String>> glossary) {
		_glossary = glossary;
	}

	/**
	 * Finds the minimum number of tokens from the glossary that allows to compose the given name.
	 */
	public List<String> split(String name) {
		Comparator<List<?>> comparator = Comparator.comparing(List::size);
		List<List<String>> splits = doSplit(name);
		return splits.stream().filter(l -> l.size() > 0).min(comparator).orElse(Collections.singletonList(name));
	}

	private List<List<String>> doSplit(String part) {
		List<List<String>> buffer = new ArrayList<>();

		for (String token : _glossary.keySet()) {
			if (token.isEmpty()) {
				// For safety reasons to prevent a stack overflow.
				continue;
			}
			if (part.startsWith(token)) {
				splitSuffix(buffer, part, token);
			}
		}

		if (buffer.isEmpty()) {
			Matcher matcher = NUMBER.matcher(part);
			if (matcher.find()) {
				splitSuffix(buffer, part, matcher.group(1));
			}
		}

		return buffer;
	}

	private void splitSuffix(List<List<String>> buffer, String part, String token) {
		String suffix = part.substring(token.length(), part.length());

		if (suffix.length() > 0) {
			List<List<String>> suffixSplits = doSplit(suffix);
			suffixSplits.forEach(l -> addReplacement(l, token));
			buffer.addAll(suffixSplits);
		} else {
			ArrayList<String> singleResult = new ArrayList<>();
			addReplacement(singleResult, token);
			buffer.add(singleResult);
		}
	}

	/**
	 * Adds the replacement for the given token to the given token list.
	 */
	protected void addReplacement(List<String> tokens, String token) {
		List<String> replacement = getGlossaryEntry(token);
		if (replacement == null) {
			tokens.add(0, token);
		} else {
			tokens.addAll(0, replacement);
		}
	}

	/**
	 * Retrieves the entry for the given token from the glossary.
	 */
	protected List<String> getGlossaryEntry(String token) {
		return _glossary.get(token);
	}

}
