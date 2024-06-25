/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec.commandhandlers;

import static com.top_logic.mig.html.HTMLConstants.*;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import jakarta.servlet.http.HttpSession;

import com.top_logic.basic.CalledFromJSP;
import com.top_logic.basic.Configuration;
import com.top_logic.basic.IdentifierUtil;
import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.TLID;
import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.Branch;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.knowledge.wrap.WrapperFactory;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.LinkGenerator;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.basic.CommandModelFactory;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.basic.check.CheckScopeProvider;
import com.top_logic.layout.basic.check.GlobalCheck;
import com.top_logic.layout.component.ComponentUtil;
import com.top_logic.layout.component.Selectable;
import com.top_logic.mig.html.layout.CommandDispatcher;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.mig.html.layout.DialogSupport;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.LayoutUtils;
import com.top_logic.mig.html.layout.MainLayout;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.BoundChecker;
import com.top_logic.tool.boundsec.BoundHelper;
import com.top_logic.tool.boundsec.BoundObject;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.CommandHandlerFactory;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.boundsec.ObjectNotFound;
import com.top_logic.tool.boundsec.compound.CompoundSecurityLayout;
import com.top_logic.util.ReferenceManager;
import com.top_logic.util.TLContext;
import com.top_logic.util.Utils;
import com.top_logic.util.error.TopLogicSecurityException;

/**
 * CommandHandler to jump between Components.
 * This CommandHandler needs two parameters, which have to be included
 * in (JavaScript) methodCalls:
 * <ol>
 * <li>GotoHandler.COMMAND_PARAM_ID, ID of KnowledgeObjects to go to</li>
 * <li>GotoHandler.COMMAND_PARAM_TYPE, Type of KO of Wrapper </li>
 * </ol>
 * Example for caling the command in a link:
 * <pre>
 * javascript:gotoCmd( '15b011c:fd46ed626a:-7edb', 'Person');
 * </pre>
 * 
 * @see OpenViewForModelCommand
 *
 * @author    <a href="mailto:mga@top-logic.com">Michael G&auml;nsler</a>
 */
public class GotoHandler extends AbstractCommandHandler {
    
	/** The command to call this handler. */
	public static final String COMMAND = "gotoCmd";
    
    /** The constant for the ID parameter. */
	public static final String COMMAND_PARAM_ID = "id";
    
    /** The constant for the branch parameter. */
	public static final String COMMAND_PARAM_REVISION = "revision";
    
	/**
	 * Argument key for the object to go to.
	 */
	private static final String COMMAND_PARAM_GOTO_OBJECT = "goto_object";

    /** The constant for the branch parameter. */
	public static final String COMMAND_PARAM_BRANCH = "branch";
    
    /** The constant for the KO type parameter. */    
	public static final String COMMAND_PARAM_TYPE = "type";
    
    /** The constant for the component name parameter. */    
	public static final String COMMAND_PARAM_COMPONENT = "view";

	private static final String[] PARAMETER_NAMES = new String[] { COMMAND_PARAM_ID, COMMAND_PARAM_BRANCH,
		COMMAND_PARAM_REVISION, COMMAND_PARAM_TYPE, COMMAND_PARAM_COMPONENT };

    public static final String GOTO_CLASS = "gotoLink";


	/**
	 * Creates a new {@link GotoHandler}.
	 */
    public GotoHandler(InstantiationContext context, Config config) {
		super(context, config);
    }

	/**
	 * Jumps to the component that is default for displaying the object identified by the parameter
	 * {@link #COMMAND_PARAM_ID} with the type {@link #COMMAND_PARAM_TYPE}.
	 * 
	 * @see com.top_logic.tool.boundsec.CommandHandler#handleCommand(DisplayContext,
	 *      LayoutComponent, Object, Map)
	 */
	@Override
	public HandlerResult handleCommand(DisplayContext aContext, LayoutComponent aComponent, Object model, Map<String, Object> someArguments) {
		ComponentName targetComponentName = getComponentName(someArguments);
		Object targetObject = getObject(someArguments);
		if (targetComponentName == null && targetObject == null) {
			// Noting to do.
			return HandlerResult.DEFAULT_RESULT;
		}
		return executeGoto(aContext, aComponent, targetComponentName, targetObject);
    }

