/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.control;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.col.TupleFactory;
import com.top_logic.basic.col.TupleFactory.Tuple;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.gui.Theme;
import com.top_logic.gui.ThemeFactory;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.Flavor;
import com.top_logic.layout.basic.ControlCommand;
import com.top_logic.layout.form.FormConstants;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.model.ComplexField;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.tooltip.OverlibTooltipFragmentGenerator;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.util.FormFieldHelper;
import com.top_logic.util.css.CssUtil;

/**
 * Displays FormFields as beacon.
 *
 * @author <a href=mailto:Christian.Braun@top-logic.com>Christian Braun</a>
 */
public class BeaconControl extends AbstractSelectControl {

	/**
	 * Custom attribute storing the icon URL to the selected version of the icon.
	 */
	private static final String DATA_SELECTED_IMAGE = HTMLConstants.DATA_ATTRIBUTE_PREFIX + "selected-image";
	
	/**
	 * Custom attribute storing the icon URL to the option version of the icon (the state if the
	 * icon is not the selected one).
	 */
	private static final String DATA_OPTION_IMAGE = HTMLConstants.DATA_ATTRIBUTE_PREFIX + "option-image";

	/**
	 * CSS class to add to icons that are not currently selected ones.
	 */
	private static final String OPTION_CSS_CLASS = "option";

	protected static final Map<String, ControlCommand> COMMANDS = createCommandMap(AbstractFormFieldControl.COMMANDS, new ControlCommand[] { });
	
    /** Map of inconConfigurations, indexed by Type */
    private static final Map<Tuple, IconConfig> iconConfigs = new HashMap<>();

	private String type;

	/**
	 * Creates a new {@link BeaconControl}.
	 */
	public BeaconControl(SelectField model) {
		super(model, COMMANDS);
	}
	
	/**
	 * Creates a new {@link BeaconControl}.
	 */
	public BeaconControl(SelectField model, String type) {
		super(model, COMMANDS);
		this.type = type;
	}

	/**
	 * Creates a new {@link BeaconControl}.
	 */
	public BeaconControl(ComplexField model) {
		super(model, COMMANDS);
	}

	/**
	 * Creates a new {@link BeaconControl}.
	 */
	public BeaconControl(ComplexField model, String type) {
		super(model, COMMANDS);
		this.type = type;
	}

	/**
	 * Sets the icon type.
	 */
	public void setType(String newType) {
		boolean changed = ! StringServices.equals(newType, type);
		
		this.type = newType;
		
		if (changed) {
			requestRepaint();
		}
	}

	@Override
	protected String getTypeCssClass() {
		return "cBeacon";
	}

	@Override
	protected void writeEditable(DisplayContext context, TagWriter out) throws IOException {
        FormField field = getFieldModel();
        int selected = getSelectedIndex(field);

        SelectField sField = field instanceof SelectField ? (SelectField)field : null;
        List options = sField == null ? null : sField.getOptions();
        boolean asArray = sField != null;

		out.beginBeginTag(SPAN);
		writeControlAttributes(context, out);
        out.endBeginTag();
        
        String contextPath = context.getContextPath();
		IconConfig icons = getIconConfig(type);

		boolean disabled = field.isDisabled();
		for (int i = 0, length = getIconCount(icons); i < length; i++) {
        	String optionID = sField != null ? sField.getOptionID(options.get(i)) : Integer.toString(i);
			ResKey optionLabel = getOptionLabel(context, i);
        	boolean isSelected = selected == i;
			String optionIcon = contextPath + getInactiveIcon(icons, i);
			String selectedIcon = contextPath + getSelectedIcon(icons, i);

			out.beginBeginTag(SPAN);
			OverlibTooltipFragmentGenerator.INSTANCE.writeTooltipAttributesPlain(context, out, optionLabel);
			out.endBeginTag();
			
            out.beginBeginTag(INPUT);
            out.writeAttribute(TYPE_ATTR, IMAGE_TYPE_VALUE);
            out.writeAttribute(NAME_ATTR, optionID);
            CssUtil.writeCombinedCssClasses(out, 
            	FormConstants.INPUT_IMAGE_CSS_CLASS, 
            	isSelected ? null : OPTION_CSS_CLASS, 
            	disabled ? FormConstants.DISABLED_CSS_CLASS : null);
            out.writeAttribute(SRC_ATTR, isSelected ? selectedIcon : optionIcon);
			out.writeAttribute(DATA_SELECTED_IMAGE, selectedIcon);
			out.writeAttribute(DATA_OPTION_IMAGE, optionIcon);
			out.writeAttribute(ALT_ATTR, StringServices.nonNull(context.getResources().getStringOptional(optionLabel)));
			out.writeAttribute(TITLE_ATTR, "");
			writeOnClick(out, FormConstants.BEACON_HANDLER_CLASS, this, "," + asArray);
			if (disabled) {
				out.writeAttribute(DISABLED_ATTR, DISABLED_DISABLED_VALUE);
			}
			out.endEmptyTag();
			
			out.endTag(SPAN);
        }

        out.endTag(SPAN);
	}

