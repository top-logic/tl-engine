/*
 * SPDX-FileCopyrightText: 2003 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout;

import static com.top_logic.layout.processor.LayoutModelConstants.*;

import java.awt.Point;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.servlet.http.HttpServletRequest;

import com.top_logic.base.accesscontrol.ApplicationPages;
import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.AliasManager;
import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.Log;
import com.top_logic.basic.Logger;
import com.top_logic.basic.NoProtocol;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable.Property;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.ConfigurationWriter;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.Location;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.constraint.check.ConstraintChecker;
import com.top_logic.basic.config.constraint.check.ConstraintFailure;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.basic.exception.ErrorSeverity;
import com.top_logic.basic.io.BinaryContent;
import com.top_logic.basic.io.Content;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.shared.collection.map.MapUtilShared;
import com.top_logic.basic.tooling.ModuleLayoutConstants;
import com.top_logic.basic.tooling.Workspace;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.basic.vars.VariableExpander;
import com.top_logic.basic.xml.XMLPrettyPrinter;
import com.top_logic.gui.Theme;
import com.top_logic.gui.ThemeFactory;
import com.top_logic.knowledge.gui.layout.LayoutConfig;
import com.top_logic.knowledge.gui.layout.TLLayoutServlet;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.FrameScope;
import com.top_logic.layout.LinkGenerator;
import com.top_logic.layout.LinkGenerator.Handle;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.WindowScope;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.component.EmbeddedLayoutConfig;
import com.top_logic.layout.component.configuration.ComponentConfigEquality;
import com.top_logic.layout.component.title.TitleProvider;
import com.top_logic.layout.editor.I18NConstants;
import com.top_logic.layout.form.tag.js.JSNativeExpression;
import com.top_logic.layout.form.tag.js.JSValue;
import com.top_logic.layout.processor.LayoutModelConstants;
import com.top_logic.layout.structure.PopupDialogControl;
import com.top_logic.layout.structure.PopupDialogModel;
import com.top_logic.layout.tabbar.TabInfo.TabConfig;
import com.top_logic.layout.window.OpenWindowCommand;
import com.top_logic.mig.html.layout.WithGotoConfiguration.GotoTarget;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLType;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.tool.boundsec.BoundCommand;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.BoundLayout;
import com.top_logic.tool.boundsec.CheckerProxyHandler;
import com.top_logic.tool.boundsec.CheckerProxyHandler.Config;
import com.top_logic.tool.boundsec.CommandGroupReference;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.CommandHandler.ExecutabilityConfig;
import com.top_logic.tool.boundsec.CommandHandlerFactory;
import com.top_logic.tool.boundsec.CommandReferenceConfig;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.boundsec.OpenModalDialogCommandHandler;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;
import com.top_logic.util.Resources;
import com.top_logic.util.TLResKeyUtil;
import com.top_logic.util.error.TopLogicException;



/**
 * Helper und util functions for the layout package
 *
 * @author    <a href="mailto:dro@top-logic.com">Dieter Rothb&auml;cher</a>
 */
public class LayoutUtils {

	/**
	 * Key for a arguments map determining the position where to open a popup.
	 * 
	 * @see #openPopupAtPosition(DisplayContext, PopupDialogModel, Map, HTMLFragment)
	 */
	public static final String EVENT_POSITION_KEY = "eventPosition";

	/**
	 * Name scope for {@link ComponentName} defined in one of the layouts in
	 * {@link LayoutConfig#getLayouts()}.
	 */
	public static final String ROOT_LAYOUT_NAME_SCOPE = "rootLayout";

	/**
	 * Name scope for components inside the main tabbar.
	 */
	public static final String MAINTABBAR_NAME_SCOPE = "mainTabbar.layout.xml";

	/** Suffix to append to {@link ResPrefix} to get the "tabber" I18N Key. */
	public static final String TABBER = "tabber";

	private static final Property<WindowScope> WINDOW_SCOPE =
		TypedAnnotatable.property(WindowScope.class, "windowScope");
    
	private static final char FILE_PATH_SEPARATOR = '/';

	private static final Path LAYOUT_SUFFIX =
		Paths.get(ModuleLayoutConstants.WEBAPP_LOCAL_DIR_NAME, ModuleLayoutConstants.WEB_INF_DIR,
			ModuleLayoutConstants.LAYOUTS_DIR_NAME);

	/** 
     * Find the first parent common to both components. May return one of 
     * the components itself in case it's parent of the other.
     * 
     * @param   lc1   The first component
     * @param   lc2   The second component
     * 
     * @return null in case components are not in the same hierarchy.
     */
    public static LayoutComponent findCommonParent(LayoutComponent lc1,
                                                   LayoutComponent lc2) {

        // try trivial cases first.
        MainLayout main1 = lc1.getMainLayout();
        MainLayout main2 = lc2.getMainLayout();
        // different hierachies ?
        if (main1 != main2) {
            return null;    
        }
        // Perhaps one already is the MainLayout ...
        if (lc1 == main2) {
            return main2;
        }
        if (lc2 == main1) {
            return main1;
        }
        
        // both are the same 
        if (lc1 == lc2) {
            return lc1.getParent();
        }
            
        return findCommonParent(main1, lc1, getHierarchyLevel(lc1, main1), lc2, getHierarchyLevel(lc2, main1));    
    }
    
    /**
     * Get the hierarchy level from child component to ancestor component.<br/>
     * Examples:<br/>
     * 0 -> the ancestor itself<br/>
     * 1 -> direct child of ancestor<br/>
     * 2 -> grandchild of ancestor<br/>
     *
     * @param   child     The child component to get the hierarchy level for
     * @param   ancestor  The ancestor of the child
     * @return  the hierarchy level, -1 if given "ancestor" is 
     *          not an ancestor of given child
     */
    private static int getHierarchyLevel(LayoutComponent child, LayoutComponent ancestor) {
        int level = 0;
        while (child != ancestor) {
            level++;
            child = child.getParent();
            if (child == null) {
                return -1;
            } 
        }
        return level;
    }
    
    // Protected static methods

