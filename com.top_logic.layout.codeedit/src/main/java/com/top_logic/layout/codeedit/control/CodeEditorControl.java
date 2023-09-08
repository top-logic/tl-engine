/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.codeedit.control;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.top_logic.base.services.simpleajax.JSSnipplet;
import com.top_logic.basic.col.TupleFactory.Pair;
import com.top_logic.basic.config.annotation.NumberOfRows;
import com.top_logic.basic.json.JSON;
import com.top_logic.basic.json.JSON.DefaultValueAnalyzer;
import com.top_logic.basic.listener.EventType.Bubble;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagUtil;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.DynamicText;
import com.top_logic.layout.basic.ControlCommand;
import com.top_logic.layout.form.CheckException;
import com.top_logic.layout.form.Constraint;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.constraints.AbstractConstraint;
import com.top_logic.layout.form.control.AbstractFormFieldControl;
import com.top_logic.layout.form.control.AbstractFormFieldControlBase;
import com.top_logic.layout.form.template.AbstractFormFieldControlProvider;
import com.top_logic.layout.form.template.ControlProvider;
import com.top_logic.mig.html.HTMLUtil;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * {@link Control} displaying an embedded source code editor.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class CodeEditorControl extends AbstractFormFieldControl {

	/**
	 * {@link #getLanguageMode() Language mode} for plain text.
	 */
	public static final String MODE_TEXT = "ace/mode/text";

	/**
	 * {@link #getLanguageMode() Language mode} for XML.
	 */
	public static final String MODE_XML = "ace/mode/xml";

	/**
	 * {@link #getLanguageMode() Language mode} for CSS.
	 */
	public static final String MODE_CSS = "ace/mode/css";

	/**
	 * {@link #getLanguageMode() Language mode} for JavaScropt.
	 */
	public static final String MODE_JS = "ace/mode/javascript";

	/**
	 * {@link #getLanguageMode() Language mode} for HTML.
	 */
	public static final String MODE_HTML = "ace/mode/html";

	/**
	 * {@link #getLanguageMode() Language mode} for Json.
	 */
	public static final String MODE_JSON = "ace/mode/json";

	/**
	 * Eclipse like {@link #getEditorTheme() editor theme}.
	 */
	public static final String THEME_ECLIPSE = "ace/theme/eclipse";

	/**
	 * CSS class name to display the code editor in full size.
	 */
	public static final String FULL_SIZE_CSS_CLASS = "fullSize";

	/**
	 * Allo supported language modes for the code editor control.
	 */
	public static final List<String> LANGUAGE_MODES = Collections.unmodifiableList(Arrays.asList(
		MODE_TEXT,
		MODE_CSS,
		MODE_HTML,
		MODE_XML,
		MODE_JS,
		MODE_JSON
	));

	/**
	 * {@link ControlProvider} creating {@link CodeEditorControl}s.
	 */
	public static class CP extends AbstractFormFieldControlProvider {

		/**
		 * Default {@link CP} instance creating an editor in text mode.
		 */
		public static final CodeEditorControl.CP INSTANCE = new CodeEditorControl.CP(MODE_TEXT);

		private final String _languageMode;

		private CodeEditorControl.WarnLevel _warningLevel = CodeEditorControl.WarnLevel.ERROR;

		/**
		 * Creates a {@link CP}.
		 *
		 * @param languageMode
		 *        See {@link CodeEditorControl#getLanguageMode()}.
		 */
		public CP(String languageMode) {
			_languageMode = languageMode;
		}

		@Override
		protected Control createInput(FormMember member) {
			CodeEditorControl ctrl = new CodeEditorControl((FormField) member, _languageMode);
			ctrl.setWarningLevel(_warningLevel);
			return ctrl;
		}

		/**
		 * Sets the {@link WarnLevel} for the created controls.
		 * 
		 * @return this {@link CP}.
		 */
		public CP warnLevel(CodeEditorControl.WarnLevel level) {
			_warningLevel = level;
			return this;
		}
	}

	/** {@link ControlCommand}s for the {@link CodeEditorControl}. */
	protected static final Map<String, ControlCommand> COMMANDS = createCommandMap(
		AbstractFormFieldControlBase.COMMANDS, new ValueChangedWithErrorUpdate(), ClientAnnotationUpdate.INSTANCE);
	static {
		/* Assert that general "value change" command is replaced by special one which updates the
		 * field constraint. */
		assert COMMANDS.get(ValueChanged.INSTANCE.getID()) instanceof ValueChangedWithErrorUpdate;
	}

	private String _editorTheme;

	private String _languageMode;

	private boolean _wrapMode;

	private int _maxLines = Integer.MAX_VALUE;

	private int _minLines = 1;

	private List<String> _excludedKeyBindings = Arrays.asList("tab", "shift-tab");

	/** {@link Constraint} that reflects an error displayed in the client side editor. */
	final ClientSideConstraint _clientErrors = new ClientSideConstraint();

	/** {@link Constraint} that reflects an warnings displayed in the client side editor. */
	final ClientSideConstraint _clientWarnings = new ClientSideConstraint();

	/** {@link WarnLevel} defining how client side failures are reported in the base form field. */
	private CodeEditorControl.WarnLevel _warningLevel = CodeEditorControl.WarnLevel.ERROR;

	/**
	 * Creates a {@link CodeEditorControl}.
	 * 
	 * @param languageMode
	 *        See {@link #getLanguageMode()}.
	 */
	public CodeEditorControl(FormField member, String languageMode) {
		this(member, languageMode, COMMANDS);
	}

	/**
	 * Creates a {@link CodeEditorControl}.
	 * 
	 * @param languageMode
	 *        See {@link #getLanguageMode()}.
	 */
	public CodeEditorControl(FormField member, String languageMode, Map<String, ControlCommand> commands) {
		super(member, commands);

		_editorTheme = THEME_ECLIPSE;
		_languageMode = languageMode;
		_wrapMode = true;

		initLines();
	}

	private void initLines() {
		Pair<Integer, Integer> lines = getFieldModel().get(NumberOfRows.NUMBER_OF_ROWS);
		
		if (lines != null) {
			Integer min = lines.getFirst();
			Integer max = lines.getSecond();

			if (min != null) {
				_minLines = min;
			}

			if (max != null) {
				_maxLines = max;
			}
		}
	}


	private boolean isImmutable() {
		FormField fieldModel = getFieldModel();

		return fieldModel.isImmutable();
	}

	/**
	 * The code editor theme.
	 * 
	 * @see "https://ace.c9.io/#nav=howto"
	 * @see #THEME_ECLIPSE
	 */
	public String getEditorTheme() {
		return _editorTheme;
	}

	/**
	 * @see #getEditorTheme()
	 */
	public void setEditorTheme(String editorTheme) {
		if (editorTheme.equals(_editorTheme)) {
			return;
		}
		_editorTheme = editorTheme;
		if (isAttached()) {
			addUpdate(new JSSnipplet(new DynamicText() {
				@Override
				public void append(DisplayContext context, Appendable out) throws IOException {
					out.append("services.codeeditor.setEditorTheme(");
					TagUtil.writeJsString(out, getID());
					out.append(", ");
					TagUtil.writeJsString(out, editorTheme);
					out.append(");");
				}
			}));
		}
	}

	/**
	 * The code editor language mode.
	 * 
	 * @see "https://ace.c9.io/#nav=howto"
	 * @see #MODE_XML
	 * @see #MODE_CSS
	 * @see #MODE_HTML
	 * @see #MODE_JS
	 * @see #MODE_JSON
	 */
	public String getLanguageMode() {
		return _languageMode;
	}

	/**
	 * @see #getLanguageMode()
	 */
	public void setLanguageMode(String languageMode) {
		if (languageMode.equals(_languageMode)) {
			return;
		}
		_languageMode = languageMode;
		if (isAttached()) {
			addUpdate(new JSSnipplet(new DynamicText() {
				@Override
				public void append(DisplayContext context, Appendable out) throws IOException {
					out.append("services.codeeditor.setLanguageMode(");
					TagUtil.writeJsString(out, getID());
					out.append(", ");
					TagUtil.writeJsString(out, languageMode);
					out.append(");");
				}
			}));
		}
	}

	/**
	 * Show gutter, left border containing among other things line numbers and folding symbols, if
	 * the given argument is true, otherwise the gutter is hidden.
	 */
	private void setShowGutter(boolean showGutter) {
		if (isAttached()) {
			addUpdate(new JSSnipplet(new DynamicText() {
				@Override
				public void append(DisplayContext context, Appendable out) throws IOException {
					out.append("services.codeeditor.setShowGutter(");
					TagUtil.writeJsString(out, getID());
					out.append(", ");
					out.append(Boolean.toString(showGutter));
					out.append(");");
				}
			}));
		}
	}

	/**
	 * Sets the editor in read only mode if the given argument is true, otherwise in edit mode.
	 */
	private void setReadOnly(boolean readOnly) {
		if (isAttached()) {
			addUpdate(new JSSnipplet(new DynamicText() {
				@Override
				public void append(DisplayContext context, Appendable out) throws IOException {
					out.append("services.codeeditor.setReadOnly(");
					TagUtil.writeJsString(out, getID());
					out.append(", ");
					out.append(Boolean.toString(readOnly));
					out.append(");");
				}
			}));
		}
	}

	/**
	 * Sets the editor in wrap mode (wrap text to view) if the given argument is true, otherwise in
	 * edit mode.
	 */
	public void setWrapMode(Boolean wrapMode) {
		if (wrapMode == _wrapMode) {
			return;
		}
		_wrapMode = wrapMode;
		if (isAttached()) {
			addUpdate(new JSSnipplet(new DynamicText() {
				@Override
				public void append(DisplayContext context, Appendable out) throws IOException {
					out.append("services.codeeditor.setWrapMode(");
					TagUtil.writeJsString(out, getID());
					out.append(", ");
					out.append(wrapMode.toString());
					out.append(");");
				}
			}));
		}
	}

	/**
	 * Sets the height of the editor to the given number of lines.
	 */
	public void setMaxLines(int maxLines) {
		if (maxLines == _maxLines) {
			return;
		}
		_maxLines = maxLines;
		if (isAttached()) {
			addUpdate(new JSSnipplet(new DynamicText() {
				@Override
				public void append(DisplayContext context, Appendable out) throws IOException {
					out.append("services.codeeditor.setMaxLines(");
					TagUtil.writeJsString(out, getID());
					out.append(", ");
					out.append(Integer.toString(maxLines));
					out.append(");");
				}
			}));
		}
	}

	@Override
	protected void writeEditable(DisplayContext context, TagWriter out) throws IOException {
		internalWriteControl(context, out, getFieldModel().isDisabled(), true);
	}

	@Override
	protected void writeImmutable(DisplayContext context, TagWriter out) throws IOException {
		internalWriteControl(context, out, true, false);
	}

	/**
	 * Common implementation of {@link #writeEditable(DisplayContext, TagWriter)} and
	 * {@link #writeImmutable(DisplayContext, TagWriter)}.
	 * 
	 * @param context
	 *        Current interaction.
	 * @param readOnly
	 *        Whether the client side editor must be read only.
	 * @param showGutter
	 *        Whether gutter, left border containing among other things line numbers and folding
	 *        symbols are shown.
	 */
	protected void internalWriteControl(DisplayContext context, TagWriter out, boolean readOnly, boolean showGutter)
			throws IOException {
		out.beginBeginTag(DIV);
		writeControlAttributes(context, out);
		out.endBeginTag();

		String editorId = getEditorID();

		out.beginBeginTag(DIV);
		out.writeAttribute(ID_ATTR, editorId);
		out.endBeginTag();

		out.endTag(DIV);

		HTMLUtil.beginScriptAfterRendering(out);
		{
			out.append("services.codeeditor.init(");
			TagUtil.writeJsString(out, getID());
			out.append(", ");
			TagUtil.writeJsString(out, editorId);
			out.append(", ");
			TagUtil.writeJsString(out, _editorTheme);
			out.append(", ");
			TagUtil.writeJsString(out, _languageMode);
			out.append(", ");
			out.append(Boolean.toString(readOnly));
			out.append(", ");
			out.append(Boolean.toString(showGutter));
			out.append(", ");
			out.append(Boolean.toString(_wrapMode));
			out.append(", ");
			out.append(Integer.toString(_minLines));
			out.append(", ");
			out.append(Integer.toString(_maxLines));
			out.append(", ");
			TagUtil.writeText(out, JSON.toString(DefaultValueAnalyzer.INSTANCE, _excludedKeyBindings));
			out.append(", ");
			TagUtil.writeJsString(out, getFieldModel().getRawValue().toString());
			boolean noClientSideWarningsExpected =
				_warningLevel == WarnLevel.NONE || (_clientWarnings._message == null && _clientErrors._message == null);
			out.append(", ");
			out.append(Boolean.toString(noClientSideWarningsExpected));
			out.append(");");
		}
		HTMLUtil.endScriptAfterRendering(out);

		out.endTag(DIV);
	}

	/**
	 * DOM element id for the tl script editor.
	 */
	protected String getEditorID() {
		return getID() + "-editor";
	}

	@Override
	protected void internalHandleValueChanged(FormField field, Object oldValue, Object newValue) {
		if (isAttached()) {
			String value = getFieldModel().getRawValue().toString();

			addUpdate(new JSSnipplet(new DynamicText() {
				@Override
				public void append(DisplayContext context, Appendable out) throws IOException {
					out.append("services.codeeditor.setValue(");
					TagUtil.writeJsString(out, getID());
					out.append(", ");
					TagUtil.writeJsString(out, value);
					out.append(");");
				}
			}));
		}
	}

	@Override
	protected void internalHandleDisabledEvent(FormMember sender, Boolean oldValue, Boolean newValue) {
		setReadOnly(newValue.booleanValue());
		setShowGutter(true);
	}

	@Override
	public Bubble handleImmutableChanged(FormMember sender, Boolean oldValue, Boolean newValue) {
		setReadOnly(newValue.booleanValue());
		setShowGutter(!newValue.booleanValue());
		return Bubble.BUBBLE;
	}

	/**
	 * Defines how client side user input problems (i.e. displayed errors and warnings) are reported
	 * to the base {@link FormField}.
	 */
	public void setWarningLevel(CodeEditorControl.WarnLevel level) {
		Objects.requireNonNull(level);
		if (isAttached()) {
			detachConstraints();
			_warningLevel = level;
			attachConstraints();
		} else {
			_warningLevel = level;
		}
	}

	@Override
	protected void internalAttach() {
		super.internalAttach();
		attachConstraints();
	}

	private void attachConstraints() {
		switch (_warningLevel) {
			case NONE:
				break;
			case WARN:
				getFieldModel().addWarningConstraint(_clientWarnings);
				getFieldModel().addWarningConstraint(_clientErrors);
				break;
			case ERROR:
				getFieldModel().addWarningConstraint(_clientWarnings);
				getFieldModel().addConstraint(_clientErrors);
				break;
			default:
				break;
		}
	}

	@Override
	protected void internalDetach() {
		detachConstraints();
		super.internalDetach();
	}

	private void detachConstraints() {
		switch (_warningLevel) {
			case NONE:
				break;
			case WARN:
				getFieldModel().removeWarningConstraint(_clientWarnings);
				getFieldModel().removeWarningConstraint(_clientErrors);
				break;
			case ERROR:
				getFieldModel().removeWarningConstraint(_clientWarnings);
				getFieldModel().removeConstraint(_clientErrors);
				break;
			default:
				break;
		}

	}

	/**
	 * Command updating the field warnings and errors in case of annotation change.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static class ClientAnnotationUpdate extends ControlCommand {

		/** ACE constant in annotation event: Key for the column of the annotation event. */
		private static final String CODE_EDITOR_ANNOTATION_COLUMN = "column";

		/** ACE constant in annotation event: Key for the row of the annotation event. */
		private static final String CODE_EDITOR_ANNOTATION_ROW = "row";

		/** ACE constant in annotation event: Key for the actual annotation message. */
		private static final String CODE_EDITOR_ANNOTATION_TEXT = "text";

		/**
		 * ACE constant in annotation event: Key for the annotation type.
		 * 
		 * @see #CODE_EDITOR_ANNOTATION_TYPE__ERROR
		 * @see #CODE_EDITOR_ANNOTATION_TYPE__WARNING
		 */
		private static final String CODE_EDITOR_ANNOTATION_TYPE = "type";

		/**
		 * ACE constant in annotation event: Value for {@link #CODE_EDITOR_ANNOTATION_TYPE} in case
		 * of a warning annotation.
		 */
		private static final String CODE_EDITOR_ANNOTATION_TYPE__WARNING = "warning";

		/**
		 * ACE constant in annotation event: Value for {@link #CODE_EDITOR_ANNOTATION_TYPE} in case
		 * of an error annotation.
		 */
		private static final String CODE_EDITOR_ANNOTATION_TYPE__ERROR = "error";

		/** Key for argument map whose value is a list of error messages. */
		public static final String ANNOTATIONS = "annotations";

		/** Singleton {@link CodeEditorControl.ClientAnnotationUpdate} instance. */
		public static final ClientAnnotationUpdate INSTANCE = new ClientAnnotationUpdate();

		/**
		 * Creates a new {@link CodeEditorControl.ClientAnnotationUpdate}.
		 */
		protected ClientAnnotationUpdate() {
			super("annotationUpdate");
		}

		@Override
		public ResKey getI18NKey() {
			return I18NConstants.CLIENT_ANNOTATION_UPDATE_COMMAND;
		}

		@Override
		protected HandlerResult execute(DisplayContext commandContext, Control control, Map<String, Object> arguments) {
			List<?> annotations = (List<?>) arguments.get(ANNOTATIONS);
			ResKey warning = null;
			ResKey error = null;
			if (annotations != null) {
				for (int i = 0; i < annotations.size(); i++) {
					Map<?, ?> annotation = (Map<?, ?>) annotations.get(i);
					Object type = annotation.get(CODE_EDITOR_ANNOTATION_TYPE);
					Object text = annotation.get(CODE_EDITOR_ANNOTATION_TEXT);
					Object row = annotation.get(CODE_EDITOR_ANNOTATION_ROW);
					Object column = annotation.get(CODE_EDITOR_ANNOTATION_COLUMN);
					if (CODE_EDITOR_ANNOTATION_TYPE__ERROR.equals(type)) {
						if (error == null) {
							error = I18NConstants.EDITOR_ERROR__MSG__ROW__COL.fill(text, row, column);
						} else {
							error = I18NConstants.MULTIPLE_ERRORS;
						}
					} else if (CODE_EDITOR_ANNOTATION_TYPE__WARNING.equals(type)) {
						if (warning == null) {
							warning = I18NConstants.EDITOR_WARNING__MSG__ROW__COL.fill(text, row, column);
						} else {
							warning = I18NConstants.MULTIPLE_WARNINGS;
						}
					}
				}
			}
			((CodeEditorControl) control)._clientErrors._message = error;
			((CodeEditorControl) control)._clientWarnings._message = warning;
			return HandlerResult.DEFAULT_RESULT;
		}

	}

	/**
	 * {@link com.top_logic.layout.form.control.AbstractFormFieldControlBase.ValueChanged} for
	 * {@link CodeEditorControl} that also updates the error state of the base field, when the
	 * client side editor displays errors.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static class ValueChangedWithErrorUpdate extends ValueChanged {

		/**
		 * Creates a new {@link ValueChangedWithErrorUpdate}.
		 * 
		 */
		public ValueChangedWithErrorUpdate() {
			super();
		}

		@Override
		protected void updateValue(DisplayContext commandContext, AbstractFormFieldControlBase formFieldControl,
				Object newClientValue, Map<String, Object> arguments) {
			formFieldControl.executeCommand(commandContext, ClientAnnotationUpdate.INSTANCE.getID(), arguments);
			super.updateValue(commandContext, formFieldControl, newClientValue, arguments);
		}
	}

	/**
	 * {@link AbstractConstraint} that reflects an error or warning displayed in the editor on
	 * client side.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	private static class ClientSideConstraint extends AbstractConstraint {

		/**
		 * The displayed failure message. If <code>null</code>, this constraint will not throw a
		 * {@link CheckException}
		 */
		ResKey _message;

		/**
		 * Creates a new {@link ClientSideConstraint}.
		 */
		public ClientSideConstraint() {
		}

		@Override
		public boolean check(Object value) throws CheckException {
			if (_message != null) {
				throw new CheckException(_message);
			}
			return true;
		}
	}

	/**
	 * Level defining which client side warnings and errors are reflected to the the underlying
	 * {@link FormField}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static enum WarnLevel {

		/**
		 * Client side warnings are shown as warnings and errors are shown as errors in the base
		 * form field.
		 */
		ERROR,

		/** Client side warnings and errors are both displayed as warning in the base form field. */
		WARN,

		/**
		 * Client side warnings and errors do not effect the warning, or error state of the base
		 * form field.
		 */
		NONE,

		;
	}

}
