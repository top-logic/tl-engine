/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.kbbased;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.col.NameValueBuffer;
import com.top_logic.basic.col.NameValueBuilder;
import com.top_logic.basic.module.BasicRuntimeModule;
import com.top_logic.basic.module.ManagedClass;
import com.top_logic.basic.module.ModuleException;
import com.top_logic.basic.module.ServiceDependencies;
import com.top_logic.dob.DataObjectException;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.element.core.CreateElementException;
import com.top_logic.element.meta.AttributeOperations;
import com.top_logic.element.meta.MetaElementFactory;
import com.top_logic.element.meta.MetaElementHolder;
import com.top_logic.element.meta.MetaElementUtil;
import com.top_logic.element.model.ModelFactory;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.KnowledgeBaseException;
import com.top_logic.knowledge.service.KnowledgeBaseRuntimeException;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.service.db2.PersistentObject;
import com.top_logic.knowledge.wrap.ValueProvider;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.knowledge.wrap.WrapperFactory;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLScope;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.annotate.util.TLAnnotations;
import com.top_logic.model.util.TLModelUtil;

/**
 * Abstract superclass for wrapper factories.
 *
 * TODO KHA/KBU auto register subclasses with/via the WrapperFactory.
 *
 * @author <a href=mailto:fma@top-logic.com>Dr. Frank Mausz</a>
 */
public abstract class AbstractWrapperResolver extends ModelFactory {

    /**
	 * The type name of the given item.
	 */
	protected String getDynamicTypeSuffix(KnowledgeItem item) {
		return item.getWrapper().tType().getName();
	}

	@Override
	public TLObject createObject(TLClass type, TLObject context, ValueProvider initialValues) {
		failIfAbstract(type);

		String tableType = TLAnnotations.getTable(type);
		KnowledgeBase kb = getKnowledgeBase();
		NameValueBuffer createValues = new NameValueBuffer();
		buildInitialValues(createValues, tableType, type);
		KnowledgeObject item = kb.createKnowledgeObject(tableType, createValues);

		TLObject result = WrapperFactory.getWrapper(item);
		if (result != null) {
			if (result instanceof TLScope) {
				MetaElementFactory typeFactory = MetaElementFactory.getInstance();
				typeFactory.setupLocalScope(getModule(), (TLScope) result, type.getName());
			}

			setupDefaultValues(context, result, type);
		}

		return result;
	}

	/**
	 * Return the default knowledgebase to be used for elements.
	 * 
	 * @return The requested knowledgebase.
	 */
    protected KnowledgeBase getKnowledgeBase() {
        return PersistencyLayer.getKnowledgeBase();
    }
    
    /**
	 * Creates a new AttributedWarpper of the given type
	 * 
	 * @param anElementName
	 *        the type of wrapper to create. May not be null or empty
	 * @return a new AttributedWrapper
	 * @throws CreateElementException
	 *         if the wrapper can not be created
	 * 
	 * @deprecated Use {@link #createObject(TLClass)}
	 */
	@Deprecated
	public final AttributedWrapper createNewWrapper(String anElementName) throws CreateElementException {
		return (AttributedWrapper) createObject(type(anElementName));
    }

	/**
	 * Creates a new AttributedWarpper of the given type
	 * 
	 * @param anElementName
	 *        the type of wrapper to create. May not be null or empty
	 * @param initialValues
	 *        Initial values for the wrapper. The buffer is modified by this method. See
	 *        {@link #buildInitialValues(NameValueBuilder, String, TLClass)}
	 * @return a new AttributedWrapper
	 * 
	 * @throws CreateElementException
	 *         if the wrapper can not be created
	 * 
	 * @deprecated Implement {@link #createObject(TLClass, TLObject, ValueProvider)}
	 */
	@Deprecated
	protected final AttributedWrapper createNewWrapper(String anElementName, NameValueBuffer initialValues) {
		return (AttributedWrapper) createObject(type(anElementName));
	}