    /**
     * Helper function that must assume lc1 != lc2 and both
     * have the same given MainLayout.
     * 
     * @param   main    The main layout
     * @param   lc1     The first component
     * @param   level1  The hierarchy level of the first component
     * @param   lc2     The second component
     * @param   level2  The hierarchy level of the second component
     */
    private static LayoutComponent findCommonParent(MainLayout main,  
                                                      LayoutComponent lc1,
                                                      int level1, 
                                                      LayoutComponent lc2,
                                                      int level2) {

        LayoutComponent p1 = lc1.getParent();
        LayoutComponent p2 = lc2.getParent();
        if (p1 == lc2) {
            return lc2.getParent();
        }
        if (p2 == lc1) {
            return lc1.getParent();
        }
        if (p1 == p2) {
            return p1;
        }

        if (p1 == main || p2 == main) {
            return main;
        }
        
        if (level2 > level1) {
            return findCommonParent(main, lc1, level1, p2, level2--);
        }   
        if (level1 > level2) {
            return findCommonParent(main, p1, level1--, lc2, level2);
        } 
        return findCommonParent(main, p1, level1--, p2, level2--);
    }
    
    
    /**
     * Check if the given command corresponds to the command handler responsible
     * for opening the indicated dialog at the given component.
     * 
     * @param aCommand
     *            a command to check
     * @param anOpenHandlerName
     *            the open handler name used as configured in the corresponding
     *            dialog info section in the layout xml used the
     *            <code>openHandlerName</code> attribute
     * @param aLayoutComponent 
     *            the layout component containing the dialogs 
     */
    public static boolean isOpenHandlerFor(BoundCommand aCommand, String anOpenHandlerName, LayoutComponent aLayoutComponent) {
        String         theCommandId   = aCommand.getID();
        CommandHandler theOpenHandler = aLayoutComponent.getCommandById(anOpenHandlerName);
        if (theOpenHandler == null) {
            return false;
        } else {
            return theOpenHandler.getID().equals(theCommandId);
        }
    }

	/**
	 * Appends a client side reference form the window of the document
	 * represented by the top most enclosing window of <code>targetScope</code>
	 * down to the window of the document represented by
	 * <code>targetScope</code>.
	 * 
	 * @param out
	 *        the {@link Appendable} to append the reference
	 * @param targetScope
	 *        the scope to navigate from its top level frame scope to
	 * 
	 * @return a reference to the given {@link Appendable}
	 * 
	 * @throws IOException
	 *         iff the given {@link Appendable} throws some
	 */
    private static <T extends Appendable> T appendPathFromTopScope(T out, FrameScope targetScope) throws IOException {
    	if (targetScope != null) {
    		appendPathFromTopScope(out, targetScope.getEnclosingScope());
    		targetScope.appendClientReference(out);
    	}
    	return out;
    }
    
	/**
	 * Appends a reference from the window of the document represented by the
	 * top most {@link WindowScope#getOpener() opener} of the given
	 * <code>targetScope</code> to the window of the document represented by the
	 * {@link WindowScope#getTopLevelFrameScope() top level frame scope} of the
	 * given <code>targetScope</code>.
	 * 
	 * @param out
	 *        the {@link Appendable} to append the reference
	 * @param targetScope
	 *        the scope to navigate from its top most opener to. must not be <code>null</code>.
	 * 
	 * @return a reference to the given {@link Appendable}
	 * 
	 * @throws IOException
	 *         iff the given {@link Appendable} throws some
	 */
    private static <T extends Appendable> T appendPathFromTopOpener(T out, WindowScope targetScope) throws IOException {
    	final WindowScope opener = targetScope.getOpener();
    	if (opener != null) {
    		appendPathFromTopOpener(out, opener);
    		opener.appendReference(out, targetScope);
    	}
    	return out;
    }
    
	/**
	 * This method returns a client side reference from the window of the
	 * document represented by <code>source</code> to the window of the document
	 * represented by <code>target</code>.
	 */
	public static String getFrameReference(FrameScope source, FrameScope target) {
		if (source.equals(target)) {
			return "window";
		}

		final WindowScope sourceWindow = source.getWindowScope();
		final WindowScope targetWindow = target.getWindowScope();
		if (sourceWindow.equals(targetWindow)) {
			StringBuilder result = new StringBuilder("window");
			// climb up to the top level frame scope
			FrameScope sourceParent = source.getEnclosingScope();
			while (sourceParent != null) {
				result.append(".parent");
				sourceParent = sourceParent.getEnclosingScope();
			}
			try {
				appendPathFromTopScope(result, target);
			} catch (IOException ex) {
				throw new UnreachableAssertion("StringBuilder doesn't throw IOExceptions", ex);
			}
			return result.toString();
		}

		StringBuilder result = new StringBuilder("window");
		// climb up top level Frame scope
		FrameScope sourceParent = source.getEnclosingScope();
		while (sourceParent != null) {
			sourceParent = sourceParent.getEnclosingScope();
			result.append(".parent");
		}

		// climb up to topmost opener
		WindowScope sourceOpener = sourceWindow.getOpener();
		while (sourceOpener != null) {
			sourceOpener = sourceOpener.getOpener();
			result.append(".opener");
		}

		// go down to target window
		try {
			appendPathFromTopOpener(result, targetWindow);
		} catch (IOException ex) {
			throw new UnreachableAssertion("StringBuilder doesn't throw IOExceptions", ex);
		}

		// go down to correct frame scope
		try {
			appendPathFromTopScope(result, target);
		} catch (IOException ex) {
			throw new UnreachableAssertion("StringBuilder doesn't throw IOExceptions", ex);
		}

		return result.toString();
	}

	/**
	 * This method returns an JavaScript expression to come from the main layout
	 * to <code>targetComponent</code>.
	 */
	public static String createActionContext(LayoutComponent targetComponent) {
		// The context must be null, or a valid JavaScript expression, not the
		// empty string.
		MainLayout mainLayout = targetComponent.getMainLayout();
		if (mainLayout == targetComponent) {
			return "window";
		}
		return LayoutUtils.getFrameReference(mainLayout.getEnclosingFrameScope(), targetComponent.getEnclosingFrameScope());
	}
	
	/**  Map EventyTypes from {@link ModelEventListener} to readable String */
	private static final Map<Integer, String> EVENT_NAMES = initEventNames();
	
