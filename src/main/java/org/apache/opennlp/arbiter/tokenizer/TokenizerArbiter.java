package org.apache.opennlp.arbiter.tokenizer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.opennlp.arbiter.AlgorithmConfiguration;
import org.apache.opennlp.arbiter.AlgorithmSpace;
import org.apache.opennlp.arbiter.postagger.OpenNLPTaskCreator;
import org.deeplearning4j.arbiter.optimize.api.CandidateGenerator;
import org.deeplearning4j.arbiter.optimize.api.saving.InMemoryResultSaver;
import org.deeplearning4j.arbiter.optimize.api.saving.ResultReference;
import org.deeplearning4j.arbiter.optimize.api.termination.MaxCandidatesCondition;
import org.deeplearning4j.arbiter.optimize.candidategenerator.RandomSearchGenerator;
import org.deeplearning4j.arbiter.optimize.config.OptimizationConfiguration;
import org.deeplearning4j.arbiter.optimize.runner.IOptimizationRunner;
import org.deeplearning4j.arbiter.optimize.runner.LocalOptimizationRunner;
import org.deeplearning4j.arbiter.optimize.runner.listener.runner.LoggingOptimizationRunnerStatusListener;
import org.deeplearning4j.arbiter.optimize.ui.ArbiterUIServer;
import org.deeplearning4j.arbiter.optimize.ui.listener.UIOptimizationRunnerStatusListener;

/**
 * Created by wcolen on 22/05/17.
 */
public class TokenizerArbiter {

  static Map<String, String> lang = new HashMap<>();
  static {
    lang.put("UD_Arabic", "ara");
    lang.put("UD_Croatian", "bos");
    lang.put("UD_Bulgarian", "bul");
    lang.put("UD_Catalan", "cat");
    lang.put("UD_Czech", "ces");
    lang.put("UD_Chinese", "cmn");
    lang.put("UD_German", "deu");
    lang.put("UD_Greek", "ell");
    lang.put("UD_English", "eng");
    lang.put("UD_Estonian", "est");
    lang.put("UD_Basque", "eus");
    lang.put("UD_Finnish", "fin");
    lang.put("UD_French", "fra");
    lang.put("UD_Galician", "glg");
    lang.put("UD_Hebrew", "heb");
    lang.put("UD_Hungarian", "hun");
    lang.put("UD_Indonesian", "ind");
    lang.put("UD_Italian", "ita");
    lang.put("UD_Japanese", "jpn");
    lang.put("UD_Korean", "kor");
    lang.put("UD_Latin-ITTB", "lat");
    lang.put("UD_Latvian", "lav");
    lang.put("UD_Dutch", "nld");
    lang.put("UD_Norwegian-Nynorsk", "nno");
    lang.put("UD_Norwegian-Bokmaal", "nob");
    lang.put("UD_Persian", "pes");
    lang.put("UD_Polish", "pol");
    lang.put("UD_Portuguese-BR", "por");
    lang.put("UD_Romanian", "ron");
    lang.put("UD_Russian", "rus");
    lang.put("UD_Slovak", "slk");
    lang.put("UD_Slovenian", "slv");
    lang.put("UD_Spanish", "spa");
    lang.put("UD_Swedish", "swe");
    lang.put("UD_Turkish", "tur");
    lang.put("UD_Urdu", "urd");
    lang.put("UD_Vietnamese", "vie");
    lang.put("UD_Hindi", "hin");



    // cant train tok with
    //lang.put("UD_Danish", "dan"); // already tokenized
  }

  public static void main(String[] args) throws IOException {

    String root = "Universal Dependencies 2.0/ud-treebanks-conll2017/";
    for (String folder:
         lang.keySet()) {
      System.out.println("****************");
      System.out.println(folder);
      Path p = Paths.get(root + folder);


      Stream<Path> stream = Files.find(p, 1,
          (path, attr) -> path.getFileName().toString().endsWith("train.conllu") );
      Path train = stream.findFirst().get();

      stream = Files.find(p, 1,
          (path, attr) -> path.getFileName().toString().endsWith("dev.conllu") );
      Path eval = stream.findFirst().get();

      String l = lang.get(folder);
      if (Files.notExists(Paths.get("models/"+l))) {

        ResultReference<AlgorithmConfiguration, AlgorithmConfiguration, Void> result = evaluate(train, eval, l, null);
      }


    }

  }


  public static ResultReference<AlgorithmConfiguration, AlgorithmConfiguration, Void> evaluate(Path trainingData, Path evaluationData, String language, char[] eos) throws IOException {
    //Define configuration:
    CandidateGenerator<AlgorithmConfiguration> candidateGenerator = new RandomSearchGenerator(new AlgorithmSpace());

//    CandidateGenerator<AlgorithmConfiguration> candidateGenerator =
//        new GridSearchCandidateGenerator<>(new OpenNLPSpace(), 4,
//            GridSearchCandidateGenerator.Mode.Sequential);

    OptimizationConfiguration<AlgorithmConfiguration, AlgorithmConfiguration, Void, Void> configuration =
        new OptimizationConfiguration.Builder<AlgorithmConfiguration, AlgorithmConfiguration, Void, Void >()
            .candidateGenerator(candidateGenerator)
            .scoreFunction(new TokenizerScoreFunction(trainingData, evaluationData, language, eos))
            .terminationConditions(new MaxCandidatesCondition(200))
            .modelSaver(new InMemoryResultSaver())
            .build();

    IOptimizationRunner<AlgorithmConfiguration, AlgorithmConfiguration, Void> runner
        = new LocalOptimizationRunner(4, configuration, new OpenNLPTaskCreator());
        runner.addListeners(new LoggingOptimizationRunnerStatusListener());

    ArbiterUIServer server = ArbiterUIServer.getInstance();
    runner.addListeners(new UIOptimizationRunnerStatusListener(server));
    runner.execute();


    System.out.println("Score for " + language);
    System.out.println("Best score: " + runner.bestScore());
    System.out.println("Best score index: " + runner.bestScoreCandidateIndex());

    ResultReference<AlgorithmConfiguration, AlgorithmConfiguration, Void> result = runner.getResults().get(runner.bestScoreCandidateIndex());

    System.out.println(result.getResult().getCandidate().toString());


    // create final model
    TokenizerScoreFunction.evaluateAndSerializedModel(language, trainingData, evaluationData, result.getResult().getCandidate().getValue());
    result.getResult().getCandidate().getValue();
    System.out.println("----- Complete -----");

    return result;
  }
}
