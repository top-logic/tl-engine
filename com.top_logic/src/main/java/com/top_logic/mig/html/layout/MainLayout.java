/*
 * SPDX-FileCopyrightText: 2002 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout;

import static com.top_logic.mig.html.HTMLConstants.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

import org.apache.commons.collections4.BidiMap;

import com.top_logic.base.context.TLSubSessionContext;
import com.top_logic.base.services.simpleajax.AbstractSystemAjaxCommand;
import com.top_logic.base.services.simpleajax.ClientAction;
import com.top_logic.base.services.simpleajax.ElementReplacement;
import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.base.services.simpleajax.JSSnipplet;
import com.top_logic.base.services.simpleajax.RequestUtil;
import com.top_logic.basic.CalledFromJSP;
import com.top_logic.basic.DebugHelper;
import com.top_logic.basic.Log;
import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.annotation.FrameworkInternal;
import com.top_logic.basic.col.ArrayStack;
import com.top_logic.basic.col.BidiHashMap;
import com.top_logic.basic.col.Stack;
import com.top_logic.basic.col.filter.FilterFactory;
import com.top_logic.basic.col.map.MultiMaps;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.ReferenceResolver;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.ImplementationClassDefault;
import com.top_logic.basic.config.annotation.defaults.ItemDefault;
import com.top_logic.basic.config.annotation.defaults.NullDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.config.copy.ConfigCopier;
import com.top_logic.basic.config.misc.DescendingConfigurationItemVisitor;
import com.top_logic.basic.io.StreamUtilities;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.StopWatch;
import com.top_logic.basic.version.Version;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.event.infoservice.InfoService;
import com.top_logic.event.infoservice.InfoServiceXMLStringConverter;
import com.top_logic.gui.FaviconTag;
import com.top_logic.gui.ThemeFactory;
import com.top_logic.layout.ApplicationWindowScope;
import com.top_logic.layout.ControlScope;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.FrameScope;
import com.top_logic.layout.LayoutContext;
import com.top_logic.layout.LayoutLinker;
import com.top_logic.layout.UpdateQueue;
import com.top_logic.layout.UpdateWriter;
import com.top_logic.layout.WindowScopeProvider;
import com.top_logic.layout.basic.CommandModelRegistry;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.basic.RenderErrorUtil;
import com.top_logic.layout.basic.component.AJAXControlSupport;
import com.top_logic.layout.basic.component.AJAXSupport;
import com.top_logic.layout.basic.component.BasicAJAXSupport;
import com.top_logic.layout.basic.component.ControlComponent.DispatchAction;
import com.top_logic.layout.basic.component.ControlSupport;
import com.top_logic.layout.channel.ComponentChannel;
import com.top_logic.layout.channel.DefaultChannel;
import com.top_logic.layout.editor.LayoutTemplateUtils;
import com.top_logic.layout.form.tag.js.JSObject;
import com.top_logic.layout.internal.SubsessionHandler;
import com.top_logic.layout.scripting.recorder.ScriptingRecorder;
import com.top_logic.layout.structure.BrowserWindowControl;
import com.top_logic.layout.structure.LayoutControl;
import com.top_logic.layout.structure.LayoutControlFactory;
import com.top_logic.layout.structure.LayoutFactory;
import com.top_logic.layout.window.WindowComponent;
import com.top_logic.layout.window.WindowManager;
import com.top_logic.layout.window.WindowManager.OnCloseWindow;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.mig.html.HTMLUtil;
import com.top_logic.mig.html.UserAgent;
import com.top_logic.model.listen.ModelScope;
import com.top_logic.tool.boundsec.BoundChecker;
import com.top_logic.tool.boundsec.BoundComponent;
import com.top_logic.tool.boundsec.BoundHelper;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.boundsec.OpenModalDialogCommandHandler;
import com.top_logic.tool.boundsec.commandhandlers.GotoHandler;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;
import com.top_logic.util.Resources;
import com.top_logic.util.TLContext;
import com.top_logic.util.error.ErrorHandlingHelper;
import com.top_logic.util.license.LicenseTool;

/**
 * This is the top most part of a complete HTML-Layout.
 * 
 * In Addition it provides a lot of static function to retrieve and navigate
 * in layouts. 
 * 
 * @author  <a href="mailto:kha@top-logic.com">Klaus Halfmann</a>
 */
public abstract class MainLayout extends Layout implements WindowScopeProvider {
	
	/**
	 * Configuration for {@link MainLayout}. System identifiers for various document type definitions.
	 */
	public interface GlobalConfig extends ConfigurationItem {
		/**
		 * See {@link GlobalConfig#getDocType}.
		 */
		String DOC_TYPE = "docType";

		/**
		 * @see #getEventForwarder()
		 */
		String EVENT_FORWARDER = "eventForwarder";

		/** Getter for the {@link GlobalConfig#DOC_TYPE}. */
		@Name(DOC_TYPE)
		DocType getDocType();

		/**
		 * The {@link ModelEventForwarder} that transforms change sets from the persistency layer to
		 * session events.
		 */
		@Name(EVENT_FORWARDER)
		@ItemDefault(GlobalModelEventForwarder.class)
		PolymorphicConfiguration<? extends ModelEventForwarder> getEventForwarder();
	}

	/**
	 * Configuration of an XHTML document type.
	 * 
	 * <code>
	 * &lt;!DOCTYPE HTML PUBLIC "{@value #PUBLIC_ID}" "{@value #PATH}{@value #DTD}"&gt;
	 * </code>
	 * 
	 * @see MainLayout#writeDoctype(DisplayContext, TagWriter)
	 */
	public interface DocType extends ConfigurationItem {

		/**
		 * The name of the DTD without path. See {@link DocType#getDTD}.
		 */
		String DTD = "dtd";

		/**
		 * The path to the DTD, relatively to the applications context path. See {@link DocType#getPath}.
		 */
		String PATH = "path";

		/**
		 * The public ID. See {@link DocType#getPublicID}.
		 */
		String PUBLIC_ID = "publicID";

		/** Getter for {@link DocType#DTD}. */
		@Name(DTD)
		@Mandatory
		String getDTD();

		/** Getter for {@link DocType#PATH}. */
		@Name(PATH)
		@NullDefault
		String getPath();

		/** Getter for {@link DocType#PUBLIC_ID}. */
		@Name(PUBLIC_ID)
		String getPublicID();
	}

	@Override
	public void visitChildrenRecursively(LayoutComponentVisitor aVisitor) {
		super.visitChildrenRecursively(aVisitor);

		if (windowManager != null) {
			List<WindowComponent> windows = windowManager.getWindows();
			for (WindowComponent window : windows) {
				window.acceptVisitorRecursively(aVisitor);
			}
		}
	}

	/**
	 * @see SubComponentConfig#getComponents()
	 * @see LayoutComponent.Config#getDialogs()
	 * @see MainLayout#COMPONENT_DESCRIPTORS
	 */
	public static final Map<String, ConfigurationDescriptor> COMPONENT_DESCRIPTORS;

	static {
		ConfigurationDescriptor descriptor = TypedConfiguration.getConfigurationDescriptor(SubComponentConfig.class);
		PropertyDescriptor property = descriptor.getProperty(SubComponentConfig.COMPONENTS);
		HashMap<String, ConfigurationDescriptor> map = new HashMap<>();
		for (String tagName : property.getElementNames()) {
			map.put(tagName, property.getElementDescriptor(tagName));
		}
		COMPONENT_DESCRIPTORS = Collections.unmodifiableMap(map);
	}

	public interface Config extends Layout.Config {

		/** @see com.top_logic.basic.reflect.DefaultMethodInvoker */
		Lookup LOOKUP = MethodHandles.lookup();

