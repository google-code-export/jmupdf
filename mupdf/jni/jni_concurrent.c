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
	jni_locks *locks = (jni_locks*)user;
	int r =0;
	switch (lock) {
		case FZ_LOCK_FILE:
			r = (*env)->MonitorEnter(env, locks->lock_file);
			break;
		case FZ_LOCK_ALLOC:
			r = (*env)->MonitorEnter(env, locks->lock_alloc);
			break;
		case FZ_LOCK_FREETYPE:
			r = (*env)->MonitorEnter(env, locks->lock_freetype);
			break;
		case FZ_LOCK_GLYPHCACHE:
			r = (*env)->MonitorEnter(env, locks->lock_glyphcache);
			break;
		default:
			r = (*env)->MonitorEnter(env, locks->lock_other);
			break;
	}
	if (r != JNI_OK)
	{
		// TODO: error handling here
	}
}

/**
 * Exit critical section
 */
static void jni_unlock(void *user, int lock)
{
	JNIEnv *env = jni_get_env();
	jni_locks *locks = (jni_locks*)user;
	int r = 0;
	switch (lock) {
		case FZ_LOCK_FILE:
			r = (*env)->MonitorExit(env, locks->lock_file);
			break;
		case FZ_LOCK_ALLOC:
			r = (*env)->MonitorExit(env, locks->lock_alloc);
			break;
		case FZ_LOCK_FREETYPE:
			r = (*env)->MonitorExit(env, locks->lock_freetype);
			break;
		case FZ_LOCK_GLYPHCACHE:
			r = (*env)->MonitorExit(env, locks->lock_glyphcache);
			break;
		default:
			r = (*env)->MonitorExit(env, locks->lock_other);
			break;
	}
	if (r != JNI_OK)
	{
		// TODO: error handling here
	}
}

/**
 * Create new lock object
 */
static void * jni_new_lock_obj(fz_context *ctx)
{
	jni_locks *locks = fz_malloc_no_throw(ctx, sizeof(jni_locks));

	if (!locks)
	{
		return NULL;
	}

	JNIEnv *env = jni_get_env();

	jclass l1 = (*env)->FindClass(env, "java/lang/Boolean");
	jclass l2 = (*env)->FindClass(env, "java/lang/Boolean");
	jclass l3 = (*env)->FindClass(env, "java/lang/Boolean");
	jclass l4 = (*env)->FindClass(env, "java/lang/Boolean");
	jclass l5 = (*env)->FindClass(env, "java/lang/Boolean");

	locks->lock_file = (*env)->NewGlobalRef(env, l1);
	locks->lock_alloc = (*env)->NewGlobalRef(env, l2);
	locks->lock_freetype = (*env)->NewGlobalRef(env, l3);
	locks->lock_glyphcache = (*env)->NewGlobalRef(env, l4);
	locks->lock_other = (*env)->NewGlobalRef(env, l5);

	(*env)->DeleteLocalRef(env, l1);
	(*env)->DeleteLocalRef(env, l2);
	(*env)->DeleteLocalRef(env, l3);
	(*env)->DeleteLocalRef(env, l4);
	(*env)->DeleteLocalRef(env, l5);

	return locks;
}

/**
 * Configure fz_locks_context
 */
void jni_new_locks(jni_document *doc)
{
	doc->locks.user = jni_new_lock_obj(doc->ctx);
	doc->locks.lock = jni_lock;
	doc->locks.unlock = jni_unlock;
	doc->ctx->locks = &doc->locks;
}

/**
 * Free lock object
 */
void jni_free_locks(void *locks)
{
	if (locks)
	{
		jni_locks *l = (jni_locks*)locks;
		JNIEnv *env = jni_get_env();

		jobject l1 = (jobject)l->lock_file;
		jobject l2 = (jobject)l->lock_alloc;
		jobject l3 = (jobject)l->lock_freetype;
		jobject l4 = (jobject)l->lock_glyphcache;
		jobject l5 = (jobject)l->lock_other;

		(*env)->DeleteGlobalRef(env, l1);
		(*env)->DeleteGlobalRef(env, l2);
		(*env)->DeleteGlobalRef(env, l3);
		(*env)->DeleteGlobalRef(env, l4);
		(*env)->DeleteGlobalRef(env, l5);
	}
}

/**
 * Cache virtual machine pointer
 */
JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *pvt )
{
	jvm = vm;
	return JNI_VERSION_1_4;
}
