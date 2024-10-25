Um die Anwendung als Service zu startet wird die WinSW.exe benötigt.
Download und Infos: https://github.com/winsw/winsw?tab=readme-ov-file

Danach schreibt man die XML Datei. Der Name ist frei wählbar. Als beispiel smartcardservice.xml.

In dieser gibt man folgende Dinge an:

Wie der Server unter Windows erreichbar ist (id)
Den Namen des Service (name)
Eine Beschreibung was der Service macht (description)
Den Pfad zur Java Anwendung (executable)
Wie die Jar Datei heißt und das es sich um eine jar Datei handelt (arguments)
Wo das Arbeitsverzeichnis ist, in dem die Jar Datei liegt (workingdirectory)
Welchen Starttyp der Dienst haben soll (startmode)
Was passieren soll, wenn der Serive absürzt (onfailure)


Nach dem die XML Datei geschrieben worden ist, bennent man die runtergelandene WinSW.exe so um, wie auch die XML Datei heißt.
Danach muss man den Service installieren mit: smartcardservice.exe install
Nach der Installation kann man den Service mit smartcardservice.exe start