all: classes jar

trm:
	java -jar Trm.jar r $(SRC)

jar:
	jar cfev Trm.jar Trm *.class

classes:
	javac *.java

clean:
	rm -rf *.class *.jar
