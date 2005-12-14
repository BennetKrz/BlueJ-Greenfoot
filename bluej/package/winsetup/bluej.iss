; Script generated by the Inno Setup Script Wizard.
; SEE THE DOCUMENTATION FOR DETAILS ON CREATING INNO SETUP SCRIPT FILES!

[Setup]
AppName=BlueJ
AppVerName=BlueJ 2.1.1
AppPublisher=Deakin University
AppPublisherURL=http://www.bluej.org
AppSupportURL=http://www.bluej.org
AppUpdatesURL=http://www.bluej.org
UninstallFilesDir={app}\uninst
DefaultDirName={sd}\BlueJ
DefaultGroupName=BlueJ
Compression=bzip/9
OutputBaseFilename=bluejsetup
OutputDir=.

[Messages]
SetupWindowTitle=BlueJ Installer
SetupAppTitle=BlueJ Installer
SetupLdrStartupMessage=This installer will install %1. Do you wish to continue?
WelcomeLabel1=Welcome to the [name] Installer
WelcomeLabel2=This installer will install [name/ver] on your computer.%n%nIt is strongly recommended that you close all other applications you have running before continuing. This will help prevent any conflicts during the installation process.

[Tasks]
Name: "desktopicon"; Description: "Create a &desktop icon"; GroupDescription: "Additional icons:"; MinVersion: 4,4

[Icons]
Name: "{group}\BlueJ"; Filename: "{app}\bluej.exe"; WorkingDir: "{app}"
Name: "{userdesktop}\BlueJ"; Filename: "{app}\bluej.exe"; WorkingDir: "{app}"; Tasks: desktopicon
Name: "{group}\Select VM"; Filename: "{app}\bluej.exe"; WorkingDir: "{app}"; Parameters: "/select"; IconIndex: 1
Name: "{app}\Select VM"; Filename: "{app}\bluej.exe"; WorkingDir: "{app}"; Parameters: "/select"; IconIndex: 1

[InstallDelete]
Type: files; Name: "{app}\lib\extensions\submission.jar"

[Files]
Source: "..\install_tmp\*.*"; DestDir: "{app}"; CopyMode: alwaysoverwrite; Flags: recursesubdirs
Source: "..\winlaunch\vmselect.exe"; DestDir: "{app}"; DestName: "bluej.exe"
Source: "..\winlaunch\README.TXT"; DestDir: "{app}"

[Run]
Filename: "{app}\README.TXT"; Description: "View the README file"; Flags: postinstall shellexec skipifsilent
Filename: "{app}\bluej.exe"; WorkingDir: "{app}"; Parameters: "/select"; Description: "Launch BlueJ"; Flags: postinstall nowait skipifsilent unchecked

