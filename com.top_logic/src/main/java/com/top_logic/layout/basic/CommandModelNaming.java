/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic;

import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable.Property;
import com.top_logic.layout.scripting.recorder.ref.AbstractModelNamingScheme;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.ModelNamingScheme;
import com.top_logic.layout.scripting.recorder.ref.ModelResolver;
import com.top_logic.layout.scripting.runtime.ActionContext;

/**
 * {@link ModelNamingScheme} of the {@link CommandModel}, uses the {@link CommandModelName}.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class CommandModelNaming extends AbstractModelNamingScheme<CommandModel, CommandModelNaming.CommandModelName> {

	private static final Property<CommandModelOwner> COMMAND_MODEL_OWNER =
		TypedAnnotatable.property(CommandModelOwner.class, "CommandModelOwner");

	/** {@link ModelName} of the {@link CommandModel}, used by the {@link CommandModelNaming}. */
	public interface CommandModelName extends ModelName {

		/** The {@link ModelName} of the {@link CommandModel}'s owner. */
		ModelName getOwner();

		/** @see #getOwner() */
		void setOwner(ModelName value);

	}

	@Override
	public Class<CommandModelName> getNameClass() {
		return CommandModelName.class;
	}

	@Override
	public Class<CommandModel> getModelClass() {
		return CommandModel.class;
	}

	@Override
	public CommandModel locateModel(ActionContext context, CommandModelName name) {
		CommandModelOwner owner = (CommandModelOwner) ModelResolver.locateModel(context, name.getOwner());
		return owner.getCommandModel();
	}

	@Override
	protected void initName(CommandModelName name, CommandModel model) {
		name.setOwner(ModelResolver.buildModelName(getOwner(model)));
	}

	@Override
	protected boolean isCompatibleModel(CommandModel model) {
		CommandModelOwner owner = getOwner(model);
		return (owner != null) && (owner != CommandModelOwner.NO_OWNER);
	}

	private CommandModelOwner getOwner(CommandModel model) {
		return model.get(COMMAND_MODEL_OWNER);
	}

	/**
	 * Registers a {@link CommandModelOwner} to its {@link CommandModel}.
	 */
	public static void setOwner(CommandModel model, CommandModelOwner owner) {
		if (owner != null) {
			model.set(COMMAND_MODEL_OWNER, owner);
		} else {
			model.set(COMMAND_MODEL_OWNER, CommandModelOwner.NO_OWNER);
		}
	}

}
