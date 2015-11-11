package com.project.informationRetrieval.crawler;

import java.io.File;
import java.util.regex.Pattern;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.jsoup.*;
import org.jsoup.nodes.*;
import org.jsoup.select.Elements;

public class Crawler extends WebCrawler {
	private final static String CORPUS_LOCATION = "D:\\Programming\\Java_64_Workspace\\informationRetrieval\\Corpus\\";

	private final static Pattern RESOURCE_FILTER = Pattern
			.compile(".*(\\.(css|js|gif|jpe?g" + "|png|mp3|mp3|zip|gz))$");

	private final static Pattern TOPIC_PAGE = Pattern
			.compile("http://techcrunch.com/topic/[a-zA-Z|-]+/[a-zA-Z|-]+/(\\d+/)*");

	private final static Pattern ARTICLE_PAGE = Pattern
			.compile("http://techcrunch.com/\\d+/\\d+/\\d+/.*");

	private DocumentBuilderFactory docFactory = DocumentBuilderFactory
			.newInstance();
	private DocumentBuilder docBuilder;

	/**
	 * This method receives two parameters. The first parameter is the page in
	 * which we have discovered this new url and the second parameter is the new
	 * url. You should implement this function to specify whether the given url
	 * should be crawled or not (based on your crawling logic). In this example,
	 * we are instructing the crawler to ignore urls that have css, js, git, ...
	 * extensions and to only accept urls that start with
	 * "http://www.ics.uci.edu/". In this case, we didn't need the referringPage
	 * parameter to make the decision.
	 */
	@Override
	public boolean shouldVisit(Page referringPage, WebURL url) {
		String href = url.getURL().toLowerCase();

		boolean isTopicPage = TOPIC_PAGE.matcher(href).matches();
		boolean isArticlePage = ARTICLE_PAGE.matcher(href).matches();
		boolean isResource = RESOURCE_FILTER.matcher(href).matches();

		return (isTopicPage || isArticlePage) && !isResource;
	}

	/**
	 * This function is called when a page is fetched and ready to be processed
	 * by your program.
	 */
	@Override
	public void visit(Page page) {
		String url = page.getWebURL().getURL();

		if (ARTICLE_PAGE.matcher(url).matches()) {
			System.out.println("URL: " + url);

			HtmlParseData data = (HtmlParseData) page.getParseData();

			Document doc = Jsoup.parse(data.getHtml());
			Elements articles = doc.select(".article-entry");
			Element article = articles.first();
			
			String title = doc.select(".tweet-title").first().text();

			saveArticle(article, title, url);
		}
	}

	private void saveArticle(Element article, String title, String url) {
		String articleText = article.text();
		
		title = title.replace('?', ' ');
		title = title.replace('!', ' ');
		title = title.replace('.', ' ');
		title = title.replace(',', ' ');
		
		try {
			docBuilder = docFactory.newDocumentBuilder();
			org.w3c.dom.Document doc = docBuilder.newDocument();
			org.w3c.dom.Element rootElement = doc.createElement("Article");
			doc.appendChild(rootElement);

			org.w3c.dom.Element titleElement = doc.createElement("Title");
			titleElement.setTextContent(title);

			org.w3c.dom.Element contentElement = doc.createElement("Content");
			contentElement.setTextContent(articleText);

			org.w3c.dom.Element urlElement = doc.createElement("Url");
			urlElement.setTextContent(url);

			rootElement.appendChild(titleElement);
			rootElement.appendChild(urlElement);
			rootElement.appendChild(contentElement);

			TransformerFactory transformerFactory = TransformerFactory
					.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);

			String savePath = CORPUS_LOCATION + title + ".xml";
			StreamResult result = new StreamResult(new File(savePath));

			transformer.transform(source, result);
			
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformerConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
