/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic;

import java.io.IOException;
import java.util.Map;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.xml.TagUtil;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.FrameScope;
import com.top_logic.layout.form.control.AbstractButtonControl;
import com.top_logic.layout.form.control.ButtonControl;
import com.top_logic.layout.form.tag.js.JSValue;
import com.top_logic.mig.html.layout.LayoutUtils;
import com.top_logic.tool.boundsec.BoundCommand;
import com.top_logic.tool.boundsec.CommandHandler;

/**
 * The class {@link FormSubmitExpressionProvider} is a special {@link InvokeExpressionProvider} for
 * {@link ButtonControl}s which have a {@link ComponentCommandModel} as model. The method
 * {@link #writeInvokeExpression(DisplayContext, Appendable, AbstractButtonControl)} returns a JavaScript function to
 * trigger a function at the Component the command was registered.
 * 
 * @author <a href=mailto:daniel.busche@top-logic.com>Daniel Busche</a>
 */
public class FormSubmitExpressionProvider implements InvokeExpressionProvider {

	public static final FormSubmitExpressionProvider INSTANCE = new FormSubmitExpressionProvider();

	/**
	 * Returns a JavaScript function to call the method written by the command, assuming that the
	 * command id of the command is also the method name. The arguments are taken from the
	 * {@link CommandModel} of the {@link ButtonControl}.
	 * 
	 * @see com.top_logic.layout.basic.InvokeExpressionProvider#writeInvokeExpression(com.top_logic.layout.DisplayContext,
	 *      Appendable, AbstractButtonControl)
	 */
	@Override
	public void writeInvokeExpression(DisplayContext context, Appendable out, AbstractButtonControl<?> buttonControl)
			throws IOException {
		ComponentCommandModel model = (ComponentCommandModel) buttonControl.getModel();
		BoundCommand command = model.getCommandHandler();
		Map arguments = model.getArguments();
		FrameScope scope = buttonControl.getScope().getFrameScope();
		String ref = LayoutUtils.getFrameReference(scope, model.getComponent().getEnclosingFrameScope());
		if (StringServices.isEmpty(ref)) {
			ref = "window";
		}
		out.append(ref);
		out.append('.');
		out.append(command.getID());
		out.append('(');
		if (command instanceof CommandHandler) {
			String[] paramterNames = ((CommandHandler) command).getAttributeNames();
			if (paramterNames != null) {
				for (int i = 0; i < paramterNames.length; i++) {
					if (i > 0) {
						out.append(',');
					}
					Object value = arguments.get(paramterNames[i]);
					if (value instanceof JSValue) {
						((JSValue) value).eval(out);
					} else {
						TagUtil.writeJsString(out, String.valueOf(value));
					}
				}
			}
		}
		out.append("); return false;");
	}

}
