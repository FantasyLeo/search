package edu.index.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * <p>
 * 在线性时间内抽取主题类（新闻、博客等）网页的正文。 采用了<b>基于行块分布函数</b>的方法，
 * 为保持通用性没有针对特定网站编写规则。
 * </p>
 * 
 * @author Liu Hongtao
 * @version 1.0, 2015-05-28
 */
public class TextExtract {

	private final static int blocksWidth;
	

	static {
		blocksWidth = 3;
	}


	/**
	 * 抽取网页正文，不判断该网页是否是目录型。即已知传入的肯定是可以抽取正文的主题类网页。
	 * 
	 * @param _html
	 *            网页HTML字符串
	 * 
	 * @return 网页正文string
	 */
	public static String parse(String _html) {
		return parse(_html, 86);
	}

	/**
	 * 判断传入HTML，若是主题类网页，则抽取正文；否则输出<b>"unkown"</b>。
	 * 
	 * @param _html
	 *            网页HTML字符串
	 * @param threshold
	 *            当待抽取的网页正文中遇到成块的新闻标题未剔除时，只要增大此阈值即可。
	 * 阈值增大，准确率提升，召回率下降；值变小，噪声会大，但可以保证抽到只有一句话的正文 
	 * 
	 * @return 网页正文string
	 */
	public static String parse(String _html,int threshold) {
		_html = preProcess(_html);
//		System.out.println("=====================================================");
//		System.out.println(_html);
//		System.out.println("=====================================================");
		/* 当待抽取的网页正文中遇到成块的新闻标题未剔除时，只要增大此阈值即可。 */
		/* 阈值增大，准确率提升，召回率下降；值变小，噪声会大，但可以保证抽到只有一句话的正文 */
		return getText(_html,threshold);
	}

	public static String preProcess(String html) {
		html = html.replaceAll("(?is)<!DOCTYPE.*?>", "");
		html = html.replaceAll("(?is)<!--.*?-->", ""); // remove html comment
		html = html.replaceAll("(?is)<script.*?>.*?</script>", ""); // remove javascript
		html = html.replaceAll("(?is)<noscript.*?>.*?</noscript>", "");
		html = html.replaceAll("(?is)<style.*?>.*?</style>", ""); // remove css
		html = html.replaceAll("&.{2,5};|&#.{2,5};", " "); // remove special char
		html = html.replaceAll("(?is)<.*?>", "");
		// <!--[if !IE]>|xGv00|9900d21eb16fa4350a3001b3974a9415<![endif]-->
		return html;
	}

	/**
	 * @param html 去除无效标签字符的 html字符串
	 * @return
	 */
	public static String getText(String html,	int threshold) {
		ArrayList<Integer> indexDistribution = new ArrayList<Integer>();
		List<String> lines = Arrays.asList(html.split("\n"));

		for (int i = 0; i < lines.size() - blocksWidth; i++) {
			int wordsNum = 0;

			for (int j = i; j < i + blocksWidth; j++) {
				lines.set(j, lines.get(j).replaceAll("\\s+", " "));//不能去除所有空格，兼容英文单词
				wordsNum += lines.get(j).length() -1;
			}

			indexDistribution.add(wordsNum);
			// System.out.println(wordsNum);
		}

		int start = -1;
		int  end = -1;
		boolean boolstart = false, boolend = false;
		StringBuilder text = new StringBuilder();

		for (int i = 0; i < indexDistribution.size() - 1; i++) {
			if (indexDistribution.get(i) > threshold && !boolstart) {
				if (indexDistribution.get(i + 1).intValue() != 0
						|| indexDistribution.get(i + 2).intValue() != 0
						|| indexDistribution.get(i + 3).intValue() != 0) {
					boolstart = true;
					start = i;
					continue;
				}
			}

			if (boolstart) {
				if (indexDistribution.get(i).intValue() == 0
						|| indexDistribution.get(i + 1).intValue() == 0) {
					end = i;
					boolend = true;
				}
			}

			StringBuilder tmp = new StringBuilder();
			if (boolend) {
				// System.out.println(start+1 + "\t\t" + end+1);
				for (int ii = start; ii <= end; ii++) {
					if (lines.get(ii).length() < 5) {//判断是否是空行
						continue;
					}

					tmp.append(lines.get(ii) + "\n");
				}
				String str = tmp.toString();
				// System.out.println(str);
				if (str.contains("Copyright") || str.contains("版权所有")) {
					continue;
				}

				text.append(str);
				boolstart = boolend = false;
			}
		}

		return text.toString();
	}
}
