package org.apache.opennlp.arbiter.postagger;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.opennlp.arbiter.AlgorithmConfiguration;

@AllArgsConstructor
@Data
public class POSAlgorithmConfiguration extends AlgorithmConfiguration {

  public POSFeatureGeneratorXML featureGeneratorXML;

  public POSAlgorithmConfiguration(String algo, int cutoff, POSFeatureGeneratorXML fg) {
    super(algo, cutoff);
    this.featureGeneratorXML = fg;
  }

}
