/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.monitor;

import static com.top_logic.knowledge.search.ExpressionFactory.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.dob.DataObjectException;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.search.ExpressionFactory;
import com.top_logic.knowledge.search.RevisionQuery;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.AbstractWrapper;
import com.top_logic.knowledge.wrap.WrapperFactory;

/**
 * This wrapper represents an failed login.
 *
 * @author <a href="mailto:Christian.Braun@top-logic.com">Christian Braun</a>
 */
public class FailedLogin extends AbstractWrapper {

    /** KO type for this wrapper. */
    public static final String KO_TYPE = "FailedLogin";

    public static final String USER_NAME = "name";
    public static final String CLIENT_IP = "ip";
    public static final String SERVER = "server";
    public static final String REASON = "reason";
    public static final String DATE = "date";

    // reason keys for failed logins
    public static final String REASON_NO_PERSON = "noPerson";
    public static final String REASON_NO_PASSWORD = "noPassword";
    public static final String REASON_PERSON_TOO_LONG = "personTooLong";
    public static final String REASON_PWD_TOO_LONG = "pwdTooLong";
    public static final String REASON_BOTH_TOO_LONG = "bothTooLong";
    public static final String REASON_UNKNOWN_PERSON = "unknownPerson";
    public static final String REASON_NO_AUTH_DEVICE = "noAuthDevice";
    public static final String REASON_AUTH_DEVICE_NOT_FOUND = "authDeviceNotFound";
    public static final String REASON_PWD_VALIDATION_FAILED = "pwdValidationFailed";

	/** Message in case of number of users exceed the license specification. */
	public static final String REASON_MAX_USERS_EXCEEDED = "maxUsersExceeded";
    public static final String REASON_USER_NOT_VALID = "userNotValid";
    public static final String REASON_MAINTENANCE_MODE = "maintenanceMode";

	public static final String NO_CLIENT_IP = "noClientIP";

	public static final String EMPTY_USER_NAME = "empty_user_name";

    /**
     * Creates a new instance of this class.
     */
    public FailedLogin(KnowledgeObject ko) {
        super(ko);
    }



    /**
	 * Creates a new entry in the failed login table with the given parameters.
	 * 
	 * @param loginName
	 *        Failed login name. If empty or <code>null</code>, {@link #EMPTY_USER_NAME} is stored.
	 * @param clientIP
	 *        IP address identifying the client which tries the login. If empty or <code>null</code>
	 *        , {@link #NO_CLIENT_IP} is stored.
	 */
    public static void storeNewFailedLogin(String loginName, String clientIP, String reason) {
    	if (StringServices.isEmpty(loginName)) {
			loginName = EMPTY_USER_NAME;
		}
		if (StringServices.isEmpty(clientIP)) {
			clientIP = NO_CLIENT_IP;
    	}
        KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
        Transaction transaction = kb.beginTransaction();
        try {
			KnowledgeObject ko = kb.createKnowledgeObject(KO_TYPE);
            ko.setAttributeValue(USER_NAME, loginName);
            ko.setAttributeValue(CLIENT_IP, clientIP);
            ko.setAttributeValue(SERVER, UserSession.getServerName());
            ko.setAttributeValue(REASON, reason);
            ko.setAttributeValue(DATE, new Date());
            transaction.commit();
        } catch (DataObjectException e) {
            Logger.error("Failed to store failed login.", e, FailedLogin.class);
        } finally {
            transaction.rollback();
        }
    }

    /**
     * Gets all failed login entries within the given date range.
     */
    public static List<FailedLogin> getFailedLogins(Date start, Date end) {

		RevisionQuery<KnowledgeObject> query = ExpressionFactory.queryUnresolved(ExpressionFactory.filter(ExpressionFactory.allOf(FailedLogin.KO_TYPE),
		    ExpressionFactory.and(
		        ExpressionFactory.ge(ExpressionFactory.attribute(KO_TYPE, DATE), ExpressionFactory.literal(start)),
		        ExpressionFactory.le(ExpressionFactory.attribute(KO_TYPE, DATE), ExpressionFactory.literal(end))
		    )
		), orderDesc(attribute(KO_TYPE, DATE)));

        List<?> searchResult = PersistencyLayer.getKnowledgeBase().search(query);
        int length = searchResult.size();
        List<FailedLogin> result = new ArrayList<>(length);
        for (int i = 0; i < length; i++) {
            FailedLogin entry = (FailedLogin)WrapperFactory.getWrapper((KnowledgeObject)searchResult.get(i));
            if (entry != null) result.add(entry);
        }

        return result;
    }

}
