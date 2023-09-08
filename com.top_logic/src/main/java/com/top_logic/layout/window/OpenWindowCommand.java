/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.window;

import java.util.Map;

import com.top_logic.base.services.simpleajax.AJAXCommandHandler;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.processor.LayoutModelConstants;
import com.top_logic.mig.html.layout.AbstractWindowInfo;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.CommandHandlerFactory;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.boundsec.OpenModalDialogCommandHandler;

/**
 * Handler for opening a window.
 * 
 * This handler isn't registered in the {@link CommandHandlerFactory}, because
 * it isn't stateless.
 * 
 * @author <a href=mailto:twi@top-logic.com>twi</a>
 */
public class OpenWindowCommand extends AJAXCommandHandler {

    private static final String COMMAND_ID_PREFIX = "openWindow";

    /** The layout template that is instantiated by this command. */
    private String windowTemplate;
    
    /** key to use for translations */
	private ResKey i18NKey;

	public interface Config extends AJAXCommandHandler.Config {
		public String getWindowTemplateName();

		public void setWindowTemplateName(String value);
	}

	public OpenWindowCommand(InstantiationContext context, Config config) {
		super(context, config);
		this.windowTemplate = config.getWindowTemplateName();
    }

    private static String createID(String windowTemplate) {
		String base;
		if (windowTemplate.endsWith(LayoutModelConstants.XML_SUFFIX)) {
			base = windowTemplate.substring(0, windowTemplate.length() - LayoutModelConstants.XML_SUFFIX.length());
		} else {
			base = windowTemplate;
		}
		return COMMAND_ID_PREFIX + "_" + base.replaceAll("\\.", "_").replaceAll("\\/", "_");
	}

    /**
     * Execution of the AjaxCommand.
     *     
     * @return the ClientActions that will result in opening the window.
     */
    @Override
	public HandlerResult handleCommand(DisplayContext context, LayoutComponent opener, Object model, Map<String, Object> someArguments) {
    	WindowManager windowManager = opener.getMainLayout().getWindowManager();

    	WindowComponent window = 
    		windowManager.openWindow(context, opener, this.windowTemplate);
    	
		initWindow(context, opener, window, someArguments);
    	
    	return HandlerResult.DEFAULT_RESULT;
    }

    
    /**
     * Hook for subclasses to post-process an opened window.
     */
	protected void initWindow(DisplayContext context, LayoutComponent opener, WindowComponent window,
			Map<String, Object> args) {
		// Nothing.
	}
    
    @Override
	@Deprecated
	public ResKey getDefaultI18NKey() {
		if (i18NKey != null) {
			return i18NKey;
		}
		return super.getDefaultI18NKey();
    }

    /**
     * @see #getDefaultI18NKey()
     */
	public void setDefaultI18NKey(ResKey aKey) {
        i18NKey = aKey;
    }

	/**
	 * Creates the {@link OpenWindowCommand.Config} for the given {@link WindowTemplate}.
	 * 
	 * @param template
	 *        The {@link WindowTemplate} to create opener for.
	 */
	public static PolymorphicConfiguration<? extends CommandHandler> createWindowOpenHandler(
			WindowTemplate.Config template) {
		return createWindowOpenHandler(template.getWindowInfo(), template.getTemplate());
	}

	/**
	 * Creates the {@link OpenWindowCommand.Config} derived from the given window info.
	 * 
	 * @param windowInfo
	 *        The {@link WindowInfo} defining the opener.
	 * @param windowTemplateName
	 *        The name of the window template to instantiate.
	 */
	public static PolymorphicConfiguration<? extends CommandHandler> createWindowOpenHandler(WindowInfo windowInfo,
			String windowTemplateName) {
		OpenWindowCommand.Config customConfig = windowInfo.getOpenHandler();
		if (customConfig != null) {
			Config result = TypedConfiguration.copy(customConfig);
			result.setWindowTemplateName(windowTemplateName);
			return result;
		}

		Class<? extends CommandHandler> openerClass = windowInfo.getOpenHandlerClass();

		String commandId = windowInfo.getOpenHandlerName();
		if (commandId == null) {
			commandId = createID(windowTemplateName);
		}
		Config config = AbstractCommandHandler.createConfig(openerClass, commandId);

		OpenModalDialogCommandHandler.transferInfo(windowInfo, config);
		transferLabel(windowInfo, config);

		config.setWindowTemplateName(windowTemplateName);
	
		return config;
	}

	private static void transferLabel(AbstractWindowInfo window, CommandHandler.Config command) {
		ResKey defaultI18N = window.getDefaultI18N();
		if (defaultI18N != null) {
			updateResourceKey(command, defaultI18N);
		}
	}

}