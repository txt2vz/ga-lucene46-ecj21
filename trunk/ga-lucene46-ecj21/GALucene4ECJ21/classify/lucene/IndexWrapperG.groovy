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
 * Singleton class to store index information.
 * Set the path to the lucene index here
 */

class IndexWrapperG {

	private final static String pathToIndex = //"D:\\indexes\\20newsgroups";
	//   "D:\\indexes\\medical50";
	//"C:\\indexes\\reuters10";
	//"C:\\Users\\laurie\\Java\\indexes\\ind1";
	//	"C:\\Users\\laurie\\Java\\indexes\\indexReuters10";

	"C:\\Users\\laurie\\Java\\indexes\\indexReuters10NoDup";
	//"C:\\Users\\Laurie\\Java\\indexes\\indexOhsumed"
	//"C:\\Users\\Laurie\\Java\\indexes\\reuters10pft"
	//	"C:\\indexes\\20newsgroups";

	private static IndexWrapperG INSTANCE;

	private IndexSearcher indexSearcher;

	private IndexSearcher fsIndexSearcher;

	private int categoryNumber = 1;

	// Lucene mandatory field names
	public static final String FIELD_CATEGORY = "category";
	public static final String FIELD_CONTENTS = "contents";
	public static final String FIELD_PATH = "path";
	public static final String FIELD_TEST_TRAIN = "test_train";

	public Filter catTrainF, othersTrainF, catTestF, othersTestF, trainF;

	public BooleanQuery catTrainBQ, othersTrainBQ, catTestBQ, othersTestBQ;

	public int totalTrainDocsInCat, totalTestDocsInCat, totalOthersTrainDocs;

	private static final String train = "train", test = "test";

	private final TermQuery trainQ = new TermQuery(new Term(
	IndexWrapperG.FIELD_TEST_TRAIN, train));

	private final TermQuery testQ = new TermQuery(new Term(
	IndexWrapperG.FIELD_TEST_TRAIN, test));

	private IndexWrapperG() {
	}

