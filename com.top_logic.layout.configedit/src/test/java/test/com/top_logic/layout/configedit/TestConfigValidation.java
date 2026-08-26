/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.configedit;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.ModuleLicenceTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.basic.config.constraint.annotation.Comparision;
import com.top_logic.basic.config.constraint.annotation.ComparisonDependency;
import com.top_logic.basic.config.constraint.annotation.Constraint;
import com.top_logic.basic.config.constraint.impl.Positive;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.layout.configedit.ConfigFieldIndex;
import com.top_logic.layout.configedit.ConfigFieldModel;
import com.top_logic.layout.configedit.ConfigValidation;
import com.top_logic.layout.configedit.ConfigValidation.Violation;

/**
 * Tests for {@link ConfigValidation}.
 */
public class TestConfigValidation extends TestCase {

	/**
	 * Test configuration interface with a mandatory property.
	 */
	public interface MandatoryConfig extends ConfigurationItem {

		/** Property name for {@link #getName()}. */
		String NAME = "name";

		@Name(NAME)
		@Mandatory
		String getName();

		/** @see #getName() */
		void setName(String value);
	}

	/** Test configuration interface nesting a {@link MandatoryConfig}. */
	public interface OuterConfig extends ConfigurationItem {

		/** Property name for {@link #getInner()}. */
		String INNER = "inner";

		@Name(INNER)
		MandatoryConfig getInner();

		/** @see #getInner() */
		void setInner(MandatoryConfig value);
	}

	/**
	 * Test configuration interface with two ITEM properties, so the very same
	 * {@link MandatoryConfig} instance can be reached through both - the shape that pins the
	 * revisit guard against reporting one violation twice.
	 */
	public interface TwoRefsConfig extends ConfigurationItem {

		/** Property name for {@link #getFirst()}. */
		String FIRST = "first";

		/** Property name for {@link #getSecond()}. */
		String SECOND = "second";

		@Name(FIRST)
		MandatoryConfig getFirst();

		/** @see #getFirst() */
		void setFirst(MandatoryConfig value);

		@Name(SECOND)
		MandatoryConfig getSecond();

		/** @see #getSecond() */
		void setSecond(MandatoryConfig value);
	}

	/** A single entry of {@link CollectionConfig}'s collections. */
	public interface Entry extends ConfigurationItem {

		/** Property name for {@link #getTitle()}. */
		String TITLE = "title";

		@Name(TITLE)
		String getTitle();

		/** @see #getTitle() */
		void setTitle(String value);
	}

	/**
	 * Test configuration interface whose mandatory properties are a collection and an item - the
	 * kinds this editor renders as something other than a field.
	 */
	public interface CollectionConfig extends ConfigurationItem {

		/** Property name for {@link #getEntries()}. */
		String ENTRIES = "entries";

		/** Property name for {@link #getIndex()}. */
		String INDEX = "index";

		/** Property name for {@link #getPart()}. */
		String PART = "part";

		@Name(ENTRIES)
		@Mandatory
		List<Entry> getEntries();

		@Name(INDEX)
		@Mandatory
		@Key(Entry.TITLE)
		Map<String, Entry> getIndex();

		@Name(PART)
		@Mandatory
		Entry getPart();

		/** @see #getPart() */
		void setPart(Entry value);
	}

	/** Test configuration interface with {@link Constraint} annotated properties. */
	public interface ConstrainedConfig extends ConfigurationItem {

		/** Property name for {@link #getPositive()}. */
		String POSITIVE = "positive";

		/** Property name for {@link #getPositiveWarning()}. */
		String POSITIVE_WARNING = "positiveWarning";

		@Name(POSITIVE)
		@Constraint(Positive.class)
		int getPositive();

		/** @see #getPositive() */
		void setPositive(int value);

		@Name(POSITIVE_WARNING)
		@Constraint(value = Positive.class, asWarning = true)
		int getPositiveWarning();

		/** @see #getPositiveWarning() */
		void setPositiveWarning(int value);
	}

	/** The item a {@link CrossRefConfig}'s constraint reaches into. */
	public interface LimitConfig extends ConfigurationItem {

		/** Property name for {@link #getMax()}. */
		String MAX = "max";

		@Name(MAX)
		int getMax();

		/** @see #getMax() */
		void setMax(int value);
	}

	/**
	 * Test configuration whose constraint reaches through a multi-step
	 * {@link com.top_logic.basic.config.annotation.Ref} into another item - so a violation of it is
	 * reported on a property that belongs to that other item, not to this one.
	 */
	public interface CrossRefConfig extends ConfigurationItem {

		/** Property name for {@link #getLimit()}. */
		String LIMIT = "limit";

		/** Property name for {@link #getAmount()}. */
		String AMOUNT = "amount";

		@Name(LIMIT)
		LimitConfig getLimit();

		/** @see #getLimit() */
		void setLimit(LimitConfig value);

		@Name(AMOUNT)
		@ComparisonDependency(comparison = Comparision.SMALLER_OR_EQUAL, other = @Ref({ LIMIT, LimitConfig.MAX }))
		int getAmount();

		/** @see #getAmount() */
		void setAmount(int value);
	}

