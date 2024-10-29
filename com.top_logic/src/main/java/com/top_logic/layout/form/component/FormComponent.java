/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.component;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.Decision;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Derived;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Inspectable;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.config.annotation.defaults.ImplementationClassDefault;
import com.top_logic.basic.config.annotation.defaults.InstanceDefault;
import com.top_logic.basic.config.annotation.defaults.ItemDefault;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.basic.func.Function2;
import com.top_logic.basic.func.Function3;
import com.top_logic.basic.listener.EventType.Bubble;
import com.top_logic.basic.listener.GenericPropertyListener;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.knowledge.wrap.person.PersonalConfiguration;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.basic.CommandHandlerCommand;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.basic.RenderErrorUtil;
import com.top_logic.layout.basic.component.AJAXComponent;
import com.top_logic.layout.basic.component.ControlSupport;
import com.top_logic.layout.component.ComponentUtil;
import com.top_logic.layout.form.CollapsedListener;
import com.top_logic.layout.form.Collapsible;
import com.top_logic.layout.form.CollapsibleFormMember;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormHandler;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.MemberChangedListener;
import com.top_logic.layout.form.declarative.DirectFormControlProvider;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.layout.form.model.FormState;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.form.model.TableField;
import com.top_logic.layout.form.util.FormFieldConstants;
import com.top_logic.layout.structure.LayoutControlProvider;
import com.top_logic.layout.structure.LayoutControlProvider.Layouting;
import com.top_logic.layout.structure.MediaQueryControl;
import com.top_logic.layout.structure.SeparateTableControlProvider;
import com.top_logic.layout.table.component.BuilderComponent;
import com.top_logic.layout.table.model.TableConfig;
import com.top_logic.layout.table.model.TableConfiguration;
import com.top_logic.layout.table.model.TableConfigurationFactory;
import com.top_logic.layout.table.model.TableConfigurationProvider;
import com.top_logic.layout.toolbar.ToolBar;
import com.top_logic.mig.html.ModelBuilder;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.SubComponentConfig;
import com.top_logic.model.TLObject;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.execution.ExecutableState;

/**
 * {@link LayoutComponent} for editing forms.
 * 
 * <p>
 * The form model must be provided by a {@link ModelBuilder}, see {@link Config#getModelBuilder()}.
 * The actual form layout is defined either by a {@link LayoutControlProvider} (see
 * {@link Config#getComponentControlProvider()}), or a JSP (see {@link Config#getPage()}).
 * </p>
 * 
 * @see EditComponent Creating forms that also have a view mode optimized for reading.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 * @author <a href="mailto:kha@top-logic.com">Klaus Halfmann</a>
 * @author Dieter Rothbächer
 */
