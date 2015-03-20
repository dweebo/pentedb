
#include "AiWrapper.h"
#include "Ai.h"

/*
 * Class:     org_pente_mmai_Ai
 * Method:    init
 * Signature: ()V
 */
JNIEXPORT jlong JNICALL Java_org_pente_mmai_Ai_init
  (JNIEnv *env, jobject o)
{
    CAi *cai = new CAi();
    return ((jlong)cai);
}

/*
 * Class:     org_pente_mmai_Ai
 * Method:    destroy
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_org_pente_mmai_Ai_privateDestroy(JNIEnv *env, jobject o, jlong ptr)
{
	CAi* cai=(CAi*)ptr;
    cai->CAi::~CAi();
    printf("c destroyed\n");
}

/*
 * Class:     org_pente_mmai_Ai
 * Method:    stop
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_org_pente_mmai_Ai_stop(JNIEnv *env, jobject o, jlong ptr)
{
   CAi* cai=(CAi*)ptr;
   cai->stopped = 1;
}

/*
 * Class:     org_pente_mmai_Ai
 * Method:    stop
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_org_pente_mmai_Ai_toggleCallbacks(JNIEnv *env, jobject o, jlong ptr, jint callbacks)
{
   CAi* cai=(CAi*)ptr;
   cai->callbacks = callbacks;
}

/*
 * Class:     org_pente_mmai_Ai
 * Method:    stop
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_org_pente_mmai_Ai_start(JNIEnv *env, jobject o, jlong ptr)
{
   CAi* cai=(CAi*)ptr;
   cai->stopped = 0;
}

/*
 * Class:     org_pente_mmai_Ai
 * Method:    move
 * Signature: ([I)I
 */
JNIEXPORT jint JNICALL Java_org_pente_mmai_Ai_move(JNIEnv *env, jobject obj, 
	jlong ptr, jintArray movesArr, jint game, jint level, jint vct)
{
    CAi* cai=(CAi*)ptr;
    if (cai->stopped) return -1;

    jclass cls = (*env).GetObjectClass(obj);
    jmethodID mid = 
         (*env).GetMethodID(cls, "aiEvaluatedCallBack", "()V");
    if (mid == NULL) {
        printf("method 1 not found\n");
        return -1; /* method not found */
    }
    jmethodID mid2 = 
         (*env).GetMethodID(cls, "aiVisualizationCallBack", "([I)V");
    if (mid2 == NULL) {
        printf("method 2 not found\n");
        return -1; /* method not found */
    }
    
    if (game == 3) {
        cai->Kgame = 1;
    }
    else {
        cai->Kgame = 0;
    }

    jsize numMoves = (*env).GetArrayLength(movesArr);
    jint *movesp = (*env).GetIntArrayElements(movesArr, 0);
    cai->Move(env, obj, mid, mid2, numMoves, movesp, level, vct);
    
    (*env).ReleaseIntArrayElements(movesArr, movesp, 0);
    
    if (cai->stopped) return -1;
    else return cai->bmove;
}
