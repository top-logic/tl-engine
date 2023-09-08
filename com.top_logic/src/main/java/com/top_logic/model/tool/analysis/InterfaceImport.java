/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.tool.analysis;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Container;
import com.top_logic.basic.config.annotation.External;
import com.top_logic.basic.config.annotation.Indexed;
import com.top_logic.basic.config.annotation.Reference;
import com.top_logic.basic.config.annotation.Root;
import com.top_logic.basic.config.internal.gen.ConfigurationDescriptorSPI;
import com.top_logic.model.TLAssociation;
import com.top_logic.model.TLAssociationEnd;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModel;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLPrimitive;
import com.top_logic.model.TLProperty;
import com.top_logic.model.builtin.TLCore;
import com.top_logic.model.util.TLModelUtil;

/**
 * Import annotated configuration interfaces as models.
 * 
 * @see TypedConfiguration
 * 
 * @see Root
 * @see Container
 * @see Reference
 * @see Indexed
 * @see External
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class InterfaceImport {
	
	private final TLModel targetModel;
	
	private final Map<ConfigurationDescriptor, TLClass> importedDescriptors = 
		new HashMap<>();

	private Map<Class<?>, TLClass> pendingImports = new HashMap<>();

	/**
	 * Creates a {@link InterfaceImport}.
	 * 
	 * @param targetModule
	 *        The module to fill with models extracted from configuration
	 *        interface hierarchies.
	 *        
	 * @see #importInterface(Class)
	 */
	public InterfaceImport(TLModel targetModule) {
		this.targetModel = targetModule;
	}
	
	/**
	 * The {@link TLModel} that is being created by this {@link InterfaceImport}.
	 */
	public TLModel getTargetModel() {
		return targetModel;
	}

	/**
	 * Imports the given root of a configuration interface hierarchy to the
	 * {@link #getTargetModel()}.
	 * 
	 * @param rootIntf
	 *        The root of a configuration interface hierarchy.
	 */
	public void importInterface(Class<?> rootIntf) {
		ConfigurationDescriptorSPI rootDescriptor =
			(ConfigurationDescriptorSPI) TypedConfiguration.getConfigurationDescriptor(rootIntf);
		importDescriptor(rootDescriptor);
		
		Set<Class<?>> concreteTypes = rootDescriptor.getConcreteTypes();
		if (concreteTypes != null) {
			for (Class<?> concreteIntf : concreteTypes) {
				ConfigurationDescriptor descriptor = TypedConfiguration.getConfigurationDescriptor(concreteIntf);
				importDescriptor(descriptor);
			}
		}
		
		while (! pendingImports.isEmpty()) {
			ArrayList<Class<?>> workList = new ArrayList<>(pendingImports.keySet());
			pendingImports.clear();
			
			for (Class<?> pendingImport : workList) {
				ConfigurationDescriptor descriptor = TypedConfiguration.getConfigurationDescriptor(pendingImport);
				importDescriptor(descriptor);
			}
		}
		
	}

	private void importDescriptor(ConfigurationDescriptor descriptor) {
		if (importedDescriptors.containsKey(descriptor)) {
			return;
		}
		
		Class<?> configurationInterface = descriptor.getConfigurationInterface();
		
		// Import all super descriptors.
		for (ConfigurationDescriptor superDescr : descriptor.getSuperDescriptors()) {
			importDescriptor(superDescr);
		}
		
		TLClass importedClass = makeClass(configurationInterface);
		importedDescriptors.put(descriptor, importedClass);

		// Create extends relations.
		for (ConfigurationDescriptor superDescr : descriptor.getSuperDescriptors()) {
			importedClass.getGeneralizations().add(importedDescriptors.get(superDescr));
		}
		
		for (PropertyDescriptor property : descriptor.getProperties()) {
			if (property.isInherited()) {
				continue;
			}
			
			String propertyName = property.getPropertyName();
			Class<?> propertyType = property.getType();
			
			switch (property.kind()) {
				case ITEM: {
				// Single containment.
				TLClass referencedClass = makeClass(propertyType);
				
				addContainmentReference(importedClass, referencedClass, propertyName, false);
				break;
			}
				case MAP:
				case ARRAY:
				case LIST: {
				// Multiple containment.
				TLClass referencedClass = makeClass(property.getElementType());
				
				addContainmentReference(importedClass, referencedClass, propertyName, true);
				break;
			}
				case REF: {
				TLClass referencedClass = makeClass(property.isMultiple() ? property.getElementType() : propertyType);
				
					TLAssociation reference =
						TLModelUtil.addAssociation((TLModule) importedClass.getScope(), importedClass.getName() + '$'
							+ propertyName);
				
				// TODO: Problem: Order of ends is determined by the loading order.
				// TODO: Other end of the reference (if it exists) is not mapped to the same association.
				TLAssociationEnd sourceEnd = TLModelUtil.addEnd(reference, "source", importedClass);
				sourceEnd.setMultiple(true);
				
				TLAssociationEnd targetEnd = TLModelUtil.addEnd(reference, "target", referencedClass);
				targetEnd.setOrdered(property.isOrdered());
				targetEnd.setMultiple(property.isMultiple());
				
				TLModelUtil.addReference(importedClass, propertyName, targetEnd);
				break;
			}
			
				case COMPLEX: {
				// TODO: Property of external type, or reference to external class.
				break;
			}

				case PLAIN: {
				if (Enum.class.isAssignableFrom(propertyType)) {
					// TODO: Import of Java enum types.
				} else {
					TLProperty tlProperty = TLModelUtil.addProperty(importedClass, propertyName, null);
					setPrimitiveType(tlProperty, propertyType);
				}
				break;
			}
				case DERIVED: {
					// The derived value will be set automatically,
					// when the property from which it is derived / calculated is set.
					break;
				}
			
			default: {
					throw new UnsupportedOperationException("Model import for properties of kind '" + property.kind()
						+ "' is not supported.");
			}
			
			}
		}
	}

	private TLClass makeClass(Class<?> configurationInterface) {
		TLModule module = TLModelUtil.makeModule(targetModel, configurationInterface.getPackage().getName());
		
		String simpleName = configurationInterface.getSimpleName();
		TLClass importedClass = (TLClass) module.getType(simpleName);
		if (importedClass != null) {
			return importedClass;
		} 
		TLClass newClass = TLModelUtil.addClass(module, simpleName);
		
		pendingImports.put(configurationInterface, newClass);
		return newClass;
	}

	private void setPrimitiveType(TLProperty property, Class<?> type) {
		TLPrimitive tlType;
		if (type == String.class) {
			tlType = TLCore.getPrimitiveType(targetModel, TLPrimitive.Kind.STRING);
		}
		else if (type == Boolean.class) {
			tlType = TLCore.getPrimitiveType(targetModel, TLPrimitive.Kind.TRISTATE);
		}
		else if (type == boolean.class) {
			tlType = TLCore.getPrimitiveType(targetModel, TLPrimitive.Kind.BOOLEAN);
			property.setMandatory(true);
		}
		else if (type == Date.class) {
			tlType = TLCore.getPrimitiveType(targetModel, TLPrimitive.Kind.DATE);
		}
		else if (type == Byte.class || type == Short.class || type == Integer.class || type == Long.class) {
			tlType = TLCore.getPrimitiveType(targetModel, TLPrimitive.Kind.INT);
		}
		else if (type == byte.class || type == short.class || type == int.class || type == long.class) {
			tlType = TLCore.getPrimitiveType(targetModel, TLPrimitive.Kind.INT);
			property.setMandatory(true);
		}
		else if (type == Float.class || type == Double.class) {
			tlType = TLCore.getPrimitiveType(targetModel, TLPrimitive.Kind.FLOAT);
		}
		else if (type == float.class || type == double.class) {
			tlType = TLCore.getPrimitiveType(targetModel, TLPrimitive.Kind.FLOAT);
			property.setMandatory(true);
		}
		else {
			throw new IllegalArgumentException("Not a primitive type in property '" + property.getOwner().getName() + "." + property.getName() + "': " + type);
		}
		
		property.setType(tlType);
	}

	private void addContainmentReference(TLClass importedClass, TLClass referencedClass, String referenceName, boolean multiple) {
		TLAssociation containment =
			TLModelUtil.addAssociation((TLModule) importedClass.getScope(), importedClass.getName() + '$'
				+ referenceName);
		
		TLAssociationEnd containerEnd = TLModelUtil.addEnd(containment, "container", importedClass);
		containerEnd.setAggregate(true);
		
		TLAssociationEnd contentsEnd = TLModelUtil.addEnd(containment, "contents", referencedClass);
		contentsEnd.setComposite(true);
		contentsEnd.setOrdered(true);
		contentsEnd.setMultiple(multiple);
		
		TLModelUtil.addReference(importedClass, referenceName, contentsEnd);
	}

}
