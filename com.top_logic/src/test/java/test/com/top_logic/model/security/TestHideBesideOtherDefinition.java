/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.model.security;


import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.layout.form.model.FieldMode;
import com.top_logic.model.security.AccessParentConfig;
import com.top_logic.model.security.ContainerAccessParent;
import com.top_logic.model.security.HideBesideOtherDefinition;
import com.top_logic.model.security.SelfAccessParent;

/**
 * Test for {@link HideBesideOtherDefinition}: a form offers either an access parent or marks of
 * the type's own, never both; the grants are offered beside an access parent.
 */
@SuppressWarnings("javadoc")
public class TestHideBesideOtherDefinition extends TestCase {

	private final HideBesideOtherDefinition _mode = new HideBesideOtherDefinition();

	private final HideBesideOtherDefinition.ForGrants _grantsMode = new HideBesideOtherDefinition.ForGrants();

	public void testMarksAreHiddenWhileTheTypeDelegates() {
		Object container = TypedConfiguration.newConfigItem(ContainerAccessParent.Config.class);

		assertEquals(FieldMode.INVISIBLE, _mode.invoke(false, container));
		assertEquals("A mark that is set stays, so that the conflict can be resolved.", FieldMode.ACTIVE,
			_mode.invoke(true, container));
	}

	public void testMarksStayWhileTheAccessParentDoesNotDelegate() {
		Object self = TypedConfiguration.newConfigItem(SelfAccessParent.Config.class);

		assertEquals("Self delegates to nothing.", FieldMode.ACTIVE, _mode.invoke(false, self));
		assertEquals("No kind chosen yet.", FieldMode.ACTIVE, _mode.invoke(false, (Object) null));
	}

	public void testAccessParentIsHiddenWhileTheTypeHasAMark() {
		assertEquals(FieldMode.INVISIBLE, _mode.invoke(null, false, true));
		assertEquals(FieldMode.ACTIVE, _mode.invoke(null, false, false));
	}

	public void testAnAccessParentBeingChosenIsNotHidden() {
		AccessParentConfig accessParent = TypedConfiguration.newConfigItem(AccessParentConfig.class);

		assertEquals("A value that is set is never hidden.", FieldMode.ACTIVE, _mode.invoke(accessParent, true, false));
	}

	/**
	 * The grants are offered beside every access parent: they decide the operations it does not
	 * delegate, or all operations beside it when it names the object itself.
	 */
	public void testGrantsAreOfferedBesideAnAccessParent() {
		assertEquals(FieldMode.ACTIVE, _grantsMode.invoke(false));
	}

	public void testGrantsAreHiddenWithoutSecurity() {
		assertEquals(FieldMode.INVISIBLE, _grantsMode.invoke(true));
	}

	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestHideBesideOtherDefinition.class, TypeIndex.Module.INSTANCE));
	}

}
