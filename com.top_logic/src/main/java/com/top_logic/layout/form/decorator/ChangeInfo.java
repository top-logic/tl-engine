/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.decorator;

import java.awt.Color;

import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.tool.export.ExcelExportSupport;

/**
 * Information about the change type of an object.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public enum ChangeInfo {

	/**
	 * Constant indicating that the object is unchanged.
	 */
	NO_CHANGE {

		@Override
		String getCSSClass() {
			return "changeInfoNoChange";
		}

		@Override
		ResKey getI18NKey() {
			return I18NConstants.CHANGE_INFO_NO_CHANGE;
		}

		@Override
		ThemeImage getImage() {
			return Icons.UNCHANGED;
		}

		@Override
		Color getExcelColor() {
			return null;
		}

	},

	/**
	 * Constant indicating that the object was created.
	 */
	CREATED {

		@Override
		String getCSSClass() {
			return "changeInfoCreated";
		}

		@Override
		ResKey getI18NKey() {
			return I18NConstants.CHANGE_INFO_CREATED;
		}

		@Override
		ThemeImage getImage() {
			return Icons.CREATED;
		}

		@Override
		Color getExcelColor() {
			return Color.GREEN;
		}

	},

	/**
	 * Constant indicating that the object was removed.
	 */
	REMOVED {

		@Override
		String getCSSClass() {
			return "changeInfoRemoved";
		}

		@Override
		ResKey getI18NKey() {
			return I18NConstants.CHANGE_INFO_REMOVED;
		}

		@Override
		ThemeImage getImage() {
			return Icons.REMOVED;
		}

		@Override
		Color getExcelColor() {
			return Color.RED;
		}

	},

	/**
	 * Constant indicating that some simple properties of the object have changed.
	 * 
	 * @see ChangeInfo#DEEP_CHANGED
	 */
	CHANGED {

		@Override
		String getCSSClass() {
			return "changeInfoChanged";
		}

		@Override
		ResKey getI18NKey() {
			return I18NConstants.CHANGE_INFO_CHANGED;
		}

		@Override
		ThemeImage getImage() {
			return Icons.CHANGED;
		}

		@Override
		Color getExcelColor() {
			return Color.BLUE;
		}

	},

	/**
	 * Constant indicating that the structure of the object has changed.
	 * 
	 * @see ChangeInfo#CHANGED
	 */
	DEEP_CHANGED {

		@Override
		String getCSSClass() {
			return "changeInfoDeepChanged";
		}

		@Override
		ResKey getI18NKey() {
			return I18NConstants.CHANGE_INFO_DEEP_CHANGED;
		}

		@Override
		ThemeImage getImage() {
			return Icons.DEEP_CHANGED;
		}

		@Override
		Color getExcelColor() {
			return ExcelExportSupport.LIGHT_BLUE;
		}

	};

	/**
	 * @see CompareInfo#getChangedCSSClass()
	 */
	abstract String getCSSClass();

	/**
	 * @see CompareInfo#getI18NKey()
	 */
	abstract ResKey getI18NKey();

	/**
	 * @see CompareInfo#getChangedIcon()
	 */
	abstract ThemeImage getImage();

	/**
	 * @see CompareInfo#getExcelColor()
	 */
	abstract Color getExcelColor();

	/**
	 * Service method to use in default case in switch blocks to assert that all cases are covered.
	 * 
	 * @param o
	 *        The {@link ChangeInfo} that is not covered.
	 */
	public static UnreachableAssertion noSuchChangeInfo(ChangeInfo o) {
		throw new UnreachableAssertion("There is no ChangeInfo:" + o);
	}

}
