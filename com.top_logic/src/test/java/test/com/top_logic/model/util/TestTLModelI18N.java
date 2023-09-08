/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.model.util;

import java.util.Locale;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.TLTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.model.TLClass;
import com.top_logic.model.TLClassProperty;
import com.top_logic.model.TLModule;
import com.top_logic.model.annotate.util.AttributeSettings;
import com.top_logic.model.impl.TLModelImpl;
import com.top_logic.model.util.TLModelI18N;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.Resources;

/**
 * Test case for {@link TLModelI18N}
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestTLModelI18N extends TestCase {

	public void testNameAsLabelFallback() {
		TLModelImpl model = new TLModelImpl();
		model.addCoreModule();
		TLModule module = TLModelUtil.makeModule(model, "myModule");
		TLClass type = TLModelUtil.makeClass(module, "MyClass");
		TLClassProperty property =
			model.addClassProperty(type, "myProperty", model.getModule("tl.core").getType("String"));

		Resources bundle = Resources.getInstance(Locale.CHINESE);

		assertEquals("My property", TLModelI18N.getI18NName(bundle, property));
		assertEquals("My property", bundle.getString(TLModelI18N.getI18NKey(property)));
		assertEquals("My property", bundle.getString(TLModelI18N.getTableTitleKey(property)));
	}

	public static Test suite() {
		Test t = new TestSuite(TestTLModelI18N.class);
		t = ServiceTestSetup.createSetup(t, AttributeSettings.Module.INSTANCE);
		return TLTestSetup.createTLTestSetup(t);
	}

}
