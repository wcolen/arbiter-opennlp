My first try to use DL4J Arbiter and OpenNLP, originally written in 2017.

I wrote the code using the following pattern:

- Train & evaluate routines that tries multiple parameters
- Code do do that with sentdetect, tokenizer and POS
- Each package has a main class: <Module>Arbiter.java
- Specific optimization functions were created, for example:	
    - It tries to automatically infer by counting the End of Sentence chars for each language. It is needed to configure the model	
    - For POS, it was created as a tool to generate Feature XML automatically, but the search universe gets really big.

To execute you can download the UD 2.0 from here: https://lindat.mff.cuni.cz/repository/xmlui/handle/11234/1-1983

There are newer versions of UD dataset, currently 2.8.