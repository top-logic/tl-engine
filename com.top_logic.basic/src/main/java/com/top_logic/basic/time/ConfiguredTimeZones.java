/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
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

