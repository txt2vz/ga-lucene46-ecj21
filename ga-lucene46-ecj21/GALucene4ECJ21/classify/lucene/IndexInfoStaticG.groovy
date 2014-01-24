package lucene

import java.io.File;
import java.io.IOException;
import query.*;

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
import org.apache.lucene.util.BytesRef
import org.apache.lucene.util.Version;

/**
 * Static class to store index information.
 * Set the path to the lucene index here
 */

public class IndexInfoStaticG {

	private final static String pathToIndex =
//	"C:\\Users\\laurie\\Java\\indexes\\index20News10B"
	//"C:\\Users\\laurie\\Java\\indexes\\indexReuters10NoDup";
	//"C:\\Users\\laurie\\Java\\indexes\\indexOhsumed"
	"C:\\Users\\laurie\\Java\\indexes\\index20News"

	static IndexSearcher indexSearcher;

	//private static int categoryNumber = 13;
	private static String categoryNumber="3";

	// Lucene field names
	public static final String FIELD_CATEGORY = "category";
	public static final String FIELD_CONTENTS = "contents";
	public static final String FIELD_PATH = "path";
	public static final String FIELD_TEST_TRAIN = "test_train";

	public static Filter catTrainF, othersTrainF, catTestF, othersTestF, trainF;

	public static BooleanQuery catTrainBQ, othersTrainBQ, catTestBQ, othersTestBQ;

	public static int totalTrainDocsInCat, totalTestDocsInCat, totalOthersTrainDocs;

	private static final TermQuery trainQ = new TermQuery(new Term(
	IndexInfoStaticG.FIELD_TEST_TRAIN, "train"));

	private static final TermQuery testQ = new TermQuery(new Term(
	IndexInfoStaticG.FIELD_TEST_TRAIN, "test"));

	static {
		final IndexReader reader = DirectoryReader.open(FSDirectory.open(new File(pathToIndex)));
		println "in IndexInfoStaticG reader.numDocs: ${reader.numDocs()} "
		indexSearcher = new IndexSearcher(reader);
		setFilters()
	}

	public static void setCatNumber(final int cn) {
		categoryNumber = cn;
		setFilters()
	}

	public static String getCatnumberAsString() {
		//return categoryNumber;
		return String.valueOf(categoryNumber);
	}

	private static void setFilters() throws IOException {

		final TermQuery catQ = new TermQuery(new Term(IndexInfoStaticG.FIELD_CATEGORY,
		//	"02_crude"));
		//	"C14"));
		//String.valueOf(categoryNumber)
			categoryNumber
		
		));

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

		catTrainF = new CachingWrapperFilter(new QueryWrapperFilter(catTrainBQ));
		othersTrainF = new CachingWrapperFilter(new QueryWrapperFilter(
		othersTrainBQ));

		catTestF = new CachingWrapperFilter(new QueryWrapperFilter(catTestBQ));
		othersTestF = new CachingWrapperFilter(new QueryWrapperFilter(
		othersTestBQ));

		trainF = new CachingWrapperFilter(new QueryWrapperFilter(trainQ));

		println "Total train docs: $totalTrain"
		println "CategoryNumber $categoryNumber Total train in cat: $totalTrainDocsInCat  Total others tain: $totalOthersTrainDocs   Total test in cat : $totalTestDocsInCat  "
	}

