@echo off

set DIR=%~dp0
set APP_ROOT=%DIR%..

echo 1. ��ʼ�������
rem call D:\android-ndk-r9d\ndk-build.cmd NDK_DEBUG=1  -C . NDK_MODULE_PATH=%APP_ROOT%
call D:\android-ndk-r9d\ndk-build.cmd  -C . NDK_MODULE_PATH=%APP_ROOT%
pause

echo 2. ���ͳ���ģ�������ֻ���
adb push %APP_ROOT%\libs\armeabi\game /sdcard/src/
pause