@file:JvmName("ReflectionUtils")

package org.taymyr.lagom.internal.openapi

import io.swagger.v3.core.util.ReflectionUtils
import java.lang.reflect.Method

fun <A : Annotation> Class<*>.getAnnotationInherited(aCls: Class<out A>): A? = ReflectionUtils.getAnnotation(this, aCls)
fun <A : Annotation> Class<*>.getAnnotationsInherited(aCls: Class<out A>): List<A>? = ReflectionUtils.getRepeatableAnnotations(this, aCls)
fun Class<*>.isAnnotationPresentInherited(aCls: Class<out Annotation>) = this.getAnnotationInherited(aCls) != null

fun <A : Annotation> Method.getAnnotationInherited(aCls: Class<out A>): A? = ReflectionUtils.getAnnotation(this, aCls)
fun <A : Annotation> Method.getAnnotationsInherited(aCls: Class<out A>): List<A>? = ReflectionUtils.getRepeatableAnnotations(this, aCls)
