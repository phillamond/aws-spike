aws-spike
=========

A design challenge for a music artist email notification system to subscribers.

See https://github.com/phillamond/aws-spike/wiki/Technical-Design---Approach for more details

To build from project root:

```
mvn clean install

To run the unit test specs:

```
mvn clean test -Dtest=*Spec

To run the acceptance tests (requires integration with already set up AWS managed services):

```
cd cucumber
mvn clean test
