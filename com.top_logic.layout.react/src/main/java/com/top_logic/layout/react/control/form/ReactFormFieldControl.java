/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.form;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.top_logic.basic.listener.EventType.Bubble;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.UpdateQueue;
import com.top_logic.layout.VetoException;
import com.top_logic.layout.basic.ControlCommand;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.control.AbstractFormFieldControl;
import com.top_logic.layout.form.model.FormFieldInternals;
import com.top_logic.layout.react.I18NConstants;
import com.top_logic.layout.react.ReactControl;
import com.top_logic.layout.react.SSEUpdateQueue;
import com.top_logic.layout.react.protocol.PatchEvent;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.mig.html.HTMLUtil;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.error.TopLogicException;

/**
 * A form field control that renders via a React component.
 *
 * <p>
 * On initial render, the full field state (value, editable, disabled, mandatory, errors, label,
 * tooltip) is sent as JSON. Subsequent field changes are delivered as incremental patches via SSE.
 * </p>
 */
public class ReactFormFieldControl extends AbstractFormFieldControl {

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

	private static final String VALUE_CHANGED_COMMAND = "valueChanged";

	private static final Map<String, ControlCommand> COMMANDS =
		createCommandMap(new ControlCommand[] { ValueChanged.INSTANCE });

	private final String _reactModule;

	private SSEUpdateQueue _sseQueue;

	/**
	 * Creates a new {@link ReactFormFieldControl}.
	 *
	 * @param model
	 *        The form field.
	 * @param reactModule
	 *        The React module identifier (e.g. "TLTextInput").
	 */
	public ReactFormFieldControl(FormField model, String reactModule) {
		super(model, COMMANDS);
		_reactModule = reactModule;
	}

	@Override
	protected void writeEditable(DisplayContext context, TagWriter out) throws IOException {
		writeMountPoint(context, out);
	}

	@Override
	protected void writeImmutable(DisplayContext context, TagWriter out) throws IOException {
		writeMountPoint(context, out);
	}

	private void writeMountPoint(DisplayContext context, TagWriter out) throws IOException {
		Map<String, Object> state = buildFullFieldState();

		out.beginBeginTag(HTMLConstants.DIV);
		writeControlAttributes(context, out);
		out.writeAttribute("data-react-module", _reactModule);
		out.writeAttribute("data-react-state", ReactControl.toJsonString(state));
		out.endBeginTag();
		out.endTag(HTMLConstants.DIV);

		HTMLUtil.beginScriptAfterRendering(out);
		out.append("TLReact.mountField('");
		out.append(getID());
		out.append("', '");
		out.append(_reactModule);
		out.append("', ");
		ReactControl.writeJsonLiteral(out, state);
		out.append(");");
		HTMLUtil.endScriptAfterRendering(out);

		SSEUpdateQueue queue = SSEUpdateQueue.forSession(context.asRequest().getSession());
		_sseQueue = queue;
		queue.registerControl(this);
	}

	@Override
	protected void internalDetach() {
		SSEUpdateQueue queue = _sseQueue;
		if (queue != null) {
			queue.unregisterControl(this);
			_sseQueue = null;
		}
		super.internalDetach();
	}

	@Override
	protected void internalRevalidate(DisplayContext context, UpdateQueue actions) {
		// Updates are delivered via SSE, not through the standard revalidation path.
	}

	/**
	 * Builds the full field state as a map.
	 *
	 * <p>
	 * Subclasses may override this method to add additional state entries (e.g. options for select
	 * fields).
	 * </p>
	 */
	protected Map<String, Object> buildFullFieldState() {
		FormField field = getFieldModel();
		Map<String, Object> state = new HashMap<>();
		state.put(VALUE, field.hasValue() ? field.getValue() : null);
		state.put(EDITABLE, !field.isImmutable());
		state.put(DISABLED, field.isDisabled());
		state.put(MANDATORY, field.isMandatory());
		state.put(HAS_ERROR, field.hasError());
		state.put(ERROR_MESSAGE, field.hasError() ? field.getError().toString() : null);
		state.put(LABEL, field.hasLabel() ? field.getLabel() : null);
		state.put(TOOLTIP, field.getTooltip() != null ? field.getTooltip().toString() : null);
		return state;
	}

	@Override
	protected void internalHandleValueChanged(FormField field, Object oldValue, Object newValue) {
		Map<String, Object> patch = new HashMap<>();
		patch.put(VALUE, field.hasValue() ? field.getValue() : null);
		sendPatch(patch);
	}

	@Override
	protected void internalHandleDisabledEvent(FormMember sender, Boolean oldValue, Boolean newValue) {
		Map<String, Object> patch = new HashMap<>();
		patch.put(DISABLED, newValue);
		sendPatch(patch);
	}

	@Override
	public Bubble handleMandatoryChanged(FormField sender, Boolean oldValue, Boolean newValue) {
		if (!skipEvent(sender)) {
			Map<String, Object> patch = new HashMap<>();
			patch.put(MANDATORY, newValue);
			sendPatch(patch);
		}
		return Bubble.BUBBLE;
	}

	@Override
	public Bubble hasErrorChanged(FormField sender, Boolean oldError, Boolean newError) {
		if (!skipEvent(sender)) {
			Map<String, Object> patch = new HashMap<>();
			patch.put(HAS_ERROR, newError);
			patch.put(ERROR_MESSAGE, Boolean.TRUE.equals(newError) ? sender.getError().toString() : null);
			sendPatch(patch);
		}
		return Bubble.BUBBLE;
	}

	@Override
	public Bubble handleImmutableChanged(FormMember sender, Boolean oldValue, Boolean newValue) {
		if (!skipEvent(sender)) {
			Map<String, Object> patch = new HashMap<>();
			patch.put(EDITABLE, !Boolean.TRUE.equals(newValue));
			sendPatch(patch);
		}
		return Bubble.BUBBLE;
	}

	protected void sendPatch(Map<String, Object> patch) {
		SSEUpdateQueue queue = _sseQueue;
		if (queue == null) {
			return;
		}
		PatchEvent event = PatchEvent.create()
			.setControlId(getID())
			.setPatch(ReactControl.toJsonString(patch));
		queue.enqueue(event);
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
