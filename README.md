# SNMP
A simple SNMP-Scanner based on the tnm4j-library.
To run the scanner on your computer, download the executable jar file from the latest release and run the following 
command in the folder, where you downloaded it: ```java -jar [JAR-NAME]```.  
The jar has to be executed using Java 8. To verify whether you are running Java 8, run the command ```java -version```. 
The first line of the output should look similar to this: ```java version "1.8.0_181"```.  
Alternatively, you can build the project yourself, you will need to have JDK 1.8.0 installed though. For additional 
instructions refer to the following section. 

## Build the project yourself
To build the Scanner on your own, import the project into your favourite IDE.
For this example, I will use IntelliJ IDEA (Community Version). The Community Version can be downloaded for free from [here](https://www.jetbrains.com/idea/download).
1. Open IntelliJ IDEA.
2. Navigate to File > New > Project from Version Control... > Git  
Here enter the following link: https://github.com/Maximilian-Mitterrutzner/SNMP.git and confirm with "Clone".
3. Now Right-Click on the Projekt Name and navigate to "Add Frameworks Support..." and select the checkBox with Maven. Confirm with OK.
4. Under Project Structure > Project Settings > Project > Project SDK select "1.8.0_181" and set the project language level to "SDK default".
5. In the last step go to Add Configuration... > + > Application and select "com.mitmax.Main" as the Main class.  
Optionally you can set the name to Main too. Confirm with OK.
6. That's it. Now just press on the green Play button, and the application should start.