/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.admin.monitor;

import java.lang.management.ManagementFactory;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.FileManager;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.view.UIElement;
import com.top_logic.util.Resources;
import com.top_logic.util.monitor.SystemEnvironmentBuilder;

/**
 * Read-only table of the runtime environment: Java system properties and JVM startup arguments, one
 * row per entry with the originating section, the entry name and its value.
 *
 * <p>
 * App-specific admin widget (referenced by {@code class=}, not a reusable {@code @TagName}
 * element). Values whose key is {@link SystemEnvironmentBuilder#protectedNames() protected} are
 * masked, so passwords and secrets carried in properties or VM arguments are not exposed. The
 * configuration variables are shown by the view <code>admin/system/config-variables.view.xml</code>.
 * </p>
 */
public class SystemEnvironmentTable extends SectionedTable {

	/** Matches a {@code -Dkey=value} JVM property argument. */
	private static final Pattern SYS_PROP_ARG_PATTERN = Pattern.compile("-D([^=]+)=(.*)");

	/**
	 * Configuration for {@link SystemEnvironmentTable}.
	 */
	public interface Config extends SectionedTable.Config {

		@Override
		@ClassDefault(SystemEnvironmentTable.class)
		Class<? extends UIElement> getImplementationClass();
	}

	/**
	 * Creates a new {@link SystemEnvironmentTable} from configuration.
	 */
	@CalledByReflection
	public SystemEnvironmentTable(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	protected List<Row> rows() {
		Resources resources = Resources.getInstance();
		String systemProperties =
			resources.getString(I18NConstants.SYSTEM_ENVIRONMENT_SECTION_SYSTEM_PROPERTIES);
		String vmArguments =
			resources.getString(I18NConstants.SYSTEM_ENVIRONMENT_SECTION_VM_ARGUMENTS);
		String blocked = resources.getString(com.top_logic.layout.form.I18NConstants.BLOCKED_VALUE_TEXT);

		Predicate<String> protectedKeys = SystemEnvironmentBuilder.protectedNames();

		List<Row> rows = new ArrayList<>();

		Properties props = System.getProperties();
		for (String name : props.stringPropertyNames()) {
			rows.add(new Row(systemProperties, name, mask(protectedKeys, name, props.getProperty(name), blocked)));
		}
		List<Path> paths = FileManager.getInstance().getPaths();
		if (!paths.isEmpty()) {
			// A single row holding all (distinct) resource paths, rather than one row per path.
			String joined = paths.stream().map(Path::toString).distinct().collect(Collectors.joining(", "));
			rows.add(new Row(systemProperties, "tl.resource.path", joined));
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

		return rows;
	}

	/**
	 * Returns the value, or the {@code blocked} replacement if {@code key} matches a protected
	 * pattern.
	 */
	private static String mask(Predicate<String> protectedKeys, String key, String value, String blocked) {
		return protectedKeys.test(key) ? blocked : value;
	}
}
