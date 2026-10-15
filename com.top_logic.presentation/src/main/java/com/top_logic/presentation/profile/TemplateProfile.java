/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.presentation.profile;

import static com.top_logic.presentation.data.PresentationData.*;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.presentation.data.PresentationData;
import com.top_logic.presentation.model.ColorSpec;
import com.top_logic.presentation.model.DescriptionReader;
import com.top_logic.presentation.model.Entries;
import com.top_logic.presentation.model.LayoutRef;
import com.top_logic.presentation.model.LayoutRole;
import com.top_logic.presentation.model.Value;
import com.top_logic.presentation.template.LayoutInfo;
import com.top_logic.presentation.template.TemplateAnalysis;
import com.top_logic.util.error.TopLogicException;

/**
 * What a presentation export needs to know about a template beyond its raw
 * {@link TemplateAnalysis}: which slide layout serves which purpose, which placeholders take the
 * subtitle and the footer, where the content of a slide goes, and which colors the color roles
 * stand for.
 *
 * <p>
 * A profile is derived from the template analysis by {@link #derive(TemplateAnalysis)}, see
 * {@link ProfileDerivation} for the rules. Its {@link #toData() data representation} can be
 * overridden partially, e.g. to choose another content layout or to define color roles, see
 * {@link #create(TemplateAnalysis, Map)}.
 * </p>
 *
 * @param layouts
 *        The index of the layout for each role that has one.
 * @param slideLayouts
 *        The properties of the slide layouts by their index.
 * @param roles
 *        The colors of the color roles by the role names.
 */
