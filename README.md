# CoreNLPPepper


## Stanford Core NLP integrated into a Pepper module (into the pom.xml and into the manipulator)


### To compile:

In the CoreNLPPepper project folder execute:

mvn clean install assembly:single

And maybe also: mvn dependency:copy-dependencies (not sure)


### To integrate into Pepper:

In the file pepper/conf/pepper.properties add the path to the target directory of the project (and maybe also to the dependency subdirectory of the target directory, I'm not sure) into pepper.dropin.paths

Start Pepper.


### Tip:

If I have an error message when launching Pepper, I have to remove the following paragraph from MANIFEST.MF in the SNAPSHOT.jar in the target directory:

Require-Capability: osgi.ee; filter="(&(..."

(and maybe also from 1 of the .jars in the dependency subdirectory of the target directory)
