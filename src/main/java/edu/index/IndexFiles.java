package edu.index;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Date;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import edu.index.core.MyHtmlParser;

/**
 * 建立索引
 */
public class IndexFiles {

	private IndexFiles() {
	}

	/** Index all text files under a directory. */
	public static void main(String[] args) {
		/* 索引保存路径 */
		String indexPath = "e:\\index_ik";

		/* 资源文件路径 */
		String docsPath ="E:\\weblech\\sites";
		boolean create = true;

		final Path docDir = Paths.get(docsPath);
		if (!Files.isReadable(docDir)) {
			System.out
					.println("Document directory '"
							+ docDir.toAbsolutePath()
							+ "' does not exist or is not readable, please check the path");
			System.exit(0);
		}

		Date start = new Date();
		try {
			System.out.println("Indexing to directory '" + indexPath + "'...");

			Directory dir = FSDirectory.open(Paths.get(indexPath));
//			Analyzer analyzer = new StandardAnalyzer();
//			Analyzer analyzer = new IKAnalyzer();
			Analyzer analyzer = new SmartChineseAnalyzer(true);
			IndexWriterConfig iwc = new IndexWriterConfig(analyzer);

			if (create) {
				// Create a new index in the directory, removing any
				// previously indexed documents:
				iwc.setOpenMode(OpenMode.CREATE);
			} else {
				// Add new documents to an existing index:
				iwc.setOpenMode(OpenMode.CREATE_OR_APPEND);
			}

			// Optional: for better indexing performance, if you
			// are indexing many documents, increase the RAM
			// buffer. But if you do this, increase the max heap
			// size to the JVM (eg add -Xmx m or -Xmx g):
			// iwc.setRAMBufferSizeMB( .);
			IndexWriter writer = new IndexWriter(dir, iwc);
			indexDocs(writer, docDir);

			writer.close();

			Date end = new Date();
			System.out.println(end.getTime() - start.getTime()
					+ " total milliseconds");

		} catch (IOException e) {
			System.out.println(" caught a " + e.getClass()
					+ "\n with message: " + e.getMessage());
		}
	}

	/**
	 * Indexes the given file using the given writer, or if a directory is
	 * given, recurses over files and directories found under the given
	 * directory.
	 * 
	 * @param writer
	 *            Writer to the index where the given file/dir info will be
	 *            stored
	 * @param path
	 *            The file to index, or the directory to recurse into to find
	 *            files to index
	 * @throws Exception 
	 */
	static void indexDocs(final IndexWriter writer, Path path)
			throws IOException {
		if (Files.isDirectory(path)) {
			Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
				/*
				 *访问该目录下所有的文件
				 */
				@Override
				public FileVisitResult visitFile(Path file,
						BasicFileAttributes attrs) throws IOException {
					try {
						int lastIndex = file.toString().lastIndexOf('.');
						if(lastIndex < 0) {
							return FileVisitResult.CONTINUE;
						}

						String subfix = file.toString().substring(lastIndex);
						if(subfix.contains(".html") 
							|| subfix.contains(".htm")
							|| subfix.contains(".aspx")
							|| subfix.contains(".asps")
							|| subfix.contains(".jsp")
							|| subfix.contains(".php")) {
							indexDoc(writer, file, attrs.lastModifiedTime()
									.toMillis());
						}
					} catch (IOException ignore) {
						// don't index files that can't be read.
						System.err.println("ignore_______");
						//ignore.printStackTrace();
					} catch (Exception e) {
						System.err.println("Exception 179______");
						//e.printStackTrace();
					}
					return FileVisitResult.CONTINUE;
				}

			});
		} else {
			int lastIndex = path.toString().lastIndexOf('.');
			if(lastIndex < 0) {
				return ;
			}

			String subfix = path.toString().substring(lastIndex);
			if(subfix.contains(".html") 
				|| subfix.contains(".htm")
				|| subfix.contains(".aspx")
				|| subfix.contains(".asps")
				|| subfix.contains(".jsp")
				|| subfix.contains(".php")) {
				indexDoc(writer, path, Files.getLastModifiedTime(path).toMillis());
			}
		}
	}

	/** Indexes a single document 
	 * @throws IOException */
	static void indexDoc(IndexWriter writer, Path file, long lastModified)
			throws IOException {
		try (MyHtmlParser mhp = new MyHtmlParser(file.toFile())) {
			if(mhp.getTitle() == null || mhp.getContent() == null) {
				//网页没有标题，跳过
				return;
			}

			// make a new, empty document
			Document doc = new Document();

			//路径域
			Field pathField = new StringField("path", mhp.getPath(),
					Field.Store.YES);
			doc.add(pathField);

			//标题域
			Field titleField = new TextField("title", mhp.getTitle(),
					Field.Store.YES);
			doc.add(titleField);

			//摘要域
			Field abstrField = new StringField("abstr", mhp.getAbstr(),
					Field.Store.YES);
			doc.add(abstrField);

			//url路径域
			doc.add(new StringField("url",mhp.getUrl(),Field.Store.YES));

			// Add the last modified date of the file a field named "modified".
			// Use a LongField that is indexed (i.e. efficiently filterable with
			// NumericRangeFilter). This indexes to milli-second resolution,
			// which
			// is often too fine. You could instead create a number based on
			// year/month/day/hour/minutes/seconds, down the resolution you
			// require.
			// For example the long value would mean
			// February , , - PM.
			doc.add(new LongField("modified", lastModified, Field.Store.NO));

			doc.add(new TextField("contents", new StringReader(mhp.getContent())));

			if (writer.getConfig().getOpenMode() == OpenMode.CREATE) {
				System.out.println("adding " + file);
				writer.addDocument(doc);
			} else {
				System.out.println("updating " + file);
				writer.updateDocument(new Term("path", file.toString()), doc);
			}
		}
	}
}