	private int getIconCount(IconConfig icons) {
		int emptyIcon = 1;
		return icons.size() + emptyIcon;
	}

	private ResKey getOptionLabel(DisplayContext context, int optionIndex) {
		return ResKey.legacy("beacon_" + type + '_' + optionIndex);
	}

	private String getInactiveIcon(IconConfig icons, int optionIndex) {
		return getIcon(icons, optionIndex, Flavor.DISABLED);
	}

	private String getSelectedIcon(IconConfig icons, int optionIndex) {
		return getIcon(icons, optionIndex, Flavor.DEFAULT);
	}

	private String getIcon(IconConfig icons, int optionIndex, Flavor flavor) {
		if (optionIndex > 0) {
			return icons.getIcon(optionIndex - 1, flavor);
		} else {
			return icons.getEmptyIcon(flavor);
		}
	}

	@Override
	protected void writeImmutable(DisplayContext context, TagWriter out) throws IOException {
        FormField field = getFieldModel();
        int selected = getSelectedIndex(field);

		out.beginBeginTag(SPAN);
		writeControlAttributes(context, out);
        out.endBeginTag();
        
        String contextPath = context.getContextPath();
		IconConfig icons = getIconConfig(type);
        		
		out.beginBeginTag(IMG);
		ResKey optionLabel = getOptionLabel(context, selected);
		out.writeAttribute(SRC_ATTR, contextPath + getSelectedIcon(icons, selected));
		out.writeAttribute(ALT_ATTR, context.getResources().getStringOptional(optionLabel));
					out.writeAttribute(TITLE_ATTR, "");
					OverlibTooltipFragmentGenerator.INSTANCE.writeTooltipAttributes(context, out, optionLabel);
        			out.endEmptyTag();
        
        out.endTag(SPAN);
	}

	@Override
	public void internalHandleDisabledEvent(FormMember sender, Boolean oldValue, Boolean newValue) {
		requestRepaint();
	}

	@Override
	protected void internalHandleValueChanged(FormField field, Object oldValue, Object newValue) {
		requestRepaint();
	}

	
    /**
	 * Returns the selected index of the beacon.
	 * 
	 * @param field
	 *        the field holding the selected value.
	 * @return the selected index of the beacon or 0, if nothing is selected.
	 */
	protected int getSelectedIndex(FormField field) {
    	if (field instanceof SelectField) {
    		SelectField selectField = (SelectField)field;
			Object selection = selectField.getSingleSelection();
    		List options = selectField.getOptions();
    		if (!CollectionUtil.isEmptyOrNull((Collection)selectField.getValue()) && options != null) {
				return Math.max(0, options.indexOf(selection));
    		}
    	}
    	else {
    		Integer value = FormFieldHelper.getIntegerValue(field);
			if (value != null) {
				return Math.max(0, value.intValue());
			}
    	}
		return 0;
	}


