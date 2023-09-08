/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.editor;

import java.util.function.BiFunction;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.channel.linking.impl.AbstractTransformLinking;
import com.top_logic.layout.channel.linking.impl.ChannelLinking;
import com.top_logic.mig.html.layout.LayoutStorage;
import com.top_logic.mig.html.layout.TLLayout;

/**
 * {@link ChannelLinking} transforming a layout key to the actual {@link TLLayout}.
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
public class KeyToLayoutTransformLinking extends AbstractTransformLinking<AbstractTransformLinking.Config> {

	/**
	 * Creates a {@link KeyToLayoutTransformLinking}.
	 */
	public KeyToLayoutTransformLinking(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public void appendTo(StringBuilder result) {
		result.append("LayoutKeyToConfig(");
		input().appendTo(result);
		result.append(")");
	}

	@Override
	protected BiFunction<Object, Object, ?> transformation() {
		return (layoutKey, oldValue) -> {
			if (!StringServices.isEmpty(layoutKey)) {
				return LayoutStorage.getInstance().getLayout((String) layoutKey);
			}

			return null;
		};
	}

}
