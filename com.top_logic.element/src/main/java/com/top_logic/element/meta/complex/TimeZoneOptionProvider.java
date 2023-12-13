/*
 * Copyright (c) 2023 Business Operation Systems GmbH. All Rights Reserved.
 */
package com.top_logic.element.meta.complex;

import java.util.List;
import java.util.TimeZone;

import com.top_logic.basic.time.ConfiguredTimeZones;
import com.top_logic.basic.time.TimeZones;
import com.top_logic.element.meta.ListOptionProvider;
import com.top_logic.element.meta.form.EditContext;

/**
 * {@link ListOptionProvider} delivering all available {@link TimeZone}.
 * 
 * @see TimeZones
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TimeZoneOptionProvider implements ListOptionProvider {

	@Override
	public List<?> getOptionsList(EditContext editContext) {
		return ConfiguredTimeZones.INSTANCE.apply();
	}

}

