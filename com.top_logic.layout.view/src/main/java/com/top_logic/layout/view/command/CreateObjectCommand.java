/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.command;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.element.model.DynamicModelService;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLType;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.model.ModelService;

/**
 * Command that creates a new persistent object of a configured type.
 *
 * <p>
 * Creates an empty instance of the configured {@link TLClass} in a KB transaction and writes it to
 * the configured output channel. Intended for "New" buttons in list/table views.
 * </p>
 */
public class CreateObjectCommand implements ViewCommand {

	/**
	 * Configuration for {@link CreateObjectCommand}.
	 */
	@TagName("create-object")
	public interface Config extends ViewCommand.Config {

		@Override
		@ClassDefault(CreateObjectCommand.class)
		Class<? extends ViewCommand> getImplementationClass();

		/** Configuration name for {@link #getTypeName()}. */
		String TYPE = "type";

		/** Configuration name for {@link #getOutput()}. */
		String OUTPUT = "output";

		/**
		 * Qualified type name of the object to create (e.g.
		 * {@code test.constraints:ConstraintTestType}).
		 */
		@Name(TYPE)
		@Mandatory
		String getTypeName();

		/**
		 * Name of the channel to write the newly created object to.
		 */
		@Name(OUTPUT)
		@Mandatory
		String getOutput();
	}

	private final String _typeName;

	private final String _outputChannel;

	/**
	 * Creates a new {@link CreateObjectCommand}.
	 */
	@CalledByReflection
	public CreateObjectCommand(InstantiationContext context, Config config) {
		_typeName = config.getTypeName();
		_outputChannel = config.getOutput();
	}

	@Override
	public HandlerResult execute(ReactContext context, Object input) {
		TLType type = TLModelUtil.findType(ModelService.getApplicationModel(), _typeName);
		if (!(type instanceof TLClass)) {
			throw new IllegalArgumentException("Type '" + _typeName + "' is not a TLClass.");
		}

		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		TLObject newObject;
		Transaction tx = kb.beginTransaction();
		try {
			newObject = DynamicModelService.getInstance().createObject((TLClass) type);
			tx.commit();
		} finally {
			tx.rollback();
		}

		// Write to output channel.
		if (context instanceof ViewContext) {
			ViewContext viewContext = (ViewContext) context;
			if (viewContext.hasChannel(_outputChannel)) {
				ViewChannel channel = viewContext.resolveChannel(new ChannelRef(_outputChannel));
				channel.set(newObject);
			}
		}

		return HandlerResult.DEFAULT_RESULT;
	}
}
