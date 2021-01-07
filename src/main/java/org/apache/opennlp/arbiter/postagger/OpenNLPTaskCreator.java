package org.apache.opennlp.arbiter.postagger;

import java.util.concurrent.Callable;

import org.apache.opennlp.arbiter.AlgorithmConfiguration;
import org.deeplearning4j.arbiter.optimize.api.Candidate;
import org.deeplearning4j.arbiter.optimize.api.OptimizationResult;
import org.deeplearning4j.arbiter.optimize.api.TaskCreator;
import org.deeplearning4j.arbiter.optimize.api.data.DataProvider;
import org.deeplearning4j.arbiter.optimize.api.score.ScoreFunction;
import org.deeplearning4j.arbiter.optimize.runner.Status;
import org.deeplearning4j.arbiter.optimize.runner.listener.candidate.UICandidateStatusListener;
import org.deeplearning4j.ui.components.text.ComponentText;

/**
 * Created by wcolen on 22/05/17.
 */
public class OpenNLPTaskCreator implements TaskCreator<AlgorithmConfiguration,AlgorithmConfiguration,Void,Void> {
  @Override
  public Callable<OptimizationResult<AlgorithmConfiguration, AlgorithmConfiguration, Void>>
        create(final Candidate<AlgorithmConfiguration> candidate,
               final DataProvider<Void> dataProvider,
               final ScoreFunction<AlgorithmConfiguration, Void> scoreFunction,
               final UICandidateStatusListener statusListener) {

    if(statusListener != null){
      statusListener.reportStatus(Status.Created,new ComponentText("Config: " + candidate.toString(), null));
    }

    return new Callable<OptimizationResult<AlgorithmConfiguration, AlgorithmConfiguration, Void>>() {
      @Override
      public OptimizationResult<AlgorithmConfiguration, AlgorithmConfiguration, Void> call() throws Exception {

        if(statusListener != null) {
          statusListener.reportStatus(Status.Running,
              new ComponentText("Config: " + candidate.toString(), null)
          );
        }

        double score = scoreFunction.score(candidate.getValue(),null,null);
        System.out.println(candidate.getValue().toString() + "\t" + score);

        Thread.sleep(500);
        if(statusListener != null) {
          statusListener.reportStatus(Status.Complete,
              new ComponentText("Config: " + candidate.toString(), null),
              new ComponentText("Score: " + score, null)
          );
        }

        return new OptimizationResult<>(candidate,candidate.getValue(), score, candidate.getIndex(), null);
      }
    };
  }
}
