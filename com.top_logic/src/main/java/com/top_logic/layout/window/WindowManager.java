/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.window;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.top_logic.base.services.simpleajax.AJAXCommandHandler;
import com.top_logic.base.services.simpleajax.JSSnipplet;
import com.top_logic.basic.Configuration;
import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.Logger;
import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.annotation.FrameworkInternal;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.xml.TagUtil;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.AbstractDisplayValue;
import com.top_logic.layout.CompositeContentHandler;
import com.top_logic.layout.ContentHandler;
import com.top_logic.layout.DefaultUpdateQueue;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.DisplayUnit;
import com.top_logic.layout.FrameScope;
import com.top_logic.layout.URLBuilder;
import com.top_logic.layout.UpdateQueue;
import com.top_logic.layout.WindowScope;
import com.top_logic.layout.internal.WindowHandler;
import com.top_logic.layout.internal.WindowId;
import com.top_logic.layout.internal.WindowRegistry;
import com.top_logic.layout.scripting.action.ActionFactory;
import com.top_logic.layout.scripting.recorder.ScriptingRecorder;
import com.top_logic.layout.window.WindowCloseActionOp.WindowCloseAction;
import com.top_logic.mig.html.HTMLUtil;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.mig.html.layout.Layout;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.LayoutUtils;
import com.top_logic.mig.html.layout.MainLayout;
import com.top_logic.mig.html.layout.ModelEventListener;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Management of external windows.
 * 
 * <p>
 * The {@link WindowComponent} cares (via onUnload()) for the other side of the
 * (JS-based) communication.
 * </p>
 * 
 * <h3>Events, this implementation must deal with</h3>
 * 
 * <dl>
 * <dt>Opening a new window</dt>
 * <dd>The reference of the newly opened window is stored on the client in the
 * global object <code>allWindows</code> indexed by the server-side window name.
 * </dd>
 * 
 * <dt>Routing events to components in windows</dt>
 * <dd>The client-side component reference is contructed as
 * <code>reference-to-the-management-component</code> -> <code>allWindows</code>
 * -> <code>name-of-the-window</code> ->
 * <code>reference-to-the-target-component</code>.</dd>
 * 
 * <dt>Programmatically closing a window</dt>
 * <dd>The window is destroyed on the server and a
 * <code>allWindows.window-name.close()</code> snippet is executed on the
 * client.</dd>
 * 
 * <dt>Closing windows upon user request</dt>
 * <dd>The user may close all external windows by clicking on the window's
 * system close button. The server must make sure
 * <ul>
 * <li>to destroy the corresponding server-side component and</li>
 * <li>not to send any further events to components in the closed window.</li>
 * </ul>
 * The client-side code must not rely on the <code>onunload</code> event,
 * because this event is suppressed in IE, if the window was closed before the
 * loading of all components in the window is completed. Even if an
 * <code>onunload</code> event <b>is</b> fired, one cannot be sure that the
 * corresponding window has been closed, because this event is also fired, if
 * the window contents is only reloaded.</dd>
 * 
 * <dt>Reloading a separate window</dt>
 * <dd>
 * 
 * </dd>
 * 
 * <dt>Reloading the main page</dt>
 * <dd>
 * 
 * </dd>
 * </dl>
 * 
 * 
 * <h3>Browser details</h3>
 * 
 * <dl>
 * <dt>Events</dt>
 * <dd>After a window has been opened through the <code>window.open()</code>
 * function, and after the loading of all frames of the opened window has
 * completed, an <code>onload</code> event is fired in the open window.
 * Immediately when closing a browser window, an <code>onunload</code> event is
 * fired. At the time, the <code>onunload</code> event is executed, the
 * <code>closed</code> property of the window being closed is still
 * <code>false</code>.</dd>
 * 
 * <dt>Event ordering</dt>
 * <dd>Normally, the <code>onload</code> event is fired after the loading of the
 * window contents completes and before the window is closed and the
 * corresponding <code>onunload</code> event is fired. If loading window
 * contents is slow, the user is able to close the window before the
 * <code>onload</code> event has been fired. If this happens, the behavior of IE
 * and FF differs: In IE, neither the <code>onload</code> nor the
 * <code>onunload</code> event is fired for the corresponding window. In FF, the
 * <code>onunload</code> event is fired <b>before</b> the <code>onload</code>
 * event.</dd>
 * 
 * <dt>Access restrictions for closed window objects</dt>
 * <dd>After a window has been closed (either programatically, or by user
 * interaction), IE denies all access to the corresponding window object except
 * for the <code>closed</code> property. In FF, no access violation is thrown,
 * but properties return <code>null</code>.</dd>
 * 
 * <dt>Re-attaching windows</dt>
 * <dd>There is no way to query all currently open windows from JavaScript. If
 * the window opener is reloaded, there is no longer any information about its
 * currently open windows. The only way to get a fresh reference to an already
 * open window is to call the <code>window.open()</code> function with an empty
 * URL and the name of the still open window. However, if the corresponding
 * window has been closed in the meantime, a fresh window with the
 * <code>about:blank</code> location is opened. Whether a fresh window was
 * opened, or a reference to the existing window was returned, can be decided by
 * checking the <code>document.location</code> property.</dd>
 * 
 * <dt>Distinguishing between window reload and window close</dt>
 * <dd>An <code>onunload</code> event is fired if the window has been closed
 * <b>or</b> its contents has been reloaded (by e.g. pressing F5). Only in FF,
 * the opener of a window can reliably distinguish a reload from a close event
 * by setting up a timer function from the <code>onunload</code> handler that
 * tests the <code>window.closed</code> property. If the window is still marked
 * open at the time the timer function is run, the <code>onunload</code> event
 * was fired in response to a reload. Otherwise, the <code>onunload</code> was
 * triggered by closing the window. This works reliably in FF, because both, the
 * <code>onunload</code> event handler and the timer function are executed in
 * the same thread. However, in IE, the <code>onunload</code> handler and the
 * timer function are executed in separate threads. Without further
 * "synchronization", if the <code>onunload</code> handler is slow, the window
 * may still be observed as open form the timer function.</dd>
 * 
 * </dl>
 * 
 * "_e_" Is the prefix for the Ids of Layouts contained in this class.
 * 
 * @author <a href="mailto:twi@top-logic.com">twi</a>
 * 
 * @since 5.7
 */
