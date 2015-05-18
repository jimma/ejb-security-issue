# TO reprodcue this security issue:
1. download wildfly9 snapshot from https://www.dropbox.com/s/67cj5csww25ggpu/wildfly-9.0.0.CR2-SNAPSHOT.zip?dl=0
   and there is jaxws-samples-wsse-policy-username-jaas-ejb-digest.jar is included under standalone/deployments.
2. start wildfly server with standalone.sh
3. git clonet this repo and run mvn test.

If the concurrent number in this test UsernameAuthorizationDigestEjbTestCase is decreased to 5, this test is passed. 
