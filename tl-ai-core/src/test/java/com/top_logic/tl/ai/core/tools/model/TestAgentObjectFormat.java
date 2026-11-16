/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.model;

import java.util.Collections;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationDescriptorConfig;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.constraint.check.ConstraintChecker;
import com.top_logic.basic.config.constraint.check.ConstraintFailure;
import com.top_logic.basic.config.json.JsonConfigSchemaBuilder;
import com.top_logic.basic.config.json.JsonConfigurationReader;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.basic.json.schema.JsonSchemaWriter;
import com.top_logic.basic.json.schema.model.Schema;
import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.model.TLObject;
import com.top_logic.model.format.I18NConstants;
import com.top_logic.model.format.ObjectType;
import com.top_logic.tl.ai.core.model.TlAiCoreFactory;
import com.top_logic.tl.ai.core.security.ModelSecurity;
import com.top_logic.tl.ai.core.security.ReadFilter;
import com.top_logic.util.model.ModelService;

/**
 * Test for {@link AgentObjectFormat} and the {@link ObjectType} constraint it is combined with.
 */
public class TestAgentObjectFormat extends BasicTestCase {

	/** Qualified name of the type of the objects created by this test. */
	private static final String AGENT_TYPE = "tl.ai.core:Agent";

	/** A type the objects of this test are not instances of. */
	private static final String SKILL_TYPE = "tl.ai.core:Skill";

	/** A generalization of {@link #AGENT_TYPE}. */
	private static final String TL_OBJECT_TYPE = "tl.model:TLObject";

	/**
	 * Arguments with an object that must be a {@link #SKILL_TYPE}.
	 */
	public interface SkillArgument extends ConfigurationItem {

		/** @see #getObject() */
		String OBJECT = "object";

		/**
		 * The skill to work on.
		 */
		@Name(OBJECT)
		@Format(AgentObjectFormat.class)
		@ObjectType(SKILL_TYPE)
		TLObject getObject();
	}

	/**
	 * Arguments with an object of any type.
	 */
	public interface AnyObjectArgument extends ConfigurationItem {

		/** @see #getObject() */
		String OBJECT = "object";

		/**
		 * The object to work on.
		 */
		@Name(OBJECT)
		@Format(AgentObjectFormat.class)
		@ObjectType(TL_OBJECT_TYPE)
		TLObject getObject();
	}

	/** The identifier of a readable object resolves to the object. */
	public void testResolvesReadableObject() throws ConfigurationException {
		TLObject agent = createAgent("format-readable");

		try (var binding = AgentObjectFormat.withSecurity(ModelSecurity.UNCHECKED)) {
			assertSame(agent, AgentObjectFormat.INSTANCE.getValue(SkillArgument.OBJECT, ReadFilter.idOf(agent)));
		}
		assertEquals(ReadFilter.idOf(agent), AgentObjectFormat.INSTANCE.getSpecification(agent));
	}

	/** An object the user may not read is refused with the refusal for a missing object. */
	public void testUnreadableObjectIsRefusedLikeMissingOne() {
		TLObject agent = createAgent("format-hidden");
		String tid = ReadFilter.idOf(agent);

		ResKey hidden = refusal(new TestTidResolverDiagnostics.NoRead(), tid);

		try (Transaction tx = PersistencyLayer.getKnowledgeBase().beginTransaction()) {
			agent.tDelete();
			tx.commit();
		}
		ResKey missing = refusal(ModelSecurity.UNCHECKED, tid);

		assertEquals(missing, hidden);
	}

	/** An object of another type is reported with the expected type. */
	public void testOtherTypeIsReportedWithExpectedType() throws ConfigurationException {
		TLObject agent = createAgent("format-other-type");

		List<ConstraintFailure> failures = check(bind(SkillArgument.class, agent));

		assertEquals(1, failures.size());
		assertEquals(I18NConstants.ERROR_OBJECT_TYPE_MISMATCH__EXPECTED_ACTUAL.fill(SKILL_TYPE, AGENT_TYPE),
			failures.get(0).getConstraintName());
	}

	/** An object of a specialization of the expected type is accepted. */
	public void testSpecializationIsAccepted() throws ConfigurationException {
		TLObject agent = createAgent("format-specialization");

		ConfigurationItem arguments = bind(AnyObjectArgument.class, agent);

		assertEquals(Collections.emptyList(), check(arguments));
		assertSame(agent, ((AnyObjectArgument) arguments).getObject());
	}

