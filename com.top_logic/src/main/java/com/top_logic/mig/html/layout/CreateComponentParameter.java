/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout;

import java.util.Map;

import com.top_logic.layout.processor.LayoutResolver;
import com.top_logic.layout.processor.ParameterValue;

/**
 * Parameter object to create components from XML.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class CreateComponentParameter {

	/**
	 * Creates a new {@link CreateComponentParameter} with given layout name.
	 * 
	 * @param layoutName
	 *        see {@link #getLayoutName()};
	 */
	public static CreateComponentParameter newParameter(String layoutName) {
		CreateComponentParameter parameter = new CreateComponentParameter();
		parameter.setLayoutName(layoutName);
		return parameter;
	}

	private LayoutContainer _parent;

	private LayoutResolver _layoutResolver;

	private String _layoutName;

	private Map<String, ? extends ParameterValue> _templateArguments;

	private boolean _resolveComponent;

	private String _layoutNameScope;

	/**
	 * The parent for the new component.
	 */
	public LayoutContainer getParent() {
		return _parent;
	}

	/**
	 * Sets value of {@link #getParent()}
	 */
	public void setParent(LayoutContainer parent) {
		_parent = parent;
	}

	/**
	 * {@link LayoutResolver} to read the component from the layout XML file.
	 */
	public LayoutResolver getLayoutResolver() {
		return _layoutResolver;
	}

	/**
	 * Sets value of {@link #getLayoutResolver()}.
	 */
	public void setLayoutResolver(LayoutResolver layoutResolver) {
		_layoutResolver = layoutResolver;
	}

	/**
	 * Name of the file to read layout definition from.
	 */
	public String getLayoutName() {
		return _layoutName;
	}

	/**
	 * Sets value of {@link #getLayoutName()}
	 */
	public void setLayoutName(String layoutName) {
		_layoutName = layoutName;
	}

	/**
	 * The name scope to qualify components loaded from {@link #getLayoutName()}.
	 */
	public String getLayoutNameScope() {
		if (_layoutNameScope == null) {
			return _layoutName;
		}
		return _layoutNameScope;
	}

	/**
	 * Setter for {@link #getLayoutNameScope()}.
	 * 
	 * @param layoutNameScope
	 *        New value of {@link #getLayoutNameScope()}. May be <code>null</code> which means that
	 *        {@link #getLayoutNameScope()} returns {@link #getLayoutName()}.
	 */
	public void setLayoutNameScope(String layoutNameScope) {
		_layoutNameScope = layoutNameScope;
		
	}

	/**
	 * Argument that are needed to instantiate the component defined in {@link #getLayoutName()}.
	 */
	public Map<String, ? extends ParameterValue> getTemplateArguments() {
		return _templateArguments;
	}

	/**
	 * Sets value of {@link #getTemplateArguments()}
	 */
	public void setTemplateArguments(Map<String, ? extends ParameterValue> templateArguments) {
		_templateArguments = templateArguments;
	}

	/**
	 * Whether the created component should be resolved, i.e. whether
	 * {@link LayoutComponent#resolveComponent(com.top_logic.basic.config.InstantiationContext)}
	 * should be called.
	 */
	public boolean isResolveComponent() {
		return _resolveComponent;
	}

	/**
	 * Sets value of {@link #isResolveComponent()}
	 */
	public void setResolveComponent(boolean resolveComponent) {
		_resolveComponent = resolveComponent;
	}

}
