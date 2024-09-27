# Executes Command Line from Java

[![EO principles respected here](https://www.elegantobjects.org/badge.svg)](https://www.elegantobjects.org)
[![DevOps By Rultor.com](http://www.rultor.com/b/yegor256/jaxec)](http://www.rultor.com/p/yegor256/jaxec)
[![We recommend IntelliJ IDEA](https://www.elegantobjects.org/intellij-idea.svg)](https://www.jetbrains.com/idea/)

[![mvn](https://github.com/yegor256/jaxec/actions/workflows/mvn.yml/badge.svg)](https://github.com/yegor256/jaxec/actions/workflows/mvn.yml)
[![PDD status](http://www.0pdd.com/svg?name=yegor256/jaxec)](http://www.0pdd.com/p?name=yegor256/jaxec)
[![Maven Central](https://img.shields.io/maven-central/v/com.yegor256/jaxec.svg)](https://maven-badges.herokuapp.com/maven-central/com.yegor256/jaxec)
[![Javadoc](http://www.javadoc.io/badge/com.yegor256/jaxec.svg)](http://www.javadoc.io/doc/com.yegor256/jaxec)
[![codecov](https://codecov.io/gh/yegor256/jaxec/branch/master/graph/badge.svg)](https://codecov.io/gh/yegor256/jaxec)
[![Hits-of-Code](https://hitsofcode.com/github/yegor256/jaxec)](https://hitsofcode.com/view/github/yegor256/jaxec)
[![License](https://img.shields.io/badge/license-MIT-green.svg)](https://github.com/yegor256/jaxec/blob/master/LICENSE.txt)

It's a simple executor of a shell command from Java. It is essentially
a wrapper around [`Runtime.exec()`][exec], with a fluent interface.

First, you add this to your `pom.xml`:

```xml
<dependency>
  <groupId>com.yegor256</groupId>
  <artifactId>jaxec</artifactId>
  <version>0.3.1</version>
</dependency>
```

Then, you use it like this:

```java
import com.yegor256.Jaxec;
String stdout = new Jaxec("ls", "-al")
  .with("/tmp") // append argument to the command
  .withHome("/home/me") // run it in this directory
  .withRedirect(false) // don't redirect STDERR to STDOUT
  .withCheck(false) // don't throw if the exit code is not-zero
  .withStdin("Hello, world!") // send this text to the STDIN of the command
  .exec();
```

If exit code is not equal to zero, a runtime exception
will be thrown by the `exec()` method. You can also use
`unsafeExec()`, which throws checked exception `IOException`.

The stdout and stderr of the command are both sent to Slf4j logging
facility `com.jcabi.log.VerboseProcess`. The level for stdout
is `DEBUG`, while the level for stderr is `WARN`.

## How to Contribute

Fork repository, make changes, send us a
[pull request](https://www.yegor256.com/2014/04/15/github-guidelines.html).
We will review your changes and apply them to the `master` branch shortly,
provided they don't violate our quality standards. To avoid frustration,
before sending us your pull request please run full Maven build:

```bash
mvn clean install -Pqulice
```

You will need Maven 3.3+ and Java 8+.

[exec]: https://docs.oracle.com/javase/8/docs/api/java/lang/Runtime.html#exec-java.lang.String-
