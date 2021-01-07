package org.apache.opennlp.arbiter.tokenizer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.opennlp.arbiter.AlgorithmConfiguration;
import org.deeplearning4j.arbiter.optimize.api.data.DataProvider;
import org.deeplearning4j.arbiter.optimize.api.score.ScoreFunction;

import opennlp.tools.cmdline.CmdLineUtil;
import opennlp.tools.formats.conllu.ConlluStream;
import opennlp.tools.formats.conllu.ConlluTokenSampleStream;
import opennlp.tools.tokenize.TokenSample;
import opennlp.tools.tokenize.TokenizerEvaluator;
import opennlp.tools.tokenize.TokenizerFactory;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.InputStreamFactory;
import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.TrainingParameters;
import opennlp.tools.util.eval.FMeasure;
import opennlp.tools.util.model.ModelUtil;

/**
 * Created by wcolen on 22/05/17.
 */
public class TokenizerScoreFunction implements ScoreFunction<AlgorithmConfiguration,Void> {

  private static final Map<AlgorithmConfiguration, Double> visited = new ConcurrentHashMap<>();
  private final Path trainingData;
  private final Path evaluationData;
  private final String language;
  private final char[] eos;


  public TokenizerScoreFunction(Path trainingData, Path evaluationData, String language, char[] eos) {
    this.trainingData = trainingData;
    this.evaluationData = evaluationData;
    this.language = language;
    this.eos = eos;
  }

  @Override
  public double score(AlgorithmConfiguration configuration,
                      DataProvider<Void> dataProvider,
                      Map<String, Object> dataParameters) {
    if(visited.containsKey(configuration)) {
      System.out.println("**** already visited ****");
      return visited.get(configuration);
    }

    try {
      TokenizerModel model = train(createSamples(trainingData, eos), configuration.getAlgorithm(), configuration.getCutoff(), language, eos);
      double res = evaluate(model, createSamples(evaluationData, eos)).getFMeasure();
      visited.put(configuration, res);
      return res;
    } catch (IOException e) {
      throw  new RuntimeException(e);
    }
  }

  @Override
  public boolean minimize() {
    return false;
  }

  static ObjectStream<TokenSample> createSamples(Path corpus, char[] eos) {
    
    InputStreamFactory inFactory =
        CmdLineUtil.createInputStreamFactory(corpus.toFile());
    try {
      ConlluTokenSampleStream stream = new ConlluTokenSampleStream(new ConlluStream(inFactory));

      /*TokenSample entry = stream.read();
      while(entry != null) {
        System.out.println(entry);

        entry = stream.read();
      }
      stream.reset();*/

      return stream;

    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }

  static FMeasure evaluate(TokenizerModel model, ObjectStream<TokenSample> sampleStream) throws IOException {

    TokenizerEvaluator evaluator = new TokenizerEvaluator(
        new TokenizerME(model)
    );

    evaluator.evaluate(sampleStream);
    return evaluator.getFMeasure();
  }

  public static TokenizerModel train(ObjectStream<TokenSample> sampleStream, String algorithm, int cutoff, String lang, char[] eos) throws IOException {
    TokenizerModel model;

    TrainingParameters params = ModelUtil.createDefaultTrainingParameters();
    params.put(TrainingParameters.ALGORITHM_PARAM, algorithm);
    params.put(TrainingParameters.CUTOFF_PARAM, cutoff);
    params.put(TrainingParameters.THREADS_PARAM, 8);

    model = TokenizerME.train(sampleStream, new TokenizerFactory(lang, null, true, null), params);

    sampleStream.close();

    return model;
  }


  public static void evaluateAndSerializedModel(String lang, Path trainingData, Path evaluationData, AlgorithmConfiguration conf) throws IOException {
    String outputFolder = "models/" + lang + "/";
    String modelFile = outputFolder + lang + "-tokenizer.bin";
    //Files.createParentDirs(new File(outputFolder));
    new File(outputFolder).mkdirs();

    ObjectStream<TokenSample> trainingSample = createSamples(trainingData, null);

    TokenizerModel model = train(createSamples(trainingData, null), conf.algorithm, conf.getCutoff(), lang, null);
    FMeasure res = evaluate(model, createSamples(evaluationData, null));

    model.serialize(new FileOutputStream(modelFile));

    Path path = Paths.get(modelFile + ".txt");

    try (BufferedWriter writer = java.nio.file.Files.newBufferedWriter(path)) {
      writer.write("Training data: " + trainingData);
      writer.newLine();
      writer.write("Evaluation data: " + evaluationData);
      writer.newLine();
      writer.write(conf.toString());
      writer.newLine();
      writer.write(res.toString());

      writer.close();
    }

    StringBuilder sb = new StringBuilder();
    sb.append(lang).append("\tP: ")
        .append(res.getPrecisionScore()).append("\tR: ")
        .append(res.getRecallScore()).append("\tF1: ")
        .append(res.getFMeasure()).append("\n");

    try {
      java.nio.file.Files.write(Paths.get("models/sentenceDetector.txt"), sb.toString().getBytes(), StandardOpenOption.APPEND, StandardOpenOption.CREATE);
    }catch (IOException e) {
      e.printStackTrace();
    }
  }
}
