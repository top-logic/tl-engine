/*
 * SPDX-FileCopyrightText: 2004 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec.compound.gui.admin.rolesProfile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import javax.swing.tree.TreeModel;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;

import org.xml.sax.SAXException;

import com.top_logic.basic.CalledFromJSP;
import com.top_logic.basic.FileManager;
import com.top_logic.basic.Logger;
import com.top_logic.basic.Settings;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.col.DescendantDFSIterator;
import com.top_logic.basic.col.TreeView;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.BinaryContent;
import com.top_logic.basic.io.Content;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.basic.xml.sax.SAXUtil;
import com.top_logic.event.infoservice.InfoService;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.mig.html.layout.DefaultDescendingLayoutVisitor;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.LayoutConfigTree;
import com.top_logic.mig.html.layout.LayoutConfigTreeNode;
import com.top_logic.mig.html.layout.LayoutConstants;
import com.top_logic.mig.html.layout.MainLayout;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.BoundHelper;
import com.top_logic.tool.boundsec.BoundObject;
import com.top_logic.tool.boundsec.BoundRole;
import com.top_logic.tool.boundsec.compound.CompoundSecurityLayout;
import com.top_logic.tool.boundsec.compound.CompoundSecurityLayoutCommandGroupDistributor;
import com.top_logic.tool.boundsec.compound.gui.admin.rolesProfile.SecurityConfig.RoleProfileConfig;
import com.top_logic.tool.boundsec.compound.gui.admin.rolesProfile.SecurityConfig.ViewConfig;
import com.top_logic.tool.boundsec.gui.profile.CommandGroupCollector;
import com.top_logic.tool.boundsec.gui.profile.CommandGroupDistributor;
import com.top_logic.tool.boundsec.wrap.BoundedRole;
import com.top_logic.tool.boundsec.wrap.PersBoundComp;
import com.top_logic.util.error.TopLogicException;

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
		Arrays.sort(sortedGroups, Comparator.comparing(BoundCommandGroup::getID));
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
	 * @param layoutTrees
	 *        The {@link LayoutConfigTree}'s that are known in the system.
	 * @param securityProfiles
	 *        The input stream to read the profiles from.
	 */
	public void importProfiles(Collection<LayoutConfigTree> layoutTrees, BinaryContent securityProfiles)
			throws ConfigurationException {
		SecurityConfig securityConfig = readSecurityProfiles(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY, securityProfiles);
		Map<String, RoleProfileConfig> profiles = securityConfig.getProfiles();
		Set<ComponentName> allViews = addAllViewNames(new HashSet<>(), profiles);
		Map<String, BoundedRole> roleByName = rolesByName(profiles.keySet());
		layoutTrees.forEach(tree -> importProfiles(tree, profiles, roleByName, allViews));
		if (!allViews.isEmpty()) {
			InfoService.showWarning(
				I18NConstants.IMPORT_ROLES_PROFILE_MISSING_VIEWS__VIEWS.fill(StringServices.toString(allViews)));
		}
	}

	private <T extends Collection<? super ComponentName>> T addAllViewNames(T out,
			Map<String, RoleProfileConfig> profiles) {
		profiles.values().forEach(conf -> out.addAll(conf.getViews().keySet()));
		return out;
	}

	private void importProfiles(LayoutConfigTree tree, Map<String, RoleProfileConfig> profiles,
			Map<String, BoundedRole> roleByName, Set<ComponentName> allConfiguredViews) {
		DescendantDFSIterator<LayoutConfigTreeNode> allNodes = new DescendantDFSIterator<>(tree, tree.getRoot());
		while (allNodes.hasNext()) {
			importProfiles(allNodes.next(), profiles, roleByName, allConfiguredViews);
		}
		allNodes = new DescendantDFSIterator<>(tree, tree.getRoot());
		while (allNodes.hasNext()) {
			CommandGroupDistributor.distributeNeededRolesToChildren(allNodes.next());
		}
	}

	private Map<String, BoundedRole> rolesByName(Collection<String> roleNames) {
		Map<String, BoundedRole> roles = BoundedRole.getRolesByName(roleNames);
		List<String> missingRoles = roleNames
			.stream()
			.filter(name -> !roles.containsKey(name))
			.collect(Collectors.toList());
		if (!missingRoles.isEmpty()) {
			InfoService.showWarning(
				I18NConstants.IMPORT_ROLES_PROFILE_MISSING_ROLES__ROLES.fill(StringServices.toString(missingRoles)));
		}
		return roles;
	}

	private void importProfiles(LayoutConfigTreeNode node, Map<String, RoleProfileConfig> profiles,
			Map<String, BoundedRole> roleByName, Set<ComponentName> allConfiguredViews) {
		PersBoundComp secComp = node.getSecurityComponent();
		if (secComp == null) {
			return;
		}
		ComponentName secCompName = secComp.getIdentifier();
		allConfiguredViews.remove(secCompName);
		Map<BoundedRole, Collection<BoundCommandGroup>> neededRights = new HashMap<>();
		for (Entry<String, RoleProfileConfig> profile: profiles.entrySet()) {
			BoundedRole role = roleByName.get(profile.getKey());
			if (role == null) {
				// non existing role. Already logged.
				continue;
			}
			Map<ComponentName, ViewConfig> views = profile.getValue().getViews();
			ViewConfig view = views.get(secCompName);
			if (view == null) {
				// No rights for view
				continue;
			}
			List<BoundCommandGroup> groups = view.getCommandGroups()
				.stream()
				.map(config -> config.getCommandGroup()).collect(Collectors.toList());
			neededRights.put(role, groups);
		}
		secComp.setAccess(neededRights);
	}

	private SecurityConfig readSecurityProfiles(InstantiationContext context, Content profiles)
			throws ConfigurationException {
		Map<String, ConfigurationDescriptor> rootDescriptor = Collections.singletonMap(
			ExportRolesProfileHandler.XML_TAG_SECURITY,
			TypedConfiguration.getConfigurationDescriptor(SecurityConfig.class));
		ConfigurationReader reader = new ConfigurationReader(context, rootDescriptor);
		reader.setSource(profiles);
		return (SecurityConfig) reader.read();
	}

    /**
     * Import a roles profile from a given file name.
     *
     * @param aMainLayout       the main layout to set the roles profile in
     * @param aProfileFileName  the name of the source containing the roles profile
     * @return <code>true</code> if the profiles are successfully imported
     */
	boolean importProfiles(MainLayout aMainLayout, String aProfileFileName) throws Exception {
        // check for destination file and create if necessary
        FileManager theFM = FileManager.getInstance();
		try (InputStream in = theFM.getStreamOrNull(aProfileFileName)) {
			if (in == null) {
				return false;
			}
			return importProfiles(aMainLayout, in);
		}
    }

    /**
     * Import a roles profile from a given input stream.
     *
     * @param aMainLayout       the main layout to set the roles profile in
     * @param aSource           the input stream to read the profiles from
     * @return <code>true</code> if the profiles are successfully imported
     */
	boolean importProfiles(MainLayout aMainLayout, InputStream aSource)
			throws FactoryConfigurationError, ParserConfigurationException, SAXException, IOException {
        // remove old settings
//      aMainLayout.acceptVisitorRecursively(new ProjectLayoutSecurityPreImportVisitor());
        // parse inpit and set profile on project layouts
        boolean parsingOK = false;
        try {
            ThreadContext.pushSuperUser();
			SAXParser parser = SAXUtil.newSAXParser();
			RolesProfileImporter rolesProfileImporter = new RolesProfileImporter(aMainLayout);
			parser.parse(aSource, rolesProfileImporter);
			if (!rolesProfileImporter.hasAnyProfile()) {
				// thow Exception if no profile information was found in the XML file
				throw new TopLogicException(getClass(), "noProfile");
			}
            parsingOK = true;
        } catch (SAXException sax) {
            if (sax.getCause() != null) {
                Logger.error("Failed to importProfiles()", sax.getCause(), this);
            }
            throw sax;
        }  finally {
            ThreadContext.popSuperUser();
        }

        // distribute the imported security
        aMainLayout.acceptVisitorRecursively(new ProjectLayoutSecurityPostImportVisitor());

        return parsingOK;
    }

	/**
	 * This method visits every {@link LayoutComponent} and reloads the security(roles)-profiles if
	 * it is a {@link CompoundSecurityLayout}.
	 * 
	 * @param aMainLayout
	 *        The {@link MainLayout} for which the reload should be applied.
	 */
	@CalledFromJSP
	public static void reloadSecurityProfiles(MainLayout aMainLayout) {
		ProjectLayoutSecurityPostImportVisitor visitor = new ProjectLayoutSecurityPostImportVisitor();
		aMainLayout.acceptVisitorRecursively(visitor);
	}


    /**
     * This visitor distributes the profiles set at the project layouts during the import
     * to the components contained in the project layouts.
     */
	protected static class ProjectLayoutSecurityPostImportVisitor extends DefaultDescendingLayoutVisitor {

        /**
         * Distribute the access set on the Project Layout
         *
         * @see com.top_logic.mig.html.layout.LayoutComponentVisitor#visitLayoutComponent(com.top_logic.mig.html.layout.LayoutComponent)
         */
        @Override
		public boolean visitLayoutComponent(LayoutComponent aComponent) {
            if (aComponent instanceof CompoundSecurityLayout) {
                CompoundSecurityLayout thePL = (CompoundSecurityLayout) aComponent;
                thePL.acceptVisitorRecursively(new CompoundSecurityLayoutCommandGroupDistributor());
            }

            return true;
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
