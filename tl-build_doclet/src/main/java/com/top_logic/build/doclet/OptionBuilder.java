/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.build.doclet;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Consumer;

import jdk.javadoc.doclet.Doclet.Option;

/**
 * Builder for {@link jdk.javadoc.doclet.Doclet.Option.Kind#STANDARD standard} {@link TLDoclet}
 * option.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class OptionBuilder {

	private int _count = 0;

	private String _description = "";

	private List<String> _names = new ArrayList<>();

	private String _parameters = "";

	private BiPredicate<String, List<String>> _processor;

	OptionBuilder argumentCount(int count) {
		_count = count;
		return this;
	}

	OptionBuilder description(String description) {
		_description = description;
		return this;
	}

	OptionBuilder addName(String name) {
		_names.add(name);
		return this;
	}

	OptionBuilder parameters(String parameters) {
		_parameters = parameters;
		return this;
	}

	OptionBuilder process(BiPredicate<String, List<String>> processor) {
		_processor = processor;
		return this;
	}

	OptionBuilder processArguments(Consumer<List<String>> processor) {
		_processor = (option, args) -> {
			processor.accept(args);
			return true;
		};
		return this;
	}

	/**
	 * Creates an {@link Option} based on this {@link OptionBuilder}.
	 */
	public Option build() {
		return new Option() {

			@Override
			public int getArgumentCount() {
				return _count;
			}

			@Override
			public String getDescription() {
				return _description;
			}

			@Override
			public Kind getKind() {
				return Kind.STANDARD;
			}

			@Override
			public List<String> getNames() {
				return _names;
			}

			@Override
			public String getParameters() {
				return _parameters;
			}

			@Override
			public boolean process(String option, List<String> arguments) {
				return _processor.test(option, arguments);
			}

		};
	}

}

