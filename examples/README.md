Examples
========

A few usage examples for better understanding the full functionality of the library.

Versioning
----------
An example on how to automatically update the database on program updates.
Additionally, the **Version** model stores the changelog for each version to be displayed to the user.

In general, to update a table for a record-type, one simply has to call the following code:

	new AutomaticMigration(recordType, removeObsoleteColumns).update(connection);
