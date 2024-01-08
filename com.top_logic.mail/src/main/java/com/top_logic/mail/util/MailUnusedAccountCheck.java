/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mail.util;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import com.top_logic.base.mail.MailHelper;
import com.top_logic.base.mail.MailHelper.SendMailResult;
import com.top_logic.base.user.UserInterface;
import com.top_logic.basic.AliasManager;
import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.FileManager;
import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.CommaSeparatedStrings;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.knowledge.monitor.UnusedAccountCheck;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.util.Resources;

/**
 * {@link UnusedAccountCheck} which informs users about unused accounts via Mail.
 *
 * @author <a href="mailto:Christian.Braun@top-logic.com">Christian Braun</a>
 */
public class MailUnusedAccountCheck extends UnusedAccountCheck {

    /** Config attribute to enable / disable sending notification mails. */
    public static final String CONF_SEND_MAILS = "sendMails";

    /** Config attribute for administrator email address who gets informed also per mail. */
    public static final String CONF_ADMIN_EMAIL_ADDRESSES = "adminEmailAddresses";

    /** Flag whether sending notification mails is enabled or disabled. */
    protected final boolean sendMails;

    /** administrator email address who gets informed also per mail; may be <code>null</code> */
    protected final List<String> adminEmailAddresses;

	/**
	 * Configuration for {@link MailUnusedAccountCheck}.
	 * 
	 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
	 */
	public interface Config extends UnusedAccountCheck.Config {
		/**
		 * True if its able to send mails.
		 */
		@BooleanDefault(true)
		boolean canSendMails();

		/**
		 * A list of admin email addresses.
		 */
		@Format(CommaSeparatedStrings.class)
		List<String> getAdminEmailAddresses();
	}

    /**
	 * @param context
	 *        {@link InstantiationContext} to instantiate sub configurations.
	 * @param config
	 *        Configuration for {@link MailUnusedAccountCheck}.
	 * 
	 *        Creates a new instance of this class.
	 */
	public MailUnusedAccountCheck(InstantiationContext context, Config config) {
		super(context, config);
		sendMails = config.canSendMails();
		adminEmailAddresses = config.getAdminEmailAddresses();
    }



    /**
     * Sends a notification email to the given person to inform about the unused account.
     *
     * @param person
     *        the person / account which is unused
     * @param dayDiff
     *        the amount of days the account wasn't used
     * @param deleteAccount
     *        flag whether the account gets deleted now (<code>true</code>) or this is just
     *        a reminder (<code>false</code>)
     */
    protected void sendEmail(Person person, int dayDiff, boolean deleteAccount) {
		UserInterface user = person.getUser();
		if (user == null) {
			return;
		}
		String emailAdress = user.getEMail();
        if (!StringServices.isEmpty(emailAdress)) try {
				int deleteDayCount = getConfig().getDeleteDayCount();
				Locale locale = Resources.findBestLocale(person);
            String filename = "/WEB-INF/reportTemplates/html/";
            if (deleteAccount) {
                filename += "DeleteAccountTemplate";
            }
            else {
					filename += (deleteDayCount > 0 ? "UnusedAccountTemplate" : "UnusedAccountNoDeleteTemplate");
            }
            filename += "_" + locale.getLanguage() + ".html";
            BinaryData file = FileManager.getInstance().getDataOrNull(filename);
            if (file == null) {
            	Logger.error("Cannot find template file (" + filename + ")", MailUnusedAccountCheck.class);
            }
            else {
                Resources res = Resources.getInstance(locale);
					String appName = res.getString(I18NConstants.APP_NAME);

                String content = FileUtilities.readFileToString(file);
                content = content.replace("%APP_NAME%", appName);
                content = content.replace("%LOGIN_NAME%", person.getName());
                content = content.replace("%DAY_DIFF%", Integer.toString(dayDiff));
					content = content.replace("%DELETE_DAYS_COUNT%", Integer.toString(deleteDayCount));
					String link = getAppLink(appName);
					if (link != null) {
						content = content.replace("%HOMEPAGE_LINK%", link);
					}
					String subject = res.getString(I18NConstants.SUBJECT__APPLICATION_NAME.fill(appName));

                if (sendMails) {
                    SendMailResult result = MailHelper.getInstance().sendSystemMail(CollectionUtil.intoList(emailAdress), adminEmailAddresses,
                        Collections.emptyList(), subject, content, Collections.emptyList(), MailHelper.CONTENT_TYPE_HTML);
                    if (result.isSuccess()) {
							Logger.debug("E-Mail to '" + emailAdress + "' succeeded.", MailUnusedAccountCheck.class);
                    }
                    else {
							Logger.warn(
								"Failed to send email to '" + emailAdress + "': " + result.getErrorResultString(),
								result.getException(), MailUnusedAccountCheck.class);
							Logger.debug("E-Mail to '" + emailAdress + "' failed.", MailUnusedAccountCheck.class);
                    }
                }
                else {
						Logger.debug("E-Mail to '" + emailAdress + "' not send as configured.",
							MailUnusedAccountCheck.class);
                }
					Logger.debug(content, MailUnusedAccountCheck.class);
            }
        }
        catch (IOException e) {
				Logger.error("Failed to read email notification template.", e, MailUnusedAccountCheck.class);
        }
        else {
			Logger.info("Can't send email notification to '" + person.getName() + "' as person has no email adress.",
				MailUnusedAccountCheck.class);
        }
    }

	private String getAppLink(String appName) {
		String host = AliasManager.getInstance().getAlias(AliasManager.HOST);
		String appContext = AliasManager.getInstance().getAlias(AliasManager.APP_CONTEXT);
		if (host != null && appContext != null) {
			return "<a href=\"" + host + appContext + "\">" + appName + "</a>";
		}
		return null;
	}



    @Override
    protected void notifyUnused(Person person, int dayDiff) {
        sendEmail(person, dayDiff, false);
        super.notifyUnused(person, dayDiff);
    }

    @Override
    protected void deleteUnusedAccount(Person person, int dayDiff) {
        sendEmail(person, dayDiff, true);
        super.deleteUnusedAccount(person, dayDiff);
    }

}
