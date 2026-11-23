/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.element.boundsec.manager.coverage;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.element.boundsec.manager.TestAllOfAccessParent;
import test.com.top_logic.element.util.ElementWebTestSetup;

import com.top_logic.base.security.device.TLSecurityDeviceManager;
import com.top_logic.basic.BufferingProtocol;
import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.config.ConfigUtil;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.NamePath;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.basic.config.constraint.check.ConstraintChecker;
import com.top_logic.basic.config.misc.PropertyValue;
import com.top_logic.basic.func.IGenericFunction;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.model.TLClass;
import com.top_logic.model.security.AccessParentConfig;
import com.top_logic.model.security.AccessParentDefinition;
import com.top_logic.model.security.AccessParentFunction;
import com.top_logic.model.security.AnyOfAccessParent;
import com.top_logic.model.security.AnyOfRelation;
import com.top_logic.model.security.ContainerRelation;
import com.top_logic.model.security.OperationRestriction;
import com.top_logic.model.security.RefererAccessParent;
import com.top_logic.model.security.RefererRelation;
import com.top_logic.model.security.RestrictedRelation;
import com.top_logic.model.security.SecurityConfigurationService;
import com.top_logic.model.security.SecurityConfigurationService.TLClassAccessRights;
import com.top_logic.model.security.TargetAccessParent;
import com.top_logic.model.security.TargetRelation;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.simple.CommandGroupRegistry;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;
import com.top_logic.util.Resources;

/**
 * Test for reading and resolving the <code>&lt;access-parent&gt;</code> setting of a
 * {@link TLClassAccessRights} entry, against the model module {@code TestSecurityCoverage} of the
 * element test application.
 *
 * <p>
 * The operations an access parent is restricted to are resolved by the
 * {@link SecurityConfigurationService}, so the tests of that restriction instantiate the service
 * with the entry under test alone.
 * </p>
 */
@SuppressWarnings("javadoc")
public class TestAccessParentConfig extends BasicTestCase {

	private static final String MODULE = "TestSecurityCoverage";

	private static final String LINKED = MODULE + ":Linked";

	private static final String TARGET_PARENT = "<target reference='" + LINKED + "#target'";

	private static final List<BoundCommandGroup> BUILT_IN = List.of(SimpleBoundCommandGroup.READ,
		SimpleBoundCommandGroup.WRITE, SimpleBoundCommandGroup.CREATE, SimpleBoundCommandGroup.DELETE,
		SimpleBoundCommandGroup.DISCOVER, SimpleBoundCommandGroup.EXPORT);

	public void testContainerWithReference() throws Exception {
		AccessParentFunction parent = resolveOk("ExplicitPart",
			"<access-parent><container reference='TestSecurityCoverage:Covered#explicitParts'/></access-parent>");
		assertEquals("TestSecurityCoverage:Covered#explicitParts",
			TLModelUtil.qualifiedName(((ContainerRelation) parent).composition()));
	}

	public void testContainerWithoutReference() throws Exception {
		assertSame(ContainerRelation.ANY,
			resolveOk("AnyContainerPart", "<access-parent><container/></access-parent>"));
	}

	public void testTarget() throws Exception {
		AccessParentFunction parent = resolveOk("Linked",
			"<access-parent><target reference='TestSecurityCoverage:Linked#target'/></access-parent>");
		assertEquals("TestSecurityCoverage:Linked#target",
			TLModelUtil.qualifiedName(((TargetRelation) parent).reference()));
	}

	public void testSelf() throws Exception {
		BufferingProtocol log = new BufferingProtocol();
		assertNull("A type deciding for itself has no access parent.",
			resolve(log, "SelfDeciding", "<access-parent><self/></access-parent>"));
		assertTrue(log.getErrors().toString(), log.getErrors().isEmpty());
	}

	public void testCompositionNotHoldingTheType() throws Exception {
		assertResolveError("Linked",
			"<access-parent><container reference='TestSecurityCoverage:Covered#explicitParts'/></access-parent>",
			"is not a composition holding objects of the type");
	}

	public void testMultipleTargetIsAccepted() throws Exception {
		AccessParentFunction parent = resolveOk("Covered",
			"<access-parent><target reference='TestSecurityCoverage:Covered#explicitParts'/></access-parent>");
		assertEquals("TestSecurityCoverage:Covered#explicitParts",
			TLModelUtil.qualifiedName(((TargetRelation) parent).reference()));
	}

	public void testTargetOfOtherType() throws Exception {
		assertResolveError("Linked",
			"<access-parent><target reference='TestSecurityCoverage:Covered#explicitParts'/></access-parent>",
			"is not a reference of the type");
	}

	public void testReferer() throws Exception {
		AccessParentFunction parent = resolveOk("Covered",
			"<access-parent><referer reference='" + LINKED + "#target'/></access-parent>");
		RefererRelation referer = (RefererRelation) parent;
		assertEquals(LINKED + "#target", TLModelUtil.qualifiedName(referer.reference()));
		assertEquals(Set.of(TLModelUtil.findType(LINKED)),
			referer.getParentTypes((TLClass) TLModelUtil.findType(MODULE + ":Covered")));
	}

	public void testRefererWithIncompatibleReferenceIsError() throws Exception {
		assertResolveError("Linked",
			"<access-parent><referer reference='" + LINKED + "#target'/></access-parent>",
			"does not point to objects of the type");
	}

