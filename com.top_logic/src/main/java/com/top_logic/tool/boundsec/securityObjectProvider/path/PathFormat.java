/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec.securityObjectProvider.path;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ListConfigValueProvider;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.tool.boundsec.securityObjectProvider.PathSecurityObjectProvider;
import com.top_logic.tool.boundsec.securityObjectProvider.path.Component.Config;

/**
 * Compact format to configure the path of a {@link PathSecurityObjectProvider}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class PathFormat extends ListConfigValueProvider<PolymorphicConfiguration<? extends SecurityPath>> {

	private static final Pattern PATH_PATTERN;

	private static final int CURRENTOBJECT_GROUP;

	private static final int MODEL_GROUP;

	private static final int WINDOW_GROUP;

	private static final int MASTER_GROUP;

	private static final int SELECTABLEMASTER_GROUP;

	private static final int SELECTION_GROUP;

	private static final int OPENER_GROUP;

	private static final int PARENT_GROUP;

	private static final int COMPONENT_GROUP;
	static {
		int groupIndex = 1;
		String currentobjectGroup = group(PathSecurityObjectProvider.PATH_ELEMENT_CURRENTOBJECT);
		CURRENTOBJECT_GROUP = groupIndex++;
		String modelGroup = group(PathSecurityObjectProvider.PATH_ELEMENT_MODEL);
		MODEL_GROUP = groupIndex++;
		String windowGroup = group(PathSecurityObjectProvider.PATH_ELEMENT_WINDOW);
		WINDOW_GROUP = groupIndex++;
		String masterGroup = group(PathSecurityObjectProvider.PATH_ELEMENT_MASTER);
		MASTER_GROUP = groupIndex++;
		String selectablemasterGroup = group(PathSecurityObjectProvider.PATH_ELEMENT_SELECTABLEMASTER);
		SELECTABLEMASTER_GROUP = groupIndex++;
		String selectionGroup = group(PathSecurityObjectProvider.PATH_ELEMENT_SELECTION);
		SELECTION_GROUP = groupIndex++;
		String parentGroup = group(PathSecurityObjectProvider.PATH_ELEMENT_PARENT);
		PARENT_GROUP = groupIndex++;
		String openerGroup = group(PathSecurityObjectProvider.PATH_ELEMENT_OPENER);
		OPENER_GROUP = groupIndex++;
		String componentGroup = PathSecurityObjectProvider.PATH_ELEMENT_COMPONENT + "\\(" + group("[^\\)]*") + "\\)";
		COMPONENT_GROUP = groupIndex++;

		PATH_PATTERN = Pattern.compile("\\.?(?:" + currentobjectGroup + "|" + modelGroup + "|" + windowGroup + "|"
			+ masterGroup + "|" + selectablemasterGroup + "|" + selectionGroup + "|" + parentGroup + "|"
			+ openerGroup + "|" + componentGroup + ")");
	}

	private static String group(String content) {
		return "(" + content + ")";
	}

	/** Singleton {@link PathFormat} instance. */
	public static final PathFormat INSTANCE = new PathFormat();

	private PathFormat() {
		// singleton instance
	}

	@Override
	protected List<PolymorphicConfiguration<? extends SecurityPath>> getValueNonEmpty(String propertyName,
			CharSequence propertyValue) throws ConfigurationException {
		ArrayList<PolymorphicConfiguration<? extends SecurityPath>> l = new ArrayList<>();

		int offset = 0;
		Matcher matcher = PATH_PATTERN.matcher(propertyValue);
		while (matcher.lookingAt()) {
			if (matcher.group(CURRENTOBJECT_GROUP) != null) {
				l.add(SecurityPath.currentObject().getConfig());
			} else if (matcher.group(MODEL_GROUP) != null) {
				l.add(SecurityPath.model().getConfig());
			} else if (matcher.group(OPENER_GROUP) != null) {
				l.add(SecurityPath.opener().getConfig());
			} else if (matcher.group(WINDOW_GROUP) != null) {
				l.add(SecurityPath.window().getConfig());
			} else if (matcher.group(PARENT_GROUP) != null) {
				l.add(SecurityPath.parent().getConfig());
			} else if (matcher.group(MASTER_GROUP) != null) {
				l.add(SecurityPath.master().getConfig());
			} else if (matcher.group(SELECTABLEMASTER_GROUP) != null) {
				l.add(SecurityPath.selectablemaster().getConfig());
			} else if (matcher.group(SELECTION_GROUP) != null) {
				l.add(SecurityPath.selection().getConfig());
			} else if (matcher.group(COMPONENT_GROUP) != null) {
				l.add(componentConfig(propertyName, matcher.group(COMPONENT_GROUP)));
			} else {
				throw new UnreachableAssertion("Each part of the pattern needs a group match.");
			}
			offset = matcher.end();
			matcher.region(offset, propertyValue.length());
		}
		if (offset < propertyValue.length()) {
			String unknownPart = propertyValue.subSequence(offset, propertyValue.length()).toString();
			throw new ConfigurationException(I18NConstants.UNKNOWN_PATH_ELEMENT__PATH_ELEMENT.fill(unknownPart),
				propertyName, unknownPart);
		}
		
		return l;
	}

	private static Config componentConfig(String propertyName, String componentName) throws ConfigurationException {
		return SecurityPath.componentConfig(ComponentName.newConfiguredName(propertyName, componentName));
	}

	@Override
	protected String getSpecificationNonNull(List<PolymorphicConfiguration<? extends SecurityPath>> configValue) {
		switch (configValue.size()) {
			case 0:
				return StringServices.EMPTY_STRING;
			case 1:
				return getSpec(configValue.get(0));
			default:
				StringBuilder tmp = new StringBuilder(getSpec(configValue.get(0)));
				for (int i = 1; i < configValue.size(); i++) {
					tmp.append(PathSecurityObjectProvider.PATH_ELEMENT_SEPARATOR);
					tmp.append(getSpec(configValue.get(i)));
				}
				return tmp.toString();
		}

	}

	private String getSpec(PolymorphicConfiguration<? extends SecurityPath> path) {
		if (path.getImplementationClass() == CurrentObject.class) {
			return PathSecurityObjectProvider.PATH_ELEMENT_CURRENTOBJECT;
		}
		if (path.getImplementationClass() == Master.class) {
			return PathSecurityObjectProvider.PATH_ELEMENT_MASTER;
		}
		if (path.getImplementationClass() == Selection.class) {
			return PathSecurityObjectProvider.PATH_ELEMENT_SELECTION;
		}
		if (path.getImplementationClass() == SelectableMaster.class) {
			return PathSecurityObjectProvider.PATH_ELEMENT_SELECTABLEMASTER;
		}
		if (path.getImplementationClass() == Opener.class) {
			return PathSecurityObjectProvider.PATH_ELEMENT_OPENER;
		}
		if (path.getImplementationClass() == Model.class) {
			return PathSecurityObjectProvider.PATH_ELEMENT_MODEL;
		}
		if (path.getImplementationClass() == Parent.class) {
			return PathSecurityObjectProvider.PATH_ELEMENT_PARENT;
		}
		if (path.getImplementationClass() == Window.class) {
			return PathSecurityObjectProvider.PATH_ELEMENT_WINDOW;
		}
		if (path instanceof Component.Config) {
			StringBuilder componentPath = new StringBuilder();
			componentPath.append(PathSecurityObjectProvider.PATH_ELEMENT_COMPONENT);
			componentPath.append("(");
			componentPath.append(((Component.Config) path).getName().qualifiedName());
			componentPath.append(")");
			return componentPath.toString();
		}
		throw new IllegalArgumentException("Unsupported: " + path);
	}

}

