/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.doc.command;

import static com.top_logic.basic.config.misc.TypedConfigUtil.*;

import java.util.Map;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.NonNullable;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.basic.config.annotation.defaults.ImplementationClassDefault;
import com.top_logic.basic.config.annotation.defaults.ItemDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.doc.model.Page;
import com.top_logic.layout.ModelSpec;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.MainLayout;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.CommandGroupReference;
import com.top_logic.tool.boundsec.CommandHandlerFactory;
import com.top_logic.tool.boundsec.conditional.CommandStep;
import com.top_logic.tool.boundsec.conditional.Failure;
import com.top_logic.tool.boundsec.conditional.FunctionalSuccess;
import com.top_logic.tool.boundsec.conditional.PreconditionCommandHandler;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;

/**
 * Generates documentation pages for every view in the application.
 * <p>
 * <em>Warning:</em> Overrides the titles of pages that already exist.
 * </p>
 * <p>
 * Uses the labels of the views as titles for the pages. The {@link Config#getTarget() target} will
 * be the root of the generated subtree. Existing pages are kept and their title is updated.
 * </p>
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class GenerateDocumentationCommand extends PreconditionCommandHandler {

	/** {@link ConfigurationItem} for the {@link GenerateDocumentationCommand}. */
	public interface Config extends AbstractCommandHandler.Config {

		/** Property name of {@link #getGenerator()}. */
		String GENERATOR = "generator";

		@Override
		@StringDefault(CommandHandlerFactory.ADDITIONAL_GROUP)
		String getClique();

		@Override
		@FormattedDefault(SimpleBoundCommandGroup.CREATE_NAME)
		CommandGroupReference getGroup();

		@Override
		@FormattedDefault(TARGET_SELECTION_SELF)
		ModelSpec getTarget();

		/** The {@link DocumentationGenerator} that does the actual generation. */
		@ItemDefault
		@ImplementationClassDefault(DocumentationGenerator.class)
		@NonNullable
		@Name(GENERATOR)
		PolymorphicConfiguration<DocumentationGenerator> getGenerator();

	}

	/** {@link TypedConfiguration} constructor for {@link GenerateDocumentationCommand}. */
	public GenerateDocumentationCommand(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	protected CommandStep prepare(LayoutComponent component, Object model, Map<String, Object> arguments) {
		if (!(model instanceof Page)) {
			return new Failure(I18NConstants.SELECTION_IS_NO_PAGE);
		}
		MainLayout mainLayout = component.getMainLayout();
		Page page = (Page) model;
		return new FunctionalSuccess(ignored -> generate(mainLayout, page));
	}

	/**
	 * Generates the documentation tree for the given {@link MainLayout}.
	 * 
	 * @param mainLayout
	 *        Is not allowed to be null.
	 * @param rootPage
	 *        Is not allowed to be null.
	 * 
	 * @see DocumentationGenerator
	 */
	public void generate(MainLayout mainLayout, Page rootPage) {
		DocumentationGenerator generator = createGenerator();
		generator.generate(mainLayout, rootPage);
	}

	/** Creates the {@link DocumentationGenerator}. */
	protected DocumentationGenerator createGenerator() {
		return createInstance(getConfigTyped().getGenerator());
	}

	/** Correctly typed variant of {@link #getConfig()}. */
	public Config getConfigTyped() {
		return (Config) getConfig();
	}

}