	public void testRefererWithoutReference() {
		assertParseError("<class name='TestSecurityCoverage:Covered'><access-parent><referer/></access-parent></class>");
	}

	public void testRefererRestricted() throws Exception {
		BufferingProtocol log = new BufferingProtocol();
		AccessParentFunction parent = newService(log, "<class name='" + MODULE + ":Covered'><access-parent>"
			+ "<referer reference='" + LINKED + "#target' operations='Read'/></access-parent></class>")
				.getAccessParent((TLClass) TLModelUtil.findType(MODULE + ":Covered"));
		assertTrue(log.getErrors().toString(), log.getErrors().isEmpty());
		assertEquals(Set.of(SimpleBoundCommandGroup.READ), parent.getOperations());
		assertTrue(parent.getRelation() instanceof RefererRelation);
	}

	public void testAnyOfResolvesCandidates() throws Exception {
		BufferingProtocol log = new BufferingProtocol();
		AccessParentFunction parent = linkedParent(log, "<class name='" + LINKED + "'><access-parent><any-of>"
			+ "<container/>" + TARGET_PARENT + " operations='Read'/></any-of></access-parent></class>");
		assertTrue(log.getErrors().toString(), log.getErrors().isEmpty());

		AnyOfRelation anyOf = (AnyOfRelation) parent;
		assertEquals(2, anyOf.candidates().size());
		assertSame(ContainerRelation.ANY, anyOf.candidates().get(0));
		AccessParentFunction restricted = anyOf.candidates().get(1);
		assertTrue("The candidate is restricted: " + restricted, restricted instanceof RestrictedRelation);
		assertTrue(restricted.getRelation() instanceof TargetRelation);

		assertSame(anyOf, anyOf.getRelation());
		assertNull("A candidate delegates all operations.", anyOf.getOperations());
		for (BoundCommandGroup operation : BUILT_IN) {
			assertTrue(operation.getID(), anyOf.delegates(operation));
		}
		assertNull("The parent types of the container candidate are not known statically.",
			anyOf.getParentTypes((TLClass) TLModelUtil.findType(LINKED)));
		assertTrue(anyOf.isDisjunctive());
		assertTrue("The container candidate delegates Create.", anyOf.delegatesCreation());
	}

	public void testAnyOfOfRestrictedCandidates() throws Exception {
		BufferingProtocol log = new BufferingProtocol();
		AccessParentFunction parent = linkedParent(log, "<class name='" + LINKED + "'><access-parent><any-of>"
			+ "<container operations='Write'/>" + TARGET_PARENT + " operations='Read'/></any-of></access-parent>"
			+ "<grant operation='Delete'/></class>");
		assertTrue(log.getErrors().toString(), log.getErrors().isEmpty());
		assertEquals(Set.of(SimpleBoundCommandGroup.READ, SimpleBoundCommandGroup.WRITE), parent.getOperations());
		assertTrue(parent.delegates(SimpleBoundCommandGroup.DISCOVER));
		assertFalse(parent.delegates(SimpleBoundCommandGroup.DELETE));
		assertFalse("No container candidate delegates Create.", parent.delegatesCreation());
	}

	public void testAnyOfParentTypes() throws Exception {
		BufferingProtocol log = new BufferingProtocol();
		TLClass covered = (TLClass) TLModelUtil.findType(MODULE + ":Covered");
		AccessParentFunction parent = newService(log, "<class name='" + MODULE + ":Covered'><access-parent><any-of>"
			+ "<referer reference='" + LINKED + "#target'/>"
			+ "<target reference='" + MODULE + ":Covered#explicitParts'/>"
			+ "</any-of></access-parent></class>").getAccessParent(covered);
		assertTrue(log.getErrors().toString(), log.getErrors().isEmpty());
		assertEquals(Set.of(TLModelUtil.findType(LINKED), TLModelUtil.findType(MODULE + ":ExplicitPart")),
			parent.getParentTypes(covered));
		assertFalse("No candidate leads to a container.", parent.delegatesCreation());
	}

	public void testNestedAnyOfIsFlattened() throws Exception {
		BufferingProtocol log = new BufferingProtocol();
		AccessParentFunction parent = linkedParent(log, "<class name='" + LINKED + "'><access-parent><any-of>"
			+ "<container/><any-of>" + TARGET_PARENT + "/><container/></any-of></any-of></access-parent></class>");
		assertTrue(log.getErrors().toString(), log.getErrors().isEmpty());
		List<AccessParentFunction> candidates = ((AnyOfRelation) parent).candidates();
		assertEquals(3, candidates.size());
		assertTrue(candidates.get(1) instanceof TargetRelation);
	}

	public void testSelfInAnyOfIsError() throws Exception {
		BufferingProtocol log = new BufferingProtocol();
		AccessParentFunction parent = linkedParent(log, "<class name='" + LINKED + "'><access-parent><any-of>"
			+ "<self/>" + TARGET_PARENT + "/></any-of></access-parent></class>");
		assertNull("An unusable candidate leaves the type without access parent.", parent);
		assertError(log, "<self/>");
	}

