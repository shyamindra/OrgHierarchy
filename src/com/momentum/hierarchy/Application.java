package com.momentum.hierarchy;

import java.io.IOException;

import com.momentum.hierarchy.datareader.CSVReader;

public class Application {

	public static void main(String[] args) {
		try {
			new HierarchyBuilder().buildAndPrintHierarchyTree(
					new CSVReader().readCSVAndGetRecords());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
