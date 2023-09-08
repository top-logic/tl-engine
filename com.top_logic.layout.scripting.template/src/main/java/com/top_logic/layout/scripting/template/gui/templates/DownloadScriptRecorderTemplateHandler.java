/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.template.gui.templates;

import java.util.Map;

import com.top_logic.basic.FileManager;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.layout.DisplayContext;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Handler to download the given ScriptRecorder template.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class DownloadScriptRecorderTemplateHandler extends DownloadSelectedTemplateFolderHandler {

	/**
	 * Creates a {@link DownloadScriptRecorderTemplateHandler} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	public DownloadScriptRecorderTemplateHandler(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public HandlerResult handleCommand(DisplayContext context, LayoutComponent component, Object model,
			Map<String, Object> arguments) {
		{
			BinaryData file = getSelectedTemplateFile(component);
			if (file != null) {
				deliverContent(context, file);
			} else {
				super.handleCommand(context, component, model, arguments);
			}
		}

		return HandlerResult.DEFAULT_RESULT;
	}

	private BinaryData getSelectedTemplateFile(LayoutComponent component) {
		String path = ScriptTemplateUtil.getSelectedTemplatePath(getTemplateTreeComponent(component));
		return FileManager.getInstance().getDataOrNull(path);
	}

	private void deliverContent(DisplayContext context, BinaryData file) {
		context.getWindowScope().deliverContent(file);
	}

	private TemplateTreeComponent getTemplateTreeComponent(LayoutComponent component) {
		return (TemplateTreeComponent) component;
	}

}
