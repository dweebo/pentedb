;NSIS Modern User Interface
;Welcome/Finish Page Example Script
;Written by Joost Verburg

 !define SRC_DIR "C:\projects\pentedb_src"

!define JRE_VERSION "1.6.0"

Var InstallJRE
Var JREPath
Var JRE64bit


;--------------------------------
;Include Modern UI

  !include "MUI2.nsh"
  !include "InstallOptions.nsh"
  
;--------------------------------
;General

  ;Name and file
  Name "Pente Db"
  OutFile "pentedb-install.exe"

  ;Default installation folder
  InstallDir "$PROGRAMFILES\pentedb"

  ;Get installation folder from registry if available
  InstallDirRegKey HKCU "Software\pentedb" ""

  BrandingText " "
  ShowInstDetails show
 
;--------------------------------
;Interface Settings

  !define MUI_ABORTWARNING

;--------------------------------
;Pages
  !define MUI_WELCOMEPAGE_TEXT "The wizard will guide your through the installation of Pente Db$\n$\nClick Next to continue."
  
  !define MUI_ICON "${SRC_DIR}\resources\images\logo.ico"
  
  !insertmacro MUI_PAGE_WELCOME

  !insertmacro MUI_PAGE_LICENSE "${SRC_DIR}\resources\license.txt"
  
; This page checks for JRE. It displays a dialog based on JRE.ini if it needs to install JRE
  ; Otherwise you won't see it.
  Page custom CheckInstalledJRE

  !insertmacro MUI_PAGE_COMPONENTS
  !insertmacro MUI_PAGE_DIRECTORY
  !insertmacro MUI_PAGE_INSTFILES
  
    !define MUI_FINISHPAGE_RUN
    !define MUI_FINISHPAGE_RUN_CHECKED
    !define MUI_FINISHPAGE_RUN_TEXT "Start Pente Db"
    !define MUI_FINISHPAGE_RUN_FUNCTION "LaunchLink"
  !insertmacro MUI_PAGE_FINISH

  !insertmacro MUI_UNPAGE_WELCOME
  !insertmacro MUI_UNPAGE_CONFIRM
  !insertmacro MUI_UNPAGE_INSTFILES
  
  !define MUI_FINISHPAGE_NOAUTOCLOSE
  !insertmacro MUI_UNPAGE_FINISH

;--------------------------------
;Languages

  !insertmacro MUI_LANGUAGE "English"
  
  ;Header
  LangString TEXT_JRE_TITLE ${LANG_ENGLISH} "Java Runtime Environment"
  LangString TEXT_JRE_SUBTITLE ${LANG_ENGLISH} "Installation"
  LangString TEXT_PRODVER_TITLE ${LANG_ENGLISH} "Installed version of Pente Db"
  LangString TEXT_PRODVER_SUBTITLE ${LANG_ENGLISH} "Installation cancelled"
 
 ;--------------------------------
;Reserve Files
 
  ;Only useful for BZIP2 compression
 
  ReserveFile "jre.ini"
!insertmacro MUI_RESERVEFILE_LANGDLL ;Language selection dialog
;old  !insertmacro MUI_RESERVEFILE_INSTALLOPTIONS
 
;--------------------------------
 
;--------------------------------
;Installer Sections

