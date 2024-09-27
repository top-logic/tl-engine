/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.themeedit.browser.providers;

import static com.top_logic.mig.html.HTMLConstants.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.gui.ThemeFactory;
import com.top_logic.gui.ThemeVar;
import com.top_logic.gui.ThemeVar.TemplateVar;
import com.top_logic.gui.config.ThemeSetting;
import com.top_logic.gui.config.ThemeSetting.Config;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.WithPropertiesDelegate;
import com.top_logic.layout.basic.WithPropertiesDelegate.Property;
import com.top_logic.layout.basic.WithPropertiesDelegateFactory;
import com.top_logic.layout.basic.fragments.Fragments;
import com.top_logic.layout.form.Collapsible;
import com.top_logic.layout.form.boxes.reactive_tag.DefaultGroupSettings;
import com.top_logic.layout.form.boxes.reactive_tag.GroupCellControl;
import com.top_logic.layout.form.boxes.tag.PersonalizedExpansionModel;
import com.top_logic.layout.table.ConfigKey;
import com.top_logic.layout.template.WithProperties;
import com.top_logic.layout.tooltip.HtmlToolTip;
import com.top_logic.util.Resources;

/**
 * Maps a {@link ThemeSetting} to a copy of his config.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class ThemeSettingConfigCopyMapper implements Function<Object, ConfigurationItem> {

	private static final ConfigKey PROPERTY_DOCU_KEY = ConfigKey.named(ThemeSettingConfigCopyMapper.class.getName());

	@Override
	public ConfigurationItem apply(Object object) {
		ThemeSetting themeSetting = (ThemeSetting) object;
		Config<?> themeSettingConfig = themeSetting.getConfig();

		Config<?> setting = TypedConfiguration.copy(themeSettingConfig);
		SettingForm form = TypedConfiguration.newConfigItem(SettingForm.class);

		form.setSetting(setting);

		form.setDocumentation((context, out) -> {
			writeDocumentation(context, out, themeSetting);
		});

		return form;
	}

	private void writeDocumentation(DisplayContext context, TagWriter out, ThemeSetting themeSetting)
			throws IOException {
		String varName = themeSetting.getName();

		int sepIdx = varName.lastIndexOf('.');

		String localName;
		if (sepIdx >= 0) {
			localName = varName.substring(sepIdx + 1);
		} else {
			localName = varName;
		}

		out.beginTag(H2);
		out.write(localName);
		out.endTag(H2);

		Resources resources = context.getResources();
		if (sepIdx >= 0) {
			out.beginTag(H3);
			out.write(varName.substring(0, sepIdx));
			out.endTag(H3);

			ResKey helpHtml = ResKey.internalCreate(varName).fallback(ResKey.text(null));
			out.writeContent(HtmlToolTip.ensureSafeHTMLTooltip(context, helpHtml));
		}

		ThemeVar<?> themeVar = ThemeFactory.getInstance().getDeclaredVars().get(themeSetting.getName());
		if (themeVar instanceof TemplateVar) {
			Class<? extends WithProperties> modelType = ((TemplateVar) themeVar).getModelType();
			if (modelType != null) {
				WithPropertiesDelegate delegate = WithPropertiesDelegateFactory.lookup(modelType);
				Collection<? extends Property> availablePropertyInstances =
					delegate.getAvailablePropertyInstances();
				if (!availablePropertyInstances.isEmpty()) {
					List<Property> properties = new ArrayList<>(availablePropertyInstances);
					Collections.sort(properties, (p1, p2) -> p1.getPropertyName().compareTo(p2.getPropertyName()));

					Collapsible collapsible = new PersonalizedExpansionModel(PROPERTY_DOCU_KEY);

					GroupCellControl group =
						new GroupCellControl((c, o) -> writePropertyDocumentation(c, o, properties), collapsible,
							new DefaultGroupSettings().setColumns(1));
					group.setTitle(Fragments.message(I18NConstants.AVAILABLE_PROPERTIES));
					group.write(context, out);
				}
			}
		}
	}

	private void writePropertyDocumentation(DisplayContext context, TagWriter out,
			Collection<? extends Property> properties) throws IOException {
		Resources resources = context.getResources();

		out.beginTag(DL);
		for (Property property : properties) {
			out.beginTag(DT);
			out.beginTag(CODE);
			out.write(property.getPropertyName());
			out.endTag(CODE);
			out.endTag(DT);

			ResKey documentation = property.getDocumentation().fallback(ResKey.text(""));
			out.beginTag(DD);
			out.writeContent(HtmlToolTip.ensureSafeHTMLTooltip(context, documentation));
			out.endTag(DD);
		}
		out.endTag(DL);
	}

}