	public void testUnusableCandidateIsError() throws Exception {
		BufferingProtocol log = new BufferingProtocol();
		AccessParentFunction parent = linkedParent(log, "<class name='" + LINKED + "'><access-parent><any-of>"
			+ "<container/>" + TARGET_PARENT + " operations='NoSuchGroup'/></any-of></access-parent></class>");
		assertNull("An unusable candidate leaves the type without access parent.", parent);
		assertError(log, "NoSuchGroup");
	}

	public void testEmptyAnyOfIsError() throws Exception {
		BufferingProtocol log = new BufferingProtocol();
		AccessParentFunction parent =
			linkedParent(log, "<class name='" + LINKED + "'><access-parent><any-of/></access-parent></class>");
		assertNull(parent);
		assertError(log, "no candidate");
	}

	public void testOperationsNotAllowedOnAnyOf() {
		assertParseError("<class name='" + LINKED + "'><access-parent><any-of operations='Read'>"
			+ TARGET_PARENT + "/></any-of></access-parent></class>");
	}

	public void testAnyOfRestrictedOnlyIfAllCandidatesRestricted() throws Exception {
		TLClassAccessRights allRestricted = read("<class name='" + LINKED + "'><access-parent><any-of>"
			+ "<container operations='Write'/>" + TARGET_PARENT + " operations='Read'/></any-of></access-parent></class>");
		assertTrue(AccessParentConfig.isRestricted(allRestricted.getAccessParent()));
		assertDelegatesExactly(allRestricted.getAccessParent(), "Read", "Write", "Discover");

		TLClassAccessRights oneUnrestricted = read("<class name='" + LINKED + "'><access-parent><any-of>"
			+ "<container/>" + TARGET_PARENT + " operations='Read'/></any-of></access-parent></class>");
		assertFalse(AccessParentConfig.isRestricted(oneUnrestricted.getAccessParent()));
		assertDelegatesExactly(oneUnrestricted.getAccessParent(), "Read", "Write", "Discover", "Create", "Delete",
			"Export");

		TLClassAccessRights nested = read("<class name='" + LINKED + "'><access-parent><any-of>"
			+ "<container operations='Write'/><any-of>" + TARGET_PARENT + " operations='Read'/></any-of>"
			+ "</any-of></access-parent></class>");
		assertTrue(AccessParentConfig.isRestricted(nested.getAccessParent()));
		assertDelegatesExactly(nested.getAccessParent(), "Read", "Write", "Discover");
	}

	/**
	 * The options of a building block inside <code>&lt;any-of&gt;</code> are those of the type the
	 * enclosing entry is configured for, as for a building block directly in the setting.
	 */
	public void testOptionsInsideAnyOf() throws Exception {
		String referer = "<referer reference='" + LINKED + "#target'/>";
		String target = "<target reference='" + MODULE + ":Covered#explicitParts' operations='Read'/>";
		TLClassAccessRights direct = read("<class name='" + MODULE + ":Covered'><access-parent>" + referer
			+ "</access-parent></class>");
		TLClassAccessRights nested = read("<class name='" + MODULE + ":Covered'><access-parent><any-of>" + referer
			+ "<any-of>" + target + "</any-of></any-of></access-parent></class>");

		ConfigurationItem directReferer = (ConfigurationItem) direct.getAccessParent().getDefinition();
		AnyOfAccessParent.Config anyOf = (AnyOfAccessParent.Config) nested.getAccessParent().getDefinition();
		ConfigurationItem nestedReferer = (ConfigurationItem) anyOf.getCandidates().get(0);
		ConfigurationItem nestedTarget =
			(ConfigurationItem) ((AnyOfAccessParent.Config) anyOf.getCandidates().get(1)).getCandidates().get(0);

		List<?> referers = options(directReferer, RefererAccessParent.Config.REFERENCE);
		assertTrue("The references pointing to the type are offered: " + referers,
			referers.contains(TLModelUtil.findPart(LINKED + "#target")));
		assertEquals(referers, options(nestedReferer, RefererAccessParent.Config.REFERENCE));

		List<?> references = options(nestedTarget, TargetAccessParent.Config.REFERENCE);
		assertTrue("The references of the type are offered: " + references,
			references.contains(TLModelUtil.findPart(MODULE + ":Covered#explicitParts")));
		List<?> operations = options(nestedTarget, OperationRestriction.OPERATIONS);
		assertTrue("The operations of the type are offered: " + operations,
			operations.contains(SimpleBoundCommandGroup.READ));
	}

	/**
	 * The values the {@link Options} function of the given property offers, before mapping them to
	 * the property's value type.
	 */
	private static List<?> options(ConfigurationItem item, String propertyName) {
		PropertyDescriptor property = item.descriptor().getProperty(propertyName);
		Options annotation = property.getAnnotation(Options.class);
		assertNotNull("No options for " + propertyName, annotation);
		Ref[] refs = annotation.args();
		Object[] args = new Object[refs.length];
		for (int n = 0; n < refs.length; n++) {
			PropertyValue value = NamePath.resolve(refs[n], item);
			args[n] = value == null ? null : value.get();
		}
		IGenericFunction<? extends Iterable<?>> function;
		try {
			function = ConfigUtil.getInstance(annotation.fun());
		} catch (ConfigurationException ex) {
			throw new AssertionError(ex);
		}
		List<Object> result = new ArrayList<>();
		function.invoke(args).forEach(result::add);
		return result;
	}

