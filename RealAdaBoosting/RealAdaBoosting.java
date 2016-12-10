package com.machineLearning.adaboosting;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RealAdaBoosting {

	public static void main(String[] args) {
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
		
		double epsilon = Double.parseDouble(firstLine[2]);
		
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
		//List<Double> bound = new ArrayList<>();
		double bound=1;
		Map<Integer,Double> ft = new HashMap<>();
		int counter=0;
		
		for(int i=0;i<no_of_iterations;i++){
			counter++;
			int total_hypothesis = no_of_examples+1;
			double hypothesisValue = 0;		// hypothesis Value
			List<Double> correctClassifier = null;
			
			List<Double> afterCheck = new ArrayList<>();
			String sign = "";
			//double errors = Double.MAX_VALUE;
			double qualifer = 0;
			
			// Choose best hypothesis
			
			double minG=Double.MAX_VALUE;
			double pRightPositive=0,pRightNegative=0,pWrongPositive=0,pWrongNegative=0;
			
			for(int j=0;j<total_hypothesis;j++){									// Check if values are less than hypothesis
				
				hypothesisValue = getHypothesisVal(examples, total_hypothesis, j);				
				double pRightPositive1=0,pRightNegative1=0,pWrongPositive1=0,pWrongNegative1=0;
				
				for(int val=0;val<examples.size();val++){
					
					if(examples.get(val)<hypothesisValue){
						afterCheck.add((double)1);
					}else{
						afterCheck.add((double)-1);
					}
					
					if(afterCheck.get(val)==1 &&  yValue.get(val)==1){
						pRightPositive1 = pRightPositive1 + probabilites.get(val);
					}else if(afterCheck.get(val)==1 && yValue.get(val)== -1){
						pWrongNegative1 = pWrongNegative1 + probabilites.get(val);;
					}else if(afterCheck.get(val) == -1 && yValue.get(val)==1){
						pWrongPositive1 = pWrongPositive1 + probabilites.get(val);
					}else{
						pRightNegative1 = pRightNegative1 + probabilites.get(val);
					}
					
				}
				
				double G =  Math.sqrt(pRightPositive1*pWrongNegative1) + Math.sqrt(pWrongPositive1*pRightNegative1);
				
				if(G<minG){
					qualifer = hypothesisValue;
					minG = G;
					sign="<";
					correctClassifier = new ArrayList<>(afterCheck);
					pRightNegative = pRightNegative1;
					pRightPositive = pRightPositive1;
					pWrongNegative = pWrongNegative1;
					pWrongPositive = pWrongPositive1;
				}			
				afterCheck.clear();
			}
						
			
			for(int j=0;j<total_hypothesis;j++){									// Check if values are greater than hypothesis
				
				hypothesisValue = getHypothesisVal(examples, total_hypothesis, j);				
				
				double pRightPositive1=0,pRightNegative1=0,pWrongPositive1=0,pWrongNegative1=0;
				for(int val=0;val<examples.size();val++){
					
					if(examples.get(val)>hypothesisValue){
						afterCheck.add((double)1);
					}else{
						afterCheck.add((double)-1);
					}
					
					if(afterCheck.get(val)==1 && yValue.get(val)==1){
						pRightPositive1 = pRightPositive1 + probabilites.get(val);
					}else if(afterCheck.get(val)==1 && yValue.get(val)== -1){
						pWrongNegative1 = pWrongNegative1 + probabilites.get(val);
					}else if(afterCheck.get(val)==-1 && yValue.get(val)==1){
						pWrongPositive1 = pWrongPositive1 + probabilites.get(val);
					}else{
						pRightNegative1 = pRightNegative1 + probabilites.get(val);
					}
				}
				
				double G =  Math.sqrt(pRightPositive1*pWrongNegative1) + Math.sqrt(pWrongPositive1*pRightNegative1);
				
				if(G<minG){
					qualifer = hypothesisValue;
					minG = G;
					sign=">";
					correctClassifier = new ArrayList<>(afterCheck);
					pRightNegative = pRightNegative1;
					pRightPositive = pRightPositive1;
					pWrongNegative = pWrongNegative1;
					pWrongPositive = pWrongPositive1;
				}	
				afterCheck.clear();
				
			}
			
			// Calculate p+(right) , p-(wrong), p+(wrong), p-(wrong)
			
			boostedClassifier.add(qualifer);
			boostedClassifierSign.add(sign);
			
			System.out.println("Selected Qualifer: h" + counter + " : " + sign +" " + qualifer);
			
			
			double ctPositive = 0.5*Math.log((pRightPositive+epsilon)/(pWrongNegative+epsilon));
			double ctNegative = 0.5*Math.log((pWrongPositive+epsilon)/(pRightNegative+epsilon));
			
			
			
			boostedAlpha.add(minG);
			System.out.println("G error of h"  + counter + " : " + minG);
			System.out.println("weights , c+ :" + ctPositive + " c- :" + ctNegative);
			List<Double> preNormalisedProb = new ArrayList<>();
			double totalProb=0;
			for(int val=0;val<no_of_examples;val++){
				
				if(correctClassifier.get(val).equals((double)1)){
					if(ft.get(val)==null){
						ft.put(val, ctPositive);
					}else{
						ft.put(val, ft.get(val) + ctPositive);
					}
				}else{
					if(ft.get(val)==null){
						ft.put(val, ctNegative);
					}else{
						ft.put(val, ft.get(val) + ctNegative);
					}
				}
				
				if(correctClassifier.get(val).equals((double)1)){
					double val1 = probabilites.get(val)*Math.exp(-1*yValue.get(val)*ctPositive);
					totalProb+=val1;
					preNormalisedProb.add(val1);
					
				}else{
					double val1 = probabilites.get(val)*Math.exp(-1*yValue.get(val)*ctNegative);
					totalProb+=val1;
					preNormalisedProb.add(val1);
					
				}
			}
			
			
			
			List<Double> probAfterNormalization = new ArrayList<>();
			double newError=0;
			for(int val=0;val<no_of_examples;val++){
				probAfterNormalization.add(preNormalisedProb.get(val)/totalProb);
				if((yValue.get(val)==1 && ft.get(val) <=0) || (yValue.get(val)==-1 && ft.get(val)>=0)){
					newError++;
				}
			}
			bound = bound * totalProb;
			probabilites = new ArrayList<>(probAfterNormalization);
			System.out.println("Normalizatoin factor Z : " + totalProb);
			
			System.out.println("prob after normalization "+ probAfterNormalization);
			System.out.print("FT x values :  [");
			for(Map.Entry<Integer, Double> map:ft.entrySet()){
				System.out.print(map.getValue()+ ",");
			}
			
			System.out.println("]");
			System.out.println("Error of boosted classifier " + newError/no_of_examples);
			System.out.println("Bound on Et :" + bound);
			System.out.println("--------------");
		}
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