		/**
		 * @see #getLayoutFactory()
		 */
		String LAYOUT_FACTORY = "layoutFactory";

		/**
		 * See {@link #closeDialogOnBackgroundClick()}
		 */
		String CLOSE_DIALOG_ON_BACKGROUND_CLICK = "closeDialogOnBackgroundClick";

		@Name("shortcutIcon")
		String getShortcutIcon();

		@Name("icon")
		String getIcon();

		@Name("headerIncludeFilePath")
		String getHeaderIncludeFilePath();

		@Name("postProcessorClassName")
		String getPostProcessorClassName();

		/**
		 * The {@link LayoutFactory} to use.
		 */
		@Name(LAYOUT_FACTORY)
		@ItemDefault
		@ImplementationClassDefault(LayoutControlFactory.class)
		PolymorphicConfiguration<LayoutFactory> getLayoutFactory();

		/**
		 * Whether a dialog with a close button could be closed by a background click.
		 */
		@Name(CLOSE_DIALOG_ON_BACKGROUND_CLICK)
		boolean closeDialogOnBackgroundClick();

		@Override
		default void modifyIntrinsicCommands(CommandRegistry registry) {
			Layout.Config.super.modifyIntrinsicCommands(registry);

			registry.registerCommand(NotifyUnload.COMMAND_ID);
			registry.registerCommand(DispatchAction.COMMAND_NAME);

			// Workaround for Ticket #2455
			registry.registerCommand(GotoHandler.COMMAND);

			registry.registerCommand(OnCloseWindow.COMMAND_ID);
		}

	}

	private static class ReplaceComponentContext extends DefaultInstantiationContext {

		private final MainLayout _mainLayout;

		public ReplaceComponentContext(MainLayout mainLayout) {
			super(MainLayout.class);
			_mainLayout = mainLayout;
		}

		/**
		 * Fetches all {@link ReferenceResolver} and fills them with components known by the given
		 * {@link MainLayout}.
		 */
		void fillReferencesToExistingComponents() {
			for (Entry<Ref, Object> ref : getReferenceResolvers().entrySet()) {
				if (!(ref.getValue() instanceof ReferenceResolver<?>)) {
					// already resolved
					continue;
				}
				Ref requestedReference = ref.getKey();
				if (requestedReference instanceof IdRef) {
					IdRef requestedIdReference = (IdRef) requestedReference;
					if (requestedIdReference.scope() != LayoutComponent.class) {
						// Only LayoutComponents can be filled here
						continue;
					}
					LayoutComponent requestedComponent =
						_mainLayout.getComponentByName((ComponentName) requestedIdReference.id());
					if (requestedComponent != null) {
						fillReferenceValue(this, requestedIdReference, requestedComponent);
					}
				}
			}
		}

	}

	private static final String LAYOUT_BASE_DIR_DEFAULT = "/WEB-INF/layouts";

	/**
	 * js variable which is set in {@link #createFullReload()} to avoid executing "notify unload" command
	 * if the new loading is triggered by server
	 */
	private static final String SKIP_NOTIFY_UNLOAD_JS_VARIABLE = "services.ajax.skipNotifyUnload";

	private static final String DEFAULT_DOCTYPE_PATH = "http://www.w3.org/TR/xhtml1/DTD/";

    /** Name of class that will be called after resolving the complete Layout */
    protected String postProcessorClassName; // com.top_logic.mig.html.layout.LayoutResolvedListener

	private Map<ComponentName, LayoutComponent> _index = new HashMap<>();

	/**
	 * This Layout is the parent for all separate windows. That
	 * {@link WindowManager} manages the windows.
	 */ 
    private WindowManager windowManager;
    
    /** Ling to shortcut icon (will be prefixed by appCotext) . */
    protected String shortcutIcon;

    /** Ling to icon (will be prefixed by appCotext) . */
    protected String icon;

    /**
     * Path of a file to be included in the header of all html-pages created for
     * components of this MainLayout. The path is relative to the base
     * directory.
     */ 
    private String headerIncludeFilePath; 
    
    /**
     * Content to be included in the header of all html-pages created for
     * components of this MainLayout. Read from the file referenced at 
     * {@link MainLayout#headerIncludeFilePath}.
     */ 
    private transient String headerInclude; 
    
	protected final BasicAJAXSupport basicAJAXSupport = new BasicAJAXSupport();
    protected final ControlSupport controlSupport = new AJAXControlSupport(this, basicAJAXSupport);
	
	private BrowserWindowControl	layoutControl;

	/**
	 * The {@link ModelEventForwarder} for this {@link MainLayout}.
	 * 
	 * <p>
	 * The type of this instance can be application wide configured through
	 * {@link GlobalConfig#EVENT_FORWARDER}.
	 * </p>
	 */
	private ModelEventForwarder _modelEventForwarder;

	private List<HTMLFragment> _onLoad = Collections.emptyList();

	private SubsessionHandler _layoutContext;

	private TLSubSessionContext _subSessionContext;

	private String _location;

	private final LayoutFactory _layoutFactory;
	
	/** @see #getDialogSupport() */
	private DialogSupport _dialogSupport;

	private final BidiMap<String, LayoutComponent> _availableComponents = new BidiHashMap<>();

	private final Map<String, ComponentChannel> _partnerChannels = new HashMap<>();

	private final boolean _closeDialogOnBackgroundClick;

    /** Create a MainLayout when importing from a XML-File 
     * Attributes supported here are:<ul>
     *   <li>framed               , default true </li>
     *   <li>title                , mandatory </li>
     *   <li>headerIncludeFilePath, optional </li>
     * </ul>
     * @param  config Attributes as extracted from the XML-Tag.
     */
    public MainLayout(InstantiationContext context, Config config) throws ConfigurationException  {
        super(context, config);
        
		_layoutFactory = context.getInstance(config.getLayoutFactory());
        shortcutIcon           = StringServices.nonEmpty(config.getShortcutIcon());
        icon                   = StringServices.nonEmpty(config.getIcon());
        headerIncludeFilePath  = StringServices.nonEmpty(config.getHeaderIncludeFilePath());
        postProcessorClassName = StringServices.nonEmpty(config.getPostProcessorClassName());
		_modelEventForwarder = context.getInstance(getGlobalConfig().getEventForwarder());
		_closeDialogOnBackgroundClick = config.closeDialogOnBackgroundClick();
    }
    
	@Override
	public void createSubComponents(InstantiationContext context) {
		registerComponent(this);

		super.createSubComponents(context);
	}

	final BidiMap<String, LayoutComponent> getAvailableComponents() {
		return _availableComponents;
	}

	/**
	 * Returns the layout key to identify the given component.
	 * 
	 * @param component
	 *        The component to get layout key for.
	 * 
	 * @return May be <code>null</code>, when the component has no such key.
	 * 
	 * @see #getComponentForLayoutKey(String)
	 */
	public final String getLayoutKey(LayoutComponent component) {
		return getAvailableComponents().getKey(component);
	}

	/**
	 * Returns the {@link LayoutComponent} for the given layout key
	 * 
	 * @param layoutKey
	 *        The layout key to get component for.
	 * 
	 * @return May be <code>null</code>, when there is no component with given key.
	 * 
	 * @see #getLayoutKey(LayoutComponent)
	 */
	public final LayoutComponent getComponentForLayoutKey(String layoutKey) {
		return getAvailableComponents().get(LayoutUtils.normalizeLayoutKey(layoutKey));
	}

	@Override
	protected void internalBroadcastModelEvent(Object aModel, Object changedBy, int eventType) {
		super.internalBroadcastModelEvent(aModel, changedBy, eventType);

		List<WindowComponent> windows = getWindowManager().getWindows();
		for (int n = 0, cnt = windows.size(); n < cnt; n++) {
			WindowComponent window = windows.get(n);

			window.internalBroadcastModelEvent(aModel, changedBy, eventType);
		}
	}

