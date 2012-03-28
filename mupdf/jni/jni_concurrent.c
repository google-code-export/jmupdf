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
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ */

static JavaVM *jvm;

typedef struct jni_locks_s jni_locks;
struct jni_locks_s
{
	jobject lock;
};

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
		if (!locks[lock].lock)
		{
			return;
		}
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
		if (!locks[lock].lock)
		{
			return;
		}
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
	jni_locks *obj = malloc(sizeof(jni_locks) * JNI_MAX_LOCKS);
	if (obj)
	{
		JNIEnv *env = jni_get_env();
		jclass cls = (*env)->FindClass(env, "java/lang/String");
		if (cls)
		{
			jmethodID mid = (*env)->GetMethodID(env, cls, "<init>", "()V");
			int i = 0;
			for (i = 0; i < JNI_MAX_LOCKS; i++)
			{
				jobject new_obj = (*env)->NewObject(env, cls, mid);
				if (new_obj)
				{
					obj[i].lock = (*env)->NewGlobalRef(env, new_obj);
					(*env)->DeleteLocalRef(env, new_obj);
				}
				else
				{
					obj[i].lock = NULL;
				}
			}
			(*env)->DeleteLocalRef(env, cls);
			return obj;
		}
	}
	return NULL;
}

/**
 * Configure fz_locks_context
 */
fz_locks_context * jni_new_locks()
{
	fz_locks_context *locks = malloc(sizeof(fz_locks_context));

	if (!locks)
	{
		return NULL;
	}

	locks->user = jni_new_lock_obj();
	locks->lock = jni_lock_internal;
	locks->unlock = jni_unlock_internal;

	if (!locks->user)
	{
		free(locks);
		return NULL;
	}

	return locks;
}

/**
 * Free lock object
 */
void jni_free_locks(fz_locks_context *locks)
{
	if (locks->user)
	{
		jni_locks *obj = (jni_locks*)locks->user;
		JNIEnv *env = jni_get_env();
		int i = 0;
		for (i = 0; i < JNI_MAX_LOCKS; i++)
		{
			if (obj[i].lock)
			{
				(*env)->DeleteGlobalRef(env, obj[i].lock);
			}
		}
		free(obj);
		free(locks);
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
