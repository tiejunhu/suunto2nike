Suunto to Nike Plus Uploader
===========================


What is this
------------

This app will convert Suunto's move into Nike Plus' run. Then upload the converted data to your Nike Plus account.

How does it work
------------

It has to be run from the command line, then it will scan and upload XML files from MovesLink and MovesLink2 data folder.

To upload Moveslink data:

    java -jar suunto2nike-<version>-jar-with-dependencies.jar 1

To upload Moveslink2 data:

    java -jar suunto2nike-<version>-jar-with-dependencies.jar 2

If you run without specify any parameters, it will try Moveslink data folder then Moveslink2 data folder, uploads any XML it can find:

    java -jar suunto2nike-<version>-jar-with-dependencies.jar

When uploading Moveslink data, it will scan automatically all the XML files begin with Quest_, move all dupliate ones into a "Duplicates" folder, move all ones contain no moves into a "NoMoves" folder, move all new moves into a "Pending" folder. Then it login into your Nike Plus account and upload all pending XML files into Nike Plus and move the files into the "Uploaded" folder.

When uploading Moveslinks2 data, it will scan automatically all the XML files begin with log-, move all the XML files that are not "Running" or "Treadmill" into "NotRun" folder, move all uploaded XML files into "Uploaded" folder.

How should I use it
-------------------

Normally you just sync your watch with Moveslink/Moveslink2 application, then run this app. It will automatically scan all new moves and upload them to your Nike Plus account.

Where to get it
---------------

[here](https://github.com/oldhu/suunto2nike/releases)

Supported devices
-----------------

Currently tested with Quest and Ambit 2.

Supported OS
------------

Windows and Mac

Report Issue/Request Features
-----------------------------

Use github's issue

Thanks
------

I use Nike Plus related code from [tcx2nikeplus](https://github.com/angusws/tcx2nikeplus). Thank you.