	@Override
	public LayoutComponent getWindow() {
		return this;
	}

	/**
	 * Replaces the component for the given layout key by a new {@link LayoutComponent} instantiated
	 * from the given configuration.
	 * 
	 * @param layoutKey
	 *        The key identifying the component to replace.
	 * @param componentConfig
	 *        The configuration of the new component to instantiate.
	 * 
	 * @throws ConfigurationException
	 *         when creating the new component fails.
	 * @throws IllegalArgumentException
	 *         When there is no component with that key to replace, or when the main layout is tried
	 *         to replace.
	 */
	public final void replaceComponent(String layoutKey, LayoutComponent.Config componentConfig)
			throws ConfigurationException {
		String normalizedLayoutKey = LayoutUtils.normalizeLayoutKey(layoutKey);

		LayoutComponent knownComponent = getAvailableComponents().get(normalizedLayoutKey);

		if (knownComponent == null) {
			throw new IllegalArgumentException("No component known for key: " + normalizedLayoutKey);
		}
		if (knownComponent == this) {
			throw new IllegalArgumentException(
				"Can not replace main layout '" + this + "' for layout key: " + normalizedLayoutKey);
		}
		boolean isDialogTopLayout = isDialogTopLayout(knownComponent);

		if (isDialogTopLayout) {
			knownComponent.closeDialog();
		}

		Set<String> oldChildLayoutKeys = collectChildLayoutKeys(knownComponent);
		ConfigCopier.copyContent(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY,
			knownComponent.getLayoutInfo(), componentConfig.getLayoutInfo(), true);

		Set<String> allReloadLayouts = findAllNecessaryReloadLayouts(normalizedLayoutKey);
		Map<String, Object> newConfigByLayout = allReloadLayouts.stream().collect(
			Collectors.toMap(Function.identity(), reloadLayout -> getComponentForLayoutKey(reloadLayout).getConfig()));
		// Given config is the correct one.
		newConfigByLayout.put(normalizedLayoutKey, componentConfig);

		Map<LayoutComponent, LayoutComponent> newCompByOldComp = new HashMap<>();
		ReplaceComponentContext context = new ReplaceComponentContext(this);
		Boolean error = context.deferredReferenceCheck(() -> {
			for (Entry<String, Object> configByLayout : newConfigByLayout.entrySet()) {
				LayoutComponent comp = context.getInstance((LayoutComponent.Config) configByLayout.getValue());
				newCompByOldComp.put(getComponentForLayoutKey(configByLayout.getKey()), comp);
				configByLayout.setValue(comp);
			}
			if (context.hasErrors()) {
				// Creating component not successful
				return Boolean.TRUE;
			}
			/* Replace components in the component tree. Replacing updates the _index by removing
			 * all components from the original tree and adding the components from the new tree. */
			ReplaceComponentVisitor visitor =
				new ReplaceComponentVisitor(newCompByOldComp, MainLayout.this::getLayoutKey);
			for (LayoutComponent comp : getAvailableComponents().values()) {
				visitor.replaceComponents(comp);
			}

			/* replace known component by new component. */
			for (Entry<String, Object> configByLayout : newConfigByLayout.entrySet()) {
				getAvailableComponents().put(configByLayout.getKey(), (LayoutComponent) configByLayout.getValue());
			}
			
			ComponentInstantiationContext componentContext = new ComponentInstantiationContext(context, this);
			/* create subcomponents not before the components are part of the component tree. */
			newCompByOldComp.values().forEach(newComp -> newComp.createSubComponents(componentContext));

			LayoutComponent replacedRootComponent = (LayoutComponent) newConfigByLayout.get(normalizedLayoutKey);
			removeUnusedOldComponents(oldChildLayoutKeys, replacedRootComponent);

			/* Some of the newly created components may refer to a component that are not
			 * re-created. Fill the references with the formerly created components. */
			context.fillReferencesToExistingComponents();

			newCompByOldComp.values().forEach(newComp -> newComp.resolveComponent(componentContext));

			setupRelations(componentContext);

			return componentContext.hasErrors();
		});
		if (error) {
			context.checkErrors();
		}

		if (isDialogTopLayout) {
			LayoutComponent newComp = newCompByOldComp.get(knownComponent);
			OpenModalDialogCommandHandler.openDialog(newComp);
		}
		
		for (LayoutComponent newComponent : newCompByOldComp.values()) {
			LayoutComponent parent = newComponent.getParent();
			if (parent != null) {
				parent.invalidate();
			}
		}

		createLayoutControl();
	}

	private boolean isDialogTopLayout(LayoutComponent component) {
		if (component.openedAsDialog()) {
			if (component == component.getDialogTopLayout()) {
				return true;
			}
		}

		return false;
	}

	private void removeUnusedOldComponents(Set<String> oldChildLayoutKeys, LayoutComponent replacedRootComponent) {
		Set<String> newChildLayoutKeys = collectChildLayoutKeys(replacedRootComponent);

		oldChildLayoutKeys.removeAll(newChildLayoutKeys);
		BidiMap<String, LayoutComponent> availableComponents = getAvailableComponents();

		oldChildLayoutKeys.forEach(availableComponents::remove);
	}

	private Set<String> collectChildLayoutKeys(LayoutComponent component) {
		ComponentCollector componentCollector = new ComponentCollector(FilterFactory.trueFilter());
		component.acceptVisitorRecursively(componentCollector);

		return componentCollector.getFoundElements()
			.stream()
			.map(comp -> LayoutTemplateUtils.getNonNullNameScope(comp))
			.collect(Collectors.toSet());
	}

