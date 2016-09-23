# Java ILP master repository [![gitter][gitter-image]][gitter-url] [![CI][CI-image]][CI-url] 

[gitter-image]: https://badges.gitter.im/interledger/java.svg
[gitter-url]: https://gitter.im/interledger/java

[CI-image]: https://travis-ci.org/everis-innolab/java-ilp-master.svg?branch=master
[CI-url]: https://travis-ci.org/everis-innolab/java-ilp-master

> This project serves as an umbrella for all java-related projects, ensuring all quality rules (eg: checkstyle) and common usages applies.


## Usage

### Step 1: Clone repo

``` sh
git clone https://github.com/interledger/java-ilp-master

cd java-ilp-master
```
### Step 2: init and get all submodules up-to-date

``` 
git submodule init
git submodule update
git submodule foreach git pull origin master
```

Note: The build.gradle / pom.xml expect a directory layout similar to:

```
 .../java-ilp-master:
     +- /java-crypto-conditions
     +- /java-ilp-common
     +- /java-ilp-common-api
     +- /java-ilp-core
     +- /java-ilp-ledger-api
     +- /java-ilp-ledger-simple
```


### Step 3: Install

Either use gradle:
```
    $ gradle install
```
or maven:
```
    $ gradle writePom # <- Optional if poms are not up-to-date with gradle (someone forgot to update/commit poms)
    $ mvn install
```
Note: executing gradle writePom in java-ilp-master will automatically update all poms in child projects.

On every change to [gradle.build](gradle.build) don't forget to execute the *writePom* task.

### Step 4: Execute java-ilp-ledger 
With maven: (TODO: gradle execution and manual execution pending)
```
     $ mvn install # (if not yet done)
     $ cd java-ilp-ledger-api  
     $ ./mvn_launch_server.sh
```

#### Gradle:
required gradle version: 3.1

``` 
gradle clean install check

```

#### Maven: 
``` 
mvn clean install checkstyle:check

```

## Contributors

Any contribution is very much appreciated! [![gitter][gitter-image]][gitter-url]

## License

This code is released under the Apache 2.0 License. Please see [LICENSE](LICENSE) for the full text.
