@ECHO OFF
rem Windows batch file to build bluej on ajp's system
rem -------------------------------------------------
rem note: if not using jdk1.3, jpda.jar needs to be added

rem set JDK=F:\programming\jdk1.2
rem set JPDA=;%JDK%\lib\jpda.jar

rem set JDK=D:\java\jdk1.3
rem set JPDA=

rem set BLUEJ=d:\java\bluej_20010101\bluej

set JIKES_PATH=%BLUEJ%\classes;%BLUEJ%\lib\antlr.jar;%JDK%\jre\lib\rt.jar;%JDK%\jre\lib\i18n.jar;%JDK%\lib\dt.jar;%JDK%\lib\tools.jar%JPDA%
set JIKES_OPTS=-nowarn -depend +P +F +E -g -Xstdout


%JIKES% -classpath %JIKES_PATH% -d %BLUEJ%\lib\inspector %JIKES_OPTS%  %BLUEJ%\inspector\*.java

move /Y "%BLUEJ%\lib\inspector\MonashImageInspector*.class" "%BLUEJ%\examples\imageviewer\(Inspector)"

pause
