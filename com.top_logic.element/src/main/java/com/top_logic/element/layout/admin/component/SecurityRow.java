/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.admin.component;

import static com.top_logic.knowledge.search.ExpressionFactory.*;

import java.util.List;

import com.top_logic.basic.Logger;
import com.top_logic.basic.LongID;
import com.top_logic.basic.TLID;
import com.top_logic.element.boundsec.manager.rule.RoleProvider;
import com.top_logic.element.layout.admin.component.ShowSecurityResultConverter.ShowSecurityBusinessObjectResolver;
import com.top_logic.knowledge.objects.KnowledgeAssociation;
import com.top_logic.knowledge.search.Expression;
import com.top_logic.knowledge.service.BasicTypes;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.db2.DBKnowledgeAssociation;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.knowledge.wrap.WrapperFactory;
import com.top_logic.tool.boundsec.BoundRole;
import com.top_logic.tool.boundsec.wrap.BoundedRole;
import com.top_logic.tool.boundsec.wrap.Group;
import com.top_logic.util.Utils;

/**
 * Representation of a row from the security storage.
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public interface SecurityRow {

	/** 
	 * Return the group (or ID of the group).
	 * 
	 * @return    The requested group.
	 */
	public Object getTarget();

	/** 
	 * Return the business object (or ID of the business object).
	 * 
	 * @return    The requested business object.
	 */
	public Object getBO();

	/** 
	 * Return the affected role (or ID of the role).
	 * 
	 * @return    The requested role.
	 */
	public Object getRole();

	/** 
	 * Return the role provider (or ID of the role provider).
	 * 
	 * @return    The requested role provider.
	 */
	public Object getReason();

	/**
	 * Simple implementation of the interface which is just string based.
	 * 
	 * @author <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
	 */
	public class SimpleSecurityRow implements SecurityRow {

		// Attributes

		/** The represented row. */
		private Object[] row;

		// Constructors

		/**
		 * Creates a {@link SimpleSecurityRow}.
		 */
		public SimpleSecurityRow(Object[] aRow) {
			this.row = aRow;
			for (int i = 0; i < 3; i++) {
				row[i] = LongID.valueOf(Utils.getlongValue(row[i]));
			}
		}

		// Implementation of interface SecurityRow

		@Override
		public Object getTarget() {
			return this.row[0];
		}

		@Override
		public Object getBO() {
			return this.row[1];
		}

		@Override
		public Object getRole() {
			return this.row[2];
		}

		@Override
		public Object getReason() {
			return this.row[3];
		}
	}

	/**
	 * Security row which provides real business objects.
	 * 
	 * @author <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
	 */
	public class ComplexSecurityRow implements SecurityRow {

		// Attributes

		private Group group;

		private TLID boID;

		private BoundRole role;

		private RoleProvider provider;

		private ShowSecurityBusinessObjectResolver resolver;

		private Short reason;

		private Object bo;

		private TLID roleID;

		// Constructors

		/**
		 * Creates a {@link ComplexSecurityRow}.
		 */
		public ComplexSecurityRow(Group aTarget, TLID aBO, BoundRole aRole, RoleProvider aProvider, ShowSecurityBusinessObjectResolver aResolver) {
			this.group    = aTarget;
			this.boID     = aBO;
			this.role     = aRole;
			this.provider = aProvider;
			this.resolver = aResolver;

			aResolver.addID(boID);
		}

		/** 
		 * Creates a {@link ComplexSecurityRow}.
		 */
		public ComplexSecurityRow(Group aGroup, TLID aBOID, TLID aRoleID, BoundedRole aRole, Short reason) {
			this.group  = aGroup;
			this.boID   = aBOID;
			this.role   = aRole;
			this.reason = reason;
			this.roleID = aRoleID;
		}

		// Implementation of interface SecurityRow

		@Override
		public Object getTarget() {
			return this.group;
		}

		@Override
		public Object getBO() {
			if (this.resolver != null) {
				return this.resolver.get(this.boID);
			}
			else if (this.bo != null) { 
				return this.bo;
			}
			else {
				this.bo = this.findBOViaHasRole(boID, roleID);

				return this.bo;
			}
		}

		@Override
		public Object getRole() {
			return this.role;
		}

		@Override
		public Object getReason() {
			return (this.provider != null) ? this.provider : this.reason;
		}

		// Protected methods

		/** 
		 * Find a wrapper by inspecting the {@link KnowledgeAssociation} named {@link BoundedRole#HAS_ROLE_ASSOCIATION}.
		 * 
		 * @param    sourceID    The source ID of the association.
		 * @param    destID      The destination ID of the association.
		 * @return   The requested wrapper or the given source ID (when wrapper not found).
		 */
		protected Object findBOViaHasRole(TLID sourceID, TLID destID) {
			KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();

			Expression sourceIdAttr = identifier(
				reference(BasicTypes.ASSOCIATION_TYPE_NAME, DBKnowledgeAssociation.REFERENCE_SOURCE_NAME));
			Expression destIdAttr = identifier(
				reference(BasicTypes.ASSOCIATION_TYPE_NAME, DBKnowledgeAssociation.REFERENCE_DEST_NAME));
			List<KnowledgeAssociation> search = kb.search(associationQuery(
				filter(
					allOf(BoundedRole.HAS_ROLE_ASSOCIATION),
					and(
						eqBinary(sourceIdAttr, literal(sourceID)),
						eqBinary(destIdAttr, literal(destID))))));
			try {
				for (KnowledgeAssociation theItem : search) {
					Wrapper theWrapper;
					theWrapper = WrapperFactory.getWrapper(theItem.getSourceObject());

					return (theWrapper != null) ? theWrapper : sourceID;
				}
			}
			catch (Exception ex) {
				Logger.warn("Failed to resolve wrapper via 'hasRole' (" + sourceID + ", " + destID + ")!", ex,
					ComplexSecurityRow.class);
			}

			return sourceID;
		}

	}
}
