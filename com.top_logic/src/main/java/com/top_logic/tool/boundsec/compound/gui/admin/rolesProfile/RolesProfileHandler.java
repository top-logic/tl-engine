/*
 * SPDX-FileCopyrightText: 2004 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec.compound.gui.admin.rolesProfile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.swing.tree.TreeModel;

import com.top_logic.basic.FileManager;
import com.top_logic.basic.Settings;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.col.TreeView;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.io.BinaryContent;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.event.infoservice.InfoService;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.mig.html.layout.DefaultDescendingLayoutVisitor;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.LayoutConfigTree;
import com.top_logic.mig.html.layout.LayoutConfigTreeNode;
import com.top_logic.mig.html.layout.LayoutConstants;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.BoundHelper;
import com.top_logic.tool.boundsec.BoundObject;
import com.top_logic.tool.boundsec.BoundRole;
import com.top_logic.tool.boundsec.compound.CompoundSecurityLayout;
import com.top_logic.tool.boundsec.compound.gui.admin.rolesProfile.SecurityConfig.RoleProfileConfig;
import com.top_logic.tool.boundsec.compound.gui.admin.rolesProfile.SecurityConfig.ViewConfig;
import com.top_logic.tool.boundsec.config.AccessConfiguration;
import com.top_logic.tool.boundsec.config.AccessImporter;
import com.top_logic.tool.boundsec.gui.profile.CommandGroupCollector;
import com.top_logic.tool.boundsec.simple.CommandGroupRegistry;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;
import com.top_logic.tool.boundsec.wrap.PersBoundComp;
import com.top_logic.tool.boundsec.wrap.SecurityComponentCache;

/**
 * This class collects methods to handle roles profile related functionality.
 *
 * @author    <a href="mailto:tsa@top-logic.com">Theo Sattler</a>
 */
public class RolesProfileHandler {

    // XML tag and attribute constants

    /** the root  tag */
    public static final String ROOT_TAG         = "root";
    /** tag holding all profiles */
    public static final String PROFILES_TAG     = "profiles";
    /** tag holding the profile for a single role */
    public static final String PROFILE_TAG      = "profile";
    /** tag a view within a profile */
    public static final String VIEW_TAG         = "view";
    /** tag holding a command group within a view */
    public static final String COMMANDGROUP_TAG = "commandGroup";
    /** the name attribute used in the tags */
    public static final String NAME_ATTRIBUTE   = "name";

    // other constants

    /** indicates that an export may overwrite an existing file */
    public static boolean IGNORE_EXISTANCE = true;

    /**
     * Constructor
     */
    public RolesProfileHandler() {
        super();
    }

	public boolean exportProfile(TreeModel aTreeModel, Collection<? extends BoundRole> someRoles, File aFile)
			throws IOException {
		try (TagWriter theWriter = TagWriter.newTagWriter(aFile, LayoutConstants.UTF_8)) {
			theWriter.writeXMLHeader(LayoutConstants.UTF_8);
			this.writeStart(theWriter);

			// theWriter.writeComment("Global and project-root roles start");
			this.writeProfilesFor(theWriter, someRoles, aTreeModel);

			this.writeEnd(theWriter);

			theWriter.flush();
			return true;
		}
    }

	/**
	 * Generate an XML representation of the current roles profile.
	 * 
	 * @param aTreeModel
	 *        the layout structure the roles profile is based on.
	 * @param aDestinationName
	 *        the name of the file the xml is written to
	 * @param anIgnorExistance
	 *        indicates, that an existing file may be overwritten
	 * @return <code>true</code> if the export was successful
	 */
	public boolean exportProfiles(TreeModel aTreeModel, Collection<? extends BoundRole> someRoles,
			String aDestinationName, boolean anIgnorExistance) throws Exception {
        // check for destination file and create if necessary
        FileManager theFM = FileManager.getInstance();
        File theDestination = theFM.getIDEFile(aDestinationName);
        if (theDestination.exists()) {
            if (!anIgnorExistance) {
                return false;
            }
        } else {
            FileUtilities.enforceDirectory(theDestination.getParent());
            theDestination.createNewFile();
        }
        return exportProfile(aTreeModel, someRoles, theDestination);
    }

    public File getProfileAsFile(TreeModel aTreeModel, Collection<? extends BoundRole> someRoles) throws IOException {
		File theFile = File.createTempFile("rolesProfileExport", null, Settings.getInstance().getTempDir());
        if (this.exportProfile(aTreeModel, someRoles, theFile)) {
            return theFile;
        } else {
            return null;
        }
    }

