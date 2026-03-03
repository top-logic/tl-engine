/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.form;

import java.io.IOException;
import java.util.Map;

import com.top_logic.basic.listener.EventType.Bubble;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.FrameScope;
import com.top_logic.layout.VetoException;
import com.top_logic.layout.basic.ControlCommand;
import com.top_logic.layout.form.DisabledPropertyListener;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.HasErrorChanged;
import com.top_logic.layout.form.ImmutablePropertyListener;
import com.top_logic.layout.form.MandatoryChangedListener;
import com.top_logic.layout.form.ValueListener;
import com.top_logic.layout.form.model.FormFieldInternals;
import com.top_logic.layout.form.model.VisibilityModel;
import com.top_logic.layout.react.I18NConstants;
import com.top_logic.layout.react.ReactControl;
import com.top_logic.layout.react.SSEUpdateQueue;
import com.top_logic.mig.html.layout.VisibilityListener;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.error.TopLogicException;

import de.haumacher.msgbuf.json.JsonWriter;

/**
 * A form field control that renders via a React component.
 *
 * <p>
 * Extends {@link ReactControl} so it can be composed with other React controls (e.g. as a child of
 * {@link com.top_logic.layout.react.control.layout.ReactFormFieldChromeControl}). Listens to
 * {@link FormField} property changes and delivers incremental patches via SSE.
 * </p>
 *
 * <p>
 * On initial render, the full field state (value, editable, disabled, mandatory, errors, label,
 * tooltip) is sent as JSON. Subsequent field changes are delivered as incremental patches via SSE.
 * </p>
 */
