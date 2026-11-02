/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.top_logic.basic.FileManager;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.Location;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.element.boundsec.manager.coverage.OperationUse;
import com.top_logic.element.boundsec.manager.coverage.OperationUse.Kind;
import com.top_logic.element.boundsec.manager.coverage.OperationUseProvider;
import com.top_logic.element.boundsec.manager.coverage.SecurityCoverageCheck;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewElement;
import com.top_logic.layout.view.ViewLoader;
import com.top_logic.layout.view.element.FormElement;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.util.TLModelPartRef;
import com.top_logic.model.search.configured.ConfiguredScript;
import com.top_logic.model.search.configured.ConfiguredTLScriptFunctions;
import com.top_logic.model.search.configured.PrivilegedScript;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;

/**
 * Index of the places of the application checking an operation: the {@code .view.xml} files and
 * the configured TL-Script functions.
 *
 * <p>
 * In the views, these are
 * </p>
 * <ul>
 * <li>a UI unit with an {@code <access-control operation="…">} ({@link AccessControl}), checked on
 * its {@code type}, or on the security root when it names no object;</li>
 * <li>a command with an {@code <access>} of an {@code <operation name="…">} ({@link OperationAccess}),
 * checked on its {@code type}, or - without an object - on what the innermost enclosing unit of the
 * same view file checks;</li>
 * <li>a command with an {@code <access>} of a {@code <create type="…">} ({@link CreateAccess}),
 * checking the creation right on the created type, or, without a type, on the type of its
 * reference;</li>
 * <li>the same checks in the {@code <link-access>} and {@code <remove-access>} of an
 * {@code <object-list>}, which its {@code <link-element>} and {@code <remove-element>} actions
 * check;</li>
 * <li>a {@code <form>} declaring an {@code edit-operation} ({@link FormElement}); the type of the
 * form object is not declared.</li>
 * </ul>
 * <p>
 * In the configured TL-Script functions, every {@code <privileged-script>} ({@link PrivilegedScript})
 * with its {@code command-group}; the type of its security object is not declared.
 * </p>
 *
 * <p>
 * A command whose enclosing unit lies in another view file - the view containing it is referenced
 * from a unit elsewhere - is reported as checking an object of an undeclared type, since a view
 * file can be displayed in several places. A view that cannot be loaded is left out and logged.
 * </p>
 *
 * <p>
 * The index is computed on demand and kept: a view is scanned again only when it was changed, as
 * the {@link ViewLoader} reports by handing out another configuration for it, and the scripts are
 * read again when the configured TL-Script functions were restarted.
 * </p>
 *
 * <p>
 * The index contributes to the {@link SecurityCoverageCheck.Config#getOperationUses() operation
 * uses} of the {@link SecurityCoverageCheck}.
 * </p>
 *
 * @implNote A {@code <create>} is indexed as a use of {@link SimpleBoundCommandGroup#CREATE}.
 */
public class OperationUseIndex implements OperationUseProvider {

	/** Suffix of the files the views are defined in. */
	private static final String VIEW_SUFFIX = ".view.xml";

	/** Separator between the parts of the description of a place. */
	private static final String LOCATION_SEPARATOR = ":";

	/** Marks the id of an element in the description of a place. */
	private static final String ID_PREFIX = " #";

	/** Separator between the source and the name of a configured function. */
	private static final String FUNCTION_SEPARATOR = " ";

	/** Suffix of the name of a configured function in the description of a place. */
	private static final String FUNCTION_SUFFIX = "()";

	/** Describes where the configured TL-Script functions come from. */
	private static final String SCRIPTS_SOURCE = "TL-Script";

	/**
	 * Configuration of an {@link OperationUseIndex}.
	 */
	public interface Config extends PolymorphicConfiguration<OperationUseIndex> {

		@Override
		@ClassDefault(OperationUseIndex.class)
		Class<? extends OperationUseIndex> getImplementationClass();

	}

	/**
	 * What a place checks an operation on, as far as it is declared.
	 */
	private record Checked(OperationUse.Target target, TLClass type) {

		static final Checked ROOT = new Checked(OperationUse.Target.ROOT, null);

		static final Checked UNKNOWN = new Checked(OperationUse.Target.UNKNOWN, null);

		OperationUse use(Kind kind, String operation, String user) {
			return new OperationUse(kind, operation, target, type, user);
		}

	}

