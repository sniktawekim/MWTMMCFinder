@Echo off
@cd /d %~dp0
@chdir ..
copy /b ..\S0101_810\IN_EDI_BK\*.txt ..\MWTMMCFinder\merged.txt
echo. 2>.\code\finished.txt
exit