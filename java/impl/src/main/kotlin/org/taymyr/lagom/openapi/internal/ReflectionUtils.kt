package org.taymyr.lagom.openapi.internal

import com.lightbend.lagom.internal.javadsl.api.MethodRefServiceCallHolder
import io.swagger.v3.core.converter.AnnotatedType
import io.swagger.v3.core.converter.ModelConverters
import io.swagger.v3.core.util.ReflectionUtils
import sun.misc.SharedSecrets
import java.lang.reflect.Method
import java.lang.reflect.Type

internal fun <A : Annotation> Class<*>.getAnnotationInherited(aCls: Class<out A>): A? = ReflectionUtils.getAnnotation(this, aCls)
internal fun <A : Annotation> Class<*>.getAnnotationsInherited(aCls: Class<out A>): List<A>? = ReflectionUtils.getRepeatableAnnotations(this, aCls)
internal fun Class<*>.isAnnotationPresentInherited(aCls: Class<out Annotation>) = this.getAnnotationInherited(aCls) != null

internal fun <A : Annotation> Method.getAnnotationInherited(aCls: Class<out A>): A? = ReflectionUtils.getAnnotation(this, aCls)
internal fun <A : Annotation> Method.getAnnotationsInherited(aCls: Class<out A>): List<A>? = ReflectionUtils.getRepeatableAnnotations(this, aCls)

internal fun ModelConverters.resolveAsResolvedSchema(cls: Type) = this.resolveAsResolvedSchema(AnnotatedType().type(cls))!!

internal fun MethodRefServiceCallHolder.toMethod(): Method {
    val pool = SharedSecrets.getJavaLangAccess().getConstantPool(this.methodReference().javaClass)
    for (i in 0 until pool.size) {
        try {
            val member = pool.getMethodAt(i)
            if (member is Method && !member.declaringClass.name.startsWith("java.lang")) {
                return member
            }
        } catch (t: Throwable) { }
    }
    throw IllegalArgumentException("Not a method reference")
}