	/**
	 * Without a candidate leading to the container, an <code>&lt;any-of&gt;</code> does not delegate
	 * Create: the type grants it itself, the configuration and the resolved relation agree.
	 */
	public void testAnyOfWithoutContainerCandidateLeavesCreate() throws Exception {
		String classXml = "<class name='" + LINKED + "'><access-parent><any-of>" + TARGET_PARENT + "/>"
			+ TARGET_PARENT + " operations='Read'/></any-of></access-parent><grant operation='Create'/></class>";
		BufferingProtocol log = new BufferingProtocol();
		AccessParentFunction parent = linkedParent(log, classXml);
		assertTrue(log.getErrors().toString(), log.getErrors().isEmpty());
		assertFalse(parent.delegates(SimpleBoundCommandGroup.CREATE));
		assertFalse(parent.delegatesCreation());
		assertFalse(parent.delegatesAll());
		assertTrue(parent.delegates(SimpleBoundCommandGroup.WRITE));

		TLClassAccessRights entry = read(classXml);
		assertTrue(AccessParentConfig.isRestricted(entry.getAccessParent()));
		assertFalse(AccessParentConfig.delegatesOperation(entry.getAccessParent(), "Create"));
		assertTrue(AccessParentConfig.delegatesOperation(entry.getAccessParent(), "Write"));

		BufferingProtocol writeLog = new BufferingProtocol();
		linkedParent(writeLog, "<class name='" + LINKED + "'><access-parent><any-of>" + TARGET_PARENT + "/>"
			+ TARGET_PARENT + " operations='Read'/></any-of></access-parent><grant operation='Write'/></class>");
		assertError(writeLog, "delegates the operation Write");
	}

	/**
	 * A container candidate delegating Create makes the <code>&lt;any-of&gt;</code> delegate it.
	 */
	public void testAnyOfWithContainerCandidateDelegatesCreate() throws Exception {
		TLClassAccessRights entry = read("<class name='" + LINKED + "'><access-parent><any-of>"
			+ "<container/>" + TARGET_PARENT + "/></any-of></access-parent></class>");
		assertFalse(AccessParentConfig.isRestricted(entry.getAccessParent()));
		assertTrue(AccessParentConfig.delegatesOperation(entry.getAccessParent(), "Create"));

		TLClassAccessRights restricted = read("<class name='" + LINKED + "'><access-parent><any-of>"
			+ "<container operations='Write'/>" + TARGET_PARENT + " operations='Read,Create'/></any-of></access-parent></class>");
		assertFalse("A target candidate does not delegate Create.",
			AccessParentConfig.delegatesOperation(restricted.getAccessParent(), "Create"));
		BufferingProtocol log = new BufferingProtocol();
		AccessParentFunction parent = linkedParent(log, "<class name='" + LINKED + "'><access-parent><any-of>"
			+ "<container operations='Write'/>" + TARGET_PARENT + " operations='Read,Create'/></any-of></access-parent></class>");
		assertTrue(log.getErrors().toString(), log.getErrors().isEmpty());
		assertEquals(Set.of(SimpleBoundCommandGroup.READ, SimpleBoundCommandGroup.WRITE), parent.getOperations());
		assertFalse(parent.delegates(SimpleBoundCommandGroup.CREATE));
	}

	/**
	 * A nested <code>&lt;any-of&gt;</code> counts by its candidates, as the resolved relation takes
	 * them: the configuration and the relation agree whether the type grants operations itself.
	 */
	public void testNestedAnyOfConfigAgreesWithRelation() throws Exception {
		assertConfigAgrees("<container operations='Create,Write'/><any-of>" + TARGET_PARENT + "/>" + TARGET_PARENT
			+ " operations='Read'/></any-of>", true);
		assertConfigAgrees(TARGET_PARENT + " operations='Read'/><any-of>" + TARGET_PARENT + "/>" + TARGET_PARENT
			+ " operations='Write'/></any-of>", false);
		assertConfigAgrees("<container operations='Write'/><any-of>" + TARGET_PARENT + " operations='Read'/>"
			+ TARGET_PARENT + " operations='Export'/></any-of>", false);
	}

	private static void assertConfigAgrees(String candidatesXml, boolean delegatesAll) throws Exception {
		String classXml =
			"<class name='" + LINKED + "'><access-parent><any-of>" + candidatesXml + "</any-of></access-parent></class>";
		BufferingProtocol log = new BufferingProtocol();
		AccessParentFunction parent = linkedParent(log, classXml);
		assertTrue(log.getErrors().toString(), log.getErrors().isEmpty());
		assertEquals(candidatesXml, delegatesAll, parent.delegatesAll());
		AccessParentConfig config = read(classXml).getAccessParent();
		assertEquals(candidatesXml, !delegatesAll, AccessParentConfig.isRestricted(config));
		for (String operation : List.of("Read", "Write", "Create", "Delete", "Export", "Discover")) {
			assertEquals(candidatesXml + ": " + operation, AccessParentConfig.delegatesOperation(config, operation),
				parent.delegates(CommandGroupRegistry.getInstance().getGroup(operation)));
		}
	}