public class WindowManager extends WindowRegistry<WindowHandler> {

	private static final ComponentName GLOBAL_WINDOW_CONTAINER_NAME =
		ComponentName.newName(LayoutUtils.MAINTABBAR_NAME_SCOPE, "mainTabber");

	/**
	 * time in milliseconds how long external windows shall still be open after
	 * leaving the application using an external link or bookmark (no explicit
	 * logout occurred).
	 */
	private static final int WINDOW_CLOSE_TIMEOUT;
	
	/**
	 * configuration property to set {@link #WINDOW_CLOSE_TIMEOUT}.
	 */
	private static final String WINDOW_CLOSE_TIMEOUT_PROPERTY_NAME = "windowCloseTimeout";

	static {
		int timeOut;
		int defaultTimeout = 5000;
		try {
			timeOut = Configuration.getConfiguration(WindowManager.class).getInteger(WINDOW_CLOSE_TIMEOUT_PROPERTY_NAME, defaultTimeout);
		} catch (ConfigurationException ex) {
			Logger.error("configuration of '" + WINDOW_CLOSE_TIMEOUT_PROPERTY_NAME + "' faulty.", ex, WindowManager.class);
			timeOut = defaultTimeout;
		}
		WINDOW_CLOSE_TIMEOUT = timeOut;
	}

	/** The list of {@link WindowComponent}s. */
	private final ArrayList<WindowComponent> openedWindows;

	/**
	 * Storage of client actions to bring to GUI when AJAX command occurred.
	 */
	private final DefaultUpdateQueue updates = new DefaultUpdateQueue();