    /**  
     * Lazy accessor to an IconConfig for the given type.
     * 
     * @param aType arbitrary name, must be found in Themes mapping properties.
     */
    public static IconConfig getIconConfig(String aType) {
        
        IconConfig result;
        synchronized (iconConfigs) {
			Theme theTheme = ThemeFactory.getTheme();
			Tuple theKey = TupleFactory.newTuple(aType, theTheme);
			result = iconConfigs.get(theKey);
            if (result == null) {
				result = createIconConfig(aType, theTheme);
				iconConfigs.put(theKey, result);
            }
        }
        return result;
    }
    
    /**
	 * Create a IconConfig for given Type via ThemeFactory.
	 * 
	 * The config for the beacon images are found in the 
	 * themes/.../styles/mapping.properties
	 * 
	 * Warning: is called while synchronized to inconConfigs.
	 * 
	 * @return null on any error.
	 */
	private static IconConfig createIconConfig(String aType, Theme theTheme) {
		try {
			int num = theTheme.getIntValue(aType + "_NUM");
			IconConfig result = new IconConfig(num, getEmptyIcons(theTheme, aType));
			for (int i = 1; i <= num; i++) {
				result.add(getValueIcons(theTheme, aType, i));
			}

			return result;
		} catch (Exception any) {
			Logger.error("Failed to createIconConfig for'" + aType + "'", any, BeaconControl.class);
		}
		return null;
	}

	private static String[] getEmptyIcons(Theme theTheme, String aType) {
		return getIcons(theTheme, aType, "EMPTY");
	}

	private static String[] getValueIcons(Theme theTheme, String aType, int index) {
		return getIcons(theTheme, aType, String.valueOf(index));
	}

	private static String[] getIcons(Theme theTheme, String aType, String id) {
		String defaultIcon = getActiveIcon(theTheme, aType, id);
		String inactiveIcon = getInactiveIcon(theTheme, aType, id);
		return new String[] { defaultIcon, inactiveIcon };
	}

	private static String getActiveIcon(Theme theTheme, String aType, String id) {
		return theTheme.getFileLink(theTheme.getRawValue(aType + "_" + id));
	}

	private static String getInactiveIcon(Theme theTheme, String aType, String id) {
		String beaconIconPath = getActiveIcon(theTheme, aType, id);
		String suffix = beaconIconPath.substring(beaconIconPath.lastIndexOf("."));
		String disabledBeaconIcon = beaconIconPath.substring(0, beaconIconPath.lastIndexOf("."));
		return disabledBeaconIcon.concat("_inactive").concat(suffix);
	}

    
    /**
	 * Configures how a set of Icons will look like.
	 */
	public static class IconConfig extends ArrayList<String[]> {
        
		/** The icons to show when nothing is selected */
		private String[] emptyIcons;

        /**
		 * @param initialCapacity
		 *        the initial capacity of the list.
		 * @param emptyIcons
		 *        - icons to show when an entry is not selected.
		 */
		public IconConfig(int initialCapacity, String[] emptyIcons) {
            super(initialCapacity);
			this.emptyIcons = emptyIcons;
        }

		/**
		 * @param index
		 *        - icon index
		 * @param iconType
		 *        - whether {@link Flavor#DEFAULT} or {@link Flavor#DISABLED}
		 * @return path to icon at specified index of specified icon type.
		 */
		public String getIcon(int index, Flavor iconType) {
			return getFlavoredIcon(super.get(index), iconType);
		}

		private String getFlavoredIcon(String[] icons, Flavor iconType) {
			if (iconType.equals(Flavor.DEFAULT)) {
				return icons[0];
			} else {
				return icons[1];
			}
		}

		/**
		 * Name of Icon to use for empty
		 * 
		 * @param iconType
		 *        - whether {@link Flavor#DEFAULT} or {@link Flavor#DISABLED}
		 **/
		public String getEmptyIcon(Flavor iconType) {
			return getFlavoredIcon(emptyIcons, iconType);
        }
    }

}