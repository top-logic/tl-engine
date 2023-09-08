/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.control;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import com.top_logic.base.services.simpleajax.PropertyUpdate;
import com.top_logic.basic.Logger;
import com.top_logic.basic.col.ComparableComparator;
import com.top_logic.basic.listener.EventType.Bubble;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.Utils;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.KeyEvent;
import com.top_logic.layout.KeyEventDispatcher;
import com.top_logic.layout.KeyEventListener;
import com.top_logic.layout.KeySelector;
import com.top_logic.layout.VetoException;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.basic.ConstantDisplayValue;
import com.top_logic.layout.basic.ControlCommand;
import com.top_logic.layout.basic.FocusHandling;
import com.top_logic.layout.basic.Focusable;
import com.top_logic.layout.basic.KeyCodeHandler;
import com.top_logic.layout.form.BlockedStateChangedListener;
import com.top_logic.layout.form.CheckException;
import com.top_logic.layout.form.DisabledPropertyListener;
import com.top_logic.layout.form.FormConstants;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.HasErrorChanged;
import com.top_logic.layout.form.HasWarningsChangedListener;
import com.top_logic.layout.form.ImmutablePropertyListener;
import com.top_logic.layout.form.MandatoryChangedListener;
import com.top_logic.layout.form.RawValueListener;
import com.top_logic.layout.form.UINonBlocking;
import com.top_logic.layout.form.ValueListener;
import com.top_logic.layout.form.model.AbstractFormField;
import com.top_logic.layout.form.model.FormFieldInternals;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Base class for {@link com.top_logic.layout.Control} implementations that
 * display a single {@link FormField} in the GUI.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractFormFieldControlBase extends AbstractFormMemberControl implements KeyEventDispatcher,
		Comparable, MandatoryChangedListener, HasErrorChanged, HasWarningsChangedListener, ImmutablePropertyListener,
		DisabledPropertyListener, RawValueListener, ValueListener, BlockedStateChangedListener,
		Focusable.FocusRequestedListener {

	protected static final Map<String, ControlCommand> COMMANDS = createCommandMap(
		AbstractFormMemberControl.COMMANDS,
		new ControlCommand[] { ValueChanged.INSTANCE, KeyCodeHandler.INSTANCE, FieldInspector.INSTANCE });

	/**
	 * The css-style to be applied to the input-element.
	 */
    private String inputStyle;

	/**
	 * The last raw field value that was displayed in the UI.
	 * 
	 * <p>
	 * This compare value is used to prevent the {@link ValueListener} implementation from mirroring
	 * value changes back to the UI, if the value change was initiated from the client-side instance
	 * of this control. Note: There may be several input controls for the same field model.
	 * </p>
	 */
	private Object _lastRawValue;

	public AbstractFormFieldControlBase(FormField model, Map<String, ControlCommand> commandsByName) {
    	super(model, commandsByName);
    }

	protected AbstractFormFieldControlBase(FormField model) {
		this(model, COMMANDS);
	}

	/**
	 * Service method to add an property update for the
	 * {@link HTMLConstants#DISABLED_ATTR disabled} attribute and update the CSS
	 * classes.
	 * 
	 * @param disabled
	 *        whether the field must be disabled.
	 */
	protected final void addDisabledUpdate(boolean disabled) {
		updateCss();
		addUpdate(new PropertyUpdate(getInputId(), DISABLED_ATTR, ConstantDisplayValue.valueOf(disabled)));
	}

	@Override
	protected void registerListener(FormMember member) {
		super.registerListener(member);
		member.addListener(FormField.HAS_ERROR_PROPERTY, this);
		member.addListener(FormField.HAS_WARNINGS_PROPERTY, this);
		member.addListener(FormField.IMMUTABLE_PROPERTY, this);
		member.addListener(FormField.DISABLED_PROPERTY, this);
		member.addListener(FormField.VALUE_PROPERTY, this);
		member.addListener(FormField.MANDATORY_PROPERTY, this);
		member.addListener(FormField.BLOCKED_PROPERTY, this);
		member.addListener(FormField.FOCUS_PROPERTY, this);
		((FormField) member).addValueListener(this);
	}

	@Override
	protected void deregisterListener(FormMember member) {
		((FormField) member).removeValueListener(this);
		member.removeListener(FormField.FOCUS_PROPERTY, this);
		member.removeListener(FormField.BLOCKED_PROPERTY, this);
		member.removeListener(FormField.MANDATORY_PROPERTY, this);
		member.removeListener(FormField.VALUE_PROPERTY, this);
		member.removeListener(FormField.DISABLED_PROPERTY, this);
		member.removeListener(FormField.IMMUTABLE_PROPERTY, this);
		member.removeListener(FormField.HAS_WARNINGS_PROPERTY, this);
		member.removeListener(FormField.HAS_ERROR_PROPERTY, this);
		super.deregisterListener(member);
	}

	/**
     * @see #getModel()
     */
    public FormField getFieldModel() {
        return (FormField) getModel();
    }

	@Override
	public HandlerResult dispatchKeyEvent(DisplayContext commandContext, KeyEvent event) {
		return ((AbstractFormField) getModel()).dispatchKeyEvent(commandContext, event);
	}

	@Override
	protected void writeControlClassesContent(Appendable out) throws IOException {
		super.writeControlClassesContent(out);
		appendFieldControlClasses(out, getFieldModel());
	}

	public static void appendFieldControlClasses(Appendable out, FormField field) throws IOException {
		if (field.hasError()) {
			out.append(FormConstants.ERROR_CSS_CLASS);
		} else if (field.hasWarnings()) {
			out.append(FormConstants.WARNING_CSS_CLASS);
		} else {
			out.append(FormConstants.NO_ERROR_CSS_CLASS);
		}

		if (field.isMandatory() && field.isActive()) {
			out.append(FormConstants.MANDATORY_CSS_CLASS);
		}
		if (field.isVisible()) {
			if (field.isBlocked()) {
				// blocked fields are also immutable so no else case
				out.append(FormConstants.IS_BLOCKED_CSS_CLASS);
			}
			if (field.isImmutable()) {
				out.append(FormConstants.IMMUTABLE_CSS_CLASS);
			} else if (field.isDisabled()) {
				out.append(FormConstants.DISABLED_CSS_CLASS);
			} else {
				out.append(FormConstants.ACTIVE_CSS_CLASS);
			}
		}
	}

	@Override
	protected void writeControlAttributes(DisplayContext context, TagWriter out) throws IOException {
		super.writeControlAttributes(context, out);
		FormMember field = getModel();
		if (field instanceof AbstractFormField) {
			Collection<KeyEventListener> listeners = ((AbstractFormField) field).inspectKeyListeners();
			if (listeners != null) {
				StringBuilder buffer = new StringBuilder();
				for (KeyEventListener listener : listeners) {
					KeySelector[] subscribedKeys = listener.subscribedKeys();
					for (int n = 0, cnt = subscribedKeys.length; n < cnt; n++) {
						KeySelector selector = subscribedKeys[n];
						if (n > 0) {
							buffer.append(' ');
						}
						buffer.append('K');
						if (selector.hasShiftModifier()) {
							buffer.append('S');
						}
						if (selector.hasCtrlModifier()) {
							buffer.append('C');
						}
						if (selector.hasAltModifier()) {
							buffer.append('A');
						}
						int scanCode = selector.getKeyCode().getScanCode();
						if (scanCode < 100) {
							buffer.append('0');
						}
						if (scanCode < 10) {
							buffer.append('0');
						}
						buffer.append(scanCode);
					}
				}
				out.writeAttribute(TL_KEY_SELECTORS, buffer);
			}
		}
	}

    @Override
	public int compareTo(Object aObject) {
        FormField theField = getFieldModel();
        Object value1 = theField != null && theField.hasValue() ? theField.getValue() : null;
        theField = ((AbstractFormFieldControlBase)aObject).getFieldModel();
        Object value2 = theField != null && theField.hasValue() ? theField.getValue() : null;
        return ComparableComparator.INSTANCE.compare(value1, value2);
    }

    /**
     * The CSS style attribute of this control's input element.
     */
	public final String getInputStyle() {
    	return this.inputStyle;
    }

    /**
     * @see #getInputStyle()
     */
    public final void setInputStyle(String inputStyle) {
    	this.inputStyle = inputStyle;
    	
    	if (isAttached()) {
    		handleInputStyleChange();
    	}
    }

    /**
     * Called after the {@link #getInputStyle()} has changed.
     */
	protected void handleInputStyleChange() {
		this.requestRepaint();
	}

	@Override
	protected final void internalWrite(DisplayContext context, TagWriter out) throws IOException {
		doInternalWrite(context, out);

		if (FocusHandling.shouldFocus(context, getFieldModel())) {
			FocusHandling.writeFocusRequest(out, getID());
		}
	}

	/**
	 * Implementation of {@link #write(DisplayContext, TagWriter)}.
	 */
	protected abstract void doInternalWrite(DisplayContext context, TagWriter out) throws IOException;

	/**
	 * calls {@link #writeJSAction(TagWriter, String, String, AbstractFormFieldControlBase, String)} with
	 * "handleClick" as method
	 * 
	 * @see #writeJSAction(TagWriter, String, String, AbstractFormFieldControlBase, String)
	 */
	protected static void writeOnClick(TagWriter out, String jsClass, AbstractFormFieldControlBase ctrl,
			String additional) throws IOException {
		out.beginAttribute(ONCLICK_ATTR);
		writeJSAction(out, jsClass, "handleClick", ctrl, additional);
		out.endAttribute();
	}

	/**
	 * calls {@link #writeJSAction(TagWriter, String, String, AbstractFormFieldControlBase, String)} with
	 * "handleOnChange" as method
	 * 
	 * @see #writeJSAction(TagWriter, String, String, AbstractFormFieldControlBase, String)
	 */
	protected static void writeHandleOnChangeAction(TagWriter out, String jsClass, AbstractFormFieldControlBase ctrl,
			String additional) throws IOException {
		writeJSAction(out, jsClass, "handleOnChange", ctrl, additional);
	}

	/**
	 * Returns a JS function to trigger a method in some JS-Object
	 * @param jsClass
	 *        the JS-Object containing the method
	 * @param method
	 *        the method to trigger, the first parameter is 'this', so the element which gets the
	 *        resulting action, the second is the id of the given control, then the arguments
	 *        encoded in the <code> additional</code>, and at least <code>true</code> if the wait
	 *        cursor must be shown, otherwise no additional parameter.
	 * @param ctrl
	 *        the control which writes the resulting action to some element
	 * @param additional
	 *        additional parameter for the command. Will not be encoded. must be of the form
	 *        <code>,x1,x2,x3...</code>
	 * 
	 * @see #showWait(AbstractFormFieldControlBase)
	 */
	protected static void writeJSAction(TagWriter out, String jsClass, String method,
			AbstractFormFieldControlBase ctrl, String additional) throws IOException {
		out.append("return ");
		out.append(jsClass);
		out.append('.');
		out.append(method);
		out.append("(this,");
		ctrl.writeIdJsString(out);
		if (additional != null) {
			out.append(additional);
		}
		if (showWait(ctrl)) {
			out.append(",true);");
		} else {
			out.append(");");
		}
	}

	/**
	 * Whether the GUI shall show a wait cursor when sending new value of field to server.
	 * 
	 * @param ctrl
	 *        the displaying control
	 */
	protected static boolean showWait(AbstractFormFieldControlBase ctrl) {
		FormField fieldModel = ctrl.getFieldModel();
		for (ValueListener listener : fieldModel.getValueListeners()) {
			if (listener.getClass().getAnnotation(UINonBlocking.class) != null) {
				// value listener must not block UI.
				continue;
			}
			return true;
		}
		return false;
	}

	@Override
	public Bubble handleMandatoryChanged(FormField sender, Boolean oldValue, Boolean newValue) {
		if (!skipEvent(sender)) {
			updateCss();
		}
		return Bubble.BUBBLE;
	}

	@Override
	public Bubble hasWarningsChanged(FormField field, Boolean oldWarnings, Boolean newWarnings) {
		if (!skipEvent(field)) {
			updateCss();
		}
		return Bubble.BUBBLE;
	}

	@Override
	public Bubble hasErrorChanged(FormField sender, Boolean oldError, Boolean newError) {
		if (!skipEvent(sender)) {
			updateCss();
		}
		return Bubble.BUBBLE;
	}
	
	@Override
	public Bubble handleDisabledChanged(FormMember sender, Boolean oldValue, Boolean newValue) {
		if (!skipEvent(sender)) {
			internalHandleDisabledEvent(sender,oldValue,newValue);
		}
		return Bubble.BUBBLE;
	}


	protected abstract void internalHandleDisabledEvent(FormMember sender, Boolean oldValue, Boolean newValue);

	@Override
	public Bubble handleImmutableChanged(FormMember sender, Boolean oldValue, Boolean newValue) {
		return repaintOnEvent(sender);
	}

	@Override
	public final Bubble handleRawValueChanged(FormField field, Object oldValue, Object newValue) {
		if (!skipEvent(field)) {
			internalHandleValueChanged(field, oldValue, newValue);
		}
		return Bubble.BUBBLE;
	}

	@Override
	protected void attachRevalidated() {
		super.attachRevalidated();

		updateRawValue(getFieldModel().getRawValue());
	}

	@Override
	public void valueChanged(FormField field, Object oldValue, Object newValue) {
		if (!skipEvent(field)) {
			// In the case that the field is displayed multiple times in the UI, it is not
			// sufficient to use the RawValueListener interface for updating the value, since the
			// event is not fired, if the change happened in the UI.
			Object currentRawValue = toRawValue(field, newValue);
			Object lastRawValue = _lastRawValue;
			if (!Utils.equals(currentRawValue, lastRawValue)) {
				updateRawValue(currentRawValue);
				if (isAttached()) {
					internalHandleValueChanged(field, lastRawValue, currentRawValue);
				}
			}
		}
	}

	/**
	 * Computes the new raw value of the given application value.
	 * 
	 * @param newValue
	 *        The new value that was set to the field.
	 * @return The raw value of the given new application value.
	 */
	protected Object toRawValue(FormField field, Object newValue) {
		return field.getRawValue();
	}

	/**
	 * Defines the control-specific reaction on the programmatic change of this control's field.
	 * 
	 * <p>
	 * The implementation must care for transporting the new value to the GUI.
	 * </p>
	 */
	protected abstract void internalHandleValueChanged(FormField field, Object oldValue, Object newValue);

	@Override
	public Bubble handleIsBlockedChanged(FormField sender, Boolean oldValue, Boolean newValue) {
		return repaintOnEvent(sender);
	}

	/**
	 * Pass the focus to this view.
	 */
	@Override
	public Bubble handleFocusRequested(Focusable sender) {
		if (!skipEvent(sender)) {
			addUpdate(FocusHandling.focusRequest(getID()));
		}
		return Bubble.BUBBLE;
	}

	/**
	 * Sets the cached raw value that is currently displayed at the UI.
	 * 
	 * <p>
	 * This value is used to check, whether a client-update is necessary if the server-side value
	 * changes. If two controls display the same value, one of them must update the client-side
	 * view, even if the change came from the UI.
	 * </p>
	 */
	protected void updateRawValue(Object nextRawValue) {
		_lastRawValue = nextRawValue;
	}

	protected static class ValueChanged extends ControlCommand {

		/** Key for the new value in the arguments map, received from the UI. */
		protected static final String VALUE_ARG = "value";

		protected static final String COMMAND_ID = "valueChanged";
		
		public static final ControlCommand INSTANCE = new ValueChanged();

		public ValueChanged() {
			super(COMMAND_ID);
		}

		@Override
		protected HandlerResult execute(DisplayContext commandContext, Control control, Map<String, Object> arguments) {
			final AbstractFormFieldControlBase formFieldControl = (AbstractFormFieldControlBase) control;
			if (!executeCommandIfViewDisabled() && formFieldControl.isViewDisabled()) {
				Logger.warn("Received '" + getID() + "' for a client side disabled control. Model: " + formFieldControl.getModel(),
						AbstractFormFieldControlBase.class);
				formFieldControl.requestRepaint();
				return HandlerResult.DEFAULT_RESULT;
			}

			final Object newValue = arguments.get(VALUE_ARG);

			updateValue(commandContext, formFieldControl, newValue, arguments);

			return HandlerResult.DEFAULT_RESULT;
		}

		protected void updateValue(DisplayContext commandContext, AbstractFormFieldControlBase formFieldControl,
				Object newClientValue, Map<String, Object> arguments) {
			// Prevent mirroring the change back to the UI by eagerly updating the compare value.
			formFieldControl.updateRawValue(newClientValue);

			final FormField field = formFieldControl.getFieldModel();
			try {
				FormFieldInternals.updateFieldNoClientUpdate(field, newClientValue);
			} catch (VetoException ex) {
				ex.setContinuationCommand(new Command() {

					@Override
					public HandlerResult executeCommand(DisplayContext context) {
						try {
							Object parsedValue =
								FormFieldInternals.parseRawValue((AbstractFormField) field, newClientValue);
							field.setValue(parsedValue);
						} catch (CheckException ex) {
							// Ignore
						}

						return HandlerResult.DEFAULT_RESULT;
					}
				});
				ex.process(formFieldControl.getWindowScope());
			}
		}

		@Override
		public ResKey getI18NKey() {
			return I18NConstants.VALUE_CHANGED;
		}
	}

}
