/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.wysiwyg.ui;

import static com.top_logic.basic.shared.collection.factory.CollectionFactoryShared.*;
import static com.top_logic.model.util.TLModelUtil.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.element.layout.meta.DefaultFormContextModificator;
import com.top_logic.element.layout.meta.FormContextModificator;
import com.top_logic.element.meta.AttributeUpdate;
import com.top_logic.element.meta.form.AttributeFormContext;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.tool.boundsec.CommandHandler;

/**
 * A {@link FormContextModificator} for changing which {@link CommandHandler} will be used for
 * opening the view of a WYSIWYG field.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class SetOpenLinkCommandModificator extends DefaultFormContextModificator
		implements ConfiguredInstance<SetOpenLinkCommandModificator.Config> {

	/**
	 * A mapping from a {@link TLType} to the {@link CommandHandler} that should be used to display
	 * an instance of that {@link TLType}.
	 */
	public interface CommandForTypeConfig extends ConfigurationItem {

		/** Property name of {@link #getCommandId()}. */
		String COMMAND_ID = "commandId";

		/** Property name of {@link #getType()}. */
		String TYPE = "type";

		/** The qualified name of the {@link TLType}. */
		@Mandatory
		@Name(TYPE)
		String getType();

		/** The id of the {@link CommandHandler}. */
		@Mandatory
		@Name(COMMAND_ID)
		String getCommandId();

	}

	/** {@link ConfigurationItem} for the {@link SetOpenLinkCommandModificator}. */
	public interface Config extends PolymorphicConfiguration<SetOpenLinkCommandModificator> {

		/** Property name of {@link #getField()}. */
		String FIELD = "field";

		/** Property name of {@link #getCommandMappings()}. */
		String COMMAND_MAPPINGS = "commandMappings";

		/** The name of the attribute, whose {@link FormField} should be modified. */
		@Name(FIELD)
		String getField();

		/**
		 * Defines a mapping from {@link TLType}s to the ids of the {@link CommandHandler} that
		 * should display instances of that type.
		 * <p>
		 * The types are matched with "instance of" against the instances, not with equality. That
		 * means, multiple entries can match against one instance. The types are checked in the
		 * given order. The first matching type wins.
		 * </p>
		 */
		@DefaultContainer
		@Name(COMMAND_MAPPINGS)
		List<CommandForTypeConfig> getCommandMappings();

	}

	private final Config _config;

	private final LinkedHashMap<TLType, String> _typeToCommand;

	/** {@link TypedConfiguration} constructor for {@link SetOpenLinkCommandModificator}. */
	public SetOpenLinkCommandModificator(InstantiationContext context, Config config) {
		_config = config;
		_typeToCommand = createTypeToCommandMap(context, config);
	}

	private LinkedHashMap<TLType, String> createTypeToCommandMap(InstantiationContext context, Config config) {
		LinkedHashMap<TLType, String> result = linkedMap();
		for (CommandForTypeConfig entry : config.getCommandMappings()) {
			result.put(resolveType(context, entry.getType()), entry.getCommandId());
		}
		return result;
	}

	private TLClass resolveType(InstantiationContext context, String qualifiedName) {
		return (TLClass) TLModelUtil.resolveQualifiedName(qualifiedName);
	}

	@Override
	public Config getConfig() {
		return _config;
	}

	@Override
	public void modify(LayoutComponent component, String attributeName, FormMember field,
			TLStructuredTypePart attribute, TLClass type, TLObject tlObject, AttributeUpdate attributeUpdate,
			AttributeFormContext formContext, FormContainer currentGroup) {
		if (Objects.equals(attributeName, getConfig().getField())) {
			OpenTLObjectLink.annotateCommandId(field, this::map);
		}
	}

	private String map(TLType type) {
		for (Entry<TLType, String> entry : _typeToCommand.entrySet()) {
			if (isCompatibleType(entry.getKey(), type)) {
				return entry.getValue();
			}
		}
		return null;
	}

}
