LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_LDLIBS += -L$(SYSROOT)/usr/lib -llog
LOCAL_MODULE    := getPic

LOCAL_SRC_FILES := ReadFrameBuffer.c

include $(BUILD_SHARED_LIBRARY)