public class FormComponent extends BuilderComponent implements FormHandler, FormFieldConstants,
		TableFieldConfigurator {

	/**
	 * Configuration options for {@link FormComponent}.
	 */
	@TagName(Config.TAG_NAME)
	public interface Config extends BuilderComponent.Config {

		/**
		 * @see SubComponentConfig#getComponents()
		 */
		String TAG_NAME = "form";

		/** @see #isResetInvalidate() */
		String RESET_INVALIDATE = "resetInvalidate";

		@Override
		@ClassDefault(FormComponent.class)
		Class<? extends LayoutComponent> getImplementationClass();

		/**
		 * The JSP page used for rendering.
		 * 
		 * <p>
		 * If no page is given, the component must have a {@link #getComponentControlProvider()}
		 * that does custom rendering.
		 * </p>
		 */
		@Name(PAGE_ATTRIBUTE)
		@Nullable
		@Format(ResourceReferenceFormat.class)
		String getPage();

		/** @see #getPage() */
		void setPage(String value);

		/**
		 * Whether form state (expansion of groups) should be automatically saved to and loaded from
		 * the {@link PersonalConfiguration}.
		 */
		@Name(PERSISTENT_STATE_ATTRIBUTE)
		@BooleanDefault(true)
		boolean getPersistentState();

		/**
		 * {@link TableConfig}s to be applied to tables displayed within this form.
		 */
		@Key(TableConfig.TABLE_NAME_ATTRIBUTE)
		Map<String, ? extends FormTableConfig> getTables();

		/**
		 * Whether warnings upon apply should be ignored for all commands in this component.
		 * 
		 * @see AbstractApplyCommandHandler.GlobalConfig#getWarningsDisabled()
		 * @see FormComponent.Config#getIgnoreWarnings()
		 * @see AbstractApplyCommandHandler.Config#getIgnoreWarnings()
		 */
		@Name(IGNORE_WARNINGS_ATTRIBUTE)
		Decision getIgnoreWarnings();

		/**
		 * Whether to reset the form when component is invalidated.
		 * 
		 * @see #getResetInvisible()
		 */
		@Name(RESET_INVALIDATE)
		@BooleanDefault(true)
		boolean isResetInvalidate();

		@Override
		@BooleanDefault(true)
		boolean hasToolbar();

		@Override
		@ItemDefault
		@ImplementationClassDefault(FormComponent.LegacyBuilder.class)
		public PolymorphicConfiguration<? extends ModelBuilder> getModelBuilder();

		@Override
		@InstanceDefault(MediaQueryControl.Layout.class)
		Layouting getContentLayouting();

		@Override
		@Derived(fun = FormControlProviderSelection.class, args = {
			@Ref(XML_TAG_COMPONENT_CONTROLPROVIDER_NAME), @Ref(ATT_RENDER_INLINE), @Ref(PAGE_ATTRIBUTE) })
		LayoutControlProvider getActiveComponentControlProvider();

		/**
		 * Implementation of {@link Config#getActiveComponentControlProvider()}.
		 * 
		 * <p>
		 * Selects either {@link Config#getComponentControlProvider()} if set, or
		 * {@link DirectFormControlProvider}, if no {@link Config#getPage()} is set. In all other
		 * cases,
		 * {@link com.top_logic.mig.html.layout.LayoutComponent.Config.ComponentControlProviderSelection}
		 * is used.
		 * </p>
		 */
		class FormControlProviderSelection
				extends
				Function3<LayoutControlProvider, PolymorphicConfiguration<LayoutControlProvider>, Boolean, String> {
			Function2<LayoutControlProvider, PolymorphicConfiguration<LayoutControlProvider>, Boolean> _super =
				new ComponentControlProviderSelection();

			@Override
			public LayoutControlProvider apply(PolymorphicConfiguration<LayoutControlProvider> customProvider,
					Boolean renderInline,
					String page) {
				if (customProvider != null) {
					return TypedConfigUtil.createInstance(customProvider);
				}
				if (page == null) {
					DirectFormControlProvider.Config<?> controlProviderConf =
						TypedConfiguration.newConfigItem(DirectFormControlProvider.Config.class);
					return TypedConfigUtil.createInstance(controlProviderConf);
				}
				return _super.apply(customProvider, renderInline);
			}

		}
	}

	/**
	 * Attribute for configuring the {@link #page} property.
	 */
	public static final String PAGE_ATTRIBUTE = "page";

	/**
	 * Attribute for configuring the {@link #persistFormState} property.
	 */
	public static final String PERSISTENT_STATE_ATTRIBUTE = "persistentState";

	/**
	 * Attribute for configuring the {@link #checkWarnings()} property.
	 */
	public static final String IGNORE_WARNINGS_ATTRIBUTE = "ignoreWarnings";

	private static final Property<Map<String, ToolBar>> TABLE_TOOLBARS = TypedAnnotatable.propertyMap("tableToolbars");

	/**
	 * A path to a (JSP-)page that renders the user interface for the
	 * {@link com.top_logic.layout.form.model.FormContext} of this component.
	 * 
	 * The path is relative to the the context path of the application.
	 */
	@Inspectable
    protected String page;
    
    /** 
     * The {@link com.top_logic.layout.form.model.FormContext} of this from. The form context may be null, if . 
     *
     * Note: The form context <b>must</b> be private, because the form context is initalized 
     * lazily upon its first use. Therefore, all accesses to the form context must use the 
     * {@link #getFormContext()} method to trigger initialization and avoid dependencies on 
     * the former control flow.   
     */
	@Inspectable
    private FormContext formContext;
    

    /**
     * @see #shouldPersistFormState()
	 */
	private boolean persistFormState;

	/**
	 * Whether warnings are checked in this component.
	 */
	private final boolean _checkComponentWarnings;

	private final Map<String, TableConfigurationProvider> _tables;

	/**
	 * {@link TableConfigurationProvider} used for all tables that are not explicitly configured.
	 */
	private TableConfigurationProvider _defaultProvider;
	
	private final MemberChangedListener _toolbarExchange = new MemberChangedListener() {
		@Override
		public Bubble memberAdded(FormContainer parent, FormMember member) {
			ToolBar toolbar = get(TABLE_TOOLBARS).get(member.getName());
			if (toolbar != null) {
				TableField table = ((TableField) member);
				table.setToolBar(toolbar);
			}
			return Bubble.BUBBLE;
		}
		
		@Override
		public Bubble memberRemoved(FormContainer parent, FormMember member) {
			if (get(TABLE_TOOLBARS).containsKey(member.getName())) {
				((TableField) member).setToolBar(null);
			}
			return Bubble.BUBBLE;
		}
	};

    /**
	 * Initialize from a collection of XML attributes.
	 * 
	 * Supported attributes:
	 * <dl>
	 * 	<dt>page</dt> <dd>{@link #page}</dd>
	 * </dl>
	 */
	public FormComponent(InstantiationContext context, Config config) throws ConfigurationException {
		super(context, config);
		this.page = config.getPage();
		this.persistFormState = config.getPersistentState();
		_tables = initTables(context, config);
		_checkComponentWarnings = checkWarnings(config);
    }

	/** Allow subclasses to set page as desired (even null) */
	protected FormComponent(InstantiationContext context, Config config, String aPage) throws ConfigurationException {
		super(context, config);
		this.page = StringServices.nonEmpty(aPage);
		this.persistFormState = false;
		_tables = initTables(context, config);
		_checkComponentWarnings = checkWarnings(config);
    }
	
	private boolean checkWarnings(Config config) {
		boolean checkGloballyRequested = !AbstractApplyCommandHandler.warningsDisabledGlobally();
		return Decision.not(config.getIgnoreWarnings()).toBoolean(checkGloballyRequested);
	}

	private Map<String, TableConfigurationProvider> initTables(InstantiationContext context, Config config) {
		@SuppressWarnings("unchecked")
		Class<? extends TableConfig> defaultTableType = (Class<? extends TableConfig>) config.descriptor()
			.getProperty("tables").getDefaultDescriptor().getConfigurationInterface();
		_defaultProvider =
			TableConfigurationFactory.toProvider(context, TypedConfiguration.newConfigItem(defaultTableType));

		Collection<? extends TableConfig> tables = config.getTables().values();
		if (tables.isEmpty()) {
			return Collections.emptyMap();
		} else {
			HashMap<String, TableConfigurationProvider> providers = new HashMap<>();
			for (TableConfig table : tables) {
				providers.put(tableName(table), TableConfigurationFactory.toProvider(context, table));
			}
			return providers;
		}
	}

	@Override
	protected void becomingInvisible() {
		super.becomingInvisible();

		if (config().getResetInvisible()) {
			removeFormContext();
		}
	}

	private Config config() {
		return (Config) getConfig();
	}

    /**
     * Since the page contains complete HTML this must be true.
     * 
     * @return <code>true</code>
     */
	@Override
    public boolean isCompleteRenderer() {
        return true;
    }
    
    /**
	 * @see Config#getPersistentState()
	 */
    public final boolean shouldPersistFormState() {
    	return this.persistFormState;
	}

	/**
	 * Whether warnings should be checked in this component.
	 */
	public final boolean checkWarnings() {
		return _checkComponentWarnings;
	}

    /** Will be called to include the actual page. */
	@Override
    public void writeBody(ServletContext context,
    		HttpServletRequest request, HttpServletResponse response,
    		TagWriter out
    ) throws IOException, ServletException {

        String pathToPage = this.getPage();
		if (pathToPage == null) {
			RenderErrorUtil.reportComponentRenderingError(DefaultDisplayContext.getDisplayContext(request), out,
				out.getDepth(),
				"No page defined for component '" + getName() + "': " + getLocation(),
				null);
			return;
		}
		out.flushBuffer();
        // KHA: This check will prevent adding Parameters like ?x=1 to the path
        // if (context.getResource(pathToPage) != null) {
            context.getRequestDispatcher(pathToPage).include(request, response);
        /*
        } else {
            Logger.error("Page (" + pathToPage + ") does not exist!", this);
        }
        */
    }

	@Override
    public boolean receiveDialogEvent(Object aDialog, Object anOwner, boolean dialogOpened) {
		if (!this.isVisible()) {
			return false;
		}
		if (this.getDialogParent() == anOwner) {
			// This component is the being opened as dialog. Do not mangle its
			// immutable state.
			return false;
		}

		return super.receiveDialogEvent(aDialog, anOwner, dialogOpened);
	}

	@Override
	protected void afterModelSet(Object oldModel, Object newModel) {
		super.afterModelSet(oldModel, newModel);
		updateForm();
		this.resetScrollPosition();
	}

	@Override
	public void invalidate() {
		super.invalidate();

		if (config().isResetInvalidate()) {
			updateForm();
		}
	}

	/**
	 * Update the {@link #getFormContext()} after a new model has been set or the model has changed
	 * internally.
	 */
	protected void updateForm() {
		removeFormContext();
	}

	/**
     * Check for changes of model by some other component.
     * 
     * Must reset the FormContext to refetch the changed model.
     */
	@Override
    protected boolean receiveModelChangedEvent(Object aModel, Object changedBy) {
    	boolean wasInvalidated = super.receiveModelChangedEvent(aModel, changedBy);
    	
        if (wasInvalidated && hasFormContext()) {
			updateForm();
        }
        
        return wasInvalidated;
    }

    /** In case our model was deleted, take care of FormContext */
	@Override
    protected boolean receiveModelDeletedEvent(Set<TLObject> aModel, Object changedBy) {
		boolean becameInvalid;
		if (changedBy != this && hasFormContext() && aModel.contains(getModel())) {
            try {
                removeFormContext();
            } catch (Exception exp) {
                Logger.error("Failed to receiveModelChangedEvent()", exp, this);
            } 
			becameInvalid = true;
		} else {
			becameInvalid = false;
        }
		boolean superInvalidated = super.receiveModelDeletedEvent(aModel, changedBy);
		return becameInvalid || superInvalidated;
    }

    /**
     * @see com.top_logic.layout.basic.component.ControlComponent#createControlSupport()
     */
	@Override
    protected ControlSupport createControlSupport() {
        return new FormControlSupport(this);
    }

	/**
	 * Accessor to the lazily created {@link #formContext}.
	 * 
	 * This method is synchronized to protect the lazy creation of the form context against multiple
	 * threads accessing this component concurrently.
	 * 
	 * @see #FORM_CONTEXT_EVENT
	 */
    @Override
	public synchronized FormContext getFormContext() {
        if (this.formContext == null) {
			FormContext newFormContext = this.createFormContext();
            if (newFormContext != null) {
            	if (shouldPersistFormState()) {
            		FormState.loadFormState(PersonalConfiguration.getPersonalConfiguration(), this, newFormContext);
					newFormContext.addListener(Collapsible.COLLAPSED_PROPERTY, new FormContextObserver());
            	}
            	installFormContext(newFormContext);

				applyToolbars(newFormContext);
            }
        }
        return this.formContext;
    }

	/**
	 * Installs a {@link ToolBar} for a {@link TableField} rendered externally.
	 * 
	 * @param tableFieldName
	 *        The table field name.
	 * @param toolbar
	 *        The toolbar to use.
	 * 
	 * @see SeparateTableControlProvider
	 */
	public void setToolbar(String tableFieldName, ToolBar toolbar) {
		mkMap(TABLE_TOOLBARS).put(tableFieldName, toolbar);

		if (hasFormContext()) {
			applyToolbar(getFormContext(), tableFieldName);
		}
	}

	private void applyToolbars(FormContext context) {
		for (String tableFieldName : get(TABLE_TOOLBARS).keySet()) {
			applyToolbar(context, tableFieldName);
		}
	}
	
	private void applyToolbar(FormContext context, String tableFieldName) {
		if (!context.hasMember(tableFieldName)) {
			return;
		}
		TableField tableField = (TableField) context.getField(tableFieldName);
		FormContainer parent = tableField.getParent();
		parent.addListener(FormGroup.MEMBER_ADDED_PROPERTY, _toolbarExchange);
		parent.addListener(FormGroup.MEMBER_REMOVED_PROPERTY, _toolbarExchange);
		_toolbarExchange.memberAdded(parent, tableField);
	}

	private void unregisterToolbars(FormContext context) {
		for (String tableFieldName : get(TABLE_TOOLBARS).keySet()) {
			unregisterToolbar(context, tableFieldName);
		}
	}

	private void unregisterToolbar(FormContext context, String tableFieldName) {
		if (!context.hasMember(tableFieldName)) {
			return;
		}
		TableField table = (TableField) context.getField(tableFieldName);
		table.setToolBar(null);
	}

	@Override
	public boolean isModelValid() {
		return super.isModelValid() && toolbarsApplied();
	}

	private boolean toolbarsApplied() {
		if (hasFormContext()) {
			return true;
		}
		if (isSet(TABLE_TOOLBARS)) {
			// A form with externally set toolbars must not create the form context during
			// rendering, see above.
			return false;
		}
		return true;
	}

	@Override
	public boolean validateModel(DisplayContext context) {
		boolean result = super.validateModel(context);

		if (isSet(TABLE_TOOLBARS)) {
			// Prevent lazy initialization during rendering.
			getFormContext();
		}

		return result;
	}

	/**
	 * Updates the {@link FormContext} of this component.
	 * 
	 * @param newFormContext
	 *        The {@link FormContext} to be installed, <code>null</code> means
	 *        {@link #removeFormContext()}.
	 * 
	 * @deprecated There is no need to externally provide a {@link FormContext}. Use
	 *             {@link #removeFormContext()} to enforce re-creating a new context upon
	 *             {@link #getFormContext()}.
	 */
    @Deprecated
	public final void setFormContext(FormContext newFormContext) {
		if (newFormContext == null) {
			removeFormContext();
		} else {
			installFormContext(newFormContext);
		}
    }

	/**
	 * Installs a new non-<code>null</code> {@link FormContext}.
	 * 
	 * @see #removeFormContext()
	 */
	protected void installFormContext(FormContext newFormContext) {
		assert newFormContext != null : "FormContext must not be null. Use removeFormContext() for dropping a context.";
		internalSetFormContext(newFormContext);
	}

	private void internalSetFormContext(FormContext newFormContext) {
		FormContext oldFormContext = this.formContext;
		if (newFormContext == oldFormContext) {
			return;
		}

		if (oldFormContext != null) {
			resetControls();

			unregisterToolbars(oldFormContext);
		}
		
		if (newFormContext != null) {
			setupFormContext(newFormContext);
		}
		
		this.formContext = newFormContext;

		handleFormContextChange(oldFormContext, newFormContext);

		firePropertyChanged(FORM_CONTEXT_EVENT, this, oldFormContext, newFormContext);
	}

	/**
	 * Called whenever the {@link FormContext} of this component changes.
	 * 
	 * @param oldFormContext
	 *        The {@link FormContext} before the change, or <code>null</code> if no
	 *        {@link FormContext} was installed before.
	 * @param newFormContext
	 *        The new {@link FormContext} installed, or <code>null</code> if the {@link FormContext}
	 *        was removed.
	 */
	protected void handleFormContextChange(FormContext oldFormContext, FormContext newFormContext) {
		// Hook for sub-classes.
	}

	/**
	 * Setup the {@link FormContext}. This method can modify the {@link FormContext} after
	 * {@link #createFormContext() creating} or {@link #setFormContext(FormContext) setting} a new
	 * context.
	 * 
	 * @param newFormContext
	 *        the new context which will be returned by {@link #getFormContext()}.
	 */
	protected void setupFormContext(FormContext newFormContext) {
		// Note: This should have been done by the builder, since building the form context
		// may require access to the component. To allow this, the linking should be established
		// right after constructing the form context.
		FormComponent.initFormContext(this, this, newFormContext);

		newFormContext.setFieldsToDefaultValues();
	}
    
    /**
	 * Constructs a {@link FormContext} for the this component's {@link #getModel() model}.
	 * 
	 * <p>
	 * This method is called, whenever the model changes to build a new {@link FormContext}.
	 * </p>
	 */
	public FormContext createFormContext() {
		Object model = getModel();
		if (!ComponentUtil.isValid(model)) {
			return null;
		}
		return (FormContext) getBuilder().getModel(model, this);
	}

    /**
     * whether this component has a {@link #formContext}.
     */
    @Override
	public boolean hasFormContext() {
    	return formContext != null;
    }

    /**
	 * Removes the {@link FormContext} from this component.
	 * 
	 * <p>
	 * After this method has been called, {@link #hasFormContext()} returns <code>false</code> and
	 * {@link #createFormContext()} will be called during the next call to {@link #getFormContext()}.
	 * </p>
	 * 
	 * @see #installFormContext(FormContext)
	 */
	public void removeFormContext() {
    	internalSetFormContext(null);
    }

    /**
     * @see #page 
     */
    public String getPage() {
        return page;
    }

    /**
     * @see #page 
     */
    public void setPage(String aPage) {
        page = aPage;
    }

	private static final Property<LayoutComponent> CONTEXT_COMPONENT =
		TypedAnnotatable.property(LayoutComponent.class, "contextComponent");

	/**
	 * The FormControlSupport is a special {@link ControlSupport} which detaches
	 * all displayed {@link Control}s when a complete redraw happens.
	 * 
	 * @author <a href=mailto:dbu@top-logic.com>dbu</a>
	 */
    protected class FormControlSupport extends ControlSupport {
        /**
		 * Creates a {@link FormControlSupport}.
		 * 
		 * @param aComponent
		 *        See {@link #getComponent()}.
		 */
        public FormControlSupport(AJAXComponent aComponent) {
            super(aComponent);
        }

    	@Override
        public void startRendering() {
        	super.startRendering();
            // Detach all currently displayed controls, because this component is rendered 
            // by a JSP and it is not known in advance which controls will be rendered.
            detachDisplayedControls();
        }
    }

	/**
	 * {@link GenericPropertyListener} to make changes to {@link FormGroup#isCollapsed()} persistent.
	 * 
	 * @author <a href=mailto:daniel.busche@top-logic.com>Daniel Busche</a>
	 */
	private final class FormContextObserver implements CollapsedListener {

		public FormContextObserver() {
			// need no arguments.
		}

		@Override
		public Bubble handleCollapsed(Collapsible collapsible, Boolean oldValue, Boolean newValue) {
			// Update the personal configuration.
			FormState.saveCollapsedState(PersonalConfiguration.getPersonalConfiguration(), FormComponent.this,
				(CollapsibleFormMember) collapsible);
			return Bubble.BUBBLE;
		}
	}

	/**
	 * Utility method to collect all errors of formfields in the given
	 * {@link com.top_logic.layout.form.model.FormContext} and add them to the given {@link com.top_logic.tool.boundsec.HandlerResult}.
	 * 
	 * <p>
	 * This method simplifies the work of a {@link com.top_logic.tool.boundsec.CommandHandler} after it
	 * detects that some fields have errors attached.
	 * </p>
	 */
	public static HandlerResult addFieldErrors(HandlerResult aResult, FormContext aFormCtx) {
		for (Iterator<? extends FormField> theIt = aFormCtx.getDescendantFields(); theIt.hasNext(); ) {
			FormField theField = theIt.next();
			if (theField.hasError()) {
				aResult.addErrorText(theField.getError());
			}
		}
		return aResult;
	}

	/**
	 * Hook for computing the name to index a configured {@link TableConfig}.
	 */
	protected String tableName(TableConfig tableConfig) {
		return tableConfig.getTableName();
	}

	/**
	 * Retrieve the configuration from the component XML with the given name.
	 */
	protected final TableConfiguration getTableConfiguration(String name) {
		TableConfiguration result = TableConfigurationFactory.table();
		adaptTableConfiguration(name, result);
		return result;
	}

	@Override
	public final void makeConfigurable(SelectField field) {
		makeConfigurable(field, getConfigName(field));
	}

	/**
	 * Provider for configuration names for {@link FormMember}s
	 * 
	 * @see #makeConfigurable(SelectField)
	 */
	protected String getConfigName(FormMember field) {
		return field.getName();
	}

	@Override
	public void makeConfigurable(SelectField field, final String configName) {
		TableConfigurationProvider customConfig = lookupTableConfigurationBuilder(configName);
		if (customConfig == null) {
			return;
		}

		final TableConfigurationProvider defaultProvider = field.getTableConfigurationProvider();
		final TableConfigurationProvider customProvider = customConfig;
		if (defaultProvider == null) {
			field.setTableConfigurationProvider(customProvider);
		} else {
			field.setTableConfigurationProvider(TableConfigurationFactory.combine(defaultProvider, customConfig));
		}
	}

	@Override
	public final void adaptTableConfiguration(String name, TableConfiguration table) {
		TableConfigurationProvider provider = lookupTableConfigurationBuilder(name);
		TableConfigurationFactory.apply(provider, table);
	}

	@Override
	public final TableConfigurationProvider lookupTableConfigurationBuilder(String name) {
		TableConfigurationProvider result = _tables.get(name);
		if (result == null) {
			return _defaultProvider;
		}
		return result;
	}

	/**
	 * Creates and {@link #initFormContext(LayoutComponent, FormHandler, FormContext) initializes} a
	 * {@link FormContext} for the given {@link LayoutComponent}.
	 * <p>
	 * The {@link FormContext} will uses the {@link ResPrefix} from the component
	 * </p>
	 * <p>
	 * The type parameter is just a workaround to specify that the parameter has to be both a
	 * {@link LayoutComponent} and a {@link FormHandler}. Java has no "normal" way to specify such a
	 * type combination.
	 * </p>
	 */
	public static <T extends LayoutComponent & FormHandler> FormContext createFormContext(T component) {
		FormContext formContext = new FormContext(component);
		initFormContext(component, component, formContext);
		return formContext;
	}

	/**
	 * Installs the link {@link FormContext} to its owning {@link FormHandler}.
	 * 
	 * <p>
	 * Note: This method is a workaround for the fact that a SelectField rendered as
	 * SelectionControl requires the context component for opening a separate window for choosing
	 * options.
	 * </p>
	 * 
	 * @param aComponent
	 *        The component managing the given {@link FormContext}.
	 * @param self
	 *        The owning component.
	 * @param context
	 *        The new {@link FormContext}.
	 * 
	 * @see #componentForMember(FormMember)
	 * @see #formHandlerForMember(FormMember)
	 */
    public static void initFormContext(LayoutComponent aComponent, FormHandler self, FormContext context) {
		context.set(FormComponent.CONTEXT_COMPONENT, aComponent);
		context.setOwningModel(self);
    }

	/**
	 * The {@link LayoutComponent} that owns the {@link FormContext} of the given {@link FormMember}
	 * .
	 * 
	 * @see #initFormContext(LayoutComponent, FormHandler, FormContext)
	 */
    public static LayoutComponent componentForMember(FormMember member) {
        FormContext context = member.getFormContext();
		if (context == null) {
			return null;
		}
		return context.get(CONTEXT_COMPONENT);
    }

	/**
	 * The {@link FormHandler} that owns the {@link FormContext} of the given {@link FormMember}.
	 * 
	 * @see #initFormContext(LayoutComponent, FormHandler, FormContext)
	 */
    public static FormHandler formHandlerForMember(FormMember member) {
		FormContext context = member.getFormContext();
		if (context == null) {
			return null;
		}
		return context.getOwningModel();
    }

    @Override
	public Command getApplyClosure() {
    	return null;
    }
    
    @Override
	public Command getDiscardClosure() {
		CommandHandler cancelCommand = getCancelCommand();
    	if (cancelCommand == null) {
    		return null;
    	}
		final ExecutableState executable =
			cancelCommand.isExecutable(this, CommandHandler.NO_ARGS);
    	if (executable.isExecutable()) {
			return new CommandHandlerCommand(cancelCommand, this);
    	}
    	return null;
    }

	/**
	 * Returns the {@link ResKey title} key for the given table.
	 * 
	 * @param tableName
	 *        The name of the table to get title key for.
	 */
	public final ResKey getTitleKey(String tableName) {
		TableConfigurationProvider tableConfig = lookupTableConfigurationBuilder(tableName);

		ResKey titleKey;
		if (tableConfig != null) {
			TableConfiguration table = TableConfigurationFactory.build(tableConfig);
			titleKey = table.getTitleKey();
			if (titleKey == null) {
				titleKey = getResPrefix().key(tableName);

				ResPrefix tableResPrefix =
					table.getResPrefix() instanceof ResPrefix ? (ResPrefix) table.getResPrefix() : null;
				if (tableResPrefix != null) {
					titleKey = ResKey.fallback(tableResPrefix.key("title"), titleKey);
				}
			}
		} else {
			titleKey = ResKey.fallback(getResPrefix().key(tableName), getTitleKey());
		}
		return titleKey;
	}

	/**
	 * {@link ModelBuilder} that is used, if {@link FormComponent#createFormContext()} is
	 * implemented directly instead of implementing a {@link ModelBuilder}.
	 */
	public static class LegacyBuilder implements ModelBuilder {

		/**
		 * Singleton {@link LegacyBuilder} instance.
		 */
		public static final LegacyBuilder INSTANCE = new LegacyBuilder();

		private LegacyBuilder() {
			// Singleton constructor.
		}

		@Override
		public Object getModel(Object businessModel, LayoutComponent aComponent) {
			throw new UnsupportedOperationException(
				"You must either overrided createFormContext(), or configure a `modelBuilder`: "
					+ aComponent.getLocation());
		}

		@Override
		public boolean supportsModel(Object aModel, LayoutComponent aComponent) {
			return true;
		}

	}

}