	/**
	 * Execute the jump to the target object and component.
	 * 
	 * @param displayContext
	 *        See {@link #handleCommand(DisplayContext, LayoutComponent, Object, Map)}.
	 * @param contextComponent
	 *        See {@link #handleCommand(DisplayContext, LayoutComponent, Object, Map)}.
	 * @param targetComponentName
	 *        The target component to jump to.
	 * @param targetObject
	 *        The target object to show.
	 * @return See {@link #handleCommand(DisplayContext, LayoutComponent, Object, Map)}.
	 */
	public HandlerResult executeGoto(DisplayContext displayContext, LayoutComponent contextComponent,
			ComponentName targetComponentName, Object targetObject) {
		try {
			LayoutComponent theTargetComponent = this.gotoLayout(contextComponent, targetObject, targetComponentName);

			makeTargetVisible(displayContext, contextComponent, theTargetComponent);

		} catch (TopLogicSecurityException ex) {
			return handleSecurityFailure(displayContext, ex);
		}

		return HandlerResult.DEFAULT_RESULT;
	}

	/**
	 * Ensures that the targetComponent is visible.
	 * 
	 * @param displayContext
	 *        Context in which goto occurs.
	 * @param contextComponent
	 *        Component in which goto was triggered.
	 * @param targetComponent
	 *        The target component of the goto.
	 */
	protected void makeTargetVisible(DisplayContext displayContext, LayoutComponent contextComponent,
			LayoutComponent targetComponent) {
		if (targetComponent == null) {
			return;
		}
		DialogSupport dialogSupport = targetComponent.getMainLayout().getDialogSupport();
		Set<LayoutComponent> keySet = dialogSupport.getOpenedDialogs().keySet();
		if (keySet.isEmpty()) {
			// No opened dialogs
			return;
		}
		Iterator<LayoutComponent> iterator = keySet.iterator();
		LayoutComponent firstDialog = iterator.next();
		if (targetComponent.openedAsDialog()) {
			LayoutComponent dialogTopLayout = targetComponent.getDialogTopLayout();
			LayoutComponent dialogInStack = firstDialog;
			while (true) {
				if (dialogInStack == dialogTopLayout) {
					if (iterator.hasNext()) {
						firstDialog = iterator.next();
						break;
					}
					// goto to top dialog
					return;
				}
				if (iterator.hasNext()) {
					dialogInStack = iterator.next();
				}

			}
		}
		/* This closes all dialogs with lower index (i.e. all inner dialogs) automatically */
		CommandDispatcher.closeDialog(firstDialog, displayContext, Collections.<String, Object> emptyMap());

	}

	/**
	 * Handles failed GOTO due to security reasons.
	 * 
	 * @param context
	 *        context in which command executed
	 * @param ex
	 *        the caught exception. must not be <code>null</code>
	 * @return a HandlerResult for the client
	 */
	public static HandlerResult handleSecurityFailure(DisplayContext context, TopLogicSecurityException ex) {
		if (Configuration.getConfiguration(GotoHandler.class).getProperties()
					.getProperty("logoutOnSecurityViolation", "FALSE").equalsIgnoreCase("TRUE")) {
			final HttpSession session = context.asRequest().getSession(false); // TODO KBU is this the right way to do it? Better forward to Logout
			if (session != null) {
				session.invalidate();
			}
		}

		HandlerResult errorResult = new HandlerResult();
		errorResult.addErrorMessage(ex.getErrorKey());
		return errorResult;
	}
    
	/**
	 * Computes the {@link BoundChecker} that is responsible for answering the
	 * {@link #checkSecurity(LayoutComponent, Object, Map)} question for a potential model.
	 * 
	 * @see BoundChecker#allowPotentialModel(com.top_logic.tool.boundsec.BoundCommandGroup, Object)
	 */
	protected BoundChecker getBoundChecker(BoundChecker aChecker, BoundObject aBoundObject,
			Map<String, Object> someArguments) {
		MainLayout mainLayout = ((LayoutComponent) aChecker).getMainLayout();
		ComponentName targetComponentName = getComponentName(someArguments);
		if (targetComponentName != null) {
			LayoutComponent targetComponent = mainLayout.getComponentByName(targetComponentName);
			if (targetComponent instanceof BoundChecker) {
				return (BoundChecker)targetComponent;
			}
		}
		BoundChecker theChecker = BoundHelper.getInstance().getDefaultChecker(mainLayout, aBoundObject);
		if (theChecker != null) {
			return theChecker;
		}
		return aChecker;
    }
    
