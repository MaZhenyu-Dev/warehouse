@echo off
setlocal
cd /d "%~dp0"

echo [INFO] Start YC sync loop: every 300 seconds.
python sync_yc_to_feishu.py

echo [INFO] YC sync loop stopped.
pause
