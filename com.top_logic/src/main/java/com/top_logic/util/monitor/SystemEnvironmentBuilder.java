/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util.monitor;

import static com.top_logic.layout.form.template.model.Templates.*;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.top_logic.basic.AliasManager;
import com.top_logic.basic.FileManager;
import com.top_logic.basic.col.factory.CollectionFactory;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.EntryTag;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.format.RegExpValueProvider;
import com.top_logic.html.template.HTMLTemplateFragment;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.template.model.Templates;
import com.top_logic.layout.table.ConfigKey;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.mig.html.ModelBuilder;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.util.TLContext;

/**
 * {@link ModelBuilder} creating a {@link FormContext} to display system environment values.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class SystemEnvironmentBuilder implements ModelBuilder {

	private static final Pattern SYS_PROP_ARG_PATTERN = Pattern.compile("-D([^=]+)=(.*)");

	/**
	 * Configuration of a {@link SystemEnvironmentBuilder}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface GlobalConfig extends ConfigurationItem {

		/**
		 * List of {@link Pattern} that an alias key must match, if its value must not be displayed.
		 * 
		 * <p>
		 * Aliases may hold informations that must not be displayed here, e.g. passwords. If an
		 * alias matches one of the returned pattern, then not the actual value but a replacement is
		 * displayed instead.
		 * </p>
		 */
		@DefaultContainer
		@EntryTag("protected-alias")
		List<PatternConfig> getProtectedAliases();

	}

	/**
	 * Configuration of a {@link Pattern}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface PatternConfig extends ConfigurationItem {

		/**
		 * {@link Pattern} that an alias key must match, if its value must not be displayed.
		 */
		@Format(RegExpValueProvider.class)
		@Mandatory
		Pattern getPattern();

	}

	@Override
	public Object getModel(Object businessModel, LayoutComponent aComponent) {
		FormContext context = new FormContext(aComponent);

		Templates.template(context, Templates.div(title(), div(createContents(context))));

		return context;
	}

	/**
	 * {@link Collator} to order strings in the locale of the current user.
	 */
	protected Collator newCollator() {
		Collator collator = Collator.getInstance(TLContext.getLocale());
		collator.setStrength(Collator.PRIMARY);
		return collator;
	}

	/**
	 * Creates the contents of the page.
	 * 
	 * @param context
	 *        Created {@link FormContext}.
	 */
	protected List<HTMLTemplateFragment> createContents(FormContext context) {
		List<Pattern> protectedValuesPattern =
			ApplicationConfig.getInstance().getConfig(GlobalConfig.class).getProtectedAliases()
				.stream()
				.map(PatternConfig::getPattern)
				.collect(Collectors.toList());
		Predicate<String> isProtected = s -> isProtectedValue(protectedValuesPattern, s);

		List<HTMLTemplateFragment> content = new ArrayList<>();
		content.add(classLoader());
		content.add(systemProperties(isProtected));
		content.add(vmArgs(isProtected));
		content.add(applicationProperties(isProtected));
		return content;
	}

	/**
	 * Title of the page.
	 */
	protected HTMLTemplateFragment title() {
		return empty();
	}

	/**
	 * Template displaying the configuration variables of the application.
	 * 
	 * @see AliasManager#getDefinedAliases()
	 */
	protected HTMLTemplateFragment applicationProperties(Predicate<String> isProtected) {
		Map<String, String> aliases = AliasManager.getInstance().getDefinedAliases();

		List<String> keys = new ArrayList<>(aliases.keySet());
		Collections.sort(keys, newCollator());

		List<HTMLTemplateFragment> tmp = new ArrayList<>();
		for (String key : keys) {
			String value = aliases.get(key);
			HTMLTemplateFragment content;
			if (isProtected.test(key)) {
				content = resource(com.top_logic.layout.form.I18NConstants.BLOCKED_VALUE_TEXT);
			} else {
				content = text(value);
			}
			tmp.add(descriptionBox(text(key), content, true));
		}
		return fieldsetBoxWrap(resource(I18NConstants.SYSTEM_ENVIRONMENT_APPLICATION_PROPERTIES), div(tmp),
			ConfigKey.none());
	}

	private boolean isProtectedValue(List<Pattern> protectedValues, String key) {
		return protectedValues.stream().anyMatch(pattern -> pattern.matcher(key).find());
	}

	/**
	 * {@link HTMLTemplateFragment} displaying the VM arguments.
	 * 
	 * @see RuntimeMXBean#getInputArguments()
	 */
	protected HTMLTemplateFragment vmArgs(Predicate<String> isProtected) {
		// input arguments are immutable.
		List<String> arguments = new ArrayList<>(ManagementFactory.getRuntimeMXBean().getInputArguments());
		Collections.sort(arguments, newCollator());
		// Get the URLs
		HTMLTemplateFragment[] vmArgs = arguments.stream()
			.map(arg -> protectArg(isProtected, arg))
			.map(Templates::div)
			.toArray(HTMLTemplateFragment[]::new);

		return fieldsetBoxWrap(resource(I18NConstants.SYSTEM_ENVIRONMENT_VM_ARGUMENTS), div(vmArgs), ConfigKey.none());
	}

	private HTMLTemplateFragment protectArg(Predicate<String> isProtected, String arg) {
		Matcher matcher = SYS_PROP_ARG_PATTERN.matcher(arg);
		if (matcher.matches()) {
			String key = matcher.group(1);
			if (isProtected.test(key)) {
				return span(text("-D" + key + "="),
					resource(com.top_logic.layout.form.I18NConstants.BLOCKED_VALUE_TEXT));
			}
		}
		return text(arg);
	}

	/**
	 * Template displaying the System Properties.
	 * 
	 * @see System#getProperties()
	 */
	protected HTMLTemplateFragment systemProperties(Predicate<String> isProtected) {
		Properties props = System.getProperties();
		List<String> propertyNames = CollectionFactory.list(props.stringPropertyNames());
		Collections.sort(propertyNames, newCollator());
		
		List<HTMLTemplateFragment> properties = propertyNames.stream()
			.map(prop -> templateForSystemProperty(isProtected, prop))
			.collect(Collectors.toList());

		List<Path> paths = FileManager.getInstance().getPaths();
		HTMLTemplateFragment resourcePath;
		if (paths.size() > 1) {
			resourcePath = Templates.tag(HTMLConstants.UL, paths.stream().map(
				p -> Templates.tag(HTMLConstants.LI, text(p.toString()))).collect(Collectors.toList()));
		} else {
			resourcePath = text(paths.isEmpty() ? "-" : paths.get(0).toString());
		}
		// Add a pseudo property from the file manager.
		properties.add(descriptionBox(text("tl.resource.path"), resourcePath, true));

		return fieldsetBoxWrap(resource(I18NConstants.SYSTEM_ENVIRONMENT_SYSTEM_PROPERTIES), div(properties),
			ConfigKey.none());
	}

	private HTMLTemplateFragment templateForSystemProperty(Predicate<String> isProtected, String propName) {
		HTMLTemplateFragment value =
			isProtected.test(propName) ? resource(com.top_logic.layout.form.I18NConstants.BLOCKED_VALUE_TEXT)
			: systemPropValue(propName);
		return descriptionBox(text(propName), value, true);
	}

	private HTMLTemplateFragment systemPropValue(String propName) {
		String value = System.getProperty(propName);
		if (propName.endsWith(".path")) {
			String[] split = value.split(Pattern.quote(File.pathSeparator));
			if (split.length > 1) {
				return Templates.tag(HTMLConstants.UL, Arrays.asList(split).stream()
					.map(s -> Templates.tag(HTMLConstants.LI, text(s))).collect(Collectors.toList()));
			}
		}
		return text(value);
	}

	/**
	 * Displays the system class loader.
	 * 
	 * @see ClassLoader#getSystemClassLoader()
	 */
	protected HTMLTemplateFragment classLoader() {
		// Get the System Classloader
		ClassLoader sysClassLoader = ClassLoader.getSystemClassLoader();
		if (sysClassLoader instanceof URLClassLoader) {
			// Get the URLs
			HTMLTemplateFragment[] urls = Arrays.stream(((URLClassLoader) sysClassLoader).getURLs())
				.map(URL::toString)
				.map(Templates::text)
				.map(Templates::div)
				.toArray(HTMLTemplateFragment[]::new);

			return fieldsetBoxWrap(resource(I18NConstants.SYSTEM_ENVIRONMENT_CLASS_LOADER), div(urls),
				ConfigKey.none());
		} else {
			return empty();
		}
	}

	@Override
	public boolean supportsModel(Object aModel, LayoutComponent aComponent) {
		return true;
	}

}

