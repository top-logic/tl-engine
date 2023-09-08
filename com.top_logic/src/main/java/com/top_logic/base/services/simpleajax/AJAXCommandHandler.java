/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.services.simpleajax;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.event.infoservice.InfoService;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.I18NConstantsBase;
import com.top_logic.layout.LayoutContext;
import com.top_logic.layout.form.component.FormComponent;
import com.top_logic.layout.form.tag.js.JSObject;
import com.top_logic.layout.scripting.recorder.gui.inspector.GuiInspectorUtil;
import com.top_logic.layout.structure.BrowserWindowControl;
import com.top_logic.layout.window.WindowComponent;
import com.top_logic.mig.html.HTMLUtil;
import com.top_logic.mig.html.UserAgent;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.LayoutUtils;
import com.top_logic.mig.html.layout.MainLayout;
import com.top_logic.mig.html.layout.PageComponent;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.Resources;

/**
 * Abstract base class of all commands that process service requests sent from
 * the browser client via asynchronous HTTP request.
 * 
 * <p>
 * An {@link AJAXCommandHandler} must be
 * {@link com.top_logic.mig.html.layout.LayoutComponent#registerCommand(CommandHandler) registered}
 * at a parent {@link com.top_logic.mig.html.layout.LayoutComponent} and is
 * executed in the context of that component. The dispatch of finding a matching
 * {@link AJAXCommandHandler} for an incoming request is handled by the
 * {@link AJAXServlet}.
 * </p>
 * 
 * @see #handleCommand(DisplayContext, LayoutComponent, Object, Map) for the entry
 *      point of the central command processing of an {@link AJAXCommandHandler}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AJAXCommandHandler extends AbstractCommandHandler {

	public static final String USE_WAIT_PANE = String.valueOf(true);
	public static final String DONT_USE_WAIT_PANE = String.valueOf(false);

	public static final String SYSTEM_COMMAND_ID = "isSystemCommand";

	/**
	 * Dummy function that swallows the error. This prevents the default alert box from being shown.
	 */
	static final String NO_ON_ERROR = "function() {}";

	static final String NO_SERVER_RESONSE_CALLBACK = "null";
	static final String NO_CONTEXT_INFORMATION = "null";

	public interface Config extends AbstractCommandHandler.Config {
		String READ_ONLY_PROPERTY = "read-only";
		String CONCURRENT_PROPERTY = "concurrent";

		@Name(READ_ONLY_PROPERTY)
		boolean getReadOnly();

		@Name(CONCURRENT_PROPERTY)
		boolean isConcurrent();
	}

	/**
	 * Whether this command does not modify server state and therefore does not
	 * need a sequence number.
	 * 
	 * @see RequestLock
	 */
	private final boolean readonly; 
	
	/**
	 * Whether this command allows concurrent user interactions while it is
	 * executing.
	 * 
	 * <p>
	 * All {@link #readonly} commands are by default concurrent commands. 
	 * </p>
	 */
	private final boolean concurrent; 
	
	/**
	 * Creates a new command with the
	 * {@link com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup#READ}
	 * command group.
	 */
	public AJAXCommandHandler(InstantiationContext context, Config config) {
		super(context, config);

		this.readonly = config.getReadOnly();
		this.concurrent = config.isConcurrent();
	}

	@Override
	public abstract HandlerResult handleCommand(DisplayContext aContext, LayoutComponent aComponent, Object model, Map<String, Object> someArguments);
	
    @Override
	public boolean isConcurrent() {
    	return this.concurrent;
    }

	public void appendInvokeExpression(Appendable out, JSObject argumentObject) throws IOException {
		appendInvokeExpression(out, getID(), argumentObject);
	}

	public static void appendInvokeExpression(Appendable out, String aCommand, JSObject argumentObject)
			throws IOException {
		out.append("services.ajax.execute('");
		out.append(aCommand);
		out.append("', ");
		argumentObject.eval(out);
		out.append(", ");
		out.append(USE_WAIT_PANE);
		out.append(");");
	}

	/**
	 * Includes and initializes the JavaScript that is required make a component
	 * AJAX enabled.
	 * 
	 * @see com.top_logic.mig.html.layout.LayoutComponent#writeHeader(String, TagWriter, HttpServletRequest)
	 */
	public static void writeComponentHeader(
			UserAgent userAgent, 
			String contextPath, 
			TagWriter out,
			LayoutComponent component) throws IOException 
	{
		out.beginScript();
		HTMLUtil.writeJavaScriptContent(out, "WebService.prototype.CONTEXT_PATH   = '" + contextPath + "';");

		// Export all static ajax resources.
		writeI18NConstants(out,
			I18NConstantsBase.getI18NConstants(I18NConstants.class, AJAXCommandHandler::isJavaScripResKey));
		writeI18NConstants(out, I18NConstantsBase.getI18NConstants(
			com.top_logic.layout.form.component.I18NConstants.class, AJAXCommandHandler::isJavaScripResKey));

		LayoutContext layoutContext = component.getMainLayout().getLayoutContext();
		HTMLUtil.writeJavaScriptContent(out,
			"services.ajax.WINDOW_NAME         = '" + layoutContext.getWindowId().getWindowName() + "';");
		HTMLUtil.writeJavaScriptContent(out,
			"services.ajax.TIMEOUT               = " + layoutContext.getLock().getOptions().getReorderTimeout() + ";");
		HTMLUtil.writeJavaScriptContent(out, "services.ajax.COMPONENT_ID          = '" + component.getName() + "';");
		HTMLUtil.writeJavaScriptContent(out, "services.ajax.SUBMIT_NUMBER         = " + component.getSubmitNumber() + ";");
		HTMLUtil.writeJavaScriptContent(out, "services.ajax.mainLayout            = " + getMainLayoutReferenceForComponent(component) + ";");
		HTMLUtil.writeJavaScriptContent(out, "services.ajax.systemCommandId       = '" + SYSTEM_COMMAND_ID + "';");
		if (component instanceof MainLayout) {
			MainLayout mainLayout = (MainLayout) component;
			BrowserWindowControl layoutControl = mainLayout.getLayoutControl();
			layoutControl.fetchID(mainLayout.getEnclosingFrameScope());
			HTMLUtil.writeJavaScriptContent(out, "services.ajax.mainLayout.controlID  = '" + layoutControl.getID() + "';");
		}
		HTMLUtil.writeJavaScriptContent(out, "services.ajax.topWindow             = " + getTopWindowReferenceForComponent(component) + ";");
		AJAX config = ApplicationConfig.getInstance().getConfig(AJAX.class);
		HTMLUtil.writeJavaScriptContent(out, "services.ajax.USE_WAIT_PANE_IN_FORMULA = "
				+ (config.getUseWaitPaneInFormula() ? USE_WAIT_PANE : DONT_USE_WAIT_PANE) + ";");
		HTMLUtil.writeJavaScriptContent(out, "services.ajax.ENABLE_INSPECT = " + GuiInspectorUtil.isGuiInspectorEnabled() + ";");

		HTMLUtil.writeJavaScriptContent(out, "services.ajax.progressBarDelay = " + config.getProgressBarDelay() + ";");
		HTMLUtil.writeJavaScriptContent(out, "services.ajax.WAIT_PANE_DELAY = " + config.getWaitPaneDelay() + ";");
		
		HTMLUtil.writeJavaScriptContent(out, "services.log.COMPONENT_NAME         = '" + component.getName() + "';");
		HTMLUtil.writeJavaScriptContent(out, "services.log.COMPONENT_CLASS        = '" + component.getClass().getName() + "';");
		HTMLUtil.writeJavaScriptContent(out, "services.log.COMPONENT_LOCATION     = '" + HTMLUtil.encodeJS(component.getLocation()) + "';");

		String writtenJSP = "-";
		if (component instanceof FormComponent) {
			writtenJSP = ((FormComponent) component).getPage();
		} else if (component instanceof PageComponent) {
			writtenJSP = ((PageComponent) component).getPage();
		}
        HTMLUtil.writeJavaScriptContent(out, "services.log.JSP_PAGE               = '" + HTMLUtil.encodeJS(writtenJSP) + "';");
		HTMLUtil.writeJavaScriptContent(out, "services.log.RESOURCE_PREFIX        = '" + HTMLUtil.encodeJS(component.getResPrefix().toPrefix()) + "';");
		if (LayoutComponent.isLoggingEnabled()) {
			HTMLUtil.writeJavaScriptContent(out, "services.log.installGlobalHandler();");
		}
		HTMLUtil.writeJavaScriptContent(out, "services.infoServiceFadeoutTimerSeconds = "
			+ ApplicationConfig.getInstance().getConfig(InfoService.Config.class).getFadeOutTimerSeconds() + ";");
		out.endScript();
		// include takes place here since 'services.ajax.mainLayout' is used inside ajax-form.js
		HTMLUtil.writeJavascriptRef(out, contextPath, "/script/tl/ajax-form.js");
	}

	private static boolean isJavaScripResKey(Field f) {
		return f.getAnnotation(JavaScriptResKey.class) != null;
	}

	/**
	 * returns a JS reference from the given component to the mainLayout of the given component.
	 * 
	 * @param component
	 *            must have a representation on the client
	 * @return the reference to navigate from the window-object of the given component to the window
	 *         object of the main layout.
	 */
	private static String getMainLayoutReferenceForComponent(LayoutComponent component) {
		String reference = LayoutUtils.getFrameReference(component.getEnclosingFrameScope(), component.getMainLayout().getEnclosingFrameScope());
		if (StringServices.isEmpty(reference)) {
			return "window";
		} else {
			return reference;
		}
	}

	/**
	 * returns a JS reference from the given component to the topmost window of the application,
	 * i.e. if the component is shown in the main window it is the same reference as given in
	 * {@link #getMainLayoutReferenceForComponent}, or "window.top" if the component is shown in an
	 * external window.
	 * 
	 * @param component
	 *            must have a client side representation
	 * @return the reference to the topmost window of the application
	 */
	private static String getTopWindowReferenceForComponent(LayoutComponent component) {
		WindowComponent enclosingWindow = component.getEnclosingWindow();
		if (enclosingWindow == null) {
			return getMainLayoutReferenceForComponent(component);
		} else {
			return "window.top";
		}
	}

	private static void writeI18NConstants(TagWriter out, Iterator<? extends Field> i18nConstants)
			throws IOException, UnreachableAssertion {
		Resources resources = Resources.getInstance();
		for (Iterator<? extends Field> it = i18nConstants; it.hasNext();) {
			Field i18nConstant = it.next();
			Object value;
			try {
				value = i18nConstant.get(null);
			} catch (IllegalArgumentException e) {
				throw new UnreachableAssertion("Non-static I18N constant.");
			} catch (IllegalAccessException e) {
				throw new UnreachableAssertion("Inaccessible I18N constant.");
			}
			// All ResKeyX types are in fact ResKey.
			ResKey key = (ResKey) value;
			out.writeIndent();
			out.append("services.i18n.");
			out.append(i18nConstant.getName());
			out.append(" = ");
			String text = resources.getString(key);
			out.writeJsString(text.replace("\n", "<br/>"));
			out.append(";");
		}
	}

	public static void writeBALInclude(TagWriter writer, String contextPath, UserAgent userAgent) throws IOException {
		HTMLUtil.writeJavascriptRef(writer, contextPath, "/script/tl/bal.js");
		if (userAgent.is_ie6up()) {
			HTMLUtil.writeJavascriptRef(writer, contextPath, "/script/tl/bal-ie.js");
			if (UserAgent.is_ie6down(userAgent)) {
				HTMLUtil.writeJavascriptRef(writer, contextPath, "/script/tl/bal-ie6.js");
			}
			if (UserAgent.is_ie8down(userAgent)) {
				HTMLUtil.writeJavascriptRef(writer, contextPath, "/script/tl/bal-ie8below.js");
			}
			if (UserAgent.is_ie9down(userAgent)) {
				HTMLUtil.writeJavascriptRef(writer, contextPath, "/script/tl/bal-ie9below.js");
			}
		} else if (userAgent.is_httpunit()) {
			HTMLUtil.writeJavascriptRef(writer, contextPath, "/script/tl/bal-httpunit.js");
		} else {
			HTMLUtil.writeJavascriptRef(writer, contextPath, "/script/tl/bal-ff.js");
		}
	}

	public static <C extends Config> C updateConcurrent(C config, boolean concurrent) {
		return update(config, Config.CONCURRENT_PROPERTY, concurrent);
	}

	public static <C extends Config> C updateReadOnly(C config, boolean value) {
		return update(config, Config.READ_ONLY_PROPERTY, value);
	}

}
