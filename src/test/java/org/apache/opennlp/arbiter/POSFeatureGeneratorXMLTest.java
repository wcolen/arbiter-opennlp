package org.apache.opennlp.arbiter;

import org.apache.opennlp.arbiter.postagger.POSFeatureGeneratorXML;
import org.junit.Assert;

/**
 * Created by wcolen on 28/05/17.
 */
public class POSFeatureGeneratorXMLTest {


  @org.junit.Test
  public void testSuffix() throws Exception {
    POSFeatureGeneratorXML builder = new POSFeatureGeneratorXML();

    builder.setSuffix_window(new POSFeatureGeneratorXML.Window(3,4));
    builder.setSuffix_lenght(2);
    builder.setSuffix_enable(true);

    String xml = new String(builder.create());

    Assert.assertTrue(xml.contains("window nextLength=\"4\" prevLength=\"3\""));
    Assert.assertTrue(xml.contains("<suffix length=\"2\"/>"));

    builder.setSuffix_enable(false);
    xml = new String(builder.create());
    Assert.assertFalse(xml.contains("window nextLength=\"4\" prevLength=\"3\""));
    Assert.assertFalse(xml.contains("<suffix length=\"2\"/>"));

  }

  @org.junit.Test
  public void testPrefix() throws Exception {
    POSFeatureGeneratorXML builder = new POSFeatureGeneratorXML();

    builder.setPrefix_window(new POSFeatureGeneratorXML.Window(3,4));
    builder.setPrefix_lenght(2);
    builder.setPrefix_enable(true);

    String xml = new String(builder.create());

    Assert.assertTrue(xml.contains("window nextLength=\"4\" prevLength=\"3\""));
    Assert.assertTrue(xml.contains("<prefix length=\"2\"/>"));

    builder.setPrefix_enable(false);
    xml = new String(builder.create());
    Assert.assertFalse(xml.contains("window nextLength=\"4\" prevLength=\"3\""));
    Assert.assertFalse(xml.contains("<prefix length=\"2\"/>"));

  }

  @org.junit.Test
  public void testToken() throws Exception {
    POSFeatureGeneratorXML builder = new POSFeatureGeneratorXML();

    builder.setToken_window(new POSFeatureGeneratorXML.Window(3,4));
    builder.setToken_enable(true);

    String xml = new String(builder.create());

    Assert.assertTrue(xml.contains("window nextLength=\"4\" prevLength=\"3\""));

    Assert.assertTrue(xml.contains("<token/>"));
    builder.setToken_enable(false);
    xml = new String(builder.create());
    Assert.assertFalse(xml.contains("window nextLength=\"4\" prevLength=\"3\""));
    Assert.assertFalse(xml.contains("<prefix length=\"2\"/>"));

  }

  @org.junit.Test
  public void testSentence() throws Exception {
    POSFeatureGeneratorXML builder = new POSFeatureGeneratorXML();

    builder.setSentence_window(new POSFeatureGeneratorXML.Window(3,4));
    builder.setSentence_enable(true);
    builder.setSentence_begin(true);
    builder.setSentence_end(false);

    String xml = new String(builder.create());

    Assert.assertTrue(xml.contains("window nextLength=\"4\" prevLength=\"3\""));

    Assert.assertTrue(xml.contains("<sentence begin=\"true\" end=\"false\"/>"));
    builder.setSentence_begin(false);
    builder.setSentence_end(true);
    xml = new String(builder.create());
    Assert.assertTrue(xml.contains("<sentence begin=\"false\" end=\"true\"/>"));

    builder.setSentence_enable(false);
    xml = new String(builder.create());
    Assert.assertFalse(xml.contains("window nextLength=\"4\" prevLength=\"3\""));
    Assert.assertFalse(xml.contains("sentence"));

  }

  @org.junit.Test
  public void testTokenclass() throws Exception {
    POSFeatureGeneratorXML builder = new POSFeatureGeneratorXML();

    builder.setTokenclass_window(new POSFeatureGeneratorXML.Window(3,4));
    builder.setTokenclass_enable(true);
    builder.setTokenclass_wcf(true);

    String xml = new String(builder.create());

    Assert.assertTrue(xml.contains("window nextLength=\"4\" prevLength=\"3\""));

    Assert.assertTrue(xml.contains("<tokenclass wordAndClass=\"true\"/>"));

    builder.setTokenclass_wcf(false);
    xml = new String(builder.create());
    Assert.assertTrue(xml.contains("<tokenclass wordAndClass=\"false\"/>"));

    builder.setTokenclass_enable(false);
    xml = new String(builder.create());
    Assert.assertFalse(xml.contains("window nextLength=\"4\" prevLength=\"3\""));
    Assert.assertFalse(xml.contains("tokenclass"));

  }

}