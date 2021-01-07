package org.apache.opennlp.arbiter.sentdetect;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.opennlp.arbiter.AlgorithmConfiguration;
import org.deeplearning4j.arbiter.optimize.api.data.DataProvider;
import org.deeplearning4j.arbiter.optimize.api.score.ScoreFunction;

import opennlp.tools.cmdline.CmdLineUtil;
import opennlp.tools.formats.conllu.ConlluSentenceSampleStream;
import opennlp.tools.formats.conllu.ConlluStream;
import opennlp.tools.sentdetect.SentenceDetectorEvaluator;
import opennlp.tools.sentdetect.SentenceDetectorFactory;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.sentdetect.SentenceSample;
import opennlp.tools.util.InputStreamFactory;
import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.Span;
import opennlp.tools.util.TrainingParameters;
import opennlp.tools.util.eval.FMeasure;
import opennlp.tools.util.model.ModelUtil;

/**
 * Created by wcolen on 22/05/17.
 */
public class SentenceScoreFunction implements ScoreFunction<AlgorithmConfiguration,Void> {

  private static final Map<AlgorithmConfiguration, Double> visited = new ConcurrentHashMap<>();
  private final Path trainingData;
  private final Path evaluationData;
  private final String language;
  private final char[] eos;


  public SentenceScoreFunction(Path trainingData, Path evaluationData, String language, char[] eos) {
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
      SentenceModel model = train(createSamples(trainingData, eos), configuration.getAlgorithm(), configuration.getCutoff(), language, eos);
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

  static ObjectStream<SentenceSample> createSamples(Path corpus, char[] eos) {

    InputStreamFactory inFactory =
        CmdLineUtil.createInputStreamFactory(corpus.toFile());
    try {
      Character defaultEOS = null;
      if (eos != null && eos.length > 0) {
        defaultEOS = eos[0];
      }
      ConlluSentenceSampleStream stream = new ConlluSentenceSampleStream(new ConlluStream(inFactory),
          3);

      /*SentenceSample entry = stream.read();
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

  static FMeasure evaluate(SentenceModel model, ObjectStream<SentenceSample> sampleStream) throws IOException {

    SentenceDetectorEvaluator evaluator = new SentenceDetectorEvaluator(
        new SentenceDetectorME(model)
    );

    evaluator.evaluate(sampleStream);
    return evaluator.getFMeasure();
  }

  public static SentenceModel train(ObjectStream<SentenceSample> sampleStream, String algorithm, int cutoff, String lang, char[] eos) throws IOException {
    SentenceModel model;

    TrainingParameters params = ModelUtil.createDefaultTrainingParameters();
    params.put(TrainingParameters.ALGORITHM_PARAM, algorithm);
    params.put(TrainingParameters.CUTOFF_PARAM, cutoff);
    params.put(TrainingParameters.THREADS_PARAM, 8);

    model = SentenceDetectorME.train(lang, sampleStream, new SentenceDetectorFactory(lang, true, null, eos), params);

    sampleStream.close();

    return model;
  }

  static char[] findEOS(ObjectStream<SentenceSample> sampleStream) throws IOException {
    Map<Character, AtomicLong> eosMap = new HashMap<>();
    Map<Character, AtomicLong> map = new HashMap<>();

    SentenceSample entry = sampleStream.read();
    while (entry != null) {

      for (Span s:
           entry.getSentences()) {
        Character c = entry.getDocument().charAt(s.getEnd() - 1);
        if (!eosMap.containsKey(c)) {
          eosMap.put(c, new AtomicLong());
        }
        eosMap.get(c).incrementAndGet();

        for (int i = s.getStart(); i < s.getEnd() - 2; i++) {
          c = entry.getDocument().charAt(i);
          if (!map.containsKey(c)) {
            map.put(c, new AtomicLong());
          }
          map.get(c).incrementAndGet();
        }
      }
      entry = sampleStream.read();
    }
    List<Character> charList = new ArrayList<>();
    for (Character c:
         eosMap.keySet()) {
      long nonEOS = map.getOrDefault(c, new AtomicLong(0)).get();
      if(nonEOS < eosMap.get(c).get()) {
        charList.add(c);
      }
    }

    sampleStream.reset();
    charList.sort(Comparator.comparingInt(o -> eosMap.get(o).intValue()).reversed());
    char[] output = new char[charList.size()];
    for (int i = 0; i < charList.size(); i++) {
      output[i] = charList.get(i);
    }
    return output;
  }


  public static void evaluateAndSerializedModel(String lang, Path trainingData, Path evaluationData, AlgorithmConfiguration conf) throws IOException {
    String outputFolder = "models/" + lang + "/";
    String modelFile = outputFolder + lang + "-sentdetect.bin";
    //Files.createParentDirs(new File(outputFolder));
    new File(outputFolder).mkdirs();

    ObjectStream<SentenceSample> trainingSample = createSamples(trainingData, null);
    char[] eos = findEOS(trainingSample);
    SentenceModel model = train(createSamples(trainingData, eos), conf.algorithm, conf.getCutoff(), lang, eos);
    FMeasure res = evaluate(model, createSamples(evaluationData, eos));

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
