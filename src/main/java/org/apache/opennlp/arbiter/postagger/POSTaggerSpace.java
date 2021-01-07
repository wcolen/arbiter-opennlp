package org.apache.opennlp.arbiter.postagger;

import java.util.ArrayList;
import java.util.List;

import org.deeplearning4j.arbiter.optimize.api.ParameterSpace;
import org.deeplearning4j.arbiter.optimize.parameter.discrete.DiscreteParameterSpace;
import org.deeplearning4j.arbiter.optimize.parameter.integer.IntegerParameterSpace;

/**
 * Created by wcolen on 22/05/17.
 */
public class POSTaggerSpace implements ParameterSpace<POSAlgorithmConfiguration> {
  private ParameterSpace<String> algo = new DiscreteParameterSpace("MAXENT", "PERCEPTRON");
  private IntegerParameterSpace cutodd = new IntegerParameterSpace(0,6);

  private IntegerParameterSpace suffix_window_PrevLength = new IntegerParameterSpace(0, 3);
  private IntegerParameterSpace suffix_window_NextLength = new IntegerParameterSpace(0, 3);
  private DiscreteParameterSpace<Boolean> suffix_enable = new DiscreteParameterSpace(true, false);
  private IntegerParameterSpace suffix_length = new IntegerParameterSpace(1, 4);

  private IntegerParameterSpace prefix_window_PrevLength = new IntegerParameterSpace(0, 3);
  private IntegerParameterSpace prefix_window_NextLength = new IntegerParameterSpace(0, 3);
  private DiscreteParameterSpace<Boolean> prefix_enable = new DiscreteParameterSpace(true, false);
  private IntegerParameterSpace prefix_length = new IntegerParameterSpace(1, 5);

  private IntegerParameterSpace token_window_PrevLength = new IntegerParameterSpace(0, 3);
  private IntegerParameterSpace token_window_NextLength = new IntegerParameterSpace(0, 3);
  private DiscreteParameterSpace<Boolean> token_enable = new DiscreteParameterSpace(true, false);

  private IntegerParameterSpace sentence_window_PrevLength = new IntegerParameterSpace(0, 3);
  private IntegerParameterSpace sentence_window_NextLength = new IntegerParameterSpace(0, 3);
  private DiscreteParameterSpace<Boolean> sentence_enable = new DiscreteParameterSpace(true, false);
  private DiscreteParameterSpace<Boolean> sentence_begin = new DiscreteParameterSpace(true, false);
  private DiscreteParameterSpace<Boolean> sentence_end = new DiscreteParameterSpace(true, false);

  private IntegerParameterSpace tokenclass_window_PrevLength = new IntegerParameterSpace(0, 3);
  private IntegerParameterSpace tokenclass_window_NextLength = new IntegerParameterSpace(0, 3);
  private DiscreteParameterSpace<Boolean> tokenclass_enable = new DiscreteParameterSpace(true, false);
  private DiscreteParameterSpace<Boolean> tokenclass_wcf = new DiscreteParameterSpace(true, false);

  // >> arrumar leafs
  private static final int PARAMETERS = 22;

  @Override
  public POSAlgorithmConfiguration getValue(double[] parameterValues) {


    String a = algo.getValue(parameterValues);
    int c = cutodd.getValue(parameterValues);

    POSFeatureGeneratorXML fg = new POSFeatureGeneratorXML();

    fg.setSuffix_window(new POSFeatureGeneratorXML.Window(
        suffix_window_PrevLength.getValue(parameterValues),
        suffix_window_NextLength.getValue(parameterValues)));
    fg.setSuffix_enable(suffix_enable.getValue(parameterValues));
    fg.setSuffix_lenght(suffix_length.getValue(parameterValues));

    fg.setPrefix_window(new POSFeatureGeneratorXML.Window(
        prefix_window_PrevLength.getValue(parameterValues),
        prefix_window_NextLength.getValue(parameterValues)));
    fg.setPrefix_enable(prefix_enable.getValue(parameterValues));
    fg.setPrefix_lenght(prefix_length.getValue(parameterValues));

    fg.setToken_window(new POSFeatureGeneratorXML.Window(
        token_window_PrevLength.getValue(parameterValues),
        token_window_NextLength.getValue(parameterValues)));
    fg.setToken_enable(token_enable.getValue(parameterValues));

    fg.setSentence_window(new POSFeatureGeneratorXML.Window(
        sentence_window_PrevLength.getValue(parameterValues),
        sentence_window_NextLength.getValue(parameterValues)));
    fg.setSentence_enable(sentence_enable.getValue(parameterValues));
    fg.setSentence_begin(sentence_enable.getValue(parameterValues));
    fg.setSentence_end(sentence_enable.getValue(parameterValues));

    fg.setTokenclass_window(new POSFeatureGeneratorXML.Window(
        tokenclass_window_PrevLength.getValue(parameterValues),
        tokenclass_window_NextLength.getValue(parameterValues)));
    fg.setTokenclass_enable(tokenclass_enable.getValue(parameterValues));
    fg.setTokenclass_wcf(tokenclass_wcf.getValue(parameterValues));


    POSAlgorithmConfiguration conf = new POSAlgorithmConfiguration(a,c, fg);
    System.out.println("****** Just prepared a configuratioo: \n   " + conf.toString());
    return conf;

  }

  @Override
  public int numParameters() {
    return PARAMETERS;
  }

  @Override
  public List<ParameterSpace> collectLeaves() {
    List<ParameterSpace> list = new ArrayList<>();
    list.addAll(algo.collectLeaves());
    list.addAll(cutodd.collectLeaves());

    list.addAll(suffix_window_PrevLength.collectLeaves());
    list.addAll(suffix_window_NextLength.collectLeaves());
    list.addAll(suffix_enable.collectLeaves());
    list.addAll(suffix_length.collectLeaves());

    list.addAll(prefix_window_PrevLength.collectLeaves());
    list.addAll(prefix_window_NextLength.collectLeaves());
    list.addAll(prefix_enable.collectLeaves());
    list.addAll(prefix_length.collectLeaves());

    list.addAll(token_window_PrevLength.collectLeaves());
    list.addAll(token_window_NextLength.collectLeaves());
    list.addAll(token_enable.collectLeaves());

    list.addAll(sentence_window_PrevLength.collectLeaves());
    list.addAll(sentence_window_NextLength.collectLeaves());
    list.addAll(sentence_enable.collectLeaves());
    list.addAll(sentence_begin.collectLeaves());
    list.addAll(sentence_end.collectLeaves());

    list.addAll(tokenclass_window_PrevLength.collectLeaves());
    list.addAll(tokenclass_window_NextLength.collectLeaves());
    list.addAll(tokenclass_enable.collectLeaves());
    list.addAll(tokenclass_wcf.collectLeaves());

    return list;
  }

  @Override
  public boolean isLeaf() {
    return false;
  }

  @Override
  public void setIndices(int... indices) {
    throw new UnsupportedOperationException();
  }
}