	/** A mandatory property that was never given a value is a violation. */
	public void testAnEmptyMandatoryPropertyIsAViolation() {
		MandatoryConfig config = TypedConfiguration.newConfigItem(MandatoryConfig.class);

		List<Violation> violations = ConfigValidation.check(config);

		assertEquals(1, violations.size());
		assertSame(config, violations.get(0).item());
		assertEquals(MandatoryConfig.NAME, violations.get(0).property().getPropertyName());
	}

	/** Once it has a value it is not. */
	public void testAFilledMandatoryPropertyIsNoViolation() {
		MandatoryConfig config = TypedConfiguration.newConfigItem(MandatoryConfig.class);
		config.setName("given");

		assertEquals(Collections.emptyList(), ConfigValidation.check(config));
	}

	/** The check reaches into a nested item. */
	public void testTheCheckReachesNestedItems() {
		OuterConfig config = TypedConfiguration.newConfigItem(OuterConfig.class);
		MandatoryConfig inner = TypedConfiguration.newConfigItem(MandatoryConfig.class);
		config.setInner(inner);

		List<Violation> violations = ConfigValidation.check(config);

		assertEquals(1, violations.size());
		assertSame("The violation belongs to the inner item, not the outer one.",
			inner, violations.get(0).item());
	}

	/**
	 * The very same item, reached through two different properties, is still only checked once -
	 * without the revisit guard this would report the missing value twice.
	 */
	public void testTheSameItemReachedTwiceIsReportedOnce() {
		TwoRefsConfig config = TypedConfiguration.newConfigItem(TwoRefsConfig.class);
		MandatoryConfig inner = TypedConfiguration.newConfigItem(MandatoryConfig.class);
		config.setFirst(inner);
		config.setSecond(inner);

		List<Violation> violations = ConfigValidation.check(config);

		assertEquals("The item reached twice must be checked once.", 1, violations.size());
		assertSame(inner, violations.get(0).item());
	}

	/**
	 * An empty mandatory collection is not a violation: it is rendered by
	 * {@link com.top_logic.layout.configedit.ConfigListEditorControl}, not as a field, so a
	 * refusal naming it would have nothing on screen to correct.
	 */
	public void testAnEmptyMandatoryCollectionIsNoViolation() {
		CollectionConfig config = TypedConfiguration.newConfigItem(CollectionConfig.class);
		config.setPart(TypedConfiguration.newConfigItem(Entry.class));

		assertEquals(Collections.emptyList(), ConfigValidation.check(config));
	}

	/**
	 * And a filled one is not either - the case a {@code valueSet} check ahead of the kind
	 * exclusion gets wrong, since
	 * {@link com.top_logic.layout.configedit.ConfigCollectionValue} mutates the live collection in
	 * place and never calls {@link ConfigurationItem#update(PropertyDescriptor, Object)}.
	 */
	public void testAFilledMandatoryCollectionIsNoViolation() {
		CollectionConfig config = TypedConfiguration.newConfigItem(CollectionConfig.class);
		config.setPart(TypedConfiguration.newConfigItem(Entry.class));
		Entry listed = TypedConfiguration.newConfigItem(Entry.class);
		listed.setTitle("listed");
		config.getEntries().add(listed);
		Entry indexed = TypedConfiguration.newConfigItem(Entry.class);
		indexed.setTitle("indexed");
		config.getIndex().put(indexed.getTitle(), indexed);

		assertEquals("Neither list nor map may be flagged once entries were added in place.",
			Collections.emptyList(), ConfigValidation.check(config));
	}

	/**
	 * An unset mandatory ITEM property is not a violation either: it has no field of its own, and
	 * a monomorphic one renders nothing at all while its value is {@code null}.
	 */
	public void testAnUnsetMandatoryItemIsNoViolation() {
		CollectionConfig config = TypedConfiguration.newConfigItem(CollectionConfig.class);

		assertEquals(Collections.emptyList(), ConfigValidation.check(config));
	}

	/** A violation is put on the field that edits the offending property. */
	public void testAViolationIsReportedAtItsField() {
		MandatoryConfig config = TypedConfiguration.newConfigItem(MandatoryConfig.class);
		PropertyDescriptor property = config.descriptor().getProperty(MandatoryConfig.NAME);
		ConfigFieldModel model = new ConfigFieldModel(config, property);
		ConfigFieldIndex index = new ConfigFieldIndex();
		index.register(config, property, model);

		boolean complete = ConfigValidation.report(ConfigValidation.check(config), index);

		assertTrue("Every violation had a field to go to.", complete);
		assertNotNull("The field must carry the error.", model.getError());
	}

	/** A violation with no field of its own is reported as not placed, rather than swallowed. */
	public void testAViolationWithoutAFieldIsReportedAsIncomplete() {
		MandatoryConfig config = TypedConfiguration.newConfigItem(MandatoryConfig.class);

		boolean complete = ConfigValidation.report(ConfigValidation.check(config), new ConfigFieldIndex());

		assertFalse(complete);
	}