	private static Map<Integer, String> initEventNames() {
		Map<Integer, String> eventNames = new HashMap<>(16);
		eventNames.put(Integer.valueOf(ModelEventListener.DIALOG_CLOSED), "DIALOG_CLOSED");
		eventNames.put(Integer.valueOf(ModelEventListener.DIALOG_OPENED), "DIALOG_OPENED");
		eventNames.put(Integer.valueOf(ModelEventListener.GLOBAL_REFRESH), "GLOBAL_REFRESH");
		eventNames.put(Integer.valueOf(ModelEventListener.MODEL_CREATED), "MODEL_CREATED");
		eventNames.put(Integer.valueOf(ModelEventListener.MODEL_DELETED), "MODEL_DELETED");
		eventNames.put(Integer.valueOf(ModelEventListener.MODEL_MODIFIED), "MODEL_MODIFIED");
		eventNames.put(Integer.valueOf(ModelEventListener.SECURITY_CHANGED), "SECURITY_CHANGED");
		eventNames.put(Integer.valueOf(ModelEventListener.WINDOW_CLOSED), "WINDOW_CLOSED");
		eventNames.put(Integer.valueOf(ModelEventListener.WINDOW_OPENED), "WINDOW_OPENED");
		return eventNames;
	}

	/** Map EventyTypes from {@link ModelEventListener} to readable String */
	private static String mapEventType(int aType) {
		String result = EVENT_NAMES.get(Integer.valueOf(aType));
	    return result != null ? result : "UNKNOWN_" + aType;
	}
	
	/**
	 * Call this method to {@link Logger#debug(String, Object) debug} Event Problems.
	 */
	public static void logEvent(int eventType, Object changedBy, Object model, Object owner) {
        String type    = LayoutUtils.mapEventType(eventType);
		String changer = (changedBy instanceof LayoutComponent) ? ((LayoutComponent) changedBy).getName().qualifiedName()
			: String.valueOf(changedBy);
		String objName = String.valueOf(model);
        Logger.debug("logEvent(" + type + ":" + changer  + "," + objName + ")", owner);
	}

	/**
	 * Installs the component currently processed in give {@link DisplayContext}.
	 * 
	 * <p>
	 * Moreover the {@link WindowScope} of the context is installed.
	 * </p>
	 * 
	 * @param contextComponent
	 *        May be <code>null</code> to reset the context component.
	 */
	public static void setContextComponent(DisplayContext displayContext, LayoutComponent contextComponent) {
		displayContext.set(LayoutConstants.ATTRIBUTE_LAYOUT, contextComponent);
		WindowScope windowScope;
		if (contextComponent != null) {
			windowScope = contextComponent.getEnclosingFrameScope().getWindowScope();
		} else {
			// Context was reset.
			windowScope = null;
		}
		setWindowScope(displayContext, windowScope);
	}
	
	public static void setWindowScope(DisplayContext context, WindowScope windowScope) {
		context.set(WINDOW_SCOPE, windowScope);
	}
	
	public static WindowScope getWindowScope(DisplayContext context) {
		return context.get(WINDOW_SCOPE);
	}

	/**
	 * Create an exception aborting the current command with a message that informs the user about
	 * the current selection being deleted.
	 */
	public static TopLogicException createErrorSelectedObjectDeleted() {
		TopLogicException result =
			new TopLogicException(com.top_logic.layout.component.I18NConstants.ERROR_SELECTED_OBJECT_DELETED);
		result.initSeverity(ErrorSeverity.INFO);
		return result;
	}

	public static LayoutComponent instantiateSubComponents(InstantiationContext context, EmbeddedLayoutConfig config) {
		LayoutComponent.Config innerConfig;
		List<LayoutComponent.Config> componentConfigs = config.getComponents();
		if (componentConfigs.size() == 1) {
			innerConfig = componentConfigs.get(0);
		} else {
			Layout.Config layoutConfig = TypedConfiguration.newConfigItem(BoundLayout.Config.class);
			layoutConfig.setHorizontal(config.getHorizontal());
			layoutConfig.getComponents().addAll(componentConfigs);
			innerConfig = layoutConfig;
		}
		return context.getInstance(innerConfig);
	}

	public static DialogInfo createDialogInfo(boolean createOpenerButtons, ComponentName anOpeningButtonSecCompName,
			BoundCommandGroup openerCommandGroup, Class anOpenHandlerClass, String aHandlerName,
			DisplayDimension width, DisplayDimension height,
			ResKey defaultI18N, String executability, ThemeImage image, ThemeImage disabledImage) {

		DialogInfo result = TypedConfiguration.newConfigItem(DialogInfo.class);

		result.setWidth(width);
		result.setHeight(height);
		result.setDefaultI18N(defaultI18N);
		result.setCreateOpenerButtons(createOpenerButtons);
		result.setSecurityComponentName(anOpeningButtonSecCompName);
		result.setOpenerCommandGroup(openerCommandGroup);

		if (anOpenHandlerClass != null) {
			result.setOpenHandlerClass(anOpenHandlerClass);
		} else {
			result.setOpenHandlerClass(OpenModalDialogCommandHandler.class);
		}

		result.setOpenHandlerName(aHandlerName);
		try {
			result.getExecutability()
				.addAll(
					ExecutabilityConfig.SimplifiedFormat.INSTANCE.getValue(ExecutabilityConfig.EXECUTABILITY_PROPERTY,
					executability));
		} catch (ConfigurationException ex) {
			throw new ConfigurationError("Parsing dialog executability failed.", ex);
		}
		result.setImage(image);
		result.setDisabledImage(disabledImage);

		return result;
	}

