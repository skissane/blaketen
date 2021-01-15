[Setup]
AppName=BlakeTen
AppId=BlakeTen
AppVersion=1.0
AppVerName=BlakeTen 1.0
AppPublisher=Simon Kissane
AppCopyright=Public Domain, 2015-2021
DisableProgramGroupPage=yes

SourceDir=..\innosetup\BlakeTen
OutputDir=..\..\innosetup
OutputBaseFilename=BlakeTen
DefaultDirName=c:\BlakeTen
DisableDirPage=yes
DisableReadyPage=yes
SetupIconFile=icon.ico
WizardImageFile=logo.bmp

[Files]
Source: "*"; DestDir: "{app}"; Flags: recursesubdirs createallsubdirs nocompression

[Icons]
Name: "{userdesktop}\BlakeTen"; Filename: "{app}\BlakeTen.exe"; IconFilename: "{app}\icon.ico"
