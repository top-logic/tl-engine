/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.configedit;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.ModuleLicenceTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.CommaSeparatedStrings;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.layout.configedit.ConfigSelectFieldModel;
import com.top_logic.layout.form.model.SelectFieldModel;
import com.top_logic.layout.form.values.edit.OptionMapping;

/**
 * Tests for {@link ConfigSelectFieldModel}.
 */
public class TestConfigSelectFieldModel extends TestCase {

	/** Test enum. */
	public enum Color {
		/** Red. */
		RED,
		/** Green. */
		GREEN,
		/** Blue. */
		BLUE
	}

	/**
	 * An option a {@link TestConfig#SHAPE} property offers - a different Java type than the name
	 * the property stores for it, which is what makes the mapping below a non-identity one.
	 */
	public static final class Shape {

		private final String _name;

		/** Creates a {@link Shape}. */
		public Shape(String name) {
			_name = name;
		}

		/** The shape's name. */
		public String getName() {
			return _name;
		}
	}

	/** The two shapes a {@link TestConfig#SHAPE} property offers. */
	static final List<Shape> SHAPES = Arrays.asList(new Shape("circle"), new Shape("square"));

	/** Translates a {@link Shape} option into the name stored for it, and back. */
	public static class ShapeNameMapping implements OptionMapping {

		@Override
		public Object toSelection(Object option) {
			return ((Shape) option).getName();
		}

		@Override
		public Object asOption(Iterable<?> allOptions, Object selection) {
			for (Object option : allOptions) {
				if (((Shape) option).getName().equals(selection)) {
					return option;
				}
			}
			return null;
		}
	}

	/** Test configuration with an enum property and two properties with mapped options. */
	public interface TestConfig extends ConfigurationItem {

		/** Property name for {@link #getColor()}. */
		String COLOR = "color";

		/** Property name for {@link #getShape()}. */
		String SHAPE = "shape";

		/** Property name for {@link #getShapes()}. */
		String SHAPES_PROPERTY = "shapes";

		@Name(COLOR)
		Color getColor();

		void setColor(Color value);

		/** The name of the chosen shape. */
		@Name(SHAPE)
		String getShape();

		/** @see #getShape() */
		void setShape(String value);

		/** The names of the chosen shapes, written as comma separated text. */
		@Name(SHAPES_PROPERTY)
		@Format(CommaSeparatedStrings.class)
		List<String> getShapes();

		/** @see #getShapes() */
		void setShapes(List<String> value);
	}

	/**
	 * Tests that enum constants are available as options.
	 */
	public void testOptions() {
		TestConfig config = TypedConfiguration.newConfigItem(TestConfig.class);
		PropertyDescriptor property = config.descriptor().getProperty(TestConfig.COLOR);

		List<Color> options = Arrays.asList(Color.values());
		ConfigSelectFieldModel model = new ConfigSelectFieldModel(config, property, options, false);

		assertEquals(3, model.getOptions().size());
		assertEquals(Color.RED, model.getOptions().get(0));
		assertEquals(Color.GREEN, model.getOptions().get(1));
		assertEquals(Color.BLUE, model.getOptions().get(2));
		assertFalse(model.isMultiple());
	}

	/**
	 * Tests setting a value through the select model.
	 */
	public void testSetValueEnum() {
		TestConfig config = TypedConfiguration.newConfigItem(TestConfig.class);
		PropertyDescriptor property = config.descriptor().getProperty(TestConfig.COLOR);

		List<Color> options = Arrays.asList(Color.values());
		ConfigSelectFieldModel model = new ConfigSelectFieldModel(config, property, options, false);

		model.setValue(Color.GREEN);
		assertEquals(Color.GREEN, config.getColor());
		assertEquals(Color.GREEN, model.getValue());
	}

	/**
	 * Tests that setOptions fires the options listener.
	 */
	public void testOptionsListener() {
		TestConfig config = TypedConfiguration.newConfigItem(TestConfig.class);
		PropertyDescriptor property = config.descriptor().getProperty(TestConfig.COLOR);

		List<Color> options = Arrays.asList(Color.values());
		ConfigSelectFieldModel model = new ConfigSelectFieldModel(config, property, options, false);

		Object[] capturedOptions = new Object[1];
		model.addOptionsListener(new SelectFieldModel.SelectOptionsListener() {
			@Override
			public void onOptionsChanged(SelectFieldModel source, List<?> newOptions) {
				capturedOptions[0] = newOptions;
			}
		});

		List<Color> newOptions = Arrays.asList(Color.RED, Color.BLUE);
		model.setOptions(newOptions);

		assertNotNull("Options listener should have been called", capturedOptions[0]);
		assertEquals(2, ((List<?>) capturedOptions[0]).size());
	}

