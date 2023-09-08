/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.time;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

import com.top_logic.basic.ArrayUtil;
import com.top_logic.basic.Log;
import com.top_logic.basic.col.MapUtil;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.NamedConfigMandatory;
import com.top_logic.basic.config.TimeZoneValueProvider;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.module.ManagedClass;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.basic.util.ResKey;

/**
 * Module handling time zones in <i>TopLogic</i>
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TimeZones extends ManagedClass {

	/**
	 * {@link TimeZone} UTC without any offset, and without daylight saving.
	 */
	public static final TimeZone UTC;

	static {
		if (Arrays.asList(TimeZone.getAvailableIDs()).contains("UTC")) {
			// Use existing TimeZone
			UTC = TimeZone.getTimeZone("UTC");
		} else if (Arrays.asList(TimeZone.getAvailableIDs()).contains("GMT")) {
			// GMT is same as UTC but legacy
			UTC = TimeZone.getTimeZone("GMT");
		} else {
			// Create TimeZone without offset.
			UTC = new SimpleTimeZone(0, "UTC");
		}
	}

	/**
	 * Configuration of {@link TimeZones}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends ServiceConfiguration<TimeZones> {

		/** @see #getDefaultTimeZone() */
		String DEFAULT_TIME_ZONE_ATTRIBUTE = "default-time-zone";

		/** @see #getSystemTimeZone() */
		String SYSTEM_TIME_ZONE_ATTRIBUTE = "system-time-zone";

		/**
		 * The {@link TimeZone}s known by the system.
		 */
		@Key(TimeZoneConfig.NAME_ATTRIBUTE)
		List<TimeZoneConfig> getTimeZones();

		/**
		 * {@link TimeZone} a newly created user has.
		 * 
		 * <p>
		 * The default {@link TimeZone} must be one of the configured.
		 * </p>
		 * 
		 * @see #getTimeZones()
		 */
		@Format(TimeZoneValueProvider.class)
		@Name(DEFAULT_TIME_ZONE_ATTRIBUTE)
		@Mandatory
		TimeZone getDefaultTimeZone();

		/**
		 * {@link TimeZone} in which the application stores and display dates.
		 * 
		 * <p>
		 * The system {@link TimeZone} must be one of the configured.
		 * </p>
		 * 
		 * @see #getTimeZones()
		 */
		@Format(TimeZoneValueProvider.class)
		@Name(SYSTEM_TIME_ZONE_ATTRIBUTE)
		@Mandatory
		TimeZone getSystemTimeZone();

	}

	/**
	 * Configuration of the a {@link TimeZone}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface TimeZoneConfig extends NamedConfigMandatory {

		/**
		 * {@link TimeZone#getID() ID} of the configured {@link TimeZone}.
		 */
		@Override
		String getName();

		/**
		 * {@link ResKey} for the label of the configured {@link TimeZone}.
		 */
		@Mandatory
		ResKey getResKey();

	}

	private final Map<TimeZone, ResKey> _timeZones;

	private final TimeZone _defaultTimeZone;

	private final TimeZone _systemTimeZone;

	/**
	 * Creates a new {@link TimeZones} from the given configuration.
	 * 
	 * @param context
	 *        {@link InstantiationContext} to instantiate sub configurations.
	 * @param config
	 *        Configuration for this {@link TimeZones}.
	 */
	public TimeZones(InstantiationContext context, Config config) {
		super(context, config);
		_timeZones = Collections.unmodifiableMap(createTimeZones(context, config.getTimeZones()));
		_defaultTimeZone = config.getDefaultTimeZone();
		if (!_timeZones.keySet().contains(_defaultTimeZone)) {
			context.error("Unknown default time zone: " + config.getDefaultTimeZone().getID());
		}
		_systemTimeZone = config.getSystemTimeZone();
		if (!_timeZones.keySet().contains(_systemTimeZone)) {
			context.error("Unknown system time zone: " + config.getDefaultTimeZone().getID());
		}
	}

	private Map<TimeZone, ResKey> createTimeZones(InstantiationContext context, List<TimeZoneConfig> timeZones) {
		Map<TimeZone, ResKey> result = MapUtil.newLinkedMap(timeZones.size());
		for (TimeZoneConfig config : timeZones) {
			addTimeZone(context, result, config);
		}
		return result;
	}

	private void addTimeZone(InstantiationContext context, Map<TimeZone, ResKey> result, TimeZoneConfig config) {
		String name = config.getName();
		if (!ArrayUtil.contains(TimeZone.getAvailableIDs(), name)) {
			// Note: Do not prevent the application from starting by creating an error because the
			// number of available time zones differ from Java version to Java version.
			context.info("Unknown TimeZone ID: " + name, Log.WARN);
			return;
		}
		result.put(TimeZone.getTimeZone(name), config.getResKey());
	}

	/**
	 * A {@link ResKey} for the given {@link TimeZone} or {@link ResKey#NONE} when no key
	 *         can be found.
	 */
	public ResKey getResKey(TimeZone timeZones) {
		ResKey key = _timeZones.get(timeZones);
		if (key == null) {
			key = ResKey.NONE;
		}
		return key;
	}

	/**
	 * All {@link TimeZone}s known by the system.
	 */
	public Collection<TimeZone> getTimeZones() {
		return _timeZones.keySet();
	}

	/**
	 * The default {@link TimeZone} for all users.
	 */
	public TimeZone getDefaultTimeZone() {
		return _defaultTimeZone;
	}

	/**
	 * The {@link TimeZone} which is used by the application to display and store date.
	 */
	public TimeZone getSystemTimeZone() {
		return _systemTimeZone;
	}

	/**
	 * {@link TimeZone} of the system which is used for all {@link Calendar} for {@link Date}s which
	 * should be same displayed for all users around the world.
	 */
	public static TimeZone systemTimeZone() {
		if (TimeZones.Module.INSTANCE.isActive()) {
			return getInstance().getSystemTimeZone();
		}
		return TimeZone.getDefault();
	}

	/**
	 * The {@link TimeZone} of the application.
	 * 
	 * @return Either the configured {@link TimeZone} or {@link TimeZone} of the server.
	 * 
	 * @see TimeZone#getDefault()
	 */
	public static TimeZone defaultUserTimeZone() {
		if (Module.INSTANCE.isActive()) {
			return getInstance().getDefaultTimeZone();
		} else {
			return TimeZone.getDefault();
		}
		
	}

	/**
	 * Access to the singleton instance of {@link TimeZones}.
	 */
	public static TimeZones getInstance() {
		return Module.INSTANCE.getImplementationInstance();
	}

	/**
	 * Module for the {@link TimeZones} service.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static final class Module extends TypedRuntimeModule<TimeZones> {

		/** Singleton {@link TimeZones.Module} instance. */
		public static final Module INSTANCE = new Module();

		private Module() {
			// singleton instance
		}

		@Override
		public Class<TimeZones> getImplementation() {
			return TimeZones.class;
		}

	}

}

