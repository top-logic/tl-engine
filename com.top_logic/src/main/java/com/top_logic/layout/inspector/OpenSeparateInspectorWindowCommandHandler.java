/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.inspector;

import java.util.Map;

import com.top_logic.basic.col.Filter;
import com.top_logic.basic.col.filter.FilterFactory;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.window.OpenWindowCommand;
import com.top_logic.layout.window.WindowComponent;
import com.top_logic.mig.html.layout.FindFirstMatchingComponent;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * This command handler is aware of two parameters that can be passed on client
 * side through java script. It expects that the LayoutComponent calling the
 * command is a ViewInfoComponent.
 * 
 * @author     <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class OpenSeparateInspectorWindowCommandHandler extends OpenWindowCommand {

	/** Object to be inspected. */
	public static final String PARAM_OBJECT = "_inspectObject";

	private static final Filter<? super Object> INSPECTOR_CLASS_FILTER = FilterFactory
		.createClassFilter(InspectorComponent.class);

	/** 
	 * Creates a {@link OpenSeparateInspectorWindowCommandHandler}.
	 */
	public OpenSeparateInspectorWindowCommandHandler(InstantiationContext context, Config config) {
		super(context, config);
    }

	@Override
	protected void initWindow(DisplayContext context, LayoutComponent opener, WindowComponent window, Map<String, Object> args) {
		super.initWindow(context, opener, window, args);

		Object newModel = args.get(PARAM_OBJECT);
		if (newModel == null) {
			newModel = opener.getModel();
		}
		LayoutComponent inspectorComponent = inspectorComponentChild(window);
		inspectorComponent.setModel(newModel);
	}

	private LayoutComponent inspectorComponentChild(LayoutComponent parent) {
		FindFirstMatchingComponent visitor = new FindFirstMatchingComponent(INSPECTOR_CLASS_FILTER);
		parent.acceptVisitorRecursively(visitor);
		return visitor.result();
	}

}
