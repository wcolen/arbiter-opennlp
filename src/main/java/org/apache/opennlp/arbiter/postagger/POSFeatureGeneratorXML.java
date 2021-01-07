package org.apache.opennlp.arbiter.postagger;

import java.io.ByteArrayOutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;


@Data
public class POSFeatureGeneratorXML {

  private Window suffix_window;
  private Boolean suffix_enable;
  private int suffix_lenght;

  private Window prefix_window;
  private Boolean prefix_enable;
  private int prefix_lenght;

  private Window token_window;
  private Boolean token_enable;

  private Window sentence_window;
  private Boolean sentence_enable;
  private Boolean sentence_begin;
  private Boolean sentence_end;

  private Window tokenclass_window;
  private Boolean tokenclass_enable;
  private Boolean tokenclass_wcf;


  public static void main(String[] args) {
    POSFeatureGeneratorXML xml = new POSFeatureGeneratorXML();


    xml.setSuffix_window(new Window(1,2));
    xml.setSuffix_enable(true);
    xml.setSuffix_lenght(3);

    xml.setPrefix_window(new Window(3,4));
    xml.setPrefix_enable(true);
    xml.setPrefix_lenght(5);

    byte[] arr = xml.create();

    System.out.println(arr.length);
  }

  @Override
  public String toString() {
     return "\n" + new String(create()) + "\n";
  }


  public byte[] create() {
    DocumentBuilderFactory icFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder icBuilder;

    try {
      icBuilder = icFactory.newDocumentBuilder();
      Document doc = icBuilder.newDocument();

      Element generators = doc.createElement("generators");
      doc.appendChild(generators);

      Element cache = doc.createElement("cache");
      generators.appendChild(cache);

      Element inner = doc.createElement("generators");
      cache.appendChild(inner);

      // append child elements to root element
      inner.appendChild(getDefinition(doc));

      // append child elements to root element
      inner.appendChild(getPostagger(doc));

      if(isEnable(suffix_enable) && suffix_window != null) {
        inner.appendChild(getWindow(doc, getSuffix(doc, suffix_lenght), suffix_window));
      }

      if(isEnable(prefix_enable) && prefix_window != null) {
        inner.appendChild(getWindow(doc, getPrefix(doc, prefix_lenght), prefix_window));
      }

      if(isEnable(token_enable) && token_window != null) {
        inner.appendChild(getWindow(doc, getToken(doc), token_window));
      }

      if(isEnable(sentence_enable) && sentence_window != null) {
        inner.appendChild(getWindow(doc, getSentence(doc, sentence_begin, sentence_end), sentence_window));
      }

      if(isEnable(tokenclass_enable) && tokenclass_window != null) {
        inner.appendChild(getWindow(doc, getTokenclass(doc, tokenclass_wcf), tokenclass_window));
      }


      // output DOM XML to console
      Transformer transformer = TransformerFactory.newInstance().newTransformer();
      transformer.setOutputProperty(OutputKeys.INDENT, "yes");
      transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
      DOMSource source = new DOMSource(doc);
      StreamResult console = new StreamResult(System.out);
      transformer.transform(source, console);

      System.out.println("\nXML DOM Created Successfully..");

      // output DOM XML to bytes
      transformer = TransformerFactory.newInstance().newTransformer();
      transformer.setOutputProperty(OutputKeys.INDENT, "yes");
      transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
      source = new DOMSource(doc);

      ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
      StreamResult streamResult = new StreamResult(byteArrayOutputStream);
      transformer.transform(source, streamResult);

      return byteArrayOutputStream.toByteArray();

    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  private boolean isEnable(Boolean value) {
    if(value != null) {
      return value;
    }
    return false;
  }

  private Node getWindow(Document doc, Node child, Window w) {
    Element node = doc.createElement("window");
    node.setAttribute("prevLength", Integer.toString(w.getPrevLength()));
    node.setAttribute("nextLength", Integer.toString(w.getNextLength()));

    node.appendChild(child);

    return node;
  }

  private static Node getDefinition(Document doc) {
    Element node = doc.createElement("definition");
    return node;
  }

  private Node getPostagger(Document doc) {
    Element node = doc.createElement("postagger");
    return node;
  }

  private static Node getSuffix(Document doc, int lenght) {
    Element node = doc.createElement("suffix");
    node.setAttribute("length", Integer.toString(lenght));
    return node;
  }

  private static Node getPrefix(Document doc, int lenght) {
    Element node = doc.createElement("prefix");
    node.setAttribute("length", Integer.toString(lenght));
    return node;
  }

  private static Node getToken(Document doc) {
    Element node = doc.createElement("token");
    return node;
  }

  private Node getSentence(Document doc, Boolean sentence_begin, Boolean sentence_end) {
    Element node = doc.createElement("sentence");
    node.setAttribute("begin", Boolean.toString(sentence_begin));
    node.setAttribute("end", Boolean.toString(sentence_end));
    return node;
  }

  private Node getTokenclass(Document doc, Boolean tokenclass_wcf) {
    Element node = doc.createElement("tokenclass");
    node.setAttribute("wordAndClass", Boolean.toString(tokenclass_wcf));
    return node;
  }

  @Data
  @AllArgsConstructor
  public static class Window {
    private int prevLength;
    private int nextLength;
  }

}
