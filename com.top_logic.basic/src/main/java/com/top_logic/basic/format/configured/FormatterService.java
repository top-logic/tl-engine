/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.format.configured;

import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.col.MapUtil;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.format.FormatDefinition;
import com.top_logic.basic.module.ConfiguredManagedClass;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.basic.thread.ThreadContext;

/**
 * The {@link Formatter} service implementation.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class FormatterService extends ConfiguredManagedClass<FormatterService.Config> {

	/**
	 * Configuration options for {@link Formatter}.
	 */
	public interface Config extends ConfiguredManagedClass.Config<FormatterService>, Formatter.Config {
		// Pure sum interface.
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
		return new Formatter(getConfig(), timeZone, locale);
	}

	/**
	 * Resolves the {@link FormatDefinition} with the given ID from the configuration.
	 */
	public FormatDefinition<?> getFormatDefinition(String id) {
		return getConfig().getFormats().get(id);
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