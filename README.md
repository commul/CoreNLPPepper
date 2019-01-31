# CoreNLPPepper


## Stanford Core NLP integrated into a Pepper module (into the pom.xml and into the manipulator)

### Make sure the version of Pepper indicated in the pom.xml in CoreNLPPepper is the same as the version of Pepper you have on  your computer:

```
<parent>
	<groupId>org.corpus-tools</groupId>
	<artifactId>pepper-parentModule</artifactId>
	<version>3.1.0</version>
</parent>
```

### To compile:

In the CoreNLPPepper project folder execute:

mvn clean install assembly:single

(This may take quite some time.)

And maybe also: mvn dependency:copy-dependencies (not sure)


### To integrate into Pepper:

In the file pepper/conf/pepper.properties add the path to the target directory of the project (and maybe also to the dependency subdirectory of the target directory, I'm not sure) into pepper.dropin.paths

Start Pepper.


### Tip:

If I have an error message when launching Pepper, I have to remove the following paragraph from MANIFEST.MF in the SNAPSHOT.jar in the target directory in CoreNLPPepper:

Require-Capability: osgi.ee; filter="(&(..."

(and maybe also from 1 of the .jars in the dependency subdirectory of the target directory)
