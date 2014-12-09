cntz-document-notification
==========================

Alfresco Module to get Email notification on update of content

Installation

Install the following JAR files:
- cntz-document-notification-<version>-shared.jar in tomcat/shared/lib
- cntz-document-notification-<version>-src.jar in tomcat/webapps/alfresco/WEB-INF/lib

Or you can build amps to include the files.

ant

The script will build an AMP file and a JAR file in the build/dist directory within your project, which can then be installed as per the instructions in Installation, above.

Once you have deployed the JAR file you will need to restart Tomcat so that the additional resources are picked up.

Usage

Add the used email template into your running Alfresco instance:
cntz-document-notification\src\resources\alfresco\data\Data Dictionary\Email Templates\Notify Email Templates\notify_user_email.ftl needs to be placed in Data Dictionary\Email Templates\Notify Email Templates\

A custom Notification Action will be visible on Content of type Document.
The current user is added in a default Person association Aspect.

The behavior listens to policy onUpdateProperties and checks if the Version Label property differs and then sends emails.
