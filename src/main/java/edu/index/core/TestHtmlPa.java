package edu.index.core;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public class TestHtmlPa {
	public static void main(String[] args) {
		//File in = new File("E:/weblech/sites/bjx.sdust.edu.cn/index.html");
		File in = new File("E:/weblech/sites/jpkc.sdust.edu.cn/down2.htm");
		try {
			MyHtmlParser mhp = new MyHtmlParser(in);
//			System.out.println(mhp.getPath());
//			System.out.println(mhp.getUrl());
//			System.out.println("-----------------title------------------------");
//			System.out.println(mhp.getTitle());
//			System.out.println("----------------------abstr-------------------");
//			System.out.println(mhp.getAbstr());
//			System.out.println("--------------content---------------------------");
//			System.out.println(mhp.getContent());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
