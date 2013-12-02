package query;

import java.io.IOException;


import org.apache.lucene.search.IndexSearcher;
//import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.TopScoreDocCollector; //import org.apache.lucene.search.TopDocCollector;
import org.apache.lucene.search.TotalHitCountCollector;

import lucene.IndexWrapperG;
import ec.EvolutionState;
import ec.Fitness;
import ec.Statistics;
import ec.simple.SimpleStatistics;

public class ClassifyGAStatistics extends SimpleStatistics {

	public void finalStatistics(final EvolutionState state, final int result) {
		// print out the other statistics
		super.finalStatistics(state, result);
	}

	public void postEvaluationStatistics(EvolutionState state) {
		super.postEvaluationStatistics(state);
		Fitness bestFitOfSubp = null, bestFitOfPop = null;
		for (int subPop = 0; subPop < state.population.subpops.length; ++subPop) {
			bestFitOfSubp = state.population.subpops[subPop].individuals[0].fitness;
			for (int i = 1; i < state.population.subpops[subPop].individuals.length; ++i) {
				Fitness fit = state.population.subpops[subPop].individuals[i].fitness;
				if (fit.betterThan(bestFitOfSubp))
					bestFitOfSubp = fit;
			}
			if (bestFitOfPop == null)
				bestFitOfPop = bestFitOfSubp;
			else if (bestFitOfSubp.betterThan(bestFitOfPop))
				bestFitOfPop = bestFitOfSubp;
		}

		//final GAFit cf = (GAFit) bestFitOfPop;
		 final GASFQ cf = (GASFQ) bestFitOfPop;

		// get test results on best individual
		try {

			IndexSearcher searcher = IndexWrapperG.getInstance().getIndexSearcher();
		
			TotalHitCountCollector collector  = new TotalHitCountCollector();

//			TopScoreDocCollector collector = TopScoreDocCollector.create(0,
//					false);
			searcher.search(cf.getQuery(), IndexWrapperG.getInstance().catTestF,
					collector);
			final int positiveMatchTest = collector.getTotalHits();

		//	collector = TopScoreDocCollector.create(0, false);
			collector  = new TotalHitCountCollector();
			searcher.search(cf.getQuery(),
					IndexWrapperG.getInstance().othersTestF, collector);
			final int negativeMatchTest = collector.getTotalHits();

			cf.setTestValues(positiveMatchTest, negativeMatchTest);

			cf.setF1Test(ClassifyQuery.f1(positiveMatchTest, negativeMatchTest,
					IndexWrapperG.getInstance().totalTestDocsInCat));
			
			cf.setBEPTest(ClassifyQuery.bep(positiveMatchTest, negativeMatchTest,
					IndexWrapperG.getInstance().totalTestDocsInCat));

			System.out.println("F1Test: " + cf.getF1Test() + " F1Train: "
					+ " bepTest " + cf.getBEPTest()
					+ cf.getF1Train() + " positive match test: "
					+ positiveMatchTest + " negative match test: "
					+ negativeMatchTest + " Total test: "
					+ IndexWrapperG.getInstance().totalTestDocsInCat
					+ " Total terms in query: " + cf.getNumberOfTerms()
					+ " neutralHit " + cf.getNeutralHit() + '\n' + " Query "
					+ cf.getQuery().toString(IndexWrapperG.FIELD_CONTENTS));

		} catch (IOException e) {

			e.printStackTrace();
		}
	}
}
