package wordTools;

import org.apache.lucene.search.spans.SpanNearQuery;

public class SpanQueryList {

	private SpanNearQuery snq[];

	public SpanQueryList(SpanNearQuery[] snqs) {
		snq = snqs;
	}

	/**
	 * protected method to allow GPs to retrieve spans from the array
	 */
	public SpanNearQuery getSNQ(int x) {

		if (snq == null || snq.length < 1) {
			System.err.println("error in wordList: empty or null list");
			return null;
		}

		if (x >= snq.length || x < 0) {
			return snq[0];
		}

		return snq[x];
	}

	public SpanNearQuery[] getWordArray() {
		return snq;
	}

	public int size() {
		return snq.length;
	}

	public String toString() {
		
		if (snq == null)
			return " spanNearQuery List is null ";
		
		String report = new String();
		for (int x = 0; x < 40; x++) {
			report = report + snq[x] + " ";
		}

		return "Size of spanNearQuery array: " + snq.length + " " + report;
	}
}
