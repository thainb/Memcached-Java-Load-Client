/**
 * Copyright (c) 2010 Yahoo! Inc. All rights reserved. 
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you
 * may not use this file except in compliance with the License. You
 * may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0 
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * permissions and limitations under the License. See accompanying
 * LICENSE file.
 */

package com.yahoo.ycsb.workloads;

import java.util.Properties;
import com.yahoo.ycsb.*;
import com.yahoo.ycsb.database.DB;
import com.yahoo.ycsb.generator.CounterGenerator;
import com.yahoo.ycsb.generator.DiscreteGenerator;
import com.yahoo.ycsb.generator.Generator;
import com.yahoo.ycsb.generator.IntegerGenerator;
import com.yahoo.ycsb.generator.ScrambledZipfianGenerator;
import com.yahoo.ycsb.generator.SkewedLatestGenerator;
import com.yahoo.ycsb.generator.UniformIntegerGenerator;
import com.yahoo.ycsb.generator.ZipfianGenerator;
import com.yahoo.ycsb.measurements.Measurements;
import com.yahoo.ycsb.memcached.Memcached;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Vector;

/**
 * The core benchmark scenario. Represents a set of clients doing simple CRUD
 * operations. The relative proportion of different kinds of operations, and
 * other properties of the workload, are controlled by parameters specified at
 * runtime.
 * 
 * Properties to control the client:
 * <UL>
 * <LI><b>fieldcount</b>: the number of fields in a record (default: 10)
 * <LI><b>fieldlength</b>: the size of each field (default: 100)
 * <LI><b>readallfields</b>: should reads read all fields (true) or just one
 * (false) (default: true)
 * <LI><b>writeallfields</b>: should updates and read/modify/writes update all
 * fields (true) or just one (false) (default: false)
 * <LI><b>readproportion</b>: what proportion of operations should be reads
 * (default: 0.95)
 * <LI><b>updateproportion</b>: what proportion of operations should be updates
 * (default: 0.05)
 * <LI><b>insertproportion</b>: what proportion of operations should be inserts
 * (default: 0)
 * <LI><b>scanproportion</b>: what proportion of operations should be scans
 * (default: 0)
 * <LI><b>readmodifywriteproportion</b>: what proportion of operations should be
 * read a record, modify it, write it back (default: 0)
 * <LI><b>requestdistribution</b>: what distribution should be used to select
 * the records to operate on - uniform, zipfian or latest (default: uniform)
 * <LI><b>maxscanlength</b>: for scans, what is the maximum number of records to
 * scan (default: 1000)
 * <LI><b>scanlengthdistribution</b>: for scans, what distribution should be
 * used to choose the number of records to scan, for each scan, between 1 and
 * maxscanlength (default: uniform)
 * <LI><b>insertorder</b>: should records be inserted in order by key
 * ("ordered"), or in hashed order ("hashed") (default: hashed)
 * </ul>
 */
public class MemcachedCoreWorkload extends Workload {
	
	// The prefix to be added to each key.
	public static final String KEY_PREFIX_PROPERTY = "keyprefix";
	public static final String KEY_PREFIX_PROPERTY_DEFAULT = "user";
	public static String keyprefix;

	// The name of the property for the proportion of transactions that are adds.
	public static final String ADD_PROPORTION_PROPERTY = "memaddproportion";
	public static final String ADD_PROPORTION_PROPERTY_DEFAULT = "0.0";
	double addproportion;
	
	// The name of the property for the proportion of transactions that are appends.
	public static final String APPEND_PROPORTION_PROPERTY = "memappendproportion";
	public static final String APPEND_PROPORTION_PROPERTY_DEFAULT = "0.0";
	double appendproportion;
	
	// The name of the property for the proportion of transactions that are cas.
	public static final String CAS_PROPORTION_PROPERTY = "memcasproportion";
	public static final String CAS_PROPORTION_PROPERTY_DEFAULT = "0.0";
	double casproportion;
	
	// The name of the property for the proportion of transactions that are decrs.
	public static final String DECR_PROPORTION_PROPERTY = "memdecrproportion";
	public static final String DECR_PROPORTION_PROPERTY_DEFAULT = "0.0";
	double decrproportion;
	
