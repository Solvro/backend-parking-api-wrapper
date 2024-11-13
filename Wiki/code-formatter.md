# Configuration Instruction

## Prerequisites

1. Make sure that you have maven installed on your machine. If not, you can download it (you need BINARY zip archive) from [here](https://maven.apache.org/download.cgi).
2. Set the `MAVEN_HOME` environment variable to the path of your maven installation. You can find information about how to do it [here](https://phoenixnap.com/kb/install-maven-windows).

## GitHooks configuration

1. Execute maven command `mvn clean install` in the root directory of the project.
2. Now, when you commit your changes, the pre-commit hook will apply code formatter to automatically fix your code style.

## Code Formatter usage

1. If you want to format your code manually, you can execute maven command `mvn spotless:apply` in the root directory of the project. It will format your code according to the code style standards.
2. Now code formatter supports Java, POM XML and Markdown files.

