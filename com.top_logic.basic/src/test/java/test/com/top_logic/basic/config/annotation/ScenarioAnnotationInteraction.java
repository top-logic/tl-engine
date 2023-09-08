/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.config.annotation;

import test.com.top_logic.basic.config.annotation.ScenarioAnnotationInteraction.ScenarioContainerBaseTypes.ScenarioTypeBase;
import test.com.top_logic.basic.config.annotation.ScenarioAnnotationInteraction.ScenarioContainerBaseTypes.ScenarioTypeBaseADefault;
import test.com.top_logic.basic.config.annotation.ScenarioAnnotationInteraction.ScenarioContainerBaseTypes.ScenarioTypeBaseADefaultSecond;
import test.com.top_logic.basic.config.annotation.ScenarioAnnotationInteraction.ScenarioContainerBaseTypes.ScenarioTypeBaseADefaultThird;
import test.com.top_logic.basic.config.annotation.ScenarioAnnotationInteraction.ScenarioContainerBaseTypes.ScenarioTypeBaseAbstract;
import test.com.top_logic.basic.config.annotation.ScenarioAnnotationInteraction.ScenarioContainerBaseTypes.ScenarioTypeBaseCDefault;
import test.com.top_logic.basic.config.annotation.ScenarioAnnotationInteraction.ScenarioContainerBaseTypes.ScenarioTypeBaseCDefaultSecond;
import test.com.top_logic.basic.config.annotation.ScenarioAnnotationInteraction.ScenarioContainerBaseTypes.ScenarioTypeBaseCDefaultThird;
import test.com.top_logic.basic.config.annotation.ScenarioAnnotationInteraction.ScenarioContainerBaseTypes.ScenarioTypeBaseContainer;
import test.com.top_logic.basic.config.annotation.ScenarioAnnotationInteraction.ScenarioContainerBaseTypes.ScenarioTypeBaseContainerSecond;
import test.com.top_logic.basic.config.annotation.ScenarioAnnotationInteraction.ScenarioContainerBaseTypes.ScenarioTypeBaseContainerThird;
import test.com.top_logic.basic.config.annotation.ScenarioAnnotationInteraction.ScenarioContainerBaseTypes.ScenarioTypeBaseDerived;
import test.com.top_logic.basic.config.annotation.ScenarioAnnotationInteraction.ScenarioContainerBaseTypes.ScenarioTypeBaseDerivedSecond;
import test.com.top_logic.basic.config.annotation.ScenarioAnnotationInteraction.ScenarioContainerBaseTypes.ScenarioTypeBaseDerivedThird;
import test.com.top_logic.basic.config.annotation.ScenarioAnnotationInteraction.ScenarioContainerBaseTypes.ScenarioTypeBaseMandatory;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.Container;
import com.top_logic.basic.config.annotation.DerivedRef;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Subtypes;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.basic.config.annotation.defaults.ItemDefault;
import com.top_logic.basic.config.container.ConfigPart;
import com.top_logic.basic.config.internal.gen.NoImplementationClassGeneration;