	/** The scanned views, by their full path: the configuration scanned, and the uses found. */
	private final Map<String, ScannedView> _views = new LinkedHashMap<>();

	private Object _scannedScripts;

	private List<OperationUse> _scriptUses = Collections.emptyList();

	private record ScannedView(ViewElement.Config config, List<OperationUse> uses) {
		// Pure data.
	}

	/**
	 * Creates a {@link OperationUseIndex} from configuration.
	 */
	public OperationUseIndex(InstantiationContext context, Config config) {
		// Nothing configured.
	}

	@Override
	public synchronized List<OperationUse> getOperationUses() {
		List<OperationUse> result = new ArrayList<>(viewUses());
		result.addAll(scriptUses());
		return result;
	}

	/**
	 * The uses of the views, scanning the views that changed since they were scanned.
	 */
	private List<OperationUse> viewUses() {
		List<String> paths = new ArrayList<>();
		collectViews(FileManager.getInstance(), ViewLoader.VIEW_BASE_PATH, paths);
		Collections.sort(paths);
		_views.keySet().retainAll(paths);

		List<OperationUse> result = new ArrayList<>();
		for (String path : paths) {
			ViewElement.Config config;
			try {
				config = ViewLoader.getOrLoadConfig(path);
			} catch (ConfigurationException | RuntimeException ex) {
				Logger.info("The view '" + path + "' cannot be loaded, its operation checks are not indexed: "
					+ ex.getMessage(), OperationUseIndex.class);
				_views.remove(path);
				continue;
			}
			ScannedView scanned = _views.get(path);
			if (scanned == null || scanned.config() != config) {
				scanned = new ScannedView(config, scanView(ViewLoader.viewRef(path), config));
				_views.put(path, scanned);
			}
			result.addAll(scanned.uses());
		}
		return result;
	}

	private static void collectViews(FileManager files, String dir, List<String> found) {
		for (String resource : files.getResourcePaths(dir)) {
			String path = resource.startsWith("/") ? resource : "/" + resource;
			if (files.isDirectory(path)) {
				collectViews(files, path.endsWith("/") ? path : path + "/", found);
			} else if (path.endsWith(VIEW_SUFFIX)) {
				found.add(path);
			}
		}
	}

	/**
	 * The places checking an operation in the given view configuration.
	 *
	 * @param viewRef
	 *        The path of the view as it is referenced, describing where a place is.
	 * @param config
	 *        The configuration of the view.
	 */
	public static List<OperationUse> scanView(String viewRef, ConfigurationItem config) {
		List<OperationUse> result = new ArrayList<>();
		scan(viewRef, config, null, result, Collections.newSetFromMap(new IdentityHashMap<>()));
		return result;
	}

	/**
	 * Collects the uses of the given item and of all items below it.
	 *
	 * @param enclosing
	 *        What the innermost enclosing unit with an access control checks, <code>null</code>
	 *        outside of such a unit.
	 */
	private static void scan(String viewRef, ConfigurationItem item, Checked enclosing, List<OperationUse> result,
			Set<ConfigurationItem> seen) {
		if (!seen.add(item)) {
			return;
		}
		Checked inner = enclosing;
		if (item instanceof WithAccessControl unit && unit.getAccessControl() != null) {
			AccessControl access = unit.getAccessControl();
			Checked checked = access.getType() != null ? checkedType(access.getType())
				: access.getObject() == null ? Checked.ROOT : Checked.UNKNOWN;
			result.add(checked.use(Kind.VIEW, access.getOperation().id(), describe(viewRef, item)));
			inner = checked;
		}
		if (item instanceof OperationAccess.Config operation) {
			Checked checked = operation.getType() != null ? checkedType(operation.getType())
				: operation.getObject() != null || enclosing == null ? Checked.UNKNOWN : enclosing;
			result.add(checked.use(Kind.COMMAND, operation.getName().id(), describe(viewRef, item)));
		}
		if (item instanceof CreateAccess.Config create
			&& (create.getType() != null || create.getReference() != null)) {
			Checked checked =
				create.getType() != null ? checkedType(create.getType()) : referenceType(create.getReference());
			result.add(checked.use(Kind.COMMAND, SimpleBoundCommandGroup.CREATE_NAME, describe(viewRef, item)));
		}
		if (item instanceof FormElement.Config form
			&& form.valueSet(form.descriptor().getProperty(FormElement.Config.EDIT_OPERATION))) {
			result.add(Checked.UNKNOWN.use(Kind.FORM, form.getEditOperation().id(), describe(viewRef, item)));
		}
		for (PropertyDescriptor property : item.descriptor().getProperties()) {
			switch (property.kind()) {
				case ITEM, ARRAY, LIST, MAP -> scanValue(viewRef, item.value(property), inner, result, seen);
				default -> {
					// No configuration below.
				}
			}
		}
	}