Section "Pente Db core files" pentedb 
  SectionIn RO
  SetOutPath "$INSTDIR"
  AddSize 1800
  
  Push "${JRE_VERSION}"
  Call DetectJRE  
  Pop $0	  ; DetectJRE's return value
  ;MessageBox MB_OK "$0"
  ; runs the java program firstinstall to init db
  StrCpy $0 '"$0" -classpath pentedb.jar;log4j-1.2.8.jar;derby.jar org.pente.gameDatabase.swing.install.FirstInstall'
  ;MessageBox MB_OK "$0"
  
  File ${SRC_DIR}\build\pentedb.exe
  File ${SRC_DIR}\lib\log4j-1.2.8.jar
  File ${SRC_DIR}\lib\derby.jar
  File ${SRC_DIR}\build\pentedb.jar
  File ${SRC_DIR}\lib\swingx-0.9.1.jar
  File ${SRC_DIR}\resources\sample.sgf
  File ${SRC_DIR}\resources\images\splash.png
  File ${SRC_DIR}\resources\log4j.properties
  
  SetOutPath "$INSTDIR\x64"
  File ${SRC_DIR}\mmai64\Ai.dll
  SetOutPath "$INSTDIR"
  File ${SRC_DIR}\mmai\Ai.dll
  File ${SRC_DIR}\mmai\pente.scs
  File ${SRC_DIR}\mmai\pente.tbl
  File ${SRC_DIR}\resources\mmai.sgf
  
  File ${SRC_DIR}\resources\pro\player_slim.txt
  File ${SRC_DIR}\resources\pro\game_site.txt
  File ${SRC_DIR}\resources\pro\game_event.txt
  

  SetOutPath $INSTDIR
  DetailPrint "Database initilization will take a few minutes..."
  nsExec::ExecToLog $0 $1
  ;$1 has error code if any

  Delete $INSTDIR\player_slim.txt
  Delete $INSTDIR\game_site.txt
  Delete $INSTDIR\game_event.txt
  
  ;Allow windows add/remove components uninstall
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\pentedb" "DisplayName" "Pente Db"
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\pentedb" "UninstallString" '"$INSTDIR\uninstall.exe"'
  WriteRegDWORD HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\pentedb" "NoModify" "1"
  WriteRegDWORD HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\pentedb" "NoRepair" "1"
  
  ;Store installation folder
  WriteRegStr HKCU "Software\pentedb" "" $INSTDIR

  ;Create uninstaller
  WriteUninstaller "$INSTDIR\Uninstall.exe"

SectionEnd

Section "Pro-Pente Database" pentepro 

  ;Run java FirstInstall in install directory

  File ${SRC_DIR}\resources\pro\pente_game_pro.txt
  File ${SRC_DIR}\resources\pro\pente_move_pro.txt
 
  ;pente-pro database inserts cause additional file size
  ;but subtract the txt files since they are temporary
  AddSize 90000

  Push "${JRE_VERSION}"
  Call DetectJRE  
  Pop $0	  ; DetectJRE's return value
  StrCpy $0 '"$0" -classpath pentedb.jar;log4j-1.2.8.jar;derby.jar org.pente.gameDatabase.swing.install.FirstInstall pro'
  SetOutPath $INSTDIR
  DetailPrint "Pro-Pente Database Install will take a few minutes..."
  nsExec::ExecToLog $0
  
  Delete $INSTDIR\pente_game_pro.txt
  Delete $INSTDIR\pente_move_pro.txt

SectionEnd

Section "Start menu shortcuts" SecCreateShortcut
  SectionIn 1	; Can be unselected
  CreateDirectory "$SMPROGRAMS\Pente Db"
  CreateShortCut "$SMPROGRAMS\Pente Db\Uninstall.lnk" "$INSTDIR\uninstall.exe" "" "$INSTDIR\uninstall.exe" 0
  CreateShortCut "$SMPROGRAMS\Pente Db\Pente Db.lnk" "$INSTDIR\pentedb.exe" "" "$INSTDIR\pentedb.exe" 0
; Etc
SectionEnd

;--------------------------------
;Descriptions

  ;Language strings
  LangString DESC_pentedb ${LANG_ENGLISH} "The core Pente Db files (required)."
  LangString DESC_pentePro ${LANG_ENGLISH} "A database of pro level Pente games."
  LangString DESC_shortcut ${LANG_ENGLISH} "Start menu shortcuts to Pente Db."

  ;Assign language strings to sections
  !insertmacro MUI_FUNCTION_DESCRIPTION_BEGIN
    !insertmacro MUI_DESCRIPTION_TEXT ${pentedb} $(DESC_pentedb)
    !insertmacro MUI_DESCRIPTION_TEXT ${pentepro} $(DESC_pentePro)
    !insertmacro MUI_DESCRIPTION_TEXT ${SecCreateShortcut} $(DESC_shortcut)
  !insertmacro MUI_FUNCTION_DESCRIPTION_END

;--------------------------------
;Uninstaller Section

