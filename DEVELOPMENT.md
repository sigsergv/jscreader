Overview
========

Project uses Gradle and Gradle wrapper to build.

Commands
========

Build project:

    ./gradlew build

Run project:

    ./gradlew run

Create binary distribution (still requires external JRE/JDK):

    ./gradlew distZip

Create binary image:

    ./gradlew jlink

To reduce size of created image (linux):
    
    find build/image -name '*.so' -exec strip -p --strip-unneeded {} \;


APIs and manuals
================

* [JDK 11 API](https://docs.oracle.com/en/java/javase/11/docs/api/index.html)
* [javax.smartcardio](https://docs.oracle.com/en/java/javase/11/docs/api/java.smartcardio/javax/smartcardio/package-summary.html)
* [JavaFX](https://openjfx.io/javadoc/11/)


Additional reference information
================================

* [Complete list of Application Identifiers (AID)](https://www.eftlab.com/index.php/site-map/knowledge-base/211-emv-aid-rid-pix)