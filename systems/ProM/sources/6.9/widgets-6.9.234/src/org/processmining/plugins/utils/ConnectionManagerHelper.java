package org.processmining.plugins.utils;

import java.util.ArrayList;
import java.util.Collection;

import org.processmining.framework.connections.Connection;
import org.processmining.framework.connections.ConnectionCannotBeObtained;
import org.processmining.framework.connections.ConnectionID;
import org.processmining.framework.connections.ConnectionManager;

/**
 * Helper methods to work in a 'safer' way with the {@link ConnectionManager} of
 * ProM.
 * 
 * @author F. Mannhardt
 * 
 */
public final class ConnectionManagerHelper {

	private ConnectionManagerHelper() {
		super();
	}

	/**
	 * Returns a collection of connections between the objects specified, such
	 * that the type of the connection is assignable from the given
	 * connectionType. Returns an empty {@link Collection} in case no connection
	 * is available. Does NOT try to automatically create a connection.
	 * 
	 * @param connectionManager
	 * @param connectionType
	 * @param objects
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Connection> Collection<T> safeGetConnections(ConnectionManager connectionManager,
			Class<T> connectionType, Object... objects) {
		Collection<T> connections = new ArrayList<T>(1);
		for (ConnectionID connID : connectionManager.getConnectionIDs()) {
			try {
				Connection c = connectionManager.getConnection(connID);
				if (((connectionType == null) || connectionType.isAssignableFrom(c.getClass()))
						&& c.containsObjects(objects)) {
					connections.add((T) c);
				}
			} catch (ConnectionCannotBeObtained e) {
			}
		}
		return connections;
	}

	/**
	 * Returns the first connection between the objects specified, such that the
	 * type of the connection is assignable from the given connectionType. Throw
	 * {@link ConnectionCannotBeObtained} in case no connection is available.
	 * Does NOT try to automatically create a connection.
	 * 
	 * @param connectionManager
	 * @param connectionType
	 * @param objects
	 * @return
	 * @throws ConnectionCannotBeObtained
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Connection> T safeGetFirstConnection(ConnectionManager connectionManager,
			Class<T> connectionType, Object... objects) throws ConnectionCannotBeObtained {
		for (ConnectionID connID : connectionManager.getConnectionIDs()) {
			Connection c = connectionManager.getConnection(connID);
			if (((connectionType == null) || connectionType.isAssignableFrom(c.getClass()))
					&& c.containsObjects(objects)) {
				return (T) c;
			}
		}
		throw new ConnectionCannotBeObtained("Connection not found", connectionType, objects);
	}

}