	private Set<String> findAllNecessaryReloadLayouts(String layoutName) {
		class Visitor extends DescendingConfigurationItemVisitor {

			/**
			 * The component with the "key" {@link ComponentName} is contained in the configurations
			 * of the "value" {@link ComponentName}s
			 */
			Map<ComponentName, Set<ComponentName>> _componentReferences = new HashMap<>();

			Map<ComponentName, String> _layoutByComponentName = new HashMap<>();

			Map<String, Set<ComponentName>> _componentsByLayoutName = new HashMap<>();

			String _currentLayoutKey;

			ComponentName _currentComponent;

			@Override
			protected void handleProperties(ConfigurationItem config) {
				if (config instanceof LayoutReference) {
					String resource = LayoutUtils.normalizeLayoutKey(((LayoutReference) config).getResource());
					LayoutComponent referencedComponent = getComponentForLayoutKey(resource);
					if (referencedComponent != null) {
						referencedComponent.getConfig().setLayoutInfo(((LayoutReference) config).getLayoutInfo());
						String formerKey = _currentLayoutKey;
						_currentLayoutKey = resource;
						try {
							// Call myself with the referenced layout.
							this.handleProperties(referencedComponent.getConfig());
						} finally {
							_currentLayoutKey = formerKey;
						}
					} else {
						return;
					}
				} else if (config instanceof LayoutComponent.Config) {
					ComponentName formerName = _currentComponent;
					_currentComponent = ((LayoutComponent.Config) config).getName();
					try {
						_layoutByComponentName.put(_currentComponent, _currentLayoutKey);
						MultiMaps.add(_componentsByLayoutName, _currentLayoutKey, _currentComponent);
						super.handleProperties(config);
					} finally {
						_currentComponent = formerName;
					}
				} else {
					super.handleProperties(config);
				}
			}

			@Override
			protected void handlePlainProperty(ConfigurationItem config, PropertyDescriptor property) {
				if (isComponentNameProperty(property)) {
					if (config instanceof LayoutComponent.Config
						&& LayoutComponent.Config.NAME.equals(property.getPropertyName())) {
						// Of course each component references itself by the "name" attribute
						return;
					}
					ComponentName name = (ComponentName) config.value(property);
					process(name);
				} else if (isComponentCollectionProperty(property)) {
					Collection<?> names = (Collection<?>) config.value(property);
					names.stream()
						.map(ComponentName.class::cast)
						.forEach(name -> process(name));
				}

			}

			private void process(ComponentName name) {
				if (name == null) {
					// Reference not filled.
					return;
				}
				MultiMaps.add(_componentReferences, name, _currentComponent);
			}

			private boolean isComponentNameProperty(PropertyDescriptor property) {
				return ComponentName.class.isAssignableFrom(property.getType());
			}

			private boolean isComponentCollectionProperty(PropertyDescriptor property) {
				return Collection.class.isAssignableFrom(property.getType())
					&& ComponentName.class.isAssignableFrom(property.getElementType());
			}

			void visit(String rootLayoutKey, LayoutComponent.Config rootConfig) {
				_currentLayoutKey = rootLayoutKey;
				handleProperties(rootConfig);
			}
		}
		Visitor visitor = new Visitor();
		visitor.visit(getLayoutKey(this), getConfig());
		Set<String> allReloadLayouts = new HashSet<>();
		allReloadLayouts.add(layoutName);

		LayoutComponent topLevelComponent = getComponentForLayoutKey(layoutName);
		if(isDialogTopLayout(topLevelComponent)) {
			LayoutComponent dialogParent = topLevelComponent.getDialogParent();
			allReloadLayouts.add(LayoutTemplateUtils.getNonNullNameScope(dialogParent));
		}

		Stack<String> layoutsToProcess = new ArrayStack<>();
		layoutsToProcess.push(layoutName);
		while (!layoutsToProcess.isEmpty()) {
			String layout = layoutsToProcess.pop();
			Set<ComponentName> componentsInLayout = visitor._componentsByLayoutName.get(layout);
			for (ComponentName componentInLayout : componentsInLayout) {
				Set<ComponentName> componentReferers = visitor._componentReferences.get(componentInLayout);
				if (componentReferers == null) {
					continue;
				}
				for (ComponentName referer : componentReferers) {
					String layoutToReload = visitor._layoutByComponentName.get(referer);
					boolean isNew = allReloadLayouts.add(layoutToReload);
					if (isNew) {
						layoutsToProcess.push(layoutToReload);
					}
				}
			}
		}
		return allReloadLayouts;
	}

	/**
	 * Sets up the {@link LayoutContext}, may be called only once.
	 */
	public void initLayoutContext(TLSubSessionContext subsession) {
		assert _layoutContext == null;
		_layoutContext = (SubsessionHandler) subsession.getLayoutContext();
		_subSessionContext = subsession;
	}

    /**
     * The {@link LayoutContext} for this top-level component.
     */
	public LayoutContext getLayoutContext() {
		return _layoutContext;
	}

	/**
	 * The {@link LayoutContext} for this top-level component.
	 */
	public TLSubSessionContext getSubSessionContext() {
		return _subSessionContext;
	}

    @Override
	protected final AJAXSupport ajaxSupport() {
    	return controlSupport;
    }

	@Override
	public final ControlScope getControlScope() {
		return controlSupport;
	}

    @Override
	public void invalidateAJAXSupport() {
    	// must not invalidate AJAXSupport since all updates are necessary 
    }
    
    @Override
	public void invalidate() {
		// Note: When calling super.invalidate() the component is marked invalid and is waiting for
		// a complete repaint.
		//
		// super.invalidate();
		if (layoutControl != null) {
			layoutControl.requestRepaint();
		} else {
			super.invalidate();
		}
    }
    
	/**
	 * Factory method for creating a snipplet that reloads the complete
	 * application.
	 */
	public static JSSnipplet createFullReload() {
		// Note: Must not use a constant, because JSSnipplet is not immutable.
		return new JSSnipplet("services.ajax.mainLayout." + SKIP_NOTIFY_UNLOAD_JS_VARIABLE + "=true;services.ajax.mainLayout.location.reload()");
	}
    
    /** Access the MainLayout, which is this. */
    @Override
	public MainLayout getMainLayout() {
        return this;
    }

    @Override
    public LayoutComponent getComponentByName(ComponentName aName) {
    	return _index.get(aName);
    }

	@Override
	public String getLocation() {
		return _location;
	}

	/**
	 * Explicitly set the layout name, this component tree was loaded from.
	 */
	public void setLocation(String location) {
		_location = location;
	}

	/**
	 * Makes the given component part of the layout tree routed at this component.
	 * 
	 * @see #unregisterComponent(LayoutComponent)
	 */
	public void registerComponent(LayoutComponent component) {
		ComponentName name = component.getName();
		LayoutComponent clash = _index.put(name, component);
		if (clash != null) {
			Logger.warn(
				"Duplicate component name '" + name + "' in '" + clash.getLocation() + "' and '" + component.getLocation()
					+ "'.", MainLayout.class);
		}
		invalidateBoundCheckerCache(component);
	}

	/**
	 * Removes the given component from the layout tree routed at this component.
	 * 
	 * @see #registerComponent(LayoutComponent)
	 */
	public void unregisterComponent(LayoutComponent component) {
		invalidateBoundCheckerCache(component);
		ComponentName name = component.getName();
		LayoutComponent removed = _index.remove(name);
		if (removed != component) {
			// Revert - the given component is not registered. This may happen in a broken layout
			// where components have non-unique names.
			_index.put(name, removed);
		}
	}

	private void invalidateBoundCheckerCache(LayoutComponent component) {
		List<String> defaultForTypes = component.getDefaultForTypes();
		if (!defaultForTypes.isEmpty()) {
			BoundHelper.getInstance().invalidateBoundCheckerCache(this);
		}
	}

    /**
     * MainLayout has no parent...
     */
    @Override
	public boolean isVisible() {
        return this._isVisible();
    }

    /**
     * Writes the content of the file {@link MainLayout#headerIncludeFilePath}
     * to the given TagWriter.
     */
    private void includeHeaderFile(ServletContext context, TagWriter out)
			throws IOException {
        if (getHeaderInclude(context) != null) {
            out.writeIndent();
			out.contentWriter().write(getHeaderInclude(context));
        }
    }
    
    /**
     * Returns the content of the file {@link MainLayout#headerIncludeFilePath}.
     */
    private String getHeaderInclude(ServletContext context) throws IOException{
        if (headerIncludeFilePath == null) {
            return null;
        }
        if (headerInclude == null) {
            headerInclude = getResource(context, headerIncludeFilePath);
        }
        return headerInclude;
    }
    
    /**
     * Returns  the content of a file as a string.
     * @param context a ServletContext
     * @param path a path to a File, relative to the current context root. The
     * path must begin with a slash ("/").
     * 
     * @return String       The content of the file.
     */
    private static String getResource(ServletContext context, String path)
        throws IOException {
            
        InputStream inStream = context.getResourceAsStream(path);
        if (inStream == null) {
            throw new FileNotFoundException(path + " as Servlet resource.");
        }
		try {
			return StreamUtilities.readAllLinesFromStream(inStream);
		} finally {
			inStream.close();
		}
    }

	/**
	 * Writes the start of the html-structure: e.g.
	 * 
	 * <pre>
	 * &lt;?xml version="1.0" encoding="encoding" ?&gt;
	 * &lt;!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" 
	 *      "http://www.w3c.org/TR/xhtml1/DTD/xhtml1.1.dtd"&gt;
	 * </pre>
	 * 
	 * @param out
	 *        the <code>TagWriter</code> to write the data into.
	 */
    public static void writeHTMLStructureStart(DisplayContext context, 
												TagWriter out) throws IOException {

		writeXmlProlog(context, out);
		writeDoctype(context, out);
		writeHtmlStartTag(out);
    }