	/**
	 * Appends an javascript command to open a popup dialog at the position where the javascript
	 * command is triggered, typically at the mouse position.
	 * 
	 * @param renderContext
	 *        context to render the command to.
	 * @param out
	 *        The {@link Appendable} to append commant to.
	 * @param content
	 *        The {@link HTMLFragment} to render in the popup dialog.
	 * @param popupModel
	 *        See {@link PopupDialogControl#getModel()}.
	 * 
	 * @return The {@link Handle} for the registered command, see {@link Handle#dispose()}.
	 * 
	 * @throws IOException
	 *         iff the given {@link Appendable} throws one.
	 */
	public static Handle renderOpenPopupDialog(DisplayContext renderContext, Appendable out,
			Supplier<? extends HTMLFragment> content, PopupDialogModel popupModel) throws IOException {
		Command cmd = new Command() {

			@Override
			public HandlerResult executeCommand(DisplayContext context) {
				Map<String, Object> arguments = LinkGenerator.getArguments(context);
				openPopupAtPosition(context, popupModel, arguments, content.get());
				return HandlerResult.DEFAULT_RESULT;
			}

		};
		Map<String, JSValue> additionalArguments =
			Collections.singletonMap(EVENT_POSITION_KEY, new JSNativeExpression("BAL.getAbsoluteEventPosition(event)"));
		return LinkGenerator.renderLink(renderContext, out, cmd, additionalArguments);
	}

	/**
	 * Opens a popup with the given content at the position determined by the key
	 * {@value #EVENT_POSITION_KEY} in the given arguments map.
	 * 
	 * @param context
	 *        Current interaction.
	 * @param popupModel
	 *        The model for the resulting popup.
	 * @param arguments
	 *        Arguments to fetch position from. It is expected that the map contains a value for the
	 *        key {@value #EVENT_POSITION_KEY}. The value is expected to be a {@link Map} which maps
	 *        keys "x" and "y" to integer values. On client side the value can be filled e.g. with
	 *        the value "BAL.getAbsoluteEventPosition(event)".
	 * @param content
	 *        The actual displayed popup content.
	 */
	public static void openPopupAtPosition(DisplayContext context, PopupDialogModel popupModel,
			Map<String, Object> arguments, HTMLFragment content) {
		Map<?, ?> eventPosition = (Map<?, ?>) arguments.get(EVENT_POSITION_KEY);
		// "x" and "y" are used in BAL.Coordinates
		int x = ((Number) eventPosition.get("x")).intValue();
		int y = ((Number) eventPosition.get("y")).intValue();
		Point position = new Point(x, y);
		LayoutComponentScope frameScope = MainLayout.getComponent(context).getEnclosingFrameScope();
		PopupDialogControl control = new PopupDialogControl(frameScope, popupModel, position);
		control.setContent(content);
		context.getWindowScope().openPopupDialog(control);
	}

	/**
	 * Appends the full URL of the host of the application to the given output, e.g.
	 * https://aps.top-logic.com:8080.
	 * 
	 * <p>
	 * When a value for the alias {@link AliasManager#HOST} is present, it is used.
	 * </p>
	 * 
	 * @param renderContext
	 *        The context of the current request.
	 * @param out
	 *        The output to append content to.
	 * 
	 * @throws IOException
	 *         Thrown by {@link Appendable#append(CharSequence)}.
	 */
	public static void appendHostURL(DisplayContext renderContext, Appendable out) throws IOException {
		String configuredHost = AliasManager.getInstance().getAlias(AliasManager.HOST);
		if (!StringServices.isEmpty(configuredHost)) {
			out.append(configuredHost);
		} else {
			HttpServletRequest req = renderContext.asRequest();
			out.append(req.getScheme());
			out.append("://");
			out.append(req.getServerName());
			out.append(':');
			out.append(Integer.toString(req.getServerPort()));
		}
	}

	/**
	 * Appends the full {@link URL} to display to the {@link TLLayoutServlet}.
	 * 
	 * <p>
	 * E.g. http://localhost:8080/application/servlet/LayoutServlet
	 * </p>
	 * 
	 * @param renderContext
	 *        The context to fetch URL from.
	 * @param out
	 *        The {@link Appendable} to append URL to.
	 */
	public static void appendFullLayoutServletURL(DisplayContext renderContext, Appendable out) throws IOException {
		appendHostURL(renderContext, out);
		out.append(renderContext.asRequest().getContextPath());
		out.append(ApplicationPages.getInstance().getLayoutServletPath());
	}

	/**
	 * Reads a {@link LayoutComponent} configuration without base configuration.
	 * 
	 * @see #createLayoutConfigurationInternal(InstantiationContext, Theme, String, Content)
	 */
	public static LayoutComponent.Config createLayoutConfiguration(InstantiationContext context,
			Theme theme, String layoutName, BinaryContent source, String nameScope) throws ConfigurationException {
		LayoutComponent.Config configuration =
			createLayoutConfigurationInternal(context, theme, layoutName, source);
		LayoutUtils.qualifyComponentNames(nameScope, configuration);
		return configuration;
	}

	/**
	 * Reads a {@link LayoutComponent} configuration from the given source and the given base
	 * configuration, checks the constraints, and returns it.
	 * 
	 * @param context
	 *        The {@link InstantiationContext} that is used to create configurations.
	 * @param source
	 *        The {@link Content} to read configuration from.
	 * @return The de-serialised {@link LayoutComponent.Config}. May be <code>null</code> in some
	 *         cases when an error occurred.
	 */
	public static LayoutComponent.Config createLayoutConfigurationInternal(InstantiationContext context, Theme theme,
			String layoutName, Content source) throws ConfigurationException {
		Map<String, ConfigurationDescriptor> globalDescriptorByName = MainLayout.COMPONENT_DESCRIPTORS;
		ConfigurationItem configuration =
			createConfiguration(context, theme, layoutName, source, globalDescriptorByName);
		return transformLayoutConfig((LayoutComponent.Config) configuration);
	}

	/**
	 * Transforms the given {@link LayoutComponent.Config} for the current theme.
	 */
	public static LayoutComponent.Config transformLayoutConfig(LayoutComponent.Config config) {
		return ThemeFactory.getTheme().getValue(Icons.LAYOUT_TRANSFORM).transform(config);
	}

	/**
	 * Reads a configuration from the given source and the given base configuration, checks the
	 * constraints, and returns it.
	 */
	public static ConfigurationItem createConfiguration(InstantiationContext context, Theme theme, String layoutName,
			Content source, Map<String, ConfigurationDescriptor> globalDescriptorByName) throws ConfigurationException {
		ConfigurationReader reader = new ConfigurationReader(context, globalDescriptorByName);
		VariableExpander expander = LayoutVariables.layoutExpander(theme, layoutName);
		ConfigurationItem configuration = reader.setSource(source).setVariableExpander(expander).read();
		checkConstraints(context, configuration);
		return configuration;
	}