    /**
     * @see com.top_logic.tool.boundsec.CommandHandler#getAttributeNames()
     */
    @Override
	public String[] getAttributeNames() {
        return GotoHandler.PARAMETER_NAMES;
    }

	/**
	 * Public access to {@link #getComponentName(Map)}.
	 */
	public final ComponentName findComponentName(Map<String, Object> someArguments) {
		return getComponentName(someArguments);
	}

	/**
	 * Retrieves the component name from request parameters.
	 */
	protected ComponentName getComponentName(Map<String, Object> someArguments) {
		return (ComponentName) someArguments.get(COMMAND_PARAM_COMPONENT);
	}

	/**
	 * Public access to {@link #getObject(Map)}.
	 */
	public final Object findObject(Map<String, Object> someArguments) {
		return getObject(someArguments);
	}

    @Override
    protected Object getObject(Map<String, Object> someArguments) {
		if (someArguments.containsKey(COMMAND_PARAM_GOTO_OBJECT)) {
			return someArguments.get(COMMAND_PARAM_GOTO_OBJECT);
		}
        return GotoHandler.findObjectByReferenceManager(someArguments);
    }

	@Override
   public boolean checkSecurity(LayoutComponent component, Object model, Map<String, Object> someValues) {
		Object gotoObject = getObject(someValues);
		if (gotoObject instanceof BoundObject && component instanceof BoundChecker) {
			BoundChecker securityChecker =
				getBoundChecker((BoundChecker) component, (BoundObject) gotoObject, someValues);
    		return securityChecker.allowPotentialModel(getCommandGroup(), gotoObject);
		} else {
			return super.checkSecurity(component, model, someValues);
		}
    }

	/**
	 * Display the given wrapper in the layout. If the given name describes a known component, the
	 * handler will use that one, otherwise the default component will be used for display.
	 * 
	 * @param contextComponent
	 *        The component to goto, must not be <code>null</code>
	 * @param targetObject
	 *        The model to be set to the component, must not be <code>null</code>.
	 * @param targetComponentName
	 *        The name of the component to be used for displaying, may be <code>null</code>.
	 * @return The LayoutComponent which is used as target for the goto and the goto succeeded;
	 *         <code>null</code> if the goto does not succeeded.
	 */
	public LayoutComponent gotoLayout(LayoutComponent contextComponent, Object targetObject, ComponentName targetComponentName) {
		LayoutComponent theResult = null;
		boolean isProcessed = false;
		MainLayout theMain = contextComponent.getMainLayout();
		LayoutComponent layout;
		if (targetComponentName == null) {
			layout = TLModelUtil.getBestMatch(targetObject, contextComponent.getGotoTargets());
			if (layout != null) {
				targetComponentName = layout.getName();
			}
		} else {
			layout = theMain.getComponentByName(targetComponentName);
			if (layout == null) {
				Logger.warn("Component " + targetComponentName + " as goto target for " + targetObject
					+ " not found. Using default view.", GotoHandler.class);
			}
		}
		
		// Security check
		if (targetObject instanceof BoundObject) {
			if (!(layout instanceof BoundChecker)) {
					layout = null;
			}

			if (!ComponentUtil.isValid(targetObject)) {
				throw LayoutUtils.createErrorSelectedObjectDeleted();
			}

			if (!GotoHandler.canShow(targetObject, (BoundChecker) layout)) {
				String theUser = TLContext.getContext().getCurrentUserName();
				// This can happen in case Components ignore the security when rendering GOTO-Links
				// or store their model as initial in the PersonalConfiguration.
				Logger.warn("Deep link for user '" + theUser + "' showing '" + targetObject + "' in "
					+ (layout == null ? "default component" : "'" + layout.getName() + "'") + " was denied.",
					GotoHandler.class);

				throw new TopLogicSecurityException(I18NConstants.GOTO_DENY__USER_TARGET.fill(theUser, targetObject));
			}
		}

		if (targetComponentName != null) {
			if (layout != null) {
				isProcessed = true;
				if ((layout instanceof CompoundSecurityLayout) && (targetObject instanceof BoundObject)) {
					// Compatibility with incomprehensible legacy quirks.
					((CompoundSecurityLayout) layout).setCurrentObject((BoundObject) targetObject);
				}
				{
					if (layout instanceof Selectable) {
						Selectable selectable = (Selectable) layout;
						boolean selectionChanged = selectable.setSelected(targetObject);
						if (selectionChanged) {
							/* Expected that the target object is now the selection of the
							 * selectable. */
							theResult = layout;
						} else {
							/* target object may already be the selection of selectable, therefore
							 * return value is false. */
							boolean targetObjectIsSelection = Utils.equals(selectable.getSelected(), targetObject);
							if (targetObjectIsSelection) {
								theResult = layout;
							} else {
								/* Try set the target object as model. */
								theResult = null;
							}
						}
					}

					if (theResult == null && layout.supportsModel(targetObject)) {
						layout.setModel(targetObject);
						theResult = layout;
					}

					if (theResult != null) {
						boolean makeVisible = layout.makeVisible();
						theResult = makeVisible ? layout : null;
						if (theResult == null) {
							Logger.info("gotoLayout() failed: Could not makeVisible() '" + layout + "'", this);
						}
					} else {
						Logger.info("gotoLayout() failed: target '" + layout + "' did not acceptModel(" + targetObject + ")", this);
					}

				}
			}
		}

		if (!isProcessed) {
			theResult = theMain.showDefaultFor(targetObject);

			if (theResult != null) {
				boolean updateEnvironment = this.updateEnvironment(theResult, targetObject);
				if (!updateEnvironment) {
					theResult = null;
				}
			} else {
				Logger.warn("No default view defined for " + targetObject, GotoHandler.class);
			}
		}

        return (theResult);
    }

