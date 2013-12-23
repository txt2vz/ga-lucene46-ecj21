package query

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import lucene.IndexInfoStaticG;

import org.apache.lucene.index.Term
import org.apache.lucene.search.BooleanQuery
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.Query
import org.apache.lucene.search.spans.SpanFirstQuery
import org.apache.lucene.search.spans.SpanNearQuery;
import org.apache.lucene.search.spans.SpanQuery;
import org.apache.lucene.search.spans.SpanTermQuery


class QueryReadable {

	public static String getQueryMinimal(Query query) {

		final String queryWithoutComma = query.toString(
				IndexInfoStaticG.FIELD_CONTENTS).replaceAll(", ", "#~");

		boolean spanF = queryWithoutComma.contains("spanFirst");
		boolean spanN = queryWithoutComma.contains("spanNear")
		// spanNear([atheism#~god]#~10#~true) spanNear([atheist#~said]#~10#~true) spanNear([atheists#~belief]#~10#~true) spanNear([belief#~atheists]#~10#~true) spanNear([benedikt#~rosenau]#~10#~true) spanNear([christian#~moral]#~10#~true) spanNear([claim#~solntze.wpd.sgi.com]#~10#~true) spanNear([islam#~islamic]#~10#~true) spanNear([jaeger#~buphy.bu.edu]#~10#~true) spanNear([keith#~schneider]#~10#~true) spanNear([livesey#~jon]#~10#~true) spanNear([mozumder#~atheism]#~10#~true) spanNear([po.cwru.edu#~keith]#~10#~true) spanNear([political#~atheists]#~10#~true) spanNear([religious#~say]#~10#~true) spanNear([rushdie#~islamic]#~10#~true) spanNear([ryan#~po.cwru.edu]#~10#~true) spanNear([say#~christian]#~10#~true) spanNear([therefore#~atheism]#~10#~true)
		if (spanN){
			def s = queryWithoutComma.replaceAll(
					"spanNear", "");
			s = s.replaceAll("#~true", "");
			s = s.replaceAll("#~false", "");
			s = s.replaceAll("\\[", "");
			s = s.replaceAll("\\]", "");
			s = s.replaceAll("#~", " ");
			println "s is $s"
			return s
		}

		if (spanF) {
 
			String s = queryWithoutComma.replaceAll(
					"spanFirst", "");
			s = s.replaceAll("\\(", "");
			s = s.replaceAll(" ", "")
			s = s.replaceAll("\\)", "#~");

			List sfList = s.tokenize("#~")

			def spanFirstMap = [:]

			for (int x = 0; x < sfList.size; x = x + 2) {

				if (sfList[x+1]==null) continue;

				def word = sfList[x]
				if (spanFirstMap.containsKey(word)) {
					spanFirstMap.put(word, Math.max(spanFirstMap.get(word),Integer.parseInt(sfList[x +1])))
				} else{
					spanFirstMap.put((word), Integer.parseInt(sfList[x +1]).intValue());
				}
			}

			spanFirstMap = spanFirstMap.sort { it.value}

			def sfshort = ''
			spanFirstMap.each { entry ->
				sfshort += "("+ entry.key + " "+ entry.value + ")"
			}

			return sfshort;
		} else
			return queryWithoutComma;
	}


	static main(args) {
		boolean sn = true;
		BooleanQuery query = new BooleanQuery(true);

		if (sn){
			SpanQuery snw0   = new SpanTermQuery(new Term(IndexInfoStaticG.FIELD_CONTENTS, "hello"));
			SpanQuery snw1   = new SpanTermQuery(new Term(IndexInfoStaticG.FIELD_CONTENTS, "hello2"));
			SpanQuery [] sqa = [snw0, snw1]			

			SpanQuery spanN = 	new SpanNearQuery(sqa, 10, true);
			query.add(spanN, BooleanClause.Occur.SHOULD);
			query.add(spanN, BooleanClause.Occur.SHOULD);

		}
		else {
			SpanFirstQuery sfq = new SpanFirstQuery(new SpanTermQuery(new Term(
			IndexInfoStaticG.FIELD_CONTENTS, "angina")), 139);
			query.add(sfq, BooleanClause.Occur.SHOULD);

			sfq = new SpanFirstQuery(new SpanTermQuery(new Term(
			IndexInfoStaticG.FIELD_CONTENTS, "aortic")), 84);
			query.add(sfq, BooleanClause.Occur.SHOULD);

			sfq = new SpanFirstQuery(new SpanTermQuery(new Term(
			IndexInfoStaticG.FIELD_CONTENTS, "aortic")), 24);
			query.add(sfq, BooleanClause.Occur.SHOULD);

			sfq = new SpanFirstQuery(new SpanTermQuery(new Term(
			IndexInfoStaticG.FIELD_CONTENTS, "angioplasty")), 281);
			query.add(sfq, BooleanClause.Occur.SHOULD);
			sfq = new SpanFirstQuery(new SpanTermQuery(new Term(
			IndexInfoStaticG.FIELD_CONTENTS, "antih,ypertensive")), 135);
			query.add(sfq, BooleanClause.Occur.SHOULD);

		}

		println " "
		println "query is $query"
		println getQueryMinimal(query)
	}
}