Section "Uninstall"

  RMDir /r /REBOOTOK "$INSTDIR\db"

  Delete /REBOOTOK "$INSTDIR\pentedb.exe"
  Delete /REBOOTOK "$INSTDIR\log4j-1.2.8.jar"
  Delete /REBOOTOK "$INSTDIR\derby.jar"
  Delete /REBOOTOK "$INSTDIR\pentedb.jar"
  Delete /REBOOTOK "$INSTDIR\swingx-0.9.1.jar"
  Delete /REBOOTOK "$INSTDIR\splash.png"
  Delete /REBOOTOK "$INSTDIR\sample.sgf"
  Delete /REBOOTOK "$INSTDIR\Ai.dll"
  Delete /REBOOTOK "$INSTDIR\x64\Ai.dll"
  Delete /REBOOTOK "$INSTDIR\pente.scs"
  Delete /REBOOTOK "$INSTDIR\pente.tbl"
  Delete /REBOOTOK "$INSTDIR\mmai.sgf"
  Delete /REBOOTOK "$INSTDIR\Uninstall.exe"
  Delete /REBOOTOK "$INSTDIR\derby.log"
  Delete /REBOOTOK "$INSTDIR\err.log"
  Delete /REBOOTOK "$INSTDIR\out.log"
  Delete /REBOOTOK "$INSTDIR\log4j.properties"

  RMDir "$INSTDIR"

  Delete "$SMPROGRAMS\Pente Db\*"
  RMDir "$SMPROGRAMS\Pente Db"
  
  DeleteRegKey /ifempty HKCU "Software\pentedb"
  DeleteRegKey /ifempty HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\pentedb"
  DeleteRegKey /ifempty HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\pentedb"
  DeleteRegKey /ifempty HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\pentedb"
  DeleteRegKey /ifempty HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\pentedb"
  
SectionEnd


Function .onInit
 
  ;Extract InstallOptions INI Files
  !insertmacro INSTALLOPTIONS_EXTRACT "jre.ini"

FunctionEnd
 

 
Function CheckInstalledJRE
  ;MessageBox MB_OK "Checking Installed JRE Version"
  Push "${JRE_VERSION}"
  Call DetectJRE
  ;Messagebox MB_OK "Done checking JRE version"
  ;Exch $0	; Get return value from stack
  Pop $0
  Pop $JRE64bit
  ;Messagebox MB_OK "0=$0 64bit=$JRE64bit"
  StrCmp $0 "0" NoFound
  StrCmp $0 "-1" FoundOld
  Goto JREAlreadyInstalled
  
FoundOld:
;  MessageBox MB_OK "Old JRE found"
  !insertmacro INSTALLOPTIONS_WRITE "jre.ini" "Field 1" "Text" "Pente Db requires a more recent version of the Java Runtime Environment\r\nthan the one found on your computer.\r\n\r\nPlease install JRE v${JRE_VERSION} or \r\ndownload the Pente Db installer that comes with a JRE."
  !insertmacro MUI_HEADER_TEXT "$(TEXT_JRE_TITLE)" "$(TEXT_JRE_SUBTITLE)"
  !insertmacro INSTALLOPTIONS_DISPLAY_RETURN "jre.ini"
  Goto MustInstallJRE
 
NoFound:
;  MessageBox MB_OK "JRE not found"
  !insertmacro INSTALLOPTIONS_WRITE "jre.ini" "Field 1" "Text" "No Java Runtime Environment could be found on your computer.\r\n\r\nPlease install JRE v${JRE_VERSION} or \r\ndownload the Pente Db installer that comes with a JRE."
  !insertmacro MUI_HEADER_TEXT "$(TEXT_JRE_TITLE)" "$(TEXT_JRE_SUBTITLE)"
  !insertmacro INSTALLOPTIONS_DISPLAY_RETURN "jre.ini"
  Goto MustInstallJRE
 
MustInstallJRE:
  ;Exch $0	; $0 now has the installoptions page return value
  ; Do something with return value here
  ;Pop $0	; Restore $0
  StrCpy $InstallJRE "yes"
  Return
  
JREAlreadyInstalled:
;  MessageBox MB_OK "No download: ${TEMP2}"
  ;MessageBox MB_OK "JRE already installed"
  StrCpy $InstallJRE "no"
  StrCpy $JREPath $0
 ; MessageBox MB_OK "Found: $JREPath"
  !insertmacro INSTALLOPTIONS_WRITE "jre.ini" "UserDefinedSection" "JREPath" $JREPath
  Return
 
