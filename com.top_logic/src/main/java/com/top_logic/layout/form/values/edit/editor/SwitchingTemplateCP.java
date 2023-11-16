/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.values.edit.editor;

import java.io.IOException;
import java.util.Objects;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.html.template.HTMLTemplateFragment;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.AbstractConstantControlBase;
import com.top_logic.layout.form.template.ControlProvider;
import com.top_logic.layout.form.template.DefaultFormFieldControlProvider;
import com.top_logic.layout.form.template.model.internal.TemplateControlProvider;
import com.top_logic.layout.form.values.ListenerBinding;
import com.top_logic.layout.form.values.Value;

/**
 * {@link ControlProvider} creating a {@link Control} that delegates to different {@link Control}s
 * based on a given {@link Value}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class SwitchingTemplateCP implements ControlProvider {

	private final Value<Boolean> _value;

	private ControlProvider _trueCP;

	private ControlProvider _falseCP;

	private ListenerBinding _binding = ListenerBinding.NONE;

	/**
	 * Creates a {@link SwitchingTemplateCP}.
	 * 
	 * @param value
	 *        {@link Value} determining the actual content.
	 * @param trueCP
	 *        {@link ControlProvider} creating the control that is rendered when the given value is
	 *        <code>true</code>.
	 * @param falseCP
	 *        {@link ControlProvider} creating the control that is rendered when the given value is
	 *        <code>false</code>.
	 */
	public SwitchingTemplateCP(Value<Boolean> value, ControlProvider trueCP,
			ControlProvider falseCP) {
		_value = value;
		_trueCP = trueCP;
		_falseCP = falseCP;
	}
	
	/**
	 * Creates a {@link SwitchingTemplateCP} using {@link ControlProvider} creating for the given
	 * templates.
	 * 
	 * @see SwitchingTemplateCP#SwitchingTemplateCP(Value, ControlProvider, ControlProvider)
	 */
	public SwitchingTemplateCP(Value<Boolean> value, HTMLTemplateFragment trueFragment,
			HTMLTemplateFragment falseFragment) {
		this(value,
			new TemplateControlProvider(trueFragment, DefaultFormFieldControlProvider.INSTANCE),
			new TemplateControlProvider(falseFragment, DefaultFormFieldControlProvider.INSTANCE));
	}

	@Override
	public Control createControl(Object model, String style) {
		_binding.close();
		Control trueControl = _trueCP.createControl(model, style);
		Control falseControl = _falseCP.createControl(model, style);
		Control delegate;
		if (_value.get()) {
			delegate = trueControl;
		} else {
			delegate = falseControl;
		}
		ProxyControl proxy = new ProxyControl(delegate);
		ListenerBinding listeningBinding = _value.addListener(sender -> {
			Control newDelegate;
			if (_value.get()) {
				newDelegate = trueControl;
			} else {
				newDelegate = falseControl;
			}
			proxy.setDelegate(newDelegate);
		});
		_binding = () -> {
			listeningBinding.close();
			// Control does not longer react on value changes. For safety reason ensure that it is
			// not longer rendered.
			proxy.detach();
		};
		return proxy;
	}

	/**
	 * Control that renders a root tag and delegates the content to a given other {@link Control}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static class ProxyControl extends AbstractConstantControlBase {

		private Control _delegate;

		private String _rootTag = DIV;

		/**
		 * Creates a {@link ProxyControl}.
		 */
		public ProxyControl(Control delegate) {
			setDelegate(delegate);
		}

		@Override
		protected void internalWrite(DisplayContext context, TagWriter out) throws IOException {
			out.beginBeginTag(_rootTag);
			writeControlAttributes(context, out);
			out.endBeginTag();
			_delegate.write(context, out);
			out.endTag(_rootTag);
		}

		/**
		 * Sets the root tag for this control.
		 */
		public void setRootTag(String tag) {
			requestRepaint();
			_rootTag = Objects.requireNonNull(tag);
		}

		/**
		 * Sets the {@link Control} to render.
		 */
		public void setDelegate(Control delegate) {
			requestRepaint();
			_delegate = Objects.requireNonNull(delegate);
		}

		@Override
		protected void detachInvalidated() {
			super.detachInvalidated();
			_delegate.detach();
		}

		@Override
		public Control getModel() {
			return _delegate;
		}

		@Override
		public boolean isVisible() {
			return getModel().isVisible();
		}

	}

}
