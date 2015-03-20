
 
 !define SRC_DIR "C:\projects\pente_src"
  !include "InstallOptions.nsh"
; Java Launcher
;--------------
 
;You want to change the next four lines
Name "Pente Db"
Caption "Pente Db"
Icon "${SRC_DIR}\images\logo.ico"
OutFile "pentedb.exe"
 
SilentInstall silent
AutoCloseWindow true
ShowInstDetails nevershow
 
;You want to change the next two lines too
!define CLASSPATH ".;pentedb.jar;log4j-1.2.8.jar;derby.jar;swingx-0.9.1.jar"
!define CLASS "org.pente.gameDatabase.swing.Main"
!define JRE_VERSION "1.6.0"
 
Section ""
  Push "${JRE_VERSION}"
  Call GetJRE
  Pop $R0 ; either java home or 0 or -1 for not compatible jre
  Pop $R1 ; 32 or 64 bit
 
  StrCmp $R0 "0" NoFound
  StrCmp $R0 "-1" FoundOld
  Goto Found
  
FoundOld:
  ;MessageBox MB_OK "Update your java runtime to be at least 1.6"
  Goto AbortLauncher
NoFound:
  ;MessageBox MB_OK "No java runtime found, install at least java 1.6"
  Goto AbortLauncher
Found:
  ${If} $R1 == "64"
	StrCpy $R1 "x64"
  ${Else}
    StrCpy $R1 "."
  ${EndIf}
  StrCpy $0 '"$R0" -classpath "${CLASSPATH}" -Djava.library.path=$R1 -Dderby.storage.pageCacheSize=20000 -Xmx200M -Dhost=pente.org -splash:splash.png ${CLASS} log4j.properties db log'
  ;MessageBox MB_OK "$0"
 
  SetOutPath $EXEDIR
  Exec $0
AbortLauncher:
SectionEnd
 
 
Function GetJRE
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
  ;MessageBox MB_OK "Read : $1"
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
  Push "-1"
  Goto DetectJRECoreEnd  
FoundNew:
  ;MessageBox MB_OK "JRE is new: $3 is newer than $4"
 
  Push "$2\bin\javaw.exe"

   Goto DetectJRECoreEnd
DetectJRECoreEnd:

	Push $5
FunctionEnd