public record TemplateProfile(Map<LayoutRole, Integer> layouts, Map<Integer, LayoutProfile> slideLayouts,
		Map<String, ColorSpec> roles) {

	/**
	 * Derives the profile of the analyzed template.
	 *
	 * <p>
	 * The derived profile defines no color roles.
	 * </p>
	 */
	public static TemplateProfile derive(TemplateAnalysis analysis) {
		return ProfileDerivation.derive(analysis);
	}

	/**
	 * Creates the profile of the analyzed template, overridden by the given profile data.
	 *
	 * @param analysis
	 *        The analysis of the template.
	 * @param override
	 *        Profile data in the form of {@link #toData()}, given partially, <code>null</code> for
	 *        none. It is {@link PresentationData#merge(Map, Map) merged} into the data of the
	 *        derived profile. Errors in it are reported with paths starting with
	 *        {@link PresentationData#PROFILE}.
	 * @return The profile.
	 */
	public static TemplateProfile create(TemplateAnalysis analysis, Map<?, ?> override) {
		TemplateProfile derived = derive(analysis);
		if (override == null || override.isEmpty()) {
			return derived;
		}
		return read(analysis, PresentationData.merge(derived.toData(), override));
	}

	/**
	 * Reads a profile from its {@link #toData() data representation}.
	 *
	 * @param analysis
	 *        The analysis of the template the profile describes.
	 * @param data
	 *        The complete profile data. Errors are reported with paths starting with
	 *        {@link PresentationData#PROFILE}.
	 */
	public static TemplateProfile read(TemplateAnalysis analysis, Map<?, ?> data) {
		Entries entries = new Value(data, PROFILE).asEntries();

		Map<LayoutRole, Integer> layouts = new EnumMap<>(LayoutRole.class);
		Value layoutsValue = entries.get(LAYOUTS);
		if (!layoutsValue.isAbsent()) {
			Entries roleEntries = layoutsValue.asEntries();
			for (LayoutRole role : LayoutRole.values()) {
				Value layout = roleEntries.get(role.id());
				if (!layout.isAbsent()) {
					layouts.put(role, Integer.valueOf(layoutIndex(analysis, layout)));
				}
			}
			roleEntries.checkComplete();
		}

		Map<Integer, LayoutProfile> slideLayouts = new LinkedHashMap<>();
		Value slideLayoutsValue = entries.get(SLIDE_LAYOUTS);
		if (!slideLayoutsValue.isAbsent()) {
			for (Map.Entry<String, Value> entry : slideLayoutsValue.asEntries().all().entrySet()) {
				Value layout = entry.getValue();
				int index = layoutIndex(analysis, new Value(parseIndex(entry.getKey()), layout.path()));
				slideLayouts.put(Integer.valueOf(index), readLayoutProfile(layout));
			}
		}

		Map<String, ColorSpec> roles = new LinkedHashMap<>();
		Value rolesValue = entries.get(ROLES);
		if (!rolesValue.isAbsent()) {
			for (Map.Entry<String, Value> entry : rolesValue.asEntries().all().entrySet()) {
				Value color = entry.getValue().require(com.top_logic.presentation.model.I18NConstants.EXPECTED_COLOR);
				roles.put(entry.getKey(), DescriptionReader.readColor(color));
			}
		}
		entries.checkComplete();
		return new TemplateProfile(layouts, slideLayouts, roles);
	}

	private static Object parseIndex(String key) {
		if (!key.isEmpty() && key.length() < 10 && key.chars().allMatch(Character::isDigit)) {
			return Integer.valueOf(key);
		}
		return key;
	}

	private static LayoutProfile readLayoutProfile(Value layout) {
		Entries entries = layout.asEntries();
		String name = entries.get(NAME).optString();
		Long subtitle = placeholder(entries.get(SUBTITLE));
		Long footer = placeholder(entries.get(FOOTER));
		Entries area = entries.require(CONTENT_AREA, com.top_logic.presentation.model.I18NConstants.EXPECTED_DICTIONARY)
			.asEntries();
		Rectangle2D contentArea = new Rectangle2D.Double(
			area.require(X, com.top_logic.presentation.model.I18NConstants.EXPECTED_NUMBER).asNumber(),
			area.require(Y, com.top_logic.presentation.model.I18NConstants.EXPECTED_NUMBER).asNumber(),
			area.require(WIDTH, com.top_logic.presentation.model.I18NConstants.EXPECTED_NON_NEGATIVE_NUMBER)
				.asNonNegative(),
			area.require(HEIGHT, com.top_logic.presentation.model.I18NConstants.EXPECTED_NON_NEGATIVE_NUMBER)
				.asNonNegative());
		area.checkComplete();
		entries.checkComplete();
		return new LayoutProfile(name, subtitle, footer, contentArea);
	}

	private static Long placeholder(Value placeholder) {
		if (placeholder.isAbsent() || (placeholder.isString() && NONE.equals(placeholder.asString()))) {
			return null;
		}
		if (placeholder.isNumber()) {
			return Long.valueOf(placeholder.asInteger(0, Integer.MAX_VALUE));
		}
		throw placeholder.invalid(I18NConstants.EXPECTED_PLACEHOLDER);
	}

	/**
	 * The index of the layout given by its index or its name.
	 */
	private static int layoutIndex(TemplateAnalysis analysis, Value layout) {
		if (layout.isNumber()) {
			return checkIndex(analysis, layout.asInteger(0, Integer.MAX_VALUE), layout.path());
		}
		if (layout.isString()) {
			return indexOfName(analysis, layout.asString(), layout.path());
		}
		throw layout.invalid(I18NConstants.EXPECTED_LAYOUT);
	}

	/**
	 * Resolves a reference to a slide layout of the template.
	 *
	 * @param analysis
	 *        The analysis of the template.
	 * @param layout
	 *        The reference, <code>null</code> for the layout of the role
	 *        {@link LayoutRole#CONTENT}.
	 * @param path
	 *        The path of the reference in the presentation description, for error messages.
	 * @return The index of the layout in {@link TemplateAnalysis#layouts()}.
	 * @throws TopLogicException
	 *         If the template has no such layout.
	 */
	public int resolve(TemplateAnalysis analysis, LayoutRef layout, String path) {
		if (layout == null) {
			return resolveRole(LayoutRole.CONTENT, path);
		}
		if (layout instanceof LayoutRef.Index index) {
			return checkIndex(analysis, index.index(), path);
		}
		if (layout instanceof LayoutRef.Name name) {
			return indexOfName(analysis, name.name(), path);
		}
		return resolveRole(((LayoutRef.Role) layout).role(), path);
	}

	private int resolveRole(LayoutRole role, String path) {
		Integer result = layouts.get(role);
		if (result == null) {
			List<String> available = new ArrayList<>();
			for (LayoutRole defined : layouts.keySet()) {
				available.add(defined.id());
			}
			throw new TopLogicException(I18NConstants.ERROR_NO_LAYOUT_FOR_ROLE__PATH_ROLE_ROLES.fill(path, role.id(),
				String.join(", ", available)));
		}
		return result.intValue();
	}

	private static int checkIndex(TemplateAnalysis analysis, int index, String path) {
		int count = analysis.layouts().size();
		if (index >= count) {
			throw new TopLogicException(
				I18NConstants.ERROR_NO_SUCH_LAYOUT_INDEX__PATH_INDEX_COUNT.fill(path, index, count));
		}
		return index;
	}

	private static int indexOfName(TemplateAnalysis analysis, String name, String path) {
		LayoutInfo layout = analysis.layout(name);
		if (layout == null) {
			List<String> names = new ArrayList<>();
			for (LayoutInfo candidate : analysis.layouts()) {
				names.add(candidate.name());
			}
			throw new TopLogicException(
				I18NConstants.ERROR_NO_SUCH_LAYOUT_NAME__PATH_NAME_NAMES.fill(path, name, String.join(", ", names)));
		}
		return layout.index();
	}

	/**
	 * The properties of the layout with the given index.
	 *
	 * @return The properties, <code>null</code> if the profile does not describe the layout.
	 */
	public LayoutProfile slideLayout(int index) {
		return slideLayouts.get(Integer.valueOf(index));
	}

	/**
	 * The data representation with the keys {@link PresentationData#LAYOUTS} (the layout index for
	 * each {@link LayoutRole#id() role}), {@link PresentationData#SLIDE_LAYOUTS} (the
	 * {@link LayoutProfile#toData() layout properties} by the layout index) and
	 * {@link PresentationData#ROLES} (the {@link ColorSpec#toData() color} for each color role).
	 */
	public Map<String, Object> toData() {
		Map<String, Object> result = PresentationData.map();
		Map<String, Object> layoutData = PresentationData.map();
		for (Map.Entry<LayoutRole, Integer> entry : layouts.entrySet()) {
			layoutData.put(entry.getKey().id(), Double.valueOf(entry.getValue().intValue()));
		}
		result.put(LAYOUTS, layoutData);
		Map<String, Object> slideLayoutData = PresentationData.map();
		for (Map.Entry<Integer, LayoutProfile> entry : slideLayouts.entrySet()) {
			slideLayoutData.put(entry.getKey().toString(), entry.getValue().toData());
		}
		result.put(SLIDE_LAYOUTS, slideLayoutData);
		Map<String, Object> roleData = PresentationData.map();
		for (Map.Entry<String, ColorSpec> entry : roles.entrySet()) {
			roleData.put(entry.getKey(), entry.getValue().toData());
		}
		result.put(ROLES, roleData);
		return result;
	}

}
