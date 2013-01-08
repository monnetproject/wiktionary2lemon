package eu.monnetproject.stl;

import java.io.File;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;

public class FilePathMetaDataExtractor {

	// namespaces
	static String dbp  = "http://dbpedia.org/resource/";
	static String unlp_wc = "http://unlp.deri.ie/WindCorpus#";

	// maps
	static String[][] companyMap = {{"vestas",dbp+"Vestas"},
		{"gamesa",dbp+"Gamesa"}};
	static String[][] doctypeMap = {{"finreps",unlp_wc+"Finrep"},
		{"news",unlp_wc+"News"}};
	static String[][] reporttypeMap  = {{"AR",unlp_wc+"AR"},
		{"Q1",unlp_wc+"Q1"},
		{"Q2",unlp_wc+"Q2"},
		{"Q3",unlp_wc+"Q3"},};

	private static void addAnnotation(Set<URI> annotations,String[][] map, String attributeValue) {
		for (int i = 0; i < map.length; i++) {
			String[] el = map[i];
			String uri = el[1];
			if (el[0].equals(attributeValue)) {
				annotations.add(URI.create(uri));
				break;
			}
		}
	}

	public static Set<URI> getMetaData(String path,String startPattern,String fileType,int companyNameIdx,int finRepDetailsIdx) {
		Set<URI> annotations = new HashSet<URI>();
		// remove start and end from path
		String endPattern = "\\."+fileType;
		path = path.replaceAll("^.*"+startPattern, "");
		path = path.replaceAll(endPattern+".*$", "");
		// split metadata values by file separator
		String[] metaDataValues;
		if (path.contains("\\"))
			metaDataValues = path.split("\\\\");
		else
			metaDataValues = path.split("\\/");

		// add company annotation
		String company = metaDataValues[companyNameIdx].toLowerCase();
		addAnnotation(annotations,companyMap,company);

		// process details
		boolean isReport = false;
		String details = metaDataValues[finRepDetailsIdx].toLowerCase();

		// quaterly or annual report
		String p1 = "[a-zA-Z0-9]{2,2}_20[0-9]{2,2}";
		String p2 = "20[0-9]{2}_[a-zA-Z0-9]{2}";
		if (details.matches(p1)||details.matches(p2)) {
			isReport=true;
			// offsets of reporttype and year value
			int offset_yr_start = 3;
			int offset_yr_end   = 7;
			int offset_type_start = 0;
			int offset_type_end   = 2;
			if (details.matches(p2)) {
				offset_yr_start = 0;
				offset_yr_end   = 4;
				offset_type_start = 5;
				offset_type_end   = 7;
			}
			// add year annotation
			String year = details.substring(offset_yr_start,offset_yr_end);
			URI yearURI = URI.create(unlp_wc+"year"+year);
			annotations.add(yearURI);
			// add reporttype annotation
			String reporttype = details.substring(offset_type_start,offset_type_end).toUpperCase();
			addAnnotation(annotations,reporttypeMap,reporttype);
		}

		if (isReport) {
			addAnnotation(annotations,doctypeMap,"finreps");
		}

		return annotations;
	}

}
