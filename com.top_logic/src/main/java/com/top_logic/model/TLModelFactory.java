/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model;

import com.top_logic.basic.config.annotation.Factory;
import com.top_logic.model.TLPrimitive.Kind;
import com.top_logic.model.access.StorageMapping;

/**
 * Factory interface for parts of a {@link TLModel}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Factory
public interface TLModelFactory {

	/**
	 * Adds a new {@link TLModule} to the given {@link TLModel}.
	 * 
	 * @param model
	 *        The {@link TLModel} to add the new {@link TLModule} to.
	 * @param moduleName
	 *        The name of the new module.
	 * 
	 * @return The newly created module.
	 * 
	 * @throws IllegalArgumentException
	 *         if the given model already contains an module with the given name.
	 */
	TLModule addModule(TLModel model, String moduleName);

	/**
	 * Adds a new {@link TLEnumeration}.
	 * 
	 * @param module
	 *        The {@link TLModule} to add the new {@link TLEnumeration} to.
	 * @param scope
	 *        The {@link TLScope} of the new {@link TLEnumeration}.
	 * @param name
	 *        The name of the new enumeration.
	 * 
	 * @throws IllegalArgumentException
	 *         if the given scope already contains a {@link TLType} with the given name.
	 */
	TLEnumeration addEnumeration(TLModule module, TLScope scope, String name);
	
	/**
	 * Adds the a new {@link TLPrimitive} to the given module.
	 * 
	 * @param module
	 *        The {@link TLModule} to add the new {@link TLPrimitive} to.
	 * @param scope
	 *        The {@link TLScope} of the new {@link TLPrimitive}.
	 * @param name
	 *        The name of the new primitive.
	 * @param kind
	 *        The kind of the new primitive.
	 * @param mapping
	 *        The {@link StorageMapping} converting from application values to storage values and
	 *        vice versa.
	 * @return The newly created primitive.
	 * 
	 * @throws IllegalArgumentException
	 *         if the given scope already contains a {@link TLType} with the given name.
	 */
	TLPrimitive addDatatype(TLModule module, TLScope scope, String name, Kind kind, StorageMapping<?> mapping);

	/**
	 * Creates a new {@link TLClassifier}.
	 * 
	 * @param name
	 *        The name of the new classifier.
	 */
	TLClassifier createClassifier(String name);
	
	/**
	 * Adds the a new {@link TLClass} to the given module.
	 * 
	 * @param module
	 *        The {@link TLModule} to add the new {@link TLClass} to.
	 * @param scope
	 *        The {@link TLScope} of the new {@link TLClass}.
	 * @param name
	 *        The name of the new class.
	 * 
	 * @return The newly created class.
	 * 
	 * @throws IllegalArgumentException
	 *         if the given scope already contains a {@link TLType} with the given name.
	 */
	TLClass addClass(TLModule module, TLScope scope, String name);

	/**
	 * Adds the a new {@link TLAssociation} to the given module.
	 * 
	 * @param module
	 *        The {@link TLModule} to add the new {@link TLAssociation} to.
	 * @param scope
	 *        The {@link TLScope} of the new {@link TLAssociation}.
	 * @param name
	 *        The name of the new association.
	 * 
	 * @return The newly created association.
	 * 
	 * @throws IllegalArgumentException
	 *         if the given scope already contains a {@link TLType} with the given name.
	 */
	TLAssociation addAssociation(TLModule module, TLScope scope, String name);

	/**
	 * Adds a new {@link TLAssociationProperty} to the given {@link TLAssociation}
	 * 
	 * @param association
	 *        The {@link TLAssociation} to add the new property to.
	 * @param name
	 *        The name of the new property.
	 * @param valueType
	 *        The {@link TLTypePart#getType() value type} of the new {@link TLAssociationProperty}.
	 * @return The newly created {@link TLAssociationProperty}
	 * 
	 * @throws IllegalArgumentException
	 *         if the given {@link TLAssociation} already contains a part with the given name.
	 */
	TLAssociationProperty addAssociationProperty(TLAssociation association, String name, TLType valueType);

	/**
	 * Adds a new {@link TLClassProperty} to the given {@link TLClass}
	 * 
	 * @param tlClass
	 *        The {@link TLClass} to add the new property to.
	 * @param name
	 *        The name of the new property.
	 * @param valueType
	 *        The {@link TLTypePart#getType() value type} of the new {@link TLClassProperty}.
	 * 
	 * @return The newly created {@link TLClassProperty}.
	 * 
	 * @throws IllegalArgumentException
	 *         if the given {@link TLClass} already contains a part with the given name.
	 */
	TLClassProperty addClassProperty(TLClass tlClass, String name, TLType valueType);

	/**
	 * Adds a new {@link TLReference} to the given {@link TLClass}.
	 * 
	 * @param tlClass
	 *        The {@link TLClass} to add new {@link TLReference reference} to.
	 * @param end
	 *        The {@link TLAssociationEnd} the new {@link TLReference} implements.
	 * @param name
	 *        The name of the new reference.
	 * 
	 * @return The newly created {@link TLReference}.
	 * 
	 * @throws IllegalArgumentException
	 *         if the given {@link TLClass} already contains a part with the given name, or the
	 *         given {@link TLAssociationEnd} is already implemented any {@link TLReference}.
	 */
	TLReference addReference(TLClass tlClass, String name, TLAssociationEnd end);

	/**
	 * Adds a new {@link TLAssociationEnd} to the given {@link TLAssociation}.
	 * 
	 * @param association
	 *        The {@link TLAssociation} to add {@link TLAssociationEnd end} to.
	 * @param name
	 *        The name of the new end.
	 * @param targetType
	 *        The {@link TLTypePart#getType() target type} of the new {@link TLAssociationEnd}.
	 * 
	 * @return The newly created {@link TLAssociationEnd}.
	 * 
	 * @throws IllegalArgumentException
	 *         if the given {@link TLAssociation} already contains a part with the given name.
	 */
	TLAssociationEnd addAssociationEnd(TLAssociation association, String name, TLType targetType);

}
