/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.editor;

import com.top_logic.basic.func.Function1;
import com.top_logic.layout.channel.linking.ref.ComponentRef;
import com.top_logic.layout.form.model.FieldMode;

/**
 * Function activating {@link com.top_logic.layout.channel.linking.Channel#getName()} field depending on
 * the existence of a {@link com.top_logic.layout.channel.linking.ref.ComponentRef}.
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
public class ChannelNameMode extends Function1<FieldMode, ComponentRef> {

	@Override
	public FieldMode apply(ComponentRef ref) {
		if (ref != null) {
			return FieldMode.ACTIVE;
		}

		return FieldMode.DISABLED;
	}
}
