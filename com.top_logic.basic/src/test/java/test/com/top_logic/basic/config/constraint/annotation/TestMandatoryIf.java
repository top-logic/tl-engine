/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.config.constraint.annotation;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ExternallyNamed;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.basic.config.constraint.annotation.MandatoryIf;
import com.top_logic.basic.config.constraint.check.ConstraintChecker;
import com.top_logic.basic.config.constraint.check.ConstraintFailure;

/**
 * Test case for {@link MandatoryIf}.
 */
@SuppressWarnings("javadoc")
public class TestMandatoryIf extends TestCase {

	/**
	 * A setting one of the properties below is required by.
	 */
	public enum Display implements ExternallyNamed {

		INPUT("input"),

		SLIDER("slider");

		private final String _externalName;

		private Display(String externalName) {
			_externalName = externalName;
		}

		@Override
		public String getExternalName() {
			return _externalName;
		}

	}

	public interface Config extends ConfigurationItem {

		String DISPLAY = "display";

		String BOUND = "bound";

		String FIXED = "fixed";

		String NOTE = "note";

		@Name(DISPLAY)
		Display getDisplay();

		void setDisplay(Display value);

		/** Required by the display that has a range to draw. */
		@Name(BOUND)
		@Nullable
		@MandatoryIf(other = @Ref(DISPLAY), value = "slider")
		Double getBound();

		void setBound(Double value);

		@Name(FIXED)
		boolean getFixed();

		void setFixed(boolean value);

		/** Required as soon as the value is fixed, a boolean naming its value as a configuration does. */
		@Name(NOTE)
		@Nullable
		@MandatoryIf(other = @Ref(FIXED), value = "true")
		String getNote();

		void setNote(String value);

	}

	/** The property is not required while the other one holds another value. */
	public void testNotRequiredForAnotherValue() throws ConfigurationException {
		assertProblems(config());
	}

	/**
	 * The value the other property is compared with is the one it effectively has: a default that
	 * demands the annotated property is no different from a written one.
	 */
	public void testTheDefaultOfTheOtherPropertyCounts() throws ConfigurationException {
		Config config = config();
		config.setDisplay(Display.SLIDER);

		assertProblems(config, Config.BOUND);

		config.setBound(Double.valueOf(1));

		assertProblems(config);
	}

	/** An enumeration literal is named as it is written in a configuration: by its external name. */
	public void testTheLiteralIsNamedByItsExternalName() throws ConfigurationException {
		assertEquals("slider", Display.SLIDER.getExternalName());

		Config config = config();
		config.setDisplay(Display.SLIDER);

		assertProblems(config, Config.BOUND);
	}

	/** Any kind of value names the setting, a truth value as well as a literal. */
	public void testAnyKindOfValueNamesTheSetting() throws ConfigurationException {
		Config config = config();
		config.setFixed(true);

		assertProblems(config, Config.NOTE);

		config.setNote("because");

		assertProblems(config);
	}

	/** The problem is reported on the property that is missing, not on the one that demands it. */
	public void testTheProblemIsReportedOnTheMissingProperty() throws ConfigurationException {
		Config config = config();
		config.setDisplay(Display.SLIDER);

		ConstraintChecker checker = new ConstraintChecker();
		checker.check(config);

		assertEquals(1, checker.getFailures().size());
		assertEquals(Config.BOUND, checker.getFailures().get(0).getContextProperty().getPropertyName());
	}

	private static Config config() {
		return TypedConfiguration.newConfigItem(Config.class);
	}

	private static void assertProblems(ConfigurationItem config, String... properties) throws ConfigurationException {
		assertEquals(List.of(properties), problems(config));
	}

	private static List<String> problems(ConfigurationItem config) throws ConfigurationException {
		ConstraintChecker checker = new ConstraintChecker();
		checker.check(config);

		List<String> result = new ArrayList<>();
		for (ConstraintFailure failure : checker.getFailures()) {
			result.add(failure.getContextProperty().getPropertyName());
		}
		return result;
	}

}
