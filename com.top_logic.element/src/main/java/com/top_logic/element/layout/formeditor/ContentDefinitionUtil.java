/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.formeditor;

import java.util.List;
import java.util.function.Predicate;

import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.equal.ConfigEquality;
import com.top_logic.element.layout.formeditor.definition.TLFormDefinition;
import com.top_logic.element.layout.formeditor.definition.TabbarDefinition;
import com.top_logic.element.layout.formeditor.definition.TabbarDefinition.TabDefinition;
import com.top_logic.model.form.definition.ContainerDefinition;
import com.top_logic.model.form.definition.FormDefinition;
import com.top_logic.model.form.definition.FormElement;
import com.top_logic.model.form.implementation.FormElementTemplateProvider;

/**
 * A utility class for {@link FormElement}s.
 * 
 * @author <a href="mailto:iwi@top-logic.com">Isabell Wittich</a>
 */
public class ContentDefinitionUtil {

	private static final Predicate<PropertyDescriptor> CAN_HAVE_SETTERS = new Predicate<>() {

		@Override
		public boolean test(PropertyDescriptor property) {
			return property.canHaveSetter();
		}

	};

	private static final Predicate<PropertyDescriptor> IS_MANDATORY = new Predicate<>() {

		@Override
		public boolean test(PropertyDescriptor property) {
			return property.isMandatory();
		}

	};

	/**
	 * Removes a {@link FormElement} of a {@link ContainerDefinition}.
	 * 
	 * @param container
	 *        The ancestor of the element to remove. It is not necessary that it is the parent.
	 * @param toRemove
	 *        The element to remove.
	 * 
	 * @return Whether an element has been removed.
	 */
	public static boolean removeDefininition(ContainerDefinition<?> container, FormElement<?> toRemove) {
		boolean removed = false;
		if (container != null) {
			List<PolymorphicConfiguration<? extends FormElementTemplateProvider>> content = container.getContent();
			boolean isRemoved = content.remove(toRemove);
			if (!isRemoved) {
				for (PolymorphicConfiguration<? extends FormElementTemplateProvider> c : content) {
					if (c instanceof ContainerDefinition) {
						removed = removed || removeDefininition((ContainerDefinition<?>) c, toRemove);
					}
					else if (c instanceof TabbarDefinition) {
						for (TabDefinition tab : ((TabbarDefinition) c).getTabs()) {
							removed = removed || removeDefininition(tab, toRemove);
						}
					}
				}
			} else {
				return true;
			}
		}

		return removed;
	}

	/**
	 * Checks if there is at least one attribute taken of a given {@link ConfigurationDescriptor}
	 * which can be edited.
	 * 
	 * @param descriptor
	 *        The {@link ConfigurationDescriptor} to get the properties of.
	 * @see PropertyDescriptor#canHaveSetter()
	 * 
	 * @return Whether there is at least one attribute which can be edited.
	 */
	public static boolean hasAttributes(ConfigurationDescriptor descriptor) {
		return hasAttributes(descriptor, CAN_HAVE_SETTERS);
	}

	/**
	 * Checks if there is at least one attribute taken of a given {@link ConfigurationDescriptor}
	 * which is mandatory.
	 * 
	 * @param descriptor
	 *        The {@link ConfigurationDescriptor} to get the properties of.
	 * @see PropertyDescriptor#isMandatory()
	 * 
	 * @return Whether there is at least one attribute which is mandatory.
	 */
	public static boolean hasMandatoryAttributes(ConfigurationDescriptor descriptor) {
		return hasAttributes(descriptor, IS_MANDATORY);
	}

	/**
	 * Checks if there is at least one attribute taken of a given {@link ConfigurationDescriptor}
	 * which fulfills the propertyTest.
	 * 
	 * @param descriptor
	 *        The {@link ConfigurationDescriptor} to get the properties of.
	 * @param propertyTest
	 *        {@link Predicate} to test whether a property fulfills a given criteria.
	 * 
	 * @return Whether there is at least one attribute which fulfills a given criteria.
	 */
	public static boolean hasAttributes(ConfigurationDescriptor descriptor,
			Predicate<PropertyDescriptor> propertyTest) {
		boolean foundAttribute = false;
		for (PropertyDescriptor property : descriptor.getProperties()) {
			if (propertyTest.test(property)) {
				foundAttribute = true;
			}
		}

		return foundAttribute;
	}

	/**
	 * Checks based on the ID of the {@link FormElement} whether the
	 * {@link ContainerDefinition} contains it. Goes deep though the given container.
	 * 
	 * @param container
	 *        The container to check whether it contains the {@link FormElement}.
	 * @param def
	 *        The {@link FormElement} to check whether its inside in the container.
	 * 
	 * @return Whether the container or any sub-container contains the given
	 *         {@link FormElement}.
	 */
	public static boolean contains(ContainerDefinition<?> container, FormElement<?> def) {
		List<PolymorphicConfiguration<? extends FormElementTemplateProvider>> content = container.getContent();
		boolean contains = false;

		for (PolymorphicConfiguration<? extends FormElementTemplateProvider> c : content) {
			if (c.equals(def)) {
				return true;
			} else {
				if (c instanceof ContainerDefinition) {
					contains = contains || contains((ContainerDefinition<?>) c, def);
				}
				else if (c instanceof TabbarDefinition) {
					for (TabDefinition tab : ((TabbarDefinition) c).getTabs()) {
						contains = contains || contains(tab, def);
					}
				}
			}
		}

		return contains;
	}
	
	/**
	 * Looks for the position of a {@link FormElement} inside a list.
	 * 
	 * @param list
	 *        The list of {@link FormElement}s.
	 * @param item
	 *        The {@link FormElement} to look for.
	 * @return The index of the {@link FormElement}.
	 */
	public static int indexOf(List<PolymorphicConfiguration<? extends FormElementTemplateProvider>> list,
			FormElement<?> item) {
		int index = -1;

		for (int i = 0; i < list.size(); i++) {
			if (ConfigEquality.INSTANCE_ALL_BUT_DERIVED.equals(list.get(i), item)) {
				index = i;
			}
		}
		return index;
	}

	/**
	 * Creates a {@link PropertyDescriptor} for the property {@link ContainerDefinition#CONTENT}.
	 * 
	 * @param model
	 *        The {@link TLFormDefinition} to create the descriptor for.
	 * @return The {@link PropertyDescriptor}.
	 */
	public static PropertyDescriptor getContentProperty(FormDefinition model) {
		return model.descriptor().getProperty(ContainerDefinition.CONTENT);
	}

}