import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;

public class NearestNeighbour {

	public static void main(String[] args) {
		
		Scanner in = new Scanner(System.in);
		
		System.out.println("enter name of first input file");
		
		String file1 = in.nextLine();//"input1.txt";
		String file2 = in.nextLine();//"input2.txt";
		String outputFile = in.nextLine();
		Map<Integer, String> firstInput = new HashMap<>();
		Map<Integer, String> secondInput = new HashMap<>();
		
		getInputDataFromFIle(in, file1, file2, firstInput, secondInput);		// Setting input data from File
		
		in.close();
		
		String[] firstInputFirstRowValues = firstInput.get(0).split(" ");
		int kFold = Integer.parseInt(firstInputFirstRowValues[0]);		
		int numberOfExamples = Integer.parseInt(firstInputFirstRowValues[1]);
		int permutations = Integer.parseInt(firstInputFirstRowValues[2]);
		
		Map<Integer, List<Integer>> exampleValues = new HashMap<>();		// Map of no of examples to their respective values 
		
		/**
		 * creating  map of all the examples and their indexes
		 */
		
		for(int j=1;j<=permutations;j++){
			
			String[] egValues = firstInput.get(j).split(" ");
			
			List<Integer> listOfExample = new ArrayList<>();
			for(String s:egValues){
				
				listOfExample.add(Integer.parseInt(s));
				
			}
			
			exampleValues.put(j, listOfExample);
		}
		
		String[] rowAndColumns = secondInput.get(0).split(" ");
		
		int rows = Integer.parseInt(rowAndColumns[0]);
		int columns = Integer.parseInt(rowAndColumns[1]);
				
		Map<Integer,Map<String,String>> permutationToCoordinates = new HashMap<>();			// map of the x1,x2 coordinates and their corresponding values i.e, + or -
		
		Map<String,String> coordinatesVal = new HashMap<>();
		
		setIndexToCoordinates(secondInput, rows, columns, permutationToCoordinates,coordinatesVal);		
				
		int[] folds = getFoldIndexes(kFold, numberOfExamples);				 // Folds array contains the division of examples based on the number of folds e.g: 
																		 // 9 examples and fold is 2. Array will have values 5,4.
		
		Map<Integer,Double> classifierErrorForEachNN = new HashMap<>();
		Map<Integer,Double> varianceForEachNN = new HashMap<>();
		
		Map<Integer,List<Integer>> testMap = new HashMap<>();
		
		PrintWriter printWriter = null;
		try {
			printWriter = new PrintWriter(outputFile,"UTF-8");
		} catch (FileNotFoundException e) {
			
			System.out.println("File not found" + outputFile);
		} catch (UnsupportedEncodingException e) {
			System.out.println("Encoding Exception" + e.toString());
		}
		
		
		for(int k=1;k<=5;k++){
			
			List<Double> errorList = new ArrayList<>();
			List<Integer> countErrorList = new ArrayList<>();
			for(int q=0;q<permutations;q++){
				
				Map<Integer,Map<String,String>> indexesAfterPermutation = new HashMap<>();
				List<Integer> indexValues = exampleValues.get(q+1);
				
				for(int j=0;j<indexValues.size();j++){				
					indexesAfterPermutation.put(j, permutationToCoordinates.get(indexValues.get(j)));				
				}
				
				int errorCount = 0;
				for(int j=0;j<folds.length;j++){
					
					Map<Integer,Map<String,String>> trainingData = new HashMap<>();
					Map<Integer,Map<String,String>> testingData = new HashMap<>();
					
	 				setTestingAndTrainingData(numberOfExamples, folds, indexesAfterPermutation, j, trainingData, testingData);
					
					for(Map.Entry<Integer,Map<String,String>> testData : trainingData.entrySet()){
						int x = 0,y = 0;
						String signOfTestData = "";
						Map<String,String> testD = testData.getValue();						
						
						for(Map.Entry<String,String> iter : testD.entrySet()){
							
							String key = iter.getKey();
							signOfTestData = iter.getValue();
							x = Character.getNumericValue(key.charAt(0));			// test data x coordinates
							y = Character.getNumericValue(key.charAt(1));			// test data y coordinates
							
						}
						
						Map<String,Map<Double,String>> distance = new LinkedHashMap<>();
						for(Map.Entry<Integer, Map<String,String>> trainData:testingData.entrySet()){
							
							distanceMapForEachTrainingData(x, y, distance, trainData);
							
						}
						
						sortMapBasedOnDistance(distance);
						
						// fetch the k values from distance map and check if their is error or not
						
						errorCount = getErrorCount(k, errorCount, signOfTestData, distance);		
						
					}					
				}
				
				
				countErrorList.add(errorCount);
				errorList.add((double)errorCount/numberOfExamples);
			}
			
			testMap.put(k, countErrorList);
			double accurateClassifier = getAccurateClassifier(errorList);			

			classifierErrorForEachNN.put(k, accurateClassifier);
			
			double variance = getVariance(errorList, accurateClassifier);
			
			varianceForEachNN.put(k, variance);
			
			
				
			printWriter.println("K = " + k + " e = " + classifierErrorForEachNN.get(k) + " sigma = " + varianceForEachNN.get(k));
			
			printCoordinatesOnGrid(rows, columns, permutationToCoordinates, coordinatesVal, k,printWriter);
			
		}		
		
		printWriter.close();
		
		System.out.println("Values has been printed to the output file with name : " + outputFile);
	}

