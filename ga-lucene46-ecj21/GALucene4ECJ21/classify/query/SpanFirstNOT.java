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
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TotalHitCountCollector;
import org.apache.lucene.search.spans.SpanFirstQuery;
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

public class SpanFirstNOT extends Problem implements SimpleProblemForm {

	private IndexSearcher searcher = IndexInfoStaticG.getIndexSearcher();

	private float F1train = 0;

	private String[] wordArrayPos, wordArrayNeg;

	private BooleanQuery query;

	public void setup(final EvolutionState state, final Parameter base) {

		super.setup(state, base);

		try {

			ImportantWords iw = new ImportantWords();

			System.out.println("Total docs for cat  "
					+ IndexInfoStaticG.getCatnumberAsString() + " "
					+ IndexInfoStaticG.totalTrainDocsInCat
					+ " Total test docs for cat "
					+ IndexInfoStaticG.totalTestDocsInCat);

			wordArrayPos = iw.getF1WordList(false, true);
			wordArrayNeg = iw.getF1WordList(false, false);

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

		query = new BooleanQuery(true);
		// query.setMinimumNumberShouldMatch(2);

		// read through vector 2 ints at at time. 1st int retrieves word, second
		// specifies end for Lucene spanFirstQuery
		for (int i = 0; i < intVectorIndividual.genome.length; i = i + 2) {

			if (intVectorIndividual.genome[i] < 0
					|| intVectorIndividual.genome[i + 1] < 0
					|| intVectorIndividual.genome[i] >= wordArrayNeg.length
					|| intVectorIndividual.genome[i] >= wordArrayPos.length
					|| intVectorIndividual.genome[i + 1] >= ImportantWords.SPAN_FIRST_MAX_END)
				continue;

			int wordInd = intVectorIndividual.genome[i];
	
			if (i == 0) {
				
				SpanFirstQuery sfq = new SpanFirstQuery(new SpanTermQuery(
						new Term(IndexInfoStaticG.FIELD_CONTENTS, wordArrayNeg[wordInd])),
						intVectorIndividual.genome[i + 1]);
				query.add(sfq, BooleanClause.Occur.MUST_NOT);
	
			} else {

				SpanFirstQuery sfq = new SpanFirstQuery(new SpanTermQuery(
						new Term(IndexInfoStaticG.FIELD_CONTENTS, wordArrayPos[wordInd])),
						intVectorIndividual.genome[i + 1]);
				query.add(sfq, BooleanClause.Occur.SHOULD);
			}
		}

		fitness.setNumberOfTerms(intVectorIndividual.genome.length / 2);

		try {
			TotalHitCountCollector collector = new TotalHitCountCollector();
			searcher.search(query, IndexInfoStaticG.catTrainF, collector);
			final int positiveMatch = collector.getTotalHits();

			collector = new TotalHitCountCollector();
			searcher.search(query, IndexInfoStaticG.othersTrainF, collector);
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
