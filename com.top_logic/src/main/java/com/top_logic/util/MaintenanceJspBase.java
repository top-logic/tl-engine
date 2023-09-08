/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util;

import static com.top_logic.mig.html.HTMLConstants.*;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;

import com.top_logic.basic.ArrayUtil;
import com.top_logic.basic.CalledFromJSP;
import com.top_logic.basic.DebugHelper;
import com.top_logic.basic.Logger;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable.Property;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.basic.exception.I18NException;
import com.top_logic.basic.html.SafeHTML;
import com.top_logic.basic.util.NumberUtil;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagUtil;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.KnowledgeBaseFactory;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.URLBuilder;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.basic.CommandModelFactory;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.buttonbar.Icons;
import com.top_logic.layout.form.control.ButtonControl;
import com.top_logic.layout.form.control.DefaultButtonRenderer;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.MainLayout;
import com.top_logic.tool.boundsec.CommandHandler;


/**
 * Superclass for maintenance JSPs.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class MaintenanceJspBase extends TopLogicJspBase {

	private static final Property<JspWriter> JSP_WRITER = TypedAnnotatable.property(JspWriter.class, "JSP writer");

	/** Whether printed messages should also be logged to {@link Logger}. */
	protected boolean LOG_PRINTS = false;

	/**
	 * The message to show on slider, when only simulation is requested.
	 * 
	 * @see #WAITING_MESSAGE
	 */
	protected String SIM_WAITING_MESSAGE = null;

	/**
	 * The message to show on slider.
	 * 
	 * @see #SIM_WAITING_MESSAGE
	 */
	protected String WAITING_MESSAGE = null;

	/** Whether the waiting slider must be displayed. */
	protected boolean USE_WAITING_ANI = false;

	/** Description of the page. */
	protected CharSequence DESCRIPTION = null;

	/** Title of the page. */
	protected CharSequence TITLE = null;

	/** Whether time for execution should be logged. */
	protected boolean LOG_TIME = false;

	private static final String SPAN_START_BEGIN = "<span style=\"color:";

	private static final String SPAN_START_END = "\">";

	private static final String SPAN_GREEN = SPAN_START_BEGIN + "green" + SPAN_START_END;

	private static final String SPAN_ORANGE = SPAN_START_BEGIN + "orange" + SPAN_START_END;

	private static final String SPAN_RED = SPAN_START_BEGIN + "red" + SPAN_START_END;

	private static final String SPAN_END = "</span>";

	/**
	 * Initialises the Writer to write to. Must be called before any "print" method is called.
	 */
	protected void initWriter(JspWriter out) {
		DefaultDisplayContext.getDisplayContext().set(JSP_WRITER, out);
	}

	/**
	 * Writes the given {@link String} followed by line break as error to the client.
	 */
	protected void printError(String string) throws IOException {
		if (LOG_PRINTS) {
			Logger.error(string, getClass());
		}
		write(SPAN_RED + quote(string) + SPAN_END + "<br/>\n");
	}

	/**
	 * Returns the {@link JspWriter} formerly initialised with {@link #initWriter(JspWriter)}.
	 */
	protected JspWriter getWriter() {
		JspWriter writer = DefaultDisplayContext.getDisplayContext().get(JSP_WRITER);
		if (writer == null) {
			throw new IllegalStateException("No JSPWriter available. initWriter(JSPWriter) not called.");
		}
		return writer;
	}

	/**
	 * Writes the given {@link String} followed by line break as error to the client.
	 */
	protected void printError(String string, Throwable error) throws IOException {
		if (LOG_PRINTS) {
			Logger.error(string, error, getClass());
		}
		write(SPAN_RED + quote(string) + SPAN_END + "<br/>\n");
	}

	/**
	 * Writes the given {@link String} followed by line break as warning to the client.
	 */
	protected void printWarning(String string) throws IOException {
		if (LOG_PRINTS) {
			Logger.warn(string, getClass());
		}
		write(SPAN_ORANGE + quote(string) + SPAN_END + "<br/>\n");
	}

	/**
	 * Writes the given {@link String} followed by line break as warning to the client.
	 */
	protected void printWarning(String string, Throwable exception) throws IOException {
		if (LOG_PRINTS) {
			Logger.warn(string, exception, getClass());
		}
		write(SPAN_ORANGE + quote(string) + SPAN_END + "<br/>\n");
	}

	/**
	 * Writes the given {@link String} followed by line break as info to the client.
	 */
	protected void printInfo(String string) throws IOException {
		printLog(string);
		write(SPAN_GREEN + quote(string) + SPAN_END + "<br/>\n");
	}

	/**
	 * Writes the given {@link String} followed by line break as info to the client.
	 */
	protected void printInfo(String string, Throwable error) throws IOException {
		printLog(string, error);
		write(SPAN_GREEN + quote(string) + SPAN_END + "<br/>\n");
	}

	/**
	 * color may be a CSS word (e.g. red, green, ...) or HTML code (eg. #FF0000, rgb(10%,50%,100%),
	 * ...)
	 */
	protected void printColor(String color, String string) throws IOException {
		printLog(string);
		write(SPAN_START_BEGIN + color + SPAN_START_END + quote(string) + SPAN_END + "<br/>\n");
	}

	/**
	 * color may be a CSS word (e.g. red, green, ...) or HTML code (eg. #FF0000, rgb(10%,50%,100%),
	 * ...)
	 */
	protected void printColor(String color, String string, Throwable error) throws IOException {
		printLog(string, error);
		write(SPAN_START_BEGIN + color + SPAN_START_END + quote(string) + SPAN_END + "<br/>\n");
	}

	/**
	 * Prints the given {@link String} to {@link Logger} when {@link #LOG_PRINTS} is
	 * <code>true</code>.
	 */
	protected void printLog(String string) {
		if (LOG_PRINTS) {
			Logger.info(string, getClass());
		}
	}

	/**
	 * Prints the given {@link String} to {@link Logger} when {@link #LOG_PRINTS} is
	 * <code>true</code>.
	 */
	protected void printLog(String string, Throwable exception) {
		if (LOG_PRINTS) {
			Logger.info(string, exception, getClass());
		}
	}

	/**
	 * Prints the given {@link String} to the client followed by line break.
	 */
	protected void print(String string) throws IOException {
		printLog(string);
		write(quote(string) + "<br/>\n");
	}

	/**
	 * Prints a line break.
	 */
	protected void print() throws IOException {
		write("<br/>\n");
	}

	/**
	 * Prints the given {@link String} unquoted to the client followed by line break.
	 */
	protected void printUnquoted(String string) throws IOException {
		printLog(string);
		write(string + "<br/>\n");
	}

	/** Writes the given HTML <em>without quoting</em> to the client. */
	protected void write(String html) throws IOException {
		try {
			JspWriter writer = getWriter();
			writer.write(html);
			writer.flush();
		} catch (IOException|RuntimeException exception) {
			Logger.error("Writing to the client failed. Cause: " + exception.getMessage(), exception, getClass());
			throw exception;
		}
	}

	/**
	 * Quotes the given String.
	 */
	protected String quote(String string) {
		return TagUtil.encodeXML(string);
	}

	protected double percent(int current, int expected) {
		return expected == 0 ? 0 : NumberUtil.round(((double) current / (double) expected * 100), 1);
	}

	/**
	 * Commits the {@link KnowledgeBase} changes.
	 * 
	 * @param simulate
	 *        If <code>true</code> actually no commit occurs.
	 */
	protected boolean commit(boolean simulate) throws IOException {
		KnowledgeBase theKB = KnowledgeBaseFactory.getInstance().getDefaultKnowledgeBase();
		if (simulate) {
			rollback();
			printColor("blue", "Simulating OK.");
			return true;
		}
		else if (theKB.commit()) {
			printInfo("Commit OK.");
			return true;
		}
		else {
			printError("Commit failed.");
			return false;
		}
	}

	/**
	 * Reverts the {@link KnowledgeBase} changes.
	 */
	protected void rollback() {
		KnowledgeBaseFactory.getInstance().getDefaultKnowledgeBase().rollback();
	}

	/**
	 * Method to call from JSP.
	 */
	protected void runWork(JspWriter out, boolean doSimulate, HttpServletRequest aRequest) throws Exception {
		initWriter(out);
		long startTime = System.currentTimeMillis();
		try {
			doWork(out, doSimulate, aRequest);
		} catch (Throwable exception) {
			Logger.error("Failed to execute maintenance work.", exception, getClass());
			throw exception;
		}
		print();
		if (!LOG_TIME) {
			print("Finished.");
		} else {
			print("Finished. Run needed " + DebugHelper.getTime(System.currentTimeMillis() - startTime));
		}
	}

	/**
	 * Method to override in JSP to make actual work.
	 * 
	 * @param out
	 *        The {@link JspWriter} to write actual content to.
	 * @param simulate
	 *        Whether the work must not be executed but simulated.
	 * @param request
	 *        The actual request.
	 */
	protected void doWork(JspWriter out, boolean simulate, HttpServletRequest request) throws Exception {
		throw new IllegalStateException("Override doWork(JspWriter out, boolean simulate, HttpServletRequest request).");
	}

	/**
	 * Writes the waiting slider.
	 * 
	 * @param pageContext
	 *        The current {@link PageContext}.
	 * @param runParameters
	 *        The name of the parameters that triggers a run, e.g. "SUBMIT, "SIMULATE. When the
	 *        waiting slider is displayed, the parameter values are transfered to corresponding
	 *        "DO_" parameters, i.e. the "SUBMIT" value is after the waiting slider available under
	 *        "DO_SUBMIT".
	 */
	protected void writeWaitingSlider(PageContext pageContext, String... runParameters) throws IOException {
		ServletRequest request = pageContext.getRequest();
		LayoutComponent component = MainLayout.getComponent(pageContext);
		DisplayContext displayContext = DefaultDisplayContext.getDisplayContext(pageContext);
		// waiting slider
		TagWriter out = MainLayout.getTagWriter(pageContext);
		boolean hasRunParmeter = false;
		for (String runParam : runParameters) {
			hasRunParmeter = hasRunParmeter || request.getParameter(runParam) != null;
		}
		if (USE_WAITING_ANI && hasRunParmeter) {
			boolean doSimulate = request.getParameter("SIMULATE") != null;
			out.beginBeginTag(DIV);
			out.writeAttribute(ID_ATTR, "progressDiv");
			out.writeAttribute(CLASS_ATTR, "fullProgressVisible");
			out.endBeginTag();
			{
				out.beginBeginTag(TABLE);
				out.writeAttribute(WIDTH_ATTR, "100%");
				out.writeAttribute(HEIGHT_ATTR, "100%");
				out.endBeginTag();
				{
					out.beginTag(TR);
					{
						out.beginBeginTag(TD);
						out.writeAttribute(ALIGN_ATTR, CENTER_VALUE);
						out.endBeginTag();
						{
							out.beginTag(BOLD);
							out.writeText(doSimulate ? SIM_WAITING_MESSAGE : WAITING_MESSAGE);
							out.endTag(BOLD);
							out.emptyTag(BR);
							out.emptyTag(BR);
							{
								Icons.SLIDER_IMG.get().writeWithCss(
									DefaultDisplayContext.getDisplayContext(pageContext),
									out, "fullProgress");
							}
						}
						out.endTag(TD);
					}
					out.endTag(TR);
				}
				out.endTag(TABLE);
			}
			out.endTag(DIV);

			out.beginScript();
			out.writeScript("self.location.href = ");
			out.writeJsString(
				addParameters(component.getComponentURL(displayContext), request, runParameters).getURL());
			out.writeScript(";");
			out.endScript();
		} else {
			if (TITLE != null) {
				out.beginTag(H1);
				out.writeContent(TITLE);
				out.endTag(H1);
			}
			if (DESCRIPTION != null) {
				out.beginTag(PARAGRAPH);
				out.writeContent(DESCRIPTION);
				out.endTag(PARAGRAPH);
			}
		}

		out.flushBuffer();
	}

	private URLBuilder addParameters(URLBuilder url, ServletRequest request, String[] runParameters) {
		Set<String> params = new HashSet<>();
		Collections.addAll(params, runParameters);
		Map<String, String[]> theParameters = request.getParameterMap();
		for (Map.Entry<String, String[]> entry : theParameters.entrySet()) {
			String key = entry.getKey();
			String value = (String) ArrayUtil.getFirst(entry.getValue());
			if (params.contains(key)) {
				key = "DO_" + key;
			}
			url.appendParameter(key, value);
		}
		return url;
	}

	/**
	 * Resolves the given resource key.
	 */
	@CalledFromJSP
	public static String res(ResKey key) {
		return Resources.getInstance().getString(key);
	}

	/**
	 * Renders the given resource as text to the current page.
	 */
	public void renderText(PageContext pageContext, ResKey key) throws IOException {
		renderText(pageContext, res(key));
	}

	/**
	 * Renders the given text to the current page.
	 */
	public void renderText(PageContext pageContext, String text) throws IOException {
		TagWriter out = MainLayout.getTagWriter(pageContext);
		out.writeText(text);
		out.flushBuffer();
	}

	/**
	 * Renders the given resource as HTML to the current page.
	 */
	public void renderHtml(PageContext pageContext, ResKey key) throws IOException {
		renderHtml(pageContext, res(key));
	}

	/**
	 * Renders the given HTML to the current page.
	 */
	public void renderHtml(PageContext pageContext, String html) throws IOException {
		TagWriter out = MainLayout.getTagWriter(pageContext);
		try {
			SafeHTML.getInstance().check(html);
			out.writeContent(html);
			out.flushBuffer();
		} catch (I18NException ex) {
			renderText(pageContext, ex.getErrorKey());
		}
	}

	/**
	 * Renders a {@link CommandHandler} as button on a maintenance JSP page.
	 */
	@CalledFromJSP
	public static void renderButton(PageContext pageContext, Class<? extends CommandHandler.Config> configType,
			Class<? extends CommandHandler> handlerType, Object commandId) throws IOException {
		CommandHandler.Config handlerConfig = TypedConfiguration.newConfigItem(configType);
		handlerConfig.setImplementationClass(handlerType);
		handlerConfig.update(handlerConfig.descriptor().getProperty(CommandHandler.Config.ID_PROPERTY), commandId);
		CommandHandler handler = TypedConfigUtil.createInstance(handlerConfig);
		LayoutComponent component = MainLayout.getComponent(pageContext);
		CommandModel command = CommandModelFactory.commandModel(handler, component);
		ButtonControl button = new ButtonControl(command, DefaultButtonRenderer.INSTANCE);
		TagWriter out = MainLayout.getTagWriter(pageContext);
		button.write(DefaultDisplayContext.getDisplayContext(pageContext), out);
		out.flushBuffer();
	}

}