	// just for testing
	public static void main(String[] args) {

		int positiveMatch=0, negativeMatch=0;
		IndexSearcher searcher = IndexInfoStaticG
		.getIndexSearcher();

		println "setting filters"
		IndexInfoStaticG.setFilters();

		BooleanQuery query = new BooleanQuery(true);
		query.setMinimumNumberShouldMatch(5);
		SpanFirstQuery sfq = new SpanFirstQuery(new SpanTermQuery(new Term(
		IndexInfoStaticG.FIELD_CONTENTS, "angina")), 139);
		query.add(sfq, BooleanClause.Occur.SHOULD);
		sfq = new SpanFirstQuery(new SpanTermQuery(new Term(
		IndexInfoStaticG.FIELD_CONTENTS, "angioplasty")), 281);
		query.add(sfq, BooleanClause.Occur.SHOULD);
		sfq = new SpanFirstQuery(new SpanTermQuery(new Term(
		IndexInfoStaticG.FIELD_CONTENTS, "antihypertensive")), 135);
		query.add(sfq, BooleanClause.Occur.SHOULD);
		sfq = new SpanFirstQuery(new SpanTermQuery(new Term(
		IndexInfoStaticG.FIELD_CONTENTS, "aortic")), 84);
		query.add(sfq, BooleanClause.Occur.SHOULD);
		sfq = new SpanFirstQuery(new SpanTermQuery(new Term(
		IndexInfoStaticG.FIELD_CONTENTS, "arteries")), 23);
		query.add(sfq, BooleanClause.Occur.SHOULD);
		sfq = new SpanFirstQuery(new SpanTermQuery(new Term(
		IndexInfoStaticG.FIELD_CONTENTS, "cardiac")), 23);
		query.add(sfq, BooleanClause.Occur.SHOULD);
		sfq = new SpanFirstQuery(new SpanTermQuery(new Term(
		IndexInfoStaticG.FIELD_CONTENTS, "coronary")), 266);
		query.add(sfq, BooleanClause.Occur.SHOULD);
		sfq = new SpanFirstQuery(new SpanTermQuery(new Term(
		IndexInfoStaticG.FIELD_CONTENTS, "diastolic")), 95);
		query.add(sfq, BooleanClause.Occur.SHOULD);
		sfq = new SpanFirstQuery(new SpanTermQuery(new Term(
		IndexInfoStaticG.FIELD_CONTENTS, "doppler")), 68);
		query.add(sfq, BooleanClause.Occur.SHOULD);
		sfq = new SpanFirstQuery(new SpanTermQuery(new Term(
		IndexInfoStaticG.FIELD_CONTENTS, "echocardiography")), 291);
		query.add(sfq, BooleanClause.Occur.SHOULD);
		sfq = new SpanFirstQuery(new SpanTermQuery(new Term(
		IndexInfoStaticG.FIELD_CONTENTS, "heart")), 19);
		query.add(sfq, BooleanClause.Occur.SHOULD);
		sfq = new SpanFirstQuery(new SpanTermQuery(new Term(
		IndexInfoStaticG.FIELD_CONTENTS, "hypertension")), 32);
		query.add(sfq, BooleanClause.Occur.SHOULD);
		sfq = new SpanFirstQuery(new SpanTermQuery(new Term(
		IndexInfoStaticG.FIELD_CONTENTS, "hypertensive")), 109);
		query.add(sfq, BooleanClause.Occur.SHOULD);
		sfq = new SpanFirstQuery(new SpanTermQuery(new Term(
		IndexInfoStaticG.FIELD_CONTENTS, "ischemia")), 21);
		query.add(sfq, BooleanClause.Occur.SHOULD);
		sfq = new SpanFirstQuery(new SpanTermQuery(new Term(
		IndexInfoStaticG.FIELD_CONTENTS, "myocardial")), 70);
		query.add(sfq, BooleanClause.Occur.SHOULD);
		sfq = new SpanFirstQuery(new SpanTermQuery(new Term(
		IndexInfoStaticG.FIELD_CONTENTS, "valve")), 280);
		query.add(sfq, BooleanClause.Occur.SHOULD);


		TopScoreDocCollector collector0 = TopScoreDocCollector.create(200, true);
		searcher.search(query, IndexInfoStaticG.othersTestF, collector0);
		ScoreDoc[] hits = collector0.topDocs().scoreDocs;

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

		println "Positive match test: $positiveMatch  Negative match test: $negativeMatch total in cat $totalTestDocsInCat"
		
		def f1 = ClassifyQuery.f1(positiveMatch, negativeMatch, IndexInfoStaticG.totalTestDocsInCat)
		println "F1 $f1"
	}
}



