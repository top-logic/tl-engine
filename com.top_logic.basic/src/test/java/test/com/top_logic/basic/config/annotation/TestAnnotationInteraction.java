/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.config.annotation;

import static test.com.top_logic.basic.config.annotation.ScenarioAnnotationInteraction.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.Test;

import test.com.top_logic.basic.BasicTestSetup;
import test.com.top_logic.basic.CustomPropertiesSetup;
import test.com.top_logic.basic.config.AbstractTypedConfigurationTestCase;
import test.com.top_logic.basic.config.annotation.ScenarioAnnotationInteraction.ScenarioContainerBaseTypes.ScenarioTypeBase;
import test.com.top_logic.basic.config.annotation.ScenarioAnnotationInteraction.ScenarioContainerBaseTypes.ScenarioTypeItemD;
import test.com.top_logic.basic.config.annotation.ScenarioAnnotationInteraction.ScenarioContainerLocalXY.ScenarioTypeLocalADefaultAbstract;
import test.com.top_logic.basic.config.annotation.ScenarioAnnotationInteraction.ScenarioContainerLocalXY.ScenarioTypeLocalADefaultContainer;
import test.com.top_logic.basic.config.annotation.ScenarioAnnotationInteraction.ScenarioContainerLocalXY.ScenarioTypeLocalADefaultMandatory;
import test.com.top_logic.basic.config.annotation.ScenarioAnnotationInteraction.ScenarioContainerLocalXY.ScenarioTypeLocalContainerAbstract;
import test.com.top_logic.basic.config.annotation.ScenarioAnnotationInteraction.ScenarioContainerLocalXY.ScenarioTypeLocalContainerDerived;
import test.com.top_logic.basic.config.annotation.ScenarioAnnotationInteraction.ScenarioContainerLocalXY.ScenarioTypeLocalDerivedAbstract;
import test.com.top_logic.basic.config.annotation.ScenarioAnnotationInteraction.ScenarioContainerLocalXY.ScenarioTypeLocalMandatoryAbstract;
import test.com.top_logic.basic.config.annotation.ScenarioAnnotationInteraction.ScenarioContainerLocalXY.ScenarioTypeLocalMandatoryContainer;
import test.com.top_logic.basic.config.annotation.ScenarioAnnotationInteraction.ScenarioContainerLocalXY.ScenarioTypeLocalMandatoryDerived;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.PropertyKind;
import com.top_logic.basic.config.TypedConfiguration;