    /**
     * Helper for exportProfiles(); writes the role profiles for all the given roles.
     *
     * @param aWriter        the destination to write to
     * @param someRoles      the roles to export
     * @param aTreeModel     the layout structure
     */
	@Deprecated
	public void writeProfilesFor(TagWriter aWriter, Collection<? extends BoundRole> someRoles, TreeModel aTreeModel)
			throws IOException {
		for (BoundRole theRole : someRoles) {
			this.writeRoleProfileFor(aWriter, theRole, aTreeModel);
		}
	}

	public final void addProfilesFor(SecurityConfig out, Collection<? extends BoundRole> roles, LayoutConfigTree tree) {
		addProfilesFor(out, roles, tree, tree.getRoot());
	}

	public <N extends LayoutConfigTreeNode> void addProfilesFor(SecurityConfig out,
			Collection<? extends BoundRole> roles, TreeView<N> tree, N root) {
		List<N> allNodes = sortedNodes(tree, root);
		for (BoundRole theRole : roles) {
			addRoleProfileFor(out, theRole, allNodes);
		}
	}

    /**
     * Helper for exportProfiles(); writes the role profile for a single role
     *
     * @param aWriter        the destination to write to
     * @param aRole          the role to write the profile for
     * @param aTreeModel     the layout structure
     */
	@Deprecated
    protected void writeRoleProfileFor(TagWriter aWriter, BoundRole aRole, TreeModel aTreeModel) throws IOException {
        aWriter.beginBeginTag(PROFILE_TAG);
        aWriter.writeAttribute(NAME_ATTRIBUTE, aRole.getName());
        aWriter.endBeginTag();
        this.writeCommandGroupsFor(aWriter, aRole, aTreeModel, aTreeModel.getRoot());
        aWriter.endTag(PROFILE_TAG);
    }

	private <N extends LayoutConfigTreeNode> void addRoleProfileFor(SecurityConfig out, BoundRole role,
			List<N> nodes) {
		String key = role.getName();
		RoleProfileConfig profileConfig = out.getProfiles().get(key);
		if (profileConfig == null) {
			profileConfig = SecurityConfig.RoleProfileConfig.newInstance(role);
			addCommandGroupsFor(profileConfig, role, nodes);
			out.getProfiles().put(key, profileConfig);
		} else {
			addCommandGroupsFor(profileConfig, role, nodes);
		}
	}

	/**
	 * Helper for exportProfiles(); write the command groups active for the given role.
	 *
	 * @param aWriter
	 *        the destination to write to
	 * @param aRole
	 *        the role to write the profile for
	 * @param aTreeModel
	 *        the layout structure
	 * @param aNode
	 *        the current layout component to write the command groups for
	 */
	@Deprecated
    protected void writeCommandGroupsFor(TagWriter aWriter, BoundRole aRole,
            TreeModel aTreeModel, Object aNode) throws IOException {
        // write view itself
        if (aNode instanceof CompoundSecurityLayout) {
            CompoundSecurityLayout thePL = (CompoundSecurityLayout) aNode;
			Collection<BoundCommandGroup> theCommandGroups = thePL.getCommandGroups();
            if (theCommandGroups != null) {
				Collection<BoundCommandGroup> theAvailableCommandGroups = new ArrayList<>(theCommandGroups.size());
				for (BoundCommandGroup theCG : theCommandGroups) {
                    Collection theRoles = thePL.getRolesForCommandGroup(theCG);
                    if (theRoles != null && theRoles.contains(aRole)) {
						theAvailableCommandGroups.add(theCG);
                    }
                }
                if (!theAvailableCommandGroups.isEmpty()) {
                    aWriter.beginBeginTag(VIEW_TAG);
					aWriter.writeAttribute(NAME_ATTRIBUTE, thePL.getName().qualifiedName());
                    aWriter.endBeginTag();
					for (BoundCommandGroup theCG : theAvailableCommandGroups) {
                        aWriter.beginBeginTag(COMMANDGROUP_TAG);
						aWriter.writeAttribute(NAME_ATTRIBUTE, theCG.getID());
                        aWriter.endEmptyTag();
                    }
                    aWriter.endTag(VIEW_TAG);
                }
            }
        }
        // dispatch to children
        int theChildCount = aTreeModel.getChildCount(aNode);
        for (int i = 0; i < theChildCount; i++) {
			this.writeCommandGroupsFor(aWriter, aRole, aTreeModel, aTreeModel.getChild(aNode, i));
        }
    }