    public static void writeXmlProlog(DisplayContext context, TagWriter out) throws IOException {
		out.writeXMLHeader(context.getCharacterEncoding());
    }

	public static void writeDoctype(DisplayContext context, TagWriter out) throws IOException {
		out.writeContent(HTMLConstants.DOCTYPE_HTML);
    }

	private static void writeSystemIDReference(DisplayContext context, TagWriter out, String localSystemID) throws IOException {
		// Write absolute URL to the originating server.
		LayoutUtils.appendHostURL(context, out);
		out.write(context.getContextPath());
		out.write(localSystemID);
	}

	public static void writeHtmlStartTag(TagWriter out) throws IOException {
		out.beginBeginTag(HTMLConstants.HTML);
		out.writeAttribute("xmlns", HTMLConstants.XHTML_NS);
		out.endBeginTag();
	}
	
	public static void writeHtmlEndTag(TagWriter out) throws IOException {
		out.endTag(HTMLConstants.HTML);
	}
    
    @Override
    protected void writeInOnload(String aContext, TagWriter anOut, HttpServletRequest aRequest) throws IOException {
    	super.writeInOnload(aContext, anOut, aRequest);
    	getWindowManager().writeInOnload(DefaultDisplayContext.getDisplayContext(aRequest), anOut);
		HTMLUtil.writeJavaScriptContent(anOut, "services.ajax.REQUEST_QUEUE.setAllowedParallelRequests("
			+ getLayoutContext().getLock().getOptions().getMaxWriters() + ");");
		HTMLUtil.writeJavaScriptContent(anOut, "services.ajax.REQUEST_QUEUE.resume();");
		for (HTMLFragment fragment : _onLoad) {
			fragment.write(DefaultDisplayContext.getDisplayContext(aRequest), anOut);
		}
	}

	/**
	 * Adds a {@link HTMLFragment} that is rendered within the <code>onload</code> attribute.
	 */
	public void addOnLoad(HTMLFragment fragment) {
		List<HTMLFragment> before = _onLoad;
		_onLoad = new ArrayList<>(_onLoad.size() + 1);
		_onLoad.addAll(before);
		_onLoad.add(fragment);
	}

	/**
	 * Removes a {@link HTMLFragment} that is rendered within the <code>onload</code> attribute.
	 * 
	 * @see #addOnLoad(HTMLFragment)
	 */
	public void removeOnLoad(HTMLFragment fragment) {
		_onLoad = new ArrayList<>(_onLoad);
		_onLoad.remove(fragment);
	}
    
    @Override
	protected final void writeInOnUnload(String context, TagWriter anOut, HttpServletRequest request) throws IOException {
    	super.writeInOnUnload(context, anOut, request);
    	
    	HTMLUtil.writeJavaScriptContent(anOut, "if (!" + SKIP_NOTIFY_UNLOAD_JS_VARIABLE + ") {");
    	HTMLUtil.writeJavaScriptContent(anOut, NotifyUnload.COMMAND_ID + "();");
//    	anOut.writeJavaScriptContent("return services.ajax.checkHistoryBegin('Really');");
    	HTMLUtil.writeJavaScriptContent(anOut, "}");

    	getWindowManager().writeInOnUnload(DefaultDisplayContext.getDisplayContext(request), anOut);
    }
    
    /**
     * At this time all child-components have been loaded, now set them up.
     */
    @Override
	protected void componentsResolved(InstantiationContext context) {
		LayoutContext layoutContext = getLayoutContext();

		// Prevent components from sending events to other not yet initialized components during
		// their initialization phase.
		layoutContext.forceQueueing();

    	// Make sure, local initialization is complete before children are visited.
    	initWindowManager();

        super.componentsResolved(context);
        
        LayoutResolvedListener thePP = getLayoutResolvedListener();
        thePP.process(this);
        

        /* here all it starts */
        this.setVisible(true);

		createLayoutControl();

		// Start processing events.
		layoutContext.processActions();
	}

	private void createLayoutControl() {
		Set<Entry<LayoutComponent, DialogComponent>> openedDialogs;
		String oldId;
		if (layoutControl != null && layoutControl.isAttached()) {
			/* Check whether the control was already rendered. If not, getting the id will fail. */
			oldId = layoutControl.getID();
			layoutControl.detach();
			openedDialogs = getDialogSupport().getOpenedDialogs().entrySet();
		} else {
			oldId = null;
			openedDialogs = Collections.emptySet();
		}
		// must call after super components resolved as it is needed that all descendants are
		// resolved.
		layoutControl = (BrowserWindowControl) _layoutFactory.createLayout(this);
		LayoutUtils.setWindowScope(DefaultDisplayContext.getDisplayContext(), layoutControl);
		if (oldId != null) {
			getEnclosingFrameScope().addClientAction(new ElementReplacement(oldId, layoutControl));
		}
		_dialogSupport = new DialogSupport(layoutControl);
		for (Entry<LayoutComponent, DialogComponent> entry : openedDialogs) {
			LayoutComponent newComponent = getComponentByName(entry.getKey().getName());
			if (newComponent == null) {
				// component was removed.
				continue;
			}
			OpenModalDialogCommandHandler.openDialog(newComponent, entry.getValue().getWindowTitle());
		}
	}

	/**
	 * {@link ModelScope} of this session.
	 */
	@Override
	public ModelScope getModelScope() {
		return _modelEventForwarder;
	}
    
    /** 
     * Validate the Layout before very first rendering.
     */
    protected void initialValidateModel(DisplayContext aContext) {
        this.globallyValidateModel(aContext);
    }

    /**
	 * Check, whether global state changes are allowed in this session's layout.
	 * 
	 * <p>
	 * Updates to global state are only allowed from a thread executing a
	 * command, because only command threads are guaranteed to be executed
	 * single-threaded.
	 * </p>
	 * 
	 * @param updater
	 *        The object that caused initiated the update.
	 */
	public final void checkUpdate(Object updater) {
		if (_layoutContext != null) {
			_layoutContext.checkUpdate(updater);
		}
	}

    /**
	 * Overridden to care about shortcutIcon and icon.
	 */
    @Override
	public void writeHeader(String contextPath, TagWriter out,
                                    HttpServletRequest req)
                                    throws IOException {
		super.writeHeader(contextPath, out, req);

		String shortcutIconPath = null;
		String iconPath = null;

		if (shortcutIcon != null) {
			shortcutIconPath = req.getContextPath() + shortcutIcon;
		}

		if (icon != null) {
			iconPath = req.getContextPath() + icon;
		}

		FaviconTag.write(out, shortcutIconPath, iconPath);
        
        out.beginScript();

		int nextSequenceNumber = _layoutContext.getLock().reset();
		out.writeIndent();
		out.append("services.ajax.txSequence = ");
		out.writeInt(nextSequenceNumber);
		out.append(';');
		
		// Mark top-level window with the application name to allow the login
		// page to close all openers that belong to the same application.
		out.writeIndent();
		out.append("services.appName = ");
		out.writeJsString(Version.getApplicationName());
		out.append(';');
		
        out.endScript();
        
        // js files for WindowContainer
		HTMLUtil.writeJavascriptRef(out, contextPath, "/script/tl/thread.js");
		HTMLUtil.writeJavascriptRef(out, contextPath, "/script/tl/window.js");
    }
    
