LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_CFLAGS := -DNDEBUG -O2 -Wall
LOCAL_LDLIBS := -llog
LOCAL_MODULE    := TextureLoader
LOCAL_SRC_FILES := textureloader.cpp

include $(BUILD_SHARED_LIBRARY)