	/**
	 * Keeps the names of closed windows until either the window container is
	 * reloaded or revalidated. This is a workaround for bringing the
	 * client-side in sync with the server model in case, a window was closed
	 * from a non-AJAX command.
	 */
	private final ArrayList<String> closedWindows;

	private final Layout holder;

	/**
	 * De-serializing constructor.
	 */
	public WindowManager(final Layout holder) {
		super(WindowHandler.class);

		this.holder = holder;
		openedWindows = new ArrayList<>();
		closedWindows = new ArrayList<>();
	}

	/**
	 * Close the given window.
	 * 
	 * This method generates a JavaScript-Snippet that is send back to the
	 * client.
	 */
	public void closeWindow(WindowComponent window) {
		int index = openedWindows.indexOf(window);
		if (index < 0) {
			// Window already closed.
			return;
		}

		// Note: Fetch the window name *before* the window is dropped, because the name can
		// otherwise not be computed.
		String closedWindowName = getWindowName(window);

		window.setVisible(false);

		// Delete the server-side reference.
		dropWindow(index, window);

		// Update the client.
		updates.add(new JSSnipplet(new AbstractDisplayValue() {
			@Override
			public void append(DisplayContext context, Appendable out) throws IOException {
				out.append("winCloseWindow(");
				TagUtil.writeJsString(out, closedWindowName);
				out.append(");");
			}
		}));

		closedWindows.add(closedWindowName);
	}

	/**
	 * Clears the slot in {@link #openedWindows} with index <code>index</code>, and
	 * removes the given window from the {@link #getHolder() holder}.
	 */
	private void dropWindow(int index, WindowComponent window) {
		openedWindows.remove(index);
		window.setVisible(false);
		window.notifyClosed();
		
		// De-register the windows content handler from the global URL namespace.
		window.getEnclosingFrameScope().dropUrlContext();
	}

	/**
	 * Pushes all client side updates into the given {@link UpdateQueue update
	 * queue}.
	 * 
	 * @param actions
	 *        the queue to add actions to
	 */
	public void revalidate(UpdateQueue actions) {
		updates.revalidate(actions);
		closedWindows.clear();
	}

	/**
	 * Sends a focus request to the given window.
	 */
	public boolean focusWindow(final WindowComponent window) {
		int index = openedWindows.indexOf(window);
		if (index < 0) {
			// Window not found..
			return false;
		}

		// Send the focus request.
		updates.add(new JSSnipplet(new AbstractDisplayValue() {
			@Override
			public void append(DisplayContext context, Appendable out) throws IOException {
				out.append("winFocusWindow(");
				WindowManager.this.appendJSWindowName(out, window);
				out.append(");");
			}
		}));

		return true;
	}

	/**
	 * Returns the name of the given window.
	 * 
	 * @see #appendJSWindowName(Appendable, WindowComponent)
	 */
	private String getWindowName(WindowComponent window) {
		try {
			StringBuilder out = new StringBuilder();
			appendWindowName(out, window, false);
			return out.toString();
		} catch (IOException ex) {
			throw new UnreachableAssertion("StringBuilder dows not throw exception.", ex);
		}
	}

	/**
	 * Appends the name of the given window as JS string.
	 * 
	 * @see #getWindowName(WindowComponent)
	 */
	void appendJSWindowName(Appendable out, WindowComponent window) throws IOException {
		appendWindowName(out, window, true);
	}

	private void appendWindowName(Appendable out, WindowComponent window, boolean asJSString) throws IOException {
		MainLayout mainLayout = window.getMainLayout();
		String mainWindowName = mainLayout.getLayoutContext().getWindowId().getWindowName();
		String windowName = window.getWindowName();
		if (asJSString) {
			TagUtil.beginJsString(out);
			TagUtil.writeJsStringContent(out, mainWindowName);
			out.append("_");
			TagUtil.writeJsStringContent(out, windowName);
			TagUtil.endJsString(out);
		} else {
			out.append(mainWindowName);
			out.append("_");
			out.append(windowName);
		}
	}