	private static void checkConstraints(InstantiationContext context,
			ConfigurationItem configuration)
			throws ConfigurationException {
		ConstraintChecker checker = new ConstraintChecker();
		checker.check(configuration);
		List<ConstraintFailure> failures = checker.getFailures();
		if (failures.isEmpty()) {
			return;
		}
		Resources resources = Resources.getInstance(ResourcesModule.getInstance().getDefaultLocale());
		for (ConstraintFailure failure: failures) {
			Location location = failure.getItem().location();
			StringBuilder builder = new StringBuilder();
			builder.append("Check constaint in item at location ");
			builder.append(location);
			builder.append(" failed for property ");
			builder.append(failure.getContextProperty().getPropertyName());
			builder.append(": ");
			builder.append(resources.getString(failure.getMessage()));
			if (failure.isWarning()) {
				context.info(builder.toString(), Log.WARN);
			} else {
				context.error(builder.toString());
			}
		}
	}

	/**
	 * The given {@link LayoutComponent.Config} is inspected and the {@link BoundCommandGroup} of
	 * all commands for the configured components are added to the given output collection.
	 * 
	 * @param out
	 *        The {@link Collection} to add {@link BoundCommandGroup}s to.
	 * @param config
	 *        The {@link LayoutComponent} configuration to get {@link BoundCommandGroup}s for.
	 */
	public static void addCommandGroups(Collection<? super BoundCommandGroup> out, LayoutComponent.Config config) {
		SimpleCommandRegistry registry = new SimpleCommandRegistry();
		config.modifyIntrinsicCommands(registry);
		addIntrinsicGroups(out, registry.getButtons());
		addIntrinsicGroups(out, registry.getCommands());
		if (config.getDefaultAction() != null) {
			addConfiguredGroup(out, config.getDefaultAction(), config);
		}
		if (config.getCancelAction() != null) {
			addConfiguredGroup(out, config.getCancelAction(), config);
		}
		config.getButtons().forEach(handlerConfig -> {
			addConfiguredGroup(out, handlerConfig, config);
		});
		config.getCommands().forEach(handlerConfig -> {
			addConfiguredGroup(out, handlerConfig, config);
		});
		config.getWindows().forEach(template -> {
			addConfiguredGroup(out, OpenWindowCommand.createWindowOpenHandler(template), config);
		});
		config.getDialogs().forEach(dialog -> {
			// Ignore error, because it is also reported during instantiation of the component.
			Log log = NoProtocol.INSTANCE;
			dialog = LayoutUtils.resolveComponentReference(log, dialog);
			if (dialog == null) {
				return;
			}
			PolymorphicConfiguration<? extends CommandHandler> handlerConfig =
				OpenModalDialogCommandHandler.createDialogOpenHandler(log, config, dialog);
			if (handlerConfig != null) {
				addConfiguredGroup(out, handlerConfig, config);
			}
		});
		Set<BoundCommandGroup> additionalGroups = new HashSet<>();
		config.addAdditionalCommandGroups(additionalGroups);
		out.addAll(additionalGroups);
	}

	private static void addConfiguredGroup(Collection<? super BoundCommandGroup> out,
			PolymorphicConfiguration<? extends CommandHandler> handlerConfig, LayoutComponent.Config layoutConfig) {
		if (handlerConfig instanceof CommandHandler.Config) {
			addGroup(out, (CommandHandler.Config) handlerConfig);
		} else if (handlerConfig instanceof CommandReferenceConfig) {
			String handlerId = ((CommandReferenceConfig) handlerConfig).getCommandId();
			addGroup(out, registeredHandler(handlerId));
		} else if (handlerConfig instanceof CheckerProxyHandler.Config) {
			Config checkerConfig = (CheckerProxyHandler.Config) handlerConfig;
			ComponentName checkerName = checkerConfig.getName();
			if (checkerName != null && !checkerName.equals(layoutConfig.getName())) {
				/* The given layout component is not used for security checking, but the component
				 * with the checker name. Therefore the command group of the command is not relevant
				 * for the given component. */
				return;
			}
			addConfiguredGroup(out, checkerConfig.getCommand(), layoutConfig);
		} else {
			throw failUnexpectedConfigType(handlerConfig);
		}
	}

	private static IllegalArgumentException failUnexpectedConfigType(
			PolymorphicConfiguration<? extends CommandHandler> handlerConfig) {
		StringBuilder error = new StringBuilder();
		error.append("Unexpected handler configuration. Only ");
		error.append(CommandHandler.Config.class.getName());
		error.append(", ");
		error.append(CommandReferenceConfig.class.getName());
		error.append(", and ");
		error.append(CheckerProxyHandler.Config.class);
		error.append(" are supported: ");
		error.append(handlerConfig);
		return new IllegalArgumentException(error.toString());
	}

	private static void addGroup(Collection<? super BoundCommandGroup> out, CommandHandler.Config handler) {
		CommandGroupReference group = handler.getGroup();
		if (group == null) {
			out.add(SimpleBoundCommandGroup.READ);
		} else {
			BoundCommandGroup resolvedGroup = group.resolve();
			if (resolvedGroup != null) {
				out.add(resolvedGroup);
			}
		}
	}

	private static void addGroup(Collection<? super BoundCommandGroup> out, CommandHandler handler) {
		if (handler != null) {
			out.add(handler.getCommandGroup());
		}
	}

	private static void addIntrinsicGroups(Collection<? super BoundCommandGroup> out, List<String> commandIds) {
		commandIds.forEach(commandId -> addGroup(out, registeredHandler(commandId)));
	}

	private static CommandHandler registeredHandler(String commandId) {
		return CommandHandlerFactory.getInstance().getHandler(commandId);
	}
    
	/**
	 * Gets the {@link ResKey} for the "tabber" of the given layout component.
	 * 
	 * @deprecated Use {@link LayoutComponent#getTitleKey()}.
	 */
	@Deprecated
	public static ResKey getTabberKey(LayoutComponent component) {
		return component.getResPrefix().key(TABBER);
	}

