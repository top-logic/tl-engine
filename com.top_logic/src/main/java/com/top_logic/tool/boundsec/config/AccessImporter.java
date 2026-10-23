/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.col.DescendantDFSIterator;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.BinaryContent;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.mig.html.layout.LayoutConfigTree;
import com.top_logic.mig.html.layout.LayoutConfigTreeNode;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.compound.CompoundSecurityLayout;
import com.top_logic.tool.boundsec.config.AccessConfiguration.ProfileConfig;
import com.top_logic.tool.boundsec.config.AccessConfiguration.ProfileConfig.ViewConfig;
import com.top_logic.tool.boundsec.config.AccessConfiguration.ProfileConfig.ViewConfig.CommandGroupRef;
import com.top_logic.tool.boundsec.gui.profile.CommandGroupCollector;
import com.top_logic.tool.boundsec.gui.profile.ImplicitReadRights;
import com.top_logic.tool.boundsec.simple.CommandGroupRegistry;
import com.top_logic.tool.boundsec.wrap.BoundedRole;
import com.top_logic.tool.boundsec.wrap.PersBoundComp;
import com.top_logic.tool.boundsec.wrap.SecurityComponentCache;

/**
 * Import functionality for applying an {@link AccessConfiguration}.
 *
 * <p>
 * The import replaces all access rights of the application with the rights given in the
 * {@link AccessConfiguration}:
 * </p>
 * <ul>
 * <li>Rights for a view that is a security layout in one of the given {@link LayoutConfigTree}s
 * are only imported for command groups that are available in this view.</li>
 * <li>Rights for other security objects, e.g. security scopes of the view layer, are imported
 * without check.</li>
 * <li>Read rights that are implicitly needed by the imported rights are added, see
 * {@link ImplicitReadRights}.</li>
 * </ul>
 *
 * <p>
 * Entries that cannot be imported are skipped and reported in the {@link Result}.
 * </p>
 */
public class AccessImporter {

	/** Root tag of an {@link AccessConfiguration} in a file. */
	public static final String XML_TAG_SECURITY = "security";

	/**
	 * Reads an {@link AccessConfiguration} from the given content.
	 */
	public static AccessConfiguration readConfig(BinaryContent data) throws ConfigurationException {
		return (AccessConfiguration) ConfigurationReader.readContent(
			SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY,
			Collections.singletonMap(XML_TAG_SECURITY,
				TypedConfiguration.getConfigurationDescriptor(AccessConfiguration.class)),
			data);
	}

	/**
	 * The nodes of the given trees that define the security of a {@link PersBoundComp}, indexed
	 * by the {@link PersBoundComp#getIdentifier() identifier} of their {@link PersBoundComp}.
	 *
	 * <p>
	 * A security layout that delegates its security to a different security layout is not
	 * contained.
	 * </p>
	 */
	public static Map<ComponentName, LayoutConfigTreeNode> securityDefiningNodes(
			Collection<LayoutConfigTree> layoutTrees) {
		Map<ComponentName, LayoutConfigTreeNode> result = new HashMap<>();
		for (LayoutConfigTree tree : layoutTrees) {
			DescendantDFSIterator<LayoutConfigTreeNode> nodes =
				new DescendantDFSIterator<>(tree, tree.getRoot(), true);
			while (nodes.hasNext()) {
				LayoutConfigTreeNode node = nodes.next();
				if (!(node.getConfig() instanceof CompoundSecurityLayout.Config)) {
					continue;
				}
				PersBoundComp secComp = node.getSecurityComponent();
				if (secComp == null) {
					continue;
				}
				if (!node.getConfig().getName().qualifiedName().equals(secComp.getName())) {
					// Security is delegated to a different component.
					continue;
				}
				result.putIfAbsent(secComp.getIdentifier(), node);
			}
		}
		return result;
	}

