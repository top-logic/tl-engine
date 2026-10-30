/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util.sched.layout.active;

import java.io.IOException;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.Renderer;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.provider.EnumResourceProvider;
import com.top_logic.util.sched.ActiveState;

/**
 * {@link Renderer} writing the label of an {@link ActiveState}, preceded by an icon for an
 * inactive {@link com.top_logic.util.sched.task.Task} or subtask.
 * 
 * @author <a href=mailto:Jan Stolzenburg@top-logic.com>Jan Stolzenburg</a>
 */
public class ActiveStateRenderer implements Renderer<ActiveState> {

	/**
	 * Singleton {@link ActiveStateRenderer} instance.
	 */
	public static final ActiveStateRenderer INSTANCE = new ActiveStateRenderer();

	private ActiveStateRenderer() {
		// Singleton constructor.
	}

	@Override
	public void write(DisplayContext displayContext, TagWriter out, ActiveState value) throws IOException {
		if (value == null) {
			return;
		}
		String label = EnumResourceProvider.INSTANCE.getLabel(value);
		ThemeImage icon = getIcon(value);
		if (icon != null) {
			icon.writeWithTooltip(displayContext, out, label);
			out.writeText(" ");
		}
		out.writeText(label);
	}

	private ThemeImage getIcon(ActiveState state) {
		switch (state) {
			case INACTIVE_CONFIGURED:
			case INACTIVE_ADMINISTRATOR:
				return Icons.ACTIVE_STATE_INACTIVE;
			case CHILD_INACTIVE:
				return Icons.ACTIVE_STATE_CHILD_INACTIVE;
			case ACTIVE_CONFIGURED:
			case ACTIVE_ADMINISTRATOR:
				return null;
		}
		throw new IllegalArgumentException("Unexpected active state: " + state);
	}

}
