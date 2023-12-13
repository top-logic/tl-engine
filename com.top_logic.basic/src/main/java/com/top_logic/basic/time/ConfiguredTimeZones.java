/*
 * Copyright (c) 2023 Business Operation Systems GmbH. All Rights Reserved.
 */
package com.top_logic.basic.time;

import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import com.top_logic.basic.func.Function0;

/**
 * {@link Function0} delivering all configured {@link TimeZones#getTimeZones() time zones}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ConfiguredTimeZones extends Function0<List<TimeZone>> {

	/** Singleton {@link ConfiguredTimeZones} instance. */
	public static final ConfiguredTimeZones INSTANCE = new ConfiguredTimeZones();

	/**
	 * Creates a new {@link ConfiguredTimeZones}.
	 */
	protected ConfiguredTimeZones() {
		// singleton instance
	}

	@Override
	public List<TimeZone> apply() {
		return new ArrayList<>(TimeZones.getInstance().getTimeZones());
	}

}

