/*
 *  eXist Open Source Native XML Database
 *  Copyright (C) 2001-07 The eXist Project
 *  http://exist-db.org
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 *  $Id$
 */
package org.exist.indexing;

import org.w3c.dom.Element;
import org.exist.util.DatabaseConfigurationException;
import org.exist.storage.BrokerPool;
import org.exist.storage.btree.DBException;

/**
 * Represents an arbitrary index structure that can be used by eXist. This is the
 * main interface to be registered with the database instance. It provides methods
 * to configure, open and close the index. These methods will be called by the main
 * database instance during startup/shutdown. They don't need to be synchronized.
 */
public interface Index {

    /**
     * Open and configure the index and all resources associated with it. This method
     * is called while the database instance is initializing..
     *
     * @param pool the BrokerPool representing the current database instance.
     * @param dataDir the main data directory where eXist stores its files.
     * @param config the module element which configures this index, as found in conf.xml
     * @throws DatabaseConfigurationException
     */
    void open(BrokerPool pool, String dataDir, Element config) throws DatabaseConfigurationException;

    /**
     * Close the index and all associated resources.
     *
     * @throws DBException
     */
    void close() throws DBException;

    /**
     * Sync the index. This method should make sure that all index contents are written to disk.
     * It will be called during checkpoint events and the system relies on the index to materialize
     * all data.
     *
     * @throws DBException
     */
    void sync() throws DBException;

    /**
     * Create a new IndexWorker, which is used to access the index in a multi-threaded
     * environment.
     *
     * Every database instance has a number of
     * {@link org.exist.storage.DBBroker} objects. All operations on the db
     * have to go through one of these brokers. Each DBBroker retrieves an
     * IndexWorker for every index by calling this method.
     *
     * @return a new IndexWorker that can be used for concurrent access to the index.
     */
    IndexWorker getWorker();
}