	/** The schema describes the value as string naming the identifier form and the type. */
	public void testSchemaNamesIdFormAndType() throws Exception {
		Schema schema = new JsonConfigSchemaBuilder().setInline(true)
			.buildConfigSchema(TypedConfiguration.getConfigurationDescriptor(SkillArgument.class));

		String json = JsonSchemaWriter.toJson(schema, false);

		assertTrue(json, json.contains("\"type\":\"string\""));
		assertTrue(json, json.contains(AgentObjectFormat.ID_DESCRIPTION));
		assertTrue(json, json.contains(SKILL_TYPE));
	}

	/** Format and type are declared in the properties of a configuration type defined in XML. */
	public void testDeclarativeProperty() throws ConfigurationException {
		ConfigurationDescriptor descriptor = readDescriptor(
			"<properties>"
				+ "<property name='object' type='com.top_logic.model.TLObject'>"
				+ "<format value='" + AgentObjectFormat.class.getName() + "'/>"
				+ "<object-type value='" + SKILL_TYPE + "'/>"
				+ "</property>"
				+ "</properties>");
		PropertyDescriptor property = descriptor.getProperty(SkillArgument.OBJECT);
		assertEquals(SKILL_TYPE, property.getAnnotation(ObjectType.class).value());

		TLObject agent = createAgent("format-declarative");
		ConfigurationItem arguments = bind(descriptor, agent);
		assertSame(agent, arguments.value(property));

		List<ConstraintFailure> failures = check(arguments);
		assertEquals(1, failures.size());
		assertEquals(I18NConstants.ERROR_OBJECT_TYPE_MISMATCH__EXPECTED_ACTUAL.fill(SKILL_TYPE, AGENT_TYPE),
			failures.get(0).getConstraintName());
	}

	private static ResKey refusal(ModelSecurity security, String tid) {
		try (var binding = AgentObjectFormat.withSecurity(security)) {
			AgentObjectFormat.INSTANCE.getValue(SkillArgument.OBJECT, tid);
			throw new AssertionError("'" + tid + "' must not resolve.");
		} catch (ConfigurationException expected) {
			return expected.getErrorKey();
		}
	}

	private static ConfigurationItem bind(Class<? extends ConfigurationItem> type, TLObject object)
			throws ConfigurationException {
		return bind(TypedConfiguration.getConfigurationDescriptor(type), object);
	}

	private static ConfigurationItem bind(ConfigurationDescriptor descriptor, TLObject object)
			throws ConfigurationException {
		String json = "{\"" + SkillArgument.OBJECT + "\": \"" + ReadFilter.idOf(object) + "\"}";
		try (var binding = AgentObjectFormat.withSecurity(ModelSecurity.UNCHECKED)) {
			return new JsonConfigurationReader(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY, descriptor)
				.schemaAware()
				.setSource(CharacterContents.newContent(json))
				.read();
		}
	}

	private static List<ConstraintFailure> check(ConfigurationItem item) throws ConfigurationException {
		ConstraintChecker checker = new ConstraintChecker();
		checker.check(item);
		return checker.getFailures();
	}

	private static ConfigurationDescriptor readDescriptor(String xml) throws ConfigurationException {
		ConfigurationReader reader = new ConfigurationReader(
			SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY,
			Collections.singletonMap("properties",
				TypedConfiguration.getConfigurationDescriptor(ConfigurationDescriptorConfig.class)));
		reader.setSource(CharacterContents.newContent(xml));
		return TypedConfiguration.getConfigurationDescriptor((ConfigurationDescriptorConfig) reader.read());
	}

	private static TLObject createAgent(String name) {
		try (Transaction tx = PersistencyLayer.getKnowledgeBase().beginTransaction()) {
			TLObject agent = TlAiCoreFactory.getInstance().createAgent();
			agent.tUpdate(TlAiCoreFactory.getNameAgentAttr(), name);
			tx.commit();
			return agent;
		}
	}

	/** Creates the test suite. */
	public static Test suite() {
		return KBSetup.getSingleKBTest(
			ServiceTestSetup.createSetup(
				new TestSuite(TestAgentObjectFormat.class),
				ModelService.Module.INSTANCE));
	}
}
