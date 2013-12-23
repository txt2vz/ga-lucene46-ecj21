   package lucene;

import org.apache.lucene.index.SlowCompositeReaderWrapper
import org.apache.lucene.index.Term
import org.apache.lucene.index.Terms
import org.apache.lucene.index.TermsEnum
import org.apache.lucene.search.Filter
import org.apache.lucene.search.IndexSearcher
import org.apache.lucene.search.Query
import org.apache.lucene.search.TermQuery
import org.apache.lucene.search.TotalHitCountCollector
import org.apache.lucene.search.spans.SpanFirstQuery
import org.apache.lucene.search.spans.SpanTermQuery
import org.apache.lucene.util.BytesRef
import wordTools.*
import query.*;


/**
 * GP functions may return words by selecting form word lists provided by this
 * class. The words should be as far as possible in order of their likely
 * usefulness in query building
 * 
 * @author Laurie
 * 
 */

public class ImportantWords {

	public static final int SPAN_FIRST_MAX_END = 300;
	private final static int MAX_WORDLIST_SIZE = 100;

	private final IndexSearcher indexSearcher = IndexInfoStaticG
	.getIndexSearcher();

	private Set<String> stopSet = StopLists.getStopSet()

	public static void main(String[] args){
		def iw = new ImportantWords()
	}

	public ImportantWords() throws IOException {

		//def wl = getF1WordList(false, true)
	}

	/**
	 * create a set of words based on F1 measure of the word as a query
	 */
	public String[] getF1WordList(boolean spanFirstQ, boolean positiveList)
	throws IOException{

		Terms terms = SlowCompositeReaderWrapper.wrap(indexSearcher.getIndexReader()).terms(IndexInfoStaticG.FIELD_CONTENTS);

		println "Important words terms.getDocCount: ${terms.getDocCount()}"
		println "Important words terms.size ${terms.size()}"

		TermsEnum termsEnum = terms.iterator(null);
		BytesRef text;
		termsEnum = terms.iterator(null);

		def wordMap = [:]

		while((text = termsEnum.next()) != null) {

			def word = text.utf8ToString()

			final Term t = new Term(IndexInfoStaticG.FIELD_CONTENTS, word);

			if (indexSearcher.getIndexReader().docFreq(t) < 3
						|| stopSet.contains(t.text()))
			continue;

			Query q;
			if (spanFirstQ){
				q = new SpanFirstQuery(new SpanTermQuery(t),
						SPAN_FIRST_MAX_END);
			}
			else
			{
				q = new TermQuery(t);
			}

			Filter filter0, filter1;
			int totalDocs;

			if (positiveList) {
				filter0 = IndexInfoStaticG.catTrainF;
				filter1 = IndexInfoStaticG.othersTrainF;
				totalDocs = IndexInfoStaticG.totalTrainDocsInCat;
			} else {
				filter0 = IndexInfoStaticG.othersTrainF;
				filter1 = IndexInfoStaticG.catTrainF;
				totalDocs = IndexInfoStaticG.totalOthersTrainDocs;
			}

			TotalHitCountCollector collector  = new TotalHitCountCollector();
			indexSearcher.search(q, filter0, collector);
			final int positiveHits = collector.getTotalHits();

			collector  = new TotalHitCountCollector();
			indexSearcher.search(q, filter1, collector);
			final int negativeHits = collector.getTotalHits();

			def F1 = ClassifyQuery.f1(positiveHits, negativeHits,
					totalDocs);

			if (F1 > 0.05) {
				wordMap += [(word): F1]
			}
		}

		wordMap= wordMap.sort{a, b -> b.value <=> a.value}

		List wordList = wordMap.keySet().toList().take(MAX_WORDLIST_SIZE)
		println "map size: ${wordMap.size()}  List size is ${wordList.size()}  list is $wordList"

		return wordList.toArray();
	}
}