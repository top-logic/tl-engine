/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.form.component;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.Collection;
import java.util.Set;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.Logger;
import com.top_logic.basic.col.Filter;
import com.top_logic.basic.col.FilterUtil;
import com.top_logic.basic.col.filter.FilterFactory;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.table.component.BuilderComponent;
import com.top_logic.layout.table.component.TableComponent;
import com.top_logic.mig.html.ListModelBuilder;
import com.top_logic.mig.html.ListModelBuilderProxy;
import com.top_logic.mig.html.ModelBuilder;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.MainLayout;
import com.top_logic.tool.boundsec.BoundChecker;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;
import com.top_logic.tool.boundsec.util.BoundSecurityFilter;
import com.top_logic.tool.execution.I18NConstants;

/**
 * The SecuredTypeTableComponent is a {@link TableComponent} which shows only objects which are
 * visible for the current user by the security.
 *
 * @author <a href="mailto:Christian.Braun@top-logic.com">Christian Braun</a>
 */
public class SecuredTypeTableComponent extends TableComponent {

	public interface Config extends TableComponent.Config {

		/** @see com.top_logic.basic.reflect.DefaultMethodInvoker */
		Lookup LOOKUP = MethodHandles.lookup();

		@Name(XML_CONF_KEY_CHECKER_NAME)
		ComponentName getCheckerName();

		@Override
		default void addAdditionalCommandGroups(Set<? super BoundCommandGroup> additionalGroups) {
			TableComponent.Config.super.addAdditionalCommandGroups(additionalGroups);
			additionalGroups.add(SimpleBoundCommandGroup.CREATE);
		}

	}

	/** Configuration name for the security checker. */
    public static final String XML_CONF_KEY_CHECKER_NAME = "checkerName";


    /**
     * Creates a new instance of this class.
     */
    public SecuredTypeTableComponent(InstantiationContext context, Config aAttr) throws ConfigurationException {
        super(context, aAttr);
    }

    @Override
	public ResKey hideReason(Object potentialModel) {
        // This code assumes that a security filter is set for the table so
        // that the table has only entries if the current user is allowed to
        // see at least one object
		BoundCommandGroup group = SimpleBoundCommandGroup.CREATE;
		if (getTableModel().getRowCount() == 0 && !allow(group, getCurrentObject(group, potentialModel))) {
			return I18NConstants.ERROR_NO_PERMISSION;
		}
		return null;
    }


    @Override
	protected ModelBuilder createBuilder(InstantiationContext context, BuilderComponent.Config aAttr)
			throws ConfigurationException {
        ListModelBuilder theBuilder = (ListModelBuilder) super.createBuilder(context, aAttr);
		ComponentName checkerName = ((Config) aAttr).getCheckerName();
		return new SecuredModelBuilder(theBuilder, checkerName);
    }

    /**
     * Filters the model of the inner model builder and retains only this objects
     * which are allowed by security.
     *
     * @author <a href=mailto:Christian.Braun@top-logic.com>Christian Braun</a>
     */
	public static class SecuredModelBuilder extends ListModelBuilderProxy {

        /** The security filter. will be created on demand, not in the constructor. */
        private Filter filter;

		private ComponentName checkerName;

        /**
         * Creates a new instance of this class.
         *
         * @param aBuilder the inner model builder to use
         */
		public SecuredModelBuilder(ListModelBuilder aBuilder, ComponentName aCheckerName) {
			super(aBuilder);
            this.checkerName = aCheckerName;
        }


        @Override
		public Collection<?> getModel(Object businessModel, LayoutComponent aComponent) {
			Object theModel = super.getModel(businessModel, aComponent);
            Filter theFilter = getFilter(aComponent.getMainLayout());
			if (theModel instanceof Iterable) {
				return FilterUtil.filterList(theFilter, ((Iterable<?>) theModel));
            }
			return (theFilter.accept(theModel) ? CollectionUtil.singletonOrEmptyList(theModel) : null);
        }

        /**
         * Creates the security filter to filter the model with.
         *
         * @return the security filter to use
         */
        private Filter getFilter(MainLayout aMainLayout) {
            if (filter == null) {
				if (checkerName == null) {
                    filter = new BoundSecurityFilter();
                }
                else {
                    LayoutComponent theComponent = aMainLayout.getComponentByName(checkerName);
                    if (theComponent == null) {
                        Logger.error("The configured component '" + checkerName + "' was not found.", this);
                        filter = FilterFactory.falseFilter();
                    }
                    else if (!(theComponent instanceof BoundChecker)) {
                        Logger.error("The configured component '" + checkerName + "' is not a BoundChecker.", this);
                        filter = FilterFactory.falseFilter();
                    }
                    else {
                        filter = new BoundSecurityFilter((BoundChecker)theComponent);
                    }
                }
            }
            return filter;
        }

    }

}