	/**
	 * A custom combinator decides every operation, as before: its configuration is not restricted,
	 * and the relation delegates all operations although its candidates are restricted.
	 */
	public void testCustomCombinatorOfRestrictedCandidatesDelegatesAll() throws Exception {
		String classXml = "<class name='" + LINKED + "'><access-parent><test-all-of>" + TARGET_PARENT
			+ " operations='Read'/>" + TARGET_PARENT + " operations='Write'/></test-all-of></access-parent></class>";
		BufferingProtocol log = new BufferingProtocol();
		AccessParentFunction parent = linkedParent(log, classXml);
		assertTrue(log.getErrors().toString(), log.getErrors().isEmpty());
		assertTrue(parent.delegatesAll());
		assertFalse(AccessParentConfig.isRestricted(read(classXml).getAccessParent()));
	}

	/**
	 * An <code>&lt;any-of&gt;</code> with a single candidate is that candidate.
	 */
	public void testSingleCandidateAnyOfIsTheCandidate() throws Exception {
		BufferingProtocol log = new BufferingProtocol();
		AccessParentFunction parent = linkedParent(log, "<class name='" + LINKED + "'><access-parent><any-of>"
			+ "<any-of>" + TARGET_PARENT + "/></any-of></any-of></access-parent></class>");
		assertTrue(log.getErrors().toString(), log.getErrors().isEmpty());
		assertTrue(String.valueOf(parent), parent instanceof TargetRelation);
		assertTrue(parent.delegates(SimpleBoundCommandGroup.CREATE));

		TLClassAccessRights entry = read("<class name='" + LINKED + "'><access-parent><any-of>"
			+ TARGET_PARENT + "/></any-of></access-parent></class>");
		assertFalse(AccessParentConfig.isRestricted(entry.getAccessParent()));
		assertTrue(AccessParentConfig.delegatesOperation(entry.getAccessParent(), "Create"));
	}

	public void testGrantBesideUnrestrictedAnyOfIsError() throws Exception {
		BufferingProtocol log = new BufferingProtocol();
		linkedParent(log, "<class name='" + LINKED + "'><access-parent><any-of>"
			+ "<container/>" + TARGET_PARENT + " operations='Read'/></any-of></access-parent>"
			+ "<grant operation='Write'/></class>");
		assertError(log, "neither grants nor marks");
	}

	public void testCustomCombinator() throws Exception {
		BufferingProtocol log = new BufferingProtocol();
		AccessParentFunction parent = linkedParent(log, "<class name='" + LINKED + "'><access-parent>"
			+ "<test-all-of><container/>" + TARGET_PARENT + " operations='Read'/></test-all-of>"
			+ "</access-parent></class>");
		assertTrue(log.getErrors().toString(), log.getErrors().isEmpty());
		TestAllOfAccessParent.AllOfRelation allOf = (TestAllOfAccessParent.AllOfRelation) parent;
		assertEquals(2, allOf.candidates().size());
		assertTrue(allOf.candidates().get(1) instanceof RestrictedRelation);
		assertFalse(allOf.isDisjunctive());

		BufferingProtocol logNested = new BufferingProtocol();
		AccessParentFunction anyOf = linkedParent(logNested, "<class name='" + LINKED + "'><access-parent><any-of>"
			+ "<container/><test-all-of><container/>" + TARGET_PARENT + "/></test-all-of></any-of>"
			+ "</access-parent></class>");
		assertTrue(logNested.getErrors().toString(), logNested.getErrors().isEmpty());
		assertEquals("A combinator is a candidate of its own, not flattened.", 2,
			((AnyOfRelation) anyOf).candidates().size());
		assertFalse("A conjunctive candidate makes the combination non-disjunctive.", anyOf.isDisjunctive());
	}

	public void testReferenceDoesNotExist() throws Exception {
		assertResolveError("Linked",
			"<access-parent><target reference='TestSecurityCoverage:Linked#noSuchReference'/></access-parent>",
			"does not exist");
	}

	public void testTargetWithoutReference() {
		assertParseError("<class name='TestSecurityCoverage:Linked'><access-parent><target/></access-parent></class>");
	}

	public void testEmptyAccessParent() {
		assertParseError("<class name='TestSecurityCoverage:Linked'><access-parent/></class>");
	}

	public void testOldAttributeForm() {
		assertParseError("<class name='TestSecurityCoverage:Linked' access-parent='container'/>");
	}

	public void testOldReferenceAttribute() {
		assertParseError("<class name='TestSecurityCoverage:Linked' "
			+ "access-reference='TestSecurityCoverage:Linked#target'/>");
	}

	public void testDelegatingParentWithGrantsIsRejected() throws Exception {
		TLClassAccessRights entry = read("<class name='TestSecurityCoverage:Linked'>"
			+ "<access-parent><target reference='TestSecurityCoverage:Linked#target'/></access-parent>"
			+ "<grant operation='Read' roles='TestSecurityCoverage.Reader'/></class>");
		ConstraintChecker checker = new ConstraintChecker();
		checker.check(entry);
		assertFalse("An access parent together with grants must be rejected.", checker.getFailures().isEmpty());

		Resources resources = Resources.getInstance();
		String message = resources.getString(checker.getFailures().get(0).getConstraintName());
		String grantsLabel =
			resources.getString(entry.descriptor().getProperty(TLClassAccessRights.GRANTS).labelKey(null));
		assertTrue("The message names the grants by the label the user knows them by: " + message,
			message.contains(grantsLabel));
	}

