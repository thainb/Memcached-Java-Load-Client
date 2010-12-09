package com.yahoo.ycsb.rmi;

import com.yahoo.ycsb.Client;
import com.yahoo.ycsb.SlaveClient;

public class RMIImpl implements RMIInterface{
	Client client;
	
	public RMIImpl(SlaveClient client) {
		super();
		this.client = client;
	}
	
	@Override
	public String execute() {
		return client.execute();
	}

}