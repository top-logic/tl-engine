/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta;

import com.top_logic.basic.TLID;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.module.ManagedClass;
import com.top_logic.basic.module.ServiceDependencies;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.element.meta.kbbased.filtergen.AttributeValueLocatorFactory;
import com.top_logic.element.meta.kbbased.filtergen.FilterFactory;
import com.top_logic.element.meta.kbbased.filtergen.GeneratorFactory;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.model.TLAssociationEnd;
import com.top_logic.model.TLAssociationProperty;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLClassProperty;
import com.top_logic.model.TLReference;
import com.top_logic.model.TLStructuredTypePart;

/**
 * Create/remove MetaAttributes in/from MetaElements.
 * 
 * @author    <a href="mailto:kbu@top-logic.com">Karsten Buch</a>
 */
@ServiceDependencies({
	AttributeSettings.Module.class,
	PersistencyLayer.Module.class, 
	FilterFactory.Module.class,
	GeneratorFactory.Module.class,
	AttributeValueLocatorFactory.Module.class})
public abstract class MetaAttributeFactory extends ManagedClass {

	private final Config _config;

	/**
	 * Configuration options for {@link MetaAttributeFactory}.
	 */
	public interface Config extends ServiceConfiguration<MetaAttributeFactory> {

	}

	/**
	 * Creates a {@link MetaAttributeFactory} from configuration.
	 */
	public MetaAttributeFactory(InstantiationContext context, Config config) {
		super(context, config);

		_config = config;
	}

	/**
	 * Get the single instance
	 * 
	 * @return the single instance
	 */
	public static MetaAttributeFactory getInstance () {
		return Module.INSTANCE.getImplementationInstance();
    }

	/** 
	 * Get ME by identifier
	 * 
	 * @return the ME
	 */
	public abstract TLStructuredTypePart getMetaAttribute(TLID anIdentifier);
	
	/**
	 * Creates a new {@link TLAssociationEnd} in the given {@link KnowledgeBase}
	 * 
	 * @param kb
	 *        The {@link KnowledgeBase} to create end in.
	 * 
	 * @return The new {@link TLAssociationEnd}.
	 */
	public abstract TLAssociationEnd createEnd(KnowledgeBase kb);

	/**
	 * Creates a new {@link TLReference} in the given {@link KnowledgeBase} implementing the given
	 * {@link TLAssociationEnd}.
	 * 
	 * @param kb
	 *        The {@link KnowledgeBase} to create end in.
	 * @param end
	 *        The {@link TLAssociationEnd} such that the returned {@link TLReference}
	 *        <code>ref</code> fulfills <code>ref.getEnd() == end</code>
	 * 
	 * @return The new {@link TLReference}.
	 */
	public abstract TLReference createTLReference(KnowledgeBase kb, TLAssociationEnd end);

	/**
	 * Creates a new {@link TLClassProperty} in the given {@link KnowledgeBase}
	 * 
	 * @param kb
	 *        The {@link KnowledgeBase} to create property in.
	 * 
	 * @return The new {@link TLClassProperty}.
	 */
	public abstract TLClassProperty createClassProperty(KnowledgeBase kb);

	/**
	 * Creates a new {@link TLAssociationProperty} in the given {@link KnowledgeBase}
	 * 
	 * @param kb
	 *        The {@link KnowledgeBase} to create property in.
	 * 
	 * @return The new {@link TLAssociationProperty}.
	 */
	public abstract TLAssociationProperty createAssocationProperty(KnowledgeBase kb);

	/**
	 * Remove a attribute from a type.
	 * 
	 * @param aMetaElement
	 *        the type. Must not be <code>null</code>
	 * @param aMetaAttribute
	 *        the attribute. Must not be <code>null</code>
	 * @throws NoSuchAttributeException
	 *         if aMetaAttribute is not an attribute of aMetaElement
	 * @throws IllegalArgumentException
	 *         if one of the params is <code>null</code>
	 */
	public void removeMetaAttribute (TLClass aMetaElement, TLStructuredTypePart aMetaAttribute) 
			throws NoSuchAttributeException, IllegalArgumentException {
		// Check params
		if ((aMetaAttribute == null) || (aMetaElement == null)) {
			throw new IllegalArgumentException("Type and attribute must not be null.");
		}
		
		MetaElementUtil.removeMetaAttribute(aMetaElement, aMetaAttribute);
	}
	
	/**
	 * Singleton holder for the {@link MetaAttributeFactory}.
	 */
    public static final class Module extends TypedRuntimeModule<MetaAttributeFactory> {

		/**
		 * Singleton {@link MetaAttributeFactory.Module} instance.
		 */
		public static final Module INSTANCE = new Module();

		private Module() {
			// Singleton constructor.
		}

		@Override
		public Class<MetaAttributeFactory> getImplementation() {
			return MetaAttributeFactory.class;
		}
		
	}


}
