JActiveRecord
=============

A java-based object-database-binding on top of JDBC inspired by Rails ActiveRecord.

Requirements
------
This library requires **Java 8**, **JDBC** and any JDBC DB-driver with a configured database.
Additionally, **JSR 305: Annotations for Software Defect Detection in Java** is required (in version *2.0.3* or higher) for code-quality improvement.
It can be obtained via the [Google Code Project](https://code.google.com/p/jsr-305/) 
or [the Maven Repo](http://search.maven.org/#search|gav|1|g%3A%22com.google.code.findbugs%22%20AND%20a%3A%22jsr305%22).

This library is based upon Java 8 due to its use of *java.util.stream.Stream* for record retrieval.

Testing
------
The test cases use **junit 4.10** and are executed with **hsqldb** *(version 2.3.1)*, **sqlite-jdbc** *(version 3.8.7)*,
**mysql-connector-java** *(version 5.1.23)* and soon with **postgresql** *(version 9.2-1002)* which are all available over [Maven](http://search.maven.org/).

The tests are run at regular intervals and also with **JaCoCoverage** to make sure, all essential code is being tested.

Usage
------
**Record**: the java-object which represents a row in the underlying database, a subtype (class or interface) of *de.doe300.activerecord.record.ActiveRecord*.

**RecordBase**: the base-class for all Records of the same type, equivalent to one table in the database. This class also provides methods to retrieve or find records for this type.

**RecordCore**: the core-class managing the RecordBases and the JDBC connection.

**RecordStore**: the wrapper for the database-connection. JDBC-based RecordStore is provided by this library. Additional RecordStores can be added easily.


There are 2 ways to use JActiveRecord which differ only in the type of the record:

- Use an *Interface* (which extends *ActiveRecord*) as record-type. Instances of this Interface will be automatically created and maintained via Java's Proxy-API.
- Use a *plain-old-java-object* (POJO) to be managed by the corresponding *RecordBase*. 
The class **must** implement *ActiveRecord* and provide a public constructor accepting the record's ID (*Integer*) and the *RecordBase* and **should** not be instantiated outside of JActiveRecord to prevent confusion.
This version of *ActiveRecord* supports class hierarchy via the *Single Table Inheritance* principle (all objects of the class and its subclasses are stored in the same table).
Thus the subclasses can't introduce new persistent attributes but re-use the attributes defined by the super-class and override its methods.


For an example on how to use it, see the test-cases, especially *TestActiveRecordSyntax* and the *TestInterface* and *TestPOJO*.
