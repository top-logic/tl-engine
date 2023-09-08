/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.processor;

import static com.top_logic.basic.core.xml.DOMUtil.*;

import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.top_logic.basic.Protocol;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.xml.DOMUtil;

/**
 * Find all relevant Components that have no 'name' attribute, and print a warning for each of them.
 * 
 * <p>
 * The names are necessary for automated UI tests that record the names of the Component they
 * activate and replay them later.
 * </p>
 * 
 * @author <a href=mailto:jst@top-logic.com>Jan Stolzenburg</a>
 */
public class ComponentNameCheck extends Operation implements ReferenceHandler {
	
	/**
	 * Creates a {@link ComponentNameCheck}.
	 * 
	 * @param protocol
	 *        See {@link Operation#Operation(Protocol, Application)}.
	 */
	public ComponentNameCheck(Protocol protocol) {
		super(protocol, null);
	}

	Map<String, Element> allComponentNames = new HashMap<>();

	@Override
	public void handle(LayoutDefinition layout) {
		setApplication(layout.getApplication());

		checkComponentNames(layout, layout.getLayoutDocument());
	}

	private void checkComponentNames(LayoutDefinition context, Node node) {
		for (Element child : elementsNS(node, null)) {
			if (DOMUtil.hasLocalName("component", child)) {
				String className = LayoutModelUtils.getComponentClass(child);

				String componentName = LayoutModelUtils.getComponentName(child);
				if (StringServices.isEmpty(componentName)) {
					if (isRelevant(className)) {
						warn(context.getLayoutData(),
							"Unnamed component '" + className + "' in " + getElementId(context, child) + ".");
					}
				} else {
					{
						Element clash = allComponentNames.put(componentName, child);
						if (clash != null) {
							error(context.getLayoutData(), "Duplicate component name '" + componentName
								+ "' in\n " + getElementId(context, clash)
								+ "\n and " + getElementId(context, child) + ".");
						}
					}
				}
			}

			checkComponentNames(context, child);
		}
	}

	private String getElementId(LayoutDefinition context, Element component) {
		StringBuilder idBuffer = new StringBuilder();
		idBuffer.append(LayoutModelUtils.getSourceAnnotations(component));
		return idBuffer.toString();
	}

	private boolean isRelevant(String className) {
		return !(className.endsWith("Layout") || className.endsWith(".DefaultButtonComponent")
			|| className.endsWith("InfoComponent") || className.equals("%INFO_COMPONENT%"));
	}

	public void check(LayoutResolver resolver, String rootLayoutName) throws ResolveFailure {
		ConstantLayout layout = resolver.getLayout(rootLayoutName);
		new LayoutInline(resolver).inline(layout);
		handle(layout);
	}

}