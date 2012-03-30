#include "jmupdf.h"
#include "pthread.h"

/* ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 * This program implements the fz_lock()/fz_unlock()
 * call-backs necessary to make concurrent page processing
 * within a document possible. This is accomplished by
 * setting up the fz_locks_context that has been added to
 * jni_document_s. Each document has its own lock structure.
 *
 * A unique lock object is created for each opened document
 * this way each document handles locks within itself. This
 * lets us process multiple documents concurrently as well.
 *
 * =====================
 * Compiling for Windows
 * =====================
 * Make sure to have PTHREAD-W32 library and dll's.
 * I have placed the required DLL's in thirdpart/pthread/win32.
 * If you have MINGW just install the openMP feature. If not you
 * will have to download the binaries.
 *
 * =====================
 * Compiling for Linux
 * =====================
 * It just works!
 *
 * =====================
 * Final Notes
 * =====================
 * This is not 100% portable but at least it will work under
 * Windows, Linux and Mac.
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ */

typedef struct jni_locks_s jni_locks;
struct jni_locks_s
{
	pthread_mutex_t * lock;
};

enum
{
	JNI_LOCK_INTERNAL = FZ_LOCK_MAX,
	JNI_MAX_LOCKS
};

/**
 * Enter critical section
 */
static void jni_lock_internal(void *user, int lock)
{
	if (user)
	{
		jni_locks *locks = (jni_locks*)user;
		if (locks[lock].lock)
		{
			pthread_mutex_lock(locks[lock].lock);
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
		if (locks[lock].lock)
		{
			pthread_mutex_unlock(locks[lock].lock);
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
		int i = 0;
		for (i = 0; i < JNI_MAX_LOCKS; i++)
		{
			obj[i].lock = malloc(sizeof(pthread_mutex_t));
			pthread_mutex_init(obj[i].lock, NULL);
		}
		return obj;
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
		int i = 0;
		for (i = 0; i < JNI_MAX_LOCKS; i++)
		{
			if (obj[i].lock)
			{
				pthread_mutex_destroy(obj[i].lock);
				free(obj[i].lock);
			}
		}
		free(obj);
		free(locks);
	}
}
