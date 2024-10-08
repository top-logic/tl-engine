/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.formeditor.accounts;

import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.element.layout.formeditor.implementation.additional.I18NConstants;
import com.top_logic.element.meta.AttributeUpdate;
import com.top_logic.element.meta.form.AttributeFormFactory;
import com.top_logic.html.template.HTMLTemplateFragment;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.form.model.utility.ListOptionModel;
import com.top_logic.layout.form.model.utility.OptionModelListener;
import com.top_logic.layout.form.template.model.FormEditorElementTemplate;
import com.top_logic.layout.formeditor.parts.ForeignAttributeDefinition;
import com.top_logic.layout.formeditor.parts.ForeignAttributeTemplateProvider;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.form.implementation.FormEditorContext;
import com.top_logic.tool.boundsec.wrap.Group;
import com.top_logic.util.Resources;

/**
 * The field showing the members of the representative groups of an account.
 * 
 * <p>
 * Special handling is required, because this foreign field requires a special label and special
 * options.
 * </p>
 */
public class RepresentativeField extends ForeignAttributeTemplateProvider {

	/**
	 * Creates a {@link RepresentativeField} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public RepresentativeField(InstantiationContext context, ForeignAttributeDefinition config) {
		super(context, config);
	}

	@Override
	public HTMLTemplateFragment createTemplate(FormEditorContext context) {
		HTMLTemplateFragment result = super.createTemplate(context);
		if (result instanceof FormEditorElementTemplate) {
			result = ((FormEditorElementTemplate) result).getElement();
		}

		FormMember displayingField = getDelegate().getMember();
		((SelectField) displayingField).setOptionModel(AllRepresentativeGroups.INSTANCE);

		AttributeUpdate update = AttributeFormFactory.getAttributeUpdate(displayingField);
		update.setDisabled(false);
		
		displayingField.setLabel(Resources.getInstance().getString(I18NConstants.REPRESENTATIVES));
		displayingField.setTooltip(Resources.getInstance().getString(I18NConstants.REPRESENTATIVES.tooltip()));

		return result;
	}

	@Override
	public boolean getWholeLine(TLStructuredType modelType) {
		return false;
	}

	private static final class AllRepresentativeGroups implements ListOptionModel<Group> {

		/**
		 * Singleton {@link RepresentativeField.AllRepresentativeGroups} instance.
		 */
		public static final RepresentativeField.AllRepresentativeGroups INSTANCE =
			new RepresentativeField.AllRepresentativeGroups();

		private AllRepresentativeGroups() {
			// Singleton constructor.
		}

		@Override
		public List<? extends Group> getBaseModel() {
			return Person.all().stream().map(account -> account.getRepresentativeGroup()).toList();
		}

		@Override
		public boolean addOptionListener(OptionModelListener listener) {
			return false;
		}

		@Override
		public boolean removeOptionListener(OptionModelListener listener) {
			return false;
		}
	}

}

