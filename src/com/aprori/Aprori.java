package com.aprori;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Aprori {
	private static double MIN_SUPPORT = 0.4;
	private static List<List<String>> records = new ArrayList<>();

	public static void main(String[] args) {
		records = fetchRecords();
		List<Patterns> itemWithSupports = initialItemWithSupport(records);
		try (FileWriter fw = new FileWriter(new File("patterns.txt")); BufferedWriter bw = new BufferedWriter(fw);) {
			itemWithSupports = pruned(itemWithSupports);
			writeToFile(itemWithSupports, bw);
			while (!itemWithSupports.isEmpty()) {
				Set<Set<String>> previousSet = getPreviousSet(itemWithSupports);
				Set<Set<String>> candidateItemSet = getCandidateItemsFromPreviousSet(previousSet);
				itemWithSupports = getItemWithSupport(candidateItemSet);
				itemWithSupports = pruned(itemWithSupports);
				writeToFile(itemWithSupports, bw);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}

	private static List<Patterns> initialItemWithSupport(List<List<String>> records) {
		List<Patterns> itemWithSupports = new ArrayList<>();
		for (List<String> record : records) {
			for (String item : record) {
				boolean isPresent = false;
				for (Patterns itemWithSupport : itemWithSupports) {
					if (itemWithSupport.getCategories().contains(item)) {
						itemWithSupport.setSupport(itemWithSupport.getSupport() + 1);
						isPresent = true;
					}
				}
				if (!isPresent) {
					Patterns itemWithSupport = new Patterns();
					itemWithSupport.getCategories().add(item);
					itemWithSupport.setSupport(1);
					itemWithSupports.add(itemWithSupport);
				}
			}
		}
		return itemWithSupports;
	}

	private static Set<Set<String>> getPreviousSet(List<Patterns> itemWithSupports) {
		Set<Set<String>> previousSet = itemWithSupports.stream()
				.map(patterns -> patterns.getCategories().stream().collect(Collectors.toSet()))
				.collect(Collectors.toSet());
		return previousSet;
	}

	private static List<Patterns> getItemWithSupport(Set<Set<String>> candidateItemSet) {
		List<Patterns> itemWithSupport = new ArrayList<>();
		for (Set<String> items : candidateItemSet) {
			Patterns p = new Patterns();
			for (List<String> record : records) {
				int matchSize = 0;
				for (String item : items) {
					if (record.contains(item)) {
						matchSize++;
					}
				}
				if (matchSize == items.size()) {
					p.setSupport(p.getSupport() + 1);
				}
			}
			p.setCategories(items.stream().collect(Collectors.toList()));
			itemWithSupport.add(p);
		}
		return itemWithSupport;
	}

	private static Set<Set<String>> getCandidateItemsFromPreviousSet(Set<Set<String>> previousSet) {

		Set<Set<String>> candidateItemsSet = new HashSet<>();

		Set<Set<String>> tmppreviousSet = new HashSet<>();
		tmppreviousSet.addAll(previousSet);
		for (Set<String> itemSet : previousSet) {
			tmppreviousSet.remove(itemSet);

			for (Set<String> tmpItemSet : tmppreviousSet) {
				// Convert the Set of String to String
				StringBuilder newItems = new StringBuilder(String.join(",", tmpItemSet));
				newItems.append(",").append(String.join(",", itemSet));
				Set<String> newItemSet = new HashSet<>();
				Collections.addAll(newItemSet, newItems.toString().split(","));
				candidateItemsSet.add(newItemSet);
			}
		}
		return candidateItemsSet;
	}

	private static List<Patterns> pruned(List<Patterns> itemWithSupports) {
		return itemWithSupports.stream().filter(pattern -> records.size() * MIN_SUPPORT < pattern.getSupport())
				.collect(Collectors.toList());
	}

	private static void writeToFile(List<Patterns> itemWithSupports, BufferedWriter bw) throws IOException {

		for (Patterns pattern : itemWithSupports) {
			String outputLine = pattern.getSupport() + ":" + String.join(";",pattern.getCategories());
			System.out.println(outputLine);
			bw.write(outputLine);
			bw.newLine();

		}

	}

	private static List<List<String>> fetchRecords() {
		List<List<String>> records = new ArrayList<>();
		try (FileReader fr = new FileReader(new File("categories2.txt")); BufferedReader br = new BufferedReader(fr);) {
			List<String> record = new ArrayList<>();
			String line = "";
			while ((line = br.readLine()) != null) {
				record = Arrays.asList(line.split(";"));
				records.add(record);
			}
			System.out.println(records.size());
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return records;
	}

}