	/**
	 * Resolves the type for the given "element name".
	 */
	@Deprecated
	public TLClass type(String elementName) {
		if (StringServices.isEmpty(elementName)) {
            throw new IllegalArgumentException("Given type is null or empty (in createNewWrapper)");
        }      
		MetaElementFactory typeFactory = MetaElementFactory.getInstance();
		return typeFactory.findType(null, getModuleName(), elementName);
	}

    /**
	 * Create a new attributed wrapper out of the given parameters.
	 * 
	 * @param aHolder
	 *        The holder of the type to be used, must not be <code>null</code>.
	 * @param aMEType
	 *        The name of the type to be assigned to the new created object, must not be empty or
	 *        <code>null</code>.
	 * @param anElementName
	 *        The name of the element type as configured, must not be empty or <code>null</code>.
	 * @return The newly created wrapper, never <code>null</code>.
	 * @throws CreateElementException
	 *         If creation fails for a reason.
	 * 
	 * @deprecated Use {@link #createObject(TLClass, com.top_logic.model.TLObject)}
	 */
	@Deprecated
	public final AttributedWrapper createNewWrapper(MetaElementHolder aHolder, String aMEType, String anElementName)
			throws CreateElementException {
        if (aHolder == null) {
			throw new IllegalArgumentException("Given type holder is null (in createNewWrapper)");
        }
        else if (StringServices.isEmpty(aMEType)) {
			throw new IllegalArgumentException("Given type type is null or empty (in createNewWrapper)");
        }
        else if (StringServices.isEmpty(anElementName)) {
            throw new IllegalArgumentException("Given element type is null or empty (in createNewWrapper)");
        }


		String theKOType = null;
        try {
			TLClass type = aHolder.getMetaElement(aMEType);
			if (type == null) {
				throw new CreateElementException("Unable to create element, because defined type '" +
					aMEType + "' not found in holder '" +
					aHolder + "'!");
			}
			theKOType = TLAnnotations.getTable(type);
			AttributedWrapper theWrapper = getNewWrapper(theKOType, type);

            if (theWrapper != null) {
				{
					setupDefaultValues(theWrapper, type);
                }
            }

            return theWrapper;
           
        }
        catch (CreateElementException ex) {
            throw ex;
        }        
        catch (Exception ex) {
            throw new CreateElementException("Unable to create element ('" + 
                                             theKOType + "')", ex);
        }        
    }

    /** 
     * Get the ME type from configuration.
     * 
     * @param    anElementName    The element type to get the KO type for.
     * @return   The requested ME type, never <code>null</code>.
     * @throws   IllegalArgumentException    If there is no configuration for the given type.
     */
    public String getMEType(String anElementName) throws IllegalArgumentException {
		return anElementName;
    }

    /**
	 * Creates and returns a new wrapper for the given KOType and type
	 * 
	 * @param staticTypeName
	 *        the type of the KO to use
	 * @param type
	 *        the type of the wrapper
	 * @return the new Wrapper
	 */
	protected AttributedWrapper getNewWrapper(String staticTypeName, TLClass type) throws DataObjectException {
		return getNewWrapper(staticTypeName, type, new NameValueBuffer());
	}

	/**
	 * Creates and returns a new wrapper for the given KOType and type
	 * 
	 * @param staticTypeName
	 *        the type of the KO to use
	 * @param type
	 *        the type of the wrapper
	 * @param initialValues
	 *        Initial values for the wrapper. The buffer is modified by this method. See
	 *        {@link #buildInitialValues(NameValueBuilder, String, TLClass)}
	 * @return the new Wrapper
	 * 
	 * @deprecated Implement {@link #createObject(TLClass, TLObject, ValueProvider)}
	 */
	@Deprecated
	protected AttributedWrapper getNewWrapper(String staticTypeName, TLClass type, NameValueBuffer initialValues)
			throws DataObjectException {
		failIfAbstract(type);
		KnowledgeBase   theKBase  = this.getKnowledgeBase();
		buildInitialValues(initialValues, staticTypeName, type);
		KnowledgeObject theObject =
			theKBase.createKnowledgeObject(staticTypeName, initialValues);

		return (AttributedWrapper) WrapperFactory.getWrapper(theObject);
	}

