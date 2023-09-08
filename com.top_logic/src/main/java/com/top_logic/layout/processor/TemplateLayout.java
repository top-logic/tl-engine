/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.processor;

import static com.top_logic.layout.processor.LayoutModelConstants.*;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.regex.Pattern;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.top_logic.basic.Protocol;
import com.top_logic.basic.col.Filter;
import com.top_logic.basic.col.FilteredIterable;
import com.top_logic.basic.config.ConfigUtil;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.xml.DOMUtil;

/**
 * {@link ConstantLayout} that represents a template being included into another
 * {@link ConstantLayout}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TemplateLayout {

	static final String VARIABLE_NAME_PATTERN_SRC = "[\\w_\\-][\\w\\d_\\-]*";

	private final Pattern VARIABLE_NAME_PATTERN = Pattern.compile(VARIABLE_NAME_PATTERN_SRC);

	private final Map<String, ParameterValue> _declaredParameters;

	private final LayoutDefinition _layout;

	private final Element _templateElement;

	public TemplateLayout(LayoutDefinition layout) {
		this(layout, layout.getLayoutDocument().getDocumentElement());
	}

	public TemplateLayout(LayoutDefinition layout, Element templateElement) {
		_layout = layout;
		_templateElement = templateElement;

		_declaredParameters = loadParameterDeclaration();
	}

	private Map<String, ParameterValue> loadParameterDeclaration() {
		if (LayoutModelUtils.isTemplate(_templateElement)) {
			Map<String, ParameterValue> declaredParameters = new HashMap<>();
			for (Element params : DOMUtil.elementsNS(_templateElement, null, LayoutModelConstants.PARAMS_ELEMENT)) {
				for (Element param : DOMUtil.elementsNS(params, null, LayoutModelConstants.PARAM_ELEMENT)) {
					String paramName = param.getAttributeNS(null, PARAM_NAME);
					if (optionalValue(param)) {
						String defaultValue;
						if (param.hasAttributeNS(null, PARAM_VALUE)) {
							defaultValue = param.getAttributeNS(null, PARAM_VALUE);
						} else {
							defaultValue = null;
						}
						StringValue parameter = new StringValue(paramName, defaultValue, true);
						declaredParameters.put(paramName, parameter);
					} else if (param.hasAttributeNS(null, PARAM_VALUE)) {
						String defaultValue = param.getAttributeNS(null, PARAM_VALUE);
						declaredParameters.put(paramName, new StringValue(paramName, defaultValue));
					} else {
						if (param.getFirstChild() != null) {
							declaredParameters.put(paramName, RangeValue.createDefaultValue(paramName, param));
						} else {
							declaredParameters.put(paramName, new UndefinedValue(paramName));
						}
					}
				}
			}
			checkParameterNames(declaredParameters.keySet());
			return declaredParameters;
		} else {
			return Collections.emptyMap();
		}
	}

	private boolean optionalValue(Element param) {
		String optionalString = param.getAttributeNS(null, PARAM_OPTIONAL);
		try {
			return ConfigUtil.getBooleanValue(PARAM_OPTIONAL, optionalString, false);
		} catch (ConfigurationException ex) {
			StringBuilder msg = new StringBuilder();
			msg.append("Illegal '");
			msg.append(PARAM_OPTIONAL);
			msg.append("' value ");
			msg.append(optionalString);
			msg.append(" in ");
			msg.append(_layout.getLayoutName());
			msg.append("'.");
			protocol().error(msg.toString(), ex);
			return false;
		}
	}

	private void checkParameterNames(Collection<String> parameterNames) {
		for (String varname : parameterNames) {
			if (!VARIABLE_NAME_PATTERN.matcher(varname).matches()) {
				protocol().error("No invalid layout parameter '" + varname
					+ "', expected parameter name matching '" + VARIABLE_NAME_PATTERN_SRC + "'.");
			}
		}
	}

	public Map<String, ParameterValue> getDeclaredParameters() {
		return _declaredParameters;
	}

	public Iterable<? extends Node> getContent() {
		if (LayoutModelUtils.isTemplate(_templateElement)) {
			Iterable<Node> templateContent = new FilteredIterable<>(new Filter<Node>() {
				@Override
				public boolean accept(Node anObject) {
					return !DOMUtil.isElement(null, LayoutModelConstants.PARAMS_ELEMENT, anObject);
				}
			}, DOMUtil.children(_templateElement));
			return templateContent;
		} else {
			return Collections.singletonList(_templateElement);
		}
	}

	public Expansion createContentExpander(Expansion contextExpander,
			String includeId, Map<String, ? extends ParameterValue> arguments, NodeProcessor callback) {
		// Check arguments.
		if (!_declaredParameters.keySet().containsAll(arguments.keySet())) {
			HashSet<String> undeclaredArguments = new HashSet<>(arguments.keySet());
			undeclaredArguments.removeAll(_declaredParameters.keySet());

			String context = contextExpander == null ? "" : " from '" + contextExpander.getResource() + "'";
			protocol().error("Usage of undeclared parameters " + undeclaredArguments + " when expanding '" +
				_layout.getLayoutName() + "'" + context + ".");
		}

		return Expansion.newInstance(protocol(), _layout.getApplication(), _layout, _declaredParameters,
			includeId, arguments, contextExpander, callback);
	}

	private Protocol protocol() {
		return _layout.getProtocol();
	}

}