	public static synchronized IndexWrapperG getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new IndexWrapperG();
		}
		return INSTANCE;
	}

	public IndexSearcher getIndexSearcher() {

		if (indexSearcher == null) {
			this.setIndexSearcher();
		}

		return indexSearcher;
	}

	public void setCatNumber(final int cn) {
		categoryNumber = cn;
	}

	public String getCatnumberAsString() {
		return String.valueOf(categoryNumber);
	}

	public int getCatNumber() {
		return categoryNumber;
	}

	private void setIndexSearcher() {
		try {
			IndexReader reader = DirectoryReader.open(FSDirectory.open(new File(pathToIndex)));
			println "in indexWrapper reader.numDocs: ${reader.numDocs()} "
			this.indexSearcher = new IndexSearcher(reader);


		} catch (IOException e) {
			System.err.println("Error setting index searcher: ");
			e.printStackTrace();
		}
	}

	public void setFilters() throws IOException {

		TermQuery catQ = new TermQuery(new Term(IndexWrapperG.FIELD_CATEGORY,
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
	}

	// just for testing
	public static void main(String[] args) {
		println "hello in  indexwrapper main"

		int positiveMatch=0, negativeMatch=0;
		IndexSearcher searcher = IndexWrapperG.getInstance()
				.getIndexSearcher();

		println "setting filters"
		IndexWrapperG.getInstance().setFilters();
		println "finished settinf F"

		BooleanQuery query = new BooleanQuery(true);


		//	SpanFirstQuery sfq = new SpanFirstQuery(new SpanTermQuery(new Term(
		//	IndexWrapperG.FIELD_CONTENTS, "barrel")), 25);
		//	query.add(sfq, BooleanClause.Occur.SHOULD);

		//	sfq = new SpanFirstQuery(new SpanTermQuery(new Term(
		//	IndexWrapperG.FIELD_CONTENTS, "bbl")), 200);
		//	query.add(sfq, BooleanClause.Occur.SHOULD);

		/*
		 SpanFirstQuery sfq = new SpanFirstQuery(new SpanTermQuery(new Term(
		 IndexWrapperG.FIELD_CONTENTS, "corn")), 219);
		 query.add(sfq, BooleanClause.Occur.SHOULD);
		 sfq = new SpanFirstQuery(new SpanTermQuery(new Term(
		 IndexWrapperG.FIELD_CONTENTS, "farmer")), 72);
		 query.add(sfq, BooleanClause.Occur.SHOULD);
		 sfq = new SpanFirstQuery(new SpanTermQuery(new Term(
		 IndexWrapperG.FIELD_CONTENTS, "maize")), 234);
		 query.add(sfq, BooleanClause.Occur.SHOULD);
		 sfq = new SpanFirstQuery(new SpanTermQuery(new Term(
		 IndexWrapperG.FIELD_CONTENTS, "pik")), 17);
		 query.add(sfq, BooleanClause.Occur.SHOULD); 
		 */

		//(barrel 185) (barrels 205) (bbl 298) (crude 46) (distillate 48) (iranian 87) (oil 35) (opec 98)
		//(refineries 111) (refinery 54)


		/*
		 SpanFirstQuery sfq = new SpanFirstQuery(new SpanTermQuery(new Term(
		 IndexWrapperG.FIELD_CONTENTS, "angina")), 139);
		 query.add(sfq, BooleanClause.Occur.SHOULD);
		 sfq = new SpanFirstQuery(new SpanTermQuery(new Term(
		 IndexWrapperG.FIELD_CONTENTS, "angioplasty")), 281);
		 query.add(sfq, BooleanClause.Occur.SHOULD);
		 sfq = new SpanFirstQuery(new SpanTermQuery(new Term(
		 IndexWrapperG.FIELD_CONTENTS, "antihypertensive")), 135);
		 query.add(sfq, BooleanClause.Occur.SHOULD);
		 sfq = new SpanFirstQuery(new SpanTermQuery(new Term(
		 IndexWrapperG.FIELD_CONTENTS, "aortic")), 84);
		 query.add(sfq, BooleanClause.Occur.SHOULD);
		 sfq = new SpanFirstQuery(new SpanTermQuery(new Term(
		 IndexWrapperG.FIELD_CONTENTS, "arteries")), 23);
		 query.add(sfq, BooleanClause.Occur.SHOULD);
		 sfq = new SpanFirstQuery(new SpanTermQuery(new Term(
		 IndexWrapperG.FIELD_CONTENTS, "cardiac")), 23);
		 query.add(sfq, BooleanClause.Occur.SHOULD);
		 sfq = new SpanFirstQuery(new SpanTermQuery(new Term(
		 IndexWrapperG.FIELD_CONTENTS, "coronary")), 266);
		 query.add(sfq, BooleanClause.Occur.SHOULD);
		 sfq = new SpanFirstQuery(new SpanTermQuery(new Term(
		 IndexWrapperG.FIELD_CONTENTS, "diastolic")), 95);
		 query.add(sfq, BooleanClause.Occur.SHOULD);
		 sfq = new SpanFirstQuery(new SpanTermQuery(new Term(
		 IndexWrapperG.FIELD_CONTENTS, "doppler")), 68);
		 query.add(sfq, BooleanClause.Occur.SHOULD);
		 sfq = new SpanFirstQuery(new SpanTermQuery(new Term(
		 IndexWrapperG.FIELD_CONTENTS, "echocardiography")), 291);
		 query.add(sfq, BooleanClause.Occur.SHOULD);
		 sfq = new SpanFirstQuery(new SpanTermQuery(new Term(
		 IndexWrapperG.FIELD_CONTENTS, "heart")), 19);
		 query.add(sfq, BooleanClause.Occur.SHOULD);
		 sfq = new SpanFirstQuery(new SpanTermQuery(new Term(
		 IndexWrapperG.FIELD_CONTENTS, "hypertension")), 32);
		 query.add(sfq, BooleanClause.Occur.SHOULD);
		 sfq = new SpanFirstQuery(new SpanTermQuery(new Term(
		 IndexWrapperG.FIELD_CONTENTS, "hypertensive")), 109);
		 query.add(sfq, BooleanClause.Occur.SHOULD);
		 sfq = new SpanFirstQuery(new SpanTermQuery(new Term(
		 IndexWrapperG.FIELD_CONTENTS, "ischemia")), 21);
		 query.add(sfq, BooleanClause.Occur.SHOULD);
		 sfq = new SpanFirstQuery(new SpanTermQuery(new Term(
		 IndexWrapperG.FIELD_CONTENTS, "myocardial")), 70);
		 query.add(sfq, BooleanClause.Occur.SHOULD);
		 sfq = new SpanFirstQuery(new SpanTermQuery(new Term(
		 IndexWrapperG.FIELD_CONTENTS, "valve")), 280);
		 query.add(sfq, BooleanClause.Occur.SHOULD);
		 */

		//(angina 139) (angioplasty 281) (antihypertensive 135) (aortic 84) (arteries 23)
		// (cardiac 23) (coronary 266) (diastolic 95) (doppler 68) (echocardiography 291)
		//(heart 19) (hypertension 32) (hypertensive 109)
		//(ischemia 21) (myocardial 70) (valve 280)


		SpanFirstQuery sfq = new SpanFirstQuery(new SpanTermQuery(new Term(
				IndexWrapperG.FIELD_CONTENTS, "barrel")), 185);
		query.add(sfq, BooleanClause.Occur.SHOULD);
		sfq = new SpanFirstQuery(new SpanTermQuery(new Term(
				IndexWrapperG.FIELD_CONTENTS, "barrels")), 205);
		query.add(sfq, BooleanClause.Occur.SHOULD);
		sfq = new SpanFirstQuery(new SpanTermQuery(new Term(
				IndexWrapperG.FIELD_CONTENTS, "crude")), 46);
		query.add(sfq, BooleanClause.Occur.SHOULD);
		sfq = new SpanFirstQuery(new SpanTermQuery(new Term(
				IndexWrapperG.FIELD_CONTENTS, "bbl")), 298);
		query.add(sfq, BooleanClause.Occur.SHOULD);
		sfq = new SpanFirstQuery(new SpanTermQuery(new Term(
				IndexWrapperG.FIELD_CONTENTS, "crude")), 46);
		query.add(sfq, BooleanClause.Occur.SHOULD);
		sfq = new SpanFirstQuery(new SpanTermQuery(new Term(
				IndexWrapperG.FIELD_CONTENTS, "distillate")), 48);
		query.add(sfq, BooleanClause.Occur.SHOULD);
		sfq = new SpanFirstQuery(new SpanTermQuery(new Term(
				IndexWrapperG.FIELD_CONTENTS, "iranian")), 87);
		query.add(sfq, BooleanClause.Occur.SHOULD);
		sfq = new SpanFirstQuery(new SpanTermQuery(new Term(
				IndexWrapperG.FIELD_CONTENTS, "oil")), 35);
		query.add(sfq, BooleanClause.Occur.SHOULD);
		sfq = new SpanFirstQuery(new SpanTermQuery(new Term(
				IndexWrapperG.FIELD_CONTENTS, "opec")), 98);
		query.add(sfq, BooleanClause.Occur.SHOULD);
		sfq = new SpanFirstQuery(new SpanTermQuery(new Term(
				IndexWrapperG.FIELD_CONTENTS, "refineries")), 111);
		query.add(sfq, BooleanClause.Occur.SHOULD);
		sfq = new SpanFirstQuery(new SpanTermQuery(new Term(
				IndexWrapperG.FIELD_CONTENTS, "refinery")), 54);
		// */

		//query.add(sfq, BooleanClause.Occur.SHOULD);

		//searcher.search(query, IndexWrapperG.getInstance().catTrainF,
		//	collector);

		try {

			TopScoreDocCollector collector0 = TopScoreDocCollector.create(300, true);
			searcher.search(query, IndexWrapperG.getInstance().othersTestF, collector0);
			ScoreDoc[] hits = collector0.topDocs().scoreDocs;

			// 4. display results
			println "Searching for: $query Found ${hits.length} hits:"
			hits.each{
				int docId = it.doc;
				Document d = searcher.doc(docId);
				println(d.get(IndexWrapperG.FIELD_TEST_TRAIN) + "\t" + d.get("path") + "\t" + d.get(IndexWrapperG.FIELD_CATEGORY) + " cat fields "
						+ d.getValues(IndexWrapperG.FIELD_CATEGORY) );
			}

			TotalHitCountCollector collector = new TotalHitCountCollector();
			searcher.search(query, IndexWrapperG.getInstance().catTestF,
					collector);
			positiveMatch = collector.getTotalHits();


			collector = new TotalHitCountCollector();
			searcher.search(query, IndexWrapperG.getInstance().othersTestF,
					collector);
			negativeMatch = collector.getTotalHits();


		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//	double F1train = ClassifyQuery.f1(positiveMatch, negativeMatch,
		//		IndexWrapperG.getInstance().totalTrainDocsInCat);
		//
		//	double F1test = ClassifyQuery.f1(positiveMatch, negativeMatch,
		//		IndexWrapperG.getInstance().totalTestDocsInCat);


		//	System.out.println("pos match " + positiveMatch + " neg match " + negativeMatch + " totalInCat " +
		//		 IndexWrapperG.instance.totalTestDocsInCat  + " F1  " +F1test + " query " + query);


		//(corn 219) (farmer 72) (maize 234) (pik 17)

		//	System.out.println("S " + s.toString());
		//System.out.println(" md " + s.getIndexReader().maxDoc());

		/*	Term oilt = new Term(IndexWrapperG.FIELD_CONTENTS, "oil");
		 Term cornt = new Term(IndexWrapperG.FIELD_CONTENTS, "corn");
		 Query query = new TermQuery(oilt);
		 Query query2 = new TermQuery(cornt);
		 final SpanTermQuery stq1 = new SpanTermQuery(oilt);
		 final SpanTermQuery stq0 = new SpanTermQuery(cornt);
		 //	final SpanQuery[] sqPos = new SpanQuery[] { stq0, stq1 };
		 //final SpanNearQuery snq = new SpanNearQuery(sqPos, 15, true);
		 BooleanQuery bq = new BooleanQuery(true);
		 bq.add(query, BooleanClause.Occur.SHOULD);
		 // bq.add(query2, BooleanClause.Occur.SHOULD);
		 try {
		 TopDocsCollector collector = TopScoreDocCollector.create(
		 100, false);
		 s.search(query, collector);
		 ScoreDoc[] hits = collector.topDocs().scoreDocs;
		 System.out.println(" Length oil: " + collector.getTotalHits());
		 int m = Math.min(hits.length, 4);
		 for (int i = 0; i < m; i++) {
		 int docId = hits[i].doc;
		 System.out.println("coollect   "
		 + s.doc(docId).get(IndexWrapperG.FIELD_PATH) + " "
		 + hits[i].score);
		 }
		 collector = TopScoreDocCollector.create(
		 100, false);;
		 bq = new BooleanQuery(true);
		 //bq.add(snq, BooleanClause.Occur.SHOULD);
		 s.search(bq, collector);
		 ScoreDoc[] hits2 = collector.topDocs().scoreDocs;
		 System.out.println(" Length snq oil: " + collector.getTotalHits());
		 m = Math.min(hits2.length, 4);
		 for (int i = 0; i < m; i++) {
		 int docId = hits2[i].doc;
		 System.out.println("coollec snq   "
		 + s.doc(docId).get(IndexWrapperG.FIELD_PATH) + " "
		 + hits2[i].score);
		 }
		 } catch (IOException e) {
		 e.printStackTrace();
		 }
		 }*/
	}}



