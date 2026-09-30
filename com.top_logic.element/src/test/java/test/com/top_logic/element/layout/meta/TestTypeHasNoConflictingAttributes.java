/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.element.layout.meta;

import java.util.List;

import junit.framework.Test;

import test.com.top_logic.element.model.util.TLModelTest;

import com.top_logic.basic.ArrayUtil;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.basic.config.constraint.annotation.Constraint;
import com.top_logic.basic.config.constraint.check.ConstraintChecker;
import com.top_logic.basic.config.constraint.check.ConstraintFailure;
import com.top_logic.basic.util.ResKey;
import com.top_logic.element.layout.meta.I18NConstants;
import com.top_logic.element.layout.meta.TypeHasNoConflictingAttributes;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModel;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.factory.TLFactory;
import com.top_logic.model.impl.TLModelImpl;
import com.top_logic.model.impl.TransientObjectFactory;
import com.top_logic.util.error.TopLogicException;

/**
 * Test case for {@link TypeHasNoConflictingAttributes}.
 *
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@SuppressWarnings("javadoc")
public class TestTypeHasNoConflictingAttributes extends TLModelTest {

	private static final String MODULE = "test";

	/**
	 * Configuration declaring the {@link TypeHasNoConflictingAttributes} constraint in the same way
	 * as the type editor does.
	 */
	public interface ScenarioTypeGeneralizations extends ConfigurationItem {

		String TYPE = "type";

		String GENERALIZATIONS = "generalizations";

		/** The type being edited. */
		@Name(TYPE)
		@InstanceFormat
		TLStructuredType getType();

		/** @see #getType() */
		void setType(TLStructuredType value);

		/** The generalizations selected for {@link #getType()}. */
		@Name(GENERALIZATIONS)
		@InstanceFormat
		@Constraint(value = TypeHasNoConflictingAttributes.class, args = @Ref(TYPE))
		List<TLClass> getGeneralizations();

		/** @see #getGeneralizations() */
		void setGeneralizations(List<TLClass> value);

	}

	public void testDisjointAttributes() {
		TLClass specialization = addClass(MODULE, "A");
		addStringProperty(specialization, "a");

		TLClass generalization = addClass(MODULE, "B");
		addStringProperty(generalization, "b");

		TypeHasNoConflictingAttributes.checkNewGeneralization(specialization, generalization);
	}

	public void testConflictingAttributes() {
		TLClass specialization = addClass(MODULE, "A");
		addStringProperty(specialization, "name");

		TLClass generalization = addClass(MODULE, "B");
		addStringProperty(generalization, "name");

		assertConflict(specialization, generalization);
	}

	/**
	 * An attribute inherited by both classes from a common generalization has the same definition
	 * and therefore does not conflict.
	 */
	public void testCommonDefinitionDoesNotConflict() {
		TLClass base = addClass(MODULE, "Base");
		addStringProperty(base, "name");

		TLClass specialization = addClass(MODULE, "A");
		specialization.getGeneralizations().add(base);

		TLClass generalization = addClass(MODULE, "B");
		generalization.getGeneralizations().add(base);

		TypeHasNoConflictingAttributes.checkNewGeneralization(specialization, generalization);
	}

	/**
	 * The conflict is not created in the class receiving the new generalization, but in one of its
	 * specializations.
	 */
	public void testConflictInSpecialization() {
		TLClass specialization = addClass(MODULE, "A");

		TLClass sub = addClass(MODULE, "Sub");
		sub.getGeneralizations().add(specialization);
		addStringProperty(sub, "name");

		TLClass generalization = addClass(MODULE, "B");
		addStringProperty(generalization, "name");

		assertConflict(specialization, generalization);
	}

	/**
	 * Two specializations of the same class are unrelated to each other, therefore both may declare
	 * an attribute with the same name.
	 */
	public void testSiblingSpecializationsDoNotConflict() {
		TLClass specialization = addClass(MODULE, "A");

		TLClass sub1 = addClass(MODULE, "Sub1");
		sub1.getGeneralizations().add(specialization);
		addStringProperty(sub1, "name");

		TLClass sub2 = addClass(MODULE, "Sub2");
		sub2.getGeneralizations().add(specialization);
		addStringProperty(sub2, "name");

		TLClass generalization = addClass(MODULE, "B");
		addStringProperty(generalization, "other");

		TypeHasNoConflictingAttributes.checkNewGeneralization(specialization, generalization);
	}

	public void testConstraintReportsConflictingAttribute() throws ConfigurationException {
		TLClass type = addClass(MODULE, "A");
		addStringProperty(type, "name");

		TLClass generalization = addClass(MODULE, "B");
		addStringProperty(generalization, "name");

		assertEquals(1, checkConstraintCount(type, generalization));
	}

	public void testConstraintReportsConflictingGeneralizations() throws ConfigurationException {
		TLClass general1 = addClass(MODULE, "G1");
		addStringProperty(general1, "name");

		TLClass general2 = addClass(MODULE, "G2");
		addStringProperty(general2, "name");

		TLClass type = addClass(MODULE, "A");

		assertEquals(1, checkConstraintCount(type, general1, general2));
	}

	/**
	 * A generalization can be replaced by another one declaring an attribute with the same name in
	 * a single step: The attribute inherited from the generalization being removed must not be
	 * taken into account.
	 */
	public void testReplaceGeneralization() throws ConfigurationException {
		TLClass general1 = addClass(MODULE, "G1");
		addStringProperty(general1, "name");

		TLClass type = addClass(MODULE, "A");
		type.getGeneralizations().add(general1);

		TLClass general2 = addClass(MODULE, "G2");
		addStringProperty(general2, "name");

		assertEquals(0, checkConstraintCount(type, general2));
	}

	/**
	 * The specializations of the edited type inherit the attributes of a new generalization, too.
	 */
	public void testConstraintChecksSpecializations() throws ConfigurationException {
		TLClass type = addClass(MODULE, "A");

		TLClass sub = addClass(MODULE, "Sub");
		sub.getGeneralizations().add(type);
		addStringProperty(sub, "name");

		TLClass generalization = addClass(MODULE, "B");
		addStringProperty(generalization, "name");

		assertEquals(1, checkConstraintCount(type, generalization));
	}

	/**
	 * The attribute conflict is reported with the type the conflicting attribute was found in, not
	 * with the type it is declared in.
	 */
	public void testMessageNamesTheSelectedGeneralization() throws ConfigurationException {
		TLClass base = addClass(MODULE, "Base");
		addStringProperty(base, "name");

		TLClass generalization = addClass(MODULE, "G");
		generalization.getGeneralizations().add(base);

		TLClass type = addClass(MODULE, "A");
		addStringProperty(type, "name");

		List<ConstraintFailure> failures = checkConstraint(type, generalization);
		assertEquals(1, failures.size());

		ResKey expected = I18NConstants.ERROR_CONFLICTING_ATTRIBUTE__NAME_TYPE1_DEFINITION1_TYPE2_DEFINITION2
			.fill("name", "test:A", "test:A", "test:G", "test:Base");

		/* The constraint framework wraps the problem description into its own message, the other
		 * arguments of that message are irrelevant here. */
		ResKey message = failures.get(0).getMessage();
		assertTrue("Unexpected problem description: " + message,
			expected.equals(message) || ArrayUtil.contains(message.arguments(), expected));
	}

	/**
	 * The number of problems reported by {@link #checkConstraint(TLStructuredType, TLClass...)}.
	 */
	private int checkConstraintCount(TLStructuredType type, TLClass... generalizations)
			throws ConfigurationException {
		return checkConstraint(type, generalizations).size();
	}

	/**
	 * The problems reported for the given generalizations by the constraint declared in
	 * {@link ScenarioTypeGeneralizations}.
	 */
	private List<ConstraintFailure> checkConstraint(TLStructuredType type, TLClass... generalizations)
			throws ConfigurationException {
		ScenarioTypeGeneralizations config =
			TypedConfiguration.newConfigItem(ScenarioTypeGeneralizations.class);
		config.setType(type);
		config.setGeneralizations(list(generalizations));

		ConstraintChecker checker = new ConstraintChecker();
		checker.check(config);

		return checker.getFailures();
	}

	private void assertConflict(TLClass specialization, TLClass generalization) {
		try {
			TypeHasNoConflictingAttributes.checkNewGeneralization(specialization, generalization);
		} catch (TopLogicException expected) {
			return;
		}
		fail("A conflicting attribute was expected.");
	}

	@Override
	protected TLModel setUpModel() {
		TLModelImpl model = new TLModelImpl();
		model.addCoreModule();
		return model;
	}

	@Override
	protected TLFactory setUpFactory() {
		return TransientObjectFactory.INSTANCE;
	}

	@Override
	protected void tearDownModel() {
		// Nothing to do, the model is transient.
	}

	public static Test suite() {
		return suiteTransient(TestTypeHasNoConflictingAttributes.class);
	}

}
