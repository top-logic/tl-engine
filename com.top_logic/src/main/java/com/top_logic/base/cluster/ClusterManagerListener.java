/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.cluster;

/**
 * Instances of this class can listen for changes of values of cluster wide global properties.
 * Listeners are attached
 *
 * @author <a href="mailto:Christian.Braun@top-logic.com">Christian Braun</a>
 */
public interface ClusterManagerListener {

    /**
     * Informs the listener that a previous value change of the given property has been
     * confirmed by all node in the cluster
     *
     * @param propertyName
     *        the name of the property whose value change has been confirmed by all nodes in
     *        the cluster
     * @param currentValue
     *        the current value of the property, which is known by all nodes in the cluster
     */
    public void clusterPropertyChangeConfirmed(String propertyName, Object currentValue);

    /**
     * Informs the listener about a change of the value of the given property from a node in
     * the cluster. This change may be not confirmed by all node in the cluster yet. The
     * {@link #clusterPropertyChangeConfirmed(String, Object)} method will be called after
     * all node in cluster have confirmed this change.<br/>
     * <br/>
     * Note: If a property gets overwritten while the last value change of this property was
     * not confirmed, it is possible that the first value change will not be propagated by
     * this method. For example:<br/>
     * <br/>
     * <code>
     * The Value of a property is X.<br/>
     * Node A changes the value of this property to Y.<br/>
     * Node B changes the value of this property to Z.<br/>
     * This node detects the value change and informs this listener only once about
     * the complete change from X to Z.<br/>
     * </code>
     *
     * @param propertyName
     *        the name of the property whose values has been changed
     * @param oldValue
     *        the old value of the property
     * @param newValue
     *        the new value of the property
     * @param changedByThisNode
     *        flag indicating whether the change was triggered by this node
     *        (<code>true</code>) or by another node (<code>false</code>)
     */
    public void clusterPropertyChanged(String propertyName, Object oldValue, Object newValue, boolean changedByThisNode);

}
