/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.element.meta;

import junit.framework.Test;

import com.top_logic.model.TLModule;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;
import com.top_logic.util.model.ModelService;

/**
 * Test class for {@link TLStructuredTypePart}.
 * 
 * @author <a href=mailto:jst@top-logic.com>Jan Stolzenburg</a>
 */
@SuppressWarnings("javadoc")
public class TestTLStructuredTypePart extends TestPersistentModelPart {

	public void testNoException() {
		/* Iterating over all instances might seem a lot. But this is only the model. And it takes
		 * less than 0,05 seconds on a developer laptop. */
		for (TLModule tlModule : ModelService.getApplicationModel().getModules()) {
			for (TLType tlType : tlModule.getTypes()) {
				if (tlType instanceof TLStructuredType) {
					TLStructuredType structuredType = (TLStructuredType) tlType;
					for (TLStructuredTypePart attribute : structuredType.getAllParts()) {
						// Calling any of these methods on any existing instance must not throw an exception:
						attribute.isBag();
						attribute.isDerived();
						attribute.isMandatory();
						attribute.isMultiple();
						attribute.isOrdered();
						attribute.isOverride();
						assertNotNull(attribute.getAnnotations());
						assertNotNull(attribute.getDefinition());
						assertNotNull(attribute.getModel());
						assertNotNull(attribute.getModelKind());
						assertNotNull(attribute.getName());
						assertNotNull(attribute.getOwner());
						assertNotNull(attribute.getType());
					}
				}
			}
		}
	}

	public static Test suite() {
		return suite(TestTLStructuredTypePart.class);
	}

}
