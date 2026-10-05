/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.element.meta.options;

import java.util.List;

import junit.framework.Test;

import test.com.top_logic.element.meta.TestWithModelExtension;

import com.top_logic.element.config.annotation.TLOptions;
import com.top_logic.element.meta.AttributeOperations;
import com.top_logic.element.meta.SimpleEditContext;
import com.top_logic.element.meta.kbbased.filtergen.Generator;
import com.top_logic.layout.form.model.utility.ListOptionModel;
import com.top_logic.layout.form.model.utility.OptionModel;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.util.TLModelUtil;

/**
 * Test for the resolution of {@link TLOptions} in
 * {@link AttributeOperations#getOptions(TLStructuredTypePart)}: options of the attribute itself,
 * of an overridden attribute, and of the attribute's value type.
 */
@SuppressWarnings("javadoc")
public class TestTypeDefaultOptions extends TestWithModelExtension {

	private static final String MODULE = TestTypeDefaultOptions.class.getName();

	private TLModule _module;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_module = TLModelUtil.findModule(MODULE);
	}

	public void testTypeOptionsAsDefault() {
		Generator generator = AttributeOperations.getOptions(part("Holder", "typeDefault"));
		assertEquals("type", id(generator));
	}

	public void testTypeOptionsCached() {
		Generator generator1 = AttributeOperations.getOptions(part("Holder", "typeDefault"));
		Generator generator2 = AttributeOperations.getOptions(part("Holder", "otherTypeDefault"));
		assertSame(generator1, generator2);
	}

	public void testLocalOptionsTakePrecedence() {
		Generator generator = AttributeOperations.getOptions(part("Holder", "local"));
		assertEquals("local", id(generator));
	}

	public void testNoOptions() {
		TLStructuredTypePart part = part("Holder", "plain");
		assertNull(AttributeOperations.getOptions(part));

		OptionModel<?> options = AttributeOperations.allOptions(SimpleEditContext.createContext(part));
		assertEquals(List.of(), ((ListOptionModel<?>) options).getBaseModel());
	}

	public void testAllOptionsUsesTypeOptions() {
		OptionModel<?> options =
			AttributeOperations.allOptions(SimpleEditContext.createContext(part("Holder", "typeDefault")));
		assertEquals(List.of("type"), ((ListOptionModel<?>) options).getBaseModel());
	}

	public void testOverrideInheritsOptions() {
		Generator generator = AttributeOperations.getOptions(part("Sub", "ref"));
		assertEquals("base", id(generator));
	}

	private TLStructuredTypePart part(String className, String partName) {
		TLClass type = (TLClass) _module.getType(className);
		TLStructuredTypePart part = type.getPart(partName);
		assertNotNull("No part " + className + "#" + partName, part);
		return part;
	}

	private static String id(Generator generator) {
		assertNotNull("No options.", generator);
		return ((TestIdGenerator) generator).getConfig().getId();
	}

	public static Test suite() {
		return suite(new ModelExtensionTestSetup(TestTypeDefaultOptions.class));
	}

}
