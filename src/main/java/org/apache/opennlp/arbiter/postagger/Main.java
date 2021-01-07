//package org.apache.opennlp.arbiter.postagger;
//
//import java.io.IOException;
//
//import org.deeplearning4j.arbiter.optimize.api.CandidateGenerator;
//import org.deeplearning4j.arbiter.optimize.api.saving.InMemoryResultSaver;
//import org.deeplearning4j.arbiter.optimize.api.saving.ResultReference;
//import org.deeplearning4j.arbiter.optimize.api.termination.MaxCandidatesCondition;
//import org.deeplearning4j.arbiter.optimize.candidategenerator.RandomSearchGenerator;
//import org.deeplearning4j.arbiter.optimize.config.OptimizationConfiguration;
//import org.deeplearning4j.arbiter.optimize.runner.IOptimizationRunner;
//import org.deeplearning4j.arbiter.optimize.runner.LocalOptimizationRunner;
//import org.deeplearning4j.arbiter.optimize.ui.ArbiterUIServer;
//import org.deeplearning4j.arbiter.optimize.ui.listener.UIOptimizationRunnerStatusListener;
//
///**
// * Created by wcolen on 22/05/17.
// */
//public class Main {
//  public static void main(String[] args) throws IOException {
//    //Define configuration:
//    CandidateGenerator<POSAlgorithmConfiguration> candidateGenerator = new RandomSearchGenerator(new POSTaggerSpace());
//
////    CandidateGenerator<POSAlgorithmConfiguration> candidateGenerator =
////        new GridSearchCandidateGenerator<>(new OpenNLPSpace(), 4,
////            GridSearchCandidateGenerator.Mode.Sequential);
//
//    OptimizationConfiguration<POSAlgorithmConfiguration, POSAlgorithmConfiguration, Void, Void> configuration =
//        new OptimizationConfiguration.Builder<POSAlgorithmConfiguration, POSAlgorithmConfiguration, Void, Void >()
//            .candidateGenerator(candidateGenerator)
//            .scoreFunction(new OpenNLPScoreFunction(trainingData, evaluationData))
//            .terminationConditions(new MaxCandidatesCondition(50))
//            .modelSaver(new InMemoryResultSaver())
//
//            .build();
//
//    IOptimizationRunner<POSAlgorithmConfiguration, POSAlgorithmConfiguration, Void> runner
//        = new LocalOptimizationRunner(4, configuration, new OpenNLPTaskCreator());
////        runner.addListeners(new LoggingOptimizationRunnerStatusListener());
//
//    ArbiterUIServer server = ArbiterUIServer.getInstance();
//    runner.addListeners(new UIOptimizationRunnerStatusListener(server));
//    runner.execute();
//
//
//    System.out.println("Best score: " + runner.bestScore());
//    System.out.println("Best score index: " + runner.bestScoreCandidateIndex());
//
//    ResultReference<POSAlgorithmConfiguration, POSAlgorithmConfiguration, Void> result = runner.getResults().get(runner.bestScoreCandidateIndex());
//
//    System.out.println(result.getResult().getCandidate().toString());
//
//    System.out.println("----- Complete -----");
//  }
//}