	private static void scanValue(String viewRef, Object value, Checked enclosing, List<OperationUse> result,
			Set<ConfigurationItem> seen) {
		if (value instanceof ConfigurationItem child) {
			scan(viewRef, child, enclosing, result, seen);
		} else if (value instanceof Collection<?> collection) {
			for (Object element : collection) {
				scanValue(viewRef, element, enclosing, result, seen);
			}
		} else if (value instanceof Map<?, ?> map) {
			for (Object element : map.values()) {
				scanValue(viewRef, element, enclosing, result, seen);
			}
		} else if (value instanceof Object[] array) {
			for (Object element : array) {
				scanValue(viewRef, element, enclosing, result, seen);
			}
		}
	}

	/**
	 * What a place declaring the given type checks.
	 *
	 * <p>
	 * A type that cannot be resolved counts as undeclared; loading the view reports it.
	 * </p>
	 */
	private static Checked checkedType(TLModelPartRef ref) {
		try {
			return new Checked(OperationUse.Target.TYPE, ref.resolveClass());
		} catch (ConfigurationException | RuntimeException ex) {
			return Checked.UNKNOWN;
		}
	}

	/**
	 * What a creation into the given reference checks: the type of the reference.
	 */
	private static Checked referenceType(TLModelPartRef ref) {
		try {
			if (ref.resolvePart() instanceof TLStructuredTypePart part && part.getType() instanceof TLClass type) {
				return new Checked(OperationUse.Target.TYPE, type);
			}
		} catch (RuntimeException ex) {
			// Reported when the view is loaded.
		}
		return Checked.UNKNOWN;
	}

	/**
	 * Where the given item is: the view path, the line, and the id of an element that has one.
	 */
	private static String describe(String viewRef, ConfigurationItem item) {
		StringBuilder result = new StringBuilder(viewRef);
		Location location = item.location();
		if (location != null && location.getLine() != Location.NO_LINE) {
			result.append(LOCATION_SEPARATOR).append(location.getLine());
		}
		String id = id(item);
		if (id != null) {
			result.append(ID_PREFIX).append(id);
		}
		return result.toString();
	}

	/**
	 * The id of the given item, <code>null</code> when it has none.
	 *
	 * <p>
	 * Not only the elements carry an id: a tab or a section, for instance, is addressed by its id as
	 * well, so the id is looked up by the name the elements use for it.
	 * </p>
	 */
	private static String id(ConfigurationItem item) {
		PropertyDescriptor property = item.descriptor().getProperty(UIElement.Config.ID);
		if (property == null) {
			return null;
		}
		return item.value(property) instanceof String id && !id.isEmpty() ? id : null;
	}

	/**
	 * The uses of the privileged scripts, read again when the configured functions changed.
	 */
	private List<OperationUse> scriptUses() {
		if (!ConfiguredTLScriptFunctions.Module.INSTANCE.isActive()) {
			return Collections.emptyList();
		}
		Map<String, ConfiguredScript.Config> scripts =
			ConfiguredTLScriptFunctions.Module.INSTANCE.getImplementationInstance().getConfig().getScripts();
		if (scripts != _scannedScripts) {
			_scriptUses = scanScripts(scripts.values());
			_scannedScripts = scripts;
		}
		return _scriptUses;
	}

	/**
	 * The places checking an operation among the given configured TL-Script functions.
	 */
	public static List<OperationUse> scanScripts(Collection<? extends ConfiguredScript.Config> scripts) {
		List<OperationUse> result = new ArrayList<>();
		for (ConfiguredScript.Config script : scripts) {
			if (script instanceof PrivilegedScript.Config privileged && privileged.getCommandGroup() != null) {
				result.add(OperationUse.onUnknown(Kind.SCRIPT, privileged.getCommandGroup().id(),
					SCRIPTS_SOURCE + FUNCTION_SEPARATOR + privileged.getName() + FUNCTION_SUFFIX));
			}
		}
		return result;
	}

}
