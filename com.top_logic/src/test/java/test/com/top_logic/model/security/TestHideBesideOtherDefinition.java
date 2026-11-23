/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.model.security;

import java.util.Arrays;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.layout.form.model.FieldMode;
import com.top_logic.model.security.AccessParentConfig;
import com.top_logic.model.security.AccessParentDefinition;
import com.top_logic.model.security.AnyOfAccessParent;
import com.top_logic.model.security.ContainerAccessParent;
import com.top_logic.model.security.HideBesideOtherDefinition;
import com.top_logic.model.security.SelfAccessParent;
import com.top_logic.model.security.TargetAccessParent;
import com.top_logic.tool.boundsec.CommandGroupReference;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;

/**
 * Test for {@link HideBesideOtherDefinition}: a form offers either an access parent or grants and
 * marks of the type's own, never both.
 */
@SuppressWarnings("javadoc")
public class TestHideBesideOtherDefinition extends TestCase {

	private final HideBesideOtherDefinition _mode = new HideBesideOtherDefinition();

	private final HideBesideOtherDefinition _grantsMode = new HideBesideOtherDefinition.ForGrants();

	public void testOwnDefinitionIsHiddenWhileTheTypeDelegates() {
		Object container = TypedConfiguration.newConfigItem(ContainerAccessParent.Config.class);

		assertEquals(FieldMode.INVISIBLE, _mode.invoke(false, container));
		assertEquals(FieldMode.INVISIBLE, _grantsMode.invoke(false, List.of(), container));
	}

	public void testOwnDefinitionStaysWhileTheAccessParentDoesNotDelegate() {
		Object self = TypedConfiguration.newConfigItem(SelfAccessParent.Config.class);

		assertEquals("Self delegates to nothing.", FieldMode.ACTIVE, _mode.invoke(false, self));
		assertEquals("No kind chosen yet.", FieldMode.ACTIVE, _mode.invoke(false, (Object) null));
	}

	public void testAccessParentIsHiddenWhileTheTypeHasGrantsOrAMark() {
		Object grant = new Object();

		assertEquals(FieldMode.INVISIBLE, _mode.invoke(null, List.of(grant), false, false));
		assertEquals(FieldMode.INVISIBLE, _mode.invoke(null, List.of(), false, true));
		assertEquals(FieldMode.ACTIVE, _mode.invoke(null, List.of(), false, false));
	}

	public void testAnAccessParentBeingChosenIsNotHidden() {
		AccessParentConfig accessParent = TypedConfiguration.newConfigItem(AccessParentConfig.class);

		assertEquals("A value that is set is never hidden.", FieldMode.ACTIVE,
			_mode.invoke(accessParent, List.of(new Object()), false, false));
	}

	public void testBothWaysTakenShowBoth() {
		Object container = TypedConfiguration.newConfigItem(ContainerAccessParent.Config.class);

		assertEquals("Grants that are set stay, so that the conflict can be resolved.", FieldMode.ACTIVE,
			_grantsMode.invoke(false, List.of(new Object()), container));
		assertEquals(FieldMode.ACTIVE, _mode.invoke(true, container));
	}

	public void testGrantsVisibleBesideRestrictedParent() {
		TargetAccessParent.Config restricted = TypedConfiguration.newConfigItem(TargetAccessParent.Config.class);
		restricted.setOperations(List.of(new CommandGroupReference(SimpleBoundCommandGroup.READ_NAME)));
		TargetAccessParent.Config unrestricted = TypedConfiguration.newConfigItem(TargetAccessParent.Config.class);

		assertEquals("The operations not delegated are granted by the type itself.", FieldMode.ACTIVE,
			_grantsMode.invoke(false, List.of(), restricted));
		assertEquals(FieldMode.INVISIBLE, _grantsMode.invoke(false, List.of(), unrestricted));
		assertEquals("A restricted access parent leaves no room for a mark.", FieldMode.INVISIBLE,
			_mode.invoke(false, restricted));
	}

	public void testGrantsVisibleBesideRestrictedAnyOf() {
		AnyOfAccessParent.Config restricted = anyOf(target(SimpleBoundCommandGroup.READ_NAME),
			anyOf(target(SimpleBoundCommandGroup.WRITE_NAME)));
		AnyOfAccessParent.Config unrestricted = anyOf(target(SimpleBoundCommandGroup.READ_NAME),
			TypedConfiguration.newConfigItem(ContainerAccessParent.Config.class));

		assertEquals("All candidates leave some operations to the type itself.", FieldMode.ACTIVE,
			_grantsMode.invoke(false, List.of(), restricted));
		assertEquals("The container candidate delegates all operations.", FieldMode.INVISIBLE,
			_grantsMode.invoke(false, List.of(), unrestricted));
	}

	public void testGrantsVisibleBesideAnyOfWithoutContainer() {
		assertEquals("Create is granted by the type itself.", FieldMode.ACTIVE,
			_grantsMode.invoke(false, List.of(), anyOf(target(), target())));
		assertEquals("The container candidate delegates Create.", FieldMode.INVISIBLE,
			_grantsMode.invoke(false, List.of(), anyOf(TypedConfiguration.newConfigItem(ContainerAccessParent.Config.class),
				target())));
		assertEquals("A single candidate is the candidate itself.", FieldMode.INVISIBLE,
			_grantsMode.invoke(false, List.of(), anyOf(target())));
	}

	private static TargetAccessParent.Config target(String... operations) {
		TargetAccessParent.Config result = TypedConfiguration.newConfigItem(TargetAccessParent.Config.class);
		result.setOperations(Arrays.stream(operations).map(CommandGroupReference::new).toList());
		return result;
	}

	@SafeVarargs
	private static AnyOfAccessParent.Config anyOf(PolymorphicConfiguration<? extends AccessParentDefinition>... candidates) {
		AnyOfAccessParent.Config result = TypedConfiguration.newConfigItem(AnyOfAccessParent.Config.class);
		result.getCandidates().addAll(Arrays.asList(candidates));
		return result;
	}

	public void testGrantsAreHiddenWithoutSecurity() {
		assertEquals(FieldMode.INVISIBLE, _grantsMode.invoke(true, List.of(new Object()), null));
	}

	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestHideBesideOtherDefinition.class, TypeIndex.Module.INSTANCE));
	}

}
