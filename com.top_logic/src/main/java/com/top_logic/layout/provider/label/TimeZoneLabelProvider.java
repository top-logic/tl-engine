/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.provider.label;

import java.util.TimeZone;

import com.top_logic.basic.time.TimeZones;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.LabelProvider;
import com.top_logic.util.Resources;
import com.top_logic.util.TLContext;

/**
 * {@link LabelProvider} for {@link TimeZone}s.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TimeZoneLabelProvider extends TypeSafeLabelProvider<TimeZone> {

	@Override
	protected Class<TimeZone> getObjectType() {
		return TimeZone.class;
	}

	@Override
	protected String getNonNullLabel(TimeZone object) {
		if (TimeZones.Module.INSTANCE.isActive()) {
			ResKey configuredKey = TimeZones.getInstance().getResKey(object);
			if (configuredKey != ResKey.NONE) {
				return Resources.getInstance().getString(configuredKey);
			}
		}
		return object.getDisplayName(TLContext.getLocale());
	}

}

