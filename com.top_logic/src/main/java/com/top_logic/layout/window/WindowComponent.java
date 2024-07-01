/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.window;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.top_logic.base.services.simpleajax.JSSnipplet;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.listener.EventType.Bubble;
import com.top_logic.basic.version.Version;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.ControlScope;
import com.top_logic.layout.WindowScope;
import com.top_logic.layout.WindowScopeProvider;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.basic.component.AJAXControlSupport;
import com.top_logic.layout.basic.component.AJAXSupport;
import com.top_logic.layout.basic.component.BasicAJAXSupport;
import com.top_logic.layout.basic.component.ControlComponent.DispatchAction;
import com.top_logic.layout.basic.component.ControlSupport;
import com.top_logic.layout.form.model.VisibilityModel;
import com.top_logic.layout.internal.WindowRegistry;
import com.top_logic.layout.structure.BrowserWindowControl;
import com.top_logic.layout.structure.LayoutFactory;
import com.top_logic.layout.window.WindowTemplate.WindowConfig;
import com.top_logic.mig.html.HTMLUtil;
import com.top_logic.mig.html.layout.CommandRegistry;
import com.top_logic.mig.html.layout.DialogSupport;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.LayoutComponentScope;
import com.top_logic.mig.html.layout.LayoutContainer;
import com.top_logic.mig.html.layout.MainLayout;
import com.top_logic.mig.html.layout.TopLevelComponentScope;
import com.top_logic.mig.html.layout.VisibilityListener;
import com.top_logic.tool.boundsec.BoundLayout;

/**
 * Top-level component of a layout sub-tree that is displayed in a browser window.
 * 
 * <p>
 * This component is automatically wrapped around a {@link LayoutComponent}, if it is being
 * {@link WindowManager#openWindow(com.top_logic.layout.DisplayContext, LayoutComponent, String)
 * opened in a window}.
 * </p>
 * 
 * @see WindowManager Manage for open windows.
 * 
 * @author <a href="mailto:twi@top-logic.com">twi</a>
 */
public class WindowComponent extends BoundLayout implements VisibilityListener, WindowScopeProvider {

	/**
	 * Configuration for the {@link WindowComponent}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends BoundLayout.Config, WindowConfig {

		/** @see com.top_logic.basic.reflect.DefaultMethodInvoker */
		Lookup LOOKUP = MethodHandles.lookup();

		@Override
		default void modifyIntrinsicCommands(CommandRegistry registry) {
			BoundLayout.Config.super.modifyIntrinsicCommands(registry);
			registry.registerCommand(DispatchAction.COMMAND_NAME);
		}

