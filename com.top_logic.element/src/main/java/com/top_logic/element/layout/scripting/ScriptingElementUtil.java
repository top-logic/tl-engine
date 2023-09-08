/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.scripting;

import java.util.Set;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.element.meta.AttributeOperations;
import com.top_logic.element.meta.MetaElementUtil;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLClass;
import com.top_logic.element.structured.StructuredElementFactory;
import com.top_logic.element.structured.wrap.AttributedStructuredElementWrapper;
import com.top_logic.knowledge.wrap.Wrapper;

/**
 * Utilities for the ScriptingFramework that require classes from <code>com.top_logic.element</code>.
 * 
 * @author     <a href="mailto:Jan.Stolzenburg@top-logic.com">Jan.Stolzenburg</a>
 */
public class ScriptingElementUtil {

	public static TLClass getMetaElement(String structureName, String metaElementTypeName) {
		StructuredElementFactory structure = StructuredElementFactory.getInstanceForStructure(structureName);
		AttributedStructuredElementWrapper structureRoot = (AttributedStructuredElementWrapper) structure.getRoot();
		return structureRoot.getMetaElement(metaElementTypeName);
	}

	public static Set<Wrapper> navigateBackwards(TLClass metaElement, String attributeName, Wrapper wrapper) {
		try {
			TLStructuredTypePart metaAttribute = MetaElementUtil.getMetaAttribute(metaElement, attributeName);
			return CollectionUtil.dynamicCastView(Wrapper.class, AttributeOperations.getReferers(wrapper, metaAttribute));
		}
		catch (NoSuchAttributeException exception) {
			throw new RuntimeException(exception);
		}
	}

}
