/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.values.edit;

import java.util.Arrays;
import java.util.List;

import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.func.Function0;
import com.top_logic.layout.ModelSpec;
import com.top_logic.layout.channel.linking.Channel;
import com.top_logic.layout.channel.linking.ref.NamedComponent;

/**
 * Specialized {@link ModelSpec} options for dynamic component creation.
 * 
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class DefaultModelSpecOptions extends Function0<List<ModelSpec>> {

	@Override
	public List<ModelSpec> apply() {
		Channel channelModelSpec = TypedConfiguration.newConfigItem(Channel.class);
		channelModelSpec.setComponentRef(TypedConfiguration.newConfigItem(NamedComponent.class));

		return Arrays.asList(channelModelSpec);
	}

}
