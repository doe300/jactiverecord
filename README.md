JActiveRecord
=============

A java-based object-database-binding on top of JDBC inspired by Rails ActiveRecord.

Requirements
------
This library uses no other requirement than **Java 8**, **JDBC** and any JDBC DB-driver with a configured database.

This library is based upon Java 8 due to its use of *java.util.stream.Stream* for record retrieval.

Testing
------
The test cases use **junit 4.10** and **hsqldb 2.3.1**.

The tests are run at regular intervals and also with **JaCoCoverage** to make sure, all essential code is being tested.

Usage
------
**Record**: the java-object which represents a row in the underlying database, a subtype (class or interface) of *de.doe300.activerecord.record.ActiveRecord*.

**RecordBase**: the base-class for all Records of the same type, equivalent to one table in the database. This class also provides methods to retrieve or find records for this type.

**RecordCore**: the core-class managing the RecordBases and the JDBC connection.

**RecordStore**: the wrapper for the database-connection. JDBC-based RecordStore is provided by this library. Additional RecordStores can be added easily.


There are 2 ways to use JActiveRecord only which differ in the type of the record:

- Use an *Interface* (which extends *ActiveRecord*) as record-type. Instances of this Interface will be automatically created and maintained via Java's Proxy-API.
- Use a *plain-old-java-object* (POJO) to be managed by the corresponding *RecordBase*. The class **must** implement *ActiveRecord* and provide a public constructor accepting the record's ID (*Integer*) and the *RecordBase* and **should** not be instantiated outside of JActiveRecord to prevent confusion.


For an example on how to use it, see the test-cases, especially *TestActiveRecordSyntax* and the *TestInterface* and *TestPOJO*.
