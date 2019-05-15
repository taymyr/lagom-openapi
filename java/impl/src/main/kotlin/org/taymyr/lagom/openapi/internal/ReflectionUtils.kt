package org.taymyr.lagom.openapi.internal

import com.lightbend.lagom.internal.javadsl.api.MethodRefResolver
import com.lightbend.lagom.internal.javadsl.api.MethodRefServiceCallHolder
import io.swagger.v3.core.converter.AnnotatedType
import io.swagger.v3.core.converter.ModelConverters
import io.swagger.v3.core.util.ReflectionUtils
import java.lang.reflect.Method
import java.lang.reflect.Type

internal fun <A : Annotation> Class<*>.getAnnotationInherited(aCls: Class<out A>): A? = ReflectionUtils.getAnnotation(this, aCls)
internal fun <A : Annotation> Class<*>.getAnnotationsInherited(aCls: Class<out A>): List<A>? = ReflectionUtils.getRepeatableAnnotations(this, aCls)
internal fun Class<*>.isAnnotationPresentInherited(aCls: Class<out Annotation>) = this.getAnnotationInherited(aCls) != null

internal fun <A : Annotation> Method.getAnnotationInherited(aCls: Class<out A>): A? = ReflectionUtils.getAnnotation(this, aCls)
internal fun <A : Annotation> Method.getAnnotationsInherited(aCls: Class<out A>): List<A>? = ReflectionUtils.getRepeatableAnnotations(this, aCls)

internal fun ModelConverters.resolveAsResolvedSchema(cls: Type) = this.resolveAsResolvedSchema(AnnotatedType().type(cls))!!

internal fun MethodRefServiceCallHolder.toMethod(): Method = when (val methodReference = this.methodReference()) {
    is Method -> methodReference
    else -> {
        try {
            MethodRefResolver.resolveMethodRef(methodReference)
        } catch (t: Throwable) {
            throw IllegalStateException(
                """Unable to resolve method for service call.
                Ensure that the you have passed a method reference (ie, this::someMethod). Passing anything else,
                for example lambdas, anonymous classes or actual implementation classes, is forbidden in declaring a
                service descriptor.""".trimIndent(), t
            )
        }
    }
}