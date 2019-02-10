mvn compile
MoreLib=$(echo target/dependency/*.jar | tr ' ' ':')
CLASSPATH=".:target/classes:$MoreLib:$dataDir:/home/hpeng7/gurobi501/linux64/lib/gurobi.jar"
nice java -Xmx5g -cp ${CLASSPATH} edu.illinois.cs.cogcomp.classification.representation.indexer.simple.WikipediaIndexing