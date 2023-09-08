/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.json;

import java.io.IOError;
import java.io.IOException;
import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.util.Objects;

import com.top_logic.basic.json.JSON.DefaultValueAnalyzer;
import com.top_logic.basic.json.JSON.DefaultValueFactory;
import com.top_logic.basic.json.JSON.ParseException;
import com.top_logic.basic.json.JSON.ValueAnalyzer;
import com.top_logic.basic.json.JSON.ValueFactory;

/**
 * {@link Format} to serialise JSON objects.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class JSONFormat extends Format {

	private ValueAnalyzer _analyzer = DefaultValueAnalyzer.INSTANCE;

	private ValueFactory _factory = DefaultValueFactory.INSTANCE;

	private boolean _pretty = true;

	@Override
	public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
		Objects.requireNonNull(toAppendTo);
		Objects.requireNonNull(pos);
		try {
			JSON.write(toAppendTo, getAnalyzer(), obj, isPretty());
		} catch (IOException ex) {
			throw new IOError(ex);
		} catch (RuntimeException ex) {
			throw new IllegalArgumentException(ex);
		}
		return toAppendTo;
	}

	@Override
	public Object parseObject(String source, ParsePosition pos) {
		Objects.requireNonNull(source);
		Objects.requireNonNull(pos);
		try {
			Object result = JSON.fromString(source.substring(pos.getIndex()), getFactory());
			pos.setIndex(source.length());
			return result;
		} catch (ParseException ex) {
			pos.setErrorIndex(pos.getIndex() + ex.getErrorOffset());
			return null;
		}
	}

	/**
	 * The {@link ValueAnalyzer} that interprets the JSON string and reconstructs the JSON object.
	 */
	public ValueAnalyzer getAnalyzer() {
		return _analyzer;
	}

	/**
	 * The {@link ValueFactory} that creates the actual string representation of the stored JSON
	 * element.
	 */
	public ValueFactory getFactory() {
		return _factory;
	}

	/**
	 * Setter for {@link #getFactory()} and {@link #getAnalyzer()}.
	 */
	public JSONFormat setFactoryAndAnalyzer(ValueFactory factory, ValueAnalyzer analyzer) {
		_factory = Objects.requireNonNull(factory);
		_analyzer = Objects.requireNonNull(analyzer);
		return this;
	}

	/**
	 * Whether the formatted string should be pretty.
	 */
	public boolean isPretty() {
		return _pretty;
	}

	/**
	 * Setter for {@link #isPretty()}.
	 */
	public JSONFormat setPretty(boolean pretty) {
		_pretty = pretty;
		return this;
	}

}

