package io.swagger.v3.lagom

import com.lightbend.lagom.internal.javadsl.api.MethodRefServiceCallHolder
import io.swagger.v3.core.converter.AnnotatedType
import io.swagger.v3.core.converter.ModelConverters
import io.swagger.v3.core.util.ReflectionUtils
import sun.misc.SharedSecrets
import java.lang.reflect.Method
import java.lang.reflect.Type

fun <A : Annotation> Class<*>.getAnnotationInherited(aCls: Class<out A>): A? = ReflectionUtils.getAnnotation(this, aCls)

fun Class<*>.isAnnotationPresentInherited(aCls: Class<out Annotation>) = this.getAnnotationInherited(aCls) != null

fun ModelConverters.resolveAsResolvedSchema(cls: Type) = this.resolveAsResolvedSchema(AnnotatedType().type(cls))!!

fun MethodRefServiceCallHolder.toMethod(): Method {
    val pool = SharedSecrets.getJavaLangAccess().getConstantPool(this.methodReference()!!.javaClass)
    val size = pool.size
    for (i in 1..size) {
        try {
            val member = pool.getMethodAt(i)
            if (member is Method && !member.declaringClass.name.startsWith("java.lang")) {
                return member
            }
        } catch (t: Throwable) { }
    }
    throw IllegalArgumentException("Not a method reference")
}