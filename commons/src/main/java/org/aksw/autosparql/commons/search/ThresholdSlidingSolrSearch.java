package org.aksw.autosparql.commons.search;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ThresholdSlidingSolrSearch extends SolrSearch {

	private double minThreshold = 0.8;
	private double step = 0.1;

	private NumberFormat format = NumberFormat.getInstance(Locale.GERMAN);

	public ThresholdSlidingSolrSearch(String solrServerURL) {
		super(solrServerURL);
	}

	public ThresholdSlidingSolrSearch(String solrServerURL, String searchField) {
		super(solrServerURL, searchField);
	}

	public ThresholdSlidingSolrSearch(String solrServerURL, String searchField, double minThreshold, double step) {
		this(solrServerURL, searchField, null, minThreshold, step);
	}

	public ThresholdSlidingSolrSearch(String solrServerURL, String searchField, String labelField, double minThreshold, double step) {
		super(solrServerURL, searchField, labelField);
		this.minThreshold = minThreshold;
		this.step = step;
	}

	public ThresholdSlidingSolrSearch(SolrSearch search){
		super(search.getServerURL(), search.getSearchField(), search.getLabelField());
	}


	@Override
	public List<String> getResources(String queryString, int limit) {
		List<String> resources = new ArrayList<String>();


		double threshold = 1;

		String queryWithThreshold = queryString;
		while(resources.size() < limit && threshold >= minThreshold){
			if(threshold < 1){
				queryWithThreshold = queryString + "~" + format.format(threshold);
			}

			resources.addAll(getResources(queryWithThreshold, limit - resources.size()));
			threshold -= step;
		}

		return resources;
	}

	@Override
	public SolrQueryResultSet getResourcesWithScores(String queryString, int limit, int offset, boolean sorted) {
		SolrQueryResultSet rs = new SolrQueryResultSet();

		double threshold = 1;

		String queryWithThreshold = queryString;
		while(rs.getItems().size() < limit && threshold >= minThreshold){
			if(threshold < 1){
				queryWithThreshold = queryString + "~" + format.format(threshold);
			}

			rs.add(findResourcesWithScores(queryWithThreshold, limit - rs.getItems().size(), 0, sorted));
			threshold -= step;
		}

		return rs;
	}

}