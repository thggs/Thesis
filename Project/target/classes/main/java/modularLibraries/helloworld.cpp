#include <iostream>
#include <jni.h>
#include "modularLibraries_ModularLibrary.h"

void sayHello(){
    std::cout << "Hello from C++ !!" << std::endl;
}

JNIEXPORT void Java_modularLibraries_ModularLibrary_helloWorld
    (JNIEnv* env, jobject thisObject) 
    {
       sayHello(); 
    }