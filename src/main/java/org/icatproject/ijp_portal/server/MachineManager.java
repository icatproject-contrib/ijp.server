package org.icatproject.ijp_portal.server;

import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.icatproject.ijp_portal.server.ejb.entity.Account;
import org.icatproject.ijp_portal.server.ejb.session.MachineEJB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.icatproject.ijp_portal.shared.ServerException;

public class MachineManager {

	final static Logger logger = LoggerFactory.getLogger(MachineManager.class);

	private LoadFinder loadFinder;
	private Pbs pbs;
	private MachineEJB machineEJB;

	public MachineManager(MachineEJB machineEJB) throws ServerException {
		this.machineEJB = machineEJB;
		loadFinder = new LoadFinder();
		pbs = new Pbs();
		logger.debug("Machine Manager Initialised");
	}

	public Account prepareMachine(String userName, String sessionId, Long dsid, String command)
			throws ServerException {
		Set<String> machines = new HashSet<String>();
		Map<String, Float> loads = loadFinder.getLoads();
		Map<String, String> avail = pbs.getStates();
		for (Entry<String, String> pair : avail.entrySet()) {
			boolean online = true;
			for (String state : pair.getValue().split(",")) {
				if (state.equals("offline")) {
					online = false;
					break;
				}
			}
			if (online) {
				logger.debug(pair.getKey() + " is currently online");
				machines.add(pair.getKey());
			}
		}
		if (machines.isEmpty()) {
			machines = avail.keySet();
			if (machines.isEmpty()) {
				throw new ServerException("No machines available");
			}
		}

		String lightest = null;
		for (String machine : machines) {
			if (lightest == null || loads.get(machine) < loads.get(lightest)) {
				lightest = machine;
			}
		}

		pbs.setOffline(lightest);
		return machineEJB.getAccount(lightest, userName, sessionId, dsid, command);

	}
}