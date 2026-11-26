/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.model.search.rules;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.Test;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;
import test.com.top_logic.knowledge.wrap.person.TestPerson;

import com.top_logic.base.security.device.TLSecurityDeviceManager;
import com.top_logic.base.services.InitialRolesManager;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.util.ResKey;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.element.boundsec.manager.ElementAccessManager;
import com.top_logic.element.boundsec.manager.RoleRulesImporter;
import com.top_logic.element.boundsec.manager.coverage.CoverageFinding;
import com.top_logic.element.boundsec.manager.coverage.FindingKind;
import com.top_logic.element.boundsec.manager.coverage.SecurityCoverageAnalysis;
import com.top_logic.element.boundsec.manager.rule.PathElement;
import com.top_logic.element.boundsec.manager.rule.RoleProvider;
import com.top_logic.element.boundsec.manager.rule.config.HistoryMode;
import com.top_logic.element.boundsec.manager.rule.config.PathElementConfig;
import com.top_logic.element.boundsec.manager.rule.config.RoleRuleConfig;
import com.top_logic.element.boundsec.manager.rule.config.RoleRulesConfig;
import com.top_logic.element.model.DynamicModelService;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.service.UpdateEvent;
import com.top_logic.knowledge.service.UpdateListener;
import com.top_logic.knowledge.wrap.WrapperHistoryUtils;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.layout.provider.LabelProviderService;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.util.TLModelPartRef;
import com.top_logic.model.search.expr.config.ExprFormat;
import com.top_logic.model.search.expr.config.SearchBuilder;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.model.security.ModelAccessRights;
import com.top_logic.model.security.SecurityConfigurationService;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.BoundObject;
import com.top_logic.tool.boundsec.manager.AccessManager;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;
import com.top_logic.tool.boundsec.wrap.BoundedRole;
import com.top_logic.tool.boundsec.wrap.Group;
import com.top_logic.util.TLContextManager;
import com.top_logic.util.model.ModelService;

/**
 * Tests the access rights on historic versions of objects with the reference example of the
 * specification of the object security for historic versions: releases referring to released
 * versions of a document.
 *
 * <p>
 * The rules and grants are configured in <code>TestHistoricSecurity-test.config.xml</code>.
 * </p>
 */
@SuppressWarnings("javadoc")
public class TestHistoricSecurity extends BasicTestCase {

	private static final String MODULE = "TestHistoricSecurity";

	private KnowledgeBase _kb;

	private final Map<ObjectKey, KnowledgeItem> _toDelete = new HashMap<>();

	private final UpdateListener _creationListener = new UpdateListener() {
		@Override
		public void notifyUpdate(KnowledgeBase sender, UpdateEvent event) {
			_toDelete.putAll(event.getCreatedObjects());
			_toDelete.keySet().removeAll(event.getDeletedObjectKeys());
		}
	};

	private Person _pUser;

	private Person _p2User;

	private Person _qUser;

	private Person _anna;

	private Person _bernd;

	private Person _erik;

	private Person _paul;

	private Person _rita;

	private TLObject _partnerP;

	private TLObject _partnerP2;

	private TLObject _partnerQ;

	private TLObject _d;

	private TLObject _s1;

	private TLObject _s2;

	private TLObject _s3;

	private TLObject _paragraph;

	private TLObject _glossary;

	private TLObject _r1;

	private TLObject _r2;

	private TLObject _r3;

	/** Revision without any release. */
	private Revision _r10;

	/** Revision of R1. */
	private Revision _r11;

	/** Intermediate revision that was never released. */
	private Revision _r15;

	/** Revision of R2. */
	private Revision _r21;

	/** Revision of R3. */
	private Revision _r22;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_kb = PersistencyLayer.getKnowledgeBase();
		_kb.addUpdateListener(_creationListener);

