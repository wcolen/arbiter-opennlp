package org.apache.opennlp.arbiter;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AlgorithmConfiguration {
  public String algorithm;
  public int cutoff;

  public AlgorithmConfiguration() {
  }
}
