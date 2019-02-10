mvn compile
mvn dependency:copy-dependencies
MoreLib=$(echo target/dependency/*.jar | tr ' ' ':')
CLASSPATH=".:target/classes:$MoreLib"
nice java -Xmx5g -cp ${CLASSPATH} edu.illinois.cs.cogcomp.classification.hierarchy.run.ml.newsgroups.ConceptClassificationESAML