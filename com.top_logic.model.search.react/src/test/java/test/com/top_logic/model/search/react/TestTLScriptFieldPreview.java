/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.model.search.react;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.layout.react.field.FieldSpec;
import com.top_logic.model.search.expr.config.ExprFormat;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.react.TLScriptFieldControlProvider;

/**
 * Tests how a {@link TLScriptFieldControlProvider} field is displayed where it has no room.
 */
public class TestTLScriptFieldPreview extends TestCase {

	private final TLScriptFieldControlProvider _provider = new TLScriptFieldControlProvider();

	private final FieldSpec _field = FieldSpec.of(Expr.class, "Script");

	/**
	 * A script editor always needs more room than a single line.
	 */
	public void testIsLarge() {
		assertTrue(_provider.isLarge(_field));
	}

	/**
	 * The first line of the script's source holding more than white space stands for it.
	 */
	public void testPreview() throws ConfigurationException {
		assertEquals("", _provider.previewText(_field, null));

		String source = "\n\n  x -> {\n    $x + 1;\n  }";
		Expr expr = ExprFormat.INSTANCE.getValue("test", source);
		assertEquals("x -> {", _provider.previewText(_field, expr));
	}

	/**
	 * Test suite providing the type index the script parser looks its functions up in.
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestTLScriptFieldPreview.class, TypeIndex.Module.INSTANCE));
	}

}