	/**
	 * {@link ConfigSelectFieldModel#setValue(Object)} forwards a non-{@link String} value
	 * unchanged to {@link com.top_logic.layout.configedit.ConfigFieldModel#setValue(Object)}, so
	 * the null-refusal for a technically mandatory property (see
	 * {@link com.top_logic.layout.configedit.ConfigFieldModel}) already applies here without any
	 * change of its own: clearing the (non-nullable) enum selection is refused as a field error,
	 * leaving the configuration untouched.
	 */
	public void testSetValueRejectsNullForNonNullableProperty() {
		TestConfig config = TypedConfiguration.newConfigItem(TestConfig.class);
		config.setColor(Color.BLUE);
		PropertyDescriptor property = config.descriptor().getProperty(TestConfig.COLOR);

		List<Color> options = Arrays.asList(Color.values());
		ConfigSelectFieldModel model = new ConfigSelectFieldModel(config, property, options, false);

		model.setValue(null);

		assertEquals("Clearing a non-nullable selection must not change its value.", Color.BLUE, config.getColor());
		assertNotNull("Clearing a non-nullable selection must be reported as a field error.",
			model.getInputError());
	}

	/**
	 * A field over a property whose options are not the values stored for them hands out the
	 * option the stored value stands for.
	 */
	public void testMappedValueIsTheOptionTheStoredValueStandsFor() {
		TestConfig config = TypedConfiguration.newConfigItem(TestConfig.class);
		config.setShape("square");
		ConfigSelectFieldModel model = mappedModel(config, TestConfig.SHAPE, false);

		assertSame("The value is the option the stored name stands for.", SHAPES.get(1), model.getValue());
	}

	/** Setting an option stores what the mapping says that option stands for. */
	public void testSettingAMappedOptionStoresWhatItStandsFor() {
		TestConfig config = TypedConfiguration.newConfigItem(TestConfig.class);
		ConfigSelectFieldModel model = mappedModel(config, TestConfig.SHAPE, false);

		model.setValue(SHAPES.get(0));

		assertEquals("The property holds the name, not the option.", "circle", config.getShape());
		assertSame("The field still shows the option.", SHAPES.get(0), model.getValue());
	}

	/**
	 * A select control reports its selection as a list whether or not the field takes more than one
	 * value, so a single-valued property takes the one element of that list.
	 */
	public void testASingleValuedMappedFieldTakesTheSelectionAsAList() {
		TestConfig config = TypedConfiguration.newConfigItem(TestConfig.class);
		ConfigSelectFieldModel model = mappedModel(config, TestConfig.SHAPE, false);

		model.setValue(Collections.singletonList(SHAPES.get(1)));
		assertEquals("square", config.getShape());

		model.setValue(Collections.emptyList());
		assertTrue("An empty selection clears the property - a string property holds the empty "
			+ "string for no value, which is what the configuration framework stores when a "
			+ "non-nullable string is cleared.", StringServices.isEmpty(config.getShape()));
	}

	/** A multiple selection translates every element, in both directions. */
	public void testAMultipleSelectionTranslatesEveryElement() {
		TestConfig config = TypedConfiguration.newConfigItem(TestConfig.class);
		config.setShapes(Arrays.asList("square", "circle"));
		ConfigSelectFieldModel model = mappedModel(config, TestConfig.SHAPES_PROPERTY, true);

		assertTrue(model.isMultiple());
		assertEquals("Every stored name is shown as the option it stands for.",
			Arrays.asList(SHAPES.get(1), SHAPES.get(0)), model.getValue());

		model.setValue(Arrays.asList(SHAPES.get(0)));
		assertEquals("Every picked option is stored as what it stands for.",
			Arrays.asList("circle"), config.getShapes());

		model.setValue(Collections.emptyList());
		assertEquals("An empty selection stores no entries.", Collections.emptyList(), config.getShapes());
	}

	/**
	 * Which option a stored value stands for is answered against the options, so a different option
	 * list can make it a different option - or none.
	 */
	public void testNewOptionsReDeriveTheDisplayedSelection() {
		TestConfig config = TypedConfiguration.newConfigItem(TestConfig.class);
		config.setShape("square");
		ConfigSelectFieldModel model = mappedModel(config, TestConfig.SHAPE, false);

		Shape replacement = new Shape("square");
		model.setOptions(Arrays.asList(replacement));

		assertSame("The selection must be re-derived from the new options.", replacement, model.getValue());
		assertEquals("Re-deriving what is displayed must not rewrite the configuration.",
			"square", config.getShape());

		model.setOptions(Arrays.asList(new Shape("triangle")));
		assertNull("A stored value the options no longer name has no option to be displayed as.",
			model.getValue());
		assertEquals("The configuration keeps it all the same.", "square", config.getShape());
	}

	/** The field model of a mapped property. */
	private static ConfigSelectFieldModel mappedModel(TestConfig config, String propertyName, boolean multiple) {
		PropertyDescriptor property = config.descriptor().getProperty(propertyName);
		return new ConfigSelectFieldModel(config, property, SHAPES, multiple, new ShapeNameMapping());
	}

	/**
	 * Suite requiring {@link TypeIndex} for {@link TypedConfiguration} and
	 * {@link ThreadContextManager} for the label resolution the rejected-value error message uses.
	 */
	public static Test suite() {
		return ModuleLicenceTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestConfigSelectFieldModel.class,
				ThreadContextManager.Module.INSTANCE, TypeIndex.Module.INSTANCE));
	}
}