	private <N extends LayoutConfigTreeNode> void addCommandGroupsFor(RoleProfileConfig out, BoundRole role,
			List<N> nodes) {
		for (N child : nodes) {
			addCommandGroups(out, role, child);
		}
	}

	private <N extends LayoutConfigTreeNode> List<N> sortedNodes(TreeView<N> tree, N node) {
		List<N> allNodes = addAllNodes(new ArrayList<>(), tree, node);
		allNodes.sort(Comparator.comparing(elem -> elem.getConfig().getName().qualifiedName()));
		return allNodes;
	}

	private <N extends LayoutConfigTreeNode> List<N> addAllNodes(List<N> allNodes, TreeView<N> tree, N node) {
		if (node.getConfig() instanceof CompoundSecurityLayout.Config) {
			allNodes.add(node);
		}
		Iterator<? extends N> children = tree.getChildIterator(node);
		while (children.hasNext()) {
			allNodes = addAllNodes(allNodes, tree, children.next());
		}
		return allNodes;
	}

	private <N extends LayoutConfigTreeNode> void addCommandGroups(RoleProfileConfig out, BoundRole role, N node) {
		Collection<BoundCommandGroup> allCommandGroups = CommandGroupCollector.collectNonSystemGroups(node);
		if (allCommandGroups.isEmpty()) {
			return;
		}
		if (out.getViews().get(node.getConfig().getName()) != null) {
			/* A security node with the given security id already exists, maybe from a different
			 * MainLayout. Ignore it. */
			return;
		}
		PersBoundComp secComp = node.getSecurityComponent();
		if (secComp == null) {
			return;
		}
		if (!node.getConfig().getName().qualifiedName().equals(secComp.getName())) {
			/*Component delegates to another PersBoundComp. Ignore it.*/
			return;
		}
		ViewConfig viewConfig = null;
		for (BoundCommandGroup cg : sortedCopy(allCommandGroups)) {
			Collection theRoles = secComp.rolesForCommandGroup(cg);
			if (theRoles == null || !theRoles.contains(role)) {
				continue;
			}
			if (viewConfig == null) {
				viewConfig = SecurityConfig.ViewConfig.newInstance(node.getConfig());
			}
			viewConfig.getCommandGroups().add(SecurityConfig.CommandGroupConfig.newInstance(cg));
		}
		if (viewConfig != null) {
			out.getViews().put(viewConfig.getName(), viewConfig);
		}
	}

	private BoundCommandGroup[] sortedCopy(Collection<BoundCommandGroup> allCommandGroups) {
		BoundCommandGroup[] sortedGroups =
			allCommandGroups.toArray(new BoundCommandGroup[allCommandGroups.size()]);
		Arrays.sort(sortedGroups, SimpleBoundCommandGroup.COMPARE_BY_ID);
		return sortedGroups;
	}

	/**
	 * Helper for exportProfiles(); write the head of the roles profile
	 *
	 * @param aWriter
	 *        the destination to write to
	 */
    protected void writeStart(TagWriter aWriter) throws IOException {
        //aWriter.writeComment("Roles profile exported at " + (new Date()).toGMTString());
        aWriter.beginTag(ROOT_TAG);
        aWriter.beginTag(PROFILES_TAG);
    }

    /**
     * Helper for exportProfiles(); write the tail of the roles profile
     *
     * @param aWriter        the destination to write to
     */
    protected void writeEnd(TagWriter aWriter) throws IOException {
        aWriter.endTag(PROFILES_TAG);
        aWriter.endTag(ROOT_TAG);
    }

    /**
     * Return the default root element for this component.
     *
     * @return    The root element for this component, never <code>null</code>.
     */
    protected BoundObject getRoot() {
        return BoundHelper.getInstance().getDefaultObject();
    }

