LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE := pagif
LOCAL_CFLAGS := -fvisibility=hidden -Ofast -finline-functions
LOCAL_LDLIBS := \
    -ljnigraphics \
    -landroid \

LOCAL_SRC_FILES := \
    bitmap.c \
    decoding.c \
    memset32_neon.S \
    drawing.c \
    memset.arm.S \
    metadata.c \
    time.c \
    open_close.c \
    surface.c \
    gif.c \
    control.c \
    exception.c \
    giflib/gifalloc.c \
    giflib/dgif_lib.c \

include $(BUILD_SHARED_LIBRARY)