	public WindowComponent openWindow(DisplayContext context, LayoutComponent opener, String templateName) {
		// Get the window template.
		WindowTemplate template = (WindowTemplate) opener.getWindowTemplate(templateName);

		return openWindow(context, opener, template);
	}

	public WindowComponent openWindow(DisplayContext context, LayoutComponent opener, WindowTemplate template) {
		// Check WindowInfo, if window does reuse existing window.

		ComponentName windowName;

		WindowComponent window = null;
		boolean multi = template.getWindowInfo().getMultipleWindows();
		if (!multi) {
			// Only create new Window when not already existent.
			windowName = template.getWindowName();
			window = getWindowByName(windowName);
		}

		if (window == null) {
			// Create and open a new window.
			InstantiationContext instantiationContext = new DefaultInstantiationContext(WindowManager.class);
			window = instantiationContext.deferredReferenceCheck(() -> {
				WindowComponent newWin = template.instantiate(instantiationContext, opener);
				newWin.accessibleComponentsResolved(instantiationContext);
				return newWin;
			});
			try {
				instantiationContext.checkErrors();
			} catch (ConfigurationException ex) {
				throw new ConfigurationError(ex);
			}

			registerWindow(window);
			MainLayout ml = window.getMainLayout();
			ml.setupRelations(instantiationContext);
			try {
				instantiationContext.checkErrors();
			} catch (ConfigurationException ex) {
				throw new ConfigurationError(ex);
			}
			
			installURLContext(window);
			
            openWindow(context, window);
		} else {
			focusWindow(window);
			window.getChild(0).invalidate();
		}

		// Send window opening event.
		opener.doFireModelEvent(window, opener, ModelEventListener.WINDOW_OPENED);

		return window;
	}

	/**
	 * Adds updates to open the given {@link WindowComponent window}
	 * 
	 * @param context
	 *        context in which command execution occurs
	 * @param window
	 *        the window to open. must not be <code>null</code>
	 */
	private void openWindow(DisplayContext context, final WindowComponent window) {
		final String url = getWindowURL(context, window);
		final WindowInfo windowInfo = window.getWindowInfo();

		DisplayDimension width = windowInfo.getWidth();
		DisplayDimension height = windowInfo.getHeight();

		if (width.getUnit() != DisplayUnit.PIXEL || height.getUnit() != DisplayUnit.PIXEL) {
			throw new RuntimeException("window.open API supports only the pixel unit for its height and width.");
		}

		updates.add(new JSSnipplet(new AbstractDisplayValue() {
			@Override
			public void append(DisplayContext resultContext, Appendable out) throws IOException {
				out.append("winOpenWindow(");
				WindowManager.this.appendJSWindowName(out, window);
				out.append(", ");
				TagUtil.writeJsString(out, url);
				out.append(", ");
				out.append(String.valueOf(width.getValue()));
				out.append(", ");
				out.append(String.valueOf(height.getValue()));
				out.append(", ");
				out.append("/*hasLocation*/false, ");
				out.append("/*hasStatus*/false, ");
				out.append("/*isResizable*/true, ");
				out.append("/*hasScrollbars*/false, ");
				out.append("/*hasToolbar*/false);");
			}

		}));
	}

	/**
	 * Installs the URLContext of the {@link FrameScope} corresponding to the
	 * given {@link LayoutComponent}.
	 * 
	 * Must be corresponding to {@link #getWindowURL(DisplayContext, LayoutComponent)}
	 * 
	 * @see #getWindowURL(DisplayContext, LayoutComponent)
	 */
	private void installURLContext(WindowComponent window) {
		final CompositeContentHandler toplevelContext = this;
		String windowName = getWindowName(window);
		final WindowHandler windowHandler = new WindowHandler(toplevelContext, new WindowId(windowName)) {

			@Override
			protected void handleDeregister(ContentHandler handler) {
				toplevelContext.deregisterContentHandler(this);
				super.handleDeregister(handler);
			}

		};
		toplevelContext.registerContentHandler(windowName, windowHandler);
		
		window.getEnclosingFrameScope().setUrlContext(windowHandler);
	}
	
