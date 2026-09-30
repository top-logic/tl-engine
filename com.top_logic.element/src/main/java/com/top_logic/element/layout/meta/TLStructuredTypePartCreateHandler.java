/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.meta;

import java.util.Map;

import com.top_logic.basic.BufferingProtocol;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.copy.ConfigCopier;
import com.top_logic.basic.util.ResourceTransaction;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.element.config.AttributeConfig;
import com.top_logic.element.config.PartConfig;
import com.top_logic.element.config.ReferenceConfig;
import com.top_logic.element.config.ReferenceConfig.ReferenceKind;
import com.top_logic.element.config.annotation.TLStorage;
import com.top_logic.element.layout.meta.TLStructuredTypePartFormBuilder.EditModel;
import com.top_logic.element.layout.meta.TLStructuredTypePartFormBuilder.PartModel;
import com.top_logic.element.model.ModelResolver;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.component.AbstractCreateCommandHandler;
import com.top_logic.layout.form.values.edit.EditorFactory;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.ModelKind;
import com.top_logic.model.TLReference;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.error.TopLogicException;
import com.top_logic.util.model.ModelService;

/**
 * The {@link TLStructuredTypePartCreateHandler} handles the creation of a new {@link TLStructuredTypePart}
 * .
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TLStructuredTypePartCreateHandler extends AbstractCreateCommandHandler {

	/** The command provided by this instance. */
	public static final String	COMMAND	= "attCreate";

	/**
	 * Creates a {@link TLStructuredTypePartCreateHandler} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public TLStructuredTypePartCreateHandler(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public Object createObject(LayoutComponent component, Object createContext, FormContainer formContainer,
			Map<String, Object> arguments) {
		EditModel editModel = (EditModel) EditorFactory.getModel(formContainer);

		TLStructuredType type;

		if (createContext instanceof TLStructuredTypePart) {
			type = (TLStructuredType) ((TLStructuredTypePart) createContext).getType();
		} else {
			type = (TLStructuredType) createContext;
		}

		TLStructuredTypePart result = createAttribute(type, editModel.getPartModel());

		return result;
	}

	/**
	 * @param owner
	 *        The type to add the attribute to, must not be <code>null</code>.
	 * @param partModel
	 *        The configuration of the new attribute model of the editor.
	 * @return The newly created attribute.
	 */
	protected TLStructuredTypePart createAttribute(TLStructuredType owner, PartModel partModel) {
		BufferingProtocol log = new BufferingProtocol() {
			@Override
			protected RuntimeException createAbort() {
				return new TopLogicException(
					I18NConstants.ERROR_CREATING_ATTRIBUTE_FAILD__DETAIL.fill(StringServices.join(getErrors(), " \n")));
			}
		};
		ModelResolver resolver = new ModelResolver(log, owner.getModel(), ModelService.getInstance().getFactory());

		PartConfig partConfig;
		if (partModel.isOverride()) {
			// Determine whether to create property or reference.
			TLStructuredTypePart overriddenPart = owner.getPart(partModel.getName());
			if (overriddenPart.getModelKind() == ModelKind.REFERENCE) {
				ReferenceConfig refConfig = TypedConfiguration.newConfigItem(ReferenceConfig.class);
				if (TLModelUtil.getEndIndex(((TLReference) overriddenPart).getEnd()) == 0) {
					// backwards reference
					refConfig.setKind(ReferenceKind.BACKWARDS);
				}
				partConfig = refConfig;
			} else {
				partConfig = TypedConfiguration.newConfigItem(AttributeConfig.class);
			}
			ConfigCopier.copyContent(new DefaultInstantiationContext(log), partModel, partConfig);

			// Default-Values are not copied - even if the defaults differ.
			partConfig.setOverride(true);
		} else if (partModel.getResolvedType().getModelKind() != ModelKind.DATATYPE
			&& !ReferenceConfig.class.isAssignableFrom(partModel.getConfigurationInterface())
			&& partModel.getAnnotation(TLStorage.class) == null) {
			// Common invalid configuration with catastrophic consequences - convert to regular
			// reference.
			partConfig = TypedConfiguration.newConfigItem(ReferenceConfig.class);
			ConfigCopier.copyContent(new DefaultInstantiationContext(log), partModel, partConfig);
		} else {
			partConfig = TypedConfiguration.copy(partModel);
		}

		resolver.addPart(owner, partConfig);
		resolver.complete();

		TLStructuredTypePart newPart = owner.getPart(partModel.getName());
		try (ResourceTransaction tx = ResourcesModule.getInstance().startResourceTransaction()) {
			TLMetaModelUtil.saveI18NForPart(newPart, partModel, tx);

			tx.commit();
		}
		return newPart;
	}

}