	public void testSelfWithGrantsIsAccepted() throws Exception {
		TLClassAccessRights entry = read("<class name='TestSecurityCoverage:SelfDeciding'>"
			+ "<access-parent><self/></access-parent>"
			+ "<grant operation='Read' roles='TestSecurityCoverage.Reader'/></class>");
		ConstraintChecker checker = new ConstraintChecker();
		checker.check(entry);
		assertTrue("A type deciding for itself keeps its grants: " + checker.getFailures(),
			checker.getFailures().isEmpty());
	}

	public void testRestrictedParentWithGrantsIsAccepted() throws Exception {
		TLClassAccessRights entry = read("<class name='" + LINKED + "'><access-parent>" + TARGET_PARENT
			+ " operations='Read'/></access-parent><grant operation='Write' roles='TestSecurityCoverage.Reader'/>"
			+ "</class>");
		ConstraintChecker checker = new ConstraintChecker();
		checker.check(entry);
		assertTrue("A type delegating some operations grants the others: " + checker.getFailures(),
			checker.getFailures().isEmpty());
	}

	public void testRestrictedParentWithGrantOfDelegatedOperationIsRejected() throws Exception {
		for (String operation : List.of("Read", "Discover")) {
			TLClassAccessRights entry = read("<class name='" + LINKED + "'><access-parent>" + TARGET_PARENT
				+ " operations='Read'/></access-parent><grant operation='" + operation
				+ "' roles='TestSecurityCoverage.Reader'/></class>");
			ConstraintChecker checker = new ConstraintChecker();
			checker.check(entry);
			assertFalse(operation + " is delegated and must not be granted beside the access parent.",
				checker.getFailures().isEmpty());
		}
	}

	public void testRestrictedParentWithMarkIsRejected() throws Exception {
		TLClassAccessRights entry = read("<class name='" + LINKED + "' internal='true'><access-parent>"
			+ TARGET_PARENT + " operations='Read'/></access-parent></class>");
		ConstraintChecker checker = new ConstraintChecker();
		checker.check(entry);
		assertFalse("A mark beside a restricted access parent must be rejected.", checker.getFailures().isEmpty());
	}

	public void testOperationsRestrictRelation() throws Exception {
		BufferingProtocol log = new BufferingProtocol();
		AccessParentFunction parent = linkedParent(log, "<class name='" + LINKED + "'><access-parent>"
			+ TARGET_PARENT + " operations='Read'/></access-parent></class>");
		assertTrue(log.getErrors().toString(), log.getErrors().isEmpty());

		assertTrue("The relation is restricted: " + parent, parent instanceof RestrictedRelation);
		assertTrue(parent.delegates(SimpleBoundCommandGroup.READ));
		assertTrue("Discover is delegated with any operation.", parent.delegates(SimpleBoundCommandGroup.DISCOVER));
		assertFalse(parent.delegates(SimpleBoundCommandGroup.WRITE));
		assertFalse("Create is delegated only when named.", parent.delegates(SimpleBoundCommandGroup.CREATE));
		assertFalse("Delete is delegated only when named.", parent.delegates(SimpleBoundCommandGroup.DELETE));
		assertFalse(parent.delegates(SimpleBoundCommandGroup.EXPORT));
		assertEquals(Set.of(SimpleBoundCommandGroup.READ), parent.getOperations());

		AccessParentFunction relation = parent.getRelation();
		assertTrue("The relation without restriction: " + relation, relation instanceof TargetRelation);
		assertEquals(LINKED + "#target", TLModelUtil.qualifiedName(((TargetRelation) relation).reference()));
		assertSame(relation, relation.getRelation());
	}

	public void testSeveralOperations() throws Exception {
		BufferingProtocol log = new BufferingProtocol();
		AccessParentFunction parent = linkedParent(log, "<class name='" + LINKED + "'><access-parent>"
			+ TARGET_PARENT + " operations='Read, Write,Delete'/></access-parent></class>");
		assertTrue(log.getErrors().toString(), log.getErrors().isEmpty());
		assertEquals(Set.of(SimpleBoundCommandGroup.READ, SimpleBoundCommandGroup.WRITE, SimpleBoundCommandGroup.DELETE),
			parent.getOperations());
		assertFalse(parent.delegates(SimpleBoundCommandGroup.CREATE));
	}

	public void testEmptyOperationEntriesAreSkipped() throws Exception {
		BufferingProtocol log = new BufferingProtocol();
		AccessParentFunction parent = linkedParent(log, "<class name='" + LINKED + "'><access-parent>"
			+ TARGET_PARENT + " operations='Read,,Write,'/></access-parent></class>");
		assertTrue(log.getErrors().toString(), log.getErrors().isEmpty());
		assertEquals(Set.of(SimpleBoundCommandGroup.READ, SimpleBoundCommandGroup.WRITE), parent.getOperations());
	}

	public void testOperationsWithoutNamesAreRejected() {
		for (String blank : new String[] { ",", " ", " , ", ",," }) {
			assertParseError("<class name='" + LINKED + "'><access-parent>" + TARGET_PARENT
				+ " operations='" + blank + "'/></access-parent></class>");
		}
	}