	/**
	 * @param aLayout
	 *        The layout becoming visible, must not be <code>null</code>
	 * @param aModel
	 *        The model set to that layout, must not be <code>null</code>.
	 * @return <code>true</code>, if "goto" succeeds.
	 */
    protected boolean updateEnvironment(LayoutComponent aLayout, Object aModel) {
        return (true);
    }

	/**
	 * Short-cut for {@link #getJSCallStatement(DisplayContext, Object, ComponentName)} where no
	 * {@link DisplayContext} is available.
	 */
	public static String getJSCallStatement(Object targetObject, ComponentName componentName) {
		return getJSCallStatement(linkContext(), targetObject, componentName);
	}

	/**
	 * The context in which to render a link, if the environment does not provide a
	 * {@link DisplayContext}.
	 */
	private static DisplayContext linkContext() {
		return DefaultDisplayContext.getDisplayContext();
	}

	/**
	 * Creates an <code>onclick</code> expression displaying the given object in the given
	 * component.
	 * 
	 * @param targetComponent
	 *        The component to show the given object in. <code>null</code> means to show the object
	 *        in ti's default view.
	 * @param object
	 *        The object to display.
	 */
	public static String getJSCallStatement(DisplayContext context, LayoutComponent targetComponent, Object object) {
		if (object != null) {
			boolean canShow = canShow(object, targetComponent);
			if (canShow) {
				return getJSCallStatement(context, object, targetComponent != null ? targetComponent.getName() : null);
			}
		}
		return null;
	}

	/**
	 * Get a java script goto command call for the given object.
	 * 
	 * @param displayContext
	 *        The rendering context.
	 * @param targetObject
	 *        The object to be displayed, must not be <code>null</code>.
	 * @param componentName
	 *        The name of the component to be used for display later, may be <code>null</code>.
	 */
	public static String getJSCallStatement(DisplayContext displayContext, Object targetObject, ComponentName componentName) {
		StringBuilder out = new StringBuilder();
		try {
			appendJSCallStatement(displayContext, out, targetObject, componentName);
		} catch (IOException ex) {
			throw new UnreachableAssertion(ex);
		}
		return out.toString();
	}

	private static CommandModel getGotoCommand(DisplayContext displayContext, Object targetObject,
			ComponentName componentName) {
		CommandHandler handler = CommandHandlerFactory.getInstance().getHandler(COMMAND);
		Map<String, Object> args = createGotoArgs(targetObject, componentName);

		LayoutComponent renderedComponent = MainLayout.getComponent(displayContext);
		return CommandModelFactory.commandModel(handler, renderedComponent, args);
	}

	/**
	 * Creates a argument map for the {@link GotoHandler} for the given arguments.
	 * 
	 * @see #addGotoArgs(Map, Object, ComponentName)
	 */
	public static Map<String, Object> createGotoArgs(Object targetObject, ComponentName componentName) {
		Map<String, Object> args = new HashMap<>();
		addGotoArgs(args, targetObject, componentName);
		return args;
	}

