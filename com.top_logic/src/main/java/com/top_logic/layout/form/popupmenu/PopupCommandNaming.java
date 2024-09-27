/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.popupmenu;

import com.top_logic.basic.col.Maybe;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable.Property;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.basic.CommandModelAdapter;
import com.top_logic.layout.basic.PopupMenuModel;
import com.top_logic.layout.basic.contextmenu.menu.Menu;
import com.top_logic.layout.scripting.recorder.ref.GlobalModelNamingScheme;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.ModelNamingScheme;
import com.top_logic.layout.scripting.recorder.ref.ModelResolver;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.layout.scripting.runtime.action.ApplicationAssertions;
import com.top_logic.util.Resources;

/**
 * {@link ModelNamingScheme} that is capable of finding a {@link CommandModel} in a
 * {@link PopupMenuModel} by label comparison.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class PopupCommandNaming extends GlobalModelNamingScheme<CommandModel, PopupCommandNaming.Name> {

	/**
	 * {@link ModelName} used be {@link PopupCommandNaming}.
	 */
	public interface Name extends ModelName {

		/**
		 * {@link ModelName} of the {@link PopupMenuModel}.
		 */
		ModelName getPopup();

		/**
		 * @see #getPopup()
		 */
		void setPopup(ModelName value);

		/**
		 * The {@link CommandModel}'s label.
		 * 
		 * @see CommandModel#getLabel()
		 */
		String getLabel();

		/**
		 * @see #getLabel()
		 */
		void setLabel(String value);

	}

	/**
	 * Singleton {@link PopupCommandNaming} instance.
	 */
	public static final PopupCommandNaming INSTANCE = new PopupCommandNaming();

	/**
	 * {@link Property} annotating the context {@link PopupMenuModel} to a {@link CommandModel}.
	 */
	public static final Property<PopupMenuModel> POPUP = TypedAnnotatable.property(PopupMenuModel.class, "popup");

	private PopupCommandNaming() {
		super(CommandModel.class, Name.class);
	}

	@Override
	public Maybe<Name> buildName(CommandModel model) {
		if (model instanceof CommandModelAdapter) {
			// Commands in a popup are always wrapped within a command that closes the popup after
			// execution of the inner command. For replay, only the inner command is required.
			model = ((CommandModelAdapter) model).unwrap();
		}

		PopupMenuModel popup = model.get(POPUP);
		if (popup == null) {
			return Maybe.none();
		}

		Maybe<? extends ModelName> popupName = ModelResolver.buildModelNameIfAvailable(popup);
		if (!popupName.hasValue()) {
			return Maybe.none();
		}

		Name name = createName();
		name.setLabel(Resources.getInstance().getString(model.getLabel()));
		name.setPopup(popupName.get());
		return Maybe.some(name);
	}

	@Override
	public CommandModel locateModel(ActionContext context, Name name) {
		PopupMenuModel popup = (PopupMenuModel) ModelResolver.locateModel(context, name.getPopup());
		Menu menu = popup.getMenu();
		for (CommandModel command : menu.buttons()) {
			{
				if (name.getLabel().equals(command.getLabel())) {
					return command;
				}
			}
		}
		throw ApplicationAssertions.fail(name,
			"Cannot resolve command '" + name.getLabel() + "' in popup '" + popup + "'.");
	}

}
