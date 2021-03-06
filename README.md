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

And also (only for the first deployment, not needed for minor code changes, only needed if you add new dependencies into the pom.xml):

mvn dependency:copy-dependencies


### To integrate into Pepper:

In the file pepper/conf/pepper.properties add the path to the target directory of the project (and after adding new dependencies to pom.xml also to the dependency subdirectory of the target directory) into pepper.dropin.paths

Start Pepper.

Pepper documentation: https://korpling.github.io/pepper/doc/tutorial.html


### Tip:

If I have an error message when launching Pepper:

org.osgi.framework.BundleException: The bundle "CoreNLPPepper_1.0.0.SNAPSHOT [2]" could not be resolved. Reason: Missing Constraint: Require-Capability: osgi.extender; filter="(&(osgi.extender=osgi.component)(version>=1.3.0)(!(version>=2.0.0)))",

I have to remove the following paragraph from META-INF/MANIFEST.MF in the SNAPSHOT.jar in the target directory in CoreNLPPepper:

Require-Capability: osgi.ee; filter="(&(..."

(And maybe also from 1 of the .jars in the dependency subdirectory of the target directory, if you have the corresponding error message.)


If you can't save the modified file in the archive, maybe it's due to absence of free space on you computer, because maven took all of it. In that case you have to do :

mvn dependency:purge-local-repository -DactTransitively=false -DreResolve=false --fail-at-end

Even better: delete the content of the .m2 folder.


### Out of memory:

If you ask for named entity recognition, you are likely to run into java.lang.OutOfMemoryError: GC overhead limit exceeded, because Core NLP consumes a lot of memory. 


### What it is capable of doing now:

It does tokenization, sentence splitting, POS-tagging, lemmatization and syntactic parsing for English and German.