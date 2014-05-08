package lucene

import java.io.File;
import java.io.IOException;
import org.apache.lucene.analysis.Analyzer
import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.document.Document
import org.apache.lucene.document.Field
import org.apache.lucene.document.StringField
import org.apache.lucene.document.TextField
import org.apache.lucene.index.IndexReader
import org.apache.lucene.index.IndexWriter
import org.apache.lucene.index.IndexWriterConfig
import org.apache.lucene.index.IndexWriterConfig.OpenMode
import org.apache.lucene.queryparser.classic.QueryParser
import org.apache.lucene.search.IndexSearcher
import org.apache.lucene.search.Query
import org.apache.lucene.search.ScoreDoc
import org.apache.lucene.search.TopScoreDocCollector
import org.apache.lucene.store.Directory
import org.apache.lucene.store.FSDirectory
import org.apache.lucene.util.Version

import org.apache.lucene.index.IndexWriter;

class IndexDocsReutersNoDups {
	def indexPath =  "C:\\Users\\laurie\\Java\\indexes\\indexReuters10NoDupDelete" // Create Lucene index in this directory
	//def docsPath =  "C:\\Users\\laurie\\Dataset\\reuters-top10\\08_trade" // Index files in this directory
	def docsPath =  "C:\\Users\\laurie\\Dataset\\reuters-top10" // Index files in this directory
	def docsCatMap=[:]

	static main(args) {
		def p = new IndexDocsReutersNoDups()
		p.setup()
	}

	def setup() {

		Date start = new Date();
		println("Indexing to directory '" + indexPath + "'...");

		Directory dir = FSDirectory.open(new File(indexPath));
		Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_46);
		IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_46, analyzer);

		//Codec c  = new DirectPostingsFormat();
		// Create a new index in the directory, removing any
		// previously indexed documents:
		iwc.setOpenMode(OpenMode.CREATE);
		//iwc.setCodec(c);

		// Optional: for better indexing performance, if you
		// are indexing many documents, increase the RAM
		// buffer.  But if you do this, increase the max heap
		// size to the JVM (eg add -Xmx512m or -Xmx1g):
		//
		iwc.setRAMBufferSizeMB(512.0);

		IndexWriter writer = new IndexWriter(dir, iwc);

		//create map of files with list of categories

		def catNumber=0;
		new File(docsPath).eachDir {
			it.eachFileRecurse {
				if (!it.hidden && it.exists() && it.canRead() && !it.directory && it.name.endsWith('.txt'))  {
					docsCatMap[(it.name)] = docsCatMap.get(it.name, []) << catNumber.toString()
				}
			}
			catNumber++;
		}

		catNumber=0;
		def Set<String> set=new HashSet();
		new File(docsPath).eachDir {

			it.eachFileRecurse {
				if (!it.hidden && it.exists() && it.canRead() && !it.directory && it.name.endsWith('.txt'))  {
					if (set.add(it.name))
						indexDocs(writer,it, catNumber)
				}
			}
			catNumber++;
		}

		Date end = new Date();
		println(end.getTime() - start.getTime() + " total milliseconds");
		println "***************************************************************"

		String querystr =  "corn";
		//String querystr2 =  "08_trade";
		// the "title" arg specifies the default field to use
		// when no field is explicitly specified in the query.
		Query q = new QueryParser(Version.LUCENE_45, IndexInfoStaticG.FIELD_CONTENTS, analyzer).parse(querystr);
		//Query q = new QueryParser(Version.LUCENE_46, IndexInfoStaticG.FIELD_CATEGORY, analyzer).parse(querystr);

		// 3. search
		int hitsPerPage = 5;
		IndexReader reader =  writer.getReader();//  DirectoryReader.open(writer);

		println " reader max doc " + reader.maxDoc()
		IndexSearcher searcher = new IndexSearcher(reader);
		TopScoreDocCollector collector = TopScoreDocCollector.create(hitsPerPage, true);
		searcher.search(q, collector);
		ScoreDoc[] hits = collector.topDocs().scoreDocs;

		// 4. display results
		println "Searching for: $querystr Found ${hits.length} hits:"
		hits.each{
			int docId = it.doc;
			Document d = searcher.doc(docId);
			println(d.get(IndexInfoStaticG.FIELD_TEST_TRAIN) + "\t" + d.get("path") + "\t" +
					d.get(IndexInfoStaticG.FIELD_CATEGORY) );
		}

		println "YYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYY"

		reader.close();
		writer.close();
	}
	/**
	 * Indexes the given file using the given writer, or if a directory is given,
	 * recurses over files and directories found under the given directory.
	 */
	def indexDocs(IndexWriter writer, File f, categoryNumber)
	throws IOException {

		Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_46);

		//	println "Indexing ${f.canonicalPath} categorynumber: $categoryNumber"
		//	println " parent ${f.getParent()}"
		//	println " parent parent " + f.getParentFile().getParentFile().name;

		def doc = new Document()
		FileInputStream fis=new FileInputStream(f);

		// Construct a Field that is tokenized and indexed, but is not stored in the index verbatim.
		//	doc.add(Field.Text("contents", fis))

		// Add the path of the file as a field named "path".  Use a
		// field that is indexed (i.e. searchable), but don't tokenize
		// the field into separate words and don't index term frequency
		// or positional information:
		Field pathField = new StringField(IndexInfoStaticG.FIELD_PATH, f.getPath(), Field.Store.YES);
		doc.add(pathField);

		doc.add(new TextField(IndexInfoStaticG.FIELD_CONTENTS, new BufferedReader(new InputStreamReader(fis, "UTF-8"))) );

		def categoryList = docsCatMap.get(f.name)

		if (categoryList.size()>1)
			println "l is  $categoryList ********************************************************************************************************************"

		categoryList.each {
			Field categoryField = new StringField(IndexInfoStaticG.FIELD_CATEGORY, it, Field.Store.YES);
			doc.add(categoryField)
		}

		String test_train
		if ( f.canonicalPath.contains("test")) test_train="test" else test_train="train";
		Field ttField = new StringField(IndexInfoStaticG.FIELD_TEST_TRAIN, test_train, Field.Store.YES)
		doc.add(ttField)

		writer.addDocument(doc);
	}

}
