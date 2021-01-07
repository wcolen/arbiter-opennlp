package org.apache.opennlp.arbiter;

import java.util.ArrayList;
import java.util.List;

import org.deeplearning4j.arbiter.optimize.api.ParameterSpace;
import org.deeplearning4j.arbiter.optimize.parameter.discrete.DiscreteParameterSpace;
import org.deeplearning4j.arbiter.optimize.parameter.integer.IntegerParameterSpace;

/**
 * Created by wcolen on 22/05/17.
 */
public class AlgorithmSpace implements ParameterSpace<AlgorithmConfiguration> {
  private ParameterSpace<String> algo = new DiscreteParameterSpace("MAXENT", "PERCEPTRON");
  private IntegerParameterSpace cutodd = new IntegerParameterSpace(0,10);

  // >> arrumar leafs
  private static final int PARAMETERS = 2;

  @Override
  public AlgorithmConfiguration getValue(double[] parameterValues) {


    String a = algo.getValue(parameterValues);
    int c = cutodd.getValue(parameterValues);

    AlgorithmConfiguration conf = new AlgorithmConfiguration(a,c);
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
