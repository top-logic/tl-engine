/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic.contextmenu.command;

import java.io.IOException;
import java.util.Map;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable.Property;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.func.IFunction2;
import com.top_logic.basic.xml.TagUtil;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.ScriptFunction2;
import com.top_logic.layout.basic.ButtonUIModel;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.basic.InvokeExpressionProvider;
import com.top_logic.layout.form.control.AbstractButtonControl;
import com.top_logic.layout.form.control.AbstractButtonControl.HandleClickInvokeExpressionProvider;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.CommandHandlerUtil;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * {@link CommandHandler} extracting a text from its target model and copying to the browser's
 * clipboard.
 * 
 * <p>
 * Note: For technical reasons, this command only works if displayed in a context menu.
 * </p>
 */
@InApp(classifiers = "contextMenu")
public class CopyToClipboardCommand extends AbstractCommandHandler implements InvokeExpressionProvider {

	private static final Property<String> TEXT = TypedAnnotatable.property(String.class, "text");

	/**
	 * Configuration options for {@link CopyToClipboardCommand}.
	 */
	public interface Config extends AbstractCommandHandler.Config {
		/**
		 * Function extracting the text to copy to the clipboard from the target and component
		 * model.
		 * 
		 * <pre>
		 * <code>target -> model -> $target.label()</code>
		 * </pre>
		 */
		ScriptFunction2<String, Object, Object> getTextExtraction();
	}

	private IFunction2<String, Object, Object> _textExtraction;

	/**
	 * Creates a {@link CopyToClipboardCommand}.
	 */
	public CopyToClipboardCommand(InstantiationContext context, Config config) {
		super(context, config);
		_textExtraction = context.getInstance(config.getTextExtraction());
	}

	@Override
	public HandlerResult handleCommand(DisplayContext aContext, LayoutComponent aComponent, Object model,
			Map<String, Object> someArguments) {
		return HandlerResult.DEFAULT_RESULT;
	}

	@Override
	public CommandModel createCommandModel(LayoutComponent component, Map<String, Object> arguments) {
		String text =
			_textExtraction.apply(arguments.get(CommandHandlerUtil.TARGET_MODEL_ARGUMENT), component.getModel());
		CommandModel result = super.createCommandModel(component, arguments);
		result.set(AbstractButtonControl.INVOKE_EXPRESSION_PROVIDER, this);
		result.set(TEXT, text);
		return result;
	}

	@Override
	public void writeInvokeExpression(DisplayContext context, Appendable out, AbstractButtonControl<?> buttonControl)
			throws IOException {
		ButtonUIModel model = buttonControl.getModel();
		String text = model.get(TEXT);
		out.append("navigator.clipboard.writeText(");
		TagUtil.writeJsString(out, text);
		out.append("); ");
		HandleClickInvokeExpressionProvider.INSTANCE.writeInvokeExpression(context, out, buttonControl);
	}

}
