package com.ibm.journey.gdpr.scorer.config;

import java.util.HashMap;

/*
 * Class for configuration settings for
 * 1. PII data categories
 * 2. Weightage for each of the categories
 * 3. PII data mapping to individual categories
 * 
 * All the above configuration data are stored as environment variables.. Example of such a configuration is as below
 * 		Categories=Very_High,High,Medium,Low
 * 		Very_High_Weight=50
 * 		High_Weight=40
 * 		Medium_Weight=20
 * 		Low_Weight=10
 * 		Very_High_PIIs=Person,MobileNumber
 * 		High_PIIs=Address
 * 		Medium_PIIs=EmailId
 * 		Low_PIIs=Name,Company
 */
public class GDPRConfig {
	private static String[] categoriesArray = null; // Categories in config
	private static HashMap<String, String> weightageMap = null; // weightages in
																// config
	private static HashMap<String, String[]> categoryPIIMapping = null; // PIIs
																		// in
																		// config

	// Get the catgories configured as a String Array
	public static String[] getCategories() throws Exception {
		try {
			if (categoriesArray == null) {
				setCategories();
			}
			return categoriesArray;
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		}
	}

	// Get the weights for each category as a map of category->weight
	public static HashMap<String, String> getWeightages() throws Exception {
		try {
			if (weightageMap == null) {
				setWeightages();
			}
			return weightageMap;
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		}
	}

	// Mapping of each category with all PII data. represented as map category
	// -> PIIData[]
	// e.g. ""Very_High" -> ["Name", "emailid"]
	public static HashMap<String, String[]> getCategoryPIIMapping() throws Exception {
		try {
			if (categoryPIIMapping == null) {
				setCategoryPIIMapping();
			}
			return categoryPIIMapping;
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		}
	}

	// Set categories in config as an array
	private static void setCategories() throws Exception {
		try {
			String categoriesStr = System.getenv("Categories").trim();
			if (categoriesStr == null || categoriesStr.length() <= 0) {
				throw new Exception("Catgories not found in config");
			}

			categoriesArray = trimArrayValues(categoriesStr.split(","));

			if (categoriesArray.length <= 0) {
				throw new Exception("Categories not found in config");
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		}
	}

	// Set weights for categories in config as a map of category -> weight
	private static void setWeightages() throws Exception {
		try {
			if (categoriesArray == null) {
				setCategories();
			}

			weightageMap = new HashMap<String, String>();

			for (int i = 0; i < categoriesArray.length; i++) {
				String key = categoriesArray[i] + "_Weight";
				String weight = System.getenv(key);
				weightageMap.put(categoriesArray[i], weight);
			}
			if (weightageMap.size() <= 0) {
				throw new Exception("Attributes could not be retrieved");
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		}
	}

	// set category to PII data mapping
	private static void setCategoryPIIMapping() throws Exception {
		try {
			if (categoriesArray == null) {
				setCategories();
			}

			categoryPIIMapping = new HashMap<String, String[]>();

			for (int i = 0; i < categoriesArray.length; i++) {
				String key = categoriesArray[i] + "_PIIs";
				String attributes = System.getenv(key);
				String[] attributesArray = trimArrayValues(attributes.split(","));
				categoryPIIMapping.put(categoriesArray[i], attributesArray);
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		}

	}

	// This method return the appropriate category, given a PII, as specified in
	// config
	public static String getCategoryForPII(String pii) throws Exception {
		try {
			if (categoriesArray == null) {
				setCategories();
			}
			if (weightageMap == null) {
				setWeightages();
			}
			if (categoryPIIMapping == null) {
				setCategoryPIIMapping();
			}

			// for each category check if the PII exists
			for (int i = 0; i < categoriesArray.length; i++) {
				// get pii attributes array
				String category = categoriesArray[i];
				// get categoryPII mapping for the category
				String[] attributes = categoryPIIMapping.get(category);
				for (int j = 0; j < attributes.length; j++) {
					if (pii.equalsIgnoreCase(attributes[j])) {
						return category;
					}
				}
			}
			return null; // category not found
		} catch (Exception e) {
			System.out.println("Error: " + e.getMessage());
			e.printStackTrace();
			throw new Exception(e.getMessage());
		}

	}

	// Given a category, get weight for it as specified in config
	public static float getWeightForCategory(String category) throws Exception {
		try {
			if (weightageMap == null) {
				setWeightages();
			}

			return Float.parseFloat(weightageMap.get(category));
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		}

	}

	private static String[] trimArrayValues(String[] stringArray) {
		for (int i = 0; i < stringArray.length; i++) {
			stringArray[i] = stringArray[i].trim();
		}
		return stringArray;
	}

}
