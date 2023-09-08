/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.administration;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.top_logic.base.accesscontrol.Login;
import com.top_logic.base.accesscontrol.Login.InMaintenanceModeException;
import com.top_logic.base.accesscontrol.SessionService;
import com.top_logic.base.cluster.ClusterManager;
import com.top_logic.base.cluster.ClusterManager.PropertyType;
import com.top_logic.base.cluster.ClusterManagerListener;
import com.top_logic.base.services.InitialGroupManager;
import com.top_logic.base.user.UserInterface;
import com.top_logic.basic.Log;
import com.top_logic.basic.Logger;
import com.top_logic.basic.NamedConstant;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable.Property;
import com.top_logic.basic.config.CommaSeparatedStringSet;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.module.ManagedClass;
import com.top_logic.basic.module.ServiceDependencies;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.tool.boundsec.wrap.Group;
import com.top_logic.util.Resources;
import com.top_logic.util.TLContext;
import com.top_logic.util.Utils;
import com.top_logic.util.db.DBUtil;

/**
 * The MaintenanceWindowManager manages the maintenance window mode of the system, including
 * entering and leaving of this mode.
 *
 * @author <a href="mailto:CBR@top-logic.com">CBR</a>
 */
@ServiceDependencies({
	ClusterManager.Module.class,
	PersonManager.Module.class,
	PersistencyLayer.Module.class,
	InitialGroupManager.Module.class, // is needed ensure that the configured allowed groups exist
	SessionService.Module.class,
	Login.Module.class,
})
public final class MaintenanceWindowManager extends ManagedClass implements ClusterManagerListener {

    /**
     * Because of transfer latency between server and client, the time left to maintenance mode
     * shown in the GUI will be smaller for this amount of milliseconds, so that the maintenance
     * mode will not be entered earlier than the user was shown.
     */
    public static final long LATENCY_TIME = 0;

    // maintenance states of the application
    public static final int IN_MAINTENANCE_MODE = 0;
    public static final int ABOUT_TO_ENTER_MAINTENANCE_MODE = 1;
    public static final int DEFAULT_MODE = 2;


    /* Properties for cluster manager */
    public static final String PROPERTY_MESSAGE = "mwm_message";
    public static final String PROPERTY_USER = "mwm_user";
    public static final String PROPERTY_STATE_CHANGE = "mwm_state_change";

    public static final Long CM_STATE_DEFAULT_MODE = Long.valueOf(-1);
    public static final Long CM_STATE_MAINTENANCE_MODE = Long.valueOf(0);

	private static final Property<NamedConstant> TOKEN_KEY = TypedAnnotatable.property(NamedConstant.class, "tokenKey");

	/**
	 * Configuration of the {@link MaintenanceWindowManager}.
	 * 
	 * @since 5.7.5
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends ServiceConfiguration<MaintenanceWindowManager> {

		/** Name of configuration {@link Config#getMinIntervallInCluster()}. */
		String MIN_INTERVALL_IN_CLUSTER_NAME = "min-intervall-in-cluster";

		/** Name of configuration {@link Config#getAllowedGroups()}. */
		String ALLOWED_GROUPS_NAME = "allowed-groups";

		/**
		 * Minimum delay in milliseconds for entering maintenance mode in cluster.
		 */
		@Name(MIN_INTERVALL_IN_CLUSTER_NAME)
		long getMinIntervallInCluster();