		@Override
		@ClassDefault(WindowComponent.class)
		Class<? extends LayoutComponent> getImplementationClass();

	}

	/**
	 * The {@link LayoutComponent} that opened this window.
	 */
	private VisibilityModel _visibilityModel;

	private final BasicAJAXSupport basicAJAXSupport = new BasicAJAXSupport();

	private final ControlSupport controlSupport = new AJAXControlSupport(this, basicAJAXSupport);
	private BrowserWindowControl layoutControl;
	private WindowManager manager;

	/**
	 * @see WindowComponent#getWindowName()
	 */
	private String _endcodedWindowName;

	/** @see #getDialogSupport() */
	private DialogSupport _dialogSupport;

	public WindowComponent(InstantiationContext context, Config atts) throws ConfigurationException {
		super(context, atts);

	}

	/**
	 * The component that opened this window.
	 * 
	 * @return May be <code>null</code> if this {@link WindowComponent} was not opened by a
	 *         {@link LayoutComponent}.
	 */
	public LayoutComponent getOpener() {
		return _visibilityModel instanceof LayoutComponent ? (LayoutComponent) _visibilityModel : null;
	}

	@Override
	public boolean isVisible() {
		return _isVisible();
	}

	/**
	 * @see #getOpener()
	 */
	public void setOpener(LayoutComponent opener) {
		init(opener, opener != null ? opener.getMainLayout() : null);
	}

	/**
	 * Initialises this {@link WindowComponent}.
	 *
	 * @param visibilityModel
	 *        A {@link VisibilityModel} to observe. If {@link #closeIfParentBecomesInvisible()} then
	 *        this window is closed when the visibility model becomes invisible. May be
	 *        <code>null</code> if not {@link #closeIfParentBecomesInvisible()}.
	 * @param mainLayout
	 *        The {@link MainLayout} where the component is registered to.
	 */
	public void init(VisibilityModel visibilityModel, MainLayout mainLayout) {
		boolean closeOnParentInvisibility = closeIfParentBecomesInvisible();
		if (closeOnParentInvisibility && (_visibilityModel != null)) {
			_visibilityModel.removeListener(LayoutComponent.VISIBILITY_EVENT, this);
		}
		registerMainLayout(mainLayout);
		_visibilityModel = visibilityModel;
		if (closeOnParentInvisibility && (_visibilityModel != null)) {
			_visibilityModel.addListener(LayoutComponent.VISIBILITY_EVENT, this);
		}
	}

	private boolean closeIfParentBecomesInvisible() {
		return this.getWindowInfo().getCloseIfParentBecomesInvisible();
	}

	@Override
	public Bubble handleVisibilityChange(Object sender, Boolean oldVisibility, Boolean newVisibility) {
		if (sender != _visibilityModel) {
			return Bubble.BUBBLE;
		}
		if (!closeIfParentBecomesInvisible()) {
			return Bubble.BUBBLE;
		}
		if (!newVisibility) {
			close();
		}
		return Bubble.BUBBLE;
	}
	
	@Override
	protected void becomingInvisible() {
		super.becomingInvisible();
		
		if (closeIfParentBecomesInvisible()) {
			// de-register from further notifications. Re-opening the window will
			// again add the listener.
			_visibilityModel.removeListener(LayoutComponent.VISIBILITY_EVENT, this);
		}
	}

	/**
	 * The {@link WindowInfo} for this component.
	 * 
	 * @see WindowComponent.Config#getWindowInfo()
	 */
	public WindowInfo getWindowInfo() {
		return ((Config) getConfig()).getWindowInfo();
	}

	@Override
	public void writeBody(ServletContext context, HttpServletRequest req, HttpServletResponse resp, TagWriter out) throws IOException,
			ServletException {
		layoutControl.detach();
		layoutControl.write(DefaultDisplayContext.getDisplayContext(req), out);
	}
	
	@Override
	protected void writeJSTags(String contextPath, TagWriter out, HttpServletRequest request) throws IOException {
		super.writeJSTags(contextPath, out, request);
		MainLayout.writeTopLevelComponentJSTags(contextPath, out, request);
	}
	
	@Override
	public void writeJavaScript(String contextPath, TagWriter out, HttpServletRequest req) throws IOException {
		super.writeJavaScript(contextPath, out, req);
		
		HTMLUtil.writeJavaScriptContent(out, "var winAttached = false;");
		HTMLUtil.writeJavaScriptContent(out, "function winDetach() {");
		HTMLUtil.writeJavaScriptContent(out, "	winAttached = false;");
		HTMLUtil.writeJavaScriptContent(out, "}");
		HTMLUtil.writeJavaScriptContent(out, "function winDetachAndClose() {");
		HTMLUtil.writeJavaScriptContent(out, "	winAttached = false;");
		HTMLUtil.writeJavaScriptContent(out, "	window.close();");
		HTMLUtil.writeJavaScriptContent(out, "}");
		
		// Mark top-level window with the application name to allow the login
		// page to close all openers that belong to the same application.
		out.writeIndent();
		out.append("services.appName = ");
		out.writeJsString(Version.getApplicationName());
		out.append(";");
	}
	
	/**
	 * Notify opener that window has completed loading.
	 */
	@Override
	protected void writeInOnload(String context, TagWriter out, HttpServletRequest request) throws IOException {
		HTMLUtil.writeJavaScriptContent(out, "opener.winOnLoadWindow(window)");
		HTMLUtil.writeJavaScriptContent(out, "winAttached = true;");

		// This code will be called, while the window component writes the header for the
		// current browser window document.
		// After that the browser window control will just be called to write the document body.
		// So in current state it does not have any id, because it is not attached. Therefore an
		// explicit id fetch is necessary.
		// As soon as the ajax-communication is fully switched to the responsible controls, this
		// id fetch can be removed.
		BrowserWindowControl bwc = getLayoutControl();
		bwc.fetchID(getEnclosingFrameScope());
		String windowId = bwc.getID();
		HTMLUtil.writeJavaScriptContent(out, "services.form.installKeyEventHandler('" + windowId + "');");
	}

	/**
	 * Notify opener that window is being closed or reloaded.
	 */
	@Override
	protected void writeInOnUnload(String aContext, TagWriter out, HttpServletRequest aRequest) throws IOException {
		HTMLUtil.writeJavaScriptContent(out, "if (winAttached) {");
		// Black magic to cope with browsers form the dark side.
		HTMLUtil.writeJavaScriptContent(out, "	opener.winLockWindow(window)");
		HTMLUtil.writeJavaScriptContent(out, "	opener.winOnUnloadWindow(window)");
		HTMLUtil.writeJavaScriptContent(out, "	opener.winUnlockWindow(window)");
		HTMLUtil.writeJavaScriptContent(out, "}");
	}

	/**
	 * Closes this window. Actually just calls
	 * {@link WindowManager#closeWindow(WindowComponent)} of
	 * {@link #getWindowManager()}.
	 */
	public final void close() {
		if (getWindowManager() == null) {
			throw new IllegalStateException("Trying to close a window without WindowManager");
		}
		getWindowManager().closeWindow(this);
	}
	
	/**
	 * Make {@link #componentsResolved(InstantiationContext)} accessible to be able to call it during the window loading
	 * process from {@link WindowManager}.
	 * 
	 * @see LayoutContainer#componentsResolved(InstantiationContext)
	 */
	final void accessibleComponentsResolved(InstantiationContext context) {
		resolveComponent(context);
	}

	@Override
	protected void componentsResolved(InstantiationContext context) {
		super.componentsResolved(context);

		LayoutFactory layoutFactory = getMainLayout().getLayoutFactory();
		layoutControl = (BrowserWindowControl) layoutFactory.createLayout(this);
		_dialogSupport = new DialogSupport(layoutControl);
		_endcodedWindowName = WindowRegistry.encodeWindowName(getName().qualifiedName());
	}

	/**
	 * Encoded version of {@link #getName()} that is allowed to be contained within a client side
	 * window name.
	 * 
	 * @see WindowRegistry#encodeWindowName(String)
	 */
	String getWindowName() {
		return _endcodedWindowName;
	}

	@Override
	public ControlScope getControlScope() {
		return controlSupport;
	}

	@Override
	protected AJAXSupport ajaxSupport() {
		return controlSupport;
	}
	
	@Override
	protected LayoutComponentScope createEnclosingFrameScope() {
		return TopLevelComponentScope.createTopLevelComponentScope(this, this, basicAJAXSupport);
	}
	
	@Override
	public LayoutComponent getWindow() {
		return this;
	}

	@Override
	public WindowScope getWindowScope() {
		return getLayoutControl();
	}

	/**
	 * Informs that {@link WindowComponent} that it is requested to open
	 * 
	 * Sets the {@link WindowManager} which manages this {@link WindowComponent
	 * window}. That {@link WindowManager} is used when closing this window.
	 * 
	 * @param manager
	 *        the {@link WindowManager manager} which opens this
	 *        {@link WindowComponent}. must not be <code>null</code>
	 */
	public void notifyOpened(WindowManager manager) {
		this.manager = manager;
		
	}

	/**
	 * Informs that {@link WindowComponent} that it is requested to close.
	 */
	public void notifyClosed() {
		manager = null;
		if (layoutControl != null) {
			/*
			 * if the control is null, then this component was never displayed.
			 * This can occur if e.g. the window was blocked by a popup blocker.
			 */
			layoutControl.detach();
		}
		if (closeIfParentBecomesInvisible()) {
			// don't react if already closed
			_visibilityModel.removeListener(LayoutComponent.VISIBILITY_EVENT, this);
		}

		setOpener(null);
	}

	/**
	 * Returns the {@link WindowManager} set by
	 * {@link #notifyOpened(WindowManager)}
	 * 
	 * @see #notifyOpened(WindowManager)
	 */
	public WindowManager getWindowManager() {
		return manager;
	}

	/**
	 * The lazily created {@link BrowserWindowControl} which is responsible for this window.
	 */
	public BrowserWindowControl getLayoutControl() {
		return layoutControl;
	}

	@Override
	public DialogSupport getDialogSupport() {
		return _dialogSupport;
	}

	/**
	 * Reloads the complete window.
	 */
	public void reload() {
		getWindowScope().getTopLevelFrameScope().addClientAction(new JSSnipplet("window.location.reload();"));
	}

}