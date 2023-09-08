/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.constraints;

import java.util.regex.Pattern;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.form.CheckException;
import com.top_logic.layout.form.I18NConstants;
import com.top_logic.util.Resources;

/**
 * A Constraint that uses a regular expression to match against the user's input.
 * 
 * @author     <a href="mailto:tma@top-logic.com">tma</a>
 */
public class RegExpConstraint extends AbstractConstraint {
	
	private final String regExp;

	private final ResKey key;

	/**
	 * Constructs a {@link RegExpConstraint}, which uses the given regular expression to match
	 * against the value (see {@link #check(Object)}).
	 * 
	 * <p>
	 * The key {@link I18NConstants#INPUT_DID_NOT_MATCH_PATTERN__PATTERN} is used to lookup the
	 * internationalized error message.
	 * </p>
	 * 
	 * @param regExp
	 *        the regular expression
	 */
	public RegExpConstraint(String regExp) {
		this(regExp, I18NConstants.INPUT_DID_NOT_MATCH_PATTERN__PATTERN.fill(regExp));
	}

	/**
	 * Constructs a RegExConstraint which uses the given regular expression to match 
	 * against the value (see {@link #check(Object)}). The key is used to lookup an internationalized 
	 * error message.
	 * 
	 * @param regExp the regular expression
	 */
	public RegExpConstraint(String regExp, ResKey key) {
		this.regExp = regExp;
		this.key = key;
	}
	
	/** 
	 * Checks if the regular expression matches the given input value. Also accepts the empty string.
	 * 
	 * @see com.top_logic.layout.form.Constraint#check(java.lang.Object)
	 */
	@Override
	public boolean check(Object value) throws CheckException {
		
		CharSequence sequence = (CharSequence) value;
		
		if ((sequence == null) || (sequence.length() == 0)) {
			// Note: An specialized constraint must not imply a mandatory constraint.
			return true;
		}
		
		if (Pattern.matches(this.regExp,sequence)) {
			return true;
		} else {
			String msg = Resources.getInstance().getString(key);
			throw new CheckException(msg);
		}
	}

}
