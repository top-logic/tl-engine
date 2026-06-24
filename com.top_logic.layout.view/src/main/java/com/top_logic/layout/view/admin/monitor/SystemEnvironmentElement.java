/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.admin.monitor;

import java.lang.management.ManagementFactory;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.top_logic.basic.AliasManager;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.FileManager;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.react.control.table.TableViewControl;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.table.CellContent;
import com.top_logic.table.Column;
import com.top_logic.table.filter.TextColumnFilter;
import com.top_logic.table.impl.DefaultColumn;
import com.top_logic.table.impl.DefaultTableView;
import com.top_logic.table.impl.ListRowSource;
import com.top_logic.util.Resources;
import com.top_logic.util.monitor.SystemEnvironmentBuilder;

/**
 * Read-only table of the runtime environment: Java system properties, JVM startup arguments and the
 * application's configuration aliases, one row per entry with the originating section, the entry name
 * and its value.
 *
 * <p>
 * App-specific admin widget (referenced by {@code class=}, not a reusable {@code @TagName} element).
 * Values whose key matches a configured protected-alias pattern (see
 * {@link SystemEnvironmentBuilder.GlobalConfig}) are masked, so passwords and secrets carried in
 * properties, VM arguments or aliases are not exposed. It renders through the green-field
 * {@link TableViewControl}, so all three columns are sortable and text-filterable.
 * </p>
 */
public class SystemEnvironmentElement implements UIElement {

	/** Column id for the originating section. */
	private static final String SECTION_COLUMN = "section";

	/** Column id for the entry name. */
	private static final String NAME_COLUMN = "name";

	/** Column id for the entry value. */
	private static final String VALUE_COLUMN = "value";

	/** Matches a {@code -Dkey=value} JVM property argument. */
	private static final Pattern SYS_PROP_ARG_PATTERN = Pattern.compile("-D([^=]+)=(.*)");

	/**
	 * Configuration for {@link SystemEnvironmentElement}.
	 */
	public interface Config extends UIElement.Config {

		@Override
		@ClassDefault(SystemEnvironmentElement.class)
		Class<? extends UIElement> getImplementationClass();
	}

	/**
	 * Creates a new {@link SystemEnvironmentElement} from configuration.
	 */
	@CalledByReflection
	public SystemEnvironmentElement(InstantiationContext context, Config config) {
		// No configuration needed.
	}

	@Override
	public IReactControl createControl(ViewContext context) {
		List<Row> rows = collectRows();

		List<Column<Row, ?>> columns = new ArrayList<>();
		columns.add(textColumn(SECTION_COLUMN, I18NConstants.SYSTEM_ENVIRONMENT_SECTION_COLUMN, Row::section, 200));
		columns.add(textColumn(NAME_COLUMN, I18NConstants.SYSTEM_ENVIRONMENT_NAME_COLUMN, Row::name, 320));
		columns.add(textColumn(VALUE_COLUMN, I18NConstants.SYSTEM_ENVIRONMENT_VALUE_COLUMN, Row::value, 480));

		ListRowSource<Row> source = new ListRowSource<>(rows, columns);
		DefaultTableView<Row> view = DefaultTableView.create(columns, source);
		return new TableViewControl<>(context, view, false);
	}

	/**
	 * Builds the table rows from the system properties, VM arguments and application aliases.
	 */
	private List<Row> collectRows() {
		Resources resources = Resources.getInstance();
		String systemProperties =
			resources.getString(com.top_logic.util.monitor.I18NConstants.SYSTEM_ENVIRONMENT_SYSTEM_PROPERTIES);
		String vmArguments =
			resources.getString(com.top_logic.util.monitor.I18NConstants.SYSTEM_ENVIRONMENT_VM_ARGUMENTS);
		String applicationProperties =
			resources.getString(com.top_logic.util.monitor.I18NConstants.SYSTEM_ENVIRONMENT_APPLICATION_PROPERTIES);
		String blocked = resources.getString(com.top_logic.layout.form.I18NConstants.BLOCKED_VALUE_TEXT);

		List<Pattern> protectedKeys = ApplicationConfig.getInstance()
			.getConfig(SystemEnvironmentBuilder.GlobalConfig.class)
			.getProtectedAliases()
			.stream()
			.map(SystemEnvironmentBuilder.PatternConfig::getPattern)
			.collect(Collectors.toList());

		List<Row> rows = new ArrayList<>();

		Properties props = System.getProperties();
		for (String name : props.stringPropertyNames()) {
			rows.add(new Row(systemProperties, name, mask(protectedKeys, name, props.getProperty(name), blocked)));
		}
		for (Path path : FileManager.getInstance().getPaths()) {
			rows.add(new Row(systemProperties, "tl.resource.path", path.toString()));
		}

		for (String arg : ManagementFactory.getRuntimeMXBean().getInputArguments()) {
			Matcher matcher = SYS_PROP_ARG_PATTERN.matcher(arg);
			if (matcher.matches()) {
				rows.add(new Row(vmArguments, "-D" + matcher.group(1),
					mask(protectedKeys, matcher.group(1), matcher.group(2), blocked)));
			} else {
				rows.add(new Row(vmArguments, arg, ""));
			}
		}

		for (Map.Entry<String, String> alias : AliasManager.getInstance().getDefinedAliases().entrySet()) {
			rows.add(new Row(applicationProperties, alias.getKey(),
				mask(protectedKeys, alias.getKey(), alias.getValue(), blocked)));
		}

		return rows;
	}

	/**
	 * Returns the value, or the {@code blocked} replacement if {@code key} matches a protected
	 * pattern.
	 */
	private static String mask(List<Pattern> protectedKeys, String key, String value, String blocked) {
		boolean isProtected = protectedKeys.stream().anyMatch(pattern -> pattern.matcher(key).find());
		return isProtected ? blocked : value;
	}

	/**
	 * A sortable, text-filterable column reading one {@link String} property of a {@link Row}.
	 */
	private static Column<Row, String> textColumn(String id, ResKey label,
			java.util.function.Function<? super Row, String> value, int width) {
		return DefaultColumn.<Row, String> builder(id, value)
			.label(label)
			.renderer(CellContent::text)
			.sort(() -> Comparator.<String> naturalOrder())
			.filter(new TextColumnFilter<>(text -> text))
			.width(width)
			.build();
	}

	/**
	 * One environment entry: its originating section, name and (possibly masked) value.
	 */
	private static final class Row {

		private final String _section;

		private final String _name;

		private final String _value;

		Row(String section, String name, String value) {
			_section = section;
			_name = name;
			_value = value;
		}

		String section() {
			return _section;
		}

		String name() {
			return _name;
		}

		String value() {
			return _value;
		}
	}
}
