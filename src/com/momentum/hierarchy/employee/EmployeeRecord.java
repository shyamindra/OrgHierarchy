package com.momentum.hierarchy.employee;

import com.opencsv.bean.CsvBindByPosition;

public class EmployeeRecord {
	
	@CsvBindByPosition(position = 0)
	private String name;
	
	@CsvBindByPosition(position = 1)
	private String iD;
	
	@CsvBindByPosition(position = 2)
	private String mgrID;
	
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