	// The name of the property for the proportion of transactions that are decrs.
	public static final String DELETE_PROPORTION_PROPERTY = "memdeleteproportion";
	public static final String DELETE_PROPORTION_PROPERTY_DEFAULT = "0.0";
	double deleteproportion;
	
	// The name of the property for the proportion of transactions that are gets.
	public static final String GET_PROPORTION_PROPERTY = "memgetproportion";
	public static final String GET_PROPORTION_PROPERTY_DEFAULT = "0.95";
	double getproportion;
	
	// The name of the property for the proportion of transactions that are gets's.
	public static final String GETS_PROPORTION_PROPERTY = "memgetsproportion";
	public static final String GETS_PROPORTION_PROPERTY_DEFAULT = "0.0";
	double getsproportion;
	
	// The name of the property for the proportion of transactions that are incrs.
	public static final String INCR_PROPORTION_PROPERTY = "memincrproportion";
	public static final String INCR_PROPORTION_PROPERTY_DEFAULT = "0.0";
	double incrproportion;
	
	// The name of the property for the proportion of transactions that are prepends.
	public static final String PREPEND_PROPORTION_PROPERTY = "memprependproportion";
	public static final String PREPEND_PROPORTION_PROPERTY_DEFAULT = "0.0";
	double prependproportion;
	
	// The name of the property for the proportion of transactions that are replaces.
	public static final String REPLACE_PROPORTION_PROPERTY = "memreplaceproportion";
	public static final String REPLACE_PROPORTION_PROPERTY_DEFAULT = "0.0";
	double replaceproportion;
	
	// The name of the property for the proportion of transactions that are sets.
	public static final String SET_PROPORTION_PROPERTY = "memsetproportion";
	public static final String SET_PROPORTION_PROPERTY_DEFAULT = "0.05";
	double setproportion;
	
	// The length of the value to store with a key
	public static final String VALUE_LENGTH_PROPERTY = "valuelength";
	public static final String VALUE_LENGTH_PROPERTY_DEFAULT = "256";
	int valuelength;
	
	
	
	
	
	
	
	
	

	/**
	 * The name of the database table to run queries against.
	 */
	public static final String TABLENAME_PROPERTY = "table";

	/**
	 * The default name of the database table to run queries against.
	 */
	public static final String TABLENAME_PROPERTY_DEFAULT = "usertable";

	public static String table;

	/**
	 * The name of the property for the number of fields in a record.
	 */
	public static final String FIELD_COUNT_PROPERTY = "fieldcount";

	/**
	 * Default number of fields in a record.
	 */
	public static final String FIELD_COUNT_PROPERTY_DEFAULT = "10";

	int fieldcount;

	/**
	 * The name of the property for deciding whether to read one field (false)
	 * or all fields (true) of a record.
	 */
	public static final String READ_ALL_FIELDS_PROPERTY = "readallfields";

	/**
	 * The default value for the readallfields property.
	 */
	public static final String READ_ALL_FIELDS_PROPERTY_DEFAULT = "true";

	boolean readallfields;

	/**
	 * The name of the property for deciding whether to write one field (false)
	 * or all fields (true) of a record.
	 */
	public static final String WRITE_ALL_FIELDS_PROPERTY = "writeallfields";

	/**
	 * The default value for the writeallfields property.
	 */
	public static final String WRITE_ALL_FIELDS_PROPERTY_DEFAULT = "false";

	boolean writeallfields;

	/**
	 * The name of the property for the the distribution of requests across the
	 * keyspace. Options are "uniform", "zipfian" and "latest"
	 */
	public static final String REQUEST_DISTRIBUTION_PROPERTY = "requestdistribution";

	/**
	 * The default distribution of requests across the keyspace
	 */
	public static final String REQUEST_DISTRIBUTION_PROPERTY_DEFAULT = "uniform";

	/**
	 * The name of the property for the max scan length (number of records)
	 */
	public static final String MAX_SCAN_LENGTH_PROPERTY = "maxscanlength";

