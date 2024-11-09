package com.example;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;

public class Sampling {
	public ArrayList<String[]> GetData(String filePath) {
		String csvFile = filePath;
		String line;
		String csvSplitBy = ",";
		
		ArrayList<String[]> dataList = new ArrayList<>();
		
		try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
			br.readLine();  // skip header
			
			while ((line = br.readLine()) != null) {
				String[] values = line.split(csvSplitBy);
				dataList.add(values);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return dataList;
	}
}