    /**
     * Writes the subcomponent independent header.
     */
    public void writeDefaultHeader(ServletContext context, 
                                   TagWriter out, 
                                   HttpServletRequest req, 
                                   boolean writeTitle)
        throws IOException {

		String viewportValue = ThemeFactory.getTheme().getValue(Icons.META_VIEWPORT);
		if (!StringServices.isEmpty(viewportValue)) {
			out.beginBeginTag(META);
			out.writeAttribute(NAME_ATTR, "viewport");
			out.writeAttribute(CONTENT_ATTR, viewportValue);
			out.endEmptyTag();
		}

		ResKey theTitle;
        if (writeTitle && null != (theTitle = getTitleKey())) {
			out.beginTag(HTMLConstants.TITLE);
			out.writeText(Resources.getInstance().getString(theTitle));
			out.endTag(HTMLConstants.TITLE);
        }
        
		FaviconTag.write(out, req.getContextPath());
        includeHeaderFile(context, out);
    }

    /**
     * Writes the complete HTML page for the given LayoutComponent. When not
     * in frame context (<code>MainLayout.isFramed() == false</code>) the whole
     * MainLayout is rendered.
     * @param out the TagWriter that is to be used to render the page.
     * @param lc the LayoutComponent that is to be rendered.
     */
    public static void renderPage(ServletContext context,
                                  HttpServletRequest request  , 
                                  HttpServletResponse response,
                                  TagWriter out, 
                                  LayoutComponent lc)
        throws IOException, ServletException {

        MainLayout  ml           = lc.getMainLayout();
        String      contextPath  = request.getContextPath();

        DisplayContext displayContext = DefaultDisplayContext.getDisplayContext(request);
        ControlScope componentControlScope = lc.getControlScope();
        if (componentControlScope != null) {
        	displayContext.initScope(componentControlScope);
        }
        LayoutUtils.setContextComponent(displayContext, lc);

        // Inform component about the beginning of the rendering process.
		lc.beforeRendering(displayContext);
        try {
			if (!lc.isVisible()) {
				RenderErrorUtil.reportComponentRenderingError(displayContext, out, out.getDepth(),
					"Component '" + lc.getName() + "' invisible: " + lc.getLocation(), null);
        		return;
        	}
	        
	        if (lc.isCompleteRenderer()) {
	            // rendering is controlled by component itself (e.g. JSP-Page)
	            lc.writeBody(context, request, response, out);
	        } 
	        else {
	            // rendering of html-structure is controlled by this method.
				MainLayout.writeHTMLStructureStart(displayContext, out);
	            out.beginTag(HTMLConstants.HEAD);                

				out.beginBeginTag(META);
				out.writeAttribute(NAME_ATTR, "generator");
				out.writeAttribute(CONTENT_ATTR, LicenseTool.productType());
				out.endEmptyTag();

	            lc.writeHeader(contextPath, out, request);
	            if (ml != null)
	                ml.writeDefaultHeader(context, out, request, true);
	            out.endTag(HTMLConstants.HEAD);
				boolean writeBodyTag = (lc instanceof WindowComponent) ||
					(!(lc instanceof LayoutContainer)) ||
					(!((LayoutContainer) lc).isOuterFrameset());

				if (writeBodyTag) {
					// For explanation take a look at LayoutComponent.writeBodyAttributes(...)
					boolean disableScrolling = false;
					UserAgent agent = displayContext.getUserAgent();
					if (agent.is_ie6up() && !agent.is_ie8up()) {
						disableScrolling = (lc instanceof MainLayout) || (lc instanceof WindowComponent);
					}
					lc.writeBodyTag(context, request, response, out, disableScrolling);
				}
				if (lc.isVisible()) {
					int tagDepthBefore = out.getDepth();
					try {
						lc.writeBody(context, request, response, out);
					} catch (Exception ex) {
						RenderErrorUtil.reportComponentRenderingError(displayContext, out, lc, tagDepthBefore, ex);
					}
				} else {
					out.writeText("Reload in Progress, please stand by.");
				}
				if (writeBodyTag) {
					addInfoServiceItems(out, displayContext);
					out.endTag(HTMLConstants.BODY);
				}
				out.endTag(HTMLConstants.HTML);
	        }
        } finally {
        	lc.afterRendering();
        }
    }

	@SuppressWarnings("unchecked")
	private static void addInfoServiceItems(TagWriter out, DisplayContext displayContext) throws IOException {
		if (displayContext.isSet(InfoService.INFO_SERVICE_ENTRIES)) {
			List<HTMLFragment> errors = displayContext.get(InfoService.INFO_SERVICE_ENTRIES);
			out.beginScript();
			out.append(InfoServiceXMLStringConverter.getJSInvocation(displayContext, errors));
			out.endScript();
		}
	}
    
	@Override
	public void writeBody(ServletContext context, HttpServletRequest req, HttpServletResponse resp, TagWriter out)
			throws IOException, ServletException {
		layoutControl.detach();
		layoutControl.write(DefaultDisplayContext.getDisplayContext(req), out);

		for (WindowComponent window : getWindowManager().getWindows()) {
			window.reload();
		}
	}

    /**
	 * Overwritten to reset the scroll position map required by other components to store their
	 * current scroll position.
	 */
	@Override
	public void writeJavaScript(String contextPath, TagWriter out,
			HttpServletRequest req) throws IOException {
        HTMLUtil.writeJavaScriptContent(out, "window.scrollMap = new Array()");
		super.writeJavaScript(contextPath, out, req);
	}

    /**
     * when we are a tablelayout, all the childs are included in our page.
     * in orther case there are refernces.
     */
    public boolean isFramed() {
    	return true;
    }
    
    /**
	 * Create the LayoutRelationManager when this class is created.
	 * 
	 * @param log
	 *        Log to write messages and problems to.
	 */
	public void setupRelations(Log log) {
		acceptVisitorRecursively(new LayoutLinker(log));
    }

	/**
	 * Defines a channel to synchronize selections of multiple components without requiring that
	 * each partner knows each other.
	 * 
	 * @param groupName
	 *        The name of the partner group.
	 * @return The {@link ComponentChannel} to synchronize all partner channels against.
	 */
	public ComponentChannel makePartnerChannel(String groupName) {
		ComponentChannel result = _partnerChannels.get(groupName);
		if (result == null) {
			result = new DefaultChannel(this, "partnerGroup:" + groupName, null);
			_partnerChannels.put(groupName, result);
		}
		return result;
	}

	/**
	 * Gets the {@link MainLayout} of the currently logged in user.
	 * 
	 * @return the {@link MainLayout} of the currently logged in user
	 */
	public static MainLayout getMainLayout(DisplayContext context) {
		final LayoutContext layoutContext = context.getLayoutContext();
		if (layoutContext == null) {
			return null;
		}
		return layoutContext.getMainLayout();
	}

	/**
	 * Gets the {@link MainLayout} for the default {@link DisplayContext}.
	 * 
	 * @see #getMainLayout(DisplayContext)
	 */
	public static MainLayout getDefaultMainLayout() {
		return MainLayout.getMainLayout(DefaultDisplayContext.getDisplayContext());
	}

