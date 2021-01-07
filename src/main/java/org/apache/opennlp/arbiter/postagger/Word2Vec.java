package org.apache.opennlp.arbiter.postagger;

import java.io.IOException;

/**
 * Created by wcolen on 28/05/17.
 */
public class Word2Vec {

  private String trainFile;
  private String outputFile;

  private int size;
  private int windows;
  private float sample;
  private int hs;
  private int negative;
  private int threads;
  private int minCount;
  private float alpha;
  private int classes;
  private int cbow;


  public void exec() throws IOException, InterruptedException {

    ProcessBuilder pb = new ProcessBuilder("/Users/wcolen/git/word2vec/bin/word2vec");
    pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
    pb.redirectError(ProcessBuilder.Redirect.INHERIT);
    Process pr = pb.start();


    int retVal = pr.waitFor();


  }

  public static void main(String[] args) throws IOException, InterruptedException {
    new Word2Vec().exec();
  }

}