	/**
	 * A {@link ConstraintChecker} failure is a violation too, named by the very item and property
	 * {@link com.top_logic.basic.config.constraint.check.ConstraintFailure} carries - this fails if
	 * {@code collectConstraintFailures} used a logging {@code check} overload instead of
	 * {@link com.top_logic.basic.config.constraint.check.ConstraintChecker#check(ConfigurationItem)},
	 * since those clear the failure list before it can be read.
	 */
	public void testAConstraintViolationIsAViolation() {
		ConstrainedConfig config = TypedConfiguration.newConfigItem(ConstrainedConfig.class);
		config.setPositive(-1);
		config.setPositiveWarning(1);

		List<Violation> violations = ConfigValidation.check(config);

		assertEquals(1, violations.size());
		assertSame(config, violations.get(0).item());
		assertEquals(ConstrainedConfig.POSITIVE, violations.get(0).property().getPropertyName());
	}

	/**
	 * The message put on the field is what the constraint said, not the wording written to the
	 * server log - which names the configuration interface, the property, the raw value and the
	 * source location, none of which belongs next to a form field.
	 */
	public void testAConstraintViolationCarriesTheConstraintsOwnMessage() {
		ConstrainedConfig config = TypedConfiguration.newConfigItem(ConstrainedConfig.class);
		config.setPositive(-1);
		config.setPositiveWarning(1);

		List<Violation> violations = ConfigValidation.check(config);

		assertEquals(1, violations.size());
		assertEquals("The constraint's own message, not the log wording around it.",
			com.top_logic.basic.config.constraint.impl.I18NConstants.POSITIVE_VALUE_EXPECTED,
			violations.get(0).message());
	}

	/**
	 * A constraint reaching through a multi-step {@link com.top_logic.basic.config.annotation.Ref}
	 * reports on a property of the item it reached into, and the violation must name <em>that</em>
	 * item - naming the item whose constraint fired instead would pair it with a property that item
	 * does not have.
	 */
	public void testACrossItemConstraintNamesTheItemOwningTheProperty() {
		CrossRefConfig config = TypedConfiguration.newConfigItem(CrossRefConfig.class);
		LimitConfig limit = TypedConfiguration.newConfigItem(LimitConfig.class);
		limit.setMax(5);
		config.setLimit(limit);
		config.setAmount(10);

		List<Violation> violations = ConfigValidation.check(config);

		assertEquals("The dependency is symmetric: both ends are reported.", 2, violations.size());
		Violation onAmount = violationOf(violations, CrossRefConfig.AMOUNT);
		Violation onMax = violationOf(violations, LimitConfig.MAX);
		assertSame(config, onAmount.item());
		assertSame("The referenced end belongs to the referenced item.", limit, onMax.item());
	}

	/**
	 * And so both ends reach their own field: the one inside the referenced item too, which a
	 * violation naming the wrong item could never find.
	 */
	public void testBothEndsOfACrossItemConstraintReachTheirField() {
		CrossRefConfig config = TypedConfiguration.newConfigItem(CrossRefConfig.class);
		LimitConfig limit = TypedConfiguration.newConfigItem(LimitConfig.class);
		limit.setMax(5);
		config.setLimit(limit);
		config.setAmount(10);
		ConfigFieldIndex index = new ConfigFieldIndex();
		ConfigFieldModel amountField = register(index, config, CrossRefConfig.AMOUNT);
		ConfigFieldModel maxField = register(index, limit, LimitConfig.MAX);

		boolean complete = ConfigValidation.report(ConfigValidation.check(config), index);

		assertTrue("Both violations had a field to go to.", complete);
		assertNotNull(amountField.getError());
		assertNotNull("The referenced item's own field must carry its end of the violation.",
			maxField.getError());
	}

	/** The violation naming the given property, failing the test if there is none. */
	private Violation violationOf(List<Violation> violations, String propertyName) {
		for (Violation violation : violations) {
			if (propertyName.equals(violation.property().getPropertyName())) {
				return violation;
			}
		}
		fail("No violation for property '" + propertyName + "' in " + violations + ".");
		return null;
	}

	/** Builds and registers a {@link ConfigFieldModel} for the given property. */
	private ConfigFieldModel register(ConfigFieldIndex index, ConfigurationItem item, String propertyName) {
		PropertyDescriptor property = item.descriptor().getProperty(propertyName);
		ConfigFieldModel model = new ConfigFieldModel(item, property);
		index.register(item, property, model);
		return model;
	}

	/** A constraint failure marked as a warning must not block Apply. */
	public void testAWarningIsNotAViolation() {
		ConstrainedConfig config = TypedConfiguration.newConfigItem(ConstrainedConfig.class);
		config.setPositive(1);
		config.setPositiveWarning(-1);

		assertEquals(Collections.emptyList(), ConfigValidation.check(config));
	}

	/**
	 * Suite requiring {@link TypeIndex} for {@link TypedConfiguration} and
	 * {@link ThreadContextManager} for the label resolution the mandatory-value message uses.
	 */
	public static Test suite() {
		return ModuleLicenceTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestConfigValidation.class,
				ThreadContextManager.Module.INSTANCE, TypeIndex.Module.INSTANCE));
	}
}
