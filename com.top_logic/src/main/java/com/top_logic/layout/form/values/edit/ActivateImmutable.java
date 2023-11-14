/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.values.edit;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.base.services.simpleajax.PropertyUpdate;
import com.top_logic.basic.col.MapUtil;
import com.top_logic.basic.listener.EventType.Bubble;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.AbstractVisibleControl;
import com.top_logic.layout.basic.ConstantDisplayValue;
import com.top_logic.layout.basic.ControlCommand;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.ImmutablePropertyListener;
import com.top_logic.layout.form.template.ControlProvider;
import com.top_logic.layout.form.template.DefaultFormFieldControlProvider;
import com.top_logic.layout.form.template.FormTemplateConstants;
import com.top_logic.mig.html.HTMLUtil;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * {@link ControlProvider} that wraps a {@link Control} around a {@link FormField} display that is
 * active only when the field is immutable and allows to switch back the field to editable state,
 * whenever it is clicked.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class ActivateImmutable implements ControlProvider {

	private final ControlProvider _impl;

	/**
	 * Singleton {@link ActivateImmutable} instance.
	 */
	public static final ActivateImmutable INSTANCE = new ActivateImmutable(DefaultFormFieldControlProvider.INSTANCE);

	/**
	 * Creates a {@link ActivateImmutable}.
	 * 
	 * @param impl
	 *        The {@link ControlProvider} that creates the wrapped control.
	 */
	public ActivateImmutable(ControlProvider impl) {
		_impl = impl;
	}

	@Override
	public Control createControl(Object model, String style) {
		Control display = _impl.createControl(model, FormTemplateConstants.STYLE_DIRECT_VALUE);
		return new ActivateImmutableControl((FormField) model, display);
	}

	@Override
	public HTMLFragment createFragment(Object model, String style) {
		HTMLFragment display = _impl.createFragment(model, FormTemplateConstants.STYLE_DIRECT_VALUE);
		return new ActivateImmutableControl((FormField) model, display);
	}

	static class ActivateImmutableControl extends AbstractVisibleControl implements ImmutablePropertyListener {

		private static final String ACTIVATABLE_CSS_CLASS = "activatable";

		private static final Map<String, ControlCommand> COMMANDS_BY_NAME = index(new OnClick());

		private final FormField _model;

		private final HTMLFragment _display;

		static class OnClick extends ControlCommand {

			private static final String ONCLICK_COMMAND = "onclick";

			OnClick() {
				super(ONCLICK_COMMAND);
			}

			@Override
			protected HandlerResult execute(DisplayContext commandContext, Control control,
					Map<String, Object> arguments) {
				return ((ActivateImmutableControl) control).onClick(commandContext);
			}

			@Override
			public ResKey getI18NKey() {
				return I18NConstants.ACTIVATE_IMMUTABLE;
			}

		}

		public ActivateImmutableControl(FormField model, HTMLFragment display) {
			super(COMMANDS_BY_NAME);

			_model = model;
			_display = display;
		}

		@Override
		public FormField getModel() {
			return _model;
		}

		private static Map<String, ControlCommand> index(ControlCommand... commands) {
			HashMap<String, ControlCommand> result = MapUtil.newMap(commands.length);
			for (ControlCommand command : commands) {
				result.put(command.getID(), command);
			}
			return Collections.unmodifiableMap(result);
		}

		/**
		 * {@link OnClick} action.
		 * 
		 * @param commandContext
		 *        The {@link DisplayContext} of the invocation.
		 */
		public HandlerResult onClick(DisplayContext commandContext) {
			_model.setImmutable(false);

			return HandlerResult.DEFAULT_RESULT;
		}

		@Override
		protected void attachRevalidated() {
			super.attachRevalidated();

			_model.addListener(FormMember.IMMUTABLE_PROPERTY, this);
		}

		@Override
		protected void detachInvalidated() {
			_model.removeListener(FormMember.IMMUTABLE_PROPERTY, this);

			super.detachInvalidated();
		}

		@Override
		public Bubble handleImmutableChanged(FormMember sender, Boolean oldValue, Boolean newValue) {
			if (sender != _model) {
				return Bubble.BUBBLE;
			}
			addUpdate(new PropertyUpdate(getID(), CLASS_PROPERTY, new ConstantDisplayValue(getCurrentCssClass())));
			return Bubble.BUBBLE;
		}

		@Override
		protected void internalWrite(DisplayContext context, TagWriter out) throws IOException {
			out.beginBeginTag(SPAN);
			writeControlAttributes(context, out);
			writeOnClick(out);
			out.endBeginTag();

			_display.write(context, out);

			out.endTag(SPAN);
		}

		@Override
		protected void writeControlClassesContent(Appendable out) throws IOException {
			super.writeControlClassesContent(out);
			HTMLUtil.appendCSSClass(out, getCurrentCssClass());
		}

		private void writeOnClick(TagWriter out) throws IOException {
			out.beginAttribute(ONCLICK_ATTR);
			out.append("if (BAL.DOM.containsClass(document.getElementById(");
			writeIdJsString(out);
			out.append("), '");
			out.append(ACTIVATABLE_CSS_CLASS);
			out.append("')) {services.ajax.execute('dispatchControlCommand', {controlCommand : '");
			out.append(OnClick.ONCLICK_COMMAND);
			out.append("', controlID : ");
			writeIdJsString(out);
			out.append("}, false, null); } return false;");
			out.endAttribute();
		}

		private String getCurrentCssClass() {
			return _model.isImmutable() ? ACTIVATABLE_CSS_CLASS : "";
		}

	}

}