	/**
	 * The default max scan length.
	 */
	public static final String MAX_SCAN_LENGTH_PROPERTY_DEFAULT = "1000";

	/**
	 * The name of the property for the scan length distribution. Options are
	 * "uniform" and "zipfian" (favoring short scans)
	 */
	public static final String SCAN_LENGTH_DISTRIBUTION_PROPERTY = "scanlengthdistribution";

	/**
	 * The default max scan length.
	 */
	public static final String SCAN_LENGTH_DISTRIBUTION_PROPERTY_DEFAULT = "uniform";

	/**
	 * The name of the property for the order to insert records. Options are
	 * "ordered" or "hashed"
	 */
	public static final String INSERT_ORDER_PROPERTY = "insertorder";

	/**
	 * Default insert order.
	 */
	public static final String INSERT_ORDER_PROPERTY_DEFAULT = "hashed";

	IntegerGenerator keysequence;

	DiscreteGenerator operationchooser;

	IntegerGenerator keychooser;

	Generator fieldchooser;

	CounterGenerator transactioninsertkeysequence;

	IntegerGenerator scanlength;

	boolean orderedinserts;

	int recordcount;

	/**
	 * Initialize the scenario. Called once, in the main client thread, before
	 * any operations are started.
	 */
	public void init(Properties p) throws WorkloadException {
		table = p.getProperty(TABLENAME_PROPERTY, TABLENAME_PROPERTY_DEFAULT);
		fieldcount = Integer.parseInt(p.getProperty(FIELD_COUNT_PROPERTY, FIELD_COUNT_PROPERTY_DEFAULT));
		valuelength = Integer.parseInt(p.getProperty(VALUE_LENGTH_PROPERTY, VALUE_LENGTH_PROPERTY_DEFAULT));
		double getproportion = Double.parseDouble(p.getProperty(GET_PROPORTION_PROPERTY, GET_PROPORTION_PROPERTY_DEFAULT));
		double setproportion = Double.parseDouble(p.getProperty(SET_PROPORTION_PROPERTY, SET_PROPORTION_PROPERTY_DEFAULT));
		recordcount = Integer.parseInt(p.getProperty(Client.RECORD_COUNT_PROPERTY));
		String requestdistrib = p.getProperty(REQUEST_DISTRIBUTION_PROPERTY, REQUEST_DISTRIBUTION_PROPERTY_DEFAULT);
		int maxscanlength = Integer.parseInt(p.getProperty( MAX_SCAN_LENGTH_PROPERTY, MAX_SCAN_LENGTH_PROPERTY_DEFAULT));
		String scanlengthdistrib = p.getProperty(SCAN_LENGTH_DISTRIBUTION_PROPERTY, SCAN_LENGTH_DISTRIBUTION_PROPERTY_DEFAULT);
		int insertstart = Integer.parseInt(p.getProperty(INSERT_START_PROPERTY, INSERT_START_PROPERTY_DEFAULT));
		keyprefix = p.getProperty(KEY_PREFIX_PROPERTY, KEY_PREFIX_PROPERTY_DEFAULT);
		readallfields = Boolean.parseBoolean(p.getProperty(READ_ALL_FIELDS_PROPERTY, READ_ALL_FIELDS_PROPERTY_DEFAULT));
		writeallfields = Boolean.parseBoolean(p.getProperty(WRITE_ALL_FIELDS_PROPERTY, WRITE_ALL_FIELDS_PROPERTY_DEFAULT));

		if (p.getProperty(INSERT_ORDER_PROPERTY, INSERT_ORDER_PROPERTY_DEFAULT)
				.compareTo("hashed") == 0) {
			orderedinserts = false;
		} else {
			orderedinserts = true;
		}

		keysequence = new CounterGenerator(insertstart);
		operationchooser = new DiscreteGenerator();
		if (getproportion > 0) {
			operationchooser.addValue(getproportion, "GET");
		}
		
		if (setproportion > 0) {
			operationchooser.addValue(setproportion, "SET");
		}
		
		

		transactioninsertkeysequence = new CounterGenerator(recordcount);
		if (requestdistrib.compareTo("uniform") == 0) {
			keychooser = new UniformIntegerGenerator(0, recordcount - 1);
		} else if (requestdistrib.compareTo("zipfian") == 0) {
			// it does this by generating a random "next key" in part by taking
			// the modulus over the number of keys
			// if the number of keys changes, this would shift the modulus, and
			// we don't want that to change which keys are popular
			// so we'll actually construct the scrambled zipfian generator with
			// a keyspace that is larger than exists at the beginning
			// of the test. that is, we'll predict the number of inserts, and
			// tell the scrambled zipfian generator the number of existing keys
			// plus the number of predicted keys as the total keyspace. then, if
			// the generator picks a key that hasn't been inserted yet, will
			// just ignore it and pick another key. this way, the size of the
			// keyspace doesn't change from the perspective of the scrambled
			// zipfian generator

			int opcount = Integer.parseInt(p
					.getProperty(Client.OPERATION_COUNT_PROPERTY));
			int expectednewkeys = (int) (((double) opcount) * setproportion * 2.0); // 2
																						// is
																						// fudge
																						// factor

			keychooser = new ScrambledZipfianGenerator(recordcount
					+ expectednewkeys);
		} else if (requestdistrib.compareTo("latest") == 0) {
			keychooser = new SkewedLatestGenerator(transactioninsertkeysequence);
		} else {
			throw new WorkloadException("Unknown distribution \""
					+ requestdistrib + "\"");
		}

		fieldchooser = new UniformIntegerGenerator(0, fieldcount - 1);

		if (scanlengthdistrib.compareTo("uniform") == 0) {
			scanlength = new UniformIntegerGenerator(1, maxscanlength);
		} else if (scanlengthdistrib.compareTo("zipfian") == 0) {
			scanlength = new ZipfianGenerator(1, maxscanlength);
		} else {
			throw new WorkloadException("Distribution \"" + scanlengthdistrib
					+ "\" not allowed for scan length");
		}
	}

