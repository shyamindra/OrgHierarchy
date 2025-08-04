package com.momentum.hierarchy.employee;

import com.opencsv.bean.CsvBindByPosition;

public class EmployeeRecord {
	
	@CsvBindByPosition(position = 0)
	private String name;
	
	@CsvBindByPosition(position = 1)
	private String iD;
	
	@CsvBindByPosition(position = 2)
	private String mgrID;
	
	/**
	 * Default constructor required by OpenCSV.
	 */
	public EmployeeRecord() {
	}
	
	/**
	 * Constructor for creating EmployeeRecord instances manually.
	 * 
	 * @param name Employee name
	 * @param id Employee ID
	 * @param mgrId Manager ID (can be null for CEO)
	 */
	public EmployeeRecord(String name, String id, String mgrId) {
		this.name = name;
		this.iD = id;
		this.mgrID = mgrId;
	}
	
	public String getName() {
		return name;
	}
	public String getID() {
		return iD;
	}
	public String getMgrID() {
		return mgrID;
	}
	
	@Override
	public String toString() {
		return "EmployeeRecord [name=" + name + ", iD=" + iD + ", mgrID=" + mgrID + "]";
	}
	
	/*
	 * Converts an EmployeeRecord into an Employee object 
	 */
	public Employee convertToEmployee() {
		return new Employee(this.getName(), this.getID());
	}
}
