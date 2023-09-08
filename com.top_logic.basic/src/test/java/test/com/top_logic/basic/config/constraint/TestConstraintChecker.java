/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.config.constraint;

import static test.com.top_logic.basic.BasicTestCase.*;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

import junit.framework.Test;

import test.com.top_logic.basic.config.AbstractTypedConfigurationTestCase;
import test.com.top_logic.basic.config.constraint.ScenarioConstraints.ScenarioTypeInstanceFormat;

import com.top_logic.basic.ArrayUtil;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Container;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.basic.config.constraint.algorithm.GenericValueDependency;
import com.top_logic.basic.config.constraint.algorithm.PropertyModel;
import com.top_logic.basic.config.constraint.annotation.Comparision;
import com.top_logic.basic.config.constraint.annotation.ComparisonDependencies;
import com.top_logic.basic.config.constraint.annotation.ComparisonDependency;
import com.top_logic.basic.config.constraint.annotation.Constraint;
import com.top_logic.basic.config.constraint.annotation.I18NConstants;
import com.top_logic.basic.config.constraint.annotation.OverrideConstraints;
import com.top_logic.basic.config.constraint.check.ConstraintChecker;
import com.top_logic.basic.config.constraint.check.ConstraintFailure;
import com.top_logic.basic.config.constraint.impl.Negative;
import com.top_logic.basic.config.container.ConfigPart;
import com.top_logic.basic.util.ResKey;

