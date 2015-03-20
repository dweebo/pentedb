#pentedb

A powerful game analyzer, database and AI for the board game Pente.

It's functional and mostly complete but could use some work.

Inspired from my work on [Pente.org](http://pente.org)

## Building instructions

### Prerequisites
1. Install Java 6 or above. 
2. Install ant version 1.7
3. If you need to update the windows installers, install NSIS version 2.46
4. If you need to update the C++ AI code and you are on windows, install mingw-w64

Note: If you want to compile for a 64-bit system then choose the x86_64 installation
      If you want to compile for a 32-bit system then choose the i686 installation

### Build java
Run "ant" from command line in the build directory
Output will be a file db2.jar in the build directory

### Build C++ AI code
1. Run mingw-w64.bat
2. Go to the mmai or mmai64 directory
3. For 64bit dll run
g++ -m64 -D_JNI_IMPLEMENTATION_ -static -Wl,--kill-at -I"C:\<path to your jdk>\include" -I"C:\<path to your jdk>\include\win32" -shared CPoint.c Ai.cpp AiWrapper.cpp -o Ai.dll
Output is the file Ai.dll
4. For 32bit dll run
g++ -m32 -D_JNI_IMPLEMENTATION_ -static -Wl,--kill-at -I"C:\<path to your jdk>\include" -I"C:\<path to your jdk>\include\win32" -shared CPoint.c Ai.cpp AiWrapper.cpp -o Ai.dll
Output is the file Ai.dll

### Build windows installers
1. Edit the nsi files and change this line to point to the root of your pentedb source files
!define SRC_DIR "C:\projects\pentedb_src"

2. Run the NSIS program to compile the executables

Output is pentedb.exe and pentedb-install.exe

### Tests

Yeah...those would be nice to have


## TODO
- Find my old notes and add things to the TODO list ;)
- Automate the building process more
- Switch from ant to maven?
- Package for linux, OSX