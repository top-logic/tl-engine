/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.config.container;

import java.util.List;
import java.util.Map;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Container;
import com.top_logic.basic.config.annotation.DerivedRef;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Subtypes;
import com.top_logic.basic.config.annotation.Subtypes.Subtype;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.basic.config.annotation.defaults.ItemDefault;
import com.top_logic.basic.config.annotation.defaults.ListDefault;
import com.top_logic.basic.config.container.ConfigPart;
import com.top_logic.basic.config.internal.gen.NoImplementationClassGeneration;

/**
 * {@link ConfigurationItem} interfaces for testing the container references.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
@SuppressWarnings("javadoc")
public interface ScenarioContainerReference {

	public interface A extends ConfigPart {
		String NAME = "name";

		String B_PROPERTY = "b";

		String B_LIST_PROPERTY = "b-list";

		String B_MAP_PROPERTY = "b-map";

		@Name(NAME)
		String getName();

		void setName(String value);

		@Name(B_PROPERTY)
		B getB();

		void setB(B value);

		@Name(B_LIST_PROPERTY)
		@Subtypes({
			@Subtype(tag = "b", type = B.class),
			@Subtype(tag = "bx", type = BX.class),
			@Subtype(tag = "by", type = BY.class),
			@Subtype(tag = "bz", type = BZ.class)
		})
		List<B> getBList();

		@Name(B_MAP_PROPERTY)
		@Key(B.NAME)
		@Subtypes({
			@Subtype(tag = "b", type = B.class),
			@Subtype(tag = "bx", type = BX.class),
			@Subtype(tag = "by", type = BY.class),
			@Subtype(tag = "bz", type = BZ.class)
		})
		Map<String, B> getBMap();
	}

	public interface AS extends A {
		String getValue();

		void setValue(String value);
	}

	public interface B extends ConfigPart {
		String NAME = "name";

		@Name(NAME)
		String getName();

		void setName(String value);
	}

	public interface BX extends B {
		@Container
		A getParent();
	}

	public interface BY extends B {
		@Container
		AS getContext();
	}

	public interface BZ extends BX, BY {
		// Pure sum interface.
	}

	public interface ScenarioTypeEntry extends ConfigPart {
		// Nothing needed
	}

	public interface ScenarioTypeContainer extends ConfigurationItem {

		ConfigPart getEntry();

		void setEntry(ConfigPart newValue);

	}

	public interface ScenarioTypeContainerMulti extends ConfigurationItem {

		ConfigPart getLeftEntry();

		void setLeftEntry(ConfigPart newValue);

		ConfigPart getRightEntry();

		void setRightEntry(ConfigPart newValue);

	}

	public interface ScenarioTypeContainerList extends ConfigurationItem {

		List<? extends ConfigPart> getEntries();

		void setEntries(List<? extends ConfigPart> newValues);

	}

	public interface ScenarioTypeContainerListMulti extends ConfigurationItem {

		List<? extends ConfigPart> getLeftEntries();

		void setLeftEntries(List<? extends ConfigPart> newValues);

		List<? extends ConfigPart> getRightEntries();

		void setRightEntries(List<? extends ConfigPart> newValues);

	}

	public interface ScenarioTypeEntrySpecificContainer extends ConfigPart {

		@Container
		ScenarioTypeSpecificContainer getContainer();

	}

	public interface ScenarioTypePropertyEntrySpecificContainer extends ConfigPart {

		@Container
		public ScenarioTypeSpecificContainer getParent();

	}

	public interface ScenarioTypeSpecificContainer extends ConfigurationItem {

		ConfigPart getEntry();

		void setEntry(ConfigPart newValue);

	}

	@NoImplementationClassGeneration
	public interface ScenarioTypeEntryMoreSpecificContainer extends ScenarioTypeEntrySpecificContainer {

		@Override
		public ScenarioTypeMoreSpecificContainer container();

	}

	public interface ScenarioTypePropertyEntryMoreSpecificContainer extends ScenarioTypePropertyEntrySpecificContainer {

		@Override
		@Container
		public ScenarioTypeMoreSpecificContainer getParent();

	}

	public interface ScenarioTypeMoreSpecificContainer extends ScenarioTypeSpecificContainer {
		// Nothing needed: Pure sub interface
	}

	public interface ScenarioTypePropertySetter extends ConfigPart {

		@Container
		ConfigPart getParent();

		void setParent(ConfigPart newValue);

	}

	public interface ScenarioTypeProperty extends ConfigPart {

		String PARENT = "parent";

		@Container
		@Name(PARENT)
		@Subtypes({})
		ConfigurationItem getParent();

	}

	public interface ScenarioTypePropertySub extends ScenarioTypeProperty {

		@Override
		ConfigurationItem getParent();

	}

	public interface ScenarioTypePropertySubNotMentioned extends ScenarioTypeProperty {
		// Nothing needed: Pure sub interface
	}

	public interface ScenarioTypePropertySubSpecific extends ScenarioTypeProperty {

		@Override
		ScenarioTypeContainer getParent();

	}

	public interface ScenarioTypeGetContainer extends ConfigPart {

		@Container
		@Subtypes({})
		ConfigurationItem getContainer();

	}

	@NoImplementationClassGeneration
	public interface ScenarioTypeNoConfigPartButContainerAnnotation extends ConfigurationItem {

		@Container
		@Subtypes({})
		ConfigurationItem getParent();

	}

	@NoImplementationClassGeneration
	public interface ScenarioTypeContainerMethodOverride extends ConfigPart {

		@Override
		ConfigurationItem container();

	}

	public interface ScenarioTypeItemCommon extends ConfigurationItem {

		@Subtypes({})
		ConfigurationItem getChild();

		void setChild(ConfigurationItem child);

	}

	public interface ScenarioTypeItemA extends ScenarioTypeItemCommon {
		// Nothing needed
	}

	public interface ScenarioTypeItemB extends ScenarioTypeItemCommon {
		// Nothing needed
	}

	public interface ScenarioTypeItemAB extends ScenarioTypeItemA, ScenarioTypeItemB {
		// Nothing needed
	}

	public interface ScenarioTypeMultipleLocalContainerProperties extends ConfigPart {

		@Container
		ScenarioTypeItemA getContainerA();

		@Container
		ScenarioTypeItemB getContainerB();

	}

	public interface ScenarioTypeChildA extends ConfigPart {

		@Container
		ScenarioTypeItemA getContainerA();

	}

	public interface ScenarioTypeChildB extends ConfigPart {

		@Container
		ScenarioTypeItemB getContainerB();

	}

	public interface ScenarioTypeMultipleInheritedContainerProperties extends ScenarioTypeChildA, ScenarioTypeChildB {
		// Nothing
	}

	public interface ScenarioTypeLocalAndInheritedContainerProperties extends ScenarioTypeChildA {

		@Container
		ScenarioTypeItemB getContainerB();

	}

	public interface ScenarioTypeDerivedViaContainerEntry extends ConfigPart {

		String CONTAINER = "container";

		@Container
		@Name(CONTAINER)
		ScenarioTypeDerivedViaContainerParent getContainer();

		@DerivedRef({ CONTAINER, ScenarioTypeDerivedViaContainerParent.SOURCE })
		int getDerived();

	}

	public interface ScenarioTypeDerivedViaContainerParent extends ConfigPart {

		int DEFAULT_VALUE_SOURCE = 3;

		String SOURCE = "source";

		@IntDefault(DEFAULT_VALUE_SOURCE)
		@Name(SOURCE)
		int getSource();

		void setSource(int value);

		ScenarioTypeDerivedViaContainerEntry getEntry();

		void setEntry(ScenarioTypeDerivedViaContainerEntry entry);

	}

	public interface ScenarioTypeContainerOfDefaultItem extends ConfigurationItem {

		@ItemDefault
		ConfigPart getEntry();

	}

	public interface ScenarioTypeContainerOfDefaultItemList extends ConfigurationItem {

		@ListDefault({ ConfigPart.class })
		List<ConfigPart> getEntries();

	}

}
