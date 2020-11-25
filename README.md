# SNMP
A simple SNMP-Scanner based on the tnm4j-library.

## Installation
1. Create an empty project with maven.
2. Add the following lines to the POM.xml. This will import tnm4j into your project and set the java language level to 8 (so you can use lambdas):
```xml
<properties>
    <maven.compiler.source>1.8</maven.compiler.source>
    <maven.compiler.target>1.8</maven.compiler.target>
</properties>

<dependencies>
    <dependency>
        <groupId>org.soulwing.snmp</groupId>
        <artifactId>tnm4j</artifactId>
        <version>1.0.10</version>
    </dependency>
</dependencies>
```
3. Run ```mvn install``` in the console.

## Simple Test
Add the following code to your Main.java (replace ```[IP-ADDRESS]``` with the IP-Address of a device with an SNMP-Client):
```java
SimpleSnmpV2cTarget target = new SimpleSnmpV2cTarget();
target.setAddress("[IP-ADDRESS]");
target.setCommunity("public");

Mib mib = MibFactory.getInstance().newMib();
mib.load("SNMPv2-MIB");
mib.load("IF-MIB");

try (SnmpContext context = SnmpFactory.getInstance().newContext(target)) {
    VarbindCollection varbinds = context.get("1.3.6.1.2.1.1.3.0").get();
    System.out.println(varbinds.get(0).getName() + ": " + varbinds.get(0).toString());
} catch (Exception e) {
    e.printStackTrace();
}
```
If everything goes right, you should now see something like this in the output:
```
1.3.6.1.2.1.1.3.0: 488087483
```
