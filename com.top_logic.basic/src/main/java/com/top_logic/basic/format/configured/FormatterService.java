/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.format.configured;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.col.MapUtil;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.basic.format.FormatConfig;
import com.top_logic.basic.format.FormatDefinition;
import com.top_logic.basic.format.configured.FormatterService.Config.FormatEntry;
import com.top_logic.basic.module.ConfiguredManagedClass;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.basic.util.ResKey;

/**
 * Service providing global formats referenced by other parts of the application.
 * 
 * <p>
 * E.g. format annotations to model elements can refere to formats defined here.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Label("Format configurations")
public final class FormatterService extends ConfiguredManagedClass<FormatterService.Config> {

	/**
	 * Configuration options for {@link Formatter}.
	 */
	public interface Config extends ConfiguredManagedClass.Config<FormatterService>, FormatConfig {
		/**
		 * {@link FormatDefinition}s indexed by their IDs.
		 */
		@Key(FormatEntry.ID_NAME)
		Map<String, FormatEntry> getFormats();

		/**
		 * Entry in the global list of application-wide defined formats.
		 */
		@DisplayOrder({
			FormatEntry.ID_NAME,
			FormatEntry.LABEL,
			FormatEntry.DEFINITION
		})
		interface FormatEntry extends ConfigurationItem {
			/** Name of the "id" property. */
			String ID_NAME = "id";

			/**
			 * @see #getLabel()
			 */
			String LABEL = "label";

			/**
			 * @see #getDefinition()
			 */
			String DEFINITION = "definition";

			/**
			 * The internal ID for referencing this format.
			 */
			@Name(ID_NAME)
			@Mandatory
			String getId();

			/**
			 * The name of the format displayed in format selectors.
			 */
			@Name(LABEL)
			ResKey getLabel();

			/**
			 * The format definition.
			 */
			@Name(DEFINITION)
			@Mandatory
			@DefaultContainer
			PolymorphicConfiguration<? extends FormatDefinition<?>> getDefinition();
		}
	}

	private static final class ZoneAndLocale {

		final TimeZone _zone;

		final Locale _locale;

		public ZoneAndLocale(TimeZone zone, Locale locale) {
			_zone = zone;
			_locale = locale;
		}

		/**
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + _locale.hashCode();
			result = prime * result + _zone.hashCode();
			return result;
		}

		/**
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			ZoneAndLocale other = (ZoneAndLocale) obj;
			if (!_locale.equals(other._locale))
				return false;
			if (!_zone.equals(other._zone))
				return false;
			return true;
		}

	}

	/** All instances mapped via their Locale per Thread */
	private final ThreadLocal<ConcurrentMap<ZoneAndLocale, Formatter>> _threadInstances =
		new ThreadLocal<>();

	private Map<String, FormatDefinition<?>> _formats;

	private Map<String, ResKey> _labels;

	/**
	 * Creates a {@link Formatter} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public FormatterService(InstantiationContext context, Config config) {
		super(context, config);

		_formats = new HashMap<>();
		_labels = new HashMap<>();
		for (Entry<String, FormatEntry> entry : config.getFormats().entrySet()) {
			_formats.put(entry.getKey(), context.getInstance(entry.getValue().getDefinition()));
			_labels.put(entry.getKey(), label(entry.getValue()));
		}
	}

	private static ResKey label(FormatEntry value) {
		ResKey result = value.getLabel();
		if (result != null) {
			return result;
		}
		return ResKey.text(value.getId());
	}

	/**
	 * A thread-local {@link Formatter} instance for the given {@link Locale}.
	 */
	public Formatter getThreadInstance(TimeZone timeZone, Locale locale) {
		ConcurrentMap<ZoneAndLocale, Formatter> formatterByLocale = _threadInstances.get();
		if (formatterByLocale == null) {
			formatterByLocale = new ConcurrentHashMap<>();
			_threadInstances.set(formatterByLocale);
		}

		ZoneAndLocale key = new ZoneAndLocale(timeZone, locale);
		Formatter instance = formatterByLocale.get(key);
		if (instance == null) {
			Formatter newInstance = createFormatter(timeZone, locale);
			instance = MapUtil.putIfAbsent(formatterByLocale, key, newInstance);
		}
		return instance;
	}

	/**
	 * Creates a concrete formatter instance.
	 * 
	 * @param timeZone
	 *        The {@link TimeZone} to use in created formatter.
	 * @param locale
	 *        The locale to use in the created formatter.
	 * 
	 * @return The new formatter.
	 */
	protected Formatter createFormatter(TimeZone timeZone, Locale locale) {
		return new Formatter(getConfig(), timeZone, locale, _formats);
	}

	/**
	 * Resolves the {@link FormatDefinition} with the given ID from the configuration.
	 */
	public FormatDefinition<?> getFormatDefinition(String id) {
		return _formats.get(id);
	}

	/**
	 * All defined {@link FormatDefinition} IDs.
	 * 
	 * @see #getFormatDefinition(String)
	 */
	public Set<String> getFormats() {
		return _formats.keySet();
	}

	/**
	 * The label for the format with the given ID.
	 * 
	 * @see #getFormatDefinition(String)
	 */
	public ResKey getLabel(String id) {
		return _labels.get(id);
	}

	/**
	 * Returns {@link Formatter} for users {@link TimeZone} and {@link Locale}.
	 *
	 * Creates a new {@link Formatter} if it doesn't exist yet.
	 */
	static public Formatter getFormatter() {
		return getFormatter(ThreadContext.getLocale());
	}

	/**
	 * Returns {@link Formatter} for users {@link TimeZone} and given {@link Locale}.
	 *
	 * Creates a new {@link Formatter} if it doesn't exist yet.
	 */
	static public Formatter getFormatter(Locale locale) {
		return getFormatter(ThreadContext.getTimeZone(), locale);
	}

	/**
	 * Returns {@link Formatter} for given {@link TimeZone} and {@link Locale}.
	 *
	 * Creates a new {@link Formatter} if it doesn't exist yet.
	 */
	static public Formatter getFormatter(TimeZone timeZone, Locale locale) {
		return getInstance().getThreadInstance(timeZone, locale);
	}

	/**
	 * The singleton {@link FormatterService} instance.
	 */
	public static FormatterService getInstance() {
		return Module.INSTANCE.getImplementationInstance();
	}

	/**
	 * Singleton reference to {@link Formatter}.
	 */
	public static class Module extends TypedRuntimeModule<FormatterService> {

		/**
		 * Singleton {@link Module} instance.
		 */
		public static final Module INSTANCE = new Module();

		private Module() {
			// Singleton constructor.
		}

		@Override
		public Class<FormatterService> getImplementation() {
			return FormatterService.class;
		}
	}

}