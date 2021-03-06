package evaluation;

import java.util.*;

//import main.Evaluator;

public class News_lda_sklearn_cleanText
{

	public static double evalPurity(Double[][] confusionMatrix){
		ContingencyTable table = new ContingencyTable(confusionMatrix);
		ClusterEvaluator ev = new ClusterEvaluator();
		ev.setData(table);
		//Evaluator ev = new Evaluator(confusionMatrix);
		/*ev.initEvaluator();
		ev.computePurity();*/
		return ev.getPurity();
	}

	public static double evalARI(Double[][] confusionMatrix){
		ContingencyTable table = new ContingencyTable(confusionMatrix);
		ClusterEvaluator ev = new ClusterEvaluator();
		ev.setData(table);		
		/*Evaluator ev = new Evaluator(confusionMatrix);
		ev.initEvaluator();
		ev.computeARI();*/	
		//return ev.getARI();
		return ev.getAdjustedRandIndex();
	}
	
	public static void stats(ArrayList<Double> myList){
		double average = 0.0;
		double std = 0.0;

		for (double elem : myList){
			average += elem;
		}
		average /= myList.size();
		
		for (double elem : myList){
			std += Math.pow(elem - average, 2);
		}
		std /= myList.size();
		std = Math.sqrt(std);
		
		System.out.printf("%f\t%f\n", average, std);
	}
		
	
	public static void main(String[] args){
		
		/*ArrayList<Double> ARI_sum_news_cleanText_tlda_tfidf_sklearn = new ArrayList<Double>();
		ArrayList<Double> PUR_sum_news_cleanText_tlda_tfidf_sklearn = new ArrayList<Double>();
		ArrayList<Double> time_sum_news_cleanText_tlda_tfidf_sklearn = new ArrayList<Double>();
		Double[][] news_cleanText_tlda_tfidf_sklearn_1 = {{0.0,0.0,1.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,993.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0},{0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,1.0,0.0,0.0,957.0,0.0,0.0,0.0,0.0,1.0,1.0,0.0,0.0},{0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,772.0,0.0,0.0,0.0,1.0,0.0,0.0,0.0,0.0},{0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,987.0,0.0,0.0,0.0,0.0,0.0,0.0,10.0,0.0},{0.0,0.0,1.0,0.0,0.0,2.0,0.0,1.0,0.0,1.0,0.0,959.0,1.0,0.0,1.0,0.0,0.0,0.0,1.0,1.0},{0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,626.0,0.0,0.0,1.0,0.0,1.0,0.0,0.0,0.0},{0.0,1.0,0.0,1.0,0.0,2.0,1.0,0.0,0.0,0.0,0.0,960.0,0.0,0.0,3.0,0.0,2.0,4.0,1.0,4.0},{0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,1.0,0.0,981.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0},{0.0,0.0,0.0,1.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,908.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0},{0.0,0.0,0.0,0.0,0.0,1.0,0.0,0.0,0.0,0.0,0.0,797.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0},{0.0,0.0,2.0,0.0,0.0,0.0,2.0,0.0,0.0,0.0,0.0,982.0,2.0,1.0,0.0,0.0,0.0,0.0,0.0,1.0},{0.0,0.0,0.0,0.0,0.0,0.0,1.0,0.0,0.0,0.0,0.0,989.0,0.0,0.0,0.0,0.0,1.0,0.0,0.0,0.0},{0.0,0.0,0.0,0.0,0.0,1.0,0.0,0.0,0.0,0.0,0.0,985.0,0.0,0.0,0.0,1.0,0.0,0.0,0.0,0.0},{0.0,0.0,0.0,0.0,0.0,0.0,2.0,0.0,0.0,0.0,2.0,967.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,1.0},{0.0,0.0,0.0,0.0,0.0,2.0,0.0,0.0,0.0,0.0,0.0,995.0,0.0,1.0,0.0,0.0,0.0,0.0,0.0,0.0},{0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,993.0,0.0,0.0,0.0,0.0,0.0,1.0,0.0,0.0},{0.0,0.0,1.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,977.0,0.0,1.0,1.0,0.0,0.0,0.0,0.0,0.0},{0.0,0.0,0.0,0.0,10.0,6.0,0.0,0.0,0.0,0.0,0.0,962.0,0.0,1.0,0.0,0.0,1.0,1.0,1.0,2.0},{0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,988.0,0.0,1.0,0.0,0.0,0.0,0.0,1.0,0.0},{0.0,4.0,8.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,919.0,0.0,8.0,0.0,0.0,0.0,0.0,1.0,0.0}};
		ARI_sum_news_cleanText_tlda_tfidf_sklearn.add(evalARI(news_cleanText_tlda_tfidf_sklearn_1));
		PUR_sum_news_cleanText_tlda_tfidf_sklearn.add(evalPurity(news_cleanText_tlda_tfidf_sklearn_1));
		time_sum_news_cleanText_tlda_tfidf_sklearn.add(9230.05334997);
		Double[][] news_cleanText_tlda_tfidf_sklearn_2 = {{0.0,0.0,0.0,0.0,994.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0},{0.0,0.0,0.0,0.0,956.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,1.0,3.0,0.0},{0.0,0.0,0.0,0.0,772.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,1.0,0.0,0.0,0.0},{0.0,0.0,0.0,0.0,997.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0},{0.0,0.0,0.0,0.0,958.0,0.0,1.0,0.0,0.0,2.0,1.0,0.0,1.0,1.0,1.0,1.0,1.0,0.0,1.0,0.0},{0.0,0.0,0.0,0.0,627.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,1.0},{0.0,0.0,2.0,1.0,964.0,0.0,1.0,0.0,0.0,1.0,1.0,0.0,0.0,0.0,0.0,0.0,1.0,7.0,1.0,0.0},{0.0,0.0,0.0,0.0,979.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,1.0,0.0,0.0,0.0,0.0,0.0,0.0,2.0},{0.0,0.0,0.0,0.0,908.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,1.0,0.0,0.0},{0.0,0.0,0.0,0.0,797.0,0.0,0.0,0.0,1.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0},{0.0,0.0,0.0,0.0,988.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,1.0,1.0,0.0,0.0,0.0},{0.0,1.0,1.0,0.0,988.0,0.0,0.0,0.0,0.0,0.0,0.0,1.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0},{0.0,0.0,0.0,0.0,985.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,1.0,1.0},{0.0,0.0,0.0,0.0,966.0,0.0,0.0,2.0,1.0,0.0,1.0,0.0,0.0,0.0,0.0,0.0,0.0,1.0,0.0,1.0},{0.0,0.0,0.0,0.0,990.0,0.0,5.0,0.0,0.0,1.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,2.0},{0.0,0.0,0.0,0.0,993.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,1.0},{0.0,0.0,0.0,0.0,978.0,0.0,1.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,1.0,0.0},{0.0,0.0,3.0,2.0,962.0,0.0,2.0,1.0,0.0,11.0,0.0,0.0,0.0,0.0,0.0,1.0,0.0,0.0,0.0,2.0},{0.0,0.0,0.0,0.0,990.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0},{0.0,0.0,0.0,5.0,933.0,0.0,0.0,1.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,1.0,0.0}};
		ARI_sum_news_cleanText_tlda_tfidf_sklearn.add(evalARI(news_cleanText_tlda_tfidf_sklearn_2));
		PUR_sum_news_cleanText_tlda_tfidf_sklearn.add(evalPurity(news_cleanText_tlda_tfidf_sklearn_2));
		time_sum_news_cleanText_tlda_tfidf_sklearn.add(8520.34084201);
		Double[][] news_cleanText_tlda_tfidf_sklearn_3 = {{0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,994.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0},{0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,1.0,953.0,1.0,0.0,0.0,2.0,2.0,0.0,1.0,0.0,0.0},{1.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,772.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0},{0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,996.0,0.0,0.0,1.0,0.0,0.0,0.0,0.0,0.0,0.0},{0.0,0.0,1.0,0.0,0.0,0.0,0.0,1.0,2.0,0.0,954.0,1.0,2.0,0.0,0.0,3.0,2.0,1.0,1.0,0.0},{0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,628.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0},{1.0,0.0,3.0,0.0,1.0,1.0,0.0,1.0,0.0,2.0,958.0,2.0,4.0,1.0,0.0,2.0,0.0,0.0,0.0,3.0},{0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,1.0,0.0,979.0,0.0,0.0,0.0,0.0,1.0,0.0,0.0,0.0,1.0},{0.0,0.0,0.0,0.0,0.0,1.0,0.0,0.0,0.0,0.0,907.0,0.0,0.0,0.0,0.0,1.0,0.0,0.0,0.0,0.0},{0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,797.0,0.0,0.0,0.0,0.0,0.0,1.0,0.0,0.0,0.0},{0.0,0.0,0.0,0.0,0.0,1.0,0.0,0.0,1.0,2.0,984.0,1.0,0.0,0.0,0.0,0.0,0.0,1.0,0.0,0.0},{0.0,0.0,0.0,0.0,0.0,0.0,1.0,0.0,1.0,0.0,987.0,0.0,1.0,0.0,0.0,0.0,0.0,0.0,0.0,1.0},{0.0,1.0,0.0,0.0,0.0,0.0,1.0,0.0,0.0,0.0,984.0,1.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0},{0.0,2.0,0.0,1.0,0.0,1.0,0.0,0.0,1.0,0.0,962.0,3.0,0.0,0.0,1.0,0.0,0.0,1.0,0.0,0.0},{0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,2.0,992.0,3.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,1.0},{0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,993.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,1.0},{0.0,1.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,974.0,4.0,0.0,0.0,0.0,0.0,1.0,0.0,0.0,0.0},{1.0,0.0,0.0,0.0,0.0,2.0,0.0,0.0,0.0,3.0,959.0,0.0,0.0,1.0,0.0,2.0,1.0,1.0,1.0,13.0},{0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,989.0,0.0,0.0,0.0,0.0,1.0,0.0,0.0,0.0,0.0},{0.0,0.0,0.0,0.0,0.0,8.0,0.0,0.0,0.0,0.0,924.0,0.0,0.0,2.0,0.0,0.0,0.0,1.0,0.0,5.0}};
		ARI_sum_news_cleanText_tlda_tfidf_sklearn.add(evalARI(news_cleanText_tlda_tfidf_sklearn_3));
		PUR_sum_news_cleanText_tlda_tfidf_sklearn.add(evalPurity(news_cleanText_tlda_tfidf_sklearn_3));
		time_sum_news_cleanText_tlda_tfidf_sklearn.add(8741.23580909);
		Double[][] news_cleanText_tlda_tfidf_sklearn_4 = {{0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,994.0,0.0},{0.0,0.0,0.0,1.0,0.0,1.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,958.0,0.0},{0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,1.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,772.0,0.0},{0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,997.0,0.0},{0.0,0.0,1.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,1.0,0.0,1.0,0.0,0.0,0.0,1.0,1.0,961.0,2.0},{0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,628.0,0.0},{0.0,0.0,0.0,1.0,2.0,6.0,1.0,0.0,0.0,0.0,0.0,1.0,2.0,0.0,0.0,0.0,9.0,3.0,954.0,0.0},{0.0,0.0,0.0,0.0,0.0,0.0,0.0,1.0,0.0,0.0,0.0,3.0,0.0,0.0,0.0,0.0,0.0,0.0,978.0,0.0},{0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,1.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,908.0,0.0},{0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,1.0,0.0,0.0,0.0,797.0,0.0},{0.0,0.0,0.0,0.0,0.0,1.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,1.0,1.0,0.0,0.0,987.0,0.0},{0.0,0.0,0.0,0.0,2.0,0.0,2.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,1.0,986.0,0.0},{0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,1.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,986.0,0.0},{0.0,0.0,0.0,0.0,0.0,1.0,0.0,2.0,0.0,0.0,1.0,0.0,0.0,0.0,0.0,0.0,0.0,1.0,966.0,1.0},{0.0,0.0,0.0,2.0,1.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,994.0,1.0},{0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,1.0,0.0,992.0,1.0},{0.0,0.0,1.0,0.0,0.0,0.0,0.0,1.0,0.0,0.0,0.0,0.0,1.0,0.0,1.0,0.0,0.0,0.0,976.0,0.0},{2.0,0.0,0.0,0.0,11.0,1.0,0.0,0.0,2.0,0.0,0.0,0.0,4.0,1.0,1.0,0.0,0.0,0.0,961.0,1.0},{0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,990.0,0.0},{0.0,0.0,0.0,0.0,4.0,0.0,1.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,5.0,0.0,930.0,0.0}};
		ARI_sum_news_cleanText_tlda_tfidf_sklearn.add(evalARI(news_cleanText_tlda_tfidf_sklearn_4));
		PUR_sum_news_cleanText_tlda_tfidf_sklearn.add(evalPurity(news_cleanText_tlda_tfidf_sklearn_4));
		time_sum_news_cleanText_tlda_tfidf_sklearn.add(8724.52715707);
		Double[][] news_cleanText_tlda_tfidf_sklearn_5 = {{0.0,0.0,0.0,0.0,0.0,0.0,994.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0},{0.0,0.0,0.0,1.0,0.0,0.0,958.0,0.0,0.0,0.0,0.0,0.0,0.0,1.0,0.0,0.0,0.0,0.0,0.0,0.0},{0.0,0.0,0.0,0.0,1.0,0.0,772.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0},{0.0,0.0,0.0,0.0,0.0,0.0,997.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0},{0.0,1.0,1.0,0.0,0.0,0.0,959.0,1.0,0.0,2.0,0.0,0.0,1.0,0.0,1.0,0.0,0.0,0.0,1.0,1.0},{0.0,0.0,0.0,0.0,1.0,0.0,627.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0},{0.0,2.0,0.0,0.0,2.0,2.0,956.0,0.0,1.0,0.0,0.0,4.0,2.0,1.0,0.0,0.0,4.0,0.0,4.0,1.0},{3.0,0.0,1.0,0.0,0.0,0.0,978.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0},{1.0,0.0,0.0,0.0,0.0,0.0,908.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0},{0.0,0.0,0.0,0.0,1.0,0.0,797.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0},{0.0,0.0,0.0,0.0,0.0,1.0,989.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0},{0.0,0.0,1.0,0.0,0.0,0.0,987.0,0.0,0.0,0.0,0.0,0.0,1.0,0.0,0.0,0.0,0.0,1.0,0.0,1.0},{0.0,0.0,0.0,0.0,0.0,0.0,985.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,1.0,0.0,0.0,0.0,1.0,0.0},{0.0,0.0,0.0,0.0,0.0,0.0,965.0,0.0,2.0,2.0,0.0,0.0,0.0,1.0,0.0,0.0,0.0,0.0,2.0,0.0},{0.0,2.0,0.0,0.0,0.0,0.0,990.0,1.0,0.0,0.0,0.0,0.0,0.0,0.0,1.0,0.0,3.0,0.0,0.0,1.0},{0.0,0.0,0.0,0.0,0.0,0.0,993.0,0.0,1.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0},{4.0,1.0,0.0,0.0,0.0,0.0,971.0,0.0,0.0,2.0,0.0,0.0,0.0,1.0,0.0,0.0,0.0,0.0,1.0,0.0},{1.0,1.0,3.0,0.0,0.0,4.0,971.0,0.0,0.0,1.0,0.0,0.0,0.0,0.0,2.0,0.0,0.0,1.0,0.0,0.0},{0.0,0.0,0.0,0.0,0.0,1.0,989.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0},{0.0,1.0,0.0,0.0,0.0,0.0,931.0,0.0,0.0,0.0,0.0,8.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0}};
		ARI_sum_news_cleanText_tlda_tfidf_sklearn.add(evalARI(news_cleanText_tlda_tfidf_sklearn_5));
		PUR_sum_news_cleanText_tlda_tfidf_sklearn.add(evalPurity(news_cleanText_tlda_tfidf_sklearn_5));
		time_sum_news_cleanText_tlda_tfidf_sklearn.add(8564.65088916);
		Double[][] news_cleanText_tlda_tfidf_sklearn_6 = {{0.0,994.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0},{0.0,957.0,0.0,0.0,0.0,0.0,0.0,1.0,0.0,0.0,2.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0},{0.0,772.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,1.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0},{0.0,988.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,9.0,0.0,0.0},{1.0,959.0,0.0,2.0,0.0,1.0,0.0,0.0,1.0,0.0,1.0,0.0,0.0,0.0,1.0,0.0,0.0,1.0,0.0,1.0},{0.0,627.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,1.0,0.0,0.0,0.0,0.0},{0.0,960.0,3.0,0.0,0.0,1.0,0.0,0.0,3.0,4.0,2.0,1.0,1.0,0.0,0.0,4.0,0.0,0.0,0.0,0.0},{0.0,981.0,1.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0},{0.0,908.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,1.0,0.0,0.0,0.0,0.0},{0.0,797.0,0.0,1.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0},{0.0,987.0,0.0,0.0,1.0,0.0,0.0,0.0,0.0,1.0,1.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0},{0.0,987.0,1.0,1.0,0.0,0.0,1.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,1.0,0.0,0.0,0.0},{0.0,985.0,0.0,0.0,1.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,1.0,0.0,0.0},{0.0,966.0,0.0,1.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,3.0,0.0,1.0,0.0,0.0,1.0,0.0},{0.0,994.0,1.0,0.0,0.0,0.0,0.0,0.0,1.0,0.0,0.0,1.0,0.0,1.0,0.0,0.0,0.0,0.0,0.0,0.0},{0.0,993.0,0.0,0.0,0.0,1.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0},{0.0,974.0,0.0,0.0,0.0,0.0,0.0,2.0,0.0,0.0,0.0,0.0,0.0,3.0,0.0,0.0,0.0,1.0,0.0,0.0},{0.0,962.0,1.0,0.0,2.0,0.0,0.0,1.0,0.0,0.0,1.0,0.0,11.0,0.0,1.0,2.0,1.0,0.0,0.0,2.0},{0.0,989.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,1.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0},{8.0,924.0,0.0,0.0,0.0,5.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,2.0,1.0,0.0,0.0,0.0,0.0,0.0}};
		ARI_sum_news_cleanText_tlda_tfidf_sklearn.add(evalARI(news_cleanText_tlda_tfidf_sklearn_6));
		PUR_sum_news_cleanText_tlda_tfidf_sklearn.add(evalPurity(news_cleanText_tlda_tfidf_sklearn_6));
		time_sum_news_cleanText_tlda_tfidf_sklearn.add(8611.36470294);
		System.out.println("ARI lda_tfidf_sklearn");
		Collections.sort( ARI_sum_news_cleanText_tlda_tfidf_sklearn);
		stats(ARI_sum_news_cleanText_tlda_tfidf_sklearn);
		System.out.println("PUR lda_tfidf_sklearn");
		Collections.sort( PUR_sum_news_cleanText_tlda_tfidf_sklearn);
		stats(PUR_sum_news_cleanText_tlda_tfidf_sklearn);
		System.out.println("Time lda_tfidf_sklearn");
		Collections.sort( time_sum_news_cleanText_tlda_tfidf_sklearn);
		stats(time_sum_news_cleanText_tlda_tfidf_sklearn);*/
		
		Double[][] mat1 = { { 8.0, 0.0 }, { 0.0, 12.0} };
		Double[][] mat2 = { { 1.0, 0.0 }, { 0.0, 1.0} };
		Double[][] mat3 = { { 0.9, 0.1 }, { 0.1, 0.9} };
		Double[][] mat4 = { { 900.0, 0.0, 0.0 }, { 900.0, 0.0, 0.0}, { 900.0, 0.0, 0.0} };
		Double[][] mat5 = { { 900.0, 900.0, 900.0 }, { 0.0, 0.0, 0.0}, { 0.0, 0.0, 0.0} };
		
		print(mat1);
		print(mat2);
		print(mat3);
		print(mat4);
		print(mat5);
		
	}
	
	private static void print(Double[][] mat)
	{
		System.out.println("Purity: " + evalPurity(mat) + " // ARI: " + evalARI(mat));
	}
	
}
