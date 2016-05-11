package edu.index.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
/**
 * 此类事对原始html文件处理
 * 抽取其标题，内容等信息
 * @author fantasy
 *
 */
public class MyHtmlParser implements AutoCloseable {
	private String title;
	private String content;
	private String url;
	private String path;
	private ByteBuffer buffer;//html页面字节缓存器

	public MyHtmlParser(File htmlFile) throws IOException {
		buffer = new ByteBuffer(20480);

		FileInputStream fis = new FileInputStream(htmlFile);//文件字节输入流
		byte[] buf = new byte[2048];//字节读取缓存区，该数字是经测试，优于 1024 和 4096
		int len = -1;
		while((len = fis.read(buf)) != -1) {
			buffer.add(buf, 0, len);
		}
		fis.close();

		/*  start - 对字符集的判读与读取 */
		String htmlstr = new String(buffer.arrayByte(), "utf-8");
		int headend = htmlstr.indexOf("</head>");

		if(headend < 0) {
			headend = htmlstr.indexOf("</HEAD>");
		}

		if(headend < 0) {
			return;
		}

		int charsetPos = htmlstr.substring(0, headend).indexOf("charset=");

		if(charsetPos != -1) {
			int end = htmlstr.indexOf('"' ,charsetPos);
			String charset = htmlstr.substring(charsetPos+8, end);
			if(charset != null && charset.length() > 0 && !"utf-8".equalsIgnoreCase(charset) ) {
//				System.out.println("charset[" + charset + "]");
				htmlstr = new String(buffer.arrayByte(), charset);
			}
		}
		/*  end - 对字符集的判读与读取 */

		int st = htmlstr.indexOf("<title>") + 7;
		int lt = htmlstr.indexOf("</title>");

		if(st > 0 && lt > st) {
			this.title = htmlstr.substring(st,lt);
		}

		String temp = TextExtract.preProcess(htmlstr);
		content = TextExtract.getText(temp, 86);
		if(content.length() < 140) {
			content = TextExtract.getText(temp, 16);
		}

		path = htmlFile.getAbsolutePath().replace('\\', '/');
		url = "http:/" + path.substring(16);
	}

	public String getTitle() {
		return title;
	}

	public String getAbstr() {
		if(content != null && content.length() > 140) {
			return content.substring(0, 140);
		}

		return content;
	}

	public String getContent() {
		return content;
	}


	public String getUrl() {
		return url;
	}

	public String getPath() {
		return path;
	}

	@Override
	public void close() throws IOException {
		// Nothing
	}
}
