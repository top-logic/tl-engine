/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.ConfigUtil;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.element.meta.AttributeException;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.BoundChecker;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.BoundHelper;
import com.top_logic.tool.boundsec.BoundObject;
import com.top_logic.tool.boundsec.simple.CommandGroupRegistry;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;

/**
 * Check security based on one {@link BoundChecker}.
 * 
 * @author <a href="mailto:mga@top-logic.com>Michael Gänsler</a>
 */
public class SecurityFilter<C extends BoundObject> extends BasicCollectionFilter<C> {

	/** Filter sort order (according to efficiency; high value means slow). */
	protected static final Integer SORT_ORDER = Integer.valueOf(200);

	/** Key for command group name persistence. */
	public static final String COMMAND_GROUP_KEY = "cmd_grp";

	/** Key for bound checker persistence. */
	public static final String BOUND_CHECKER_KEY = "bound_checker";

	private BoundChecker _checker;

	private String _commandGroupName;

	private BoundCommandGroup _commandGroup;

	/**
	 * Default CTor (no negation).
	 * 
	 * @param aChecker
	 *        The checker to be used.
	 * @throws IllegalArgumentException
	 *         if aChecker is <code>null</code>.
	 */
	public SecurityFilter(BoundChecker aChecker) throws IllegalArgumentException {
		this(aChecker, null, false, true);
	}

	/**
	 * Create a SecurityFilter.
	 * 
	 * @param aChecker
	 *        The checker to be used, if <code>null</code> the {@link #getDefaultChecker()} will be
	 *        used.
	 * @param aCmdGrpName
	 *        The command group to be checked, if <code>null</code>
	 *        {@link #getDefaultCommandGroupName()} will be used.
	 * @param doNegate
	 *        the negation flag.
	 * @param isRelevant
	 *        the relevant flag.
	 * @throws IllegalArgumentException
	 *         if aChecker is <code>null</code>.
	 */
	public SecurityFilter(BoundChecker aChecker, String aCmdGrpName, boolean doNegate, boolean isRelevant)
			throws IllegalArgumentException {
		super(doNegate, isRelevant);

		if (aChecker == null) {
			if (this.useDefaultChecker()) {
				_checker = this.getDefaultChecker();
			}
		} else {
			_checker = aChecker;
		}
		_commandGroupName = (aCmdGrpName != null) ? aCmdGrpName : this.getDefaultCommandGroupName();

		if (_commandGroupName == null || this.getCommandGroup() == null) {
			throw new IllegalArgumentException("Missing command group for SecurityFilter.");
		}
	}

	/**
	 * CTor that gets all values from a map (used for StoredQuery setup in
	 * {@link #setUpFromValueMap(Map)}).
	 * 
	 * @param aValueMap
	 *        the Map with all values. Must not be <code>null</code>.
	 * 
	 * @throws IllegalArgumentException
	 *         if the map is <code>null</code> or some of its values do not match the filter's
	 *         constraints.
	 */
	public SecurityFilter(Map<String, Object> aValueMap) throws IllegalArgumentException {
		super(aValueMap);
	}

	@Override
	public Integer getSortOrder() {
		return SORT_ORDER; // We are a quite inefficient filter....
	}

	@Override
	public Collection<C> filter(Collection<C> aCollection) throws NoSuchAttributeException, AttributeException {
		if (!this.isRelevant()) {
			return aCollection;
		} else {
			List<C> theResult = new ArrayList<>(aCollection.size());
			BoundCommandGroup theCommand = this.getCommandGroup();

			for (C theElement : aCollection) {
				if (this.getChecker(theElement, theCommand).allow(theCommand, theElement)) {
					theResult.add(theElement);
				}
			}

			return theResult;
		}
	}

