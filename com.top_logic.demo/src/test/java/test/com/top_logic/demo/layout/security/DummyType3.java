/*
 * SPDX-FileCopyrightText: 2004 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.demo.layout.security;

import com.top_logic.element.model.DynamicModelService;
import com.top_logic.element.structured.StructuredElement;
import com.top_logic.element.structured.StructuredElementFactory;
import com.top_logic.element.structured.wrap.AttributedStructuredElementWrapper;
import com.top_logic.knowledge.objects.KnowledgeObject;

/**
 * Just a dummy class used as type in TestDelegateProjectLayout
 * 
 * @author    <a href="mailto:tsa@top-logic.com">Theo Sattler</a>
 * 
 */
public class DummyType3 extends AttributedStructuredElementWrapper {

	public DummyType3(KnowledgeObject ko) {
		super(ko);
	}

	public static DummyType3 newDummyType3(String name) {
		StructuredElementFactory factory =
			(StructuredElementFactory) DynamicModelService.getFactoryFor("compoundSecurity");
		StructuredElement root = factory.getRoot();
		return (DummyType3) root.createChild(name, "CompoundSecurityChild3");
	}

}