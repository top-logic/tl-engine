/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.element.boundsec.manager;

import com.top_logic.element.model.DynamicModelService;
import com.top_logic.element.structured.StructuredElement;
import com.top_logic.element.structured.StructuredElementFactory;
import com.top_logic.element.structured.wrap.AttributedStructuredElementWrapper;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.tool.boundsec.BoundObject;

/**
 * {@link AttributedStructuredElementWrapper} with an programmatic set security parent.
 * 
 * @since 5.7.4
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class SecurityChild extends AttributedStructuredElementWrapper {

	private BoundObject _securityParent;

	/**
	 * @see AttributedStructuredElementWrapper#AttributedStructuredElementWrapper(KnowledgeObject)
	 */
	public SecurityChild(KnowledgeObject ko) {
		super(ko);
	}

	private void setSecurityParent(BoundObject securityParent) {
		_securityParent = securityParent;
	}

	@Override
	public BoundObject getSecurityParent() {
		return _securityParent;
	}

	/**
	 * Creates a new {@link SecurityChild} with given security parent.
	 */
	public static SecurityChild newSecurityChild(String childName, BoundObject parent) {
		StructuredElementFactory factory = (StructuredElementFactory) DynamicModelService.getFactoryFor("securityChildren");
		StructuredElement securityChildrenRoot = factory.getRoot();
		SecurityChild child = (SecurityChild) securityChildrenRoot.createChild(childName, "SecurityChild");
		child.setSecurityParent(parent);
		return child;
	}

}

