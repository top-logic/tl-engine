/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.model.annotate;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.model.annotate.AnnotationContainer;
import com.top_logic.model.annotate.TLSize;
import com.top_logic.model.annotate.ui.BooleanDisplay;

/**
 * Test case for {@link AnnotationContainer}.
 */
@SuppressWarnings("javadoc")
public class TestAnnotationContainer extends TestCase {

	public void testLookup() {
		assertNull(AnnotationContainer.EMPTY.getAnnotation(TLSize.class));

		TLSize size1 = TypedConfiguration.newConfigItem(TLSize.class);
		TLSize size2 = TypedConfiguration.newConfigItem(TLSize.class);
		BooleanDisplay display = TypedConfiguration.newConfigItem(BooleanDisplay.class);

		assertSame(size1, AnnotationContainer.EMPTY.with(size1).getAnnotation(TLSize.class));
		assertSame(size2, AnnotationContainer.EMPTY.with(size1).with(size2).getAnnotation(TLSize.class));
		assertNull(AnnotationContainer.EMPTY.with(size1).with(size2).without(TLSize.class).getAnnotation(TLSize.class));
		assertSame(size2, AnnotationContainer.EMPTY.with(size1).with(size2).without(BooleanDisplay.class)
			.getAnnotation(TLSize.class));

		assertSame(size1, AnnotationContainer.EMPTY.with(size1).with(display).getAnnotation(TLSize.class));
		assertNull(
			AnnotationContainer.EMPTY.with(size1).with(display).without(TLSize.class).getAnnotation(TLSize.class));
		assertSame(display, AnnotationContainer.EMPTY.with(size1).with(display).without(TLSize.class)
			.getAnnotation(BooleanDisplay.class));
	}

	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestAnnotationContainer.class, TypeIndex.Module.INSTANCE));
	}
}
