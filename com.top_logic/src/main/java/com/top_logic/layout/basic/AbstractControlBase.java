/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import com.top_logic.base.services.simpleajax.AbstractCssClassUpdate;
import com.top_logic.base.services.simpleajax.ClientAction;
import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.ExceptionUtil;
import com.top_logic.basic.Logger;
import com.top_logic.basic.col.Maybe;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.listener.EventType;
import com.top_logic.basic.listener.NoBubblingEventType;
import com.top_logic.basic.listener.PropertyListener;
import com.top_logic.basic.listener.PropertyObservable;
import com.top_logic.basic.listener.PropertyObservableBase;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.event.infoservice.InfoService;
import com.top_logic.layout.CommandListener;
import com.top_logic.layout.Control;
import com.top_logic.layout.ControlScope;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.FrameScope;
import com.top_logic.layout.InvalidationListener;
import com.top_logic.layout.LocalScope;
import com.top_logic.layout.LocalScope.LocalScopeValidator;
import com.top_logic.layout.NotifyListener;
import com.top_logic.layout.ProcessingInfo;
import com.top_logic.layout.ProcessingKind;
import com.top_logic.layout.Renderer;
import com.top_logic.layout.UpdateListener;
import com.top_logic.layout.UpdateQueue;
import com.top_logic.layout.Validator;
import com.top_logic.layout.WindowScope;
import com.top_logic.layout.WindowScopeProvider;
import com.top_logic.layout.admin.component.PerformanceMonitor;
import com.top_logic.layout.basic.component.ControlComponent.DispatchAction;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.component.FormComponent;
import com.top_logic.layout.form.component.I18NConstants;
import com.top_logic.layout.form.control.ButtonControl;
import com.top_logic.layout.scripting.action.ControlAction;
import com.top_logic.layout.scripting.action.ControlAction.Argument;
import com.top_logic.layout.scripting.recorder.ScriptingRecorder;
import com.top_logic.layout.scripting.recorder.gui.inspector.GuiInspectorCommand;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.ModelResolver;
import com.top_logic.layout.structure.LayoutControl;
import com.top_logic.layout.template.NoSuchPropertyException;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.mig.html.HTMLUtil;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.LayoutComponentScope;
import com.top_logic.mig.html.layout.MainLayout;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * The {@link AbstractControlBase} implements the command management defined in {@link Control} and
 * provides support for marking a control completely invalid by {@link #requestRepaint() requesting
 * a repaint}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractControlBase extends PropertyObservableBase implements Control, HTMLConstants,
		UpdateListener, CommandListener, ControlScope, WindowScopeProvider, InvalidationListener {

	/**
	 * Name of the template variable creating the built-in CSS classes.
	 * 
	 * @see #writeControlClassesContentProperty(Appendable)
	 */
	protected static final String CSS_CLASS = "cssClass";

	/**
	 * The {@link #isAttached()} property observable through
	 * {@link #addListener(EventType, PropertyListener)}.
	 * 
	 * @see AttachedPropertyListener
	 */
	public static final EventType<AttachedPropertyListener, AbstractControlBase, Boolean> ATTACHED_PROPERTY =
		new NoBubblingEventType<>("attached") {

			@Override
			protected void internalDispatch(AttachedPropertyListener listener, AbstractControlBase sender,
					Boolean oldValue, Boolean newValue) {
				listener.handleAttachEvent(sender, oldValue, newValue);
			}
		};
	
	/**
	 * The {@link #isRepaintRequested()} property observable through
	 * {@link #addListener(EventType, PropertyListener)}.
	 * 
	 * @see UpToDateListener
	 */
	public static final EventType<UpToDateListener, AbstractControlBase, Boolean> UP_TO_DATE_PROPERTY =
		new NoBubblingEventType<>("up-to-date") {

			@Override
			protected void internalDispatch(UpToDateListener listener, AbstractControlBase sender, Boolean oldValue,
					Boolean newValue) {
				listener.isUpToDate(sender, oldValue, newValue);
			}

		};

	/**
	 * CSS class for the control tag of the control to identify it on the client-side.
	 */
	public static final String IS_CONTROL_CSS_CLASS = "is-control";

	/**
	 * CSS class that marks such controls that can be inspected on the client-side.
	 * 
	 * @see #canInspect()
	 */
	public static final String CAN_INSPECT_CSS_CLASS = "can-inspect";

	private String id;

	/**
	 * {@link ControlCommand}s indexed by their command name.
	 * 
	 * @see ControlCommand#getID()
	 * @see CommandListener#executeCommand(DisplayContext, String, Map)
	 */
	private final Map<String, ControlCommand> commandsByName;

	/** The state managed by this class */
	private boolean attached = false;

	/** The {@link ControlScope} this {@link AbstractControlBase} is attached to, if it is attached */
	private ControlScope _scope;

	private boolean repaintRequired = false;

	private LocalScope localScope;

	private final WithPropertiesDelegate _propertyDelegate;

	/**
	 * the {@link FrameScope} which was used to create the id of this control. may be
	 * <code>null</code> if {@link #getID()} was created during rendering.
	 */
	private FrameScope externalFrameScope;

	public AbstractControlBase(Map<String, ControlCommand> commandsByName) {
		this.commandsByName = CollectionUtil.nonNull(commandsByName);
		_propertyDelegate = WithPropertiesDelegateFactory.lookup(getClass());
	}
	
	public AbstractControlBase() {
		this.commandsByName = Collections.emptyMap();
		_propertyDelegate = WithPropertiesDelegateFactory.lookup(getClass());
	}

	@Override
	public Object getPropertyValue(String propertyName) throws NoSuchPropertyException {
		return _propertyDelegate.getPropertyValue(this, propertyName);
	}

	@Override
	public Optional<Collection<String>> getAvailableProperties() {
		return Optional.of(_propertyDelegate.getAvailableProperties(this));
	}

	@Override
	public void renderProperty(DisplayContext context, TagWriter out, String propertyName) throws IOException {
		_propertyDelegate.renderProperty(context, out, this, propertyName);
	}

	/**
	 * Implemented to manage the {@link #isAttached()} state.
	 * <p>
	 * The actual implementation of attaching to the model must be provided by sub-classes in
	 * {@link #internalAttach()} and {@link #attachRevalidated()}.
	 * </p>
	 * <p>
	 * <b>Note: This method is framework internal and must not be called.</b>
	 * </p>
	 * 
	 * @return Whether the attachment operation was actually performed.
	 * @throws IllegalArgumentException
	 *             if it is not possible to use the given {@link ControlScope}.
	 * @see #internalAttach()
	 * @see #attachRevalidated()
	 */
	protected final boolean attach(ControlScope scope) {
		if (attached) {
			ControlScope oldScope = getScope();
			assert oldScope != null : this + " is attached but has no scope.";
			if (scope != oldScope) {
				// Already attached to another scope and therefore registered as listener there.
				throw new IllegalArgumentException(this + " already attached to another scope.");
			}

			return false;
		}

		setScope(scope);

		fetchID(scope.getFrameScope());
		scope.addUpdateListener(this);
		if (hasCommands()) {
			scope.getFrameScope().addCommandListener(this);
		}

		this.repaintRequired = false;

		// Note: Order matters: attachRevalidated() may depend on code in 
		// internalAttach().
		internalAttach();
		attachRevalidated();

		/* Propagate to the local scope whether the scope of this AbstractControlBase is disabled.
		 * This is necessary as if the parent control performs a redraw, then this control is
		 * detached and will was not informed about a change of the disabled state. */
		disableChildScopes(isViewDisabled());

		attached = true;
		return true;
	}

	/**
	 * installs the {@link #getID() id} of this {@link AbstractControlBase}.
	 * After a call of this method {@link #getID()} will return the document
	 * unique id of this control.
	 * 
	 * @param frameScope
	 *        must be the {@link FrameScope} of the {@link ControlScope} using
	 *        to register this control. must not be <code>null</code>
	 */
	public void fetchID(FrameScope frameScope) {
		if (this.id == null) {
			this.id = frameScope.createNewID();
			this.externalFrameScope = frameScope;
		}
	}

	/**
	 * This method sets the {@link ControlScope} for this {@link AbstractControlBase}
	 * 
	 * @param newScope
	 *        the {@link ControlScope} which shall be used to attach this
	 *        {@link AbstractControlBase} to.
	 * @return The old scope that was previously set.
	 * @throws IllegalArgumentException
	 *         if it is not possible to use the given {@link ControlScope}.
	 */
	private ControlScope setScope(ControlScope newScope) {
		ControlScope oldScope = _scope;
		_scope = newScope;

		if (newScope != null && oldScope != null) {
			FrameScope newFrameScope = newScope.getFrameScope();
			if (oldScope.getFrameScope() != newFrameScope) {
				/* If this AbstractControlBase is not attached it is just necessary to get an
				 * ControlScope with the same FrameScope to ensure that the ID of this Control is
				 * already valid. It is possible to get a ControlScope with the same FrameScope as
				 * the FrameScope of my ControlScope. E.g. a Control x was build and provides an
				 * ControlScope for some Control y. The Control y was cached but Control x was
				 * rebuild and therefore during rendering it provided a new ControlScope for y. */
				throw new IllegalArgumentException("This " + AbstractControlBase.class + " '" + this.toString()
						+ "' has a Scope with a different FrameScope!");
			}
			if (externalFrameScope != null && externalFrameScope != newFrameScope) {
				throw new IllegalArgumentException("This " + AbstractControlBase.class + " '" + this.toString()
						+ "' has gotten an ID by a FrameScope different to the new one!");
			}
		}

		return oldScope;
	}

	/**
	 * This method is the hook for subclasses to do work if the {@link AbstractControlBase} attaches
	 * to its {@link ControlScope}. This method executes methods to ensure that the
	 * {@link AbstractControlBase} will be informed about changes for its client-side view.
	 * <p>
	 * {@link #attachRevalidated()} will be executed directly after the call of this method.
	 * </p>
	 * 
	 * @see #attach(ControlScope)
	 */
	protected void internalAttach() {
		notifyListeners(ATTACHED_PROPERTY, this, Boolean.FALSE, Boolean.TRUE);
	}

	/**
	 * This method is called before writing a complete redraw is enforced and before the first
	 * rendering.
	 */
	protected void attachRevalidated() {
		notifyListeners(UP_TO_DATE_PROPERTY, this, Boolean.FALSE, Boolean.TRUE);
	}

	/**
	 * Implemented to manage the {@link #isAttached()} state.
	 * <p>
	 * The actual implementation of detaching from the model must be provided by sub-classes in
	 * {@link #internalDetach()} and {@link #detachInvalidated()}.
	 * </p>
	 * 
	 * @return Whether the detachment operation was actually performed.
	 * @see #internalDetach()
	 * @see #detachInvalidated()
	 */
	@Override
	public final boolean detach() {
		if (!attached) {
			return false;
		}

		attached = false;

		ControlScope oldScope = getScope();
		oldScope.removeUpdateListener(this);
		if (hasCommands()) {
			oldScope.getFrameScope().removeCommandListener(this);
		}

		// clear local scope also informs attached controls about clearing, so
		// they detach themselves
		clearLocalScope();

		// Send events only after all child controls have been detached to prevent them from
		// producing updates in response to the detached event.
		detachInvalidated();
		internalDetach();

		// Note: Hooks like internalDetach() may need the old scope for more deregister calls.
		setScope(null);

		return true;
	}

	private boolean hasCommands() {
		return !commandsByName.isEmpty();
	}

	/**
	 * This method is a hook for subclasses to do something when this {@link AbstractControlBase}
	 * will be detached from its {@link ControlScope}.
	 * <p>
	 * This method will be called directly after {@link #detachInvalidated()}
	 * </p>
	 */
	protected void internalDetach() {
		notifyListeners(ATTACHED_PROPERTY, this, Boolean.TRUE, Boolean.FALSE);
	}

	/**
	 * This method is called when a complete redraw is enforced.
	 * <p>
	 * The corresponding method {@link #attachRevalidated()} will be called before the rendering
	 * starts.
	 * </p>
	 * 
	 * @see #attachRevalidated()
	 */
	protected void detachInvalidated() {
		notifyListeners(UP_TO_DATE_PROPERTY, this, Boolean.TRUE, Boolean.FALSE);
	}

	protected ControlCommand getCommand(String commandName) {
		return commandsByName.get(commandName);
	}

	@Override
	public final String getID() {
		if (id == null) {
			throw new IllegalStateException("Control has no id!. Method is called before initial write occured or fetchID() was called.");
		}
		return id;
	}

	/**
	 * Writes attributes of this {@link Control}'s root tag.
	 * 
	 * <p>
	 * Note: By default, the {@link HTMLConstants#ID_ATTR} and {@link HTMLConstants#CLASS_ATTR} are
	 * written.
	 * </p>
	 * 
	 * @param context
	 *        The rendering context.
	 * @param out
	 *        The stream to write to.
	 * @throws IOException
	 *         If writing fails.
	 * 
	 * @see #writeControlClassesContent(Appendable)
	 */
	protected void writeControlAttributes(DisplayContext context, TagWriter out) throws IOException {
		writeIdAttribute(out);
		writeControlClasses(out);
	}

	/**
	 * Writes the control ID attribute.
	 */
	protected final void writeIdAttribute(TagWriter out) {
		out.writeAttribute(ID_ATTR, getID());
	}

	/**
	 * Writes the {@link HTMLConstants#CLASS_ATTR} for this {@link Control}.
	 */
	protected final void writeControlClasses(TagWriter out) throws IOException {
		out.beginCssClasses();
		writeControlClassesContent(out);
		out.endCssClasses();
	}

	/**
	 * Whether this control can be inspected on the client-side.
	 */
	private boolean canInspect() {
		return getCommand(GuiInspectorCommand.COMMAND_NAME) != null;
	}

	/**
	 * Writes the CSS classes intrinsic to this control.
	 * 
	 * @see #writeControlClassesContent(Appendable) For overriding.
	 */
	@TemplateVariable(CSS_CLASS)
	public final void writeControlClassesContentProperty(Appendable out) throws IOException {
		writeControlClassesContent(out);
	}

	/**
	 * Produces the CSS classes content of the {@link HTMLConstants#CLASS_ATTR} of this control.
	 * 
	 * <p>
	 * The contents of this method should look like:
	 * </p>
	 * 
	 * <pre>
	 * super.writeControlClassesContent(out);
	 * out.append(&quot;FirstCSSClass&quot;);
	 * out.append(&quot;SecondCSSClass&quot;);
	 * </pre>
	 * 
	 * <b>Note:</b> Necessary separators between css classes will be generated by the given
	 * {@link Appendable} itself. Empty strings or <code>null</code> strings will be ignored.
	 * 
	 * @param out
	 *        The writer to write to.
	 * @throws IOException
	 *         If writing fails.
	 */
	protected void writeControlClassesContent(Appendable out) throws IOException {
		HTMLUtil.appendCSSClass(out, getTypeCssClass());
		HTMLUtil.appendCSSClass(out, IS_CONTROL_CSS_CLASS);
		writeCanInspectClass(out);
	}

	/**
	 * Technical CSS class that marks a control as potential target for the client-side UI inspector
	 * tool.
	 */
	@TemplateVariable("canInspectClass")
	public void writeCanInspectClass(Appendable out) throws IOException {
		if (canInspect()) {
			HTMLUtil.appendCSSClass(out, CAN_INSPECT_CSS_CLASS);
		}
	}

	/**
	 * Create a {@link ClientAction} updating the client-side CSS class of this control to the value
	 * currently returned by {@link #writeControlClassesContent(Appendable)}.
	 */
	protected final ClientAction createCssUpdate() {
		AbstractCssClassUpdate update = new AbstractCssClassUpdate(getID()) {
			@Override
			protected void writeCssClassContent(DisplayContext context, Appendable out) throws IOException {
				writeControlClassesContent(out);
			}
		};
		return update;
	}

	/**
	 * The single CSS class that defines the client-side structure of this control.
	 * 
	 * <p>
	 * When overriding this method, super <b>must not</b> be called. Adding an additional class must
	 * be written by overriding {@link #writeControlClassesContent(Appendable)}.
	 * </p>
	 * 
	 * @return A single CSS class or <code>null</code>. <code>null</code> means no CSS class at all.
	 */
	protected String getTypeCssClass() {
		return null;
	}

	/**
	 * Writes a JavaScript literal containing the {@link #getID()}.
	 */
	public final void writeIdJsString(TagWriter out) throws IOException {
		out.writeJsString(getID());
	}

	@Override
	public final boolean isInvalid() {
		if (localScope != null) {
			return isRepaintRequested() || hasUpdates() || localScope.hasUpdates();
		} else {
			return isRepaintRequested() || hasUpdates();
		}
	}

	@Override
	public final boolean isAttached() {
		return attached;
	}

	/**
	 * This method returns the {@link ControlScope} this {@link AbstractControlBase} was attached to
	 * the first time or <code>null</code> if this {@link AbstractControlBase} was never attached.
	 */
	public final ControlScope getScope() {
		return _scope;
	}

	/**
	 * Whether this {@link Control} has incremental updates.
	 */
	protected abstract boolean hasUpdates();

	/**
	 * Provide a refresh of the client-side view of this {@link Control}.
	 * <p>
	 * Implementing classes must add a {@link ClientAction} to the given actions list that updates
	 * its client-side display.
	 * </p>
	 * <p>
	 * This method is called if the {@link AbstractControlBase} {@link #hasUpdates() has updates}.
	 * </p>
	 * 
	 * @param context
	 *            the {@link DisplayContext} used for revalidate this {@link AbstractControlBase}
	 * @param actions
	 *            the {@link UpdateQueue} to append the constructed {@link ClientAction}s
	 * @see #hasUpdates()
	 * @see #revalidate(DisplayContext, UpdateQueue)
	 */
	protected abstract void internalRevalidate(DisplayContext context, UpdateQueue actions);

	@Override
	public final void revalidate(DisplayContext context, UpdateQueue actions) {
		revalidateControl(context, actions);
	}

	/**
	 * Revalidates this {@link AbstractControlBase}.
	 * 
	 * <p>
	 * If {@link #isRepaintRequested()} then the complete control is repainted. Otherwise the local
	 * updates are processed and the updates of potential children.
	 * </p>
	 */
	protected void revalidateControl(DisplayContext context, UpdateQueue actions) {
		if (isRepaintRequested()) {
			/*
			 * Since repaint is requested, there is no need to revalidate the localScope. Moreover
			 * it is necessary to clear it since this allows the registered UpdateListener to clear
			 * their updates.
			 */
			clearLocalScope();

			handleRepaintRequested(actions);
			
			/*
			 * It is necessary to add the repaint action after clearing the
			 * localScope, since if adding the repaintAction implies executing
			 * the rendering process directly then the children have to attach
			 * to the local scope.
			 */
			actions.add(ControlRepaint.newControlRepaint(this));
		} else {
			if (localScope != null) {
				/*
				 * It is just necessary to get the update action of this control if there are
				 * updates.
				 */
				if (hasUpdates()) {
					context.validateScoped(this, InternalRevalidator.INSTANCE, actions, this);
				}
				if (localScope.hasUpdates()) {
					/*
					 * The local scope has to be revalidated scoped, since during rendering this
					 * control was installed as ControlScope to ensure that building the LocalScope
					 * takes place as late as possible. So each listener in the LocalScope has this
					 * Control as ControlScope.
					 */
					context.validateScoped(this, LocalScopeValidator.INSTANCE, actions, localScope);
				}
			} else {
				/*
				 * Since the localScope is null and it is not repaint requested, this controls has
				 * updates for the client.
				 */
				context.validateScoped(this, InternalRevalidator.INSTANCE, actions, this);
			}
		}
		reset();
	}

	/**
	 * This method is a hook for subclasses to clean up the state after a
	 * repaint is required. This method is called in the revalidation process if
	 * {@link #isRepaintRequested()} is <code>true</code>. It is called directly
	 * before the {@link ControlRepaint repaint action} for this control is
	 * added to the {@link UpdateQueue}.
	 * 
	 * @param actions
	 *        The {@link UpdateQueue} which will be used to add the actions for
	 *        the complete redraw.
	 */
	protected void handleRepaintRequested(UpdateQueue actions) {
		// we are fine here. no things which have to be done here.
	}

	public boolean isRepaintRequested() {
		return this.repaintRequired;
	}

	/**
	 * Marks this {@link Control} {@link #isInvalid() invalid}.
	 * <p>
	 * At {@link #revalidate(DisplayContext, UpdateQueue) revalidation time}, an invalid
	 * {@link Control} must render a {@link #internalRevalidate(DisplayContext, UpdateQueue)
	 * complete repaint} of its client-side view.
	 * </p>
	 * 
	 * @see #internalRequestRepaint() for overriding and adding functionality in subclasses.
	 */
	public final void requestRepaint() {
		if (!isAttached())
			return;
		if (isRepaintRequested())
			return;

		assert checkCommandThread();
		
		internalRequestRepaint();
		clearLocalScope();
	}
	
	/**
	 * {@link #requestRepaint() Repaints} this control on invalidation.
	 * 
	 * <p>
	 * To ensure that observing just occurs during lifetime of this control use
	 * {@link #listenForInvalidation(PropertyObservable)} to register on the appropriate target.
	 * </p>
	 * 
	 * @see com.top_logic.layout.InvalidationListener#notifyInvalid(Object)
	 */
	@Override
	public void notifyInvalid(Object invalidObject) {
		requestRepaint();
	}

	/**
	 * Ensures that this {@link LayoutControl} reacts on invalidation of the given
	 * {@link PropertyObservable}.
	 *
	 * @param observable
	 *        The {@link PropertyObservable} which is observed for invalidation, e.g. a
	 *        {@link LayoutComponent}.
	 */
	public void listenForInvalidation(PropertyObservable observable) {
		addListener(AbstractControlBase.ATTACHED_PROPERTY, new AttachedPropertyListener() {

			@Override
			public void handleAttachEvent(AbstractControlBase sender, Boolean oldValue, Boolean newValue) {
				if (newValue) {
					observable.addListener(InvalidationListener.INVALIDATION_PROPERTY, sender);
				} else {
					observable.removeListener(InvalidationListener.INVALIDATION_PROPERTY, sender);
				}

			}
		});
	}

	/**
	 * checks whether the current thread is a command thread. Logs an error if not.
	 * 
	 * @return constantly <code>true</code> to use in <code>assert</code> statements.
	 */
	protected final boolean checkCommandThread() {
		if (!DefaultDisplayContext.getDisplayContext().getLayoutContext().isInCommandPhase()) {
			Logger.error("The current thread is a rendering thread. Updates won't be evaluated.", new Exception(
				"Stack trace."), AbstractControlBase.class);
		}
		return true;
	}

	/**
	 * Implementation of {@link #requestRepaint()}. May be overridden in subclasses.
	 */
	protected void internalRequestRepaint() {
		if (!this.repaintRequired) {
			this.repaintRequired = true;

			detachInvalidated();
		}
	}

	/**
	 * Same as {@link #createCommandMap(Map, ControlCommand[])} with <code>null</code> first
	 * argument.
	 */
	public static Map<String, ControlCommand> createCommandMap(ControlCommand... commands) {
		return createCommandMap(null, commands);
	}

	/**
	 * Utility method to extend a given command map with an array of new commands.
	 * 
	 * @param base
	 *            The map of commands that is extended. The given value is not modified. Instead, an
	 *            extended map is returned.
	 * @param commands
	 *            The commands that should be added to the command map. If the value is
	 *            <code>null</code>, no commands are added.
	 * @return A map that maps command identifiers to commands that contains all mappings in
	 *         <code>base</code>.
	 */
	public static Map<String, ControlCommand> createCommandMap(Map<String, ? extends ControlCommand> base,
			ControlCommand... commands) {
		HashMap<String, ControlCommand> commandsByName = new HashMap<>();

		if (base != null) {
			commandsByName.putAll(base);
		}

		if (commands != null) {
			for (int cnt = commands.length, n = 0; n < cnt; n++) {
				commandsByName.put(commands[n].getID(), commands[n]);
			}
		}

		return Collections.unmodifiableMap(commandsByName);
	}

	protected void reset() {
		if (this.repaintRequired) {
			this.repaintRequired = false;
			attachRevalidated();
		}
	}

	@Override
	public final void write(DisplayContext context, TagWriter out) throws IOException {
		int currentDepth = out.getDepth();
		try {
			// Attach before write. This is required to bring this control into a
			// consistent state at a defined time.
			this.attach(context.getExecutionScope());

			this.reset();

			/* using this as ControlScope ensures that the LocalScope is build as late as possible. */
			context.renderScoped(this, ScopedRenderer.INSTANCE, out, this);
		} catch (Throwable throwable) {
			try {
				out.endAll(currentDepth);
				RuntimeException renderingError = ExceptionUtil.createException(
					createErrorMessage(),
					Collections.singletonList(throwable));
				produceErrorOutput(context, out, renderingError);
			} catch (Throwable inner) {
				// In the rare case of catastrophe better throw the original.
				throw throwable;
			}
		}
	}

	private String createErrorMessage() {
		Object model = getModel();
		String modelMsg;
		if (model instanceof FormMember) {
			modelMsg = "form member '" + ((FormMember) model).getQualifiedName() + "'";
		} else if (model != null) {
			modelMsg = "'" + model.getClass().getName() + "'";
		} else {
			modelMsg = "'null'";
		}
		return "Rendering '" + getClass().getName() + "' failed for model " + modelMsg + ".";
	}

	/**
	 * Logs the given {@link Throwable} and produces an error fragment, rendered as explanation,
	 * that something went wrong during rendering.
	 * 
	 * @see #writeErrorFragment(DisplayContext, TagWriter, String)
	 */
	public final void produceErrorOutput(DisplayContext context, TagWriter out, Throwable throwable)
			throws IOException {
		InfoService.logError(context, I18NConstants.ERROR_VIEW_CREATION, throwable, this);
		writeErrorFragment(context, out, throwable.getMessage());
	}

	/**
	 * In case during rendering of this {@link AbstractControlBase} an error occurred, an error
	 * marker with some explanation will be rendered as placeholder
	 */
	protected void writeErrorFragment(DisplayContext context, TagWriter out, String errorReason) throws IOException {
		RenderErrorUtil.writeErrorFragment(context, out, errorReason, this);
	}

	/**
	 * Writes this control to the the given stream.
	 */
	protected abstract void internalWrite(DisplayContext context, TagWriter out) throws IOException;

	/**
	 * Actually ignores <code>aModel</code>. Just calls {@link #detach()}.
	 * 
	 * @see NotifyListener#notifyDetachedFrom(Object)
	 */
	@Override
	public final void notifyDetachedFrom(Object aModel) {
		this.detach();
	}

	/**
	 * This implementation does nothing.
	 * 
	 * @see NotifyListener#notifyAttachedTo(Object)
	 */
	@Override
	public final void notifyAttachedTo(Object aModel) {
		// nothing to do since attach occurs during rendering
	}

	/**
	 * Executes the {@link ControlCommand} registered under <code>commandName</code>.
	 * 
	 * @throws IllegalArgumentException
	 *             if no command with the given name exists.
	 * @see CommandListener#executeCommand(DisplayContext, String, Map)
	 * @return the {@link HandlerResult} returned by the executed command
	 */
	@Override
	public final HandlerResult executeCommand(DisplayContext context, String commandName, Map<String, Object> arguments) {
		ControlCommand command = getCommand(commandName);
		if (command == null) {
			throw new IllegalArgumentException("No command '" + commandName + "' registered in control '" + this + "'.");
		}

		if (isViewDisabled() && !command.executeCommandIfViewDisabled()) {
			arguments = new HashMap<>(arguments);
			// CONTROL_ID_PARAM is the id of this control
			arguments.remove(ControlCommand.CONTROL_ID_PARAM);
			// CONTROL_COMMAND_PARAM is the given command name
			arguments.remove(DispatchAction.CONTROL_COMMAND_PARAM);

			StringBuilder msg = new StringBuilder();
			msg.append("Command was triggered whereas the views are disabled. (listenerId: '");
			msg.append(getID());
			msg.append("', listener class: '");
			msg.append(getClass().getName());
			msg.append("', commandName: '");
			msg.append(commandName);
			if (this instanceof ButtonControl) {
				msg.append("', label: '");
				msg.append(((ButtonControl) this).getLabel());
			}
			if (!arguments.isEmpty()) {
				msg.append("', arguments: '");
				msg.append(arguments);
			}
			msg.append("')");
			Logger.warn(msg.toString(), new AssertionError("Stack trace"), AbstractControlBase.class);
			requestRepaint();
			return HandlerResult.DEFAULT_RESULT;
		}

		if (PerformanceMonitor.isEnabled()) {
			ProcessingInfo processingInfo = context.getProcessingInfo();
			processingInfo.setCommandID(commandName);
			processingInfo.setCommandName(command.getI18NKey());
			processingInfo.setProcessingKind(ProcessingKind.TECHNICAL_COMMAND_EXECUTION);
			if (_scope != null) {
				// Note: During script execution, a command can be triggered on a control that is
				// not attached (if e.g. the button is located in a popup-dialog, but the script
				// does not open the popup to click the button).
				FrameScope frameScope = _scope.getFrameScope();
				if (frameScope instanceof LayoutComponentScope) {
					processingInfo.setComponentName(((LayoutComponentScope) frameScope)
						.getComponent().getName());
				}
			}
		}

		record:
		if (ScriptingRecorder.isRecordingActive()
			&& ScriptingRecorder.recordTechnicalCommands(context.getSubSessionContext())) {
			Object model = getModel();
			if (ScriptingRecorder.mustNotRecord(model)) {
				break record;
			}

			Maybe<? extends ModelName> modelName = ModelResolver.buildModelNameIfAvailable(model);
			if (modelName.hasValue()) {
				ControlAction action = TypedConfiguration.newConfigItem(ControlAction.class);
				action.setCommand(commandName);
				FrameScope frameScope = getScope().getFrameScope();
				LayoutComponent component;
				if (frameScope instanceof LayoutComponentScope) {
					component = ((LayoutComponentScope) frameScope).getComponent();
				}
				else if (frameScope instanceof LayoutComponent) {
					component = (LayoutComponent) frameScope;
				}
				else {
					component = null;
				}
				if (model instanceof FormMember) {
					LayoutComponent implicitComponent = FormComponent.componentForMember((FormMember) model);
					if (component == implicitComponent) {
						component = null;
					}
				}
				else if (component instanceof MainLayout) {
					component = null;
				}
				action.setComponent(ModelResolver.buildModelName(component));
				action.setModel(modelName.get());
				action.setType(getClass());
				if (arguments.size() > 2) {
					for (Entry<String, Object> entry : arguments.entrySet()) {
						String argumentName = entry.getKey();
						if (ControlCommand.CONTROL_ID_PARAM.equals(argumentName)) {
							continue;
						}
						if (DispatchAction.CONTROL_COMMAND_PARAM.equals(argumentName)) {
							continue;
						}

						Maybe<? extends ModelName> valueName =
							ModelResolver.buildModelNameIfAvailable(entry.getValue());
						if (!valueName.hasValue()) {
							break record;
						}

						Argument argument = TypedConfiguration.newConfigItem(Argument.class);
						argument.setName(argumentName);
						argument.setValue(valueName.get());
						action.getArguments().add(argument);
					}
				}
				ScriptingRecorder.recordAction(action);
			}
		}

		return command.execute(context, this, arguments);
	}

	private void clearLocalScope() {
		if (localScope != null) {
			localScope.clear();
		}
	}

	/* IMPLEMENTATION OF CONTROLSCOPE */

	/*
	 * AbstractControlBase implements ControlScope to install a ControlScope during rendering.
	 * Actually it is the LocalScope of this AbstractControlBase which should be installed, but that
	 * requires that the LocalScope must installed during rendering. This is not necessary for
	 * Controls which does not have ChildControls. This construct ensures that the LocalScope is
	 * installed not before it is really necessary. All calls of ControlScope methods are dispatches
	 * to the LocalScope.
	 */
	private LocalScope initLocalScope() {
		if (this.localScope == null) {
			this.localScope = newLocalScope();
		}
		return this.localScope;
	}

	/**
	 * Creates a new {@link LocalScope} with the same {@link FrameScope} in which this control is
	 * rendered.
	 */
	protected LocalScope newLocalScope() {
		return new LocalScope(getFrameScope(), isViewDisabled());
	}

	@Override
	public final void addUpdateListener(UpdateListener listener) {
		initLocalScope().addUpdateListener(listener);
	}

	@Override
	public final boolean removeUpdateListener(UpdateListener listener) {
		return localScope.removeUpdateListener(listener);
	}

	@Override
	public final FrameScope getFrameScope() {
		if (_scope == null) {
			/* Control is not rendered yet. ID may be fetched before, so the later FrameScope is
			 * already known. */
			return externalFrameScope;
		}
		return _scope.getFrameScope();
	}
	
	@Override
	public final void disableScope(boolean disable) {
		if (localScope != null) {
			localScope.disableScope(disable);
		}
	}
	
	@Override
	public final boolean isScopeDisabled() {
		if (localScope != null) {
			return localScope.isScopeDisabled();
		}
		return false;
	}

	/* END IMPLEMENTATION OF CONTROLSCOPE */
	@Override
	public final void notifyDisabled(boolean disabled) {
		disableChildScopes(disabled);
	}

	/**
	 * Disables {@link ControlScope} that are used to render content of this control.
	 * 
	 * @param disabled
	 *        Whether {@link ControlScope#isScopeDisabled()} must be <code>true</code>.
	 */
	protected void disableChildScopes(boolean disabled) {
		disableScope(disabled);
	}

	@Override
	public boolean isViewDisabled() {
		final ControlScope controlScope = getScope();
		if (controlScope != null) {
			return controlScope.isScopeDisabled();
		}
		return false;
	}
	
	/**
	 * Method to get the server side representation of the browser window this
	 * control is rendered in.
	 * 
	 * must only be called if this control is attached.
	 * 
	 * @see com.top_logic.layout.WindowScopeProvider#getWindowScope()
	 */
	@Override
	public final WindowScope getWindowScope() {
		return getScope().getFrameScope().getWindowScope();
	}

	/**
	 * The class {@link AbstractControlBase.ScopedRenderer} is a simple
	 * implementation to render an {@link AbstractControlBase} via
	 * {@link DisplayContext#renderScoped(ControlScope, Renderer, TagWriter, Object)}
	 * 
	 * @author <a href=mailto:daniel.busche@top-logic.com>Daniel Busche</a>
	 */
	private static class ScopedRenderer implements Renderer<AbstractControlBase> {

		/* package protected */ static final Renderer<AbstractControlBase> INSTANCE = new ScopedRenderer();

		/**
		 * This method expects an {@link AbstractControlBase} as value and dispatches to its
		 * {@link AbstractControlBase#internalWrite(DisplayContext, TagWriter) internal write
		 * method}.
		 * 
		 * @see Renderer#write(DisplayContext, TagWriter, Object)
		 */
		@Override
		public void write(DisplayContext context, TagWriter out, AbstractControlBase value) throws IOException {
			value.internalWrite(context, out);
		}

	}

	/**
	 * The class {@link AbstractControlBase.InternalRevalidator} is a
	 * {@link Validator} implementation to validate {@link AbstractControlBase}s
	 * by calling
	 * {@link AbstractControlBase#internalRevalidate(DisplayContext, UpdateQueue)}.
	 * 
	 * @author <a href=mailto:daniel.busche@top-logic.com>Daniel Busche</a>
	 */
	private static class InternalRevalidator implements Validator<AbstractControlBase> {

		/*package protected*/ static final Validator<AbstractControlBase> INSTANCE = new InternalRevalidator();

		/**
		 * This method expects an {@link AbstractControlBase} as object to validate and dispatches
		 * to its {@link AbstractControlBase#internalRevalidate(DisplayContext, UpdateQueue)
		 * internal revalidate method}.
		 * 
		 * @see Validator#validate(DisplayContext, UpdateQueue, Object)
		 */
		@Override
		public void validate(DisplayContext context, UpdateQueue queue, AbstractControlBase obj) {
			obj.internalRevalidate(context, queue);
		}

	}
}
