/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.react;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;

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

		/** Configuration name for {@link #getReadOnly()}. */
		String READ_ONLY = "readOnly";

		/**
		 * The initial TL-Script expression text.
		 */
		@Name(VALUE)
		String getValue();

		/**
		 * Whether the editor is read-only.
		 */
		@Name(READ_ONLY)
		@BooleanDefault(false)
		boolean getReadOnly();
	}

	private final String _value;

	private final boolean _readOnly;

	/**
	 * Creates a new {@link TLScriptEditorElement} from configuration.
	 */
	@CalledByReflection
	public TLScriptEditorElement(InstantiationContext context, Config config) {
		_value = config.getValue();
		_readOnly = config.getReadOnly();
	}

	@Override
	public IReactControl createControl(ViewContext context) {
		return new TLScriptEditorReactControl(context, _value, _readOnly);
	}
}
