LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

LOCAL_STATIC_JAVA_LIBRARIES := supportlib telephonylib

LOCAL_SRC_FILES := $(call all-java-files-under, src)

LOCAL_PACKAGE_NAME := AppMarket

include $(BUILD_PACKAGE)
##################################################

include $(CLEAR_VARS)
LOCAL_JAVA_LIBRARIES := general-java-lib

LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := supportlib:lib/android-support-v4.jar \
telephonylib:lib/telephony.jar

include $(BUILD_MULTI_PREBUILT)

# Use the folloing include to make our test apk.
include $(call all-makefiles-under,$(LOCAL_PATH))
