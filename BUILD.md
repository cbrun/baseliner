

## **Build the baseliner update-site**

If you have Java and Maven, you can build baseliner.

### ** Building for helios **

#### ** Launching the build **

Go to the parent project : 
`cd ./`

In one command line:

`mvn clean package -Phelios`

To launch the tests :

`mvn clean verify -Phelios`

#### ** Build results **

The build produces an update-site containing all the features of the project in :
`./target/repository/`


#### On the Eclipse Build Servers

If you need to sign the build artifacts, use the profile SIGN, for instance: 

`mvn clean verify -Phelios,SIGN`

### ** Building for juno **

#### ** Launching the build **

Go to the parent project : 
`cd ./`

In one command line:

`mvn clean package -Pjuno`

To launch the tests :

`mvn clean verify -Pjuno`

#### ** Build results **

The build produces an update-site containing all the features of the project in :
`./target/repository/`


#### On the Eclipse Build Servers

If you need to sign the build artifacts, use the profile SIGN, for instance: 

`mvn clean verify -Pjuno,SIGN`

### ** Building for kepler **

#### ** Launching the build **

Go to the parent project : 
`cd ./`

In one command line:

`mvn clean package -Pkepler`

To launch the tests :

`mvn clean verify -Pkepler`

#### ** Build results **

The build produces an update-site containing all the features of the project in :
`./target/repository/`


#### On the Eclipse Build Servers

If you need to sign the build artifacts, use the profile SIGN, for instance: 

`mvn clean verify -Pkepler,SIGN`

### ** Building for luna **

#### ** Launching the build **

Go to the parent project : 
`cd ./`

In one command line:

`mvn clean package -Pluna`

To launch the tests :

`mvn clean verify -Pluna`

#### ** Build results **

The build produces an update-site containing all the features of the project in :
`./target/repository/`


#### On the Eclipse Build Servers

If you need to sign the build artifacts, use the profile SIGN, for instance: 

`mvn clean verify -Pluna,SIGN`


#### Setting up Jenkins ####

Creating the job

`curl --user USER:PASS -H "Content-Type: text/xml" -s --data-binary "@config.xml" "http://JENKINS_ENDPOINT/createItem?name=baseliner--master"`

Updating the job 

`curl --user USER:PASS -H "Content-Type: text/xml" -s --data-binary "@config.xml" "http://JENKINS_ENDPOINT/job/baseliner--master/config.xml`

