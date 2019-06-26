package com.momentum.hierarchy.employee;

import java.util.List;

public class Employee {

	private String name;
	private String iD;
	private List<Employee> reportees;
	
	public Employee(String name, String iD) {
		this.setName(name);
		this.setID(iD);
	}
	
	public Employee(String name, String iD, List<Employee> reportees) {
		this.setName(name);
		this.setID(iD);
		this.setReportees(reportees);
	}
	
	public String getName() {
		return name;
	}
	private void setName(String name) {
		this.name = name;
	}
	public List<Employee> getReportees() {
		return reportees;
	}
	public void setReportees(List<Employee> reportees) {
		this.reportees = reportees;
	}
	public String getID() {
		return iD;
	}
	private void setID(String iD) {
		this.iD = iD;
	}

	@Override
	public String toString() {
		return "Employee [name=" + name + ", iD=" + iD + ", reportees=" + reportees + "]";
	}
	
}
