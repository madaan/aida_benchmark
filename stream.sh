#sg
for i in `cat data/listOfAnnotatedFiles`
do
echo data/crawledDocs/$i
fileName=data/crawledDocs/$i
 #/usr/lib/jvm/java-7-openjdk-amd64/bin/java -agentlib:jdwp=transport=dt_socket,suspend=y,address=localhost:39383 -Dfile.encoding=UTF-8 -classpath /home/aman/workspace/aida_benchmark/bin:/home/aman/aida-master/target/aida-2.0.4-jar-with-dependencies.jar:/home/aman/workspace/aida_benchmark/lib/commons-math3-3.2.jar AidaAnnotator $fileName
 /usr/lib/jvm/java-7-openjdk-amd64/bin/java  -Dfile.encoding=UTF-8 -classpath /home/aman/workspace/aida_benchmark/bin:/home/aman/aida-master/target/aida-2.0.4-jar-with-dependencies.jar:/home/aman/workspace/aida_benchmark/lib/commons-math3-3.2.jar AidaAnnotator $fileName
done