	/**
	 * Resets and imports access rights specified in the given {@link AccessConfiguration}.
	 *
	 * @param config
	 *        The rights to import.
	 * @param layoutTrees
	 *        The layouts of the application. Used to check the imported command groups and to add
	 *        implicitly needed read rights.
	 * @return The entries that could not be imported and the added implicit read rights.
	 */
	public Result importAccessConfig(AccessConfiguration config, Collection<LayoutConfigTree> layoutTrees) {
		Result result = new Result();

		// Reset.
		for (PersBoundComp securityComponent : SecurityComponentCache.getAllSecurityComponents()) {
			securityComponent.removeAllAccess();
		}

		// Add access.
		Map<ComponentName, LayoutConfigTreeNode> definingNodes = securityDefiningNodes(layoutTrees);
		List<BoundedRole> importedRoles = new ArrayList<>();
		for (ProfileConfig profile : config.getProfiles()) {
			String roleName = profile.getRole();
			BoundedRole role = BoundedRole.getRoleByName(roleName);
			if (role == null) {
				result._missingRoles.add(roleName);
				continue;
			}
			importedRoles.add(role);

			for (ViewConfig view : profile.getViews()) {
				ComponentName componentName = view.getComponent();

				PersBoundComp securityComponent = SecurityComponentCache.getSecurityComponent(componentName);
				if (securityComponent == null) {
					result._missingViews.add(componentName.qualifiedName());
					continue;
				}

				LayoutConfigTreeNode node = definingNodes.get(securityComponent.getIdentifier());
				Collection<BoundCommandGroup> availableGroups =
					node == null ? null : CommandGroupCollector.collectNonSystemGroups(node);
				for (CommandGroupRef commandGroupRef : view.getCommandGroups()) {
					String commandGroupName = commandGroupRef.getName();
					BoundCommandGroup commandGroup = CommandGroupRegistry.getInstance().getGroup(commandGroupName);
					if (commandGroup == null) {
						result._unknownCommandGroups.add(entry(commandGroupName, componentName, role));
						continue;
					}
					if (availableGroups != null && !availableGroups.contains(commandGroup)) {
						result._unavailableCommandGroups.add(entry(commandGroupName, componentName, role));
						continue;
					}

					securityComponent.addAccess(commandGroup, role);
				}
			}
		}

		// Add implicitly needed read rights.
		for (BoundedRole role : importedRoles) {
			List<PersBoundComp> added = new ArrayList<>();
			for (LayoutConfigTree tree : layoutTrees) {
				ImplicitReadRights.addImplicitReadRights(tree, role, added);
			}
			for (PersBoundComp secComp : added) {
				result._implicitReadRights.add("'" + secComp.getName() + "' for role '" + role.getName() + "'");
			}
		}
		return result;
	}

	private static String entry(String commandGroup, ComponentName view, BoundedRole role) {
		return "'" + commandGroup + "' in '" + view.qualifiedName() + "' for role '" + role.getName() + "'";
	}

	/**
	 * Report of an {@link AccessImporter#importAccessConfig(AccessConfiguration, Collection)
	 * import}.
	 */
	public static class Result {

		final Set<String> _missingRoles = new TreeSet<>();

		final Set<String> _missingViews = new TreeSet<>();

		final Set<String> _unknownCommandGroups = new TreeSet<>();

		final Set<String> _unavailableCommandGroups = new TreeSet<>();

		final Set<String> _implicitReadRights = new TreeSet<>();

		/**
		 * Names of the roles in the configuration that do not exist. Their rights are not
		 * imported.
		 */
		public Set<String> getMissingRoles() {
			return Collections.unmodifiableSet(_missingRoles);
		}

		/**
		 * Names of the views in the configuration that have no security object. Their rights are
		 * not imported.
		 */
		public Set<String> getMissingViews() {
			return Collections.unmodifiableSet(_missingViews);
		}

		/**
		 * Command groups in the configuration that are not registered. They are not imported.
		 */
		public Set<String> getUnknownCommandGroups() {
			return Collections.unmodifiableSet(_unknownCommandGroups);
		}

		/**
		 * Command groups in the configuration that are not available in their view. They are not
		 * imported.
		 */
		public Set<String> getUnavailableCommandGroups() {
			return Collections.unmodifiableSet(_unavailableCommandGroups);
		}

		/**
		 * Views and roles for which an implicitly needed read right was added.
		 */
		public Set<String> getImplicitReadRights() {
			return Collections.unmodifiableSet(_implicitReadRights);
		}

		/**
		 * Writes a warning to the log for each kind of reported entries.
		 */
		public void log() {
			warn("Rights for non-existing roles not imported: ", _missingRoles);
			warn("Rights for non-existing views not imported: ", _missingViews);
			warn("Unknown command groups not imported: ", _unknownCommandGroups);
			warn("Command groups not available in their views not imported: ", _unavailableCommandGroups);
			warn("Implicitly needed read rights added: ", _implicitReadRights);
		}

		private static void warn(String message, Set<String> entries) {
			if (!entries.isEmpty()) {
				Logger.warn(message + StringServices.toString(entries), AccessImporter.class);
			}
		}

	}

}
