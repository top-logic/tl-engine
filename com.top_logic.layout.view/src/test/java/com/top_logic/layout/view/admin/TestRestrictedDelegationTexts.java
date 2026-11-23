/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.admin;

import java.util.List;
import java.util.Locale;
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
import com.top_logic.basic.xml.TagUtil;
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
import com.top_logic.model.security.AnyOfRelation;
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

	private static final AnyOfRelation ANY_OF = new AnyOfRelation(List.of(ContainerRelation.ANY,
		new RestrictedRelation(ContainerRelation.DEFAULT, Set.of(SimpleBoundCommandGroup.READ))));

	private static final AnyOfRelation RESTRICTED_ANY_OF = new AnyOfRelation(List.of(
		new RestrictedRelation(ContainerRelation.ANY, Set.of(SimpleBoundCommandGroup.WRITE)),
		new RestrictedRelation(ContainerRelation.DEFAULT, Set.of(SimpleBoundCommandGroup.READ))));

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

	/**
	 * The label of a restricted relation names the delegated operations by their labels in the
	 * language of the user, ordered by their names.
	 */
	public void testLabelOfRestrictedRelation() {
		AccessParentFunction relation = new RestrictedRelation(ContainerRelation.ANY,
			Set.of(SimpleBoundCommandGroup.WRITE, SimpleBoundCommandGroup.READ));
		Resources german = Resources.getInstance(Locale.GERMAN);
		String label = german.getString(relation.getLabel());
		String read = german.getString(com.top_logic.tool.boundsec.simple.I18NConstants.COMMAND_GROUP_NAMES
			.key(SimpleBoundCommandGroup.READ_NAME));
		String write = german.getString(com.top_logic.tool.boundsec.simple.I18NConstants.COMMAND_GROUP_NAMES
			.key(SimpleBoundCommandGroup.WRITE_NAME));
		assertFalse("Precondition: the operations have labels other than their names.",
			read.equals(SimpleBoundCommandGroup.READ_NAME) || write.equals(SimpleBoundCommandGroup.WRITE_NAME));
		assertTrue("The operations are named by their labels: " + label, label.endsWith(read + ", " + write));
		assertTrue("The relation is named: " + label,
			label.startsWith(german.getString(ContainerRelation.ANY.getLabel())));
	}

	/**
	 * An operation decided by several objects names all of them; an operation decided by a single
	 * object keeps its text.
	 */
	public void testReasonNamesEveryRoleHolder() {
		TLModule module = TLModelUtil.addModule(new TLModelImpl(), "test.explain");
		TLClass object = TLModelUtil.addClass(module, "Part");
		TLClass folder = TLModelUtil.addClass(module, "Folder");
		TLClass document = TLModelUtil.addClass(module, "Document");
		OperationDecision several = decision(SimpleBoundCommandGroup.READ, List.of(folder, document));
		OperationDecision single = decision(SimpleBoundCommandGroup.WRITE, List.of(folder));
		AccessExplanation explanation = explanation(object, folder, List.of(several, single));

		String reason = EffectiveAccessFunctions.reason(explanation, several, LABELS);
		String expected = translate(I18NConstants.EFFECTIVE_ACCESS_CHECKED_ON_SEVERAL__OPERATION_OBJECTS_REASON.fill(
			translate(SecurityGrantFunctions.labelKey(SimpleBoundCommandGroup.READ)), "Folder, Document",
			translate(I18NConstants.EFFECTIVE_ACCESS_DENIED_RESTRICTED)));
		assertEquals("Every object deciding the operation is named.", expected, reason);

		String singleReason = EffectiveAccessFunctions.reason(explanation, single, LABELS);
		String singleExpected = translate(I18NConstants.EFFECTIVE_ACCESS_CHECKED_ON__OPERATION_OBJECT_REASON.fill(
			translate(SecurityGrantFunctions.labelKey(SimpleBoundCommandGroup.WRITE)), "Folder",
			translate(I18NConstants.EFFECTIVE_ACCESS_DENIED_RESTRICTED)));
		assertEquals("A single object deciding keeps its text.", singleExpected, singleReason);
	}

	/**
	 * An access parent combining its candidates itself is told, an access parent combining them by
	 * OR is not.
	 */
	public void testCombiningParentIsTold() {
		TLModule module = TLModelUtil.addModule(new TLModelImpl(), "test.explain");
		TLClass object = TLModelUtil.addClass(module, "Both");
		AccessParentFunction combining = new AccessParentFunction() {
			@Override
			public TLObject resolve(TLObject delegating) {
				return null;
			}

			@Override
			public Set<TLClass> getParentTypes(TLClass type) {
				return null;
			}

			@Override
			public ResKey getLabel() {
				return ResKey.text("all of first, second");
			}

			@Override
			public boolean isDisjunctive() {
				return false;
			}
		};
		AccessExplanation explanation = new AccessExplanation(null, object, false, false, false, false, object,
			combining, List.of(), List.of(), List.of(), true);

		ResKey text = EffectiveAccessFunctions.combination(explanation);
		assertNotNull(text);
		assertEquals(I18NConstants.EFFECTIVE_ACCESS_COMBINED__PARENT.getKey(), text.plain().getKey());
		assertEquals("The combining access parent is named.",
			translate(I18NConstants.EFFECTIVE_ACCESS_COMBINED__PARENT.fill(combining.getLabel())), translate(text));

		assertNull("An access parent combining by OR has nothing to tell.",
			EffectiveAccessFunctions.combination(explanation(object, object, List.of())));
	}

	private static final Function<Object, String> LABELS = object -> ((TLClass) object).getName();

	private static AccessExplanation explanation(TLObject object, TLObject decidingObject,
			List<OperationDecision> decisions) {
		return new AccessExplanation(null, object, false, false, false, false, decidingObject, null, List.of(),
			decisions, List.of(), true);
	}

	private static OperationDecision decision(BoundCommandGroup operation, TLObject roleHolder) {
		return decision(operation, List.of(roleHolder));
	}

	private static OperationDecision decision(BoundCommandGroup operation, List<TLObject> roleHolders) {
		return new OperationDecision(operation, operation, roleHolders, false, Set.of(), Set.of(), Set.of(), true);
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

	/**
	 * An access parent with several candidates is told as such, naming every candidate, with the
	 * operations its candidates delegate when none delegates all of them.
	 */
	public void testGrantsOfAnyOf() {
		ResKey all = SecurityGrantFunctions.delegation(ANY_OF);
		assertEquals(I18NConstants.GRANTS_DELEGATED_ANY_OF__CANDIDATES.getKey(), all.plain().getKey());
		assertNamesCandidates(ANY_OF, translate(all));

		ResKey restricted = SecurityGrantFunctions.delegation(RESTRICTED_ANY_OF);
		assertEquals(I18NConstants.GRANTS_DELEGATED_RESTRICTED_ANY_OF__CANDIDATES_OPERATIONS.getKey(),
			restricted.plain().getKey());
		assertNamesCandidates(RESTRICTED_ANY_OF, translate(restricted));
		assertTrue("The delegated operations are named: " + translate(restricted),
			translate(restricted).contains(SecurityGrantFunctions.operationLabels(RESTRICTED_ANY_OF.getOperations())));
	}

	/**
	 * The coverage of a type with several access parents tells them as such, naming every
	 * candidate.
	 */
	public void testCoverageOfAnyOf() {
		TLModule module = TLModelUtil.addModule(new TLModelImpl(), "test.coverage");
		TLClass part = TLModelUtil.addClass(module, "Part");
		for (AnyOfRelation anyOf : List.of(ANY_OF, RESTRICTED_ANY_OF)) {
			TypeCoverage row = row(part, anyOf);
			ViewChannel findings = new DefaultViewChannel("findings");
			Detail detail = new Detail(null, null, null, null, findings, null, null);

			detail.show(part, Map.of(part, row)::get);

			String html = (String) findings.get();
			assertNamesCandidates(anyOf, html);
		}
		ResKey all = SecurityCoverageTable.delegation(row(part, ANY_OF));
		assertEquals(I18NConstants.COVERAGE_DELEGATED_ANY_OF__CANDIDATES.getKey(), all.plain().getKey());
		ResKey restricted = SecurityCoverageTable.delegation(row(part, RESTRICTED_ANY_OF));
		assertEquals(I18NConstants.COVERAGE_DELEGATED_RESTRICTED_ANY_OF__CANDIDATES_OPERATIONS.getKey(),
			restricted.plain().getKey());
		assertTrue("The delegated operations are named: " + translate(restricted),
			translate(restricted).contains(SecurityGrantFunctions.operationLabels(RESTRICTED_ANY_OF.getOperations())));
	}

	private static TypeCoverage row(TLClass type, AccessParentFunction accessParent) {
		return new TypeCoverage(type, null, null, accessParent, List.of(), Set.of(), List.of(), List.of(), List.of());
	}

	private static void assertNamesCandidates(AnyOfRelation anyOf, String text) {
		for (AccessParentFunction candidate : anyOf.candidates()) {
			String candidateLabel = TagUtil.encodeXML(translate(candidate.getLabel()));
			assertTrue("The candidate '" + candidateLabel + "' is named: " + text, text.contains(candidateLabel)
				|| text.contains(translate(candidate.getLabel())));
		}
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
