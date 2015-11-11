package com.project.informationRetrieval.crawler;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;

public class CrawlerController {

	private CrawlConfig config;

	private CrawlController controller;

	private static int NUMBER_OF_CRAWLERS = 1;

	private void initConfig() {
		config = new CrawlConfig();
		config.setCrawlStorageFolder("D:\\Programming\\Java_64_Workspace\\informationRetrieval\\Corpus\\Meta");
		config.setMaxPagesToFetch(-1);
		config.setMaxDepthOfCrawling(4);
		config.setPolitenessDelay(100);
		config.setUserAgentString("Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/40.0.2214.111 Safari/537.36");
		config.setResumableCrawling(false);
	}

	private void initSeeds() {
		controller.addSeed("http://techcrunch.com/topic/");
		//controller.addSeed("http://techcrunch.com/topic/subject/internet-of-things/");
	}

	public CrawlerController() throws Exception {
		initConfig();
		
		PageFetcher pageFetcher = new PageFetcher(config);
		RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
		
		robotstxtConfig.setEnabled(false);
		robotstxtConfig.setUserAgentName("Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/40.0.2214.111 Safari/537.36");
		
		RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig,
				pageFetcher);
		
		controller = new CrawlController(config, pageFetcher, robotstxtServer);
		
		initSeeds();
	}

	public void start() {
		controller.start(Crawler.class, NUMBER_OF_CRAWLERS);
	}

	public void stop() {
		controller.shutdown();
	}

}
