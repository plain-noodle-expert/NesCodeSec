# NesCodeSec: Next Edit Prediction Code Security Evaluation

## Development Environment for Java
### IDE
We use the [Jetbrains IDEA Ultimate](https://www.jetbrains.com/idea/download/?section=windows) as our development IDE for Java.

University student can enjoy the education plan: https://www.jetbrains.com/community/education/#students

If you prefer remote development on a Linux server, use [JetBrains Gateway](https://www.jetbrains.com/zh-cn/remote-development/gateway/). Here is the document: https://www.jetbrains.com/help/phpstorm/remote-development-a.html#gateway

### SDK
We use [sdkman](https://sdkman.io/) to manage the Software Development Kits we need.

Install sdkman:
```angular2html
curl -s "https://get.sdkman.io" | bash
```

List SDKs:
```angular2html
sdk list java
```

Install a Java SDK:
```angular2html
sdk install java 17.0.0-tem
```

Set a Java SDK as the default SDK:
```angular2html
sdk default java 17.0.8-tem
```

Temporary use a Java SDK (Only for current session):
```angular2html
sdk use java 17.0.8-tem
```

Check current java SDK version:
```angular2html
sdk current java
```

### Maven
[Maven](https://maven.apache.org/) is a build tool for Java projects. Using a project object model (POM), Maven manages a project's compilation, testing, and documentation. With Maven, you don't need to build projects by yourself.

Install:
```angular2html
sdk install maven
```

Verify:
```angular2html
mvn -v
```