	/**
	 * Initializes a newly created {@link MainLayout}.
	 * 
	 * @param instantiationContext
	 *        The context for configuration instantiations.
	 */
	@FrameworkInternal
	public static void initializeMainLayout(InstantiationContext instantiationContext, DisplayContext context,
			MainLayout ml, SubsessionHandler layoutContext, TLSubSessionContext subSession) {
		StopWatch watch = StopWatch.createStartedWatch();

		ml.initLayoutContext(subSession);

		boolean before = layoutContext.enableUpdate(true);
		try {
			/* post initialization. (we guarantee, that theMainLayout is available at this point) */
			ml.resolveComponent(new ComponentInstantiationContext(instantiationContext, ml));
			/* Suppress (costly) visibility when there is no real person around. This a) wastes time b)
			 * leads to all sort of errors anyway. */
			TLContext theTLC = TLContext.getContext();
			if (theTLC != null && theTLC.getPerson() != null) {

				ml.setupRelations(instantiationContext);

				/* Install context component and WindowScope. This is needed, because initial
				 * validation restores the homepage which records an action and therefore needs the
				 * window scope. */
				/* Note: This must be called after components resolved, because WindowScope is
				 * installed there. */
				LayoutUtils.setContextComponent(context, ml);
				assert LayoutUtils.getWindowScope(context) != null : "No window scope in main layout found.";

				/* Initialize the models. */
				ml.initialValidateModel(context);
			}
		} finally {
			layoutContext.enableUpdate(before);
		}

		DebugHelper.logTiming(context.asRequest(), "Initializing layout", watch, 500, MainLayout.class);
	}

    /**
	 * @see #getComponent(DisplayContext)
	 */
	@CalledFromJSP
	public static LayoutComponent getComponent(PageContext context) {
    	return getComponent(DefaultDisplayContext.getDisplayContext(context));
    }
    
    /**
	 * Get a Component identified by a unique id in a Request Context.
	 * <p>
	 * While traversion through a layout this is found as attribute in the request. If not found
	 * there we will look it up in the session
	 * </p>
	 * 
	 * @return null when no component could be found. or on invalid IDs
	 */
	public static LayoutComponent getComponent(DisplayContext context) {
		return context.get(ATTRIBUTE_LAYOUT);
    }

	/** returns the holder for dialogs for this instance of MainLayout. */
    public synchronized WindowManager getWindowManager() {
		// No lazy initialization. Must be available during first rendering!
        return this.windowManager;
    }

	private void initWindowManager() {
		if(this.windowManager == null) {
			this.windowManager = new WindowManager(this);

			getEnclosingFrameScope().registerContentHandler("_windows", windowManager);
        }
	}
	
    /**
     * Special behavior since this is the outermost component.
     *
     * @return always true since this should always be visible.
     */
    @Override
	public boolean makeVisible() {
        return true;
    }

    /** Extract a type from some arbitrary object.
     * 
     * We use <code>getClass().getName()</code> here but in TL this 
     * will be the MetaObjectttype.
     * 
     * @return aString describing the type of Object, null for a null object.
     * @deprecated handled via {@link BoundHelper}
     */
    @Deprecated
    public String getTypeFor(Object anObject) {
        if (anObject != null) {
            return anObject.getClass().getName();
        }
        return null;   
    }

    /**
     * Return the super implementation of the given object.
     * 
     * @param    anObject    The object to be inspected.
     * @return   The super class of that object (not its class!).
     * @deprecated handled via {@link BoundHelper}
     */
    @Deprecated
    protected Object getSuperImpl(Object anObject) {
        Class theClass;

        if (!(anObject instanceof Class)) {
            theClass = anObject.getClass();
        }
        else {
            theClass = (Class) anObject;

            if (theClass == Object.class) {
                return (null);
            }
        }

        return (theClass.getSuperclass());
    }

    /** Complex function to display a component for some arbitrary object. 
     * 
     * @return true when some component was found to display the object,
     *         an the component could be made visible. 
     */
    public LayoutComponent showDefaultFor(Object anObject) {
		LayoutComponent toShow = this.findDefaultFor(anObject);
 
        if (toShow != null) {
			toShow.setModel(anObject);
			toShow.makeVisible();
            return toShow;
        }

        return null;   
    }
    
    /** Complex function to display a component for some arbitrary object. 
     * 
     * @return true when some component was found to display the object,
     *         and the component could be made visible. 
     */
    public LayoutComponent findDefaultFor(Object anObject) {
		BoundChecker checker =
			BoundHelper.getInstance().getDefaultBoundChecker(this, anObject, SimpleBoundCommandGroup.READ);

		// The checker may also be the default checker.
		if (checker instanceof LayoutComponent) {
			return this.getComponentByName(((LayoutComponent) checker).getName());
		}
		
		return null;
	}

    /**
	 * Variant of
	 * {@link #getTagWriter(HttpServletRequest, HttpServletResponse)} that can
	 * be used by a JSP page that is not part of a component. 
	 */
    public static TagWriter getTagWriter(PageContext pageContext) {
		ServletRequest request = pageContext.getRequest();
		TagWriter out = RequestUtil.lookupTagWriter(request);
	    if (out == null) {
	    	// The TagWriter must be created with the page's underlying
			// JSPWriter instead of the response writer. Otherwise, the contents
			// generated from the returned TagWriter is output out of order.
			out = new TagWriter(pageContext.getOut());
			RequestUtil.installTagWriter(request, out);
	    }
	    return out;
	}

	/** Return the TagWriter for the current Request/Response.
     * 
     * Will create the HTML-Writer only once.
     */
	public static TagWriter getTagWriter(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        TagWriter wout = RequestUtil.lookupTagWriter(req);
        if (wout == null) {   
			wout = new TagWriter(resp.getWriter());
			RequestUtil.installTagWriter(req, wout);
        }
        return wout;
    }

	@Override
	protected void writeBodyCssClassesContent(TagWriter out) throws IOException {
    	super.writeBodyCssClassesContent(out);
		out.append("mainlayout");
    }

    /**
     * Get the listener to call in {@link #componentsResolved(InstantiationContext)}.
     * 
     * @return an instance of LayoutResolvedListener or a Subclass if configured
     */
    protected LayoutResolvedListener getLayoutResolvedListener() {
        
        LayoutResolvedListener thePP = null;
        if (null != postProcessorClassName) {
            try {
                Class theClass = Class.forName(postProcessorClassName);
				thePP = (LayoutResolvedListener) theClass.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                Logger.warn("Could not instanciate post processor.", e, this);
            }
        } else {
            thePP = new LayoutResolvedListener();
        }
        return thePP;
    }
    
    /**
     * Overwritten to get rid of the strange sroll bars in the IE.
     * AKS TSA/BHU if this makes any problems.
     * 
     * @see com.top_logic.mig.html.layout.LayoutContainer#isOuterFrameset()
     */
    @Override
	public boolean isOuterFrameset() {
        return false;
    }

    public ClientAction[] globallyValidateModel(DisplayContext context) {
        // Install security cache for the process of updating the models.
		boolean installed = BoundComponent.installRequestCacheIfNotExisting();
		boolean isScriptingRecorderEnabled = ScriptingRecorder.isRecordingActive();
		try {
	        
			if (isScriptingRecorderEnabled) {
				ScriptingRecorder.pause();
			}

	        // Forward global model events to this session. This must not happen in
			// the validation loop, because new events may continuously arrive. This
			// might unacceptably deferr model validation.
			processGlobalEvents();

			/* register the LegacyModelValidator which inspects the whole component tree for invalid
			 * components */
			LayoutContext layoutContext = getLayoutContext();
			layoutContext.notifyInvalid(new LegacyModelValidator(this));
			HandlerResult result = layoutContext.runValidation(context);

			if (result.isSuccess()) {
				/* updates all CommandModels which need external updates. Must be done after
				 * validation and event sending as executability may use objects which are deleted
				 * now */
				CommandModelRegistry registry = CommandModelRegistry.getRegistry();
				layoutContext.notifyInvalid(registry);
				HandlerResult commandModelValidationResult = layoutContext.runValidation(context);
				result = commandModelValidationResult;
			}

			return ErrorHandlingHelper.transformHandlerResult(context.getWindowScope(), result);
        } finally {
			if (installed) {
				BoundComponent.uninstallRequestCache();
			}
			if (isScriptingRecorderEnabled) {
				ScriptingRecorder.resume();
			}
        }
    }
    
