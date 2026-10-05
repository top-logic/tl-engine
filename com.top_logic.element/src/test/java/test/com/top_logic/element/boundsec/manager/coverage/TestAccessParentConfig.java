/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.element.boundsec.manager.coverage;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.element.util.ElementWebTestSetup;

import com.top_logic.base.security.device.TLSecurityDeviceManager;
import com.top_logic.basic.BufferingProtocol;
import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.constraint.check.ConstraintChecker;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.model.TLClass;
import com.top_logic.model.security.AccessParentDefinition;
import com.top_logic.model.security.AccessParentFunction;
import com.top_logic.model.security.ContainerRelation;
import com.top_logic.model.security.SecurityConfigurationService.TLClassAccessRights;
import com.top_logic.model.security.TargetRelation;
import com.top_logic.model.util.TLModelUtil;

/**
 * Test for reading and resolving the <code>&lt;access-parent&gt;</code> setting of a
 * {@link TLClassAccessRights} entry, against the model module {@code TestSecurityCoverage} of the
 * element test application.
 */
@SuppressWarnings("javadoc")
public class TestAccessParentConfig extends BasicTestCase {

	private static final String MODULE = "TestSecurityCoverage";

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

	public void testTargetMultiple() throws Exception {
		assertResolveError("Covered",
			"<access-parent><target reference='TestSecurityCoverage:Covered#explicitParts'/></access-parent>",
			"is not a to-one reference of the type");
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