	/**
	 * Returns the URL for the given component.
	 */
	private String getWindowURL(DisplayContext context, LayoutComponent comp) {
		return comp.getEnclosingFrameScope().getURL(context).getURL();
	}

	/**
	 * Add a window to the childList
	 * @param newWindow
	 *        the new {@link WindowComponent} to add.
	 */
	private void registerWindow(WindowComponent newWindow) {
		openedWindows.add(newWindow);

		newWindow.notifyOpened(this);

		newWindow.setVisible(true);

		// Do not send a reload, before the window has opened.
		newWindow.resetForReload();
	}

	/**
	 * Return the opened {@link WindowComponent} with the given name.
	 * 
	 * <p>
	 * The name of a {@link WindowComponent} is assigned at creation time.
	 * </p>
	 * 
	 * @see #openWindow(DisplayContext, LayoutComponent, String)
	 */
	public WindowComponent getWindowByName(ComponentName windowName) {
		for (int n = 0, cnt = openedWindows.size(); n < cnt; n++) {
			WindowComponent window = openedWindows.get(n);
			if (windowName.equals(window.getName())) {
				return window;
			}
		}
		return null;
	}

	/**
	 * Return all opened windows.
	 * 
	 * <p>
	 * <b>Attention:</b> The list is memory optimized and may contain empty slots.
	 * </p>
	 * 
	 * @return An unmodifiable list containing all {@link WindowComponent}. The returned list may
	 *         contain one or more <code>null</code>.
	 */
	@FrameworkInternal
	public List<WindowComponent> getWindows() {
		return Collections.unmodifiableList(openedWindows);
	}

	/**
	 * Appends a reference to come from the window of the document represented
	 * by the {@link MainLayout#getEnclosingFrameScope() frame scope} of the
	 * {@link #getHolder() corresponding component} to the window to the
	 * document represented by the top level frame scope of the given
	 * {@link WindowScope} if the windowScope was opened by this
	 * {@link WindowManager}.
	 * 
	 * <p>
	 * If the given {@link WindowScope} is not the scope of a
	 * {@link WindowComponent} opened by this {@link WindowManager}, nothing is
	 * appended.
	 * </p>
	 * 
	 * 
	 * @param out
	 *        the {@link Appendable} to add reference to
	 * @param openedWindow
	 *        the window to which the reference shall point
	 * @return a reference to the given Appendable
	 * @throws IOException
	 *         iff the {@link Appendable} throws some
	 */
	public <T extends Appendable> T addWindowReference(T out, WindowScope openedWindow) throws IOException {
		for (int index = 0, size = openedWindows.size(); index < size; index++) {
			final WindowComponent component = openedWindows.get(index);
			if (component.getEnclosingFrameScope().getWindowScope().equals(openedWindow)) {
				out.append(".winAllWindows[");
				appendJSWindowName(out, component);
				out.append("].ref");
				break;
			}
		}
		return out;
	}

	/**
	 * Appends a js function to the given {@link Appendable out} whose execution
	 * will close all currently opened separate windows.
	 * 
	 * @param out
	 *        the {@link Appendable} to append content to.
	 * @param source
	 *        the {@link FrameScope} which wants to write that js function
	 * @throws IOException
	 *         if <code>out</code> throws some
	 */
	public void appendCloseAllWindows(Appendable out, FrameScope source) throws IOException{
		out.append(LayoutUtils.getFrameReference(source, getHolder().getEnclosingFrameScope()));
		out.append(".winCloseAllOpenWindows();");
	}

	/**
	 * Returns the Layout which serves as parent for all the windows opened by
	 * this {@link WindowManager}.
	 */
	private Layout getHolder() {
		return holder;
	}

