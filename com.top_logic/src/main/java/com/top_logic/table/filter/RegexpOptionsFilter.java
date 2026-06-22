/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.table.filter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.util.ResKey;
import com.top_logic.table.ColumnFilter;
import com.top_logic.table.FilterInput;
import com.top_logic.table.FilterState;
import com.top_logic.table.Option;

/**
 * A column filter offering a fixed set of named regular expressions as checkboxes; a cell matches
 * when its text matches <em>any</em> selected pattern.
 *
 * <p>
 * Reuses the options (checkbox) input and editor - each {@link Option} carries a regexp as its value
 * and a display label - but replaces value-equality with a regexp match. This is the canonical
 * example of an application-defined filter that plugs in through the {@link ColumnFilter} /
 * {@link FilterInput} seam without any new UI: configure it on a column to offer predefined
 * facets such as {@code ^Part}, {@code ^Tool}, {@code ^Usage}.
 * </p>
 */
public class RegexpOptionsFilter implements ColumnFilter<String> {

	/**
	 * Configuration of a {@link RegexpOptionsFilter}.
	 */
	public interface Config extends PolymorphicConfiguration<RegexpOptionsFilter> {

		/**
		 * The offered patterns, in display order.
		 */
		@DefaultContainer
		List<PatternConfig> getPatterns();

	}

	/**
	 * One offered regular expression.
	 */
	@TagName("pattern")
	public interface PatternConfig extends ConfigurationItem {

		/** Configuration name of {@link #getRegexp()}. */
		String REGEXP = "regexp";

		/** Configuration name of {@link #getLabel()}. */
		String LABEL = "label";

		/**
		 * The regular expression matched against the cell text.
		 *
		 * @implNote Matched via {@link java.util.regex.Matcher#find()}.
		 */
		@Name(REGEXP)
		@Mandatory
		String getRegexp();

		/**
		 * The checkbox label; defaults to the {@link #getRegexp() regexp} itself.
		 */
		@Name(LABEL)
		ResKey getLabel();

	}

	private final List<Option> _options = new ArrayList<>();

	private final Map<Object, Pattern> _patterns = new LinkedHashMap<>();

	/**
	 * Creates a {@link RegexpOptionsFilter} from configuration.
	 */
	@CalledByReflection
	public RegexpOptionsFilter(InstantiationContext context, Config config) {
		for (PatternConfig patternConfig : config.getPatterns()) {
			String regexp = patternConfig.getRegexp();
			ResKey label = patternConfig.getLabel() != null ? patternConfig.getLabel() : ResKey.text(regexp);
			try {
				_patterns.put(regexp, Pattern.compile(regexp));
				_options.add(new Option(regexp, label));
			} catch (PatternSyntaxException ex) {
				context.error("Invalid regular expression '" + regexp + "' in column filter.", ex);
			}
		}
	}

	@Override
	public FilterInput input() {
		return new FilterInput.Options(_options);
	}

	@Override
	public Predicate<String> predicate(FilterState state) {
		Set<Object> selected = ((OptionsFilterState) state).selected();
		List<Pattern> active = new ArrayList<>();
		for (Object key : selected) {
			Pattern pattern = _patterns.get(key);
			if (pattern != null) {
				active.add(pattern);
			}
		}
		return value -> {
			if (value == null) {
				return false;
			}
			for (Pattern pattern : active) {
				if (pattern.matcher(value).find()) {
					return true;
				}
			}
			return false;
		};
	}

	@Override
	public boolean countsMatches() {
		return true;
	}

	@Override
	public Collection<Object> facetKeys(String value) {
		if (value == null) {
			return List.of();
		}
		List<Object> keys = new ArrayList<>();
		for (Map.Entry<Object, Pattern> entry : _patterns.entrySet()) {
			if (entry.getValue().matcher(value).find()) {
				keys.add(entry.getKey());
			}
		}
		return keys;
	}

}
