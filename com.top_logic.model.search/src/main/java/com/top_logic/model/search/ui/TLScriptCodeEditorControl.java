/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.ui;

import java.io.IOException;
import java.util.Map;

import com.top_logic.base.services.simpleajax.JSSnipplet;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.xml.TagUtil;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.DynamicText;
import com.top_logic.layout.basic.ControlCommand;
import com.top_logic.layout.codeedit.control.CodeEditorControl;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.control.BlockControl;
import com.top_logic.layout.form.control.DefaultSimpleCompositeControlRenderer;
import com.top_logic.layout.form.control.ErrorControl;
import com.top_logic.layout.form.control.PopupEditControl;
import com.top_logic.layout.form.template.AbstractFormFieldControlProvider;
import com.top_logic.layout.form.template.ControlProvider;
import com.top_logic.mig.html.HTMLUtil;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.util.regex.TLRegexBuilder;


/**
 * {@link CodeEditorControl} for editing {@link Expr TL-Script expressions}.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class TLScriptCodeEditorControl extends CodeEditorControl implements TLScriptConstants {

	/**
	 * CSS class decorating {@link TLScriptCodeEditorControl}s.
	 */
	public static final String TLSCRIPT_CSS_CLASS = "tlscript";

	/**
	 * Language mode for TLScript.
	 */
	public static final String MODE_TL_SCRIPT = "ace/mode/tlscript";

	private static Map<String, ControlCommand> getControlCommands() {
		ControlCommand[] autoCompletionCommand = new ControlCommand[] { new TLScriptAutoCompletionCommand(false) };

		return createCommandMap(CodeEditorControl.COMMANDS, autoCompletionCommand);
	}

	/**
	 * Creates a {@link CodeEditorControl}.
	 */
	public TLScriptCodeEditorControl(FormField member) {
		super(member, MODE_TL_SCRIPT, getControlCommands());
	}

	/**
	 * Trigger a callback which show the corresponding completions in a popup window.
	 */
	public void sendCompletions(String completions) {
		addUpdate(new JSSnipplet(new DynamicText() {
			@Override
			public void append(DisplayContext context, Appendable out) throws IOException {
				out.append("services.tlscriptsearch.showCompletions(");
				TagUtil.writeJsString(out, completions);
				out.append(")");
			}
		}));
	}

	/**
	 * {@link ControlProvider} creating {@link TLScriptCodeEditorControl}s without
	 * {@link ErrorControl}.
	 */
	public static final class PlainCP extends AbstractFormFieldControlProvider {

		/**
		 * Default {@link PlainCP} instance creating an editor in tlscript mode.
		 */
		public static final PlainCP INSTANCE = new PlainCP();

		@Override
		protected Control createInput(FormMember member) {
			return new TLScriptCodeEditorControl((FormField) member);
		}
	}

	/**
	 * {@link ControlProvider} creating {@link TLScriptCodeEditorControl}s.
	 */
	public static final class CP extends AbstractFormFieldControlProvider {

		/**
		 * Default {@link CP} instance creating an editor in tlscript mode.
		 */
		public static final CP INSTANCE = new CP();

		@Override
		protected Control createInput(FormMember member) {
			BlockControl result = new BlockControl();
			result.setRenderer(DefaultSimpleCompositeControlRenderer.divWithCSSClass("popupEditField"));
			result.addChild(new TLScriptCodeEditorControl((FormField) member));
			result.addChild(new ErrorControl(member, true));
			return result;
		}
	}

	/**
	 * {@link ControlProvider} displaying a {@link TLScriptCodeEditorControl} in a popup.
	 */
	public static final class PopupCP extends PopupEditControl.CP {

		/**
		 * Creates a {@link PopupCP} from configuration.
		 * 
		 * @param context
		 *        The context for instantiating sub configurations.
		 * @param config
		 *        The configuration.
		 */
		@CalledByReflection
		public PopupCP(InstantiationContext context, Config<?> config) {
			super(context, config);
		}

		@Override
		protected Control createInput(FormMember member) {
			return new PopupEditControl(getSettings(), (FormField) member);
		}
	}

	@Override
	protected String getTypeCssClass() {
		return TLSCRIPT_CSS_CLASS;
	}

	@Override
	protected void internalWriteControl(DisplayContext context, TagWriter out, boolean readOnly, boolean showGutter)
			throws IOException {
		super.internalWriteControl(context, out, readOnly, showGutter);

		enableCustomAutoCompletion(out);
	}

	private void enableCustomAutoCompletion(TagWriter out) throws IOException {
		String regex = createTLScriptAutoCompletionRegex();

		HTMLUtil.beginScriptAfterRendering(out);
		out.append("services.tlscript.enableCustomEditorAutoComplete(");
		TagUtil.writeJsString(out, getEditorID());
		out.append(',');
		TagUtil.writeText(out, regex);
		out.append(");");
		HTMLUtil.endScriptAfterRendering(out);
	}

	private String createTLScriptAutoCompletionRegex() {
		return new TLRegexBuilder().add(createTLModelConstantExpression())
			.or().add(createTLScriptFunctionExpression())
			.or().add(createDefaultExpression()).toJavascriptString();
	}

	private String createTLScriptFunctionExpression() {
		return new TLRegexBuilder().then(".").emptyWord().endOfLine().toString();
	}

	private String createDefaultExpression() {
		return new TLRegexBuilder().word().endOfLine().toString();
	}

	private String createTLModelConstantExpression() {
		return new TLRegexBuilder().nonWord().then(MODEL_SCOPE_SEPARATOR)
			.group().javaIdentifier()
				.group().then(".").javaIdentifier().endGroup().zeroOrMore()
				.group().then(MODEL_TYPE_SEPARATOR).javaIdentifier()
					.group().then(".").javaIdentifier().endGroup().zeroOrMore()
				.endGroup().maybe()
				.group().then(MODEL_ATTRIBUTE_SEPARATOR).javaIdentifier().endGroup().maybe()
			.endGroup().maybe().endOfLine().toString();
	}

}
