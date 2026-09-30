/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.command;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.view.security.ModelAccessRule;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLType;
import com.top_logic.model.impl.TransientObjectFactory;
import com.top_logic.model.util.TLModelPartRef;

/**
 * {@link ViewAction} creating a transient object of a type, the draft a create dialog edits.
 *
 * <p>
 * The result is a fresh transient object of the {@link Config#getType() type}, as the TL-Script
 * {@code new(type, transient: true)} creates it; the action ignores its input. A
 * {@link PersistTransientAction} in the dialog later makes the draft persistent.
 * </p>
 *
 * <p>
 * The action brings its executability: the command opening the dialog is offered only where the
 * user may create an object of the type. Without a {@link Config#getContainer() container}, that is
 * checked against the security root, and a refused command is hidden. With a container, it is
 * checked in the context of the container and, with a {@link Config#getReference() reference},
 * together with the right to write that reference; a refused command is disabled. The container
 * and the reference are only checked here: the dialog receives the container through the bindings
 * of the {@link OpenDialogAction}. See {@link ModelAccessRule} for how a refusal is displayed.
 * </p>
 *
 * <p>
 * Example: open a dialog creating a milestone of the selected project scope.
 * </p>
 *
 * <pre>
 * &lt;generic-command&gt;
 *   &lt;create-transient type="my.module:Milestone" container="selectedScope" reference="milestones"/&gt;
 *   &lt;open-dialog bind-input-to="model" dialog-view="create-milestone.view.xml"&gt;
 *     ...
 *   &lt;/open-dialog&gt;
 * &lt;/generic-command&gt;
 * </pre>
 */
@InApp
public class CreateTransientAction implements ViewAction {

	/**
	 * Configuration for {@link CreateTransientAction}.
	 */
	@TagName(Config.TAG_NAME)
	public interface Config extends PolymorphicConfiguration<CreateTransientAction>, CreationContainer {

		/** Tag name of a {@link CreateTransientAction} in an action chain. */
		String TAG_NAME = "create-transient";

		/** Configuration name for {@link #getType()}. */
		String TYPE = "type";

		@Override
		@ClassDefault(CreateTransientAction.class)
		Class<? extends CreateTransientAction> getImplementationClass();

		/**
		 * The type of the created object.
		 */
		@Name(TYPE)
		@Mandatory
		TLModelPartRef getType();
	}

	private final Config _config;

	/**
	 * Creates a {@link CreateTransientAction} from configuration.
	 */
	@CalledByReflection
	public CreateTransientAction(InstantiationContext context, Config config) {
		_config = config;
		CreationContainer.checkContainer(context, config);
	}

	/**
	 * The right to create an object of the type, in the context of the container if one is given.
	 */
	@Override
	public ViewExecutabilityRule getIntrinsicRule() {
		return ModelAccessRule.creation(_config.getType(), _config.getContainer(), _config.getReference());
	}

	@Override
	public Object execute(ReactContext context, Object input) {
		TLType type = _config.getType().resolveType();
		if (!(type instanceof TLClass clazz)) {
			throw new IllegalArgumentException("The '" + Config.TYPE + "' '" + _config.getType().qualifiedName()
				+ "' of a '" + Config.TAG_NAME + "' action is not a class.");
		}
		return TransientObjectFactory.INSTANCE.createObject(clazz, null);
	}
}
