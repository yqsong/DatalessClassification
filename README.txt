This package is a Java library that implements Hierarchical Dataless Text Classification using different representations. The representations are mainly constructed based on Wikipedia:

http://download.wikimedia.org/enwiki/latest/enwiki-latest-pages-articles.xml.bz2. This is a very large file (>10GB) and will take time.

Some of the processed data can be found here:

http://cogcomp.cs.illinois.edu/page/resource_view/97

--Update 20190412--
Some of the files are copied here.
http://home.cse.ust.hk/~yqsong/uiuc_backup/WikiLuceneIndex_word500_link30.zip
http://home.cse.ust.hk/~yqsong/uiuc_backup/wikiIndexLucene3.0.2_vivek.zip
http://home.cse.ust.hk/~yqsong/uiuc_backup/wiki_structured.zip
http://home.cse.ust.hk/~yqsong/uiuc_backup/enwiki_vivek_200.zip
http://home.cse.ust.hk/~yqsong/uiuc_backup/vectors-enwikitext_vivek200.zip
http://home.cse.ust.hk/~yqsong/uiuc_backup/MemoryBasedESA.zip
--End of Update 20190412--

It contails following files:

1. Wikipedia index simple version (11G): used for edu.illinois.cs.cogcomp.classification.representation.esa.simple.SimpleESALocal

2. Wikipedia index complex version (7G): intermediate index file for edu.illinois.cs.cogcomp.classification.representation.indexer.complex.IndexWords1stStep
3. Wikipedia structured data (3G): used for edu.illinois.cs.cogcomp.classification.representation.esa.complex.DiskBasedComplexESA
4. Wikipedia word inverted index based on complex method (7G): used for edu.illinois.cs.cogcomp.classification.representation.esa.complex.DiskBasedComplexESA

5. Memory based ESA: caching each word representation (0.4G): used for edu.illinois.cs.cogcomp.classification.representation.esa.complex.MemoryBasedESA

6. Word embedding index trained based on Wikipedia (5G): used for edu.illinois.cs.cogcomp.classification.representation.word2vec.DiskBasedWordEmbedding

7. Original word2vec representation in raw format (2.2G): used for edu.illinois.cs.cogcomp.classification.representation.word2vec.MemoryBasedWordEmbedding

8. Word2vec representation for Wikipedia Titles (1.4G): edu.illinois.cs.cogcomp.classification.densification.representation.SparseSimilarityCondensation

Always configure the paths of source files in this file:

conf/configurations.properties

used by this class:

edu.illinois.cs.cogcomp.classification.main.DatalessResourcesConfig (You should always new an instance of this class before running).

Then for general text classification, modify and try 

edu.illinois.cs.cogcomp.classification.main.Pipeline

---------------------------------------------------------------

To replicate the experiments for AAAI14, prepare the data here:

edu.illinois.cs.cogcomp.classification.hierarchy.run.preparedata.newsgroups

and run the classification here:

edu.illinois.cs.cogcomp.classification.hierarchy.run.ml.newsgroups

Citation: On Dataless Hierarchical Text Classification. Y. Song and D. Roth. AAAI. 2014.
---------------------------------------------------------------

To replicate the experiments for NAACL15, test the code here:

edu.illinois.cs.cogcomp.classification.densification.run

Citation: Unsupervised Sparse Vector Densification for Short Text Similarity. Y. Song and D. Roth. NAACL. 2015.
---------------------------------------------------------------

Here is an example script for running a class in the package:

./script/20NGTestDataless.sh
