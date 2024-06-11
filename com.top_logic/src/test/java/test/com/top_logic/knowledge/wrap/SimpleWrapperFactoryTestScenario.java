/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.wrap;

import java.util.Iterator;
import java.util.Set;

import test.com.top_logic.knowledge.service.db2.KnowledgeBaseMigrationTestScenario;
import test.com.top_logic.knowledge.service.db2.KnowledgeBaseTestScenarioConstants;
import test.com.top_logic.knowledge.service.db2.KnowledgeBaseTestScenarioImpl;

import com.top_logic.basic.TLID;
import com.top_logic.basic.col.NameValueBuffer;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.dob.DataObjectException;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.knowledge.objects.KnowledgeAssociation;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.AssociationQuery;
import com.top_logic.knowledge.service.Branch;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.KnowledgeBaseRuntimeException;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.db2.AssociationSetQuery;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.knowledge.wrap.WrapperFactory;
import com.top_logic.knowledge.wrap.exceptions.WrapperRuntimeException;
import com.top_logic.tool.boundsec.wrap.AbstractBoundWrapper;

/**
 * Base class for tests with a single {@link Wrapper}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public abstract class SimpleWrapperFactoryTestScenario implements KnowledgeBaseTestScenarioConstants {

	/**
	 * Wrapper for type {@link #A_NAME}.
	 */
	public static class AObj extends AbstractBoundWrapper {

		public static final AssociationSetQuery<KnowledgeAssociation> AB_ATTR = AssociationQuery.createOutgoingQuery(
			"ab", AB_NAME);

		public static final AssociationSetQuery<KnowledgeAssociation> AB_REFERENCES_ATTR = AssociationQuery
			.createIncomingQuery("abReferences",
			AB_NAME);
		
		public static final AssociationSetQuery<KnowledgeAssociation> BC_ATTR = AssociationQuery.createOutgoingQuery(
			"bc", BC_NAME);

		public static final AssociationSetQuery<KnowledgeAssociation> BC_REFERENCES_ATTR = AssociationQuery
			.createIncomingQuery("bcReferences",
			BC_NAME);

		/**
		 * Name of flexible attribute <code>f1</code>.
		 */
		public static final String F1_NAME = "f1";
		
		/**
		 * Name of flexible attribute <code>f2</code>.
		 */
		public static final String F2_NAME = "f2";
		
		/**
		 * Name of flexible attribute <code>f4</code>.
		 */
		public static final String F4_NAME = "f4";
	

		public AObj(KnowledgeObject ko) {
			super(ko);
		}
		
		@Override
		public String getName() {
			return getA1();
		}

		/**
		 * Getter for {@link #A1_NAME}.
		 */
		public String getA1() {
			return (String) getValue(A1_NAME);
		}
		
		/**
		 * Setter for {@link #A1_NAME}.
		 */
		public void setA1(String value) {
			setValue(A1_NAME, value);
		}
		
		/**
		 * Getter for {@link #A2_NAME}.
		 */
		public String getA2() {
			return (String) getValue(A2_NAME);
		}
		
		/**
		 * Setter for {@link #A2_NAME}.
		 */
		public void setA2(String value) {
			setValue(A2_NAME, value);
		}
		
		public void addAB(SimpleWrapperFactoryTestScenario.AObj other) {
			{
				KnowledgeObject self = tHandle();
				self.getKnowledgeBase().createAssociation(self, other.tHandle(), AB_NAME);
			}
		}
	
		public boolean removeAB(SimpleWrapperFactoryTestScenario.AObj other) {
			{
				KnowledgeObject self = tHandle();
				for (Iterator it = self.getOutgoingAssociations(AB_NAME, other.tHandle()); it.hasNext();) {
					KnowledgeItem link = (KnowledgeItem) it.next();
					link.delete();
					return true;
				}
				return false;
			}
		}
		
		public Set getAB() {
			return resolveWrappers(AB_ATTR);
		}
	
		public Set getABSources() {
			return resolveWrappers(AB_REFERENCES_ATTR);
		}
		
		public void setAB(Set newValue) {
			Set oldValue = getAB();
			for (Iterator it = oldValue.iterator(); it.hasNext(); ) {
				SimpleWrapperFactoryTestScenario.AObj oldDestination = (SimpleWrapperFactoryTestScenario.AObj) it.next();
				if (! newValue.contains(oldDestination)) {
					removeAB(oldDestination);
				}
			}
			for (Iterator it = newValue.iterator(); it.hasNext(); ) {
				SimpleWrapperFactoryTestScenario.AObj newDestination = (SimpleWrapperFactoryTestScenario.AObj) it.next();
				if (! oldValue.contains(newDestination)) {
					addAB(newDestination);
				}
			}
		}
		
		public void addBC(SimpleWrapperFactoryTestScenario.AObj other) {
			{
				KnowledgeObject self = tHandle();
				self.getKnowledgeBase().createAssociation(self, other.tHandle(), BC_NAME);
			}
		}
		
		public boolean removeBC(SimpleWrapperFactoryTestScenario.AObj other) {
			{
				KnowledgeObject self = tHandle();
				for (Iterator it = self.getOutgoingAssociations(BC_NAME, other.tHandle()); it.hasNext();) {
					KnowledgeItem link = (KnowledgeItem) it.next();
					link.delete();
					return true;
				}
				return false;
			}
		}
		
		public Set getBC() {
			return resolveWrappers(BC_ATTR);
		}
		
		public Set getBCSources() {
			return resolveWrappers(BC_REFERENCES_ATTR);
		}
		
		public void setBC(Set newValue) {
			Set oldValue = getBC();
			for (Iterator it = oldValue.iterator(); it.hasNext(); ) {
				SimpleWrapperFactoryTestScenario.AObj oldDestination = (SimpleWrapperFactoryTestScenario.AObj) it.next();
				if (! newValue.contains(oldDestination)) {
					removeBC(oldDestination);
				}
			}
			for (Iterator it = newValue.iterator(); it.hasNext(); ) {
				SimpleWrapperFactoryTestScenario.AObj newDestination = (SimpleWrapperFactoryTestScenario.AObj) it.next();
				if (! oldValue.contains(newDestination)) {
					addBC(newDestination);
				}
			}
		}
		
		/**
		 * @see #setF1(String)
		 */
		public String getF1() {
			return (String) getValue(F1_NAME);
		}

		/**
		 * Setter for the row attribute {@link KnowledgeBaseMigrationTestScenario#T_F1_NAME} in
		 * migration target scenario and flex attribute {@link #F1_NAME} in migration source
		 * scenario.
		 */
		public void setF1(String value) {
			setValue(F1_NAME, value);
		}
		
		/**
		 * Getter for {@link #F2_NAME}.
		 */
		public String getF2() {
			return (String) getValue(F2_NAME);
		}
		
		/**
		 * Setter for {@link #F2_NAME}.
		 */
		public void setF2(String value) {
			setValue(F2_NAME, value);
		}
		
		/**
		 * Getter for {@link #F4_NAME}.
		 */
		public BinaryData getF4() {
			return (BinaryData) getValue(F4_NAME);
		}
		
		/**
		 * Setter for {@link #F4_NAME}.
		 */
		public void setF4(BinaryData value) {
			setValue(F4_NAME, value);
		}
	}

	/**
	 * Wrapper for type {@link #B_NAME}.
	 */
	public static class BObj extends AObj {
	
		/**
		 * Name of flexible attribute <code>f3</code>.
		 */
		public static final String F3_NAME = "f3";
	
		public BObj(KnowledgeObject ko) {
			super(ko);
		}
		
		public static SimpleWrapperFactoryTestScenario.BObj newBObj(String a1) {
			return newBObj(PersistencyLayer.getKnowledgeBase(), a1);
		}

		public static SimpleWrapperFactoryTestScenario.BObj newBObj(String a1, TLID internalId) {
			return newBObj(PersistencyLayer.getKnowledgeBase(), a1, internalId);
		}

		public static SimpleWrapperFactoryTestScenario.BObj newBObj(KnowledgeBase kb, String a1) {
			return newBObj(kb, a1, null);
		}

		public static SimpleWrapperFactoryTestScenario.BObj newBObj(KnowledgeBase kb, String a1, TLID internalId) {
			{
				KnowledgeObject ko = kb.createKnowledgeObject(internalId, B_NAME);
				SimpleWrapperFactoryTestScenario.BObj result = (SimpleWrapperFactoryTestScenario.BObj) WrapperFactory.getWrapper(ko);
				result.setA1(a1);
				return result;
			}
		}

		/**
		 * Creates a new {@link BObj}.
		 * 
		 * @param b
		 *        the {@link Branch} in which the object should live.
		 */
		public static BObj newBObj(KnowledgeBase kb, Branch b, String a1, TLID internalId) {
			{
				KnowledgeObject ko = kb.createKnowledgeObject(b, internalId, B_NAME);
				BObj result = (BObj) WrapperFactory.getWrapper(ko);
				result.setA1(a1);
				return result;
			}
		}

		/**
		 * Getter for {@link #B1_NAME}.
		 */
		public String getB1() {
			return (String) getValue(B1_NAME);
		}
		
		/**
		 * Setter for {@link #B1_NAME}.
		 */
		public void setB1(String value) {
			setValue(B1_NAME, value);
		}

		/**
		 * Getter for {@link #B2_NAME}.
		 */
		public String getB2() {
			return (String) getValue(B2_NAME);
		}
		
		/**
		 * Getter for {@link KnowledgeBaseMigrationTestScenario#T_B2_NAME} after migration.
		 */
		public String getT_B2() {
			return (String) getValue(KnowledgeBaseMigrationTestScenario.T_B2_NAME);
		}
		
		/**
		 * Setter for {@link #B2_NAME}.
		 */
		public void setB2(String value) {
			setValue(B2_NAME, value);
		}

		/**
		 * Setter for the row attribute {@link KnowledgeBaseTestScenarioConstants#F3_NAME} in
		 * migration source scenario and flex attribute {@link #F3_NAME} in migration target
		 * scenario.
		 */
		public void setF3(String value ) {
			setValue(F3_NAME, value);
		}

		/**
		 * @see #setF3(String)
		 */
		public String getF3() {
			return (String) getValue(F3_NAME);
		}
	}

	/**
	 * Wrapper for type {@link #C_NAME}.
	 */
	public static class CObj extends BObj {
	
		public CObj(KnowledgeObject ko) {
			super(ko);
		}
		
		public static SimpleWrapperFactoryTestScenario.CObj newCObj(String a1) {
			KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
			return newCObj(kb, a1);
		}

		public static SimpleWrapperFactoryTestScenario.CObj newCObj(KnowledgeBase kb, String a1) {
			{
				KnowledgeObject ko = kb.createKnowledgeObject(C_NAME);
				SimpleWrapperFactoryTestScenario.CObj result = (SimpleWrapperFactoryTestScenario.CObj) WrapperFactory.getWrapper(ko);
				result.setA1(a1);
				return result;
			}
		}

		/**
		 * Creates a new {@link CObj}.
		 * 
		 * @param b
		 *        the {@link Branch} in which the object should live.
		 */
		public static CObj newCObj(KnowledgeBase kb, Branch b, String a1, TLID internalId) {
			{
				KnowledgeObject ko = kb.createKnowledgeObject(b, internalId, C_NAME);
				CObj result = (CObj) WrapperFactory.getWrapper(ko);
				result.setA1(a1);
				return result;
			}
		}

		/**
		 * Getter for {@link #C1_NAME}.
		 */
		public String getC1() {
			return (String) getValue(C1_NAME);
		}
		
		/**
		 * Setter for {@link #C1_NAME}.
		 */
		public void setC1(String value) {
			setValue(C1_NAME, value);
		}
		
		/**
		 * Getter for {@link #C2_NAME}.
		 */
		public String getC2() {
			return (String) getValue(C2_NAME);
		}
		
		/**
		 * Getter for {@link KnowledgeBaseMigrationTestScenario#T_C2_NAME}.
		 */
		public Integer getT_C2() {
			return (Integer) getValue(KnowledgeBaseMigrationTestScenario.T_C2_NAME);
		}
		
		/**
		 * Setter for {@link #C2_NAME}.
		 */
		public void setC2(String value) {
			setValue(C2_NAME, value);
		}
		
		/**
		 * Setter for {@link KnowledgeBaseMigrationTestScenario#T_C3_NAME}.
		 */
		public Integer getT_C3() {
			return (Integer) getValue(KnowledgeBaseMigrationTestScenario.T_C3_NAME);
		}
	}

	/**
	 * Wrapper for type {@link #D_NAME}.
	 */
	public static class DObj extends AObj {

		public DObj(KnowledgeObject ko) {
			super(ko);
		}

		public static DObj newDObj(String a1) {
			KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
			return newDObj(kb, a1);
		}

		public static DObj newDObj(KnowledgeBase kb, String a1) {
			{
				DObj result = (DObj) kb.createKnowledgeObject(D_NAME).getWrapper();
				result.setA1(a1);
				return result;
			}
		}

		/**
		 * Getter for {@link #C1_NAME}.
		 */
		public String getD1() {
			return (String) getValue(D1_NAME);
		}

		/**
		 * Setter for {@link #D1_NAME}.
		 */
		public void setD1(String value) {
			setValue(D1_NAME, value);
		}

		/**
		 * Getter for {@link #D2_NAME}.
		 */
		public String getD2() {
			return (String) getValue(D2_NAME);
		}

		/**
		 * Setter for {@link #D2_NAME}.
		 */
		public void setD2(String value) {
			setValue(D2_NAME, value);
		}

		public DObj setPolyCurLocal(KnowledgeObject value) throws DataObjectException {
			tHandle().setAttributeValue(REFERENCE_POLY_CUR_LOCAL_NAME, value);
			return this;
		}

		public DObj getMonoHistGlobal() {
			return getReference(DObj.class, REFERENCE_MONO_HIST_GLOBAL_NAME);
		}
		
		public DObj setMonoHistGlobal(DObj value) {
			setReference(REFERENCE_MONO_HIST_GLOBAL_NAME, value);
			return this;
		}

		public DObj getMonoHistLocal() {
			return getReference(DObj.class, REFERENCE_MONO_HIST_LOCAL_NAME);
		}

		public DObj setMonoHistLocal(DObj value) {
			setReference(REFERENCE_MONO_HIST_LOCAL_NAME, value);
			return this;
		}

		public DObj getMonoCurGlobal() {
			return getReference(DObj.class, REFERENCE_MONO_CUR_GLOBAL_NAME);
		}

		public DObj setMonoCurGlobal(DObj value) {
			setReference(REFERENCE_MONO_CUR_GLOBAL_NAME, value);
			return this;
		}

		public DObj getMonoCurLocal() {
			return getReference(DObj.class, REFERENCE_MONO_CUR_LOCAL_NAME);
		}

		public DObj setMonoCurLocal(DObj value) {
			setReference(REFERENCE_MONO_CUR_LOCAL_NAME, value);
			return this;
		}

		public DObj getMonoMixedGlobal() {
			return getReference(DObj.class, REFERENCE_MONO_MIXED_GLOBAL_NAME);
		}

		public DObj setMonoMixedGlobal(DObj value) {
			setReference(REFERENCE_MONO_MIXED_GLOBAL_NAME, value);
			return this;
		}

		public DObj getMonoMixedLocal() {
			return getReference(DObj.class, REFERENCE_MONO_MIXED_LOCAL_NAME);
		}

		public DObj setMonoMixedLocal(DObj value) {
			setReference(REFERENCE_MONO_MIXED_LOCAL_NAME, value);
			return this;
		}

		public DObj getPolyHistGlobal() {
			return getReference(DObj.class, REFERENCE_POLY_HIST_GLOBAL_NAME);
		}

		public DObj setPolyHistGlobal(DObj value) {
			setReference(REFERENCE_POLY_HIST_GLOBAL_NAME, value);
			return this;
		}

		public DObj getPolyHistLocal() {
			return getReference(DObj.class, REFERENCE_POLY_HIST_LOCAL_NAME);
		}

		public DObj setPolyHistLocal(DObj value) {
			setReference(REFERENCE_POLY_HIST_LOCAL_NAME, value);
			return this;
		}

		public DObj getPolyCurGlobal() {
			return getReference(DObj.class, REFERENCE_POLY_CUR_GLOBAL_NAME);
		}

		public DObj setPolyCurGlobal(DObj value) {
			setReference(REFERENCE_POLY_CUR_GLOBAL_NAME, value);
			return this;
		}

		public DObj getPolyCurLocal() {
			return getReference(DObj.class, REFERENCE_POLY_CUR_LOCAL_NAME);
		}

		public DObj setPolyCurLocal(DObj value) {
			setReference(REFERENCE_POLY_CUR_LOCAL_NAME, value);
			return this;
		}

		public DObj getPolyMixedGlobal() {
			return getReference(DObj.class, REFERENCE_POLY_MIXED_GLOBAL_NAME);
		}

		public DObj setPolyMixedGlobal(DObj value) {
			setReference(REFERENCE_POLY_MIXED_GLOBAL_NAME, value);
			return this;
		}

		public DObj getPolyMixedLocal() {
			return getReference(DObj.class, REFERENCE_POLY_MIXED_LOCAL_NAME);
		}

		public DObj setPolyMixedLocal(DObj value) {
			setReference(REFERENCE_POLY_MIXED_LOCAL_NAME, value);
			return this;
		}

	}

	/**
	 * Wrapper type for {@link KnowledgeBaseTestScenarioImpl#E}
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	public static class EObj extends DObj {

		public EObj(KnowledgeObject ko) {
			super(ko);
		}

		public int getE1() {
			return ((Number) getValue(E1_NAME)).intValue();
		}

		public void setE1(int value) {
			setValue(E1_NAME, Integer.valueOf(value));
		}

		public int getE2() {
			return ((Number) getValue(E2_NAME)).intValue();
		}

		public void setE2(int value) {
			setValue(E2_NAME, Integer.valueOf(value));
		}

		public void setReference(KnowledgeItem reference) {
			setValue(KnowledgeBaseTestScenarioConstants.REFERENCE_POLY_HIST_GLOBAL_NAME, reference);
		}

		public KnowledgeItem getReference() {
			return (KnowledgeItem) getValue(KnowledgeBaseTestScenarioConstants.REFERENCE_POLY_HIST_GLOBAL_NAME);
		}

		public static SimpleWrapperFactoryTestScenario.EObj newEObj(String a1) {
			return newEObj(PersistencyLayer.getKnowledgeBase(), a1);
		}

		public static SimpleWrapperFactoryTestScenario.EObj newBObj(String a1, TLID internalId) {
			return newEObj(PersistencyLayer.getKnowledgeBase(), a1, internalId);
		}

		public static SimpleWrapperFactoryTestScenario.EObj newEObj(KnowledgeBase kb, String a1) {
			return newEObj(kb, a1, null);
		}

		public static SimpleWrapperFactoryTestScenario.EObj newEObj(KnowledgeBase kb, String a1, TLID internalId) {
			{
				KnowledgeObject ko = kb.createKnowledgeObject(internalId, E_NAME);
				SimpleWrapperFactoryTestScenario.EObj result =
					(SimpleWrapperFactoryTestScenario.EObj) WrapperFactory.getWrapper(ko);
				result.setA1(a1);
				return result;
			}
		}

	}
	
	static abstract class S extends AbstractBoundWrapper {

		public S(KnowledgeObject ko) {
			super(ko);
		}

		public String getS1() {
			return getString(S1_NAME);
		}

	}

	/**
	 * Wrapper with sub-type stored in {@link KnowledgeBaseTestScenarioConstants#S_NAME}.
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	public static class SA extends S {

		/**
		 * Subtype of {@link SA} wrappers.
		 */
		public static final String SA_TYPE = "SA";

		public SA(KnowledgeObject ko) {
			super(ko);
		}
		
		public void setF1(String f1) {
			setValue("f1", f1);
		}

		public String getF1() {
			return getString("f1");
		}
		
		public static SA newSAObj(String s1) {
			KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
			return newSAObj(kb, s1);
		}
		
		public static SA newSAObj(KnowledgeBase kb, String s1) {
			{
				KnowledgeObject ko =
					kb.createKnowledgeObject(S_NAME,
						new NameValueBuffer().put(S_TYPE_NAME, SA_TYPE).put(S1_NAME, s1));
				SA result = (SA) WrapperFactory.getWrapper(ko);
				return result;
			}
		}
	}
	
	/**
	 * Wrapper with sub-type stored in {@link KnowledgeBaseTestScenarioConstants#S_NAME}.
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	public static class SB extends S {
		
		/**
		 * Subtype of {@link SB} wrappers.
		 */
		public static final String SB_TYPE = "SB";

		public SB(KnowledgeObject ko) {
			super(ko);
		}
		
		public void setF1(int f1) {
			setValue("f1", f1);
		}

		public int getF1() {
			return getInt("f1");
		}
		
		public static SB newSBObj(String s1) {
			KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
			return newSBObj(kb, s1);
		}
		
		public static SB newSBObj(KnowledgeBase kb, String s1) {
			{
				KnowledgeObject ko =
					kb.createKnowledgeObject(S_NAME,
						new NameValueBuffer().put(S_TYPE_NAME, SB_TYPE).put(S1_NAME, s1));
				SB result = (SB) WrapperFactory.getWrapper(ko);
				return result;
			}
		}
	}
	
	/**
	 * Wrapper with sub-type stored in {@link KnowledgeBaseTestScenarioConstants#S_NAME}.
	 * 
	 * The wrapper accesses reference {@link KnowledgeBaseTestScenarioConstants#S_REFERENCE_ATTR} in
	 * its constructor.
	 */
	public static class SC extends S {
		
		/**
		 * Subtype of {@link SC} wrappers.
		 */
		public static final String SC_TYPE = "SC";

		public SC(KnowledgeObject ko) {
			super(ko);
			if (getReference() == null) {
				throw new WrapperRuntimeException("Missing reference value.");
			}
		}

		public KnowledgeItem getReference() {
			try {
				return (KnowledgeItem) tHandle().getAttributeValue(S_REFERENCE_ATTR);
			} catch (NoSuchAttributeException ex) {
				throw new KnowledgeBaseRuntimeException(ex);
			}
		}
		
		public static SC newSCObj(String s1, KnowledgeItem sRef) {
			KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
			return newSCObj(kb, s1, sRef);
		}
		
		public static SC newSCObj(KnowledgeBase kb, String s1, KnowledgeItem sRef) {
			{
				KnowledgeObject ko =
					kb.createKnowledgeObject(S_NAME,
						new NameValueBuffer().put(S_TYPE_NAME, SC_TYPE).put(S1_NAME, s1).put(S_REFERENCE_ATTR, sRef));
				SC result = (SC) WrapperFactory.getWrapper(ko);
				return result;
			}
		}
	}
	
	public abstract static class MigrationScenario extends SimpleWrapperFactoryTestScenario {
		// Pure base class.
	}
}