/**
 * Test case for {@link ConfigurationItem}s where default value, mandatory and derived interact.
 * <p>
 * Combinations of "Configured Default" and "Container" are not tested, as they can never interact:
 * Only primitive value can be configured as default, but a container property always has an
 * {@link ConfigurationItem} as value, never a primitive.
 * </p>
 * <p>
 * If more than one annotation is inherited, they are tested only in one order, not in all possible
 * combinations. Exception: "Configured Default" vs. "Annotated Default" is tested in both orders
 * and "derived" vs. "container", as these pair are closely related and the order might matter.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestAnnotationInteraction extends AbstractTypedConfigurationTestCase {

	private static final String ERRR_ONLY_ONE_OF_THE_SETTINGS = "Only one of the settings";

	@SuppressWarnings("hiding")
	private enum PropertyModifier {
		CONFIGURED_DEFAULT("Configured Default", "CDefault"),
		ANNOTATED_DEFAULT("Annotated Default", "ADefault"),
		MANDATORY("Mandatory"),
		CONTAINER("Container"),
		DERIVED("Derived"),
		ABSTRACT("Abstract");

		private final String _uiName;

		private final String _classNamePart;

		private PropertyModifier(String name) {
			this(name, name);
		}

		private PropertyModifier(String uiName, String classNamePart) {
			_uiName = uiName;
			_classNamePart = classNamePart;
		}

		public String getUiName() {
			return _uiName;
		}

		public String getClassNamePart() {
			return _classNamePart;
		}

	}

	// Self-imports without causing JavaC to fail:

	private static final PropertyModifier CONFIGURED_DEFAULT = PropertyModifier.CONFIGURED_DEFAULT;

	private static final PropertyModifier ANNOTATED_DEFAULT = PropertyModifier.ANNOTATED_DEFAULT;

	private static final PropertyModifier MANDATORY = PropertyModifier.MANDATORY;

	private static final PropertyModifier CONTAINER = PropertyModifier.CONTAINER;

	private static final PropertyModifier DERIVED = PropertyModifier.DERIVED;

	private static final PropertyModifier ABSTRACT = PropertyModifier.ABSTRACT;

	private static final String TESTED_PROPERTY = ScenarioTypeBase.PROPERTY_NAME_EXAMPLE;

	private static final String TESTED_ITEM_PROPERTY = ScenarioTypeBase.PROPERTY_NAME_ITEM_EXAMPLE;

	/* Section: Two local annotations */
	public void testLocalConfiguredDefaultAnnotatedDefault() {
		Set<PropertyModifier> localModifiers = EnumSet.of(ANNOTATED_DEFAULT, CONFIGURED_DEFAULT);
		List<PropertyModifier> inheritedModifiers = Collections.emptyList();
		Set<PropertyModifier> expectedModifiers = EnumSet.of(CONFIGURED_DEFAULT);
		assertPropertyState(localModifiers, inheritedModifiers, expectedModifiers);
	}

	// testLocalConfiguredDefaultMandatory

	// testLocalConfiguredDefaultContainer

	// A derived property cannot have a configured default.
	// testLocalConfiguredDefaultDerived

	// An abstract property cannot have a configured default.
	// testLocalConfiguredDefaultAbstract

	public void testLocalAnnotatedDefaultMandatory() {
		Class<? extends ConfigurationItem> itemClass = ScenarioTypeLocalADefaultMandatory.class;
		String message = "Direct conflict between default value and mandatory is not detected";
		assertIllegal(message, ERRR_ONLY_ONE_OF_THE_SETTINGS, itemClass);
	}

	public void testLocalAnnotatedDefaultContainer() {
		Class<? extends ConfigurationItem> itemClass = ScenarioTypeLocalADefaultContainer.class;
		String message = "Direct conflict between default value and container is not detected";
		assertIllegal(message, ERRR_ONLY_ONE_OF_THE_SETTINGS, itemClass);
	}
	
	public void testLocalAnnotatedDefaultDerived() {
		Set<PropertyModifier> localModifiers = EnumSet.of(ANNOTATED_DEFAULT, DERIVED);
		List<PropertyModifier> inheritedModifiers = Collections.emptyList();
		Set<PropertyModifier> expectedModifiers = EnumSet.of(ANNOTATED_DEFAULT, DERIVED);
		assertPropertyState(localModifiers, inheritedModifiers, expectedModifiers);
	}

	public void testLocalAnnotatedDefaultAbstract() {
		Class<? extends ConfigurationItem> itemClass = ScenarioTypeLocalADefaultAbstract.class;
		String message = "Direct conflict between mandatory and abstract is not detected";
		assertIllegal(message, ERRR_ONLY_ONE_OF_THE_SETTINGS, itemClass);
	}

	public void testLocalMandatoryContainer() {
		Class<? extends ConfigurationItem> itemClass = ScenarioTypeLocalMandatoryContainer.class;
		String message = "Direct conflict between mandatory and container is not detected";
		assertIllegal(message, ERRR_ONLY_ONE_OF_THE_SETTINGS, itemClass);
	}
	
	public void testLocalMandatoryDerived() {
		Class<? extends ConfigurationItem> itemClass = ScenarioTypeLocalMandatoryDerived.class;
		String message = "Direct conflict between mandatory and derived is not detected";
		assertIllegal(message, ERRR_ONLY_ONE_OF_THE_SETTINGS, itemClass);
	}

	public void testLocalMandatoryAbstract() {
		Class<? extends ConfigurationItem> itemClass = ScenarioTypeLocalMandatoryAbstract.class;
		String message = "Direct conflict between mandatory and abstract is not detected";
		assertIllegal(message, ERRR_ONLY_ONE_OF_THE_SETTINGS, itemClass);
	}

	public void testLocalContainerDerived() {
		Class<? extends ConfigurationItem> itemClass = ScenarioTypeLocalContainerDerived.class;
		String message = "Direct conflict between container and derived is not detected";
		assertIllegal(message, ERRR_ONLY_ONE_OF_THE_SETTINGS, itemClass);
	}

	public void testLocalContainerAbstract() {
		Class<? extends ConfigurationItem> itemClass = ScenarioTypeLocalContainerAbstract.class;
		String message = "Direct conflict between container and abstract is not detected";
		assertIllegal(message, ERRR_ONLY_ONE_OF_THE_SETTINGS, itemClass);
	}

	public void testLocalDerivedAbstract() {
		Class<? extends ConfigurationItem> itemClass = ScenarioTypeLocalDerivedAbstract.class;
		String message = "Direct conflict between derived and abstract is not detected";
		assertIllegal(message, ERRR_ONLY_ONE_OF_THE_SETTINGS, itemClass);
	}

	/* Section: A local annotation and an inherited one */
	public void testLocalConfiguredDefaultInheritedAnnotatedDefault() {
		Set<PropertyModifier> localModifiers = EnumSet.of(CONFIGURED_DEFAULT);
		List<PropertyModifier> inheritedModifiers = Arrays.asList(ANNOTATED_DEFAULT);
		Set<PropertyModifier> expectedModifiers = EnumSet.of(CONFIGURED_DEFAULT);
		assertPropertyState(localModifiers, inheritedModifiers, expectedModifiers);
	}

	// testLocalConfiguredDefaultInheritedMandatory

	// testLocalConfiguredDefaultInheritedContainer

	// A derived property cannot have a configured default.
	// testLocalConfiguredDefaultInheritedDerived

	// An abstract property cannot have a configured default.
	// testLocalConfiguredDefaultInheritedAbstract

	public void testLocalAnnotatedDefaultInheritedConfiguredDefault() {
		Set<PropertyModifier> localModifiers = EnumSet.of(ANNOTATED_DEFAULT);
		List<PropertyModifier> inheritedModifiers = Arrays.asList(CONFIGURED_DEFAULT);
		Set<PropertyModifier> expectedModifiers = EnumSet.of(ANNOTATED_DEFAULT);
		assertPropertyState(localModifiers, inheritedModifiers, expectedModifiers);
	}

	public void testLocalAnnotatedDefaultInheritedMandatory() {
		Set<PropertyModifier> localModifiers = EnumSet.of(ANNOTATED_DEFAULT);
		List<PropertyModifier> inheritedModifiers = Arrays.asList(MANDATORY);
		Set<PropertyModifier> expectedModifiers = EnumSet.of(ANNOTATED_DEFAULT);
		assertPropertyState(localModifiers, inheritedModifiers, expectedModifiers);
	}

	public void testLocalAnnotatedDefaultInheritedContainer() {
		Set<PropertyModifier> localModifiers = EnumSet.of(ANNOTATED_DEFAULT);
		List<PropertyModifier> inheritedModifiers = Arrays.asList(CONTAINER);
		Set<PropertyModifier> expectedModifiers = EnumSet.of(ANNOTATED_DEFAULT);
		assertPropertyState(localModifiers, inheritedModifiers, expectedModifiers);
	}
	
	public void testLocalAnnotatedDefaultInheritedDerived() {
		Set<PropertyModifier> localModifiers = EnumSet.of(ANNOTATED_DEFAULT);
		List<PropertyModifier> inheritedModifiers = Arrays.asList(DERIVED);
		Set<PropertyModifier> expectedModifiers = EnumSet.of(ANNOTATED_DEFAULT, DERIVED);
		assertPropertyState(localModifiers, inheritedModifiers, expectedModifiers);
	}

	public void testLocalAnnotatedDefaultInheritedAbstract() {
		Set<PropertyModifier> localModifiers = EnumSet.of(ANNOTATED_DEFAULT);
		List<PropertyModifier> inheritedModifiers = Arrays.asList(ABSTRACT);
		Set<PropertyModifier> expectedModifiers = EnumSet.of(ANNOTATED_DEFAULT);
		assertPropertyState(localModifiers, inheritedModifiers, expectedModifiers);
	}

	public void testLocalMandatoryInheritedConfiguredDefault() {
		Set<PropertyModifier> localModifiers = EnumSet.of(MANDATORY);
		List<PropertyModifier> inheritedModifiers = Arrays.asList(CONFIGURED_DEFAULT);
		Set<PropertyModifier> expectedModifiers = EnumSet.of(MANDATORY);
		assertPropertyState(localModifiers, inheritedModifiers, expectedModifiers);
	}

	public void testLocalMandatoryInheritedAnnotatedDefault() {
		Set<PropertyModifier> localModifiers = EnumSet.of(MANDATORY);
		List<PropertyModifier> inheritedModifiers = Arrays.asList(ANNOTATED_DEFAULT);
		Set<PropertyModifier> expectedModifiers = EnumSet.of(MANDATORY);
		assertPropertyState(localModifiers, inheritedModifiers, expectedModifiers);
	}

	public void testLocalMandatoryInheritedContainer() {
		Set<PropertyModifier> localModifiers = EnumSet.of(MANDATORY);
		List<PropertyModifier> inheritedModifiers = Arrays.asList(CONTAINER);
		Set<PropertyModifier> expectedModifiers = EnumSet.of(MANDATORY);
		assertPropertyState(localModifiers, inheritedModifiers, expectedModifiers);
	}

	public void testLocalMandatoryInheritedDerived() {
		Set<PropertyModifier> localModifiers = EnumSet.of(MANDATORY);
		List<PropertyModifier> inheritedModifiers = Arrays.asList(DERIVED);
		Set<PropertyModifier> expectedModifiers = EnumSet.of(MANDATORY);
		assertPropertyState(localModifiers, inheritedModifiers, expectedModifiers);
	}

	public void testLocalMandatoryInheritedAbstract() {
		Set<PropertyModifier> localModifiers = EnumSet.of(MANDATORY);
		List<PropertyModifier> inheritedModifiers = Arrays.asList(ABSTRACT);
		Set<PropertyModifier> expectedModifiers = EnumSet.of(MANDATORY);
		assertPropertyState(localModifiers, inheritedModifiers, expectedModifiers);
	}

	// testLocalContainerInheritedConfiguredDefault

	public void testLocalContainerInheritedAnnotatedDefault() {
		Set<PropertyModifier> localModifiers = EnumSet.of(CONTAINER);
		List<PropertyModifier> inheritedModifiers = Arrays.asList(ANNOTATED_DEFAULT);
		Set<PropertyModifier> expectedModifiers = EnumSet.of(CONTAINER);
		assertPropertyState(localModifiers, inheritedModifiers, expectedModifiers);
	}

	public void testLocalContainerInheritedMandatory() {
		Set<PropertyModifier> localModifiers = EnumSet.of(CONTAINER);
		List<PropertyModifier> inheritedModifiers = Arrays.asList(MANDATORY);
		Set<PropertyModifier> expectedModifiers = EnumSet.of(CONTAINER);
		assertPropertyState(localModifiers, inheritedModifiers, expectedModifiers);
	}

	public void testLocalContainerInheritedDerived() {
		Set<PropertyModifier> localModifiers = EnumSet.of(CONTAINER);
		List<PropertyModifier> inheritedModifiers = Arrays.asList(DERIVED);
		Set<PropertyModifier> expectedModifiers = EnumSet.of(CONTAINER);
		assertPropertyState(localModifiers, inheritedModifiers, expectedModifiers);
	}

	public void testLocalContainerInheritedAbstract() {
		Set<PropertyModifier> localModifiers = EnumSet.of(CONTAINER);
		List<PropertyModifier> inheritedModifiers = Arrays.asList(ABSTRACT);
		Set<PropertyModifier> expectedModifiers = EnumSet.of(CONTAINER);
		assertPropertyState(localModifiers, inheritedModifiers, expectedModifiers);
	}

	public void testLocalDerivedInheritedConfiguredDefault() {
		Set<PropertyModifier> localModifiers = EnumSet.of(DERIVED);
		List<PropertyModifier> inheritedModifiers = Arrays.asList(CONFIGURED_DEFAULT);
		Set<PropertyModifier> expectedModifiers = EnumSet.of(CONFIGURED_DEFAULT, DERIVED);
		assertPropertyState(localModifiers, inheritedModifiers, expectedModifiers);
	}

	public void testLocalDerivedInheritedAnnotatedDefault() {
		Set<PropertyModifier> localModifiers = EnumSet.of(DERIVED);
		List<PropertyModifier> inheritedModifiers = Arrays.asList(ANNOTATED_DEFAULT);
		Set<PropertyModifier> expectedModifiers = EnumSet.of(ANNOTATED_DEFAULT, DERIVED);
		assertPropertyState(localModifiers, inheritedModifiers, expectedModifiers);
	}

	public void testLocalDerivedInheritedMandatory() {
		Set<PropertyModifier> localModifiers = EnumSet.of(DERIVED);
		List<PropertyModifier> inheritedModifiers = Arrays.asList(MANDATORY);
		Set<PropertyModifier> expectedModifiers = EnumSet.of(DERIVED);
		assertPropertyState(localModifiers, inheritedModifiers, expectedModifiers);
	}

	public void testLocalDerivedInheritedContainer() {
		Set<PropertyModifier> localModifiers = EnumSet.of(DERIVED);
		List<PropertyModifier> inheritedModifiers = Arrays.asList(CONTAINER);
		Set<PropertyModifier> expectedModifiers = EnumSet.of(DERIVED);
		assertPropertyState(localModifiers, inheritedModifiers, expectedModifiers);
	}

	public void testLocalDerivedInheritedAbstract() {
		Set<PropertyModifier> localModifiers = EnumSet.of(DERIVED);
		List<PropertyModifier> inheritedModifiers = Arrays.asList(ABSTRACT);
		Set<PropertyModifier> expectedModifiers = EnumSet.of(DERIVED);
		assertPropertyState(localModifiers, inheritedModifiers, expectedModifiers);
	}

	/* Section: Two inherited annotations */
	public void testInheritedConfiguredDefaultConfiguredDefault() {
		Set<PropertyModifier> localModifiers = Collections.emptySet();
		List<PropertyModifier> inheritedModifiers = Arrays.asList(CONFIGURED_DEFAULT, CONFIGURED_DEFAULT);
		Set<PropertyModifier> expectedModifiers = EnumSet.of(CONFIGURED_DEFAULT);
		assertPropertyState(localModifiers, inheritedModifiers, expectedModifiers);
	}

	public void testInheritedConfiguredDefaultAnnotatedDefault() {
		Set<PropertyModifier> localModifiers = Collections.emptySet();
		List<PropertyModifier> inheritedModifiers = Arrays.asList(CONFIGURED_DEFAULT, ANNOTATED_DEFAULT);
		Set<PropertyModifier> expectedModifiers = EnumSet.of(CONFIGURED_DEFAULT);
		assertPropertyState(localModifiers, inheritedModifiers, expectedModifiers);
	}

	public void testInheritedConfiguredDefaultMandatory() {
		Set<PropertyModifier> localModifiers = Collections.emptySet();
		List<PropertyModifier> inheritedModifiers = Arrays.asList(CONFIGURED_DEFAULT, MANDATORY);
		Set<PropertyModifier> expectedModifiers = EnumSet.of(CONFIGURED_DEFAULT);
		assertPropertyState(localModifiers, inheritedModifiers, expectedModifiers);
	}

	// testInheritedConfiguredDefaultContainer

	public void testInheritedConfiguredDefaultDerived() {
		Set<PropertyModifier> localModifiers = Collections.emptySet();
		List<PropertyModifier> inheritedModifiers = Arrays.asList(CONFIGURED_DEFAULT, DERIVED);
		Set<PropertyModifier> expectedModifiers = EnumSet.of(CONFIGURED_DEFAULT, DERIVED);
		assertPropertyState(localModifiers, inheritedModifiers, expectedModifiers);
	}

	public void testInheritedConfiguredDefaultAbstract() {
		Set<PropertyModifier> localModifiers = Collections.emptySet();
		List<PropertyModifier> inheritedModifiers = Arrays.asList(CONFIGURED_DEFAULT, ABSTRACT);
		Set<PropertyModifier> expectedModifiers = EnumSet.of(CONFIGURED_DEFAULT);
		assertPropertyState(localModifiers, inheritedModifiers, expectedModifiers);
	}

	public void testInheritedAnnotatedDefaultConfiguredDefault() {
		Set<PropertyModifier> localModifiers = Collections.emptySet();
		List<PropertyModifier> inheritedModifiers = Arrays.asList(ANNOTATED_DEFAULT, CONFIGURED_DEFAULT);
		Set<PropertyModifier> expectedModifiers = EnumSet.of(CONFIGURED_DEFAULT);
		assertPropertyState(localModifiers, inheritedModifiers, expectedModifiers);
	}

	public void testInheritedAnnotatedDefaultAnnotatedDefault() {
		Set<PropertyModifier> localModifiers = Collections.emptySet();
		List<PropertyModifier> inheritedModifiers = Arrays.asList(ANNOTATED_DEFAULT, ANNOTATED_DEFAULT);
		Set<PropertyModifier> expectedModifiers = EnumSet.of(ANNOTATED_DEFAULT);
		assertPropertyState(localModifiers, inheritedModifiers, expectedModifiers);
	}

	public void testInheritedAnnotatedDefaultMandatory() {
		Set<PropertyModifier> localModifiers = Collections.emptySet();
		List<PropertyModifier> inheritedModifiers = Arrays.asList(ANNOTATED_DEFAULT, MANDATORY);
		Set<PropertyModifier> expectedModifiers = EnumSet.of(ANNOTATED_DEFAULT);
		assertPropertyState(localModifiers, inheritedModifiers, expectedModifiers);
	}

	public void testInheritedAnnotatedDefaultContainer() {
		Set<PropertyModifier> localModifiers = Collections.emptySet();
		List<PropertyModifier> inheritedModifiers = Arrays.asList(ANNOTATED_DEFAULT, CONTAINER);
		Set<PropertyModifier> expectedModifiers = EnumSet.of(ANNOTATED_DEFAULT);
		assertPropertyState(localModifiers, inheritedModifiers, expectedModifiers);
	}

	public void testInheritedAnnotatedDefaultDerived() {
		Set<PropertyModifier> localModifiers = Collections.emptySet();
		List<PropertyModifier> inheritedModifiers = Arrays.asList(ANNOTATED_DEFAULT, DERIVED);
		Set<PropertyModifier> expectedModifiers = EnumSet.of(ANNOTATED_DEFAULT, DERIVED);
		assertPropertyState(localModifiers, inheritedModifiers, expectedModifiers);
	}

	public void testInheritedAnnotatedDefaultAbstract() {
		Set<PropertyModifier> localModifiers = Collections.emptySet();
		List<PropertyModifier> inheritedModifiers = Arrays.asList(ANNOTATED_DEFAULT, ABSTRACT);
		Set<PropertyModifier> expectedModifiers = EnumSet.of(ANNOTATED_DEFAULT);
		assertPropertyState(localModifiers, inheritedModifiers, expectedModifiers);
	}

	public void testInheritedMandatoryContainer() {
		Set<PropertyModifier> localModifiers = Collections.emptySet();
		List<PropertyModifier> inheritedModifiers = Arrays.asList(MANDATORY, CONTAINER);
		Set<PropertyModifier> expectedModifiers = EnumSet.of(CONTAINER);
		assertPropertyState(localModifiers, inheritedModifiers, expectedModifiers);
	}

	public void testInheritedMandatoryDerived() {
		Set<PropertyModifier> localModifiers = Collections.emptySet();
		List<PropertyModifier> inheritedModifiers = Arrays.asList(MANDATORY, DERIVED);
		Set<PropertyModifier> expectedModifiers = EnumSet.of(DERIVED);
		assertPropertyState(localModifiers, inheritedModifiers, expectedModifiers);
	}

	public void testInheritedMandatoryAbstract() {
		Set<PropertyModifier> localModifiers = Collections.emptySet();
		List<PropertyModifier> inheritedModifiers = Arrays.asList(MANDATORY, ABSTRACT);
		Set<PropertyModifier> expectedModifiers = EnumSet.of(MANDATORY);
		assertPropertyState(localModifiers, inheritedModifiers, expectedModifiers);
	}

	public void testInheritedContainerContainer() {
		Set<PropertyModifier> localModifiers = Collections.emptySet();
		List<PropertyModifier> inheritedModifiers = Arrays.asList(CONTAINER, CONTAINER);
		Set<PropertyModifier> expectedModifiers = EnumSet.of(CONTAINER);
		assertPropertyState(localModifiers, inheritedModifiers, expectedModifiers);
	}

	public void testInheritedContainerDerived() {
		Set<PropertyModifier> localModifiers = Collections.emptySet();
		List<PropertyModifier> inheritedModifiers = Arrays.asList(CONTAINER, DERIVED);
		Set<PropertyModifier> expectedModifiers = EnumSet.of(CONTAINER);
		assertPropertyState(localModifiers, inheritedModifiers, expectedModifiers);
	}

	public void testInheritedContainerAbstract() {
		Set<PropertyModifier> localModifiers = Collections.emptySet();
		List<PropertyModifier> inheritedModifiers = Arrays.asList(CONTAINER, ABSTRACT);
		Set<PropertyModifier> expectedModifiers = EnumSet.of(CONTAINER);
		assertPropertyState(localModifiers, inheritedModifiers, expectedModifiers);
	}

	public void testInheritedDerivedContainer() {
		Set<PropertyModifier> localModifiers = Collections.emptySet();
		List<PropertyModifier> inheritedModifiers = Arrays.asList(DERIVED, CONTAINER);
		Set<PropertyModifier> expectedModifiers = EnumSet.of(CONTAINER);
		assertPropertyState(localModifiers, inheritedModifiers, expectedModifiers);
	}

	public void testInheritedDerivedDerived() {
		Set<PropertyModifier> localModifiers = Collections.emptySet();
		List<PropertyModifier> inheritedModifiers = Arrays.asList(DERIVED, DERIVED);
		Set<PropertyModifier> expectedModifiers = EnumSet.of(DERIVED);
		assertPropertyState(localModifiers, inheritedModifiers, expectedModifiers);
	}

	public void testInheritedDerivedAbstract() {
		Set<PropertyModifier> localModifiers = Collections.emptySet();
		List<PropertyModifier> inheritedModifiers = Arrays.asList(DERIVED, ABSTRACT);
		Set<PropertyModifier> expectedModifiers = EnumSet.of(DERIVED);
		assertPropertyState(localModifiers, inheritedModifiers, expectedModifiers);
	}

	/* Section: Local configured default value and two inherited annotations */
	public void testLocalConfiguredDefaultInheritedConfiguredDefaultConfiguredDefault() {
		Set<PropertyModifier> localModifiers = EnumSet.of(CONFIGURED_DEFAULT);
		List<PropertyModifier> inheritedModifiers = Arrays.asList(CONFIGURED_DEFAULT, CONFIGURED_DEFAULT);
		Set<PropertyModifier> expectedModifiers = EnumSet.of(CONFIGURED_DEFAULT);
		assertPropertyState(localModifiers, inheritedModifiers, expectedModifiers);
	}
	
	public void testLocalConfiguredDefaultInheritedConfiguredDefaultAnnotatedDefault() {
		Set<PropertyModifier> localModifiers = EnumSet.of(CONFIGURED_DEFAULT);
		List<PropertyModifier> inheritedModifiers = Arrays.asList(CONFIGURED_DEFAULT, ANNOTATED_DEFAULT);
		Set<PropertyModifier> expectedModifiers = EnumSet.of(CONFIGURED_DEFAULT);
		assertPropertyState(localModifiers, inheritedModifiers, expectedModifiers);
	}

	// testLocalConfiguredDefaultInheritedConfiguredDefaultMandatory
	
	// testLocalConfiguredDefaultInheritedConfiguredDefaultContainer

	/* Cannot be tested, as the inherited configured default is not visible until all defaults have
	 * been read. But to read all defaults, the default of this class has to be read, but it is
	 * still derived, as the inherited configured default is just being read. */
	// testLocalConfiguredDefaultInheritedConfiguredDefaultDerived

	public void testLocalConfiguredDefaultInheritedConfiguredDefaultAbstract() {
		Set<PropertyModifier> localModifiers = EnumSet.of(CONFIGURED_DEFAULT);
		List<PropertyModifier> inheritedModifiers = Arrays.asList(CONFIGURED_DEFAULT, ABSTRACT);
		Set<PropertyModifier> expectedModifiers = EnumSet.of(CONFIGURED_DEFAULT);
		assertPropertyState(localModifiers, inheritedModifiers, expectedModifiers);
	}

	public void testLocalConfiguredDefaultInheritedAnnotatedDefaultConfiguredDefault() {
		Set<PropertyModifier> localModifiers = EnumSet.of(CONFIGURED_DEFAULT);
		List<PropertyModifier> inheritedModifiers = Arrays.asList(ANNOTATED_DEFAULT, CONFIGURED_DEFAULT);
		Set<PropertyModifier> expectedModifiers = EnumSet.of(CONFIGURED_DEFAULT);
		assertPropertyState(localModifiers, inheritedModifiers, expectedModifiers);
	}

	public void testLocalConfiguredDefaultInheritedAnnotatedDefaultAnnotatedDefault() {
		Set<PropertyModifier> localModifiers = EnumSet.of(CONFIGURED_DEFAULT);
		List<PropertyModifier> inheritedModifiers = Arrays.asList(ANNOTATED_DEFAULT, ANNOTATED_DEFAULT);
		Set<PropertyModifier> expectedModifiers = EnumSet.of(CONFIGURED_DEFAULT);
		assertPropertyState(localModifiers, inheritedModifiers, expectedModifiers);
	}
	
	public void testLocalConfiguredDefaultInheritedAnnotatedDefaultMandatory() {
		Set<PropertyModifier> localModifiers = EnumSet.of(CONFIGURED_DEFAULT);
		List<PropertyModifier> inheritedModifiers = Arrays.asList(ANNOTATED_DEFAULT, MANDATORY);
		Set<PropertyModifier> expectedModifiers = EnumSet.of(CONFIGURED_DEFAULT);
		assertPropertyState(localModifiers, inheritedModifiers, expectedModifiers);
	}
	
	// No testLocalConfiguredDefaultInheritedAnnotatedDefaultContainer

	// A derived property cannot have a configured default.
	// testLocalConfiguredDefaultInheritedAnnotatedDefaultDerived

	public void testLocalConfiguredDefaultInheritedAnnotatedDefaultAbstract() {
		Set<PropertyModifier> localModifiers = EnumSet.of(CONFIGURED_DEFAULT);
		List<PropertyModifier> inheritedModifiers = Arrays.asList(ANNOTATED_DEFAULT, ABSTRACT);
		Set<PropertyModifier> expectedModifiers = EnumSet.of(CONFIGURED_DEFAULT);
		assertPropertyState(localModifiers, inheritedModifiers, expectedModifiers);
	}

	// No testLocalConfiguredDefaultInheritedMandatoryContainer
	// No testLocalConfiguredDefaultInheritedMandatoryDerived
	// No testLocalConfiguredDefaultInheritedMandatoryAbstract
	// No testLocalConfiguredDefaultInheritedContainerContainer
	// No testLocalConfiguredDefaultInheritedContainerDerived
	// No testLocalConfiguredDefaultInheritedContainerAbstract
	// No testLocalConfiguredDefaultInheritedDerivedContainer

	// A derived property cannot have a configured default.
	// No testLocalConfiguredDefaultInheritedDerivedDerived

	// A derived property cannot have a configured default.
	// No testLocalConfiguredDefaultInheritedDerivedAbstract

	/* Section: Local default value annotation and two inherited annotations */
	public void testLocalAnnotatedDefaultInheritedConfiguredDefaultConfiguredDefault() {
		Set<PropertyModifier> localModifiers = EnumSet.of(ANNOTATED_DEFAULT);
		List<PropertyModifier> inheritedModifiers = Arrays.asList(CONFIGURED_DEFAULT, CONFIGURED_DEFAULT);
		Set<PropertyModifier> expectedModifiers = EnumSet.of(ANNOTATED_DEFAULT);
		assertPropertyState(localModifiers, inheritedModifiers, expectedModifiers);
	}

	public void testLocalAnnotatedDefaultInheritedConfiguredDefaultAnnotatedDefault() {
		Set<PropertyModifier> localModifiers = EnumSet.of(ANNOTATED_DEFAULT);
		List<PropertyModifier> inheritedModifiers = Arrays.asList(CONFIGURED_DEFAULT, ANNOTATED_DEFAULT);
		Set<PropertyModifier> expectedModifiers = EnumSet.of(ANNOTATED_DEFAULT);
		assertPropertyState(localModifiers, inheritedModifiers, expectedModifiers);
	}

	public void testLocalAnnotatedDefaultInheritedConfiguredDefaultMandatory() {
		Set<PropertyModifier> localModifiers = EnumSet.of(ANNOTATED_DEFAULT);
		List<PropertyModifier> inheritedModifiers = Arrays.asList(CONFIGURED_DEFAULT, MANDATORY);
		Set<PropertyModifier> expectedModifiers = EnumSet.of(ANNOTATED_DEFAULT);
		assertPropertyState(localModifiers, inheritedModifiers, expectedModifiers);
	}

	// testLocalAnnotatedDefaultInheritedConfiguredDefaultContainer
	
	public void testLocalAnnotatedDefaultInheritedConfiguredDefaultDerived() {
		Set<PropertyModifier> localModifiers = EnumSet.of(ANNOTATED_DEFAULT);
		List<PropertyModifier> inheritedModifiers = Arrays.asList(CONFIGURED_DEFAULT, DERIVED);
		Set<PropertyModifier> expectedModifiers = EnumSet.of(ANNOTATED_DEFAULT, DERIVED);
		assertPropertyState(localModifiers, inheritedModifiers, expectedModifiers);
	}

	public void testLocalAnnotatedDefaultInheritedConfiguredDefaultAbstract() {
		Set<PropertyModifier> localModifiers = EnumSet.of(ANNOTATED_DEFAULT);
		List<PropertyModifier> inheritedModifiers = Arrays.asList(CONFIGURED_DEFAULT, ABSTRACT);
		Set<PropertyModifier> expectedModifiers = EnumSet.of(ANNOTATED_DEFAULT);
		assertPropertyState(localModifiers, inheritedModifiers, expectedModifiers);
	}

	public void testLocalAnnotatedDefaultInheritedAnnotatedDefaultConfiguredDefault() {
		Set<PropertyModifier> localModifiers = EnumSet.of(ANNOTATED_DEFAULT);
		List<PropertyModifier> inheritedModifiers = Arrays.asList(ANNOTATED_DEFAULT, CONFIGURED_DEFAULT);
		Set<PropertyModifier> expectedModifiers = EnumSet.of(ANNOTATED_DEFAULT);
		assertPropertyState(localModifiers, inheritedModifiers, expectedModifiers);
	}

	public void testLocalAnnotatedDefaultInheritedAnnotatedDefaultAnnotatedDefault() {
		Set<PropertyModifier> localModifiers = EnumSet.of(ANNOTATED_DEFAULT);
		List<PropertyModifier> inheritedModifiers = Arrays.asList(ANNOTATED_DEFAULT, ANNOTATED_DEFAULT);
		Set<PropertyModifier> expectedModifiers = EnumSet.of(ANNOTATED_DEFAULT);
		assertPropertyState(localModifiers, inheritedModifiers, expectedModifiers);
	}

	public void testLocalAnnotatedDefaultInheritedAnnotatedDefaultMandatory() {
		Set<PropertyModifier> localModifiers = EnumSet.of(ANNOTATED_DEFAULT);
		List<PropertyModifier> inheritedModifiers = Arrays.asList(ANNOTATED_DEFAULT, MANDATORY);
		Set<PropertyModifier> expectedModifiers = EnumSet.of(ANNOTATED_DEFAULT);
		assertPropertyState(localModifiers, inheritedModifiers, expectedModifiers);
	}

	public void testLocalAnnotatedDefaultInheritedAnnotatedDefaultContainer() {
		Set<PropertyModifier> localModifiers = EnumSet.of(ANNOTATED_DEFAULT);
		List<PropertyModifier> inheritedModifiers = Arrays.asList(ANNOTATED_DEFAULT, CONTAINER);
		Set<PropertyModifier> expectedModifiers = EnumSet.of(ANNOTATED_DEFAULT);
		assertPropertyState(localModifiers, inheritedModifiers, expectedModifiers);
	}
	
	public void testLocalAnnotatedDefaultInheritedAnnotatedDefaultDerived() {
		Set<PropertyModifier> localModifiers = EnumSet.of(ANNOTATED_DEFAULT);
		List<PropertyModifier> inheritedModifiers = Arrays.asList(ANNOTATED_DEFAULT, DERIVED);
		Set<PropertyModifier> expectedModifiers = EnumSet.of(ANNOTATED_DEFAULT, DERIVED);
		assertPropertyState(localModifiers, inheritedModifiers, expectedModifiers);
	}

	public void testLocalAnnotatedDefaultInheritedAnnotatedDefaultAbstract() {
		Set<PropertyModifier> localModifiers = EnumSet.of(ANNOTATED_DEFAULT);
		List<PropertyModifier> inheritedModifiers = Arrays.asList(ANNOTATED_DEFAULT, ABSTRACT);
		Set<PropertyModifier> expectedModifiers = EnumSet.of(ANNOTATED_DEFAULT);
		assertPropertyState(localModifiers, inheritedModifiers, expectedModifiers);
	}

	public void testLocalAnnotatedDefaultInheritedMandatoryContainer() {
		Set<PropertyModifier> localModifiers = EnumSet.of(ANNOTATED_DEFAULT);
		List<PropertyModifier> inheritedModifiers = Arrays.asList(MANDATORY, CONTAINER);
		Set<PropertyModifier> expectedModifiers = EnumSet.of(ANNOTATED_DEFAULT);
		assertPropertyState(localModifiers, inheritedModifiers, expectedModifiers);
	}
	
	public void testLocalAnnotatedDefaultInheritedMandatoryDerived() {
		Set<PropertyModifier> localModifiers = EnumSet.of(ANNOTATED_DEFAULT);
		List<PropertyModifier> inheritedModifiers = Arrays.asList(MANDATORY, DERIVED);
		Set<PropertyModifier> expectedModifiers = EnumSet.of(ANNOTATED_DEFAULT, DERIVED);
		assertPropertyState(localModifiers, inheritedModifiers, expectedModifiers);
	}

	public void testLocalAnnotatedDefaultInheritedMandatoryAbstract() {
		Set<PropertyModifier> localModifiers = EnumSet.of(ANNOTATED_DEFAULT);
		List<PropertyModifier> inheritedModifiers = Arrays.asList(MANDATORY, ABSTRACT);
		Set<PropertyModifier> expectedModifiers = EnumSet.of(ANNOTATED_DEFAULT);
		assertPropertyState(localModifiers, inheritedModifiers, expectedModifiers);
	}

	public void testLocalAnnotatedDefaultInheritedContainerContainer() {
		Set<PropertyModifier> localModifiers = EnumSet.of(ANNOTATED_DEFAULT);
		List<PropertyModifier> inheritedModifiers = Arrays.asList(CONTAINER, CONTAINER);
		Set<PropertyModifier> expectedModifiers = EnumSet.of(ANNOTATED_DEFAULT);
		assertPropertyState(localModifiers, inheritedModifiers, expectedModifiers);
	}

	public void testLocalAnnotatedDefaultInheritedContainerDerived() {
		Set<PropertyModifier> localModifiers = EnumSet.of(ANNOTATED_DEFAULT);
		List<PropertyModifier> inheritedModifiers = Arrays.asList(CONTAINER, DERIVED);
		Set<PropertyModifier> expectedModifiers = EnumSet.of(ANNOTATED_DEFAULT, DERIVED);
		assertPropertyState(localModifiers, inheritedModifiers, expectedModifiers);
	}
	
	public void testLocalAnnotatedDefaultInheritedContainerAbstract() {
		Set<PropertyModifier> localModifiers = EnumSet.of(ANNOTATED_DEFAULT);
		List<PropertyModifier> inheritedModifiers = Arrays.asList(CONTAINER, ABSTRACT);
		Set<PropertyModifier> expectedModifiers = EnumSet.of(ANNOTATED_DEFAULT);
		assertPropertyState(localModifiers, inheritedModifiers, expectedModifiers);
	}

	public void testLocalAnnotatedDefaultInheritedDerivedContainer() {
		Set<PropertyModifier> localModifiers = EnumSet.of(ANNOTATED_DEFAULT);
		List<PropertyModifier> inheritedModifiers = Arrays.asList(DERIVED, CONTAINER);
		Set<PropertyModifier> expectedModifiers = EnumSet.of(ANNOTATED_DEFAULT, DERIVED);
		assertPropertyState(localModifiers, inheritedModifiers, expectedModifiers);
	}
	
	public void testLocalAnnotatedDefaultInheritedDerivedDerived() {
		Set<PropertyModifier> localModifiers = EnumSet.of(ANNOTATED_DEFAULT);
		List<PropertyModifier> inheritedModifiers = Arrays.asList(DERIVED, DERIVED);
		Set<PropertyModifier> expectedModifiers = EnumSet.of(ANNOTATED_DEFAULT, DERIVED);
		assertPropertyState(localModifiers, inheritedModifiers, expectedModifiers);
	}
	
	public void testLocalAnnotatedDefaultInheritedDerivedAbstract() {
		Set<PropertyModifier> localModifiers = EnumSet.of(ANNOTATED_DEFAULT);
		List<PropertyModifier> inheritedModifiers = Arrays.asList(DERIVED, ABSTRACT);
		Set<PropertyModifier> expectedModifiers = EnumSet.of(ANNOTATED_DEFAULT, DERIVED);
		assertPropertyState(localModifiers, inheritedModifiers, expectedModifiers);
	}

	/* Section: Local mandatory annotation and two inherited annotations */
	public void testLocalMandatoryInheritedConfiguredDefaultConfiguredDefault() {
		Set<PropertyModifier> localModifiers = EnumSet.of(MANDATORY);
		List<PropertyModifier> inheritedModifiers = Arrays.asList(CONFIGURED_DEFAULT, CONFIGURED_DEFAULT);
		Set<PropertyModifier> expectedModifiers = EnumSet.of(MANDATORY);
		assertPropertyState(localModifiers, inheritedModifiers, expectedModifiers);
	}

	public void testLocalMandatoryInheritedConfiguredDefaultAnnotatedDefault() {
		Set<PropertyModifier> localModifiers = EnumSet.of(MANDATORY);
		List<PropertyModifier> inheritedModifiers = Arrays.asList(CONFIGURED_DEFAULT, ANNOTATED_DEFAULT);
		Set<PropertyModifier> expectedModifiers = EnumSet.of(MANDATORY);
		assertPropertyState(localModifiers, inheritedModifiers, expectedModifiers);
	}
	
	public void testLocalMandatoryInheritedConfiguredDefaultMandatory() {
		Set<PropertyModifier> localModifiers = EnumSet.of(MANDATORY);
		List<PropertyModifier> inheritedModifiers = Arrays.asList(CONFIGURED_DEFAULT, MANDATORY);
		Set<PropertyModifier> expectedModifiers = EnumSet.of(MANDATORY);
		assertPropertyState(localModifiers, inheritedModifiers, expectedModifiers);
	}

	// testLocalMandatoryInheritedConfiguredDefaultContainer
	
	public void testLocalMandatoryInheritedConfiguredDefaultDerived() {
		Set<PropertyModifier> localModifiers = EnumSet.of(MANDATORY);
		List<PropertyModifier> inheritedModifiers = Arrays.asList(CONFIGURED_DEFAULT, DERIVED);
		Set<PropertyModifier> expectedModifiers = EnumSet.of(MANDATORY);
		assertPropertyState(localModifiers, inheritedModifiers, expectedModifiers);
	}

	public void testLocalMandatoryInheritedConfiguredDefaultAbstract() {
		Set<PropertyModifier> localModifiers = EnumSet.of(MANDATORY);
		List<PropertyModifier> inheritedModifiers = Arrays.asList(CONFIGURED_DEFAULT, ABSTRACT);
		Set<PropertyModifier> expectedModifiers = EnumSet.of(MANDATORY);
		assertPropertyState(localModifiers, inheritedModifiers, expectedModifiers);
	}

	public void testLocalMandatoryInheritedAnnotatedDefaultConfiguredDefault() {
		Set<PropertyModifier> localModifiers = EnumSet.of(MANDATORY);
		List<PropertyModifier> inheritedModifiers = Arrays.asList(ANNOTATED_DEFAULT, CONFIGURED_DEFAULT);
		Set<PropertyModifier> expectedModifiers = EnumSet.of(MANDATORY);
		assertPropertyState(localModifiers, inheritedModifiers, expectedModifiers);
	}
	
	public void testLocalMandatoryInheritedAnnotatedDefaultAnnotatedDefault() {
		Set<PropertyModifier> localModifiers = EnumSet.of(MANDATORY);
		List<PropertyModifier> inheritedModifiers = Arrays.asList(ANNOTATED_DEFAULT, ANNOTATED_DEFAULT);
		Set<PropertyModifier> expectedModifiers = EnumSet.of(MANDATORY);
		assertPropertyState(localModifiers, inheritedModifiers, expectedModifiers);
	}

	public void testLocalMandatoryInheritedAnnotatedDefaultMandatory() {
		Set<PropertyModifier> localModifiers = EnumSet.of(MANDATORY);
		List<PropertyModifier> inheritedModifiers = Arrays.asList(ANNOTATED_DEFAULT, MANDATORY);
		Set<PropertyModifier> expectedModifiers = EnumSet.of(MANDATORY);
		assertPropertyState(localModifiers, inheritedModifiers, expectedModifiers);
	}

	public void testLocalMandatoryInheritedAnnotatedDefaultContainer() {
		Set<PropertyModifier> localModifiers = EnumSet.of(MANDATORY);
		List<PropertyModifier> inheritedModifiers = Arrays.asList(ANNOTATED_DEFAULT, CONTAINER);
		Set<PropertyModifier> expectedModifiers = EnumSet.of(MANDATORY);
		assertPropertyState(localModifiers, inheritedModifiers, expectedModifiers);
	}
	
	public void testLocalMandatoryInheritedAnnotatedDefaultDerived() {
		Set<PropertyModifier> localModifiers = EnumSet.of(MANDATORY);
		List<PropertyModifier> inheritedModifiers = Arrays.asList(ANNOTATED_DEFAULT, DERIVED);
		Set<PropertyModifier> expectedModifiers = EnumSet.of(MANDATORY);
		assertPropertyState(localModifiers, inheritedModifiers, expectedModifiers);
	}

	public void testLocalMandatoryInheritedAnnotatedDefaultAbstract() {
		Set<PropertyModifier> localModifiers = EnumSet.of(MANDATORY);
		List<PropertyModifier> inheritedModifiers = Arrays.asList(ANNOTATED_DEFAULT, ABSTRACT);
		Set<PropertyModifier> expectedModifiers = EnumSet.of(MANDATORY);
		assertPropertyState(localModifiers, inheritedModifiers, expectedModifiers);
	}

	public void testLocalMandatoryInheritedMandatoryContainer() {
		Set<PropertyModifier> localModifiers = EnumSet.of(MANDATORY);
		List<PropertyModifier> inheritedModifiers = Arrays.asList(MANDATORY, CONTAINER);
		Set<PropertyModifier> expectedModifiers = EnumSet.of(MANDATORY);
		assertPropertyState(localModifiers, inheritedModifiers, expectedModifiers);
	}
	
	public void testLocalMandatoryInheritedMandatoryDerived() {
		Set<PropertyModifier> localModifiers = EnumSet.of(MANDATORY);
		List<PropertyModifier> inheritedModifiers = Arrays.asList(MANDATORY, DERIVED);
		Set<PropertyModifier> expectedModifiers = EnumSet.of(MANDATORY);
		assertPropertyState(localModifiers, inheritedModifiers, expectedModifiers);
	}

	public void testLocalMandatoryInheritedMandatoryAbstract() {
		Set<PropertyModifier> localModifiers = EnumSet.of(MANDATORY);
		List<PropertyModifier> inheritedModifiers = Arrays.asList(MANDATORY, ABSTRACT);
		Set<PropertyModifier> expectedModifiers = EnumSet.of(MANDATORY);
		assertPropertyState(localModifiers, inheritedModifiers, expectedModifiers);
	}

	public void testLocalMandatoryInheritedContainerContainer() {
		Set<PropertyModifier> localModifiers = EnumSet.of(MANDATORY);
		List<PropertyModifier> inheritedModifiers = Arrays.asList(CONTAINER, CONTAINER);
		Set<PropertyModifier> expectedModifiers = EnumSet.of(MANDATORY);
		assertPropertyState(localModifiers, inheritedModifiers, expectedModifiers);
	}

	public void testLocalMandatoryInheritedContainerDerived() {
		Set<PropertyModifier> localModifiers = EnumSet.of(MANDATORY);
		List<PropertyModifier> inheritedModifiers = Arrays.asList(CONTAINER, DERIVED);
		Set<PropertyModifier> expectedModifiers = EnumSet.of(MANDATORY);
		assertPropertyState(localModifiers, inheritedModifiers, expectedModifiers);
	}
	
	public void testLocalMandatoryInheritedContainerAbstract() {
		Set<PropertyModifier> localModifiers = EnumSet.of(MANDATORY);
		List<PropertyModifier> inheritedModifiers = Arrays.asList(CONTAINER, ABSTRACT);
		Set<PropertyModifier> expectedModifiers = EnumSet.of(MANDATORY);
		assertPropertyState(localModifiers, inheritedModifiers, expectedModifiers);
	}

	public void testLocalMandatoryInheritedDerivedContainer() {
		Set<PropertyModifier> localModifiers = EnumSet.of(MANDATORY);
		List<PropertyModifier> inheritedModifiers = Arrays.asList(DERIVED, CONTAINER);
		Set<PropertyModifier> expectedModifiers = EnumSet.of(MANDATORY);
		assertPropertyState(localModifiers, inheritedModifiers, expectedModifiers);
	}
	
	public void testLocalMandatoryInheritedDerivedDerived() {
		Set<PropertyModifier> localModifiers = EnumSet.of(MANDATORY);
		List<PropertyModifier> inheritedModifiers = Arrays.asList(DERIVED, DERIVED);
		Set<PropertyModifier> expectedModifiers = EnumSet.of(MANDATORY);
		assertPropertyState(localModifiers, inheritedModifiers, expectedModifiers);
	}
	
	public void testLocalMandatoryInheritedDerivedAbstract() {
		Set<PropertyModifier> localModifiers = EnumSet.of(MANDATORY);
		List<PropertyModifier> inheritedModifiers = Arrays.asList(DERIVED, ABSTRACT);
		Set<PropertyModifier> expectedModifiers = EnumSet.of(MANDATORY);
		assertPropertyState(localModifiers, inheritedModifiers, expectedModifiers);
	}

	/* Section: Local container annotation and two inherited annotations */

	// testLocalContainerInheritedConfiguredDefaultConfiguredDefault
	// testLocalContainerInheritedConfiguredDefaultAnnotatedDefault
	// testLocalContainerInheritedConfiguredDefaultMandatory
	// testLocalContainerInheritedConfiguredDefaultContainer
	// testLocalContainerInheritedConfiguredDefaultDerived
	// testLocalContainerInheritedConfiguredDefaultAbstract
	// testLocalContainerInheritedAnnotatedDefaultConfiguredDefault

	public void testLocalContainerInheritedAnnotatedDefaultAnnotatedDefault() {
		Set<PropertyModifier> localModifiers = EnumSet.of(CONTAINER);
		List<PropertyModifier> inheritedModifiers = Arrays.asList(ANNOTATED_DEFAULT, ANNOTATED_DEFAULT);
		Set<PropertyModifier> expectedModifiers = EnumSet.of(CONTAINER);
		assertPropertyState(localModifiers, inheritedModifiers, expectedModifiers);
	}

	public void testLocalContainerInheritedAnnotatedDefaultMandatory() {
		Set<PropertyModifier> localModifiers = EnumSet.of(CONTAINER);
		List<PropertyModifier> inheritedModifiers = Arrays.asList(ANNOTATED_DEFAULT, MANDATORY);
		Set<PropertyModifier> expectedModifiers = EnumSet.of(CONTAINER);
		assertPropertyState(localModifiers, inheritedModifiers, expectedModifiers);
	}

	public void testLocalContainerInheritedAnnotatedDefaultContainer() {
		Set<PropertyModifier> localModifiers = EnumSet.of(CONTAINER);
		List<PropertyModifier> inheritedModifiers = Arrays.asList(ANNOTATED_DEFAULT, CONTAINER);
		Set<PropertyModifier> expectedModifiers = EnumSet.of(CONTAINER);
		assertPropertyState(localModifiers, inheritedModifiers, expectedModifiers);
	}

	public void testLocalContainerInheritedAnnotatedDefaultDerived() {
		Set<PropertyModifier> localModifiers = EnumSet.of(CONTAINER);
		List<PropertyModifier> inheritedModifiers = Arrays.asList(ANNOTATED_DEFAULT, DERIVED);
		Set<PropertyModifier> expectedModifiers = EnumSet.of(CONTAINER);
		assertPropertyState(localModifiers, inheritedModifiers, expectedModifiers);
	}

	public void testLocalContainerInheritedAnnotatedDefaultAbstract() {
		Set<PropertyModifier> localModifiers = EnumSet.of(CONTAINER);
		List<PropertyModifier> inheritedModifiers = Arrays.asList(ANNOTATED_DEFAULT, ABSTRACT);
		Set<PropertyModifier> expectedModifiers = EnumSet.of(CONTAINER);
		assertPropertyState(localModifiers, inheritedModifiers, expectedModifiers);
	}

	public void testLocalContainerInheritedMandatoryContainer() {
		Set<PropertyModifier> localModifiers = EnumSet.of(CONTAINER);
		List<PropertyModifier> inheritedModifiers = Arrays.asList(MANDATORY, CONTAINER);
		Set<PropertyModifier> expectedModifiers = EnumSet.of(CONTAINER);
		assertPropertyState(localModifiers, inheritedModifiers, expectedModifiers);
	}

	public void testLocalContainerInheritedMandatoryDerived() {
		Set<PropertyModifier> localModifiers = EnumSet.of(CONTAINER);
		List<PropertyModifier> inheritedModifiers = Arrays.asList(MANDATORY, DERIVED);
		Set<PropertyModifier> expectedModifiers = EnumSet.of(CONTAINER);
		assertPropertyState(localModifiers, inheritedModifiers, expectedModifiers);
	}

	public void testLocalContainerInheritedMandatoryAbstract() {
		Set<PropertyModifier> localModifiers = EnumSet.of(CONTAINER);
		List<PropertyModifier> inheritedModifiers = Arrays.asList(MANDATORY, ABSTRACT);
		Set<PropertyModifier> expectedModifiers = EnumSet.of(CONTAINER);
		assertPropertyState(localModifiers, inheritedModifiers, expectedModifiers);
	}

	public void testLocalContainerInheritedContainerContainer() {
		Set<PropertyModifier> localModifiers = EnumSet.of(CONTAINER);
		List<PropertyModifier> inheritedModifiers = Arrays.asList(CONTAINER, CONTAINER);
		Set<PropertyModifier> expectedModifiers = EnumSet.of(CONTAINER);
		assertPropertyState(localModifiers, inheritedModifiers, expectedModifiers);
	}

	public void testLocalContainerInheritedContainerDerived() {
		Set<PropertyModifier> localModifiers = EnumSet.of(CONTAINER);
		List<PropertyModifier> inheritedModifiers = Arrays.asList(CONTAINER, DERIVED);
		Set<PropertyModifier> expectedModifiers = EnumSet.of(CONTAINER);
		assertPropertyState(localModifiers, inheritedModifiers, expectedModifiers);
	}

	public void testLocalContainerInheritedContainerAbstract() {
		Set<PropertyModifier> localModifiers = EnumSet.of(CONTAINER);
		List<PropertyModifier> inheritedModifiers = Arrays.asList(CONTAINER, ABSTRACT);
		Set<PropertyModifier> expectedModifiers = EnumSet.of(CONTAINER);
		assertPropertyState(localModifiers, inheritedModifiers, expectedModifiers);
	}

	public void testLocalContainerInheritedDerivedContainer() {
		Set<PropertyModifier> localModifiers = EnumSet.of(CONTAINER);
		List<PropertyModifier> inheritedModifiers = Arrays.asList(DERIVED, CONTAINER);
		Set<PropertyModifier> expectedModifiers = EnumSet.of(CONTAINER);
		assertPropertyState(localModifiers, inheritedModifiers, expectedModifiers);
	}

	public void testLocalContainerInheritedDerivedDerived() {
		Set<PropertyModifier> localModifiers = EnumSet.of(CONTAINER);
		List<PropertyModifier> inheritedModifiers = Arrays.asList(DERIVED, DERIVED);
		Set<PropertyModifier> expectedModifiers = EnumSet.of(CONTAINER);
		assertPropertyState(localModifiers, inheritedModifiers, expectedModifiers);
	}
	
	public void testLocalContainerInheritedDerivedAbstract() {
		Set<PropertyModifier> localModifiers = EnumSet.of(CONTAINER);
		List<PropertyModifier> inheritedModifiers = Arrays.asList(DERIVED, ABSTRACT);
		Set<PropertyModifier> expectedModifiers = EnumSet.of(CONTAINER);
		assertPropertyState(localModifiers, inheritedModifiers, expectedModifiers);
	}

	/* Section: Local derived annotation and two inherited annotations */
	public void testLocalDerivedInheritedConfiguredDefaultConfiguredDefault() {
		Set<PropertyModifier> localModifiers = EnumSet.of(DERIVED);
		List<PropertyModifier> inheritedModifiers = Arrays.asList(CONFIGURED_DEFAULT, CONFIGURED_DEFAULT);
		Set<PropertyModifier> expectedModifiers = EnumSet.of(DERIVED, CONFIGURED_DEFAULT);
		assertPropertyState(localModifiers, inheritedModifiers, expectedModifiers);
	}

	public void testLocalDerivedInheritedConfiguredDefaultAnnotatedDefault() {
		Set<PropertyModifier> localModifiers = EnumSet.of(DERIVED);
		List<PropertyModifier> inheritedModifiers = Arrays.asList(CONFIGURED_DEFAULT, ANNOTATED_DEFAULT);
		Set<PropertyModifier> expectedModifiers = EnumSet.of(DERIVED, CONFIGURED_DEFAULT);
		assertPropertyState(localModifiers, inheritedModifiers, expectedModifiers);
	}

	public void testLocalDerivedInheritedConfiguredDefaultMandatory() {
		Set<PropertyModifier> localModifiers = EnumSet.of(DERIVED);
		List<PropertyModifier> inheritedModifiers = Arrays.asList(CONFIGURED_DEFAULT, MANDATORY);
		Set<PropertyModifier> expectedModifiers = EnumSet.of(DERIVED, CONFIGURED_DEFAULT);
		assertPropertyState(localModifiers, inheritedModifiers, expectedModifiers);
	}

	// testLocalDerivedInheritedConfiguredDefaultContainer

	public void testLocalDerivedInheritedConfiguredDefaultDerived() {
		Set<PropertyModifier> localModifiers = EnumSet.of(DERIVED);
		List<PropertyModifier> inheritedModifiers = Arrays.asList(CONFIGURED_DEFAULT, DERIVED);
		Set<PropertyModifier> expectedModifiers = EnumSet.of(DERIVED, CONFIGURED_DEFAULT);
		assertPropertyState(localModifiers, inheritedModifiers, expectedModifiers);
	}

	public void testLocalDerivedInheritedConfiguredDefaultAbstract() {
		Set<PropertyModifier> localModifiers = EnumSet.of(DERIVED);
		List<PropertyModifier> inheritedModifiers = Arrays.asList(CONFIGURED_DEFAULT, ABSTRACT);
		Set<PropertyModifier> expectedModifiers = EnumSet.of(DERIVED, CONFIGURED_DEFAULT);
		assertPropertyState(localModifiers, inheritedModifiers, expectedModifiers);
	}

	public void testLocalDerivedInheritedAnnotatedDefaultConfiguredDefault() {
		Set<PropertyModifier> localModifiers = EnumSet.of(DERIVED);
		List<PropertyModifier> inheritedModifiers = Arrays.asList(ANNOTATED_DEFAULT, CONFIGURED_DEFAULT);
		Set<PropertyModifier> expectedModifiers = EnumSet.of(DERIVED, CONFIGURED_DEFAULT);
		assertPropertyState(localModifiers, inheritedModifiers, expectedModifiers);
	}

	public void testLocalDerivedInheritedAnnotatedDefaultAnnotatedDefault() {
		Set<PropertyModifier> localModifiers = EnumSet.of(DERIVED);
		List<PropertyModifier> inheritedModifiers = Arrays.asList(ANNOTATED_DEFAULT, ANNOTATED_DEFAULT);
		Set<PropertyModifier> expectedModifiers = EnumSet.of(DERIVED, ANNOTATED_DEFAULT);
		assertPropertyState(localModifiers, inheritedModifiers, expectedModifiers);
	}

	public void testLocalDerivedInheritedAnnotatedDefaultMandatory() {
		Set<PropertyModifier> localModifiers = EnumSet.of(DERIVED);
		List<PropertyModifier> inheritedModifiers = Arrays.asList(ANNOTATED_DEFAULT, MANDATORY);
		Set<PropertyModifier> expectedModifiers = EnumSet.of(DERIVED, ANNOTATED_DEFAULT);
		assertPropertyState(localModifiers, inheritedModifiers, expectedModifiers);
	}

	public void testLocalDerivedInheritedAnnotatedDefaultContainer() {
		Set<PropertyModifier> localModifiers = EnumSet.of(DERIVED);
		List<PropertyModifier> inheritedModifiers = Arrays.asList(ANNOTATED_DEFAULT, CONTAINER);
		Set<PropertyModifier> expectedModifiers = EnumSet.of(DERIVED, ANNOTATED_DEFAULT);
		assertPropertyState(localModifiers, inheritedModifiers, expectedModifiers);
	}

	public void testLocalDerivedInheritedAnnotatedDefaultDerived() {
		Set<PropertyModifier> localModifiers = EnumSet.of(DERIVED);
		List<PropertyModifier> inheritedModifiers = Arrays.asList(ANNOTATED_DEFAULT, DERIVED);
		Set<PropertyModifier> expectedModifiers = EnumSet.of(DERIVED, ANNOTATED_DEFAULT);
		assertPropertyState(localModifiers, inheritedModifiers, expectedModifiers);
	}

	public void testLocalDerivedInheritedAnnotatedDefaultAbstract() {
		Set<PropertyModifier> localModifiers = EnumSet.of(DERIVED);
		List<PropertyModifier> inheritedModifiers = Arrays.asList(ANNOTATED_DEFAULT, ABSTRACT);
		Set<PropertyModifier> expectedModifiers = EnumSet.of(DERIVED, ANNOTATED_DEFAULT);
		assertPropertyState(localModifiers, inheritedModifiers, expectedModifiers);
	}

	public void testLocalDerivedInheritedMandatoryContainer() {
		Set<PropertyModifier> localModifiers = EnumSet.of(DERIVED);
		List<PropertyModifier> inheritedModifiers = Arrays.asList(MANDATORY, CONTAINER);
		Set<PropertyModifier> expectedModifiers = EnumSet.of(DERIVED);
		assertPropertyState(localModifiers, inheritedModifiers, expectedModifiers);
	}

	public void testLocalDerivedInheritedMandatoryDerived() {
		Set<PropertyModifier> localModifiers = EnumSet.of(DERIVED);
		List<PropertyModifier> inheritedModifiers = Arrays.asList(MANDATORY, DERIVED);
		Set<PropertyModifier> expectedModifiers = EnumSet.of(DERIVED);
		assertPropertyState(localModifiers, inheritedModifiers, expectedModifiers);
	}

	public void testLocalDerivedInheritedMandatoryAbstract() {
		Set<PropertyModifier> localModifiers = EnumSet.of(DERIVED);
		List<PropertyModifier> inheritedModifiers = Arrays.asList(MANDATORY, ABSTRACT);
		Set<PropertyModifier> expectedModifiers = EnumSet.of(DERIVED);
		assertPropertyState(localModifiers, inheritedModifiers, expectedModifiers);
	}

	public void testLocalDerivedInheritedContainerContainer() {
		Set<PropertyModifier> localModifiers = EnumSet.of(DERIVED);
		List<PropertyModifier> inheritedModifiers = Arrays.asList(CONTAINER, CONTAINER);
		Set<PropertyModifier> expectedModifiers = EnumSet.of(DERIVED);
		assertPropertyState(localModifiers, inheritedModifiers, expectedModifiers);
	}

	public void testLocalDerivedInheritedContainerDerived() {
		Set<PropertyModifier> localModifiers = EnumSet.of(DERIVED);
		List<PropertyModifier> inheritedModifiers = Arrays.asList(CONTAINER, DERIVED);
		Set<PropertyModifier> expectedModifiers = EnumSet.of(DERIVED);
		assertPropertyState(localModifiers, inheritedModifiers, expectedModifiers);
	}

	public void testLocalDerivedInheritedContainerAbstract() {
		Set<PropertyModifier> localModifiers = EnumSet.of(DERIVED);
		List<PropertyModifier> inheritedModifiers = Arrays.asList(CONTAINER, ABSTRACT);
		Set<PropertyModifier> expectedModifiers = EnumSet.of(DERIVED);
		assertPropertyState(localModifiers, inheritedModifiers, expectedModifiers);
	}

	public void testLocalDerivedInheritedDerivedContainer() {
		Set<PropertyModifier> localModifiers = EnumSet.of(DERIVED);
		List<PropertyModifier> inheritedModifiers = Arrays.asList(DERIVED, CONTAINER);
		Set<PropertyModifier> expectedModifiers = EnumSet.of(DERIVED);
		assertPropertyState(localModifiers, inheritedModifiers, expectedModifiers);
	}

	public void testLocalDerivedInheritedDerivedDerived() {
		Set<PropertyModifier> localModifiers = EnumSet.of(DERIVED);
		List<PropertyModifier> inheritedModifiers = Arrays.asList(DERIVED, DERIVED);
		Set<PropertyModifier> expectedModifiers = EnumSet.of(DERIVED);
		assertPropertyState(localModifiers, inheritedModifiers, expectedModifiers);
	}

	public void testLocalDerivedInheritedDerivedAbstract() {
		Set<PropertyModifier> localModifiers = EnumSet.of(DERIVED);
		List<PropertyModifier> inheritedModifiers = Arrays.asList(DERIVED, ABSTRACT);
		Set<PropertyModifier> expectedModifiers = EnumSet.of(DERIVED);
		assertPropertyState(localModifiers, inheritedModifiers, expectedModifiers);
	}

	/* Section: Local mandatory annotation and two inherited annotations */
	public void testLocalAbstractInheritedConfiguredDefaultConfiguredDefault() {
		Set<PropertyModifier> localModifiers = EnumSet.of(ABSTRACT);
		List<PropertyModifier> inheritedModifiers = Arrays.asList(CONFIGURED_DEFAULT, CONFIGURED_DEFAULT);
		Set<PropertyModifier> expectedModifiers = EnumSet.of(ABSTRACT);
		assertPropertyState(localModifiers, inheritedModifiers, expectedModifiers);
	}

	public void testLocalAbstractInheritedConfiguredDefaultAnnotatedDefault() {
		Set<PropertyModifier> localModifiers = EnumSet.of(ABSTRACT);
		List<PropertyModifier> inheritedModifiers = Arrays.asList(CONFIGURED_DEFAULT, ANNOTATED_DEFAULT);
		Set<PropertyModifier> expectedModifiers = EnumSet.of(ABSTRACT);
		assertPropertyState(localModifiers, inheritedModifiers, expectedModifiers);
	}

	public void testLocalAbstractInheritedConfiguredDefaultMandatory() {
		Set<PropertyModifier> localModifiers = EnumSet.of(ABSTRACT);
		List<PropertyModifier> inheritedModifiers = Arrays.asList(CONFIGURED_DEFAULT, MANDATORY);
		Set<PropertyModifier> expectedModifiers = EnumSet.of(ABSTRACT);
		assertPropertyState(localModifiers, inheritedModifiers, expectedModifiers);
	}

	// testLocalAbstractInheritedConfiguredDefaultContainer

	public void testLocalAbstractInheritedConfiguredDefaultDerived() {
		Set<PropertyModifier> localModifiers = EnumSet.of(ABSTRACT);
		List<PropertyModifier> inheritedModifiers = Arrays.asList(CONFIGURED_DEFAULT, DERIVED);
		Set<PropertyModifier> expectedModifiers = EnumSet.of(ABSTRACT);
		assertPropertyState(localModifiers, inheritedModifiers, expectedModifiers);
	}

	public void testLocalAbstractInheritedConfiguredDefaultAbstract() {
		Set<PropertyModifier> localModifiers = EnumSet.of(ABSTRACT);
		List<PropertyModifier> inheritedModifiers = Arrays.asList(CONFIGURED_DEFAULT, ABSTRACT);
		Set<PropertyModifier> expectedModifiers = EnumSet.of(ABSTRACT);
		assertPropertyState(localModifiers, inheritedModifiers, expectedModifiers);
	}

	public void testLocalAbstractInheritedAnnotatedDefaultConfiguredDefault() {
		Set<PropertyModifier> localModifiers = EnumSet.of(ABSTRACT);
		List<PropertyModifier> inheritedModifiers = Arrays.asList(ANNOTATED_DEFAULT, CONFIGURED_DEFAULT);
		Set<PropertyModifier> expectedModifiers = EnumSet.of(ABSTRACT);
		assertPropertyState(localModifiers, inheritedModifiers, expectedModifiers);
	}

	public void testLocalAbstractInheritedAnnotatedDefaultAnnotatedDefault() {
		Set<PropertyModifier> localModifiers = EnumSet.of(ABSTRACT);
		List<PropertyModifier> inheritedModifiers = Arrays.asList(ANNOTATED_DEFAULT, ANNOTATED_DEFAULT);
		Set<PropertyModifier> expectedModifiers = EnumSet.of(ABSTRACT);
		assertPropertyState(localModifiers, inheritedModifiers, expectedModifiers);
	}

	public void testLocalAbstractInheritedAnnotatedDefaultMandatory() {
		Set<PropertyModifier> localModifiers = EnumSet.of(ABSTRACT);
		List<PropertyModifier> inheritedModifiers = Arrays.asList(ANNOTATED_DEFAULT, MANDATORY);
		Set<PropertyModifier> expectedModifiers = EnumSet.of(ABSTRACT);
		assertPropertyState(localModifiers, inheritedModifiers, expectedModifiers);
	}

	public void testLocalAbstractInheritedAnnotatedDefaultContainer() {
		Set<PropertyModifier> localModifiers = EnumSet.of(ABSTRACT);
		List<PropertyModifier> inheritedModifiers = Arrays.asList(ANNOTATED_DEFAULT, CONTAINER);
		Set<PropertyModifier> expectedModifiers = EnumSet.of(ABSTRACT);
		assertPropertyState(localModifiers, inheritedModifiers, expectedModifiers);
	}

	public void testLocalAbstractInheritedAnnotatedDefaultDerived() {
		Set<PropertyModifier> localModifiers = EnumSet.of(ABSTRACT);
		List<PropertyModifier> inheritedModifiers = Arrays.asList(ANNOTATED_DEFAULT, DERIVED);
		Set<PropertyModifier> expectedModifiers = EnumSet.of(ABSTRACT);
		assertPropertyState(localModifiers, inheritedModifiers, expectedModifiers);
	}

	public void testLocalAbstractInheritedAnnotatedDefaultAbstract() {
		Set<PropertyModifier> localModifiers = EnumSet.of(ABSTRACT);
		List<PropertyModifier> inheritedModifiers = Arrays.asList(ANNOTATED_DEFAULT, ABSTRACT);
		Set<PropertyModifier> expectedModifiers = EnumSet.of(ABSTRACT);
		assertPropertyState(localModifiers, inheritedModifiers, expectedModifiers);
	}

	public void testLocalAbstractInheritedMandatoryContainer() {
		Set<PropertyModifier> localModifiers = EnumSet.of(ABSTRACT);
		List<PropertyModifier> inheritedModifiers = Arrays.asList(MANDATORY, CONTAINER);
		Set<PropertyModifier> expectedModifiers = EnumSet.of(ABSTRACT);
		assertPropertyState(localModifiers, inheritedModifiers, expectedModifiers);
	}

	public void testLocalAbstractInheritedMandatoryDerived() {
		Set<PropertyModifier> localModifiers = EnumSet.of(ABSTRACT);
		List<PropertyModifier> inheritedModifiers = Arrays.asList(MANDATORY, DERIVED);
		Set<PropertyModifier> expectedModifiers = EnumSet.of(ABSTRACT);
		assertPropertyState(localModifiers, inheritedModifiers, expectedModifiers);
	}

	public void testLocalAbstractInheritedMandatoryAbstract() {
		Set<PropertyModifier> localModifiers = EnumSet.of(ABSTRACT);
		List<PropertyModifier> inheritedModifiers = Arrays.asList(MANDATORY, ABSTRACT);
		Set<PropertyModifier> expectedModifiers = EnumSet.of(ABSTRACT);
		assertPropertyState(localModifiers, inheritedModifiers, expectedModifiers);
	}

	public void testLocalAbstractInheritedContainerContainer() {
		Set<PropertyModifier> localModifiers = EnumSet.of(ABSTRACT);
		List<PropertyModifier> inheritedModifiers = Arrays.asList(CONTAINER, CONTAINER);
		Set<PropertyModifier> expectedModifiers = EnumSet.of(ABSTRACT);
		assertPropertyState(localModifiers, inheritedModifiers, expectedModifiers);
	}

	public void testLocalAbstractInheritedContainerDerived() {
		Set<PropertyModifier> localModifiers = EnumSet.of(ABSTRACT);
		List<PropertyModifier> inheritedModifiers = Arrays.asList(CONTAINER, DERIVED);
		Set<PropertyModifier> expectedModifiers = EnumSet.of(ABSTRACT);
		assertPropertyState(localModifiers, inheritedModifiers, expectedModifiers);
	}

	public void testLocalAbstractInheritedContainerAbstract() {
		Set<PropertyModifier> localModifiers = EnumSet.of(ABSTRACT);
		List<PropertyModifier> inheritedModifiers = Arrays.asList(CONTAINER, ABSTRACT);
		Set<PropertyModifier> expectedModifiers = EnumSet.of(ABSTRACT);
		assertPropertyState(localModifiers, inheritedModifiers, expectedModifiers);
	}

	public void testLocalAbstractInheritedDerivedContainer() {
		Set<PropertyModifier> localModifiers = EnumSet.of(ABSTRACT);
		List<PropertyModifier> inheritedModifiers = Arrays.asList(DERIVED, CONTAINER);
		Set<PropertyModifier> expectedModifiers = EnumSet.of(ABSTRACT);
		assertPropertyState(localModifiers, inheritedModifiers, expectedModifiers);
	}

	public void testLocalAbstractInheritedDerivedDerived() {
		Set<PropertyModifier> localModifiers = EnumSet.of(ABSTRACT);
		List<PropertyModifier> inheritedModifiers = Arrays.asList(DERIVED, DERIVED);
		Set<PropertyModifier> expectedModifiers = EnumSet.of(ABSTRACT);
		assertPropertyState(localModifiers, inheritedModifiers, expectedModifiers);
	}

	public void testLocalAbstractInheritedDerivedAbstract() {
		Set<PropertyModifier> localModifiers = EnumSet.of(ABSTRACT);
		List<PropertyModifier> inheritedModifiers = Arrays.asList(DERIVED, ABSTRACT);
		Set<PropertyModifier> expectedModifiers = EnumSet.of(ABSTRACT);
		assertPropertyState(localModifiers, inheritedModifiers, expectedModifiers);
	}

	/* Section: Three inherited annotations. Every annotation appears only once per test, to keep
	 * the number of tests manageable. I.e. "X X Y" is not tested, only "X Y Z". */
	public void testInheritedConfiguredDefaultAnnotatedDefaultMandatory() {
		Set<PropertyModifier> localModifiers = Collections.emptySet();
		List<PropertyModifier> inheritedModifiers = Arrays.asList(CONFIGURED_DEFAULT, ANNOTATED_DEFAULT, MANDATORY);
		Set<PropertyModifier> expectedModifiers = EnumSet.of(CONFIGURED_DEFAULT);
		assertPropertyState(localModifiers, inheritedModifiers, expectedModifiers);
	}

	// testInheritedConfiguredDefaultAnnotatedDefaultContainer

	public void testInheritedConfiguredDefaultAnnotatedDefaultDerived() {
		Set<PropertyModifier> localModifiers = Collections.emptySet();
		List<PropertyModifier> inheritedModifiers = Arrays.asList(CONFIGURED_DEFAULT, ANNOTATED_DEFAULT, DERIVED);
		Set<PropertyModifier> expectedModifiers = EnumSet.of(CONFIGURED_DEFAULT, DERIVED);
		assertPropertyState(localModifiers, inheritedModifiers, expectedModifiers);
	}

	public void testInheritedConfiguredDefaultAnnotatedDefaultAbstract() {
		Set<PropertyModifier> localModifiers = Collections.emptySet();
		List<PropertyModifier> inheritedModifiers = Arrays.asList(CONFIGURED_DEFAULT, ANNOTATED_DEFAULT, ABSTRACT);
		Set<PropertyModifier> expectedModifiers = EnumSet.of(CONFIGURED_DEFAULT);
		assertPropertyState(localModifiers, inheritedModifiers, expectedModifiers);
	}

	// testInheritedConfiguredDefaultMandatoryContainer

	public void testInheritedConfiguredDefaultMandatoryDerived() {
		Set<PropertyModifier> localModifiers = Collections.emptySet();
		List<PropertyModifier> inheritedModifiers = Arrays.asList(CONFIGURED_DEFAULT, MANDATORY, DERIVED);
		Set<PropertyModifier> expectedModifiers = EnumSet.of(CONFIGURED_DEFAULT, DERIVED);
		assertPropertyState(localModifiers, inheritedModifiers, expectedModifiers);
	}

	public void testInheritedConfiguredDefaultMandatoryAbstract() {
		Set<PropertyModifier> localModifiers = Collections.emptySet();
		List<PropertyModifier> inheritedModifiers = Arrays.asList(CONFIGURED_DEFAULT, MANDATORY, ABSTRACT);
		Set<PropertyModifier> expectedModifiers = EnumSet.of(CONFIGURED_DEFAULT);
		assertPropertyState(localModifiers, inheritedModifiers, expectedModifiers);
	}

	// testInheritedConfiguredDefaultContainerDerived

	// testInheritedConfiguredDefaultContainerAbstract

	// testInheritedConfiguredDefaultDerivedContainer

	public void testInheritedConfiguredDefaultDerivedAbstract() {
		Set<PropertyModifier> localModifiers = Collections.emptySet();
		List<PropertyModifier> inheritedModifiers = Arrays.asList(CONFIGURED_DEFAULT, DERIVED, ABSTRACT);
		Set<PropertyModifier> expectedModifiers = EnumSet.of(CONFIGURED_DEFAULT, DERIVED);
		assertPropertyState(localModifiers, inheritedModifiers, expectedModifiers);
	}

	public void testInheritedAnnotatedDefaultConfiguredDefaultMandatory() {
		Set<PropertyModifier> localModifiers = Collections.emptySet();
		List<PropertyModifier> inheritedModifiers = Arrays.asList(ANNOTATED_DEFAULT, CONFIGURED_DEFAULT, MANDATORY);
		Set<PropertyModifier> expectedModifiers = EnumSet.of(CONFIGURED_DEFAULT);
		assertPropertyState(localModifiers, inheritedModifiers, expectedModifiers);
	}

	// testInheritedAnnotatedDefaultConfiguredDefaultContainer

	public void testInheritedAnnotatedDefaultConfiguredDefaultDerived() {
		Set<PropertyModifier> localModifiers = Collections.emptySet();
		List<PropertyModifier> inheritedModifiers = Arrays.asList(ANNOTATED_DEFAULT, CONFIGURED_DEFAULT, DERIVED);
		Set<PropertyModifier> expectedModifiers = EnumSet.of(CONFIGURED_DEFAULT, DERIVED);
		assertPropertyState(localModifiers, inheritedModifiers, expectedModifiers);
	}

	public void testInheritedAnnotatedDefaultConfiguredDefaultAbstract() {
		Set<PropertyModifier> localModifiers = Collections.emptySet();
		List<PropertyModifier> inheritedModifiers = Arrays.asList(ANNOTATED_DEFAULT, CONFIGURED_DEFAULT, ABSTRACT);
		Set<PropertyModifier> expectedModifiers = EnumSet.of(CONFIGURED_DEFAULT);
		assertPropertyState(localModifiers, inheritedModifiers, expectedModifiers);
	}

	public void testInheritedAnnotatedDefaultMandatoryContainer() {
		Set<PropertyModifier> localModifiers = Collections.emptySet();
		List<PropertyModifier> inheritedModifiers = Arrays.asList(ANNOTATED_DEFAULT, MANDATORY, CONTAINER);
		Set<PropertyModifier> expectedModifiers = EnumSet.of(ANNOTATED_DEFAULT);
		assertPropertyState(localModifiers, inheritedModifiers, expectedModifiers);
	}

	public void testInheritedAnnotatedDefaultMandatoryDerived() {
		Set<PropertyModifier> localModifiers = Collections.emptySet();
		List<PropertyModifier> inheritedModifiers = Arrays.asList(ANNOTATED_DEFAULT, MANDATORY, DERIVED);
		Set<PropertyModifier> expectedModifiers = EnumSet.of(ANNOTATED_DEFAULT, DERIVED);
		assertPropertyState(localModifiers, inheritedModifiers, expectedModifiers);
	}

	public void testInheritedAnnotatedDefaultMandatoryAbstract() {
		Set<PropertyModifier> localModifiers = Collections.emptySet();
		List<PropertyModifier> inheritedModifiers = Arrays.asList(ANNOTATED_DEFAULT, MANDATORY, ABSTRACT);
		Set<PropertyModifier> expectedModifiers = EnumSet.of(ANNOTATED_DEFAULT);
		assertPropertyState(localModifiers, inheritedModifiers, expectedModifiers);
	}

	public void testInheritedAnnotatedDefaultContainerDerived() {
		Set<PropertyModifier> localModifiers = Collections.emptySet();
		List<PropertyModifier> inheritedModifiers = Arrays.asList(ANNOTATED_DEFAULT, CONTAINER, DERIVED);
		Set<PropertyModifier> expectedModifiers = EnumSet.of(ANNOTATED_DEFAULT, DERIVED);
		assertPropertyState(localModifiers, inheritedModifiers, expectedModifiers);
	}

	public void testInheritedAnnotatedDefaultContainerAbstract() {
		Set<PropertyModifier> localModifiers = Collections.emptySet();
		List<PropertyModifier> inheritedModifiers = Arrays.asList(ANNOTATED_DEFAULT, CONTAINER, ABSTRACT);
		Set<PropertyModifier> expectedModifiers = EnumSet.of(ANNOTATED_DEFAULT);
		assertPropertyState(localModifiers, inheritedModifiers, expectedModifiers);
	}

	public void testInheritedAnnotatedDefaultDerivedContainer() {
		Set<PropertyModifier> localModifiers = Collections.emptySet();
		List<PropertyModifier> inheritedModifiers = Arrays.asList(ANNOTATED_DEFAULT, DERIVED, CONTAINER);
		Set<PropertyModifier> expectedModifiers = EnumSet.of(ANNOTATED_DEFAULT, DERIVED);
		assertPropertyState(localModifiers, inheritedModifiers, expectedModifiers);
	}

	public void testInheritedAnnotatedDefaultDerivedAbstract() {
		Set<PropertyModifier> localModifiers = Collections.emptySet();
		List<PropertyModifier> inheritedModifiers = Arrays.asList(ANNOTATED_DEFAULT, DERIVED, ABSTRACT);
		Set<PropertyModifier> expectedModifiers = EnumSet.of(ANNOTATED_DEFAULT, DERIVED);
		assertPropertyState(localModifiers, inheritedModifiers, expectedModifiers);
	}

	public void testInheritedMandatoryContainerDerived() {
		Set<PropertyModifier> localModifiers = Collections.emptySet();
		List<PropertyModifier> inheritedModifiers = Arrays.asList(MANDATORY, CONTAINER, DERIVED);
		Set<PropertyModifier> expectedModifiers = EnumSet.of(CONTAINER);
		assertPropertyState(localModifiers, inheritedModifiers, expectedModifiers);
	}

	public void testInheritedMandatoryContainerAbstract() {
		Set<PropertyModifier> localModifiers = Collections.emptySet();
		List<PropertyModifier> inheritedModifiers = Arrays.asList(MANDATORY, CONTAINER, ABSTRACT);
		Set<PropertyModifier> expectedModifiers = EnumSet.of(CONTAINER);
		assertPropertyState(localModifiers, inheritedModifiers, expectedModifiers);
	}

	public void testInheritedMandatoryDerivedContainer() {
		Set<PropertyModifier> localModifiers = Collections.emptySet();
		List<PropertyModifier> inheritedModifiers = Arrays.asList(MANDATORY, DERIVED, CONTAINER);
		Set<PropertyModifier> expectedModifiers = EnumSet.of(CONTAINER);
		assertPropertyState(localModifiers, inheritedModifiers, expectedModifiers);
	}

	public void testInheritedMandatoryDerivedAbstract() {
		Set<PropertyModifier> localModifiers = Collections.emptySet();
		List<PropertyModifier> inheritedModifiers = Arrays.asList(MANDATORY, DERIVED, ABSTRACT);
		Set<PropertyModifier> expectedModifiers = EnumSet.of(DERIVED);
		assertPropertyState(localModifiers, inheritedModifiers, expectedModifiers);
	}

	public void testInheritedContainerDerivedAbstract() {
		Set<PropertyModifier> localModifiers = Collections.emptySet();
		List<PropertyModifier> inheritedModifiers = Arrays.asList(CONTAINER, DERIVED, ABSTRACT);
		Set<PropertyModifier> expectedModifiers = EnumSet.of(CONTAINER);
		assertPropertyState(localModifiers, inheritedModifiers, expectedModifiers);
	}

	public void testInheritedDerivedContainerAbstract() {
		Set<PropertyModifier> localModifiers = Collections.emptySet();
		List<PropertyModifier> inheritedModifiers = Arrays.asList(DERIVED, CONTAINER, ABSTRACT);
		Set<PropertyModifier> expectedModifiers = EnumSet.of(CONTAINER);
		assertPropertyState(localModifiers, inheritedModifiers, expectedModifiers);
	}

	/* Section: Four inherited annotations. Every annotation appears only once per test, to keep the
	 * number of tests manageable. I.e. "X X Y" is not tested, only "X Y Z". */

	// testInheritedConfiguredDefaultAnnotatedDefaultMandatoryContainer

	public void testInheritedConfiguredDefaultAnnotatedDefaultMandatoryDerived() {
		Set<PropertyModifier> localModifiers = Collections.emptySet();
		List<PropertyModifier> inheritedModifiers =
			Arrays.asList(CONFIGURED_DEFAULT, ANNOTATED_DEFAULT, MANDATORY, DERIVED);
		Set<PropertyModifier> expectedModifiers = EnumSet.of(CONFIGURED_DEFAULT, DERIVED);
		assertPropertyState(localModifiers, inheritedModifiers, expectedModifiers);
	}

	public void testInheritedConfiguredDefaultAnnotatedDefaultMandatoryAbstract() {
		Set<PropertyModifier> localModifiers = Collections.emptySet();
		List<PropertyModifier> inheritedModifiers =
			Arrays.asList(CONFIGURED_DEFAULT, ANNOTATED_DEFAULT, MANDATORY, ABSTRACT);
		Set<PropertyModifier> expectedModifiers = EnumSet.of(CONFIGURED_DEFAULT);
		assertPropertyState(localModifiers, inheritedModifiers, expectedModifiers);
	}

	// testInheritedConfiguredDefaultAnnotatedDefaultContainerDerived
	// testInheritedConfiguredDefaultAnnotatedDefaultContainerAbstract
	// testInheritedConfiguredDefaultAnnotatedDefaultDerivedContainer

	public void testInheritedConfiguredDefaultAnnotatedDefaultDerivedAbstract() {
		Set<PropertyModifier> localModifiers = Collections.emptySet();
		List<PropertyModifier> inheritedModifiers =
			Arrays.asList(CONFIGURED_DEFAULT, ANNOTATED_DEFAULT, DERIVED, ABSTRACT);
		Set<PropertyModifier> expectedModifiers = EnumSet.of(CONFIGURED_DEFAULT, DERIVED);
		assertPropertyState(localModifiers, inheritedModifiers, expectedModifiers);
	}

	// testInheritedConfiguredDefaultMandatoryContainerDerived
	// testInheritedConfiguredDefaultMandatoryContainerAbstract
	// testInheritedConfiguredDefaultMandatoryDerivedContainer

	public void testInheritedConfiguredDefaultMandatoryDerivedAbstract() {
		Set<PropertyModifier> localModifiers = Collections.emptySet();
		List<PropertyModifier> inheritedModifiers =
			Arrays.asList(CONFIGURED_DEFAULT, MANDATORY, DERIVED, ABSTRACT);
		Set<PropertyModifier> expectedModifiers = EnumSet.of(CONFIGURED_DEFAULT, DERIVED);
		assertPropertyState(localModifiers, inheritedModifiers, expectedModifiers);
	}

	// testInheritedConfiguredDefaultContainerDerivedAbstract
	// testInheritedConfiguredDefaultDerivedContainerAbstract
	// testInheritedAnnotatedDefaultConfiguredDefaultMandatoryContainer

	public void testInheritedAnnotatedDefaultConfiguredDefaultMandatoryDerived() {
		Set<PropertyModifier> localModifiers = Collections.emptySet();
		List<PropertyModifier> inheritedModifiers =
			Arrays.asList(ANNOTATED_DEFAULT, CONFIGURED_DEFAULT, MANDATORY, DERIVED);
		Set<PropertyModifier> expectedModifiers = EnumSet.of(CONFIGURED_DEFAULT, DERIVED);
		assertPropertyState(localModifiers, inheritedModifiers, expectedModifiers);
	}

	public void testInheritedAnnotatedDefaultConfiguredDefaultMandatoryAbstract() {
		Set<PropertyModifier> localModifiers = Collections.emptySet();
		List<PropertyModifier> inheritedModifiers =
			Arrays.asList(ANNOTATED_DEFAULT, CONFIGURED_DEFAULT, MANDATORY, ABSTRACT);
		Set<PropertyModifier> expectedModifiers = EnumSet.of(CONFIGURED_DEFAULT);
		assertPropertyState(localModifiers, inheritedModifiers, expectedModifiers);
	}

	// testInheritedAnnotatedDefaultConfiguredDefaultContainerDerived
	// testInheritedAnnotatedDefaultConfiguredDefaultContainerAbstract
	// testInheritedAnnotatedDefaultConfiguredDefaultDerivedContainer

	public void testInheritedAnnotatedDefaultConfiguredDefaultDerivedAbstract() {
		Set<PropertyModifier> localModifiers = Collections.emptySet();
		List<PropertyModifier> inheritedModifiers =
			Arrays.asList(ANNOTATED_DEFAULT, CONFIGURED_DEFAULT, DERIVED, ABSTRACT);
		Set<PropertyModifier> expectedModifiers = EnumSet.of(CONFIGURED_DEFAULT, DERIVED);
		assertPropertyState(localModifiers, inheritedModifiers, expectedModifiers);
	}

	public void testInheritedAnnotatedDefaultMandatoryContainerDerived() {
		Set<PropertyModifier> localModifiers = Collections.emptySet();
		List<PropertyModifier> inheritedModifiers =
			Arrays.asList(ANNOTATED_DEFAULT, MANDATORY, CONTAINER, DERIVED);
		Set<PropertyModifier> expectedModifiers = EnumSet.of(ANNOTATED_DEFAULT, DERIVED);
		assertPropertyState(localModifiers, inheritedModifiers, expectedModifiers);
	}

	public void testInheritedAnnotatedDefaultMandatoryContainerAbstract() {
		Set<PropertyModifier> localModifiers = Collections.emptySet();
		List<PropertyModifier> inheritedModifiers =
			Arrays.asList(ANNOTATED_DEFAULT, MANDATORY, CONTAINER, ABSTRACT);
		Set<PropertyModifier> expectedModifiers = EnumSet.of(ANNOTATED_DEFAULT);
		assertPropertyState(localModifiers, inheritedModifiers, expectedModifiers);
	}

	public void testInheritedAnnotatedDefaultMandatoryDerivedContainer() {
		Set<PropertyModifier> localModifiers = Collections.emptySet();
		List<PropertyModifier> inheritedModifiers =
			Arrays.asList(ANNOTATED_DEFAULT, MANDATORY, DERIVED, CONTAINER);
		Set<PropertyModifier> expectedModifiers = EnumSet.of(ANNOTATED_DEFAULT, DERIVED);
		assertPropertyState(localModifiers, inheritedModifiers, expectedModifiers);
	}

	public void testInheritedAnnotatedDefaultMandatoryDerivedAbstract() {
		Set<PropertyModifier> localModifiers = Collections.emptySet();
		List<PropertyModifier> inheritedModifiers =
			Arrays.asList(ANNOTATED_DEFAULT, MANDATORY, DERIVED, ABSTRACT);
		Set<PropertyModifier> expectedModifiers = EnumSet.of(ANNOTATED_DEFAULT, DERIVED);
		assertPropertyState(localModifiers, inheritedModifiers, expectedModifiers);
	}

	public void testInheritedAnnotatedDefaultContainerDerivedAbstract() {
		Set<PropertyModifier> localModifiers = Collections.emptySet();
		List<PropertyModifier> inheritedModifiers =
			Arrays.asList(ANNOTATED_DEFAULT, CONTAINER, DERIVED, ABSTRACT);
		Set<PropertyModifier> expectedModifiers = EnumSet.of(ANNOTATED_DEFAULT, DERIVED);
		assertPropertyState(localModifiers, inheritedModifiers, expectedModifiers);
	}

	public void testInheritedAnnotatedDefaultDerivedContainerAbstract() {
		Set<PropertyModifier> localModifiers = Collections.emptySet();
		List<PropertyModifier> inheritedModifiers =
			Arrays.asList(ANNOTATED_DEFAULT, DERIVED, CONTAINER, ABSTRACT);
		Set<PropertyModifier> expectedModifiers = EnumSet.of(ANNOTATED_DEFAULT, DERIVED);
		assertPropertyState(localModifiers, inheritedModifiers, expectedModifiers);
	}

	public void testInheritedMandatoryContainerDerivedAbstract() {
		Set<PropertyModifier> localModifiers = Collections.emptySet();
		List<PropertyModifier> inheritedModifiers =
			Arrays.asList(MANDATORY, CONTAINER, DERIVED, ABSTRACT);
		Set<PropertyModifier> expectedModifiers = EnumSet.of(CONTAINER);
		assertPropertyState(localModifiers, inheritedModifiers, expectedModifiers);
	}

	public void testInheritedMandatoryDerivedContainerAbstract() {
		Set<PropertyModifier> localModifiers = Collections.emptySet();
		List<PropertyModifier> inheritedModifiers =
			Arrays.asList(MANDATORY, DERIVED, CONTAINER, ABSTRACT);
		Set<PropertyModifier> expectedModifiers = EnumSet.of(CONTAINER);
		assertPropertyState(localModifiers, inheritedModifiers, expectedModifiers);
	}

	// custom asserts

	/**
	 * <p>
	 * The class is found by {@link #buildClassName(Set, List)}.
	 * </p>
	 * <p>
	 * The tested properties have to be {@link #TESTED_PROPERTY} and {@link #TESTED_ITEM_PROPERTY}.
	 * </p>
	 * <p>
	 * If a configured default value is expected, it has to have the value
	 * {@link ScenarioAnnotationInteraction#CONFIGURED_DEFAULT_VALUE}.
	 * </p>
	 * <p>
	 * If a annotated default value is expected, it has to have the value
	 * {@link ScenarioAnnotationInteraction#ANNOTATED_DEFAULT_VALUE}.
	 * </p>
	 * <p>
	 * None of the parameters is allowed to be or contain null.
	 * </p>
	 */
	private void assertPropertyState(Set<PropertyModifier> localModifiers, List<PropertyModifier> inheritedModifiers,
			Set<PropertyModifier> expectedModifiers) {
		boolean assertions = false;
		if (!(localModifiers.contains(PropertyModifier.CONTAINER)
			|| inheritedModifiers.contains(PropertyModifier.CONTAINER)
			|| expectedModifiers.contains(PropertyModifier.CONTAINER))) {
			assertPropertyState(TESTED_PROPERTY, localModifiers, inheritedModifiers, expectedModifiers);
			assertions = true;
		}
		if (!(localModifiers.contains(PropertyModifier.CONFIGURED_DEFAULT)
			|| inheritedModifiers.contains(PropertyModifier.CONFIGURED_DEFAULT)
			|| expectedModifiers.contains(PropertyModifier.CONFIGURED_DEFAULT))) {
			assertPropertyState(TESTED_ITEM_PROPERTY, localModifiers, inheritedModifiers, expectedModifiers);
			assertions = true;
		}
		assert assertions;
	}

	private void assertPropertyState(String propertyName, Set<PropertyModifier> localModifiers,
			List<PropertyModifier> inheritedModifiers, Set<PropertyModifier> expectedModifiers) {
		Class<? extends ScenarioTypeBase> itemClass = getClass(buildClassName(localModifiers, inheritedModifiers));
		String messageSituation = buildMessageSituation(localModifiers, inheritedModifiers, itemClass);
		int expectedDefault;
		if (expectedModifiers.contains(PropertyModifier.CONFIGURED_DEFAULT)
			|| expectedModifiers.contains(PropertyModifier.ANNOTATED_DEFAULT)) {
			assertPropertyHasDefault(messageSituation, itemClass, propertyName);
			if (expectedModifiers.contains(PropertyModifier.CONFIGURED_DEFAULT)) {
				assertPropertyDefaultValue(messageSituation, CONFIGURED_DEFAULT_VALUE, itemClass);
				expectedDefault = CONFIGURED_DEFAULT_VALUE;
			} else {
				assert expectedModifiers.contains(PropertyModifier.ANNOTATED_DEFAULT);
				if (propertyName.equals(TESTED_PROPERTY)) {
					assertPropertyDefaultValue(messageSituation, ANNOTATED_DEFAULT_VALUE, itemClass);
				} else {
					assertItemPropertyDefaultValue(messageSituation, itemClass);
				}
				expectedDefault = ANNOTATED_DEFAULT_VALUE;
			}
		} else {
			assertPropertyHasNoDefault(messageSituation, itemClass, propertyName);
			expectedDefault = 0;
		}
		if (expectedModifiers.contains(PropertyModifier.MANDATORY)) {
			assertPropertyIsMandatory(messageSituation, itemClass, propertyName);
		} else {
			assertPropertyIsNotMandatory(messageSituation, itemClass, propertyName);
		}
		if (expectedModifiers.contains(PropertyModifier.DERIVED)) {
			assertPropertyIsDerived(messageSituation, itemClass, propertyName);
			if (propertyName.equals(TESTED_PROPERTY)) {
				assertDerivedUsesPropertySource(messageSituation, itemClass, expectedDefault);
			} else {
				assert propertyName.equals(TESTED_ITEM_PROPERTY);
				assertItemDerivedUsesPropertySource(messageSituation, itemClass);
			}
		} else {
			assertPropertyIsNotDerived(messageSituation, itemClass, propertyName);
		}
		if (expectedModifiers.contains(PropertyModifier.CONTAINER)) {
			assertPropertyIsContainer(messageSituation, itemClass, propertyName);
		} else {
			assertPropertyIsNotContainer(messageSituation, itemClass, propertyName);
		}
		if (expectedModifiers.contains(PropertyModifier.DERIVED)
			|| expectedModifiers.contains(PropertyModifier.CONTAINER)) {
			assertPropertyKindDerived(messageSituation, itemClass, propertyName);
		}
		if ((!expectedModifiers.contains(PropertyModifier.DERIVED))
			&& !expectedModifiers.contains(PropertyModifier.CONTAINER)) {
			assertPropertyKindNotDerived(messageSituation, itemClass, propertyName);
		}
		if (expectedModifiers.contains(PropertyModifier.ABSTRACT)) {
			assertPropertyIsAbstract(messageSituation, itemClass, propertyName);
			assertConfigIsAbstract(messageSituation, itemClass);
		} else {
			assertPropertyIsNotAbstract(messageSituation, itemClass, propertyName);
			assertConfigIsNotAbstract(messageSituation, itemClass);
		}
	}

	private String buildClassName(Set<PropertyModifier> localModifiers, List<PropertyModifier> inheritedModifiers) {
		String packageName = getClass().getPackage().getName();
		String containerClassName = ScenarioAnnotationInteraction.class.getSimpleName();
		String className = packageName + "." + containerClassName;
		if (localModifiers.size() == 2 && inheritedModifiers.isEmpty()) {
			className += "$ScenarioContainerLocalXY";
		} else if (localModifiers.size() == 1 && inheritedModifiers.size() == 1) {
			className += "$ScenarioContainerLocalXInheritedY";
		} else if (localModifiers.isEmpty() && inheritedModifiers.size() == 2) {
			className += "$ScenarioContainerInheritedXY";
		} else if (localModifiers.size() == 1 && inheritedModifiers.size() == 2) {
			PropertyModifier localModifier = CollectionUtil.getFirst(localModifiers);
			className += ("$ScenarioContainerLocal" + localModifier.getClassNamePart() + "InheritedXY");
		} else if (localModifiers.isEmpty() && inheritedModifiers.size() == 3) {
			className += "$ScenarioContainerInheritedXYZ";
		} else if (localModifiers.isEmpty() && inheritedModifiers.size() == 4) {
			className += "$ScenarioContainerInheritedWXYZ";
		} else {
			throw new UnsupportedOperationException("Container interface for this situation is unknown.");
		}
		className += "$" + "ScenarioType";
		if (!localModifiers.isEmpty()) {
			List<PropertyModifier> sortedLocalModifiers = new ArrayList<>(localModifiers);
			className += "Local";
			Collections.sort(sortedLocalModifiers);
			for (PropertyModifier modifier : sortedLocalModifiers) {
				className += modifier.getClassNamePart();
			}
		}
		if (!inheritedModifiers.isEmpty()) {
			className += "Inherited";
			Collections.sort(inheritedModifiers);
			for (PropertyModifier modifier : inheritedModifiers) {
				className += modifier.getClassNamePart();
			}
		}
		return className;
	}

	private Class<? extends ScenarioTypeBase> getClass(String className) {
		try {
			Class<?> rawClass = Class.forName(className);
			assert ScenarioTypeBase.class.isAssignableFrom(rawClass) : "All test classes have to be ConfigurationItems.";
			return rawClass.asSubclass(ScenarioTypeBase.class);
		} catch (ClassNotFoundException ex) {
			throw new RuntimeException(ex);
		}
	}

	private String buildMessageSituation(
			Set<PropertyModifier> localModifiers, List<PropertyModifier> inheritedModifiers, Class<?> itemClass) {
		String messageSituation = "Ticket #11628: Situation: ";
		if (localModifiers.isEmpty()) {
			messageSituation += "Local: Nothing, ";
		} else {
			List<PropertyModifier> sortedLocalModifiers = new ArrayList<>(localModifiers);
			messageSituation += "Local: ";
			Collections.sort(sortedLocalModifiers);
			for (PropertyModifier modifier : sortedLocalModifiers) {
				messageSituation += (modifier.getUiName() + ", ");
			}
		}
		if (inheritedModifiers.isEmpty()) {
			messageSituation += "Inheriting: Nothing.";
		} else {
			messageSituation += "Inheriting: ";
			Collections.sort(inheritedModifiers);
			for (PropertyModifier modifier : inheritedModifiers) {
				messageSituation += (modifier.getUiName() + ", ");
			}
		}
		messageSituation += " Example class: " + itemClass.getName();
		return messageSituation;
	}

	private void assertPropertyHasDefault(String messageSituation, Class<? extends ConfigurationItem> itemClass,
			String propertyName) {

		boolean hasExplicitDefault = property(itemClass, propertyName).hasExplicitDefault();
		String detailMessage =
			" Result: The property '" + propertyName + "' is expected to have an explicit default value, but has none.";
		assertTrue(messageSituation + detailMessage, hasExplicitDefault);
	}

	private void assertPropertyHasNoDefault(String messageSituation, Class<? extends ConfigurationItem> itemClass,
			String propertyName) {

		boolean hasExplicitDefault = property(itemClass, propertyName).hasExplicitDefault();
		String detailMessage =
			" Result: The property '" + propertyName
				+ "' is expected to have no explicit default value, but has one: '"
			+ property(itemClass, propertyName).getDefaultValue() + "'";
		assertFalse(messageSituation + detailMessage, hasExplicitDefault);
	}

	private void assertPropertyIsMandatory(String messageSituation, Class<? extends ConfigurationItem> itemClass,
			String propertyName) {

		boolean isMandatory = property(itemClass, propertyName).isMandatory();
		String detailMessage =
			" Result: The property '" + propertyName + "' is not mandatory, but is expected to be mandatory.";
		assertTrue(messageSituation + detailMessage, isMandatory);
	}

	private void assertPropertyIsNotMandatory(String messageSituation, Class<? extends ConfigurationItem> itemClass,
			String propertyName) {

		boolean isMandatory = property(itemClass, propertyName).isMandatory();
		String detailMessage =
			" Result: The property '" + propertyName + "' is mandatory, but is expected to be not mandatory.";
		assertFalse(messageSituation + detailMessage, isMandatory);
	}

	private void assertPropertyIsContainer(String messageSituation, Class<? extends ConfigurationItem> itemClass,
			String propertyName) {

		boolean isContainer = property(itemClass, propertyName).hasContainerAnnotation();
		String detailMessage =
			" Result: The property '" + propertyName + "' is not a container property, but is expected to be.";
		assertTrue(messageSituation + detailMessage, isContainer);
	}

	private void assertPropertyIsNotContainer(String messageSituation, Class<? extends ConfigurationItem> itemClass,
			String propertyName) {

		boolean isContainer = property(itemClass, propertyName).hasContainerAnnotation();
		String detailMessage =
			" Result: The property '" + propertyName + "' is a container property, but is expected to be not.";
		assertFalse(messageSituation + detailMessage, isContainer);
	}

	private void assertPropertyIsDerived(String messageSituation, Class<? extends ConfigurationItem> itemClass,
			String propertyName) {

		boolean isDerived = property(itemClass, propertyName).isDerived();
		String detailMessage =
			" Result: The property '" + propertyName + "' is not derived, but is expected to be derived.";
		assertTrue(messageSituation + detailMessage, isDerived);
	}

	private void assertPropertyIsNotDerived(String messageSituation, Class<? extends ConfigurationItem> itemClass,
			String propertyName) {

		boolean isDerived = property(itemClass, propertyName).isDerived();
		String detailMessage =
			" Result: The property '" + propertyName + "' is derived, but is expected to be not derived.";
		assertFalse(messageSituation + detailMessage, isDerived);
	}

	private void assertPropertyKindDerived(String messageSituation, Class<? extends ConfigurationItem> itemClass,
			String propertyName) {

		boolean isKindDerived = property(itemClass, propertyName).kind().equals(PropertyKind.DERIVED);
		String detailMessageKind =
			" Result: The property '" + propertyName + "' is not of kind derived, but is expected to be.";
		assertTrue(messageSituation + detailMessageKind, isKindDerived);
	}

	private void assertPropertyKindNotDerived(String messageSituation, Class<? extends ConfigurationItem> itemClass,
			String propertyName) {

		boolean isKindDerived = property(itemClass, propertyName).kind().equals(PropertyKind.DERIVED);
		String detailMessageKind =
			" Result: The property '" + propertyName + "' is of kind derived, but is expected to be not.";
		assertFalse(messageSituation + detailMessageKind, isKindDerived);
	}

	/** Compares with {@link Object#equals(Object)}. */
	private void assertPropertyDefaultValue(String messageSituation, Object expected,
			Class<? extends ConfigurationItem> itemClass) {

		Object actualDefaultValue = property(itemClass, TESTED_PROPERTY).getDefaultValue();
		String detailMessage =
			" Result: The default value of the property '" + TESTED_PROPERTY + "' is not used or wrong.";
		assertEquals(messageSituation + detailMessage, expected, actualDefaultValue);
	}

	private void assertItemPropertyDefaultValue(String messageSituation, Class<? extends ConfigurationItem> itemClass) {
		ConfigurationItem actualDefaultValue =
			(ConfigurationItem) property(itemClass, TESTED_ITEM_PROPERTY).getDefaultValue();
		String detailMessage =
			" Result: The default value of the property '" + TESTED_ITEM_PROPERTY + "' is not used or wrong.";
		assertEquals(messageSituation + detailMessage, ANNOTATED_DEFAULT_ITEM_CLASS,
			getConfigInterfaceOrNull(actualDefaultValue));
	}

	private static void assertDerivedUsesPropertySource(
			String messagePrefix, Class<? extends ScenarioTypeBase> itemClass, int defaultValue) {
		ScenarioTypeBase item = create(itemClass);
		assert item.getSource() == null;
		String messageDefaultNotUsed =
			messagePrefix + " the default value for property '" + TESTED_PROPERTY + "' is not used or wrong.";
		assertEquals(messageDefaultNotUsed, defaultValue, item.getExample());

		ScenarioTypeBase source = create(ScenarioTypeBase.class);
		item.setSource(source);
		int nonDefaultValue = defaultValue + 1;
		source.setExample(nonDefaultValue);
		String messageDerivedValueUnexpected = messagePrefix
				+ " The derived value for property '" + TESTED_PROPERTY
				+ "' is not updated or to the wrong value, when the source is changed.";
		assertEquals(messageDerivedValueUnexpected, nonDefaultValue, item.getExample());

		item.setSource(null);
		String messageDefaultNotUsedOnReset = messagePrefix
				+ " The default value for property '" + TESTED_PROPERTY
				+ "' is not used, when the source ConfigItem is reset to null.";
		assertEquals(messageDefaultNotUsedOnReset, defaultValue, item.getExample());
	}

	private static void assertItemDerivedUsesPropertySource(
			String messagePrefix, Class<? extends ScenarioTypeBase> itemClass) {
		ScenarioTypeBase item = create(itemClass);
		assert item.getSource() == null;

		Class<? extends ConfigurationItem> defaultValue;
		if (property(itemClass, TESTED_ITEM_PROPERTY).hasExplicitDefault()) {
			defaultValue = ANNOTATED_DEFAULT_ITEM_CLASS;
		} else {
			defaultValue = null;
		}
		String messageDefaultNotUsed =
			messagePrefix + " the default for property '" + TESTED_ITEM_PROPERTY + "' value is not used or wrong.";
		assertEquals(messageDefaultNotUsed, defaultValue, getConfigInterfaceOrNull(item.getItemExample()));

		ScenarioTypeBase source = create(ScenarioTypeBase.class);
		item.setSource(source);
		ConfigurationItem nonDefaultValue = create(ScenarioTypeItemD.class);
		source.setItemExample(nonDefaultValue);
		String messageDerivedValueUnexpected = messagePrefix
				+ " The derived value for property '" + TESTED_ITEM_PROPERTY
				+ "' is not updated or to the wrong value, when the source is changed.";
		assertEquals(messageDerivedValueUnexpected,
			nonDefaultValue.getConfigurationInterface(),
			getConfigInterfaceOrNull(item.getItemExample()));

		item.setSource(null);
		String messageDefaultNotUsedOnReset = messagePrefix
				+ " The default value for property '" + TESTED_ITEM_PROPERTY
				+ "' is not used, when the source ConfigItem is reset to null.";
		assertEquals(messageDefaultNotUsedOnReset, defaultValue, getConfigInterfaceOrNull(item.getItemExample()));
	}

	private void assertPropertyIsAbstract(String messageSituation, Class<? extends ConfigurationItem> itemClass,
			String propertyName) {

		boolean isAbstract = property(itemClass, propertyName).isAbstract();
		String detailMessage =
			" Result: The property '" + propertyName + "' is not abstract, but is expected to be abstract.";
		assertTrue(messageSituation + detailMessage, isAbstract);
	}

	private void assertPropertyIsNotAbstract(String messageSituation, Class<? extends ConfigurationItem> itemClass,
			String propertyName) {

		boolean isAbstract = property(itemClass, propertyName).isAbstract();
		String detailMessage =
			" Result: The property '" + propertyName + "' is abstract, but is expected to be not abstract.";
		assertFalse(messageSituation + detailMessage, isAbstract);
	}

	private void assertConfigIsAbstract(String messageSituation, Class<? extends ConfigurationItem> itemClass) {

		ConfigurationDescriptor config = TypedConfiguration.getConfigurationDescriptor(itemClass);
		String detailMessage =
			" Result: The config interface is not annotated @Abstract, but is expected to be annotated @Abstract.";
		assertTrue(messageSituation + detailMessage, config.isAbstract());
	}

	private void assertConfigIsNotAbstract(String messageSituation, Class<? extends ConfigurationItem> itemClass) {

		ConfigurationDescriptor config = TypedConfiguration.getConfigurationDescriptor(itemClass);
		String detailMessage =
			" Result: The config interface is annotated @Abstract, but is expected to be not annotated @Abstract.";
		assertFalse(messageSituation + detailMessage, config.isAbstract());
	}

	private static Class<?> getConfigInterfaceOrNull(ConfigurationItem value) {
		if (value == null) {
			return null;
		}
		return value.getConfigurationInterface();
	}

	private static PropertyDescriptor property(Class<? extends ConfigurationItem> itemClass, String propertyName) {
		ConfigurationDescriptor descriptor = TypedConfiguration.getConfigurationDescriptor(itemClass);
		PropertyDescriptor property = descriptor.getProperty(propertyName);
		return property;
	}

	@Override
	protected Map<String, ConfigurationDescriptor> getDescriptors() {
		return Collections.emptyMap();
	}

	public static Test suite() {
		return BasicTestSetup
			.createBasicTestSetup(new CustomPropertiesSetup(TestAnnotationInteraction.class, true));
	}

}