	/**
	 * 
	 * @param rows
	 * @param columns
	 * @param permutationToCoordinates
	 * @param coordinatesVal
	 * @param k
	 * @param printWriter 
	 */
	private static void printCoordinatesOnGrid(int rows, int columns,
			Map<Integer, Map<String, String>> permutationToCoordinates, Map<String, String> coordinatesVal, int k, PrintWriter printWriter) {
		for(int i =0;i<rows;i++){
			
			for(int j=0;j<columns;j++){
				
				int x = j;
				int y=i;
				
				String s = x + "" + y;
				
				if(coordinatesVal.containsKey(s)){
					//System.out.print(coordinatesVal.get(s) + " ");
					
					printWriter.print(coordinatesVal.get(s) + " ");
					
				}else{

					Map<String,Map<Double,String>> distance = new LinkedHashMap<>();
					
					for(Map.Entry<Integer, Map<String, String>> entry : permutationToCoordinates.entrySet()){				
						distanceMapForEachTrainingData(x, y, distance, entry);
					}
					
					sortMapBasedOnDistance(distance);
					
					printWriter.print(calculateSignOfPoint(k, distance) + " ");
					//System.out.print(calculateSignOfPoint(k, distance) + " ");
					
				}
				
			}
			
			printWriter.println();
			//System.out.println();
		}
	}

	/**
	 * Sort Map based on distance
	 * @param distance
	 */
	private static void sortMapBasedOnDistance(Map<String, Map<Double, String>> distance) {
		List<Map.Entry<String, Map<Double,String>>> list = new LinkedList<>(distance.entrySet());
		
		Collections.sort(list, new Comparator<Map.Entry<String, Map<Double,String>>>() {

			@Override
			public int compare(Entry<String, Map<Double, String>> arg0,
					Entry<String, Map<Double, String>> arg1) {
				
				
				Double val1 = (double) arg0.getValue().keySet().toArray()[0];
				
				Double val2 = (double) arg1.getValue().keySet().toArray()[0];
				
				return val1.compareTo(val2);
				
			}
			
		});
		
		distance.clear();
		for(Map.Entry<String, Map<Double,String>> entry :list){
			
			distance.put(entry.getKey(), entry.getValue());
			
		}
	}

	/**
	 * This method gives the variance for the list of errors
	 * 
	 * @param errorList
	 * @param accurateClassifier
	 * @return
	 */
	private static double getVariance(List<Double> errorList, double accurateClassifier) {
		double variance=0;
		double V = 0;
		
		for(double a:errorList){
			V = V + Math.pow(a-accurateClassifier, 2);
		}
		
		V = V/(errorList.size()-1);
		
		variance = Math.sqrt(V);
		return variance;
	}

	/**
	 * This method gives the accurate classifier
	 * @param errorList
	 * @return
	 */
	private static double getAccurateClassifier(List<Double> errorList) {
		double classifier = 0;
		double accurateClassifier =0;
		for(double a:errorList){
			
			classifier = classifier + a;
			
		}
		
		accurateClassifier = classifier/errorList.size();
		return accurateClassifier;
	}

	/**
	 * Set Input data from files
	 * 
	 * @param in
	 * @param file1
	 * @param file2
	 * @param firstInput
	 * @param secondInput
	 */
	private static void getInputDataFromFIle(Scanner in, String file1, String file2, Map<Integer, String> firstInput,
			Map<Integer, String> secondInput) {
		int i=0;
		try {
			BufferedReader bufferedReader = new BufferedReader(new FileReader(NearestNeighbour.class.getResource(file1).getPath()));
			
			String inputData;
			
			while((inputData = bufferedReader.readLine())!=null){
				
				firstInput.put(i++, inputData);
				
			}
			bufferedReader.close();
			
			
			BufferedReader bufferedReader2 = new BufferedReader(new FileReader(NearestNeighbour.class.getResource(file2).getPath()));
			
			i=0;
			String inputData1;
			
			while((inputData1 = bufferedReader2.readLine())!=null){
				
				secondInput.put(i++, inputData1);
			}
			
			bufferedReader2.close();
		} catch (FileNotFoundException e) {
			in.close();
			System.out.println("file not found");
			System.exit(0);
			
		} catch (IOException e) {
			in.close();
			System.out.println("IO exception");
			System.exit(0);
		}
	}

