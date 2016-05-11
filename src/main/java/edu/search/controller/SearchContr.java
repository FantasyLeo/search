package edu.search.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.highlight.Fragmenter;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.apache.lucene.search.highlight.NullFragmenter;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleFragmenter;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.search.highlight.TextFragment;
import org.apache.lucene.search.highlight.TokenSources;
import org.apache.lucene.store.FSDirectory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import edu.index.core.MyHtmlParser;
import edu.search.core.SearcherUtils;

@Controller
public class SearchContr {

	@RequestMapping("search")
	@ResponseBody
	public Object search(String queryString, Integer curPage, Integer an) {
		List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
		Map<String, Object> resp = new HashMap<String, Object>();
		if (curPage == null || curPage < 1) {
			curPage = 1;
		}

		if (curPage > 10) {
			curPage = 10;
		}

		String index = "e:\\index";

		switch (an) {
		case 1:
			index = "e:\\index_std";
			break;
		case 2:
			index = "e:\\index_sc";
			break;
		case 3:
			index = "e:\\index_ik";
			break;
		}

		String queries = null;
		int repeat = 0;
		boolean raw = false;
		int hitsPerPage = 10;
		IndexReader reader = null;

		try {
			reader = DirectoryReader.open(FSDirectory.open(Paths.get(index)));
			IndexSearcher searcher = new IndexSearcher(reader);
			Analyzer analyzer = SearcherUtils.chooseAnalyzer(an);
			// Analyzer analyzer = new StandardAnalyzer();
			// QueryParser parser = new QueryParser(field, analyzer);
			// Query query = parser.parse(queryString);
			Query query = MultiFieldQueryParser.parse(queryString,
					new String[] { "title", "contents" }, new Occur[] {
							Occur.SHOULD, Occur.MUST }, analyzer);

			TopDocs results = searcher.search(query, 10 * hitsPerPage);
			ScoreDoc[] hits = results.scoreDocs;

			/* 关键词高亮显示 */
			Fragmenter fragmenter = new SimpleFragmenter(160);
			NullFragmenter nullFragmenter = new NullFragmenter();
			SimpleHTMLFormatter htmlFormatter = new SimpleHTMLFormatter("<font color='red'>", "</font>");
			Highlighter highlighter = new Highlighter(htmlFormatter,
					new QueryScorer(query));

			Document doc = null;
			Map<String, Object> map = null;
			resp.put("total", hits.length);
			resp.put("curPage", curPage);

			for (int i = curPage - 1; i < hits.length; i++) {
				doc = searcher.doc(hits[i].doc);
//				TokenStream tokenStream = TokenSources.getAnyTokenStream(
//						searcher.getIndexReader(), hits[i].doc, "contents", analyzer);
				String text = new MyHtmlParser(new File(doc.get("path"))).getContent();
				TokenStream tokenStream = analyzer.tokenStream("contents", text);
				TextFragment[] frag = null;
				try {
					highlighter.setTextFragmenter(fragmenter);
					frag = highlighter.getBestTextFragments(
							tokenStream, 
							text,
							false, 1);
				} catch (InvalidTokenOffsetsException e) {
					e.printStackTrace();
				}

				map = new HashMap<String, Object>();
				map.put("path", doc.get("path"));

				if(frag != null && frag.length > 0) {
					if ((frag[0] != null) && (frag[0].getScore() > 0)) {
						map.put("abstr", frag[0].toString());
					}else {
						map.put("abstr", doc.get("abstr"));
					}

					if(i < 3) {
						for (int j = 0; j < frag.length; j++) {
							if ((frag[j] != null) && (frag[j].getScore() > 0)) {
								System.out.println((frag[j].toString()));
								System.out.println("-----------------------------");
							}
						}
						System.out.println("============================");
					}
				} else {
					map.put("abstr", doc.get("abstr"));
				}
				//map.put("abstr", doc.get("abstr"));
				map.put("url", doc.get("url"));

				highlighter.setTextFragmenter(nullFragmenter);
				tokenStream = TokenSources.getAnyTokenStream(
						searcher.getIndexReader(), hits[i].doc, "title", analyzer);

				String title = null;
				try {
					title = highlighter.getBestFragment(
							tokenStream, 
							doc.get("title"));
				} catch (InvalidTokenOffsetsException e) {
					e.printStackTrace();
				}

				if(title != null&& title.length() > 0) {
					map.put("title", title);
				} else {
					map.put("title", doc.get("title"));//
				}

				result.add(map);
			}
			resp.put("results", result);

			return resp;
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
				}
			}
		}

		return "error";
	}

	@RequestMapping("searchTitle")
	@ResponseBody
	public Object searchTitle(String queryString, Integer curPage, Integer an) {
		List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
		Map<String, Object> resp = new HashMap<String, Object>();
		if (curPage == null || curPage < 1) {
			curPage = 1;
		}

		if (curPage > 10) {
			curPage = 10;
		}

		String index = "e:\\index";

		switch (an) {
		case 1:
			index = "e:\\index_std";
			break;
		case 2:
			index = "e:\\index_sc";
			break;
		case 3:
			index = "e:\\index_ik";
			break;
		}

		String queries = null;
		int repeat = 0;
		boolean raw = false;
		int hitsPerPage = 10;
		IndexReader reader = null;

		try {
			reader = DirectoryReader.open(FSDirectory.open(Paths.get(index)));
			IndexSearcher searcher = new IndexSearcher(reader);
			Analyzer analyzer = SearcherUtils.chooseAnalyzer(an);
			// Analyzer analyzer = new StandardAnalyzer();
			QueryParser parser = new QueryParser("title", analyzer);
			 Query query = parser.parse(queryString);

			TopDocs results = searcher.search(query, 10 * hitsPerPage);
			ScoreDoc[] hits = results.scoreDocs;

			/* 关键词高亮显示 */
			NullFragmenter nullFragmenter = new NullFragmenter();
			SimpleHTMLFormatter htmlFormatter = new SimpleHTMLFormatter("<font color='red'>", "</font>");
			Highlighter highlighter = new Highlighter(htmlFormatter,
					new QueryScorer(query));

			Document doc = null;
			Map<String, Object> map = null;
			resp.put("total", hits.length);
			resp.put("curPage", curPage);

			for (int i = curPage - 1; i < hits.length; i++) {
				doc = searcher.doc(hits[i].doc);
//				TokenStream tokenStream = TokenSources.getAnyTokenStream(
//						searcher.getIndexReader(), hits[i].doc, "contents", analyzer);

				map = new HashMap<String, Object>();
				map.put("path", doc.get("path"));
				map.put("abstr", doc.get("abstr"));
				map.put("url", doc.get("url"));

				highlighter.setTextFragmenter(nullFragmenter);
				TokenStream tokenStream = TokenSources.getAnyTokenStream(
						searcher.getIndexReader(), hits[i].doc, "title", analyzer);

				String title = null;
				try {
					title = highlighter.getBestFragment(
							tokenStream, 
							doc.get("title"));
				} catch (InvalidTokenOffsetsException e) {
					e.printStackTrace();
				}

				if(title != null&& title.length() > 0) {
					map.put("title", title);
				} else {
					map.put("title", doc.get("title"));//
				}

				result.add(map);
			}
			resp.put("results", result);

			return resp;
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
				}
			}
		}

		return "error";
	}

	@RequestMapping("searchContent")
	@ResponseBody
	public Object searchContent(String queryString, Integer curPage, Integer an) {
		List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
		Map<String, Object> resp = new HashMap<String, Object>();
		if (curPage == null || curPage < 1) {
			curPage = 1;
		}

		if (curPage > 10) {
			curPage = 10;
		}

		String index = "e:\\index";

		/* 匹配索引目录*/
		switch (an) {
		case 1:
			index = "e:\\index_std";
			break;
		case 2:
			index = "e:\\index_sc";
			break;
		case 3:
			index = "e:\\index_ik";
			break;
		}

		int hitsPerPage = 10;
		IndexReader reader = null;

		try {
			reader = DirectoryReader.open(FSDirectory.open(Paths.get(index)));
			IndexSearcher searcher = new IndexSearcher(reader);
			Analyzer analyzer = SearcherUtils.chooseAnalyzer(an);
			// Analyzer analyzer = new StandardAnalyzer();
			 QueryParser parser = new QueryParser("contents", analyzer);
			 Query query = parser.parse(queryString);

			TopDocs results = searcher.search(query, 10 * hitsPerPage);
			ScoreDoc[] hits = results.scoreDocs;

			/* 关键词高亮显示 */
			Fragmenter fragmenter = new SimpleFragmenter(160);
			SimpleHTMLFormatter htmlFormatter = new SimpleHTMLFormatter("<font color='red'>", "</font>");
			Highlighter highlighter = new Highlighter(htmlFormatter,
					new QueryScorer(query));

			Document doc = null;
			Map<String, Object> map = null;
			resp.put("total", hits.length);
			resp.put("curPage", curPage);

			for (int i = curPage - 1; i < hits.length; i++) {
				doc = searcher.doc(hits[i].doc);
				String text = new MyHtmlParser(new File(doc.get("path"))).getContent();
				TokenStream tokenStream = analyzer.tokenStream("contents", text);
				TextFragment[] frag = null;
				try {
					highlighter.setTextFragmenter(fragmenter);
					frag = highlighter.getBestTextFragments(
							tokenStream, 
							text,
							false, 1);
				} catch (InvalidTokenOffsetsException e) {
					e.printStackTrace();
				}

				map = new HashMap<String, Object>();
				map.put("path", doc.get("path"));

				if(frag != null && frag.length > 0) {
					if ((frag[0] != null) && (frag[0].getScore() > 0)) {
						map.put("abstr", frag[0].toString());
					}else {
						map.put("abstr", doc.get("abstr"));
					}
				} else {
					map.put("abstr", doc.get("abstr"));
				}

				//map.put("abstr", doc.get("abstr"));
				map.put("url", doc.get("url"));
				map.put("title", doc.get("title"));//

				result.add(map);
			}
			resp.put("results", result);

			return resp;
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
				}
			}
		}

		return "error";
	}

	@RequestMapping("s")
	public Object searches() {
		return "search";
	}
}