	/**
	 * Copies the given configuration and replaces all {@link LayoutReference} by the corresponding
	 * resolved component.
	 * 
	 * @return A copy of the given component configuration in which all {@link LayoutReference} are
	 *         replaced by the corresponding configuration.
	 */
	public static LayoutComponent.Config inlineLayoutReferences(LayoutComponent.Config config) {
		config = TypedConfiguration.copy(config);
		return (LayoutComponent.Config) TypedConfigUtil.replace(config, item -> {
			if (item instanceof LayoutReference) {
				LayoutReference reference = (LayoutReference) item;
				String resource = reference.getResource();
				try {
					item = LayoutStorage.getInstance().getOrCreateLayoutConfig(resource);
					// do not use stored item as it is potentially modified.
					item = TypedConfiguration.copy(item);
				} catch (ConfigurationException ex) {
					Logger.error("Unable to create configuration defined in " + resource, ex);
				}
			}
			return item;
		});
	}

	/**
	 * Qualifies the local component names with the given scope.
	 * 
	 * @param config
	 *        The configuration to update.
	 * @param scope
	 *        The scope for local {@link ComponentName}s.
	 */
	public static void qualifyComponentNames(String scope, ConfigurationItem config) {
		qualifyComponentNames(localName -> ComponentName.newName(scope, localName.localName()), config);
	}

	/**
	 * Qualifies the local component names by applying the given name mapping.
	 * 
	 * @param config
	 *        The configuration to update.
	 * @param nameMapping
	 *        Mapping for local {@link ComponentName}s to qualified names.
	 */
	public static void qualifyComponentNames(Function<ComponentName, ComponentName> nameMapping,
			ConfigurationItem config) {
		new QualifyComponentNameVisitor(nameMapping).qualify(config);
	}

	/**
	 * Modifies the components in the {@link LayoutComponent.Config configuration tree} starting
	 * with the given {@link LayoutComponent.Config}.
	 * 
	 * <p>
	 * The given configuration is visited including all referenced items and the items referenced in
	 * the references and so on. When a visited item is a {@link LayoutComponent.Config layout
	 * configuration} then it is modified defined by the given replacements. The visited layout
	 * configuration is replaced and descending continues.
	 * </p>
	 * 
	 * @param config
	 *        The component configuration to update.
	 * @param replacements
	 *        Replacement instruction: The input is the configuration item to change, the output is
	 *        either the unmodified input or a new configuration. It is not allowed to modify the
	 *        given configuration inline. It is not allowed to return <code>null</code>.
	 * 
	 * @return The updated configuration. It may be the given configuration (e.g. only inner
	 *         components are updated), but also a new configuration (e.g. some local properties are
	 *         changed).
	 */
	public static LayoutComponent.Config modify(LayoutComponent.Config config,
			Function<LayoutComponent.Config, LayoutComponent.Config> replacements) {
		return (LayoutComponent.Config) TypedConfigUtil.replace(config,
			new Function<ConfigurationItem, ConfigurationItem>() {

				@Override
				public ConfigurationItem apply(ConfigurationItem t) {
					if (t instanceof LayoutComponent.Config) {
						t = replacements.apply((LayoutComponent.Config) t);
					}
					return t;
				}

			});
	}

	/**
	 * Computes from the {@link Path} (relative to the layout folder) that represents an
	 * {@link LayoutModelConstants#LAYOUT_XML_OVERLAY_FILE_SUFFIX overlay layout} the name of the
	 * {@link LayoutModelConstants#LAYOUT_XML_FILE_SUFFIX layout file} for which this is an overlay.
	 * 
	 * @param relativeOverlayPath
	 *        A path relative to the "layout root path" whose name ends with
	 *        {@link LayoutModelConstants#LAYOUT_XML_OVERLAY_FILE_SUFFIX}.
	 * 
	 * @return The name of the of the {@link LayoutModelConstants#LAYOUT_XML_FILE_SUFFIX layout
	 *         file} for which the path contains an overlay.
	 */
	public static String getOverlayedComponentPath(Path relativeOverlayPath) {
		Path file = relativeOverlayPath.getFileName();
		if (file == null) {
			throw new IllegalArgumentException("Path " + relativeOverlayPath + " is not a regular file.");
		}
		String fileName = file.toString();
		String overlayedName = fileName.substring(0,
			fileName.length() - LayoutModelConstants.LAYOUT_XML_OVERLAY_FILE_SUFFIX.length())
			+ LayoutModelConstants.LAYOUT_XML_FILE_SUFFIX;
		return normalizeFileName(relativeOverlayPath.resolveSibling(overlayedName)).toString();
	}

	/**
	 * Separates the parts of the given relative {@link Path} by '/'.
	 */
	public static StringBuilder normalizeFileName(Path relativePath) {
		if (relativePath.getRoot() != null) {
			throw new IllegalArgumentException(relativePath + " is not relative.");
		}
		StringBuilder tmp = new StringBuilder(FILE_PATH_SEPARATOR);
		for (Path name : relativePath) {
			if (tmp.length() > 0) {
				tmp.append(FILE_PATH_SEPARATOR);
			}
			tmp.append(name.toString());
		}
		return tmp;
	}

	/**
	 * Resolves the given {@link LayoutComponent.Config}.
	 * 
	 * <p>
	 * If the given config is not a {@link LayoutReference}, it is simply returned. <br/>
	 * If the given config is a {@link LayoutReference}, the referenced resource is loaded to a
	 * {@link LayoutComponent.Config} and returned.
	 * </p>
	 * 
	 * @param log
	 *        A {@link Log} to write potential problems to.
	 * @param config
	 *        The configuration to resolve.
	 * @return May be <code>null</code>, in case loading the referenced layout fails.
	 */
	public static LayoutComponent.Config resolveComponentReference(Log log, LayoutComponent.Config config) {
		if (!(config instanceof LayoutReference)) {
			return config;
		}
		String resource = ((LayoutReference) config).getResource();
		try {
			config = LayoutStorage.getInstance().getOrCreateLayoutConfig(resource);
		} catch (ConfigurationException ex) {
			log.error("Unable to resolve " + resource, ex);
			config = null;
		}
		return config;
	}

