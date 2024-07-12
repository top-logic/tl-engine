/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.diagramjs.server.util.model;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.layout.form.declarative.DeclarativeFormBuilder;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.model.ModelKind;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLEnumeration;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLType;
import com.top_logic.model.config.TLModelPartMapping;
import com.top_logic.model.util.AllTypes;
import com.top_logic.model.util.TLModelUtil;

/**
 * {@link DeclarativeFormBuilder} for a {@link TLInheritance}.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class TLInheritanceFormBuilder
		extends DeclarativeFormBuilder<TLInheritance, TLInheritanceFormBuilder.EditModel> {

	/**
	 * Creates a {@link TLInheritanceFormBuilder} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	public TLInheritanceFormBuilder(InstantiationContext context, Config config) {
		super(context, config);
	}

	/**
	 * Base edit properties of all {@link TLInheritance} configurations.
	 *
	 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
	 */
	@DisplayOrder(
	{ EditModel.GENERALIZATION_NAME, EditModel.SPECIALIZATION_NAME }
	)
	public interface EditModel extends ConfigurationItem {

		/**
		 * Name for {@link #getSpecialization()} property.
		 */
		static String SPECIALIZATION_NAME = "specialization";
		
		/**
		 * Name for {@link #getGeneralization()} property.
		 */
		static String GENERALIZATION_NAME = "generalization";
		
		/**
		 * Source node.
		 */
		@Options(fun = AllReferenceTypes.class, mapping = TLModelPartMapping.class)
		@Name(SPECIALIZATION_NAME)
		String getSpecialization();

		/**
		 * @see #getSpecialization()
		 */
		void setSpecialization(String sourceType);

		/**
		 * Target node.
		 */
		@Options(fun = AllReferenceTypes.class, mapping = TLModelPartMapping.class)
		@Name(GENERALIZATION_NAME)
		String getGeneralization();

		/**
		 * @see #getGeneralization()
		 */
		void setGeneralization(String targetType);

		/**
		 * {@link com.top_logic.model.util.AllTypes} reduced to only {@link TLClass
		 * classes} and {@link TLEnumeration}s.
		 */
		class AllReferenceTypes extends AllTypes {
			@Override
			protected TypesTree tree() {
				return new ReferenceTypesTree();
			}

			/**
			 * {@link com.top_logic.model.util.AllTypes.TypesTree} accepting only {@link TLClass}
			 * and {@link TLEnumeration}.
			 * 
			 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
			 */
			protected static class ReferenceTypesTree extends TypesTree {
				@Override
				protected boolean acceptType(TLModule module, TLType type) {
					ModelKind kind = type.getModelKind();
					if (kind != ModelKind.CLASS && kind != ModelKind.ENUMERATION) {
						return false;
					}
					return super.acceptType(module, type);
				}
			}
		}
	}

	@Override
	protected Class<? extends TLInheritance> getModelType() {
		return TLInheritance.class;
	}

	@Override
	protected Class<? extends EditModel> getFormType(Object contextModel) {
		return EditModel.class;
	}

	@Override
	protected void fillFormModel(EditModel formModel, TLInheritance businessModel) {
		formModel.setSpecialization(TLModelUtil.qualifiedName(businessModel.getSpecialization()));
		formModel.setGeneralization(TLModelUtil.qualifiedName(businessModel.getGeneralization()));
	}

}
