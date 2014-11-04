LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)
LOCAL_SRC_FILES:=  gameConverseByJiec.cpp \
bmptest.c

LOCAL_MODULE := game
LOCAL_MODULE_TAGS := optional
LOCAL_CFLAGS := -DANDROID -DTHUMB -g
#LOCAL_C_INCLUDES := 
LOCAL_LDLIBS    := -llog
LOCAL_WHOLE_STATIC_LIBRARIES := luajit_static \
								mp4v2_static \
TARGET_ARCH_ABI := armeabi

LOCAL_SHARED_LIBRARIES := \
	syslib_gui \

LOCAL_EXPORT_C_INCLUDES := $(LOCAL_PATH)/../luajit/include \

LOCAL_C_INCLUDES := $(LOCAL_PATH)/ \
                    $(LOCAL_PATH)/../luajit/include \
					$(LOCAL_PATH)/../syslib/include \


include $(BUILD_EXECUTABLE)

$(call import-module,luajit)
$(call import-module,syslib)
$(call import-module,mp4v2)