		try (Transaction tx = beginTx()) {
			_pUser = TestPerson.createPerson("histsecP");
			_p2User = TestPerson.createPerson("histsecP2");
			_qUser = TestPerson.createPerson("histsecQ");
			_anna = TestPerson.createPerson("histsecAnna");
			_bernd = TestPerson.createPerson("histsecBernd");
			_erik = TestPerson.createPerson("histsecErik");
			_paul = TestPerson.createPerson("histsecPaul");
			_rita = TestPerson.createPerson("histsecRita");

			_partnerP = partner("P", _pUser, _paul, _rita);
			_partnerP2 = partner("P2", _p2User);
			_partnerQ = partner("Q", _qUser, _paul);

			_glossary = newObject("Glossary");
			_glossary.tUpdateByName("name", "G");

			_d = newObject("Document");
			_d.tUpdateByName("name", "D");
			_d.tUpdateByName("editors", list(_anna));
			_d.tUpdateByName("glossary", _glossary);
			_s1 = section("S1", _partnerP);
			_s2 = section("S2", _partnerP);
			_s3 = section("S3", _partnerQ);
			_s3.tUpdateByName("reviewers", list(_rita));
			_paragraph = newObject("Paragraph");
			_paragraph.tUpdateByName("name", "S1.1");
			_s1.tUpdateByName("paragraphs", list(_paragraph));
			_d.tUpdateByName("sections", list(_s1, _s2, _s3));
			tx.commit();
			_r10 = tx.getCommitRevision();
		}
		try (Transaction tx = beginTx()) {
			_r1 = release("R1", _partnerP);
			tx.commit();
			_r11 = tx.getCommitRevision();
		}
		try (Transaction tx = beginTx()) {
			_s1.tUpdateByName("name", "S1 (work in progress)");
			tx.commit();
			_r15 = tx.getCommitRevision();
		}
		try (Transaction tx = beginTx()) {
			_s1.tUpdateByName("partner", _partnerP2);
			_s2.tUpdateByName("partner", _partnerP2);
			_s3.tUpdateByName("partner", _partnerP);
			_d.tUpdateByName("editors", list(_bernd));
			tx.commit();
		}
		try (Transaction tx = beginTx()) {
			_r2 = release("R2", _partnerP2);
			tx.commit();
			_r21 = tx.getCommitRevision();
		}
		try (Transaction tx = beginTx()) {
			_r3 = release("R3", null);
			_r3.tUpdateByName("recipients", list(_erik));
			tx.commit();
			_r22 = tx.getCommitRevision();
		}
		try (Transaction tx = beginTx()) {
			_d.tUpdateByName("name", "D (current)");
			_s1.tUpdateByName("name", "S1 (current)");
			_s2.tUpdateByName("name", "S2 (current)");
			_s3.tUpdateByName("name", "S3 (current)");
			tx.commit();
		}
	}

	@Override
	protected void tearDown() throws Exception {
		_kb.removeUpdateListener(_creationListener);
		try (Transaction tx = beginTx()) {
			KBUtils.deleteAllKI(_toDelete.values().iterator());
			tx.commit();
		}
		_toDelete.clear();
		_kb = null;
		super.tearDown();
	}

	public void testSnapshotIsFixedToRevisionOfRelease() {
		TLObject snapshot = (TLObject) _r1.tValueByName("snapshot");
		assertEquals(_r11.getCommitNumber(), snapshot.tHistoryContext());
	}

	public void testPartnerReadsReleasedSections() {
		assertTrue(read(_pUser, _r1));
		assertFalse(read(_pUser, _r2));
		assertFalse(read(_pUser, _r3));

		assertTrue("Covered by R1, readable for P, rule A leads to P in r11.", read(_pUser, at(_s1, _r11)));
		assertTrue(read(_pUser, at(_s2, _r11)));
		assertFalse("Rule A leads to Q in r11.", read(_pUser, at(_s3, _r11)));
	}

	public void testPartnerReadsNoVersionWithoutRelease() {
		assertFalse("Not covered by any release, although the partner is P there.", read(_pUser, at(_s1, _r10)));
		assertFalse(read(_pUser, at(_s1, _r15)));
		assertFalse("Rule A leads to P in r21, but only R2 covers the version, which P cannot read.",
			read(_pUser, at(_s3, _r21)));
	}

	public void testAnchoredRuleGrantsNothingOnCurrentObject() {
		assertFalse(read(_pUser, _s1));
		assertFalse("The current partner of S3 is P.", read(_pUser, _s3));
		assertFalse(read(_p2User, _s1));
	}

	public void testPartnerDiscoversReleasedDocument() {
		TLObject released = at(_d, _r11);
		assertFalse(read(_pUser, released));
		assertTrue(discover(_pUser, released));
		assertTrue(ModelAccessRights.getInstance().isReadAllowed(_pUser, released,
			released.tType().getPart("sections")));
		assertFalse(discover(_pUser, at(_d, _r21)));
		assertFalse(discover(_pUser, at(_d, _r10)));
		assertFalse(discover(_pUser, _d));
	}

	public void testOtherPartner() {
		assertTrue(read(_p2User, _r2));
		assertFalse(read(_p2User, _r1));
		assertFalse("Only covered by R1.", read(_p2User, at(_s1, _r11)));
		assertTrue(read(_p2User, at(_s1, _r21)));
		assertTrue(read(_p2User, at(_s2, _r21)));
		assertFalse("Rule A leads to P in r21.", read(_p2User, at(_s3, _r21)));
		assertFalse("Only covered by R3.", read(_p2User, at(_s1, _r22)));
		assertTrue(discover(_p2User, at(_d, _r21)));
	}

	public void testPartnerWithoutRelease() {
		assertFalse("Rule A leads to Q in r11, but no release is readable for Q.", read(_qUser, at(_s3, _r11)));
	}

	public void testFormerEditorSeesNothing() {
		assertFalse(read(_anna, _d));
		assertFalse(read(_anna, at(_d, _r11)));
		assertFalse(read(_anna, at(_s1, _r11)));
		assertFalse(read(_anna, _r1));
	}

	public void testCurrentEditorSeesEachVersion() {
		assertTrue(read(_bernd, _d));
		assertTrue(read(_bernd, _s1));
		assertTrue(read(_bernd, at(_d, _r10)));
		assertTrue(read(_bernd, at(_d, _r11)));
		assertTrue(read(_bernd, at(_s1, _r15)));
		assertTrue(read(_bernd, at(_s3, _r21)));
		assertTrue("Rule F inherits the role of the editor from the released version.", read(_bernd, _r1));
		assertTrue(read(_bernd, _r2));
		assertTrue(read(_bernd, _r3));
	}

	public void testRecipientReadsExactlyTheReleasedVersion() {
		assertTrue(read(_erik, _r3));
		assertTrue(read(_erik, at(_d, _r22)));
		assertTrue(read(_erik, at(_s1, _r22)));
		assertTrue(read(_erik, at(_s3, _r22)));
		assertFalse("Only covered by R2, although the content is the same.", read(_erik, at(_d, _r21)));
		assertFalse(read(_erik, at(_s1, _r21)));
		assertFalse(read(_erik, at(_d, _r11)));
		assertFalse(read(_erik, at(_s1, _r15)));
		assertFalse("An anchor step leads nowhere from a current object.", read(_erik, _d));
		assertFalse(read(_erik, _s1));
	}

	public void testReferencedObjectInheritsFromAnchor() {
		assertTrue(read(_erik, at(_glossary, _r22)));
		assertFalse(read(_erik, at(_glossary, _r21)));
		assertFalse(read(_erik, _glossary));
	}

	public void testCompositionPartFollowsContainerInItsVersion() {
		assertTrue(read(_pUser, at(_paragraph, _r11)));
		assertFalse(read(_pUser, at(_paragraph, _r10)));
		assertFalse(read(_pUser, _paragraph));
		assertTrue(read(_bernd, _paragraph));
	}

	public void testUserInTwoPartnerGroups() {
		assertTrue(read(_paul, at(_s1, _r11)));
		assertFalse("The path leads to Q, the gate is open for P: no group fulfills both.",
			read(_paul, at(_s3, _r11)));
	}

	public void testPersonNamedInPathAndMemberOfGateGroup() {
		assertTrue("Rita reviews S3 in r11 and is a member of P, for which R1 was released.",
			read(_rita, at(_s3, _r11)));
		assertFalse(read(_rita, at(_s3, _r10)));
		assertFalse(read(_rita, _s3));
	}

	public void testModificationIsDecidedOnCurrentObject() {
		assertFalse("The role of the gate does not admit a modification.", write(_pUser, at(_s1, _r11)));
		assertFalse(write(_erik, at(_d, _r22)));
		assertTrue(write(_bernd, at(_d, _r11)));
		assertTrue(write(_bernd, at(_s1, _r15)));
		assertFalse(write(_anna, at(_d, _r11)));
	}

	public void testDeletedSectionStaysReleased() throws Exception {
		TLObject s2 = at(_s2, _r11);
		try (Transaction tx = beginTx()) {
			_s2.tDelete();
			tx.commit();
		}
		assertTrue(read(_pUser, s2));
		assertFalse("A rule evaluated on the current data grants nothing on a deleted object.", read(_bernd, s2));
	}

	public void testDeletedReleaseEndsAccess() throws Exception {
		TLObject s1 = at(_s1, _r11);
		assertTrue(read(_pUser, s1));
		try (Transaction tx = beginTx()) {
			_r1.tDelete();
			tx.commit();
		}
		assertFalse(read(_pUser, s1));
	}

	public void testChangedPartnerOfReleaseChangesReaders() throws Exception {
		TLObject s3 = at(_s3, _r11);
		assertFalse(read(_qUser, s3));
		try (Transaction tx = beginTx()) {
			_r1.tUpdateByName("partner", _partnerQ);
			tx.commit();
		}
		assertTrue(read(_qUser, s3));
		assertFalse(read(_pUser, at(_s1, _r11)));
	}

	public void testDirectAssignmentHoldsForEachVersion() throws Exception {
		BoundedRole recipient = BoundedRole.getRoleByName(MODULE + ".ReleaseRecipient");
		try (Transaction tx = beginTx()) {
			BoundedRole.assignRole(_d, _anna, recipient);
			tx.commit();
		}
		assertTrue(read(_anna, _d));
		assertTrue(read(_anna, at(_d, _r10)));
		assertTrue(read(_anna, at(_d, _r15)));
		assertFalse("A section has rules of its own and does not follow the document.", read(_anna, at(_s1, _r10)));
	}

	public void testNoAssignmentToHistoricVersion() throws Exception {
		BoundedRole recipient = BoundedRole.getRoleByName(MODULE + ".ReleaseRecipient");
		try (Transaction tx = beginTx()) {
			try {
				BoundedRole.assignRole(at(_d, _r11), _anna, recipient);
				fail("A historic version is no context of a role assignment.");
			} catch (IllegalArgumentException ex) {
				// Expected.
			}
		}
	}

	public void testAllowedBusinessObjects() {
		AccessManager accessManager = AccessManager.getInstance();
		BoundedRole reader = BoundedRole.getRoleByName(MODULE + ".PartnerReader");
		List<BoundObject> candidates = Arrays.asList((BoundObject) at(_s1, _r11), (BoundObject) at(_s3, _r11),
			(BoundObject) at(_s1, _r10), (BoundObject) _s1);
		Collection<BoundObject> allowed = TLContextManager.inPersonContext(_pUser,
			() -> accessManager.getAllowedBusinessObjects(_pUser, Collections.singletonList(reader), candidates));
		assertEquals(set(at(_s1, _r11)), new HashSet<>(allowed));
	}

	public void testRoleOnAnyObject() {
		AccessManager accessManager = AccessManager.getInstance();
		BoundedRole reader = BoundedRole.getRoleByName(MODULE + ".PartnerReader");
		assertTrue("The role is only held on historic versions, which the storage does not know.",
			accessManager.hasRoleOnAnyObject(_pUser, Collections.singletonList(reader)));
	}

	public void testSectionsOfReleasedDocumentAreFiltered() {
		TLObject released = at(_d, _r11);
		Set<Object> visible = new HashSet<>();
		for (Object section : (Collection<?>) released.tValueByName("sections")) {
			if (discover(_pUser, (TLObject) section)) {
				visible.add(section);
			}
		}
		assertEquals(set(at(_s1, _r11), at(_s2, _r11)), visible);
	}

	public void testInRevisionDeliversOnlyDiscoverableVersions() throws Exception {
		Expr expr = ExprFormat.INSTANCE.getValue("expr", "x -> r -> $x.inRevision($r)");
		QueryExecutor script = QueryExecutor.compile(expr);
		TLObject released = at(_s1, _r11);
		assertEquals(released, TLContextManager.inPersonContext(_pUser,
			() -> script.execute(released, Long.valueOf(_r11.getCommitNumber()))));
		assertNull("Another version of the same object is not delivered.", TLContextManager.inPersonContext(_pUser,
			() -> script.execute(released, Long.valueOf(_r15.getCommitNumber()))));
		assertNull(TLContextManager.inPersonContext(_pUser, () -> script.execute(released, Revision.CURRENT)));
		assertEquals(at(_s1, _r15), TLContextManager.inPersonContext(_bernd,
			() -> script.execute(released, Long.valueOf(_r15.getCommitNumber()))));
	}

	public void testCoverageFindings() {
		SecurityCoverageAnalysis analysis = SecurityCoverageAnalysis.newInstance(Set.of());
		assertEquals(Set.of(), historyFindings(analysis, "Section"));
		assertEquals(Set.of(), historyFindings(analysis, "Document"));
		assertEquals(Set.of(), historyFindings(analysis, "Release"));
		assertEquals("A rule inheriting a role from the parent chapter is no cycle.", Set.of(),
			historyFindings(analysis, "Chapter"));
		assertEquals("All rules of the glossary grant roles on historic versions only.",
			Set.of(FindingKind.NO_CURRENT_ROLE_SOURCE), historyFindings(analysis, "Glossary"));
	}

	private static Set<FindingKind> historyFindings(SecurityCoverageAnalysis analysis, String className) {
		Set<FindingKind> historyKinds = Set.of(FindingKind.ANCHOR_NEVER_COVERS,
			FindingKind.ANCHOR_WITHOUT_INDEX, FindingKind.ROLE_CYCLE, FindingKind.ANCHOR_STEP_FROM_CURRENT,
			FindingKind.NO_CURRENT_ROLE_SOURCE, FindingKind.RULE_NOT_STORED, FindingKind.MIXED_BACKWARDS_STEP);
		Set<FindingKind> result = new HashSet<>();
		for (CoverageFinding finding : analysis.analyze((TLClass) TLModelUtil.findType(MODULE + ":" + className))
			.findings()) {
			if (historyKinds.contains(finding.getKind())) {
				result.add(finding.getKind());
			}
		}
		return result;
	}

	public void testRuleWithHistoricForwardStepIsNotStored() {
		ElementAccessManager accessManager = (ElementAccessManager) AccessManager.getInstance();
		Set<String> notStored = new HashSet<>();
		for (RoleProvider rule : accessManager.getRuleAnalysis().getNotStoredRules()) {
			notStored.add(rule.getConfigId());
		}
		assertTrue(notStored.contains("histsec-F"));
		assertFalse(notStored.contains("histsec-B"));
		assertFalse(notStored.contains("histsec-E"));
	}

	/**
	 * Releases of an area and of a part of it: the path to the anchor uses an ordinary reference
	 * and parent chapters, navigated as role parents in the checked version.
	 */
	public void testReleaseOfAreaAndOfPartOfIt() throws Exception {
		Person tina = _rita;
		TLObject area;
		TLObject chapter1;
		TLObject chapter2;
		TLObject chapter11;
		TLObject part;
		Revision areaReleased;
		Revision rootsReleased;
		try (Transaction tx = beginTx()) {
			area = newObject("Area");
			area.tUpdateByName("name", "A");
			area.tUpdateByName("editors", list(_bernd));
			chapter1 = chapter("C1", area);
			chapter2 = chapter("C2", area);
			chapter11 = chapter("C1.1", area);
			part = newObject("ChapterPart");
			part.tUpdateByName("name", "T1");
			chapter1.tUpdateByName("children", list(chapter11));
			chapter1.tUpdateByName("parts", list(part));
			tx.commit();
		}
		try (Transaction tx = beginTx()) {
			TLObject release = newObject("AreaRelease");
			release.tUpdateByName("name", "RA");
			release.tUpdateByName("snapshot", area);
			release.tUpdateByName("recipients", list(_erik));
			tx.commit();
			areaReleased = tx.getCommitRevision();
		}
		try (Transaction tx = beginTx()) {
			TLObject release = newObject("AreaRelease");
			release.tUpdateByName("name", "RT");
			release.tUpdateByName("roots", list(chapter1));
			release.tUpdateByName("recipients", list(tina));
			tx.commit();
			rootsReleased = tx.getCommitRevision();
		}

		assertTrue(read(_erik, at(area, areaReleased)));
		assertTrue("Role parent in the version is the released area.", read(_erik, at(chapter1, areaReleased)));
		assertTrue(read(_erik, at(chapter2, areaReleased)));
		assertTrue(read(_erik, at(chapter11, areaReleased)));
		assertTrue("The part follows its chapter in the version.", read(_erik, at(part, areaReleased)));
		assertFalse(read(_erik, at(area, rootsReleased)));
		assertFalse(read(_erik, at(chapter1, rootsReleased)));
		assertFalse(read(_erik, area));
		assertFalse(read(_erik, chapter1));

		assertTrue(read(tina, at(chapter1, rootsReleased)));
		assertTrue("The parent chapter in the version is the released chapter.",
			read(tina, at(chapter11, rootsReleased)));
		assertTrue(read(tina, at(part, rootsReleased)));
		assertFalse(read(tina, at(chapter2, rootsReleased)));
		assertFalse(read(tina, at(area, rootsReleased)));
		assertFalse(read(tina, chapter11));

		assertTrue(read(_bernd, area));
		assertTrue(read(_bernd, chapter11));
		assertTrue(read(_bernd, at(chapter11, areaReleased)));
		assertTrue(read(_bernd, part));

		TLObject releasedChild = at(chapter11, rootsReleased);
		TLObject releasedPart = at(part, rootsReleased);
		try (Transaction tx = beginTx()) {
			chapter1.tUpdateByName("children", list());
			chapter2.tUpdateByName("children", list(chapter11));
			tx.commit();
		}
		try (Transaction tx = beginTx()) {
			chapter1.tDelete();
			tx.commit();
		}
		assertTrue("Moving the chapter and deleting its former parent do not change the released version.",
			read(tina, releasedChild));
		assertTrue(read(tina, releasedPart));
		assertFalse(read(tina, chapter11));
	}

	public void testConfigurationErrors() {
		assertTrue(problems(rule(null, null, step("Section#partner", false), step("Partner#group", false))).isEmpty());
		assertTrue(problems(rule(HistoryMode.ANCHORED, "Release#snapshot", step("Section#partner", false),
			step("Partner#group", false))).isEmpty());

		assertFalse("Anchored without anchor reference.",
			problems(rule(HistoryMode.ANCHORED, null, step("Section#partner", false))).isEmpty());
		assertFalse("Anchor references without anchored.",
			problems(rule(HistoryMode.HISTORIC, "Release#snapshot", step("Section#partner", false))).isEmpty());
		assertFalse("Anchored without path.", problems(rule(HistoryMode.ANCHORED, "Release#snapshot")).isEmpty());
		assertFalse("The anchor reference is not historic.",
			problems(rule(HistoryMode.ANCHORED, "Release#partner", step("Section#partner", false))).isEmpty());
		assertFalse("The anchor reference does not exist.",
			problems(rule(HistoryMode.ANCHORED, "Release#noSuchPart", step("Section#partner", false))).isEmpty());
		assertFalse("The anchor reference cannot be navigated backwards.",
			problems(rule(HistoryMode.ANCHORED, "Release#draft", step("Section#partner", false))).isEmpty());
		assertFalse("The reference of the anchor step cannot be navigated backwards.",
			problems(rule(null, null, step("Release#draft", true))).isEmpty());
		assertFalse("History mode at a rule with anchor step.",
			problems(rule(HistoryMode.CURRENT, null, step("Document#sections", true), step("Release#snapshot", true)))
				.isEmpty());
	}

	private static List<ResKey> problems(RoleRuleConfig rule) {
		RoleRulesConfig rules = TypedConfiguration.newConfigItem(RoleRulesConfig.class);
		rules.getRules().add(rule);
		return RoleRulesImporter.loadRules(rules).getProblems();
	}

	@SafeVarargs
	private static RoleRuleConfig rule(HistoryMode history, String anchor,
			PolymorphicConfiguration<? extends PathElement>... steps) {
		RoleRuleConfig rule = TypedConfiguration.newConfigItem(RoleRuleConfig.class);
		rule.setId("histsec-test");
		rule.setMetaElement(MODULE + ":Section");
		rule.update(rule.descriptor().getProperty(RoleRuleConfig.XML_ATTRIBUTE_ROLE),
			Collections.singletonList(MODULE + ".PartnerReader"));
		rule.setHistory(history);
		if (anchor != null) {
			rule.setAnchors(Collections.singletonList(MODULE + ":" + anchor));
		}
		rule.getPathElements().addAll(Arrays.asList(steps));
		return rule;
	}

	private static PolymorphicConfiguration<? extends PathElement> step(String reference, boolean inverse) {
		PathElementConfig step = TypedConfiguration.newConfigItem(PathElementConfig.class);
		step.setAttribute(TLModelPartRef.ref(MODULE + ":" + reference));
		step.setInverse(inverse);
		return step;
	}

	private TLObject chapter(String name, TLObject area) {
		TLObject chapter = newObject("Chapter");
		chapter.tUpdateByName("name", name);
		chapter.tUpdateByName("area", area);
		return chapter;
	}

	private static boolean read(Person person, TLObject object) {
		return allowed(person, object, SimpleBoundCommandGroup.READ);
	}

	private static boolean write(Person person, TLObject object) {
		return allowed(person, object, SimpleBoundCommandGroup.WRITE);
	}

	private static boolean discover(Person person, TLObject object) {
		return allowed(person, object, SimpleBoundCommandGroup.DISCOVER);
	}

	private static boolean allowed(Person person, TLObject object, BoundCommandGroup operation) {
		return TLContextManager.inPersonContext(person,
			() -> ModelAccessRights.getInstance().isAllowed(person, object, operation));
	}

	private static TLObject at(TLObject object, Revision revision) {
		return WrapperHistoryUtils.getWrapper(revision, object);
	}

	private TLObject partner(String name, Person... members) {
		Group group = Group.createGroup("histsecGroup" + name);
		for (Person member : members) {
			group.addMember(member);
		}
		TLObject partner = newObject("Partner");
		partner.tUpdateByName("name", name);
		partner.tUpdateByName("group", group);
		return partner;
	}

	private TLObject section(String name, TLObject partner) {
		TLObject section = newObject("Section");
		section.tUpdateByName("name", name);
		section.tUpdateByName("partner", partner);
		return section;
	}

	private TLObject release(String name, TLObject partner) {
		TLObject release = newObject("Release");
		release.tUpdateByName("name", name);
		release.tUpdateByName("snapshot", _d);
		release.tUpdateByName("partner", partner);
		return release;
	}

	private TLObject newObject(String className) {
		TLClass type = (TLClass) TLModelUtil.findType(MODULE + ":" + className);
		return DynamicModelService.getFactoryFor(MODULE).createObject(type);
	}

	private Transaction beginTx() {
		return _kb.beginTransaction(com.top_logic.knowledge.service.I18NConstants.NO_COMMIT_MESSAGE);
	}

	/**
	 * The test runs on versioned knowledge bases only: an unversioned one keeps no historic
	 * versions, so a historic reference cannot hold a version apart from the current object.
	 */
	public static Test suite() {
		return KBSetup.wrapTLCommonSetups(KBSetup.getKBTestWithoutSetups(TestHistoricSecurity.class,
			ServiceTestSetup.createStarterFactoryForModules(
				SearchBuilder.Module.INSTANCE,
				ModelService.Module.INSTANCE,
				LabelProviderService.Module.INSTANCE,
				AccessManager.Module.INSTANCE,
				TLSecurityDeviceManager.Module.INSTANCE,
				PersonManager.Module.INSTANCE,
				InitialRolesManager.Module.INSTANCE,
				SecurityConfigurationService.Module.INSTANCE),
			true));
	}

}
