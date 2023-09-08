/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.gui;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.element.meta.AttributeUpdate;
import com.top_logic.element.meta.form.AttributeFormContext;
import com.top_logic.element.meta.form.AttributeFormFactory;
import com.top_logic.element.meta.form.overlay.TLFormObject;
import com.top_logic.element.model.DynamicModelService;
import com.top_logic.element.model.ModelFactory;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.component.AbstractCreateCommandHandler;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;
import com.top_logic.model.util.TLModelUtil;


/**
 * Extended handler for create commands with {@link com.top_logic.knowledge.wrap.Wrapper}.
 * 
 * <p>
 * Additionally to the {@link AbstractCreateCommandHandler}, this class allows the automatic storage
 * of values from the {@link com.top_logic.knowledge.wrap.Wrapper} objects via
 * {@link #saveMetaAttributes(Map, Wrapper)}. This will only store the values defined in the
 * {@link com.top_logic.model.TLClass}.
 * </p>
 * 
 * @author <a href="mailto:mga@top-logic.com">Michael G&auml;nsler</a>
 */
public abstract class AbstractCreateAttributedCommandHandler extends AbstractCreateCommandHandler {

    /** 
     * Creates a {@link AbstractCreateAttributedCommandHandler}.
     */
    public AbstractCreateAttributedCommandHandler(InstantiationContext context, Config config) {
        super(context, config);
    }

    /** 
     * Create a new attributed object out of the given information.
     * 
     * This method didn't need to store more than the minimum information, all information will
     * be placed in the new created object {@link #saveMetaAttributes(Map, Wrapper) later}.
     * 
     * @param    someValues    The meta attribute values as found in the form context.
     * @param    aModel        The model of the calling component.
     * @return   The new created attribute, never <code>null</code>.
     * @see      #createNewObjectFromMap(Map, Wrapper)
     */
    public abstract Wrapper createNewObject(Map<String, Object> someValues, Wrapper aModel);

    /**
     * Create a new attributed out of the given form context.
     * 
     * Therefore this method will do three things:
     * <ol>
     *  <li>Extract all meta element relevant information from given form context.</li>
     *  <li>Call the abstract method {@link #createNewObject(Map, Wrapper)}.</li>
     *  <li>Store the rest of meta element related information to the new created attributed.</li>
     * </ol>
     * 
     * @see com.top_logic.layout.form.component.AbstractCreateCommandHandler#createObject(LayoutComponent, java.lang.Object, FormContainer, Map)
     */
    @Override
	public Object createObject(LayoutComponent component, Object createContext, FormContainer formContainer, Map<String, Object> arguments) {
		Wrapper theModel = (Wrapper) getContainer(formContainer, createContext);
        Map<String,Object> theMap    = this.extractValues(formContainer, theModel);
        Wrapper         theResult = this.createNewObjectFromMap(theMap, theModel);

        return theResult;
    }

	/**
	 * Looks up the container for the new object to create.
	 */
	protected TLObject getContainer(FormContainer formContainer, Object createContext) {
		AttributeFormContext formContext = formContext(formContainer);
		if (formContext == null) {
			// Do the best for legacy compatibility.
			return (TLObject) createContext;
		}
		TLFormObject overlay = formContext.getAttributeUpdateContainer().getOverlay(null, null);
		TLObject container = overlay.tContainer();
		if (container == null) {
			// Do the best for legacy compatibility.
			return (TLObject) createContext;
		}
		return container;
	}

	private AttributeFormContext formContext(FormContainer form) {
		while (!(form instanceof AttributeFormContext)) {
			if (form == null) {
				return null;
			}
			form = form.getParent();
		}
		return (AttributeFormContext) form;
	}

    /**
	 * Create a new attributed object out of the given information.
	 * 
	 * This method will store all information found in the given map.
	 * 
	 * @param aMap
	 *        The meta attribute values as found in the form context.
	 * @param aModel
	 *        The model of the calling component.
	 * @return The new created attribute, never <code>null</code>.
	 * @see #createNewObject(Map, Wrapper)
	 * @see #saveMetaAttributes(Map, Wrapper)
	 */
    public Wrapper createNewObjectFromMap(Map<String,Object> aMap, Wrapper aModel) {
        Wrapper theResult = this.createNewObject(aMap, aModel);

        this.saveMetaAttributes(aMap, theResult);

        return theResult;
    }

    /**
     * Store the changes done to the {@link com.top_logic.knowledge.wrap.Wrapper attributed} values.
     * 
     * @param    formContainer    The context holding all needed information for update, may be <code>null</code>.
     * @return   Flag, if updating has taken place (will not happen, if given context is not instance of
     *           {@link AttributeFormContext}).
     */
    protected Map<String,Object> extractValues(FormContainer formContainer, Wrapper anAttributed) {
        Map<String,Object> theMap = new HashMap<>();

        for (Iterator theIt = formContainer.getFields(); theIt.hasNext();) {
            FormField       theField  = (FormField) theIt.next();
			AttributeUpdate theUpdate = AttributeFormFactory.getAttributeUpdate(theField);

            if (theUpdate != null) {
                TLStructuredTypePart theMA    = theUpdate.getAttribute();
                Object        theValue = theUpdate.getCorrectValues();

                theMap.put(theMA.getName(), theValue);
            }
            else {
                theMap.put(theField.getName(), theField.getValue());
            }
        }

        return theMap;
    }

    /**
     * Store the changes done to the {@link com.top_logic.knowledge.wrap.Wrapper attributed} values.
     *
     * @return   Flag, if updating has taken place (will not happen, if given context is not instance of
     *           {@link AttributeFormContext}).
     */
    protected boolean saveMetaAttributes(Map<String, Object> someValues, Wrapper anAttributed) {
        boolean hasChanged = false;

		TLStructuredType type = anAttributed.tType();
		for (Entry<String, Object> entry : someValues.entrySet()) {
			String key = entry.getKey();
			TLStructuredTypePart part = type.getPart(key);
			if (part == null) {
				continue;
			}
			if (TLModelUtil.isDerived(part)) {
				continue;
			}
			Object theValue = entry.getValue();
			anAttributed.setValue(key, theValue);
			hasChanged = true;
        }

        return (hasChanged);
    }

	/**
	 * Return the factory to be used for creating the new object.
	 * 
	 * @param type
	 *        Is not allowed to be null.
	 * @return Null, if there is not factory registered for the given type.
	 */
	protected final ModelFactory getFactory(TLType type) {
		return getFactory(type.getModule().getName());
	}

	/**
	 * Return the factory to be used for creating the new object.
	 * 
	 * @param module
	 *        Name of the requested module.
	 * @return Null, if there is not factory registered for the given module name.
	 */
	protected final ModelFactory getFactory(String module) {
		return DynamicModelService.getFactoryFor(module);
	}

}