	public void testEmptyOperationsAreUnrestricted() throws Exception {
		BufferingProtocol log = new BufferingProtocol();
		AccessParentFunction parent = linkedParent(log, "<class name='" + LINKED + "'><access-parent>"
			+ TARGET_PARENT + " operations=''/></access-parent></class>");
		assertTrue(log.getErrors().toString(), log.getErrors().isEmpty());
		assertTrue("An empty list of operations delegates all.", parent.delegates(SimpleBoundCommandGroup.CREATE));
		assertNull(parent.getOperations());
	}

	public void testGrantedOperationIsNamedByLabel() throws Exception {
		TLClassAccessRights entry = read("<class name='" + LINKED + "'><access-parent>" + TARGET_PARENT
			+ " operations='Read'/></access-parent>"
			+ "<grant operation='Read' roles='TestSecurityCoverage.Reader'/></class>");
		ConstraintChecker checker = new ConstraintChecker();
		checker.check(entry);
		assertFalse(checker.getFailures().isEmpty());
		// The English label of Read equals its raw id, so only a locale with a different label tells
		// them apart.
		Resources resources = Resources.getInstance(Locale.GERMAN);
		String message = resources.getString(checker.getFailures().get(0).getConstraintName());
		String label = resources.getString(
			com.top_logic.tool.boundsec.simple.I18NConstants.COMMAND_GROUP_NAMES.key(SimpleBoundCommandGroup.READ_NAME));
		assertTrue("Operation label '" + label + "' expected in: " + message, message.contains(label));
		assertFalse("Raw operation id not expected in: " + message, message.contains(" Read "));
	}

	public void testRestrictionToNoOperationsIsRejected() {
		try {
			new RestrictedRelation(ContainerRelation.ANY, Set.of());
			fail("A relation restricted to no operations must be rejected.");
		} catch (IllegalArgumentException ex) {
			// Expected.
		}
	}

	public void testUnknownOperationIsError() throws Exception {
		BufferingProtocol log = new BufferingProtocol();
		AccessParentFunction parent = linkedParent(log, "<class name='" + LINKED + "'><access-parent>"
			+ TARGET_PARENT + " operations='NoSuchGroup'/></access-parent></class>");
		assertNull("An unusable restriction leaves the type without access parent.", parent);
		assertError(log, "NoSuchGroup");
	}

	public void testInapplicableOperationIsError() throws Exception {
		BufferingProtocol log = new BufferingProtocol();
		AccessParentFunction parent = linkedParent(log, "<class name='" + LINKED + "'><access-parent>"
			+ TARGET_PARENT + " operations='Read," + SimpleBoundCommandGroup.FINISH_NAME
			+ "'/></access-parent></class>");
		assertNull("An unusable restriction leaves the type without access parent.", parent);
		assertError(log, "does not apply");
	}

	public void testGrantForDelegatedOperationIsError() throws Exception {
		BufferingProtocol log = new BufferingProtocol();
		linkedParent(log, "<class name='" + LINKED + "'><access-parent>"
			+ TARGET_PARENT + " operations='Read'/></access-parent><grant operation='Read'/></class>");
		assertError(log, "delegates the operation Read");

		BufferingProtocol logOwn = new BufferingProtocol();
		linkedParent(logOwn, "<class name='" + LINKED + "'><access-parent>"
			+ TARGET_PARENT + " operations='Read'/></access-parent><grant operation='Write'/></class>");
		assertTrue("A grant of an operation not delegated is the own definition of the type: " + logOwn.getErrors(),
			logOwn.getErrors().isEmpty());
	}

	public void testDiscoverGrantIsDelegated() throws Exception {
		BufferingProtocol log = new BufferingProtocol();
		linkedParent(log, "<class name='" + LINKED + "'><access-parent>"
			+ TARGET_PARENT + " operations='Read'/></access-parent><grant operation='Discover'/></class>");
		assertError(log, "delegates the operation Discover");
	}

	public void testMarkWithRestrictedParentIsError() throws Exception {
		BufferingProtocol log = new BufferingProtocol();
		linkedParent(log, "<class name='" + LINKED + "' internal='true'><access-parent>"
			+ TARGET_PARENT + " operations='Read'/></access-parent></class>");
		assertError(log, "must not be marked");
	}

	public void testNoOperationsDelegatesAll() throws Exception {
		BufferingProtocol log = new BufferingProtocol();
		AccessParentFunction parent = linkedParent(log, "<class name='" + LINKED + "'><access-parent>"
			+ TARGET_PARENT + "/></access-parent></class>");
		assertTrue(log.getErrors().toString(), log.getErrors().isEmpty());
		assertTrue("Without restriction, the relation is entered as it is: " + parent,
			parent instanceof TargetRelation);
		assertNull("All operations are delegated.", parent.getOperations());
		assertSame(parent, parent.getRelation());
		for (BoundCommandGroup operation : BUILT_IN) {
			assertTrue(operation.getID(), parent.delegates(operation));
		}
	}

	public void testNoOperationsWithGrantIsError() throws Exception {
		BufferingProtocol log = new BufferingProtocol();
		linkedParent(log, "<class name='" + LINKED + "'><access-parent>"
			+ TARGET_PARENT + "/></access-parent><grant operation='Write'/></class>");
		assertError(log, "neither grants nor marks");
	}

