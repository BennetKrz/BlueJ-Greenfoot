/*
 * #%L
 * **********************************************************************
 * ORGANIZATION  :  Pi4J
 * PROJECT       :  Pi4J :: JNI Native Library
 * FILENAME      :  com_pi4j_wiringpi_Gpio.h  
 * 
 * This file is part of the Pi4J project. More information about 
 * this project can be found here:  http://www.pi4j.com/
 * **********************************************************************
 * %%
 * Copyright (C) 2012 - 2015 Pi4J
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class com_pi4j_wiringpi_Gpio */

#ifndef _Included_com_pi4j_wiringpi_Gpio
#define _Included_com_pi4j_wiringpi_Gpio
#ifdef __cplusplus
extern "C" {
#endif
#undef com_pi4j_wiringpi_Gpio_NUM_PINS
#define com_pi4j_wiringpi_Gpio_NUM_PINS 20L
#undef com_pi4j_wiringpi_Gpio_INPUT
#define com_pi4j_wiringpi_Gpio_INPUT 0L
#undef com_pi4j_wiringpi_Gpio_OUTPUT
#define com_pi4j_wiringpi_Gpio_OUTPUT 1L
#undef com_pi4j_wiringpi_Gpio_PWM_OUTPUT
#define com_pi4j_wiringpi_Gpio_PWM_OUTPUT 2L
#undef com_pi4j_wiringpi_Gpio_LOW
#define com_pi4j_wiringpi_Gpio_LOW 0L
#undef com_pi4j_wiringpi_Gpio_HIGH
#define com_pi4j_wiringpi_Gpio_HIGH 1L
#undef com_pi4j_wiringpi_Gpio_PUD_OFF
#define com_pi4j_wiringpi_Gpio_PUD_OFF 0L
#undef com_pi4j_wiringpi_Gpio_PUD_DOWN
#define com_pi4j_wiringpi_Gpio_PUD_DOWN 1L
#undef com_pi4j_wiringpi_Gpio_PUD_UP
#define com_pi4j_wiringpi_Gpio_PUD_UP 2L
/*
 * Class:     com_pi4j_wiringpi_Gpio
 * Method:    wiringPiSetup
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_com_pi4j_wiringpi_Gpio_wiringPiSetup
  (JNIEnv *, jclass);

/*
 * Class:     com_pi4j_wiringpi_Gpio
 * Method:    wiringPiSetupSys
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_com_pi4j_wiringpi_Gpio_wiringPiSetupSys
  (JNIEnv *, jclass);

/*
 * Class:     com_pi4j_wiringpi_Gpio
 * Method:    wiringPiSetupGpio
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_com_pi4j_wiringpi_Gpio_wiringPiSetupGpio
  (JNIEnv *, jclass);

/*
 * Class:     com_pi4j_wiringpi_Gpio
 * Method:    pinMode
 * Signature: (II)V
 */
JNIEXPORT void JNICALL Java_com_pi4j_wiringpi_Gpio_pinMode
  (JNIEnv *, jclass, jint, jint);

/*
 * Class:     com_pi4j_wiringpi_Gpio
 * Method:    pullUpDnControl
 * Signature: (II)V
 */
JNIEXPORT void JNICALL Java_com_pi4j_wiringpi_Gpio_pullUpDnControl
  (JNIEnv *, jclass, jint, jint);

/*
 * Class:     com_pi4j_wiringpi_Gpio
 * Method:    digitalWrite
 * Signature: (II)V
 */
JNIEXPORT void JNICALL Java_com_pi4j_wiringpi_Gpio_digitalWrite
  (JNIEnv *, jclass, jint, jint);

/*
 * Class:     com_pi4j_wiringpi_Gpio
 * Method:    pwmWrite
 * Signature: (II)V
 */
JNIEXPORT void JNICALL Java_com_pi4j_wiringpi_Gpio_pwmWrite
  (JNIEnv *, jclass, jint, jint);

/*
 * Class:     com_pi4j_wiringpi_Gpio
 * Method:    digitalRead
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_com_pi4j_wiringpi_Gpio_digitalRead
  (JNIEnv *, jclass, jint);

/*
 * Class:     com_pi4j_wiringpi_Gpio
 * Method:    delay
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_com_pi4j_wiringpi_Gpio_delay
  (JNIEnv *, jclass, jlong);

/*
 * Class:     com_pi4j_wiringpi_Gpio
 * Method:    millis
 * Signature: ()J
 */
JNIEXPORT jlong JNICALL Java_com_pi4j_wiringpi_Gpio_millis
  (JNIEnv *, jclass);

/*
 * Class:     com_pi4j_wiringpi_Gpio
 * Method:    delayMicroseconds
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_com_pi4j_wiringpi_Gpio_delayMicroseconds
  (JNIEnv *, jclass, jlong);

/*
 * Class:     com_pi4j_wiringpi_Gpio
 * Method:    piHiPri
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_com_pi4j_wiringpi_Gpio_piHiPri
  (JNIEnv *, jclass, jint);

/*
 * Class:     com_pi4j_wiringpi_Gpio
 * Method:    waitForInterrupt
 * Signature: (II)I
 */
JNIEXPORT jint JNICALL Java_com_pi4j_wiringpi_Gpio_waitForInterrupt
  (JNIEnv *, jclass, jint, jint);

/*
 * Class:     com_pi4j_wiringpi_Gpio
 * Method:    piBoardRev
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_com_pi4j_wiringpi_Gpio_piBoardRev
  (JNIEnv *, jclass);

/*
 * Class:     com_pi4j_wiringpi_Gpio
 * Method:    wpiPinToGpio
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_com_pi4j_wiringpi_Gpio_wpiPinToGpio
  (JNIEnv *, jclass, jint);

#ifdef __cplusplus
}
#endif
#endif
