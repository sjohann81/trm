all: classes jars

trm:
	java -jar Trm.jar r $(SRC)

jars:
	cp common/*.class .
	cp cli/*.class .
	jar cfev Trm.jar Trm *.class
	rm -f *.class
	cp common/*.class .
	cp gui/*.class .
	jar cfev TrmGUI.jar TrmGUI *.class
	rm -f *.class

classes:
	javac common/*.java cli/*.java
	javac common/*.java gui/*.java
clean:
	rm -rf *.class common/*.class cli/*.class gui/*.class *.jar
