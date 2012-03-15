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

enum
{
	JNI_LOCK_INTERNAL = FZ_LOCK_MAX,
	JNI_MAX_LOCKS
};

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
static void jni_lock_internal(void *user, int lock)
{
	if (user)
	{
		jni_locks *locks = (jni_locks*)user;
		JNIEnv *env = jni_get_env();
		if ((*env)->MonitorEnter(env, locks[lock].lock) != JNI_OK)
		{
			fprintf(stderr, "JMuPDF: could not obtain a lock on object %i \n", lock);
		}
	}
}

/**
 * Exit critical section
 */
static void jni_unlock_internal(void *user, int lock)
{
	if (user)
	{
		jni_locks *locks = (jni_locks*)user;
		JNIEnv *env = jni_get_env();
		if ((*env)->MonitorExit(env, locks[lock].lock) != JNI_OK)
		{
			fprintf(stderr, "JMuPDF: could not exit lock on object %i \n", lock);
		}
	}
}

/**
 * Create new lock object
 */
static void * jni_new_lock_obj()
{
	jni_locks *locks = malloc(sizeof(jni_locks) * JNI_MAX_LOCKS);
	if (locks)
	{
		JNIEnv *env = jni_get_env();
		int i = 0;
		for (i = 0; i < JNI_MAX_LOCKS; i++)
		{
			jclass c = (*env)->FindClass(env, "java/lang/Boolean");
			locks[i].lock = (*env)->NewGlobalRef(env, c);
			(*env)->DeleteLocalRef(env, c);
		}
		return locks;
	}
	return NULL;
}

/**
 * Configure fz_locks_context
 */
void jni_new_locks(jni_document *doc)
{
	doc->locks.user = jni_new_lock_obj();
	doc->locks.lock = jni_lock_internal;
	doc->locks.unlock = jni_unlock_internal;
	doc->ctx->locks = &doc->locks;
}

/**
 * Free lock object
 */
void jni_free_locks(void *user)
{
	if (user)
	{
		jni_locks *locks = (jni_locks*)user;
		JNIEnv *env = jni_get_env();
		int i = 0;
		for (i = 0; i < JNI_MAX_LOCKS; i++)
		{
			(*env)->DeleteGlobalRef(env, locks[i].lock);
		}
		free(locks);
	}
}

/**
 * Enter critical section
 */
void jni_lock(fz_context *ctx)
{
	jni_lock_internal(ctx->locks->user, JNI_LOCK_INTERNAL);
}

/**
 * Exit critical section
 */
void jni_unlock(fz_context *ctx)
{
	jni_unlock_internal(ctx->locks->user, JNI_LOCK_INTERNAL);
}

/**
 * Cache virtual machine pointer
 */
JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *pvt )
{
	jvm = vm;
	return JNI_VERSION_1_4;
}