	/**
	 * Fills the given arguments map to display the given target object on the given component
	 * later.
	 * 
	 * @param args
	 *        The map to fill.
	 * @param targetObject
	 *        The Object to display.
	 * @param componentName
	 *        The component used to display the object. May be <code>null</code>.
	 */
	public static void addGotoArgs(Map<String, Object> args, Object targetObject, ComponentName componentName) {
		if (componentName != null) {
			args.put(COMMAND_PARAM_COMPONENT, componentName);
		}
		args.put(COMMAND_PARAM_GOTO_OBJECT, targetObject);
	}

	/**
	 * Write a java script goto command call for the given object.
	 * 
	 * @param displayContext
	 *        The rendering context.
	 * @param targetObject
	 *        The object to be displayed, must not be <code>null</code>.
	 * @param componentName
	 *        The name of the component to be used for display later, may be <code>null</code>.
	 */
	public static void appendJSCallStatement(DisplayContext displayContext, Appendable out, Object targetObject, ComponentName componentName)
			throws IOException {
		out.append("return ");
		CommandModel model = getGotoCommand(displayContext, targetObject, componentName);
		LinkGenerator.renderLink(displayContext, out, model);
	}

    /** 
     * Check, if the given checker can handle the model.
     * 
     * @param    aModel    The model to be displayed, may be <code>null</code>.
     * @param    aComp     The component to display the model, may be <code>null</code>.
     * @return   Flag, if displaying is allowed.
     */
	private static boolean canShow(Object aModel, BoundChecker aComp) {
		if (aComp == null) {
			return GotoHandler.canShow(aModel);
		}
		// Ticket #14627: Check for supportsObject only if component isn't a selectable component,
		// because in this case the given model will be used as selection. See also implementation
		// of gotoLayout() method.
		boolean canShow = aComp instanceof Selectable
			|| (aComp instanceof LayoutComponent && ((LayoutComponent) aComp).supportsModel(aModel));
		if (aModel instanceof BoundObject) {
			// Check that user has right to see the given model.
			return canShow && aComp.allowPotentialModel(aModel);
		} else {
			return canShow;
        }
    }
   

	/**
	 * Writes the start of the goto link.
	 * 
	 * @param displayContext
	 *        The rendering context.
	 * @param writer
	 *        A {@link TagWriter} to generate the needed HTML.
	 * @param object
	 *        The object to go to, only Wrapper up to now
	 * 
	 * @throws IOException
	 *         If an error occurs during writing HTML.
	 */
	@CalledFromJSP
	public static void writeGotoStart(DisplayContext displayContext, TagWriter writer, Object object) throws IOException {
		writeGotoStart(displayContext, writer, object, null);
    }

	/**
	 * Writes the start of the goto link to a specified {@link LayoutComponent}.
	 * 
	 * @param displayContext
	 *        The rendering context.
	 * @param writer
	 *        An TagWriter to generate the needed HTML.
	 * @param object
	 *        The object to go to, only Wrapper up to now
	 * @param component
	 *        The component to go to (default if null)
	 * 
	 * @throws IOException
	 *         If an error occurs during writing HTML.
	 */
	public static void writeGotoStart(DisplayContext displayContext, TagWriter writer, Object object, LayoutComponent component) throws IOException {
		writer.beginBeginTag(ANCHOR);
		writer.writeAttribute(HREF_ATTR, "#");
		if (canShow(object, component)) {
			{
				writer.writeAttribute(CLASS_ATTR, GotoHandler.GOTO_CLASS);

				writer.beginAttribute(ONCLICK_ATTR);
				GotoHandler.appendJSCallStatement(displayContext, writer, object, name(component));
				writer.endAttribute();
			}
		}
        writer.endBeginTag();
    }

	private static ComponentName name(LayoutComponent optionalComponent) {
		if (optionalComponent == null) {
			return null;
		}
		return optionalComponent.getName();
	}

	/**
	 * Check, whether the given component can display the given object.
	 * 
	 * @param object
	 *        The model to be displayed, may be <code>null</code>.
	 * @param component
	 *        The component to display the model, may be <code>null</code>.
	 * @return Flag, if displaying is allowed.
	 */
	public static boolean canShow(Object object, LayoutComponent component) {
		if (component instanceof BoundChecker) {
			return canShow(object, (BoundChecker) component);
		} else {
			return canShow(object);
		}
	}

