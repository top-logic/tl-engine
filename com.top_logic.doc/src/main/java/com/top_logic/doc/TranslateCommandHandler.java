/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.doc;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.basic.translation.TranslationService;
import com.top_logic.doc.command.AbstractImportExportDocumentationCommand;
import com.top_logic.doc.component.DocumentationTreeComponent;
import com.top_logic.doc.component.WithDocumentationLanguage;
import com.top_logic.doc.model.Page;
import com.top_logic.element.structured.StructuredElement;
import com.top_logic.element.structured.wrap.SubtreeLoader;
import com.top_logic.layout.DisplayContext;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.CommandGroupReference;
import com.top_logic.tool.boundsec.conditional.CommandStep;
import com.top_logic.tool.boundsec.conditional.Failure;
import com.top_logic.tool.boundsec.conditional.PreconditionCommandHandler;
import com.top_logic.tool.boundsec.conditional.Success;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;
import com.top_logic.tool.execution.CombinedExecutabilityRule;
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.tool.execution.NullModelDisabled;

/**
 * An {@link AbstractCommandHandler} translating the {@link Page} selected in the
 * {@link DocumentationTreeComponent}.
 *
 * @author <a href="mailto:Dmitry.Ivanizki@top-logic.com">Dmitry Ivanizki</a>
 */
public class TranslateCommandHandler extends PreconditionCommandHandler {

	/**
	 * Configuration of an {@link TranslateCommandHandler}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config
			extends AbstractImportExportDocumentationCommand.Config, WithDocumentationLanguage {

		@Override
		@FormattedDefault(SimpleBoundCommandGroup.WRITE_NAME)
		CommandGroupReference getGroup();

		/**
		 * Whether to overwrite existing text.
		 * 
		 * <p>
		 * If overwrite is not active, only empty pages are filled with translated text.
		 * </p>
		 */
		boolean isOverwrite();

		/**
		 * Whether not just the current page, but the whole subtree must be translated.
		 */
		boolean isRecursive();

	}

	/**
	 * Creates a new {@link TranslateCommandHandler}.
	 */
	public TranslateCommandHandler(InstantiationContext context, Config config) {
		super(context, config);
	}

	Config config() {
		return (Config) getConfig();
	}

	@Override
	protected CommandStep prepare(LayoutComponent component, Object model, Map<String, Object> arguments) {
		if (model == null ) {
			return new Failure(com.top_logic.tool.execution.I18NConstants.ERROR_NO_MODEL_2);
		}
		if (!TranslationService.isActive()) {
			return new Failure(I18NConstants.TRANSLATION_SERVICE_NOT_ACTIVE);
		}
		Locale targetLocale = config().resolveLanguage(component);
		if (!TranslationService.getInstance().isSupported(targetLocale)) {
			return new Failure(I18NConstants.LANGUAGE_NOT_SUPPORTED);
		}
		return new Success() {

			@Override
			protected void doExecute(DisplayContext context) {
				new TranslateDialog(pagesList(model), targetLocale, config().isOverwrite()).open(context);
			}
		};
	}

	List<Page> pagesList(Object model) {
		Page page = (Page) model;
		if (config().isRecursive()) {
			List<Page> pages = new ArrayList<>();
			try (SubtreeLoader loader = new SubtreeLoader()) {
				loader.loadTree(page);
				for (StructuredElement element : loader.getLoadedNodes()) {
					pages.add((Page) element);
				}
			}
			return pages;

		} else {
			return CollectionUtil.createList(page);
		}
	}

	@Override
	protected ExecutabilityRule intrinsicExecutability() {
		return CombinedExecutabilityRule.combine(super.intrinsicExecutability(), NullModelDisabled.INSTANCE);
	}

}
