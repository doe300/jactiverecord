JActiveRecord
=============

[![BuildStatus](https://travis-ci.org/doe300/jactiverecord.svg)](https://travis-ci.org/doe300/jactiverecord)
[![GitHub license](https://img.shields.io/github/license/doe300/jactiverecord.svg)](https://github.com/doe300/jactiverecord/blob/master/LICENSE)
[![Release](https://img.shields.io/github/tag/doe300/jactiverecord.svg)](https://github.com/doe300/jactiverecord/releases/latest)
[![Dependency Status](https://www.versioneye.com/user/projects/570638fbfcd19a004543fceb/badge.svg?style=flat)](https://www.versioneye.com/user/projects/570638fbfcd19a004543fceb)

A java-based object-database-binding on top of JDBC inspired by Rails ActiveRecord.

Requirements
------
This library requires **Java 8**, **JDBC** and any JDBC DB-driver with a configured database.
Additionally, **JSR 305: Annotations for Software Defect Detection in Java** is required for code-quality improvements.
It can be obtained via the [Maven Repo](http://search.maven.org/#search|gav|1|g%3A%22com.google.code.findbugs%22%20AND%20a%3A%22jsr305%22).

This library is based upon Java 8 due to its use of *java.util.stream.Stream* for record retrieval.

Testing
------
The test cases use **junit 4** and are executed with **hsqldb**, **sqlite-jdbc**,
**mysql-connector-java** and with **postgresql** which are all automatically managed in their latest version via [Maven](http://search.maven.org/).

The tests are run at regular intervals and also with **JaCoCoverage** to make sure, all essential code is being tested.

Usage
------
**Record**: the java-object which represents a row in the underlying database, a subtype (class or interface) of *de.doe300.activerecord.record.ActiveRecord*.

**RecordBase**: the base-class for all Records of the same type, equivalent to one table in the database. This class also provides methods to retrieve or find records for this type.

**RecordCore**: the core-class managing the RecordBases and the JDBC connection.

**RecordStore**: the wrapper for the database-connection. JDBC-based RecordStore is provided by this library. Additional RecordStores can be added easily.


There are 3 ways to use JActiveRecord which differ only in the type of the record, see [RecordTypes](https://github.com/doe300/jactiverecord/wiki/RecordTypes):

- Use an *Interface* (which extends *ActiveRecord*) as record-type. Instances of this Interface will be automatically created and maintained via Java's Proxy-API.
- Use a *plain-old-java-object* (POJO) to be managed by the corresponding *RecordBase*. 
The class **must** implement *ActiveRecord* and provide a public constructor accepting the record's ID (*Integer*) and the *RecordBase* and **should** not be instantiated outside of JActiveRecord to prevent confusion.
This version of *ActiveRecord* supports class hierarchy via the *Single Table Inheritance* principle (all objects of the class and its subclasses are stored in the same table) by annotating with *SingleTableInheritance*.
Thus the subclasses can't introduce new persistent attributes but re-use the attributes defined by the super-class and override its methods.
- Use an *Interface* or a *plain-old-java-object* and add attributes via *AddAttribute* and *AddAttributes*. 
This will run an *AnnotationProcessor* generating an interface in the same package as the originating type containing the getter- and setter-methods for the specified attributes, see [Generators](https://github.com/doe300/jactiverecord/wiki/Generators).


For an example on how to use it, see the test-cases, especially *TestActiveRecordSyntax* and the *TestInterface* and *TestPOJO*
or [Wiki: RecordType](https://github.com/doe300/jactiverecord/wiki/RecordTypes).

Extensions
---------
For a list of additional features and extensions, see [doe300/jactiverecord-extensions](https://github.com/doe300/jactiverecord-extensions).
