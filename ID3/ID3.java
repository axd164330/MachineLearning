import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class ID3 {

	public static void main(String[] args) {
		
		Scanner in = new Scanner(System.in);
		
		//String input = in.nextLine();
	/*	
		if(!input.equalsIgnoreCase("MyProgram")){
			System.out.println("Input is wrong");
			in.close();
			System.exit(0);
		}*/
		
		System.out.println("Enter names of the files dataset input-partition output-partition");
		
		String fileName = in.nextLine();
		
		String files[] = fileName.split(" ");
		in.close();
		
		Map<Integer,String> inputDataSet = new LinkedHashMap<Integer,String>();
		Map<Integer, String> partitionDataSet = new LinkedHashMap<Integer,String>();
		int i=0;
		try {
			if(ID3.class.getResource(files[0]) == null){
				System.out.println("File not found " + files[0]);
				System.out.println("exiting the program");
				System.exit(0);
			}
			String dataSet = ID3.class.getResource(files[0]).getPath();
			BufferedReader bufferedReader = new BufferedReader(new FileReader(dataSet));
			
			String inputData;
			while((inputData=bufferedReader.readLine())!=null){
				inputDataSet.put(i, inputData);
				i++;
			}
			// "C:/Users/asdha/workspace/MailCar/ID3Project/src/com/id3/partition.txt"
			i=0;
			String partitionData;
			if(ID3.class.getResource(files[1]) == null){
				System.out.println("File not found " + files[0]);
				System.out.println("exiting the program");
				System.exit(0);
			}
			dataSet = ID3.class.getResource(files[1]).getPath();
			BufferedReader bufferedReader1 = new BufferedReader(new FileReader(dataSet));
			while((partitionData = bufferedReader1.readLine())!=null){
				partitionDataSet.put(i, partitionData);
				i++;
			}
			bufferedReader1.close();
			bufferedReader.close();
		} catch (FileNotFoundException e) {
			System.out.println("No such file found " + e);
		} catch (IOException e) {
			System.out.println("IO excepetion " + e);
		}
		
		
		String firstRow = inputDataSet.get(0);
		
		String[] firstSpiltted = firstRow.split(" ");
		
		int no_of_instances = Integer.parseInt(firstSpiltted[0]);
		int no_of_features = Integer.parseInt(firstSpiltted[1]);
		
		//System.out.println(no_of_features);
		//System.out.println(no_of_instances);
		
		
		Map<Integer, List<String>> featureMap = new LinkedHashMap<>();		// feature Map contains the values of all the features and last key in this map contains values of the target 
				
		for(int j=0;j<no_of_features;j++){
			String[] featureSplit;
			List<String> particularFeatureValues = new ArrayList<>();
			for(int k=0;k<no_of_instances;k++){
				
				String featureRowData = inputDataSet.get(k+1);
				featureSplit = featureRowData.split(" ");
				particularFeatureValues.add(featureSplit[j]);				
			}
			featureMap.put(j,particularFeatureValues);			
		}
		
		//System.out.println("Feature Map " + featureMap); 
		
		Map<String,List<String>> partitionData = new LinkedHashMap<>(); // partition data map contains the values of the pre-partitioned data. Key contains the feature name 
		
		for(Map.Entry<Integer, String> entry : partitionDataSet.entrySet()){
			
			String data = entry.getValue();
			
			String[] split = data.split(" ");
			List<String> list = new ArrayList<>();
			for(int k=0;k<split.length-1;k++){				
				list.add(split[k+1]);
			}
			partitionData.put(split[0], list);	
		}
		
		//System.out.println("Partition Data " + partitionData);
		
		List<String> targetValue = featureMap.get(featureMap.size()-1);  // target value List has all the values of the target for each instance
	//	System.out.println("targetValue" + targetValue);
		Map<List<String>, List<Double>> subsetEntropy = new LinkedHashMap<>();	// contains List of entropies for each subset for each feature
		for(Map.Entry<String, List<String>> entry: partitionData.entrySet()){
			
			Map<String, String> serialNumberToTarget = new LinkedHashMap<>(); 	// contains the values of target for each subset value
			Map<String, String> serialNumberToFeature = new LinkedHashMap<>(); // contains the values of single feature for each subset value mapping to that instance
			List<String> subset = entry.getValue();
			List<Double> entropyListForSubset = new ArrayList<>();
			
			for(int k = 0;k<no_of_features-1;k++){
				
				for(String s:subset){
					
					serialNumberToTarget.put(s, targetValue.get(Integer.parseInt(s)-1));
					serialNumberToFeature.put(s, ((List<String>) featureMap.get(k)).get(Integer.parseInt(s)-1));
					
					
				}
				entropyListForSubset.add(calculateEntropyGain(serialNumberToTarget, serialNumberToFeature));
				
			}
			subsetEntropy.put(subset, entropyListForSubset);
			
		}
		Map<List<String>, Map<Double,Integer>> maxF = new LinkedHashMap<>();
		
		for(Map.Entry<List<String>, List<Double>> entry : subsetEntropy.entrySet()){
			double prob = 0.0;
			List<Double> value = entry.getValue();
			Map<Double, Integer> maxAndIndex = new LinkedHashMap<>();
			double greatestValue = 0.0;
			int index = 1,count=1;
			for(Double double1 : value){				
				if(double1  > greatestValue){
					greatestValue = double1;
					index = count;
				}
				count++;
			}
			prob = ((double) value.size())/no_of_instances;
			greatestValue = prob*greatestValue;
			maxAndIndex.put(greatestValue, index);
			maxF.put(entry.getKey(), maxAndIndex);
		}
				
		//System.out.println("subsetEntropy" + subsetEntropy);
		
	//	System.out.println("MaxF" + maxF);
		
		int feature = 0;
		boolean partitionPossible = false;
		double max =0.0;
		List<String> setTobePatitioned = new ArrayList<>();
		for(Map.Entry<List<String>, Map<Double,Integer>> entry : maxF.entrySet()){	
			
			Map<Double,Integer> maxMap = entry.getValue();
			for(Map.Entry<Double, Integer> entry2 : maxMap.entrySet()){				
				if(entry2.getKey() > max){
					setTobePatitioned = entry.getKey();	
					feature = entry2.getValue();
					partitionPossible = true;
				}
			}
		}
		
		if(!partitionPossible){
			System.out.println("No partition possible");
			System.exit(0);
		}
		
		//System.out.println("set to be partitioned" + setTobePatitioned);
		//System.out.println("feature to be used " + feature);			
		
		String setNameTobePartitioned = "";
		
		for(Map.Entry<String, List<String>> entry : partitionData.entrySet()){
			
			if((entry.getValue().equals(setTobePatitioned))){
				setNameTobePartitioned = entry.getKey();				
			}
		}
		
		Map<String,List<String>> featureValueToTarget = new LinkedHashMap<>();
		for(String s:setTobePatitioned){
			
			if(featureValueToTarget.containsKey((featureMap.get(feature-1)).get(Integer.parseInt(s)-1))){
				List<String> value = featureValueToTarget.get((featureMap.get(feature-1)).get(Integer.parseInt(s)-1));
				value.add(s);
			}else{
				List<String> value = new ArrayList<>();
				value.add(s);				
				featureValueToTarget.put((featureMap.get(feature-1)).get(Integer.parseInt(s)-1), value);
			}
		}
		
	//	System.out.println("setName " + setNameTobePartitioned);
	//	System.out.println("featureValuetoTarget " + featureValueToTarget);
		
		StringBuffer newSetNames= new StringBuffer();
		int setValue = 0;
		for(Map.Entry<String, List<String>> entry:featureValueToTarget.entrySet()){
			newSetNames.append(setNameTobePartitioned + (++setValue) + ",");
		}
		
		System.out.println("Partition " + setNameTobePartitioned + " was replaced with partition " + newSetNames.toString() + " using feature " + feature) ;
		
		try {
			PrintWriter printWriter = new PrintWriter(files[2] , "UTF-8");
			setValue = 0;
			for(Map.Entry<String, List<String>> entry : partitionData.entrySet()){
				List<String> value = entry.getValue();
				if(!entry.getKey().equals(setNameTobePartitioned)){
					StringBuffer parValue = new StringBuffer();
					for(String s:value){
						parValue.append(s + " ");
					}

					printWriter.println(entry.getKey() + " " + parValue);
				}
			}
			
			for(Map.Entry<String, List<String>> entry:featureValueToTarget.entrySet()){
				List<String> value = entry.getValue();
				StringBuffer parValue = new StringBuffer();
				for(String s:value){
					parValue.append(s + " ");
				}
				printWriter.println(setNameTobePartitioned + (++setValue) + " " + parValue);
			}
			printWriter.close();
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		 
	}
	
	/**
	 * 
	 * @param serialNumberToTarget
	 * @param serialNumberToFeature
	 * @return
	 */
	private static double calculateEntropyGain(Map<String, String> serialNumberToTarget,
			Map<String, String> serialNumberToFeature) {
		
		int sizeOfSubset = serialNumberToFeature.size();		
		
		Map<String, Integer> targetCount = new LinkedHashMap<>();		// targetCount contains the count of target variables in particular subset
																		// key is target attribute, value is count of that attribute
		Map<String, List<String>> featureEntropy = new LinkedHashMap<>();
		
		Map<String, Integer> featureCount = new LinkedHashMap<>();		// featureCount contains the count of features (ie. how many times particular feature is repeated in particular subset)
																		// key is feature, value is count of that feature		
		for(Map.Entry<String, String> entry:serialNumberToFeature.entrySet()){			
			if(featureCount.containsKey(entry.getValue())){
				featureCount.put(entry.getValue(), featureCount.get(entry.getValue()) + 1);
			}else{
				featureCount.put(entry.getValue(), 1);
			}
		}
		
		for(Map.Entry<String, String> entry:serialNumberToTarget.entrySet()){			
			if(targetCount.containsKey(entry.getValue())){
				targetCount.put(entry.getValue(), targetCount.get(entry.getValue()) + 1);
			}else{
				targetCount.put(entry.getValue(), 1);
			}
		}
		double subSetEntropy = entropy(targetCount,sizeOfSubset);			// entropy of the subset
				
		if(subSetEntropy == 0.0){
			return subSetEntropy;
		}		
		
		for(Map.Entry<String, String> entry:serialNumberToFeature.entrySet()){	// This loop connects every value of feature subset to it's target value
			
			if(featureEntropy.containsKey(entry.getValue())){
				List<String> value = featureEntropy.get(entry.getValue());
				value.add(serialNumberToTarget.get(entry.getKey()));
			}else{
				List<String> targetValues = new ArrayList<>();
				targetValues.add(serialNumberToTarget.get(entry.getKey()));
				featureEntropy.put(serialNumberToFeature.get(entry.getKey()), targetValues);
			}
			
		}
		
		
		double entropy = 0.0, prob = 0.0;
		
		for(Map.Entry<String, List<String>> entry: featureEntropy.entrySet()){
			Map<String,Integer> subsetFeatureToTarget = new LinkedHashMap<>();	
			List<String> value = entry.getValue();
			
			for(String s:value){
				if(subsetFeatureToTarget.containsKey(s)){					
					subsetFeatureToTarget.put(s, subsetFeatureToTarget.get(s) + 1);
				}else{
					subsetFeatureToTarget.put(s, 1);
				}
			}
			prob = ((double) value.size())/sizeOfSubset;
			entropy = entropy + (prob*entropy(subsetFeatureToTarget, value.size()));		// Finds the entropy of each feature in one subset and then  add's up ,i.e P(T/feature) for each set
			 
		}
		
		subSetEntropy = subSetEntropy - entropy;						// finds the gain of the feature
		
		return subSetEntropy;
	}

	/**
	 * 
	 * @param targetCount
	 * @param sizeOfSubset
	 * @return
	 */
	private static double entropy(Map<String, Integer> targetCount, int sizeOfSubset) {
		
		double value = 0.0;
		double probability = 0.0;
		int totalCount = 0;
		for(Map.Entry<String, Integer> entry:targetCount.entrySet()){
			
			totalCount = totalCount + entry.getValue();
		}
		for(Map.Entry<String, Integer> entry:targetCount.entrySet()){
			
			probability = ((double) entry.getValue())/totalCount;
			value = value + (probability*Math.log(1/probability)/Math.log(2));
			
		}
		if(Double.compare(value, Double.NaN) == 0){
			value = 0.0;
		}
		
		return value;
	}
}
