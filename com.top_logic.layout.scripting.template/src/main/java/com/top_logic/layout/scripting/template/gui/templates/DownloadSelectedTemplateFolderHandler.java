/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.template.gui.templates;

import java.util.Collection;
import java.util.Collections;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.component.HasSelection;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.execution.CombinedExecutabilityRule;
import com.top_logic.tool.execution.ExecutabilityRule;

/**
 * Handler to download the selected template folder.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class DownloadSelectedTemplateFolderHandler extends DownloadScriptRecorderTemplateFoldersHandler {

	/**
	 * Creates a {@link DownloadScriptRecorderTemplateFoldersHandler} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	public DownloadSelectedTemplateFolderHandler(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	protected Collection<String> getToZippedTemplateFolderPaths(TemplateTreeComponent component) {
		String templatePath = ScriptTemplateUtil.getSelectedTemplateResourcePathSuffix(component);

		return Collections
			.singleton(ScriptTemplateUtil.getTemplateRootPath(component) + removePossibleLeadingSlash(templatePath));
	}

	@Override
	protected String getZipName(LayoutComponent component) {
		String selectedTemplateName = ScriptTemplateUtil.getSelectedTemplateName(getTemplateTreeComponent(component));

		return SCRIPT_TEMPLATE_ZIP_PREFIX_FILENAME + "-" + selectedTemplateName;
	}

	private TemplateTreeComponent getTemplateTreeComponent(LayoutComponent component) {
		return (TemplateTreeComponent) component;
	}

	private String removePossibleLeadingSlash(String string) {
		if (string.indexOf("/") == 0) {
			return string.substring(1);
		}

		return string;
	}

	@Override
	protected ExecutabilityRule intrinsicExecutability() {
		return CombinedExecutabilityRule.combine(HasSelection.INSTANCE, super.intrinsicExecutability());
	}

}