	/**
	 * 
	 * @param secondInput
	 * @param rows
	 * @param columns
	 * @param permutationToCoordinates
	 * @param cordVal
	 */
	private static void setIndexToCoordinates(Map<Integer, String> secondInput, int rows, int columns,
			Map<Integer, Map<String, String>> permutationToCoordinates,Map<String,String> cordVal) {
		int exampleNumber = 0;
		for(int q=0;q<rows;q++){			
			String[] val = secondInput.get(q+1).split(" ");
			
			
			for(int j=0;j<columns;j++){				
				if(val[j].equalsIgnoreCase("+") || val[j].equalsIgnoreCase("-")){
					Map<String,String> coordinateValues = new HashMap<>();
					String cordinates = j +"" + q;
					coordinateValues.put(cordinates, val[j]);
					cordVal.put(cordinates, val[j]);
					permutationToCoordinates.put(exampleNumber++, coordinateValues);
				}	
			}
		}
	}

	/**
	 * This method returns the error count for each permutation and total number of folds
	 * 
	 * @param k
	 * @param errorCount
	 * @param signOfTestData
	 * @param distance
	 * @return
	 */
	private static int getErrorCount(int k, int errorCount, String signOfTestData, Map<String,Map<Double, String>> distance) {
		
		String signOfTrainingData = calculateSignOfPoint(k, distance);
		if(!signOfTrainingData.equalsIgnoreCase(signOfTestData)){
			errorCount++;
		}
		return errorCount;
	}

	/**
	 * This method returns the count of the point which is calculated based on the nearest neighbour
	 * 
	 * @param k
	 * @param distance
	 * @return
	 */
	private static String calculateSignOfPoint(int k, Map<String, Map<Double, String>> distance) {
		int positiveSignCount = 0,negativeSignCount=0;
		String signOfTrainingData = "";
		int count = 0;
		boolean flag = false;
		Set<Double> prevValues = new HashSet<>();
		for(Map.Entry<String, Map<Double,String>> en : distance.entrySet()){
			for(Map.Entry<Double, String> entry: en.getValue().entrySet()){
				
				if(prevValues.contains(entry.getKey())){
					
				}else if(count>=k){
					flag = true;
					break;
				}
				
				if(entry.getValue().equalsIgnoreCase("+")){
					positiveSignCount++;
				}else{
					negativeSignCount++;
				}	
				prevValues.add(entry.getKey());
				count++;
			}
			if(flag){
				break;
			}
		}
		
		
		if(positiveSignCount>negativeSignCount){
			signOfTrainingData = "+";
		}else{
			signOfTrainingData="-";
		}
		return signOfTrainingData;
	}

	/**
	 * This gives distance for x and y provided with all the training Data
	 * 
	 * @param x
	 * @param y
	 * @param distance
	 * @param trainData
	 */
	private static void distanceMapForEachTrainingData(int x, int y, Map<String,Map<Double, String>> distance,
			Map.Entry<Integer, Map<String, String>> trainData) {
		Map<String,String> trainD = trainData.getValue();
		int x1,y1;
		String sign1;
		
		
		for(Map.Entry<String, String> iter:trainD.entrySet()){
			
			Map<Double,String> dis = new HashMap<>();
			String key = iter.getKey();
			sign1 = iter.getValue();
			
			x1 = Character.getNumericValue(key.charAt(0));
			y1 = Character.getNumericValue(key.charAt(1));
			
			double dist = Math.sqrt(Math.pow(x1-x, 2) + Math.pow(y1-y, 2));
			
			dis.put(dist, sign1);			// Map for distance of the test Data with training Data
			distance.put(x1+""+y1, dis);
			
		}
	}

	/**
	 * This methods gives the folds at which the data will be partitioned
	 * 
	 * @param kFold
	 * @param exampleNumber
	 * @return
	 */
	private static int[] getFoldIndexes(int kFold, int exampleNumber) {
		int folds[] = new int[kFold];
		
		int remainder = exampleNumber%kFold;
		
		if(remainder==0){
			for(int j=0;j<kFold;j++){
				folds[j] = exampleNumber/kFold;
			}
		}else{
			for(int j=0;j<kFold;j++){
				if(remainder>0){
					folds[j] = (exampleNumber/kFold)+1;
					remainder--;
				}else{
					folds[j] = exampleNumber/kFold;
				}
			}
		}
		return folds;
	}

	/**
	 * Setting the testing and training Data in maps
	 * 
	 * @param exampleNumber
	 * @param folds
	 * @param indexesAfterPermutation
	 * @param j
	 * @param trainingData
	 * @param testingData
	 */
	private static void setTestingAndTrainingData(int exampleNumber, int[] folds,
			Map<Integer, Map<String, String>> indexesAfterPermutation, int j,
			Map<Integer, Map<String, String>> trainingData, Map<Integer, Map<String, String>> testingData) {
		int foldValue;
		if(j==0){

			foldValue= folds[j];
			
		}else{
			foldValue = folds[j-1];
		}
		
		int start = foldValue*j;
		
		int end = start + folds[j];
		for(int a=start;a<end;a++){
			
			trainingData.put(a, indexesAfterPermutation.get(a));
			
		}
		
		if(j!=0){										
			for(int a=0;a<start;a++ ){
				testingData.put(a, indexesAfterPermutation.get(a));
			}	
		}
		
		
		for(int a = end;a<exampleNumber;a++){
			testingData.put(a, indexesAfterPermutation.get(a));
		}
	}
	
	
}
