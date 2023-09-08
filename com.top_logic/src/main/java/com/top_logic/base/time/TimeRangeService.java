/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.time;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.top_logic.basic.Logger;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.NamedConfigMandatory;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.module.ConfiguredManagedClass;
import com.top_logic.basic.module.TypedRuntimeModule;

/**
 * A service managing {@link TimeRangeIterator}s.
 *
 * @author <a href=mailto:Dmitry.Ivanizki@top-logic.com>Dmitry Ivanizki</a>
 */
public class TimeRangeService extends ConfiguredManagedClass<TimeRangeService.Config> {

	/**
	 * Configuration for {@link TimeRangeService}.
	 */
	public interface Config extends ConfiguredManagedClass.Config<TimeRangeService> {

		/** Getter for time ranges. */
		@Key(RangeConfig.NAME_ATTRIBUTE)
		Map<String, RangeConfig> getRanges();
	}

	/**
	 * Configuration for a time range.
	 */
	public interface RangeConfig extends NamedConfigMandatory {
		/**
		 * See {@link RangeConfig#getImplementation}.
		 */
		String IMPLEMENTATION = "implementation";

		/** Getter for {@link RangeConfig#IMPLEMENTATION}. */
		@Name(IMPLEMENTATION)
		@Mandatory
		Class<? extends TimeRangeIterator> getImplementation();
	}

	/** Configured {@link TimeRangeIterator}s by name. */
	private Map<String, Class<? extends TimeRangeIterator>> _timeRanges;
    
	/**
	 * Creates a new {@link TimeRangeService}.
	 */
	public TimeRangeService(InstantiationContext context, Config config) {
		super(context, config);
		initTimeRanges();
	}

	/**
	 * Setup the {@link TimeRangeIterator}s from the configuration.
	 */
	protected void initTimeRanges() {
		Map<String, RangeConfig> ranges = getRanges();
		if (ranges.isEmpty()) {
			Logger.warn("No Configuration for TimeRangeIterator", TimeRangeService.class);
            return;
        }
		_timeRanges = new HashMap<>();
		for (Entry<String, RangeConfig> entry : ranges.entrySet()) {
			RangeConfig range = entry.getValue();
			Class<? extends TimeRangeIterator> theClass = range.getImplementation();
			String name = range.getName();
			_timeRanges.put(name, theClass);
        }
		_timeRanges = Collections.unmodifiableMap(_timeRanges);
    }

	/**
	 * Getter for {@link TimeRangeIterator} by name.
	 */
	public Class<? extends TimeRangeIterator> getTimeRange(String name) {
		return getTimeRanges().get(name);
	}

	/**
	 * Return the configured {@link TimeRangeIterator}s.
	 */
	public Map<String, Class<? extends TimeRangeIterator>> getTimeRanges() {
		return _timeRanges;
	}

	/**
	 * Getter for {@link RangeConfig}s.
	 */
	public Map<String, RangeConfig> getRanges() {
		return getConfig().getRanges();
	}

	/**
	 * The currently active {@link TimeRangeService}.
	 */
	public static TimeRangeService getInstance() {
		return Module.INSTANCE.getImplementationInstance();
	}

	/**
	 * Module for instantiation of the {@link TimeRangeService}.
	 */
	public static class Module extends TypedRuntimeModule<TimeRangeService> {

		/** Singleton for this module. */
		public static final Module INSTANCE = new Module();

		private Module() {
			// Singleton constructor.
		}

		@Override
		public Class<TimeRangeService> getImplementation() {
			return TimeRangeService.class;
		}
	}
}
