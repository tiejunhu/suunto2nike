Suunto to NikePlus Uploader
===========================


What is this
------------

This app will convert Suunto's move into Nike Plus' run. Then upload the converted data to your Nike Plus account.

How it works
------------

It has to be run from the command line, it can upload the "move" you downloaded from Suunto's movescount website as Excel file. It can also upload the XML files under your Moveslink data folder.

To upload an Excel file:

    java -jar suunto2nike-0.0.2-jar-with-dependencies.jar <path to your excel file>

To upload Moveslink data:

    java -jar suunto2nike-0.0.2-jar-with-dependencies.jar

It will scan automatically all the XML files begin with Quest_, move all dupliate ones into a "Duplicates" folder, move all ones contain no moves into a "NoMoves" folder, move all new moves into a "Pending" folder. Then it login into your Nike Plus account and upload all pending XML files into Nike Plus and move the files into the "Uploaded" folder.

Supported devices
-----------------

Currently Quest with HR and foot pod only. Because that's what I have.