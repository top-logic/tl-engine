/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.gui;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.element.layout.create.CreateFormBuilder;
import com.top_logic.element.layout.structured.StructuredElementCreateComponent;
import com.top_logic.element.meta.form.AttributeFormContext;
import com.top_logic.element.meta.kbbased.AbstractWrapperResolver;
import com.top_logic.element.model.util.TypeReferenceConfig;
import com.top_logic.element.structured.StructuredElement;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.ValueListener;
import com.top_logic.layout.form.component.AbstractCreateComponent;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.HiddenField;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.model.TLClass;
import com.top_logic.model.util.TLModelUtil;

/**
 * Default component to create an {@link Wrapper} object which is <b>NO</b>
 * {@link StructuredElement}.
 * 
 * @author <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 * 
 * @deprecated Use {@link AbstractCreateComponent} with {@link CreateFormBuilder}
 */
@Deprecated
public class DefaultCreateAttributedComponent extends CreateAttributedComponent {

	/**
	 * Relevant information for creating an {@link Wrapper}. 
	 * 
	 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
	 */
	public interface Config extends CreateAttributedComponent.Config, TypeReferenceConfig {

		// pure sume interface

	}

	/** Meta element type of the created attributed. */
	public static final String ELEMENT_TYPE = StructuredElementCreateComponent.ELEMENT_TYPE;

	/** Image field matching to the selected meta element type. */
	public static final String IMAGE_TYPE_FIELD = StructuredElementCreateComponent.IMAGE_TYPE_FIELD;

	static final Property<String> STRUCT_NAME = TypedAnnotatable.property(String.class, "_dottedName");

	private TLClass _metaElement;

	private AbstractWrapperResolver _factory;

	/**
	 * Creates a new {@link DefaultCreateAttributedComponent} from the given configuration.
	 * 
	 * @param context
	 *        {@link InstantiationContext} to instantiate sub configurations.
	 * @param config
	 *        Configuration for this {@link DefaultCreateAttributedComponent}.
	 * 
	 * @throws ConfigurationException
	 *         iff configuration is invalid.
	 */
	public DefaultCreateAttributedComponent(InstantiationContext context, Config config) throws ConfigurationException {
		super(context, config);

		_metaElement = TypeReferenceConfig.Resolver.getMetaElement(config);
		_factory = (AbstractWrapperResolver) TypeReferenceConfig.Resolver.getFactory(_metaElement);
	}

	@Override
	public Config getConfig() {
		return (Config) super.getConfig();
	}

	@Override
	public TLClass getMetaElement() {
		return _metaElement;
	}

	@Override
	protected void addMoreAttributes(TLClass aME, AttributeFormContext aContext) {
		super.addMoreAttributes(aME, aContext);

		FormField typeField = addMetaElementField(aContext);
		addImageTypeField(aContext, typeField);
	}

	/**
	 * Return a field which contains the information about the meta element to be created.
	 * 
	 * <p>
	 * In this implementation this method returns a hidden field containing the value from
	 * {@link #getMetaElement()}, other implementations may use a {@link SelectField} for allowing
	 * the user to select different kind of elements for creation.
	 * </p>
	 * 
	 * @return The field containing the meta element to be created.
	 */
	@SuppressWarnings("deprecation")
	protected FormField createMetaElementField(String aName) {
		HiddenField theField = FormFactory.newHiddenField(aName, getMetaElement());
		String theStruct = getConfig().getStructure();

		if (!theStruct.isEmpty()) {
			String theElement = getConfig().getElement();

			theField.set(STRUCT_NAME, TLModelUtil.qualifiedNameDotted(theStruct, theElement));
		}

		return theField;
	}

	/**
	 * Return the factory to be used for creating the {@link Wrapper}.
	 * 
	 * @return The requested factory.
	 */
	protected AbstractWrapperResolver getFactory() {
		return _factory;
	}

	private FormField addMetaElementField(FormContext context) {
		FormField field = createMetaElementField(DefaultCreateAttributedComponent.ELEMENT_TYPE);
		context.addMember(field);
		return field;
	}

	private void addImageTypeField(FormContext context, FormField typeField) {
		final HiddenField imageField = FormFactory.newHiddenField(DefaultCreateAttributedComponent.IMAGE_TYPE_FIELD);

		imageField.setVisible(false);
		imageField.setInheritDeactivation(false);

		ValueListener listener = new ValueListener() {
			@Override
			public void valueChanged(FormField aField, Object oldValue, Object newValue) {
				TLClass type = (TLClass) CollectionUtil.getSingleValueFrom(newValue);

				if (type == null) {
					imageField.setValue(null);
				} else {
					imageField.setValue(type);
				}
			}
		};
		typeField.addValueListener(listener);
		// Initialize.
		listener.valueChanged(typeField, null, typeField.getValue());

		context.addMember(imageField);
	}

}
