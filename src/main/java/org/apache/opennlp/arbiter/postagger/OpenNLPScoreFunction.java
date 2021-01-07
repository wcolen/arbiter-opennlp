package org.apache.opennlp.arbiter.postagger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.deeplearning4j.arbiter.optimize.api.data.DataProvider;
import org.deeplearning4j.arbiter.optimize.api.score.ScoreFunction;

import opennlp.tools.cmdline.ObjectStreamFactory;
import opennlp.tools.cmdline.StreamFactoryRegistry;
import opennlp.tools.cmdline.TerminateToolException;
import opennlp.tools.formats.conllu.ConlluPOSSampleStreamFactory;
import opennlp.tools.postag.POSEvaluator;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSSample;
import opennlp.tools.postag.POSTaggerFactory;
import opennlp.tools.util.InvalidFormatException;
import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.TrainingParameters;
import opennlp.tools.util.model.ModelUtil;

/**
 * Created by wcolen on 22/05/17.
 */
public class OpenNLPScoreFunction implements ScoreFunction<POSAlgorithmConfiguration,Void> {

  private static final Map<POSAlgorithmConfiguration, Double> visited = new ConcurrentHashMap<>();
  private final String language;
  private final Path evaluationData;
  private final Path trainingData;

  public OpenNLPScoreFunction(Path trainingData, Path evaluationData, String language) {
    this.trainingData = trainingData;
    this.evaluationData = evaluationData;
    this.language = language;
  }

  @Override
  public double score(POSAlgorithmConfiguration configuration,
                      DataProvider<Void> dataProvider,
                      Map<String, Object> dataParameters) {
    if(visited.containsKey(configuration)) {
      System.out.println("**** already visited ****");
      return visited.get(configuration);
    }

    System.out.println("Algo: " + configuration.getAlgorithm());
    System.out.println("Cutoff: " + configuration.getCutoff());
    try {
      POSModel model = train(createSamples(trainingData), configuration.getAlgorithm(), configuration.getCutoff(), configuration.getFeatureGeneratorXML());
      double res = evaluate(model, createSamples(evaluationData));
      visited.put(configuration, res);
      return res;
    } catch (Throwable e) {
      e.printStackTrace();
      return 0.01;
    }
  }

  @Override
  public boolean minimize() {
    return false;
  }

  static ObjectStream<POSSample> createSamples(Path corpus) {


    ConlluPOSSampleStreamFactory.registerFactory();
    ObjectStreamFactory<POSSample> factory = StreamFactoryRegistry.getFactories(POSSample.class).get(ConlluPOSSampleStreamFactory.CONLLU_FORMAT);
    ObjectStream<POSSample> samples = factory.create(new String[] {
        "-data", corpus.toString(),
        "-tagset", "u"
    });
    return samples;
  }

  static double evaluate(POSModel model, ObjectStream<POSSample> sampleStream) throws IOException {

    POSEvaluator evaluator = new POSEvaluator(
        new opennlp.tools.postag.POSTaggerME(model) {

        });

    evaluator.evaluate(sampleStream);
    return evaluator.getWordAccuracy();
  }

  static POSModel train(ObjectStream<POSSample> sampleStream, String algorithm, int cutoff, POSFeatureGeneratorXML featureGeneratorXML) throws IOException {
    POSModel model;

    TrainingParameters params = ModelUtil.createDefaultTrainingParameters();
    params.put(TrainingParameters.ALGORITHM_PARAM, algorithm);
    params.put(TrainingParameters.CUTOFF_PARAM, cutoff);
    params.put(TrainingParameters.THREADS_PARAM, 8);

    POSTaggerFactory postaggerFactory;
    try {
      postaggerFactory = POSTaggerFactory.create(null, featureGeneratorXML.create(),
          null, null);
    } catch (InvalidFormatException e) {
      throw new TerminateToolException(-1, e.getMessage(), e);
    }

    model = opennlp.tools.postag.POSTaggerME.train("pt",
        sampleStream, params, postaggerFactory);

    sampleStream.close();

    return model;
  }




  public static void evaluateAndSerializedModel(String lang, Path trainingData, Path evaluationData, POSAlgorithmConfiguration conf) throws IOException {
    String outputFolder = "models/" + lang + "/";
    String modelFile = outputFolder + lang + "-tokenizer.bin";
    //Files.createParentDirs(new File(outputFolder));
    new File(outputFolder).mkdirs();

    ObjectStream<POSSample> trainingSample = createSamples(trainingData);

    POSModel model = train(createSamples(trainingData), conf.algorithm, conf.getCutoff(), conf.getFeatureGeneratorXML());
    double res = evaluate(model, createSamples(evaluationData));

    model.serialize(new FileOutputStream(modelFile));

    Path path = Paths.get(modelFile + ".txt");

    try (BufferedWriter writer = java.nio.file.Files.newBufferedWriter(path)) {
      writer.write("Training data: " + trainingData);
      writer.newLine();
      writer.write("Evaluation data: " + evaluationData);
      writer.newLine();
      writer.write(conf.toString());
      writer.newLine();
      writer.write(Double.toString(res));

      writer.close();
    }

    StringBuilder sb = new StringBuilder();
    sb.append(lang).append("\t")
        .append(Double.toString(res)).append("\n");

    try {
      java.nio.file.Files.write(Paths.get("models/postagger.txt"), sb.toString().getBytes(), StandardOpenOption.APPEND, StandardOpenOption.CREATE);
    }catch (IOException e) {
      e.printStackTrace();
    }
  }


}
