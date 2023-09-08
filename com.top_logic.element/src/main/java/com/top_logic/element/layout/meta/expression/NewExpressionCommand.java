/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.meta.expression;

import static com.top_logic.element.layout.meta.expression.SaveExpressionCommand.*;

import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.knowledge.wrap.mapBasedPersistancy.MapBasedPersistancySupport;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.component.AbstractCreateCommandHandler;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.tool.boundsec.wrap.Group;
import com.top_logic.util.TLContext;

/**
 * The common superclass for creating new searches and reports.
 * 
 * @author     <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public abstract class NewExpressionCommand extends AbstractCreateCommandHandler {

	/**
	 * Called by the {@link TypedConfiguration} for creating a {@link NewExpressionCommand}.
	 * <p>
	 * <b>Don't call directly.</b> Use
	 * {@link InstantiationContext#getInstance(PolymorphicConfiguration)} instead.
	 * </p>
	 * 
	 * @param context
	 *        For error reporting and instantiation of dependent configured objects.
	 * @param config
	 *        The configuration for the new instance.
	 */
	@CalledByReflection
	public NewExpressionCommand(InstantiationContext context, Config config) {
		super(context, config);
	}

	/**
	 * Store the owner of the given expression.
	 * <p>
	 * The "owner" is the current person.
	 * </p>
	 */
	protected void storeOwner(Wrapper expression) {
		Person owner = TLContext.getContext().getCurrentPersonWrapper();
		MapBasedPersistancySupport.assignContainer(expression, owner);
	}

	protected void publish(Wrapper expression, FormContainer publishGroupParent) {
		FormGroup publishGroup = getPublishGroup(publishGroupParent);
		if (!publishGroup.isVisible()) {
			return;
		}
		boolean publish = shouldPublish(publishGroup);
		if (!publish) {
			return;
		}
		List<Group> groups = getSelectedGroups(publishGroup);
		SaveExpressionCommand.addGroups(expression, groups);
	}

}
