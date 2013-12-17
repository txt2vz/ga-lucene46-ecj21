package classify

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Formatter;

import lucene.IndexInfoStaticG;
import query.ClassifyQuery;
import query.GAFit;
import ec.EvolutionState;
import ec.Evolve;
import ec.Fitness;
import ec.util.ParameterDatabase;
import ec.Fitness;

class GAmainG extends Evolve {

	private final boolean sf = true;

	private final String parameterFilePath =

	"C:\\Users\\Laurie\\Java\\classifyGALucene4ECJ21\\classify\\cfg\\classifyGASubpop.params";
	//"C:\\Users\\Laurie\\Java\\classifyGALucene4ECJ21\\classify\\cfg\\classifyGAindGeneAndOrNot.params";
	//	"C:\\Users\\Laurie\\Java\\classifyGALucene4ECJ21\\classify\\cfg\\classifyGAindGene.params";
	//"C:\\Users\\Laurie\\Java\\classifyGALucene4ECJ21\\classify\\cfg\\classifyGA_SpanNear10.params";

	private int totPosMatchedTest = 0, totTest = 0, totNegMatchTest = 0;

	private final static int NUMBER_OF_CATEGORIES = 10, NUMBER_OF_JOBS = 1;

	private double microF1AllRunsTotal = 0, macroF1AllRunsTotal = 0,
	microBEPAllRunsTotal = 0;

	public GAmainG(){
		EvolutionState state;

		Formatter bestResultsOut = new Formatter("results.csv");

		final String fileHead = "category, job, f1train, f1test, bepTest, totPositiveTest, totNegativeTest, totTestDocsInCat, query" + '\n';

		//System.out.println(fileHead);
		bestResultsOut.format("%s", fileHead);

		ParameterDatabase parameters = null;

		final Date startRun = new Date();

		for (job in 1..NUMBER_OF_JOBS){
			parameters = new ParameterDatabase(new File(parameterFilePath));

			double macroF1 = 0;

			for (cat in 0..(NUMBER_OF_CATEGORIES-1)){
				//	for (cat in 2..3){
				IndexInfoStaticG.setCatNumber(cat)
				state = initialize(parameters, job);

				state.output.systemMessage("Job: " + job);
				state.job = new Object[1];
				state.job[0] = new Integer(job + cat);

				if (NUMBER_OF_JOBS >= 1) {
					final String jobFilePrefix = "job." + job + "." + cat;
					state.output.setFilePrefix(jobFilePrefix);

					state.checkpointPrefix = jobFilePrefix 	+ state.checkpointPrefix;
				}
				state.run(EvolutionState.C_STARTED_FRESH);
				def bestFitInAllSubPops=null;

				/*	state.population.subpops.each { subp ->
				 Fitness bestFitInSubPop = subp.individuals[0].fitness;
				 subp.individuals.each {indi ->
				 Fitness fit = indi.fitness
				 if (fit.betterThan(bestFitInSubPop))
				 bestFitInSubPop=fit;
				 }
				 if (bestFitInAllSubPops==null)
				 bestFitInAllSubPops=bestFitInSubPop
				 else if (bestFitInAllSubPops.betterThan(bestFitInSubPop))
				 bestFitInAllSubPops=bestFitInSubPop;
				 }
				 */
				for (int subPop = 0; subPop < state.population.subpops.length; ++subPop) {
					Fitness bestFitOfSubPop = state.population.subpops[subPop].individuals[0].fitness;

					for (int i = 1; i < state.population.subpops[subPop].individuals.length; ++i) {
						Fitness fit = state.population.subpops[subPop].individuals[i].fitness;
						if (fit.betterThan(bestFitOfSubPop)) {
							bestFitOfSubPop = fit;
						}
					}

					if (bestFitInAllSubPops == null) {
						bestFitInAllSubPops = bestFitOfSubPop;
					} else if (bestFitOfSubPop.betterThan(bestFitInAllSubPops)) {
						bestFitInAllSubPops = bestFitOfSubPop;
					}
				}

				final GAFit cfit = (GAFit) bestFitInAllSubPops;
				final float testF1 = cfit.getF1Test();
				final float trainF1 = cfit.getF1Train();
				final float testBEP = cfit.getBEPTest();
				macroF1 += testF1;

				totPosMatchedTest += cfit.getPositiveMatchTest();
				totNegMatchTest += cfit.getNegativeMatchTest();
				totTest += IndexInfoStaticG.totalTestDocsInCat;

				if (sf) {
					final String queryWithoutComma = cfit.getQuery()
							.toString(IndexInfoStaticG.FIELD_CONTENTS)
							.replaceAll(",", " ");

					final String spanFirstQueryMinimal = queryWithoutComma
							.replaceAll("spanFirst", "");

				//	System.out.println("OLd query " + spanFirstQueryMinimal);
				//	System.out.println("New query " + cfit.getQueryMinimal());
				}

				bestResultsOut.format(
						"%s, %d, %.3f, %.3f, %.3f, %d, %d, %d, %s \n",
						cat, job, trainF1, testF1, testBEP,
						cfit.getPositiveMatchTest(),
						cfit.getNegativeMatchTest(),
						IndexInfoStaticG.totalTestDocsInCat,
						cfit.getQuery());
						//cfit.getQueryMinimal());

				bestResultsOut.flush();
				println "Test F1 for cat $cat : $testF1 *******************************"
				cleanup(state);
			}

			final double microF1 = ClassifyQuery.f1(totPosMatchedTest,
					totNegMatchTest, totTest);
			final double microBEP = ClassifyQuery.bep(totPosMatchedTest,
					totNegMatchTest, totTest);

			macroF1 = macroF1 / NUMBER_OF_CATEGORIES;
			println "OVERALL: micro f1:  $microF1  macroF1: $macroF1";

			bestResultsOut.format(" \n");
			bestResultsOut.format("Run Number, %d", job);
			bestResultsOut
					.format(", Micro F1: , %.4f, Micro bep: , %.4f, Macro F1: , %.4f,  Total Positive Matches , %d, Total Negative Matches, %d, Total Docs,  %d \n",
					microF1, microBEP, macroF1, totPosMatchedTest,
					totNegMatchTest, totTest);

			macroF1AllRunsTotal = macroF1AllRunsTotal + macroF1;
			microF1AllRunsTotal = microF1AllRunsTotal + microF1;
			microBEPAllRunsTotal = microBEPAllRunsTotal + microBEP;

			final double microAverageF1AllRuns = microF1AllRunsTotal / (job);
			final double microAverageBEPAllRuns = microBEPAllRunsTotal / (job);
			final double macroAverageF1AllRuns = macroF1AllRunsTotal / (job);
			bestResultsOut
					.format(",, Overall Micro F1 , %.4f, Overall Micro BEP , %.4f, Overall Macro F1, %.4f",
					microAverageF1AllRuns, microAverageBEPAllRuns,
					macroAverageF1AllRuns);

			totPosMatchedTest = 0;
			totNegMatchTest = 0;
			totTest = 0;

			bestResultsOut.format(" \n");
			bestResultsOut.format(" \n");
			bestResultsOut.flush();

			println " ---------------------------------END-----------------------------------------------"
		}

		final Date endRun = new Date();
		def time= endRun.getTime() - startRun.getTime();
		println "Total time taken: $time"
		bestResultsOut.close();
	}

	static main (args){
		new GAmainG()
	}
}
