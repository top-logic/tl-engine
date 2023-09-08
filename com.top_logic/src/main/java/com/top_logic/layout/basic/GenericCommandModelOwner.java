/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic;

import com.top_logic.basic.col.Mapping;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable.Property;
import com.top_logic.layout.scripting.recorder.ref.GenericModelOwner;

/**
 * {@link GenericModelOwner} for {@link CommandModel}s.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class GenericCommandModelOwner extends GenericModelOwner<CommandModel> implements CommandModelOwner {

	/**
	 * Mapping that maps an {@link TypedAnnotatable} to a previously attached {@link CommandModel}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static class AnnotatedCommandModelMapping implements Mapping<Object, CommandModel> {

		/**
		 * Singleton {@link AnnotatedCommandModelMapping} instance.
		 */
		public static final AnnotatedCommandModelMapping INSTANCE = new AnnotatedCommandModelMapping();

		/**
		 * Creates a new {@link AnnotatedCommandModelMapping}.
		 */
		protected AnnotatedCommandModelMapping() {
			// singleton instance
		}

		/** Property that is used to annotate the command model to its owner. */
		public static final Property<CommandModel> COMMAND_MODEL =
			TypedAnnotatable.property(CommandModel.class, "command model");

		@Override
		public CommandModel map(Object input) {
			return ((TypedAnnotatable) input).get(COMMAND_MODEL);
		}

		/**
		 * Annotates the given command model to the given owner
		 */
		public static void annotate(CommandModel model, TypedAnnotatable owner) {
			owner.set(COMMAND_MODEL, model);
		}

	}

	/**
	 * Creates a new {@link GenericCommandModelOwner}.
	 */
	public GenericCommandModelOwner(Object reference, Mapping<Object, CommandModel> algorithm) {
		super(reference, algorithm);
	}

	@Override
	public CommandModel getCommandModel() {
		return getModel();
	}

	/**
	 * Service method to create a {@link GenericCommandModelOwner} based on the given
	 * {@link TypedAnnotatable owner} and to attach to the given {@link CommandModel}.
	 * 
	 * @param model
	 *        The {@link CommandModel} to record.
	 * @param owner
	 *        The new (logical) owner of the model. It must be possible to record this
	 *        {@link TypedAnnotatable}.
	 */
	public static void setOwner(CommandModel model, TypedAnnotatable owner) {
		AnnotatedCommandModelMapping.annotate(model, owner);
		CommandModelNaming.setOwner(model, new GenericCommandModelOwner(owner, AnnotatedCommandModelMapping.INSTANCE));
	}

}