	/**
	 * Normalizes the layout key by removing a leading '/'.
	 * 
	 * @param layout
	 *        Name of a layout relative to {@link ModuleLayoutConstants#LAYOUT_LOC}, read from a
	 *        layout reference.
	 * 
	 * @return Either the given layout, or the layout with removed leading '/'.
	 */
	public static String normalizeLayoutKey(String layout) {
		String layoutKey = normalizeLayoutName(layout);
		return isApplicationRootLayout(layoutKey) ? LayoutUtils.ROOT_LAYOUT_NAME_SCOPE : layoutKey;
	}

	/**
	 * Normalizes the layout name by removing a leading '/'.
	 */
	public static String normalizeLayoutName(String layout) {
		if (!layout.isEmpty() && layout.charAt(0) == '/') {
			Logger.warn("Layout keys must not start with a '/' character: " + layout, LayoutUtils.class);
			layout = layout.substring(1);
		}
		return layout;
	}

	private static boolean isApplicationRootLayout(String layout) {
		return LayoutConfig.getAvailableLayouts().contains(layout);
	}

	/**
	 * The layout root directory for the current top level application.
	 */
	public static File getCurrentTopLayoutBaseDirectory() {
		return new File(Workspace.topLevelWebapp(), LayoutConstants.LAYOUT_BASE_DIRECTORY);
	}

	/**
	 * Finds first parent of type given by the class object from a given source
	 * {@link LayoutComponent}.
	 */
	@SuppressWarnings("unchecked")
	public static <T extends LayoutComponent> T findFirstParent(LayoutComponent component, Class<T> type) {
		if (type.isInstance(component)) {
			return (T) component;
		}

		return findFirstParent(component.getParent(), type);
	}

	/**
	 * Returns compatible layout filename part consisting of alphanumeric characters or an
	 * underscore.
	 */
	public static String getCompatibleLayoutFilenamePart(String filenamePart) {
		return filenamePart.replaceAll("[^a-zA-Z0-9_]", "_");
	}

	/**
	 * Changes the {@link LayoutModelConstants#LAYOUT_XML_FILE_SUFFIX layout suffix} to
	 * {@link LayoutModelConstants#LAYOUT_XML_OVERLAY_FILE_SUFFIX overlay suffix} for the given
	 * relative layout input path.
	 */
	public static String createLayoutOverlayPath(String relativeLayoutPath) {
		String layoutSuffix = LayoutModelConstants.LAYOUT_XML_FILE_SUFFIX;
		String layoutOverlaySuffix = LayoutModelConstants.LAYOUT_XML_OVERLAY_FILE_SUFFIX;

		return StringServices.changeSuffix(relativeLayoutPath, layoutSuffix, layoutOverlaySuffix);
	}

	/**
	 * Heuristic creating a user friendly readable label for the given component.
	 */
	public static String getLabel(LayoutComponent component) {
		ResKey labelKey = getLabelKey(component);
		if (labelKey != null) {
			return Resources.getInstance().getString(labelKey, null);
		}
		return component.getName().localName();
	}

	/**
	 * The effective {@link ResKey} for labeling the given {@link LayoutComponent}, or
	 * <code>null</code> if the component has no user-visible name.
	 */
	public static ResKey getLabelKey(LayoutComponent component) {
		if (isRootDialog(component)) {
			ResKey dialogTitle = getDialogTitle(component);

			if (dialogTitle != null) {
				return dialogTitle;
			}
		}

		return getLabelKey(component.getConfig());
	}

	/**
	 * Returns a label for the given component configuration.
	 */
	public static String getLabel(LayoutComponent.Config config) {
		ResKey labelKey = getLabelKey(config);
		if (labelKey != null) {
			return Resources.getInstance().getString(labelKey, null);
		}
		return config.getName().localName();
	}

	/**
	 * The label {@link ResKey} for the given {@link LayoutComponent} configuration.
	 */
	private static ResKey getLabelKey(com.top_logic.mig.html.layout.LayoutComponent.Config config) {
		ResKey titleKey = LayoutComponent.Config.getEffectiveTitleKey(config);
		if (TLResKeyUtil.existsResource(titleKey)) {
			return titleKey;
		}

		TabConfig tabInfo = config.getTabInfo();
		if (tabInfo != null) {
			return tabInfo.getLabel();
		}
		return null;
	}

	/**
	 * Checks if the given component is the root component inside a dialog.
	 */
	public static boolean isRootDialog(LayoutComponent component) {
		LayoutComponent parent = component.getParent();
		LayoutComponent dialogParent = component.getDialogParent();

		return (dialogParent != null && dialogParent == parent);
	}

	/**
	 * Extract the title of the given dialog.
	 */
	public static ResKey getDialogTitle(LayoutComponent dialog) {
		PolymorphicConfiguration<TitleProvider> titleConfig = dialog.getDialogInfo().getTitle();

		if (titleConfig != null) {
			InstantiationContext instantiationContext = SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY;

			return instantiationContext.getInstance(titleConfig).getSimpleTitle(dialog.getConfig());
		}

		return null;
	}

	/**
	 * Creates the {@link LayoutConfigTree}s from the configured components.
	 * 
	 * @param context
	 *        {@link InstantiationContext} to read configurations.
	 * @return Mapping from the {@link LayoutConfig#getAvailableLayouts() available layouts} to the
	 *         corresponding {@link LayoutConfigTree}.
	 */
	public static Map<String, LayoutConfigTree> getLayoutConfigTreesByName(InstantiationContext context) {
		List<String> layouts = LayoutConfig.getAvailableLayouts();
		Map<String, LayoutConfigTree> layoutTrees;
		switch (layouts.size()) {
			case 0: {
				context.error("No layout files configured.");
				layoutTrees = Collections.emptyMap();
				break;
			}
			case 1: {
				LayoutConfigTree layoutTree = createLayoutTree(context, layouts.get(0));
				if (layoutTree == null) {
					layoutTrees = Collections.emptyMap();
				} else {
					layoutTrees = Collections.singletonMap(layouts.get(0), layoutTree);
				}
				break;
			}
			default: {
				/* Ensure stable order. */
				layoutTrees = new LinkedHashMap<>();
				for (String layout : layouts) {
					LayoutConfigTree layoutTree = createLayoutTree(context, layout);
					if (layoutTree != null) {
						layoutTrees.put(layout, layoutTree);
					}
				}
			}
		}
		return layoutTrees;
	}