	/**
	 * Do one insert operation. Because it will be called concurrently from
	 * multiple client threads, this function must be thread safe. However,
	 * avoid synchronized, or the threads will block waiting for each other, and
	 * it will be difficult to reach the target throughput. Ideally, this
	 * function would have no side effects other than DB operations.
	 */
	public boolean doInsert(DataStore memcached, Object threadstate) {
		int keynum = keysequence.nextInt();
		if (!orderedinserts) {
			keynum = Utils.hash(keynum);
		}
		String dbkey = keyprefix + keynum;
		String value = Utils.ASCIIString(valuelength);
		
		if (((Memcached)memcached).set(dbkey, value) == 0)
			return true;
		else
			return false;
	}

	/**
	 * Do one transaction operation. Because it will be called concurrently from
	 * multiple client threads, this function must be thread safe. However,
	 * avoid synchronized, or the threads will block waiting for each other, and
	 * it will be difficult to reach the target throughput. Ideally, this
	 * function would have no side effects other than DB operations.
	 */
	public boolean doTransaction(DataStore memcached, Object threadstate) {
		String op = operationchooser.nextString();

		if (op.compareTo("GET") == 0) {
			doTransactionGet((Memcached)memcached);
		} else if (op.compareTo("SET") == 0) {
			doTransactionSet((Memcached)memcached);
		}

		return true;
	}

	public void doTransactionGet(Memcached memcached) {
		// choose a random key
		int keynum;
		do {
			keynum = keychooser.nextInt();
		} while (keynum > transactioninsertkeysequence.lastInt());

		if (!orderedinserts) {
			keynum = Utils.hash(keynum);
		}
		String keyname = "user" + keynum;

		memcached.get(keyname, null);
	}
	
	public void doTransactionSet(Memcached memcached) {
		// choose the next key
		int keynum = transactioninsertkeysequence.nextInt();
		if (!orderedinserts) {
			keynum = Utils.hash(keynum);
		}
		String dbkey = keyprefix + keynum;
		String value = Utils.ASCIIString(valuelength);
		memcached.set(dbkey, value);
	}
}