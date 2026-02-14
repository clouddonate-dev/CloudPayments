# CloudPayments - Donate Plugin
A plugin for Minecraft that automatically issues purchases to players after successful payment on the site through the CloudPayments payment system.

## Installation
1. Download the latest version from https://cdonate.ru
2. Place the JAR file into your server's `plugins/` folder
3. Restart the server or run `/plugman load`
4. Configure `config.yml` according to your needs

## Requirements

- **Java 8** or higher
- Spigot 1.12.2+ (or any fork)
- **Store** at https://cdonate.ru

## Building
```
mvn clean package
```

## Using CloudPayments

To use this plugin in your project, first add CloudPayments as a dependency or soft dependency to your plugin.yml file:
```yaml
depend: [ CloudPayments ]
```
Next, add as a dependency to your pom file:

### Repository:
```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```
### Dependency:
```xml
<dependency>
    <groupId>com.github.clouddonate-dev</groupId>
    <artifactId>CloudPayments</artifactId>
    <version>version</version> <!-- Use the latest version -->
    <scope>provided</scope>
</dependency>
```
To work with the plugin API, use:

```
ru.clouddonate.cloudpayments.api.CloudPaymentsApi
```