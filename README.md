Overview
========

Mongo Java is a sample project for using Mongo in a sharded manner with the use of the Morphia library.
This project will compile and run but is only meant to showcase 2 different ways in implementing sharding.

## README Contents

- [Infrastructure](#infrastructure)
- [Important Classes](#imporant)
- [Support & Help](#support)

<a name="infrastructure" />
Running on Infrastructure
=========================

Make sure you have at least 1 Mongos, 2 Mongod, 1 config server installed on your system where the Mongos uses the default port. You can get the latest version at
http://www.mongodb.org/downloads

Once you have everything configured and this project is checked out you you will execute the following command:

    mvn clean package tomcat:run

<a name="important" />
Important Classes
=================

  - DatastoreShadImpl
    - This class is extending the Morphia DatastoreImpl class in order to implement an update mechanism that handles a new annotation of @ShardKey.
    - Ideally someone would complete all the other methods such as find, and actually contribute it to the Morphia project but due to lack of time, this hasn't been done.
  - User
    - This class contains a method getObjectIdShardKey.  This is used to generate the key and is used as a sample for the second method of sharding.  There is no need for annotation for this method but would require this method to be part of an abstract class that is extended if there were more than one object that is sharded.
      
<a name="support" />
Support & Help
==============

This project is provided on an `as is` basis buy you may find me at http://www.linkedin.com/in/kovshilovsky for questions.
 