	/**
	 * Whether the given object can be displayed in it's default view.
	 */
	public static boolean canShow(Object anObject) {
        if (anObject instanceof BoundObject) {
            BoundHelper theHelper = BoundHelper.getInstance();
            return theHelper.allowView((BoundObject) anObject, theHelper.getRootChecker());
        }
        else {
            return false;
        }
	}
    
    /**
     * Writes the end of the goto link.
     * @param aWriter           an TagWriter to generate the needed HTML.
     * @param anObject          not evaluated, may be null or use Object from writeStart
     * @throws IOException      if an error occurs during writing HTML.
     */
	@CalledFromJSP
	public static void writeGotoEnd(TagWriter aWriter, Object anObject) throws IOException {
		aWriter.endTag(ANCHOR);
    }
    
	/**
	 * Resolves a target object assuming that the following arguments are in the given argument map.
	 * 
	 * <dl>
	 * <dt>{@link #COMMAND_PARAM_TYPE}</dt>
	 * <dd>The plain {@link KnowledgeObject#tTable()} name</dd>
	 * <dt>{@link #COMMAND_PARAM_ID}</dt>
	 * <dd>The {@link ReferenceManager} id for the {@link KnowledgeObject#getObjectName()}</dd>
	 * <dt>{@link #COMMAND_PARAM_BRANCH}</dt>
	 * <dd>The plain {@link KnowledgeObject#getBranchContext()}</dd>
	 * <dt>{@link #COMMAND_PARAM_REVISION}</dt>
	 * <dd>The plain {@link KnowledgeObject#getHistoryContext()}</dd>
	 * </dl>
	 * 
	 * @param arguments
	 *        The arguments described above.
	 * @return The identified object, or <code>null</code>, if the object could not be found.
	 * 
	 * @see #findObjectByInternalIdentity(String, TLID, String, String)
	 */
	protected static Wrapper findObjectByReferenceManager(Map<String, Object> arguments) throws ObjectNotFound {
		TLID theGotoId =
			IdentifierUtil.fromExternalForm(LayoutComponent.getParameter(arguments, GotoHandler.COMMAND_PARAM_ID));
		String theGotoRevision = LayoutComponent.getParameter(arguments, GotoHandler.COMMAND_PARAM_REVISION);
		String theGotoBranch = LayoutComponent.getParameter(arguments, GotoHandler.COMMAND_PARAM_BRANCH);
		String theGotoType = LayoutComponent.getParameter(arguments, GotoHandler.COMMAND_PARAM_TYPE);
		
		return GotoHandler.findObjectByInternalIdentity(theGotoType, theGotoId, theGotoBranch, theGotoRevision);
	}

	/**
	 * Resolve the target object by its internal textual identification.
	 * 
	 * @param objectType
	 *        The name of the target object type.
	 * @param objectName
	 *        The object name of the target object.
	 * @param branchId
	 *        The branch of the requested object.
	 * @param revisionId
	 *        The revision number of the requested object.
	 * @return The identified object.
	 * 
	 * @throws ObjectNotFound
	 *         iff the both the <code>objectType</code> and the <code>objectName</code> are given,
	 *         but no wrapper was found.
	 */
	public static Wrapper findObjectByInternalIdentity(String objectType, TLID objectName, String branchId,
			String revisionId) throws ObjectNotFound {
		if (!StringServices.isEmpty(objectName) && !StringServices.isEmpty(objectType)) {
			Revision revision =
				(StringServices.isEmpty(revisionId)) ?
					Revision.CURRENT :
					HistoryUtils.getRevision(Long.parseLong(revisionId));
			Branch branch =
				(branchId != null) ? HistoryUtils.getBranch(Long.parseLong(branchId)) : HistoryUtils.getTrunk();

			Wrapper result = WrapperFactory.getWrapper(branch, revision, objectName, objectType);
			if (result == null) {
				throw new ObjectNotFound(I18NConstants.GOTO_TARGET_OBJECT_NOT_FOUND);
			}
			return result;
		} else {
			return null;
		}
	}

	@Override
	@Deprecated
	public CheckScopeProvider getCheckScopeProvider() {
    	return GlobalCheck.INSTANCE;
    }

}