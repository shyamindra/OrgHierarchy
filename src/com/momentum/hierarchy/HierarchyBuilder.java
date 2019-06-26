package com.momentum.hierarchy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.momentum.hierarchy.employee.Employee;
import com.momentum.hierarchy.employee.EmployeeRecord;

public class HierarchyBuilder {
	
	private Employee ceo;
	private Map<String, Employee> employeeMap;
	private Map<String, List<Employee>>  mgrEmployeeMap;

	public HierarchyBuilder() {
		employeeMap = new HashMap<String, Employee>();
		mgrEmployeeMap = new HashMap<String, List<Employee>>();
	}
	
	private void buildEmployeeMap(List<EmployeeRecord> employeeRecords){
		for(EmployeeRecord employeeRecord : employeeRecords) {
			employeeMap.put(employeeRecord.getID(), employeeRecord.convertToEmployee());
			if(employeeRecord.getMgrID() == null) {
				this.ceo = employeeRecord.convertToEmployee();
			}
		}
	}
	
	/*
	 * builds a map of Mgr IDs and the corresponding reportees for the Mgr
	 * Enables retrieval of reportees directly from the map instead of 
	 * iterating for each record 
	 * 
	 */
	private void buildMgrEmployeeMap(List<EmployeeRecord> employeeRecords) {
		for(EmployeeRecord employeeRecord : employeeRecords) {
			String mgrId = employeeRecord.getMgrID();
			List<Employee> mgrList = mgrEmployeeMap.get(mgrId);
			if(mgrList == null) {
				mgrList = new ArrayList<Employee>();
			}
			mgrList.add(employeeRecord.convertToEmployee());
			mgrEmployeeMap.put(mgrId, mgrList);
		}
	}
	
	
	/*
	 * The hierarchy tree considers the ceo as the root node
	 * and recursively keeps adding the reportees to the child nodes
	 * 
	 * Assumption is that the CEO is the only person without a manager
	 */
	private void buildHierarchyTree(Employee root) {
		Employee employee = root;
		List<Employee> reportees = mgrEmployeeMap.get(employee.getID());
		employee.setReportees(reportees);
        if (reportees == null || reportees.size() == 0) {
            return;
        }
        for (Employee e : reportees) {
        	buildHierarchyTree(e);
        }
	}
	
	
	/*
	 * recursively prints each node starting from the ceo, i.e. the root node
	 *  
	 */
	private String printHierarchyTree(Employee root, int level) {
		 StringBuilder builder = new StringBuilder();
		    for(int i = 0; i < level; i++){
		        builder.append("\t");
		    }
		    builder.append("- " + root.getName());
		    builder.append("\n");
		    List<Employee> reportees = root.getReportees();
			if(null != reportees) {
				 for (Employee employee : reportees) {
					 builder.append(printHierarchyTree(employee, level + 1));
				 }
			}
		    return builder.toString();
	}
	
	public void buildAndPrintHierarchyTree(List<EmployeeRecord> employeeRecords) {
		buildEmployeeMap(employeeRecords);
		buildMgrEmployeeMap(employeeRecords);
		buildHierarchyTree(ceo);
		System.out.println(printHierarchyTree(ceo, 0));
	}
	
}
