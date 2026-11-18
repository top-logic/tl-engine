/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.react.control.table;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.layout.AbstractResourceProvider;
import com.top_logic.layout.ResourceProvider;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactValueColor;
import com.top_logic.layout.react.control.table.ReactResourceCellControl;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.model.annotate.ui.ValueColor;
import com.top_logic.model.annotate.ui.ValueColorProvider;

/**
 * Tests which {@link ReactValueColor#ROLE color role} a {@link ReactResourceCellControl} sends for
 * its value: the one its {@link ResourceProvider} decides if that is a {@link ValueColorProvider},
 * the color of the value in the model otherwise.
 */
public class TestResourceCellColorRole extends TestCase {

	/** The value the {@link ColoringProvider} gives a color. */
	private static final String COLORED = "colored";

	/** A value no provider gives a color. */
	private static final String PLAIN = "plain";

	/** A {@link ResourceProvider} deciding the color of the values it displays. */
	private static final class ColoringProvider extends AbstractResourceProvider implements ValueColorProvider {

		@Override
		public String getLabel(Object object) {
			return String.valueOf(object);
		}

		@Override
		public ValueColor colorOf(Object value) {
			return COLORED.equals(value) ? ValueColor.CATEGORY_3 : null;
		}
	}

	/** A {@link ResourceProvider} without an opinion about colors. */
	private static final ResourceProvider LABELS = new AbstractResourceProvider() {
		@Override
		public String getLabel(Object object) {
			return String.valueOf(object);
		}
	};

	/** A cell exposing the state it sends to the client. */
	private static final class TestCell extends ReactResourceCellControl {

		TestCell(ReactContext context, Object value, ResourceProvider provider) {
			super(context, value, provider, false, true, false);
		}

		Object role() {
			return getState(ReactValueColor.ROLE);
		}
	}

	private ReactContext _context;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_context = new DefaultReactContext("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test"));
	}

	@Override
	protected void tearDown() throws Exception {
		_context = null;
		super.tearDown();
	}

	/**
	 * Tests that a provider that is a {@link ValueColorProvider} decides the role of the value.
	 */
	public void testProviderDecidesRole() {
		TestCell cell = new TestCell(_context, COLORED, new ColoringProvider());
		assertEquals(ValueColor.CATEGORY_3.getExternalName(), cell.role());
	}

	/**
	 * Tests that a value the coloring provider answers no color for is sent without a role.
	 */
	public void testProviderWithoutColorSendsNoRole() {
		TestCell cell = new TestCell(_context, PLAIN, new ColoringProvider());
		assertNull(cell.role());
	}

	/**
	 * Tests that the role follows the displayed value.
	 */
	public void testRoleFollowsValue() {
		TestCell cell = new TestCell(_context, PLAIN, new ColoringProvider());
		cell.update(COLORED);
		assertEquals(ValueColor.CATEGORY_3.getExternalName(), cell.role());
		cell.update(PLAIN);
		assertNull(cell.role());
	}

	/**
	 * Tests that a provider that is no {@link ValueColorProvider} leaves the role to the model: a
	 * value without a color in the model is sent without one.
	 */
	public void testPlainProviderUsesModelColor() {
		TestCell cell = new TestCell(_context, COLORED, LABELS);
		assertNull(cell.role());
	}

	/** Suite requiring the resources the cell resolves its display from. */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestResourceCellColorRole.class,
				ThreadContextManager.Module.INSTANCE, ResourcesModule.Module.INSTANCE));
	}

}
