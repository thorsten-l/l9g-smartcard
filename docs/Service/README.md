# Windows-Service (de)

Um die Anwendung als Service zu starten wird die `WinSW.exe` (Windows Service Wrapper) benötigt.
Download und Infos: [WinSW](https://github.com/winsw/winsw)

Danach wird die XML Datei erstellt. Der Name ist frei wählbar. Als Beispiel smartcardservice.xml.

In dieser gibt man folgende Dinge an:

- Wie der Server unter Windows erreichbar ist (id)
- Den Namen des Service (name)
- Eine Beschreibung was der Service macht (description)
- Den Pfad zur Java Anwendung (executable)
- Wie die Jar Datei heißt und das es sich um eine jar Datei handelt (arguments)
- Wo das Arbeitsverzeichnis ist, in dem die Jar Datei liegt (workingdirectory)
- Welchen Starttyp der Dienst haben soll (startmode)
- Was passieren soll, wenn der Serive absürzt (onfailure)

Nach dem die XML Datei geschrieben worden ist, bennent man die runtergelandene `WinSW.exe` so um, wie auch die XML Datei heißt.

Nun wird der Service wie folgt installiert: `smartcardservice.exe install`

Nach der Installation kann man den Service mit `smartcardservice.exe start`

---

# Windows service (en)

To start the application as a service, the `WinSW.exe` (Windows Service Wrapper) is required.
Download and information: [WinSW](https://github.com/winsw/winsw)
After that, the XML file is created. The name is arbitrary. As an example, smartcardservice.xml.
In this, you specify the following things:
- How the server can be reached on Windows (id)
- The name of the service (name)
- A description of what the service does (description)
- The path to the Java application (executable)
- What the jar file is called and that it is a jar file (arguments)
- Where the working directory is in which the jar file is located (workingdirectory)
- What start type the service should have (startmode)
- What should happen if the service crashes (onfailure)

After the XML file has been written, rename the downloaded WinSW.exe to the same name as the XML file.

Now the service is installed as follows: `smartcardservice.exe install`
After installation, the service can be started with `smartcardservice.exe start`
