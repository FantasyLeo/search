package edu.search.core;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.wltea.analyzer.lucene.IKAnalyzer;


public class SearcherUtils {
	public static Analyzer chooseAnalyzer(int an) {
		Analyzer analyzer = null;
		switch(an) {
		case 1:
			analyzer = new StandardAnalyzer();
			break;
		case 2:
			analyzer = new SmartChineseAnalyzer();
			break;
		case 3:
			analyzer = new IKAnalyzer();
			break;
		default:
			analyzer = new IKAnalyzer();
		}

		return analyzer;
	}
}