	/**
	 * Notifies the window manager about a client-side window close operation
	 * issued by the user.
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	public static class OnCloseWindow extends AJAXCommandHandler {

		private static final String NAME_PARAM = "name";
		public static final String COMMAND_ID = "onWindowClose";

		/**
		 * Singleton constructor.
		 */
		public OnCloseWindow(InstantiationContext context, Config config) {
			super(context, config);
		}

		@Override
		public HandlerResult handleCommand(DisplayContext aContext, LayoutComponent aComponent, Object model, Map<String, Object> someArguments) {
			String clientWindowName = (String) someArguments.get(NAME_PARAM);
			WindowManager manager = ((MainLayout) aComponent).getWindowManager();
			WindowComponent window = manager.onWindowClose(clientWindowName);
			if (ScriptingRecorder.isRecordingActive()) {
				if (window != null) {
					WindowCloseAction windowCloseAction = ActionFactory.newComponentAction(WindowCloseAction.class,
						WindowCloseActionOp.class, aComponent);
					windowCloseAction.setCloseWindowName(window.getName());
					ScriptingRecorder.recordAction(windowCloseAction);
				}
			}

			return HandlerResult.DEFAULT_RESULT;
		}

		@Override
		protected boolean mustNotRecord(DisplayContext context, LayoutComponent component,
				Map<String, Object> someArguments) {
			/* Do not record command, because in the default application action the NAME_PARAM
			 * contains the client side window name which contains a synthetic generic id. This id
			 * can not resolved. */
			return true;
		}

	}

	/* package protected */WindowComponent onWindowClose(String name) {
		for (int n = 0, cnt = openedWindows.size(); n < cnt; n++) {
			WindowComponent window = openedWindows.get(n);

			String windowName = getWindowName(window);
			if (name.equals(windowName)) {
				dropWindow(n, window);
				return window;
			}
		}
		return null;
	}
	
	public void writeInOnUnload(DisplayContext context, TagWriter out) throws IOException {
		HTMLUtil.writeJavaScriptContent(out, "winCloseDelayed(" + WINDOW_CLOSE_TIMEOUT + ");");
	}

	public void writeInOnload(DisplayContext context, TagWriter out) throws IOException {

		for (int n = 0, cnt = closedWindows.size(); n < cnt; n++) {
			String name = closedWindows.get(n);

			out.writeIndent();
			out.append("winConnectAndCloseWindow(");
			out.writeJsString(name);
			out.append(");");
		}
		closedWindows.clear();

		// Establish references to all open windows. All closed windows are
		// reopened.
		for (Iterator<WindowComponent> it = openedWindows.iterator(); it.hasNext();) {
			WindowComponent window = it.next();

            String url = getWindowURL(context, window);
			WindowInfo windowInfo = window.getWindowInfo();

			out.writeIndent();
			out.append("winConnectWindow(");
			WindowManager.this.appendJSWindowName(out, window);
			out.append(", ");
			out.writeJsString(url);
			out.append(", ");
			out.append(String.valueOf(windowInfo.getWidth().getValue()));
			out.append(", ");
			out.append(String.valueOf(windowInfo.getHeight().getValue()));
			out.append(", ");
			out.append("/*hasLocation*/false, ");
			out.append("/*hasStatus*/false, ");
			out.append("/*isResizable*/true, ");
			out.append("/*hasScrollbars*/true, ");
			out.append("/*hasToolbar*/false);");
		}

		HTMLUtil.writeJavaScriptContent(out, "winStopClosing();");
	}

	public static WindowComponent openGlobalWindow(DisplayContext context, LayoutComponent opener, String windowName) {
		MainLayout mainLayout = opener.getMainLayout();
		LayoutComponent globalWindows = mainLayout.getComponentByName(GLOBAL_WINDOW_CONTAINER_NAME);

		WindowTemplate windowTemplate = (WindowTemplate) globalWindows.getWindowTemplate(windowName);
		WindowManager manager = mainLayout.getWindowManager();

		return manager.openWindow(context, opener, windowTemplate);
	}

	@Override
	public URLBuilder getURL(DisplayContext context) {
		return holder.getEnclosingFrameScope().getURL(context, this);
	}
}