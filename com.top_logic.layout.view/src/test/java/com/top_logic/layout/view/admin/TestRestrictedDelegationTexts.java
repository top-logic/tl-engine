/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.admin;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.TLTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.module.ModuleException;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.element.boundsec.manager.coverage.TypeCoverage;
import com.top_logic.element.boundsec.manager.explain.AccessExplanation;
import com.top_logic.element.boundsec.manager.explain.OperationDecision;
import com.top_logic.layout.view.admin.SecurityCoverageTable.Detail;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLReference;
import com.top_logic.model.impl.TLModelImpl;
import com.top_logic.model.security.AccessParentFunction;
import com.top_logic.model.security.ContainerRelation;
import com.top_logic.model.security.RestrictedRelation;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;
import com.top_logic.util.Resources;

/**
 * Test for the texts of the access administration describing an access parent restricted to some
 * operations: they name the delegated operations instead of claiming that the type delegates every
 * access decision.
 */
@SuppressWarnings("javadoc")
public class TestRestrictedDelegationTexts extends TestCase {

	private static final AccessParentFunction RESTRICTED =
		new RestrictedRelation(ContainerRelation.ANY, Set.of(SimpleBoundCommandGroup.READ));

	public void testGrantsOfRestrictedType() {
		ResKey restricted = SecurityGrantFunctions.delegation(RESTRICTED);
		assertEquals(I18NConstants.GRANTS_DELEGATED_RESTRICTED__PARENT_OPERATIONS.getKey(), restricted.plain().getKey());
		assertTrue("The delegated operations are named: " + translate(restricted),
			translate(restricted).contains(label(SimpleBoundCommandGroup.READ_NAME)));

		ResKey unrestricted = SecurityGrantFunctions.delegation(ContainerRelation.ANY);
		assertEquals("A type delegating everything keeps its text.", I18NConstants.GRANTS_DELEGATED__PARENT.getKey(),
			unrestricted.plain().getKey());
	}

	public void testEffectiveAccessOfRestrictedType() {
		ResKey restricted = EffectiveAccessFunctions.delegation("Entry", "Catalog", RESTRICTED);
		assertEquals(I18NConstants.EFFECTIVE_ACCESS_DELEGATED_RESTRICTED__TYPE_OBJECT_OPERATIONS.getKey(),
			restricted.plain().getKey());
		assertTrue("The delegated operations are named: " + translate(restricted),
			translate(restricted).contains(label(SimpleBoundCommandGroup.READ_NAME)));

		ResKey unrestricted = EffectiveAccessFunctions.delegation("Entry", "Catalog", ContainerRelation.ANY);
		assertEquals("A type delegating everything keeps its text.",
			I18NConstants.EFFECTIVE_ACCESS_DELEGATED__TYPE_OBJECT.getKey(), unrestricted.plain().getKey());
	}

	/**
	 * An object deciding Read itself, whose access parent decides Write, is described with the
	 * object deciding Write, although the explanation names the object itself as deciding object.
	 */
	public void testEffectiveAccessOfRestrictedTypeDecidingReadItself() {
		TLModule module = TLModelUtil.addModule(new TLModelImpl(), "test.explain");
		TLClass object = TLModelUtil.addClass(module, "Entry");
		TLClass parentObject = TLModelUtil.addClass(module, "Catalog");
		AccessParentFunction writeOnly =
			new RestrictedRelation(ContainerRelation.ANY, Set.of(SimpleBoundCommandGroup.WRITE));
		AccessExplanation explanation = explanation(object, object, List.of(
			decision(SimpleBoundCommandGroup.READ, object),
			decision(SimpleBoundCommandGroup.WRITE, parentObject)));

		ResKey restricted = EffectiveAccessFunctions.delegation("Entry", explanation, writeOnly, LABELS);
		assertNotNull("The delegated operations are told although Read is decided by the object.", restricted);
		assertEquals(I18NConstants.EFFECTIVE_ACCESS_DELEGATED_RESTRICTED__TYPE_OBJECT_OPERATIONS.getKey(),
			restricted.plain().getKey());
		assertEquals("The object deciding the delegated operation is named.", "Catalog", restricted.arguments()[1]);

		assertNull("A type without access parent has nothing to tell.",
			EffectiveAccessFunctions.delegation("Entry", explanation, null, LABELS));
		AccessExplanation delegating = explanation(object, parentObject, List.of());
		assertEquals("A type delegating everything keeps its text.",
			I18NConstants.EFFECTIVE_ACCESS_DELEGATED__TYPE_OBJECT.getKey(),
			EffectiveAccessFunctions.delegation("Entry", delegating, ContainerRelation.ANY, LABELS).plain().getKey());
	}

	private static final Function<Object, String> LABELS = object -> ((TLClass) object).getName();

	private static AccessExplanation explanation(TLObject object, TLObject decidingObject,
			List<OperationDecision> decisions) {
		return new AccessExplanation(null, object, false, false, false, false, decidingObject, List.of(), decisions,
			List.of(), true);
	}

	private static OperationDecision decision(BoundCommandGroup operation, TLObject roleHolder) {
		return new OperationDecision(operation, operation, roleHolder, false, Set.of(), Set.of(), Set.of(), true);
	}

	/**
	 * A restricted access parent to whichever container holds the object lists the containers and
	 * the delegated operations.
	 */
	public void testCoverageOfRestrictedAnyContainer() {
		TLModule module = TLModelUtil.addModule(new TLModelImpl(), "test.coverage");
		TLClass part = TLModelUtil.addClass(module, "Part");
		TLClass container = TLModelUtil.addClass(module, "Container");
		TLReference parts = TLModelUtil.addForwardReference(container, "parts", part);
		TypeCoverage row =
			new TypeCoverage(part, null, null, RESTRICTED, List.of(parts), Set.of(), List.of(), List.of(), List.of());
		ViewChannel findings = new DefaultViewChannel("findings");
		Detail detail = new Detail(null, null, null, null, findings, null, null);

		detail.show(part, Map.of(part, row)::get);

		String html = (String) findings.get();
		assertTrue("The containers are named: " + html, html.contains(TLModelUtil.qualifiedName(parts)));
		assertTrue("The delegated operations are named: " + html,
			html.contains(label(SimpleBoundCommandGroup.READ_NAME)));
		String unrestricted = Resources.getInstance()
			.getString(I18NConstants.COVERAGE_DELEGATED_CONTAINER__CONTAINERS.fill(TLModelUtil.qualifiedName(parts)));
		assertFalse("A restricted type does not delegate every access decision: " + html, html.contains(unrestricted));
	}

	private static String translate(ResKey key) {
		return Resources.getInstance().getString(key);
	}

	private static String label(String operation) {
		return translate(com.top_logic.tool.boundsec.simple.I18NConstants.COMMAND_GROUP_NAMES.key(operation));
	}

	/**
	 * Test suite requiring the {@link ResourcesModule}, which translates the texts.
	 */
	public static Test suite() throws ModuleException {
		return TLTestSetup.createTLTestSetup(ServiceTestSetup.createSetup(TestRestrictedDelegationTexts.class,
			TypeIndex.Module.INSTANCE, ResourcesModule.Module.INSTANCE));
	}

}
