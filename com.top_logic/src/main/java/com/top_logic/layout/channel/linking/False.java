/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.channel.linking;

import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.ModelSpec;
import com.top_logic.layout.channel.linking.impl.ChannelLinking;
import com.top_logic.layout.channel.linking.impl.FalseLinking;

/**
 * Short-cut for a {@link Provider} returning the constant <code>false</code>.
 */
@TagName("false")
public interface False extends ModelSpec {

	@Override
	@ClassDefault(FalseLinking.class)
	Class<? extends ChannelLinking> getImplementationClass();

}