/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.formeditor.implementation.additional;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.element.layout.formeditor.definition.FieldDefinition;
import com.top_logic.element.layout.formeditor.implementation.FieldDefinitionTemplateProvider;
import com.top_logic.element.meta.AttributeUpdate;
import com.top_logic.element.meta.AttributeUpdate.StoreAlgorithm;
import com.top_logic.element.meta.form.AttributeFormFactory;
import com.top_logic.html.template.HTMLTemplateFragment;
import com.top_logic.knowledge.service.event.Modification;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.form.model.utility.ListOptionModel;
import com.top_logic.layout.form.model.utility.OptionModelListener;
import com.top_logic.layout.form.template.model.FormEditorElementTemplate;
import com.top_logic.model.form.implementation.FormEditorContext;
import com.top_logic.tool.boundsec.wrap.Group;

/**
 * The field showing the groups of an account.
 * 
 * <p>
 * Special handling is required, because this field displays in inverse association but should be
 * editable.
 * </p>
 */
public class GroupsField extends FieldDefinitionTemplateProvider {

	/**
	 * Creates a {@link GroupsField} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public GroupsField(InstantiationContext context, FieldDefinition config) {
		super(context, config);
	}

	@Override
	public HTMLTemplateFragment createTemplate(FormEditorContext context) {
		HTMLTemplateFragment result = super.createTemplate(context);
		if (result instanceof FormEditorElementTemplate) {
			result = ((FormEditorElementTemplate) result).getElement();
		}

		FormMember displayingField = getMember();
		((SelectField) displayingField).setOptionModel(AllGroups.INSTANCE);

		AttributeUpdate update = AttributeFormFactory.getAttributeUpdate(displayingField);
		update.setDisabled(false);
		update.setStoreAlgorithm(GroupsStorage.INSTANCE);

		return result;
	}

	private static final class AllGroups implements ListOptionModel<Group> {

		/**
		 * Singleton {@link GroupsField.AllGroups} instance.
		 */
		public static final GroupsField.AllGroups INSTANCE = new GroupsField.AllGroups();

		private AllGroups() {
			// Singleton constructor.
		}

		@Override
		public List<? extends Group> getBaseModel() {
			return Group.getAll();
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

	private static final class GroupsStorage implements StoreAlgorithm {

		/**
		 * Singleton {@link GroupsField.GroupsStorage} instance.
		 */
		public static final GroupsField.GroupsStorage INSTANCE = new GroupsStorage();

		private GroupsStorage() {
			// Singleton constructor.
		}

		@Override
		public Modification store(AttributeUpdate update) {
			Person account = (Person) update.getObject();

			@SuppressWarnings("unchecked")
			Collection<? extends Group> newGroups = (Collection<? extends Group>) update.getEditedValue();

			Set<Group> groupsBefore = new HashSet<>(account.getGroups());
			for (Group newGroup : newGroups) {
				if (!groupsBefore.remove(newGroup)) {
					newGroup.addMember(account);
				}
			}

			for (Group oldGroup : groupsBefore) {
				oldGroup.removeMember(account);
			}

			return Modification.NONE;
		}
	}

}

