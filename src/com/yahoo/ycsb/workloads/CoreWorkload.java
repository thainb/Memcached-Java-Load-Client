package com.yahoo.ycsb.workloads;

import com.yahoo.ycsb.DataStore;
import com.yahoo.ycsb.Workload;

public abstract class CoreWorkload extends Workload{

	@Override
	public abstract boolean doInsert(DataStore db, Object threadstate);

	@Override
	public abstract boolean doTransaction(DataStore db, Object threadstate);
	
	

}
