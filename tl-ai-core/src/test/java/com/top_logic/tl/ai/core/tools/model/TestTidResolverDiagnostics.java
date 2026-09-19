/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.dob.MetaObject;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.tl.ai.core.model.TlAiCoreFactory;
import com.top_logic.tl.ai.core.security.ModelSecurity;
import com.top_logic.tl.ai.core.security.ReadFilter;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.util.model.ModelService;

/**
 * What a tool is told when the identifier its model produced does not resolve.
 *
 * <p>
 * The classification itself is pinned without a knowledge base in {@link TestTidDiagnostics}. What
 * needs one is the other half: that {@link TidResolver} reaches the classification from every
 * branch that refuses, that the knowledge base's own exceptions no longer escape as themselves -
 * an unknown type used to leave as a {@code KnowledgeBaseRuntimeException} and an id that is no
 * number as a {@code NumberFormatException}, neither of which a tool catching
 * {@link IllegalArgumentException} ever saw - and that a hidden object is still refused with the
 * sentence of a missing one, to the byte.
 * </p>
 */
public class TestTidResolverDiagnostics extends BasicTestCase {

	/** An id no object of the test's type has. */
	private static final String FREE_ID = "999999999";

	/**
	 * The table the fixture's objects live in, looked up once.
	 *
	 * <p>
	 * Static because an agent's name is unique: every test method that needed the table name would
	 * otherwise create a second agent under the same one.
	 * </p>
	 */
	private static String _typeName;

	/** Refuses every read, and grants nothing else either. */
	private static class NoRead implements ModelSecurity {

		@Override
		public boolean canRead(TLObject obj) {
			return false;
		}

		@Override
		public boolean canRead(TLObject obj, TLStructuredTypePart attribute) {
			return false;
		}

		@Override
		public boolean canWrite(TLObject obj) {
			return false;
		}

		@Override
		public boolean canWrite(TLObject obj, TLStructuredTypePart attribute) {
			return false;
		}

		@Override
		public boolean canDelete(TLObject obj) {
			return false;
		}

		@Override
		public boolean canCreate(TLClass type, TLObject context) {
			return false;
		}

		@Override
		public boolean canCreate(TLObject parent, TLStructuredTypePart compositionAttribute) {
			return false;
		}

		@Override
		public boolean canPerform(TLObject obj, BoundCommandGroup operation) {
			return false;
		}

		@Override
		public <T extends TLObject> List<T> filterReadable(Collection<T> objects) {
			return new ArrayList<>();
		}

		@Override
		public Set<TLClass> accessibleTypes(BoundCommandGroup operation) {
			return Set.of();
		}
	}

	/** The happy path is untouched: a real identifier still names its object. */
	public void testAValidIdentifierStillResolves() {
		TLObject agent = createAgent("resolves");

		assertSame(agent, TidResolver.resolve(ReadFilter.idOf(agent), ModelSecurity.UNCHECKED));
	}

	/** So is the {@code label [id:TID]} form a text result writes. */
	public void testTheLabelledFormStillResolves() {
		TLObject agent = createAgent("labelled");

		assertSame(agent, TidResolver.resolve(ReadFilter.withId("Some agent", ReadFilter.idOf(agent)),
			ModelSecurity.UNCHECKED));
	}

	/**
	 * A type name no table has is named as the mistake - and reaches the caller as the documented
	 * {@link IllegalArgumentException}, which the engine's own exception did not.
	 */
	public void testAnUnknownTypeIsNamed() {
		String message = refusalFor("Invoice:5371");

		assertSays("No object type is called 'Invoice'", message);
	}

	/** An abstract table stores nothing, and the message says that rather than blaming the id. */
	public void testAnAbstractTypeIsNamed() {
		String abstractType = anAbstractType();
		if (abstractType == null) {
			// Every table of this knowledge base is instantiable; TestTidDiagnostics covers the
			// case against the seam.
			return;
		}

		assertSays("is abstract", refusalFor(abstractType + ":5371"));
	}

	/** An id the engine's parser rejects is named as such, not reported as a number format. */
	public void testAnIdThatIsNoIdIsNamed() {
		String message = refusalFor(typeName() + ":no-such-object");

		assertSays("The part behind the colon", message);
	}

	/** A uuid is recognised before anything is looked up. */
	public void testAUuidIsNamed() {
		assertSays("is a uuid", refusalFor("8f1c2d3e-4b5a-6c7d-8e9f-0a1b2c3d4e5f"));
	}

	/** An identifier of the right shape that names nothing says so. */
	public void testAMissingObjectIsNamed() {
		assertSays("not found", refusalFor(typeName() + ":" + FREE_ID));
	}

	/**
	 * An object the principal may not read is refused with the sentence of one that does not exist.
	 *
	 * <p>
	 * Not "a similar sentence": the same one, character for character, for the same identifier. The
	 * test gets both for one object by asking first with rights that read nothing and then, after
	 * deleting it, with rights that read everything.
	 * </p>
	 */
	public void testAHiddenObjectIsRefusedLikeAMissingOne() {
		TLObject agent = createAgent("hidden");
		String tid = ReadFilter.idOf(agent);

		String hidden = refusalFor(tid, new NoRead());

		try (Transaction tx = kb().beginTransaction()) {
			agent.tDelete();
			tx.commit();
		}
		String missing = refusalFor(tid, ModelSecurity.UNCHECKED);

		assertEquals("Hiding an object must not be distinguishable from its absence.", missing, hidden);
	}

	/** Nothing at all is answered without reaching the knowledge base. */
	public void testAnEmptyIdentifier() {
		assertSays("An object id is required", refusalFor(""));
	}

	/** What the refusal for that identifier says. */
	private static String refusalFor(String tid) {
		return refusalFor(tid, ModelSecurity.UNCHECKED);
	}

	private static String refusalFor(String tid, ModelSecurity security) {
		try {
			TidResolver.resolve(tid, security);
			throw new AssertionError("'" + tid + "' must not resolve.");
		} catch (IllegalArgumentException expected) {
			return expected.getMessage();
		}
	}

	/** A persisted object of this application, for an identifier that really exists. */
	private static TLObject createAgent(String name) {
		try (Transaction tx = kb().beginTransaction()) {
			TLObject agent = TlAiCoreFactory.getInstance().createAgent();
			agent.tUpdate(TlAiCoreFactory.getNameAgentAttr(), name);
			tx.commit();
			return agent;
		}
	}

	/** The table name the test's own objects are stored under, asked once. */
	private static String typeName() {
		if (_typeName == null) {
			_typeName = ReadFilter.idOf(createAgent("typename")).split(":", 2)[0];
		}
		return _typeName;
	}

	/** The name of an abstract table of this knowledge base, or <code>null</code> if it has none. */
	private static String anAbstractType() {
		for (MetaObject type : kb().getMORepository().getMetaObjects()) {
			if (type instanceof MOClass clazz && clazz.isAbstract()) {
				return clazz.getName();
			}
		}
		return null;
	}

	private static KnowledgeBase kb() {
		return PersistencyLayer.getKnowledgeBase();
	}

	private static void assertSays(String expected, String actual) {
		assertTrue("Expected '" + expected + "' in: " + actual, actual.contains(expected));
	}

	/** Creates the test suite. */
	public static Test suite() {
		return KBSetup.getSingleKBTest(
			ServiceTestSetup.createSetup(
				new TestSuite(TestTidResolverDiagnostics.class),
				ModelService.Module.INSTANCE));
	}
}
