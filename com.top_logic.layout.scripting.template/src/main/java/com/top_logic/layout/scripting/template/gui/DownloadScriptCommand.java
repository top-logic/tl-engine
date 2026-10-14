/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.template.gui;

import java.util.Map;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.basic.mime.MimeTypesModule;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.scripting.recorder.ActionWriter;
import com.top_logic.layout.scripting.template.gui.ScriptContainer.PersistableScriptContainer;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.execution.CombinedExecutabilityRule;
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.tool.execution.ExecutableState;

/**
 * A {@link CommandHandler} that delivers the {@link ScriptContainer} that is the model of its
 * component as XML file to the browser.
 *
 * <p>
 * In contrast to {@link SaveScriptCommand}, which writes the script to a file on the server, this
 * command allows storing a recorded script on the local computer of the user.
 * </p>
 * 
 * @author <a href="mailto:dbu@top-logic.com">Daniel Busche</a>
 */
public class DownloadScriptCommand extends AbstractCommandHandler {

	/**
	 * The {@link ExecutabilityRule} of the {@link DownloadScriptCommand}: requires a
	 * {@link ScriptContainer} as model.
	 */
	private static final class Executability implements ExecutabilityRule {

		/**
		 * Singleton {@link Executability} instance.
		 */
		static final Executability INSTANCE = new Executability();

		private Executability() {
			// Singleton constructor.
		}

		@Override
		public ExecutableState isExecutable(LayoutComponent component, Object model, Map<String, Object> values) {
			if (!(model instanceof ScriptContainer)) {
				return ExecutableState.createDisabledState(I18NConstants.DOWNLOAD_SCRIPT_ERROR_NO_SCRIPT);
			}
			return ExecutableState.EXECUTABLE;
		}

	}

	/**
	 * Name of the downloaded file, if the script was not loaded from a file.
	 */
	public static final String DEFAULT_FILE_NAME = "script.xml";

	/**
	 * Called by the {@link TypedConfiguration} for creating a {@link DownloadScriptCommand}.
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
	public DownloadScriptCommand(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	protected ExecutabilityRule intrinsicExecutability() {
		return CombinedExecutabilityRule.combine(super.intrinsicExecutability(), Executability.INSTANCE);
	}

	@Override
	public HandlerResult handleCommand(DisplayContext context, LayoutComponent component,
			Object model, Map<String, Object> arguments) {
		if (!(model instanceof ScriptContainer)) {
			return HandlerResult.error(I18NConstants.DOWNLOAD_SCRIPT_ERROR_NO_SCRIPT);
		}
		ScriptContainer script = (ScriptContainer) model;
		String content = ActionWriter.INSTANCE.writeAction(script.getAction(), true, true);
		BinaryData data = BinaryDataFactory.createBinaryData(content.getBytes(StringServices.CHARSET_UTF_8),
			MimeTypesModule.XML_TYPE, fileName(script));
		context.getWindowScope().deliverContent(data);
		return HandlerResult.DEFAULT_RESULT;
	}

	private static String fileName(ScriptContainer script) {
		if (script instanceof PersistableScriptContainer) {
			return ((PersistableScriptContainer) script).getFile().getName();
		}
		return DEFAULT_FILE_NAME;
	}

}
