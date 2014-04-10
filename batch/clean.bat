cd /d %~dp0
chdir ..
del "merged.txt" >logs\cleanlog.txt
del "finished.txt" >logs\cleanlog.txt
del "code\finished.txt" >logs\cleanlog.txt
exit