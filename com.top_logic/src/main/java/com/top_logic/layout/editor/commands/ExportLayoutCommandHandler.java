/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.editor.commands;

import static com.top_logic.layout.DisplayDimension.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.util.ResKey;
import com.top_logic.event.infoservice.InfoService;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.DisplayUnit;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.basic.fragments.Fragments;
import com.top_logic.layout.messagebox.MessageBox;
import com.top_logic.layout.messagebox.MessageBox.ButtonType;
import com.top_logic.layout.structure.DefaultLayoutData;
import com.top_logic.layout.structure.LayoutData;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.LayoutScopeMapper;
import com.top_logic.mig.html.layout.TLLayout;
import com.top_logic.tool.boundsec.ConfirmCommandHandler;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.tool.execution.InDesignModeExecutable;
import com.top_logic.util.LayoutBasedSecurity;
import com.top_logic.util.TLContext;

/**
 * Exports the in-app configured application views currently stored in the database to XML files in
 * the development environment.
 * 
 * <p>
 * Changes done in design-mode are stored in the application's database. While this is OK for pure
 * in-app development, the designed application views can also be exported to a local development
 * environment for a conventional build/deploy/release process.
 * </p>
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
@Label("Export layout")
public class ExportLayoutCommandHandler extends ConfirmCommandHandler {

	/**
	 * Creates a {@link ExportLayoutCommandHandler} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	public ExportLayoutCommandHandler(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	protected HandlerResult internalHandleCommand(DisplayContext context, LayoutComponent component, Object model,
			Map<String, Object> args) {

		HandlerResult result = KBUtils.inTransaction(() -> exportLayout(context));

		InfoService.showInfo(I18NConstants.LAYOUT_EXPORT_LOGIN_RECOMMENDATION);
		
		if (result.isSuccess()) {
			LayoutBasedSecurity.reloadPersistentSecurity();
		} else if (result.isSuspended()) {
			result.appendContinuation(resumeContext -> {
				LayoutBasedSecurity.reloadPersistentSecurity();
				return HandlerResult.DEFAULT_RESULT;
			});
		}

		return result;
	}

	private HandlerResult exportLayout(DisplayContext context) {
		Map<String, ConfigurationException> exceptionsByLayoutKey = exportLayoutToFilesystem(createLayoutContext());

		if (!CollectionUtil.isEmptyOrNull(exceptionsByLayoutKey)) {
			return handleFaultyLayouts(context, exceptionsByLayoutKey);
		}

		return HandlerResult.DEFAULT_RESULT;
	}

	private Map<String, ConfigurationException> exportLayoutToFilesystem(LayoutExportContext context) {
		Map<String, ConfigurationException> exportExceptionsByLayoutKey = new HashMap<>();

		LayoutExporter exporter = createLayoutExporter(context);

		for (Entry<String, TLLayout> entry : context.getTemplatesByLayoutKey().entrySet()) {
			try {
				exporter.export(entry.getKey(), entry.getValue());
			} catch (ConfigurationException exception) {
				exportExceptionsByLayoutKey.put(entry.getKey(), exception);
			}
		}

		return exportExceptionsByLayoutKey;

	}

	private LayoutExporter createLayoutExporter(LayoutExportContext context) {
		return new LayoutExporter(createLayoutScopeMapper(context));
	}

	private LayoutScopeMapper createLayoutScopeMapper(LayoutExportContext context) {
		return new LayoutScopeMapper(context.getExportNameByLayoutKey());
	}

	private LayoutExportContext createLayoutContext() {
		return new LayoutExportContext(TLContext.getContext().getPerson());
	}

	private HandlerResult handleFaultyLayouts(DisplayContext context, Map<String, ConfigurationException> exceptions) {
		CommandModel yesCommand = createDeleteFaultyLayoutsCommand(exceptions.keySet());
		CommandModel noCommand = MessageBox.button(ButtonType.NO);
		HTMLFragment title = Fragments.message(I18NConstants.LAYOUT_EXPORT_ERROR_TITLE);
		HTMLFragment message = createMessage(exceptions.values());

		return MessageBox.confirm(context.getWindowScope(), createDialogLayout(), true, title, message, yesCommand, noCommand);
	}

	private CommandModel createDeleteFaultyLayoutsCommand(Set<String> layoutKeys) {
		return MessageBox.button(ButtonType.YES, new Command() {

			@Override
			public HandlerResult executeCommand(DisplayContext context) {
				KBUtils.inTransaction(() -> LayoutExportUtils.deletePersistentLayoutTemplates(layoutKeys));
				return HandlerResult.DEFAULT_RESULT;
			}
		});
	}

	private LayoutData createDialogLayout() {
		return DefaultLayoutData.newLayoutData(dim(50, DisplayUnit.PERCENT), dim(50, DisplayUnit.PERCENT));
	}

	private HTMLFragment createMessage(Collection<ConfigurationException> exceptions) {
		HTMLFragment messagePrefix = Fragments.message(I18NConstants.LAYOUT_EXPORT_ERROR_MESSAGE_PREFIX);
		HTMLFragment exceptionCauses = createExceptionCausesFragment(exceptions);
		HTMLFragment suffixMessage = Fragments.message(I18NConstants.LAYOUT_EXPORT_ERROR_MESSAGE_SUFFIX);

		return Fragments.concat(messagePrefix, exceptionCauses, suffixMessage);
	}

	private HTMLFragment createExceptionCausesFragment(Collection<ConfigurationException> exceptions) {
		Collection<HTMLFragment> listEntries = new HashSet<>();

		for (ConfigurationException exception : exceptions) {
			ResKey errorKey = exception.getErrorKey();

			if (isTemplateDeserializationError(errorKey)) {
				listEntries.add(createExceptionCauseEntry(exception, errorKey));
			}
		}

		return Fragments.ul(listEntries.toArray(new HTMLFragment[listEntries.size()]));
	}

	private HTMLFragment createExceptionCauseEntry(ConfigurationException exception, ResKey errorKey) {
		Object[] arguments = errorKey.arguments();

		ResKey deserializationErrorIntro = I18NConstants.DESERIALIZATION_ERROR.fill(arguments[1], arguments[2]);

		HTMLFragment exceptionIntro = Fragments.message(deserializationErrorIntro);
		HTMLFragment argumentConfigFragment = Fragments.pre(Fragments.text((String) arguments[0]));
		HTMLFragment innerCause = Fragments.text(exception.getCause().getMessage());

		return Fragments.li(Fragments.concat(exceptionIntro, argumentConfigFragment, innerCause));
	}

	private boolean isTemplateDeserializationError(ResKey errorKey) {
		return com.top_logic.layout.editor.I18NConstants.TEMPLATE_DESERIALIZATION_ERROR__ARGUMENTS_TEMPLATE_KEY == errorKey.plain();
	}

	@Override
	protected ResKey getConfirmMessage(DisplayContext context, LayoutComponent component, Object model,
			Map<String, Object> arguments) {
		return I18NConstants.CONFIRM_EXPORT_LAYOUTS;
	}

	@Override
	protected ExecutabilityRule intrinsicExecutability() {
		return InDesignModeExecutable.INSTANCE;
	}

}
