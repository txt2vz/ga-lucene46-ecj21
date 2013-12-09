package query;

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

import lucene.ImportantWords;
import lucene.IndexInfoStaticG;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TotalHitCountCollector;
import org.apache.lucene.search.spans.SpanFirstQuery;
import org.apache.lucene.search.spans.SpanNearQuery;
import org.apache.lucene.search.spans.SpanQuery;
import org.apache.lucene.search.spans.SpanTermQuery;

import ec.EvolutionState;
import ec.Individual;
import ec.Problem;
import ec.simple.SimpleFitness;
import ec.simple.SimpleProblemForm;
import ec.util.Parameter;
import ec.vector.IntegerVectorIndividual;

/**
 * To generate queries to perform binary text classification using GA string of
 * integer pairs which are translated into spanFirst queries
 * 
 * @author Laurie
 */

public class ClassifySpanNear10 extends Problem implements SimpleProblemForm {

	private IndexSearcher searcher = IndexInfoStaticG.getIndexSearcher();

	private float F1train = 0;

	private String[] wordArray;

	private BooleanQuery query;
	
	private final static int WORD_DISTANCE=10;

	public void setup(final EvolutionState state, final Parameter base) {

		super.setup(state, base);

		try {	

			ImportantWords importantWords = new ImportantWords();

			System.out.println("Total docs for cat  "
					+ IndexInfoStaticG.getCatnumberAsString() + " "
					+ IndexInfoStaticG.totalTrainDocsInCat
					+ " Total test docs for cat "
					+ IndexInfoStaticG.totalTestDocsInCat);

			wordArray = importantWords.getF1WordList(true);

			System.out.println();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void evaluate(final EvolutionState state, final Individual ind,
			final int subpopulation, final int threadnum) {

		if (ind.evaluated)
			return;

		GAFit fitness = (GAFit) ind.fitness;

		IntegerVectorIndividual intVectorIndividual = (IntegerVectorIndividual) ind;

		// use sorted map to remove redundant elements and improve readability
		Map<String, String> spanNearMap = new TreeMap<String, String>();

		// create query from Map
		query = new BooleanQuery(true);

		// read through vector 2 ints at at time. 1st int retrieves word, second
		// specifies end for Lucene spanFirstQuery
		// store results in Map after removing redundant queries (i.e. same word
		// but lower end value)

		for (int i = 0; i < (intVectorIndividual.genome.length - 1); i = i + 2) {

			int wordInd = 0;

			if (intVectorIndividual.genome[i] < 0
					|| intVectorIndividual.genome[i + 1] < 0
					|| intVectorIndividual.genome[i] > wordArray.length
					|| intVectorIndividual.genome[i + 1] > wordArray.length
					|| intVectorIndividual.genome[i] == intVectorIndividual.genome[i + 1]
					)
				continue;
		
			final String word0 = wordArray[intVectorIndividual.genome[i]];
			final String word1 = wordArray[intVectorIndividual.genome[i+1]];
			spanNearMap.put(word0, word1);
			
		}

		for (String word : spanNearMap.keySet()) {
			
			SpanQuery snw0   = new SpanTermQuery(new Term(IndexInfoStaticG.FIELD_CONTENTS, word));
			SpanQuery snw1   = new SpanTermQuery(new Term(IndexInfoStaticG.FIELD_CONTENTS, spanNearMap.get(word)));			
			
			SpanQuery spanN =
					   new SpanNearQuery(new SpanQuery[] {snw0,snw1}, WORD_DISTANCE, true);
			
			query.add(spanN, BooleanClause.Occur.SHOULD);
		}

		fitness.setNumberOfTerms(spanNearMap.size());

		try {
			TotalHitCountCollector collector = new TotalHitCountCollector();
			searcher.search(query, IndexInfoStaticG.catTrainF,
					collector);
			final int positiveMatch = collector.getTotalHits();
			;
			collector = new TotalHitCountCollector();
			searcher.search(query, IndexInfoStaticG.othersTrainF,
					collector);
			final int negativeMatch = collector.getTotalHits();

			F1train = ClassifyQuery.f1(positiveMatch, negativeMatch,
					IndexInfoStaticG.totalTrainDocsInCat);

			fitness.setTrainValues(positiveMatch, negativeMatch);
			fitness.setF1Train(F1train);
			fitness.setQuery(query);

		} catch (IOException e) {

			e.printStackTrace();
		}

		float rawfitness = F1train;

		((SimpleFitness) intVectorIndividual.fitness).setFitness(state,
				rawfitness, false);

		ind.evaluated = true;
	}
}
