package classify;

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

/**
 * to run the GA classify system. Need to set the number of categories for each
 * dataset and filepath to ECJ parmater file
 * 
 * @author Laurie
 */

public class GAClassifyMain extends Evolve {

	private String category;
	private final boolean sf = true;

	private final String parameterFilePath =
			"C:\\Users\\Laurie\\Java\\classifyGALucene4ECJ21\\classify\\cfg\\classifyGAindGene.params";
	//"C:\\Users\\Laurie\\Java\\classifyGALucene4ECJ21\\classify\\cfg\\classifyGASubpop.params";
	//"C:\\Users\\Laurie\\Java\\classifyGALucene4ECJ21\\classify\\cfg\\classifyGAindGeneAndOrNot.params";
	//"C:\\Users\\Laurie\\Java\\classifyGALucene4ECJ21\\classify\\cfg\\classifyGAindGene.params";
	//"C:\\Users\\Laurie\\Java\\classifyGALucene4ECJ21\\classify\\cfg\\classifyGA_SpanNear10.params";


	private Fitness bestOfAll = null;

	private int totPosMatchedTest = 0, totTest = 0, totNegMatchTest = 0;

	private final static int NUMBER_OF_CATEGORIES = 10, NUMBER_OF_JOBS = 1,
			START_CAT = 0;;

	private double microF1AllRunsTotal = 0, macroF1AllRunsTotal = 0,
			microBEPAllRunsTotal = 0;

	public GAClassifyMain() throws IOException {

		EvolutionState state;

		Formatter bestResultsOut = new Formatter("results.csv");
		Formatter timingInfoOut = new Formatter("timingInfo.txt");

		final String fileHead = "cat, job, f1train, f1test, bepTest, totPositiveTest, totNegativeTest, totTestDocsInCat, neutralHit, query" + '\n';

		System.out.println(fileHead);
		bestResultsOut.format("%s", fileHead);

		ParameterDatabase parameters = null;

		final Date startRun = new Date();

		for (int job = 0; job < NUMBER_OF_JOBS; job++) {

			parameters = new ParameterDatabase(new File(parameterFilePath));

			double macroF1 = 0;

			final int maxCatNumber = START_CAT + NUMBER_OF_CATEGORIES;

			final Date startJob = new Date();

			for (int cat = START_CAT; cat < maxCatNumber; cat++) {

				Date start = new Date();
				category = String.valueOf(cat);
				IndexInfoStaticG.setCatNumber(cat);
				bestOfAll = null;

				state = initialize(parameters, job);

				state.output.systemMessage("Job: " + job);
				state.job = new Object[1];
				state.job[0] = new Integer(job + cat);

				if (NUMBER_OF_JOBS >= 1) {
					final String jobFilePrefix = "job." + job + "." + cat;
					state.output.setFilePrefix(jobFilePrefix);

					state.checkpointPrefix = jobFilePrefix
							+ state.checkpointPrefix;
				}

				state.run(EvolutionState.C_STARTED_FRESH);

				for (int subPop = 0; subPop < state.population.subpops.length; ++subPop) {
					Fitness bestFitOfSubPop = state.population.subpops[subPop].individuals[0].fitness;

					for (int i = 1; i < state.population.subpops[subPop].individuals.length; ++i) {
						Fitness fit = state.population.subpops[subPop].individuals[i].fitness;
						if (fit.betterThan(bestFitOfSubPop)) {
							bestFitOfSubPop = fit;
						}
					}

					if (bestOfAll == null) {
						bestOfAll = bestFitOfSubPop;
					} else if (bestFitOfSubPop.betterThan(bestOfAll)) {
						bestOfAll = bestFitOfSubPop;
					}
				}

				// final GAFit cfit = (GAFit) bestOfAll;
				final GAFit cfit = (GAFit) bestOfAll;

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
						category, job, trainF1, testF1, testBEP,
						cfit.getPositiveMatchTest(),
						cfit.getNegativeMatchTest(),
						IndexInfoStaticG.totalTestDocsInCat,
					cfit.getQuery());
						//cfit.getQueryMinimal());
				// spanFirstQueryMinimal);

				bestResultsOut.flush();

				System.out.println("TEST F1 : " + testF1);

				cleanup(state);

				final Date end = new Date();		
				timingInfoOut.format("%14s %d %7d %n", "Time for cat", cat,
						end.getTime() - start.getTime());

				System.out.println(end.getTime() - start.getTime()
						+ " total milliseconds for category: " + cat);
				timingInfoOut.flush();

			}
			final Date endJob = new Date();
			timingInfoOut.format("%n%14s %7d %s ", "Total Time: ",
					endJob.getTime() - startJob.getTime(), "for run");
			System.out.println(endJob.getTime() - startJob.getTime()
					+ " total milliseconds for job " + job);
			timingInfoOut.flush();

			final double microF1 = ClassifyQuery.f1(totPosMatchedTest,
					totNegMatchTest, totTest);
			final double microBEP = ClassifyQuery.bep(totPosMatchedTest,
					totNegMatchTest, totTest);

			macroF1 = macroF1 / NUMBER_OF_CATEGORIES;
			System.out.println("OVERALL: micro f1" + microF1 + " macroF1 "
					+ macroF1);

			bestResultsOut.format(" \n");
			bestResultsOut.format("Run Number, %d", job);
			bestResultsOut
					.format(", Micro F1: , %.4f, Micro bep: , %.4f, Macro F1: , %.4f,  Total Positive Matches , %d, Total Negative Matches, %d, Total Docs,  %d \n",
							microF1, microBEP, macroF1, totPosMatchedTest,
							totNegMatchTest, totTest);

			macroF1AllRunsTotal = macroF1AllRunsTotal + macroF1;
			microF1AllRunsTotal = microF1AllRunsTotal + microF1;
			microBEPAllRunsTotal = microBEPAllRunsTotal + microBEP;

			final double microAverageF1AllRuns = microF1AllRunsTotal
					/ (job + 1);
			final double microAverageBEPAllRuns = microBEPAllRunsTotal
					/ (job + 1);
			final double macroAverageF1AllRuns = macroF1AllRunsTotal
					/ (job + 1);
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
			System.out.println();
		}
		final Date endRun = new Date();
		System.out.println(endRun.getTime() - startRun.getTime()
				+ " total milliseconds for run ");
		timingInfoOut.format("%n%14s %7d %s ", "Total Time for Run: ",
				endRun.getTime() - startRun.getTime(), "for run");
		timingInfoOut.flush();

		bestResultsOut.close();
		System.exit(0);
	}

	public static void main(String[] args) {
		try {
			new GAClassifyMain();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