    /**
     * Include JavaScript libraries that are required by view elements on the
     * client side.
     */
    @Override
	protected void writeJSTags(String contextPath, TagWriter out, HttpServletRequest request) throws IOException {
        super.writeJSTags(contextPath, out, request);
        
        writeTopLevelComponentJSTags(contextPath, out, request);

    }

	/**
	 * Writes the JS files that are needed by Top level component, i.e. {@link MainLayout} and
	 * {@link WindowComponent}.
	 * 
	 * @param contextPath
	 *        Same as in {@link LayoutComponent#writeJSTags(String, TagWriter, HttpServletRequest)}.
	 * @param out
	 *        Same as in {@link LayoutComponent#writeJSTags(String, TagWriter, HttpServletRequest)}.
	 * @param request
	 *        Same as in {@link LayoutComponent#writeJSTags(String, TagWriter, HttpServletRequest)}.
	 * @throws IOException
	 *         If writing to the underlying writer fails.
	 * 
	 * @see LayoutComponent#writeJSTags(String, TagWriter, HttpServletRequest)
	 */
	@FrameworkInternal
	public static void writeTopLevelComponentJSTags(String contextPath, TagWriter out, HttpServletRequest request)
			throws IOException {
		HTMLUtil.writeJavascriptRef(out, contextPath, "/script/tl/dialog.js");
		HTMLUtil.writeJavascriptRef(out, contextPath, "/script/tl/color.js");
		HTMLUtil.writeJavascriptRef(out, contextPath, "/script/tl/layout.js");
		HTMLUtil.writeJavascriptRef(out, contextPath, "/script/tl/countdownTimer.js");

    }

    /**
     * The {@link LayoutFactory} to create {@link LayoutControl} for components in this subsession.
     */
	public LayoutFactory getLayoutFactory() {
		return _layoutFactory;
	}

	@Override
	public DialogSupport getDialogSupport() {
		return _dialogSupport;
	}

	/**
	 * Overridden to implement type-specific visiting for {@link MainLayout}s.
	 * 
	 * @see LayoutComponent#acceptVisitor(LayoutComponentVisitor)
	 */
	@Override
	public boolean acceptVisitor(LayoutComponentVisitor aVisitor) {
		return aVisitor.visitMainLayout(this);
	}
	
	@Override
	public void revalidate(DisplayContext context, UpdateWriter actions) {
		super.revalidate(context, actions);
		getWindowManager().revalidate(actions);
	}
	
    
   // inner classes
    
	/**
	 * The {@link MainLayout.NotifyUnload} command is triggered during the unload of the body of the
	 * {@link MainLayout}. It will be triggered if the user pressed the "F5" button. Triggering of
	 * this command will be suppressed if the server demands a complete reload.
	 * 
	 * @author <a href=mailto:daniel.busche@top-logic.com>Daniel Busche</a>
	 */
	public static class NotifyUnload extends AbstractSystemAjaxCommand {

		public static final String COMMAND_ID = "notifyUnload";

		/**
		 * Configuration of the {@link NotifyUnload}.
		 * 
		 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
		 */
		public interface Config extends AbstractSystemAjaxCommand.Config {

			@StringDefault(COMMAND_ID)
			@Override
			String getId();

			@BooleanDefault(true)
			@Override
			boolean getReadOnly();

			@BooleanDefault(true)
			@Override
			boolean isConcurrent();

		}

		/**
		 * Javascript function that calculates whether the server's notification of an unload must
		 * be executed sequentially or not. This indirection is necessary because IE 6-11 and
		 * Firefox (up to version 91), require a sequential call to the server, whereas Edge and
		 * Chrome (resp. Chromium) do not allow sequential information of the server in unload.
		 */
		public static final String UNLOAD_SEQUENTIAL = "BAL.unloadSequential()";

		public NotifyUnload(InstantiationContext context, Config config) {
			super(context, config);
        }

		@Override
		public HandlerResult handleCommand(DisplayContext commandContext, LayoutComponent component, Object model, Map<String, Object> arguments) {
            MainLayout self = ((MainLayout) component);

			TopLevelComponentScope frameScope = (TopLevelComponentScope) self.getEnclosingFrameScope();
			frameScope.markReloadPending();

            return HandlerResult.DEFAULT_RESULT;
        }

		@Override
		public void appendInvokeExpression(Appendable result, JSObject argumentObject) throws IOException {
			result.append("services.ajax.invokeRead('");
			result.append(getID());
			result.append("', ");
			argumentObject.eval(result);
			result.append(", ");
			result.append("null, ");
			result.append(UNLOAD_SEQUENTIAL);
			result.append(')');
		}

		@Override
		protected boolean mustNotRecord(DisplayContext context, LayoutComponent component,
				Map<String, Object> someArguments) {
			return true;
		}

    }
    
	public BrowserWindowControl getLayoutControl() {
		return layoutControl;
	}

	/**
	 * Dispatches the given event to all of its receivers.
	 * 
	 * @param model
	 *        The affected business model object.
	 * @param changedBy
	 *        The sender of the event. In case of this event being a component internal event, the
	 *        sender must be a {@link LayoutComponent}. In all other cases, the sender is undefined.
	 *        See {@link ModelEventListener} for the classification of events into component
	 *        internal and external events.
	 * @param eventType
	 *        One of the types defined in {@link ModelEventListener}.
	 * 
	 * @see ModelEventListener#handleModelEvent(Object, Object, int)
	 */
	public void broadcastModelEvent(Object model, Object changedBy, int eventType) {
    	if (Logger.isDebugEnabled(this)) {
    	    LayoutUtils.logEvent(eventType,  changedBy, model, this);
    	}

		_layoutContext.forceQueueing();
		try {
			internalBroadcastModelEvent(model, changedBy, eventType);
		} finally {
			_layoutContext.processActions();
		}
	}

	public void processGlobalEvents() {
		this._modelEventForwarder.synthesizeModelEvents();
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
	 * 
	 * @since 5.7
	 */
	public void appendCloseAllWindows(Appendable out, FrameScope source) throws IOException {
		getWindowManager().appendCloseAllWindows(out, source);
	}

	@Override
	protected LayoutComponentScope createEnclosingFrameScope() {
		return TopLevelComponentScope.createTopLevelComponentScope(this, this, getUpdates());
	}

	@Override
	public ApplicationWindowScope getWindowScope() {
		return layoutControl;
	}
	
	// End implementation of WindowScopeProvider
	
	/**
	 * Returns an {@link UpdateQueue} to add {@link ClientAction}s for the GUI.
	 */
	public UpdateQueue getUpdates() {
		return basicAJAXSupport;
	}

	/**
	 * Getter for the global configuration.
	 */
	public static GlobalConfig getGlobalConfig() {
		return ApplicationConfig.getInstance().getConfig(GlobalConfig.class);
	}

	/**
	 * Whether a dialog with a close button could be closed by a background click.
	 */
	public boolean closeDialogOnBackgroundClick() {
		return _closeDialogOnBackgroundClick;
	}

	/** Getter for {@link DocType}. */
	public static DocType getDoctype() {
		return getGlobalConfig().getDocType();
	}

	/** Getter for {@link DocType#DTD}. */
	public static String getDoctypeDTD() {
		DocType docType = getDoctype();
		return docType == null ? null : docType.getDTD();
	}

	/** Getter for {@link DocType#PATH}. */
	public static String getDoctypePath() {
		DocType docType = getDoctype();
		return docType == null ? null : docType.getPath();
	}

	/** Getter for {@link DocType#PUBLIC_ID}. */
	public static String getDoctypePublicID() {
		DocType docType = getDoctype();
		return docType == null ? "" : docType.getPublicID();
	}

}
