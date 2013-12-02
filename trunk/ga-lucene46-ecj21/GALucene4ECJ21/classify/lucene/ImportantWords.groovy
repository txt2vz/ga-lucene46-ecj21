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
	private final static int MAX_WORDLIST_SIZE = 40;

	private final IndexSearcher indexSearcher = IndexWrapperG.getInstance()
	.getIndexSearcher();

	private Set<String> stopSet;

	public static void main(String[] args){
		def iw = new ImportantWords()
	}

	public ImportantWords() throws IOException {
		//stopSet = StopLists.textFileToStopList();
		IndexWrapperG.getInstance().setFilters();
		
		def l = getF1WordList(true)
	}

	/**
	 * create a set of words based on F1 measure of the word as a query
	 */
	public String[] getF1WordList(final boolean positiveList)
	throws IOException{

	
		Terms terms = SlowCompositeReaderWrapper.wrap(indexSearcher.getIndexReader()).terms(IndexWrapperG.FIELD_CONTENTS);

		println "terms.getDocCount: ${terms.getDocCount()}"
		println "terms.size ${terms.size()}"

		TermsEnum termsEnum = terms.iterator(null);
		BytesRef text;
		termsEnum = terms.iterator(null);
		
		//def termMap = [:]
		def wordMap = [:]

		while((text = termsEnum.next()) != null) {
			
			def word = text.utf8ToString()

			final Term t = new Term(IndexWrapperG .FIELD_CONTENTS, word);

			//	if (indexSearcher.getIndexReader().docFreq(t) < 2
			//			|| stopSet.contains(t.text()))
			//  		continue;

			//final Query q = new TermQuery(t);
			final Query sfq = new SpanFirstQuery(new SpanTermQuery(t),
					SPAN_FIRST_MAX_END);

			Filter filter0, filter1;
			int totalDocs;

			if (positiveList) {
				filter0 = IndexWrapperG.getInstance().catTrainF;
				filter1 = IndexWrapperG.getInstance().othersTrainF;
				totalDocs = IndexWrapperG.getInstance().totalTrainDocsInCat;
			} else {
				filter0 = IndexWrapperG.getInstance().othersTrainF;
				filter1 = IndexWrapperG.getInstance().catTrainF;
				totalDocs = IndexWrapperG.getInstance().totalOthersTrainDocs;
			}

			TotalHitCountCollector collector  = new TotalHitCountCollector();
			indexSearcher.search(sfq, filter0, collector);
			final int positiveHits = collector.getTotalHits();

			collector  = new TotalHitCountCollector();
			indexSearcher.search(sfq, filter1, collector);
			def negativeHits = collector.getTotalHits();

		    def F1 = ClassifyQuery.f1(positiveHits, negativeHits,
					totalDocs);

			if (F1 > 0.02) {

				//println "${text.utf8ToString()} word $word f1: $F1  positiveHits $positiveHits  neg hits $negativeHits"
				
				wordMap += [(word): F1]
				//println "wordmpa size " + wordMap.size()
			}			
		}
		println wordMap
		wordMap= wordMap.sort{a, b -> b.value <=> a.value}
		
		println "sorted wordMap $wordMap  and sorted wordMap $wordMap"
		
		def firstWord = wordMap.keySet().first()
		
		 Query sfqtest = new SpanFirstQuery(new SpanTermQuery(new Term(IndexWrapperG.FIELD_CONTENTS, firstWord )),
			SPAN_FIRST_MAX_END);		
		
		TotalHitCountCollector collector  = new TotalHitCountCollector();
		indexSearcher.search(sfqtest,  collector);
		def n = collector.getTotalHits();
		println "n is $n"
		
		List wordList = wordMap.keySet().toList().take(MAX_WORDLIST_SIZE)
		println "list size is ${wordList.size()}  list is $wordList"		
		
		return wordList.toArray();		
	}
}

