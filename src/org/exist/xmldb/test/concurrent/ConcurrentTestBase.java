/*
 *  eXist Open Source Native XML Database
 *  Copyright (C) 2000-04,  Wolfgang M. Meier (wolfgang@exist-db.org)
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Library General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Library General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 * 
 *  $Id$
 */
package org.exist.xmldb.test.concurrent;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.xmldb.api.base.Collection;
import org.xmldb.api.modules.CollectionManagementService;

import junit.framework.TestCase;

/**
 * Abstract base class for concurrent tests.
 * 
 * @author wolf
 */
public abstract class ConcurrentTestBase extends TestCase {

	protected String rootColURI;
	protected Collection rootCol;
	protected String testColName;
	protected Collection testCol;
	
	protected String[] wordList;
	
	protected List actions = new ArrayList(5);
	
	protected volatile boolean failed = false;
	
	/**
	 * @param name the name of the test.
	 * @param uri the XMLDB URI of the root collection.
	 * @param testCollection the name of the collection that will be created for the test.
	 */
	public ConcurrentTestBase(String name, String uri, String testCollection) {
		super(name);
		this.rootColURI = uri;
		this.testColName = testCollection;
	}

	/**
	 * Add an {@link Action} to the list of actions that will be processed
	 * concurrently. Should be called after {@link #setUp()}.
	 * 
	 * @param action the action.
	 * @param repeat number of times the actions should be repeated.
	 */
	public void addAction(Action action, int repeat, long delay) {
		actions.add(new Runner(action, repeat, delay));
	}
	
	public Collection getTestCollection() {
		return testCol;
	}
	
	public void testConcurrent() throws Exception {
		// start all threads
		for(int i = 0; i < actions.size(); i++) {
			Thread t = (Thread)actions.get(i);
			t.start();
		}
		
		// wait for threads to finish
		try {
			for(int i = 0; i < actions.size(); i++) {
				Thread t = (Thread)actions.get(i);
				t.join();
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		assertFalse(failed);
	}
	
	/*
	 * @see TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		rootCol = DBUtils.setupDB(rootColURI);
		

		testCol = rootCol.getChildCollection(testColName);
		if(testCol != null) {
			CollectionManagementService mgr = DBUtils.getCollectionManagementService(rootCol);
			mgr.removeCollection(testColName);
		}
		
		testCol = DBUtils.addCollection(rootCol, testColName);
		assertNotNull(testCol);
	}

	/*
	 * @see TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		DBUtils.removeCollection(rootCol, testColName);
		DBUtils.shutdownDB(rootColURI);
	}
	
	/**
	 * Runs the specified Action a number of times.
	 * 
	 * @author wolf
	 */
	class Runner extends Thread {
		
		private Action action;
		private int repeat;
		private long delay = 0;
		
		public Runner(Action action, int repeat, long delay) {
			super();
			this.action = action;
			this.repeat = repeat;
			this.delay = delay;
		}
		
		/* (non-Javadoc)
		 * @see java.lang.Thread#run()
		 */
		public void run() {
			try
	        {
	            for (int i = 0; i < repeat; i++)
	            {
	                if (failed)
	                {
	                    break;
	                }

	                failed = action.execute();
	                if(delay > 0)
	                	synchronized(this) {
	                		wait(delay);
	                	}
	            }
	        }
	        catch (Exception e)
	        {
	        	System.err.println("Action failed in Thread " + getName() + ": " + e.getMessage());
	            e.printStackTrace();
	            failed = true;
	        }
		}
	}
}
