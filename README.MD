To deploy codebase to Tomcat:
 - mvn clean install tomcat7:deploy
    or use profile
 - mvn -Pdeploy

add '-X' to enable maven debug mode

Initial setup Tomcat server:
1. Download and extract Tomcat9 (apache-tomcat-9.0.67-windows-x64.zip) in a folder d:\Soft\Tomcat 9.0_MyTomcat9
2. Change a port number in the file \conf\server.xml
3. Setup user groups\roles in \conf\tomcat-users.xml
4. Add system environment variable TOMCAT9=d:\Soft\Tomcat 9.0_MyTomcat9
5. Create folder GADB under Tomcat 9.0_MyTomcat9