/**
 * Container interface for test scenario {@link ConfigurationItem} interfaces where default value,
 * mandatory and derived interact.
 * <p>
 * "ADefault" means "AnnotatedDefault". "CDefault" means "ConfiguredDefault".
 * </p>
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
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
@SuppressWarnings("javadoc")
public interface ScenarioAnnotationInteraction {

	/**
	 * Configured and annotated default value have to be distinct for the tests to detect which of
	 * the two is used.
	 */
	public static final int CONFIGURED_DEFAULT_VALUE = 111;

	/**
	 * Configured and annotated default value have to be distinct for the tests to detect which of
	 * the two is used.
	 */
	public static final int ANNOTATED_DEFAULT_VALUE = -123;

	/* No CONFIGURED_DEFAULT_ITEM_CLASS, as only primitive value properties can have a configured
	 * default */

	/**
	 * Configured and annotated default value have to be distinct for the tests to detect which of
	 * the two is used.
	 */
	public static final Class<? extends ConfigurationItem> ANNOTATED_DEFAULT_ITEM_CLASS =
		ScenarioContainerBaseTypes.ScenarioTypeItemA.class;

	public interface ScenarioContainerBaseTypes {

		/**
		 * Used as {@link ItemDefault} default value for {@link ScenarioTypeBase#getItemExample()}.
		 */
		public interface ScenarioTypeItemA extends ConfigurationItem {
			// Nothing needed
		}

		/**
		 * Used as {@link ItemDefault} default value for {@link ScenarioTypeBase#getItemExample()}.
		 */
		public interface ScenarioTypeItemB extends ConfigurationItem {
			// Nothing needed
		}

		/**
		 * Used as {@link ItemDefault} default value for {@link ScenarioTypeBase#getItemExample()}.
		 */
		public interface ScenarioTypeItemC extends ConfigurationItem {
			// Nothing needed
		}

		/**
		 * Used as non-default value for {@link ScenarioTypeBase#getItemExample()}.
		 */
		public interface ScenarioTypeItemD extends ConfigurationItem {
			// Nothing needed
		}

		public interface ScenarioTypeBase extends ConfigPart {

			String PROPERTY_NAME_EXAMPLE = "example";

			String PROPERTY_NAME_ITEM_EXAMPLE = "item-example";

			String PROPERTY_NAME_SOURCE = "source";

			/**
			 * This property is used by everything except {@link Container} property tests.
			 */
			@Name(PROPERTY_NAME_EXAMPLE)
			int getExample();

			void setExample(int value);

			/**
			 * This property is used by everything except "configured default" tests.
			 */
			@Name(PROPERTY_NAME_ITEM_EXAMPLE)
			@Subtypes({})
			ConfigurationItem getItemExample();

			void setItemExample(ConfigurationItem value);

			/**
			 * For easier testing of the derived properties, their source is already declared here
			 * and not in the various interfaces declaring derived properties. This allows to
			 * abstract over them without having to use the reflection API.
			 */
			@Name(PROPERTY_NAME_SOURCE)
			ScenarioTypeBase getSource();

			void setSource(ScenarioTypeBase value);

		}

		public interface ScenarioTypeBaseCDefault extends ScenarioTypeBase {

			/** Configured Default: {@link #CONFIGURED_DEFAULT_VALUE} */
			@Override
			int getExample();

			// No getItemExample, as that cannot have a configured default

		}

		public interface ScenarioTypeBaseCDefaultSecond extends ScenarioTypeBase {

			/** Configured Default: something =! {@link #CONFIGURED_DEFAULT_VALUE} */
			@Override
			int getExample();

			// No getItemExample, as that cannot have a configured default

		}

		public interface ScenarioTypeBaseCDefaultThird extends ScenarioTypeBase {

			/** Configured Default: something =! {@link #CONFIGURED_DEFAULT_VALUE} */
			@Override
			int getExample();

			// No getItemExample, as that cannot have a configured default

		}

		public interface ScenarioTypeBaseADefault extends ScenarioTypeBase {

			@IntDefault(ANNOTATED_DEFAULT_VALUE)
			@Override
			int getExample();

			@ItemDefault(ScenarioTypeItemA.class)
			@Override
			ConfigurationItem getItemExample();

		}

		public interface ScenarioTypeBaseADefaultSecond extends ScenarioTypeBase {

			@IntDefault(-2)
			@Override
			int getExample();

			@ItemDefault(ScenarioTypeItemB.class)
			@Override
			ConfigurationItem getItemExample();

		}

		public interface ScenarioTypeBaseADefaultThird extends ScenarioTypeBase {

			@IntDefault(-3)
			@Override
			int getExample();

			@ItemDefault(ScenarioTypeItemC.class)
			@Override
			ConfigurationItem getItemExample();

		}

		public interface ScenarioTypeBaseMandatory extends ScenarioTypeBase {

			@Mandatory
			@Override
			int getExample();

			@Mandatory
			@Override
			ConfigurationItem getItemExample();

		}

		public interface ScenarioTypeBaseContainer extends ScenarioTypeBase {

			@Container
			@Override
			ConfigurationItem getItemExample();

		}

		public interface ScenarioTypeBaseContainerSecond extends ScenarioTypeBase {

			@Container
			@Override
			ConfigurationItem getItemExample();

		}

		public interface ScenarioTypeBaseContainerThird extends ScenarioTypeBase {

			@Container
			@Override
			ConfigurationItem getItemExample();

		}

		public interface ScenarioTypeBaseDerived extends ScenarioTypeBase {

			@DerivedRef({ PROPERTY_NAME_SOURCE, ScenarioTypeBase.PROPERTY_NAME_EXAMPLE })
			@Override
			int getExample();

			@DerivedRef({ PROPERTY_NAME_SOURCE, ScenarioTypeBase.PROPERTY_NAME_ITEM_EXAMPLE })
			@Override
			ConfigurationItem getItemExample();
		}

		public interface ScenarioTypeBaseDerivedSecond extends ScenarioTypeBase {

			String PROPERTY_NAME_SECOND = "second";

			String PROPERTY_NAME_ITEM_SECOND = "item-second";

			@DerivedRef(PROPERTY_NAME_SECOND)
			@Override
			int getExample();

			@DerivedRef(PROPERTY_NAME_ITEM_SECOND)
			@Override
			ConfigurationItem getItemExample();

			@Name(PROPERTY_NAME_SECOND)
			int getSecond();

			@Name(PROPERTY_NAME_ITEM_SECOND)
			@Subtypes({})
			ConfigurationItem getItemSecond();
		}

		public interface ScenarioTypeBaseDerivedThird extends ScenarioTypeBase {

			String PROPERTY_NAME_THIRD = "third";

			String PROPERTY_NAME_ITEM_THIRD = "item-third";

			@DerivedRef(PROPERTY_NAME_THIRD)
			@Override
			int getExample();

			@DerivedRef(PROPERTY_NAME_ITEM_THIRD)
			@Override
			ConfigurationItem getItemExample();

			@Name(PROPERTY_NAME_THIRD)
			int getThird();

			@Name(PROPERTY_NAME_ITEM_THIRD)
			@Subtypes({})
			ConfigurationItem getItemThird();

		}

		@Abstract
		public interface ScenarioTypeBaseAbstract extends ScenarioTypeBase {

			@Abstract
			@Override
			int getExample();

			@Abstract
			@Override
			ConfigurationItem getItemExample();

		}

	}

	public interface ScenarioContainerLocalXY {

		public interface ScenarioTypeLocalCDefaultADefault extends ScenarioTypeBase {

			@IntDefault(ANNOTATED_DEFAULT_VALUE)
			@Override
			int getExample();

		}

		// ScenarioTypeLocalCDefaultMandatory

		// ScenarioTypeLocalCDefaultContainer

		// ScenarioTypeLocalCDefaultDerived

		// ScenarioTypeLocalCDefaultAbstract

		@NoImplementationClassGeneration
		public interface ScenarioTypeLocalADefaultMandatory extends ScenarioTypeBase {

			@IntDefault(ANNOTATED_DEFAULT_VALUE)
			@Mandatory
			@Override
			int getExample();

			@ItemDefault(ScenarioContainerBaseTypes.ScenarioTypeItemA.class)
			@Mandatory
			@Override
			ConfigurationItem getItemExample();

		}

		@NoImplementationClassGeneration
		public interface ScenarioTypeLocalADefaultContainer extends ScenarioTypeBase {

			@ItemDefault(ScenarioContainerBaseTypes.ScenarioTypeItemA.class)
			@Container
			@Override
			ConfigurationItem getItemExample();

		}

		public interface ScenarioTypeLocalADefaultDerived extends ScenarioTypeBase {

			@IntDefault(ANNOTATED_DEFAULT_VALUE)
			@DerivedRef({ PROPERTY_NAME_SOURCE, ScenarioTypeBase.PROPERTY_NAME_EXAMPLE })
			@Override
			int getExample();

			@ItemDefault(ScenarioContainerBaseTypes.ScenarioTypeItemA.class)
			@DerivedRef({ PROPERTY_NAME_SOURCE, ScenarioTypeBase.PROPERTY_NAME_ITEM_EXAMPLE })
			@Override
			ConfigurationItem getItemExample();

		}

		@Abstract
		@NoImplementationClassGeneration
		public interface ScenarioTypeLocalADefaultAbstract extends ScenarioTypeBase {

			@IntDefault(ANNOTATED_DEFAULT_VALUE)
			@Abstract
			@Override
			int getExample();

			@ItemDefault(ScenarioContainerBaseTypes.ScenarioTypeItemA.class)
			@Abstract
			@Override
			ConfigurationItem getItemExample();

		}

		@NoImplementationClassGeneration
		public interface ScenarioTypeLocalMandatoryContainer extends ScenarioTypeBase {

			@Mandatory
			@Container
			@Override
			ConfigurationItem getItemExample();

		}

		@NoImplementationClassGeneration
		public interface ScenarioTypeLocalMandatoryDerived extends ScenarioTypeBase {

			@Mandatory
			@DerivedRef({ PROPERTY_NAME_SOURCE, ScenarioTypeBase.PROPERTY_NAME_EXAMPLE })
			@Override
			int getExample();

			@Mandatory
			@DerivedRef({ PROPERTY_NAME_SOURCE, ScenarioTypeBase.PROPERTY_NAME_ITEM_EXAMPLE })
			@Override
			ConfigurationItem getItemExample();

		}

		@NoImplementationClassGeneration
		@Abstract
		public interface ScenarioTypeLocalMandatoryAbstract extends ScenarioTypeBase {

			@Mandatory
			@Abstract
			@Override
			int getExample();

			@Mandatory
			@Abstract
			@Override
			ConfigurationItem getItemExample();

		}

		@NoImplementationClassGeneration
		public interface ScenarioTypeLocalContainerDerived extends ScenarioTypeBase {

			@Container
			@DerivedRef({ PROPERTY_NAME_SOURCE, ScenarioTypeBase.PROPERTY_NAME_ITEM_EXAMPLE })
			@Override
			int getExample();

		}

		@NoImplementationClassGeneration
		@Abstract
		public interface ScenarioTypeLocalContainerAbstract extends ScenarioTypeBase {

			@Container
			@Abstract
			@Override
			int getExample();

		}

		@NoImplementationClassGeneration
		@Abstract
		public interface ScenarioTypeLocalDerivedAbstract extends ScenarioTypeBase {

			@DerivedRef({ PROPERTY_NAME_SOURCE, ScenarioTypeBase.PROPERTY_NAME_ITEM_EXAMPLE })
			@Abstract
			@Override
			int getExample();

		}

	}

	public interface ScenarioContainerLocalXInheritedY {

		public interface ScenarioTypeLocalCDefaultInheritedADefault extends ScenarioTypeBaseADefault {
			// Nothing needed
		}

		// ScenarioTypeLocalCDefaultInheritedMandatory

		// ScenarioTypeLocalCDefaultInheritedContainer

		// A derived property cannot have a configured default.
		// ScenarioTypeLocalCDefaultInheritedDerived

		// An abstract property cannot have a configured default.
		// ScenarioTypeLocalCDefaultInheritedAbstract

		public interface ScenarioTypeLocalADefaultInheritedCDefault extends ScenarioTypeBaseCDefault {

			@IntDefault(ANNOTATED_DEFAULT_VALUE)
			@Override
			int getExample();

		}

		public interface ScenarioTypeLocalADefaultInheritedMandatory extends ScenarioTypeBaseMandatory {

			@IntDefault(ANNOTATED_DEFAULT_VALUE)
			@Override
			int getExample();

			@ItemDefault(ScenarioContainerBaseTypes.ScenarioTypeItemA.class)
			@Override
			ConfigurationItem getItemExample();

		}

		public interface ScenarioTypeLocalADefaultInheritedContainer extends ScenarioTypeBaseContainer {

			@ItemDefault(ScenarioContainerBaseTypes.ScenarioTypeItemA.class)
			@Override
			ConfigurationItem getItemExample();

		}

		public interface ScenarioTypeLocalADefaultInheritedDerived extends ScenarioTypeBaseDerived {

			@IntDefault(ANNOTATED_DEFAULT_VALUE)
			@Override
			int getExample();

			@ItemDefault(ScenarioContainerBaseTypes.ScenarioTypeItemA.class)
			@Override
			ConfigurationItem getItemExample();

		}

		public interface ScenarioTypeLocalADefaultInheritedAbstract extends ScenarioTypeBaseAbstract {

			@IntDefault(ANNOTATED_DEFAULT_VALUE)
			@Override
			int getExample();

			@ItemDefault(ScenarioContainerBaseTypes.ScenarioTypeItemA.class)
			@Override
			ConfigurationItem getItemExample();

		}

		public interface ScenarioTypeLocalMandatoryInheritedCDefault extends ScenarioTypeBaseCDefault {

			@Mandatory
			@Override
			int getExample();

		}

		public interface ScenarioTypeLocalMandatoryInheritedADefault extends ScenarioTypeBaseADefault {

			@Mandatory
			@Override
			int getExample();

			@Mandatory
			@Override
			ConfigurationItem getItemExample();

		}

		public interface ScenarioTypeLocalMandatoryInheritedContainer extends ScenarioTypeBaseContainer {

			@Mandatory
			@Override
			ConfigurationItem getItemExample();

		}

		public interface ScenarioTypeLocalMandatoryInheritedDerived extends ScenarioTypeBaseDerived {

			@Mandatory
			@Override
			int getExample();

			@Mandatory
			@Override
			ConfigurationItem getItemExample();

		}

		public interface ScenarioTypeLocalMandatoryInheritedAbstract extends ScenarioTypeBaseAbstract {

			@Mandatory
			@Override
			int getExample();

			@Mandatory
			@Override
			ConfigurationItem getItemExample();

		}

		// ScenarioTypeLocalContainerInheritedCDefault

		public interface ScenarioTypeLocalContainerInheritedADefault extends ScenarioTypeBaseADefault {

			@Container
			@Override
			ConfigurationItem getItemExample();

		}

		public interface ScenarioTypeLocalContainerInheritedMandatory extends ScenarioTypeBaseMandatory {

			@Container
			@Override
			ConfigurationItem getItemExample();

		}

		public interface ScenarioTypeLocalContainerInheritedContainer extends ScenarioTypeBaseContainer {

			@Container
			@Override
			ConfigurationItem getItemExample();

		}

		public interface ScenarioTypeLocalContainerInheritedDerived extends ScenarioTypeBaseDerived {

			@Container
			@Override
			ConfigurationItem getItemExample();

		}

		public interface ScenarioTypeLocalContainerInheritedAbstract extends ScenarioTypeBaseAbstract {

			// Redeclare to remove @Abstract annotation
			@Override
			int getExample();

			@Container
			@Override
			ConfigurationItem getItemExample();

		}

		public interface ScenarioTypeLocalDerivedInheritedCDefault extends ScenarioTypeBaseCDefault {

			@DerivedRef({ PROPERTY_NAME_SOURCE, ScenarioTypeBase.PROPERTY_NAME_EXAMPLE })
			@Override
			int getExample();

		}

		public interface ScenarioTypeLocalDerivedInheritedADefault extends ScenarioTypeBaseADefault {

			@DerivedRef({ PROPERTY_NAME_SOURCE, ScenarioTypeBase.PROPERTY_NAME_EXAMPLE })
			@Override
			int getExample();

			@DerivedRef({ PROPERTY_NAME_SOURCE, ScenarioTypeBase.PROPERTY_NAME_ITEM_EXAMPLE })
			@Override
			ConfigurationItem getItemExample();

		}

		public interface ScenarioTypeLocalDerivedInheritedMandatory extends ScenarioTypeBaseMandatory {

			@DerivedRef({ PROPERTY_NAME_SOURCE, ScenarioTypeBase.PROPERTY_NAME_EXAMPLE })
			@Override
			int getExample();

			@DerivedRef({ PROPERTY_NAME_SOURCE, ScenarioTypeBase.PROPERTY_NAME_ITEM_EXAMPLE })
			@Override
			ConfigurationItem getItemExample();

		}

		public interface ScenarioTypeLocalDerivedInheritedContainer extends ScenarioTypeBaseContainer {

			@DerivedRef({ PROPERTY_NAME_SOURCE, ScenarioTypeBase.PROPERTY_NAME_ITEM_EXAMPLE })
			@Override
			ConfigurationItem getItemExample();

		}

		public interface ScenarioTypeLocalDerivedInheritedAbstract extends ScenarioTypeBaseAbstract {

			@DerivedRef({ PROPERTY_NAME_SOURCE, ScenarioTypeBase.PROPERTY_NAME_EXAMPLE })
			@Override
			int getExample();

			@DerivedRef({ PROPERTY_NAME_SOURCE, ScenarioTypeBase.PROPERTY_NAME_ITEM_EXAMPLE })
			@Override
			ConfigurationItem getItemExample();

		}

		@Abstract
		public interface ScenarioTypeLocalAbstractInheritedCDefault extends ScenarioTypeBaseCDefault {

			@Abstract
			@Override
			int getExample();

		}

		@Abstract
		public interface ScenarioTypeLocalAbstractInheritedADefault extends ScenarioTypeBaseADefault {

			@Abstract
			@Override
			int getExample();

			@Abstract
			@Override
			ConfigurationItem getItemExample();

		}

		@Abstract
		public interface ScenarioTypeLocalAbstractInheritedMandatory extends ScenarioTypeBaseMandatory {

			@Abstract
			@Override
			int getExample();

			@Abstract
			@Override
			ConfigurationItem getItemExample();

		}

		@Abstract
		public interface ScenarioTypeLocalAbstractInheritedContainer extends ScenarioTypeBaseContainer {

			@Abstract
			@Override
			ConfigurationItem getItemExample();

		}

		@Abstract
		public interface ScenarioTypeLocalAbstractInheritedDerived extends ScenarioTypeBaseDerived {

			@Abstract
			@Override
			int getExample();

			@Abstract
			@Override
			ConfigurationItem getItemExample();

		}

	}

	public interface ScenarioContainerInheritedXY {

		public interface ScenarioTypeInheritedCDefaultCDefault
				extends ScenarioTypeBaseCDefault, ScenarioTypeBaseCDefaultSecond {
			// Nothing needed
		}

		public interface ScenarioTypeInheritedCDefaultADefault
				extends ScenarioTypeBaseCDefault, ScenarioTypeBaseADefault {
			// Nothing needed
		}

		public interface ScenarioTypeInheritedCDefaultMandatory
				extends ScenarioTypeBaseCDefault, ScenarioTypeBaseMandatory {
			// Nothing needed
		}

		// ScenarioTypeInheritedCDefaultContainer

		public interface ScenarioTypeInheritedCDefaultDerived
				extends ScenarioTypeBaseCDefault, ScenarioTypeBaseDerived {
			// Nothing needed
		}

		public interface ScenarioTypeInheritedCDefaultAbstract
				extends ScenarioTypeBaseCDefault, ScenarioTypeBaseAbstract {
			// Nothing needed
		}

		public interface ScenarioTypeInheritedADefaultCDefault
				extends ScenarioTypeBaseADefault, ScenarioTypeBaseCDefault {
			// Nothing needed
		}

		public interface ScenarioTypeInheritedADefaultADefault
				extends ScenarioTypeBaseADefault, ScenarioTypeBaseADefaultSecond {
			// Nothing needed
		}

		public interface ScenarioTypeInheritedADefaultMandatory
				extends ScenarioTypeBaseADefault, ScenarioTypeBaseMandatory {
			// Nothing needed
		}

		public interface ScenarioTypeInheritedADefaultContainer
				extends ScenarioTypeBaseADefault, ScenarioTypeBaseContainer {
			// Nothing needed
		}

		public interface ScenarioTypeInheritedADefaultDerived
				extends ScenarioTypeBaseADefault, ScenarioTypeBaseDerived {
			// Nothing needed
		}

		public interface ScenarioTypeInheritedADefaultAbstract
				extends ScenarioTypeBaseADefault, ScenarioTypeBaseAbstract {
			// Nothing needed
		}

		public interface ScenarioTypeInheritedMandatoryContainer
				extends ScenarioTypeBaseMandatory, ScenarioTypeBaseContainer {
			// Nothing needed
		}

		public interface ScenarioTypeInheritedMandatoryDerived
				extends ScenarioTypeBaseMandatory, ScenarioTypeBaseDerived {
			// Nothing needed
		}

		public interface ScenarioTypeInheritedMandatoryAbstract
				extends ScenarioTypeBaseMandatory, ScenarioTypeBaseAbstract {
			// Nothing needed
		}

		public interface ScenarioTypeInheritedContainerContainer
				extends ScenarioTypeBaseContainer, ScenarioTypeBaseContainerSecond {
			// Nothing needed
		}

		public interface ScenarioTypeInheritedContainerDerived
				extends ScenarioTypeBaseContainer, ScenarioTypeBaseDerived {
			// Nothing needed
		}

		public interface ScenarioTypeInheritedContainerAbstract
				extends ScenarioTypeBaseContainer, ScenarioTypeBaseAbstract {
			// Nothing needed
		}

		public interface ScenarioTypeInheritedDerivedContainer
				extends ScenarioTypeBaseDerived, ScenarioTypeBaseContainer {
			// Nothing needed
		}

		public interface ScenarioTypeInheritedDerivedDerived
				extends ScenarioTypeBaseDerived, ScenarioTypeBaseDerivedSecond {
			// Nothing needed
		}

		public interface ScenarioTypeInheritedDerivedAbstract
				extends ScenarioTypeBaseDerived, ScenarioTypeBaseAbstract {
			// Nothing needed
		}

	}

	public interface ScenarioContainerLocalCDefaultInheritedXY {

		public interface ScenarioTypeLocalCDefaultInheritedCDefaultCDefault
				extends ScenarioTypeBaseCDefaultSecond, ScenarioTypeBaseCDefaultThird {
			// Nothing needed
		}

		public interface ScenarioTypeLocalCDefaultInheritedCDefaultADefault
				extends ScenarioTypeBaseCDefaultSecond, ScenarioTypeBaseADefault {
			// Nothing needed
		}

		// ScenarioTypeLocalCDefaultInheritedCDefaultMandatory

		// ScenarioTypeLocalCDefaultInheritedCDefaultContainer

		/* Cannot be tested, as the inherited configured default is not visible until all defaults
		 * have been read. But to read all defaults, the default of this class has to be read, but
		 * it is still derived, as the inherited configured default is just being read. */
		// ScenarioTypeLocalCDefaultInheritedCDefaultDerived

		public interface ScenarioTypeLocalCDefaultInheritedCDefaultAbstract
				extends ScenarioTypeBaseCDefaultSecond, ScenarioTypeBaseAbstract {
			// Nothing needed
		}

		public interface ScenarioTypeLocalCDefaultInheritedADefaultCDefault
				extends ScenarioTypeBaseADefault, ScenarioTypeBaseCDefault {
			// Nothing needed
		}

		public interface ScenarioTypeLocalCDefaultInheritedADefaultADefault
				extends ScenarioTypeBaseADefault, ScenarioTypeBaseADefaultSecond {
			// Nothing needed
		}

		public interface ScenarioTypeLocalCDefaultInheritedADefaultMandatory
				extends ScenarioTypeBaseADefault, ScenarioTypeBaseMandatory {
			// Nothing needed
		}

		// ScenarioTypeLocalCDefaultInheritedADefaultContainer

		// A derived property cannot have a configured default.
		// ScenarioTypeLocalCDefaultInheritedADefaultDerived

		public interface ScenarioTypeLocalCDefaultInheritedADefaultAbstract
				extends ScenarioTypeBaseADefault, ScenarioTypeBaseAbstract {
			// Nothing needed
		}

		// ScenarioTypeLocalCDefaultInheritedMandatoryContainer
		// ScenarioTypeLocalCDefaultInheritedMandatoryDerived
		// ScenarioTypeLocalCDefaultInheritedMandatoryAbstract
		// ScenarioTypeLocalCDefaultInheritedContainerContainer
		// ScenarioTypeLocalCDefaultInheritedContainerDerived
		// ScenarioTypeLocalCDefaultInheritedContainerAbstract
		// ScenarioTypeLocalCDefaultInheritedDerivedContainer

		// A derived property cannot have a configured default.
		// ScenarioTypeLocalCDefaultInheritedDerivedDerived

		// A derived property cannot have a configured default.
		// ScenarioTypeLocalCDefaultInheritedDerivedAbstract

	}

	public interface ScenarioContainerLocalADefaultInheritedXY {

		public interface ScenarioTypeLocalADefaultInheritedCDefaultCDefault
				extends ScenarioTypeBaseCDefault, ScenarioTypeBaseCDefaultSecond {

			@IntDefault(ANNOTATED_DEFAULT_VALUE)
			@Override
			int getExample();

		}

		public interface ScenarioTypeLocalADefaultInheritedCDefaultADefault
				extends ScenarioTypeBaseCDefault, ScenarioTypeBaseADefault {

			@IntDefault(ANNOTATED_DEFAULT_VALUE)
			@Override
			int getExample();

		}

		public interface ScenarioTypeLocalADefaultInheritedCDefaultMandatory
				extends ScenarioTypeBaseCDefault, ScenarioTypeBaseMandatory {

			@IntDefault(ANNOTATED_DEFAULT_VALUE)
			@Override
			int getExample();

		}

		// ScenarioTypeLocalADefaultInheritedCDefaultContainer

		public interface ScenarioTypeLocalADefaultInheritedCDefaultDerived
				extends ScenarioTypeBaseCDefault, ScenarioTypeBaseDerived {

			@IntDefault(ANNOTATED_DEFAULT_VALUE)
			@Override
			int getExample();

		}

		public interface ScenarioTypeLocalADefaultInheritedCDefaultAbstract
				extends ScenarioTypeBaseCDefault, ScenarioTypeBaseAbstract {

			@IntDefault(ANNOTATED_DEFAULT_VALUE)
			@Override
			int getExample();

		}

		public interface ScenarioTypeLocalADefaultInheritedADefaultCDefault
				extends ScenarioTypeBaseADefaultSecond, ScenarioTypeBaseCDefault {

			@IntDefault(ANNOTATED_DEFAULT_VALUE)
			@Override
			int getExample();

		}

		public interface ScenarioTypeLocalADefaultInheritedADefaultADefault
				extends ScenarioTypeBaseADefaultSecond, ScenarioTypeBaseADefaultThird {

			@IntDefault(ANNOTATED_DEFAULT_VALUE)
			@Override
			int getExample();

			@ItemDefault(ScenarioContainerBaseTypes.ScenarioTypeItemA.class)
			@Override
			ConfigurationItem getItemExample();

		}

		public interface ScenarioTypeLocalADefaultInheritedADefaultMandatory
				extends ScenarioTypeBaseADefaultSecond, ScenarioTypeBaseMandatory {

			@IntDefault(ANNOTATED_DEFAULT_VALUE)
			@Override
			int getExample();

			@ItemDefault(ScenarioContainerBaseTypes.ScenarioTypeItemA.class)
			@Override
			ConfigurationItem getItemExample();

		}

		public interface ScenarioTypeLocalADefaultInheritedADefaultContainer
				extends ScenarioTypeBaseADefaultSecond, ScenarioTypeBaseContainer {

			@ItemDefault(ScenarioContainerBaseTypes.ScenarioTypeItemA.class)
			@Override
			ConfigurationItem getItemExample();

		}

		public interface ScenarioTypeLocalADefaultInheritedADefaultDerived
				extends ScenarioTypeBaseADefaultSecond, ScenarioTypeBaseDerived {

			@IntDefault(ANNOTATED_DEFAULT_VALUE)
			@Override
			int getExample();

			@ItemDefault(ScenarioContainerBaseTypes.ScenarioTypeItemA.class)
			@Override
			ConfigurationItem getItemExample();

		}

		public interface ScenarioTypeLocalADefaultInheritedADefaultAbstract
				extends ScenarioTypeBaseADefaultSecond, ScenarioTypeBaseAbstract {

			@IntDefault(ANNOTATED_DEFAULT_VALUE)
			@Override
			int getExample();

			@ItemDefault(ScenarioContainerBaseTypes.ScenarioTypeItemA.class)
			@Override
			ConfigurationItem getItemExample();

		}

		public interface ScenarioTypeLocalADefaultInheritedMandatoryContainer
				extends ScenarioTypeBaseMandatory, ScenarioTypeBaseContainer {

			@ItemDefault(ScenarioContainerBaseTypes.ScenarioTypeItemA.class)
			@Override
			ConfigurationItem getItemExample();

		}

		public interface ScenarioTypeLocalADefaultInheritedMandatoryDerived
				extends ScenarioTypeBaseMandatory, ScenarioTypeBaseDerived {

			@IntDefault(ANNOTATED_DEFAULT_VALUE)
			@Override
			int getExample();

			@ItemDefault(ScenarioContainerBaseTypes.ScenarioTypeItemA.class)
			@Override
			ConfigurationItem getItemExample();

		}

		public interface ScenarioTypeLocalADefaultInheritedMandatoryAbstract
				extends ScenarioTypeBaseMandatory, ScenarioTypeBaseAbstract {

			@IntDefault(ANNOTATED_DEFAULT_VALUE)
			@Override
			int getExample();

			@ItemDefault(ScenarioContainerBaseTypes.ScenarioTypeItemA.class)
			@Override
			ConfigurationItem getItemExample();

		}

		public interface ScenarioTypeLocalADefaultInheritedContainerContainer
				extends ScenarioTypeBaseContainer, ScenarioTypeBaseContainerSecond {

			@ItemDefault(ScenarioContainerBaseTypes.ScenarioTypeItemA.class)
			@Override
			ConfigurationItem getItemExample();

		}

		public interface ScenarioTypeLocalADefaultInheritedContainerDerived
				extends ScenarioTypeBaseContainer, ScenarioTypeBaseDerived {

			@ItemDefault(ScenarioContainerBaseTypes.ScenarioTypeItemA.class)
			@Override
			ConfigurationItem getItemExample();

		}

		public interface ScenarioTypeLocalADefaultInheritedContainerAbstract
				extends ScenarioTypeBaseContainer, ScenarioTypeBaseAbstract {

			@ItemDefault(ScenarioContainerBaseTypes.ScenarioTypeItemA.class)
			@Override
			ConfigurationItem getItemExample();

		}

		public interface ScenarioTypeLocalADefaultInheritedDerivedContainer
				extends ScenarioTypeBaseDerived, ScenarioTypeBaseContainer {

			@ItemDefault(ScenarioContainerBaseTypes.ScenarioTypeItemA.class)
			@Override
			ConfigurationItem getItemExample();

		}

		public interface ScenarioTypeLocalADefaultInheritedDerivedDerived
				extends ScenarioTypeBaseDerived, ScenarioTypeBaseDerivedSecond {

			@IntDefault(ANNOTATED_DEFAULT_VALUE)
			@Override
			int getExample();

			@ItemDefault(ScenarioContainerBaseTypes.ScenarioTypeItemA.class)
			@Override
			ConfigurationItem getItemExample();

		}

		public interface ScenarioTypeLocalADefaultInheritedDerivedAbstract
				extends ScenarioTypeBaseDerived, ScenarioTypeBaseAbstract {

			@IntDefault(ANNOTATED_DEFAULT_VALUE)
			@Override
			int getExample();

			@ItemDefault(ScenarioContainerBaseTypes.ScenarioTypeItemA.class)
			@Override
			ConfigurationItem getItemExample();

		}

	}

	public interface ScenarioContainerLocalMandatoryInheritedXY {

		public interface ScenarioTypeLocalMandatoryInheritedCDefaultCDefault
				extends ScenarioTypeBaseCDefault, ScenarioTypeBaseCDefaultSecond {

			@Mandatory
			@Override
			int getExample();

		}

		public interface ScenarioTypeLocalMandatoryInheritedCDefaultADefault
				extends ScenarioTypeBaseCDefault, ScenarioTypeBaseADefault {

			@Mandatory
			@Override
			int getExample();

		}

		public interface ScenarioTypeLocalMandatoryInheritedCDefaultMandatory
				extends ScenarioTypeBaseCDefault, ScenarioTypeBaseMandatory {

			@Mandatory
			@Override
			int getExample();

		}

		// ScenarioTypeLocalMandatoryInheritedCDefaultContainer

		public interface ScenarioTypeLocalMandatoryInheritedCDefaultDerived
				extends ScenarioTypeBaseCDefault, ScenarioTypeBaseDerived {

			@Mandatory
			@Override
			int getExample();

		}

		public interface ScenarioTypeLocalMandatoryInheritedCDefaultAbstract
				extends ScenarioTypeBaseCDefault, ScenarioTypeBaseAbstract {

			@Mandatory
			@Override
			int getExample();

		}

		public interface ScenarioTypeLocalMandatoryInheritedADefaultCDefault
				extends ScenarioTypeBaseADefault, ScenarioTypeBaseCDefault {

			@Mandatory
			@Override
			int getExample();

		}

		public interface ScenarioTypeLocalMandatoryInheritedADefaultADefault
				extends ScenarioTypeBaseADefault, ScenarioTypeBaseADefaultSecond {

			@Mandatory
			@Override
			int getExample();

			@Mandatory
			@Override
			ConfigurationItem getItemExample();

		}

		public interface ScenarioTypeLocalMandatoryInheritedADefaultMandatory
				extends ScenarioTypeBaseADefault, ScenarioTypeBaseMandatory {

			@Mandatory
			@Override
			int getExample();

			@Mandatory
			@Override
			ConfigurationItem getItemExample();

		}

		public interface ScenarioTypeLocalMandatoryInheritedADefaultContainer
				extends ScenarioTypeBaseADefault, ScenarioTypeBaseContainer {

			@Mandatory
			@Override
			ConfigurationItem getItemExample();

		}

		public interface ScenarioTypeLocalMandatoryInheritedADefaultDerived
				extends ScenarioTypeBaseADefault, ScenarioTypeBaseDerived {

			@Mandatory
			@Override
			int getExample();

			@Mandatory
			@Override
			ConfigurationItem getItemExample();

		}

		public interface ScenarioTypeLocalMandatoryInheritedADefaultAbstract
				extends ScenarioTypeBaseADefault, ScenarioTypeBaseAbstract {

			@Mandatory
			@Override
			int getExample();

			@Mandatory
			@Override
			ConfigurationItem getItemExample();

		}

		public interface ScenarioTypeLocalMandatoryInheritedMandatoryContainer
				extends ScenarioTypeBaseMandatory, ScenarioTypeBaseContainer {

			@Mandatory
			@Override
			ConfigurationItem getItemExample();

		}

		public interface ScenarioTypeLocalMandatoryInheritedMandatoryDerived
				extends ScenarioTypeBaseMandatory, ScenarioTypeBaseDerived {

			@Mandatory
			@Override
			int getExample();

			@Mandatory
			@Override
			ConfigurationItem getItemExample();

		}

		public interface ScenarioTypeLocalMandatoryInheritedMandatoryAbstract
				extends ScenarioTypeBaseMandatory, ScenarioTypeBaseAbstract {

			@Mandatory
			@Override
			int getExample();

			@Mandatory
			@Override
			ConfigurationItem getItemExample();

		}

		public interface ScenarioTypeLocalMandatoryInheritedContainerContainer
				extends ScenarioTypeBaseContainer, ScenarioTypeBaseContainerSecond {

			@Mandatory
			@Override
			ConfigurationItem getItemExample();

		}

		public interface ScenarioTypeLocalMandatoryInheritedContainerDerived
				extends ScenarioTypeBaseContainer, ScenarioTypeBaseDerived {

			@Mandatory
			@Override
			ConfigurationItem getItemExample();

		}

		public interface ScenarioTypeLocalMandatoryInheritedContainerAbstract
				extends ScenarioTypeBaseContainer, ScenarioTypeBaseAbstract {

			@Mandatory
			@Override
			ConfigurationItem getItemExample();

		}

		public interface ScenarioTypeLocalMandatoryInheritedDerivedContainer
				extends ScenarioTypeBaseDerived, ScenarioTypeBaseContainer {

			@Mandatory
			@Override
			ConfigurationItem getItemExample();

		}

		public interface ScenarioTypeLocalMandatoryInheritedDerivedDerived
				extends ScenarioTypeBaseDerived, ScenarioTypeBaseDerivedSecond {

			@Mandatory
			@Override
			int getExample();

			@Mandatory
			@Override
			ConfigurationItem getItemExample();

		}

		public interface ScenarioTypeLocalMandatoryInheritedDerivedAbstract
				extends ScenarioTypeBaseDerived, ScenarioTypeBaseAbstract {

			@Mandatory
			@Override
			int getExample();

			@Mandatory
			@Override
			ConfigurationItem getItemExample();

		}

	}

	public interface ScenarioContainerLocalContainerInheritedXY {

		// ScenarioTypeLocalContainerInheritedCDefaultCDefault
		// ScenarioTypeLocalContainerInheritedCDefaultADefault
		// ScenarioTypeLocalContainerInheritedCDefaultMandatory
		// ScenarioTypeLocalContainerInheritedCDefaultContainer
		// ScenarioTypeLocalContainerInheritedCDefaultDerived
		// ScenarioTypeLocalContainerInheritedCDefaultAbstract
		// ScenarioTypeLocalContainerInheritedADefaultCDefault

		public interface ScenarioTypeLocalContainerInheritedADefaultADefault
				extends ScenarioTypeBaseADefault, ScenarioTypeBaseADefaultSecond {

			@Container
			@Override
			ConfigurationItem getItemExample();

		}

		public interface ScenarioTypeLocalContainerInheritedADefaultMandatory
				extends ScenarioTypeBaseADefault, ScenarioTypeBaseMandatory {

			@Container
			@Override
			ConfigurationItem getItemExample();

		}

		public interface ScenarioTypeLocalContainerInheritedADefaultContainer
				extends ScenarioTypeBaseADefault, ScenarioTypeBaseContainer {

			@Container
			@Override
			ConfigurationItem getItemExample();

		}

		public interface ScenarioTypeLocalContainerInheritedADefaultDerived
				extends ScenarioTypeBaseADefault, ScenarioTypeBaseDerived {

			@Container
			@Override
			ConfigurationItem getItemExample();

		}

		public interface ScenarioTypeLocalContainerInheritedADefaultAbstract
				extends ScenarioTypeBaseADefault, ScenarioTypeBaseAbstract {

			@Container
			@Override
			ConfigurationItem getItemExample();

		}

		public interface ScenarioTypeLocalContainerInheritedMandatoryContainer
				extends ScenarioTypeBaseMandatory, ScenarioTypeBaseContainer {

			@Container
			@Override
			ConfigurationItem getItemExample();

		}

		public interface ScenarioTypeLocalContainerInheritedMandatoryDerived
				extends ScenarioTypeBaseMandatory, ScenarioTypeBaseDerived {

			@Container
			@Override
			ConfigurationItem getItemExample();

		}

		public interface ScenarioTypeLocalContainerInheritedMandatoryAbstract
				extends ScenarioTypeBaseMandatory, ScenarioTypeBaseAbstract {

			@Container
			@Override
			ConfigurationItem getItemExample();

		}

		public interface ScenarioTypeLocalContainerInheritedContainerContainer
				extends ScenarioTypeBaseContainerSecond, ScenarioTypeBaseContainerThird {

			@Container
			@Override
			ConfigurationItem getItemExample();

		}

		public interface ScenarioTypeLocalContainerInheritedContainerDerived
				extends ScenarioTypeBaseContainerSecond, ScenarioTypeBaseDerived {

			@Container
			@Override
			ConfigurationItem getItemExample();

		}

		public interface ScenarioTypeLocalContainerInheritedContainerAbstract
				extends ScenarioTypeBaseContainerSecond, ScenarioTypeBaseAbstract {

			@Container
			@Override
			ConfigurationItem getItemExample();

		}

		public interface ScenarioTypeLocalContainerInheritedDerivedContainer
				extends ScenarioTypeBaseDerived, ScenarioTypeBaseContainerSecond {

			@Container
			@Override
			ConfigurationItem getItemExample();

		}

		public interface ScenarioTypeLocalContainerInheritedDerivedDerived
				extends ScenarioTypeBaseDerived, ScenarioTypeBaseDerivedSecond {

			@Container
			@Override
			ConfigurationItem getItemExample();

		}

		public interface ScenarioTypeLocalContainerInheritedDerivedAbstract
				extends ScenarioTypeBaseDerived, ScenarioTypeBaseAbstract {

			@Container
			@Override
			ConfigurationItem getItemExample();

		}

	}

	public interface ScenarioContainerLocalDerivedInheritedXY {

		public interface ScenarioTypeLocalDerivedInheritedCDefaultCDefault
				extends ScenarioTypeBaseCDefault, ScenarioTypeBaseCDefaultSecond {

			@DerivedRef({ PROPERTY_NAME_SOURCE, ScenarioTypeBase.PROPERTY_NAME_EXAMPLE })
			@Override
			int getExample();

		}

		public interface ScenarioTypeLocalDerivedInheritedCDefaultADefault
				extends ScenarioTypeBaseCDefault, ScenarioTypeBaseADefault {

			@DerivedRef({ PROPERTY_NAME_SOURCE, ScenarioTypeBase.PROPERTY_NAME_EXAMPLE })
			@Override
			int getExample();

		}

		public interface ScenarioTypeLocalDerivedInheritedCDefaultMandatory
				extends ScenarioTypeBaseCDefault, ScenarioTypeBaseMandatory {

			@DerivedRef({ PROPERTY_NAME_SOURCE, ScenarioTypeBase.PROPERTY_NAME_EXAMPLE })
			@Override
			int getExample();

		}

		// ScenarioTypeLocalDerivedInheritedCDefaultContainer

		public interface ScenarioTypeLocalDerivedInheritedCDefaultDerived
				extends ScenarioTypeBaseCDefault, ScenarioTypeBaseDerivedSecond {

			@DerivedRef({ PROPERTY_NAME_SOURCE, ScenarioTypeBase.PROPERTY_NAME_EXAMPLE })
			@Override
			int getExample();

		}

		public interface ScenarioTypeLocalDerivedInheritedCDefaultAbstract
				extends ScenarioTypeBaseCDefault, ScenarioTypeBaseAbstract {

			@DerivedRef({ PROPERTY_NAME_SOURCE, ScenarioTypeBase.PROPERTY_NAME_EXAMPLE })
			@Override
			int getExample();

		}

		public interface ScenarioTypeLocalDerivedInheritedADefaultCDefault
				extends ScenarioTypeBaseADefault, ScenarioTypeBaseCDefault {

			@DerivedRef({ PROPERTY_NAME_SOURCE, ScenarioTypeBase.PROPERTY_NAME_EXAMPLE })
			@Override
			int getExample();

		}

		public interface ScenarioTypeLocalDerivedInheritedADefaultADefault
				extends ScenarioTypeBaseADefault, ScenarioTypeBaseADefaultSecond {

			@DerivedRef({ PROPERTY_NAME_SOURCE, ScenarioTypeBase.PROPERTY_NAME_EXAMPLE })
			@Override
			int getExample();

			@DerivedRef({ PROPERTY_NAME_SOURCE, ScenarioTypeBase.PROPERTY_NAME_ITEM_EXAMPLE })
			@Override
			ConfigurationItem getItemExample();

		}

		public interface ScenarioTypeLocalDerivedInheritedADefaultMandatory
				extends ScenarioTypeBaseADefault, ScenarioTypeBaseMandatory {

			@DerivedRef({ PROPERTY_NAME_SOURCE, ScenarioTypeBase.PROPERTY_NAME_EXAMPLE })
			@Override
			int getExample();

			@DerivedRef({ PROPERTY_NAME_SOURCE, ScenarioTypeBase.PROPERTY_NAME_ITEM_EXAMPLE })
			@Override
			ConfigurationItem getItemExample();

		}

		public interface ScenarioTypeLocalDerivedInheritedADefaultContainer
				extends ScenarioTypeBaseADefault, ScenarioTypeBaseContainer {

			@DerivedRef({ PROPERTY_NAME_SOURCE, ScenarioTypeBase.PROPERTY_NAME_ITEM_EXAMPLE })
			@Override
			ConfigurationItem getItemExample();

		}

		public interface ScenarioTypeLocalDerivedInheritedADefaultDerived
				extends ScenarioTypeBaseADefault, ScenarioTypeBaseDerivedSecond {

			@DerivedRef({ PROPERTY_NAME_SOURCE, ScenarioTypeBase.PROPERTY_NAME_EXAMPLE })
			@Override
			int getExample();

			@DerivedRef({ PROPERTY_NAME_SOURCE, ScenarioTypeBase.PROPERTY_NAME_ITEM_EXAMPLE })
			@Override
			ConfigurationItem getItemExample();

		}

		public interface ScenarioTypeLocalDerivedInheritedADefaultAbstract
				extends ScenarioTypeBaseADefault, ScenarioTypeBaseAbstract {

			@DerivedRef({ PROPERTY_NAME_SOURCE, ScenarioTypeBase.PROPERTY_NAME_EXAMPLE })
			@Override
			int getExample();

			@DerivedRef({ PROPERTY_NAME_SOURCE, ScenarioTypeBase.PROPERTY_NAME_ITEM_EXAMPLE })
			@Override
			ConfigurationItem getItemExample();

		}

		public interface ScenarioTypeLocalDerivedInheritedMandatoryContainer
				extends ScenarioTypeBaseMandatory, ScenarioTypeBaseContainer {

			@DerivedRef({ PROPERTY_NAME_SOURCE, ScenarioTypeBase.PROPERTY_NAME_ITEM_EXAMPLE })
			@Override
			ConfigurationItem getItemExample();

		}

		public interface ScenarioTypeLocalDerivedInheritedMandatoryDerived
				extends ScenarioTypeBaseMandatory, ScenarioTypeBaseDerivedSecond {

			@DerivedRef({ PROPERTY_NAME_SOURCE, ScenarioTypeBase.PROPERTY_NAME_EXAMPLE })
			@Override
			int getExample();

			@DerivedRef({ PROPERTY_NAME_SOURCE, ScenarioTypeBase.PROPERTY_NAME_ITEM_EXAMPLE })
			@Override
			ConfigurationItem getItemExample();

		}

		public interface ScenarioTypeLocalDerivedInheritedMandatoryAbstract
				extends ScenarioTypeBaseMandatory, ScenarioTypeBaseAbstract {

			@DerivedRef({ PROPERTY_NAME_SOURCE, ScenarioTypeBase.PROPERTY_NAME_EXAMPLE })
			@Override
			int getExample();

			@DerivedRef({ PROPERTY_NAME_SOURCE, ScenarioTypeBase.PROPERTY_NAME_ITEM_EXAMPLE })
			@Override
			ConfigurationItem getItemExample();

		}

		public interface ScenarioTypeLocalDerivedInheritedContainerContainer
				extends ScenarioTypeBaseContainer, ScenarioTypeBaseContainerSecond {

			@DerivedRef({ PROPERTY_NAME_SOURCE, ScenarioTypeBase.PROPERTY_NAME_ITEM_EXAMPLE })
			@Override
			ConfigurationItem getItemExample();

		}

		public interface ScenarioTypeLocalDerivedInheritedContainerDerived
				extends ScenarioTypeBaseContainer, ScenarioTypeBaseDerivedSecond {

			@DerivedRef({ PROPERTY_NAME_SOURCE, ScenarioTypeBase.PROPERTY_NAME_ITEM_EXAMPLE })
			@Override
			ConfigurationItem getItemExample();

		}

		public interface ScenarioTypeLocalDerivedInheritedContainerAbstract
				extends ScenarioTypeBaseContainer, ScenarioTypeBaseAbstract {

			@DerivedRef({ PROPERTY_NAME_SOURCE, ScenarioTypeBase.PROPERTY_NAME_ITEM_EXAMPLE })
			@Override
			ConfigurationItem getItemExample();

		}

		public interface ScenarioTypeLocalDerivedInheritedDerivedContainer
				extends ScenarioTypeBaseDerivedSecond, ScenarioTypeBaseContainer {

			@DerivedRef({ PROPERTY_NAME_SOURCE, ScenarioTypeBase.PROPERTY_NAME_ITEM_EXAMPLE })
			@Override
			ConfigurationItem getItemExample();

		}

		public interface ScenarioTypeLocalDerivedInheritedDerivedDerived
				extends ScenarioTypeBaseDerivedSecond, ScenarioTypeBaseDerivedThird {

			@DerivedRef({ PROPERTY_NAME_SOURCE, ScenarioTypeBase.PROPERTY_NAME_EXAMPLE })
			@Override
			int getExample();

			@DerivedRef({ PROPERTY_NAME_SOURCE, ScenarioTypeBase.PROPERTY_NAME_ITEM_EXAMPLE })
			@Override
			ConfigurationItem getItemExample();

		}

		public interface ScenarioTypeLocalDerivedInheritedDerivedAbstract
				extends ScenarioTypeBaseDerivedSecond, ScenarioTypeBaseAbstract {

			@DerivedRef({ PROPERTY_NAME_SOURCE, ScenarioTypeBase.PROPERTY_NAME_EXAMPLE })
			@Override
			int getExample();

			@DerivedRef({ PROPERTY_NAME_SOURCE, ScenarioTypeBase.PROPERTY_NAME_ITEM_EXAMPLE })
			@Override
			ConfigurationItem getItemExample();

		}

	}

	public interface ScenarioContainerLocalAbstractInheritedXY {

		@Abstract
		public interface ScenarioTypeLocalAbstractInheritedCDefaultCDefault
				extends ScenarioTypeBaseCDefault, ScenarioTypeBaseCDefaultSecond {

			@Abstract
			@Override
			int getExample();

		}

		@Abstract
		public interface ScenarioTypeLocalAbstractInheritedCDefaultADefault
				extends ScenarioTypeBaseCDefault, ScenarioTypeBaseADefault {

			@Abstract
			@Override
			int getExample();

		}

		@Abstract
		public interface ScenarioTypeLocalAbstractInheritedCDefaultMandatory
				extends ScenarioTypeBaseCDefault, ScenarioTypeBaseMandatory {

			@Abstract
			@Override
			int getExample();

		}

		// ScenarioTypeLocalAbstractInheritedCDefaultContainer

		@Abstract
		public interface ScenarioTypeLocalAbstractInheritedCDefaultDerived
				extends ScenarioTypeBaseCDefault, ScenarioTypeBaseDerived {

			@Abstract
			@Override
			int getExample();

		}

		@Abstract
		public interface ScenarioTypeLocalAbstractInheritedCDefaultAbstract
				extends ScenarioTypeBaseCDefault, ScenarioTypeBaseAbstract {

			@Abstract
			@Override
			int getExample();

		}

		@Abstract
		public interface ScenarioTypeLocalAbstractInheritedADefaultCDefault
				extends ScenarioTypeBaseADefault, ScenarioTypeBaseCDefault {

			@Abstract
			@Override
			int getExample();

		}

		@Abstract
		public interface ScenarioTypeLocalAbstractInheritedADefaultADefault
				extends ScenarioTypeBaseADefault, ScenarioTypeBaseADefaultSecond {

			@Abstract
			@Override
			int getExample();

			@Abstract
			@Override
			ConfigurationItem getItemExample();

		}

		@Abstract
		public interface ScenarioTypeLocalAbstractInheritedADefaultMandatory
				extends ScenarioTypeBaseADefault, ScenarioTypeBaseMandatory {

			@Abstract
			@Override
			int getExample();

			@Abstract
			@Override
			ConfigurationItem getItemExample();

		}

		@Abstract
		public interface ScenarioTypeLocalAbstractInheritedADefaultContainer
				extends ScenarioTypeBaseADefault, ScenarioTypeBaseContainer {

			@Abstract
			@Override
			ConfigurationItem getItemExample();

		}

		@Abstract
		public interface ScenarioTypeLocalAbstractInheritedADefaultDerived
				extends ScenarioTypeBaseADefault, ScenarioTypeBaseDerived {

			@Abstract
			@Override
			int getExample();

			@Abstract
			@Override
			ConfigurationItem getItemExample();

		}

		@Abstract
		public interface ScenarioTypeLocalAbstractInheritedADefaultAbstract
				extends ScenarioTypeBaseADefault, ScenarioTypeBaseAbstract {

			@Abstract
			@Override
			int getExample();

			@Abstract
			@Override
			ConfigurationItem getItemExample();

		}

		@Abstract
		public interface ScenarioTypeLocalAbstractInheritedMandatoryContainer
				extends ScenarioTypeBaseMandatory, ScenarioTypeBaseContainer {

			@Abstract
			@Override
			ConfigurationItem getItemExample();

		}

		@Abstract
		public interface ScenarioTypeLocalAbstractInheritedMandatoryDerived
				extends ScenarioTypeBaseMandatory, ScenarioTypeBaseDerived {

			@Abstract
			@Override
			int getExample();

			@Abstract
			@Override
			ConfigurationItem getItemExample();

		}

		@Abstract
		public interface ScenarioTypeLocalAbstractInheritedMandatoryAbstract
				extends ScenarioTypeBaseMandatory, ScenarioTypeBaseAbstract {

			@Abstract
			@Override
			int getExample();

			@Abstract
			@Override
			ConfigurationItem getItemExample();

		}

		@Abstract
		public interface ScenarioTypeLocalAbstractInheritedContainerContainer
				extends ScenarioTypeBaseContainer, ScenarioTypeBaseContainerSecond {

			@Abstract
			@Override
			ConfigurationItem getItemExample();

		}

		@Abstract
		public interface ScenarioTypeLocalAbstractInheritedContainerDerived
				extends ScenarioTypeBaseContainer, ScenarioTypeBaseDerived {

			@Abstract
			@Override
			ConfigurationItem getItemExample();

		}

		@Abstract
		public interface ScenarioTypeLocalAbstractInheritedContainerAbstract
				extends ScenarioTypeBaseContainer, ScenarioTypeBaseAbstract {

			@Abstract
			@Override
			ConfigurationItem getItemExample();

		}

		@Abstract
		public interface ScenarioTypeLocalAbstractInheritedDerivedContainer
				extends ScenarioTypeBaseDerived, ScenarioTypeBaseContainer {

			@Abstract
			@Override
			ConfigurationItem getItemExample();

		}

		@Abstract
		public interface ScenarioTypeLocalAbstractInheritedDerivedDerived
				extends ScenarioTypeBaseDerived, ScenarioTypeBaseDerivedSecond {

			@Abstract
			@Override
			int getExample();

			@Abstract
			@Override
			ConfigurationItem getItemExample();

		}

		@Abstract
		public interface ScenarioTypeLocalAbstractInheritedDerivedAbstract
				extends ScenarioTypeBaseDerived, ScenarioTypeBaseAbstract {

			@Abstract
			@Override
			int getExample();

			@Abstract
			@Override
			ConfigurationItem getItemExample();

		}

	}

	public interface ScenarioContainerInheritedXYZ {

		public interface ScenarioTypeInheritedCDefaultADefaultMandatory
				extends ScenarioTypeBaseCDefault, ScenarioTypeBaseADefault, ScenarioTypeBaseMandatory {
			// Nothing needed
		}

		// ScenarioTypeInheritedCDefaultADefaultContainer

		public interface ScenarioTypeInheritedCDefaultADefaultDerived
				extends ScenarioTypeBaseCDefault, ScenarioTypeBaseADefault, ScenarioTypeBaseDerived {
			// Nothing needed
		}

		public interface ScenarioTypeInheritedCDefaultADefaultAbstract
				extends ScenarioTypeBaseCDefault, ScenarioTypeBaseADefault, ScenarioTypeBaseAbstract {
			// Nothing needed
		}

		// ScenarioTypeInheritedCDefaultMandatoryContainer

		public interface ScenarioTypeInheritedCDefaultMandatoryDerived
				extends ScenarioTypeBaseCDefault, ScenarioTypeBaseMandatory, ScenarioTypeBaseDerived {
			// Nothing needed
		}

		public interface ScenarioTypeInheritedCDefaultMandatoryAbstract
				extends ScenarioTypeBaseCDefault, ScenarioTypeBaseMandatory, ScenarioTypeBaseAbstract {
			// Nothing needed
		}

		// ScenarioTypeInheritedCDefaultContainerDerived
		// ScenarioTypeInheritedCDefaultContainerAbstract
		// ScenarioTypeInheritedCDefaultDerivedContainer

		public interface ScenarioTypeInheritedCDefaultDerivedAbstract
				extends ScenarioTypeBaseCDefault, ScenarioTypeBaseDerived, ScenarioTypeBaseAbstract {
			// Nothing needed
		}

		public interface ScenarioTypeInheritedADefaultCDefaultMandatory
				extends ScenarioTypeBaseADefault, ScenarioTypeBaseCDefault, ScenarioTypeBaseMandatory {
			// Nothing needed
		}

		// ScenarioTypeInheritedADefaultCDefaultContainer

		public interface ScenarioTypeInheritedADefaultCDefaultDerived
				extends ScenarioTypeBaseADefault, ScenarioTypeBaseCDefault, ScenarioTypeBaseDerived {
			// Nothing needed
		}

		public interface ScenarioTypeInheritedADefaultCDefaultAbstract
				extends ScenarioTypeBaseADefault, ScenarioTypeBaseCDefault, ScenarioTypeBaseAbstract {
			// Nothing needed
		}

		public interface ScenarioTypeInheritedADefaultMandatoryContainer
				extends ScenarioTypeBaseADefault, ScenarioTypeBaseMandatory, ScenarioTypeBaseContainer {
			// Nothing needed
		}

		public interface ScenarioTypeInheritedADefaultMandatoryDerived
				extends ScenarioTypeBaseADefault, ScenarioTypeBaseMandatory, ScenarioTypeBaseDerived {
			// Nothing needed
		}

		public interface ScenarioTypeInheritedADefaultMandatoryAbstract
				extends ScenarioTypeBaseADefault, ScenarioTypeBaseMandatory, ScenarioTypeBaseAbstract {
			// Nothing needed
		}

		public interface ScenarioTypeInheritedADefaultContainerDerived
				extends ScenarioTypeBaseADefault, ScenarioTypeBaseContainer, ScenarioTypeBaseDerived {
			// Nothing needed
		}

		public interface ScenarioTypeInheritedADefaultContainerAbstract
				extends ScenarioTypeBaseADefault, ScenarioTypeBaseContainer, ScenarioTypeBaseAbstract {
			// Nothing needed
		}

		public interface ScenarioTypeInheritedADefaultDerivedContainer
				extends ScenarioTypeBaseADefault, ScenarioTypeBaseDerived, ScenarioTypeBaseContainer {
			// Nothing needed
		}

		public interface ScenarioTypeInheritedADefaultDerivedAbstract
				extends ScenarioTypeBaseADefault, ScenarioTypeBaseDerived, ScenarioTypeBaseAbstract {
			// Nothing needed
		}

		public interface ScenarioTypeInheritedMandatoryContainerDerived
				extends ScenarioTypeBaseMandatory, ScenarioTypeBaseContainer, ScenarioTypeBaseDerived {
			// Nothing needed
		}

		public interface ScenarioTypeInheritedMandatoryContainerAbstract
				extends ScenarioTypeBaseMandatory, ScenarioTypeBaseContainer, ScenarioTypeBaseAbstract {
			// Nothing needed
		}

		public interface ScenarioTypeInheritedMandatoryDerivedContainer
				extends ScenarioTypeBaseMandatory, ScenarioTypeBaseDerived, ScenarioTypeBaseContainer {
			// Nothing needed
		}

		public interface ScenarioTypeInheritedMandatoryDerivedAbstract
				extends ScenarioTypeBaseMandatory, ScenarioTypeBaseDerived, ScenarioTypeBaseAbstract {
			// Nothing needed
		}

		public interface ScenarioTypeInheritedContainerDerivedAbstract
				extends ScenarioTypeBaseContainer, ScenarioTypeBaseDerived, ScenarioTypeBaseAbstract {
			// Nothing needed
		}

		public interface ScenarioTypeInheritedDerivedContainerAbstract
				extends ScenarioTypeBaseDerived, ScenarioTypeBaseContainer, ScenarioTypeBaseAbstract {
			// Nothing needed
		}

	}

	public interface ScenarioContainerInheritedWXYZ {

		// ScenarioTypeInheritedCDefaultADefaultMandatoryContainer

		public interface ScenarioTypeInheritedCDefaultADefaultMandatoryDerived
				extends ScenarioTypeBaseCDefault, ScenarioTypeBaseADefault, ScenarioTypeBaseMandatory,
				ScenarioTypeBaseDerived {
			// Nothing needed
		}

		public interface ScenarioTypeInheritedCDefaultADefaultMandatoryAbstract
				extends ScenarioTypeBaseCDefault, ScenarioTypeBaseADefault, ScenarioTypeBaseMandatory,
				ScenarioTypeBaseAbstract {
			// Nothing needed
		}

		// ScenarioTypeInheritedCDefaultADefaultContainerDerived
		// ScenarioTypeInheritedCDefaultADefaultContainerAbstract
		// ScenarioTypeInheritedCDefaultADefaultDerivedContainer

		public interface ScenarioTypeInheritedCDefaultADefaultDerivedAbstract
				extends ScenarioTypeBaseCDefault, ScenarioTypeBaseADefault, ScenarioTypeBaseDerived,
				ScenarioTypeBaseAbstract {
			// Nothing needed
		}

		// ScenarioTypeInheritedCDefaultMandatoryContainerDerived
		// ScenarioTypeInheritedCDefaultMandatoryContainerAbstract
		// ScenarioTypeInheritedCDefaultMandatoryDerivedContainer

		public interface ScenarioTypeInheritedCDefaultMandatoryDerivedAbstract
				extends ScenarioTypeBaseCDefault, ScenarioTypeBaseMandatory, ScenarioTypeBaseDerived,
				ScenarioTypeBaseAbstract {
			// Nothing needed
		}

		// ScenarioTypeInheritedCDefaultContainerDerivedAbstract
		// ScenarioTypeInheritedCDefaultDerivedContainerAbstract
		// ScenarioTypeInheritedADefaultCDefaultMandatoryContainer

		public interface ScenarioTypeInheritedADefaultCDefaultMandatoryDerived
				extends ScenarioTypeBaseADefault, ScenarioTypeBaseCDefault, ScenarioTypeBaseMandatory,
				ScenarioTypeBaseDerived {
			// Nothing needed
		}

		public interface ScenarioTypeInheritedADefaultCDefaultMandatoryAbstract
				extends ScenarioTypeBaseADefault, ScenarioTypeBaseCDefault, ScenarioTypeBaseMandatory,
				ScenarioTypeBaseAbstract {
			// Nothing needed
		}

		// ScenarioTypeInheritedADefaultCDefaultContainerDerived
		// ScenarioTypeInheritedADefaultCDefaultContainerAbstract
		// ScenarioTypeInheritedADefaultCDefaultDerivedContainer

		public interface ScenarioTypeInheritedADefaultCDefaultDerivedAbstract
				extends ScenarioTypeBaseADefault, ScenarioTypeBaseCDefault, ScenarioTypeBaseDerived,
				ScenarioTypeBaseAbstract {
			// Nothing needed
		}

		public interface ScenarioTypeInheritedADefaultMandatoryContainerDerived
				extends ScenarioTypeBaseADefault, ScenarioTypeBaseMandatory, ScenarioTypeBaseContainer,
				ScenarioTypeBaseDerived {
			// Nothing needed
		}

		public interface ScenarioTypeInheritedADefaultMandatoryContainerAbstract
				extends ScenarioTypeBaseADefault, ScenarioTypeBaseMandatory, ScenarioTypeBaseContainer,
				ScenarioTypeBaseAbstract {
			// Nothing needed
		}

		public interface ScenarioTypeInheritedADefaultMandatoryDerivedContainer
				extends ScenarioTypeBaseADefault, ScenarioTypeBaseMandatory, ScenarioTypeBaseDerived,
				ScenarioTypeBaseContainer {
			// Nothing needed
		}

		public interface ScenarioTypeInheritedADefaultMandatoryDerivedAbstract
				extends ScenarioTypeBaseADefault, ScenarioTypeBaseMandatory, ScenarioTypeBaseDerived,
				ScenarioTypeBaseAbstract {
			// Nothing needed
		}

		public interface ScenarioTypeInheritedADefaultDerivedContainerAbstract
				extends ScenarioTypeBaseADefault, ScenarioTypeBaseDerived, ScenarioTypeBaseContainer,
				ScenarioTypeBaseAbstract {
			// Nothing needed
		}

		public interface ScenarioTypeInheritedADefaultContainerDerivedAbstract
				extends ScenarioTypeBaseADefault, ScenarioTypeBaseDerived, ScenarioTypeBaseContainer,
				ScenarioTypeBaseAbstract {
			// Nothing needed
		}

		public interface ScenarioTypeInheritedMandatoryContainerDerivedAbstract
				extends ScenarioTypeBaseMandatory, ScenarioTypeBaseContainer, ScenarioTypeBaseDerived,
				ScenarioTypeBaseAbstract {
			// Nothing needed
		}

		public interface ScenarioTypeInheritedMandatoryDerivedContainerAbstract
				extends ScenarioTypeBaseMandatory, ScenarioTypeBaseDerived, ScenarioTypeBaseContainer,
				ScenarioTypeBaseAbstract {
			// Nothing needed
		}

	}

}