FunctionEnd
 
; Returns: 0 - JRE not found. -1 - JRE found but too old. Otherwise - Path to JAVA EXE
 
; DetectJRE. Version requested is on the stack.
; Returns (on stack)	"0" on failure (java too old or not installed), otherwise path to java interpreter
; Stack value will be overwritten!
 
 
Function DetectJRE
  ;MessageBox MB_OK "Check 64bit jre"
  SetRegView 64
  Pop $9 	; version requested
  Push "64"
  Push $9
  Call DetectJRECore
  Exch
  Pop $0	; Get return value from stack
  Pop $1	; returned 64bit
  ;MessageBox MB_OK "DetectJRE 64 returned $0 $1"
  StrCmp $0 "0" NoFound
  StrCmp $0 "-1" NoFound
  Goto DetectJREEnd
 
NoFound:
  ;MessageBox MB_OK "Check 32"
  Push "32"
  Push $9
  SetRegView 32
  Call DetectJRECore
  Exch
  Pop $0	; Get return value from stack
  Pop $1	; returned 64bit
  ;MessageBox MB_OK "DetectJRE 32 returned $0 $1"
  
DetectJREEnd:
  Push $1
  Push $0
  Return
  
FunctionEnd
 
 
Function DetectJRECore

  Pop $0	; Get version requested  
  Pop $5	; Get 32 or 64 bit requested
  ;MessageBox MB_OK "asked for $0 $5"
 
  ;MessageBox MB_OK "Detecting JRE"
  ReadRegStr $1 HKLM "SOFTWARE\JavaSoft\Java Runtime Environment" "CurrentVersion"
  MessageBox MB_OK "Read : $1"
  StrCmp $1 "" DetectTry2
  ReadRegStr $2 HKLM "SOFTWARE\JavaSoft\Java Runtime Environment\$1" "JavaHome"
  ;MessageBox MB_OK "Read 3: $2"
  StrCmp $2 "" DetectTry2
  Goto GetJRE
 
DetectTry2:
  ReadRegStr $1 HKLM "SOFTWARE\JavaSoft\Java Development Kit" "CurrentVersion"
  ;MessageBox MB_OK "Detect Read : $1"
  StrCmp $1 "" NoFound
  ReadRegStr $2 HKLM "SOFTWARE\JavaSoft\Java Development Kit\$1" "JavaHome"
  ;MessageBox MB_OK "Detect Read 3: $2"
  StrCmp $2 "" NoFound
 
GetJRE:
; $0 = version requested. $1 = version found. $2 = javaHome
;MessageBox MB_OK "Getting JRE"
  IfFileExists "$2\bin\java.exe" 0 NoFound
  StrCpy $3 $0 1			; Get major version. Example: $1 = 1.5.0, now $3 = 1
  StrCpy $4 $1 1			; $3 = major version requested, $4 = major version found
  ;MessageBox MB_OK "Want $3 , found $4"
  IntCmp $4 $3 0 FoundOld FoundNew
  StrCpy $3 $0 1 2
  StrCpy $4 $1 1 2			; Same as above. $3 is minor version requested, $4 is minor version installed
  ;MessageBox MB_OK "Want $3 , found $4" 
  IntCmp $4 $3 FoundNew FoundOld FoundNew
 
NoFound:
  ;MessageBox MB_OK "JRE not found"
  Push "0"
  
  Goto DetectJRECoreEnd
 
FoundOld:
  ;MessageBox MB_OK "JRE too old: $3 is older than $4"
;  Push ${TEMP2}
  Push "-1"
  Goto DetectJRECoreEnd  
FoundNew:
  ;MessageBox MB_OK "JRE is new: $3 is newer than $4"
 
  Push "$2\bin\javaw.exe"
;  Push "OK"
;  Return
   Goto DetectJRECoreEnd
DetectJRECoreEnd:
	Push $5
FunctionEnd
 

Function LaunchLink
  Exec "$INSTDIR\pentedb.exe"
FunctionEnd