	private static LayoutConfigTree createLayoutTree(InstantiationContext context, String resource) {
		try {
			LayoutComponent.Config config = LayoutStorage.getInstance().getOrCreateLayoutConfig(resource);
			config = inlineLayoutReferences(config);
			return new LayoutConfigTree(config);
		} catch (ConfigurationException ex) {
			context.error("Unable to create configuration from resource '" + resource + "'.", ex);
			return null;
		}
	}

	/**
	 * Resolves the types and components in the given {@link GotoTarget}s.
	 * 
	 * @param log
	 *        Errors ar written to this {@link Log}.
	 * @param mainLayout
	 *        {@link MainLayout} to resolve components in.
	 * @param gotoTargets
	 *        {@link GotoTarget}s to resolve.
	 */
	public static Map<TLType, LayoutComponent> resolveGotoTargets(Log log, MainLayout mainLayout,
			Collection<GotoTarget> gotoTargets) {
		Map<TLType, LayoutComponent> resolvedTargets = new HashMap<>();
		for (GotoTarget gotoTarget : gotoTargets) {
			TLType type;
			try {
				TLObject part = TLModelUtil.resolveQualifiedName(gotoTarget.getTypeSpec());
				if (!(part instanceof TLType)) {
					log.error("Configured part " + part + " is not a type.");
					continue;
				}
				type = (TLType) part;
			} catch (TopLogicException ex) {
				log.error("Type " + gotoTarget.getTypeSpec() + " can not be resolved.", ex);
				continue;
			}
			LayoutComponent targetComponent = mainLayout.getComponentByName(gotoTarget.getComponent());
			if (targetComponent == null) {
				log.error("Unknown target component " + gotoTarget.getComponent() + ".");
				continue;
			}
			resolvedTargets.put(type, targetComponent);
		}
		return MapUtilShared.memoryOptimization(resolvedTargets);
	}

	/**
	 * Creates and (eventually) resolves the given layout to a component tree.
	 * 
	 * @param context
	 *        The context for configuration instantiations.
	 * @param parent
	 *        Parent of the new created component.
	 * @param layoutName
	 *        Component name.
	 * @param isResolveComponent
	 *        Whether the component should resolved after the initialization.
	 * @param configuration
	 *        Component configuration to be instantiated.
	 */
	public static LayoutComponent createComponentFromXML(InstantiationContext context, LayoutComponent parent,
			String layoutName, boolean isResolveComponent, LayoutComponent.Config configuration)
			throws IOException, ConfigurationException {
		ComponentBuilder builder = new ComponentBuilder();
	
		builder.setContext(context);
		builder.setParent(parent);
		builder.setLayoutName(layoutName);
		builder.setResolveComponent(isResolveComponent);
		builder.setConfiguration(configuration);
	
		return builder.build();
	}

	/**
	 * Return the subpath to the layout directory of the given path.
	 */
	public static Path getLayoutDirectory(Path path) {
		return FileUtilities.getPathTo(path, LAYOUT_SUFFIX);
	}

	/**
	 * Returns true if the given name ends with template suffix ".template.xml".
	 */
	public static boolean isTemplate(String filename) {
		return filename.endsWith(LayoutModelConstants.DYNAMIC_LAYOUT_TEMPLATE_FILE_SUFFIX);
	}

	/**
	 * Returns true if the given name ends with layout suffix ".layout.xml".
	 */
	public static boolean isLayout(String filename) {
		return filename.endsWith(LAYOUT_XML_FILE_SUFFIX);
	}

	/**
	 * Returns true if the given name ends with layout overlay suffix ".layout.overlay.xml".
	 */
	public static boolean isLayoutOverlay(String filename) {
		return filename.endsWith(LAYOUT_XML_OVERLAY_FILE_SUFFIX);
	}

	/**
	 * Writes the layout component configuration into the given file.
	 */
	public static void writeConfiguration(LayoutComponent.Config config, File file) throws Exception {
		try (OutputStream outputStream = new FileOutputStream(file)) {
			try (Writer out = new OutputStreamWriter(outputStream, StringServices.CHARSET_UTF_8)) {
				ConfigurationWriter confWriter = new ConfigurationWriter(out);

				confWriter.write(LayoutComponent.Config.COMPONENT, LayoutComponent.Config.class, config);
			}

			XMLPrettyPrinter.normalizeFile(file);
		}
	}

	/**
	 * Removes the layout resource prefix from the given resource path.
	 */
	public static String getLayoutKey(String resource) {
		Path layoutResourcePrefix = Paths.get(ModuleLayoutConstants.LAYOUT_RESOURCE_PREFIX);
		Path layoutKey = FileUtilities.getPathFrom(Paths.get(resource), layoutResourcePrefix);
		if (layoutKey == null) {
			Logger.warn("Layout key requested for resource not in '" + ModuleLayoutConstants.LAYOUT_RESOURCE_PREFIX
					+ "': " + resource,
				LayoutUtils.class);
			return resource;
		}
		return toKey(layoutKey);
	}

	/**
	 * The given path '/' separated independent on the runtime platform (in contrast to
	 * {@link Path#toString()}.
	 */
	private static String toKey(Path pathSuffix) {
		StringBuilder result = new StringBuilder();
		for (int n = 0, cnt = pathSuffix.getNameCount(); n < cnt; n++) {
			if (n > 0) {
				result.append(FILE_PATH_SEPARATOR);
			}
			result.append(pathSuffix.getName(n));
		}
		return result.toString();
	}

	/**
	 * Returns true if the underlying component configuration for the two {@link TLLayout}s is the
	 * same.
	 */
	public static boolean hasSameLayoutConfig(String layoutKey, TLLayout layout1, TLLayout layout2) {
		try {
			LayoutComponent.Config config1 = layout1.get();
			LayoutComponent.Config config2 = layout2.get();

			return ComponentConfigEquality.INSTANCE.equals(config1, config2);
		} catch (ConfigurationException exception) {
			throw new TopLogicException(I18NConstants.LAYOUT_RESOLVE_ERROR.fill(layoutKey), exception);
		}
	}
}
