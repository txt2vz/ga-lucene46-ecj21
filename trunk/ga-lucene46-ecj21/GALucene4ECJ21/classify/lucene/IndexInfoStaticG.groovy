package lucene

import java.io.File;
import java.io.IOException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Fields
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms
import org.apache.lucene.index.TermsEnum
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.CachingWrapperFilter;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.IndexSearcher;
//import org.apache.lucene.search.MultiSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.QueryWrapperFilter;
import org.apache.lucene.search.ScoreDoc;
//import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocsCollector;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.search.TotalHitCountCollector;
import org.apache.lucene.search.spans.SpanFirstQuery;
import org.apache.lucene.search.spans.SpanNearQuery;
import org.apache.lucene.search.spans.SpanQuery;
import org.apache.lucene.search.spans.SpanTermQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
//import org.apache.lucene.store.instantiated.InstantiatedIndex;
//import org.apache.lucene.store.instantiated.InstantiatedIndexReader;
import org.apache.lucene.util.BytesRef
import org.apache.lucene.util.Version;

/**
 * Static class to store index information.
 * Set the path to the lucene index here
 */

public class IndexInfoStaticG {

	private final static String pathToIndex = //"D:\\indexes\\20newsgroups";
	//   "D:\\indexes\\medical50";
	//"C:\\indexes\\reuters10";
	//"C:\\Users\\laurie\\Java\\indexes\\ind1";
	//	"C:\\Users\\laurie\\Java\\indexes\\indexReuters10";

	"C:\\Users\\laurie\\Java\\indexes\\indexReuters10NoDup";
	//"C:\\Users\\laurie\\Java\\indexes\\index20News"
	//"C:\\Users\\Laurie\\Java\\indexes\\indexOhsumed"
	//"C:\\Users\\Laurie\\Java\\indexes\\reuters10pft"
	//	"C:\\indexes\\20newsgroups";


	private static IndexSearcher indexSearcher;

	private static int categoryNumber = 2;

	// Lucene field names
	public static final String FIELD_CATEGORY = "category";
	public static final String FIELD_CONTENTS = "contents";
	public static final String FIELD_PATH = "path";
	public static final String FIELD_TEST_TRAIN = "test_train";

	public static Filter catTrainF, othersTrainF, catTestF, othersTestF, trainF;

	public static BooleanQuery catTrainBQ, othersTrainBQ, catTestBQ, othersTestBQ;

	public static int totalTrainDocsInCat, totalTestDocsInCat, totalOthersTrainDocs;

	private static final String train = "train", test = "test";

	private static final TermQuery trainQ = new TermQuery(new Term(
	IndexInfoStaticG.FIELD_TEST_TRAIN, train));

	private static final TermQuery testQ = new TermQuery(new Term(
	IndexInfoStaticG.FIELD_TEST_TRAIN, test));


	public static IndexSearcher getIndexSearcher() {

		if (indexSearcher == null) {
			setIndexSearcher();
			setFilters();			
		}
		return indexSearcher;
	}

	public static void setCatNumber(final int cn) {
		categoryNumber = cn;
		setFilters()
	}

	public static String getCatnumberAsString() {
		return String.valueOf(categoryNumber);
	}

	public static int getCatNumber() {
		return categoryNumber;
	}

	private static void setIndexSearcher() {
		try {
			IndexReader reader = DirectoryReader.open(FSDirectory.open(new File(pathToIndex)));
			println "in IndexInfoStaticG reader.numDocs: ${reader.numDocs()} "
			indexSearcher = new IndexSearcher(reader);
            setFilters()

		} catch (IOException e) {
			System.err.println("Error setting index searcher: ");
			e.printStackTrace();
		}
	}

