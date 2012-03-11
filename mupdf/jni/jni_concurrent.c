#include "jmupdf.h"

/* ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 * This program implements the fz_lock()/fz_unlock()
 * call-backs necessary to make concurrent page processing
 * within a document possible. This is accomplished by
 * setting up the fz_locks_context that has been added to
 * jni_document_s. Each document has its own lock structure.
 *
 * I am using java's MoniterEnter() / MonitorExit() sync
 * methods in order to stay away from additional
 * dependencies and remain portable.
 *
 * A unique lock object is created for each opened document
 * this way each document handles locks within itself. This
 * lets us process multiple documents concurrently as well.
 *
 * TODO: Stress test. This needs to be heavily tested before
 *       it can be released for production.
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ */

static JavaVM *jvm;

/**
 * Get "env" for current thread
 */
static JNIEnv * jni_get_env()
{
	JNIEnv *env;
	(*jvm)->AttachCurrentThread(jvm, (void **)&env, NULL);
	return env;
}

/**
 * Enter critical section
 */
static void jni_lock(void *user, int lock)
{
	JNIEnv *env = jni_get_env();
	jobject obj = (jobject)user;
	(*env)->MonitorEnter(env, obj);
}

/**
 * Exit critical section
 */
static void jni_unlock(void *user, int lock)
{
	JNIEnv *env = jni_get_env();
	jobject obj = (jobject)user;
	(*env)->MonitorExit(env, obj);
}

/**
 * Create new lock object
 */
static void * jni_new_lock_obj()
{
	JNIEnv *env = jni_get_env();
	jclass lock = NULL;
	jclass localRef = (*env)->FindClass(env, "java/lang/Boolean");
	lock = (*env)->NewGlobalRef(env, localRef);
	(*env)->DeleteLocalRef(env, localRef);
	return lock;
}

/**
 * Configure fz_locks_context
 */
void jni_new_locks(jni_document *doc)
{
	doc->locks.user = jni_new_lock_obj();
	doc->locks.lock = jni_lock;
	doc->locks.unlock = jni_unlock;
	doc->ctx->locks = &doc->locks;
}

/**
 * Free lock object
 */
void jni_free_locks(void *lock)
{
	JNIEnv *env = jni_get_env();
	jobject obj = (jobject)lock;
	(*env)->DeleteGlobalRef(env, obj);
}

/**
 * Cache virtual machine pointer
 */
JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *pvt )
{
	jvm = vm;
	return JNI_VERSION_1_4;
}