	/**
	 * Produces a set of initial values to construct the new item with.
	 * 
	 * <p>
	 * The initial values must be sufficient to answer {@link #getDynamicTypeSuffix(KnowledgeItem)}
	 * on the newly created item.
	 * </p>
	 * 
	 * @param initialValues
	 *        Buffer with name/value pairs.
	 * @param staticTypeName
	 *        The name of the {@link MOClass} in which the node is created.
	 * @param type
	 *        The structure node name of the created node.
	 * @return The built initial values.
	 * 
	 */
	protected NameValueBuilder buildInitialValues(NameValueBuilder initialValues, String staticTypeName, TLClass type) {
		return initialValues.setValue(PersistentObject.TYPE_REF, type.tHandle());
    }

    /**
	 * Returns a collection of all objects of type aMetaelementName which have aTargetObject as
	 * value in their attribute anAttributeName.
	 * 
	 * @param moduleName
	 *        The module that defines the given type.
	 * @param aMetaElementName
	 *        the name of the type
	 * @param anAttributeName
	 *        the name of the attribute
	 * @param aTargetObject
	 *        the target object
	 * @return collection with all objects pointing to the target
	 * @throws IllegalArgumentException
	 *         if
	 *         <ul>
	 *         <li>aMetaElementName is null or empty
	 *         <li>no type is found for the aMetaElementName
	 *         <li>anAttributeName is null or empty
	 *         <li>aTargetWrapper is null
	 *         </ul>
	 * @throws IllegalStateException
	 *         if no attribute with anAttributeName exists
	 */
    protected Collection getCollection(String moduleName, String aMetaElementName, String anAttributeName, Object aTargetObject) throws IllegalArgumentException, IllegalStateException {
        if(!(aTargetObject instanceof Wrapper)){
            throw new IllegalArgumentException("Target must be a non null wrapper");
        }
    
        TLClass theME = MetaElementFactory.getInstance().getGlobalMetaElement(aMetaElementName);
        try {
			TLStructuredTypePart theAtt = MetaElementUtil.getMetaAttribute(theME, anAttributeName);
            return AttributeOperations.getReferers((Wrapper)aTargetObject, theAtt);
            // return theAtt.getWrappersWithValue((Wrapper)aTargetObject);
        } catch (NoSuchAttributeException e) {
            throw new IllegalStateException("Failed to get attribute");
        }       
    }

    // ------------

    /**
     * Gets all wrapper with the given element type.
     *
     * @param aElementType
     *        the element type to get all wrappers for
     * @return a list of wrapper for all element with the given type.
     */
    public List getAllWrappersFor(String aElementType) {
        assert aElementType != null : "aElementType must not be null!";
		TLClass type = (TLClass) TLModelUtil.findType(getModuleName() + ":" + aElementType);
		return MetaElementUtil.getAllDirectInstancesOf(type);
    }
    
	public abstract static class WrapperResolverModule<E extends ManagedClass> extends BasicRuntimeModule<E> {

	    // Moved dependencies declaration to annotation section to make
	    // dependency declaration compatible with ConfiguredRuntimeModule.
        public static final List<Class<? extends BasicRuntimeModule<?>>> DEPENDENCIES =
            Arrays.asList(AbstractWrapperResolver.class.getAnnotation(ServiceDependencies.class).value());

		@Override
		public Collection<? extends Class<? extends BasicRuntimeModule<?>>> getDependencies() {
			return DEPENDENCIES;
		}

		@Override
		protected final E newImplementationInstance() throws ModuleException {
			Transaction tx =
				PersistencyLayer.getKnowledgeBase().beginTransaction(
					Messages.START_UP_WRAPPER_RESOLVER__IMPLCLASS.fill(getImplementation()));
			E impl = createImplementation();
			try {
				tx.commit();
			} catch (KnowledgeBaseException ex) {
				StringBuilder error = new StringBuilder();
				error.append("Unable to commit changes done when constructing implementation instance for module: ");
				error.append(WrapperResolverModule.this);
				throw new KnowledgeBaseRuntimeException(error.toString(), ex);
			}
			return impl;
		}

		protected abstract E createImplementation();
		
	}

}
