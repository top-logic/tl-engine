/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec.gui.profile;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.col.Filter;
import com.top_logic.basic.col.Mapping;
import com.top_logic.basic.col.TreeView;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.col.filter.FilterFactory;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.Renderer;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.ResourceProvider;
import com.top_logic.layout.component.TabComponent;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.ValueListener;
import com.top_logic.layout.form.component.AbstractApplyCommandHandler;
import com.top_logic.layout.form.component.EditComponent;
import com.top_logic.layout.form.component.FormComponent;
import com.top_logic.layout.form.model.BooleanField;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.layout.form.model.FormTree;
import com.top_logic.layout.form.model.NodeGroupInitializer;
import com.top_logic.layout.form.model.TreeTableField;
import com.top_logic.layout.form.template.DefaultFormFieldControlProvider;
import com.top_logic.layout.form.template.InfoAddingControlProvider;
import com.top_logic.layout.table.renderer.DefaultRowClassProvider;
import com.top_logic.layout.table.renderer.DefaultTableRenderer;
import com.top_logic.layout.tree.NodeContext;
import com.top_logic.layout.tree.TreeControl;
import com.top_logic.layout.tree.model.ConstantTreeViewTreeModelAdapter;
import com.top_logic.layout.tree.model.DefaultStructureTreeUIModel;
import com.top_logic.layout.tree.model.TLTreeModel;
import com.top_logic.layout.tree.model.TreeUIModel;
import com.top_logic.layout.tree.model.TreeUIModelUtil;
import com.top_logic.layout.tree.renderer.ColumnDeclaration;
import com.top_logic.layout.tree.renderer.DefaultColumnDeclaration;
import com.top_logic.layout.tree.renderer.DefaultTableDeclaration;
import com.top_logic.layout.tree.renderer.DefaultTreeImageProvider;
import com.top_logic.layout.tree.renderer.TableDeclaration;
import com.top_logic.layout.tree.renderer.TreeTableRenderer;
import com.top_logic.mig.html.DefaultResourceProvider;
import com.top_logic.mig.html.layout.CommandRegistry;
import com.top_logic.mig.html.layout.DefaultDescendingLayoutVisitor;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.LayoutComponentVisitor;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.BoundChecker;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.BoundComponent;
import com.top_logic.tool.boundsec.CommandGroupReference;
import com.top_logic.tool.boundsec.DefaultHandlerResult;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.boundsec.simple.CommandGroupResourceProvider;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;
import com.top_logic.tool.boundsec.wrap.BoundedRole;
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.tool.execution.InEditModeExecutable;
import com.top_logic.util.Resources;
import com.top_logic.util.Utils;

/**
 * {@link EditComponent} for editing role profiles.
 *
 * @author <a href="mailto:tsa@top-logic.com">tsa</a>
 */
public class EditRolesProfileComponent extends EditComponent {

	/**
	 * Configuration of the {@link EditRolesProfileComponent}
	 * 
	 * @since 5.8.0
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
    public interface Config extends EditComponent.Config {

		/** @see com.top_logic.basic.reflect.DefaultMethodInvoker */
		Lookup LOOKUP = MethodHandles.lookup();

		/**
		 * Name of the {@link #getColumnProvider} property.
		 */
		String COLUMN_PROVIDER_ATTR = "columnProvider";

		/**
		 * Name of the {@link #getNodeFilter()} property.
		 */
		String NODE_FILTER_ATTR = "nodeFilter";

		/**
		 * Returns the provider for the columns displayed in the table.
		 */
		@Name(COLUMN_PROVIDER_ATTR)
		@InstanceFormat
		ColumnProvider getColumnProvider();

		/**
		 * Filter for the layout components that are displayed in the tree. Only components matching
		 * this filter are displayed.
		 */
		@Name(NODE_FILTER_ATTR)
		PolymorphicConfiguration<Filter<Object>> getNodeFilter();

