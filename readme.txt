#===========================================================
#	Setup in Eclipse
#===========================================================
- Get Eclipse (tested with v4.3, Java Developers edition)
- Import the DocFetcher folder into your Eclipse workspace
- In Eclipse Preferences > Java > Build Path > User Libraries, create a
  User Library named 'SWT' which points to the right SWT jar for your
  platform. The SWT jars can be found in lib/swt. Optionally, the same
  "SWT" user library can also be used for overriding other 3rd party libs
  (for example, a local build of java-libpst-0.9.4-SNAPSHOT.jar).
- Main class: net.sourceforge.docfetcher.gui.Application
- Required VM arguments
	Windows:
		-Djava.library.path="lib/chm4j;lib/jnotify;lib/jintellitype"
	Linux:
		-Djava.library.path="lib/chm4j:lib/jnotify:lib/jxgrabkey"
	Mac OS X:
		-Djava.library.path="lib/jnotify"
		-XstartOnFirstThread
- Optional VM argument: -enableassertions
- In the Run configuration, add the folder "dist/lang" to the classpath.


#===========================================================
#	Building DocFetcher from the console
#===========================================================
- Requirements: Python and JDK 7.0+
- The bin folder of your JDK installation containing the various JDK binaries
  (javac, java, jar) has to be on your PATH variable. See:
  https://www.java.com/en/download/help/path.xml
- current-version.txt:
	- this file contains the version number used by all build scripts below
	- this file must not contain any extra whitespace or newlines
- build.py:
	- the main build file that builds DocFetcher
	- output is in the "build" folder
- build-win-installer.nsi
	- NSIS script for building the Windows installer
	- requires NSIS and must be run on Windows
	- requires NSIS plugins in dev/nsis-dependencies;
	  see installation instructions at the top of the .nsi file
	- must run build.py first before running this
	- output is in the "build" folder
- build-man.py:
	- recreates the manual
	- output is in dist/help
- build-website.py:
	- recreates the website
	- output is in dist/website
- build-dmg.sh:
	- builds a Mac OS X disk image
	- must run build.py first
	- must be run on Linux
	- requires program mkfs.hfsplus (try package hfsprogs on Ubuntu)
	- output is in the "build" folder
- build-daemon.xml:
	- Ant file for building the DocFetcher daemon
	- probably stopped working a long time ago
- deploy-website.sh:
	- deploys the website to the project webspace on SourceForge.net
	- will automatically run build-website.py
	- must specify SourceForge.net user name and password


#===========================================================
#	The DocFetcher Launchers
#===========================================================
The DocFetcher launchers for all platforms can be found under dist/launchers.
The DocFetcher.exe launchers in that folder have been created with Launch4J,
according to the settings in dev/launch4j-config.txt, and using the icon file
dev/DocFetcher.ico.