	@Override
	public Map<String, Object> getValueMap() {
		Map<String, Object> theMap = super.getValueMap();
		if (_checker != null) {
			boolean layoutChecker = _checker instanceof LayoutComponent;
			boolean defaultChecker = useDefaultChecker() || _checker == this.getDefaultChecker();

			if (!layoutChecker && !defaultChecker) {
				theMap.put(BOUND_CHECKER_KEY, _checker.getClass().getName());
			}
		}

		theMap.put(COMMAND_GROUP_KEY, _commandGroupName);

		return theMap;
	}

	@Override
	public void setUpFromValueMap(Map aValueMap) throws IllegalArgumentException {
		super.setUpFromValueMap(aValueMap);

		// Set up security checker
		String theCheckerName = (String) aValueMap.get(BOUND_CHECKER_KEY);

		if (StringServices.isEmpty(theCheckerName)) {
			_checker = getDefaultChecker();
		} else {
			try {
				_checker = ConfigUtil.getInstanceMandatory(BoundChecker.class, BOUND_CHECKER_KEY, theCheckerName);
			} catch (Exception ex) {
				throw new IllegalArgumentException("Cannot instantiate bound checker '" + theCheckerName + "'!", ex);
			}
		}

		// Set up command group
		_commandGroupName = (String) aValueMap.get(COMMAND_GROUP_KEY);

		if (_commandGroupName == null || this.getCommandGroup() == null) {
			throw new IllegalArgumentException("Missing command group for SecurityFilter.");
		}
	}

	/**
	 * Return the checker for reading the given object.
	 * 
	 * @param anObject
	 *        The object to get the checker for.
	 * @return The requested checker.
	 */
	protected BoundChecker getChecker(BoundObject anObject) {
		return this.getChecker(anObject, SimpleBoundCommandGroup.READ);
	}

	/**
	 * Return the checker for accessing the given object.
	 * 
	 * @param anObject
	 *        The object to get the checker for.
	 * @param aBCG
	 *        The requested command group.
	 * @return The requested checker.
	 */
	protected BoundChecker getChecker(BoundObject anObject, BoundCommandGroup aBCG) {
		if (_checker != null) {
			return _checker;
		}

		BoundChecker theChecker;

		if (this.useDefaultChecker()) {
			theChecker = this.getDefaultChecker();
		} else {
			theChecker = CollectionUtil.getFirst(BoundHelper.getInstance().getDefaultCheckers(anObject, null));
		}

		if (this.useFirstMatchingChecker()) {
			_checker = theChecker;
		}

		return theChecker;
	}

	/**
	 * Check if implementation should use the {@link #getDefaultChecker()} when no other checker
	 * defined.
	 * 
	 * @return <code>true</code> always in this implementation.
	 */
	protected boolean useDefaultChecker() {
		return true;
	}

	/**
	 * Flag for performance to keep the first checker found when iterating on a collection of
	 * objects.
	 * 
	 * <p>
	 * Attention: Change this flag only, when you can be sure, that the instance is used local. When
	 * using it as singleton, it might be better to provide the checker to be used in the
	 * constructor.
	 * </p>
	 * 
	 * @return <code>false</code> always in this implementation.
	 */
	protected boolean useFirstMatchingChecker() {
		return false;
	}

	/**
	 * Return the root checker.
	 * 
	 * @return The root checker.
	 * @see BoundHelper#getRootChecker()
	 */
	protected BoundChecker getDefaultChecker() {
		return BoundHelper.getInstance().getRootChecker();
	}

	/**
	 * The default read command group.
	 * @see SimpleBoundCommandGroup#READ
	 */
	protected String getDefaultCommandGroupName() {
		return SimpleBoundCommandGroup.READ.getID();
	}

	/**
	 * Return the command group this filter is working on.
	 * 
	 * @return The requested command group.
	 * @see #_commandGroupName
	 */
	protected BoundCommandGroup getCommandGroup() {
		if (_commandGroup == null) {
			_commandGroup = CommandGroupRegistry.resolve(_commandGroupName);
		}

		return _commandGroup;
	}

}
