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
public class DummyType2 extends AttributedStructuredElementWrapper {

	public DummyType2(KnowledgeObject ko) {
		super(ko);
	}

	public static DummyType2 newDummyType2(String name) {
		StructuredElementFactory factory =
			(StructuredElementFactory) DynamicModelService.getFactoryFor("compoundSecurity");
		StructuredElement root = factory.getRoot();
		return (DummyType2) root.createChild(name, "CompoundSecurityChild2");
	}

}
