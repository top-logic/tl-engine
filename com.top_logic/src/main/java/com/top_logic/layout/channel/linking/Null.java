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
import com.top_logic.layout.channel.linking.impl.NullLinking;
import com.top_logic.layout.component.model.NullModelProvider;

/**
 * Short-cut for the {@link NullModelProvider}, see {@link Provider}.
 */
@TagName("null")
public interface Null extends ModelSpec {

	@Override
	@ClassDefault(NullLinking.class)
	Class<? extends ChannelLinking> getImplementationClass();

}