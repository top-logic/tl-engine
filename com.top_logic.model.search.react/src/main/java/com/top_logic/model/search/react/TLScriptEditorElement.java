/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.react;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.ChannelRefFormat;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.channel.ViewChannel.ChannelListener;

/**
 * A {@link UIElement} that renders a <code>CodeMirror</code> 6-based TL-Script editor.
 *
 * <p>
 * Usage in {@code .view.xml}:
 * </p>
 *
 * <pre>
 * &lt;tlscript-editor value="x -&gt; $x + 1" /&gt;
 * </pre>
 *
 * <p>
 * With a {@link Config#getValueChannel() value channel}, the edited text is published to that
 * channel (and external channel writes update the editor), so a command can read the current script
 * — the basis of an interactive console.
 * </p>
 */
public class TLScriptEditorElement implements UIElement {

	/**
	 * Configuration for {@link TLScriptEditorElement}.
	 */
	@TagName("tlscript-editor")
	public interface Config extends UIElement.Config {

		@Override
		@ClassDefault(TLScriptEditorElement.class)
		Class<? extends UIElement> getImplementationClass();

		/** Configuration name for {@link #getValue()}. */
		String VALUE = "value";

		/** Configuration name for {@link #getValueChannel()}. */
		String VALUE_CHANNEL = "value-channel";

		/** Configuration name for {@link #getReadOnly()}. */
		String READ_ONLY = "readOnly";

		/**
		 * The initial TL-Script expression text.
		 *
		 * <p>
		 * Used as the initial value when no {@link #getValueChannel() value channel} is set or the
		 * channel is empty.
		 * </p>
		 */
		@Name(VALUE)
		String getValue();

		/**
		 * Channel the edited text is published to, and from which external writes update the editor.
		 */
		@Name(VALUE_CHANNEL)
		@Nullable
		@Format(ChannelRefFormat.class)
		ChannelRef getValueChannel();

		/**
		 * Whether the editor is read-only.
		 */
		@Name(READ_ONLY)
		@BooleanDefault(false)
		boolean getReadOnly();
	}

	private final String _value;

	private final ChannelRef _valueChannelRef;

	private final boolean _readOnly;

	/**
	 * Creates a new {@link TLScriptEditorElement} from configuration.
	 */
	@CalledByReflection
	public TLScriptEditorElement(InstantiationContext context, Config config) {
		_value = config.getValue();
		_valueChannelRef = config.getValueChannel();
		_readOnly = config.getReadOnly();
	}

	@Override
	public IReactControl createControl(ViewContext context) {
		ViewChannel channel = _valueChannelRef != null ? context.resolveChannel(_valueChannelRef) : null;

		String initial = _value;
		if (channel != null && channel.get() != null) {
			initial = channel.get().toString();
		}

		TLScriptEditorReactControl control = new TLScriptEditorReactControl(context, initial, _readOnly);

		if (channel != null) {
			control.setValueCallback(channel::set);
			ChannelListener listener =
				(sender, oldValue, newValue) -> control.setValue(newValue == null ? "" : newValue.toString());
			channel.addListener(listener);
			control.addCleanupAction(() -> channel.removeListener(listener));
		}

		return control;
	}
}
