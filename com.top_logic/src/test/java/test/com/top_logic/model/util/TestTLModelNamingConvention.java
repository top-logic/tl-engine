/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.model.util;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.model.TLClassifier;
import com.top_logic.model.TLEnumeration;
import com.top_logic.model.TLModule;
import com.top_logic.model.config.JavaPackage;
import com.top_logic.model.impl.TLModelImpl;
import com.top_logic.model.util.TLModelNamingConvention;
import com.top_logic.model.util.TLModelUtil;

/**
 * Test case for {@link TLModelNamingConvention}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestTLModelNamingConvention extends TestCase {

	private TLModule _module;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		TLModelImpl model = new TLModelImpl();
		_module = TLModelUtil.makeModule(model, "my.module");
		JavaPackage javaPackage = TypedConfiguration.newConfigItem(JavaPackage.class);
		javaPackage.setInterfacePackage("my.pkg");
		javaPackage.setImplementationPackage("my.pkg.impl");
		_module.setAnnotation(javaPackage);
	}

	public void testEnumClassName() {
		assertEquals("my.pkg.AddressSpace",
			TLModelNamingConvention.enumClassName(TLModelUtil.addEnumeration(_module, "AddressSpace")));
		assertEquals("my.pkg.FederalStates",
			TLModelNamingConvention.enumClassName(TLModelUtil.addEnumeration(_module, "federal.states")));
	}

	public void testEnumClassNameWithoutPackage() {
		TLModule plain = TLModelUtil.makeModule(_module.getModel(), "plain");
		assertNull(TLModelNamingConvention.enumClassName(TLModelUtil.addEnumeration(plain, "AddressSpace")));
	}

	/**
	 * Classifier names that are ambiguous when concatenated with their enumeration name are
	 * distinct within their enumeration class.
	 */
	public void testClassifierMembers() {
		TLEnumeration addressSpace = TLModelUtil.addEnumeration(_module, "AddressSpace");
		TLClassifier lcFilterMemory = TLModelUtil.addClassifier(addressSpace, "LcFilterMemory");
		TLEnumeration memoryAddressSpace = TLModelUtil.addEnumeration(_module, "MemoryAddressSpace");
		TLClassifier lcFilter = TLModelUtil.addClassifier(memoryAddressSpace, "LcFilter");

		assertEquals("LC_FILTER_MEMORY_CLASSIFIER", TLModelNamingConvention.classifierConstant(lcFilterMemory));
		assertEquals("lcFilterMemoryClassifier", TLModelNamingConvention.classifierAccessor(lcFilterMemory));
		assertEquals("LC_FILTER_CLASSIFIER", TLModelNamingConvention.classifierConstant(lcFilter));
		assertEquals("lcFilterClassifier", TLModelNamingConvention.classifierAccessor(lcFilter));

		// The concatenated names of the factory are ambiguous.
		assertEquals(TLModelNamingConvention.classifierNameConstant(lcFilterMemory),
			TLModelNamingConvention.classifierNameConstant(lcFilter));
		assertEquals(TLModelNamingConvention.classifierAccessorName(lcFilterMemory),
			TLModelNamingConvention.classifierAccessorName(lcFilter));
	}

	public void testClassifierMembersWithSeparators() {
		TLEnumeration states = TLModelUtil.addEnumeration(_module, "federal.states");
		TLClassifier bw = TLModelUtil.addClassifier(states, "Baden-Wuerttemberg");
		TLClassifier rp = TLModelUtil.addClassifier(states, "Rheinland.Pfalz");
		TLClassifier upper = TLModelUtil.addClassifier(states, "NRW");

		assertEquals("BADEN_WUERTTEMBERG_CLASSIFIER", TLModelNamingConvention.classifierConstant(bw));
		assertEquals("badenWuerttembergClassifier", TLModelNamingConvention.classifierAccessor(bw));
		assertEquals("RHEINLAND_PFALZ_CLASSIFIER", TLModelNamingConvention.classifierConstant(rp));
		assertEquals("rheinlandPfalzClassifier", TLModelNamingConvention.classifierAccessor(rp));
		assertEquals("NRW_CLASSIFIER", TLModelNamingConvention.classifierConstant(upper));
		assertEquals("nrwClassifier", TLModelNamingConvention.classifierAccessor(upper));
	}

	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestTLModelNamingConvention.class, TypeIndex.Module.INSTANCE));
	}

}