/**
 * Test case for {@link ConstraintChecker}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestConstraintChecker extends AbstractTypedConfigurationTestCase {

	public interface A extends ConfigurationItem {

		String X = "x";

		String Y = "y";

		String Z = "z";

		@Name(X)
		int getX();

		@ComparisonDependencies({
			@ComparisonDependency(comparison = Comparision.GREATER_OR_EQUAL, other = @Ref(X)),
			@ComparisonDependency(comparison = Comparision.SMALLER, other = @Ref(Z), symmetric = false),
		})
		@Name(Y)
		int getY();

		@Name(Z)
		int getZ();

		void setX(int value);

		void setY(int value);

		void setZ(int value);

	}

	public interface B extends A {

		@Override
		public int getY();

	}

	public interface C extends A {

		@Override
		@OverrideConstraints
		@Constraint(Negative.class)
		public int getY();

	}

	public static class ExampleConstraint extends GenericValueDependency<Integer, Integer> {

		static final ResKey FAILURE_MESSAGE = ResKey.text("Values must not be equal.");

		public ExampleConstraint() {
			super(Integer.class, Integer.class);
		}

		@Override
		protected void checkValue(PropertyModel<Integer> self, PropertyModel<Integer> other) {
			if (Objects.equals(self.getValue(), other.getValue())) {
				self.setProblemDescription(FAILURE_MESSAGE);
			}
		}

	}

	public interface ExampleContainer extends ConfigurationItem {

		String VALUE = "value";

		String PART = "part";

		@Name(VALUE)
		int getValue();

		void setValue(int newValue);

		@Name(PART)
		ExamplePart getPart();

		void setPart(ExamplePart part);

	}

	public interface ExamplePart extends ConfigPart {

		String CONTAINER = "container";

		String CONSTRAINED = "constrained";

		@Container
		@Name(CONTAINER)
		ExampleContainer getContainer();

		@Constraint(value = ExampleConstraint.class, args = @Ref({ CONTAINER, ExampleContainer.VALUE }))
		@Name(CONSTRAINED)
		int getValue();

		void setValue(int newValue);
	}

	public void testComparison() throws ConfigurationException {
		A a1 = TypedConfiguration.newConfigItem(A.class);
		a1.setX(0);
		a1.setY(0);
		a1.setZ(1);

		ConstraintChecker checker = new ConstraintChecker();
		checker.check(a1);

		assertEquals(list(), checker.getFailures());
	}

	public void testConstraintFailure() throws ConfigurationException {
		A a1 = TypedConfiguration.newConfigItem(A.class);
		a1.setX(0);
		a1.setY(1);
		a1.setZ(1);

		ConstraintChecker checker = new ConstraintChecker();
		checker.check(a1);

		assertEquals(list(
			new ConstraintFailure(
				a1,
				false,
				TypedConfiguration.getConfigurationDescriptor(A.class).getProperty(A.Y),
				I18NConstants.COMPARISON_DEPENDENCY_VIOLATED__VALUE_OTHER_BOUND_OPERATION.fill(
					1, ResKey.text(A.Z), 1, Comparision.SMALLER))),
			checker.getFailures());
	}

	public void testInheritedConstraints() throws ConfigurationException {
		A a1 = TypedConfiguration.newConfigItem(B.class);
		a1.setX(0);
		a1.setY(1);
		a1.setZ(1);

		ConstraintChecker checker = new ConstraintChecker();
		checker.check(a1);

		assertEquals(list(
			new ConstraintFailure(
				a1,
				false,
				TypedConfiguration.getConfigurationDescriptor(B.class).getProperty(A.Y),
				I18NConstants.COMPARISON_DEPENDENCY_VIOLATED__VALUE_OTHER_BOUND_OPERATION.fill(
					1, ResKey.text(A.Z), 1, Comparision.SMALLER))),
			checker.getFailures());
	}

	public void testOverrideConstraints() throws ConfigurationException {
		A a1 = TypedConfiguration.newConfigItem(C.class);
		a1.setX(0);
		a1.setY(1);
		a1.setZ(1);

		ConstraintChecker checker = new ConstraintChecker();
		checker.check(a1);

		assertEquals(list(
			new ConstraintFailure(
				a1,
				false,
				TypedConfiguration.getConfigurationDescriptor(C.class).getProperty(C.Y), com.top_logic.basic.config.constraint.impl.I18NConstants.NEGATIVE_VALUE_EXPECTED)),
			checker.getFailures());
	}

	public void testInstanceFormat() throws ConfigurationException {
		ScenarioTypeInstanceFormat item = create(ScenarioTypeInstanceFormat.class);
		check(item);
		item.setInstance(new Object());
		check(item);
	}

	/** @see "Ticket #25043" */
	public void testConstraintArgumentsContainContainerProperty() throws ConfigurationException {
		ExampleContainer container = create(ExampleContainer.class);
		ExamplePart part = create(ExamplePart.class);
		container.setValue(13);
		container.setPart(part);
		part.setValue(7);
		ConstraintChecker checker = new ConstraintChecker();
		checker.check(container);
		assertEmpty(true, checker.getFailures());

		/* Set the same value as the referenced property. The constraint forbids that. */
		part.setValue(13);
		checker.check(container);
		assertEquals(1, checker.getFailures().size());
		ConstraintFailure failure = checker.getFailures().get(0);
		assertTrue(containsConstraintMessage(failure.getMessage()));
	}

	private boolean containsConstraintMessage(ResKey message) {
		/* The exact error message and the other arguments may change. Ignoring these details
		 * stabilizes this test against changes that are irrelevant for this functionality. */
		if (message.equals(ExampleConstraint.FAILURE_MESSAGE)) {
			return true;
		}
		return ArrayUtil.contains(message.arguments(), ExampleConstraint.FAILURE_MESSAGE);
	}

	private void check(ConfigurationItem item) throws ConfigurationException {
		ConstraintChecker checker = new ConstraintChecker();
		checker.check(item);
		assertTrue(checker.getFailures().isEmpty());
	}

	@Override
	protected Map<String, ConfigurationDescriptor> getDescriptors() {
		return Collections.singletonMap("a", TypedConfiguration.getConfigurationDescriptor(A.class));
	}

	public static Test suite() {
		return AbstractTypedConfigurationTestCase.suite(TestConstraintChecker.class);
	}

}