		@Override
		@StringDefault(ApplyRolesProfile.COMMAND_ID)
		String getApplyCommand();

		@Override
		default void modifyIntrinsicCommands(CommandRegistry registry) {
			registry.registerButton(ExpandAllCommandHandler.COMMAND_ID);
			registry.registerButton(CollapseAllCommandHandler.COMMAND_ID);
			registry.registerButton(SetAllCommandHandler.COMMAND_ID);
			registry.registerButton(UnSetAllCommandHandler.COMMAND_ID);
			EditComponent.Config.super.modifyIntrinsicCommands(registry);
		}

	}

	public static final Property<ColumnProvider> COLUMN_PROVIDER_CONST =
		TypedAnnotatable.property(ColumnProvider.class, "columnProvider");
    public static final String        TREE_FIELD             = "tree";
    public static final String        SEPARATOR_COLUMN       = "__";

	private static final Filter<Object> TAB_FRAME_FILTER = FilterFactory.falseFilter();
    
    protected TLTreeModel     treeModel;
    protected Filter<Object>  nodeFilter;
    
	protected Collection<?> oldExpansionModel;
    protected List           allCommandGroups;
    protected Map            nodeCommandGroups;
    protected ColumnProvider columnProvider;


    /**
	 * Create a new {@link EditRolesProfileComponent} from the given configuration.
	 */
	public EditRolesProfileComponent(InstantiationContext context, Config config) throws ConfigurationException {
		super(context, config);
		this.columnProvider = (config.getColumnProvider() == null) ? new ColumnProvider() : config.getColumnProvider();
		this.nodeFilter = createNodeFilter(context, config);
    }

	private Filter<Object> createNodeFilter(InstantiationContext context, Config config) {
		PolymorphicConfiguration<Filter<Object>> nodeFilterConfig = config.getNodeFilter();
		if (nodeFilterConfig == null) {
			return TAB_FRAME_FILTER;
		}
		Filter<Object> configuredFilter = context.getInstance(nodeFilterConfig);
		if (configuredFilter == null) {
			// may occur when instantiation failed.
			context.error("Unable to create node filter for component '" + getName() + "'");
		} else {
			if (configuredFilter instanceof ComponentAware) {
				((ComponentAware) configuredFilter).setComponent(this);
			}
		}
		return configuredFilter;
	}

	@Override
	public synchronized void removeFormContext() {
		if (this.hasFormContext()) {
            // in this case we have to preserve the tree expansion state.
            // in order to do so, we have to remeber the old state, create a new form context, and set the expansion state
            FormContext theOldContext = this.getFormContext();
            if (theOldContext != null) {
                FormTree theTree = (FormTree) theOldContext.getFirstMemberRecursively(TREE_FIELD);
                if (theTree != null) {
                    this.oldExpansionModel = TreeUIModelUtil.getExpansionModel(theTree.getTreeModel());
                }
            }
            this.treeModel = null;
        }
		super.removeFormContext();
	}

	@Override
	public boolean isModelValid() {
		return super.isModelValid() && treeModel != null;
	}

	@Override
	public boolean validateModel(DisplayContext context) {
		boolean nonLocalChanges = super.validateModel(context);
		initTreeModel();
		return nonLocalChanges;
	}

    /**
     * @see com.top_logic.layout.form.component.EditComponent#createFormContext()
     */
    @Override
	public FormContext createFormContext() {
        FormContext theFC = new FormContext("treeForm", getResPrefix());

        // Setup model
        TreeUIModel uiModel   = new DefaultStructureTreeUIModel(this.getTreeModel(), false);

		theFC.set(COLUMN_PROVIDER_CONST, this.columnProvider);

        TableDeclaration theTreeTable = this.columnProvider.getTableDeclaration(this.getResPrefix(), this.allCommandGroups);

        final FormTree          theFormTree          = new FormTree(TREE_FIELD,getResPrefix(), uiModel, new CommandGroupGroupInitializer(this.allCommandGroups, this.nodeCommandGroups, this.columnProvider, (BoundedRole) this.getModel()));
        
        final TreeTableRenderer theTreeTableRenderer;
		{
            theTreeTableRenderer = new TreeTableRenderer(DefaultTreeImageProvider.INSTANCE, theTreeTable) {
				@Override
				protected void writeNodeClassesContent(TagWriter out, NodeContext nodeContext) throws IOException {
					super.writeNodeClassesContent(out, nodeContext);
					Object node = nodeContext.currentNode();
					TreeControl tree = nodeContext.getTree();
					boolean isLeaf = tree.getModel().isLeaf(node);
					if (isLeaf) {
						out.append(DefaultRowClassProvider.TR_EVEN_CSS_CLASS);
					} else {
						out.append(DefaultTableRenderer.TABLE_ROW_CSS_CLASS);
					}
				}
			};
        }

        theFormTree.setTreeRenderer(theTreeTableRenderer);
        theFC.addMember(theFormTree);

        this.createInnerNodeGroups(theFormTree.getTreeModel(), theFormTree.getTreeModel().getRoot());
        TreeUIModelUtil.setExpandedAll(theFormTree.getTreeModel(), theFormTree.getTreeModel().getRoot(), true);
        TreeUIModelUtil.setExpandedAll(theFormTree.getTreeModel(), theFormTree.getTreeModel().getRoot(), false);
        if (this.oldExpansionModel != null) {
        	TreeUIModelUtil.setExpansionModel(this.oldExpansionModel, theFormTree.getTreeModel());
        }

        return theFC;
    }

    private void createInnerNodeGroups(TreeUIModel aTree, Object aNode) {
        if ( ! aTree.isLeaf(aNode) ) {
            for (Iterator theIt = aTree.getChildIterator(aNode); theIt.hasNext();) {
                Object theChild = theIt.next();
                aTree.getUserObject(theChild);
                this.createInnerNodeGroups(aTree, theChild);
            }
        }
    }

    /**
     * Lazy access to the {@link #treeModel}
     */
    protected TLTreeModel getTreeModel() {
        if (this.treeModel == null) {
			initTreeModel();
        }
        return this.treeModel;
    }

	/**
	 * Initialises the {@link #treeModel}.
	 */
	private void initTreeModel() {
		treeModel = createTreeModel();
	}

    protected TLTreeModel createTreeModel() {
        final TreeView                         theTreeView  = new BoundCheckerTreeView(this.nodeFilter);
        final ConstantTreeViewTreeModelAdapter theTreeModel = new ConstantTreeViewTreeModelAdapter(this.getMainLayout(), theTreeView);

        this.allCommandGroups = new ArrayList();
        this.nodeCommandGroups = new HashMap();

        this.initCaches(theTreeView, this.getMainLayout());

        this.allCommandGroups = CollectionUtil.removeDuplicates(this.allCommandGroups);

        return theTreeModel;

    }

    protected void initCaches(TreeView aView, LayoutComponent aNode) {
        Collection theCommandGroups = this.columnProvider.getCommandGroups(aNode);
        this.nodeCommandGroups.put   (aNode, theCommandGroups);
        this.allCommandGroups    .addAll(theCommandGroups);
        for (Iterator theIt = aView.getChildIterator(aNode); theIt.hasNext(); ) {
            initCaches(aView, (LayoutComponent) theIt.next());
        }
    }

    /**
     * @see com.top_logic.layout.form.component.EditComponent#supportsInternalModel(java.lang.Object)
     */
    @Override
	protected boolean supportsInternalModel(Object anObject) {
        return anObject instanceof BoundedRole;
    }

    /**
     * Setup a NodeGroup with a Checkbox, StringField and SelectField.
     */
    public final class CommandGroupGroupInitializer implements NodeGroupInitializer {

        private List           commandGroups;
        private ColumnProvider columnProvider;
        private Map            nodeCommandGroups;
        private BoundedRole    role;

        public CommandGroupGroupInitializer(List someCommandGroups, Map someNodeCommandGroups, ColumnProvider aColumnProvider, BoundedRole aRole) {
            this.nodeCommandGroups = someNodeCommandGroups;
            this.commandGroups     = someCommandGroups;
            this.columnProvider    = aColumnProvider;
            this.role              = aRole;
        }

        @Override
		public void createNodeGroup(FormTree formTree, FormGroup nodeGroup, Object node)
        {
            Resources theRes = Resources.getInstance();
            for (Iterator theIt = this.commandGroups.iterator(); theIt.hasNext(); ) {
                BoundCommandGroup theCommandGroup = (BoundCommandGroup) theIt.next();
                boolean addField = this.columnProvider.addField(formTree, node, theCommandGroup, nodeCommandGroups);
                if (addField) {
                    String theId = theCommandGroup.getID();
                    Collection theRFCG = ((BoundChecker) node).getRolesForCommandGroup(theCommandGroup);
                    BooleanField theBooleanField = FormFactory.newBooleanField(theId);
					ResKey theLabel = ResKey.text(CommandGroupResourceProvider.INSTANCE.getLabel(theCommandGroup));
					ResKey theToolTip = ResKey.text(CommandGroupResourceProvider.INSTANCE.getTooltip(theCommandGroup));
                    theBooleanField.setLabel(theLabel);
                    theBooleanField.setTooltip(theToolTip);
					theBooleanField.initializeField(
						theRFCG == null ? Boolean.FALSE : Boolean.valueOf(theRFCG.contains(this.role)));
                    nodeGroup.addMember(theBooleanField);
                }
            }
            this.columnProvider.adaptColumFields(formTree, nodeGroup, node);
        }
    }


    public static class TabFrameVisitor extends DefaultDescendingLayoutVisitor implements CommandGroupCollector {
		private Set<BoundCommandGroup> commandGroups = new HashSet<>();
        private Object base;
        public TabFrameVisitor(Object aBase) {
            this.base = aBase;
        }
        @Override
		public boolean visitLayoutComponent(LayoutComponent aComponent) {
            if (aComponent.equals(this.base)) {
                return true;
            }
            if (aComponent instanceof TabComponent) {
                return false;
            }
            if (aComponent instanceof BoundComponent) {
                this.commandGroups.addAll(((BoundComponent) aComponent).getCommandGroups());
            }
            return true;
        }
        @Override
		public Set getCommandGroups() {
            return this.commandGroups;
        }
    }

    public static interface CommandGroupCollector extends LayoutComponentVisitor {
        public Set getCommandGroups();
    }

    public static class ApplyRolesProfile extends AbstractApplyCommandHandler {

        public static final String COMMAND_ID = "applyCheckerRoles";

        /**
		 * Creates an {@link ApplyRolesProfile}.
		 */
        public ApplyRolesProfile(InstantiationContext context, Config config) {
			super(context, config);
        }

        @Override
		protected boolean storeChanges(LayoutComponent component, FormContext formContext, Object model) {
			ColumnProvider theColumnProvider = formContext.get(COLUMN_PROVIDER_CONST);
            return theColumnProvider.applyRolesProfile((BoundedRole) model, (FormTree) formContext.getMember(TREE_FIELD) );
        }

    }

    public static class ColumnProvider {

		/** Singleton {@link ColumnProvider} instance. */
		public static final ColumnProvider INSTANCE =
			new ColumnProvider();

		/**
		 * Creates a new {@link ColumnProvider}.
		 * 
		 */
		protected ColumnProvider() {
			// singleton instance
		}

        private String[] declaredColumns = new String[] {
            "Read", "Write", "Delete", "Create", "Export"
        };

        protected String[] getDeclaredColumns() {
            return this.declaredColumns;
        }

        public boolean applyRolesProfile(BoundedRole aModel, FormTree aField) {
            return true;
        }

		public Renderer<Object> getHeaderRenderer(String aColumnName) {
			Renderer<Object> theComandGroupHeaderRenderer = new Renderer<>() {
				@Override
				public void write(DisplayContext aContext, TagWriter aOut, Object aValue)
                        throws IOException {
					String fullName = String.valueOf(aValue);
					String firstLetter = fullName.substring(0, 1);
					Resources resources = Resources.getInstance();
					String header = resources.getString(I18NConstants.ROLE_HEADERS.key(fullName), firstLetter);
					aOut.writeText(header);
                }
            };
            return theComandGroupHeaderRenderer;
        }

        public String[] getColumnNames(Collection someCommandGroups) {
            Collection theRequestedColumns = new HashSet();
            CollectionUtil.mapIgnoreNull(someCommandGroups.iterator(), theRequestedColumns, new Mapping() {
                @Override
				public Object map(Object aInput) {
                    final BoundCommandGroup theCommandGroup = (BoundCommandGroup) aInput;
					if (!theCommandGroup.isSystemGroup()) {
                        return (theCommandGroup).getID();
                    } else {
                        return null;
                    }
                }
            });
            HashSet theHandled = new HashSet();
            List theResult = new ArrayList();
//            int theSeparatorCount = 0;
//            theResult.add(SEPARATOR_COLUMN+theSeparatorCount++);
            String[] theDeclaredColumns = getDeclaredColumns();
            for (int i = 0; i < theDeclaredColumns.length; i++) {
                String theColumn = theDeclaredColumns[i];
//                if (SEPARATOR_COLUMN.equals(theColumn)) {
//                    theResult.add(theColumn+theSeparatorCount++);
//                } else
                if (theRequestedColumns.contains(theColumn)) {
                    theHandled.add(theColumn);
                    theResult.add(theColumn);
                }
            }
//            theResult.add(SEPARATOR_COLUMN+theSeparatorCount++);
            theRequestedColumns.removeAll(theHandled);
            if ( ! theRequestedColumns.isEmpty()) {
                theResult.addAll(theRequestedColumns);
//                theResult.add(SEPARATOR_COLUMN+theSeparatorCount++);
            }
            return (String[]) theResult.toArray(new String[] {});
        }
        /**
         * This method decides if there needs to be a checkbox for the given command group
         *
         * @param aCommandGroup          the column (command group)
         * @param someNodeCommandGroups  the command groups available for the given node
         * @return true iff the command group is one of the nodes command groups of "READ" 
         */
        public boolean addField(FormTree aFormTree, Object aNode,
                BoundCommandGroup aCommandGroup, Map someNodeCommandGroups) {
            if ("Read".equals(aCommandGroup.getID())) {
                return true;
            }
            Collection theCommandGroups = (Collection) someNodeCommandGroups.get(aNode);
            return theCommandGroups != null && theCommandGroups.contains(aCommandGroup);
        }
        public void adaptColumFields(FormTree formTree, FormGroup aRowContainer, Object node) {
            final FormField     theReadField = aRowContainer.getField("Read");
            final FormContainer theRowContainer = aRowContainer;
            theReadField.addValueListener(new ValueListener() {
                @Override
				public void valueChanged(FormField aField, Object aOldValue, Object aNewValue) {
                    if ( ! Utils.isTrue((Boolean) aNewValue)) {
                        for (Iterator theIt = theRowContainer.getMembers(); theIt.hasNext();) {
                            FormField theField = (FormField) theIt.next();
                            if ( ! theField.equals(aField)) {
                                theField.setValue(Boolean.FALSE);
                            }
                        }
                    }
                }
            });
            ValueListener theReadSetter = new ValueListener() {
                @Override
				public void valueChanged(FormField aField, Object aOldValue, Object aNewValue) {
                    if (Utils.isTrue((Boolean) aNewValue)) {
                        theReadField.setValue(Boolean.TRUE);
                    }
                }
            };
            for (Iterator theIt = theRowContainer.getMembers(); theIt.hasNext();) {
                FormField theField = (FormField) theIt.next();
                if ( ! theField.equals(theReadField)) {
                    theField.addValueListener(theReadSetter);
                }
            }
            Object theParent = formTree.getTreeModel().getParent(node);
            if (theParent != null) {
                FormContainer theParentContainer = formTree.findNodeGroup(theParent);
                if (theParentContainer != null) { // this might happen if the root node is not visible
                    final FormField     theParentReadField = theParentContainer.getField("Read");
                    theReadField.addValueListener(new ValueListener() {
                        @Override
						public void valueChanged(FormField aField, Object aOldValue, Object aNewValue) {
                            if (Utils.isTrue((Boolean) aNewValue)) {
                                theParentReadField.setValue(Boolean.TRUE);
                            }
                        }
                    });
                    theParentReadField.addValueListener(new ValueListener() {
                        @Override
						public void valueChanged(FormField aField, Object aOldValue, Object aNewValue) {
                            if (Utils.isFalse((Boolean) aNewValue)) {
                                theReadField.setValue(Boolean.FALSE);
                            }
                        }
                    });
                }
            }
        }

        public ResourceProvider getNodeResourceProvider() {
            return new DefaultResourceProvider() {
                @Override
				public String getLabel(Object aObject) {
					return Resources.getInstance().getString(((LayoutComponent) aObject).getTitleKey());
                }
            };
        }

        public Collection getCommandGroups(LayoutComponent aNode) {
            CommandGroupCollector theCollector = new TabFrameVisitor(aNode);
            aNode.acceptVisitorRecursively(theCollector);
            return theCollector.getCommandGroups();
        }

		public TableDeclaration getTableDeclaration(ResPrefix aResPrefix, Collection someCmdGroups) {
            DefaultTableDeclaration theTD =  new DefaultTableDeclaration(
                    aResPrefix, new String[] {});
            String[] theColNames = this.getColumnNames(someCmdGroups);
            for (int i = 0; i < theColNames.length; i++) {
                String theColumnName = theColNames[i];
                DefaultColumnDeclaration theCD = new DefaultColumnDeclaration(new InfoAddingControlProvider(DefaultFormFieldControlProvider.INSTANCE));
                theCD.setHeaderType(ColumnDeclaration.RENDERED_HEADER);
                theCD.setHeaderRenderer(this.getHeaderRenderer(theColumnName));
                theTD.addColumnDeclaration(theColumnName, theCD);
            }
            theTD.setHasHeader(true);
            theTD.setResourceProvider(this.getNodeResourceProvider());
            return theTD;
        }
    }

	public static class ExpandAllCommandHandler extends AbstractCommandHandler {

        public static final String COMMAND_ID = "expandRoleProfileTree";

        public ExpandAllCommandHandler(InstantiationContext context, Config config) {
			super(context, config);
        }

        @Override
		public HandlerResult handleCommand(DisplayContext aContext,
				LayoutComponent aComponent, Object model, Map<String, Object> aSomeArguments) {
			FormMember treeField = ((FormComponent) aComponent).getFormContext().getFirstMemberRecursively(TREE_FIELD);
			TreeUIModel theModel;
			if (treeField instanceof TreeTableField) {
				theModel = ((TreeTableField) treeField).getTree();
			} else {
				FormTree theTree = (FormTree) treeField;
				theModel = theTree.getTreeModel();
			}
			TreeUIModelUtil.setExpandedAll(theModel, theModel.getRoot(), true);
			return DefaultHandlerResult.DEFAULT_RESULT;
        }
    }

    public static class CollapseAllCommandHandler extends AbstractCommandHandler {

        public static final String COMMAND_ID = "collapseRoleProfileTree";


        public CollapseAllCommandHandler(InstantiationContext context, Config config) {
			super(context, config);
        }

        /**
         * @see com.top_logic.tool.boundsec.CommandHandler#handleCommand(com.top_logic.layout.DisplayContext, com.top_logic.mig.html.layout.LayoutComponent, Object, Map)
         */
        @Override
		public HandlerResult handleCommand(DisplayContext aContext,
                LayoutComponent aComponent, Object model, Map<String, Object> aSomeArguments) {
			FormMember treeField = ((FormComponent) aComponent).getFormContext().getFirstMemberRecursively(TREE_FIELD);
			TreeUIModel theModel;
			if (treeField instanceof TreeTableField) {
				theModel = ((TreeTableField) treeField).getTree();
			} else {
				FormTree theTree = (FormTree) treeField;
				theModel = theTree.getTreeModel();
			}
            TreeUIModelUtil.setExpandedAll(theModel, theModel.getRoot(), false);
            return DefaultHandlerResult.DEFAULT_RESULT;
        }
    }

    public static class SetAllCommandHandler extends AbstractCommandHandler {

        public static final String COMMAND_ID = "setAllRoleProfileTree";

		/**
		 * Configuration for {@link SetAllCommandHandler}.
		 * 
		 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
		 */
		public interface Config extends AbstractCommandHandler.Config {

			@FormattedDefault(SimpleBoundCommandGroup.WRITE_NAME)
			@Override
			CommandGroupReference getGroup();

		}

        public SetAllCommandHandler(InstantiationContext context, Config config) {
			super(context, config);
        }

        @Override
		@Deprecated
		public ExecutabilityRule createExecutabilityRule() {
            return InEditModeExecutable.INSTANCE;
        }

        /**
         * @see com.top_logic.tool.boundsec.CommandHandler#handleCommand(com.top_logic.layout.DisplayContext, com.top_logic.mig.html.layout.LayoutComponent, Object, Map)
         */
        @Override
		public HandlerResult handleCommand(DisplayContext aContext,
                LayoutComponent aComponent, Object model, Map<String, Object> aSomeArguments) {
            FormContext theFC = ((FormComponent) aComponent).getFormContext();
            for (Iterator theIt = theFC.getDescendantFields(); theIt.hasNext(); ) {
                Object theMember = theIt.next();
                if (theMember instanceof BooleanField) {
                    ((BooleanField) theMember).setAsBoolean(true);
                }
            }
            return DefaultHandlerResult.DEFAULT_RESULT;
        }
    }

    public static class UnSetAllCommandHandler extends AbstractCommandHandler {

        public static final String COMMAND_ID = "unSetAllRoleProfileTree";

		/**
		 * Configuration for {@link UnSetAllCommandHandler}.
		 * 
		 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
		 */
		public interface Config extends AbstractCommandHandler.Config {

			@FormattedDefault(SimpleBoundCommandGroup.WRITE_NAME)
			@Override
			CommandGroupReference getGroup();

		}

        public UnSetAllCommandHandler(InstantiationContext context, Config config) {
			super(context, config);
        }

        /**
         * @see com.top_logic.tool.boundsec.AbstractCommandHandler#createExecutabilityRule()
         */
        @Override
		@Deprecated
		public ExecutabilityRule createExecutabilityRule() {
            return InEditModeExecutable.INSTANCE;
        }

        /**
         * @see com.top_logic.tool.boundsec.CommandHandler#handleCommand(com.top_logic.layout.DisplayContext, com.top_logic.mig.html.layout.LayoutComponent, Object, Map)
         */
        @Override
		public HandlerResult handleCommand(DisplayContext aContext,
                LayoutComponent aComponent, Object model, Map<String, Object> aSomeArguments) {
            FormContext theFC = ((FormComponent) aComponent).getFormContext();
            for (Iterator theIt = theFC.getDescendantFields(); theIt.hasNext(); ) {
                Object theMember = theIt.next();
                if (theMember instanceof BooleanField) {
                    ((BooleanField) theMember).setAsBoolean(false);
                }
            }
            return DefaultHandlerResult.DEFAULT_RESULT;
        }
    }
}