	/**
	 * Import a roles profile from a given input stream.
	 *
	 * <p>
	 * All existing rights are replaced by the imported ones. Entries that cannot be imported are
	 * reported to the user and to the log.
	 * </p>
	 *
	 * @param layoutTrees
	 *        The {@link LayoutConfigTree}'s that are known in the system.
	 * @param securityProfiles
	 *        The input stream to read the profiles from.
	 *
	 * @see AccessImporter
	 */
	public void importProfiles(Collection<LayoutConfigTree> layoutTrees, BinaryContent securityProfiles)
			throws ConfigurationException {
		AccessConfiguration config = AccessImporter.readConfig(securityProfiles);
		AccessImporter.Result result = new AccessImporter().importAccessConfig(config, layoutTrees);
		result.log();
		showWarning(I18NConstants.IMPORT_ROLES_PROFILE_MISSING_ROLES__ROLES, result.getMissingRoles());
		showWarning(I18NConstants.IMPORT_ROLES_PROFILE_MISSING_VIEWS__VIEWS, result.getMissingViews());
		showWarning(I18NConstants.IMPORT_ROLES_PROFILE_UNKNOWN_COMMAND_GROUPS__GROUPS,
			result.getUnknownCommandGroups());
		showWarning(I18NConstants.IMPORT_ROLES_PROFILE_UNAVAILABLE_COMMAND_GROUPS__GROUPS,
			result.getUnavailableCommandGroups());
		showWarning(I18NConstants.IMPORT_ROLES_PROFILE_IMPLICIT_READ_RIGHTS__RIGHTS, result.getImplicitReadRights());
	}

	private static void showWarning(ResKey1 message, Set<String> entries) {
		if (!entries.isEmpty()) {
			InfoService.showWarning(message.fill(StringServices.toString(entries)));
		}
	}

	/**
	 * Adds the rights of all {@link PersBoundComp}s that are not defined by a security layout in
	 * the given trees, e.g. security scopes of the view layer.
	 *
	 * @param out
	 *        The configuration to add the rights to.
	 * @param roles
	 *        The roles to export rights for.
	 * @param layoutTrees
	 *        The layout trees whose security layouts are already exported.
	 */
	public void addNonLayoutProfilesFor(SecurityConfig out, Collection<? extends BoundRole> roles,
			Collection<LayoutConfigTree> layoutTrees) {
		Set<ComponentName> layoutSecurity = AccessImporter.securityDefiningNodes(layoutTrees).keySet();
		List<PersBoundComp> secComps = SecurityComponentCache.getAllSecurityComponents()
			.stream()
			.filter(secComp -> !layoutSecurity.contains(secComp.getIdentifier()))
			.sorted(Comparator.comparing(PersBoundComp::getName))
			.collect(Collectors.toList());
		List<BoundCommandGroup> groups = new ArrayList<>(CommandGroupRegistry.getInstance().getAllCommandGroups());
		groups.remove(SimpleBoundCommandGroup.SYSTEM);
		groups.sort(SimpleBoundCommandGroup.COMPARE_BY_ID);
		for (BoundRole role : roles) {
			RoleProfileConfig profileConfig = out.getProfiles().get(role.getName());
			if (profileConfig == null) {
				profileConfig = SecurityConfig.RoleProfileConfig.newInstance(role);
				out.getProfiles().put(role.getName(), profileConfig);
			}
			for (PersBoundComp secComp : secComps) {
				if (profileConfig.getViews().get(secComp.getIdentifier()) != null) {
					continue;
				}
				ViewConfig viewConfig = null;
				for (BoundCommandGroup group : groups) {
					if (!secComp.rolesForCommandGroup(group).contains(role)) {
						continue;
					}
					if (viewConfig == null) {
						viewConfig = SecurityConfig.ViewConfig.newInstance(secComp.getIdentifier());
					}
					viewConfig.getCommandGroups().add(SecurityConfig.CommandGroupConfig.newInstance(group));
				}
				if (viewConfig != null) {
					profileConfig.getViews().put(viewConfig.getName(), viewConfig);
				}
			}
		}
	}

    /**
     * This visitor removes the existing roles profiles form the
     * project layouts so the import can work on a clean layout.
     */
    protected class ProjectLayoutSecurityPreImportVisitor extends DefaultDescendingLayoutVisitor {

        /**
         * Remove all access iff component is a Project Layout
         *
         * @see com.top_logic.mig.html.layout.LayoutComponentVisitor#visitLayoutComponent(com.top_logic.mig.html.layout.LayoutComponent)
         */
        @Override
		public boolean visitLayoutComponent(LayoutComponent aComponent) {
            if (aComponent instanceof CompoundSecurityLayout) {
                CompoundSecurityLayout thePL = (CompoundSecurityLayout) aComponent;
                thePL.removeAllAccess();
            }

            return true;
        }
    }


}
