@Echo off
@cd /d %~dp0
@chdir ..
copy /b ..\S0101_856\IN_EDI_BK\*.txt ..\MWTMMCFinder\merged.txt
echo. 2>.\code\finished.txt
exit