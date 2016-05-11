package edu.search.task;

import weblech.ui.TextSpider;
import edu.index.IndexFiles;
/**
 * 获取网络资源，建立索引
 * 该类用用于管理更新索引流程
 * 被计划调度器使用
 * @author fantasy
 *
 */
public class MyTask {
	public static void main(String[] args) {
		System.out.println(Class.class.getClass().getResource("/").getPath() +"Spider.properties");
		args = new String[] {Class.class.getClass().getResource("/").getPath() +"Spider.properties"};
		//调用爬虫，爬取资源，更新资源
		TextSpider.main(args);
		//调用索引建立，更新索引
//		IndexFiles.main(null);
	}
}