		/**
		 * The groups which are allowed to login while system is in maintenance window mode.
		 * 
		 * Empty set means no one is allowed to login except root users.
		 * 
		 */
		@Format(CommaSeparatedStringSet.class)
		@Name(ALLOWED_GROUPS_NAME)
		Set<String> getAllowedGroups();
	}

	/**
	 * The groups which are allowed to login while system is in maintenance window mode.
	 * 
	 * Empty set means no one is allowed to login except root users.
	 */
	public final Set<Group> allowedGroups;

	/** Minimum delay in milliseconds for entering maintenance mode in cluster */
	public final long minIntervallInCluster;

    /** The current maintenance window state of the application. */
    private int state = DEFAULT_MODE;

    /** Saves the MaintenanceWindowTimer, if there runs one at the moment. */
    private MaintenanceWindowTimer timer = null;

    /** The message to inform users about the pending maintenance window mode. */
    private String message = null;

    /** The person which changed the maintenance window state as last. */
    private Person changingUser = null;

    /**
     * Token to identify the TLContext which changed the maintenance window state as last.
     * May be <code>null</code> if changed by another node in cluster.
     */
    private NamedConstant token;

	private final Login _login;

	/**
	 * Creates a new {@link MaintenanceWindowManager} from the given configuration.
	 * 
	 * @param context
	 *        {@link InstantiationContext} to instantiate sub configurations.
	 * @param config
	 *        Configuration for this {@link MaintenanceWindowManager}.
	 * 
	 */
	public MaintenanceWindowManager(InstantiationContext context, Config config) {
		super(context, config);
		allowedGroups = configAllowedGroups(context, config);
		minIntervallInCluster = config.getMinIntervallInCluster();
		_login = Login.getInstance();
	}

	@Override
	protected void startUp() {
		super.startUp();

        ClusterManager cm = ClusterManager.getInstance();
        cm.addClusterMessageListener(this);
		cm.declareValue(PROPERTY_MESSAGE, PropertyType.STRING);
		cm.declareWrapperValue(PROPERTY_USER, Person.OBJECT_NAME);
		cm.declareValue(PROPERTY_STATE_CHANGE, PropertyType.LONG);
        message = cm.getLatestUnconfirmedValue(PROPERTY_MESSAGE);
        changingUser = cm.getLatestUnconfirmedValue(PROPERTY_USER);
        Long value = cm.getLatestUnconfirmedValue(PROPERTY_STATE_CHANGE);
        if (value == null) {
        	cm.setValue(PROPERTY_STATE_CHANGE, CM_STATE_DEFAULT_MODE);
        }
        else {
        	clusterPropertyChanged(PROPERTY_STATE_CHANGE, null, value, false);
        }
    }

	@Override
	public void clusterPropertyChangeConfirmed(String propertyName, Object currentValue) {
        // uninteresting
    }

    @Override
	public synchronized void clusterPropertyChanged(String propertyName, Object oldValue, Object newValue, boolean thisNode) {
        if (PROPERTY_STATE_CHANGE.equals(propertyName)) {
            if (!thisNode) {
                token = null;
            }
            if (CM_STATE_MAINTENANCE_MODE.equals(newValue)) {
                internalEnterMaintenanceWindow();
            }
            else if (CM_STATE_DEFAULT_MODE.equals(newValue)) {
                internalLeaveMaintenanceWindow();
            }
            else {
				// fetching cluster mode is not synchronised
                boolean clusterMode = ClusterManager.getInstance().isClusterMode();
				internalEnterMaintenanceWindow(Utils.getlongValue(newValue), clusterMode);
            }
        }
        else if (PROPERTY_MESSAGE.equals(propertyName)) {
            this.message = (String)newValue;
        }
        else if (PROPERTY_USER.equals(propertyName)) {
            this.changingUser = (Person)newValue;
        }
    }

    /**
     * Callback method for the MaintenanceWindowTimer if the waiting time for entering the
     * maintenance window has expired.
     */
    synchronized void timeUp() {
        timer = null;
        internalEnterMaintenanceWindow();
    }



    /**
     * Gets the singleton instance of this class.
     */
    public static final MaintenanceWindowManager getInstance() {
        return Module.INSTANCE.getImplementationInstance();
    }

    /**
     * Configuration lookup.
     */
	private static final Set<Group> configAllowedGroups(Log log, Config configuration) {
		HashSet<Group> groups = new HashSet<>();
		{
			for (String groupName : configuration.getAllowedGroups()) {
				Group group = Group.getGroupByName(groupName);
				if (group == null) {
					log.error("Failed to get group with name '" + groupName + "'.");
				} else {
					groups.add(group);
				}
			}
        }
        return Collections.unmodifiableSet(groups);
    }

    /**
     * Gets the maintenance window state of the application
     */
	public synchronized int getMaintenanceModeState() {
        return state;
    }



    /**
     * Sets the system immediately into a maintenance window, in which only users within specified
     * groups are allowed to log in. All currently logged in persons, which are not a member of one
     * of the specified groups will get logged out.
     */
	public void enterMaintenanceWindow() {
		/* First we lock the ClusterManager. That is needed, because ClusterManager could be
		 * modified concurrently. In that case the ClusterManager would try to inform this
		 * MaintenanceWindowManager: Deadlock, see TTS 11648, Trac 13768 */
		ClusterManager cm = ClusterManager.getInstance();
		synchronized (cm) {
			synchronized (this) {
				internalEnterMaintenanceMode(cm);
			}
		}
    }

	/**
	 * Must be called within a <code>synchronized(this)</code> block.
	 */
	private void internalEnterMaintenanceMode(ClusterManager cm) {
		if (cm.isClusterMode() && minIntervallInCluster > 1000
			&& getMaintenanceModeState() != IN_MAINTENANCE_MODE) {
			enterMaintenanceWindow(minIntervallInCluster);
		}
		else {
			cm.refetch();
			cm.setValue(PROPERTY_STATE_CHANGE, CM_STATE_MAINTENANCE_MODE);
			setChangingInformations(cm);
		}
	}


    /**
     * Sets the system into a maintenance window after the given delay, in which only users
     * within specified groups are allowed to log in. The currently logged in users get
     * informed about the pending maintenance window mode.
     *
     * @param milliseconds
     *        the delay (in milliseconds) to wait until entering the maintenance window mode
     */
	public void enterMaintenanceWindow(long milliseconds) {
		/* First we lock the ClusterManager. That is needed, because ClusterManager could be
		 * modified concurrently. In that case the ClusterManager would try to inform this
		 * MaintenanceWindowManager: Deadlock, see TTS 11648, Trac 13768 */
		ClusterManager cm = ClusterManager.getInstance();
		synchronized (cm) {
			synchronized (this) {
				// Enter maintenance mode immediately, if the time is less than one second
				if (milliseconds < 1000) {
					internalEnterMaintenanceMode(cm);
				}
				else {
					Long finishTime = Long.valueOf(getDBTime(cm.isClusterMode()) + milliseconds);
					cm.refetch();
					if (getMaintenanceModeState() != IN_MAINTENANCE_MODE) {
						cm.setValue(PROPERTY_STATE_CHANGE, finishTime);
						setChangingInformations(cm);
					}
				}
			}
		}
    }

    /**
     * Leaves the maintenance window and sets the system back into normal mode, in which every user
     * may be allowed to log in.
     */
	public void leaveMaintenanceWindow() {
		/* First we lock the ClusterManager. That is needed, because ClusterManager could be
		 * modified concurrently. In that case the ClusterManager would try to inform this
		 * MaintenanceWindowManager: Deadlock, see TTS 11648, Trac 13768 */
		ClusterManager cm = ClusterManager.getInstance();
		synchronized (cm) {
			synchronized (this) {
				internalLeaveMaintenanceWindow(cm);
			}
		}
    }

	/**
	 * Must be called within a <code>synchronized(this)</code> block.
	 */
	private void internalLeaveMaintenanceWindow(ClusterManager cm) {
		cm.refetch();
		cm.setValue(PROPERTY_STATE_CHANGE, CM_STATE_DEFAULT_MODE);
		setChangingInformations(cm);
	}

    /**
     * Aborts a delayed entering of the maintenance window mode.
     */
	public void abortEnterMaintenanceWindow() {
		/* First we lock the ClusterManager. That is needed, because ClusterManager could be
		 * modified concurrently. In that case the ClusterManager would try to inform this
		 * MaintenanceWindowManager: Deadlock, see TTS 11648, Trac 13768 */
		ClusterManager cm = ClusterManager.getInstance();
		synchronized (cm) {
			synchronized (this) {
				if (timer != null) {
					internalLeaveMaintenanceWindow(cm);
				}
			}
		}
    }



    private synchronized void internalEnterMaintenanceWindow() {
        if (timer != null) {
            timer.interrupt();
            timer = null;
        }
        disableLogin();
        state = IN_MAINTENANCE_MODE;
        logoutUsers();
    }

    private synchronized void internalEnterMaintenanceWindow(long finishTime, boolean clusterMode) {
        if (finishTime <= 0) {
            throw new IllegalArgumentException("Finishtime must not be <= 0.");
        }
		long milliseconds = finishTime - getDBTime(clusterMode);
        // Enter maintenance mode immediately, if the time is less than one second
        if (milliseconds < 1000) {
            internalEnterMaintenanceWindow();
            return;
        }
        if (timer != null) {
            timer.interrupt();
            timer = null;
        }
        disableLogin();
        timer = new MaintenanceWindowTimer(milliseconds);
        timer.start();
        state = ABOUT_TO_ENTER_MAINTENANCE_MODE;
    }

    private synchronized void internalLeaveMaintenanceWindow() {
        if (timer != null) {
            timer.interrupt();
            timer = null;
        }
        state = DEFAULT_MODE;
        enableLogin();
    }



    /**
     * Enables login for all users which were not allowed to be logged in in maintenance window.
     */
    private void enableLogin() {
		_login.setAllowedGroups(null);
    }

    /**
     * Disables login for all users which are not allowed to be logged in in maintenance window.
     */
    private void disableLogin() {
		_login.setAllowedGroups(allowedGroups);
    }

    /**
     * Logs all users out which are not allowed to be logged in in maintenance window.
     */
    private void logoutUsers() {
        SessionService sessions = SessionService.getInstance();
		Collection<String> sessionIDs = sessions.getSessionIDs();
		for (String sessionID : sessionIDs) {
			UserInterface theUser = sessions.getUser(sessionID);
            try {
                Person thePerson = PersonManager.getManager().getPersonByUser(theUser);
				try {
					_login.checkAllowedGroups(thePerson);
				} catch (InMaintenanceModeException ex) {
					// person can not login, therefore log out person.
					sessions.invalidateSession(sessionID);
                }
            } catch (Exception e) {
				StringBuilder logoutFailed = new StringBuilder();
				logoutFailed.append("Failed to log out user ");
				logoutFailed.append(theUser.getUserName());
				logoutFailed.append(".");
				Logger.error(logoutFailed.toString(), e, MaintenanceWindowManager.class);
            }
        }
    }



    /**
     * Sets information about the change of the maintenance window state.
     */
	private void setChangingInformations(ClusterManager cm) {
        TLContext context = TLContext.getContext();
        if (context != null) {
			setChangingUser(cm, context.getCurrentPersonWrapper());
            token = new NamedConstant("currentToken");
            context.set(TOKEN_KEY, token);
        }
        else {
            token = null;
        }
    }



    /**
	 * Gets the current time of the DB in cluster mode, using system time in case of DB errors.
     */
	private long getDBTime(boolean clusterMode) {
		if (clusterMode) try {
            return DBUtil.currentTimeMillis();
        }
        catch (SQLException e) {
            // ignore
        }
        return System.currentTimeMillis();
    }



    /**
     * Returns the time left until the system enters the maintenance window mode, if the
     * system is about to enter maintenance window mode.
     *
     * @return the time left until the system enters the maintenance window mode for GUI
     *         display in milliseconds<br/>
     *         0 if the system is just now about to enter the maintenance window mode<br/>
     *         -1 if the system is not about to enter the maintenance window mode
     * @see #LATENCY_TIME
     */
    public synchronized long getTimeLeft() {
        if (timer == null) return -1;
        long time = timer.getTimeLeft() - LATENCY_TIME;
        return time < 0 ? 0 : time;
    }

    /**
     * Returns the time when the system will enter the maintenance window mode, if the
     * system is about to enter maintenance window mode.
     *
     * @return the time when the system will enter the maintenance window mode for GUI
     *         display in milliseconds<br/>
     *         -1 if the system is not about to enter the maintenance window mode
     * @see #LATENCY_TIME
     */
    public synchronized long getFinishedTime() {
        if (timer == null) return -1;
        return timer.getFinishedTime() - LATENCY_TIME;
    }


    /**
     * Gets the message to inform users about the pending maintenance window mode.
     */
    public synchronized String getMessage() {
        if (message == null) {
			message =
				Resources.getInstance().getString(I18NConstants.ENTERING_MAINTENANCE_MODE);
        }
        return message;
    }

    /**
     * Sets the message to inform users about the pending maintenance window mode
     */
	public void setMessage(String message) {
        ClusterManager.getInstance().setValue(PROPERTY_MESSAGE, message);
    }



    /**
     * This method returns the person who changed the state as last.
     *
     * @return may be <code>null</code>, which indicates that the last who changed the state
     *         was the system or that no one changed the state at all.
     */
    public synchronized Person getChangingUser() {
        return changingUser;
    }

    /**
     * Sets the person who changed the state as last.
     */
	private void setChangingUser(ClusterManager cm, Person person) {
		cm.setValue(PROPERTY_USER, person);
    }



    /**
     * This method decides whether the given context was the last who changed the state of this
     * {@link MaintenanceWindowManager}.
     *
     * @param someContext
     *            the context under test
     * @return <code>true</code> if the given {@link TLContext} is not <code>null</code> and was
     *         the active context at the time the state of the {@link MaintenanceWindowManager} was
     *         changed at last.
     */
    public synchronized boolean isChangingContext(TLContext someContext) {
        return someContext != null && token == someContext.get(TOKEN_KEY);
    }
    
	/**
	 * Call back from MaintenanceWindowTimer on interrupt.
	 */
	void forgetMe(MaintenanceWindowTimer myTimer) {
		if (timer == myTimer) {
			timer = null;
		}
	}

    @Override
    protected void shutDown() {
		if (timer != null) {
			timer.interrupt();
		}

		ClusterManager cm = ClusterManager.getInstance();
        cm.removeClusterMessageListener(this);
        cm.undeclareValue(PROPERTY_MESSAGE);
        cm.undeclareValue(PROPERTY_USER);
        cm.undeclareValue(PROPERTY_STATE_CHANGE);

		if (timer != null) {
			Logger.warn("MaintenanceWindowTimer did not properly stop", MaintenanceWindowManager.class);
		}
		super.shutDown();
    }

	public static final class Module extends TypedRuntimeModule<MaintenanceWindowManager> {

		public static final Module INSTANCE = new Module();

		@Override
		public Class<MaintenanceWindowManager> getImplementation() {
			return MaintenanceWindowManager.class;
		}
	}
}
