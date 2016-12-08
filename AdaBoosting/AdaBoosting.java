package com.machineLearning.adaboosting;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class AdaBoosting {

	public static void main(String[] args) {
		
		//Scanner in = new Scanner(System.in);
		
		String file1 = "input1.txt";//in.nextLine();
		
		//in.close();
		
		List<String> inputData = new ArrayList<>();
		try {
			BufferedReader bufferedReader = new BufferedReader(new FileReader(AdaBoosting.class.getResource(file1).getPath()));
			
			String input;
			
			while((input = bufferedReader.readLine())!=null){
				
				inputData.add(input);
			}
			
			bufferedReader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		String[] firstLine = inputData.get(0).split(" ");
		
		int no_of_iterations = Integer.parseInt(firstLine[0]);
		
		int no_of_examples = Integer.parseInt(firstLine[1]);
		
		//double epsilon = Double.parseDouble(firstLine[2]);
		
		
		List<Double> examples = new ArrayList<>();
		List<Double> yValue = new ArrayList<>();
		List<Double> probabilites = new ArrayList<>();
		
		String[] yVal = inputData.get(2).split(" ");
		String[] prob = inputData.get(3).split(" ");
		int count=0;
		for(String s:inputData.get(1).split(" ")){
			
			examples.add(Double.parseDouble(s));
			yValue.add(Double.parseDouble(yVal[count]));
			probabilites.add(Double.parseDouble(prob[count]));
			count++;
		}
		
	
		List<Double> boostedClassifier = new ArrayList<>();
		List<String> boostedClassifierSign = new ArrayList<>();
		List<Double> boostedAlpha = new ArrayList<>();
		List<Double> NormalizationFactor = new ArrayList<>();
		int count1=1;
		
		for(int i=0;i<no_of_iterations;i++){
			
			
			List<Double> preNormalizedProb = new ArrayList<>();
			
			List<Double> afterNormalizedProb = new ArrayList<>();
			
			int total_hypothesis = examples.size()+1;
			double hypothesisValue = 0;		// hypothesis Value
			List<Double> correctClassifier = null;
			
			List<Double> afterCheck = new ArrayList<>();
			String sign = "";
			double errors = Double.MAX_VALUE;
			double qualifer = 0;
			
			// Choose best hypothesis
			
			
			for(int j=0;j<total_hypothesis;j++){									// Check if values are less than hypothesis
				
				hypothesisValue = getHypothesisVal(examples, total_hypothesis, j);				
				
				for(double exampleVal:examples){
					
					if(exampleVal<hypothesisValue){
						afterCheck.add((double)1);
					}else{
						afterCheck.add((double)-1);
					}
				}
				
				double no_of_errors = getTotalErrors(yValue, afterCheck,probabilites);
				
				if(no_of_errors< errors){
					errors = no_of_errors;
					qualifer = hypothesisValue;
					sign = "<";
					correctClassifier = new ArrayList<>(afterCheck);
				}
				

				afterCheck.clear();
				
			}
						
			
			for(int j=0;j<total_hypothesis;j++){									// Check if values are greater than hypothesis
				
				hypothesisValue = getHypothesisVal(examples, total_hypothesis, j);				
				
				
				for(double d:examples){
					
					if(d>hypothesisValue){
						afterCheck.add((double)1);
					}else{
						afterCheck.add((double)-1);
					}
				}
				
				double no_of_errors = getTotalErrors(yValue, afterCheck,probabilites);
				
				if(no_of_errors< errors){
					errors = no_of_errors;
					qualifer = hypothesisValue;
					sign = ">";
					correctClassifier = new ArrayList<>(afterCheck);
				}
				afterCheck.clear();
			}
			
					
			double alpha = 0.5*Math.log((1-errors)/errors);								// calculate alpha
			
			
			boostedClassifier.add(qualifer);
			boostedClassifierSign.add(sign);
			boostedAlpha.add(alpha);
			System.out.println("Selected weak classifier h " + count1 +"is: v"+ sign +"" + qualifer );
			System.out.println("Error of h" + count1++ + " "+ errors);
			System.out.println("Weight of A : " + alpha);
			
			double qWrong = Math.exp(alpha);
			double qCorrect = Math.exp(-alpha);
			//System.out.println(qWrong + " right "  + qCorrect);
			
			double z=0;
			for(int val=0;val<examples.size();val++){
				
				if(correctClassifier.get(val).equals(yValue.get(val))){
					double value = qCorrect*probabilites.get(val);
					preNormalizedProb.add(value);
					z = z + value;
				}else{
					double value = qWrong*probabilites.get(val);
					preNormalizedProb.add(value);
					z = z + value;
				}
			}
			
			System.out.println("Normalization factor " + z);
			
			NormalizationFactor.add(z);
			
			probabilites.clear();
			for(int val=0;val<examples.size();val++){
				double value  = preNormalizedProb.get(val)/z;
				afterNormalizedProb.add(value);
				probabilites.add(value);
			}
			
			List<Double> newCheck = new ArrayList<>();
			for(int val=0;val<examples.size();val++){
				
				
				double sum=0;
				for(int j=0;j<boostedClassifier.size();j++){
					double hypoth = boostedClassifier.get(j);
					sign = boostedClassifierSign.get(j);
					
					double alp = boostedAlpha.get(j);
					if(sign.equals("<")){
						
						if(examples.get(val) < hypoth){
							sum = sum + alp*1;
						}else{
							sum = sum - alp*1;
						}
					}else{
						if(examples.get(val) > hypoth){
							sum = sum + alp*1;
						}else{
							sum = sum - alp*1;
						}
					}
				}
				
				if(sum>0){
					newCheck.add((double)1);
				}else{
					newCheck.add((double)-1);
				}
				
				
			}
			
			double newError = getTotalErrors(yValue, newCheck);
			
			
			
			newCheck.clear();
			
			System.out.println("Prob after normalization" + afterNormalizedProb);
			
			System.out.print("Boosted Qualifier : " );
			for(int val=0;val<boostedClassifier.size();val++){
				System.out.print(" " +boostedAlpha.get(val)+"*h(x"+boostedClassifierSign.get(val)+""+ boostedClassifier.get(val) +") +");
			}
			System.out.println();
			
			System.out.println("Error after hypothesis :" + newError/examples.size() );
			
			
			double boundOnEt=1;
			for(double d:NormalizationFactor){
				boundOnEt*=d;
			}
			System.out.println("Bound of et :" + boundOnEt);
			System.out.println("--------------------------------");
		}
		
		
		
	}

	private static double getTotalErrors(List<Double> yValue, List<Double> afterCheck) {
		double no_of_errors = 0;
		for(int val=0;val<afterCheck.size();val++){
			
			if(!(afterCheck.get(val).equals(yValue.get(val)))){
				no_of_errors++;// + probabilites.get(val);
			}
		}
		return no_of_errors;
	}
	
	
	private static double getTotalErrors(List<Double> yValue, List<Double> afterCheck, List<Double> probabilites) {
		double no_of_errors = 0;
		for(int val=0;val<afterCheck.size();val++){
			
			if(!(afterCheck.get(val).equals(yValue.get(val)))){
				no_of_errors = no_of_errors + probabilites.get(val);
			}
		}
		return no_of_errors;
	}

	private static double getHypothesisVal(List<Double> examples, int total_hypothesis, int j) {
		double checkVal;
		if(j-1<0){
			checkVal = examples.get(j)-0.5;
		}else if(j==total_hypothesis-1){
			checkVal = examples.get(j-1) + 0.5; 
		}else{
			checkVal = (examples.get(j) + examples.get(j-1))/2;
		}
		return checkVal;
	}
	
}