	public void testOwnGrantOfSpecializationForDelegatedOperationIsError() throws Exception {
		String entry = "<class name='TestAccessParentOperations:Entry'><access-parent>"
			+ "<target reference='TestAccessParentOperations:Entry#catalog' operations='Read'/>"
			+ "</access-parent></class>";

		BufferingProtocol log = new BufferingProtocol();
		newService(log, entry, "<class name='TestAccessParentOperations:EntrySub'><grant operation='Read'/></class>");
		assertError(log, "TestAccessParentOperations:EntrySub delegates the operation Read");

		BufferingProtocol logOwn = new BufferingProtocol();
		newService(logOwn, entry, "<class name='TestAccessParentOperations:EntrySub'><grant operation='Write'/></class>");
		assertTrue("A grant of an operation not delegated is the own definition of the specialization: "
			+ logOwn.getErrors(), logOwn.getErrors().isEmpty());
	}

	public void testInheritedGrantForDelegatedOperationIsNoError() throws Exception {
		BufferingProtocol log = new BufferingProtocol();
		newService(log,
			"<class name='TestAccessParentOperations:EntryBase'><grant inherit='true' operation='Read'/></class>",
			"<class name='TestAccessParentOperations:Entry'><access-parent>"
				+ "<target reference='TestAccessParentOperations:Entry#catalog' operations='Read'/>"
				+ "</access-parent></class>");
		assertTrue("A grant inherited from a generalization is dropped silently: " + log.getErrors(),
			log.getErrors().isEmpty());
	}

	public void testSelfHasNoOperations() {
		assertParseError("<class name='" + LINKED + "'><access-parent><self operations='Read'/></access-parent></class>");
	}

	/**
	 * Instantiates a {@link SecurityConfigurationService} with the given entry alone and returns the
	 * access parent it resolves for {@link #LINKED}.
	 */
	private static AccessParentFunction linkedParent(BufferingProtocol log, String classXml) throws Exception {
		return newService(log, classXml).getAccessParent((TLClass) TLModelUtil.findType(LINKED));
	}

	/**
	 * Instantiates a {@link SecurityConfigurationService} with the given entries alone.
	 */
	private static SecurityConfigurationService newService(BufferingProtocol log, String... classXmls)
			throws Exception {
		SecurityConfigurationService.Config config =
			TypedConfiguration.newConfigItem(SecurityConfigurationService.Config.class);
		for (String classXml : classXmls) {
			TLClassAccessRights entry = read(classXml);
			config.getSecurityConfig().put(entry.getName(), entry);
		}
		return new SecurityConfigurationService(new DefaultInstantiationContext(log), config);
	}

	private static void assertDelegatesExactly(AccessParentConfig config, String... operations) {
		Set<String> expected = Set.of(operations);
		for (String operation : List.of("Read", "Write", "Discover", "Create", "Delete", "Export")) {
			assertEquals(operation, expected.contains(operation), AccessParentConfig.delegatesOperation(config, operation));
		}
	}

	private static void assertError(BufferingProtocol log, String expectedMessagePart) {
		String errors = log.getErrors().toString();
		assertTrue("Expected an error containing '" + expectedMessagePart + "': " + errors,
			errors.contains(expectedMessagePart));
	}

	private AccessParentFunction resolveOk(String typeName, String accessParentXml) throws Exception {
		BufferingProtocol log = new BufferingProtocol();
		AccessParentFunction result = resolve(log, typeName, accessParentXml);
		assertTrue(log.getErrors().toString(), log.getErrors().isEmpty());
		assertNotNull(result);
		return result;
	}

	private void assertResolveError(String typeName, String accessParentXml, String expectedMessagePart)
			throws Exception {
		BufferingProtocol log = new BufferingProtocol();
		assertNull(resolve(log, typeName, accessParentXml));
		String errors = log.getErrors().toString();
		assertTrue("Expected an error containing '" + expectedMessagePart + "': " + errors,
			errors.contains(expectedMessagePart));
	}

	private AccessParentFunction resolve(BufferingProtocol log, String typeName, String accessParentXml)
			throws Exception {
		TLClassAccessRights entry = read("<class name='" + MODULE + ":" + typeName + "'>" + accessParentXml + "</class>");
		DefaultInstantiationContext context = new DefaultInstantiationContext(log);
		AccessParentDefinition definition = context.getInstance(entry.getAccessParent().getDefinition());
		return definition.resolve(context, (TLClass) TLModelUtil.findType(MODULE + ":" + typeName));
	}

	private static void assertParseError(String xml) {
		try {
			read(xml);
			fail("Expected a configuration error for: " + xml);
		} catch (ConfigurationException | ConfigurationError ex) {
			// expected
		}
	}

	private static TLClassAccessRights read(String xml) throws ConfigurationException {
		return TypedConfiguration.parse("class", TLClassAccessRights.class, CharacterContents.newContent(xml));
	}

	public static Test suite() {
		Test suite = ServiceTestSetup.createSetup(new TestSuite(TestAccessParentConfig.class),
			TLSecurityDeviceManager.Module.INSTANCE, PersonManager.Module.INSTANCE);
		return ElementWebTestSetup.createElementWebTestSetup(suite);
	}

}
