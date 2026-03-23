/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.view;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.ChannelRefFormat;
import com.top_logic.layout.view.channel.ViewChannel;

/**
 * Demo {@link UIElement} that displays a numbered list of entries based on a numeric channel value.
 *
 * <p>
 * Reads a {@link Number} from the configured input channel and renders "Wert 1" through "Wert N"
 * in a table via the {@code TLValueList} React component.
 * </p>
 */
public class DemoValueListElement implements UIElement {

	/**
	 * Configuration for {@link DemoValueListElement}.
	 */
	@TagName("demo-value-list")
	public interface Config extends UIElement.Config {

		@Override
		@ClassDefault(DemoValueListElement.class)
		Class<? extends UIElement> getImplementationClass();

		/** Configuration name for {@link #getInput()}. */
		String INPUT = "input";

		/** Configuration name for {@link #getTitle()}. */
		String TITLE = "title";

		/**
		 * Channel reference providing the numeric value (number of entries to display).
		 */
		@Mandatory
		@Name(INPUT)
		@Format(ChannelRefFormat.class)
		ChannelRef getInput();

		/**
		 * Optional title displayed above the table.
		 */
		@Name(TITLE)
		@Nullable
		String getTitle();
	}

	private final ChannelRef _input;

	private final String _title;

	/**
	 * Creates a new {@link DemoValueListElement}.
	 */
	@CalledByReflection
	public DemoValueListElement(InstantiationContext context, Config config) {
		_input = config.getInput();
		_title = config.getTitle();
	}

	@Override
	public IReactControl createControl(ViewContext context) {
		ViewChannel channel = context.resolveChannel(_input);
		Object value = channel.get();

		int count = 0;
		if (value instanceof Number) {
			count = ((Number) value).intValue();
		}

		ValueListControl control = new ValueListControl(context, count, _title);

		// Update when channel changes.
		ViewChannel.ChannelListener listener = (sender, oldVal, newVal) -> {
			int newCount = newVal instanceof Number ? ((Number) newVal).intValue() : 0;
			control.patchReactState(java.util.Collections.singletonMap("count", newCount));
		};
		channel.addListener(listener);
		control.addCleanupAction(() -> channel.removeListener(listener));

		return control;
	}

	/**
	 * {@link ReactControl} subclass that initializes the TLValueList state.
	 */
	private static class ValueListControl extends ReactControl {

		ValueListControl(ViewContext context, int count, String title) {
			super(context, null, "TLValueList");
			putState("count", count);
			if (title != null) {
				putState("title", title);
			}
		}
	}
}
