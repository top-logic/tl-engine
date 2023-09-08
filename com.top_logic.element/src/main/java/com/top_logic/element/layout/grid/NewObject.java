/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.grid;

import java.util.Collections;

import com.top_logic.dob.meta.MOStructure;
import com.top_logic.element.meta.gui.DefaultCreateAttributedComponent;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.component.CreateFunction;
import com.top_logic.layout.scripting.recorder.ref.ValueNamingScheme;
import com.top_logic.layout.scripting.recorder.ref.value.SingletonValueNamingScheme;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLNamed;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TransientObject;
import com.top_logic.model.factory.TLFactory;
import com.top_logic.model.impl.generated.TLObjectBase;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.util.Resources;
import com.top_logic.util.error.TopLogicException;

/**
 * Placeholder for the request of creating a new object.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class NewObject extends TransientObject implements TLNamed {

	private String typeName;

	private final TLClass _type;

	private final TLObject _container;

	private final CreateFunction createHandler;

	private Object _createContext;

	/**
	 * Creates a {@link NewObject}.
	 * 
	 * @param typeName
	 *        See {@link #getTypeName()}.
	 * @param type
	 *        See {@link #tType()}.
	 * @param createHandler
	 *        The {@link CommandHandler} that is invoked to finally create the persistent object.
	 * @param container
	 *        See {@link #getContainer()}.
	 * @param createContext
	 *        The create context ("parent" object) of the new object to create.
	 */
	public NewObject(String typeName, TLClass type, CreateFunction createHandler, TLObject container,
			Object createContext) {
		this.typeName = typeName;
		this._type = type;
		this.createHandler = createHandler;
		_container = container;
		_createContext = createContext;
	}

	@Override
	public String getName() {
		return Resources.getInstance().getString(I18NConstants.NEW_OBJECT_MARKER);
	}

	@Override
	public TLObject tContainer() {
		return _container;
	}

	@Override
	public TLStructuredType tType() {
		return _type;
	}

	@Override
	public MOStructure tTable() {
		return TLModelUtil.getTable(_type);
	}

	@Override
	public KnowledgeBase tKnowledgeBase() {
		return _type.tKnowledgeBase();
	}

	@Override
	public Object tValue(TLStructuredTypePart part) {
		if (TLObjectBase.T_TYPE_ATTR.equals(part.getName())) {
			return _type;
		}

		return super.tValue(part);
	}

	@Override
	public Object tGetData(String property) {
		return null;
	}

	/**
	 * Actually creates the persistent object from this transient {@link NewObject} template.
	 * 
	 * @param component
	 *        The context component.
	 * @param formContext
	 *        The form elements in the current row.
	 * @return The newly created persistent object.
	 */
	public Object create(LayoutComponent component, FormContainer formContext) {
		try {
			return createHandler.createObject(component, getCreateContext(), formContext,
				Collections.singletonMap(DefaultCreateAttributedComponent.ELEMENT_TYPE, this._type));
		} catch (RuntimeException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new TopLogicException(NewObject.class, "creationFailed", ex);
		}
	}

	/**
	 * The element type name of the new object to create.
	 * 
	 * @see TLFactory#createObject(TLClass, TLObject, com.top_logic.knowledge.wrap.ValueProvider)
	 */
	public String getTypeName() {
		return typeName;
	}
	
	/**
	 * The container of the new object to create.
	 */
	public TLObject getContainer() {
		return _container;
	}

	/**
	 * The create context ("parent" object) of the new object to create.
	 */
	public Object getCreateContext() {
		return _createContext;
	}

	/**
	 * {@link ValueNamingScheme} for {@link NewObject}s.
	 */
	public static class Naming extends SingletonValueNamingScheme<NewObject> {

		@Override
		public Class<NewObject> getModelClass() {
			return NewObject.class;
		}

	}

}