	private static void setFilters() throws IOException {

		TermQuery catQ = new TermQuery(new Term(IndexInfoStaticG.FIELD_CATEGORY,
				//		"01_corn"));
				//	"02_crude"));
				//	"C14"));
				String.valueOf(categoryNumber)));

		catTrainBQ = new BooleanQuery(true);
		othersTrainBQ = new BooleanQuery(true);
		catTestBQ = new BooleanQuery(true);
		othersTestBQ = new BooleanQuery(true);

		catTrainBQ.add(catQ, BooleanClause.Occur.MUST);
		catTrainBQ.add(trainQ, BooleanClause.Occur.MUST);

		catTestBQ.add(catQ, BooleanClause.Occur.MUST);
		catTestBQ.add(testQ, BooleanClause.Occur.MUST);

		othersTrainBQ.add(catQ, BooleanClause.Occur.MUST_NOT);
		othersTrainBQ.add(trainQ, BooleanClause.Occur.MUST);

		othersTestBQ.add(catQ, BooleanClause.Occur.MUST_NOT);
		othersTestBQ.add(testQ, BooleanClause.Occur.MUST);

        getIndexSearcher()
		TotalHitCountCollector collector  = new TotalHitCountCollector();
		indexSearcher.search(catTrainBQ, collector);
		totalTrainDocsInCat = collector.getTotalHits();

		collector  = new TotalHitCountCollector();
		indexSearcher.search(catTestBQ, collector);
		totalTestDocsInCat = collector.getTotalHits();

		collector  = new TotalHitCountCollector();
		indexSearcher.search(othersTrainBQ, collector);
		totalOthersTrainDocs = collector.getTotalHits();

		collector  = new TotalHitCountCollector();
		indexSearcher.search(trainQ, collector);
		int totalTrain = collector.getTotalHits();

		println " total train docs: $totalTrain"

		println " total train in cat: $totalTrainDocsInCat total others tain: $totalOthersTrainDocs   total test in cat : $totalTestDocsInCat "

		catTrainF = new CachingWrapperFilter(new QueryWrapperFilter(catTrainBQ));
		othersTrainF = new CachingWrapperFilter(new QueryWrapperFilter(
				othersTrainBQ));

		catTestF = new CachingWrapperFilter(new QueryWrapperFilter(catTestBQ));
		othersTestF = new CachingWrapperFilter(new QueryWrapperFilter(
				othersTestBQ));

		trainF = new CachingWrapperFilter(new QueryWrapperFilter(trainQ));

		println "total test in cat "
	}

	// just for testing
	public static void main(String[] args) {
		println "hello in  IndexInfoStaticG main"

		int positiveMatch=0, negativeMatch=0;
		IndexSearcher searcher = IndexInfoStaticG
				.getIndexSearcher();

		println "setting filters"
		IndexInfoStaticG.setFilters();
		println "finished settinf F"

		BooleanQuery query = new BooleanQuery(true);
		SpanFirstQuery sfq = new SpanFirstQuery(new SpanTermQuery(new Term(
				IndexInfoStaticG.FIELD_CONTENTS, "barrel")), 185);
		query.add(sfq, BooleanClause.Occur.SHOULD);
		sfq = new SpanFirstQuery(new SpanTermQuery(new Term(
				IndexInfoStaticG.FIELD_CONTENTS, "barrels")), 205);
		query.add(sfq, BooleanClause.Occur.SHOULD);
		sfq = new SpanFirstQuery(new SpanTermQuery(new Term(
				IndexInfoStaticG.FIELD_CONTENTS, "crude")), 46);
		query.add(sfq, BooleanClause.Occur.SHOULD);

		try {

			TopScoreDocCollector collector0 = TopScoreDocCollector.create(300, true);
			searcher.search(query, IndexInfoStaticG.othersTestF, collector0);
			ScoreDoc[] hits = collector0.topDocs().scoreDocs;

			// 4. display results
			println "Searching for: $query Found ${hits.length} hits:"
			hits.each{
				int docId = it.doc;
				Document d = searcher.doc(docId);
				println(d.get(IndexInfoStaticG.FIELD_TEST_TRAIN) + "\t" + d.get(IndexInfoStaticG.FIELD_PATH) + "\t" +
					d.get(IndexInfoStaticG.FIELD_CATEGORY) );
				
			}

			TotalHitCountCollector collector = new TotalHitCountCollector();
			searcher.search(query, IndexInfoStaticG.catTestF,
					collector);
			positiveMatch = collector.getTotalHits(); 


			collector = new TotalHitCountCollector();
			searcher.search(query, IndexInfoStaticG.othersTestF,
					collector);
			negativeMatch = collector.getTotalHits();


		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}



