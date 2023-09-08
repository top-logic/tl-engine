/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util.sched.layout.block;

import java.io.IOException;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.Renderer;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.provider.EnumLabelProvider;
import com.top_logic.util.sched.Scheduler;
import com.top_logic.util.sched.task.Task;

/**
 * Render an image depending on whether a {@link Task} and its children
 * {@link Scheduler#isTaskBlocked(Task) are blocked} by the scheduler or not.
 * 
 * @author <a href=mailto:Jan Stolzenburg@top-logic.com>Jan Stolzenburg</a>
 */
public class BlockStateRenderer implements Renderer<BlockState> {

	/**
	 * Singleton {@link BlockStateRenderer} instance.
	 */
	public static final BlockStateRenderer INSTANCE = new BlockStateRenderer();

	private BlockStateRenderer() {
		// Singleton constructor.
	}

	@Override
	public void write(DisplayContext displayContext, TagWriter out, BlockState value) throws IOException {
		BlockState blockState = value;
		ThemeImage icon = getIcon(blockState);
		if (icon == null) {
			return;
		}
		String label = EnumLabelProvider.INSTANCE.getLabel(blockState);
		icon.writeWithTooltip(displayContext, out, label);
	}

	private ThemeImage getIcon(BlockState blockState) {
		switch (blockState) {
			case BLOCKED:
				return Icons.PAUSED_RED;
			case CHILD_BLOCKED:
				return Icons.PAUSED_YELLOW;
			case NOT_BLOCKED:
				return null;
			default:
				throw new UnreachableAssertion("Unexpected enum value: "
					+ StringServices.getObjectDescription(blockState));
		}
	}

}