public class ReactFormFieldControl extends ReactControl
		implements MandatoryChangedListener, HasErrorChanged, ImmutablePropertyListener,
		DisabledPropertyListener, VisibilityListener, ValueListener {

	/** State key for the field value. */
	protected static final String VALUE = "value";

	/** State key for whether the field is editable. */
	protected static final String EDITABLE = "editable";

	/** State key for whether the field is disabled. */
	protected static final String DISABLED = "disabled";

	/** State key for whether the field is mandatory. */
	protected static final String MANDATORY = "mandatory";

	/** State key for whether the field has a validation error. */
	protected static final String HAS_ERROR = "hasError";

	/** State key for the error message text. */
	protected static final String ERROR_MESSAGE = "errorMessage";

	/** State key for the field label. */
	protected static final String LABEL = "label";

	/** State key for the tooltip text. */
	protected static final String TOOLTIP = "tooltip";

	/** State key for whether the control is hidden on the client. */
	protected static final String HIDDEN = "hidden";

	private static final String VALUE_CHANGED_COMMAND = "valueChanged";

	private static final Map<String, ControlCommand> COMMANDS =
		createCommandMap(new ControlCommand[] { ValueChanged.INSTANCE });

	private boolean _hidden;

	private boolean _listenersRegistered;

	/**
	 * Creates a new {@link ReactFormFieldControl}.
	 *
	 * @param model
	 *        The form field.
	 * @param reactModule
	 *        The React module identifier (e.g. "TLTextInput").
	 */
	public ReactFormFieldControl(FormField model, String reactModule) {
		super(model, reactModule, COMMANDS);
		initFieldState(model);
	}

	/**
	 * Populates the initial React state from the given {@link FormField}.
	 *
	 * <p>
	 * Called from the constructor before the control is attached to any SSE queue, so all
	 * {@link #putState} calls simply store values in the pre-render state map.
	 * </p>
	 */
	private void initFieldState(FormField field) {
		putState(VALUE, field.hasValue() ? field.getValue() : null);
		putState(EDITABLE, !field.isImmutable());
		putState(DISABLED, field.isDisabled());
		putState(MANDATORY, field.isMandatory());
		putState(HAS_ERROR, field.hasError());
		putState(ERROR_MESSAGE, field.hasError() ? field.getError().toString() : null);
		putState(LABEL, field.hasLabel() ? field.getLabel() : null);
		putState(TOOLTIP, field.getTooltip() != null ? field.getTooltip().toString() : null);
		putState(HIDDEN, Boolean.valueOf(isEffectivelyHidden()));
	}

	/**
	 * Returns the form field model.
	 */
	public FormField getFieldModel() {
		return (FormField) getModel();
	}

	/**
	 * Sets whether this control is hidden on the client.
	 *
	 * <p>
	 * When hidden, the mount-point element gets {@code display:none} applied by the bridge,
	 * preserving the React component tree and its local state.
	 * </p>
	 *
	 * @param hidden
	 *        {@code true} to hide, {@code false} to show.
	 */
	@Override
	public void setHidden(boolean hidden) {
		if (_hidden == hidden) {
			return;
		}
		_hidden = hidden;
		putState(HIDDEN, Boolean.valueOf(isEffectivelyHidden()));
	}

	/**
	 * Whether this control is currently hidden on the client.
	 */
	public boolean isHidden() {
		return _hidden;
	}

	/**
	 * Whether this control is effectively hidden, considering both the explicit hidden flag and the
	 * field's visibility.
	 */
	private boolean isEffectivelyHidden() {
		return _hidden || !getFieldModel().isVisible();
	}

	@Override
	protected void internalWrite(DisplayContext context, TagWriter out) throws IOException {
		registerFieldListeners();
		super.internalWrite(context, out);
	}

	@Override
	protected void writeAsChild(JsonWriter writer, FrameScope frameScope, SSEUpdateQueue queue)
			throws IOException {
		registerFieldListeners();
		super.writeAsChild(writer, frameScope, queue);
	}

	@Override
	protected void cleanupChildren() {
		deregisterFieldListeners();
		super.cleanupChildren();
	}

	/**
	 * Registers this control as a listener on the {@link FormField} model.
	 */
	private void registerFieldListeners() {
		if (_listenersRegistered) {
			return;
		}
		_listenersRegistered = true;

		FormField field = getFieldModel();
		field.addListener(FormField.HAS_ERROR_PROPERTY, (HasErrorChanged) this);
		field.addListener(FormField.IMMUTABLE_PROPERTY, (ImmutablePropertyListener) this);
		field.addListener(FormField.DISABLED_PROPERTY, (DisabledPropertyListener) this);
		field.addListener(FormField.MANDATORY_PROPERTY, (MandatoryChangedListener) this);
		field.addListener(VisibilityModel.VISIBLE_PROPERTY, (VisibilityListener) this);
		field.addValueListener(this);
	}

	/**
	 * Deregisters this control as a listener from the {@link FormField} model.
	 */
	private void deregisterFieldListeners() {
		if (!_listenersRegistered) {
			return;
		}
		_listenersRegistered = false;

		FormField field = getFieldModel();
		field.removeValueListener(this);
		field.removeListener(VisibilityModel.VISIBLE_PROPERTY, (VisibilityListener) this);
		field.removeListener(FormField.MANDATORY_PROPERTY, (MandatoryChangedListener) this);
		field.removeListener(FormField.DISABLED_PROPERTY, (DisabledPropertyListener) this);
		field.removeListener(FormField.IMMUTABLE_PROPERTY, (ImmutablePropertyListener) this);
		field.removeListener(FormField.HAS_ERROR_PROPERTY, (HasErrorChanged) this);
	}

	/**
	 * Whether the given sender is not the model of this control.
	 */
	protected final boolean skipEvent(Object sender) {
		return sender != getModel();
	}

	// -- FormField listener callbacks --

	@Override
	public void valueChanged(FormField field, Object oldValue, Object newValue) {
		if (!skipEvent(field)) {
			putState(VALUE, field.hasValue() ? field.getValue() : null);
		}
	}

	@Override
	public Bubble handleDisabledChanged(FormMember sender, Boolean oldValue, Boolean newValue) {
		if (!skipEvent(sender)) {
			putState(DISABLED, newValue);
		}
		return Bubble.BUBBLE;
	}

	@Override
	public Bubble handleMandatoryChanged(FormField sender, Boolean oldValue, Boolean newValue) {
		if (!skipEvent(sender)) {
			putState(MANDATORY, newValue);
		}
		return Bubble.BUBBLE;
	}

	@Override
	public Bubble hasErrorChanged(FormField sender, Boolean oldError, Boolean newError) {
		if (!skipEvent(sender)) {
			putState(HAS_ERROR, newError);
			putState(ERROR_MESSAGE, Boolean.TRUE.equals(newError) ? sender.getError().toString() : null);
		}
		return Bubble.BUBBLE;
	}

	@Override
	public Bubble handleImmutableChanged(FormMember sender, Boolean oldValue, Boolean newValue) {
		if (!skipEvent(sender)) {
			putState(EDITABLE, !Boolean.TRUE.equals(newValue));
		}
		return Bubble.BUBBLE;
	}

	@Override
	public Bubble handleVisibilityChange(Object sender, Boolean oldVisibility, Boolean newVisibility) {
		if (!skipEvent(sender)) {
			putState(HIDDEN, Boolean.valueOf(isEffectivelyHidden()));
		}
		return Bubble.BUBBLE;
	}

	/**
	 * Command that handles value changes from the React client.
	 */
	public static class ValueChanged extends ControlCommand {

		/** Singleton instance. */
		public static final ValueChanged INSTANCE = new ValueChanged();

		private ValueChanged() {
			super(VALUE_CHANGED_COMMAND);
		}

		@Override
		public com.top_logic.basic.util.ResKey getI18NKey() {
			return I18NConstants.REACT_VALUE_CHANGED;
		}

		@Override
		protected HandlerResult execute(DisplayContext commandContext,
				Control control, Map<String, Object> arguments) {
			ReactFormFieldControl reactControl = (ReactFormFieldControl) control;
			FormField field = reactControl.getFieldModel();
			Object newValue = arguments.get(VALUE);

			// Form fields expect raw string input (as from HTML forms). The React client sends
			// JSON-typed values (Integer, Boolean, etc.), so convert to String for parsing.
			String rawValue = newValue != null ? newValue.toString() : null;

			try {
				FormFieldInternals.updateFieldNoClientUpdate(field, rawValue);
			} catch (VetoException ex) {
				throw new TopLogicException(I18NConstants.ERROR_COMMAND_FAILED__MSG.fill(ex.getMessage()), ex);
			}

			return HandlerResult.DEFAULT_RESULT;
		}

	}

}
